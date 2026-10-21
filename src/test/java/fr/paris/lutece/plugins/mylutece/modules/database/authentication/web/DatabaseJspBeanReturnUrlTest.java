/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.web;

import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

/**
 * Tests the screen the button of an error message of the users and groups back office opens when there is no page to go back to.
 */
public class DatabaseJspBeanReturnUrlTest extends LuteceTestCase
{
    private static final String JSP_PATH = "jsp/admin/plugins/mylutece/modules/database/";

    @Inject
    private DatabaseJspBean _databaseJspBean;
    @Inject
    private GroupJspBean _groupJspBean;

    /**
     * A user created without name returns to the form creating a user.
     */
    @Test
    public void testDoCreateUserWithoutNameReturnsToTheCreationForm( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "login", "login" );
        request.addParameter( "email", "login@example.com" );
        request.addParameter( "first_password", "password" );
        request.addParameter( "second_password", "password" );

        String strUrl = _databaseJspBean.doCreateUser( request );

        assertTrue( strUrl.endsWith( AdminMessageService.getMessageRelativeUrl( ) ), strUrl );
        String strBackUrl = AdminMessageService.getMessage( request ).getBackUrl( );
        assertTrue( strBackUrl.endsWith( JSP_PATH + "CreateUser.jsp?plugin_name=mylutece-database" ), strBackUrl );
    }

    /**
     * A user modified without name returns to the form modifying this user.
     */
    @Test
    public void testDoModifyUserWithoutNameReturnsToTheModificationForm( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "mylutece_database_user_id", "12" );
        request.addParameter( "login", "login" );
        request.addParameter( "email", "login@example.com" );

        String strUrl = _databaseJspBean.doModifyUser( request );

        assertTrue( strUrl.endsWith( AdminMessageService.getMessageRelativeUrl( ) ), strUrl );
        String strBackUrl = AdminMessageService.getMessage( request ).getBackUrl( );
        assertTrue( strBackUrl.endsWith( JSP_PATH + "ModifyUser.jsp?plugin_name=mylutece-database&mylutece_database_user_id=12" ), strBackUrl );
    }

    /**
     * A group created without key returns to the form creating a group.
     */
    @Test
    public void testDoCreateGroupWithoutKeyReturnsToTheCreationForm( )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "group_key", "" );

        String strUrl = _groupJspBean.doCreateGroup( request );

        assertTrue( strUrl.endsWith( AdminMessageService.getMessageRelativeUrl( ) ), strUrl );
        String strBackUrl = AdminMessageService.getMessage( request ).getBackUrl( );
        assertTrue( strBackUrl.endsWith( JSP_PATH + "CreateGroup.jsp" ), strBackUrl );
    }
}
