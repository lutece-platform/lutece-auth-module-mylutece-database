/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;


/**
 *
 * DatabaseUserFactory
 *
 */
public final class DatabaseUserFactory
{
    private static final String BEAN_DATABASE_USER_FACTORY = "mylutece-database.databaseUserFactory";
    private String _strBeanDatabaseUser;
    private boolean _bIsEmailUsedAsLogin;

    /**
     * Private constructor
     */
    private DatabaseUserFactory(  )
    {
    }

    /**
     * Get the instance of the factory
     * @return the instance of the factory
     */
    public static DatabaseUserFactory getFactory(  )
    {
        return SpringContextService.getBean( BEAN_DATABASE_USER_FACTORY );
    }

    /**
     * Set the bean database user
     * @param strBeanDatabaseUser the bean database user
     */
    public void setBeanDatabaseUser( String strBeanDatabaseUser )
    {
        _strBeanDatabaseUser = strBeanDatabaseUser;
    }

    /**
     * Set true if the email is used as login, false otherwise
     * @param bIsEmailUsedAsLogin true if the email is used as login, false otherwise
     */
    public void setEmailUsedAsLogin( boolean bIsEmailUsedAsLogin )
    {
        _bIsEmailUsedAsLogin = bIsEmailUsedAsLogin;
    }

    /**
     * Check if the email is used as login
     * @return true if the email is used as login, false otherwise
     */
    public boolean isEmailUsedAsLogin(  )
    {
        return _bIsEmailUsedAsLogin;
    }

    /**
    * Instanciate a new {@link DatabaseUser} defined in <b>database_context.xml</b>
    * @return a new instance of {@link DatabaseUser}
    */
    public DatabaseUser newDatabaseUser(  )
    {
        DatabaseUser databaseUser = null;

        try
        {
            databaseUser = SpringContextService.getBean( _strBeanDatabaseUser );
        }
        catch ( BeanDefinitionStoreException e )
        {
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "DatabaseUserFactory ERROR : could not load bean '" + e.getBeanName(  ) +
                    "' - CAUSE : " + e.getMessage(  ) );
            }
        }
        catch ( NoSuchBeanDefinitionException e )
        {
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "DatabaseUserFactory ERROR : could not load bean '" + e.getBeanName(  ) +
                    "' - CAUSE : " + e.getMessage(  ) );
            }
        }
        catch ( CannotLoadBeanClassException e )
        {
            if ( AppLogService.isDebugEnabled(  ) )
            {
                AppLogService.debug( "DatabaseUserFactory ERROR : could not load bean '" + e.getBeanName(  ) +
                    "' - CAUSE : " + e.getMessage(  ) );
            }
        }

        // New DatabaseUser by default if the plugin cannot load the DatabaseUser by Spring 
        if ( databaseUser == null )
        {
            databaseUser = new DatabaseUser(  );
        }

        return databaseUser;
    }
}
