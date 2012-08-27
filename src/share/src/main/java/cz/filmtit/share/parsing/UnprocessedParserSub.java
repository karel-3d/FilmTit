package cz.filmtit.share.parsing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.regexp.shared.*;

import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.ParsingException;

/**
 * Provides a simple parsing function for reading SUB subtitle format
 * into a "shallow" GUISubtitleList (with empty matches and translations).
 * 
 * TODO add parsing of text format modifiers like "{Y:i}"
 * 
 * @author Honza Václ and Karel Bílek
 *
 */
public class UnprocessedParserSub extends UnprocessedParser {
    
    /**
     * How is newline presented in SUB format.
     */
    public static final String SUBLINE_SEPARATOR_IN = "\\|";
	
    /**
     * Regexp extracting time info.
     * Note, that in SRT, the time info are actual times, in SUB, it's frames.
     */
	public static RegExp reSubtitleLine = RegExp.compile("^\\{([0-9]+)\\}\\{([0-9]+)\\}(.*)$");  // the "{}" are here as literals

    /**
     * Parses SUB text to unprocessed chunks.
     * @param text text in SUB format
     * @return list of unprocessed chunks.
     */
     public List<UnprocessedChunk> parseUnprocessed(String text)
            throws ParsingException {
		List<UnprocessedChunk> sublist = new ArrayList<UnprocessedChunk>();
		
		String[] lines = text.split(LINE_SEPARATOR);
		
		for (int linenumber = 0; linenumber < lines.length; linenumber++) {
			String line = lines[linenumber];

			if (reSubtitleLine.test(line)) {
				MatchResult matcher = reSubtitleLine.exec(line);
				// note: the 0th group is the whole line (the whole match)
				String startTime = matcher.getGroup(1);
				String endTime   = matcher.getGroup(2);
				String lineText  = matcher.getGroup(3);
				
                //this can probably be just regexp replace
				String[] segments = lineText.split(SUBLINE_SEPARATOR_IN);
				String titText = segments[0];
                for (int i = 1; i < segments.length; i++) {
					titText += LINE_SEPARATOR_OUT + segments[i];
				}

                sublist.add(new UnprocessedChunk(startTime, endTime, titText));
			}
			else {
				throw new ParsingException("Wrong format of the subtitle", linenumber + 1, false);
			}
		}

		return sublist;
	}
	
}