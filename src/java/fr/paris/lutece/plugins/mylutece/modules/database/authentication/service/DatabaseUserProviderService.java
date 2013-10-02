package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.ILuteceUserProviderService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;


/**
 * ILuteceUserProviderService implementation of module mylutece database
 */
public class DatabaseUserProviderService implements ILuteceUserProviderService
{

    /**
     * {@inheritDoc}
     */
    @Override
    public LuteceUser getLuteceUserFromName( String strName )
    {
        return DatabaseHome.findLuteceUserByLogin( strName, PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME ),
                SecurityService.getInstance( ).getAuthenticationService( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUsersBeCached( )
    {
        return true;
    }

}
