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

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldListener;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * DatabaseUserFieldListener
 *
 */
public class DatabaseUserFieldListener implements MyLuteceUserFieldListener
{
    // Constantes
    private static final String EMPTY_STRING = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCreateUserFields( int nIdUser, HttpServletRequest request, Locale locale )
    {
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<IAttribute> listAttributes = AttributeHome.findPluginAttributes( DatabasePlugin.PLUGIN_NAME, locale, myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<MyLuteceUserField> userFields = attribute.getUserFieldsData( request, nIdUser );
            doCreateUserFields( nIdUser, userFields, locale );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCreateUserFields( int nIdUser, List<MyLuteceUserField> listUserFields, Locale locale )
    {
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );

        for ( MyLuteceUserField userField : listUserFields )
        {
            if ( ( userField != null ) && !userField.getValue( ).equals( EMPTY_STRING ) )
            {
                // Change the value of the user field
                // Instead of having the ID of the attribute field, we put the attribute field title
                // which represents the locale
                userField.setValue( userField.getAttributeField( ).getTitle( ) );
                MyLuteceUserFieldHome.create( userField, myLutecePlugin );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doModifyUserFields( int nIdUser, HttpServletRequest request, Locale locale, AdminUser currentUser )
    {
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<IAttribute> listAttributes = AttributeHome.findPluginAttributes( DatabasePlugin.PLUGIN_NAME, locale, myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<MyLuteceUserField> userFields = attribute.getUserFieldsData( request, nIdUser );
            doModifyUserFields( nIdUser, userFields, locale, currentUser );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doModifyUserFields( int nIdUser, List<MyLuteceUserField> listUserFields, Locale locale, AdminUser currentUser )
    {
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );

        for ( MyLuteceUserField userField : listUserFields )
        {
            if ( ( userField != null ) && !userField.getValue( ).equals( EMPTY_STRING ) )
            {
                // Change the value of the user field
                // Instead of having the ID of the attribute field, we put the attribute field title
                // which represents the locale
                userField.setValue( userField.getAttributeField( ).getTitle( ) );
                MyLuteceUserFieldHome.create( userField, myLutecePlugin );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveUserFields( int nIdUser, HttpServletRequest request, Locale locale )
    {
        // No action
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveUserFields( int nIdUser, Locale locale )
    {
        // No action
    }
}
