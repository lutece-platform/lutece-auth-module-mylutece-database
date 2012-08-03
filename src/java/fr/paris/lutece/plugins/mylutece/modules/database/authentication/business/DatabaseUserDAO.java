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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;
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
    private static final String SQL_QUERY_SELECT = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active, account_max_valid_date FROM mylutece_database_user WHERE mylutece_database_user_id = ?";
    private static final String SQL_QUERY_SELECT_PASSWORD = " SELECT password FROM mylutece_database_user WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO mylutece_database_user ( mylutece_database_user_id, login, name_family, name_given, email, is_active, password, password_max_valid_date, account_max_valid_date ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM mylutece_database_user WHERE mylutece_database_user_id = ?  ";
    private static final String SQL_QUERY_UPDATE = " UPDATE mylutece_database_user SET login = ?, name_family = ?, name_given = ?, email = ?, is_active = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_UPDATE_PASSWORD = " UPDATE mylutece_database_user SET password = ?, password_max_valid_date = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_UPDATE_RESET_PASSWORD = " UPDATE mylutece_database_user SET reset_password = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECTALL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user ORDER BY name_family, login";
    private static final String SQL_QUERY_SELECTALL_FOR_LOGIN = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE login = ? ";
    private static final String SQL_QUERY_SELECTALL_FOR_EMAIL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE email = ? ";
    private static final String SQL_QUERY_CHECK_PASSWORD_FOR_USER_ID = " SELECT count(*) FROM mylutece_database_user WHERE login = ? AND password = ? ";
    private static final String SQL_QUERY_SELECT_USER_FROM_SEARCH = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user " +
        " WHERE login LIKE ? AND name_family LIKE ? and name_given LIKE ? AND email LIKE ? ORDER BY name_family ";
    private static final String SQL_SELECT_USER_ID_FROM_PASSWORD = "SELECT mylutece_database_user_id FROM mylutece_database_user_password_history WHERE login = ?";
    private static final String SQL_SELECT_USER_PASSWORD_HISTORY = "SELECT password FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ? ORDER BY date_password_change desc";
    private static final String SQL_COUNT_USER_PASSWORD_HISTORY = "SELECT COUNT(*) FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ? AND date_password_change > ?";
    private static final String SQL_INSERT_PASSWORD_HISTORY = "INSERT INTO mylutece_database_user_password_history (mylutece_database_user_id, password) VALUES ( ?, ? ) ";
    private static final String SQL_DELETE_PASSWORD_HISTORY = "DELETE FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ?";
    private static final String SQL_QUERY_SELECT_EXPIRED_USER_ID = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE is_active = ?";
    
    private static final String SQL_QUERY_SELECT_EXPIRED_LIFE_TIME_USER_ID = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE account_max_valid_date < ? and is_active < ? ";

    private static final String SQL_QUERY_SELECT_USER_ID_FIRST_ALERT = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE nb_alerts_sent = 0 and is_active < ? and account_max_valid_date < ? ";
    private static final String SQL_QUERY_SELECT_USER_ID_OTHER_ALERT = "SELECT mylutece_database_user_id FROM mylutece_database_user "
            + "WHERE nb_alerts_sent > 0 and nb_alerts_sent <= ? and is_active < ? and (account_max_valid_date + nb_alerts_sent * ?) < ? ";

    private static final String SQL_QUERY_UPDATE_STATUS = " UPDATE mylutece_database_user SET is_active = ? WHERE mylutece_database_user_id IN ( ";
    private static final String SQL_QUERY_UPDATE_NB_ALERT = " UPDATE mylutece_database_user SET nb_alerts_sent = nb_alerts_sent + 1 WHERE mylutece_database_user_id IN ( ";

    private static final String SQL_QUERY_UPDATE_REACTIVATE_ACCOUNT = " UPDATE mylutece_database_user SET nb_alerts_sent = 0, account_max_valid_date = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECT_NB_ALERT_SENT = " SELECT nb_alerts_sent FROM mylutece_database_user WHERE mylutece_database_user_id = ? ";

    private static final String CONSTANT_CLOSE_PARENTHESIS = " ) ";
    private static final String CONSTANT_COMMA = ", ";

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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public void insert( DatabaseUser databaseUser, String strPassword, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        databaseUser.setUserId( newPrimaryKey( plugin ) );
        daoUtil.setInt( 1, databaseUser.getUserId(  ) );
        daoUtil.setString( 2, databaseUser.getLogin(  ) );
        daoUtil.setString( 3, databaseUser.getLastName(  ) );
        daoUtil.setString( 4, databaseUser.getFirstName(  ) );
        daoUtil.setString( 5, databaseUser.getEmail(  ) );
        daoUtil.setInt( 6, databaseUser.getStatus( ) );
        daoUtil.setString( 7, strPassword );
        daoUtil.setTimestamp( 8, databaseUser.getPasswordMaxValidDate( ) );
        if ( databaseUser.getAccountMaxValidDate( ) == null )
        {
            daoUtil.setLongNull( 9 );
        }
        else
        {
            daoUtil.setLong( 9, databaseUser.getAccountMaxValidDate( ).getTime( ) );
        }

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            databaseUser.setStatus( daoUtil.getInt( 6 ) );
            long accountTime = daoUtil.getLong( 7 );
            if ( accountTime > 0 )
            {
                databaseUser.setAccountMaxValidDate( new Timestamp( accountTime ) );
            }
        }

        daoUtil.free(  );

        return databaseUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( DatabaseUser databaseUser, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, databaseUser.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( DatabaseUser databaseUser, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        daoUtil.setString( 1, databaseUser.getLogin(  ) );
        daoUtil.setString( 2, databaseUser.getLastName(  ) );
        daoUtil.setString( 3, databaseUser.getFirstName(  ) );
        daoUtil.setString( 4, databaseUser.getEmail(  ) );
        daoUtil.setInt( 5, databaseUser.getStatus( ) );

        daoUtil.setInt( 6, databaseUser.getUserId(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePassword( DatabaseUser databaseUser, String strNewPassword, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_PASSWORD, plugin );
        daoUtil.setString( 1, strNewPassword );
        daoUtil.setTimestamp( 2, databaseUser.getPasswordMaxValidDate( ) );
        daoUtil.setInt( 3, databaseUser.getUserId( ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateResetPassword( DatabaseUser databaseUser, boolean bNewValue, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_RESET_PASSWORD, plugin );
        daoUtil.setBoolean( 1, bNewValue );
        daoUtil.setInt( 2, databaseUser.getUserId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
            databaseUser.setStatus( daoUtil.getInt( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            databaseUser.setStatus( daoUtil.getInt( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            databaseUser.setStatus( daoUtil.getInt( 6 ) );

            listDatabaseUsers.add( databaseUser );
        }

        daoUtil.free(  );

        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
            filteredUser.setStatus( daoUtil.getInt( 6 ) );
            listFilteredUsers.add( filteredUser );
        }

        daoUtil.free(  );

        return listFilteredUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findDatabaseUserIdFromLogin( String strLogin, Plugin plugin )
    {
        int nRecordId = 0;

        DAOUtil daoUtil = new DAOUtil( SQL_SELECT_USER_ID_FROM_PASSWORD, plugin );
        daoUtil.setString( 1, strLogin );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            nRecordId = daoUtil.getInt( 1 );
        }

        daoUtil.free( );

        return nRecordId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> selectUserPasswordHistory( int nUserID, Plugin plugin )
    {
        List<String> listPasswordHistory = new ArrayList<String>( );

        DAOUtil daoUtil = new DAOUtil( SQL_SELECT_USER_PASSWORD_HISTORY, plugin );
        daoUtil.setInt( 1, nUserID );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            listPasswordHistory.add( daoUtil.getString( 1 ) );
        }

        daoUtil.free( );
        return listPasswordHistory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countUserPasswordHistoryFromDate( Timestamp minDate, int nUserId, Plugin plugin )
    {
        int nNbRes = 0;

        DAOUtil daoUtil = new DAOUtil( SQL_COUNT_USER_PASSWORD_HISTORY, plugin );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setTimestamp( 2, minDate );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            nNbRes = daoUtil.getInt( 1 );
        }

        daoUtil.free( );
        return nNbRes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertNewPasswordInHistory( String strPassword, int nUserId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_INSERT_PASSWORD_HISTORY, plugin );
        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strPassword );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllPasswordHistoryForUser( int nUserId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_DELETE_PASSWORD_HISTORY, plugin );
        daoUtil.setInt( 1, nUserId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> findAllExpiredUserId( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRED_USER_ID, plugin );
        daoUtil.setInt( 1, DatabaseUser.STATUS_EXPIRED );
        List<Integer> listIdExpiredUser = new ArrayList<Integer>( );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            listIdExpiredUser.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return listIdExpiredUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersWithExpiredLifeTimeList( Timestamp currentTimestamp, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRED_LIFE_TIME_USER_ID, plugin );
        daoUtil.setLong( 1, currentTimestamp.getTime( ) );
        daoUtil.setInt( 2, DatabaseUser.STATUS_EXPIRED );
        List<Integer> listIdExpiredUser = new ArrayList<Integer>( );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            listIdExpiredUser.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return listIdExpiredUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendFirstAlert( Timestamp alertMaxDate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ID_FIRST_ALERT, plugin );
        daoUtil.setInt( 1, DatabaseUser.STATUS_EXPIRED );
        daoUtil.setLong( 2, alertMaxDate.getTime( ) );
        List<Integer> listIdUserFirstAlertlist = new ArrayList<Integer>( );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            listIdUserFirstAlertlist.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return listIdUserFirstAlertlist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendOtherAlert( Timestamp alertMaxDate, Timestamp timeBetweenAlerts,
            int maxNumberAlerts, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ID_OTHER_ALERT, plugin );
        daoUtil.setInt( 1, maxNumberAlerts );
        daoUtil.setInt( 2, DatabaseUser.STATUS_EXPIRED );
        daoUtil.setLong( 3, timeBetweenAlerts.getTime( ) );
        daoUtil.setLong( 4, alertMaxDate.getTime( ) );
        List<Integer> listIdUserFirstAlert = new ArrayList<Integer>( );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            listIdUserFirstAlert.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );
        return listIdUserFirstAlert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserStatus( List<Integer> listIdUser, int nNewStatus, Plugin plugin )
    {
        if ( listIdUser != null && listIdUser.size( ) > 0 )
        {
            StringBuilder sbSQL = new StringBuilder( );
            sbSQL.append( SQL_QUERY_UPDATE_STATUS );

            for ( int i = 0; i < listIdUser.size( ); i++ )
            {
                if ( i > 0 )
                {
                    sbSQL.append( CONSTANT_COMMA );
                }
                sbSQL.append( listIdUser.get( i ) );
            }
            sbSQL.append( CONSTANT_CLOSE_PARENTHESIS );

            DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin );
            daoUtil.setInt( 1, nNewStatus );
            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNbAlert( List<Integer> listIdUser, Plugin plugin )
    {
        if ( listIdUser != null && listIdUser.size( ) > 0 )
        {
            StringBuilder sbSQL = new StringBuilder( );
            sbSQL.append( SQL_QUERY_UPDATE_NB_ALERT );

            for ( int i = 0; i < listIdUser.size( ); i++ )
            {
                if ( i > 0 )
                {
                    sbSQL.append( CONSTANT_COMMA );
                }
                sbSQL.append( listIdUser.get( i ) );
            }
            sbSQL.append( CONSTANT_CLOSE_PARENTHESIS );

            DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin );
            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserExpirationDate( int nIdUser, Timestamp newExpirationDate, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_REACTIVATE_ACCOUNT, plugin );
        if ( newExpirationDate == null )
        {
            daoUtil.setLongNull( 1 );
        }
        else
        {
            daoUtil.setLong( 1, newExpirationDate.getTime( ) );
        }
        daoUtil.setInt( 2, nIdUser );

        daoUtil.executeUpdate( );

        daoUtil.free( );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbAccountLifeTimeNotification( int nIdUser, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NB_ALERT_SENT, plugin );
        daoUtil.setInt( 1, nIdUser );
        daoUtil.executeQuery( );
        int nRes = 0;
        if ( daoUtil.next( ) )
        {
            nRes = daoUtil.getInt( 1 );
        }
        daoUtil.free( );
        return nRes;
    }
}
