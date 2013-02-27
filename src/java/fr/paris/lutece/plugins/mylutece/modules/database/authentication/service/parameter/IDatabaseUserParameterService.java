package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service.parameter;

import fr.paris.lutece.plugins.mylutece.service.IUserParameterService;
import fr.paris.lutece.portal.service.plugin.Plugin;


/**
 * User parameter service
 * 
 */
public interface IDatabaseUserParameterService extends IUserParameterService
{
    /**
     * Check if the account creation must be validated by email
     * @param plugin the plugin
     * @return true if it must be validated by email, false otherwise
     */
    boolean isAccountCreationValidationEmail( Plugin plugin );

    /**
     * Check if the jcaptcha is enable or not
     * @param plugin the plugin
     * @return true if it is enable, false otherwise
     */
    boolean isJcaptchaEnable( Plugin plugin );
}
