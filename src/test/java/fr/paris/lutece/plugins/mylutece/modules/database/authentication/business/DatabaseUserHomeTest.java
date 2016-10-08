package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import java.math.BigInteger;
import java.util.Random;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.util.password.IPassword;

public class DatabaseUserHomeTest extends LuteceTestCase
{

    private Plugin plugin;
    private String strLogin;

    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        strLogin = getRandomName( );
    }

    @Override
    protected void tearDown( ) throws Exception
    {
        int nKey = DatabaseUserHome.findDatabaseUserIdFromLogin( strLogin, plugin );
        if ( nKey != 0 )
        {
            DatabaseUser databaseUser = DatabaseUserHome.findByPrimaryKey( nKey, plugin );
            DatabaseUserHome.remove( databaseUser, plugin );
        }
        super.tearDown( );
    }

    public void testCheckPassword_upgradeStorage( )
    {
        final String password = "junit";
        IPassword legacyPassword = new IPassword( )
        {

            @Override
            public boolean isLegacy( )
            {
                return true;
            }

            @Override
            public String getStorableRepresentation( )
            {
                return "PLAINTEXT:" + password;
            }

            @Override
            public boolean check( String strCleartextPassword )
            {
                return password.equals( strCleartextPassword );
            }
        };
        DatabaseUser databaseUser = new DatabaseUser( );
        databaseUser.setLogin( strLogin );
        databaseUser.setFirstName( strLogin );
        databaseUser.setLastName( strLogin );
        DatabaseUserHome.create( databaseUser, legacyPassword, plugin );

        IDatabaseUserDAO dao = SpringContextService.getBean( "mylutece-database.databaseUserDAO" );
        IPassword storedPassword = dao.loadPassword( strLogin, plugin );
        assertTrue( storedPassword.isLegacy( ) );

        assertTrue( DatabaseUserHome.checkPassword( strLogin, password, plugin ) );

        storedPassword = dao.loadPassword( strLogin, plugin );
        assertFalse( storedPassword.isLegacy( ) );

        // check that the password is the same
        assertTrue( DatabaseUserHome.checkPassword( strLogin, password, plugin ) );
    }

    private String getRandomName( )
    {
        Random random = new Random( );
        BigInteger bigInt = new BigInteger( 128, random );
        return "junit" + bigInt.toString( 36 );
    }

}
