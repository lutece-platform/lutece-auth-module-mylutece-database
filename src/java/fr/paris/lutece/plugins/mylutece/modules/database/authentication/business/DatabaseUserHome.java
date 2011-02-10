/*
 * Copyright (c) 2002-2009, Mairie de Paris
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

import java.util.Collection;
import java.util.List;


/**
 * This class provides instances management methods (create, find, ...) for DatabaseUser objects
 */
public final class DatabaseUserHome
{
    // Static variable pointed at the DAO instance
    private static IDatabaseUserDAO _dao = (IDatabaseUserDAO) SpringContextService.getPluginBean( "mylutece-database",
            "mylutece-database.databaseUserDAO" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private DatabaseUserHome(  )
    {
    }

    /**
     * Creation of an instance of databaseUser
     *
     * @param databaseUser The instance of the DatabaseUser which contains the informations to store
     * @param strPassword The user's password
     * @param plugin The current plugin using this method
     * @return The  instance of DatabaseUser which has been created with its primary key.
     */
    public static DatabaseUser create( DatabaseUser databaseUser, String strPassword, Plugin plugin )
    {
        _dao.insert( databaseUser, strPassword, plugin );

        return databaseUser;
    }

    /**
     * Update of the databaseUser which is specified in parameter
     *
     * @param databaseUser The instance of the DatabaseUser which contains the data to store
     * @param plugin The current plugin using this method
     * @return The instance of the  DatabaseUser which has been updated
     */
    public static DatabaseUser update( DatabaseUser databaseUser, Plugin plugin )
    {
        _dao.store( databaseUser, plugin );

        return databaseUser;
    }

    /**
     * Update of the databaseUser which is specified in parameter
     *
     * @param databaseUser The instance of the DatabaseUser which contains the data to store
     * @param strNewPassword The new password to store
     * @param plugin The current plugin using this method
     * @return The instance of the  DatabaseUser which has been updated
     */
    public static DatabaseUser updatePassword( DatabaseUser databaseUser, String strNewPassword, Plugin plugin )
    {
        _dao.updatePassword( databaseUser, strNewPassword, plugin );

        return databaseUser;
    }

    /**
     * Remove the databaseUser whose identifier is specified in parameter
     *
     * @param databaseUser The DatabaseUser object to remove
     * @param plugin The current plugin using this method
     */
    public static void remove( DatabaseUser databaseUser, Plugin plugin )
    {
        _dao.delete( databaseUser, plugin );
    }

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Returns an instance of a DatabaseUser whose identifier is specified in parameter
     *
     * @param nKey The Primary key of the databaseUser
     * @param plugin The current plugin using this method
     * @return An instance of DatabaseUser
     */
    public static DatabaseUser findByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.load( nKey, plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects
     * @param plugin The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersList( Plugin plugin )
    {
        return _dao.selectDatabaseUserList( plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects for a login
     *
     * @param strLogin The login of the databseUser
     * @param plugin The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersListForLogin( String strLogin, Plugin plugin )
    {
        return _dao.selectDatabaseUserListForLogin( strLogin, plugin );
    }

    /**
     * Returns a collection of DatabaseUser objects for a email
     *
     * @param strEmail The email of the databseUser
     * @param plugin The current plugin using this method
     * @return A collection of DatabaseUser
     */
    public static Collection<DatabaseUser> findDatabaseUsersListForEmail( String strEmail, Plugin plugin )
    {
        return _dao.selectDatabaseUserListForEmail( strEmail, plugin );
    }

    /**
     * Returns the password of the specified user
     *
     * @param nKey The Primary key of the databaseUser
     * @param plugin The current plugin using this method
     * @return An instance of DatabaseUser
     */
    public static String findPasswordByPrimaryKey( int nKey, Plugin plugin )
    {
        return _dao.selectPasswordByPrimaryKey( nKey, plugin );
    }

    /**
     * Check the password for a DatabaseUser
     *
     * @param strLogin The user login of DatabaseUser
     * @param strPassword The password of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return true if password is ok
     */
    public static boolean checkPassword( String strLogin, String strPassword, Plugin plugin )
    {
        return _dao.checkPassword( strLogin, strPassword, plugin );
    }
    
    /**
     * Find DatabaseUsers by filter
     * @param duFilter filter
     * @param plugin Plugin
     * @return a list of DatabaseUsers
     */
    public static List<DatabaseUser> findDatabaseUsersListByFilter( DatabaseUserFilter duFilter, Plugin plugin )
    {
    	return _dao.selectDatabaseUsersListByFilter( duFilter, plugin );
    }
}
