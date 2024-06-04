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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldListenerService;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldService;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.csv.CSVMessageLevel;
import fr.paris.lutece.portal.service.csv.CSVReaderService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Import database users from a CSV file
 */
public class ImportDatabaseUserService extends CSVReaderService
{
    private static final String MESSAGE_NO_STATUS = "module.mylutece.database.import_users_from_file.importNoStatus";
    private static final String MESSAGE_ACCESS_CODE_ALREADY_USED = "module.mylutece.database.message.user_exist";
    private static final String MESSAGE_EMAIL_ALREADY_USED = "module.mylutece.database.message.user_exist";
    private static final String MESSAGE_USERS_IMPORTED = "module.mylutece.database.import_users_from_file.usersImported";
    private static final String MESSAGE_ERROR_MIN_NUMBER_COLUMNS = "module.mylutece.database.import_users_from_file.messageErrorMinColumnNumber";
    private static final String MESSAGE_ACCOUNT_IMPORTED_MAIL_SUBJECT = "module.mylutece.database.import_users_from_file.email.mailSubject";
    private static final String MESSAGE_ERROR_IMPORTING_ATTRIBUTES = "module.mylutece.database.import_users_from_file.errorImportingAttributes";
    private static final String PROPERTY_NO_REPLY_EMAIL = "mail.noreply.email";
    private static final String PROPERTY_IMPORT_EXPORT_USER_SEPARATOR = "lutece.importExportUser.defaultSeparator";
    private static final String PROPERTY_SITE_NAME = "lutece.name";
    private static final String TEMPLATE_MAIL_USER_IMPORTED = "admin/plugins/mylutece/modules/database/mail_user_imported.html";
    private static final String MARK_SITE_NAME = "site_name";
    private static final String MARK_USER = "user";
    private static final String MARK_SITE_LINK = "site_link";
    private static final String MARK_PASSWORD = "password";
    private static final String CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR = ":";
    private static final String CONSTANT_ROLE = "role";
    private static final String CONSTANT_GROUP = "group";
    private static final int CONSTANT_MINIMUM_COLUMNS_PER_LINE = 7;
    private Character _strAttributesSeparator;
    private boolean _bUpdateExistingUsers;
    private IDatabaseUserParameterService _userParamService = CDI.current( ).select( IDatabaseUserParameterService.class ).get( );

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> readLineOfCSVFile( String [ ] strLineDataArray, int nLineNumber, Locale locale, String strBaseUrl )
    {
        Plugin databasePlugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        Plugin mylutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<CSVMessageDescriptor> listMessages = new ArrayList<>( );
        int nIndex = 0;

        String strAccessCode = strLineDataArray [nIndex++];
        String strLastName = strLineDataArray [nIndex++];
        String strFirstName = strLineDataArray [nIndex++];
        String strEmail = strLineDataArray [nIndex++];

        boolean bUpdateUser = getUpdateExistingUsers( );
        int nUserId = 0;

        if ( bUpdateUser )
        {
            int nAccessCodeUserId = DatabaseUserHome.findDatabaseUserIdFromLogin( strAccessCode, databasePlugin );

            if ( nAccessCodeUserId > 0 )
            {
                nUserId = nAccessCodeUserId;
            }

            bUpdateUser = nUserId > 0;
        }

        String strStatus = strLineDataArray [nIndex++];
        int nStatus = 0;

        if ( StringUtils.isNotEmpty( strStatus ) && StringUtils.isNumeric( strStatus ) )
        {
            nStatus = Integer.parseInt( strStatus );
        }
        else
        {
            Object [ ] args = {
                    strLastName, strFirstName, nStatus
            };
            String strMessage = I18nService.getLocalizedString( MESSAGE_NO_STATUS, args, locale );
            CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, nLineNumber, strMessage );
            listMessages.add( message );
        }

        // We ignore the password max valid date attribute because we changed the password.
        nIndex++;
        // We ignore the account max valid date attribute
        nIndex++;

        DatabaseUser user = new DatabaseUser( );

        user.setLogin( strAccessCode );
        user.setLastName( strLastName );
        user.setFirstName( strFirstName );
        user.setEmail( strEmail );
        user.setStatus( nStatus );

        if ( bUpdateUser )
        {
            user.setUserId( nUserId );
            // We update the user
            DatabaseService.getService( ).doUpdateUser( user, databasePlugin );
        }
        else
        {
            // We create the user
            String strPassword = SecurityUtils.makePassword( _userParamService, databasePlugin );
            DatabaseService.getService( ).doCreateUser( user, strPassword, databasePlugin );
            notifyUserAccountCreated( user, strPassword, locale, AppPathService.getProdUrl( strBaseUrl ) );
        }

        // We remove old roles, groups and attributes of the user
        DatabaseHome.removeRolesForUser( user.getUserId( ), databasePlugin );
        DatabaseHome.removeGroupsForUser( user.getUserId( ), databasePlugin );
        MyLuteceUserFieldService.doRemoveUserFields( user.getUserId( ), locale );

        // We get every attributes, roles and groups of the user
        Map<Integer, List<String>> mapAttributesValues = new HashMap<>( );
        List<String> listRoles = new ArrayList<>( );
        List<String> listGroups = new ArrayList<>( );

        while ( nIndex < strLineDataArray.length )
        {
            String strValue = strLineDataArray [nIndex];

            if ( StringUtils.isNotBlank( strValue ) && ( strValue.indexOf( getAttributesSeparator( ) ) > 0 ) )
            {
                int nSeparatorIndex = strValue.indexOf( getAttributesSeparator( ) );
                String strLineId = strValue.substring( 0, nSeparatorIndex );

                if ( StringUtils.isNotBlank( strLineId ) )
                {
                    if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_ROLE ) )
                    {
                        listRoles.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else
                        if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_GROUP ) )
                        {
                            listGroups.add( strValue.substring( nSeparatorIndex + 1 ) );
                        }
                        else
                        {
                            int nAttributeId = Integer.parseInt( strLineId );

                            String strAttributeValue = strValue.substring( nSeparatorIndex + 1 );
                            List<String> listValues = mapAttributesValues.get( nAttributeId );

                            if ( listValues == null )
                            {
                                listValues = new ArrayList<>( );
                            }

                            listValues.add( strAttributeValue );
                            mapAttributesValues.put( nAttributeId, listValues );
                        }
                }
            }

            nIndex++;
        }

        // We create roles
        for ( String strRole : listRoles )
        {
            DatabaseHome.addRoleForUser( user.getUserId( ), strRole, databasePlugin );
        }

        // We create groups
        for ( String strGoup : listGroups )
        {
            DatabaseHome.addGroupForUser( user.getUserId( ), strGoup, databasePlugin );
        }

        // We save the attributes found
        List<IAttribute> listAttributes = AttributeHome.findAll( locale, mylutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<String> listValues = mapAttributesValues.get( attribute.getIdAttribute( ) );

            if ( CollectionUtils.isNotEmpty( listValues ) )
            {
                int nIdField = 0;
                boolean bMyLuteceAttribute = ( attribute.getPlugin( ) == null )
                        || StringUtils.equals( attribute.getPlugin( ).getName( ), MyLutecePlugin.PLUGIN_NAME );

                for ( String strValue : listValues )
                {
                    int nSeparatorIndex = strValue.indexOf( getAttributesSeparator( ) );

                    if ( nSeparatorIndex >= 0 )
                    {
                        nIdField = 0;

                        try
                        {
                            nIdField = Integer.parseInt( strValue.substring( 0, nSeparatorIndex ) );
                        }
                        catch( NumberFormatException e )
                        {
                            nIdField = 0;
                        }

                        strValue = strValue.substring( nSeparatorIndex + 1 );
                    }
                    else
                    {
                        nIdField = 0;
                    }

                    String [ ] strValues = {
                            strValue
                    };

                    try
                    {
                        List<MyLuteceUserField> listUserFields = attribute.getUserFieldsData( strValues, user.getUserId( ) );

                        for ( MyLuteceUserField userField : listUserFields )
                        {
                            if ( userField != null )
                            {
                                userField.getAttributeField( ).setIdField( nIdField );
                                MyLuteceUserFieldHome.create( userField, mylutecePlugin );
                            }
                        }

                        if ( !bMyLuteceAttribute )
                        {
                            for ( MyLuteceUserFieldListenerService myLuteceUserFieldListenerService : CDI.current( )
                                    .select( MyLuteceUserFieldListenerService.class ).stream( ).toList( ) )
                            {
                                myLuteceUserFieldListenerService.doCreateUserFields( user.getUserId( ), listUserFields, locale );
                            }
                        }
                    }
                    catch( Exception e )
                    {
                        AppLogService.error( e.getMessage( ), e );

                        String strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_IMPORTING_ATTRIBUTES, locale );
                        CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strErrorMessage );
                        listMessages.add( error );
                    }
                }
            }
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> checkLineOfCSVFile( String [ ] strLineDataArray, int nLineNumber, Locale locale )
    {
        int nMinColumnNumber = CONSTANT_MINIMUM_COLUMNS_PER_LINE;
        Plugin databasePlugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        List<CSVMessageDescriptor> listMessages = new ArrayList<>( );

        if ( ( strLineDataArray == null ) || ( strLineDataArray.length < nMinColumnNumber ) )
        {
            int nNbCol;

            if ( strLineDataArray == null )
            {
                nNbCol = 0;
            }
            else
            {
                nNbCol = strLineDataArray.length;
            }

            Object [ ] args = {
                    nNbCol, nMinColumnNumber
            };
            String strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_MIN_NUMBER_COLUMNS, args, locale );
            CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strErrorMessage );
            listMessages.add( error );

            return listMessages;
        }

        if ( !getUpdateExistingUsers( ) )
        {
            String strAccessCode = strLineDataArray [0];
            String strEmail = strLineDataArray [3];

            if ( DatabaseUserHome.findDatabaseUserIdFromLogin( strAccessCode, databasePlugin ) > 0 )
            {
                String strMessage = I18nService.getLocalizedString( MESSAGE_ACCESS_CODE_ALREADY_USED, locale );
                CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strMessage );
                listMessages.add( error );
            }
            else
            {
                Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersListForEmail( strEmail, databasePlugin );

                if ( CollectionUtils.isNotEmpty( listUsers ) )
                {
                    String strMessage = I18nService.getLocalizedString( MESSAGE_EMAIL_ALREADY_USED, locale );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strMessage );
                    listMessages.add( error );
                }
            }
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> getEndOfProcessMessages( int nNbLineParses, int nNbLinesWithoutErrors, Locale locale )
    {
        List<CSVMessageDescriptor> listMessages = new ArrayList<>( );
        Object [ ] args = {
                nNbLineParses, nNbLinesWithoutErrors
        };
        String strMessageContent = I18nService.getLocalizedString( MESSAGE_USERS_IMPORTED, args, locale );
        CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, 0, strMessageContent );
        listMessages.add( message );

        return listMessages;
    }

    /**
     * Notify a user of the creation of his account and give him his credentials
     * 
     * @param user
     *            the user to notify
     * @param strPassword
     *            The password of the user
     * @param locale
     *            The locale
     * @param strProdUrl
     *            The prod URL
     */
    private void notifyUserAccountCreated( DatabaseUser user, String strPassword, Locale locale, String strProdUrl )
    {
        String strSenderEmail = AppPropertiesService.getProperty( PROPERTY_NO_REPLY_EMAIL );
        String strSiteName = AppPropertiesService.getProperty( PROPERTY_SITE_NAME );

        String strEmailSubject = I18nService.getLocalizedString( MESSAGE_ACCOUNT_IMPORTED_MAIL_SUBJECT, new String [ ] {
                strSiteName
        }, locale );
        String strBaseURL = strProdUrl;
        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_USER, user );
        model.put( MARK_SITE_NAME, strSiteName );
        model.put( MARK_SITE_LINK, MailService.getSiteLink( strBaseURL, true ) );
        model.put( MARK_PASSWORD, strPassword );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MAIL_USER_IMPORTED, locale, model );

        MailService.sendMailHtml( user.getEmail( ), strSenderEmail, strSenderEmail, strEmailSubject, template.getHtml( ) );
    }

    /**
     * Get the separator used for attributes of admin users.
     * 
     * @return The separator
     */
    public Character getAttributesSeparator( )
    {
        if ( _strAttributesSeparator == null )
        {
            _strAttributesSeparator = AppPropertiesService.getProperty( PROPERTY_IMPORT_EXPORT_USER_SEPARATOR, CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR )
                    .charAt( 0 );
        }

        return _strAttributesSeparator;
    }

    /**
     * Get the update users flag
     * 
     * @return True if existing users should be updated, false if they should be ignored.
     */
    public boolean getUpdateExistingUsers( )
    {
        return _bUpdateExistingUsers;
    }

    /**
     * Set the update users flag
     * 
     * @param bUpdateExistingUsers
     *            True if existing users should be updated, false if they should be ignored.
     */
    public void setUpdateExistingUsers( boolean bUpdateExistingUsers )
    {
        this._bUpdateExistingUsers = bUpdateExistingUsers;
    }
}
