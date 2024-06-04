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

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.IAccountLifeTimeService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.DatabaseTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;

import java.sql.Timestamp;

import java.text.DateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service to handle account life time
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseAccountLifeTimeService" )
public class DatabaseAccountLifeTimeService implements IAccountLifeTimeService
{
    private static final String PARAMETER_MYLUTECE_DATABASE_EXPIRATION_MAIL = "mylutece_database_expiration_mail";
    private static final String PARAMETER_MYLUTECE_DATABASE_FIRST_ALERT_MAIL = "mylutece_database_first_alert_mail";
    private static final String PARAMETER_MYLUTECE_DATABASE_OTHER_ALERT_MAIL = "mylutece_database_other_alert_mail";
    private static final String PARAMETER_NOTIFY_PASSWORD_EXPIRED = "mylutece_database_password_expired";
    private static final String MARK_LAST_NAME = "last_name";
    private static final String MARK_FIRST_NAME = "first_name";
    private static final String MARK_DATE_VALID = "date_valid";
    private static final String MARK_URL = "url";
    private static final String MARK_USER_ID = "user_id";
    private static final String MARK_REF = "ref";
    private static final String PROPERTY_ACCOUNT_REF_ENCRYPT_ALGO = "mylutece-database.account_life_time.refEncryptionAlgorythm";
    private static final String JSP_URL_REACTIVATE_ACCOUNT = "/jsp/site/Portal.jsp?page=mylutecedatabase&action=reactivateAccount";
    private static final String CONSTANT_AND = "&";
    private static final String CONSTANT_EQUAL = "=";
    private Plugin _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

    /**
     * Get an instance of the service
     * 
     * @return An instance of the service
     * 
     * @deprecated Use CDI inject
     */
    @Deprecated
    public static DatabaseAccountLifeTimeService getService( )
    {
        return CDI.current( ).select( DatabaseAccountLifeTimeService.class ).get( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin( )
    {
        return _plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersWithExpiredLifeTimeList( Timestamp currentTimestamp )
    {
        return DatabaseUserHome.getIdUsersWithExpiredLifeTimeList( currentTimestamp, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendFirstAlert( Timestamp alertMaxDate )
    {
        return DatabaseUserHome.getIdUsersToSendFirstAlert( alertMaxDate, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendOtherAlert( Timestamp alertMaxDate, Timestamp timeBetweenAlerts, int maxNumberAlerts )
    {
        return DatabaseUserHome.getIdUsersToSendOtherAlert( alertMaxDate, timeBetweenAlerts, maxNumberAlerts, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNbAlert( List<Integer> listIdUser )
    {
        DatabaseUserHome.updateNbAlert( listIdUser, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChangePassword( List<Integer> listIdUser )
    {
        DatabaseUserHome.updateChangePassword( listIdUser, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserStatusExpired( List<Integer> listIdUser )
    {
        DatabaseUserHome.updateUserStatus( listIdUser, DatabaseUser.STATUS_EXPIRED, _plugin );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExpirationtMailBody( )
    {
        return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DATABASE_EXPIRATION_MAIL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstAlertMailBody( )
    {
        return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DATABASE_FIRST_ALERT_MAIL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOtherAlertMailBody( )
    {
        return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DATABASE_OTHER_ALERT_MAIL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPasswordExpiredMailBody( )
    {
        return DatabaseTemplateService.getTemplateFromKey( PARAMETER_NOTIFY_PASSWORD_EXPIRED );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addParametersToModel( Map<String, String> model, Integer nIdUser )
    {
        DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nIdUser, _plugin );
        DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.SHORT, Locale.getDefault( ) );

        String accountMaxValidDate = dateFormat.format( new Date( user.getAccountMaxValidDate( ).getTime( ) ) );

        StringBuilder sbUrl = new StringBuilder( );
        // FIXME : get base URL in case the prod URL is null
        sbUrl.append( AppPathService.getProdUrl( "" ) );
        sbUrl.append( JSP_URL_REACTIVATE_ACCOUNT );
        sbUrl.append( CONSTANT_AND );
        sbUrl.append( MARK_USER_ID );
        sbUrl.append( CONSTANT_EQUAL );
        sbUrl.append( nIdUser.toString( ) );
        sbUrl.append( CONSTANT_AND );
        sbUrl.append( MARK_REF );
        sbUrl.append( CONSTANT_EQUAL );
        sbUrl.append( CryptoService.encrypt( Long.toString( user.getAccountMaxValidDate( ).getTime( ) ),
                AppPropertiesService.getProperty( PROPERTY_ACCOUNT_REF_ENCRYPT_ALGO ) ) );

        String activationURL = sbUrl.toString( );

        model.put( MARK_DATE_VALID, accountMaxValidDate );
        model.put( MARK_URL, activationURL );
        model.put( MARK_LAST_NAME, user.getLastName( ) );
        model.put( MARK_FIRST_NAME, user.getFirstName( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserMainEmail( int nIdUser )
    {
        DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nIdUser, _plugin );

        return user.getEmail( );
    }

    @Override
    public List<Integer> getIdUsersWithExpiredPasswordsList( Timestamp currentTimestamp )
    {
        return DatabaseUserHome.getIdUsersWithExpiredPasswordsList( currentTimestamp, _plugin );
    }
}
