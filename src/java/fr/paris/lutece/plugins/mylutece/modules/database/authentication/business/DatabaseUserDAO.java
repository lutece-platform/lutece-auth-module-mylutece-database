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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This class provides Data Access methods for databaseUser objects
 */
public final class DatabaseUserDAO implements IDatabaseUserDAO
{
    // Constants
    private static final String PERCENT = "%";
    private static final String SQL_QUERY_NEW_PK = " SELECT max( mylutece_database_user_id ) FROM mylutece_database_user ";
    private static final String SQL_QUERY_SELECT = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE mylutece_database_user_id = ?";
    private static final String SQL_QUERY_SELECT_PASSWORD = " SELECT password FROM mylutece_database_user WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO mylutece_database_user ( mylutece_database_user_id, login, name_family, name_given, email, is_active, password ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM mylutece_database_user WHERE mylutece_database_user_id = ?  ";
    private static final String SQL_QUERY_UPDATE = " UPDATE mylutece_database_user SET login = ?, name_family = ?, name_given = ?, email = ?, is_active = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_UPDATE_PASSWORD = " UPDATE mylutece_database_user SET password = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECTALL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user ORDER BY name_family, login";
    private static final String SQL_QUERY_SELECTALL_FOR_LOGIN = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE login = ? ";
    private static final String SQL_QUERY_SELECTALL_FOR_EMAIL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE email = ? ";
    private static final String SQL_QUERY_CHECK_PASSWORD_FOR_USER_ID = " SELECT count(*) FROM mylutece_database_user WHERE login = ? AND password = ? ";
    private static final String SQL_QUERY_SELECT_USER_FROM_SEARCH = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user " +
        " WHERE login LIKE ? AND name_family LIKE ? and name_given LIKE ? AND email LIKE ? ORDER BY name_family ";

    /** This class implements the Singleton design pattern. */
    private static DatabaseUserDAO _dao = new DatabaseUserDAO(  );

    /**
     * Creates a new databaseUserDAO object.
     */
    private DatabaseUserDAO(  )
    {
    }

    /**
     * Returns the unique instance of the singleton.
     *
     * @return the instance
     */
    static DatabaseUserDAO getInstance(  )
    {
        return _dao;
    }

    /**
     * Generates a new primary key
     * @param plugin The Plugin using this data access service
     * @return The new primary key
     */
    public int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery(  );

        int nKey;

        if ( !daoUtil.next(  ) )
        {
            // if the table is empty
            nKey = 1;
        }

        nKey = daoUtil.getInt( 1 ) + 1;

        daoUtil.free(  );

        return nKey;
    }

    /**
     * Insert a new record in the table.
     *
     * @param databaseUser The databaseUser object
     * @param strPassword The user password
     * @param plugin The Plugin using this data access service
     */
    public void insert( DatabaseUser databaseUser, String strPassword, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        databaseUser.setUserId( newPrimaryKey( plugin ) );
        daoUtil.setInt( 1, databaseUser.getUserId(  ) );
        daoUtil.setString( 2, databaseUser.getLogin(  ) );
        daoUtil.setString( 3, databaseUser.getLastName(  ) );
        daoUtil.setString( 4, databaseUser.getFirstName(  ) );
        daoUtil.setString( 5, databaseUser.getEmail(  ) );
        daoUtil.setBoolean( 6, databaseUser.isActive(  ) );
        daoUtil.setString( 7, strPassword );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Load the data of DatabaseUser from the table
     *
     * @param nUserId The identifier of User
     * @param plugin The Plugin using this data access service
     * @return the instance of the DatabaseUser
     */
    public DatabaseUser load( int nUserId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeQuery(  );

        DatabaseUser databaseUser = null;

        if ( daoUtil.next(  ) )
        {
            databaseUser = DatabaseUserFactory.getFactory(  ).newDatabaseUser(  );
            databaseUser.setUserId( daoUtil.getInt( 1 ) );
            databaseUser.setLogin( daoUtil.getString( 2 ) );
            databaseUser.setLastName( daoUtil.getString( 3 ) );
            databaseUser.setFirstName( daoUtil.getString( 4 ) );
            databaseUser.setEmail( daoUtil.getString( 5 ) );
            databaseUser.setActive( daoUtil.getBoolean( 6 ) );
        }

        daoUtil.free(  );

        return databaseUser;
    }

    /**
     * Delete a record from the table
     * @param databaseUser The DatabaseUser object
     * @param plugin The Plugin using this data access service
     */
    public void delete( DatabaseUser databaseUser, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, databaseUser.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Update the record in the table
     * @param databaseUser The reference of databaseUser
     * @param plugin The Plugin using this data access service
     */
    public void store( DatabaseUser databaseUser, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setString( 1, databaseUser.getLogin(  ) );
        daoUtil.setString( 2, databaseUser.getLastName(  ) );
        daoUtil.setString( 3, databaseUser.getFirstName(  ) );
        daoUtil.setString( 4, databaseUser.getEmail(  ) );
        daoUtil.setBoolean( 5, databaseUser.isActive(  ) );

        daoUtil.setInt( 6, databaseUser.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Update the record in the table
     * @param databaseUser The reference of databaseUser
     * @param strNewPassword The new password to store
     * @param plugin The Plugin using this data access service
     */
    public void updatePassword( DatabaseUser databaseUser, String strNewPassword, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_PASSWORD, plugin );
        daoUtil.setString( 1, strNewPassword );
        daoUtil.setInt( 2, databaseUser.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * Load the password of the specified user
     *
     * @param nDatabaseUserId The Primary key of the databaseUser
     * @param plugin The current plugin using this method
     * @return String the user password
     */
    public String selectPasswordByPrimaryKey( int nDatabaseUserId, Plugin plugin )
    {
        String strPassword = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_PASSWORD, plugin );
        daoUtil.setInt( 1, nDatabaseUserId );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            strPassword = daoUtil.getString( 1 );
        }

        daoUtil.free(  );

        return strPassword;
    }

    /**
     * Load the list of databaseUsers
     * @param plugin The Plugin using this data access service
     * @return The Collection of the databaseUsers
     */
    public Collection<DatabaseUser> selectDatabaseUserList( Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<DatabaseUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            DatabaseUser databaseUser = DatabaseUserFactory.getFactory(  ).newDatabaseUser(  );
            databaseUser.setUserId( daoUtil.getInt( 1 ) );
            databaseUser.setLogin( daoUtil.getString( 2 ) );
            databaseUser.setLastName( daoUtil.getString( 3 ) );
            databaseUser.setFirstName( daoUtil.getString( 4 ) );
            databaseUser.setEmail( daoUtil.getString( 5 ) );
            databaseUser.setActive( daoUtil.getBoolean( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * Load the list of DatabaseUsers for a login
     * @param strLogin The login of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the DatabaseUsers
     */
    public Collection<DatabaseUser> selectDatabaseUserListForLogin( String strLogin, Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<DatabaseUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FOR_LOGIN, plugin );
        daoUtil.setString( 1, strLogin );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            DatabaseUser databaseUser = DatabaseUserFactory.getFactory(  ).newDatabaseUser(  );
            databaseUser.setUserId( daoUtil.getInt( 1 ) );
            databaseUser.setLogin( daoUtil.getString( 2 ) );
            databaseUser.setLastName( daoUtil.getString( 3 ) );
            databaseUser.setFirstName( daoUtil.getString( 4 ) );
            databaseUser.setEmail( daoUtil.getString( 5 ) );
            databaseUser.setActive( daoUtil.getBoolean( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * Load the list of DatabaseUsers for a email
     * @param strEmail The email of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the DatabaseUsers
     */
    public Collection<DatabaseUser> selectDatabaseUserListForEmail( String strEmail, Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<DatabaseUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FOR_EMAIL, plugin );
        daoUtil.setString( 1, strEmail );
        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            DatabaseUser databaseUser = DatabaseUserFactory.getFactory(  ).newDatabaseUser(  );
            databaseUser.setUserId( daoUtil.getInt( 1 ) );
            databaseUser.setLogin( daoUtil.getString( 2 ) );
            databaseUser.setLastName( daoUtil.getString( 3 ) );
            databaseUser.setFirstName( daoUtil.getString( 4 ) );
            databaseUser.setEmail( daoUtil.getString( 5 ) );
            databaseUser.setActive( daoUtil.getBoolean( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * Check the password for a DatabaseUser
     *
     * @param strLogin The user login of DatabaseUser
     * @param strPassword The password of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return true if password is ok
     */
    public boolean checkPassword( String strLogin, String strPassword, Plugin plugin )
    {
        int nCount = 0;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_PASSWORD_FOR_USER_ID, plugin );
        daoUtil.setString( 1, strLogin );
        daoUtil.setString( 2, strPassword );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            nCount = daoUtil.getInt( 1 );
        }

        daoUtil.free(  );

        return ( nCount == 1 ) ? true : false;
    }

    /**
     * Load the list of DatabaseUsers by a filter
     * @param duFilter filter
     * @param plugin Plugin
     * @return a list of DatabaseUser
     */
    public List<DatabaseUser> selectDatabaseUsersListByFilter( DatabaseUserFilter duFilter, Plugin plugin )
    {
        List<DatabaseUser> listFilteredUsers = new ArrayList<DatabaseUser>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_FROM_SEARCH, plugin );
        daoUtil.setString( 1, PERCENT + duFilter.getLogin(  ) + PERCENT );
        daoUtil.setString( 2, PERCENT + duFilter.getLastName(  ) + PERCENT );
        daoUtil.setString( 3, PERCENT + duFilter.getFirstName(  ) + PERCENT );
        daoUtil.setString( 4, PERCENT + duFilter.getEmail(  ) + PERCENT );

        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            DatabaseUser filteredUser = DatabaseUserFactory.getFactory(  ).newDatabaseUser(  );
            filteredUser.setUserId( daoUtil.getInt( 1 ) );
            filteredUser.setLogin( daoUtil.getString( 2 ) );
            filteredUser.setLastName( daoUtil.getString( 3 ) );
            filteredUser.setFirstName( daoUtil.getString( 4 ) );
            filteredUser.setEmail( daoUtil.getString( 5 ) );
            filteredUser.setActive( daoUtil.getBoolean( 6 ) );
            listFilteredUsers.add( filteredUser );
        }

        daoUtil.free(  );

        return listFilteredUsers;
    }
}
