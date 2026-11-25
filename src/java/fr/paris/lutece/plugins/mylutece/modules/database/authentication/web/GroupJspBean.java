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
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.Group;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.GroupRoleHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
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
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.html.ItemNavigator;
import fr.paris.lutece.util.sort.AttributeComparator;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
 * This class provides the user interface to manage Lutece group features ( manage, create, modify, remove )
 */
@SessionScoped
@Named( "mylutecedatabase_groupJspBean" )
@Controller( controllerJsp = GroupJspBean.CONTROLLER_JSP, controllerPath = GroupJspBean.CONTROLLER_PATH, right = GroupJspBean.RIGHT_GROUPS_MANAGEMENT, securityTokenEnabled = true )
public class GroupJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = -3940937010700725590L;

    // Right
    public static final String RIGHT_GROUPS_MANAGEMENT = "DATABASE_GROUPS_MANAGEMENT";

    // Constants
    private static final String AMPERSAND = "&";
    private static final String SPACE = " ";
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSED_BRACKET = ")";

    // JSP
    static final String CONTROLLER_JSP = "ManageGroups.jsp";
    static final String CONTROLLER_PATH = "jsp/admin/plugins/mylutece/modules/database/";
    private static final String CONTROLLER_URL = CONTROLLER_PATH + CONTROLLER_JSP;

    // Views
    static final String VIEW_MANAGE_GROUPS = "manageGroups";
    static final String VIEW_CREATE_GROUP = "createGroup";
    static final String VIEW_MODIFY_GROUP = "modifyGroup";
    static final String VIEW_CONFIRM_REMOVE_GROUP = "confirmRemoveGroup";
    static final String VIEW_MANAGE_ROLES_GROUP = "manageRolesGroup";
    static final String VIEW_MANAGE_USERS_GROUP = "manageUsersGroup";
    static final String VIEW_CONFIRM_UNASSIGN_USER_GROUP = "confirmUnassignUserGroup";

    // Actions
    static final String ACTION_CREATE_GROUP = "createGroup";
    static final String ACTION_MODIFY_GROUP = "modifyGroup";
    static final String ACTION_REMOVE_GROUP = "removeGroup";
    static final String ACTION_ASSIGN_ROLE_GROUP = "assignRoleGroup";
    static final String ACTION_ASSIGN_USERS_GROUP = "assignUsersGroup";
    static final String ACTION_UNASSIGN_USER_GROUP = "unassignUserGroup";

    // JSP: URL from the root of the webapp (messages, item navigators) or from the folder of the controller (redirections)
    private static final String JSP_URL_DO_REMOVE_GROUP = CONTROLLER_URL + "?action=" + ACTION_REMOVE_GROUP;
    private static final String JSP_URL_DO_UNASSIGN_USER_GROUP = CONTROLLER_URL + "?action=" + ACTION_UNASSIGN_USER_GROUP;
    private static final String JSP_URL_MODIFY_GROUP = CONTROLLER_URL + "?view=" + VIEW_MODIFY_GROUP;
    private static final String JSP_URL_MANAGE_ROLES_GROUP = CONTROLLER_URL + "?view=" + VIEW_MANAGE_ROLES_GROUP;
    private static final String JSP_URL_MANAGE_USERS_GROUP = CONTROLLER_URL + "?view=" + VIEW_MANAGE_USERS_GROUP;
    private static final String JSP_URL_CREATE_GROUP = CONTROLLER_URL + "?view=" + VIEW_CREATE_GROUP;
    private static final String JSP_MANAGE_GROUPS = CONTROLLER_JSP;

    // Markers
    private static final String MARK_GROUPS_LIST = "groups_list";
    private static final String MARK_GROUP = "group";
    private static final String MARK_ROLES_LIST = "role_list";
    private static final String MARK_ROLES_LIST_FOR_GROUP = "group_role_list";
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";
    private static final String MARK_ITEM_NAVIGATOR = "item_navigator";
    private static final String MARK_SEARCH_IS_SEARCH = "search_is_search";
    private static final String MARK_SORT_SEARCH_ATTRIBUTE = "sort_search_attribute";
    private static final String MARK_SEARCH_GROUP_FILTER = "search_group_filter";
    private static final String MARK_AVAILABLE_USERS = "available_users";
    private static final String MARK_ASSIGNED_USERS = "assigned_users";
    private static final String MARK_ASSIGNED_USERS_NUMBER = "assigned_users_number";

    // Parameters
    private static final String PARAMETER_GROUP_KEY = "group_key";
    private static final String PARAMETER_GROUP_DESCRIPTION = "group_description";
    private static final String PARAMETER_ROLE_KEY = "role_key";
    private static final String PARAMETER_CANCEL = "cancel";
    private static final String PARAMETER_MYLUTECE_DATABASE_USER_ID = "mylutece_database_user_id";
    private static final String PARAMETER_ANCHOR = "anchor";
    private static final String PARAMETER_AVAILABLE_USERS = "available_users";
    private static final String PARAMETER_MODIFY_GROUP = "modify_group";
    private static final String PARAMETER_ASSIGN_USER = "assign_user";
    private static final String PARAMETER_ASSIGN_ROLE = "assign_role";

    // Templates
    private static final String TEMPLATE_MANAGE_GROUPS = "admin/plugins/mylutece/modules/database/manage_groups.html";
    private static final String TEMPLATE_CREATE_GROUP = "admin/plugins/mylutece/modules/database/create_group.html";
    private static final String TEMPLATE_GROUP_MODIFY = "admin/plugins/mylutece/modules/database/modify_group.html";
    private static final String TEMPLATE_MANAGE_ROLES_GROUP = "admin/plugins/mylutece/modules/database/manage_roles_group.html";
    private static final String TEMPLATE_MANAGE_USERS_GROUP = "admin/plugins/mylutece/modules/database/manage_users_group.html";

    // Properties
    private static final String PROPERTY_PAGE_TITLE_CREATE_GROUP = "module.mylutece.database.create_group.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_GROUP = "module.mylutece.database.modify_group.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ROLES_GROUP = "module.mylutece.database.manage_roles_group.pageTitle";
    private static final String PROPERTY_GROUPS_PER_PAGE = "paginator.groups.itemsPerPage";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_USERS_GROUP = "module.mylutece.database.manage_users_group.pageTitle";
    private static final String PROPERTY_USERS_PER_PAGE = "paginator.users.itemsPerPage";

    // Message
    private static final String MESSAGE_GROUP_EXIST = "module.mylutece.database.message.groupExist";
    private static final String MESSAGE_CONFIRM_REMOVE = "module.mylutece.database.message.confirmRemoveGroup";
    private static final String MESSAGE_CONFIRM_UNASSIGN_USER = "module.mylutece.database.message.confirmUnassignUserGroup";
    private static final String MESSAGE_ERROR_MODIFY = "module.mylutece.database.message.errorModifyGroup";
    private static final String MESSAGE_ERROR_REMOVE = "module.mylutece.database.message.errorRemoveGroup";
    private static final String MESSAGE_ERROR_MANAGE_GROUPS = "module.mylutece.database.message.errorManageGroups";
    private int _nItemsPerPage;
    private int _nDefaultItemsPerPage;
    private String _strCurrentPageIndex;
    private Map<String, ItemNavigator> _itemNavigators = new HashMap<>( );
    private GroupFilter _gFilter;
    private String _strSortedAttributeName;
    private boolean _bIsAscSort;

    @Inject
    private DatabaseService _databaseService;
    @Inject
    private Models _models;
    private String _strPageTitleProperty;

    /**
     * Returns Group management form
     * 
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_GROUPS, defaultView = true )
    public String getManageGroups( HttpServletRequest request )
    {
        setPageTitleProperty( null );

        // Reinit session
        reinitItemNavigators( );

        List<Group> listGroups = getAuthorizedGroups( );

        // FILTER
        _gFilter = new GroupFilter( );

        boolean bIsSearch = _gFilter.setGroupFilter( request );
        List<Group> listFilteredGroups = GroupHome.findByFilter( _gFilter, getPlugin( ) );
        List<Group> listAvailableGroups = new ArrayList<>( );

        for ( Group filteredGroup : listFilteredGroups )
        {
            for ( Group group : listGroups )
            {
                if ( filteredGroup.getGroupKey( ).equals( group.getGroupKey( ) ) )
                {
                    listAvailableGroups.add( group );
                }
            }
        }

        // SORT
        _strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );

        String strAscSort = null;

        if ( _strSortedAttributeName != null )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );

            _bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( listAvailableGroups, new AttributeComparator( _strSortedAttributeName, _bIsAscSort ) );
        }

        String strURL = getHomeUrl( request );
        UrlItem url = new UrlItem( strURL );

        if ( _strSortedAttributeName != null )
        {
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, _strSortedAttributeName );
        }

        if ( strAscSort != null )
        {
            url.addParameter( Parameters.SORTED_ASC, strAscSort );
        }

        String strSortSearchAttribute = StringUtils.EMPTY;

        if ( bIsSearch )
        {
            _gFilter.setUrlAttributes( url );

            if ( StringUtils.isNotBlank( _gFilter.getUrlAttributes( ) ) )
            {
                strSortSearchAttribute = AMPERSAND + _gFilter.getUrlAttributes( );
            }
        }

        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_GROUPS_PER_PAGE, 50 );
        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        LocalizedPaginator<Group> paginator = new LocalizedPaginator<>( listAvailableGroups, _nItemsPerPage, url.getUrl( ),
                AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale( ) );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_GROUPS_LIST, paginator.getPageItems( ) );
        model.put( MARK_SEARCH_IS_SEARCH, bIsSearch );
        model.put( MARK_SEARCH_GROUP_FILTER, _gFilter );
        model.put( MARK_SORT_SEARCH_ATTRIBUTE, strSortSearchAttribute );

        return getPageWithToken( TEMPLATE_MANAGE_GROUPS, model );
    }

    /**
     * Insert a new group
     * 
     * @param request
     *            The HTTP request
     * @return String The html code page
     */
    @View( value = VIEW_CREATE_GROUP, securityTokenAction = ACTION_CREATE_GROUP )
    public String getCreateGroup( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE_GROUP );

        return getPageWithToken( TEMPLATE_CREATE_GROUP, new HashMap<>( ) );
    }

    /**
     * Create Group
     * 
     * @param request
     *            The HTTP request
     * @return String The url page
     */
    @Action( ACTION_CREATE_GROUP )
    public String doCreateGroup( HttpServletRequest request )
    {
        String strGroupKey = request.getParameter( PARAMETER_GROUP_KEY );
        String strGroupDescription = request.getParameter( PARAMETER_GROUP_DESCRIPTION );

        // Mandatory field
        if ( strGroupKey.length( ) == 0 )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP,
                    AppPathService.getBaseUrl( request ) + JSP_URL_CREATE_GROUP ) );
        }

        // check if group exist
        if ( GroupHome.findByPrimaryKey( strGroupKey, getPlugin( ) ) != null )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_GROUP_EXIST, AdminMessage.TYPE_STOP,
                    AppPathService.getBaseUrl( request ) + JSP_URL_CREATE_GROUP ) );
        }

        Group group = new Group( );
        group.setGroupKey( strGroupKey );
        group.setGroupDescription( strGroupDescription );
        GroupHome.create( group, getPlugin( ) );

        return redirect( request, getHomeUrl( request ) );
    }

    /**
     *
     * @param request
     *            The HTTP request
     * @return String The html code page
     */
    @View( value = VIEW_MODIFY_GROUP, securityTokenAction = ACTION_MODIFY_GROUP )
    public String getModifyGroup( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_GROUP );

        Group selectedGroup = getGroupFromRequest( request );

        if ( selectedGroup == null )
        {
            return redirectView( request, VIEW_MANAGE_GROUPS );
        }

        // ASSIGNED USERS NUMBER
        List<DatabaseUser> listAllAssignedUsers = DatabaseHome.findGroupUsersFromGroupKey( selectedGroup.getGroupKey( ), getPlugin( ) );
        int nAssignedUsersNumber = listAllAssignedUsers.size( );

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_MODIFY_GROUP, selectedGroup.getGroupKey( ), AppPathService.getBaseUrl( request ) + JSP_URL_MODIFY_GROUP );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_GROUP, selectedGroup );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_MODIFY_GROUP ) );
        model.put( MARK_ASSIGNED_USERS_NUMBER, nAssignedUsersNumber );

        return getPageWithToken( TEMPLATE_GROUP_MODIFY, model );
    }

    /**
     * Modify group
     * 
     * @param request
     *            The HTTP request
     * @return String The url page
     */
    @Action( ACTION_MODIFY_GROUP )
    public String doModifyGroup( HttpServletRequest request )
    {
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_GROUPS;
        }
        else
        {
            String strGroupKey = request.getParameter( PARAMETER_GROUP_KEY );
            String strGroupDescription = request.getParameter( PARAMETER_GROUP_DESCRIPTION );

            Group group = getGroupFromRequest( request );

            if ( group == null )
            {
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_MODIFY, AdminMessage.TYPE_ERROR, getHomeUrl( request ) ) );
            }

            // Mandatory field
            if ( strGroupKey.length( ) == 0 )
            {
                UrlItem urlModify = new UrlItem( AppPathService.getBaseUrl( request ) + JSP_URL_MODIFY_GROUP );
                urlModify.addParameter( PARAMETER_GROUP_KEY, URLEncoder.encode( group.getGroupKey( ), StandardCharsets.UTF_8 ) );

                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP, urlModify.getUrl( ) ) );
            }

            group.setGroupKey( strGroupKey );
            group.setGroupDescription( strGroupDescription );
            GroupHome.update( group, getPlugin( ) );
            strReturn = getGroupViewUrl( VIEW_MODIFY_GROUP, group.getGroupKey( ) );
        }

        return redirect( request, strReturn );
    }

    /**
     * confirm Delete Group
     * 
     * @param request
     *            The HTTP request
     * @return String The html code page
     */
    @View( value = VIEW_CONFIRM_REMOVE_GROUP, securityTokenAction = ACTION_REMOVE_GROUP )
    public String getRemoveGroup( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_URL_DO_REMOVE_GROUP );
        url.addParameter( PARAMETER_GROUP_KEY, request.getParameter( PARAMETER_GROUP_KEY ) );

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Delete Group
     * 
     * @param request
     *            The HTTP request
     * @return String The url page
     */
    @Action( ACTION_REMOVE_GROUP )
    public String doRemoveGroup( HttpServletRequest request )
    {
        Group group = getGroupFromRequest( request );

        if ( group == null )
        {
            return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_REMOVE, AdminMessage.TYPE_ERROR, getHomeUrl( request ) ) );
        }

        GroupHome.remove( group.getGroupKey( ), getPlugin( ) );

        return redirect( request, getHomeUrl( request ) );
    }

    /**
     * Returns roles management form for a specified group
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @View( value = VIEW_MANAGE_ROLES_GROUP, securityTokenAction = ACTION_ASSIGN_ROLE_GROUP )
    public String getManageRolesGroup( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_ROLES_GROUP );

        Group selectedGroup = getGroupFromRequest( request );

        if ( selectedGroup == null )
        {
            return redirectView( request, VIEW_MANAGE_GROUPS );
        }

        // ASSIGNED USERS NUMBER
        List<DatabaseUser> listAllAssignedUsers = DatabaseHome.findGroupUsersFromGroupKey( selectedGroup.getGroupKey( ), getPlugin( ) );
        int nAssignedUsersNumber = listAllAssignedUsers.size( );

        Collection<Role> allRoleList = RoleHome.findAll( );
        allRoleList = AdminWorkgroupService.getAuthorizedCollection( allRoleList, (User) getUser( ) );

        List<String> groupRoleKeyList = GroupRoleHome.findGroupRoles( selectedGroup.getGroupKey( ), getPlugin( ) );
        Collection<Role> groupRoleList = new ArrayList<>( );

        for ( String strRoleKey : groupRoleKeyList )
        {
            for ( Role role : allRoleList )
            {
                if ( role.getRole( ).equals( strRoleKey ) )
                {
                    groupRoleList.add( RoleHome.findByPrimaryKey( strRoleKey ) );
                }
            }
        }

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_ASSIGN_ROLE, selectedGroup.getGroupKey( ), AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_ROLES_GROUP );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_ROLES_LIST, allRoleList );
        model.put( MARK_ROLES_LIST_FOR_GROUP, groupRoleList );
        model.put( MARK_GROUP, selectedGroup );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_ASSIGN_ROLE ) );
        model.put( MARK_ASSIGNED_USERS_NUMBER, nAssignedUsersNumber );

        return getPageWithToken( TEMPLATE_MANAGE_ROLES_GROUP, model );
    }

    /**
     * Process assignation roles for a specified group
     *
     * @param request
     *            The Http request
     * @return Html form
     */
    @Action( ACTION_ASSIGN_ROLE_GROUP )
    public String doAssignRoleGroup( HttpServletRequest request )
    {
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_GROUPS;
        }
        else
        {
            // get group
            Group group = getGroupFromRequest( request );

            if ( group == null )
            {
                return redirect( request, AdminMessageService.getMessageUrlReturningTo( request, MESSAGE_ERROR_MANAGE_GROUPS, AdminMessage.TYPE_ERROR,
                        getHomeUrl( request ) ) );
            }

            String [ ] roleArray = request.getParameterValues( PARAMETER_ROLE_KEY );

            GroupRoleHome.removeRoles( group.getGroupKey( ), getPlugin( ) );

            if ( roleArray != null )
            {
                for ( int i = 0; i < roleArray.length; i++ )
                {
                    GroupRoleHome.addRole( group.getGroupKey( ), roleArray [i], getPlugin( ) );
                }
            }

            strReturn = getGroupViewUrl( VIEW_MANAGE_ROLES_GROUP, group.getGroupKey( ) );
        }

        return redirect( request, strReturn );
    }

    /**
     *
     * @param request
     *            The HTTP request
     * @return the group
     */
    private Group getGroupFromRequest( HttpServletRequest request )
    {
        String strGroupKey = request.getParameter( PARAMETER_GROUP_KEY );

        if ( ( strGroupKey == null ) || ( strGroupKey.length( ) == 0 ) )
        {
            return null;
        }

        return GroupHome.findByPrimaryKey( strGroupKey, getPlugin( ) );
    }

    /**
     * Get the list of authorized group
     * 
     * @return a list of groups
     */
    private List<Group> getAuthorizedGroups( )
    {
        Collection<Group> allGroupList = GroupHome.findAll( getPlugin( ) );
        List<Group> groupList = new ArrayList<>( );

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

                if ( AdminWorkgroupService.isAuthorized( role, (User) getUser( ) ) )
                {
                    groupList.add( group );

                    break;
                }
            }
        }

        return groupList;
    }

    /**
     * Returns users management formfor a specified group
     * 
     * @param request
     *            HttpServletRequest
     * @return Html form
     */
    @View( value = VIEW_MANAGE_USERS_GROUP, securityTokenAction = ACTION_ASSIGN_USERS_GROUP )
    public String getManageUsersGroup( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_USERS_GROUP );

        Map<String, Object> model = new HashMap<>( );
        String strURL = AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_USERS_GROUP;
        UrlItem url = new UrlItem( strURL );

        // SELECTED GROUP
        Group selectedGroup = getGroupFromRequest( request );

        if ( selectedGroup == null )
        {
            return redirectView( request, VIEW_MANAGE_GROUPS );
        }

        // ASSIGNED USERS
        List<DatabaseUser> listAllAssignedUsers = DatabaseHome.findGroupUsersFromGroupKey( selectedGroup.getGroupKey( ), getPlugin( ) );
        List<DatabaseUser> listAssignedUsers = getListAssignedUsers( listAllAssignedUsers );

        DatabaseUserFilter duFilter = new DatabaseUserFilter( );
        boolean bIsSearch = duFilter.setDatabaseUserFilter( request );
        List<DatabaseUser> listFilteredUsers = _databaseService.getFilteredUsersInterface( duFilter, bIsSearch, listAssignedUsers, request, model, url );

        // AVAILABLE USERS
        ReferenceList listAvailableUsers = getAvailableUsers( listAssignedUsers );

        // SORT
        String strSortedAttributeName = request.getParameter( Parameters.SORTED_ATTRIBUTE_NAME );
        String strAscSort = null;

        if ( strSortedAttributeName != null )
        {
            strAscSort = request.getParameter( Parameters.SORTED_ASC );

            boolean bIsAscSort = Boolean.parseBoolean( strAscSort );

            Collections.sort( listFilteredUsers, new AttributeComparator( strSortedAttributeName, bIsAscSort ) );
        }

        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_USERS_PER_PAGE, 50 );
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        if ( strSortedAttributeName != null )
        {
            url.addParameter( Parameters.SORTED_ATTRIBUTE_NAME, strSortedAttributeName );
        }

        if ( strAscSort != null )
        {
            url.addParameter( Parameters.SORTED_ASC, strAscSort );
        }

        // ITEM NAVIGATION
        setItemNavigator( PARAMETER_ASSIGN_USER, selectedGroup.getGroupKey( ), url.getUrl( ) );

        LocalizedPaginator<DatabaseUser> paginator = new LocalizedPaginator<>( listFilteredUsers, _nItemsPerPage, url.getUrl( ),
                AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale( ) );

        model.put( MARK_GROUP, selectedGroup );
        model.put( MARK_ITEM_NAVIGATOR, _itemNavigators.get( PARAMETER_ASSIGN_USER ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( MARK_NB_ITEMS_PER_PAGE, Integer.toString( _nItemsPerPage ) );
        model.put( MARK_AVAILABLE_USERS, listAvailableUsers );
        model.put( MARK_ASSIGNED_USERS, paginator.getPageItems( ) );
        model.put( MARK_ASSIGNED_USERS_NUMBER, listAllAssignedUsers.size( ) );

        return getPageWithToken( TEMPLATE_MANAGE_USERS_GROUP, model );
    }

    /**
     * Assign users to a group
     * 
     * @param request
     *            HttpServletRequest
     * @return JSP return
     */
    @Action( ACTION_ASSIGN_USERS_GROUP )
    public String doAssignUsersGroup( HttpServletRequest request )
    {
        String strReturn;

        String strActionCancel = request.getParameter( PARAMETER_CANCEL );

        if ( strActionCancel != null )
        {
            strReturn = JSP_MANAGE_GROUPS;
        }
        else
        {
            Group selectedGroup = getGroupFromRequest( request );

            if ( selectedGroup == null )
            {
                return redirectView( request, VIEW_MANAGE_GROUPS );
            }

            // retrieve the selected portlets ids
            String [ ] arrayUsersIds = request.getParameterValues( PARAMETER_AVAILABLE_USERS );

            if ( ( arrayUsersIds != null ) )
            {
                for ( int i = 0; i < arrayUsersIds.length; i++ )
                {
                    int nUserId = Integer.parseInt( arrayUsersIds [i] );
                    DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nUserId, getPlugin( ) );
                    DatabaseHome.addGroupForUser( user.getUserId( ), selectedGroup.getGroupKey( ), getPlugin( ) );
                }
            }

            strReturn = getGroupViewUrl( VIEW_MANAGE_USERS_GROUP, selectedGroup.getGroupKey( ) );
        }

        return redirect( request, strReturn );
    }

    /**
     * Asks the confirmation of the unassignment of a user from a group
     * 
     * @param request
     *            The HTTP request
     * @return The URL of the confirmation message
     */
    @View( value = VIEW_CONFIRM_UNASSIGN_USER_GROUP, securityTokenAction = ACTION_UNASSIGN_USER_GROUP )
    public String getConfirmUnassignUserGroup( HttpServletRequest request )
    {
        UrlItem url = new UrlItem( JSP_URL_DO_UNASSIGN_USER_GROUP );
        url.addParameter( PARAMETER_GROUP_KEY, request.getParameter( PARAMETER_GROUP_KEY ) );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID, request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ) );
        if ( request.getParameter( PARAMETER_ANCHOR ) != null )
        {
            url.addParameter( PARAMETER_ANCHOR, request.getParameter( PARAMETER_ANCHOR ) );
        }

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_UNASSIGN_USER, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Unassign user from a given group
     * 
     * @param request
     *            HttpServletRequest
     * @return JSP return
     */
    @Action( ACTION_UNASSIGN_USER_GROUP )
    public String doUnassignUserGroup( HttpServletRequest request )
    {
        Group selectedGroup = getGroupFromRequest( request );

        if ( selectedGroup == null )
        {
            return redirectView( request, VIEW_MANAGE_GROUPS );
        }

        int nIdUser = NumberUtils.toInt( request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ), -1 );
        String strAnchor = request.getParameter( PARAMETER_ANCHOR );

        DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nIdUser, getPlugin( ) );

        if ( user != null )
        {
            DatabaseHome.removeGroupForUser( user.getUserId( ), selectedGroup.getGroupKey( ), getPlugin( ) );
        }

        UrlItem url = new UrlItem( getGroupViewUrl( VIEW_MANAGE_USERS_GROUP, selectedGroup.getGroupKey( ) ) );
        url.setAnchor( strAnchor );

        return redirect( request, url.getUrl( ) );
    }

    /**
     * Get the list of assigned user to the given group
     * 
     * @param listAllAssignedUsers
     *            the list of all assigned userse
     * @return a list of {@link DatabaseUser}
     */
    private List<DatabaseUser> getListAssignedUsers( List<DatabaseUser> listAllAssignedUsers )
    {
        List<DatabaseUser> listAssignedUsers = new ArrayList<>( );

        for ( DatabaseUser user : listAllAssignedUsers )
        {
            if ( _databaseService.isAuthorized( user, getUser( ), getPlugin( ) ) )
            {
                listAssignedUsers.add( user );
            }
        }

        return listAssignedUsers;
    }

    /**
     * Get the list of avaivable users
     * 
     * @param listAssignedUsers
     *            the list of assigned users
     * @return a {@link ReferenceList}
     */
    private ReferenceList getAvailableUsers( List<DatabaseUser> listAssignedUsers )
    {
        ReferenceList listAvailableUsers = new ReferenceList( );

        for ( DatabaseUser user : DatabaseUserHome.findDatabaseUsersList( getPlugin( ) ) )
        {
            boolean bIsAvailable = Boolean.TRUE;

            for ( DatabaseUser assignedUser : listAssignedUsers )
            {
                if ( !_databaseService.isAuthorized( user, getUser( ), getPlugin( ) ) || ( user.getUserId( ) == assignedUser.getUserId( ) ) )
                {
                    bIsAvailable = Boolean.FALSE;

                    break;
                }
            }

            if ( bIsAvailable )
            {
                ReferenceItem userItem = new ReferenceItem( );
                userItem.setCode( String.valueOf( user.getUserId( ) ) );
                userItem.setName( user.getLastName( ) + SPACE + user.getFirstName( ) + SPACE + OPEN_BRACKET + user.getLogin( ) + CLOSED_BRACKET );
                listAvailableUsers.add( userItem );
            }
        }

        return listAvailableUsers;
    }

    /**
     * Set the item navigator
     * 
     * @param strItemNavigatorKey
     *            The item navigator key
     * @param strGroupKey
     *            the group key
     * @param strUrl
     *            the url
     */
    private void setItemNavigator( String strItemNavigatorKey, String strGroupKey, String strUrl )
    {
        ItemNavigator itemNavigator = _itemNavigators.get( strItemNavigatorKey );

        if ( itemNavigator == null )
        {
            List<Group> listGroups = getAuthorizedGroups( );

            // FILTER
            if ( _gFilter == null )
            {
                _gFilter = new GroupFilter( );
            }

            List<Group> listFilteredGroups = GroupHome.findByFilter( _gFilter, getPlugin( ) );
            List<Group> listAvailableGroups = new ArrayList<>( );

            for ( Group filteredGroup : listFilteredGroups )
            {
                for ( Group group : listGroups )
                {
                    if ( filteredGroup.getGroupKey( ).equals( group.getGroupKey( ) ) )
                    {
                        listAvailableGroups.add( group );
                    }
                }
            }

            // SORT
            if ( StringUtils.isNotBlank( _strSortedAttributeName ) )
            {
                Collections.sort( listAvailableGroups, new AttributeComparator( _strSortedAttributeName, _bIsAscSort ) );
            }

            List<String> listIdsDatabaseUser = new ArrayList<>( );
            int nCurrentItemId = 0;
            int nIndex = 0;

            for ( Group group : listAvailableGroups )
            {
                if ( group != null )
                {
                    listIdsDatabaseUser.add( group.getGroupKey( ) );

                    if ( group.getGroupKey( ).equals( strGroupKey ) )
                    {
                        nCurrentItemId = nIndex;
                    }

                    nIndex++;
                }
            }

            itemNavigator = new ItemNavigator( listIdsDatabaseUser, nCurrentItemId, strUrl, PARAMETER_GROUP_KEY );
        }
        else
        {
            itemNavigator.setCurrentItemId( strGroupKey );
        }

        _itemNavigators.put( strItemNavigatorKey, itemNavigator );
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
     * Gets the URL of a view of a group, from the folder of the controller
     * 
     * @param strView
     *            The view
     * @param strGroupKey
     *            The key of the group
     * @return The URL
     */
    private String getGroupViewUrl( String strView, String strGroupKey )
    {
        UrlItem url = new UrlItem( CONTROLLER_JSP );
        url.addParameter( "view", strView );
        url.addParameter( PARAMETER_GROUP_KEY, URLEncoder.encode( strGroupKey, StandardCharsets.UTF_8 ) );

        return url.getUrl( );
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
}
