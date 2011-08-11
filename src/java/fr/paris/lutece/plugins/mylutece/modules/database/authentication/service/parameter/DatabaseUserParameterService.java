/*
 * Copyright (c) 2002-2011, Mairie de Paris
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

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import org.apache.commons.lang.StringUtils;


/**
 *
 * DatabaseUserParameterService
 *
 */
public final class DatabaseUserParameterService
{
    private static final String BEAN_DATABASE_USER_PARAMETER_SERVICE = "mylutece-database.databaseUserParameterService";

    // PARAMETERS
    private static final String PARAMETER_ENABLE_PASSWORD_ENCRYPTION = "enable_password_encryption";
    private static final String PARAMETER_ENCRYPTION_ALGORITHM = "encryption_algorithm";
    private static final String PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL = "account_creation_validation_email";
    private static final String PARAMETER_ENABLE_JCAPTCHA = "enable_jcaptcha";

    /**
     * Private constructor
     */
    private DatabaseUserParameterService(  )
    {
    }

    /**
     * Get the instance of the service
     * @return the instance of the service
     */
    public static DatabaseUserParameterService getService(  )
    {
        return (DatabaseUserParameterService) SpringContextService.getPluginBean( DatabasePlugin.PLUGIN_NAME,
            BEAN_DATABASE_USER_PARAMETER_SERVICE );
    }

    /**
     * Get the parameter from a given key
     * @param strParameterKey the key
     * @param plugin the plugin
     * @return the parameter
     */
    public ReferenceItem findByKey( String strParameterKey, Plugin plugin )
    {
        return DatabaseUserParameterHome.findByKey( strParameterKey, plugin );
    }

    /**
     * Update a parameter
     * @param userParam the parameter
     * @param plugin the plugin
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
     * @param plugin the plugin
     * @return a ReferenceList
     */
    public ReferenceList findAll( Plugin plugin )
    {
        return DatabaseUserParameterHome.findAll( plugin );
    }

    /**
     * Check if the passwords must be encrypted or not
     * @param plugin the plugin
     * @return true if they are encrypted, false otherwise
     */
    public boolean isPasswordEncrypted( Plugin plugin )
    {
        boolean bIsPasswordEncrypted = false;
        ReferenceItem userParam = findByKey( PARAMETER_ENABLE_PASSWORD_ENCRYPTION, plugin );

        if ( ( userParam != null ) && userParam.isChecked(  ) )
        {
            bIsPasswordEncrypted = true;
        }

        return bIsPasswordEncrypted;
    }

    /**
     * Get the encryption algorithm
     * @param plugin the plugin
     * @return the encryption algorithm
     */
    public String getEncryptionAlgorithm( Plugin plugin )
    {
        String strAlgorithm = StringUtils.EMPTY;
        ReferenceItem userParam = findByKey( PARAMETER_ENCRYPTION_ALGORITHM, plugin );

        if ( userParam != null )
        {
            strAlgorithm = userParam.getName(  );
        }

        return strAlgorithm;
    }

    /**
     * Check if the account creation must be validated by email
     * @param plugin the plugin
     * @return true if it must be validated by email, false otherwise
     */
    public boolean isAccountCreationValidationEmail( Plugin plugin )
    {
        boolean bIsValidationEmail = false;
        ReferenceItem userParam = findByKey( PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL, plugin );

        if ( ( userParam != null ) && userParam.isChecked(  ) )
        {
            bIsValidationEmail = true;
        }

        return bIsValidationEmail;
    }

    /**
     * Check if the jcaptcha is enable or not
     * @param plugin the plugin
     * @return true if it is enable, false otherwise
     */
    public boolean isJcaptchaEnable( Plugin plugin )
    {
        boolean bIsEnable = false;
        ReferenceItem userParam = findByKey( PARAMETER_ENABLE_JCAPTCHA, plugin );

        if ( ( userParam != null ) && userParam.isChecked(  ) &&
                DatabaseService.getService(  ).isPluginJcaptchaEnable(  ) )
        {
            bIsEnable = true;
        }

        return bIsEnable;
    }
}
