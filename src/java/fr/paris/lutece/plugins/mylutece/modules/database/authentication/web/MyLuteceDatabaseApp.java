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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.web;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFactory;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKey;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.key.DatabaseUserKeyService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.DatabaseUserParameterService;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the XPageApp that manage personalization features for Mylutece Database module
 * : login, account management, ...
 */
public class MyLuteceDatabaseApp implements XPageApplication
{
    // Markers
    private static final String MARK_USER = "user";
    private static final String MARK_PASSWORD = "password";
    private static final String MARK_ROLES = "roles";
    private static final String MARK_GROUPS = "groups";
    private static final String MARK_PLUGIN_NAME = "plugin_name";
    private static final String MARK_ERROR_CODE = "error_code";
    private static final String MARK_ACTION_SUCCESSFUL = "action_successful";
    private static final String MARK_EMAIL = "email";
    private static final String MARK_ACTION_VALIDATION_EMAIL = "action_validation_email";
    private static final String MARK_ACTION_VALIDATION_SUCCESS = "action_validation_success";
    private static final String MARK_VALIDATION_URL = "validation_url";
    private static final String MARK_JCAPTCHA = "jcaptcha";
    private static final String MARK_SHOW_INPUT_LOGIN = "show_input_login";

    // Parameters
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_OLD_PASSWORD = "old_password";
    private static final String PARAMETER_NEW_PASSWORD = "new_password";
    private static final String PARAMETER_CONFIRMATION_PASSWORD = "confirmation_password";
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PARAMETER_ERROR_CODE = "error_code";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_ACTION_SUCCESSFUL = "action_successful";
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_LAST_NAME = "last_name";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_KEY = "key";
    private static final String PARAMETER_ACTION_VALIDATION_EMAIL = "action_validation_email";
    private static final String PARAMETER_ACTION_VALIDATION_SUCCESS = "action_validation_success";

    // Actions
    private static final String ACTION_CHANGE_PASSWORD = "changePassword";
    private static final String ACTION_VIEW_ACCOUNT = "viewAccount";
    private static final String ACTION_LOST_PASSWORD = "lostPassword";
    private static final String ACTION_ACCESS_DENIED = "accessDenied";
    private static final String ACTION_CREATE_ACCOUNT = "createAccount";

    // Errors
    private static final String ERROR_OLD_PASSWORD = "error_old_password";
    private static final String ERROR_CONFIRMATION_PASSWORD = "error_confirmation_password";
    private static final String ERROR_SAME_PASSWORD = "error_same_password";
    private static final String ERROR_SYNTAX_EMAIL = "error_syntax_email";
    private static final String ERROR_SENDING_EMAIL = "error_sending_email";
    private static final String ERROR_UNKNOWN_EMAIL = "error_unknown_email";
    private static final String ERROR_MANDATORY_FIELDS = "error_mandatory_fields";
    private static final String ERROR_LOGIN_ALREADY_EXISTS = "error_login_already_exists";
    private static final String ERROR_CAPTCHA = "error_captcha";

    // Templates
    private static final String TEMPLATE_LOST_PASSWORD_PAGE = "skin/plugins/mylutece/modules/database/lost_password.html";
    private static final String TEMPLATE_VIEW_ACCOUNT_PAGE = "skin/plugins/mylutece/modules/database/view_account.html";
    private static final String TEMPLATE_CHANGE_PASSWORD_PAGE = "skin/plugins/mylutece/modules/database/change_password.html";
    private static final String TEMPLATE_EMAIL_BODY = "skin/plugins/mylutece/modules/database/email_body.html";
    private static final String TEMPLATE_CREATE_ACCOUNT_PAGE = "skin/plugins/mylutece/modules/database/create_account.html";
    private static final String TEMPLATE_EMAIL_VALIDATION = "skin/plugins/mylutece/modules/database/email_validation.html";

    // Properties
    private static final String PROPERTY_MYLUTECE_CHANGE_PASSWORD_URL = "mylutece-database.url.changePassword.page";
    private static final String PROPERTY_MYLUTECE_VIEW_ACCOUNT_URL = "mylutece-database.url.viewAccount.page";
    private static final String PROPERTY_MYLUTECE_CREATE_ACCOUNT_URL = "mylutece-database.url.createAccount.page";
    private static final String PROPERTY_MYLUTECE_LOST_PASSWORD_URL = "mylutece-database.url.lostPassword.page";
    private static final String PROPERTY_MYLUTECE_ACCESS_DENIED_URL = "mylutece-database.url.accessDenied.page";
    private static final String PROPERTY_MYLUTECE_DEFAULT_REDIRECT_URL = "mylutece-database.url.default.redirect";
    private static final String PROPERTY_MYLUTECE_TEMPLATE_ACCESS_DENIED = "mylutece-database.template.accessDenied";
    private static final String PROPERTY_MYLUTECE_TEMPLATE_ACCESS_CONTROLED = "mylutece-database.template.accessControled";
    private static final String PROPERTY_MYLUTECE_LOGIN_PAGE_URL = "mylutece.url.login.page";
    private static final String PROPERTY_MAIL_HOST = "mail.server";
    private static final String PROPERTY_PORTAL_NAME = "lutece.name";
    private static final String PROPERTY_NOREPLY_EMAIL = "mail.noreply.email";

    // i18n Properties
    private static final String PROPERTY_CHANGE_PASSWORD_LABEL = "module.mylutece.database.xpage.changePassword.label";
    private static final String PROPERTY_CHANGE_PASSWORD_TITLE = "module.mylutece.database.xpage.changePassword.title";
    private static final String PROPERTY_VIEW_ACCOUNT_LABEL = "module.mylutece.database.xpage.viewAccount.label";
    private static final String PROPERTY_VIEW_ACCOUNT_TITLE = "module.mylutece.database.xpage.viewAccount.title";
    private static final String PROPERTY_LOST_PASSWORD_LABEL = "module.mylutece.database.xpage.lostPassword.label";
    private static final String PROPERTY_LOST_PASSWORD_TITLE = "module.mylutece.database.xpage.lostPassword.title";
    private static final String PROPERTY_CREATE_ACCOUNT_LABEL = "module.mylutece.database.xpage.createAccount.label";
    private static final String PROPERTY_CREATE_ACCOUNT_TITLE = "module.mylutece.database.xpage.createAccount.title";
    private static final String PROPERTY_EMAIL_OBJECT = "module.mylutece.database.email.object";
    private static final String PROPERTY_EMAIL_VALIDATION_OBJECT = "module.mylutece.database.email_validation.object";
    private static final String PROPERTY_ACCESS_DENIED_ERROR_MESSAGE = "module.mylutece.database.siteMessage.access_denied.errorMessage";
    private static final String PROPERTY_ACCESS_DENIED_TITLE_MESSAGE = "module.mylutece.database.siteMessage.access_denied.title";

    // private fields
    private Plugin _plugin;
    private Locale _locale;
    private DatabaseUserParameterService _userParamService = DatabaseUserParameterService.getService(  );
    private DatabaseUserKeyService _userKeyService = DatabaseUserKeyService.getService(  );
    private CaptchaSecurityService _captchaService = new CaptchaSecurityService(  );
    private DatabaseUserFactory _userFactory = DatabaseUserFactory.getFactory(  );

    /**
     *
     * @param request The HTTP request
     * @param plugin The plugin
     */
    public void init( HttpServletRequest request, Plugin plugin )
    {
        _locale = request.getLocale(  );
        _plugin = plugin;
    }

    /**
     *
     * @param request The HTTP request
     * @param nMode The mode (admin, ...)
     * @param plugin The plugin
     * @return The Xpage
     * @throws UserNotSignedException if user not signed
     * @throws SiteMessageException Occurs when a site message need to be displayed
     */
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin )
        throws UserNotSignedException, SiteMessageException
    {
        XPage page = new XPage(  );
        String strAction = request.getParameter( PARAMETER_ACTION );
        init( request, plugin );

        if ( ACTION_CHANGE_PASSWORD.equals( strAction ) )
        {
            page = getChangePasswordPage( page, request );
        }
        else if ( ACTION_VIEW_ACCOUNT.equals( strAction ) )
        {
            page = getViewAccountPage( page, request );
        }
        else if ( ACTION_LOST_PASSWORD.equals( strAction ) )
        {
            page = getLostPasswordPage( page, request );
        }
        else if ( ACTION_CREATE_ACCOUNT.equals( strAction ) )
        {
            page = getCreateAccountPage( page, request );
        }

        if ( strAction.equals( ACTION_ACCESS_DENIED ) || ( page == null ) )
        {
            SiteMessageService.setMessage( request, PROPERTY_ACCESS_DENIED_ERROR_MESSAGE, null,
                PROPERTY_ACCESS_DENIED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
        }

        return page;
    }

    /**
     * Returns the NewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getChangePasswordUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_CHANGE_PASSWORD_URL );
    }

    /**
     * Returns the ViewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getViewAccountUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_VIEW_ACCOUNT_URL );
    }

    /**
     * Returns the createAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getNewAccountUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_CREATE_ACCOUNT_URL );
    }

    /**
     * Returns the Lost Password URL of the Authentication Service
     * @return The URL
     */
    public static String getLostPasswordUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_LOST_PASSWORD_URL );
    }

    /**
     * Returns the Default redirect URL of the Authentication Service
     * @return The URL
     */
    public static String getDefaultRedirectUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_DEFAULT_REDIRECT_URL );
    }

    /**
     * Returns the NewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getAccessDeniedUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_ACCESS_DENIED_URL );
    }

    /**
     * Returns the Login page URL of the Authentication Service
     * @return The URL
     */
    public static String getLoginPageUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_LOGIN_PAGE_URL );
    }

    /**
     * This method is call by the JSP named DoMyLuteceLogout.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the login.
     */
    public String doLogout( HttpServletRequest request )
    {
        SecurityService.getInstance(  ).logoutUser( request );

        return getDefaultRedirectUrl(  );
    }

    /**
     * Build the ViewAccount page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getViewAccountPage( XPage page, HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        DatabaseUser user = getRemoteUser( request );

        if ( user == null )
        {
            return null;
        }

        LuteceUser luteceUser = SecurityService.getInstance(  ).getRegisteredUser( request );

        if ( luteceUser == null )
        {
            return null;
        }

        model.put( MARK_USER, user );
        model.put( MARK_ROLES, luteceUser.getRoles(  ) );
        model.put( MARK_GROUPS, luteceUser.getGroups(  ) );
        model.put( MARK_SHOW_INPUT_LOGIN, !_userFactory.isEmailUsedAsLogin(  ) );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_VIEW_ACCOUNT_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_VIEW_ACCOUNT_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_VIEW_ACCOUNT_TITLE, _locale ) );

        return page;
    }

    /**
     * Build the createAccount page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getCreateAccountPage( XPage page, HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        DatabaseUser user = _userFactory.newDatabaseUser(  );

        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );
        String strSuccess = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );
        String strValidationEmail = request.getParameter( PARAMETER_ACTION_VALIDATION_EMAIL );
        String strValidationSuccess = request.getParameter( PARAMETER_ACTION_VALIDATION_SUCCESS );

        if ( StringUtils.isNotBlank( strLogin ) )
        {
            user.setLogin( strLogin );
        }

        if ( StringUtils.isNotBlank( strLastName ) )
        {
            user.setLastName( strLastName );
        }

        if ( StringUtils.isNotBlank( strFirstName ) )
        {
            user.setFirstName( strFirstName );
        }

        if ( StringUtils.isNotBlank( strEmail ) )
        {
            user.setEmail( strEmail );
        }

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_USER, user );
        model.put( MARK_ACTION_SUCCESSFUL, strSuccess );
        model.put( MARK_ACTION_VALIDATION_EMAIL, strValidationEmail );
        model.put( MARK_ACTION_VALIDATION_SUCCESS, strValidationSuccess );
        model.put( MARK_SHOW_INPUT_LOGIN, !_userFactory.isEmailUsedAsLogin(  ) );

        if ( _userParamService.isJcaptchaEnable( _plugin ) )
        {
            model.put( MARK_JCAPTCHA, _captchaService.getHtmlCode(  ) );
        }

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_CREATE_ACCOUNT_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_CREATE_ACCOUNT_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_CREATE_ACCOUNT_TITLE, _locale ) );

        return page;
    }

    /**
     * This method is call by the JSP named DoCreateAccount.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doCreateAccount( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        String strError = StringUtils.EMPTY;
        String strPassword = request.getParameter( PARAMETER_PASSWORD );
        String strConfirmation = request.getParameter( PARAMETER_CONFIRMATION_PASSWORD );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );
        String strLogin = StringUtils.EMPTY;

        if ( _userFactory.isEmailUsedAsLogin(  ) )
        {
            strLogin = strEmail;
        }
        else
        {
            strLogin = request.getParameter( PARAMETER_LOGIN );
        }

        if ( StringUtils.isBlank( strLogin ) || StringUtils.isBlank( strPassword ) ||
                StringUtils.isBlank( strConfirmation ) || StringUtils.isBlank( strFirstName ) ||
                StringUtils.isBlank( strEmail ) || StringUtils.isBlank( strLastName ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        // Check login unique code
        if ( StringUtils.isBlank( strError ) &&
                !DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).isEmpty(  ) )
        {
            strError = ERROR_LOGIN_ALREADY_EXISTS;
        }

        // Check password confirmation
        if ( StringUtils.isBlank( strError ) && !checkPassword( strPassword, strConfirmation ) )
        {
            strError = ERROR_CONFIRMATION_PASSWORD;
        }

        // Check email format
        if ( StringUtils.isBlank( strError ) && !StringUtil.checkEmail( strEmail ) )
        {
            strError = ERROR_SYNTAX_EMAIL;
        }

        // Check email attributes
        if ( StringUtils.isBlank( strError ) && !checkSendingEmailValidation(  ) )
        {
            strError = ERROR_SENDING_EMAIL;
        }

        if ( StringUtils.isBlank( strError ) && _userParamService.isJcaptchaEnable( _plugin ) &&
                !_captchaService.validate( request ) )
        {
            strError = ERROR_CAPTCHA;
        }

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getNewAccountUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_LAST_NAME, strLastName );
        url.addParameter( PARAMETER_FIRST_NAME, strFirstName );
        url.addParameter( PARAMETER_EMAIL, strEmail );

        if ( !_userFactory.isEmailUsedAsLogin(  ) )
        {
            url.addParameter( PARAMETER_LOGIN, strLogin );
        }

        if ( StringUtils.isBlank( strError ) )
        {
            boolean bAccountCreationValidationEmail = _userParamService.isAccountCreationValidationEmail( _plugin );
            DatabaseUser databaseUser = _userFactory.newDatabaseUser(  );
            databaseUser.setLogin( strLogin );
            databaseUser.setLastName( strLastName );
            databaseUser.setFirstName( strFirstName );
            databaseUser.setEmail( strEmail );
            databaseUser.setActive( !bAccountCreationValidationEmail );
            databaseUser = DatabaseUserHome.create( databaseUser, strPassword, _plugin );

            if ( bAccountCreationValidationEmail )
            {
                DatabaseUserKey userKey = _userKeyService.create( databaseUser.getUserId(  ) );

                String strName = AppPropertiesService.getProperty( PROPERTY_PORTAL_NAME );
                String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );
                String strObject = I18nService.getLocalizedString( PROPERTY_EMAIL_VALIDATION_OBJECT, _locale );

                // Send validation email
                Map<String, Object> model = new HashMap<String, Object>(  );
                model.put( MARK_VALIDATION_URL, _userKeyService.getValidationUrl( userKey.getKey(  ), request ) );

                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_VALIDATION, _locale, model );
                MailService.sendMailHtml( strEmail, strName, strSender, strObject, template.getHtml(  ) );
                url.addParameter( PARAMETER_ACTION_VALIDATION_EMAIL, getDefaultRedirectUrl(  ) );
            }
            else
            {
                url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );
            }
        }
        else
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );
        }

        return url.getUrl(  );
    }

    /**
     * Do validate an account
     * @param request the HTTP request
     * @return the login page url
     */
    public String doValidateAccount( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getNewAccountUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strKey = request.getParameter( PARAMETER_KEY );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            DatabaseUserKey userKey = _userKeyService.findByPrimaryKey( strKey );

            if ( userKey != null )
            {
                DatabaseUser databaseUser = DatabaseUserHome.findByPrimaryKey( userKey.getUserId(  ), _plugin );

                if ( databaseUser != null )
                {
                    databaseUser.setActive( true );
                    DatabaseUserHome.update( databaseUser, _plugin );
                    _userKeyService.remove( strKey );
                    url.addParameter( PARAMETER_ACTION_VALIDATION_SUCCESS, getDefaultRedirectUrl(  ) );
                }
            }
        }

        return url.getUrl(  );
    }

    /**
     * Build the default Lost password page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getLostPasswordPage( XPage page, HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strStateSending = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_ACTION_SUCCESSFUL, strStateSending );
        model.put( MARK_EMAIL, strEmail );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_LOST_PASSWORD_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_LOST_PASSWORD_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_LOST_PASSWORD_TITLE, _locale ) );

        return page;
    }

    /**
     * Build the default Change password page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getChangePasswordPage( XPage page, HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strSuccess = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_ACTION_SUCCESSFUL, strSuccess );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_CHANGE_PASSWORD_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_TITLE, _locale ) );

        return page;
    }

    /**
     * This method is call by the JSP named DoChangePassword.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doChangePassword( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getChangePasswordUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strError = StringUtils.EMPTY;
        DatabaseUser user = getRemoteUser( request );
        String strOldPassword = request.getParameter( PARAMETER_OLD_PASSWORD );
        String strNewPassword = request.getParameter( PARAMETER_NEW_PASSWORD );
        String strConfirmationPassword = request.getParameter( PARAMETER_CONFIRMATION_PASSWORD );

        if ( ( user == null ) )
        {
            try
            {
                SiteMessageService.setMessage( request, PROPERTY_ACCESS_DENIED_ERROR_MESSAGE, null,
                    PROPERTY_ACCESS_DENIED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
            }
            catch ( SiteMessageException e )
            {
                return AppPathService.getBaseUrl( request );
            }
        }

        if ( StringUtils.isBlank( strOldPassword ) || StringUtils.isBlank( strNewPassword ) ||
                StringUtils.isBlank( strConfirmationPassword ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        if ( StringUtils.isBlank( strError ) &&
                !DatabaseUserHome.checkPassword( user.getLogin(  ), strOldPassword, _plugin ) )
        {
            strError = ERROR_OLD_PASSWORD;
        }

        if ( StringUtils.isBlank( strError ) && !checkPassword( strNewPassword, strConfirmationPassword ) )
        {
            strError = ERROR_CONFIRMATION_PASSWORD;
        }

        if ( StringUtils.isBlank( strError ) && strNewPassword.equals( strOldPassword ) )
        {
            strError = ERROR_SAME_PASSWORD;
        }

        if ( StringUtils.isBlank( strError ) )
        {
            DatabaseUserHome.updatePassword( user, strNewPassword, _plugin );
            url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );
        }
        else
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );
        }

        return url.getUrl(  );
    }

    /**
     * Check the password with the password confirmation string
     * Check if password is empty
     *
     * @param strPassword The password
     * @param strConfirmation The password confirmation
     * @return true if password is equal to confirmation password and not empty
     */
    private boolean checkPassword( String strPassword, String strConfirmation )
    {
        boolean bIsPasswordCorrect = false;

        if ( StringUtils.isNotBlank( strPassword ) && StringUtils.isNotBlank( strConfirmation ) &&
                strPassword.equals( strConfirmation ) )
        {
            bIsPasswordCorrect = true;
        }

        return bIsPasswordCorrect;
    }

    /**
     * This method is call by the JSP named DoSendPassword.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the sending.
     */
    public String doSendPassword( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        Map<String, Object> model = new HashMap<String, Object>(  );
        String strError = null;
        UrlItem url = null;
        Collection<DatabaseUser> listUser = null;

        String strEmail = request.getParameter( PARAMETER_EMAIL );
        url = new UrlItem( AppPathService.getBaseUrl( request ) + getLostPasswordUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_EMAIL, strEmail );

        // Check mandatory fields
        if ( StringUtils.isBlank( strEmail ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        // Check email format
        if ( StringUtils.isBlank( strError ) && !StringUtil.checkEmail( strEmail ) )
        {
            strError = ERROR_SYNTAX_EMAIL;
        }

        listUser = DatabaseUserHome.findDatabaseUsersListForEmail( strEmail, _plugin );

        if ( StringUtils.isBlank( strError ) && ( ( listUser == null ) || ( listUser.size(  ) == 0 ) ) )
        {
            strError = ERROR_UNKNOWN_EMAIL;
        }

        if ( !checkSendingPasswordEmail(  ) )
        {
            strError = ERROR_SENDING_EMAIL;
        }

        if ( StringUtils.isBlank( strError ) )
        {
            for ( DatabaseUser user : listUser )
            {
                if ( user.isActive(  ) )
                {
                    model.put( MARK_USER, user );
                    model.put( MARK_PASSWORD, DatabaseUserHome.findPasswordByPrimaryKey( user.getUserId(  ), _plugin ) );

                    HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_BODY, _locale, model );

                    String strName = AppPropertiesService.getProperty( PROPERTY_PORTAL_NAME );
                    String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );
                    String strObject = I18nService.getLocalizedString( PROPERTY_EMAIL_OBJECT, _locale );

                    MailService.sendMailHtml( strEmail, strName, strSender, strObject, template.getHtml(  ) );
                }
            }

            url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );
        }
        else
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );
        }

        return url.getUrl(  );
    }

    /**
     * Returns the template for access denied
     * @return The template path
     */
    public static String getAccessDeniedTemplate(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_TEMPLATE_ACCESS_DENIED );
    }

    /**
     * Returns the template for access controled
     * @return The template path
     */
    public static String getAccessControledTemplate(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_TEMPLATE_ACCESS_CONTROLED );
    }

    /**
     * Get the remote user
     *
     * @param request The HTTP request
     * @return The Database User
     */
    private DatabaseUser getRemoteUser( HttpServletRequest request )
    {
        LuteceUser luteceUser = SecurityService.getInstance(  ).getRegisteredUser( request );

        if ( luteceUser == null )
        {
            return null;
        }

        Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersListForLogin( luteceUser.getName(  ),
                _plugin );

        if ( listUsers.size(  ) != 1 )
        {
            return null;
        }

        DatabaseUser user = (DatabaseUser) listUsers.iterator(  ).next(  );

        return user;
    }

    /**
     * Check if the parameters for the email validation are correctly filled
     * @return true if they are correctly filled, false otherwise
     */
    private boolean checkSendingEmailValidation(  )
    {
        boolean bIsCorrect = false;
        boolean bAccountCreationValidationEmail = _userParamService.isAccountCreationValidationEmail( _plugin );

        if ( bAccountCreationValidationEmail )
        {
            bIsCorrect = checkSendingEmail( PROPERTY_EMAIL_VALIDATION_OBJECT );
        }
        else
        {
            bIsCorrect = true;
        }

        return bIsCorrect;
    }

    /**
     * Check if the parameters for the password email are correctly flled
     * @return true if they are correctly filled, false otherwise
     */
    private boolean checkSendingPasswordEmail(  )
    {
        return checkSendingEmail( PROPERTY_EMAIL_OBJECT );
    }

    /**
     * Check if the parameters for sending an email are correctly filled
     * @param strPropertyObject the property of the object of the email
     * @return true if they are correctly filled, false otherwise
     */
    private boolean checkSendingEmail( String strPropertyObject )
    {
        boolean bIsCorrect = false;
        String strHost = AppPropertiesService.getProperty( PROPERTY_MAIL_HOST );
        String strName = AppPropertiesService.getProperty( PROPERTY_PORTAL_NAME );
        String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );
        String strObject = I18nService.getLocalizedString( strPropertyObject, _locale );

        if ( StringUtils.isNotBlank( strHost ) && StringUtils.isNotBlank( strName ) &&
                StringUtils.isNotBlank( strSender ) && StringUtils.isNotBlank( strObject ) )
        {
            bIsCorrect = true;
        }

        return bIsCorrect;
    }
}
