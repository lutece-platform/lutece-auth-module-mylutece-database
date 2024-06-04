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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.parameter;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 *
 * DefaultUserParameterDAO
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseUserParameterDAO" )
public class DatabaseUserParameterDAO implements IDatabaseUserParameterDAO
{
    private static final String SQL_QUERY_SELECT_USER_PARAMETERS_VALUE = " SELECT parameter_value FROM mylutece_database_user_parameter WHERE parameter_key = ? ";
    private static final String SQL_QUERY_UPDATE_USER_PARAMETERS = " UPDATE mylutece_database_user_parameter SET parameter_value = ? WHERE parameter_key = ? ";
    private static final String SQL_QUERY_SELECT_ALL = " SELECT parameter_key, parameter_value FROM mylutece_database_user_parameter ";

    /**
     * {@inheritDoc}
     */
    public ReferenceItem load( String strParameterKey, Plugin plugin )
    {
        ReferenceItem userParam = null;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_USER_PARAMETERS_VALUE, plugin ) )
        {
            daoUtil.setString( 1, strParameterKey );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                userParam = new ReferenceItem( );
                userParam.setCode( strParameterKey );
                userParam.setName( daoUtil.getString( 1 ) );
                userParam.setChecked( Boolean.valueOf( userParam.getName( ) ) );
            }
        }
        return userParam;
    }

    /**
     * {@inheritDoc}
     */
    public void store( ReferenceItem userParam, Plugin plugin )
    {
        int nIndex = 0;
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_USER_PARAMETERS, plugin ) )
        {
            daoUtil.setString( ++nIndex, userParam.getName( ) );
            daoUtil.setString( ++nIndex, userParam.getCode( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    public ReferenceList selectAll( Plugin plugin )
    {
        ReferenceList listUserParams = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                int nIndex = 0;
                ReferenceItem userParam = new ReferenceItem( );
                userParam.setCode( daoUtil.getString( ++nIndex ) );
                userParam.setName( daoUtil.getString( ++nIndex ) );
                userParam.setChecked( Boolean.valueOf( userParam.getName( ) ) );
                listUserParams.add( userParam );
            }
        }
        return listUserParams;
    }
}
