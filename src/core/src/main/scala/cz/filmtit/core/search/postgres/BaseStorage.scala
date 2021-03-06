/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.core.search.postgres

import org.apache.commons.logging.LogFactory
import cz.filmtit.core.model.storage.{MediaStorage, TranslationPairStorage}
import java.sql.{SQLException, Connection}
import gnu.trove.map.hash.TObjectLongHashMap
import scala.collection.JavaConversions._
import cz.filmtit.share.{Language, TranslationPair, MediaSource, TranslationSource}
import collection.mutable.{ListBuffer, HashSet}
import scala.collection.JavaConverters._
import cz.filmtit.core.model.TranslationMemory

/**
 * Base class for all translation pair storages using Postgres.
 *
 * @author Joachim Daiber
 */

abstract class BaseStorage(
  l1: Language,
  l2: Language,
  source: TranslationSource,
  connection: Connection,
  useInMemoryDB: Boolean = false
) extends TranslationPairStorage(l1, l2)
with MediaStorage {

  var tm: TranslationMemory = null

  val log = LogFactory.getLog(this.getClass.getSimpleName)

  //Load the driver:
  classOf[org.postgresql.Driver]

 
  var pairTable = "translationpairs"
  var chunkSourceMappingTable = "translationpairs_mediasources"
  var mediasourceTable = "mediasources"

  /**
   * In the case that we want to use a database that is not compatible with postgres,
   * these two variables have to be changed into the SQL-compliant datatype:
   */
  var serialDataType = "SERIAL"
  var textDataType = "TEXT"
  
  var maxCandidates = 250

  /**
   * Drop all tables from the database and recreate them.
   */
  def reset() {
    System.err.println("Resetting BaseStorage (chunks, mediasources).")

    connection.createStatement().execute(
      "DROP TABLE IF EXISTS %s CASCADE; DROP TABLE IF EXISTS %s CASCADE; DROP TABLE IF EXISTS %s CASCADE;"
        .format(pairTable, mediasourceTable, chunkSourceMappingTable))

    connection.createStatement().execute(
      "CREATE TABLE %s (source_id %s PRIMARY KEY, title %s, year VARCHAR(4), genres %s);"
        .format(mediasourceTable, serialDataType, textDataType, textDataType))


    connection.createStatement().execute(
      ("CREATE TABLE %s (pair_id %s PRIMARY KEY, chunk_l1 %s, chunk_l2 %s, pair_count INTEGER);")
        .format(pairTable, serialDataType, textDataType, textDataType))

    connection.createStatement().execute(
      ("CREATE TABLE %s (" +
        "pair_id INTEGER references %s(pair_id), " +
        "source_id INTEGER references %s(source_id)," +
        "PRIMARY KEY(pair_id, source_id)" +
        ");")
        .format(chunkSourceMappingTable, pairTable, mediasourceTable))

  }


  /**
   * The following two data structures are only used for indexing, hence
   * they are lazy and are not initialized in read-only mode.
   */
  private lazy val pairIDCache: TObjectLongHashMap[java.lang.String] = new TObjectLongHashMap[java.lang.String]()
  private lazy val pairMediaSourceMappings: HashSet[Pair[Long, Long]] = HashSet[Pair[Long, Long]]()

  private lazy val msInsertStmt = connection.prepareStatement("INSERT INTO %s(pair_id, source_id) VALUES(?, ?);".format(chunkSourceMappingTable))
  pairMediaSourceMappings.clear()
  /**
   * Add a media source <-> translation pair correspondence to
   * the database.
   *
   * @param pairID pair identifier that will be linked to the media source
   * @param mediaSourceID media source DB identifier
   */
  private def addMediaSourceForTP(pairID: Long, mediaSourceID: Long) =
    if ( !(pairMediaSourceMappings.contains(Pair(pairID, mediaSourceID))) ) {
      msInsertStmt.setLong(1, pairID)
      msInsertStmt.setLong(2, mediaSourceID)
      msInsertStmt.execute()
      pairMediaSourceMappings.add(Pair(pairID, mediaSourceID))
    }

  /**
   * Search for the translation pair in the database and return its
   * ID if it is present.
   *
   * @param translationPair the translation pair to be looked up
   * @return
   */
  private def pairIDInDatabase(translationPair: TranslationPair): Option[Long] = {
    val l: Long = pairIDCache.get("%s-%s".format(translationPair.getChunkL1, translationPair.getChunkL2))
    if (l == 0) {
      None
    }else{
      Some(l)
    }
  }

  def finishImport() {
    connection.createStatement().execute(
        (("CREATE INDEX count_idx ON %s(pair_count DESC);")
          .format(pairTable))
    )
  }

  /**
   * This is the only place where {TranslationPair}s are actually
   * added to the database. All subclasses of BaseStorage work with the
   * translation pairs that were added to the database by this method.
   *
   * @param translationPairs a Traversable of translation pairs
   */
  def addVerbose(translationPairs: TraversableOnce[TranslationPair], autoCommit: Boolean = false) {

    connection.setAutoCommit(autoCommit || useInMemoryDB)

    //postgres has RETURNING clause, useInMemoryDB doesn't have one
    val inStmt = if (useInMemoryDB) {

      connection.prepareStatement(("INSERT INTO %s (chunk_l1, chunk_l2, pair_count) VALUES (?, ?, 1);").format(pairTable))
    
    } else {
      connection.prepareStatement(("INSERT INTO %s (chunk_l1, chunk_l2, pair_count) VALUES (?, ?, 1) RETURNING pair_id;").format(pairTable))
    
    }

    val getInsertedStmt = connection.prepareStatement(("SELECT pair_id FROM %s WHERE chunk_l1=? AND chunk_l2=?;").format(pairTable))
    val upStmt = connection.prepareStatement(("UPDATE %s SET pair_count = pair_count + 1 WHERE pair_id = ?;").format(pairTable))

    //Important for performance: Only commit after all INSERT statements are
    //executed unless we are in verbose auto-commit mode:

    if (useInMemoryDB || autoCommit) {
      System.err.println("Re-writing media sources to database after failed commit...")
      translationPairs.map(_.getMediaSource).toList.filter(_ != null).distinct foreach( ms =>
          try {
            ms.setId(addMediaSource(ms))
          } catch {
            case e: SQLException =>
          }
      )
    }

    System.err.println("Writing translation pairs to database...")
    val addedPairs = ListBuffer[String]()
    translationPairs foreach { translationPair => {
      try {

        val pairID = pairIDInDatabase(translationPair) match {

          //Normal case: there is no equivalent translation pair in the database
          case None => {

            inStmt.setString(1, translationPair.getChunkL1.getSurfaceForm)
            inStmt.setString(2, translationPair.getChunkL2.getSurfaceForm)
            inStmt.execute()
            
            if (useInMemoryDB) {
              getInsertedStmt.setString(1, translationPair.getChunkL1.getSurfaceForm)
              getInsertedStmt.setString(2, translationPair.getChunkL2.getSurfaceForm)
              getInsertedStmt.execute()
            }

            //Get the pair_id of the new translation pair
            val resultStmt = if (useInMemoryDB) { getInsertedStmt } else { inStmt }
            
            val resultSet = resultStmt.getResultSet()
           
            resultSet.next()

            val newPairID = resultSet.getLong("pair_id")


            //Remember that we already put it into the database
            val pair: String = "%s-%s".format(translationPair.getChunkL1, translationPair.getChunkL2)
            pairIDCache.put(pair, newPairID)
            addedPairs.add(pair)
            newPairID
          }

          //It is already there
          case Some(existingPairID) => {
            //Special case: there is an equivalent translation pair in the database,
            upStmt.setLong(1, existingPairID)
            upStmt.execute()
            existingPairID
          }
        }

        //Add the MediaSource as the source to the TP
        if (translationPair.hasMediaSource) {
          addMediaSourceForTP(pairID, translationPair.getMediaSource.getId)
        }
      } catch {
        case e: SQLException => {
          //Since the was an error, we need to remove all the pairs in the
          //current transaction from the pair cache.

          e.printStackTrace()

          addedPairs.foreach( pair => pairIDCache.remove(pair) )

          if (!(autoCommit)) {
            System.err.println("Database error in current batch, switching to auto-commit mode.")
            addVerbose(translationPairs, autoCommit=true)
            return;
          } else {
            System.err.println("Single insert failed, skipping it..." + translationPair)
            e.printStackTrace()
          }

        }
      }
    }
  }

    System.err.println("Clearing pair<->MS set.")
    pairMediaSourceMappings.clear()


    if ( !autoCommit )
      connection.commit()

    }

 /**
   * This is the only place where {TranslationPair}s are actually
   * added to the database. All subclasses of BaseStorage work with the
   * translation pairs that were added to the database by this method.
   *
   * @param translationPairs a Traversable of translation pairs
   */
  def add(translationPairs: TraversableOnce[TranslationPair]) {
    addVerbose(translationPairs)
  }



  /**
    * Get a media source by its database identifier.
    * @param id media source identifier
    * @return
    */
  def getMediaSource(id: Int): MediaSource = {
    val stmt = connection.prepareStatement("SELECT * FROM %s where source_id = ? LIMIT 1;".format(mediasourceTable))
    stmt.setLong(1, id)
    stmt.execute()
    stmt.getResultSet.next()

    new MediaSource(
      stmt.getResultSet.getString("title"),
      stmt.getResultSet.getString("year"),
      stmt.getResultSet.getString("genres")
    )
  }


  /**
   * Add a media source to the database.
   * @param mediaSource filled media source object
   * @return database identifier of the media source
   */
  def addMediaSource(mediaSource: MediaSource): Long = {
    val stmt = connection.prepareStatement("INSERT INTO %s(title, year, genres) VALUES(?, ?, ?) RETURNING source_id;".format(mediasourceTable))

    stmt.setString(1, mediaSource.getTitle)
    stmt.setString(2, mediaSource.getYear)
    stmt.setString(3, mediaSource.getGenres mkString ",")
    stmt.execute()

    stmt.getResultSet.next()
    stmt.getResultSet.getLong("source_id")
  }

  override def close() {
    connection.close()
  }
}

