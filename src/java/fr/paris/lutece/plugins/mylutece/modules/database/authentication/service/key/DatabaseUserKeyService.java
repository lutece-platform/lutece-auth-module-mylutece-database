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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.key;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKey;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.key.DatabaseUserKeyHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.util.DatabaseUtils;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.MyLuteceDatabaseApp;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.url.UrlItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * DatabaseUserKeyService
 *
 */
@ApplicationScoped
@Named( "mylutece-database.databaseUserKeyService" )
public class DatabaseUserKeyService
{

    // CONSTANTS
    private static final String SLASH = "/";

    // PARAMETERS
    private static final String PARAMETER_KEY = "key";

    // JSP
    private static final String JSP_VALIDATE_ACCOUNT = "jsp/site/plugins/mylutece/modules/database/DoValidateAccount.jsp";

    /**
     * Get the instance of the service
     * 
     * @return the instance of the service
     * @deprecated use CDI injection instead
     */
    @Deprecated
    public static DatabaseUserKeyService getService( )
    {
        return CDI.current( ).select( DatabaseUserKeyService.class ).get( );
    }

    // CRUD

    /**
     * Create a new user key from a given user id
     * 
     * @param nUserId
     *            the id user
     * @return the key
     */
    public DatabaseUserKey create( int nUserId )
    {
        DatabaseUserKey userKey = new DatabaseUserKey( );
        userKey.setUserId( nUserId );
        userKey.setKey( DatabaseUtils.generateNewKey( ) );
        DatabaseUserKeyHome.create( userKey );

        return userKey;
    }

    /**
     * Find the key
     * 
     * @param strKey
     *            the key
     * @return the key
     */
    public DatabaseUserKey findByPrimaryKey( String strKey )
    {
        return DatabaseUserKeyHome.findByPrimaryKey( strKey );
    }

    /**
     * Get a key of a user by his login
     * 
     * @param strLogin
     *            The login of the user
     * @return A key associated to the user
     */
    public DatabaseUserKey findKeyByLogin( String strLogin )
    {
        return DatabaseUserKeyHome.findKeyByLogin( strLogin );
    }

    /**
     * Remove a key
     * 
     * @param strKey
     *            the key
     */
    public void remove( String strKey )
    {
        DatabaseUserKeyHome.remove( strKey );
    }

    /**
     * Remove a key from a given id user
     * 
     * @param nUserId
     *            the id user
     */
    public void removeByIdUser( int nUserId )
    {
        DatabaseUserKeyHome.removeByIdUser( nUserId );
    }

    // GET

    /**
     * Build the validation url
     * 
     * @param strKey
     *            the key
     * @param request
     *            the HTTP request
     * @return the validation url
     */
    public String getValidationUrl( String strKey, HttpServletRequest request )
    {
        StringBuilder sbBaseUrl = new StringBuilder( AppPathService.getBaseUrl( request ) );

        if ( ( sbBaseUrl.length( ) > 0 ) && !sbBaseUrl.toString( ).endsWith( SLASH ) )
        {
            sbBaseUrl.append( SLASH );
        }

        sbBaseUrl.append( JSP_VALIDATE_ACCOUNT );

        UrlItem url = new UrlItem( sbBaseUrl.toString( ) );
        url.addParameter( PARAMETER_KEY, strKey );

        return url.getUrl( );
    }

    /**
     * Get reinit url
     * 
     * @param strKey
     *            the key
     * @param request
     *            the HTTP request
     * @return the url
     */
    public String getReinitUrl( String strKey, HttpServletRequest request )
    {
        StringBuilder sbBaseUrl = new StringBuilder( AppPathService.getBaseUrl( request ) );

        if ( ( sbBaseUrl.length( ) > 0 ) && !sbBaseUrl.toString( ).endsWith( SLASH ) )
        {
            sbBaseUrl.append( SLASH );
        }

        sbBaseUrl.append( MyLuteceDatabaseApp.getReinitPageUrl( ) );

        UrlItem url = new UrlItem( sbBaseUrl.toString( ) );
        url.addParameter( PARAMETER_KEY, strKey );

        return url.getUrl( );
    }
}
