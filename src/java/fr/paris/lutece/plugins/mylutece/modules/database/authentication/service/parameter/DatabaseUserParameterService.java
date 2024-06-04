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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;

/**
 *
 * DatabaseUserParameterService
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseUserParameterService" )
public class DatabaseUserParameterService implements IDatabaseUserParameterService
{

    // PARAMETERS
    private static final String PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL = "account_creation_validation_email";
    private static final String PARAMETER_ENABLE_JCAPTCHA = "enable_jcaptcha";
    private static final String PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL = "auto_login_after_validation_email";

    /**
     * Private constructor
     */
    private DatabaseUserParameterService( )
    {
    }

    /**
     * Get the instance of the service
     * 
     * @return the instance of the service
     */
    @Deprecated
    public static IDatabaseUserParameterService getService( )
    {
        return CDI.current( ).select( IDatabaseUserParameterService.class ).get( );
    }

    /**
     * Get the parameter from a given key
     * 
     * @param strParameterKey
     *            the key
     * @param plugin
     *            the plugin
     * @return the parameter
     */
    public ReferenceItem findByKey( String strParameterKey, Plugin plugin )
    {
        return DatabaseUserParameterHome.findByKey( strParameterKey, plugin );
    }

    /**
     * Update a parameter
     * 
     * @param userParam
     *            the parameter
     * @param plugin
     *            the plugin
     */
    public void update( ReferenceItem userParam, Plugin plugin )
    {
        if ( userParam != null )
        {
            DatabaseUserParameterHome.update( userParam, plugin );
        }
    }

    /**
     * Find all parameters
     * 
     * @param plugin
     *            the plugin
     * @return a ReferenceList
     */
    public ReferenceList findAll( Plugin plugin )
    {
        return DatabaseUserParameterHome.findAll( plugin );
    }

    /**
     * Check if the passwords must be encrypted or not
     * 
     * @param plugin
     *            the plugin
     * @return <code>false</code>. Passwords are in fact salted and hashed, but we don't want plugin-mylutece to try and hash the password itself
     */
    public boolean isPasswordEncrypted( Plugin plugin )
    {
        return false;
    }

    /**
     * Get the encryption algorithm
     * 
     * @param plugin
     *            the plugin
     * @return the encryption algorithm
     */
    public String getEncryptionAlgorithm( Plugin plugin )
    {
        return "";
    }

    /**
     * Check if the account creation must be validated by email
     * 
     * @param plugin
     *            the plugin
     * @return true if it must be validated by email, false otherwise
     */
    public boolean isAccountCreationValidationEmail( Plugin plugin )
    {
        boolean bIsValidationEmail = false;
        ReferenceItem userParam = findByKey( PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL, plugin );

        if ( ( userParam != null ) && userParam.isChecked( ) )
        {
            bIsValidationEmail = true;
        }

        return bIsValidationEmail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoLoginAfterValidationEmail( Plugin plugin )
    {
        boolean bIsAutoLogin = false;
        ReferenceItem userParam = findByKey( PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL, plugin );

        if ( ( userParam != null ) && userParam.isChecked( ) )
        {
            bIsAutoLogin = true;
        }

        return bIsAutoLogin;
    }

    /**
     * Check if the jcaptcha is enable or not
     * 
     * @param plugin
     *            the plugin
     * @return true if it is enable, false otherwise
     */
    public boolean isJcaptchaEnable( Plugin plugin )
    {
        boolean bIsEnable = false;
        ReferenceItem userParam = findByKey( PARAMETER_ENABLE_JCAPTCHA, plugin );

        if ( ( userParam != null ) && userParam.isChecked( ) && DatabaseService.getService( ).isPluginJcaptchaEnable( ) )
        {
            bIsEnable = true;
        }

        return bIsEnable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countUserPasswordHistoryFromDate( Timestamp minDate, int nUserId, Plugin plugin )
    {
        return DatabaseUserHome.countUserPasswordHistoryFromDate( minDate, nUserId, plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> selectUserPasswordHistory( int nUserID, Plugin plugin )
    {
        // the way we store password is incompatible with this function specified by plugin-mulutece
        return Collections.emptyList( );
    }
}
