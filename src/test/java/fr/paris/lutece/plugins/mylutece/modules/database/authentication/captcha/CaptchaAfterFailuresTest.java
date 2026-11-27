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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.captcha;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLog;
import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLogHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginDefaultImplementation;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.FailedLoginCaptchaException;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;
import jakarta.enterprise.inject.spi.CDI;

/**
 * The captcha of the login form is required after the number of failures set in « access_failures_captcha »: the failure
 * that reaches this number already asks for the captcha on the next attempt
 */
public class CaptchaAfterFailuresTest extends LuteceTestCase
{
    private static final String PASSWORD = "junitjunit";
    private static final String WRONG_PASSWORD = "wrongwrong";
    private static final String PLUGIN_CAPTCHA = "captcha";
    private static final String KEY_CAPTCHA = "access_failures_captcha";
    private static final String KEY_MAX = "access_failures_max";
    private static final String KEY_INTERVAL = "access_failures_interval";

    private Plugin _plugin;
    private DatabaseUser _user;
    private String _strOrigCaptcha;
    private String _strOrigMax;
    private String _strOrigInterval;
    private String _strIp;

    @SuppressWarnings( "unchecked" )
    private static Map<String, Plugin> plugins( ) throws ReflectiveOperationException
    {
        Field field = PluginService.class.getDeclaredField( "_mapPlugins" );
        field.setAccessible( true );
        return (Map<String, Plugin>) field.get( null );
    }

    @BeforeEach
    public void init( ) throws ReflectiveOperationException
    {
        _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        _strOrigCaptcha = getParameter( KEY_CAPTCHA );
        _strOrigMax = getParameter( KEY_MAX );
        _strOrigInterval = getParameter( KEY_INTERVAL );
        setParameter( KEY_MAX, "3" );
        setParameter( KEY_INTERVAL, "10" );

        Plugin captcha = new PluginDefaultImplementation( );
        captcha.setName( PLUGIN_CAPTCHA );
        captcha.setIsInstalled( true );
        plugins( ).put( PLUGIN_CAPTCHA, captcha );
        TestCaptchaService.setEngines( "testEngine" );

        _user = new DatabaseUser( );
        String strLogin = "junit_captcha_" + Math.abs( new SecureRandom( ).nextLong( ) );
        _user.setLogin( strLogin );
        _user.setFirstName( strLogin );
        _user.setLastName( strLogin );
        _user.setEmail( strLogin + "@junit.fr" );
        _user.setStatus( DatabaseUser.STATUS_ACTIVATED );
        CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( _user, PASSWORD, _plugin );
        _strIp = "10." + new SecureRandom( ).nextInt( 200 ) + "." + new SecureRandom( ).nextInt( 200 ) + "." + ( 1 + new SecureRandom( ).nextInt( 250 ) );
    }

    @AfterEach
    public void clean( ) throws ReflectiveOperationException
    {
        setParameter( KEY_CAPTCHA, _strOrigCaptcha );
        setParameter( KEY_MAX, _strOrigMax );
        setParameter( KEY_INTERVAL, _strOrigInterval );
        plugins( ).remove( PLUGIN_CAPTCHA );
        TestCaptchaService.setEngines( );
        DatabaseUserHome.remove( _user, _plugin );
    }

    /**
     * Threshold 1: the first failure already asks for the captcha on the next attempt
     */
    @Test
    public void testFirstFailureEnablesCaptchaWithThresholdOne( )
    {
        setParameter( KEY_CAPTCHA, "1" );

        assertTrue( failedLoginEnablesCaptcha( ), "1 failure with a threshold of 1: the captcha must be asked" );
    }

    /**
     * Threshold 2: the first failure does not ask for the captcha, the second one does
     */
    @Test
    public void testSecondFailureEnablesCaptchaWithThresholdTwo( )
    {
        setParameter( KEY_CAPTCHA, "2" );

        assertFalse( failedLoginEnablesCaptcha( ), "1 failure with a threshold of 2: no captcha yet" );
        logFailure( );
        assertTrue( failedLoginEnablesCaptcha( ), "2 failures with a threshold of 2: the captcha must be asked" );
    }

    /**
     * Without a usable captcha (plugin captcha not enabled) the failures never ask for a captcha
     *
     * @throws ReflectiveOperationException
     *             if the plugin map cannot be reached
     */
    @Test
    public void testNoCaptchaWhenCaptchaNotUsable( ) throws ReflectiveOperationException
    {
        setParameter( KEY_CAPTCHA, "1" );
        plugins( ).remove( PLUGIN_CAPTCHA );

        assertFalse( failedLoginEnablesCaptcha( ), "captcha not usable: never asked" );
    }

    /**
     * Tries to log in with a wrong password
     *
     * @return true if the failure asks for the captcha on the next attempt
     */
    private boolean failedLoginEnablesCaptcha( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setRemoteAddr( _strIp );

        try
        {
            CDI.current( ).select( BaseAuthentication.class ).get( ).processLogin( _user.getLogin( ), WRONG_PASSWORD, request );
            fail( "a wrong password must be refused" );
        }
        catch( FailedLoginCaptchaException e )
        {
            return e.isCaptchaEnabled( );
        }
        catch( FailedLoginException e )
        {
            return false;
        }
        catch( LoginException e )
        {
            fail( "unexpected " + e );
        }

        return false;
    }

    /**
     * Logs a failure of the user, as MyLuteceApp does after a refused login
     */
    private void logFailure( )
    {
        ConnectionLog log = new ConnectionLog( );
        log.setIpAddress( _strIp );
        log.setLogin( _user.getLogin( ) );
        log.setDateLogin( new java.sql.Timestamp( System.currentTimeMillis( ) - 1000L ) );
        log.setLoginStatus( ConnectionLog.LOGIN_DENIED );
        ConnectionLogHome.addUserLog( log, PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME ) );
    }

    private String getParameter( String strKey )
    {
        ReferenceItem item = DatabaseUserParameterHome.findByKey( strKey, _plugin );

        return ( item == null ) ? "" : item.getName( );
    }

    private void setParameter( String strKey, String strValue )
    {
        ReferenceItem item = new ReferenceItem( );
        item.setCode( strKey );
        item.setName( strValue );
        CDI.current( ).select( IDatabaseUserParameterService.class ).get( ).update( item, _plugin );
    }
}
