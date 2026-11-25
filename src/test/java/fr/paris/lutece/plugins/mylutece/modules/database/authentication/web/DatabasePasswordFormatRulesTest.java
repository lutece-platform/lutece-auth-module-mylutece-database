/*
 * Copyright (c) 2002-2026, City of Paris
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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.proxy.WeldClientProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import jakarta.inject.Inject;

/**
 * The password format rules are shown and kept whatever the advanced security parameters
 */
public class DatabasePasswordFormatRulesTest extends LuteceTestCase
{
    private static final String UPPER_LOWER = "password_format_upper_lower_case";
    private static final String NUMERO = "password_format_numero";
    private static final String SPECIAL = "password_format_special_characters";

    @Inject
    private DatabaseJspBean _databaseJspBean;
    @Inject
    private DatabaseService _databaseService;
    private Plugin _plugin;
    private boolean _bOrigAdvanced;
    private Map<String, Boolean> _mapOrigRules;

    @BeforeEach
    public void saveParameters( )
    {
        _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        _bOrigAdvanced = SecurityUtils.isAdvancedSecurityParametersUsed( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin );
        _mapOrigRules = _databaseService.getPasswordFormatRules( _plugin );
    }

    @AfterEach
    public void restoreParameters( )
    {
        if ( _bOrigAdvanced )
        {
            SecurityUtils.useAdvancedSecurityParameters( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin );
        }
        else
        {
            SecurityUtils.removeAdvancedSecurityParameters( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin );
        }

        _databaseService.updatePasswordFormatRules( _mapOrigRules, _plugin );
    }

    /**
     * Removing the advanced security parameters keeps the password format rules
     */
    @Test
    public void testRemovingTheAdvancedParametersKeepsTheFormatRules( )
    {
        SecurityUtils.useAdvancedSecurityParameters( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin );
        Map<String, Boolean> mapRules = new HashMap<>( );
        mapRules.put( UPPER_LOWER, true );
        mapRules.put( NUMERO, false );
        mapRules.put( SPECIAL, true );
        _databaseService.updatePasswordFormatRules( mapRules, _plugin );

        withResponse( _databaseJspBean );
        _databaseJspBean.doChangeUseAdvancedSecurityParameters( new MockHttpServletRequest( ) );

        assertFalse( SecurityUtils.isAdvancedSecurityParametersUsed( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin ) );
        assertEquals( mapRules, _databaseService.getPasswordFormatRules( _plugin ) );
    }

    /**
     * The screen gets the password format rules without the advanced security parameters
     */
    @Test
    public void testTheScreenGetsTheFormatRulesWithoutTheAdvancedParameters( )
    {
        SecurityUtils.removeAdvancedSecurityParameters( CDI.current( ).select( IDatabaseUserParameterService.class ).get( ), _plugin );
        Map<String, Boolean> mapRules = new HashMap<>( );
        mapRules.put( UPPER_LOWER, false );
        mapRules.put( NUMERO, true );
        mapRules.put( SPECIAL, false );
        _databaseService.updatePasswordFormatRules( mapRules, _plugin );

        AdminUser admin = AdminUserHome.findUserByLogin( "admin" );
        admin.setRoles( AdminUserHome.getRolesListForUser( admin.getUserId( ) ) );
        Map<String, Object> model = _databaseService.getManageAdvancedParameters( admin );

        assertEquals( Boolean.FALSE, model.get( UPPER_LOWER ) );
        assertEquals( Boolean.TRUE, model.get( NUMERO ) );
        assertEquals( Boolean.FALSE, model.get( SPECIAL ) );
    }

    /**
     * Gives the controller the response its actions redirect with, as processController does
     */
    private static void withResponse( Object bean )
    {
        try
        {
            Object instance = ( (WeldClientProxy) bean ).getMetadata( ).getContextualInstance( );
            Field field = MVCAdminJspBean.class.getDeclaredField( "_response" );
            field.setAccessible( true );
            field.set( instance, new MockHttpServletResponse( ) );
        }
        catch( ReflectiveOperationException e )
        {
            throw new IllegalStateException( e );
        }
    }
}
