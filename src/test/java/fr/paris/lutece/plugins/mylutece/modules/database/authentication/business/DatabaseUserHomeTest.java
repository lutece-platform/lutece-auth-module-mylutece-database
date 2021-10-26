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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import java.math.BigInteger;
import java.util.Random;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.util.password.IPassword;
import fr.paris.lutece.util.password.IPasswordFactory;

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

    public void testRemoveRemovesPasswordHistory( )
    {
        DatabaseUser databaseUser = new DatabaseUser( );
        databaseUser.setLogin( strLogin );
        databaseUser.setFirstName( strLogin );
        databaseUser.setLastName( strLogin );
        IPasswordFactory passwordFactory = SpringContextService.getBean( IPasswordFactory.BEAN_NAME );
        DatabaseUserHome.create( databaseUser, passwordFactory.getPasswordFromCleartext( strLogin ), plugin );

        DatabaseUserHome.insertNewPasswordInHistory( passwordFactory.getPasswordFromCleartext( strLogin + "_2" ), databaseUser.getUserId( ), plugin );

        assertEquals( 1, DatabaseUserHome.selectUserPasswordHistory( databaseUser.getUserId( ), plugin ).size( ) );

        DatabaseUserHome.remove( databaseUser, plugin );

        assertEquals( 0, DatabaseUserHome.selectUserPasswordHistory( databaseUser.getUserId( ), plugin ).size( ) );
    }

    private String getRandomName( )
    {
        Random random = new Random( );
        BigInteger bigInt = new BigInteger( 128, random );
        return "junit" + bigInt.toString( 36 );
    }

}
