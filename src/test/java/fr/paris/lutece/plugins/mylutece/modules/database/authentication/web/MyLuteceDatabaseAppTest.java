package fr.paris.lutece.plugins.mylutece.modules.database.authentication.web;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKey;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKeyHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.DatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.MokeHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.password.IPassword;

public class MyLuteceDatabaseAppTest extends LuteceTestCase
{

    Plugin plugin;

    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
    }

    public void testDoCreateAccount( )
    {
        waitForHistoryPrimaryKey( );
        MokeHttpServletRequest request = new MokeHttpServletRequest( );
        String strLogin = getRandomName( );
        request.addMokeParameters( "plugin_name", plugin.getName( ) );
        request.addMokeParameters( "login", strLogin );
        request.addMokeParameters( "email", strLogin + "@junit.fr" );
        request.addMokeParameters( "password", "junitjunit" );
        request.addMokeParameters( "confirmation_password", "junitjunit" );
        request.addMokeParameters( "first_name", strLogin );
        request.addMokeParameters( "last_name", strLogin );

        MyLuteceDatabaseApp app = new MyLuteceDatabaseApp( );

        String url = app.doCreateAccount( request );
        assertNotNull( url );
        int userId = DatabaseUserHome.findDatabaseUserIdFromLogin( strLogin, plugin );
        assertFalse( "The user has not been created", 0 == userId );
        DatabaseUserHome.remove( DatabaseUserHome.findByPrimaryKey( userId, plugin ), plugin );
    }

    private String getRandomName( )
    {
        Random random = new Random( );
        BigInteger bigInt = new BigInteger( 128, random );
        return "junit" + bigInt.toString( 36 );
    }

    public void testDoReinitPassword_checkPasswordHistory_emptyHistory( )
    {
        waitForHistoryPrimaryKey( );
        DatabaseUser user = null;
        DatabaseUserKey userKey = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter(
                DatabaseUserParameterService.getService( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = "junitjunit";
            DatabaseService.getService( ).doCreateUser( user, strPassword, plugin );
            userKey = new DatabaseUserKey( );
            userKey.setKey( getRandomName( ) );
            userKey.setUserId( user.getUserId( ) );
            DatabaseUserKeyHome.create( userKey );
            // activate password history checks
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 1 ) );
            userParam.setCode( "password_history_size" );
            DatabaseUserParameterService.getService( ).update( userParam, plugin );
            MokeHttpServletRequest request = new MokeHttpServletRequest( );
            request.addMokeParameters( "key", userKey.getKey( ) );
            String strNewPassword = "junitjunit" + getRandomName( );
            request.addMokeParameters( "plugin_name", plugin.getName( ) );
            request.addMokeParameters( "password", strNewPassword );
            request.addMokeParameters( "confirmation_password", strNewPassword );

            MyLuteceDatabaseApp app = new MyLuteceDatabaseApp( );

            String url = app.doReinitPassword( request );
            assertNotNull( url );
            assertTrue( url.contains( "action_successful" ) );
            assertTrue( DatabaseService.getService( ).checkPassword( strLogin, strNewPassword, plugin ) );
            List<IPassword> history = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin );
            assertNotNull( history );
            assertFalse( history.isEmpty( ) );
            boolean matchFound = false;
            for ( IPassword password : history )
            {
                if ( password.check( strNewPassword ) )
                {
                    matchFound = true;
                    break;
                }
            }
            assertTrue( matchFound );
            userKey = DatabaseUserKeyHome.findByPrimaryKey( userKey.getKey( ) );
            assertNull( userKey );
        } finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            if ( userKey != null )
            {
                try
                {
                    DatabaseUserKeyHome.remove( userKey.getKey( ) );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    public void testDoReinitPassword_checkPasswordHistory_passwordInHistory( )
    {
        waitForHistoryPrimaryKey( );
        DatabaseUser user = null;
        DatabaseUserKey userKey = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter(
                DatabaseUserParameterService.getService( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = "junitjunit";
            DatabaseService.getService( ).doCreateUser( user, strPassword, plugin );
            userKey = new DatabaseUserKey( );
            userKey.setKey( getRandomName( ) );
            userKey.setUserId( user.getUserId( ) );
            DatabaseUserKeyHome.create( userKey );
            // activate password history checks
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            DatabaseUserParameterService.getService( ).update( userParam, plugin );
            String strNewPassword = "junitjunit" + getRandomName( );
            DatabaseService.getService( ).doInsertNewPasswordInHistory( strNewPassword, user.getUserId( ), plugin );
            int previousPasswordHistorySize = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin )
                    .size( );
            MokeHttpServletRequest request = new MokeHttpServletRequest( );
            request.addMokeParameters( "plugin_name", plugin.getName( ) );
            request.addMokeParameters( "key", userKey.getKey( ) );
            request.addMokeParameters( "password", strNewPassword );
            request.addMokeParameters( "confirmation_password", strNewPassword );

            MyLuteceDatabaseApp app = new MyLuteceDatabaseApp( );

            String url = app.doReinitPassword( request );
            assertNotNull( url );
            assertTrue( url.contains( "error_code" ) );
            assertTrue( DatabaseService.getService( ).checkPassword( strLogin, strPassword, plugin ) );
            assertEquals( previousPasswordHistorySize,
                    DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( ) );
            userKey = DatabaseUserKeyHome.findByPrimaryKey( userKey.getKey( ) );
            assertNotNull( userKey );
        } finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            if ( userKey != null )
            {
                try
                {
                    DatabaseUserKeyHome.remove( userKey.getKey( ) );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    public void testDoChangePassword_checkPasswordHistory_emptyHistory( )
    {
        waitForHistoryPrimaryKey( );
        DatabaseUser user = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter(
                DatabaseUserParameterService.getService( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = "junitjunit";
            DatabaseService.getService( ).doCreateUser( user, strPassword, plugin );
            // activate password history checks
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            DatabaseUserParameterService.getService( ).update( userParam, plugin );
            String strNewPassword = "junitjunit" + getRandomName( );
            MokeHttpServletRequest request = new MokeHttpServletRequest( );
            BaseUser loggedInUser = DatabaseHome.findLuteceUserByLogin( strLogin, plugin, new BaseAuthentication( ) );
            SecurityService.getInstance( ).registerUser( request, loggedInUser );
            request.addMokeParameters( "plugin_name", plugin.getName( ) );
            request.addMokeParameters( "old_password", strPassword );
            request.addMokeParameters( "new_password", strNewPassword );
            request.addMokeParameters( "confirmation_password", strNewPassword );

            MyLuteceDatabaseApp app = new MyLuteceDatabaseApp( );

            String url = app.doChangePassword( request );

            assertNotNull( url );
            assertTrue( url.contains( "action_successful" ) );
            assertTrue( DatabaseService.getService( ).checkPassword( strLogin, strNewPassword, plugin ) );
            List<IPassword> history = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin );
            assertNotNull( history );
            assertFalse( history.isEmpty( ) );
            boolean matchFound = false;
            for ( IPassword password : history )
            {
                if ( password.check( strNewPassword ) )
                {
                    matchFound = true;
                    break;
                }
            }
            assertTrue( matchFound );
        } finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    public void testDoChangePassword_checkPasswordHistory_passwordInHistory( )
    {
        waitForHistoryPrimaryKey( );
        DatabaseUser user = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter(
                DatabaseUserParameterService.getService( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = "junitjunit";
            DatabaseService.getService( ).doCreateUser( user, strPassword, plugin );
            // activate password history checks
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            DatabaseUserParameterService.getService( ).update( userParam, plugin );
            String strNewPassword = "junitjunit" + getRandomName( );
            DatabaseService.getService( ).doInsertNewPasswordInHistory( strNewPassword, user.getUserId( ), plugin );
            int previousPasswordHistorySize = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin )
                    .size( );
            MokeHttpServletRequest request = new MokeHttpServletRequest( );
            BaseUser loggedInUser = DatabaseHome.findLuteceUserByLogin( strLogin, plugin, new BaseAuthentication( ) );
            SecurityService.getInstance( ).registerUser( request, loggedInUser );
            request.addMokeParameters( "plugin_name", plugin.getName( ) );
            request.addMokeParameters( "old_password", strPassword );
            request.addMokeParameters( "new_password", strNewPassword );
            request.addMokeParameters( "confirmation_password", strNewPassword );

            MyLuteceDatabaseApp app = new MyLuteceDatabaseApp( );

            String url = app.doChangePassword( request );

            assertNotNull( url );
            assertTrue( url.contains( "error_code" ) );
            assertTrue( DatabaseService.getService( ).checkPassword( strLogin, strPassword, plugin ) );
            assertEquals( previousPasswordHistorySize,
                    DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( ) );
        } finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                } catch ( Exception e )
                {
                    // ignore
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    private void restorePasswordHistorySize( int nOrigPasswordHistorySize )
    {
        try
        {
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( nOrigPasswordHistorySize ) );
            userParam.setCode( "password_history_size" );
            DatabaseUserParameterService.getService( ).update( userParam, plugin );
        } catch ( Exception e )
        {
            // ignore
        }
    }

    private static void waitForHistoryPrimaryKey() {
        try
        {
            Thread.sleep( 1000 ); // Need this because the PRIMARY KEY uses the timestamp
        }
        catch( InterruptedException e )
        {
            throw new RuntimeException( e ); // Should not happen
        }
    }
}
