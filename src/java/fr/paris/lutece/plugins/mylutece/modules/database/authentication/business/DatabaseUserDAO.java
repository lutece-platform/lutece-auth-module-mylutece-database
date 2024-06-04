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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.password.IPassword;
import fr.paris.lutece.util.password.IPasswordFactory;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * This class provides Data Access methods for databaseUser objects
 */
@ApplicationScoped
@Named( "mylutece-database.databaseUserDAO" )
public class DatabaseUserDAO implements IDatabaseUserDAO
{
    // Constants
    private static final String PERCENT = "%";
    private static final String SQL_QUERY_NEW_PK = " SELECT max( mylutece_database_user_id ) FROM mylutece_database_user ";
    private static final String SQL_QUERY_SELECT = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active, account_max_valid_date FROM mylutece_database_user WHERE mylutece_database_user_id = ?";
    private static final String SQL_QUERY_INSERT = " INSERT INTO mylutece_database_user ( mylutece_database_user_id, login, name_family, name_given, email, is_active, password, password_max_valid_date, account_max_valid_date ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM mylutece_database_user WHERE mylutece_database_user_id = ?  ";
    private static final String SQL_QUERY_UPDATE = " UPDATE mylutece_database_user SET login = ?, name_family = ?, name_given = ?, email = ?, is_active = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_UPDATE_PASSWORD = " UPDATE mylutece_database_user SET password = ?, password_max_valid_date = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_UPDATE_RESET_PASSWORD = " UPDATE mylutece_database_user SET reset_password = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECTALL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active, account_max_valid_date, password_max_valid_date FROM mylutece_database_user ORDER BY name_family, login";
    private static final String SQL_QUERY_SELECTALL_FOR_LOGIN = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE login = ? ";
    private static final String SQL_QUERY_SELECTALL_FOR_EMAIL = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user WHERE email = ? ";
    private static final String SQL_QUERY_CHECK_PASSWORD_FOR_USER_ID = " SELECT password FROM mylutece_database_user WHERE login = ?";
    private static final String SQL_QUERY_SELECT_USER_FROM_SEARCH = " SELECT mylutece_database_user_id, login, name_family, name_given, email, is_active FROM mylutece_database_user "
            + " WHERE login LIKE ? AND name_family LIKE ? and name_given LIKE ? AND email LIKE ? ORDER BY name_family ";
    private static final String SQL_SELECT_USER_ID_FROM_PASSWORD = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE login = ?";
    private static final String SQL_SELECT_USER_PASSWORD_HISTORY = "SELECT password FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ? ORDER BY date_password_change desc";
    private static final String SQL_COUNT_USER_PASSWORD_HISTORY = "SELECT COUNT(*) FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ? AND date_password_change > ?";
    private static final String SQL_INSERT_PASSWORD_HISTORY = "INSERT INTO mylutece_database_user_password_history (mylutece_database_user_id, password) VALUES ( ?, ? ) ";
    private static final String SQL_DELETE_PASSWORD_HISTORY = "DELETE FROM mylutece_database_user_password_history WHERE mylutece_database_user_id = ?";
    private static final String SQL_QUERY_SELECT_EXPIRED_USER_ID = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE is_active = ?";
    private static final String SQL_QUERY_SELECT_EXPIRED_LIFE_TIME_USER_ID = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE account_max_valid_date < ? and is_active < ? ";
    private static final String SQL_QUERY_SELECT_USER_ID_FIRST_ALERT = "SELECT mylutece_database_user_id FROM mylutece_database_user WHERE nb_alerts_sent = 0 and is_active < ? and account_max_valid_date < ? ";
    private static final String SQL_QUERY_SELECT_USER_ID_OTHER_ALERT = "SELECT mylutece_database_user_id FROM mylutece_database_user "
            + "WHERE nb_alerts_sent > 0 and nb_alerts_sent <= ? and is_active < ? and (account_max_valid_date + nb_alerts_sent * ?) < ? ";
    private static final String SQL_QUERY_SELECT_USER_ID_PASSWORD_EXPIRED = " SELECT mylutece_database_user_id FROM mylutece_database_user WHERE password_max_valid_date < ? AND reset_password = 0 ";
    private static final String SQL_QUERY_UPDATE_STATUS = " UPDATE mylutece_database_user SET is_active = ? WHERE mylutece_database_user_id IN ( ";
    private static final String SQL_QUERY_UPDATE_NB_ALERT = " UPDATE mylutece_database_user SET nb_alerts_sent = nb_alerts_sent + 1 WHERE mylutece_database_user_id IN ( ";
    private static final String SQL_QUERY_UPDATE_RESET_PASSWORD_LIST_ID = " UPDATE mylutece_database_user SET reset_password = 1 WHERE mylutece_database_user_id IN ( ";
    private static final String SQL_QUERY_UPDATE_LAST_LOGGIN_DATE = " UPDATE mylutece_database_user SET last_login = ? WHERE login LIKE ? ";
    private static final String SQL_QUERY_UPDATE_REACTIVATE_ACCOUNT = " UPDATE mylutece_database_user SET nb_alerts_sent = 0, account_max_valid_date = ? WHERE mylutece_database_user_id = ? ";
    private static final String SQL_QUERY_SELECT_NB_ALERT_SENT = " SELECT nb_alerts_sent FROM mylutece_database_user WHERE mylutece_database_user_id = ? ";
    private static final String CONSTANT_CLOSE_PARENTHESIS = " ) ";
    private static final String CONSTANT_COMMA = ", ";

    @Inject
    private IPasswordFactory _passwordFactory;

    @Inject
    private IDatabaseUserFactory _databaseUserFactory;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int newPrimaryKey( Plugin plugin )
    {
        int nKey = 1;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin ) )
        {
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nKey = daoUtil.getInt( 1 ) + 1;
            }

        }

        return nKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert( DatabaseUser databaseUser, IPassword password, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            databaseUser.setUserId( newPrimaryKey( plugin ) );
            daoUtil.setInt( 1, databaseUser.getUserId( ) );
            daoUtil.setString( 2, databaseUser.getLogin( ) );
            daoUtil.setString( 3, databaseUser.getLastName( ) );
            daoUtil.setString( 4, databaseUser.getFirstName( ) );
            daoUtil.setString( 5, databaseUser.getEmail( ) );
            daoUtil.setInt( 6, databaseUser.getStatus( ) );
            daoUtil.setString( 7, password.getStorableRepresentation( ) );
            daoUtil.setTimestamp( 8, databaseUser.getPasswordMaxValidDate( ) );

            if ( databaseUser.getAccountMaxValidDate( ) == null )
            {
                daoUtil.setLongNull( 9 );
            }
            else
            {
                daoUtil.setLong( 9, databaseUser.getAccountMaxValidDate( ).getTime( ) );
            }

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseUser load( int nUserId, Plugin plugin )
    {
        System.err.println( ">>>>>> " + _databaseUserFactory );
        DatabaseUser databaseUser = null;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nUserId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                databaseUser = _databaseUserFactory.newDatabaseUser( );
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
        }
        return databaseUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( DatabaseUser databaseUser, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, databaseUser.getUserId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( DatabaseUser databaseUser, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            daoUtil.setString( 1, databaseUser.getLogin( ) );
            daoUtil.setString( 2, databaseUser.getLastName( ) );
            daoUtil.setString( 3, databaseUser.getFirstName( ) );
            daoUtil.setString( 4, databaseUser.getEmail( ) );
            daoUtil.setInt( 5, databaseUser.getStatus( ) );

            daoUtil.setInt( 6, databaseUser.getUserId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePassword( DatabaseUser databaseUser, IPassword newPassword, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_PASSWORD, plugin ) )
        {
            daoUtil.setString( 1, newPassword.getStorableRepresentation( ) );
            daoUtil.setTimestamp( 2, databaseUser.getPasswordMaxValidDate( ) );
            daoUtil.setInt( 3, databaseUser.getUserId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateResetPassword( DatabaseUser databaseUser, boolean bNewValue, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_RESET_PASSWORD, plugin ) )
        {
            daoUtil.setBoolean( 1, bNewValue );
            daoUtil.setInt( 2, databaseUser.getUserId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DatabaseUser> selectDatabaseUserList( Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                DatabaseUser databaseUser = _databaseUserFactory.newDatabaseUser( );
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

                databaseUser.setPasswordMaxValidDate( daoUtil.getTimestamp( 8 ) );
                listDatabaseUsers.add( databaseUser );
            }
        }
        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DatabaseUser> selectDatabaseUserListForLogin( String strLogin, Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FOR_LOGIN, plugin ) )
        {
            daoUtil.setString( 1, strLogin );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                DatabaseUser databaseUser = _databaseUserFactory.newDatabaseUser( );
                databaseUser.setUserId( daoUtil.getInt( 1 ) );
                databaseUser.setLogin( daoUtil.getString( 2 ) );
                databaseUser.setLastName( daoUtil.getString( 3 ) );
                databaseUser.setFirstName( daoUtil.getString( 4 ) );
                databaseUser.setEmail( daoUtil.getString( 5 ) );
                databaseUser.setStatus( daoUtil.getInt( 6 ) );

                listDatabaseUsers.add( databaseUser );
            }
        }
        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DatabaseUser> selectDatabaseUserListForEmail( String strEmail, Plugin plugin )
    {
        Collection<DatabaseUser> listDatabaseUsers = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FOR_EMAIL, plugin ) )
        {
            daoUtil.setString( 1, strEmail );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                DatabaseUser databaseUser = _databaseUserFactory.newDatabaseUser( );
                databaseUser.setUserId( daoUtil.getInt( 1 ) );
                databaseUser.setLogin( daoUtil.getString( 2 ) );
                databaseUser.setLastName( daoUtil.getString( 3 ) );
                databaseUser.setFirstName( daoUtil.getString( 4 ) );
                databaseUser.setEmail( daoUtil.getString( 5 ) );
                databaseUser.setStatus( daoUtil.getInt( 6 ) );

                listDatabaseUsers.add( databaseUser );
            }
        }
        return listDatabaseUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPassword loadPassword( String strLogin, Plugin plugin )
    {
        IPassword password = _passwordFactory.getDummyPassword( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_PASSWORD_FOR_USER_ID, plugin ) )
        {
            daoUtil.setString( 1, strLogin );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                password = _passwordFactory.getPassword( daoUtil.getString( 1 ) );
            }
        }
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DatabaseUser> selectDatabaseUsersListByFilter( DatabaseUserFilter duFilter, Plugin plugin )
    {
        List<DatabaseUser> listFilteredUsers = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_FROM_SEARCH, plugin ) )
        {
            daoUtil.setString( 1, PERCENT + duFilter.getLogin( ) + PERCENT );
            daoUtil.setString( 2, PERCENT + duFilter.getLastName( ) + PERCENT );
            daoUtil.setString( 3, PERCENT + duFilter.getFirstName( ) + PERCENT );
            daoUtil.setString( 4, PERCENT + duFilter.getEmail( ) + PERCENT );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                DatabaseUser filteredUser = _databaseUserFactory.newDatabaseUser( );
                filteredUser.setUserId( daoUtil.getInt( 1 ) );
                filteredUser.setLogin( daoUtil.getString( 2 ) );
                filteredUser.setLastName( daoUtil.getString( 3 ) );
                filteredUser.setFirstName( daoUtil.getString( 4 ) );
                filteredUser.setEmail( daoUtil.getString( 5 ) );
                filteredUser.setStatus( daoUtil.getInt( 6 ) );
                listFilteredUsers.add( filteredUser );
            }
        }
        return listFilteredUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findDatabaseUserIdFromLogin( String strLogin, Plugin plugin )
    {
        int nRecordId = 0;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_SELECT_USER_ID_FROM_PASSWORD, plugin ) )
        {
            daoUtil.setString( 1, strLogin );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nRecordId = daoUtil.getInt( 1 );
            }
        }
        return nRecordId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IPassword> selectUserPasswordHistory( int nUserID, Plugin plugin )
    {
        List<IPassword> listPasswordHistory = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_SELECT_USER_PASSWORD_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nUserID );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listPasswordHistory.add( _passwordFactory.getPassword( daoUtil.getString( 1 ) ) );
            }
        }
        return listPasswordHistory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countUserPasswordHistoryFromDate( Timestamp minDate, int nUserId, Plugin plugin )
    {
        int nNbRes = 0;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_COUNT_USER_PASSWORD_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nUserId );
            daoUtil.setTimestamp( 2, minDate );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nNbRes = daoUtil.getInt( 1 );
            }
        }
        return nNbRes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertNewPasswordInHistory( IPassword password, int nUserId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_INSERT_PASSWORD_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nUserId );
            daoUtil.setString( 2, password.getStorableRepresentation( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllPasswordHistoryForUser( int nUserId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_DELETE_PASSWORD_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nUserId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> findAllExpiredUserId( Plugin plugin )
    {
        List<Integer> listIdExpiredUser = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRED_USER_ID, plugin ) )
        {
            daoUtil.setInt( 1, DatabaseUser.STATUS_EXPIRED );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIdExpiredUser.add( daoUtil.getInt( 1 ) );
            }
        }
        return listIdExpiredUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersWithExpiredLifeTimeList( Timestamp currentTimestamp, Plugin plugin )
    {
        List<Integer> listIdExpiredUser = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRED_LIFE_TIME_USER_ID, plugin ) )
        {
            daoUtil.setLong( 1, currentTimestamp.getTime( ) );
            daoUtil.setInt( 2, DatabaseUser.STATUS_EXPIRED );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIdExpiredUser.add( daoUtil.getInt( 1 ) );
            }
        }
        return listIdExpiredUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendFirstAlert( Timestamp alertMaxDate, Plugin plugin )
    {
        List<Integer> listIdUserFirstAlertlist = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ID_FIRST_ALERT, plugin ) )
        {
            daoUtil.setInt( 1, DatabaseUser.STATUS_EXPIRED );
            daoUtil.setLong( 2, alertMaxDate.getTime( ) );

            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listIdUserFirstAlertlist.add( daoUtil.getInt( 1 ) );
            }
        }
        return listIdUserFirstAlertlist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersToSendOtherAlert( Timestamp alertMaxDate, Timestamp timeBetweenAlerts, int maxNumberAlerts, Plugin plugin )
    {
        List<Integer> listIdUserFirstAlert = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ID_OTHER_ALERT, plugin ) )
        {
            daoUtil.setInt( 1, maxNumberAlerts );
            daoUtil.setInt( 2, DatabaseUser.STATUS_EXPIRED );
            daoUtil.setLong( 3, timeBetweenAlerts.getTime( ) );
            daoUtil.setLong( 4, alertMaxDate.getTime( ) );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIdUserFirstAlert.add( daoUtil.getInt( 1 ) );
            }
        }
        return listIdUserFirstAlert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getIdUsersWithExpiredPasswordsList( Timestamp currentTimestamp, Plugin plugin )
    {
        List<Integer> idUserPasswordExpiredlist = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_ID_PASSWORD_EXPIRED, plugin ) )
        {
            daoUtil.setTimestamp( 1, currentTimestamp );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                idUserPasswordExpiredlist.add( daoUtil.getInt( 1 ) );
            }
        }
        return idUserPasswordExpiredlist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserStatus( List<Integer> listIdUser, int nNewStatus, Plugin plugin )
    {
        if ( CollectionUtils.isNotEmpty( listIdUser ) )
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

            try ( DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin ) )
            {
                daoUtil.setInt( 1, nNewStatus );
                daoUtil.executeUpdate( );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNbAlert( List<Integer> listIdUser, Plugin plugin )
    {
        if ( CollectionUtils.isNotEmpty( listIdUser ) )
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

            try ( DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin ) )
            {
                daoUtil.executeUpdate( );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChangePassword( List<Integer> listIdUser, Plugin plugin )
    {
        if ( CollectionUtils.isNotEmpty( listIdUser ) )
        {
            StringBuilder sbSQL = new StringBuilder( );
            sbSQL.append( SQL_QUERY_UPDATE_RESET_PASSWORD_LIST_ID );

            for ( int i = 0; i < listIdUser.size( ); i++ )
            {
                if ( i > 0 )
                {
                    sbSQL.append( CONSTANT_COMMA );
                }

                sbSQL.append( listIdUser.get( i ) );
            }

            sbSQL.append( CONSTANT_CLOSE_PARENTHESIS );

            try ( DAOUtil daoUtil = new DAOUtil( sbSQL.toString( ), plugin ) )
            {
                daoUtil.executeUpdate( );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserExpirationDate( int nIdUser, Timestamp newExpirationDate, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_REACTIVATE_ACCOUNT, plugin ) )
        {
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
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNbAccountLifeTimeNotification( int nIdUser, Plugin plugin )
    {
        int nRes = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NB_ALERT_SENT, plugin ) )
        {
            daoUtil.setInt( 1, nIdUser );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                nRes = daoUtil.getInt( 1 );
            }
        }
        return nRes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUserLastLoginDate( String strLogin, Timestamp dateLastLogin, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_LAST_LOGGIN_DATE, plugin ) )
        {
            daoUtil.setTimestamp( 1, dateLastLogin );
            daoUtil.setString( 2, strLogin );
            daoUtil.executeUpdate( );
        }
    }
}
