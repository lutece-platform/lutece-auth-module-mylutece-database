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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.mylutece.authentication.MultiLuteceAuthentication;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeField;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldFilter;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFieldListener;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserRoleRemovalListener;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminAuthenticationService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.role.RoleRemovalListenerService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.DatabaseTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.password.IPasswordFactory;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.xml.XmlUtil;

/**
 *
 * DatabaseService
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseService" )
public class DatabaseService
{

    // CONSTANTS
    private static final String AMPERSAND = "&";
    private static final String PLUGIN_JCAPTCHA = "jcaptcha";
    private static final String CONSTANT_XML_USER = "user";
    private static final String CONSTANT_XML_ACCESS_CODE = "access_code";
    private static final String CONSTANT_XML_LAST_NAME = "last_name";
    private static final String CONSTANT_XML_FIRST_NAME = "first_name";
    private static final String CONSTANT_XML_EMAIL = "email";
    private static final String CONSTANT_XML_STATUS = "status";
    private static final String CONSTANT_XML_PASSWORD_MAX_VALID_DATE = "password_max_valid_date";
    private static final String CONSTANT_XML_ACCOUNT_MAX_VALID_DATE = "account_max_valid_date";
    private static final String CONSTANT_XML_ROLES = "roles";
    private static final String CONSTANT_XML_GROUPS = "groups";
    private static final String CONSTANT_XML_ROLE = "role";
    private static final String CONSTANT_XML_GROUP = "group";
    private static final String CONSTANT_XML_ATTRIBUTES = "attributes";
    private static final String CONSTANT_XML_ATTRIBUTE = "attribute";
    private static final String CONSTANT_XML_ATTRIBUTE_ID = "attribute-id";
    private static final String CONSTANT_XML_ATTRIBUTE_FIELD_ID = "attribute-field-id";
    private static final String CONSTANT_XML_ATTRIBUTE_VALUE = "attribute-value";

    // MARKS
    private static final String MARK_SEARCH_IS_SEARCH = "search_is_search";
    private static final String MARK_SORT_SEARCH_ATTRIBUTE = "sort_search_attribute";
    private static final String MARK_SEARCH_USER_FILTER = "search_user_filter";
    private static final String MARK_SEARCH_MYLUTECE_USER_FIELD_FILTER = "search_mylutece_user_field_filter";
    private static final String MARK_ATTRIBUTES_LIST = "attributes_list";
    private static final String MARK_IS_PLUGIN_JCAPTCHA_ENABLE = "is_plugin_jcatpcha_enable";
    private static final String MARK_LOGIN_URL = "login_url";
    private static final String MARK_NEW_PASSWORD = "new_password";
    private static final String MARK_ENABLE_JCAPTCHA = "enable_jcaptcha";
    private static final String MARK_SITE_LINK = "site_link";
    private static final String MARK_BANNED_DOMAIN_NAMES = "banned_domain_names";

    // PARAMETERS
    private static final String PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL = "account_creation_validation_email";
    private static final String PARAMETER_ACCOUNT_REACTIVATED_MAIL_SENDER = "account_reactivated_mail_sender";
    private static final String PARAMETER_ACCOUNT_REACTIVATED_MAIL_SUBJECT = "account_reactivated_mail_subject";
    private static final String PARAMETER_ACCOUNT_REACTIVATED_MAIL_BODY = "mylutece_database_account_reactivated_mail";
    private static final String PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED = "mylutece_database_mailPasswordEncryptionChanged";
    private static final String PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED_SENDER = "mail_password_encryption_changed_sender";
    private static final String PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED_SUBJECT = "mail_password_encryption_changed_subject";
    private static final String PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL = "auto_login_after_validation_email";

    @Inject
    private IDatabaseUserParameterService _userParamService;
    @Inject
    private BaseAuthentication _baseAuthentication;
    @Inject
    private IPasswordFactory _passwordFactory;

    /**
     * Initialize the Database service
     *
     */
    @PostConstruct
    public void init( )
    {
        RoleRemovalListenerService.getService( ).registerListener( new DatabaseUserRoleRemovalListener( ) );
        DatabaseMyLuteceUserFieldListenerService.getService( ).registerListener( new DatabaseUserFieldListener( ) );

        if ( _baseAuthentication != null )
        {
            MultiLuteceAuthentication.registerAuthentication( _baseAuthentication );
        }
        else
        {
            AppLogService.error( "BaseAuthentication not found, please check your database_context.xml configuration" );
        }
    }

    /**
     * Returns the instance of the singleton
     * 
     * @return The instance of the singleton
     */
    @Deprecated
    public static synchronized DatabaseService getService( )
    {
        return CDI.current( ).select( DatabaseService.class ).get( );
    }

    /**
     * Build the advanced parameters management
     * 
     * @param user
     *            the admin user
     * @return The model for the advanced parameters
     */
    public Map<String, Object> getManageAdvancedParameters( AdminUser user )
    {
        Map<String, Object> model = new HashMap<>( );
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        if ( RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, DatabaseResourceIdService.PERMISSION_MANAGE,
                (User) user ) )
        {
            model.put( MARK_IS_PLUGIN_JCAPTCHA_ENABLE, isPluginJcaptchaEnable( ) );

            if ( isPluginJcaptchaEnable( ) )
            {
                model.put( MARK_ENABLE_JCAPTCHA, SecurityUtils.getBooleanSecurityParameter( _userParamService, plugin, MARK_ENABLE_JCAPTCHA ) );
            }

            model.put( PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL,
                    SecurityUtils.getBooleanSecurityParameter( _userParamService, plugin, PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL ) );

            model.put( PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL,
                    SecurityUtils.getBooleanSecurityParameter( _userParamService, plugin, PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL ) );

            model.put( MARK_BANNED_DOMAIN_NAMES, SecurityUtils.getLargeSecurityParameter( _userParamService, plugin, MARK_BANNED_DOMAIN_NAMES ) );

            model = SecurityUtils.checkSecurityParameters( _userParamService, model, plugin );
        }

        return model;
    }

    /**
     * Check if an Lutece user should be visible to the user according its workgroup
     * 
     * @param user
     *            the Lutece user
     * @param adminUser
     *            the admin user
     * @param plugin
     *            the plugin
     * @return true if the Lutece user should be visible, false otherwise
     */
    public boolean isAuthorized( DatabaseUser user, AdminUser adminUser, Plugin plugin )
    {
        boolean bHasRole = false;
        List<String> userRoleKeyList = DatabaseHome.findUserRolesFromLogin( user.getLogin( ), plugin );

        for ( String userRoleKey : userRoleKeyList )
        {
            bHasRole = true;

            Role role = RoleHome.findByPrimaryKey( userRoleKey );

            if ( AdminWorkgroupService.isAuthorized( role, (User) adminUser ) )
            {
                return true;
            }
        }

        List<String> userGroupKeyList = DatabaseHome.findUserGroupsFromLogin( user.getLogin( ), plugin );

        for ( String userGroupKey : userGroupKeyList )
        {
            List<String> groupRoleKeyList = GroupRoleHome.findGroupRoles( userGroupKey, plugin );

            for ( String groupRoleKey : groupRoleKeyList )
            {
                bHasRole = true;

                Role role = RoleHome.findByPrimaryKey( groupRoleKey );

                if ( AdminWorkgroupService.isAuthorized( role, (User) adminUser ) )
                {
                    return true;
                }
            }
        }

        return !bHasRole;
    }

    /**
     * Get authorized users list
     * 
     * @param adminUser
     *            the admin user
     * @param plugin
     *            the plugin
     * @return a list of users
     */
    public List<DatabaseUser> getAuthorizedUsers( AdminUser adminUser, Plugin plugin )
    {
        Collection<DatabaseUser> userList = DatabaseUserHome.findDatabaseUsersList( plugin );
        List<DatabaseUser> authorizedUserList = new ArrayList<>( );

        for ( DatabaseUser user : userList )
        {
            if ( isAuthorized( user, adminUser, plugin ) )
            {
                authorizedUserList.add( user );
            }
        }

        return authorizedUserList;
    }

    /**
     * Get the filtered list of database users
     * 
     * @param duFilter
     *            The filter
     * @param bIsSearch
     *            True if the user used search filters, false otherwise
     * @param listUsers
     *            the initial list to filter
     * @param request
     *            HttpServletRequest
     * @param model
     *            Map
     * @param url
     *            UrlItem
     * @return the filtered list
     */
    public List<DatabaseUser> getFilteredUsersInterface( DatabaseUserFilter duFilter, boolean bIsSearch, List<DatabaseUser> listUsers,
            HttpServletRequest request, Map<String, Object> model, UrlItem url )
    {
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<DatabaseUser> filteredUsers = getListFilteredUsers( request, duFilter, listUsers );
        MyLuteceUserFieldFilter mlFieldFilter = new MyLuteceUserFieldFilter( );
        mlFieldFilter.setMyLuteceUserFieldFilter( request, request.getLocale( ) );

        List<IAttribute> listAttributes = AttributeHome.findAll( request.getLocale( ), myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( attribute.getIdAttribute( ), myLutecePlugin );
            attribute.setListAttributeFields( listAttributeFields );
        }

        String strSortSearchAttribute = StringUtils.EMPTY;

        if ( bIsSearch )
        {
            duFilter.setUrlAttributes( url );

            if ( !StringUtils.EMPTY.equals( duFilter.getUrlAttributes( ) ) )
            {
                strSortSearchAttribute = AMPERSAND + duFilter.getUrlAttributes( );
            }

            mlFieldFilter.setUrlAttributes( url );

            if ( !StringUtils.EMPTY.equals( mlFieldFilter.getUrlAttributes( ) ) )
            {
                strSortSearchAttribute += ( AMPERSAND + mlFieldFilter.getUrlAttributes( ) );
            }
        }

        model.put( MARK_SEARCH_IS_SEARCH, bIsSearch );
        model.put( MARK_SEARCH_USER_FILTER, duFilter );
        model.put( MARK_SORT_SEARCH_ATTRIBUTE, strSortSearchAttribute );
        model.put( MARK_SEARCH_MYLUTECE_USER_FIELD_FILTER, mlFieldFilter );
        model.put( MARK_ATTRIBUTES_LIST, listAttributes );

        return filteredUsers;
    }

    /**
     * Get th list of filteredUsers
     * 
     * @param request
     *            the HTTP request
     * @param duFilter
     *            the filter
     * @param listUsers
     *            the list of users
     * @return a list of {@link DatabaseUser}
     */
    public List<DatabaseUser> getListFilteredUsers( HttpServletRequest request, DatabaseUserFilter duFilter, List<DatabaseUser> listUsers )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        List<DatabaseUser> listFilteredUsers = DatabaseUserHome.findDatabaseUsersListByFilter( duFilter, plugin );
        List<DatabaseUser> listAvailableUsers = new ArrayList<>( );

        for ( DatabaseUser filteredUser : listFilteredUsers )
        {
            for ( DatabaseUser user : listUsers )
            {
                if ( filteredUser.getUserId( ) == user.getUserId( ) )
                {
                    listAvailableUsers.add( user );
                }
            }
        }

        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<DatabaseUser> filteredUsers = new ArrayList<>( );

        MyLuteceUserFieldFilter mlFieldFilter = new MyLuteceUserFieldFilter( );
        mlFieldFilter.setMyLuteceUserFieldFilter( request, request.getLocale( ) );

        List<Integer> listFilteredUserIdsByUserFields = MyLuteceUserFieldHome.findUsersByFilter( mlFieldFilter, myLutecePlugin );

        if ( listFilteredUserIdsByUserFields != null )
        {
            for ( DatabaseUser filteredUser : listAvailableUsers )
            {
                for ( Integer nFilteredUserIdByUserField : listFilteredUserIdsByUserFields )
                {
                    if ( filteredUser.getUserId( ) == nFilteredUserIdByUserField )
                    {
                        filteredUsers.add( filteredUser );
                    }
                }
            }
        }
        else
        {
            filteredUsers = listAvailableUsers;
        }

        return filteredUsers;
    }

    /**
     * Do create a new database user
     * 
     * @param user
     *            the user
     * @param strPassword
     *            the password
     * @param plugin
     *            the plugin
     * @return the new database user with a new ID
     */
    public DatabaseUser doCreateUser( DatabaseUser user, String strPassword, Plugin plugin )
    {
        user.setPasswordMaxValidDate( SecurityUtils.getPasswordMaxValidDate( _userParamService, plugin ) );
        user.setAccountMaxValidDate( SecurityUtils.getAccountMaxValidDate( _userParamService, plugin ) );

        return DatabaseUserHome.create( user, _passwordFactory.getPasswordFromCleartext( strPassword ), plugin );
    }

    /**
     * Do modify the password
     * 
     * @param user
     *            the DatabaseUser
     * @param strPassword
     *            the new password not encrypted
     * @param plugin
     *            the plugin
     */
    public void doModifyPassword( DatabaseUser user, String strPassword, Plugin plugin )
    {
        // Updates password
        if ( StringUtils.isNotBlank( strPassword ) )
        {
            DatabaseUser userStored = DatabaseUserHome.findByPrimaryKey( user.getUserId( ), plugin );

            if ( userStored != null )
            {
                userStored.setPasswordMaxValidDate( SecurityUtils.getPasswordMaxValidDate( _userParamService, plugin ) );
                DatabaseUserHome.updatePassword( userStored, _passwordFactory.getPasswordFromCleartext( strPassword ), plugin );
            }
        }
    }

    /**
     * Do modify the reset password attribute
     * 
     * @param user
     *            the DatabaseUser
     * @param bNewValue
     *            the new value
     * @param plugin
     *            the plugin
     */
    public void doModifyResetPassword( DatabaseUser user, boolean bNewValue, Plugin plugin )
    {
        DatabaseUser userStored = DatabaseUserHome.findByPrimaryKey( user.getUserId( ), plugin );

        if ( userStored != null )
        {
            DatabaseUserHome.updateResetPassword( userStored, bNewValue, plugin );
        }
    }

    /**
     * Update the info of the user
     * 
     * @param user
     *            the user
     * @param plugin
     *            the plugin
     */
    public void doUpdateUser( DatabaseUser user, Plugin plugin )
    {
        DatabaseUserHome.update( user, plugin );
    }

    /**
     * Check the password
     * 
     * @param strUserGuid
     *            the user guid
     * @param strPassword
     *            the password
     * @param plugin
     *            the plugin
     * @return true if the password is the same as stored in the database, false otherwise
     */
    public boolean checkPassword( String strUserGuid, String strPassword, Plugin plugin )
    {
        return DatabaseUserHome.checkPassword( strUserGuid, strPassword, plugin );
    }

    /**
     * Check if the user is active or not
     * 
     * @param strUserName
     *            the user name
     * @param plugin
     *            the plugin
     * @return true if it is active, false otherwise
     */
    public boolean isUserActive( String strUserName, Plugin plugin )
    {
        boolean bIsActive = false;

        List<DatabaseUser> listUsers = (List<DatabaseUser>) DatabaseUserHome.findDatabaseUsersListForLogin( strUserName, plugin );

        if ( ( listUsers != null ) && !listUsers.isEmpty( ) )
        {
            DatabaseUser user = listUsers.get( 0 );
            bIsActive = user.isActive( );
        }

        return bIsActive;
    }

    /**
     * Check if the plugin jcaptcha is activated or not
     * 
     * @return true if it is activated, false otherwise
     */
    public boolean isPluginJcaptchaEnable( )
    {
        return PluginService.isPluginEnable( PLUGIN_JCAPTCHA );
    }

    /**
     * Change all user's password and notify them with an email.
     * 
     * @param strBaseURL
     *            The base url of the application
     * @param plugin
     *            The plugin
     * @param locale
     *            The locale to use
     */
    public void changeUserPasswordAndNotify( String strBaseURL, Plugin plugin, Locale locale )
    {
        // Alert all users their password have been reinitialized.
        Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersList( plugin );

        for ( DatabaseUser user : listUsers )
        {
            // Makes password
            String strPassword = SecurityUtils.makePassword( _userParamService, plugin );
            doModifyPassword( user, strPassword, plugin );

            if ( StringUtils.isNotBlank( user.getEmail( ) ) )
            {
                // Sends password by e-mail
                ReferenceItem referenceItem = _userParamService.findByKey( PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED_SENDER, plugin );
                String strSenderEmail = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );
                referenceItem = _userParamService.findByKey( PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED_SUBJECT, plugin );

                String strEmailSubject = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

                Map<String, Object> model = new HashMap<>( );
                model.put( MARK_NEW_PASSWORD, strPassword );
                model.put( MARK_LOGIN_URL, strBaseURL + AdminAuthenticationService.getInstance( ).getLoginPageUrl( ) );
                model.put( MARK_SITE_LINK, MailService.getSiteLink( strBaseURL, true ) );

                String strTemplate = DatabaseTemplateService.getTemplateFromKey( PARAMETER_MAIL_PASSWORD_ENCRYPTION_CHANGED );

                HtmlTemplate template = AppTemplateService.getTemplateFromStringFtl( strTemplate, locale, model );

                MailService.sendMailHtml( user.getEmail( ), strSenderEmail, strSenderEmail, strEmailSubject, template.getHtml( ) );
            }
        }
    }

    /**
     * Check whether a user must change his password
     * 
     * @param databaseUser
     *            The user to check
     * @param plugin
     *            The plugin
     * @return True if a user must change his password, false otherwise.
     */
    public boolean mustUserChangePassword( LuteceUser databaseUser, Plugin plugin )
    {
        return DatabaseHome.findResetPasswordFromLogin( databaseUser.getName( ), plugin );
    }

    /**
     * Log a password change in the password history
     * 
     * @param strPassword
     *            New password of the user
     * @param nUserId
     *            Id of the user
     * @param plugin
     *            The plugin
     */
    public void doInsertNewPasswordInHistory( String strPassword, int nUserId, Plugin plugin )
    {
        DatabaseUserHome.insertNewPasswordInHistory( _passwordFactory.getPasswordFromCleartext( strPassword ), nUserId, plugin );
    }

    /**
     * Update the user expiration date with new values, and notify him with an email.
     * 
     * @param nIdUser
     *            Id of the user to update
     * @param plugin
     *            The plugin
     */
    @SuppressWarnings( "deprecation" )
    public void updateUserExpirationDate( int nIdUser, Plugin plugin )
    {
        // We update the user account
        int nbMailSend = DatabaseUserHome.getNbAccountLifeTimeNotification( nIdUser, plugin );
        Timestamp newExpirationDate = SecurityUtils.getAccountMaxValidDate( _userParamService, plugin );
        DatabaseUserHome.updateUserExpirationDate( nIdUser, newExpirationDate, plugin );

        // We notify the user
        DatabaseAccountLifeTimeService accountLifeTimeService = new DatabaseAccountLifeTimeService( );
        String strUserMail = accountLifeTimeService.getUserMainEmail( nIdUser );

        if ( ( nbMailSend > 0 ) && StringUtils.isNotBlank( strUserMail ) )
        {
            String strBody = DatabaseTemplateService.getTemplateFromKey( PARAMETER_ACCOUNT_REACTIVATED_MAIL_BODY );

            ReferenceItem referenceItem = _userParamService.findByKey( PARAMETER_ACCOUNT_REACTIVATED_MAIL_SENDER, plugin );
            String strSender = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

            referenceItem = _userParamService.findByKey( PARAMETER_ACCOUNT_REACTIVATED_MAIL_SUBJECT, plugin );

            String strSubject = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

            Map<String, String> model = new HashMap<>( );
            accountLifeTimeService.addParametersToModel( model, nIdUser );

            HtmlTemplate template = AppTemplateService.getTemplateFromStringFtl( strBody, Locale.getDefault( ), model );
            MailService.sendMailHtml( strUserMail, strSender, strSender, strSubject, template.getHtml( ) );
        }
    }

    /**
     * Update a user last login date.
     * 
     * @param strLogin
     *            Login of the user to update
     * @param plugin
     *            The plugin
     */
    public void updateUserLastLoginDate( String strLogin, Plugin plugin )
    {
        DatabaseUserHome.updateUserLastLoginDate( strLogin, new Date( ), plugin );
    }

    /**
     * Get a XML string describing a given user
     * 
     * @param user
     *            The user to get the XML of.
     * @param bExportRoles
     *            True to export roles of the user, false otherwise.
     * @param bExportGroups
     *            True to export groups of the user, false otherwise.
     * @param bExportAttributes
     *            True to export attributes of the user, false otherwise.
     * @param listAttributes
     *            The list of attributes to export.
     * @param locale
     *            The locale
     * @return A string of XML with the information of the user.
     */
    public String getXmlFromUser( DatabaseUser user, boolean bExportRoles, boolean bExportGroups, boolean bExportAttributes, List<IAttribute> listAttributes,
            Locale locale )
    {
        Plugin databasePlugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        Plugin mylutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        StringBuffer sbXml = new StringBuffer( );
        DateFormat dateFormat = new SimpleDateFormat( );

        XmlUtil.beginElement( sbXml, CONSTANT_XML_USER );
        XmlUtil.addElement( sbXml, CONSTANT_XML_ACCESS_CODE, user.getLogin( ) );
        XmlUtil.addElement( sbXml, CONSTANT_XML_LAST_NAME, user.getLastName( ) );
        XmlUtil.addElement( sbXml, CONSTANT_XML_FIRST_NAME, user.getFirstName( ) );
        XmlUtil.addElement( sbXml, CONSTANT_XML_EMAIL, user.getEmail( ) );
        XmlUtil.addElement( sbXml, CONSTANT_XML_STATUS, Integer.toString( user.getStatus( ) ) );

        String strPasswordMaxValidDate = StringUtils.EMPTY;

        if ( user.getPasswordMaxValidDate( ) != null )
        {
            strPasswordMaxValidDate = dateFormat.format( user.getPasswordMaxValidDate( ) );
        }

        XmlUtil.addElement( sbXml, CONSTANT_XML_PASSWORD_MAX_VALID_DATE, strPasswordMaxValidDate );

        String strAccountMaxValidDate = StringUtils.EMPTY;

        if ( user.getAccountMaxValidDate( ) != null )
        {
            strAccountMaxValidDate = dateFormat.format( user.getAccountMaxValidDate( ) );
        }

        XmlUtil.addElement( sbXml, CONSTANT_XML_ACCOUNT_MAX_VALID_DATE, strAccountMaxValidDate );

        if ( bExportRoles )
        {
            List<String> listRoles = DatabaseHome.findUserRolesFromLogin( user.getLogin( ), databasePlugin );
            XmlUtil.beginElement( sbXml, CONSTANT_XML_ROLES );

            for ( String strRole : listRoles )
            {
                XmlUtil.addElement( sbXml, CONSTANT_XML_ROLE, strRole );
            }

            XmlUtil.endElement( sbXml, CONSTANT_XML_ROLES );
        }

        if ( bExportGroups )
        {
            List<String> listGroups = DatabaseHome.findUserGroupsFromLogin( user.getLogin( ), databasePlugin );
            XmlUtil.beginElement( sbXml, CONSTANT_XML_GROUPS );

            for ( String strGoup : listGroups )
            {
                XmlUtil.addElement( sbXml, CONSTANT_XML_GROUP, strGoup );
            }

            XmlUtil.endElement( sbXml, CONSTANT_XML_GROUPS );
        }

        if ( bExportAttributes )
        {
            XmlUtil.beginElement( sbXml, CONSTANT_XML_ATTRIBUTES );

            for ( IAttribute attribute : listAttributes )
            {
                List<MyLuteceUserField> listUserFields = MyLuteceUserFieldHome.selectUserFieldsByIdUserIdAttribute( user.getUserId( ),
                        attribute.getIdAttribute( ), mylutecePlugin );

                for ( MyLuteceUserField userField : listUserFields )
                {
                    XmlUtil.beginElement( sbXml, CONSTANT_XML_ATTRIBUTE );
                    XmlUtil.addElement( sbXml, CONSTANT_XML_ATTRIBUTE_ID, Integer.toString( attribute.getIdAttribute( ) ) );
                    XmlUtil.addElement( sbXml, CONSTANT_XML_ATTRIBUTE_FIELD_ID, userField.getAttributeField( ).getIdField( ) );
                    XmlUtil.addElement( sbXml, CONSTANT_XML_ATTRIBUTE_VALUE, userField.getValue( ) );
                    XmlUtil.endElement( sbXml, CONSTANT_XML_ATTRIBUTE );
                }
            }

            XmlUtil.endElement( sbXml, CONSTANT_XML_ATTRIBUTES );
        }

        XmlUtil.endElement( sbXml, CONSTANT_XML_USER );

        return sbXml.toString( );
    }

    /**
     * Login automatically the database user
     * 
     * @param request
     *            the HTTP request
     * @param DatabaseUser
     *            databaseUser
     * @param plugin
     *            the plugin
     */
    public void doAutoLoginDatabaseUser( HttpServletRequest request, DatabaseUser databaseUser, Plugin plugin )
    {
        if ( _baseAuthentication != null )
        {
            BaseUser user = DatabaseHome.findLuteceUserByLogin( databaseUser.getLogin( ), plugin, _baseAuthentication );
            SecurityService.getInstance( ).registerUser( request, user );
        }
    }
}
