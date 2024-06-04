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

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This class provides a filter for users search function
 */
public class DatabaseUserFilter
{
    // Constants
    private static final String EQUAL = "=";
    private static final String AMPERSAND = "&";

    // Parameteres
    private static final String PARAMETER_SEARCH_LOGIN = "search_login";
    private static final String PARAMETER_SEARCH_LAST_NAME = "search_last_name";
    private static final String PARAMETER_SEARCH_FIRST_NAME = "search_first_name";
    private static final String PARAMETER_SEARCH_EMAIL = "search_email";
    private static final String PARAMETER_SEARCH_IS_SEARCH = "search_is_search";

    // Properties
    private static final String PROPERTY_ENCODING_URL = "lutece.encoding.url";
    private String _strLogin;
    private String _strLastName;
    private String _strFirstName;
    private String _strEmail;

    /**
     * Initialize each component of the object
     */
    public void init( )
    {
        _strLogin = "";
        _strLastName = "";
        _strFirstName = "";
        _strEmail = "";
    }

    /**
     * Get the access code
     * 
     * @return The access code
     */
    public String getLogin( )
    {
        return _strLogin;
    }

    /**
     * Set the access code
     * 
     * @param strAccessCode
     *            The Access Code
     */
    public void setAccessCode( String strAccessCode )
    {
        _strLogin = strAccessCode;
    }

    /**
     * Get the last name
     * 
     * @return The last name
     */
    public String getLastName( )
    {
        return _strLastName;
    }

    /**
     * Set the last name
     * 
     * @param strLastName
     *            The Last Name
     */
    public void setLastName( String strLastName )
    {
        _strLastName = strLastName;
    }

    /**
     * Get the first name
     * 
     * @return The first name
     */
    public String getFirstName( )
    {
        return _strFirstName;
    }

    /**
     * Set the first name
     * 
     * @param strFirstName
     *            The First Name
     */
    public void setFirstName( String strFirstName )
    {
        _strFirstName = strFirstName;
    }

    /**
     * Get the email
     * 
     * @return The email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Set the email
     * 
     * @param strEmail
     *            The email
     */
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail;
    }

    /**
     * Set the value of the AdminUserFilter
     * 
     * @param request
     *            HttpServletRequest
     * @return true if there is a search
     */
    public boolean setDatabaseUserFilter( HttpServletRequest request )
    {
        boolean bIsSearch = false;
        String strIsSearch = request.getParameter( PARAMETER_SEARCH_IS_SEARCH );

        if ( strIsSearch != null )
        {
            bIsSearch = true;
            _strLogin = request.getParameter( PARAMETER_SEARCH_LOGIN );
            _strLastName = request.getParameter( PARAMETER_SEARCH_LAST_NAME );
            _strFirstName = request.getParameter( PARAMETER_SEARCH_FIRST_NAME );
            _strEmail = request.getParameter( PARAMETER_SEARCH_EMAIL );
        }
        else
        {
            init( );
        }

        return bIsSearch;
    }

    /**
     * Build url attributes
     * 
     * @param url
     *            the url
     */
    public void setUrlAttributes( UrlItem url )
    {
        url.addParameter( PARAMETER_SEARCH_IS_SEARCH, Boolean.TRUE.toString( ) );

        try
        {
            url.addParameter( PARAMETER_SEARCH_LOGIN, URLEncoder.encode( _strLogin, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            url.addParameter( PARAMETER_SEARCH_LAST_NAME, URLEncoder.encode( _strLastName, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            url.addParameter( PARAMETER_SEARCH_FIRST_NAME, URLEncoder.encode( _strFirstName, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            url.addParameter( PARAMETER_SEARCH_EMAIL, URLEncoder.encode( _strEmail, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( e );
        }
    }

    /**
     * Build url attributes
     * 
     * @return the url attributes
     */
    public String getUrlAttributes( )
    {
        StringBuilder sbUrlAttributes = new StringBuilder( );
        sbUrlAttributes.append( PARAMETER_SEARCH_IS_SEARCH + EQUAL + Boolean.TRUE );

        try
        {
            sbUrlAttributes.append(
                    AMPERSAND + PARAMETER_SEARCH_LOGIN + EQUAL + URLEncoder.encode( _strLogin, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            sbUrlAttributes.append( AMPERSAND + PARAMETER_SEARCH_LAST_NAME + EQUAL
                    + URLEncoder.encode( _strLastName, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            sbUrlAttributes.append( AMPERSAND + PARAMETER_SEARCH_FIRST_NAME + EQUAL
                    + URLEncoder.encode( _strFirstName, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
            sbUrlAttributes.append(
                    AMPERSAND + PARAMETER_SEARCH_EMAIL + EQUAL + URLEncoder.encode( _strEmail, AppPropertiesService.getProperty( PROPERTY_ENCODING_URL ) ) );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( e );
        }

        return sbUrlAttributes.toString( );
    }
}
