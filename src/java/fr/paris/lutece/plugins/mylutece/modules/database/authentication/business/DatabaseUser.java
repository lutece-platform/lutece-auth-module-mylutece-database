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

import java.io.Serializable;

import java.sql.Timestamp;

/**
 * This class represents the business object DatabaseUser
 */
public class DatabaseUser implements Serializable
{
    // Variables declarations
    /**
     * Status of not activated users.
     */
    public static final int STATUS_NOT_ACTIVATED = 0;

    /**
     * Status of activated users.
     */
    public static final int STATUS_ACTIVATED = 1;

    /**
     * Status of expired users. Expired users will be anonymized.
     */
    public static final int STATUS_EXPIRED = 5;

    /**
     * Status of anonymized users.
     */
    public static final int STATUS_ANONYMIZED = 10;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8073247646404231859L;
    private int _nUserId;
    private String _strLogin;
    private String _strLastName;
    private String _strFirstName;
    private String _strEmail;
    private int _nStatus;
    private Timestamp _passwordMaxValidDate;
    private Timestamp _accountMaxValidDate;
    public static final Timestamp DEFAULT_DATE_LAST_LOGIN = Timestamp.valueOf( "1980-01-01 00:00:00" );

    /**
     * Returns the UserId
     *
     * @return The UserId
     */
    public int getUserId( )
    {
        return _nUserId;
    }

    /**
     * Sets the UserId
     *
     * @param nUserId
     *            The UserId
     */
    public void setUserId( int nUserId )
    {
        _nUserId = nUserId;
    }

    /**
     * Returns the Email
     *
     * @return The Email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Sets the Email
     *
     * @param strEmail
     *            The email
     */
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail;
    }

    /**
     * Returns user first name
     *
     * @return The first name
     */
    public String getFirstName( )
    {
        return _strFirstName;
    }

    /**
     * Sets the first name
     *
     * @param strFirstName
     *            The first name
     */
    public void setFirstName( String strFirstName )
    {
        _strFirstName = strFirstName;
    }

    /**
     * Returns the last name
     *
     * @return The last name
     */
    public String getLastName( )
    {
        return _strLastName;
    }

    /**
     * Sets the last name
     *
     * @param strLastName
     *            The last name
     */
    public void setLastName( String strLastName )
    {
        _strLastName = strLastName;
    }

    /**
     * Returns the login
     *
     * @return The login
     */
    public String getLogin( )
    {
        return _strLogin;
    }

    /**
     * Sets the login
     *
     * @param strLogin
     *            The login
     */
    public void setLogin( String strLogin )
    {
        _strLogin = strLogin;
    }

    /**
     * Get the status of the user
     * 
     * @return The status of the user
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * Set the status of the user
     * 
     * @param nStatus
     *            The status of the user
     */
    public void setStatus( int nStatus )
    {
        _nStatus = nStatus;
    }

    /**
     * Check if the user is active
     * 
     * @return true if it is active, false otherwise
     */
    public boolean isActive( )
    {
        return ( ( _nStatus >= STATUS_ACTIVATED ) && ( _nStatus < STATUS_EXPIRED ) );
    }

    /**
     * Set the password maximum valide date of a user
     * 
     * @param passwordMaxValidDate
     *            The new value of the password maximum valide date of a user
     */
    public void setPasswordMaxValidDate( Timestamp passwordMaxValidDate )
    {
        this._passwordMaxValidDate = passwordMaxValidDate;
    }

    /**
     * Get the password maximum valide date of a user
     * 
     * @return The password maximum valide date of a user
     */
    public Timestamp getPasswordMaxValidDate( )
    {
        return this._passwordMaxValidDate;
    }

    /**
     * Get the maximum valid date of the account of the user
     * 
     * @return The maximum valid date of the account of the user
     */
    public Timestamp getAccountMaxValidDate( )
    {
        return _accountMaxValidDate;
    }

    /**
     * Set the maximum valid date of the account of the user
     * 
     * @param accountMaxValidDate
     *            The maximum valid date
     */
    public void setAccountMaxValidDate( Timestamp accountMaxValidDate )
    {
        this._accountMaxValidDate = accountMaxValidDate;
    }
}
