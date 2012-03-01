/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.plugins.mylutece.authentication.MultiLuteceAuthentication;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeField;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeFieldHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldFilter;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFieldListener;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserRoleRemovalListener;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.DatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.role.RoleRemovalListenerService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * DatabaseService
 *
 */
public final class DatabaseService
{
    private static final String BEAN_DATABASE_SERVICE = "mylutece-database.databaseService";
    private static final String AUTHENTICATION_BEAN_NAME = "mylutece-database.authentication";

    // CONSTANTS
    private static final String COMMA = ",";
    private static final String EMPTY_STRING = "";
    private static final String AMPERSAND = "&";
    private static final String PLUGIN_JCAPTCHA = "jcaptcha";

    // MARKS
    private static final String MARK_LIST_USER_PARAM_DEFAULT_VALUES = "list_user_param_default_values";
    private static final String MARK_ENCRYPTION_ALGORITHMS_LIST = "encryption_algorithms_list";
    private static final String MARK_SEARCH_IS_SEARCH = "search_is_search";
    private static final String MARK_SORT_SEARCH_ATTRIBUTE = "sort_search_attribute";
    private static final String MARK_SEARCH_USER_FILTER = "search_user_filter";
    private static final String MARK_SEARCH_MYLUTECE_USER_FIELD_FILTER = "search_mylutece_user_field_filter";
    private static final String MARK_ATTRIBUTES_LIST = "attributes_list";
    private static final String MARK_IS_PLUGIN_JCAPTCHA_ENABLE = "is_plugin_jcatpcha_enable";

    // PROPERTIES
    private static final String PROPERTY_ENCRYPTION_ALGORITHMS_LIST = "encryption.algorithmsList";

    // VARIABLES
    private DatabaseUserParameterService _userParamService;

    /**
     * Private constructor
     */
    private DatabaseService(  )
    {
    }

    /**
     * Set the database user parameter service
     * @param userParamService the user parameter service
     */
    public void setDatabaseUserParameterService( DatabaseUserParameterService userParamService )
    {
        _userParamService = userParamService;
    }

    /**
    * Initialize the Database service
    *
    */
    public void init(  )
    {
        RoleRemovalListenerService.getService(  ).registerListener( new DatabaseUserRoleRemovalListener(  ) );
        DatabaseMyLuteceUserFieldListenerService.getService(  ).registerListener( new DatabaseUserFieldListener(  ) );

        BaseAuthentication baseAuthentication = (BaseAuthentication) SpringContextService.getPluginBean( DatabasePlugin.PLUGIN_NAME,
                AUTHENTICATION_BEAN_NAME );

        if ( baseAuthentication != null )
        {
            MultiLuteceAuthentication.registerAuthentication( baseAuthentication );
        }
        else
        {
            AppLogService.error( "BaseAuthentication not found, please check your database_context.xml configuration" );
        }
    }

    /**
     * Returns the instance of the singleton
     * @return The instance of the singleton
     */
    public static DatabaseService getService(  )
    {
        return (DatabaseService) SpringContextService.getPluginBean( DatabasePlugin.PLUGIN_NAME, BEAN_DATABASE_SERVICE );
    }

    /**
     * Build the advanced parameters management
     * @param user the admin user
     * @return The model for the advanced parameters
     */
    public Map<String, Object> getManageAdvancedParameters( AdminUser user )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        // Encryption Password
        if ( RBACService.isAuthorized( DatabaseResourceIdService.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID,
                    DatabaseResourceIdService.PERMISSION_MANAGE, user ) )
        {
            String strAlgorithms = AppPropertiesService.getProperty( PROPERTY_ENCRYPTION_ALGORITHMS_LIST );

            if ( StringUtils.isNotBlank( strAlgorithms ) )
            {
                String[] listAlgorithms = strAlgorithms.split( COMMA );

                model.put( MARK_LIST_USER_PARAM_DEFAULT_VALUES, _userParamService.findAll( plugin ) );
                model.put( MARK_ENCRYPTION_ALGORITHMS_LIST, listAlgorithms );
                model.put( MARK_IS_PLUGIN_JCAPTCHA_ENABLE, isPluginJcaptchaEnable(  ) );
            }
        }

        return model;
    }

    /**
     * Check if an Lutece user should be visible to the user according its workgroup
     * @param user the Lutece user
     * @param adminUser the admin user
     * @param plugin the plugin
     * @return true if the Lutece user should be visible, false otherwise
     */
    public boolean isAuthorized( DatabaseUser user, AdminUser adminUser, Plugin plugin )
    {
        boolean bHasRole = false;
        List<String> userRoleKeyList = DatabaseHome.findUserRolesFromLogin( user.getLogin(  ), plugin );

        for ( String userRoleKey : userRoleKeyList )
        {
            bHasRole = true;

            Role role = RoleHome.findByPrimaryKey( userRoleKey );

            if ( AdminWorkgroupService.isAuthorized( role, adminUser ) )
            {
                return true;
            }
        }

        List<String> userGroupKeyList = DatabaseHome.findUserGroupsFromLogin( user.getLogin(  ), plugin );

        for ( String userGroupKey : userGroupKeyList )
        {
            List<String> groupRoleKeyList = GroupRoleHome.findGroupRoles( userGroupKey, plugin );

            for ( String groupRoleKey : groupRoleKeyList )
            {
                bHasRole = true;

                Role role = RoleHome.findByPrimaryKey( groupRoleKey );

                if ( AdminWorkgroupService.isAuthorized( role, adminUser ) )
                {
                    return true;
                }
            }
        }

        return !bHasRole;
    }

    /**
     * Get authorized users list
     * @param adminUser the admin user
     * @param plugin the plugin
     * @return a list of users
     */
    public List<DatabaseUser> getAuthorizedUsers( AdminUser adminUser, Plugin plugin )
    {
        Collection<DatabaseUser> userList = DatabaseUserHome.findDatabaseUsersList( plugin );
        List<DatabaseUser> authorizedUserList = new ArrayList<DatabaseUser>(  );

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
     * @param listUsers the initial list to filter
     * @param request HttpServletRequest
     * @param model Map
     * @param url UrlItem
     * @return the filtered list
     */
    public List<DatabaseUser> getFilteredUsersInterface( DatabaseUserFilter duFilter, boolean bIsSearch, List<DatabaseUser> listUsers, 
    		HttpServletRequest request, Map<String, Object> model, UrlItem url )
    {
    	Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
    	List<DatabaseUser> filteredUsers = getListFilteredUsers( request, duFilter, listUsers );
    	MyLuteceUserFieldFilter mlFieldFilter = new MyLuteceUserFieldFilter(  );
        mlFieldFilter.setMyLuteceUserFieldFilter( request, request.getLocale(  ) );

        List<IAttribute> listAttributes = AttributeHome.findAll( request.getLocale(  ), myLutecePlugin );

        for ( IAttribute attribute : listAttributes )
        {
            List<AttributeField> listAttributeFields = AttributeFieldHome.selectAttributeFieldsByIdAttribute( attribute.getIdAttribute(  ),
                    myLutecePlugin );
            attribute.setListAttributeFields( listAttributeFields );
        }

        String strSortSearchAttribute = EMPTY_STRING;

        if ( bIsSearch )
        {
            duFilter.setUrlAttributes( url );

            if ( duFilter.getUrlAttributes(  ) != EMPTY_STRING )
            {
                strSortSearchAttribute = AMPERSAND + duFilter.getUrlAttributes(  );
            }

            mlFieldFilter.setUrlAttributes( url );

            if ( mlFieldFilter.getUrlAttributes(  ) != EMPTY_STRING )
            {
                strSortSearchAttribute += ( AMPERSAND + mlFieldFilter.getUrlAttributes(  ) );
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
     * @param request the HTTP request
     * @param duFilter the filter
     * @param listUsers the list of users
     * @return a list of {@link DatabaseUser}
     */
    public List<DatabaseUser> getListFilteredUsers( HttpServletRequest request, DatabaseUserFilter duFilter, List<DatabaseUser> listUsers )
    {
    	Plugin plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

        List<DatabaseUser> listFilteredUsers = DatabaseUserHome.findDatabaseUsersListByFilter( duFilter, plugin );
        List<DatabaseUser> listAvailableUsers = new ArrayList<DatabaseUser>(  );

        for ( DatabaseUser filteredUser : listFilteredUsers )
        {
            for ( DatabaseUser user : listUsers )
            {
                if ( filteredUser.getUserId(  ) == user.getUserId(  ) )
                {
                    listAvailableUsers.add( user );
                }
            }
        }

        Plugin myLutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<DatabaseUser> filteredUsers = new ArrayList<DatabaseUser>(  );

        MyLuteceUserFieldFilter mlFieldFilter = new MyLuteceUserFieldFilter(  );
        mlFieldFilter.setMyLuteceUserFieldFilter( request, request.getLocale(  ) );

        List<Integer> listFilteredUserIdsByUserFields = MyLuteceUserFieldHome.findUsersByFilter( mlFieldFilter,
                myLutecePlugin );

        if ( listFilteredUserIdsByUserFields != null )
        {
            for ( DatabaseUser filteredUser : listAvailableUsers )
            {
                for ( Integer nFilteredUserIdByUserField : listFilteredUserIdsByUserFields )
                {
                    if ( filteredUser.getUserId(  ) == nFilteredUserIdByUserField )
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
     * @param user the user
     * @param strPassword the password
     * @param plugin the plugin
     * @return the new database user with a new ID
     */
    public DatabaseUser doCreateUser( DatabaseUser user, String strPassword, Plugin plugin )
    {
        String strEncryptedPassword = strPassword;

        if ( _userParamService.isPasswordEncrypted( plugin ) )
        {
            String strAlgorithm = _userParamService.getEncryptionAlgorithm( plugin );
            strEncryptedPassword = CryptoService.encrypt( strPassword, strAlgorithm );
        }

        return DatabaseUserHome.create( user, strEncryptedPassword, plugin );
    }

    /**
     * Do modify the password
     * @param user the DatabaseUser
     * @param strPassword the new password
     * @param plugin the plugin
     */
    public void doModifyPassword( DatabaseUser user, String strPassword, Plugin plugin )
    {
        // Updates password
        if ( StringUtils.isNotBlank( strPassword ) )
        {
            // Encrypts password or not
            String strEncryptedPassword = strPassword;

            if ( _userParamService.isPasswordEncrypted( plugin ) )
            {
                String strAlgorithm = _userParamService.getEncryptionAlgorithm( plugin );
                strEncryptedPassword = CryptoService.encrypt( strPassword, strAlgorithm );
            }

            DatabaseUser userStored = DatabaseUserHome.findByPrimaryKey( user.getUserId(  ), plugin );

            if ( userStored != null )
            {
                DatabaseUserHome.updatePassword( userStored, strEncryptedPassword, plugin );
            }
        }
    }

    /**
     * Update the info of the user
     * @param user the user
     * @param plugin the plugin
     */
    public void doUpdateUser( DatabaseUser user, Plugin plugin )
    {
        DatabaseUserHome.update( user, plugin );
    }

    /**
     * Check the password
     * @param strUserGuid the user guid
     * @param strPassword the password
     * @param plugin the plugin
     * @return true if the password is the same as stored in the database, false otherwise
     */
    public boolean checkPassword( String strUserGuid, String strPassword, Plugin plugin )
    {
        String strEncryptedPassword = strPassword;

        if ( _userParamService.isPasswordEncrypted( plugin ) )
        {
            String strAlgorithm = _userParamService.getEncryptionAlgorithm( plugin );
            strEncryptedPassword = CryptoService.encrypt( strPassword, strAlgorithm );
        }

        return DatabaseUserHome.checkPassword( strUserGuid, strEncryptedPassword, plugin );
    }

    /**
     * Check if the user is active or not
     * @param strUserName the user name
     * @param plugin the plugin
     * @return true if it is active, false otherwise
     */
    public boolean isUserActive( String strUserName, Plugin plugin )
    {
        boolean bIsActive = false;

        List<DatabaseUser> listUsers = (List<DatabaseUser>) DatabaseUserHome.findDatabaseUsersListForLogin( strUserName,
                plugin );

        if ( ( listUsers != null ) && !listUsers.isEmpty(  ) )
        {
            DatabaseUser user = listUsers.get( 0 );
            bIsActive = user.isActive(  );
        }

        return bIsActive;
    }

    /**
     * Check if the plugin jcaptcha is activated or not
     * @return true if it is activated, false otherwise
     */
    public boolean isPluginJcaptchaEnable(  )
    {
        return PluginService.isPluginEnable( PLUGIN_JCAPTCHA );
    }
}
