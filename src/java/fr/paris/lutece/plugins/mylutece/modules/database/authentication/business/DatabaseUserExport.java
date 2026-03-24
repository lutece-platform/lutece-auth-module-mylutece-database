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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.business;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the business object DatabaseUserExport
 */
public class DatabaseUserExport implements Serializable
{

    private String _strAccessCode;
    private String _strLastName;
    private String _strFirstName;
    private String _strEmail;
    private String _strStatus;

    private String _strPasswordMaxValidDate;
    private String _strAccountMaxValidDate;

    private List<String> _roles;
    private List<String> _groups;
    private List<DatabaseUserAttributeExport> _attributes;

    /**
     * Returns the user access code.
     *
     * @return the user access code
     */
    public String getAccessCode() {
        return _strAccessCode;
    }

    /**
     * Sets the user access code.
     *
     * @param accessCode the user access code to set
     */
    public void setAccessCode(String accessCode) {
        this._strAccessCode = accessCode;
    }

    /**
     * Returns the user last name.
     *
     * @return the user last name
     */
    public String getLastName() {
        return _strLastName;
    }

    /**
     * Sets the user last name.
     *
     * @param lastName the user last name to set
     */
    public void setLastName(String lastName) {
        this._strLastName = lastName;
    }

    /**
     * Returns the user first name.
     *
     * @return the user first name
     */
    public String getFirstName() {
        return _strFirstName;
    }

    /**
     * Sets the user first name.
     *
     * @param firstName the user first name to set
     */
    public void setFirstName(String firstName) {
        this._strFirstName = firstName;
    }

    /**
     * Returns the user email address.
     *
     * @return the user email address
     */
    public String getEmail() {
        return _strEmail;
    }

    /**
     * Sets the user email address.
     *
     * @param email the user email address to set
     */
    public void setEmail(String email) {
        this._strEmail = email;
    }

    /**
     * Returns the user status.
     *
     * @return the user status
     */
    public String getStatus() {
        return _strStatus;
    }

    /**
     * Sets the user status.
     *
     * @param status the user status to set
     */
    public void setStatus(String status) {
        this._strStatus = status;
    }

    /**
     * Returns the maximum password validity date.
     *
     * @return the maximum password validity date
     */
    public String getPasswordMaxValidDate() {
        return _strPasswordMaxValidDate;
    }

    /**
     * Sets the maximum password validity date.
     *
     * @param passwordMaxValidDate the maximum password validity date to set
     */
    public void setPasswordMaxValidDate(String passwordMaxValidDate) {
        this._strPasswordMaxValidDate = passwordMaxValidDate;
    }

    /**
     * Returns the maximum account validity date.
     *
     * @return the maximum account validity date
     */
    public String getAccountMaxValidDate() {
        return _strAccountMaxValidDate;
    }

    /**
     * Sets the maximum account validity date.
     *
     * @param accountMaxValidDate the maximum account validity date to set
     */
    public void setAccountMaxValidDate(String accountMaxValidDate) {
        this._strAccountMaxValidDate = accountMaxValidDate;
    }

    /**
     * Returns the list of roles assigned to the user.
     *
     * @return the list of roles
     */
    public List<String> getRoles() {
        return _roles;
    }

    /**
     * Sets the list of roles assigned to the user.
     *
     * @param roles the list of roles to set
     */
    public void setRoles(List<String> roles) {
        this._roles = roles;
    }

    /**
     * Returns the list of groups the user belongs to.
     *
     * @return the list of groups
     */
    public List<String> getGroups() {
        return _groups;
    }

    /**
     * Sets the list of groups the user belongs to.
     *
     * @param listGroups the list of groups to set
     */
    public void setGroups(List<String> listGroups) {
        this._groups = listGroups;
    }

    /**
     * Returns the list of user attributes.
     *
     * @return the list of attributes
     */
    public List<DatabaseUserAttributeExport> getAttributes() {
        return _attributes;
    }

    /**
     * Sets the list of user attributes.
     *
     * @param attributes the list of attributes to set
     */
    public void setAttributes(List<DatabaseUserAttributeExport> attributes) {
        this._attributes = attributes;
    }

}
