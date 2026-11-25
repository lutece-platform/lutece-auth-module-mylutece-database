/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.web;

import jakarta.enterprise.inject.spi.CDI;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKey;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKeyHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.password.IPassword;
import jakarta.inject.Inject;

public class MyLuteceDatabaseAppTest extends LuteceTestCase
{

    // A password that follows the default rules of a new installation (12 characters, upper and lower case, digit, special character)
    private static final String STRONG_PASSWORD = "Junit-Passw0rd!";
    private Plugin plugin;

    @Inject
    private MyLuteceDatabaseApp app;

    /**
     * Initializes the plugin used by the tests.
     *
     * @throws Exception
     *             if the test case setup fails
     */
    @BeforeEach
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doCreateAccount(jakarta.servlet.http.HttpServletRequest)} creates a user in the database.
     */
    @Test
    public void testDoCreateAccount( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        String strLogin = getRandomName( );
        request.addParameter( "plugin_name", plugin.getName( ) );
        request.addParameter( "login", strLogin );
        request.addParameter( "email", strLogin + "@junit.fr" );
        request.addParameter( "password", STRONG_PASSWORD );
        request.addParameter( "confirmation_password", STRONG_PASSWORD );
        request.addParameter( "first_name", strLogin );
        request.addParameter( "last_name", strLogin );

        String url = app.doCreateAccount( request );
        assertNotNull( url );
        int userId = DatabaseUserHome.findDatabaseUserIdFromLogin( strLogin, plugin );
        assertFalse( "The user has not been created", 0 == userId );
        DatabaseUserHome.remove( DatabaseUserHome.findByPrimaryKey( userId, plugin ), plugin );
    }

    /**
     * The help of a new password lists the rules really checked: the minimum length and the active format rules only
     */
    @Test
    public void testPasswordHelpFollowsTheSecurityParameters( )
    {
        String strOrigLength = getParameter( "password_minimum_length" );
        String strOrigNumero = getParameter( "password_format_numero" );
        String strOrigUpperLower = getParameter( "password_format_upper_lower_case" );
        String strOrigSpecial = getParameter( "password_format_special_characters" );
        try
        {
            MockHttpServletRequest request = new MockHttpServletRequest( );
            app.init( request, plugin );

            setParameter( "password_minimum_length", "12" );
            setParameter( "password_format_numero", "false" );
            setParameter( "password_format_upper_lower_case", "false" );
            setParameter( "password_format_special_characters", "false" );
            String strLengthHelp = I18nService.getLocalizedString( "module.mylutece.database.xpage.passwordHelp.minimumLength", new Object [ ] {
                    12
            }, request.getLocale( ) );
            assertEquals( strLengthHelp, app.getPasswordHelp( ), "only the minimum length is checked" );

            setParameter( "password_format_numero", "true" );
            assertEquals( strLengthHelp + " "
                    + SecurityUtils.getMessageFrontPasswordFormat( request.getLocale( ), CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), plugin ) + ".",
                    app.getPasswordHelp( ) );

            setParameter( "password_minimum_length", "0" );
            setParameter( "password_format_numero", "false" );
            assertEquals( "", app.getPasswordHelp( ) );
        }
        finally
        {
            setParameter( "password_minimum_length", strOrigLength );
            setParameter( "password_format_numero", strOrigNumero );
            setParameter( "password_format_upper_lower_case", strOrigUpperLower );
            setParameter( "password_format_special_characters", strOrigSpecial );
        }
    }

    private String getParameter( String strKey )
    {
        ReferenceItem item = CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).findByKey( strKey, plugin );

        return ( item == null ) ? "" : item.getName( );
    }

    private void setParameter( String strKey, String strValue )
    {
        ReferenceItem item = new ReferenceItem( );
        item.setCode( strKey );
        item.setName( strValue );
        CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( item, plugin );
    }

    /**
     * Generates a random login name used to avoid collisions across test runs.
     *
     * @return the generated login
     */
    private String getRandomName( )
    {
        Random random = new Random( );
        BigInteger bigInt = new BigInteger( 128, random );
        return "junit" + bigInt.toString( 36 );
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doReinitPassword(jakarta.servlet.http.HttpServletRequest)} succeeds when the password history is empty.
     */
    @Test
    public void testDoReinitPassword_checkPasswordHistory_emptyHistory( )
    {
        DatabaseUser user = null;
        DatabaseUserKey userKey = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = STRONG_PASSWORD;
            CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( user, strPassword, plugin );
            userKey = new DatabaseUserKey( );
            userKey.setKey( getRandomName( ) );
            userKey.setUserId( user.getUserId( ) );
            DatabaseUserKeyHome.create( userKey );
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 1 ) );
            userParam.setCode( "password_history_size" );
            CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( userParam, plugin );
            MockHttpServletRequest request = new MockHttpServletRequest( );
            request.addParameter( "key", userKey.getKey( ) );
            String strNewPassword = STRONG_PASSWORD + getRandomName( );
            request.addParameter( "plugin_name", plugin.getName( ) );
            request.addParameter( "password", strNewPassword );
            request.addParameter( "confirmation_password", strNewPassword );

            String url = app.doReinitPassword( request );
            assertNotNull( url );
            assertTrue( url.contains( "success_url" ) );
            assertTrue( CDI.current( ).select( DatabaseService.class ).get( ).checkPassword( strLogin, strNewPassword, plugin ) );
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
        }
        finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                }
                catch( Exception e )
                {
                }
            }
            if ( userKey != null )
            {
                try
                {
                    DatabaseUserKeyHome.remove( userKey.getKey( ) );
                }
                catch( Exception e )
                {
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doReinitPassword(jakarta.servlet.http.HttpServletRequest)} rejects a password already present in the history.
     */
    @Test
    public void testDoReinitPassword_checkPasswordHistory_passwordInHistory( )
    {
        DatabaseUser user = null;
        DatabaseUserKey userKey = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = STRONG_PASSWORD;
            CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( user, strPassword, plugin );
            userKey = new DatabaseUserKey( );
            userKey.setKey( getRandomName( ) );
            userKey.setUserId( user.getUserId( ) );
            DatabaseUserKeyHome.create( userKey );
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( userParam, plugin );
            String strNewPassword = STRONG_PASSWORD + getRandomName( );
            CDI.current( ).select( DatabaseService.class ).get( ).doInsertNewPasswordInHistory( strNewPassword, user.getUserId( ), plugin );
            int previousPasswordHistorySize = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( );
            MockHttpServletRequest request = new MockHttpServletRequest( );
            request.addParameter( "plugin_name", plugin.getName( ) );
            request.addParameter( "key", userKey.getKey( ) );
            request.addParameter( "password", strNewPassword );
            request.addParameter( "confirmation_password", strNewPassword );

            String url = app.doReinitPassword( request );
            assertNotNull( url );
            assertTrue( url.contains( "error_code" ) );
            assertTrue( CDI.current( ).select( DatabaseService.class ).get( ).checkPassword( strLogin, strPassword, plugin ) );
            assertEquals( previousPasswordHistorySize, DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( ) );
            userKey = DatabaseUserKeyHome.findByPrimaryKey( userKey.getKey( ) );
            assertNotNull( userKey );
        }
        finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                }
                catch( Exception e )
                {
                }
            }
            if ( userKey != null )
            {
                try
                {
                    DatabaseUserKeyHome.remove( userKey.getKey( ) );
                }
                catch( Exception e )
                {
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doChangePassword(jakarta.servlet.http.HttpServletRequest)} succeeds when the password history is empty.
     */
    @Test
    public void testDoChangePassword_checkPasswordHistory_emptyHistory( )
    {
        DatabaseUser user = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = STRONG_PASSWORD;
            CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( user, strPassword, plugin );
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( userParam, plugin );
            String strNewPassword = STRONG_PASSWORD + getRandomName( );
            MockHttpServletRequest request = new MockHttpServletRequest( );
            BaseUser loggedInUser = DatabaseHome.findLuteceUserByLogin( strLogin, plugin, CDI.current( ).select( BaseAuthentication.class ).get( ) );
            SecurityService.getInstance( ).registerUser( request, loggedInUser );
            request.addParameter( "plugin_name", plugin.getName( ) );
            request.addParameter( "old_password", strPassword );
            request.addParameter( "new_password", strNewPassword );
            request.addParameter( "confirmation_password", strNewPassword );

            String url = app.doChangePassword( request );

            assertNotNull( url );
            assertTrue( url.contains( "success_url" ) );
            assertTrue( CDI.current( ).select( DatabaseService.class ).get( ).checkPassword( strLogin, strNewPassword, plugin ) );
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
        }
        finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                }
                catch( Exception e )
                {
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doChangePassword(jakarta.servlet.http.HttpServletRequest)} rejects a password already present in the history.
     */
    @Test
    public void testDoChangePassword_checkPasswordHistory_passwordInHistory( )
    {
        DatabaseUser user = null;
        int nOrigPasswordHistorySize = SecurityUtils.getIntegerSecurityParameter( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), plugin, "password_history_size" );
        try
        {
            user = new DatabaseUser( );
            String strLogin = getRandomName( );
            user.setLogin( strLogin );
            user.setFirstName( strLogin );
            user.setLastName( strLogin );
            String strPassword = STRONG_PASSWORD;
            CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( user, strPassword, plugin );
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( 10 ) );
            userParam.setCode( "password_history_size" );
            CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( userParam, plugin );
            String strNewPassword = STRONG_PASSWORD + getRandomName( );
            CDI.current( ).select( DatabaseService.class ).get( ).doInsertNewPasswordInHistory( strNewPassword, user.getUserId( ), plugin );
            int previousPasswordHistorySize = DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( );
            MockHttpServletRequest request = new MockHttpServletRequest( );
            BaseUser loggedInUser = DatabaseHome.findLuteceUserByLogin( strLogin, plugin, CDI.current( ).select( BaseAuthentication.class ).get( ) );
            SecurityService.getInstance( ).registerUser( request, loggedInUser );
            request.addParameter( "plugin_name", plugin.getName( ) );
            request.addParameter( "old_password", strPassword );
            request.addParameter( "new_password", strNewPassword );
            request.addParameter( "confirmation_password", strNewPassword );

            String url = app.doChangePassword( request );

            assertNotNull( url );
            assertTrue( url.contains( "error_code" ) );
            assertTrue( CDI.current( ).select( DatabaseService.class ).get( ).checkPassword( strLogin, strPassword, plugin ) );
            assertEquals( previousPasswordHistorySize, DatabaseUserHome.selectUserPasswordHistory( user.getUserId( ), plugin ).size( ) );
        }
        finally
        {
            if ( user != null )
            {
                try
                {
                    DatabaseUserHome.remove( user, plugin );
                }
                catch( Exception e )
                {
                }
            }
            restorePasswordHistorySize( nOrigPasswordHistorySize );
        }
    }

    /**
     * Restores the {@code password_history_size} plugin parameter to its original value after a test has finished.
     *
     * @param nOrigPasswordHistorySize
     *            the original password history size to restore
     */
    private void restorePasswordHistorySize( int nOrigPasswordHistorySize )
    {
        try
        {
            ReferenceItem userParam = new ReferenceItem( );
            userParam.setName( Integer.toString( nOrigPasswordHistorySize ) );
            userParam.setCode( "password_history_size" );
            CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( userParam, plugin );
        }
        catch( Exception e )
        {
        }
    }


    /**
     * Tests that the reinit password page asked by an anonymous user without key gives a message, not a NullPointerException.
     */
    @Test
    public void testGetReinitPasswordPage_anonymousWithoutKey( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );

        assertThrows( SiteMessageException.class, ( ) -> app.getReinitPasswordPage( new XPage( ), request ) );
    }

    /**
     * Tests that {@link MyLuteceDatabaseApp#doSendPassword(jakarta.servlet.http.HttpServletRequest)} without plugin name uses the plugin of the
     * module.
     */
    @Test
    public void testDoSendPassword_withoutPluginName( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "email", getRandomName( ) + "@junit.fr" );

        String strUrl = app.doSendPassword( request );

        assertNotNull( strUrl );
        assertTrue( strUrl.contains( "plugin_name=" + DatabasePlugin.PLUGIN_NAME ) );
    }
}
