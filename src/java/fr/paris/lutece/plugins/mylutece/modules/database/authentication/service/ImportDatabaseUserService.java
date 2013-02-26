package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.plugins.mylutece.business.attribute.AttributeHome;
import fr.paris.lutece.plugins.mylutece.business.attribute.IAttribute;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserField;
import fr.paris.lutece.plugins.mylutece.business.attribute.MyLuteceUserFieldHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.database.authentication.business.DatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.MyLutecePlugin;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldListenerService;
import fr.paris.lutece.plugins.mylutece.service.attribute.MyLuteceUserFieldService;
import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.csv.CSVMessageLevel;
import fr.paris.lutece.portal.service.csv.CSVReaderService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.password.PasswordUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Import database users from a CSV file
 */
public class ImportDatabaseUserService extends CSVReaderService
{
    private static final String MESSAGE_NO_STATUS = "module.mylutece.database.import_users_from_file.importNoStatus";
    private static final String MESSAGE_ACCESS_CODE_ALREADY_USED = "module.mylutece.database.message.user_exist";
    private static final String MESSAGE_EMAIL_ALREADY_USED = "module.mylutece.database.message.user.accessEmailUsed";
    private static final String MESSAGE_USERS_IMPORTED = "module.mylutece.database.import_users_from_file.usersImported";
    private static final String MESSAGE_ERROR_MIN_NUMBER_COLUMNS = "module.mylutece.database.import_users_from_file.messageErrorMinColumnNumber";

    private static final String CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR = ":";
    private static final String CONSTANT_ROLE = "role";
    private static final String CONSTANT_GROUP = "group";
    private static final String PROPERTY_IMPORT_EXPORT_USER_SEPARATOR = "lutece.importExportUser.defaultSeparator";

    private static final int CONSTANT_MINIMUM_COLUMNS_PER_LINE = 7;

    private Character _strAttributesSeparator;
    private boolean _bUpdateExistingUsers;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> readLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        Plugin databasePlugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        Plugin mylutecePlugin = PluginService.getPlugin( MyLutecePlugin.PLUGIN_NAME );
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        int nIndex = 0;

        String strAccessCode = strLineDataArray[nIndex++];
        String strLastName = strLineDataArray[nIndex++];
        String strFirstName = strLineDataArray[nIndex++];
        String strEmail = strLineDataArray[nIndex++];

        boolean bUpdateUser = getUpdateExistingUsers( );
        int nUserId = 0;
        if ( bUpdateUser )
        {
            int nAccessCodeUserId = DatabaseUserHome.findDatabaseUserIdFromLogin( strAccessCode, databasePlugin );
            if ( nAccessCodeUserId > 0 )
            {
                nUserId = nAccessCodeUserId;
            }
            bUpdateUser = nUserId > 0;
        }

        String strStatus = strLineDataArray[nIndex++];
        int nStatus = 0;
        if ( StringUtils.isNotEmpty( strStatus ) )
        {
            nStatus = Integer.parseInt( strStatus );
        }
        else
        {
            Object[] args = { strLastName, strFirstName, nStatus };
            String strMessage = I18nService.getLocalizedString( MESSAGE_NO_STATUS, args, locale );
            CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, nLineNumber, strMessage );
            listMessages.add( message );
        }

        // We ignore the password max valid date attribute because we changed the password.
        // String strPasswordMaxValidDate = strLineDataArray[nIndex++];
        nIndex++;
        // We ignore the account max valid date attribute
        // String strAccountMaxValidDate = strLineDataArray[nIndex++];
        nIndex++;

        DatabaseUser user = new DatabaseUser( );

        user.setLogin( strAccessCode );
        user.setLastName( strLastName );
        user.setFirstName( strFirstName );
        user.setEmail( strEmail );
        user.setStatus( nStatus );

        if ( bUpdateUser )
        {
            user.setUserId( nUserId );
            // We update the user
            DatabaseService.getService( ).doUpdateUser( user, databasePlugin );
        }
        else
        {
            // We create the user
            DatabaseService.getService( ).doCreateUser( user, PasswordUtil.makePassword( ), databasePlugin );
        }
        DatabaseHome.removeRolesForUser( user.getUserId( ), databasePlugin );
        DatabaseHome.removeGroupsForUser( user.getUserId( ), databasePlugin );
        MyLuteceUserFieldService.doRemoveUserFields( user.getUserId( ), locale );

        Map<Integer, List<String>> mapAttributesValues = new HashMap<Integer, List<String>>( );

        // We get every attributes of the user
        List<String> listRoles = new ArrayList<String>( );
        List<String> listGroups = new ArrayList<String>( );
        while ( nIndex < strLineDataArray.length )
        {
            String strValue = strLineDataArray[nIndex];
            if ( StringUtils.isNotBlank( strValue ) && strValue.indexOf( getAttributesSeparator( ) ) > 0 )
            {
                int nSeparatorIndex = strValue.indexOf( getAttributesSeparator( ) );
                String strLineId = strValue.substring( 0, nSeparatorIndex );
                if ( StringUtils.isNotBlank( strLineId ) )
                {
                    if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_ROLE ) )
                    {
                        listRoles.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else if ( StringUtils.equalsIgnoreCase( strLineId, CONSTANT_GROUP ) )
                    {
                        listGroups.add( strValue.substring( nSeparatorIndex + 1 ) );
                    }
                    else
                    {
                        int nAttributeId = Integer.parseInt( strLineId );

                        String strAttributeValue = strValue.substring( nSeparatorIndex + 1 );
                        List<String> listValues = mapAttributesValues.get( nAttributeId );
                        if ( listValues == null )
                        {
                            listValues = new ArrayList<String>( );
                        }
                        listValues.add( strAttributeValue );
                        mapAttributesValues.put( nAttributeId, listValues );
                    }
                }
            }
            nIndex++;
        }

        for ( String strRole : listRoles )
        {
            DatabaseHome.addRoleForUser( user.getUserId( ), strRole, databasePlugin );
        }

        for ( String strGoup : listGroups )
        {
            DatabaseHome.addGroupForUser( user.getUserId( ), strGoup, databasePlugin );
        }

        List<IAttribute> listAttributes = AttributeHome.findAll( locale, mylutecePlugin );
        // We save the attributes found
        for ( IAttribute attribute : listAttributes )
        {
            List<String> listValues = mapAttributesValues.get( attribute.getIdAttribute( ) );
            if ( listValues != null && listValues.size( ) > 0 )
            {
                int nIdField = 0;
                boolean bMyLuteceAttribute = attribute.getPlugin( ) == null
                        || StringUtils.equals( attribute.getPlugin( ).getName( ),
                        MyLutecePlugin.PLUGIN_NAME );
                for ( String strValue : listValues )
                {
                    int nSeparatorIndex = strValue.indexOf( getAttributesSeparator( ) );
                    if ( nSeparatorIndex >= 0 )
                    {
                        nIdField = 0;
                        try
                        {
                            nIdField = Integer.parseInt( strValue.substring( 0, nSeparatorIndex ) );
                        }
                        catch ( NumberFormatException e )
                        {
                            nIdField = 0;
                        }
                        strValue = strValue.substring( nSeparatorIndex + 1 );
                    }
                    else
                    {
                        nIdField = 0;
                    }
                    String[] strValues = { strValue };
                    List<MyLuteceUserField> listUserFields = attribute.getUserFieldsData( strValues, user.getUserId( ) );

                    for ( MyLuteceUserField userField : listUserFields )
                    {
                        if ( userField != null )
                        {
                            userField.getAttributeField( ).setIdField( nIdField );
                            MyLuteceUserFieldHome.create( userField, mylutecePlugin );
                        }
                    }
                    if ( !bMyLuteceAttribute )
                    {
                        for ( MyLuteceUserFieldListenerService myLuteceUserFieldListenerService : SpringContextService
                                .getBeansOfType( MyLuteceUserFieldListenerService.class ) )
                        {
                            myLuteceUserFieldListenerService.doCreateUserFields( user.getUserId( ), listUserFields,
                                    locale );
                        }
                    }
                }
            }
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> checkLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        int nMinColumnNumber = CONSTANT_MINIMUM_COLUMNS_PER_LINE;
        Plugin databasePlugin = PluginService.getPlugin( DatabasePlugin.PLUGIN_NAME );
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        if ( strLineDataArray == null || strLineDataArray.length < nMinColumnNumber )
        {
            int nNbCol;
            if ( strLineDataArray == null )
            {
                nNbCol = 0;
            }
            else
            {
                nNbCol = strLineDataArray.length;
            }
            Object[] args = { nNbCol, nMinColumnNumber };
            String strErrorMessage = I18nService.getLocalizedString( MESSAGE_ERROR_MIN_NUMBER_COLUMNS, args, locale );
            CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strErrorMessage );
            listMessages.add( error );
            return listMessages;
        }
        if ( !getUpdateExistingUsers( ) )
        {
            String strAccessCode = strLineDataArray[0];
            String strEmail = strLineDataArray[3];
            if ( DatabaseUserHome.findDatabaseUserIdFromLogin( strAccessCode, databasePlugin ) > 0 )
            {
                String strMessage = I18nService.getLocalizedString( MESSAGE_ACCESS_CODE_ALREADY_USED, locale );
                CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber, strMessage );
                listMessages.add( error );
            }
            else
            {
                Collection<DatabaseUser> listUsers = DatabaseUserHome.findDatabaseUsersListForEmail( strEmail,
                        databasePlugin );
                if ( listUsers != null && listUsers.size( ) > 0 )
                {
                    String strMessage = I18nService.getLocalizedString( MESSAGE_EMAIL_ALREADY_USED, locale );
                    CSVMessageDescriptor error = new CSVMessageDescriptor( CSVMessageLevel.ERROR, nLineNumber,
                            strMessage );
                    listMessages.add( error );
                }
            }
        }

        return listMessages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> getEndOfProcessMessages( int nNbLineParses, int nNbLinesWithoutErrors,
            Locale locale )
    {
        List<CSVMessageDescriptor> listMessages = new ArrayList<CSVMessageDescriptor>( );
        Object[] args = { nNbLineParses, nNbLinesWithoutErrors };
        String strMessageContent = I18nService.getLocalizedString( MESSAGE_USERS_IMPORTED, args, locale );
        CSVMessageDescriptor message = new CSVMessageDescriptor( CSVMessageLevel.INFO, 0, strMessageContent );
        listMessages.add( message );
        return listMessages;
    }

    /**
     * Get the separator used for attributes of admin users.
     * @return The separator
     */
    public Character getAttributesSeparator( )
    {
        if ( _strAttributesSeparator == null )
        {
            _strAttributesSeparator = AppPropertiesService.getProperty( PROPERTY_IMPORT_EXPORT_USER_SEPARATOR,
                    CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR ).charAt( 0 );
        }
        return _strAttributesSeparator;
    }

    /**
     * Get the update users flag
     * @return True if existing users should be updated, false if they should be
     *         ignored.
     */
    public boolean getUpdateExistingUsers( )
    {
        return _bUpdateExistingUsers;
    }

    /**
     * Set the update users flag
     * @param bUpdateExistingUsers True if existing users should be updated,
     *            false if they should be ignored.
     */
    public void setUpdateExistingUsers( boolean bUpdateExistingUsers )
    {
        this._bUpdateExistingUsers = bUpdateExistingUsers;
    }

}
