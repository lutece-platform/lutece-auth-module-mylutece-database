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

import fr.paris.lutece.portal.service.rbac.RBACResource;

/**
 * This class represents business objects group
 */
public class Group implements RBACResource
{
    public static final String RESOURCE_TYPE = "GROUP_TYPE";
    private String _strGroupKey;
    private String _strGroupDescription;

    /**
     * Gets the group key
     * 
     * @return the group key
     */
    public String getGroupKey( )
    {
        return _strGroupKey;
    }

    /**
     * Sets the group key
     * 
     * @param strGroupKey
     *            the group key
     */
    public void setGroupKey( String strGroupKey )
    {
        _strGroupKey = ( strGroupKey != null ) ? strGroupKey : "";
    }

    /**
     * Gets the group description
     * 
     * @return the group description
     */
    public String getGroupDescription( )
    {
        return _strGroupDescription;
    }

    /**
     * Sets the group description
     * 
     * @param strGroupDescription
     *            the group description
     */
    public void setGroupDescription( String strGroupDescription )
    {
        _strGroupDescription = strGroupDescription;
    }

    /**
     * RBAC resource implmentation
     * 
     * @return The resource type code
     */
    public String getResourceTypeCode( )
    {
        return RESOURCE_TYPE;
    }

    /**
     * RBAC resource implmentation
     * 
     * @return The resourceId
     */
    public String getResourceId( )
    {
        return getGroupKey( );
    }
}
