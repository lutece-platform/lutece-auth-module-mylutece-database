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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.rbac.RBACHome;
import fr.paris.lutece.portal.business.rbac.RBACRole;
import fr.paris.lutece.portal.business.rbac.RBACRoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.util.password.IPassword;

public class DatabaseServiceTest extends LuteceTestCase
{

    private Plugin plugin;
    private DatabaseService service;
    String strLogin;
    String strPassword;
    DatabaseUser user;

    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        service = DatabaseService.getService( );
        strLogin = getRandomName( );
        user = new DatabaseUser( );
        user.setLogin( strLogin );
        user.setFirstName( strLogin );
        user.setLastName( strLogin );
        strPassword = "Junit";
        service.doCreateUser( user, strPassword, plugin );
    }

    @Override
    protected void tearDown( ) throws Exception
    {
        try
        {
            if ( user != null )
            {
                DatabaseUserHome.remove( user, plugin );
            }
        }
        finally
        {
            super.tearDown( );
        }
    }

    public void testDoCreateUser( )
    {
        DatabaseUser storedUser = DatabaseUserHome.findByPrimaryKey( user.getUserId( ), plugin );
        assertNotNull( storedUser );
        assertTrue( DatabaseUserHome.checkPassword( strLogin, strPassword, plugin ) );
        assertNotNull( storedUser.getAccountMaxValidDate( ) );
        assertTrue( storedUser.getAccountMaxValidDate( ).after( new Timestamp( ( new Date( ) ).getTime( ) ) ) );
        assertNull( storedUser.getPasswordMaxValidDate( ) );
    }

    private String getRandomName( )
    {
        Random random = new Random( );
        BigInteger bigInt = new BigInteger( 128, random );
        return "junit" + bigInt.toString( 36 );
    }

    public void testDoModifyPassword( )
    {
        assertTrue( DatabaseUserHome.checkPassword( strLogin, strPassword, plugin ) );
        String strChangedPassword = "changedPassword";
        service.doModifyPassword( user, strChangedPassword, plugin );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, strPassword, plugin ) );
        assertTrue( DatabaseUserHome.checkPassword( strLogin, strChangedPassword, plugin ) );
    }

    public void testCheckPassword( )
    {
        assertTrue( DatabaseUserHome.checkPassword( strLogin, strPassword, plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, "", plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, "  ", plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, "\t", plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, "\n", plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, strPassword.toLowerCase( ), plugin ) );
        assertFalse( DatabaseUserHome.checkPassword( strLogin, strPassword.toUpperCase( ), plugin ) );
    }

    public void testGetManageAdvancedParameters( )
    {
        AdminUser adminUser = AdminUserHome.findUserByLogin( "admin" );
        assertNotNull( "This test expects the admin BO user to exist", adminUser );
        String roleKey = giveRights( adminUser );
        try
        {
            Map<String, Object> model = service.getManageAdvancedParameters( adminUser );
            assertNotNull( model );
            assertTrue( model.containsKey( "is_plugin_jcatpcha_enable" ) );
            assertTrue( model.containsKey( "account_creation_validation_email" ) );
            assertTrue( model.containsKey( "auto_login_after_validation_email" ) );
            assertTrue( model.containsKey( "banned_domain_names" ) );
            // more keys added by plugin-mylutece
        }
        finally
        {
            removeRights( adminUser, roleKey );
        }
    }

    private void removeRights( AdminUser adminUser, String roleKey )
    {
        AdminUserHome.removeRoleForUser( adminUser.getUserId( ), roleKey );
        RBACHome.removeForRoleKey( roleKey );
        RBACRoleHome.remove( roleKey );
    }

    private String giveRights( AdminUser adminUser )
    {
        String roleKey = getRandomName( );
        RBACRole role = new RBACRole( );
        role.setKey( roleKey );
        role.setDescription( roleKey );
        RBACRoleHome.create( role );
        try
        {
            RBAC rBAC = new RBAC( );
            rBAC.setRoleKey( roleKey );
            rBAC.setResourceId( RBAC.WILDCARD_RESOURCES_ID );
            rBAC.setResourceTypeKey( DatabaseResourceIdService.RESOURCE_TYPE );
            rBAC.setPermissionKey( DatabaseResourceIdService.PERMISSION_MANAGE );
            RBACHome.create( rBAC );
            AdminUserHome.createRoleForUser( adminUser.getUserId( ), roleKey );
            adminUser.setRoles( AdminUserHome.getRolesListForUser( adminUser.getUserId( ) ) );
        }
        catch( Throwable e )
        {
            e.printStackTrace( );
        }
        return roleKey;
    }

    public void testDoInsertNewPasswordInHistory( )
    {
        String strNewPassword = getRandomName( );
        service.doInsertNewPasswordInHistory( strNewPassword, user.getUserId( ), plugin );
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

}
