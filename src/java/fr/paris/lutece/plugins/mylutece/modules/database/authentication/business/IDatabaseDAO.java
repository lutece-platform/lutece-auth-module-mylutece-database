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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.BaseUser;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.security.LuteceAuthentication;

import java.util.Collection;
import java.util.List;


/**
 *
 * @author Etienne
 */
public interface IDatabaseDAO
{
    /**
     * Find users by login
     *
     * @param strLogin the login
     * @param plugin The Plugin using this data access service
     * @param authenticationService the LuteceAuthentication object
     * @return BaseUser the user corresponding to the login
     */
    BaseUser selectLuteceUserByLogin( String strLogin, Plugin plugin, LuteceAuthentication authenticationService );

    /**
     * Load the list of {@link BaseUser}
     * @param plugin The Plugin using this data access service
     * @param authenticationService the authentication service
     * @return The Collection of the {@link BaseUser}
     */
    Collection<BaseUser> selectLuteceUserList( Plugin plugin, LuteceAuthentication authenticationService );

    /**
     * Select the list of DatabaseUsers for a login
     *
     * @param strLogin The login of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the DatabaseUsers
     */
    List<String> selectUserRolesFromLogin( String strLogin, Plugin plugin );

    /**
     * Delete roles for a user
     * @param nIdUser The id of the user
     * @param plugin The Plugin using this data access service
     */
    void deleteRolesForUser( int nIdUser, Plugin plugin );

    /**
     * Assign a role to user
     * @param nIdUser The id of the user
     * @param strRoleKey The key of the role
     * @param plugin The Plugin using this data access service
     */
    void createRoleForUser( int nIdUser, String strRoleKey, Plugin plugin );

    /**
     * Find user's groups by login
     *
     * @param strLogin the login
     * @param plugin The Plugin using this data access service
     * @return ArrayList the group key list corresponding to the login
     */
    List<String> selectUserGroupsFromLogin( String strLogin, Plugin plugin );

    /**
     * Load the list of DatabaseUsers for a Lutece role
     * @param strRoleKey The role key of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the logins
     */
    Collection<String> selectLoginListForRoleKey( String strRoleKey, Plugin plugin );

    /**
     * Delete groups for a user
     * @param nIdUser The id of the user
     * @param plugin The Plugin using this data access service
     */
    void deleteGroupsForUser( int nIdUser, Plugin plugin );

    /**
     * Assign a group to user
     * @param nIdUser The id of the user
     * @param strGroupKey The key of the group
     * @param plugin The Plugin using this data access service
     */
    void createGroupForUser( int nIdUser, String strGroupKey, Plugin plugin );

    /**
     * Find assigned users to the given group
     * @param strGroupKey The group key
     * @param plugin Plugin
     * @return a list of DatabaseUser
     */
    List<DatabaseUser> selectGroupUsersFromGroupKey( String strGroupKey, Plugin plugin );
}
