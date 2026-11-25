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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.DatabaseService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginDefaultImplementation;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.enterprise.inject.spi.CDI;

/**
 * The captcha options of the module (account creation, login after failures) must only be offered when a captcha really
 * works: the captcha service bean exists (plugin-captcha deployed), the plugin "captcha" is enabled and at least one
 * captcha engine is installed. Otherwise the widget is empty and every answer is accepted.
 */
public class CaptchaGateTest extends LuteceTestCase
{
    private static final String PLUGIN_CAPTCHA = "captcha";

    @SuppressWarnings( "unchecked" )
    private static Map<String, Plugin> plugins( ) throws ReflectiveOperationException
    {
        Field field = PluginService.class.getDeclaredField( "_mapPlugins" );
        field.setAccessible( true );
        return (Map<String, Plugin>) field.get( null );
    }

    private static void registerCaptchaPlugin( boolean bInstalled ) throws ReflectiveOperationException
    {
        Plugin plugin = new PluginDefaultImplementation( );
        plugin.setName( PLUGIN_CAPTCHA );
        plugin.setIsInstalled( bInstalled );
        plugins( ).put( PLUGIN_CAPTCHA, plugin );
    }

    private static boolean captchaOptionsAvailable( )
    {
        return CDI.current( ).select( DatabaseService.class ).get( ).isPluginJcaptchaEnable( );
    }

    @AfterEach
    protected void cleanCaptcha( ) throws ReflectiveOperationException
    {
        plugins( ).remove( PLUGIN_CAPTCHA );
        TestCaptchaService.setEngines( );
    }

    @Test
    public void testCaptchaAvailableWhenPluginEnabledWithAnEngine( ) throws ReflectiveOperationException
    {
        registerCaptchaPlugin( true );
        TestCaptchaService.setEngines( "testEngine" );
        assertTrue( captchaOptionsAvailable( ), "plugin captcha enabled with an engine: the captcha options must be available" );
    }

    @Test
    public void testCaptchaUnavailableWhenPluginDisabled( ) throws ReflectiveOperationException
    {
        registerCaptchaPlugin( false );
        TestCaptchaService.setEngines( "testEngine" );
        assertFalse( captchaOptionsAvailable( ), "plugin captcha disabled: no captcha option" );
    }

    @Test
    public void testCaptchaUnavailableWhenPluginNotRegistered( )
    {
        TestCaptchaService.setEngines( "testEngine" );
        assertFalse( captchaOptionsAvailable( ), "plugin captcha not installed: no captcha option" );
    }

    @Test
    public void testCaptchaUnavailableWithoutEngine( ) throws ReflectiveOperationException
    {
        registerCaptchaPlugin( true );
        assertFalse( captchaOptionsAvailable( ), "no captcha engine: the widget is empty and every answer accepted, no captcha option" );
    }
}
