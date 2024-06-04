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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.mylutece.authentication.PortalAuthentication;
import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLog;
import fr.paris.lutece.plugins.mylutece.authentication.logs.ConnectionLogHome;
import fr.paris.lutece.plugins.mylutece.business.LuteceUserAttributeDescription;
import fr.paris.lutece.plugins.mylutece.business.LuteceUserRoleDescription;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeField;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseAccountLifeTimeService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.MyLuteceDatabaseApp;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.template.DatabaseTemplateHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.FailedLoginCaptchaException;
import fr.paris.lutece.portal.service.security.LoginRedirectException;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.http.SecurityUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The Class provides an implementation of the inherited abstract class PortalAuthentication based on a database.
 *
 * @author Mairie de Paris
 * @version 2.0.0
 *
 * @since Lutece v2.0.0
 */
@ApplicationScoped
@Named( "mylutece-database.authentication" )
public class BaseAuthentication extends PortalAuthentication
{
    // //////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String AUTH_SERVICE_NAME = AppPropertiesService.getProperty( "mylutece-database.service.name" );
    private static final String PLUGIN_JCAPTCHA = "jcaptcha";

    // PROPERTIES
    private static final String PROPERTY_MAX_ACCESS_FAILED = "access_failures_max";
    private static final String PROPERTY_ACCESS_FAILED_CAPTCHA = "access_failures_captcha";
    private static final String PROPERTY_INTERVAL_MINUTES = "access_failures_interval";
    private static final String PROPERTY_UNBLOCK_USER = "mylutece_database_unblock_user";
    private static final String PROPERTY_TOO_MANY_FAILURES = "mylutece.ip.labelTooManyLoginTrials";

    // PARAMETERS
    private static final String PARAMETER_UNBLOCK_USER_MAIL_SENDER = "unblock_user_mail_sender";
    private static final String PARAMETER_UNBLOCK_USER_MAIL_SUBJECT = "unblock_user_mail_subject";
    private static final String PARAMETER_ENABLE_UNBLOCK_IP = "enable_unblock_ip";

    // MARK
    private static final String MARK_URL = "url";
    private static final String MARK_SITE_LINK = "site_link";

    // Messages properties
    private static final String PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE = "module.mylutece.database.message.userNotFoundDatabase";

    // constants
    private static final String CONSTANT_PATH_ICON = "images/local/skin/plugins/mylutece/modules/database/mylutece-database.png";
    private static final String BEAN_USER_ATTRIBUTES_SERVICE = "mylutece.myLuteceUserAttributesService";

    /**
     * Constructor
     *
     */
    public BaseAuthentication( )
    {
        super( );
    }

    /**
     * Gets the Authentification service name
     * 
     * @return The name of the authentication service
     */
    @Override
    public String getAuthServiceName( )
    {
        return AUTH_SERVICE_NAME;
    }

    /**
     * Gets the Authentification type
     * 
     * @param request
     *            The HTTP request
     * @return The type of authentication
     */
    @Override
    public String getAuthType( HttpServletRequest request )
    {
        return HttpServletRequest.BASIC_AUTH;
    }

    /**
     * This methods checks the login info in the database.
     *
     * Unlike its parent, it does not throw LoginRedirectException.
     *
     * @param strUserName
     *            The username
     * @param strUserPassword
     *            The password
     * @param request
     *            The HttpServletRequest
     * @return A LuteceUser object corresponding to the login
     * @throws LoginException
     *             The LoginException
     */
    @Override
    public LuteceUser login( String strUserName, String strUserPassword, HttpServletRequest request ) throws LoginException
    {
        try
        {
            return super.login( strUserName, strUserPassword, request );
        }
        catch( LoginRedirectException lre )
        {
            throw new AppException( "Mylutece-database: impossible. this code should never be reached.", lre );
        }
    }

    /**
     * This methods checks the login info in the database
     *
     * @param strUserName
     *            The username
     * @param strUserPassword
     *            The password
     * @param request
     *            The HttpServletRequest
     * @return A LuteceUser object corresponding to the login
     * @throws LoginException
     *             The LoginException
     */
    @Override
    public LuteceUser processLogin( String strUserName, String strUserPassword, HttpServletRequest request ) throws LoginException
    {
        DatabaseService databaseService = DatabaseService.getService( );

        Plugin pluginMyLutece = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        // Creating a record of connections log
        ConnectionLog connectionLog = new ConnectionLog( );
        connectionLog.setIpAddress( SecurityUtil.getRealIp( request ) );
        connectionLog.setDateLogin( new java.sql.Timestamp( new java.util.Date( ).getTime( ) ) );

        // Test the number of errors during an interval of minutes
        int nMaxFailed = DatabaseUserParameterHome.getIntegerSecurityParameter( PROPERTY_MAX_ACCESS_FAILED, plugin );
        int nMaxFailedCaptcha = 0;
        int nIntervalMinutes = DatabaseUserParameterHome.getIntegerSecurityParameter( PROPERTY_INTERVAL_MINUTES, plugin );
        boolean bEnableCaptcha = false;

        if ( PluginService.isPluginEnable( PLUGIN_JCAPTCHA ) )
        {
            nMaxFailedCaptcha = DatabaseUserParameterHome.getIntegerSecurityParameter( PROPERTY_ACCESS_FAILED_CAPTCHA, plugin );
        }

        Locale locale = request.getLocale( );

        if ( ( ( nMaxFailed > 0 ) || ( nMaxFailedCaptcha > 0 ) ) && ( nIntervalMinutes > 0 ) )
        {
            int nNbFailed = ConnectionLogHome.getLoginErrors( connectionLog, nIntervalMinutes, pluginMyLutece );

            if ( ( nMaxFailedCaptcha > 0 ) && ( nNbFailed >= nMaxFailedCaptcha ) )
            {
                bEnableCaptcha = true;
            }

            if ( ( nMaxFailed > 0 ) && ( nNbFailed >= nMaxFailed ) )
            {
                if ( nNbFailed == nMaxFailed )
                {
                    ReferenceItem item = DatabaseUserParameterHome.findByKey( PARAMETER_ENABLE_UNBLOCK_IP, plugin );

                    if ( ( item != null ) && item.isChecked( ) )
                    {
                        sendUnlockLinkToUser( strUserName, nIntervalMinutes, request, plugin );
                    }
                }

                Object [ ] args = {
                        Integer.toString( nIntervalMinutes )
                };
                String strMessage = I18nService.getLocalizedString( PROPERTY_TOO_MANY_FAILURES, args, locale );

                if ( bEnableCaptcha )
                {
                    throw new FailedLoginCaptchaException( strMessage, bEnableCaptcha );
                }
                else
                {
                    throw new FailedLoginException( strMessage );
                }
            }
        }

        BaseUser user = DatabaseHome.findLuteceUserByLogin( strUserName, plugin, this );

        // Unable to find the user
        if ( ( user == null ) || !databaseService.isUserActive( strUserName, plugin ) )
        {
            AppLogService.info( "Unable to find user in the database : " + strUserName );

            if ( bEnableCaptcha )
            {
                throw new FailedLoginCaptchaException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ), bEnableCaptcha );
            }
            else
            {
                throw new FailedLoginException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ) );
            }
        }

        // Check password
        if ( !databaseService.checkPassword( strUserName, strUserPassword, plugin ) )
        {
            AppLogService.info( "User login : Incorrect login or password" + strUserName );

            if ( bEnableCaptcha )
            {
                throw new FailedLoginCaptchaException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ), bEnableCaptcha );
            }
            else
            {
                throw new FailedLoginException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ) );
            }
        }

        // Get database roles
        List<String> arrayRoles = DatabaseHome.findUserRolesFromLogin( strUserName, plugin );

        if ( !arrayRoles.isEmpty( ) )
        {
            user.addRoles( arrayRoles );
        }

        // Get groups
        List<String> arrayGroups = DatabaseHome.findUserGroupsFromLogin( strUserName, plugin );

        if ( !arrayGroups.isEmpty( ) )
        {
            user.setGroups( arrayGroups );
        }

        // set local database user attributes
        setLocalDatabaseUserAttributes( locale, user );

        // We update the status of the user if his password has become obsolete
        Timestamp passwordMaxValidDate = DatabaseHome.findPasswordMaxValideDateFromLogin( strUserName, plugin );

        if ( ( passwordMaxValidDate != null ) && ( passwordMaxValidDate.getTime( ) < new java.util.Date( ).getTime( ) ) )
        {
            DatabaseHome.updateResetPasswordFromLogin( strUserName, Boolean.TRUE, plugin );
        }

        int nUserId = DatabaseHome.findUserIdFromLogin( strUserName, plugin );
        databaseService.updateUserExpirationDate( nUserId, plugin );

        return user;
    }

    /**
     * This methods logout the user
     * 
     * @param user
     *            The user
     */
    @Override
    public void logout( LuteceUser user )
    {
        // Nothing
    }

    /**
     * Find users by login
     * 
     * @param request
     *            The request
     * @param strLogin
     *            the login
     * @return DatabaseUser the user corresponding to the login
     */
    @Override
    public boolean findResetPassword( HttpServletRequest request, String strLogin )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        return DatabaseHome.findResetPasswordFromLogin( strLogin, plugin );
    }

    /**
     * This method returns an anonymous Lutece user
     *
     * @return An anonymous Lutece user
     */
    @Override
    public LuteceUser getAnonymousUser( )
    {
        return new BaseUser( LuteceUser.ANONYMOUS_USERNAME, this );
    }

    /**
     * Checks that the current user is associated to a given role
     * 
     * @param user
     *            The user
     * @param request
     *            The HTTP request
     * @param strRole
     *            The role name
     * @return Returns true if the user is associated to the role, otherwise false
     */
    @Override
    public boolean isUserInRole( LuteceUser user, HttpServletRequest request, String strRole )
    {
        String [ ] roles = getRolesByUser( user );

        if ( ( roles != null ) && ( strRole != null ) )
        {
            for ( String role : roles )
            {
                if ( strRole.equals( role ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the View account page URL of the Authentication Service
     * 
     * @return The URL
     */
    @Override
    public String getViewAccountPageUrl( )
    {
        return MyLuteceDatabaseApp.getViewAccountUrl( );
    }

    /**
     * Returns the New account page URL of the Authentication Service
     * 
     * @return The URL
     */
    @Override
    public String getNewAccountPageUrl( )
    {
        return MyLuteceDatabaseApp.getNewAccountUrl( );
    }

    /**
     * Returns the Change password page URL of the Authentication Service
     * 
     * @return The URL
     */
    public String getChangePasswordPageUrl( )
    {
        return MyLuteceDatabaseApp.getChangePasswordUrl( );
    }

    /**
     * Returns the lost password URL of the Authentication Service
     * 
     * @return The URL
     */
    @Override
    public String getLostPasswordPageUrl( )
    {
        return MyLuteceDatabaseApp.getLostPasswordUrl( );
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    @Override
    public String getLostLoginPageUrl( )
    {
        return MyLuteceDatabaseApp.getLostLoginUrl( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResetPasswordPageUrl( HttpServletRequest request )
    {
        return AppPathService.getBaseUrl( request ) + MyLuteceDatabaseApp.getMessageResetPasswordUrl( );
    }

    /**
     * Returns all users managed by the authentication service if this feature is available.
     * 
     * @return A collection of Lutece users or null if the service doesn't provide a users list
     */
    @Override
    public Collection<LuteceUser> getUsers( )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        Collection<BaseUser> baseUsers = DatabaseHome.findDatabaseUsersList( plugin, this );
        Collection<LuteceUser> luteceUsers = new ArrayList<>( );

        for ( BaseUser user : baseUsers )
        {
            luteceUsers.add( user );
        }

        return luteceUsers;
    }

    /**
     * Returns the user managed by the authentication service if this feature is available.
     * 
     * @param userLogin
     *            the user login
     * @return A Lutece users or null if the service doesn't provide a user
     */
    @Override
    public LuteceUser getUser( String userLogin )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        return DatabaseHome.findLuteceUserByLogin( userLogin, plugin, this );
    }

    /**
     * get all roles for this user : - user's roles - user's groups roles
     *
     * @param user
     *            The user
     * @return Array of roles
     */
    @Override
    public String [ ] getRolesByUser( LuteceUser user )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        Set<String> setRoles = new HashSet<>( );
        String [ ] strGroups = user.getGroups( );
        String [ ] strRoles = user.getRoles( );

        if ( strRoles != null )
        {
            setRoles.addAll( Arrays.asList( strRoles ) );
        }

        if ( strGroups != null )
        {
            for ( String strGroupKey : strGroups )
            {
                Collection<String> arrayRolesGroup = GroupRoleHome.findGroupRoles( strGroupKey, plugin );

                for ( String strRole : arrayRolesGroup )
                {
                    setRoles.add( strRole );
                }
            }
        }

        String [ ] strReturnRoles = new String [ setRoles.size( )];
        setRoles.toArray( strReturnRoles );

        return strReturnRoles;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl( )
    {
        return CONSTANT_PATH_ICON;
    }

    /**
     *
     * Returns {@link DatabasePlugin#PLUGIN_NAME}.
     * 
     * @return {@link DatabasePlugin#PLUGIN_NAME}
     */
    @Override
    public String getName( )
    {
        return DatabasePlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginName( )
    {
        return DatabasePlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDateLastLogin( LuteceUser user, HttpServletRequest request )
    {
        DatabaseService databaseService = DatabaseService.getService( );
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        databaseService.updateUserLastLoginDate( user.getName( ), plugin );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<LuteceUserRoleDescription> getLuteceUserRolesProvided(Locale locale)
    {
    	return RoleHome.findAll().stream().map(x->new  LuteceUserRoleDescription(x, LuteceUserRoleDescription.TYPE_MANUAL_ASSIGNMENT, null)).collect(Collectors.toList());
    	
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
   public List<LuteceUserAttributeDescription> getLuteceUserAttributesProvided(Locale locale)
    {
    	
    	List<LuteceUserAttributeDescription> listUserDescription=  new ArrayList<LuteceUserAttributeDescription>();
    	listUserDescription.add(new LuteceUserAttributeDescription( LuteceUser.NAME_FAMILY, "databaseUser.getLastName()", ""));
     	listUserDescription.add(new LuteceUserAttributeDescription( LuteceUser.NAME_GIVEN, "databaseUser.getFirstName()", ""));
     	listUserDescription.add(new LuteceUserAttributeDescription( LuteceUser.BUSINESS_INFO_ONLINE_EMAIL, "databaseUser.getEmail()", ""));
     	listUserDescription.add(new LuteceUserAttributeDescription( LuteceUser.DATE_LAST_LOGIN, "databaseUser.getLastLogin()", ""));

     	Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
     	//Add User Attributes
        listUserDescription.addAll(AttributeHome.findAll( locale, myLutecePlugin ).stream().map(x->new  LuteceUserAttributeDescription(x.getTitle(),x.getTitle(), x.getHelpMessage())).collect(Collectors.toList()));
    	
        return listUserDescription;
    }
    

    private void sendUnlockLinkToUser( String strLogin, int nIntervalMinutes, HttpServletRequest request, Plugin plugin )
    {
        int nIdUser = DatabaseUserHome.findDatabaseUserIdFromLogin( strLogin, plugin );

        if ( nIdUser > 0 )
        {
            ReferenceItem referenceItem = DatabaseUserParameterHome.findByKey( PARAMETER_UNBLOCK_USER_MAIL_SENDER, plugin );
            String strSender = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

            referenceItem = DatabaseUserParameterHome.findByKey( PARAMETER_UNBLOCK_USER_MAIL_SUBJECT, plugin );

            String strSubject = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

            String strLink = SecurityUtils.buildResetConnectionLogUrl( nIntervalMinutes, request );

            Map<String, Object> model = new HashMap<>( );
            model.put( MARK_URL, strLink );
            model.put( MARK_SITE_LINK, MailService.getSiteLink( AppPathService.getBaseUrl( request ), true ) );

            String strTemplate = DatabaseTemplateHome.getTemplateFromKey( PROPERTY_UNBLOCK_USER );
            HtmlTemplate template = AppTemplateService.getTemplateFromStringFtl( strTemplate, request.getLocale( ), model );

            DatabaseAccountLifeTimeService accountLifeTimeService = new DatabaseAccountLifeTimeService( );

            String strUserMail = accountLifeTimeService.getUserMainEmail( nIdUser );

            if ( ( strUserMail != null ) && StringUtils.isNotBlank( strUserMail ) )
            {
                MailService.sendMailHtml( strUserMail, strSender, strSender, strSubject, template.getHtml( ) );
            }
        }
    }

    /**
     * set user attributes stored in local database
     * 
     * @param locale
     * @param user
     * @return the user attributes
     */
    private void setLocalDatabaseUserAttributes( Locale locale, BaseUser user )
    {

        // Specific attributes
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        int idUser = DatabaseHome.findUserIdFromLogin( user.getAccessCode( ), myLutecePlugin );

        List<IAttribute> listAttributes = AttributeHome.findAll( locale, myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( attribute.getIdAttribute( ), myLutecePlugin );
            attribute.setListAttributeFields( listAttributeFields );

            List<MyLuteceUserField> listUserFields = MyLuteceUserFieldHome.selectUserFieldsByIdUserIdAttribute( idUser, attribute.getIdAttribute( ),
                    myLutecePlugin );

            if ( listUserFields.size( ) == 1 )
            {
                user.setUserInfo( attribute.getTitle( ), listUserFields.get( 0 ).getValue( ) );
            }
            else
                if ( listUserFields.size( ) > 0 )
                {
                    for ( MyLuteceUserField userField : listUserFields )
                    {
                        user.setUserInfo( attribute.getTitle( ) + "_" + userField.getAttributeField( ).getTitle( ), userField.getValue( ) );
                    }
                }
                else
                {
                    user.setUserInfo( String.valueOf( attribute.getTitle( ) ), StringUtils.EMPTY );
                }
        }
    }
}
