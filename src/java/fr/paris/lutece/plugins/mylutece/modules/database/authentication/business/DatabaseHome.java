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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.security.LuteceAuthentication;
import jakarta.enterprise.inject.spi.CDI;

import java.sql.Timestamp;

import java.util.Collection;
import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for databaseUser objects
 */
public final class DatabaseHome
{
    // Static variable pointed at the DAO instance
    private static IDatabaseDAO _dao = CDI.current( ).select( IDatabaseDAO.class ).get( );

    /**
     * Private constructor - this class need not be instantiated
     */
    private DatabaseHome( )
    {
    }

    /**
     * Find users by login
     *
     * @param strLogin
     *            the login
     * @param plugin
     *            The Plugin using this data access service
     * @param authenticationService
     *            the LuteceAuthentication object
     * @return DatabaseUser the user corresponding to the login
     */
    public static BaseUser findLuteceUserByLogin( String strLogin, Plugin plugin, LuteceAuthentication authenticationService )
    {
        return _dao.selectLuteceUserByLogin( strLogin, plugin, authenticationService );
    }

    /**
     * Gets the reset password attribute of the user from his login
     *
     * @param strLogin
     *            the login
     * @param plugin
     *            The Plugin using this data access service
     * @return True if the password has to be changed, false otherwise
     */
    public static boolean findResetPasswordFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectResetPasswordFromLogin( strLogin, plugin );
    }

    /**
     * Gets the expiration date of the user's password
     * 
     * @param strLogin
     *            The login of the user
     * @param plugin
     *            The plugin
     * @return The expiration date of the user's password
     */
    public static Timestamp findPasswordMaxValideDateFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectPasswordMaxValideDateFromLogin( strLogin, plugin );
    }

    /**
     * Load the list of {@link BaseUser}
     * 
     * @param plugin
     *            The Plugin using this data access service
     * @param authenticationService
     *            the authentication service
     * @return The Collection of the {@link BaseUser}
     */
    public static Collection<BaseUser> findDatabaseUsersList( Plugin plugin, LuteceAuthentication authenticationService )
    {
        return _dao.selectLuteceUserList( plugin, authenticationService );
    }

    /**
     * Find user's roles by login
     *
     * @param strLogin
     *            the login
     * @param plugin
     *            The Plugin using this data access service
     * @return ArrayList the role key list corresponding to the login
     */
    public static List<String> findUserRolesFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectUserRolesFromLogin( strLogin, plugin );
    }

    /**
     * Delete roles for a user
     * 
     * @param nIdUser
     *            The id of the user
     * @param plugin
     *            The Plugin using this data access service
     */
    public static void removeRolesForUser( int nIdUser, Plugin plugin )
    {
        _dao.deleteRolesForUser( nIdUser, plugin );
    }

    /**
     * Assign a role to user
     * 
     * @param nIdUser
     *            The id of the user
     * @param strRoleKey
     *            The key of the role
     * @param plugin
     *            The Plugin using this data access service
     */
    public static void addRoleForUser( int nIdUser, String strRoleKey, Plugin plugin )
    {
        _dao.createRoleForUser( nIdUser, strRoleKey, plugin );
    }

    /**
     * Find user's groups by login
     *
     * @param strLogin
     *            the login
     * @param plugin
     *            The Plugin using this data access service
     * @return ArrayList the group key list corresponding to the login
     */
    public static List<String> findUserGroupsFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectUserGroupsFromLogin( strLogin, plugin );
    }

    /**
     * Delete groups for a user
     * 
     * @param nIdUser
     *            The id of the user
     * @param plugin
     *            The Plugin using this data access service
     */
    public static void removeGroupsForUser( int nIdUser, Plugin plugin )
    {
        _dao.deleteGroupsForUser( nIdUser, plugin );
    }

    /**
     * Assign a group to user
     * 
     * @param nIdUser
     *            The id of the user
     * @param strGroupKey
     *            The key of the group
     * @param plugin
     *            The Plugin using this data access service
     */
    public static void addGroupForUser( int nIdUser, String strGroupKey, Plugin plugin )
    {
        _dao.createGroupForUser( nIdUser, strGroupKey, plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects for a Lutece role
     *
     * @param strRoleKey
     *            The role of the databseUser
     * @param plugin
     *            The current plugin using this method
     * @return A collection of logins
     */
    public static Collection<String> findDatabaseUsersListForRoleKey( String strRoleKey, Plugin plugin )
    {
        return _dao.selectLoginListForRoleKey( strRoleKey, plugin );
    }

    /**
     * Find assigned users to the given group
     * 
     * @param strGroupKey
     *            The group key
     * @param plugin
     *            Plugin
     * @return a list of DatabaseUser
     */
    public static List<DatabaseUser> findGroupUsersFromGroupKey( String strGroupKey, Plugin plugin )
    {
        return _dao.selectGroupUsersFromGroupKey( strGroupKey, plugin );
    }

    /**
     * Update the reset password attribut of a user from his login
     * 
     * @param strUserName
     *            Login of the user to update
     * @param bNewValue
     *            New value
     * @param plugin
     *            the plugin
     */
    public static void updateResetPasswordFromLogin( String strUserName, boolean bNewValue, Plugin plugin )
    {
        _dao.updateResetPasswordFromLogin( strUserName, bNewValue, plugin );
    }

    /**
     * Get the id of a user from his login
     * 
     * @param strLogin
     *            Login of the user
     * @param plugin
     *            The plugin
     * @return The id of the user
     */
    public static int findUserIdFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.findUserIdFromLogin( strLogin, plugin );
    }
}
