package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.IAccountLifeTimeService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.DatabaseTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.CryptoService;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Service to handle account life time
 * 
 */
public class DatabaseAccountLifeTimeService implements IAccountLifeTimeService
{

	public static final String BEAN_DATABASE_ACCOUNT_LIFE_TIME_SERVICE = "mylutece-database.databaseAccountLifeTimeService";

	private static final String PARAMETER_MYLUTECE_DIRECTORY_EXPIRATION_MAIL = "mylutece_database_expiration_mail";
	private static final String PARAMETER_MYLUTECE_DIRECTORY_FIRST_ALERT_MAIL = "mylutece_database_first_alert_mail";
	private static final String PARAMETER_MYLUTECE_DIRECTORY_OTHER_ALERT_MAIL = "mylutece_database_other_alert_mail";

	private static final String MARK_LAST_NAME = "last_name";
	private static final String MARK_FIRST_NAME = "first_name";
	private static final String MARK_DATE_VALID = "date_valid";
	private static final String MARK_URL = "url";
	private static final String MARK_USER_ID = "user_id";
	private static final String MARK_REF = "ref";

	private static final String PROPERTY_ACCOUNT_REF_ENCRYPT_ALGO = "mylutece-database.account_life_time.refEncryptionAlgorythm";
	private static final String PROPERTY_PROD_URL = "init.webapp.prod.url";

	private static final String JSP_URL_REACTIVATE_ACCOUNT = "/jsp/site/Portal.jsp?page=mylutecedatabase&action=reactivateAccount";
	private static final String CONSTANT_AND = "&";
	private static final String CONSTANT_EQUAL = "=";

	private Plugin _plugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );

	/**
	 * Get an instance of the service
	 * @return An instance of the service
	 */
	public static DatabaseAccountLifeTimeService getService( )
	{
		return SpringContextService.getBean( BEAN_DATABASE_ACCOUNT_LIFE_TIME_SERVICE );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Plugin getPlugin( )
	{
		return _plugin;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getIdUsersWithExpiredLifeTimeList( Timestamp currentTimestamp )
	{
		return DatabaseUserHome.getIdUsersWithExpiredLifeTimeList( currentTimestamp, _plugin );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getIdUsersToSendFirstAlert( Timestamp alertMaxDate )
	{
		return DatabaseUserHome.getIdUsersToSendFirstAlert( alertMaxDate, _plugin );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getIdUsersToSendOtherAlert( Timestamp alertMaxDate, Timestamp timeBetweenAlerts, int maxNumberAlerts )
	{
		return DatabaseUserHome.getIdUsersToSendOtherAlert( alertMaxDate, timeBetweenAlerts, maxNumberAlerts, _plugin );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateNbAlert( List<Integer> listIdUser )
	{
		DatabaseUserHome.updateNbAlert( listIdUser, _plugin );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserStatusExpired( List<Integer> listIdUser )
	{
		DatabaseUserHome.updateUserStatus( listIdUser, DatabaseUser.STATUS_EXPIRED, _plugin );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExpirationtMailBody( )
	{
		return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DIRECTORY_EXPIRATION_MAIL );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFirstAlertMailBody( )
	{
		return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DIRECTORY_FIRST_ALERT_MAIL );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOtherAlertMailBody( )
	{
		return DatabaseTemplateService.getTemplateFromKey( PARAMETER_MYLUTECE_DIRECTORY_OTHER_ALERT_MAIL );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addParametersToModel( Map<String, String> model, Integer nIdUser )
	{
		DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nIdUser, _plugin );
		DateFormat dateFormat = SimpleDateFormat.getDateInstance( DateFormat.SHORT, Locale.getDefault( ) );

		String accountMaxValidDate = dateFormat.format( new Date( user.getAccountMaxValidDate( ).getTime( ) ) );

		StringBuilder sbUrl = new StringBuilder( );
		sbUrl.append( AppPropertiesService.getProperty( PROPERTY_PROD_URL ) );
		sbUrl.append( JSP_URL_REACTIVATE_ACCOUNT );
		sbUrl.append( CONSTANT_AND );
		sbUrl.append( MARK_USER_ID );
		sbUrl.append( CONSTANT_EQUAL );
		sbUrl.append( nIdUser.toString( ) );
		sbUrl.append( CONSTANT_AND );
		sbUrl.append( MARK_REF );
		sbUrl.append( CONSTANT_EQUAL );
		sbUrl.append( CryptoService.encrypt( Long.toString( user.getAccountMaxValidDate( ).getTime( ) ), AppPropertiesService.getProperty( PROPERTY_ACCOUNT_REF_ENCRYPT_ALGO ) ) );

		String activationURL = sbUrl.toString( );

		model.put( MARK_DATE_VALID, accountMaxValidDate );
		model.put( MARK_URL, activationURL );
		model.put( MARK_LAST_NAME, user.getLastName( ) );
		model.put( MARK_FIRST_NAME, user.getFirstName( ) );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserMainEmail( int nIdUser )
	{
		DatabaseUser user = DatabaseUserHome.findByPrimaryKey( nIdUser, _plugin );
		return user.getEmail( );
	}

}
