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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * This class provides Data Access methods for Group objects
 */
@ApplicationScoped
@Named( "mylutece-database.databaseGroupDAO" )
public final class GroupDAO implements IGroupDAO
{
    // Constants
    private static final String PERCENT = "%";
    private static final String SQL_QUERY_SELECTALL = " SELECT group_key, group_description FROM mylutece_database_group ORDER BY group_key";
    private static final String SQL_QUERY_SELECT_BY_KEY = " SELECT group_key, group_description FROM mylutece_database_group WHERE group_key = ? ORDER BY group_key";
    private static final String SQL_QUERY_INSERT = " INSERT INTO mylutece_database_group ( group_key, group_description ) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE = " DELETE FROM mylutece_database_group WHERE group_key like ? ";
    private static final String SQL_QUERY_UPDATE = " UPDATE mylutece_database_group SET group_key = ?, group_description = ? WHERE group_key like ?";
    private static final String SQL_QUERY_SELECT_GROUP_FROM_SEARCH = " SELECT group_key, group_description FROM mylutece_database_group "
            + " WHERE group_key LIKE ? AND group_description LIKE ? ORDER BY group_key ";

    ///////////////////////////////////////////////////////////////////////////////////////
    // Access methods to data

    /**
     * Insert a new record in the table.
     * 
     * @param group
     *            The Instance of the object Group
     * @param plugin
     *            Plugin
     */
    @Override
    public synchronized void insert( Group group, Plugin plugin )
    {
        int nParam = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {

            daoUtil.setString( ++nParam, group.getGroupKey( ) );
            daoUtil.setString( ++nParam, group.getGroupDescription( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * load the data of Group from the table
     * 
     * @param strGroupKey
     *            The indentifier of the object Group
     * @param plugin
     *            Plugin
     * @return The Instance of the object Group
     */
    @Override
    public Group load( String strGroupKey, Plugin plugin )
    {
        Group group = null;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_KEY, plugin ) )
        {
            daoUtil.setString( 1, strGroupKey );

            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                int nParam = 0;
                group = new Group( );
                group.setGroupKey( daoUtil.getString( ++nParam ) );
                group.setGroupDescription( daoUtil.getString( ++nParam ) );
            }
        }
        return group;
    }

    /**
     * Delete a record from the table
     * 
     * @param strGroupKey
     *            The indentifier of the object Group
     * @param plugin
     *            Plugin
     */
    @Override
    public void delete( String strGroupKey, Plugin plugin )
    {
        int nParam = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setString( ++nParam, strGroupKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * Update the record in the table
     * 
     * @param group
     *            The instance of the Group to update
     * @param plugin
     *            Plugin
     */
    @Override
    public void store( Group group, Plugin plugin )
    {
        int nParam = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {

            daoUtil.setString( ++nParam, group.getGroupKey( ) );
            daoUtil.setString( ++nParam, group.getGroupDescription( ) );
            daoUtil.setString( ++nParam, group.getGroupKey( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * Returns a list of all the right group
     * 
     * @param plugin
     *            Plugin
     * @return A ReferenceList of group objects
     */
    @Override
    public ReferenceList selectGroupsList( Plugin plugin )
    {
        ReferenceList groupList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                int nParam = 0;
                Group group = new Group( );
                group.setGroupKey( daoUtil.getString( ++nParam ) );
                group.setGroupDescription( daoUtil.getString( ++nParam ) );
                groupList.addItem( group.getGroupKey( ), group.getGroupDescription( ) );
            }

        }
        return groupList;
    }

    /**
     * Load the list of groups
     * 
     * @param plugin
     *            Plugin
     * @return The Collection of the Groups
     */
    @Override
    public Collection<Group> selectAll( Plugin plugin )
    {
        int nParam;
        Collection<Group> listGroups = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                nParam = 0;

                Group group = new Group( );
                group.setGroupKey( daoUtil.getString( ++nParam ) );
                group.setGroupDescription( daoUtil.getString( ++nParam ) );

                listGroups.add( group );
            }

        }
        return listGroups;
    }

    /**
     * Return the filtered groups list
     *
     * @param gFilter
     *            filter
     * @param plugin
     *            Plugin
     * @return List of Group
     */
    @Override
    public List<Group> selectByFilter( GroupFilter gFilter, Plugin plugin )
    {
        List<Group> listFilteredGroups = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_GROUP_FROM_SEARCH, plugin ) )
        {

            daoUtil.setString( 1, PERCENT + gFilter.getKey( ) + PERCENT );
            daoUtil.setString( 2, PERCENT + gFilter.getDescription( ) + PERCENT );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Group group = new Group( );
                group.setGroupKey( daoUtil.getString( 1 ) );
                group.setGroupDescription( daoUtil.getString( 2 ) );

                listFilteredGroups.add( group );
            }

        }
        return listFilteredGroups;
    }
}
