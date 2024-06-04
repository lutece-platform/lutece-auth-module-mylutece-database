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
import jakarta.enterprise.inject.spi.CDI;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * DatabaseUserParameterHome
 *
 */
public final class DatabaseUserParameterHome
{
    // Static variable pointed at the DAO instance
    private static IDatabaseUserParameterDAO _dao = CDI.current( ).select( IDatabaseUserParameterDAO.class ).get( );

    /**
     * Private constructor
     */
    private DatabaseUserParameterHome( )
    {
    }

    /**
     * Load the parameter value
     * 
     * @param strParameterKey
     *            the parameter key
     * @param plugin
     *            the plugin
     * @return The parameter value
     */
    public static ReferenceItem findByKey( String strParameterKey, Plugin plugin )
    {
        return _dao.load( strParameterKey, plugin );
    }

    /**
     * Update the parameter value
     * 
     * @param userParam
     *            The parameter
     * @param plugin
     *            the plugin
     */
    public static void update( ReferenceItem userParam, Plugin plugin )
    {
        _dao.store( userParam, plugin );
    }

    /**
     * Find all parameters
     * 
     * @param plugin
     *            the plugin
     * @return a ReferenceList
     */
    public static ReferenceList findAll( Plugin plugin )
    {
        return _dao.selectAll( plugin );
    }

    /**
     * Get the integer value of a security parameter
     * 
     * @param strParameterkey
     *            The key of the parameter
     * @param plugin
     *            the plugin
     * @return The integer value of a security parameter
     */
    public static int getIntegerSecurityParameter( String strParameterkey, Plugin plugin )
    {
        ReferenceItem refItem = findByKey( strParameterkey, plugin );

        if ( ( refItem == null ) || StringUtils.isEmpty( refItem.getName( ) ) )
        {
            return 0;
        }

        try
        {
            return Integer.parseInt( refItem.getName( ) );
        }
        catch( NumberFormatException e )
        {
            return 0;
        }
    }
}
