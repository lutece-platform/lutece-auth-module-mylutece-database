/*
 * Copyright (c) 2002-2025, City of Paris
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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserFilter;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.search.IUserSearchProvider;
import fr.paris.lutece.plugins.mylutece.service.search.MyLuteceSearchUser;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.ReferenceList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * IUserSearchProvider implementation for database authentication
 */
@ApplicationScoped
@Named( "mylutece.myLuteceUserSearchProvider" )
public class DatabaseSearchProvider implements IUserSearchProvider
{
    private static final String ATTRIBUTE_LOGIN = "login";
    private static final String ATTRIBUTE_LAST_NAME = "lastName";
    private static final String ATTRIBUTE_FIRST_NAME = "firstName";
    private static final String ATTRIBUTE_EMAIL = "email";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MyLuteceSearchUser> findUsers( String strParameterLastName, String strParameterGivenName, String strParameterCriteriaMail,
            ReferenceList listProviderAttribute )
    {
        DatabaseUserFilter filter = new DatabaseUserFilter( );
        filter.init( );

        if ( strParameterLastName != null && !strParameterLastName.trim( ).isEmpty( ) )
        {
            filter.setLastName( strParameterLastName.trim( ) );
        }

        if ( strParameterGivenName != null && !strParameterGivenName.trim( ).isEmpty( ) )
        {
            filter.setFirstName( strParameterGivenName.trim( ) );
        }

        if ( strParameterCriteriaMail != null && !strParameterCriteriaMail.trim( ).isEmpty( ) )
        {
            filter.setEmail( strParameterCriteriaMail.trim( ) );
        }

        List<DatabaseUser> databaseUsers = DatabaseUserHome.findDatabaseUsersListByFilter( filter,
                PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME ) );

        List<MyLuteceSearchUser> myLuteceSearchUsers = new ArrayList<>( );
        for ( DatabaseUser databaseUser : databaseUsers )
        {
            MyLuteceSearchUser myLuteceSearchUser = convertToMyLuteceSearchUser( databaseUser );
            myLuteceSearchUsers.add( myLuteceSearchUser );
        }

        return myLuteceSearchUsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllAttributes( )
    {
        return Arrays.asList( ATTRIBUTE_LOGIN, ATTRIBUTE_LAST_NAME, ATTRIBUTE_FIRST_NAME, ATTRIBUTE_EMAIL );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MyLuteceSearchUser getUserById( String strUserId )
    {
        if ( strUserId == null || strUserId.trim( ).isEmpty( ) )
        {
            return null;
        }

        Collection<DatabaseUser> users = DatabaseUserHome.findDatabaseUsersListForLogin( strUserId, PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME ) );
        if ( users != null && !users.isEmpty( ) )
        {
            DatabaseUser databaseUser = users.iterator( ).next( );
            return convertToMyLuteceSearchUser( databaseUser );
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MyLuteceSearchUser> getUsersByIds( List<String> userIds )
    {
        if ( userIds == null || userIds.isEmpty( ) )
        {
            return new ArrayList<>( );
        }

        List<MyLuteceSearchUser> myLuteceSearchUsers = new ArrayList<>( );
        for ( String strLogin : userIds )
        {
            if ( strLogin != null && !strLogin.trim( ).isEmpty( ) )
            {
                Collection<DatabaseUser> users = DatabaseUserHome.findDatabaseUsersListForLogin( strLogin, PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME ) );
                if ( users != null && !users.isEmpty( ) )
                {
                    DatabaseUser databaseUser = users.iterator( ).next( );
                    myLuteceSearchUsers.add( convertToMyLuteceSearchUser( databaseUser ) );
                }
            }
        }

        return myLuteceSearchUsers;
    }

    /**
     * Convert a DatabaseUser to a MyLuteceSearchUser
     *
     * @param databaseUser
     *            The DatabaseUser to convert
     * @return The converted MyLuteceSearchUser
     */
    private MyLuteceSearchUser convertToMyLuteceSearchUser( DatabaseUser databaseUser )
    {
        MyLuteceSearchUser myLuteceSearchUser = new MyLuteceSearchUser( );
        myLuteceSearchUser.setId( databaseUser.getUserId( ) );
        myLuteceSearchUser.setLogin( databaseUser.getLogin( ) );
        myLuteceSearchUser.setGivenName( databaseUser.getFirstName( ) );
        myLuteceSearchUser.setLastName( databaseUser.getLastName( ) );
        myLuteceSearchUser.setEmail( databaseUser.getEmail( ) );
        myLuteceSearchUser.setProviderUserId( databaseUser.getLogin( ) );

        return myLuteceSearchUser;
    }
}
