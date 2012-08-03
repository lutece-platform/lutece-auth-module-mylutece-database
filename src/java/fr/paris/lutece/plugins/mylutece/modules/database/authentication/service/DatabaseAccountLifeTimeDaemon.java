package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter.DatabaseUserParameterService;
import fr.paris.lutece.plugins.mylutece.service.AbstractAccountLifeTimeDaemon;
import fr.paris.lutece.plugins.mylutece.service.IAccountLifeTimeService;
import fr.paris.lutece.plugins.mylutece.service.IUserParameterService;


/**
 * Account life time daemon of module mylutece directory
 */
public class DatabaseAccountLifeTimeDaemon extends AbstractAccountLifeTimeDaemon
{
    private DatabaseUserParameterService _parameterService = DatabaseUserParameterService.getService( );

    /**
     * Default constructor
     */
    public DatabaseAccountLifeTimeDaemon( )
    {
        super( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAccountLifeTimeService getAccountLifeTimeService( )
    {
        return DatabaseAccountLifeTimeService.getService( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserParameterService getParameterService( )
    {
        return _parameterService;
    }

}
