![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=auth-module-mydatabase-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Amodule-mylutece-database&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Amodule-mylutece-database)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Amodule-mylutece-database&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Amodule-mylutece-database)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Amodule-mylutece-database&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Amodule-mylutece-database)

# Module-mylutece-database

## Introduction

The module-mylutece-database provides a front office user authentication implementationfor MyLutece based on local users stored in a database table.

This module enables comprehensive user account lifecycle management, including:

 
* User account creation and modification
* Secure authentication with password management (encryption, history, expiration)
* User groups and roles management
* Account lifetime management (expiration, reactivation, anonymization)
* Lost password recovery
* User import/export (XML, CSV)
* Protection against malicious login attempts (IP blocking, CAPTCHA)

The module integrates into Lutece's security architecture and uses the MyLutece system for front office authentication, while providing a complete administration interface foruser management.

## Configuration

 **Configuration Properties** 

The following properties are available in the `mylutece-database.properties` file:

 **Authentication Service** 

 
*  `mylutece-database.service.name` : Authentication service name (default: "Lutece Database Authentication Service")

 **Pagination Configuration** 

 
*  `paginator.users.itemsPerPage` : Number of users per page (default: 50)

 **Daemon Configuration** 

 
*  `daemon.databaseAnonymizationDaemon.interval` : Anonymization daemon execution interval in seconds (default: 86400 = 24h)
*  `daemon.databaseAnonymizationDaemon.onstartup` : Execute on startup (0 or 1)
*  `daemon.databaseAccountLifeTimeDaemon.interval` : Account lifetime daemon execution interval (default: 86400 = 24h)
*  `daemon.databaseAccountLifeTimeDaemon.onstartup` : Execute on startup (0 or 1)

 **Optional URLs** 

 
*  `mylutece-database.url.login.page` : Login page
*  `mylutece-database.url.doLogin` : Login action
*  `mylutece-database.url.doLogout` : Logout action
*  `mylutece-database.url.changePassword.page` : Change password page
*  `mylutece-database.url.viewAccount.page` : View account page
*  `mylutece-database.url.createAccount.page` : Create account page
*  `mylutece-database.url.lostPassword.page` : Lost password page
*  `mylutece-database.url.lostLogin.page` : Lost login page
*  `mylutece-database.url.accessDenied.page` : Access denied page
*  `mylutece-database.url.default.redirect` : Default redirect page
*  `mylutece-database.url.reinitPassword.page` : Reinitialize password page
*  `mylutece-database.url.resetPassword.page` : Reset password page
*  `mylutece-database.url.delete.page` : Delete confirmation page
*  `mylutece-database.url.modifyAccount.page` : Modify account page

 **Security** 

 
*  `mylutece-database.account_life_time.refEncryptionAlgorythm` : Reference encryption algorithm (default: SHA-256)

 **Daemons to Activate** 

Two daemons are provided by the module:

 
* DatabaseAnonymizationDaemon: Daemon that anonymizes expired account data
* DatabaseAccountLifeTimeDaemon: Daemon that manages account lifecycle (expiration notifications, deactivation of expired accounts)

## Usage

 **Administration Rights** 

The module defines the following administration rights :

 
* DATABASE_MANAGEMENT_USERS : Database user management
* DATABASE_GROUPS_MANAGEMENT : User groups management

 **RBAC Resources** 

The module defines the following RBAC resources:

 
* DatabaseResourceIdService (resource type: DATABASE)
 
* Permission MANAGE: Advanced parameters management
* Permission IMPORT_EXPORT_DATABASE_USERS: User import/export

* GroupResourceIdService (resource type: GROUP_TYPE)
 
* Permission ASSIGN_GROUP: Assign groups to users


 **Roles** 

The following roles are defined ( `core_admin_role` and `core_admin_role_resource` tables):

 
* mylutece_database_manager : MyLutece database manager
* assign_groups : Group assignment

 **Exposed Java Services** 

 **DatabaseService** 

Main service for database user management. Primary methods:

 
*  `getService()` : Retrieves the singleton service instance
*  `doCreateUser(DatabaseUser user, String strPassword, Plugin plugin)` : Creates a new user
*  `doModifyPassword(DatabaseUser user, String strPassword, Plugin plugin)` : Modifies a user's password
*  `doUpdateUser(DatabaseUser user, Plugin plugin)` : Updates user information
*  `checkPassword(String strUserGuid, String strPassword, Plugin plugin)` : Verifies password validity
*  `isUserActive(String strUserName, Plugin plugin)` : Checks if a user is active
*  `getFilteredUsersInterface(DatabaseUserFilter duFilter, boolean bIsSearch, List<DatabaseUser>listUsers, HttpServletRequest request)` : Filters users based on criteria
*  `updateUserExpirationDate(int nIdUser, Plugin plugin)` : Updates user expiration date
*  `updateUserLastLoginDate(String strLogin, Plugin plugin)` : Updates last login date
*  `getXmlFromUser(DatabaseUser user, boolean bExportRoles, boolean bExportGroups, boolean bExportAttributes, List<IAttribute>listAttributes, Locale locale, Plugin plugin)` : Generates XML from a user
*  `doAutoLoginDatabaseUser(HttpServletRequest request, DatabaseUser databaseUser, Plugin plugin)` : Performs automatic login

 **BaseAuthentication** 

Database-based authentication implementation. Inherits from PortalAuthentication.

 
* Manages front office user authentication
* Supports password recovery
* Manages login attempts and IP blocking
* CAPTCHA integration for security

 **GroupService** 

User group management service.

 **DatabaseAccountLifeTimeService** 

Account lifecycle management service (expiration, notifications, reactivation).

 **DatabaseAnonymizationService** 

Service for anonymizing expired account data.

 **DatabaseUserKeyService** 

Service for managing user activation and reset keys.

 **ImportDatabaseUserService** 

Service for importing users from XML or CSV files.

 **DatabaseUserParameterService** 

Service for managing user parameters (account lifetime, password expiration, etc.).

 **Notification Templates** 

The module uses the following templates for email notifications:

 
*  `mylutece_database_first_alert_mail` : First account expiration warning
*  `mylutece_database_other_alert_mail` : Subsequent expiration warnings
*  `mylutece_database_expiration_mail` : Account expiration notification
*  `mylutece_database_account_reactivated_mail` : Account reactivation confirmation
*  `mylutece_database_unblock_user` : IP unblocking
*  `mylutece_database_password_expired` : Password expiration notification
*  `mylutece_database_mailLostPassword` : Lost password recovery email
*  `mylutece_database_mailPasswordEncryptionChanged` : Password encryption change notification

 **XSL Export** 

The module provides two predefined XSL exports:

 
* CSV User Export : Exports users to CSV format with their attributes, roles and groups
* XML User Export : Exports users to XML format with all their data


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/module-mylutece-database/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*