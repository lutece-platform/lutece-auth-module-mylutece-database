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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for Group right objects
 */
public final class GroupRoleHome
{
    // Static variable pointed at the DAO instance
    private static IGroupRoleDAO _dao = SpringContextService.getBean( "mylutece-database.databaseGroupRoleDAO" );

    /**
     * Creates a new GroupHome object.
     */
    private GroupRoleHome( )
    {
    }

    /**
     * Find group's roles
     *
     * @param strGroupKey
     *            the login
     * @param plugin
     *            Plugin
     * @return ArrayList the role key list corresponding to the group
     */
    public static List<String> findGroupRoles( String strGroupKey, Plugin plugin )
    {
        return _dao.selectGroupRoles( strGroupKey, plugin );
    }

    /**
     * Find group's roles
     *
     * @param strRoleKey
     *            The Role key
     * @param plugin
     *            Plugin
     * @return ArrayList the groups key list corresponding to the role
     */
    public static List<String> findGroupRolesByRoleKey( String strRoleKey, Plugin plugin )
    {
        return _dao.selectGroupRolesByRoleKey( strRoleKey, plugin );
    }

    /**
     * Delete groups for a group
     * 
     * @param strGroupKey
     *            The key of the group
     * @param plugin
     *            Plugin
     */
    public static void removeRoles( String strGroupKey, Plugin plugin )
    {
        _dao.deleteRoles( strGroupKey, plugin );
    }

    /**
     * Assign a role to group
     * 
     * @param strGroupKey
     *            The key of the group
     * @param strRoleKey
     *            The key of the role
     * @param plugin
     *            Plugin
     */
    public static void addRole( String strGroupKey, String strRoleKey, Plugin plugin )
    {
        _dao.createRole( strGroupKey, strRoleKey, plugin );
    }
}
