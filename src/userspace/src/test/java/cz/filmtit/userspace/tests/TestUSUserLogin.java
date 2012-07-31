package cz.filmtit.userspace.tests;

import cz.filmtit.core.Configuration;
import cz.filmtit.core.ConfigurationSingleton;
import cz.filmtit.userspace.FilmTitBackendServer;
import cz.filmtit.userspace.HibernateUtil;
import cz.filmtit.userspace.USUser;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestUSUserLogin {

    private static String name = "DefaultUser";
    private static String pass = "filmtit";
    private static String email = "filmtit@gmail.com";
    private static String newPass = "filmtit2012";


    @BeforeClass
    public static void InitializeDatabase() {
        DatabaseUtil.setDatabase();
        ConfigurationSingleton.setConf(new Configuration("configuration.xml"));
    }
    FilmTitBackendServer server = null;
     void TestUSUserLogin()
     {
        CreateServer();
     }

    void CreateServer()
    {
        server = new MockFilmTitBackendServer();
    }

    @Test
    public void testRegistration() {


     if (server ==null)  {CreateServer();};


       server.registration(name, pass, email, null);

       Session dbSession = HibernateUtil.getSessionWithActiveTransaction();
       List UserResult = dbSession.createQuery("select d from USUser d where d.userName ='"+name+"' ").list();
       HibernateUtil.closeAndCommitSession(dbSession);

       assertFalse(UserResult.size()==0);

    }

   @Test
    public void testLogin()
    {

      if (server ==null)  {CreateServer();};
        if (server.simple_login(name,pass)!="")
       {
           server.registration(name,pass,email,null);
       }
       String session = server.simple_login(name, pass);


    }


    @Test
    public void testChangePass(){
       String string_token = "test001";
       if (server ==null)  {CreateServer();};
       server.createTestChange(name,string_token);
       server.changePassword(name,newPass,string_token);
       String session = server.simple_login(name, newPass);
       assertTrue("test pass",session != null);
    }


    @Test
    public void testUrlChange(){

        if (server ==null)  {CreateServer();};
        USUser user = new USUser(name,pass,email,null);
        server.sendChangePassMail(user);

    }

}


