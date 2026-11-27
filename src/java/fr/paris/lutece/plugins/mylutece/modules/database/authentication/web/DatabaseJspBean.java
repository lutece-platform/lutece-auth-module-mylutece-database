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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.web;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeField;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserWrapper;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.Group;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.IDatabaseUserFactory;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabasePlugin;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseResourceIdService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.ImportDatabaseUserService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.key.DatabaseUserKeyService;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.IDatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.IAnonymizationService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.service.RoleResourceIdService;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldService;
import fr.paris.lutece.plugins.mylutece.util.SecurityUtils;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.fileupload.FileUploadService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.DatabaseTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.constants.Parameters;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.filesystem.FileSystemUtil;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.html.ItemNavigator;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage roles features ( manage, create, modify, remove )
 */
@SessionScoped
@Named( "mylutecedatabase_databaseJspBean" )
@Controller( controllerJsp = DatabaseJspBean.CONTROLLER_JSP, controllerPath = DatabaseJspBean.CONTROLLER_PATH, right = DatabaseJspBean.RIGHT_MANAGE_DATABASE_USERS, securityTokenEnabled = true )
public class DatabaseJspBean extends MVCAdminJspBean
{
    // Right
    /**
     * Right to manage database users
     */
    public static final String RIGHT_MANAGE_DATABASE_USERS = "DATABASE_MANAGEMENT_USERS";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -8867524349892775919L;

    // Contants
    private static final String REGEX_DATABASE_USER_ID = "^[\\d]+$";
    private static final String PARAMETER_ACTIVE = "active";
    private static final String SESSION_IMPORT_USERS_LIST_MESSAGES = "mylutece-database.importUsersListMessages";

    // Controller
    static final String CONTROLLER_JSP = "ManageUsers.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/mylutece/modules/database/";
    private static final String CONTROLLER_URL = CONTROLLER_PATH + CONTROLLER_JSP;

    // Views
    static final String VIEW_MANAGE_USERS = "manageUsers";
    static final String VIEW_CREATE_USER = "createUser";
    static final String VIEW_MODIFY_USER = "modifyUser";
    static final String VIEW_CONFIRM_REMOVE_USER = "confirmRemoveUser";
    static final String VIEW_MANAGE_ROLES_USER = "manageRolesUser";
    static final String VIEW_MANAGE_GROUPS_USER = "manageGroupsUser";
    static final String VIEW_MANAGE_ADVANCED_PARAMETERS = "manageAdvancedParameters";
    static final String VIEW_CONFIRM_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS = "confirmChangeUseAdvancedSecurityParameters";
    static final String VIEW_CHANGE_FIELD_ANONYMIZE_USERS = "changeFieldAnonymizeUsers";
    static final String VIEW_CONFIRM_ANONYMIZE_USER = "confirmAnonymizeUser";
    static final String VIEW_MODIFY_ACCOUNT_LIFE_TIME_EMAILS = "modifyAccountLifeTimeEmails";
    static final String VIEW_IMPORT_USERS_FROM_FILE = "importUsersFromFile";
    static final String VIEW_EXPORT_USERS = "exportUsers";

    // Actions
    static final String ACTION_CREATE_USER = "createUser";
    static final String ACTION_MODIFY_USER = "modifyUser";
    static final String ACTION_REMOVE_USER = "removeUser";
    static final String ACTION_CHANGE_USER_STATUS = "changeUserStatus";
    static final String ACTION_ASSIGN_ROLE_USER = "assignRoleUser";
    static final String ACTION_ASSIGN_GROUPS_USER = "assignGroupsUser";
    static final String ACTION_MODIFY_DATABASE_USER_PARAMETERS = "modifyDatabaseUserParameters";
    static final String ACTION_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS = "changeUseAdvancedSecurityParameters";
    static final String ACTION_CHANGE_FIELD_ANONYMIZE_USERS = "changeFieldAnonymizeUsers";
    static final String ACTION_ANONYMIZE_USER = "anonymizeUser";
    static final String ACTION_MODIFY_ACCOUNT_LIFE_TIME_EMAILS = "modifyAccountLifeTimeEmails";
    static final String ACTION_IMPORT_USERS_FROM_FILE = "importUsersFromFile";
    static final String ACTION_EXPORT_USERS = "exportUsers";

    // URL of the views and actions of this controller
    private static final String JSP_URL_DO_REMOVE_USER = CONTROLLER_URL + "?action=" + ACTION_REMOVE_USER;
    private static final String JSP_URL_DO_CHANGE_USE_ADVANCED_SECUR_PARAM = CONTROLLER_URL + "?action=" + ACTION_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS;
    private static final String JSP_URL_DO_ANONYMIZE_USER = CONTROLLER_URL + "?action=" + ACTION_ANONYMIZE_USER;
    private static final String JSP_URL_MODIFY_USER = CONTROLLER_URL + "?view=" + VIEW_MODIFY_USER;
    private static final String JSP_URL_MANAGE_ROLES_USER = CONTROLLER_URL + "?view=" + VIEW_MANAGE_ROLES_USER;
    private static final String JSP_URL_MANAGE_GROUPS_USER = CONTROLLER_URL + "?view=" + VIEW_MANAGE_GROUPS_USER;
    private static final String JSP_URL_CREATE_USER = CONTROLLER_URL + "?view=" + VIEW_CREATE_USER;
    private static final String JSP_URL_MANAGE_USERS = CONTROLLER_URL;
    private static final String JSP_URL_IMPORT_USERS_FROM_FILE = CONTROLLER_URL + "?view=" + VIEW_IMPORT_USERS_FROM_FILE;
    private static final String JSP_MANAGE_ADVANCED_PARAMETERS = CONTROLLER_JSP + "?view=" + VIEW_MANAGE_ADVANCED_PARAMETERS;

    // Propety
    private static final String PROPERTY_PAGE_TITLE_MANAGE_USERS = "module.mylutece.database.manage_users.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_USER = "module.mylutece.database.create_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_USER = "module.mylutece.database.modify_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER = "module.mylutece.database.manage_roles_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_GROUPS_USER = "module.mylutece.database.manage_groups_user.pageTitle";
    private static final String PROPERTY_MESSAGE_CONFIRM_USE_ASP = "module.mylutece.database.manage_advanced_parameters.message.confirmUseAdvancedSecurityParameters";
    private static final String PROPERTY_MESSAGE_CONFIRM_REMOVE_ASP = "mylutece.manage_advanced_parameters.message.confirmRemoveAdvancedSecurityParameters";
    private static final String PROPERTY_MESSAGE_TITLE_CHANGE_ANONYMIZE_USER = "mylutece.anonymize_user.titleAnonymizeUser";
    private static final String PROPERTY_MESSAGE_NO_USER_SELECTED = "mylutece.message.noUserSelected";
    private static final String PROPERTY_MESSAGE_CONFIRM_ANONYMIZE_USER = "mylutece.message.confirmAnonymizeUser";
    private static final String PROPERTY_FIRST_EMAIL = "mylutece.accountLifeTime.labelFirstEmail";
    private static final String PROPERTY_OTHER_EMAIL = "mylutece.accountLifeTime.labelOtherEmail";
    private static final String PROPERTY_ACCOUNT_DEACTIVATES_EMAIL = "mylutece.accountLifeTime.labelAccountDeactivatedEmail";
    private static final String PROPERTY_ACCOUNT_UPDATED_EMAIL = "mylutece.accountLifeTime.labelAccountUpdatedEmail";
    private static final String PROPERTY_UNBLOCK_USER = "mylutece.ip.unblockUser";
    private static final String PROPERTY_NOTIFY_PASSWORD_EXPIRED = "mylutece.accountLifeTime.labelPasswordExpired";
    private static final String PROPERTY_MAIL_LOST_PASSWORD = "mylutece.accountLifeTime.labelLostPasswordMail";
    private static final String PROPERTY_IMPORT_USERS_FROM_FILE_PAGETITLE = "module.mylutece.database.import_users_from_file.pageTitle";
    private static final String PROPERTY_EXPORT_USERS_PAGETITLE = "module.mylutece.database.export_users.pageTitle";
    private static final String PROPERTY_EXTERNAL_APPLICATION_EXISTS = "mylutece-database.externalApplicationExists";

    // Messages
    private static final String MESSAGE_CONFIRM_REMOVE_USER = "module.mylutece.database.message.confirmRemoveUser";
    private static final String MESSAGE_USER_EXIST = "module.mylutece.database.message.user_exist";
    private static final String MESSAGE_DIFFERENT_PASSWORD = "module.mylutece.database.message.different_password";
    private static final String MESSAGE_EMAIL_INVALID = "module.mylutece.database.message.email_invalid";
    private static final String MESSAGE_ERROR_MODIFY_USER = "module.mylutece.database.message.modify.user";
    private static final String MESSAGE_ERROR_REMOVE_USER = "module.mylutece.database.message.remove.user";
    private static final String MESSAGE_ERROR_MANAGE_ROLES = "module.mylutece.database.message.manage.roles";
    private static final String MESSAGE_ERROR_MANAGE_GROUPS = "module.mylutece.database.message.manage.groups";
    private static final String MESSAGE_MANDATORY_FIELD = "portal.util.message.mandatoryField";
    private static final String MESSAGE_ERROR_CSV_FILE_IMPORT = "module.mylutece.database.import_users_from_file.error_csv_file_import";

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
    private static final String PARAMETER_CANCEL = "cancel";
    private static final String PARAMETER_MODIFY_USER = "modify_user";
    private static final String PARAMETER_ASSIGN_ROLE = "assign_role";
    private static final String PARAMETER_ASSIGN_GROUP = "assign_group";
    private static final String PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL = "account_creation_validation_email";
    private static final String PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL = "auto_login_after_validation_email";
    private static final String PARAMETER_ENABLE_JCAPTCHA = "enable_jcaptcha";
    private static final String PARAMETER_NAME_GIVEN = "name_given";
    private static final String PARAMETER_NAME_FAMILY = "name_family";
    private static final String PARAMETER_ATTRIBUTE = "attribute_";
    private static final String PARAMETER_USER_ID = "user_id";
    private static final String PARAMETER_EMAIL_TYPE = "email_type";
    private static final String PARAMETER_FIRST_ALERT_MAIL_SENDER = "first_alert_mail_sender";
    private static final String PARAMETER_OTHER_ALERT_MAIL_SENDER = "other_alert_mail_sender";
    private static final String PARAMETER_EXPIRED_ALERT_MAIL_SENDER = "expired_alert_mail_sender";
    private static final String PARAMETER_REACTIVATED_ALERT_MAIL_SENDER = "account_reactivated_mail_sender";
    private static final String PARAMETER_FIRST_ALERT_MAIL_SUBJECT = "first_alert_mail_subject";
    private static final String PARAMETER_OTHER_ALERT_MAIL_SUBJECT = "other_alert_mail_subject";
    private static final String PARAMETER_EXPIRED_ALERT_MAIL_SUBJECT = "expired_alert_mail_subject";
    private static final String PARAMETER_REACTIVATED_ALERT_MAIL_SUBJECT = "account_reactivated_mail_subject";
    private static final String PARAMETER_FIRST_ALERT_MAIL = "mylutece_database_first_alert_mail";
    private static final String PARAMETER_OTHER_ALERT_MAIL = "mylutece_database_other_alert_mail";
    private static final String PARAMETER_EXPIRATION_MAIL = "mylutece_database_expiration_mail";
    private static final String PARAMETER_ACCOUNT_REACTIVATED = "mylutece_database_account_reactivated_mail";
    private static final String PARAMETER_BANNED_DOMAIN_NAMES = "banned_domain_names";
    private static final String PARAMETER_ACCESS_FAILURES_MAX_IP = "access_failures_max_ip";
    private static final String PARAMETER_UNBLOCK_USER_MAIL_SENDER = "unblock_user_mail_sender";
    private static final String PARAMETER_UNBLOCK_USER_MAIL_SUBJECT = "unblock_user_mail_subject";
    private static final String PARAMETER_UNBLOCK_USER = "mylutece_database_unblock_user";
    private static final String PARAMETER_PASSWORD_EXPIRED_MAIL_SENDER = "password_expired_mail_sender";
    private static final String PARAMETER_PASSWORD_EXPIRED_MAIL_SUBJECT = "password_expired_mail_subject";
    private static final String PARAMETER_NOTIFY_PASSWORD_EXPIRED = "mylutece_database_password_expired";
    private static final String PARAMETER_MAIL_LOST_PASSWORD = "mylutece_database_mailLostPassword";
    private static final String PARAMETER_MAIL_LOST_PASSWORD_SENDER = "mail_lost_password_sender";
    private static final String PARAMETER_MAIL_LOST_PASSWORD_SUBJECT = "mail_lost_password_subject";
    private static final String PARAMETER_IMPORT_USERS_FILE = "import_file";
    private static final String PARAMETER_SKIP_FIRST_LINE = "ignore_first_line";
    private static final String PARAMETER_UPDATE_USERS = "update_existing_users";
    private static final String PARAMETER_EXPORT_ATTRIBUTES = "export_attributes";
    private static final String PARAMETER_EXPORT_ROLES = "export_roles";
    private static final String PARAMETER_EXPORT_WORKGROUPS = "export_workgroups";

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
    private static final String MARK_PERMISSION_ADVANCED_PARAMETER = "permission_advanced_parameter";
    private static final String MARK_ITEM_NAVIGATOR = "item_navigator";
    private static final String MARK_ATTRIBUTES_LIST = "attributes_list";
    private static final String MARK_LOCALE = "locale";
    private static final String MARK_MAP_LIST_ATTRIBUTE_DEFAULT_VALUES = "map_list_attribute_default_values";
    private static final String MARK_SHOW_INPUT_LOGIN = "show_input_login";
    private static final String MARK_EMAIL_SENDER = "email_sender";
    private static final String MARK_EMAIL_SUBJECT = "email_subject";
    private static final String MARK_EMAIL_BODY = "email_body";
    private static final String MARK_EMAIL_LABEL = "emailLabel";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_LIST_MESSAGES = "import_messages";
    private static final String MARK_CSV_SEPARATOR = "csv_separator";
    private static final String MARK_CSV_ESCAPE = "csv_escape";
    private static final String MARK_ATTRIBUTES_SEPARATOR = "attributes_separator";
    private static final String MARK_EXPORT_USERS = "users";

    // Templates
    private static final String TEMPLATE_CREATE_USER = "admin/plugins/mylutece/modules/database/create_user.html";
    private static final String TEMPLATE_MODIFY_USER = "admin/plugins/mylutece/modules/database/modify_user.html";
    private static final String TEMPLATE_MANAGE_USERS = "admin/plugins/mylutece/modules/database/manage_users.html";
    private static final String TEMPLATE_MANAGE_ROLES_USER = "admin/plugins/mylutece/modules/database/manage_roles_user.html";
    private static final String TEMPLATE_MANAGE_GROUPS_USER = "admin/plugins/mylutece/modules/database/manage_groups_user.html";
    private static final String TEMPLATE_MANAGE_ADVANCED_PARAMETERS = "admin/plugins/mylutece/modules/database/manage_advanced_parameters.html";
    private static final String TEMPLATE_FIELD_ANONYMIZE_USER = "admin/plugins/mylutece/modules/database/field_anonymize_user.html";
    private static final String TEMPLATE_ACCOUNT_LIFE_TIME_EMAIL = "admin/plugins/mylutece/modules/database/account_life_time_email.html";
    private static final String TEMPLATE_IMPORT_USERS_FROM_FILE = "admin/plugins/mylutece/modules/database/import_users_from_file.html";
    private static final String TEMPLATE_EXPORT_USERS_FROM_FILE = "admin/plugins/mylutece/modules/database/export_users.html";
    private static final String FIELD_IMPORT_USERS_FILE = "module.mylutece.database.import_users_from_file.labelImportFile";

    private static final String TEMPLATE_EXPORT_USERS = "admin/plugins/mylutece/modules/database/export/export_csv.ftl";

    // Properties
    private static final String PROPERTY_USERS_PER_PAGE = "paginator.users.itemsPerPage";

    // Constants
    private static final String CONSTANT_EMAIL_TYPE_FIRST = "first";
    private static final String CONSTANT_EMAIL_TYPE_OTHER = "other";
    private static final String CONSTANT_EMAIL_TYPE_EXPIRED = "expired";
    private static final String CONSTANT_EMAIL_TYPE_REACTIVATED = "reactivated";
    private static final String CONSTANT_EMAIL_TYPE_IP_BLOCKED = "ip_blocked";
    private static final String CONSTANT_EMAIL_PASSWORD_EXPIRED = "password_expired";
    private static final String CONSTANT_EMAIL_TYPE_LOST_PASSWORD = "lost_password";
    private static final String CONSTANT_EXTENSION_CSV_FILE = ".csv";
    private static final String CONSTANT_MIME_TYPE_CSV = "application/csv";
    private static final String CONSTANT_MIME_TYPE_TEXT_CSV = "text/csv";
    private static final String CONSTANT_MIME_TYPE_OCTETSTREAM = "application/octet-stream";
    private static final String CONSTANT_EXPORT_USERS_FILE_NAME = "users";

    // Variables
    private int _nItemsPerPage;
    private String _strCurrentPageIndex;
    private Map<String, ItemNavigator> _itemNavigators = new HashMap<>( );
    private DatabaseUserFilter _duFilter;
    private String _strSortedAttributeName;
    private boolean _bIsAscSort = true;

    @Inject
    private IDatabaseUserParameterService _userParamService;
    @Inject
    private DatabaseService _databaseService;
    @Inject
    private IDatabaseUserFactory _userFactory;
    @Inject
    private IAnonymizationService _anonymizationService;
    @Inject
    private DatabaseUserKeyService _userKeyService;
    @Inject
    private Models _models;
    private String _strPageTitleProperty;

    /**
     * Returns users management form
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_USERS, defaultView = true, securityTokenAction = ACTION_CHANGE_USER_STATUS )
    public String getManageUsers( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_USERS );

        // Reinit session
        reinitItemNavigators( );

        Map<String, Object> model = new HashMap<>( );
        Boolean applicationsExist = AppPropertiesService.getPropertyBoolean( PROPERTY_EXTERNAL_APPLICATION_EXISTS, false );

        String strURL = getHomeUrl( request );
        UrlItem url = new UrlItem( strURL );

        int defaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_USERS_PER_PAGE, 50 );
        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, defaultItemsPerPage );

        // Get users
        List<DatabaseUser> listUsers = _databaseService.getAuthorizedUsers( getUser( ), getPlugin( ) );
        // FILTER
        _duFilter = new DatabaseUserFilter( );

        boolean bIsSearch = _duFilter.setDatabaseUserFilter( request );
        List<DatabaseUser> listFilteredUsers = _databaseService.getFilteredUsersInterface( _duFilter, bIsSearch, listUsers, request, model, url );

        // SORT
        _strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );

        String strAscSort = null;

        if ( _strSortedAttributeName != null )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );

            _bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( listFilteredUsers, new AttributeComparator( _strSortedAttributeName, _bIsAscSort ) );
        }

        if ( _strSortedAttributeName != null )
        {
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, _strSortedAttributeName );
        }

        if ( strAscSort != null )
        {
            url.addParameter( Parameters.SORTED_ASC, strAscSort );
        }

        LocalizedPaginator<DatabaseUser> paginator = new LocalizedPaginator<>( listFilteredUsers, _nItemsPerPage, url.getUrl( ),
                AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale( ) );

        boolean bPermissionAdvancedParameter = RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                DatabaseResourceIdService.PERMISSION_MANAGE, (User) getUser( ) );

        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( _nItemsPerPage ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_USERS_LIST, paginator.getPageItems( ) );
        model.put( MARK_PLUGIN_NAME, getPlugin( ).getName( ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_PERMISSION_ADVANCED_PARAMETER, bPermissionAdvancedParameter );

        return getPageWithToken( TEMPLATE_MANAGE_USERS, model );
    }

    /**
     * Returns the User creation form
     *
     * @param request
     *            The Http request
     * @return Html creation form
     */
    @View( value = VIEW_CREATE_USER, securityTokenAction = ACTION_CREATE_USER )
    public String getCreateUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE_USER );

        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );

        // Specific attributes
        List<IAttribute> listAttributes = AttributeHome.findAll( getLocale( ), myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( attribute.getIdAttribute( ), myLutecePlugin );
            attribute.setListAttributeFields( listAttributeFields );
        }

        Map<String, Object> model = new HashMap<>( );

        model.put( MARK_PLUGIN_NAME, getPlugin( ).getName( ) );
        model.put( MARK_ATTRIBUTES_LIST, listAttributes );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_SHOW_INPUT_LOGIN, !_userFactory.isEmailUsedAsLogin( ) );

        return getPageWithToken( TEMPLATE_CREATE_USER, model );
    }

    /**
     * Process user's creation
     *
     * @param request
     *            The Http request
     * @return The user's Displaying Url
     */
    @Action( ACTION_CREATE_USER )
    public String doCreateUser( HttpServletRequest request )
    {
        if ( request.getParameter( PARAMETER_CANCEL ) != null )
        {
            return redirect( request, getManageUsersUrl( ) );
        }

        String strError = StringUtils.EMPTY;
        String strReturnUrl = getCreateUserReturnUrl( request );
        String strLogin;
        String strFirstPassword = request.getParameter( PARAMETER_FIRST_PASSWORD );
        String strSecondPassword = request.getParameter( PARAMETER_SECOND_PASSWORD );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        if ( _userFactory.isEmailUsedAsLogin( ) )
        {
            strLogin = strEmail;
        }
        else
        {
            strLogin = request.getParameter( PARAMETER_LOGIN );
        }

        if ( StringUtils.isBlank( strLogin ) || StringUtils.isBlank( strFirstPassword ) || StringUtils.isBlank( strLastName )
                || StringUtils.isBlank( strFirstName ) || StringUtils.isBlank( strEmail ) )
        {
            strError = AdminMessageService.getMessageUrlReturningTo( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP, strReturnUrl );
        }

        if ( StringUtils.isBlank( strError )
                && !StringUtil.checkEmailAndDomainName( strEmail, SecurityUtils.getBannedDomainNames( _userParamService, getPlugin( ) ) ) )
        {
            strError = AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP, strReturnUrl );
        }

        if ( StringUtils.isBlank( strError ) && CollectionUtils.isNotEmpty( DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, getPlugin( ) ) ) )
        {
            strError = AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP, strReturnUrl );
        }

        if ( StringUtils.isBlank( strError ) && !strFirstPassword.equals( strSecondPassword ) )
        {
            strError = AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_DIFFERENT_PASSWORD, AdminMessage.TYPE_STOP, strReturnUrl );
        }

        if ( StringUtils.isBlank( strError ) )
        {
            strError = SecurityUtils.checkPasswordForBackOffice( _userParamService, getPlugin( ), strFirstPassword, request, strReturnUrl );
        }

        if ( StringUtils.isBlank( strError ) )
        {
            strError = MyLuteceUserFieldService.checkUserFields( request, getLocale( ), strReturnUrl );
        }

        if ( StringUtils.isNotBlank( strError ) )
        {
            return redirect( request, strError );
        }

        DatabaseUser databaseUser = _userFactory.newDatabaseUser( );
        databaseUser.setEmail( strEmail );
        databaseUser.setFirstName( strFirstName );
        databaseUser.setLastName( strLastName );
        databaseUser.setLogin( strLogin );
        databaseUser.setStatus( DatabaseUser.STATUS_ACTIVATED );

        _databaseService.doCreateUser( databaseUser, strFirstPassword, getPlugin( ) );
        _databaseService.doModifyResetPassword( databaseUser, true, getPlugin( ) );
        MyLuteceUserFieldService.doCreateUserFields( databaseUser.getUserId( ), request, getLocale( ) );

        return redirect( request, getManageUsersUrl( ) );
    }

    /**
     * Returns the User modification form
     *
     * @param request
     *            The Http request
     * @return Html modification form
     */
    @View( value = VIEW_MODIFY_USER, securityTokenAction = ACTION_MODIFY_USER )
    public String getModifyUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_MODIFY_USER, selectedUser.getUserId( ), AppPathService.getBaseUrl( request ) + JSP_URL_MODIFY_USER, request );

        Boolean applicationsExist = Boolean.FALSE;

        // Specific attributes
        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<IAttribute> listAttributes = AttributeHome.findAll( getLocale( ), myLutecePlugin );
        Map<String, List<MyLuteceUserField>> map = new HashMap<>( );

        for ( IAttribute attribute : listAttributes )
        {
            List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( attribute.getIdAttribute( ), myLutecePlugin );
            attribute.setListAttributeFields( listAttributeFields );

            List<MyLuteceUserField> listUserFields = MyLuteceUserFieldHome.selectUserFieldsByIdUserIdAttribute( selectedUser.getUserId( ),
                    attribute.getIdAttribute( ), myLutecePlugin );

            if ( CollectionUtils.isEmpty( listUserFields ) )
            {
                MyLuteceUserField userField = new MyLuteceUserField( );
                userField.setValue( StringUtils.EMPTY );
                listUserFields.add( userField );
            }

            map.put( String.valueOf( attribute.getIdAttribute( ) ), listUserFields );
        }

        Map<String, Object> model = new HashMap<>( );

        model.put( MARK_PLUGIN_NAME, getPlugin( ).getName( ) );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_MODIFY_USER ) );
        model.put( MARK_ATTRIBUTES_LIST, listAttributes );
        model.put( MARK_LOCALE, getLocale( ) );
        model.put( MARK_MAP_LIST_ATTRIBUTE_DEFAULT_VALUES, map );
        model.put( MARK_SHOW_INPUT_LOGIN, !_userFactory.isEmailUsedAsLogin( ) );

        return getPageWithToken( TEMPLATE_MODIFY_USER, model );
    }

    /**
     * Process user's modification
     *
     * @param request
     *            The Http request
     * @return The user's Displaying Url
     */
    @Action( ACTION_MODIFY_USER )
    public String doModifyUser( HttpServletRequest request )
    {
        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            return redirect( request, getManageUsersUrl( ) );
        }

        String strError;
        String strLogin;
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        if ( _userFactory.isEmailUsedAsLogin( ) )
        {
            strLogin = strEmail;
        }
        else
        {
            strLogin = request.getParameter( PARAMETER_LOGIN );
        }

        if ( StringUtils.isBlank( strLogin ) || StringUtils.isBlank( strLastName ) || StringUtils.isBlank( strFirstName ) || StringUtils.isBlank( strEmail ) )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP,
                    getModifyUserReturnUrl( request, NumberUtils.toInt( request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ) ) ) ) );
        }

        DatabaseUser databaseUser = getDatabaseUserFromRequest( request );

        if ( databaseUser == null )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_MODIFY_USER, AdminMessage.TYPE_ERROR,
                    getManageUsersReturnUrl( request ) ) );
        }
        else
            if ( !databaseUser.getLogin( ).equalsIgnoreCase( strLogin )
                    && CollectionUtils.isNotEmpty( DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, getPlugin( ) ) ) )
            {
                strError = AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP,
                        getModifyUserReturnUrl( request, databaseUser.getUserId( ) ) );
            }
            else
                if ( !StringUtil.checkEmailAndDomainName( strEmail, SecurityUtils.getBannedDomainNames( _userParamService, getPlugin( ) ) ) )
                {
                    strError = AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP,
                            getModifyUserReturnUrl( request, databaseUser.getUserId( ) ) );
                }
                else
                {
                    strError = MyLuteceUserFieldService.checkUserFields( request, getLocale( ),
                            getModifyUserReturnUrl( request, databaseUser.getUserId( ) ) );
                }

        if ( StringUtils.isNotBlank( strError ) )
        {
            return redirect( request, strError );
        }

        databaseUser.setEmail( strEmail );
        databaseUser.setFirstName( strFirstName );
        databaseUser.setLastName( strLastName );
        databaseUser.setLogin( strLogin );

        _databaseService.doUpdateUser( databaseUser, getPlugin( ) );
        MyLuteceUserFieldService.doModifyUserFields( databaseUser.getUserId( ), request, getLocale( ), getUser( ) );

        return redirect( request, getUserViewUrl( VIEW_MODIFY_USER, databaseUser.getUserId( ) ) );
    }

    /**
     * Returns removal user's form
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_CONFIRM_REMOVE_USER, securityTokenAction = ACTION_REMOVE_USER )
    public String getRemoveUser( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_URL_DO_REMOVE_USER );
        url.addParameter( PARAMETER_PLUGIN_NAME, getPlugin( ).getName( ) );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID, request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_USER, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Process user's removal
     *
     * @param request
     *            The Http request
     * @return The Jsp management URL of the process result
     */
    @Action( ACTION_REMOVE_USER )
    public String doRemoveUser( HttpServletRequest request )
    {
        DatabaseUser user = getDatabaseUserFromRequest( request );

        if ( user == null )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_REMOVE_USER, AdminMessage.TYPE_ERROR,
                    getManageUsersReturnUrl( request ) ) );
        }

        DatabaseUserHome.remove( user, getPlugin( ) );
        DatabaseHome.removeGroupsForUser( user.getUserId( ), getPlugin( ) );
        DatabaseHome.removeRolesForUser( user.getUserId( ), getPlugin( ) );
        MyLuteceUserFieldService.doRemoveUserFields( user.getUserId( ), request, getLocale( ) );
        _userKeyService.removeByIdUser( user.getUserId( ) );

        return redirect( request, getManageUsersUrl( ) );
    }

    /**
     * Returns roles management form for a specified user
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_ROLES_USER, securityTokenAction = ACTION_ASSIGN_ROLE_USER )
    public String getManageRolesUser( HttpServletRequest request )
    {
        AdminUser adminUser = getUser( );

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        Collection<Role> allRoleList = RoleHome.findAll( );
        allRoleList = RBACService.getAuthorizedCollection( allRoleList, RoleResourceIdService.PERMISSION_ASSIGN_ROLE, (User) adminUser );
        allRoleList = AdminWorkgroupService.getAuthorizedCollection( allRoleList, (User) getUser( ) );

        List<String> userRoleKeyList = DatabaseHome.findUserRolesFromLogin( selectedUser.getLogin( ), getPlugin( ) );
        Collection<Role> userRoleList = new ArrayList<>( );

        for ( String strRoleKey : userRoleKeyList )
        {
            for ( Role role : allRoleList )
            {
                if ( role.getRole( ).equals( strRoleKey ) )
                {
                    userRoleList.add( RoleHome.findByPrimaryKey( strRoleKey ) );
                }
            }
        }

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_ASSIGN_ROLE, selectedUser.getUserId( ), AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_ROLES_USER, request );

        Boolean applicationsExist = Boolean.FALSE;

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_ROLES_LIST, allRoleList );
        model.put( MARK_ROLES_LIST_FOR_USER, userRoleList );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_PLUGIN_NAME, getPlugin( ).getName( ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_ASSIGN_ROLE ) );

        return getPageWithToken( TEMPLATE_MANAGE_ROLES_USER, model );
    }

    /**
     * Process assignation roles for a specified user
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @Action( ACTION_ASSIGN_ROLE_USER )
    public String doAssignRoleUser( HttpServletRequest request )
    {
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = getManageUsersUrl( );
        }
        else
        {
            // get User
            DatabaseUser user = getDatabaseUserFromRequest( request );

            if ( user == null )
            {
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_MANAGE_ROLES, AdminMessage.TYPE_ERROR,
                        getManageUsersReturnUrl( request ) ) );
            }

            String [ ] roleArray = request.getParameterValues( PARAMETER_MYLUTECE_DATABASE_ROLE_ID );

            DatabaseHome.removeRolesForUser( user.getUserId( ), getPlugin( ) );

            if ( roleArray != null )
            {
                for ( int i = 0; i < roleArray.length; i++ )
                {
                    DatabaseHome.addRoleForUser( user.getUserId( ), roleArray [i], getPlugin( ) );
                }
            }

            strReturn = getUserViewUrl( VIEW_MANAGE_ROLES_USER, user.getUserId( ) );
        }

        return redirect( request, strReturn );
    }

    /**
     * Returns groups management form for a specified user
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_GROUPS_USER, securityTokenAction = ACTION_ASSIGN_GROUPS_USER )
    public String getManageGroupsUser( HttpServletRequest request )
    {
        AdminUser adminUser = getUser( );

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_GROUPS_USER );

        DatabaseUser selectedUser = getDatabaseUserFromRequest( request );

        if ( selectedUser == null )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        Collection<Group> allGroupList = GroupHome.findAll( getPlugin( ) );
        Collection<Group> groupList = new ArrayList<>( );

        for ( Group group : allGroupList )
        {
            List<String> groupRoleKeyList = GroupRoleHome.findGroupRoles( group.getGroupKey( ), getPlugin( ) );

            if ( CollectionUtils.isEmpty( groupRoleKeyList ) )
            {
                groupList.add( group );

                continue;
            }

            for ( String groupRoleKey : groupRoleKeyList )
            {
                Role role = RoleHome.findByPrimaryKey( groupRoleKey );

                if ( AdminWorkgroupService.isAuthorized( role, (User) adminUser ) )
                {
                    groupList.add( group );

                    break;
                }
            }
        }

        List<String> userGroupKeyList = DatabaseHome.findUserGroupsFromLogin( selectedUser.getLogin( ), getPlugin( ) );
        Collection<Group> userGroupList = new ArrayList<>( );

        for ( String strGroupKey : userGroupKeyList )
        {
            for ( Group group : groupList )
            {
                if ( group.getGroupKey( ).equals( strGroupKey ) )
                {
                    userGroupList.add( GroupHome.findByPrimaryKey( strGroupKey, getPlugin( ) ) );
                }
            }
        }

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_ASSIGN_GROUP, selectedUser.getUserId( ), AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_GROUPS_USER, request );

        Boolean applicationsExist = Boolean.FALSE;

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_GROUPS_LIST, groupList );
        model.put( MARK_GROUPS_LIST_FOR_USER, userGroupList );
        model.put( MARK_USER, selectedUser );
        model.put( MARK_PLUGIN_NAME, getPlugin( ).getName( ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_ASSIGN_GROUP ) );

        return getPageWithToken( TEMPLATE_MANAGE_GROUPS_USER, model );
    }

    /**
     * Process assignation groups for a specified user
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @Action( ACTION_ASSIGN_GROUPS_USER )
    public String doAssignGroupsUser( HttpServletRequest request )
    {
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = getManageUsersUrl( );
        }
        else
        {
            // get User
            DatabaseUser user = getDatabaseUserFromRequest( request );

            if ( user == null )
            {
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_MANAGE_GROUPS, AdminMessage.TYPE_ERROR,
                        getManageUsersReturnUrl( request ) ) );
            }

            String [ ] groupArray = request.getParameterValues( PARAMETER_MYLUTECE_DATABASE_GROUP_KEY );

            DatabaseHome.removeGroupsForUser( user.getUserId( ), getPlugin( ) );

            if ( groupArray != null )
            {
                for ( int i = 0; i < groupArray.length; i++ )
                {
                    DatabaseHome.addGroupForUser( user.getUserId( ), groupArray [i], getPlugin( ) );
                }
            }

            strReturn = getUserViewUrl( VIEW_MANAGE_GROUPS_USER, user.getUserId( ) );
        }

        return redirect( request, strReturn );
    }

    /**
     *
     * @param request
     *            The http request
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

        return DatabaseUserHome.findByPrimaryKey( nUserId, getPlugin( ) );
    }

    /**
     * Gets the url of a screen of the users opened by the button of an error message when there is no page to go back to
     *
     * @param request
     *            The http request
     * @param strJspUrl
     *            The url of the JSP of the screen
     * @return The url of the screen, for the plugin of this bean
     */
    private UrlItem getReturnUrl( HttpServletRequest request, String strJspUrl )
    {
        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + strJspUrl );
        url.addParameter( PARAMETER_PLUGIN_NAME, getPlugin( ).getName( ) );

        return url;
    }

    /**
     * Gets the url of the form creating a user, opened by the button of an error message when there is no page to go back to
     *
     * @param request
     *            The http request
     * @return The url of the form
     */
    private String getCreateUserReturnUrl( HttpServletRequest request )
    {
        return getReturnUrl( request, JSP_URL_CREATE_USER ).getUrl( );
    }

    /**
     * Gets the url of the form modifying a user, opened by the button of an error message when there is no page to go back to
     *
     * @param request
     *            The http request
     * @param nUserId
     *            The id of the user
     * @return The url of the form
     */
    private String getModifyUserReturnUrl( HttpServletRequest request, int nUserId )
    {
        UrlItem url = getReturnUrl( request, JSP_URL_MODIFY_USER );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID, nUserId );

        return url.getUrl( );
    }

    /**
     * Gets the url of the list of the users, opened by the button of an error message when there is no page to go back to
     *
     * @param request
     *            The http request
     * @return The url of the list
     */
    private String getManageUsersReturnUrl( HttpServletRequest request )
    {
        return getReturnUrl( request, JSP_URL_MANAGE_USERS ).getUrl( );
    }

    /**
     * Gets the url of the form importing users, opened by the button of an error message when there is no page to go back to
     *
     * @param request
     *            The http request
     * @return The url of the form
     */
    private String getImportUsersReturnUrl( HttpServletRequest request )
    {
        return getReturnUrl( request, JSP_URL_IMPORT_USERS_FROM_FILE ).getUrl( );
    }

    /**
     * Returns advanced parameters form
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_ADVANCED_PARAMETERS, securityTokenAction = ACTION_MODIFY_DATABASE_USER_PARAMETERS )
    public String getManageAdvancedParameters( HttpServletRequest request )
    {
        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, DatabaseResourceIdService.PERMISSION_MANAGE,
                (User) getUser( ) ) )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        Map<String, Object> model = _databaseService.getManageAdvancedParameters( getUser( ) );

        return getPageWithToken( TEMPLATE_MANAGE_ADVANCED_PARAMETERS, model );
    }

    /**
     * Do modify the database user parameters
     * 
     * @param request
     *            the HTTP request
     * @return the JSP return
     * @throws AccessDeniedException
     *             access denied if the user does not have the right
     */
    @Action( ACTION_MODIFY_DATABASE_USER_PARAMETERS )
    public String doModifyDatabaseUserParameters( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, DatabaseResourceIdService.PERMISSION_MANAGE,
                (User) getUser( ) ) )
        {
            throw new AccessDeniedException( "Access Denied" );
        }

        SecurityUtils.updateSecurityParameters( _userParamService, request, getPlugin( ) );
        SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), PARAMETER_ACCESS_FAILURES_MAX_IP, request.getParameter( PARAMETER_ACCESS_FAILURES_MAX_IP ) );

        // The password format rules are shown and saved in both modes: an unchecked switch is not posted
        Map<String, Boolean> mapPasswordFormatRules = new HashMap<>( );

        for ( String strRule : DatabaseService.PASSWORD_FORMAT_RULES )
        {
            mapPasswordFormatRules.put( strRule, Boolean.parseBoolean( request.getParameter( strRule ) ) );
        }

        _databaseService.updatePasswordFormatRules( mapPasswordFormatRules, getPlugin( ) );
        SecurityUtils.updateLargeParameterValue( _userParamService, getPlugin( ), PARAMETER_BANNED_DOMAIN_NAMES,
                request.getParameter( PARAMETER_BANNED_DOMAIN_NAMES ) );

        SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL,
                request.getParameter( PARAMETER_ACCOUNT_CREATION_VALIDATION_EMAIL ) );

        SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL,
                request.getParameter( PARAMETER_AUTO_LOGIN_AFTER_VALIDATION_EMAIL ) );

        if ( _databaseService.isPluginJcaptchaEnable( ) )
        {
            SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), PARAMETER_ENABLE_JCAPTCHA, request.getParameter( PARAMETER_ENABLE_JCAPTCHA ) );
        }

        return redirect( request, JSP_MANAGE_ADVANCED_PARAMETERS );
    }

    /**
     * Do change the status of the user
     * 
     * @param request
     *            the HTTP request
     * @param bIsActive
     *            true if the user must be changed to active, false otherwise
     * @return the JSP home
     */
    @Action( ACTION_CHANGE_USER_STATUS )
    public String doChangeUserStatus( HttpServletRequest request )
    {
        boolean bIsActive = Boolean.parseBoolean( request.getParameter( PARAMETER_ACTIVE ) );
        DatabaseUser databaseUser = getDatabaseUserFromRequest( request );

        if ( databaseUser != null )
        {
            int nStatus = bIsActive ? DatabaseUser.STATUS_ACTIVATED : DatabaseUser.STATUS_NOT_ACTIVATED;
            databaseUser.setStatus( nStatus );
            _databaseService.doUpdateUser( databaseUser, getPlugin( ) );
            // A key sent before (account validation, lost password) must not undo the decision of the administrator
            _userKeyService.removeByIdUser( databaseUser.getUserId( ) );
        }

        return redirect( request, getManageUsersUrl( ) );
    }

    /**
     * Set the item navigator
     * 
     * @param strItemNavigatorKey
     *            The item navigator
     * @param nIdDatabaseUser
     *            The id of the database user
     * @param strUrl
     *            the url
     * @param request
     *            The request
     */
    private void setItemNavigator( String strItemNavigatorKey, int nIdDatabaseUser, String strUrl, HttpServletRequest request )
    {
        ItemNavigator itemNavigator = _itemNavigators.get( strItemNavigatorKey );

        if ( itemNavigator == null )
        {
            if ( _duFilter == null )
            {
                _duFilter = new DatabaseUserFilter( );
            }

            List<String> listIdsDatabaseUser = new ArrayList<>( );
            List<DatabaseUser> listUsers = _databaseService.getAuthorizedUsers( getUser( ), getPlugin( ) );
            List<DatabaseUser> listFilteredUsers = _databaseService.getListFilteredUsers( request, _duFilter, listUsers );

            // SORT
            if ( StringUtils.isNotBlank( _strSortedAttributeName ) )
            {
                Collections.sort( listFilteredUsers, new AttributeComparator( _strSortedAttributeName, _bIsAscSort ) );
            }

            int nCurrentItemId = 0;
            int nIndex = 0;

            for ( DatabaseUser databaseUser : listFilteredUsers )
            {
                if ( databaseUser != null )
                {
                    listIdsDatabaseUser.add( Integer.toString( databaseUser.getUserId( ) ) );

                    if ( databaseUser.getUserId( ) == nIdDatabaseUser )
                    {
                        nCurrentItemId = nIndex;
                    }

                    nIndex++;
                }
            }

            itemNavigator = new ItemNavigator( listIdsDatabaseUser, nCurrentItemId, strUrl, PARAMETER_MYLUTECE_DATABASE_USER_ID );
        }
        else
        {
            itemNavigator.setCurrentItemId( Integer.toString( nIdDatabaseUser ) );
        }

        _itemNavigators.put( strItemNavigatorKey, itemNavigator );
    }

    /**
     * Reinit the item navigator
     */
    private void reinitItemNavigators( )
    {
        _itemNavigators = new HashMap<>( );
        _strSortedAttributeName = StringUtils.EMPTY;
        _bIsAscSort = true;
    }

    /**
     * Get the admin message to confirm the enabling or the disabling of the advanced security parameters
     * 
     * @param request
     *            The request
     * @return The url of the admin message
     */
    @View( value = VIEW_CONFIRM_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS, securityTokenAction = ACTION_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS )
    public String getChangeUseAdvancedSecurityParameters( HttpServletRequest request )
    {
        if ( SecurityUtils.isAdvancedSecurityParametersUsed( _userParamService, getPlugin( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_CONFIRM_REMOVE_ASP, JSP_URL_DO_CHANGE_USE_ADVANCED_SECUR_PARAM,
                    AdminMessage.TYPE_CONFIRMATION ) );
        }

        return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_CONFIRM_USE_ASP, JSP_URL_DO_CHANGE_USE_ADVANCED_SECUR_PARAM,
                AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Uses the advanced security parameters when they are not used, removes them otherwise (the confirmation said which)
     * 
     * @param request
     *            The request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CHANGE_USE_ADVANCED_SECURITY_PARAMETERS )
    public String doChangeUseAdvancedSecurityParameters( HttpServletRequest request )
    {
        if ( SecurityUtils.isAdvancedSecurityParametersUsed( _userParamService, getPlugin( ) ) )
        {
            // The password format rules are not part of the advanced mode: they are kept
            Map<String, Boolean> mapPasswordFormatRules = _databaseService.getPasswordFormatRules( getPlugin( ) );
            SecurityUtils.removeAdvancedSecurityParameters( _userParamService, getPlugin( ) );
            _databaseService.updatePasswordFormatRules( mapPasswordFormatRules, getPlugin( ) );
        }
        else
        {
            SecurityUtils.useAdvancedSecurityParameters( _userParamService, getPlugin( ) );
        }

        return redirect( request, JSP_MANAGE_ADVANCED_PARAMETERS );
    }

    /**
     * Get the page with the list of every anonymizable attribute
     * 
     * @param request
     *            The request
     * @return The admin page
     */
    @View( value = VIEW_CHANGE_FIELD_ANONYMIZE_USERS, securityTokenAction = ACTION_CHANGE_FIELD_ANONYMIZE_USERS )
    public String getChangeFieldAnonymizeAdminUsers( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<>( );

        List<IAttribute> listAllAttributes = AttributeHome.findAll( getLocale( ), getPlugin( ) );
        List<IAttribute> listAttributesText = new ArrayList<>( );

        for ( IAttribute attribut : listAllAttributes )
        {
            if ( attribut.isAnonymizable( ) )
            {
                listAttributesText.add( attribut );
            }
        }

        model.put( MARK_ATTRIBUTES_LIST, listAttributesText );

        model.putAll( AttributeHome.getAnonymizationStatusUserStaticField( getPlugin( ) ) );

        setPageTitleProperty( PROPERTY_MESSAGE_TITLE_CHANGE_ANONYMIZE_USER );

        return getPageWithToken( TEMPLATE_FIELD_ANONYMIZE_USER, model );
    }

    /**
     * Change the anonymization status of user parameters.
     * 
     * @param request
     *            The request
     * @return the Jsp URL of the process result
     */
    @Action( ACTION_CHANGE_FIELD_ANONYMIZE_USERS )
    public String doChangeFieldAnonymizeUsers( HttpServletRequest request )
    {
        if ( request.getParameter( PARAMETER_CANCEL ) != null )
        {
            return redirect( request, JSP_MANAGE_ADVANCED_PARAMETERS );
        }

        Plugin pluginMyLutece = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        AttributeHome.updateAnonymizationStatusUserStaticField( PARAMETER_LOGIN, Boolean.valueOf( request.getParameter( PARAMETER_LOGIN ) ), pluginMyLutece );
        AttributeHome.updateAnonymizationStatusUserStaticField( PARAMETER_NAME_GIVEN, Boolean.valueOf( request.getParameter( PARAMETER_NAME_GIVEN ) ),
                pluginMyLutece );
        AttributeHome.updateAnonymizationStatusUserStaticField( PARAMETER_NAME_FAMILY, Boolean.valueOf( request.getParameter( PARAMETER_NAME_FAMILY ) ),
                pluginMyLutece );
        AttributeHome.updateAnonymizationStatusUserStaticField( PARAMETER_EMAIL, Boolean.valueOf( request.getParameter( PARAMETER_EMAIL ) ), pluginMyLutece );

        List<IAttribute> listAllAttributes = AttributeHome.findAll( getLocale( ), pluginMyLutece );
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
            Boolean bNewValue = Boolean.valueOf( request.getParameter( PARAMETER_ATTRIBUTE + Integer.toString( attribute.getIdAttribute( ) ) ) );
            AttributeHome.updateAttributeAnonymization( attribute.getIdAttribute( ), bNewValue, pluginMyLutece );
        }

        return redirect( request, JSP_MANAGE_ADVANCED_PARAMETERS );
    }

    /**
     * Get the confirmation page before anonymizing a user.
     * 
     * @param request
     *            The request
     * @return The URL of the confirmation page
     */
    @View( value = VIEW_CONFIRM_ANONYMIZE_USER, securityTokenAction = ACTION_ANONYMIZE_USER )
    public String getAnonymizeUser( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_URL_DO_ANONYMIZE_USER );

        String strUserId = request.getParameter( PARAMETER_USER_ID );

        if ( ( strUserId == null ) || strUserId.isEmpty( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, PROPERTY_MESSAGE_NO_USER_SELECTED, AdminMessage.TYPE_STOP,
                    getManageUsersReturnUrl( request ) ) );
        }

        url.addParameter( PARAMETER_USER_ID, strUserId );

        return redirect( request, AdminMessageService.getMessageUrl( request, PROPERTY_MESSAGE_CONFIRM_ANONYMIZE_USER, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Anonymize a user
     * 
     * @param request
     *            The request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_ANONYMIZE_USER )
    public String doAnonymizeUser( HttpServletRequest request )
    {
        String strUserId = request.getParameter( PARAMETER_USER_ID );

        if ( ( strUserId == null ) || strUserId.isEmpty( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, PROPERTY_MESSAGE_NO_USER_SELECTED, AdminMessage.TYPE_STOP,
                    getManageUsersReturnUrl( request ) ) );
        }

        _anonymizationService.anonymizeUser( Integer.parseInt( strUserId ), getLocale( ) );

        return redirect( request, getManageUsersUrl( ) );
    }

    /**
     * Get the modify account life time emails page
     * 
     * @param request
     *            The request
     * @return The html to display
     */
    @View( value = VIEW_MODIFY_ACCOUNT_LIFE_TIME_EMAILS, securityTokenAction = ACTION_MODIFY_ACCOUNT_LIFE_TIME_EMAILS )
    public String getModifyAccountLifeTimeEmails( HttpServletRequest request )
    {
        String strEmailType = request.getParameter( PARAMETER_EMAIL_TYPE );

        Map<String, Object> model = new HashMap<>( );
        String strSenderKey = StringUtils.EMPTY;
        String strSubjectKey = StringUtils.EMPTY;
        String strBodyKey = StringUtils.EMPTY;
        String strTitle = StringUtils.EMPTY;

        if ( CONSTANT_EMAIL_TYPE_FIRST.equalsIgnoreCase( strEmailType ) )
        {
            strSenderKey = PARAMETER_FIRST_ALERT_MAIL_SENDER;
            strSubjectKey = PARAMETER_FIRST_ALERT_MAIL_SUBJECT;
            strBodyKey = PARAMETER_FIRST_ALERT_MAIL;
            strTitle = PROPERTY_FIRST_EMAIL;
        }
        else
            if ( CONSTANT_EMAIL_TYPE_OTHER.equalsIgnoreCase( strEmailType ) )
            {
                strSenderKey = PARAMETER_OTHER_ALERT_MAIL_SENDER;
                strSubjectKey = PARAMETER_OTHER_ALERT_MAIL_SUBJECT;
                strBodyKey = PARAMETER_OTHER_ALERT_MAIL;
                strTitle = PROPERTY_OTHER_EMAIL;
            }
            else
                if ( CONSTANT_EMAIL_TYPE_EXPIRED.equalsIgnoreCase( strEmailType ) )
                {
                    strSenderKey = PARAMETER_EXPIRED_ALERT_MAIL_SENDER;
                    strSubjectKey = PARAMETER_EXPIRED_ALERT_MAIL_SUBJECT;
                    strBodyKey = PARAMETER_EXPIRATION_MAIL;
                    strTitle = PROPERTY_ACCOUNT_DEACTIVATES_EMAIL;
                }
                else
                    if ( CONSTANT_EMAIL_TYPE_REACTIVATED.equalsIgnoreCase( strEmailType ) )
                    {
                        strSenderKey = PARAMETER_REACTIVATED_ALERT_MAIL_SENDER;
                        strSubjectKey = PARAMETER_REACTIVATED_ALERT_MAIL_SUBJECT;
                        strBodyKey = PARAMETER_ACCOUNT_REACTIVATED;
                        strTitle = PROPERTY_ACCOUNT_UPDATED_EMAIL;
                    }
                    else
                        if ( CONSTANT_EMAIL_TYPE_IP_BLOCKED.equalsIgnoreCase( strEmailType ) )
                        {
                            strSenderKey = PARAMETER_UNBLOCK_USER_MAIL_SENDER;
                            strSubjectKey = PARAMETER_UNBLOCK_USER_MAIL_SUBJECT;
                            strBodyKey = PARAMETER_UNBLOCK_USER;
                            strTitle = PROPERTY_UNBLOCK_USER;
                        }
                        else
                            if ( CONSTANT_EMAIL_PASSWORD_EXPIRED.equalsIgnoreCase( strEmailType ) )
                            {
                                strSenderKey = PARAMETER_PASSWORD_EXPIRED_MAIL_SENDER;
                                strSubjectKey = PARAMETER_PASSWORD_EXPIRED_MAIL_SUBJECT;
                                strBodyKey = PARAMETER_NOTIFY_PASSWORD_EXPIRED;
                                strTitle = PROPERTY_NOTIFY_PASSWORD_EXPIRED;
                            }
                            else
                                if ( CONSTANT_EMAIL_TYPE_LOST_PASSWORD.equalsIgnoreCase( strEmailType ) )
                                {
                                    strSenderKey = PARAMETER_MAIL_LOST_PASSWORD_SENDER;
                                    strSubjectKey = PARAMETER_MAIL_LOST_PASSWORD_SUBJECT;
                                    strBodyKey = PARAMETER_MAIL_LOST_PASSWORD;
                                    strTitle = PROPERTY_MAIL_LOST_PASSWORD;
                                }

        ReferenceItem referenceItem = _userParamService.findByKey( strSenderKey, getPlugin( ) );
        String strSender = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

        referenceItem = _userParamService.findByKey( strSubjectKey, getPlugin( ) );

        String strSubject = ( referenceItem == null ) ? StringUtils.EMPTY : referenceItem.getName( );

        model.put( PARAMETER_EMAIL_TYPE, strEmailType );
        model.put( MARK_EMAIL_SENDER, strSender );
        model.put( MARK_EMAIL_SUBJECT, strSubject );
        model.put( MARK_EMAIL_BODY, DatabaseTemplateService.getTemplateFromKey( strBodyKey ) );
        model.put( MARK_EMAIL_LABEL, strTitle );
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, request.getLocale( ) );

        return getPageWithToken( TEMPLATE_ACCOUNT_LIFE_TIME_EMAIL, model );
    }

    /**
     * Update an account life time email
     * 
     * @param request
     *            The request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_ACCOUNT_LIFE_TIME_EMAILS )
    public String doModifyAccountLifeTimeEmails( HttpServletRequest request )
    {
        String strEmailType = request.getParameter( PARAMETER_EMAIL_TYPE );

        String strSenderKey = StringUtils.EMPTY;
        String strSubjectKey = StringUtils.EMPTY;
        String strBodyKey = StringUtils.EMPTY;

        if ( CONSTANT_EMAIL_TYPE_FIRST.equalsIgnoreCase( strEmailType ) )
        {
            strSenderKey = PARAMETER_FIRST_ALERT_MAIL_SENDER;
            strSubjectKey = PARAMETER_FIRST_ALERT_MAIL_SUBJECT;
            strBodyKey = PARAMETER_FIRST_ALERT_MAIL;
        }
        else
            if ( CONSTANT_EMAIL_TYPE_OTHER.equalsIgnoreCase( strEmailType ) )
            {
                strSenderKey = PARAMETER_OTHER_ALERT_MAIL_SENDER;
                strSubjectKey = PARAMETER_OTHER_ALERT_MAIL_SUBJECT;
                strBodyKey = PARAMETER_OTHER_ALERT_MAIL;
            }
            else
                if ( CONSTANT_EMAIL_TYPE_EXPIRED.equalsIgnoreCase( strEmailType ) )
                {
                    strSenderKey = PARAMETER_EXPIRED_ALERT_MAIL_SENDER;
                    strSubjectKey = PARAMETER_EXPIRED_ALERT_MAIL_SUBJECT;
                    strBodyKey = PARAMETER_EXPIRATION_MAIL;
                }
                else
                    if ( CONSTANT_EMAIL_TYPE_REACTIVATED.equalsIgnoreCase( strEmailType ) )
                    {
                        strSenderKey = PARAMETER_REACTIVATED_ALERT_MAIL_SENDER;
                        strSubjectKey = PARAMETER_REACTIVATED_ALERT_MAIL_SUBJECT;
                        strBodyKey = PARAMETER_ACCOUNT_REACTIVATED;
                    }
                    else
                        if ( CONSTANT_EMAIL_TYPE_IP_BLOCKED.equalsIgnoreCase( strEmailType ) )
                        {
                            strSenderKey = PARAMETER_UNBLOCK_USER_MAIL_SENDER;
                            strSubjectKey = PARAMETER_UNBLOCK_USER_MAIL_SUBJECT;
                            strBodyKey = PARAMETER_UNBLOCK_USER;
                        }
                        else
                            if ( CONSTANT_EMAIL_PASSWORD_EXPIRED.equalsIgnoreCase( strEmailType ) )
                            {
                                strSenderKey = PARAMETER_PASSWORD_EXPIRED_MAIL_SENDER;
                                strSubjectKey = PARAMETER_PASSWORD_EXPIRED_MAIL_SUBJECT;
                                strBodyKey = PARAMETER_NOTIFY_PASSWORD_EXPIRED;
                            }
                            else
                                if ( CONSTANT_EMAIL_TYPE_LOST_PASSWORD.equalsIgnoreCase( strEmailType ) )
                                {
                                    strSenderKey = PARAMETER_MAIL_LOST_PASSWORD_SENDER;
                                    strSubjectKey = PARAMETER_MAIL_LOST_PASSWORD_SUBJECT;
                                    strBodyKey = PARAMETER_MAIL_LOST_PASSWORD;
                                }

        SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), strSenderKey, request.getParameter( MARK_EMAIL_SENDER ) );
        SecurityUtils.updateParameterValue( _userParamService, getPlugin( ), strSubjectKey, request.getParameter( MARK_EMAIL_SUBJECT ) );
        DatabaseTemplateService.updateTemplate( strBodyKey, request.getParameter( MARK_EMAIL_BODY ) );

        return redirect( request, JSP_MANAGE_ADVANCED_PARAMETERS );
    }

    /**
     * Get a page to import users from a CSV file.
     * 
     * @param request
     *            The request
     * @return The HTML content
     */
    @View( value = VIEW_IMPORT_USERS_FROM_FILE, securityTokenAction = ACTION_IMPORT_USERS_FROM_FILE )
    public String getImportUsersFromFile( HttpServletRequest request )
    {
        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                DatabaseResourceIdService.PERMISSION_IMPORT_EXPORT_DATABASE_USERS, (User) getUser( ) ) )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        setPageTitleProperty( PROPERTY_IMPORT_USERS_FROM_FILE_PAGETITLE );

        Map<String, Object> model = new HashMap<>( );

        // Messages of the import just done
        model.put( MARK_LIST_MESSAGES, request.getSession( ).getAttribute( SESSION_IMPORT_USERS_LIST_MESSAGES ) );
        request.getSession( ).removeAttribute( SESSION_IMPORT_USERS_LIST_MESSAGES );

        ImportDatabaseUserService importDatabaseUserService = new ImportDatabaseUserService( );
        String strCsvSeparator = StringUtils.EMPTY + importDatabaseUserService.getCSVSeparator( );
        String strCsvEscapeCharacter = StringUtils.EMPTY + importDatabaseUserService.getCSVEscapeCharacter( );
        String strAttributesSeparator = StringUtils.EMPTY + importDatabaseUserService.getAttributesSeparator( );
        model.put( MARK_CSV_SEPARATOR, strCsvSeparator );
        model.put( MARK_CSV_ESCAPE, strCsvEscapeCharacter );
        model.put( MARK_ATTRIBUTES_SEPARATOR, strAttributesSeparator );

        return getPageWithToken( TEMPLATE_IMPORT_USERS_FROM_FILE, model );
    }

    /**
     * Do import users from a CSV file
     * 
     * @param request
     *            The request
     * @return A DefaultPluginActionResult with the URL of the page to display, or the HTML content
     */
    @Action( ACTION_IMPORT_USERS_FROM_FILE )
    public String doImportUsersFromFile( HttpServletRequest request )
    {
        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                DatabaseResourceIdService.PERMISSION_IMPORT_EXPORT_DATABASE_USERS, (User) getUser( ) ) )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        if ( request instanceof MultipartHttpServletRequest )
        {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            var fileItem = multipartRequest.getFile( PARAMETER_IMPORT_USERS_FILE );
            String strMimeType = FileSystemUtil.getMIMEType( FileUploadService.getFileNameOnly( fileItem ) );

            if ( !( ( fileItem != null ) && !StringUtils.EMPTY.equals( fileItem.getName( ) ) ) )
            {
                Object [ ] tabRequiredFields = {
                        I18nService.getLocalizedString( FIELD_IMPORT_USERS_FILE, getLocale( ) )
                };
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields,
                        AdminMessage.TYPE_STOP, getImportUsersReturnUrl( request ) ) );
            }

            if ( ( !strMimeType.equals( CONSTANT_MIME_TYPE_CSV ) && !strMimeType.equals( CONSTANT_MIME_TYPE_OCTETSTREAM )
                    && !strMimeType.equals( CONSTANT_MIME_TYPE_TEXT_CSV ) ) || !fileItem.getName( ).toLowerCase( ).endsWith( CONSTANT_EXTENSION_CSV_FILE ) )
            {
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_CSV_FILE_IMPORT, AdminMessage.TYPE_STOP,
                        getImportUsersReturnUrl( request ) ) );
            }

            String strSkipFirstLine = multipartRequest.getParameter( PARAMETER_SKIP_FIRST_LINE );
            boolean bSkipFirstLine = StringUtils.isNotEmpty( strSkipFirstLine );
            String strUpdateUsers = multipartRequest.getParameter( PARAMETER_UPDATE_USERS );
            boolean bUpdateUsers = StringUtils.isNotEmpty( strUpdateUsers );

            ImportDatabaseUserService importDatabaseUserService = new ImportDatabaseUserService( );
            importDatabaseUserService.setUpdateExistingUsers( bUpdateUsers );

            List<CSVMessageDescriptor> listMessages = importDatabaseUserService.readCSVFile( fileItem, 0, false, false, bSkipFirstLine,
                    AdminUserService.getLocale( request ), AppPathService.getBaseUrl( request ) );

            request.getSession( ).setAttribute( SESSION_IMPORT_USERS_LIST_MESSAGES, new ArrayList<>( listMessages ) );

            return redirectView( request, VIEW_IMPORT_USERS_FROM_FILE );
        }
        else
        {
            Object [ ] tabRequiredFields = {
                    I18nService.getLocalizedString( FIELD_IMPORT_USERS_FILE, getLocale( ) )
            };
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_MANDATORY_FIELD, tabRequiredFields,
                    AdminMessage.TYPE_STOP, getImportUsersReturnUrl( request ) ) );
        }
    }

    /**
     * Get a page to export users
     * 
     * @param request
     *            The request
     * @return The html content
     */
    @View( value = VIEW_EXPORT_USERS, securityTokenAction = ACTION_EXPORT_USERS )
    public String getExportUsers( HttpServletRequest request )
    {
        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                DatabaseResourceIdService.PERMISSION_IMPORT_EXPORT_DATABASE_USERS, (User) getUser( ) ) )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        setPageTitleProperty( PROPERTY_EXPORT_USERS_PAGETITLE );

        Map<String, Object> model = new HashMap<>( );

        return getPageWithToken( TEMPLATE_EXPORT_USERS_FROM_FILE, model );
    }

    /**
     * Do export users
     * 
     * @param request
     *            The request
     * @param response
     *            The response
     * @return A DefaultPluginActionResult containing the result, or null if the file download has been initialized
     * @throws IOException
     *             If an IOException occurs
     */
    @Action( ACTION_EXPORT_USERS )
    public String doExportUsers( HttpServletRequest request )
    {
        Plugin plugin = getPlugin( );

        if ( !RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                DatabaseResourceIdService.PERMISSION_IMPORT_EXPORT_DATABASE_USERS, (User) getUser( ) ) )
        {
            return redirectView( request, VIEW_MANAGE_USERS );
        }

        String strExportAttributes = request.getParameter( PARAMETER_EXPORT_ATTRIBUTES );
        String strExportRoles = request.getParameter( PARAMETER_EXPORT_ROLES );
        String strExportWorkgroups = request.getParameter( PARAMETER_EXPORT_WORKGROUPS );
        boolean bExportAttributes = StringUtils.isNotEmpty( strExportAttributes );
        boolean bExportRoles = StringUtils.isNotEmpty( strExportRoles );
        boolean bExportWorkgroups = StringUtils.isNotEmpty( strExportWorkgroups );

        Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersList( plugin );

        List<IAttribute> listAttributes = AttributeHome.findAll( getLocale( ), PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME ) );

        List<DatabaseUserWrapper> listUserExport = new ArrayList<>();

        for ( DatabaseUser user : listUsers )
        {
            if ( user.getStatus( ) != DatabaseUser.STATUS_ANONYMIZED )
            {
                listUserExport.add( _databaseService.getExportUser( user, bExportRoles, bExportWorkgroups, bExportAttributes, listAttributes ) );
            }
        }

        Map<String, Object> model = new HashMap<>( );
        model.put(MARK_EXPORT_USERS, listUserExport);

        String strFileName = CONSTANT_EXPORT_USERS_FILE_NAME + CONSTANT_EXTENSION_CSV_FILE;

        HtmlTemplate template = AppTemplateService.getTemplate(TEMPLATE_EXPORT_USERS, getLocale(), model);
        String strExportedUsers = template.getHtml();
        download( StringUtils.trim( strExportedUsers ), strFileName, CONSTANT_MIME_TYPE_CSV );

        return null;
    }

    /**
     * Gets the page of a view: the model goes to the models of the request, which carry the security token of the view, added to the forms of the page
     * 
     * @param strTemplate
     *            The template
     * @param model
     *            The model
     * @return The page
     */
    private String getPageWithToken( String strTemplate, Map<String, Object> model )
    {
        model.forEach( ( strKey, value ) -> {
            if ( value != null )
            {
                _models.put( strKey, value );
            }
        } );

        return getPage( _strPageTitleProperty, strTemplate );
    }

    /**
     * {@inheritDoc} The property is kept for the page built with the token of the view.
     */
    @Override
    public void setPageTitleProperty( String strPageTitleKey )
    {
        _strPageTitleProperty = strPageTitleKey;
        super.setPageTitleProperty( strPageTitleKey );
    }

    /**
     * Gets the URL of the list of the users
     * 
     * @return The URL
     */
    private String getManageUsersUrl( )
    {
        UrlItem url = new UrlItem( CONTROLLER_JSP );
        url.addParameter( PARAMETER_PLUGIN_NAME, getPlugin( ).getName( ) );

        return url.getUrl( );
    }

    /**
     * Gets the URL of a view of a user
     * 
     * @param strView
     *            The view
     * @param nUserId
     *            The id of the user
     * @return The URL
     */
    private String getUserViewUrl( String strView, int nUserId )
    {
        UrlItem url = new UrlItem( CONTROLLER_JSP );
        url.addParameter( "view", strView );
        url.addParameter( PARAMETER_PLUGIN_NAME, getPlugin( ).getName( ) );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID, nUserId );

        return url.getUrl( );
    }

    /**
     * Get the Plugin
     * 
     * @return The Plugin
     */
    @Override
    public Plugin getPlugin( )
    {
        Plugin plugin = super.getPlugin( );

        if ( plugin == null )
        {
            plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        }

        return plugin;
    }

}
