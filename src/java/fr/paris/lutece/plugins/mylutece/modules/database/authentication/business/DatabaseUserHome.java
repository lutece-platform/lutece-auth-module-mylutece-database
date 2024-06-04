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
import fr.paris.lutece.portal.service.security.LuteceUserService;
import fr.paris.lutece.util.password.IPassword;
import fr.paris.lutece.util.password.IPasswordFactory;
import jakarta.enterprise.inject.spi.CDI;

import java.sql.Timestamp;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for DatabaseUser objects
 */
public final class DatabaseUserHome
{
    // Static variable pointed at the DAO instance
    private static IDatabaseUserDAO _dao = CDI.current( ).select( IDatabaseUserDAO.class ).get( );
    private static IPasswordFactory _passwordFactory = CDI.current( ).select( IPasswordFactory.class ).get( );

    /**
     * Private constructor - this class need not be instantiated
     */
    private DatabaseUserHome( )
    {
    }

    /**
     * Creation of an instance of databaseUser
     *
     * @param databaseUser
     *            The instance of the DatabaseUser which contains the informations to store
     * @param password
     *            The user's password
     * @param plugin
     *            The current plugin using this method
     * @return The instance of DatabaseUser which has been created with its primary key.
     */
    public static DatabaseUser create( DatabaseUser databaseUser, IPassword password, Plugin plugin )
    {
        _dao.insert( databaseUser, password, plugin );

        return databaseUser;
    }

    /**
     * Update of the databaseUser which is specified in parameter
     *
     * @param databaseUser
     *            The instance of the DatabaseUser which contains the data to store
     * @param plugin
     *            The current plugin using this method
     * @return The instance of the DatabaseUser which has been updated
     */
    public static DatabaseUser update( DatabaseUser databaseUser, Plugin plugin )
    {
        _dao.store( databaseUser, plugin );
        LuteceUserService.userAttributesChanged( databaseUser.getLogin( ) );

        return databaseUser;
    }

    /**
     * Update of the databaseUser which is specified in parameter
     *
     * @param databaseUser
     *            The instance of the DatabaseUser which contains the data to store
     * @param newPassword
     *            The new password to store
     * @param plugin
     *            The current plugin using this method
     * @return The instance of the DatabaseUser which has been updated
     */
    public static DatabaseUser updatePassword( DatabaseUser databaseUser, IPassword newPassword, Plugin plugin )
    {
        _dao.updatePassword( databaseUser, newPassword, plugin );

        return databaseUser;
    }

    /**
     * Update of the databaseUser which is specified in parameter
     *
     * @param user
     *            The instance of the DatabaseUser which contains the data to store
     * @param bNewValue
     *            The new value of the reset password attribute
     * @param plugin
     *            The current plugin using this method
     * @return The instance of the DatabaseUser which has been updated
     */
    public static DatabaseUser updateResetPassword( DatabaseUser user, boolean bNewValue, Plugin plugin )
    {
        _dao.updateResetPassword( user, bNewValue, plugin );

        return user;
    }

    /**
     * Remove the databaseUser whose identifier is specified in parameter
     *
     * @param databaseUser
     *            The DatabaseUser object to remove
     * @param plugin
     *            The current plugin using this method
     */
    public static void remove( DatabaseUser databaseUser, Plugin plugin )
    {
        _dao.delete( databaseUser, plugin );
        _dao.removeAllPasswordHistoryForUser( databaseUser.getUserId( ), plugin );
        LuteceUserService.userAttributesChanged( databaseUser.getLogin( ) );
    }

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a DatabaseUser whose identifier is specified in parameter
     *
     * @param nKey
     *            The Primary key of the databaseUser
     * @param plugin
     *            The current plugin using this method
     * @return An instance of DatabaseUser
     */
    public static DatabaseUser findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects
     * 
     * @param plugin
     *            The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersList( Plugin plugin )
    {
        return _dao.selectDatabaseUserList( plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects for a login
     *
     * @param strLogin
     *            The login of the databseUser
     * @param plugin
     *            The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersListForLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectDatabaseUserListForLogin( strLogin, plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects for a email
     *
     * @param strEmail
     *            The email of the databseUser
     * @param plugin
     *            The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersListForEmail( String strEmail, Plugin plugin )
    {
        return _dao.selectDatabaseUserListForEmail( strEmail, plugin );
    }

    /**
     * Check the password for a DatabaseUser
     *
     * @param strLogin
     *            The user login of DatabaseUser
     * @param strPassword
     *            The password of DatabaseUser
     * @param plugin
     *            The Plugin using this data access service
     * @return true if password is ok
     */
    public static boolean checkPassword( String strLogin, String strPassword, Plugin plugin )
    {
        IPassword storedPassword = _dao.loadPassword( strLogin, plugin );
        boolean check = storedPassword.check( strPassword );
        if ( check && storedPassword.isLegacy( ) )
        {
            // upgrade password storage
            int nUserId = findDatabaseUserIdFromLogin( strLogin, plugin );
            DatabaseUser databaseUser = findByPrimaryKey( nUserId, plugin );
            updatePassword( databaseUser, _passwordFactory.getPasswordFromCleartext( strPassword ), plugin );
        }
        return check;
    }

    /**
     * Find DatabaseUsers by filter
     * 
     * @param duFilter
     *            filter
     * @param plugin
     *            The plugin
     * @return a list of DatabaseUsers
     */
    public static List<DatabaseUser> findDatabaseUsersListByFilter( DatabaseUserFilter duFilter, Plugin plugin )
    {
        return _dao.selectDatabaseUsersListByFilter( duFilter, plugin );
    }

    /**
     * Get a user id from his login
     * 
     * @param strLogin
     *            The login of the user
     * @param plugin
     *            The plugin
     * @return The user id, or 0 if no user has this login.
     */
    public static int findDatabaseUserIdFromLogin( String strLogin, Plugin plugin )
    {
        return _dao.findDatabaseUserIdFromLogin( strLogin, plugin );
    }

    /**
     * Gets the history of password of the given user
     * 
     * @param nUserID
     *            Id of the user
     * @param plugin
     *            The plugin
     * @return The collection of recent passwords used by the user.
     */
    public static List<IPassword> selectUserPasswordHistory( int nUserID, Plugin plugin )
    {
        return _dao.selectUserPasswordHistory( nUserID, plugin );
    }

    /**
     * Get the number of password change done by a user since the given date.
     * 
     * @param minDate
     *            Minimum date to consider.
     * @param nUserId
     *            Id of the user
     * @param plugin
     *            The plugin
     * @return The number of password change done by the user since the given date.
     */
    public static int countUserPasswordHistoryFromDate( Timestamp minDate, int nUserId, Plugin plugin )
    {
        return _dao.countUserPasswordHistoryFromDate( minDate, nUserId, plugin );
    }

    /**
     * Log a password change in the password history
     * 
     * @param strPassword
     *            New password of the user
     * @param nUserId
     *            Id of the user
     * @param plugin
     *            The plugin
     */
    public static void insertNewPasswordInHistory( IPassword password, int nUserId, Plugin plugin )
    {
        _dao.insertNewPasswordInHistory( password, nUserId, plugin );
    }

    /**
     * Remove every password saved in the password history for a user.
     * 
     * @param nUserId
     *            Id of the user
     * @param plugin
     *            The plugin
     */
    public static void removeAllPasswordHistoryForUser( int nUserId, Plugin plugin )
    {
        _dao.removeAllPasswordHistoryForUser( nUserId, plugin );
    }

    /**
     * Get the list of id of user with the expired status.
     * 
     * @param plugin
     *            The plugin
     * @return The list of id of user with the expired status.
     */
    public static List<Integer> findAllExpiredUserId( Plugin plugin )
    {
        return _dao.findAllExpiredUserId( plugin );
    }

    /**
     * Get the list of id of users that have an expired time life but not the expired status
     * 
     * @param currentTimestamp
     *            Timestamp describing the current time.
     * @param plugin
     *            The plugin
     * @return the list of id of users with expired time life
     */
    public static List<Integer> getIdUsersWithExpiredLifeTimeList( Timestamp currentTimestamp, Plugin plugin )
    {
        return _dao.getIdUsersWithExpiredLifeTimeList( currentTimestamp, plugin );
    }

    /**
     * Get the list of id of users that need to receive their first alert
     * 
     * @param firstAlertMaxDate
     *            The maximum expiration date to send first alert.
     * @param plugin
     *            The plugin
     * @return the list of id of users that need to receive their first alert
     */
    public static List<Integer> getIdUsersToSendFirstAlert( Timestamp firstAlertMaxDate, Plugin plugin )
    {
        return _dao.getIdUsersToSendFirstAlert( firstAlertMaxDate, plugin );
    }

    /**
     * Get the list of id of users that need to receive their first alert
     * 
     * @param alertMaxDate
     *            The maximum date to send alerts.
     * @param timeBetweenAlerts
     *            Timestamp describing the time between two alerts.
     * @param maxNumberAlerts
     *            Maximum number of alerts to send to a user
     * @param plugin
     *            The plugin
     * @return the list of id of users that need to receive their first alert
     */
    public static List<Integer> getIdUsersToSendOtherAlert( Timestamp alertMaxDate, Timestamp timeBetweenAlerts, int maxNumberAlerts, Plugin plugin )
    {
        return _dao.getIdUsersToSendOtherAlert( alertMaxDate, timeBetweenAlerts, maxNumberAlerts, plugin );
    }

    /**
     * Get the list of id of users that have an expired password but not the change password flag
     * 
     * @param currentTimestamp
     *            Timestamp describing the current time.
     * @param plugin
     *            The plugin
     * @return the list of id of users with expired passwords
     */
    public static List<Integer> getIdUsersWithExpiredPasswordsList( Timestamp currentTimestamp, Plugin plugin )
    {
        return _dao.getIdUsersWithExpiredPasswordsList( currentTimestamp, plugin );
    }

    /**
     * Update status of a list of user accounts
     * 
     * @param listIdUser
     *            List of user accounts to update
     * @param nNewStatus
     *            New status of the user
     * @param plugin
     *            The plugin
     */
    public static void updateUserStatus( List<Integer> listIdUser, int nNewStatus, Plugin plugin )
    {
        _dao.updateUserStatus( listIdUser, nNewStatus, plugin );
    }

    /**
     * Increment the number of alert send to users by 1
     * 
     * @param listIdUser
     *            The list of users to update
     * @param plugin
     *            The plugin
     */
    public static void updateNbAlert( List<Integer> listIdUser, Plugin plugin )
    {
        _dao.updateNbAlert( listIdUser, plugin );
    }

    /**
     * Set the "change password" flag of users to true
     * 
     * @param listIdUser
     *            The list of users to update
     * @param plugin
     *            The plugin
     */
    public static void updateChangePassword( List<Integer> listIdUser, Plugin plugin )
    {
        _dao.updateChangePassword( listIdUser, plugin );
    }

    /**
     * Update the user expiration date with the new values. Also update his alert account to 0
     * 
     * @param nIdUser
     *            Id of the user to update
     * @param newExpirationDate
     *            Id of the user to update
     * @param plugin
     *            The plugin
     */
    public static void updateUserExpirationDate( int nIdUser, Timestamp newExpirationDate, Plugin plugin )
    {
        _dao.updateUserExpirationDate( nIdUser, newExpirationDate, plugin );
    }

    /**
     * Get the number of notification send to a user to warn him about the expiration of his account
     * 
     * @param nIdUser
     *            Id of the user
     * @param plugin
     *            The plugin
     * @return The number of notification send to the user
     */
    public static int getNbAccountLifeTimeNotification( int nIdUser, Plugin plugin )
    {
        return _dao.getNbAccountLifeTimeNotification( nIdUser, plugin );
    }

    /**
     * Update a user last login date
     * 
     * @param strLogin
     *            Login of the user to update
     * @param dateLastLogin
     *            date of the last login of the user
     * @param plugin
     *            The plugin
     */
    public static void updateUserLastLoginDate( String strLogin, Date dateLastLogin, Plugin plugin )
    {
        _dao.updateUserLastLoginDate( strLogin, new java.sql.Timestamp( dateLastLogin.getTime( ) ), plugin );
    }
}
