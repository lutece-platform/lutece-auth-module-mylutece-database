--
-- Dumping data for table core_admin_right
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES 
('DATABASE_MANAGEMENT_USERS', 'module.mylutece.database.adminFeature.database_management_user.name', 3, 'jsp/admin/plugins/mylutece/modules/database/ManageUsers.jsp', 'module.mylutece.database.adminFeature.database_management_user.description', 0, 'mylutece-database', 'USERS', NULL, NULL);

INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES 
('DATABASE_GROUPS_MANAGEMENT', 'module.mylutece.database.adminFeature.groups_management.name', 3, 'jsp/admin/plugins/mylutece/modules/database/ManageGroups.jsp', 'module.mylutece.database.adminFeature.groups_management.description', 0, 'mylutece-database', 'USERS', NULL, NULL);

--
-- Dumping data for table core_user_right
--
INSERT INTO core_user_right (id_right,id_user) VALUES ('DATABASE_MANAGEMENT_USERS',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('DATABASE_GROUPS_MANAGEMENT',1);

--
-- Init  table core_admin_dashboard
--
INSERT INTO core_admin_dashboard(dashboard_name, dashboard_column, dashboard_order) VALUES('databaseAdminDashboardComponent', 1, 1);

--
-- Init  table core_admin_role
--
INSERT INTO core_admin_role (role_key,role_description) VALUES ('mylutece_database_manager', 'Mylutece Database management');

--
-- Init  table core_admin_role_resource
--
INSERT INTO core_admin_role_resource (rbac_id,role_key,resource_type,resource_id,permission) VALUES 
 (206,'assign_groups','GROUP_TYPE','*','ASSIGN_GROUP');
INSERT INTO core_admin_role_resource (rbac_id,role_key,resource_type,resource_id,permission) VALUES (350,'mylutece_database_manager','DATABASE','*','*');

--
-- Init  table core_user_role
--
INSERT INTO core_user_role (role_key,id_user) VALUES ('mylutece_database_manager',1);

INSERT INTO core_template VALUES ('mylutece_database_first_alert_mail', 'Bonjour ${first_name} ! Votre compte utilisateur arrive à expiration. Pour prolonger sa validité, veuillez <a href="${url}">cliquer ici</a>.</br>Si vous ne le faites pas avant le ${date_valid}, il sera désactivé.');
INSERT INTO core_template VALUES ('mylutece_database_expiration_mail', 'Bonjour ${first_name} ! Votre compte a expiré. Vous ne pourrez plus vous connecter avec, et les données vous concernant ont été anonymisées');
INSERT INTO core_template VALUES ('mylutece_database_other_alert_mail', 'Bonjour ${first_name} ! Votre compte utilisateur arrive à expiration. Pour prolonger sa validité, veuillez <a href="${url}">cliquer ici</a>.</br>Si vous ne le faites pas avant le ${date_valid}, il sera désactivé.');
INSERT INTO core_template VALUES ('mylutece_database_account_reactivated_mail', 'Bonjour ${first_name} ! Votre compte utilisateur a bien été réactivé. Il est désormais valable jusqu''au ${date_valid}.');
INSERT INTO core_template VALUES ('mylutece_database_unblock_user', '${site_link!}<br />Bonjour ! Votre IP a été bloquée. Pour la débloquer, vous pouvez suivre le lien suivant : <a href="${url}">debloquer</a>.');
INSERT INTO core_template VALUES ('mylutece_database_password_expired', 'Bonjour ! Votre mot de passe a éxpiré. Lors de votre prochaine connection, vous pourrez le changer.');
INSERT INTO core_template VALUES ('mylutece_database_mailLostPassword', '<html><head><title>#i18n{mylutece.email_reinit.object}</title></head><body><p>#i18n{mylutece.email_reinit.content.text}<br /></p><p>#i18n{mylutece.email_reinit.content.newPassword} : ${new_password}<br /></p></body></html><p>#i18n{mylutece.email_reinit.content.reinitPassword}<a href="${reinit_url}">#i18n{mylutece.email_reinit.content.labelLink}</a></p>');
INSERT INTO core_template VALUES ('mylutece_database_mailPasswordEncryptionChanged', '<html><head><title>Votre mot de passe a été réinitialisé</title></head><body><p>Pour des raisons de sécurité, votre mot de passe a été réinitialisé.<br />\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-</p><p>Votre nouveau mot de passe est : ${new_password}<br />\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-\-</p></body></html>');

INSERT INTO core_datastore VALUES ('mylutece-database_banned_domain_names', 'yopmail.com');

INSERT INTO core_physical_file VALUES (127,'<?xml version=\"1.0\"?>\r\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n	<xsl:output method=\"text\"/>\r\n	\r\n	<xsl:template match=\"users\">\r\n		<xsl:apply-templates select=\"user\" />\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"user\">\r\n		<xsl:text>\"</xsl:text>\r\n		<xsl:value-of select=\"access_code\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"last_name\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"first_name\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"email\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"status\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"password_max_valid_date\" />\r\n		<xsl:text>\";\"</xsl:text>\r\n		<xsl:value-of select=\"account_max_valid_date\" />\r\n		<xsl:text>\"</xsl:text>\r\n		<xsl:apply-templates select=\"roles\" />\r\n		<xsl:apply-templates select=\"groups\" />\r\n		<xsl:apply-templates select=\"attributes\" />\r\n		<xsl:text>&#10;</xsl:text>\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"roles\">\r\n		<xsl:apply-templates select=\"role\" />\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"role\">\r\n		<xsl:text>;\"role:</xsl:text>\r\n		<xsl:value-of select=\"current()\" />\r\n		<xsl:text>\"</xsl:text>\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"groups\">\r\n		<xsl:apply-templates select=\"group\" />\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"group\">\r\n		<xsl:text>;\"group:</xsl:text>\r\n		<xsl:value-of select=\"current()\" />\r\n		<xsl:text>\"</xsl:text>\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"attributes\">\r\n		<xsl:apply-templates select=\"attribute\" />\r\n	</xsl:template>\r\n	\r\n	<xsl:template match=\"attribute\">\r\n		<xsl:text>;\"</xsl:text>\r\n		<xsl:value-of select=\"attribute-id\" />\r\n		<xsl:text>:</xsl:text>\r\n		<xsl:value-of select=\"attribute-field-id\" />\r\n		<xsl:text>:</xsl:text>\r\n		<xsl:value-of select=\"attribute-value\" />\r\n		<xsl:text>\"</xsl:text>\r\n	</xsl:template>\r\n	\r\n</xsl:stylesheet>');
INSERT INTO core_file VALUES (127,'export_users_csv.xml',127,1861,'application/xml');
INSERT INTO core_xsl_export VALUES (127,'MyLutece Database - Export CSV des utilisateurs','Export des utilisateur MyLutece Database dans un fichier CSV','csv',127,'mylutece-database');

INSERT INTO core_physical_file VALUES (128,'<?xml version=\"1.0\" ?>\r\n<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n	<xsl:template match=\"/ | @* | node()\">\r\n		<xsl:copy>\r\n			<xsl:apply-templates select=\"@* | node()\" />\r\n		</xsl:copy>\r\n	</xsl:template>\r\n</xsl:stylesheet>');
INSERT INTO core_file VALUES (128,'export_users_xml.xml',128,259,'application/xml');
INSERT INTO core_xsl_export VALUES (128,'MyLutece Database - Export XML des utilisateurs','Export des utilisateur MyLutece Database dans un fichier XML','xml',128,'mylutece-database');

--
-- Init Public URLs
--

INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.login.page','jsp/site/Portal.jsp?page=mylutece&action=login');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.doLogin','jsp/site/plugins/mylutece/DoMyLuteceLogin.jsp');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.doLogout','jsp/site/plugins/mylutece/DoMyLuteceLogout.jsp');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.createAccount.page','jsp/site/Portal.jsp?page=mylutecedatabase&action=createAccount');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.lostPassword.page','jsp/site/Portal.jsp?page=mylutecedatabase&action=lostPassword');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.lostLogin.page','jsp/site/Portal.jsp?page=mylutecedatabase&action=lostLogin');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.reinitPassword.page','jsp/site/Portal.jsp?page=mylutecedatabase&action=reinitPassword');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.doActionsAll','jsp/site/plugins/mylutece/modules/database/Do*');



