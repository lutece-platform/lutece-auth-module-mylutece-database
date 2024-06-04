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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 *
 * DatabaseUserKeyDAO
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseUserKeyDAO" )
public class DatabaseUserKeyDAO implements IDatabaseUserKeyDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT mylutece_database_user_key, mylutece_database_user_id FROM mylutece_database_key WHERE mylutece_database_user_key = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO mylutece_database_key (mylutece_database_user_key, mylutece_database_user_id) VALUES ( ?,? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM mylutece_database_key ";
    private static final String SQL_WHERE = " WHERE ";
    private static final String SQL_USER_KEY = " mylutece_database_user_key = ? ";
    private static final String SQL_USER_ID = " mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECT_BY_LOGIN = " SELECT mdk.mylutece_database_user_key, mdk.mylutece_database_user_id FROM mylutece_database_key mdk LEFT JOIN mylutece_database_user mdu ON (mdu.mylutece_database_user_id = mdk.mylutece_database_user_id) WHERE mdu.login = ? ";

    /**
     * {@inheritDoc}
     */
    public void insert( DatabaseUserKey userKey, Plugin plugin )
    {
        int nIndex = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            daoUtil.setString( ++nIndex, userKey.getKey( ) );
            daoUtil.setInt( ++nIndex, userKey.getUserId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    public DatabaseUserKey load( String strKey, Plugin plugin )
    {
        DatabaseUserKey userKey = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setString( 1, strKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                int nIndex = 0;
                userKey = new DatabaseUserKey( );
                userKey.setKey( daoUtil.getString( ++nIndex ) );
                userKey.setUserId( daoUtil.getInt( ++nIndex ) );
            }
        }
        return userKey;
    }

    /**
     * {@inheritDoc}
     */
    public void delete( String strKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE + SQL_WHERE + SQL_USER_KEY, plugin ) )
        {
            daoUtil.setString( 1, strKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteByIdUser( int nUserId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE + SQL_WHERE + SQL_USER_ID, plugin ) )
        {
            daoUtil.setInt( 1, nUserId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    public DatabaseUserKey selectKeyByLogin( String login, Plugin plugin )
    {
        DatabaseUserKey userKey = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_LOGIN, plugin ) )
        {
            daoUtil.setString( 1, login );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                int nIndex = 0;
                userKey = new DatabaseUserKey( );
                userKey.setKey( daoUtil.getString( ++nIndex ) );
                userKey.setUserId( daoUtil.getInt( ++nIndex ) );
            }
        }
        return userKey;
    }
}
