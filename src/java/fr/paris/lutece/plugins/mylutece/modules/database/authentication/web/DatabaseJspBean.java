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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeField;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.Group;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter.DatabaseUserParameterHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseResourceIdService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.service.RoleResourceIdService;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldService;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminAuthenticationService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.ItemNavigator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.password.PasswordUtil;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;


/**
 * This class provides the user interface to manage roles features ( manage, create, modify, remove )
 */
public class DatabaseJspBean extends PluginAdminPageJspBean
{
    // Right
    public static final String RIGHT_MANAGE_DATABASE_USERS = "DATABASE_MANAGEMENT_USERS";

    // Contants
    private static final String MANAGE_USERS = "ManageUsers.jsp";
    private static final String REGEX_DATABASE_USER_ID = "^[\\d]+$";
    private static final String CONSTANT_DEFAULT_ALGORITHM = "noValue";
    private static final String CONSTANT_EMPTY_STRING = "";
    private static final String QUESTION_MARK = "?";
	private static final String AMPERSAND = "&";
	private static final String EQUAL = "=";

    //JSP
    private static final String JSP_DO_REMOVE_USER = "jsp/admin/plugins/mylutece/modules/database/DoRemoveUser.jsp";
    private static final String JSP_MANAGE_ADVANCED_PARAMETERS = "ManageAdvancedParameters.jsp";
    private static final String JSP_URL_MANAGE_ADVANCED_PARAMETERS = "jsp/admin/plugins/mylutece/modules/database/ManageAdvancedParameters.jsp";
    private static final String JSP_URL_MODIFY_PASSWORD_ENCRYPTION = "jsp/admin/plugins/mylutece/modules/database/DoModifyPasswordEncryption.jsp";
    private static final String JSP_URL_MODIFY_USER = "jsp/admin/plugins/mylutece/modules/database/ModifyUser.jsp";
    private static final String JSP_URL_MANAGE_ROLES_USER = "jsp/admin/plugins/mylutece/modules/database/ManageRolesUser.jsp";
    private static final String JSP_URL_MANAGE_GROUPS_USER = "jsp/admin/plugins/mylutece/modules/database/ManageGroupsUser.jsp";
    private static final String JSP_MODIFY_USER = "ModifyUser.jsp";
    private static final String JSP_MANAGE_ROLES_USER = "ManageRolesUser.jsp";
    private static final String JSP_MANAGE_GROUPS_USER = "ManageGroupsUser.jsp";
    private static final String JSP_MANAGE_USERS = "ManageUsers.jsp";
    
    //Propety
    private static final String PROPERTY_PAGE_TITLE_MANAGE_USERS = "module.mylutece.database.manage_users.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_USER = "module.mylutece.database.create_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_USER = "module.mylutece.database.modify_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER = "module.mylutece.database.manage_roles_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_GROUPS_USER = "module.mylutece.database.manage_groups_user.pageTitle";
    private static final String PROPERTY_MESSAGE_CONFIRM_MODIFY_PASSWORD_ENCRYPTION = "module.mylutece.database.manage_advanced_parameters.message.confirmModifyPasswordEncryption";
    private static final String PROPERTY_MESSAGE_NO_CHANGE_PASSWORD_ENCRYPTION = "module.mylutece.database.manage_advanced_parameters.message.noChangePasswordEncryption";
    private static final String PROPERTY_MESSAGE_INVALID_ENCRYPTION_ALGORITHM = "module.mylutece.database.manage_advanced_parameters.message.invalidEncryptionAlgorithm";
    private static final String PROPERTY_NO_REPLY_EMAIL = "mail.noreply.email";

    //Messages
    private static final String MESSAGE_CONFIRM_REMOVE_USER = "module.mylutece.database.message.confirmRemoveUser";
    private static final String MESSAGE_USER_EXIST = "module.mylutece.database.message.user_exist";
    private static final String MESSAGE_DIFFERENT_PASSWORD = "module.mylutece.database.message.different_password";
    private static final String MESSAGE_EMAIL_INVALID = "module.mylutece.database.message.email_invalid";
    private static final String MESSAGE_ERROR_MODIFY_USER = "module.mylutece.database.message.modify.user";
    private static final String MESSAGE_ERROR_REMOVE_USER = "module.mylutece.database.message.remove.user";
    private static final String MESSAGE_ERROR_MANAGE_ROLES = "module.mylutece.database.message.manage.roles";
    private static final String MESSAGE_ERROR_MANAGE_GROUPS = "module.mylutece.database.message.manage.groups";
    private static final String MESSAGE_EMAIL_SUBJECT = "module.mylutece.database.forgot_password.email.subject";

    // Parameters
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PARAMETER_MYLUTECE_DATABASE_USER_ID = "mylutece_database_user_id";
    private static final String PARAMETER_MYLUTECE_DATABASE_ROLE_ID = "mylutece_database_role_id";
    private static final String PARAMETER_MYLUTECE_DATABASE_GROUP_KEY = "mylutece_database_group_key";
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_FIRST_PASSWORD = "first_password";
    private static final String PARAMETER_SECOND_PASSWORD = "second_password";
    private static final String PARAMETER_LAST_NAME = "last_name";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_ENABLE_PASSWORD_ENCRYPTION = "enable_password_encryption";
    private static final String PARAMETER_ENCRYPTION_ALGORITHM = "encryption_algorithm";
    private static final String PARAMETER_CANCEL = "cancel";

    // Marks FreeMarker
    private static final String MARK_USERS_LIST = "user_list";
    private static final String MARK_USER = "user";
    private static final String MARK_PLUGIN_NAME = "plugin_name";
    private static final String MARK_ROLES_LIST = "role_list";
    private static final String MARK_ROLES_LIST_FOR_USER = "user_role_list";
    private static final String MARK_GROUPS_LIST = "group_list";
    private static final String MARK_GROUPS_LIST_FOR_USER = "user_group_list";
    private static final String MARK_EXTERNAL_APPLICATION_EXIST = "external_application_exist";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_LOGIN_URL = "login_url";
    private static final String MARK_NEW_PASSWORD = "new_password";
    private static final String MARK_PERMISSION_ADVANCED_PARAMETER = "permission_advanced_parameter";
    private static final String MARK_ITEM_NAVIGATOR = "item_navigator";
    private static final String MARK_ATTRIBUTES_LIST = "attributes_list";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_MAP_LIST_ATTRIBUTE_DEFAULT_VALUES = "map_list_attribute_default_values";

    // Templates
    private static final String TEMPLATE_CREATE_USER = "admin/plugins/mylutece/modules/database/create_user.html";
    private static final String TEMPLATE_MODIFY_USER = "admin/plugins/mylutece/modules/database/modify_user.html";
    private static final String TEMPLATE_MANAGE_USERS = "admin/plugins/mylutece/modules/database/manage_users.html";
    private static final String TEMPLATE_MANAGE_ROLES_USER = "admin/plugins/mylutece/modules/database/manage_roles_user.html";
    private static final String TEMPLATE_MANAGE_GROUPS_USER = "admin/plugins/mylutece/modules/database/manage_groups_user.html";
    private static final String TEMPLATE_MANAGE_ADVANCED_PARAMETERS = "admin/plugins/mylutece/modules/database/manage_advanced_parameters.html";
    private static final String TEMPLATE_EMAIL_FORGOT_PASSWORD = "admin/plugins/mylutece/modules/database/email_forgot_password.html";
    
    // Properties
    private static final String PROPERTY_USERS_PER_PAGE = "paginator.users.itemsPerPage";

    // Variables
    private static Plugin _plugin;
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage;
    private String _strCurrentPageIndex;

    /**
     * Creates a new WssodatabaseJspBean object.
     */
    public DatabaseJspBean(  )
    {
    }

    /**
     * Returns users management form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageUsers( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_USERS );

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        Boolean applicationsExist = Boolean.FALSE;
        String strURL = getHomeUrl( request );
        UrlItem url = new UrlItem( strURL );

        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_USERS_PER_PAGE, 50 );
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage,
                _nDefaultItemsPerPage );

        // Get users
        List<DatabaseUser> listUsers = DatabaseService.getAuthorizedUsers( getUser(  ), _plugin );
        List<DatabaseUser> listFilteredUsers = DatabaseService.getFilteredUsersInterface( listUsers, request, model, url );
        
        // SORT
        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        String strAscSort = null;
        
        if ( strSortedAttributeName != null )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );

            boolean bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( listFilteredUsers, new AttributeComparator( strSortedAttributeName, bIsAscSort ) );
        }
        
        if ( strSortedAttributeName != null )
        {
        	url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
        }
        
        if ( strAscSort != null )
        {
        	url.addParameter( Parameters.SORTED_ASC, strAscSort );
        }
        
        LocalizedPaginator paginator = new LocalizedPaginator( (List) listFilteredUsers, _nItemsPerPage, url.getUrl(  ),
                Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale(  ) );
        
        boolean bPermissionAdvancedParameter = RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, 
        		RBAC.WILDCARD_RESOURCES_ID,	DatabaseResourceIdService.PERMISSION_MANAGE, getUser(  ) );

        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_USERS_LIST, paginator.getPageItems(  ) );
        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_PERMISSION_ADVANCED_PARAMETER, bPermissionAdvancedParameter );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_USERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Returns the User creation form
     *
     * @param request The Http request
     * @return Html creation form
     */
    public String getCreateUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE_USER );
        
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        // Specific attributes
        List<IAttribute> listAttributes = AttributeHome.findAll( getLocale(  ), myLutecePlugin );
        for ( IAttribute attribute : listAttributes )
        {
        	List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( 
        			attribute.getIdAttribute(  ), myLutecePlugin );
        	attribute.setListAttributeFields( listAttributeFields );
        }

        HashMap<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ATTRIBUTES_LIST, listAttributes );
        model.put( MARK_LOCALE, getLocale(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process user's creation
     *
     * @param request The Http request
     * @return The user's Displaying Url
     */
    public String doCreateUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strFirstPassword = request.getParameter( PARAMETER_FIRST_PASSWORD );
        String strSecondPassword = request.getParameter( PARAMETER_SECOND_PASSWORD );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        if ( ( strLogin.length(  ) == 0 ) || ( strFirstPassword.length(  ) == 0 ) || ( strLastName.length(  ) == 0 ) ||
                ( strFirstName.length(  ) == 0 ) || ( strEmail.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strEmail ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP );
        }

        DatabaseUser databaseUser = new DatabaseUser(  );
        databaseUser.setEmail( strEmail );
        databaseUser.setFirstName( strFirstName );
        databaseUser.setLastName( strLastName );
        databaseUser.setLogin( strLogin );

        if ( DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).size(  ) != 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP );
        }

        if ( !strFirstPassword.equals( strSecondPassword ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_DIFFERENT_PASSWORD, AdminMessage.TYPE_STOP );
        }
        
        if ( Boolean.valueOf( 
        		DatabaseUserParameterHome.findByKey( PARAMETER_ENABLE_PASSWORD_ENCRYPTION, getPlugin(  ) ).getParameterValue(  ) ) )
    	{
        	String strAlgorithm = DatabaseUserParameterHome.findByKey( 
        			PARAMETER_ENCRYPTION_ALGORITHM, getPlugin(  ) ).getParameterValue(  );
        	strFirstPassword = CryptoService.encrypt( strFirstPassword, strAlgorithm );
    	}
        
        String strError = MyLuteceUserFieldService.checkUserFields( request, getLocale(  ) );
        if ( strError != null )
        {
        	return strError;
        }

        DatabaseUserHome.create( databaseUser, strFirstPassword, _plugin );
        MyLuteceUserFieldService.doCreateUserFields( databaseUser.getUserId(  ), request, getLocale(  ) );

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     * Returns the User modification form
     *
     * @param request The Http request
     * @return Html modification form
     */
    public String getModifyUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return getCreateUser( request );
        }
        
        // ITEM NAVIGATION
        Map<Integer, String> listItem = new HashMap<Integer, String>(  );
        List<DatabaseUser> listUsers = DatabaseService.getAuthorizedUsers( getUser(  ), _plugin );
        int nMapKey = 1;
        int nCurrentItemId = 1;
        for( DatabaseUser user : listUsers )
        {
        	listItem.put( nMapKey, Integer.toString( user.getUserId(  ) ) );
        	if( user.getUserId(  ) == selectedUser.getUserId(  ) )
        	{
        		nCurrentItemId = nMapKey;
        	}
        	nMapKey++;
        }
        String strBaseUrl = AppPathService.getBaseUrl( request ) + JSP_URL_MODIFY_USER;
        UrlItem url = new UrlItem( strBaseUrl );
        ItemNavigator itemNavigator = new ItemNavigator( listItem, nCurrentItemId, url.getUrl(  ), PARAMETER_MYLUTECE_DATABASE_USER_ID );
        
        Boolean applicationsExist = Boolean.FALSE;
        
        // Specific attributes
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<IAttribute> listAttributes = AttributeHome.findAll( getLocale(  ), myLutecePlugin );
        Map<String, List<MyLuteceUserField>> map = new HashMap<String, List<MyLuteceUserField>>(  );
        for ( IAttribute attribute : listAttributes )
        {
        	List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( 
        			attribute.getIdAttribute(  ), myLutecePlugin );
        	attribute.setListAttributeFields( listAttributeFields );
        	List<MyLuteceUserField> listUserFields = MyLuteceUserFieldHome.selectUserFieldsByIdUserIdAttribute( 
        			selectedUser.getUserId(  ), attribute.getIdAttribute(  ), myLutecePlugin );
        	if ( listUserFields.size(  ) == 0 )
        	{
        		MyLuteceUserField userField = new MyLuteceUserField(  );
        		userField.setValue( CONSTANT_EMPTY_STRING );
        		listUserFields.add( userField );
        	}
        	map.put( String.valueOf( attribute.getIdAttribute(  ) ), listUserFields );
        }

        HashMap<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, itemNavigator );
        model.put( MARK_ATTRIBUTES_LIST, listAttributes );
        model.put( MARK_LOCALE, getLocale(  ) );
        model.put( MARK_MAP_LIST_ATTRIBUTE_DEFAULT_VALUES, map );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process user's modification
     *
     * @param request The Http request
     * @return The user's Displaying Url
     */
    public String doModifyUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_USERS;
        }
        else
        {
        	String strLogin = request.getParameter( PARAMETER_LOGIN );
            String strLastName = request.getParameter( PARAMETER_LAST_NAME );
            String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
            String strEmail = request.getParameter( PARAMETER_EMAIL );

            if ( ( strLogin.length(  ) == 0 ) || ( strLastName.length(  ) == 0 ) || ( strFirstName.length(  ) == 0 ) ||
                    ( strEmail.length(  ) == 0 ) )
            {
                return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
            }

            DatabaseUser databaseUser = getDatabaseUserFromRequest( request );

            if ( databaseUser == null )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MODIFY_USER, AdminMessage.TYPE_ERROR );
            }

            if ( !databaseUser.getLogin(  ).equalsIgnoreCase( strLogin ) &&
                    ( DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).size(  ) != 0 ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP );
            }

            if ( !StringUtil.checkEmail( strEmail ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP );
            }
            
            String strError = MyLuteceUserFieldService.checkUserFields( request, getLocale(  ) );
            if ( strError != null )
            {
            	return strError;
            }

            databaseUser.setEmail( strEmail );
            databaseUser.setFirstName( strFirstName );
            databaseUser.setLastName( strLastName );
            databaseUser.setLogin( strLogin );

            DatabaseUserHome.update( databaseUser, _plugin );
            MyLuteceUserFieldService.doModifyUserFields( databaseUser.getUserId(  ), request, getLocale(  ), getUser(  ) );
            
            strReturn = JSP_MODIFY_USER + QUESTION_MARK + PARAMETER_PLUGIN_NAME + EQUAL + _plugin.getName(  ) +
        			AMPERSAND + PARAMETER_MYLUTECE_DATABASE_USER_ID + EQUAL + databaseUser.getUserId(  );
        }
        
        return strReturn;
    }

    /**
     * Returns removal user's form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getRemoveUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        UrlItem url = new UrlItem( JSP_DO_REMOVE_USER );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID,
            request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_USER, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Process user's removal
     *
     * @param request The Http request
     * @return The Jsp management URL of the process result
     */
    public String doRemoveUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        DatabaseUser user = getDatabaseUserFromRequest( request );

        if ( user == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_USER, AdminMessage.TYPE_ERROR );
        }

        DatabaseUserHome.remove( user, _plugin );

        DatabaseHome.removeRolesForUser( user.getUserId(  ), _plugin );
        MyLuteceUserFieldService.doRemoveUserFields( user.getUserId(  ), request, getLocale(  ) );

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     * Returns roles management form for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageRolesUser( HttpServletRequest request )
    {
        AdminUser adminUser = getUser(  );

        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return getManageUsers( request );
        }

        Collection<Role> allRoleList = RoleHome.findAll(  );
        allRoleList = RBACService.getAuthorizedCollection( allRoleList,
                RoleResourceIdService.PERMISSION_ASSIGN_ROLE, adminUser );
        allRoleList = AdminWorkgroupService.getAuthorizedCollection( allRoleList, getUser(  ) );
        
        List<String> userRoleKeyList = DatabaseHome.findUserRolesFromLogin( selectedUser.getLogin(  ), _plugin );
        Collection<Role> userRoleList = new ArrayList<Role>(  );

        for ( String strRoleKey : userRoleKeyList )
        {
            for ( Role role : allRoleList )
            {
                if ( role.getRole(  ).equals( strRoleKey ) )
                {
                    userRoleList.add( RoleHome.findByPrimaryKey( strRoleKey ) );
                }
            }
        }
        
        // ITEM NAVIGATION
        Map<Integer, String> listItem = new HashMap<Integer, String>(  );
        List<DatabaseUser> listUsers = DatabaseService.getAuthorizedUsers( getUser(  ), _plugin );
        int nMapKey = 1;
        int nCurrentItemId = 1;
        for( DatabaseUser user : listUsers )
        {
        	listItem.put( nMapKey, Integer.toString( user.getUserId(  ) ) );
        	if( user.getUserId(  ) == selectedUser.getUserId(  ) )
        	{
        		nCurrentItemId = nMapKey;
        	}
        	nMapKey++;
        }
        String strBaseUrl = AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_ROLES_USER;
        UrlItem url = new UrlItem( strBaseUrl );
        ItemNavigator itemNavigator = new ItemNavigator( listItem, nCurrentItemId, url.getUrl(  ), PARAMETER_MYLUTECE_DATABASE_USER_ID );
        
        Boolean applicationsExist = Boolean.FALSE;

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_ROLES_LIST, allRoleList );
        model.put( MARK_ROLES_LIST_FOR_USER, userRoleList );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, itemNavigator );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_ROLES_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process assignation roles for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String doAssignRoleUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }
        
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_USERS;
        }
        else
        {
        	//get User
            DatabaseUser user = getDatabaseUserFromRequest( request );

            if ( user == null )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MANAGE_ROLES, AdminMessage.TYPE_ERROR );
            }

            String[] roleArray = request.getParameterValues( PARAMETER_MYLUTECE_DATABASE_ROLE_ID );

            DatabaseHome.removeRolesForUser( user.getUserId(  ), _plugin );

            if ( roleArray != null )
            {
                for ( int i = 0; i < roleArray.length; i++ )
                {
                    DatabaseHome.addRoleForUser( user.getUserId(  ), roleArray[i], _plugin );
                }
            }
            strReturn = JSP_MANAGE_ROLES_USER + QUESTION_MARK + PARAMETER_PLUGIN_NAME + EQUAL + _plugin.getName(  ) +
					AMPERSAND + PARAMETER_MYLUTECE_DATABASE_USER_ID + EQUAL + user.getUserId(  );
        }

        return strReturn;
    }

    /**
     * Returns groups management form for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageGroupsUser( HttpServletRequest request )
    {
        AdminUser adminUser = getUser(  );

        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_GROUPS_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return getManageUsers( request );
        }

        Collection<Group> allGroupList = GroupHome.findAll( getPlugin(  ) );
        Collection<Group> groupList = new ArrayList<Group>(  );
        for ( Group group : allGroupList )
        {
        	List<String> groupRoleKeyList = GroupRoleHome.findGroupRoles( group.getGroupKey(  ), getPlugin(  ) );
        	if ( groupRoleKeyList.size(  ) == 0 )
        	{
        		groupList.add( group );
        		continue;
        	}
    		for ( String groupRoleKey : groupRoleKeyList )
    		{
    			Role role = RoleHome.findByPrimaryKey( groupRoleKey );
        		if ( AdminWorkgroupService.isAuthorized( role, adminUser ) )
        		{
        			groupList.add( group );
        			break;
        		}
    		}
        }

        List<String> userGroupKeyList = DatabaseHome.findUserGroupsFromLogin( selectedUser.getLogin(  ), _plugin );
        Collection<Group> userGroupList = new ArrayList<Group>(  );

        for ( String strGroupKey : userGroupKeyList )
        {
            for ( Group group : groupList )
            {
                if ( group.getGroupKey(  ).equals( strGroupKey ) )
                {
                    userGroupList.add( GroupHome.findByPrimaryKey( strGroupKey, getPlugin(  ) ) );
                }
            }
        }
        
        // ITEM NAVIGATION
        Map<Integer, String> listItem = new HashMap<Integer, String>(  );
        List<DatabaseUser> listUsers = DatabaseService.getAuthorizedUsers( getUser(  ), _plugin );
        int nMapKey = 1;
        int nCurrentItemId = 1;
        for( DatabaseUser user : listUsers )
        {
        	listItem.put( nMapKey, Integer.toString( user.getUserId(  ) ) );
        	if( user.getUserId(  ) == selectedUser.getUserId(  ) )
        	{
        		nCurrentItemId = nMapKey;
        	}
        	nMapKey++;
        }
        String strBaseUrl = AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_GROUPS_USER;
        UrlItem url = new UrlItem( strBaseUrl );
        ItemNavigator itemNavigator = new ItemNavigator( listItem, nCurrentItemId, url.getUrl(  ), PARAMETER_MYLUTECE_DATABASE_USER_ID );
        
        Boolean applicationsExist = Boolean.FALSE;

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        model.put( MARK_GROUPS_LIST, groupList );
        model.put( MARK_GROUPS_LIST_FOR_USER, userGroupList );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, itemNavigator );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_GROUPS_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process assignation groups for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String doAssignGroupsUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }
        
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_USERS;
        }
        else
        {
        	//get User
            DatabaseUser user = getDatabaseUserFromRequest( request );

            if ( user == null )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MANAGE_GROUPS, AdminMessage.TYPE_ERROR );
            }

            String[] groupArray = request.getParameterValues( PARAMETER_MYLUTECE_DATABASE_GROUP_KEY );

            DatabaseHome.removeGroupsForUser( user.getUserId(  ), _plugin );

            if ( groupArray != null )
            {
                for ( int i = 0; i < groupArray.length; i++ )
                {
                    DatabaseHome.addGroupForUser( user.getUserId(  ), groupArray[i], _plugin );
                }
            }
            
            strReturn = JSP_MANAGE_GROUPS_USER + QUESTION_MARK + PARAMETER_PLUGIN_NAME + EQUAL + _plugin.getName(  ) +
					AMPERSAND + PARAMETER_MYLUTECE_DATABASE_USER_ID + EQUAL + user.getUserId(  ); 
        }

        return strReturn;
    }

    /**
     *
     * @param request The http request
     * @return The Database User
     */
    private DatabaseUser getDatabaseUserFromRequest( HttpServletRequest request )
    {
        String strUserId = request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID );

        if ( ( strUserId == null ) || !strUserId.matches( REGEX_DATABASE_USER_ID ) )
        {
            return null;
        }

        int nUserId = Integer.parseInt( strUserId );

        DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nUserId, _plugin );

        return user;
    }

    /**
     * Returns advanced parameters form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageAdvancedParameters( HttpServletRequest request )
    {
    	if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, 
    			DatabaseResourceIdService.PERMISSION_MANAGE, getUser(  ) ) )
    	{
    		return getManageUsers( request );
    	}
    	
    	Map<String, Object> model = DatabaseService.getManageAdvancedParameters( getUser(  ) );
    	
    	HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_ADVANCED_PARAMETERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }
    
    /**
     * Returns the page of confirmation for modifying the password
     * encryption
     *
     * @param request The Http Request
     * @return the confirmation url
     */
    public String doConfirmModifyPasswordEncryption( HttpServletRequest request )
    {
    	String strEnablePasswordEncryption = request.getParameter( PARAMETER_ENABLE_PASSWORD_ENCRYPTION );
    	String strEncryptionAlgorithm = request.getParameter( PARAMETER_ENCRYPTION_ALGORITHM );
    	
    	if ( strEncryptionAlgorithm.equals( CONSTANT_DEFAULT_ALGORITHM ) )
    	{
    		strEncryptionAlgorithm = CONSTANT_EMPTY_STRING;
    	}
    	
    	String strCurrentPasswordEnableEncryption = DatabaseUserParameterHome.findByKey( 
    			PARAMETER_ENABLE_PASSWORD_ENCRYPTION, getPlugin(  ) ).getParameterValue(  );
    	String strCurrentEncryptionAlgorithm = DatabaseUserParameterHome.findByKey( 
    			PARAMETER_ENCRYPTION_ALGORITHM, getPlugin(  ) ).getParameterValue(  );
    	
    	String strUrl = CONSTANT_EMPTY_STRING;
    	if ( strEnablePasswordEncryption.equals( strCurrentPasswordEnableEncryption ) 
    			&& strEncryptionAlgorithm.equals( strCurrentEncryptionAlgorithm ) )
    	{
    		strUrl = AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_NO_CHANGE_PASSWORD_ENCRYPTION, JSP_URL_MANAGE_ADVANCED_PARAMETERS,
                    AdminMessage.TYPE_INFO );
    	}
    	else if ( strEnablePasswordEncryption.equals( String.valueOf( Boolean.TRUE ) )  
    			&& strEncryptionAlgorithm.equals( CONSTANT_EMPTY_STRING ) )
    	{
    		strUrl = AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_INVALID_ENCRYPTION_ALGORITHM, JSP_URL_MANAGE_ADVANCED_PARAMETERS,
                    AdminMessage.TYPE_STOP );
    	}
    	else
    	{
    		if ( strEnablePasswordEncryption.equals( String.valueOf( Boolean.FALSE ) ) )
    		{
    			strEncryptionAlgorithm = CONSTANT_EMPTY_STRING;
    		}
    		String strUrlModify = JSP_URL_MODIFY_PASSWORD_ENCRYPTION + "?" + PARAMETER_ENABLE_PASSWORD_ENCRYPTION + "=" + strEnablePasswordEncryption +
    				"&" + PARAMETER_ENCRYPTION_ALGORITHM + "=" + strEncryptionAlgorithm;

    		strUrl = AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_CONFIRM_MODIFY_PASSWORD_ENCRYPTION, strUrlModify,
    				AdminMessage.TYPE_CONFIRMATION );
    	}

        return strUrl;
    }
    
    /**
     * Modify the password encryption
     * @param request HttpServletRequest
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException If the user does not have the permission
     */
    public String doModifyPasswordEncryption( HttpServletRequest request )
    	throws AccessDeniedException
    {
    	if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, 
    			DatabaseResourceIdService.PERMISSION_MANAGE, getUser(  ) ) )
    	{
    		throw new AccessDeniedException(  );
    	}
    	
    	String strEnablePasswordEncryption = request.getParameter( PARAMETER_ENABLE_PASSWORD_ENCRYPTION );
    	String strEncryptionAlgorithm = request.getParameter( PARAMETER_ENCRYPTION_ALGORITHM );
    	
    	String strCurrentPasswordEnableEncryption = DatabaseUserParameterHome.findByKey( 
    			PARAMETER_ENABLE_PASSWORD_ENCRYPTION, getPlugin(  ) ).getParameterValue(  );
    	String strCurrentEncryptionAlgorithm = DatabaseUserParameterHome.findByKey( 
    			PARAMETER_ENCRYPTION_ALGORITHM, getPlugin(  ) ).getParameterValue(  );
    	
    	if ( strEnablePasswordEncryption.equals( strCurrentPasswordEnableEncryption ) 
    			&& strEncryptionAlgorithm.equals( strCurrentEncryptionAlgorithm ) )
    	{
    		return JSP_MANAGE_ADVANCED_PARAMETERS;
    	}
    	
    	DatabaseUserParameter userParamEnablePwdEncryption = 
    		new DatabaseUserParameter( PARAMETER_ENABLE_PASSWORD_ENCRYPTION, strEnablePasswordEncryption );
    	DatabaseUserParameter userParamEncryptionAlgorithm = 
        		new DatabaseUserParameter( PARAMETER_ENCRYPTION_ALGORITHM, strEncryptionAlgorithm );
        	
    	DatabaseUserParameterHome.update( userParamEnablePwdEncryption, getPlugin(  ) );
    	DatabaseUserParameterHome.update( userParamEncryptionAlgorithm, getPlugin(  ) );
        
        // Alert all users their password have been reinitialized.
    	Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersList( _plugin );
    	
    	for ( DatabaseUser user : listUsers )
    	{   		
    		// make password
            String strPassword = PasswordUtil.makePassword(  );
            
            // update password
            if ( ( strPassword != null ) && !strPassword.equals( CONSTANT_EMPTY_STRING ) )
            {
            	// Encrypted password
            	String strEncryptedPassword = strPassword;
            	if ( Boolean.valueOf( 
                		DatabaseUserParameterHome.findByKey( PARAMETER_ENABLE_PASSWORD_ENCRYPTION, getPlugin(  ) ).getParameterValue(  ) ) )
            	{
            		String strAlgorithm = DatabaseUserParameterHome.findByKey( 
            				PARAMETER_ENCRYPTION_ALGORITHM, getPlugin(  ) ).getParameterValue(  );
                	strEncryptedPassword = CryptoService.encrypt( strPassword, strAlgorithm );
            	}
            	DatabaseUser userStored = DatabaseUserHome.findByPrimaryKey( user.getUserId(  ), _plugin );
            	DatabaseUserHome.remove( userStored, _plugin );
            	DatabaseUserHome.create( userStored, strEncryptedPassword, _plugin );
            }

            if ( !( ( user.getEmail(  ) == null ) || user.getEmail(  ).equals( CONSTANT_EMPTY_STRING ) ) )
            {
            	//send password by e-mail
                String strSenderEmail = AppPropertiesService.getProperty( PROPERTY_NO_REPLY_EMAIL );
                String strEmailSubject = I18nService.getLocalizedString( MESSAGE_EMAIL_SUBJECT, getLocale(  ) );
                HashMap<String, Object> model = new HashMap<String, Object>(  );
                model.put( MARK_NEW_PASSWORD, strPassword );
                model.put( MARK_LOGIN_URL,
                    AppPathService.getBaseUrl( request ) + AdminAuthenticationService.getInstance(  ).getLoginPageUrl(  ) );

                HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_FORGOT_PASSWORD, getLocale(  ), model );

                MailService.sendMailHtml( user.getEmail(  ), strSenderEmail, strSenderEmail, strEmailSubject,
                    template.getHtml(  ) );
            }
    	}
    	
    	return JSP_MANAGE_ADVANCED_PARAMETERS;
    }
}
