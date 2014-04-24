/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
import fr.paris.lutece.util.ReferenceList;

import java.util.Collection;
import java.util.List;


/**
 *
 *
 */
public interface IGroupDAO
{
    /**
     * Delete a record from the table
     *
     * @param strGroupKey The indentifier of the object Group
     * @param plugin Plugin
     */
    void delete( String strGroupKey, Plugin plugin );

    /**
     * Insert a new record in the table.
     *
     * @param group The Instance of the object Group
     * @param plugin Plugin
     */
    void insert( Group group, Plugin plugin );

    /**
     * load the data of Group from the table
     *
     * @param strGroupKey The indentifier of the object Group
     * @param plugin Plugin
     * @return The Instance of the object Group
     */
    Group load( String strGroupKey, Plugin plugin );

    /**
     * Returns a list of all the right group
     *
     * @param plugin Plugin
     * @return A ReferenceList of group objects
     */
    ReferenceList selectGroupsList( Plugin plugin );

    /**
     * Load the list of groups
     *
     * @param plugin Plugin
     * @return The Collection of the Groups
     */
    Collection<Group> selectAll( Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param group The instance of the Group to update
     * @param plugin Plugin
     */
    void store( Group group, Plugin plugin );

    /**
     * Return the filtered groups list
     *
     * @param gFilter filter
     * @param plugin Plugin
     * @return List of Group
     */
    List<Group> selectByFilter( GroupFilter gFilter, Plugin plugin );
}
