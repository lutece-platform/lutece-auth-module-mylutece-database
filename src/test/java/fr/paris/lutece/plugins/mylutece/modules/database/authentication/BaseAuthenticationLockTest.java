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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication;

import jakarta.enterprise.inject.spi.CDI;
import java.security.SecureRandom;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLog;
import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLogHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;

/**
 * Lock of the front office login after too many failures
 */
public class BaseAuthenticationLockTest extends LuteceTestCase
{
    private static final String PASSWORD = "junitjunit";
    private Plugin _plugin;
    private String _strOrigMax;
    private String _strOrigInterval;
    private DatabaseUser _user;
    private DatabaseUser _otherUser;

    @BeforeEach
    public void createUsers( )
    {
        _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        _strOrigMax = getParameter( "access_failures_max" );
        _strOrigInterval = getParameter( "access_failures_interval" );
        setParameter( "access_failures_max", "3" );
        setParameter( "access_failures_interval", "10" );
        _user = createUser( );
        _otherUser = createUser( );
    }

    @AfterEach
    public void removeUsers( )
    {
        setParameter( "access_failures_max", _strOrigMax );
        setParameter( "access_failures_interval", _strOrigInterval );
        DatabaseUserHome.remove( _user, _plugin );
        DatabaseUserHome.remove( _otherUser, _plugin );
    }

    /**
     * Failures of a login from several addresses lock this login from any address, and only this login
     */
    @Test
    public void testFailuresOfALoginLockItFromAnyAddress( ) throws LoginException
    {
        String strBase = "10." + new SecureRandom( ).nextInt( 200 ) + "." + new SecureRandom( ).nextInt( 200 ) + ".";

        for ( int i = 1; i <= 3; i++ )
        {
            ConnectionLog log = new ConnectionLog( );
            log.setIpAddress( strBase + i );
            log.setLogin( _user.getLogin( ) );
            log.setDateLogin( new java.sql.Timestamp( System.currentTimeMillis( ) - 60000L ) );
            log.setLoginStatus( ConnectionLog.LOGIN_DENIED );
            ConnectionLogHome.addUserLog( log, PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME ) );
        }

        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setRemoteAddr( strBase + "100" );

        try
        {
            CDI.current( ).select( BaseAuthentication.class ).get( ).processLogin( _user.getLogin( ), PASSWORD, request );
            fail( "the login is locked after 3 failures" );
        }
        catch( FailedLoginException e )
        {
            assertEquals( Boolean.TRUE, request.getAttribute( ConnectionLog.ATTRIBUTE_LOGIN_LOCKED ), "the refusal is marked as a lock" );
        }

        // Another login from an address used by the failures is not locked: each address had 1 failure only
        MockHttpServletRequest otherRequest = new MockHttpServletRequest( );
        otherRequest.setRemoteAddr( strBase + "1" );
        assertNotNull( CDI.current( ).select( BaseAuthentication.class ).get( ).processLogin( _otherUser.getLogin( ), PASSWORD, otherRequest ) );
        assertNull( otherRequest.getAttribute( ConnectionLog.ATTRIBUTE_LOGIN_LOCKED ) );
    }

    private DatabaseUser createUser( )
    {
        DatabaseUser user = new DatabaseUser( );
        String strLogin = "junit_lock_" + Math.abs( new SecureRandom( ).nextLong( ) );
        user.setLogin( strLogin );
        user.setFirstName( strLogin );
        user.setLastName( strLogin );
        user.setEmail( strLogin + "@junit.fr" );
        user.setStatus( DatabaseUser.STATUS_ACTIVATED );
        CDI.current( ).select( DatabaseService.class ).get( ).doCreateUser( user, PASSWORD, _plugin );

        return user;
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
