package fr.paris.lutece.plugins.mylutece.modules.database.authentication.service;

import fr.paris.lutece.portal.service.csv.CSVMessageDescriptor;
import fr.paris.lutece.portal.service.csv.CSVReaderService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;
import java.util.Locale;


public class ImportDatabaseUserService extends CSVReaderService
{
    private static final String CONSTANT_DEFAULT_IMPORT_EXPORT_USER_SEPARATOR = ":";
    private static final String PROPERTY_IMPORT_EXPORT_USER_SEPARATOR = "lutece.importExportUser.defaultSeparator";

    private Character _strAttributesSeparator;
    private boolean _bUpdateExistingUsers;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> readLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> checkLineOfCSVFile( String[] strLineDataArray, int nLineNumber, Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<CSVMessageDescriptor> getEndOfProcessMessages( int nNbLineParses, int nNbLinesWithoutErrors,
            Locale locale )
    {
        // TODO Auto-generated method stub
        return null;
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
