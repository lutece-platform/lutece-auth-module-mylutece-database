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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.IAnonymizationService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;

/**
 * Service to handle user anonymization
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseAnonymizationService" )
public class DatabaseAnonymizationService implements IAnonymizationService
{
    // PARAMETERS
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_NAME_GIVEN = "name_given";
    private static final String PARAMETER_NAME_FAMILY = "name_family";

    // PROPERTIES
    private static final String PROPERTY_ANONYMIZATION_ENCRYPT_ALGO = "security.anonymization.encryptAlgo";

    // CONSTANTS
    private static final String CONSTANT_DEFAULT_ENCRYPT_ALGO = "SHA-256";
    private Plugin _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

    /**
     * Returns the instance of the singleton
     * 
     * @return The instance of the singleton
     */
    public static IAnonymizationService getService( )
    {
        return CDI.current( ).select( DatabaseAnonymizationService.class ).get( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void anonymizeUser( Integer nUserId, Locale locale )
    {
        DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nUserId, _plugin );

        String strEncryptionAlgorithme = AppPropertiesService.getProperty( PROPERTY_ANONYMIZATION_ENCRYPT_ALGO, CONSTANT_DEFAULT_ENCRYPT_ALGO );
        Plugin pluginMyLutece = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        Map<String, Boolean> anonymizationStatus = AttributeHome.getAnonymizationStatusUserStaticField( pluginMyLutece );

        if ( Boolean.TRUE.equals( anonymizationStatus.get( PARAMETER_LOGIN ) ) )
        {
            user.setLogin( CryptoService.encrypt( user.getLogin( ), strEncryptionAlgorithme ) );
        }

        if ( Boolean.TRUE.equals( anonymizationStatus.get( PARAMETER_EMAIL ) ) )
        {
            user.setEmail( CryptoService.encrypt( user.getEmail( ), strEncryptionAlgorithme ) );
        }

        if ( Boolean.TRUE.equals( anonymizationStatus.get( PARAMETER_NAME_FAMILY ) ) )
        {
            user.setLastName( CryptoService.encrypt( user.getLastName( ), strEncryptionAlgorithme ) );
        }

        if ( Boolean.TRUE.equals( anonymizationStatus.get( PARAMETER_NAME_GIVEN ) ) )
        {
            user.setFirstName( CryptoService.encrypt( user.getFirstName( ), strEncryptionAlgorithme ) );
        }

        user.setStatus( DatabaseUser.STATUS_ANONYMIZED );

        DatabaseHome.removeGroupsForUser( nUserId, _plugin );
        DatabaseHome.removeRolesForUser( nUserId, _plugin );
        DatabaseUserHome.update( user, _plugin );

        List<IAttribute> listAllAttributes = AttributeHome.findAll( locale, pluginMyLutece );
        List<IAttribute> listAttributesText = new ArrayList<>( );

        for ( IAttribute attribut : listAllAttributes )
        {
            if ( attribut.isAnonymizable( ) )
            {
                listAttributesText.add( attribut );
            }
        }

        for ( IAttribute attribute : listAttributesText )
        {
            List<MyLuteceUserField> listUserField = MyLuteceUserFieldHome.selectUserFieldsByIdUserIdAttribute( nUserId, attribute.getIdAttribute( ),
                    pluginMyLutece );

            for ( MyLuteceUserField userField : listUserField )
            {
                userField.setValue( CryptoService.encrypt( userField.getValue( ), strEncryptionAlgorithme ) );
                MyLuteceUserFieldHome.update( userField, pluginMyLutece );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getExpiredUserIdList( )
    {
        return DatabaseUserHome.findAllExpiredUserId( _plugin );
    }
}
