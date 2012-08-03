package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.plugins.mylutece.service.AbstractAnonymizationDaemon;
import fr.paris.lutece.plugins.mylutece.service.IAnonymizationService;


/**
 * User anonymization daemon
 * 
 */
public class DatabaseAnonymizationDaemon extends AbstractAnonymizationDaemon
{

    /**
     * Default constructor of the daemon
     */
    public DatabaseAnonymizationDaemon( )
    {
        super( );
        setPluginName( DatabasePlugin.PLUGIN_NAME );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAnonymizationService getAnonymizationService( )
    {
        return DatabaseAnonymizationService.getService( );
    }

}
