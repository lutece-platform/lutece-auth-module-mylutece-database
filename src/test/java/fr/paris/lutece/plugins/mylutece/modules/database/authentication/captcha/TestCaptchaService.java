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
package fr.paris.lutece.plugins.mylutece.modules.database.authentication.captcha;

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.portal.service.captcha.ICaptchaService;
import fr.paris.lutece.portal.service.util.BeanUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Test double of the captcha service of plugin-captcha (same bean name): the list of captcha engines is set by the test.
 * Without any engine, plugin-captcha validates every answer and renders an empty widget.
 */
@ApplicationScoped
@Named( BeanUtils.BEAN_CAPTCHA_SERVICE )
public class TestCaptchaService implements ICaptchaService
{
    private static final List<String> ENGINES = new ArrayList<>( );

    static void setEngines( String... engines )
    {
        ENGINES.clear( );
        ENGINES.addAll( List.of( engines ) );
    }

    @Override
    public boolean validate( HttpServletRequest request )
    {
        return ENGINES.isEmpty( );
    }

    @Override
    public String getHtmlCode( )
    {
        return ENGINES.isEmpty( ) ? "" : "<div class=\"test-captcha\"></div>";
    }

    @Override
    public List<String> getCaptchaEngineNameList( )
    {
        return new ArrayList<>( ENGINES );
    }

    @Override
    public String getDefaultCaptchaEngineName( )
    {
        return ENGINES.isEmpty( ) ? "" : ENGINES.get( 0 );
    }

    @Override
    public void setDefaultCaptchaEngineName( String strDefaultCaptchaEngine )
    {
        // not used
    }
}
