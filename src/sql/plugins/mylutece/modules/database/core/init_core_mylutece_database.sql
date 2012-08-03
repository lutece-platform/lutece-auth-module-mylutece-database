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
INSERT INTO core_admin_role_resource (rbac_id,role_key,resource_type,resource_id,permission) VALUES (350,'mylutece_database_manager','DATABASE','*','*');

--
-- Init  table core_user_role
--
INSERT INTO core_user_role (role_key,id_user) VALUES ('mylutece_database_manager',1);

INSERT INTO core_template VALUES ('mylutece_database_first_alert_mail', 'Bonjour ${first_name} ! Votre compte utilisateur arrive à expiration. Pour prolonger sa validité, veuillez <a href="${url}">cliquer ici</a>.</br>Si vous ne le faites pas avant le ${date_valid}, il sera désactivé.');
INSERT INTO core_template VALUES ('mylutece_database_expiration_mail', 'Bonjour ${first_name} ! Votre compte a expiré. Vous ne pourrez plus vous connecter avec, et les données vous concernant ont été anonymisées');
INSERT INTO core_template VALUES ('mylutece_database_other_alert_mail', 'Bonjour ${first_name} ! Votre compte utilisateur arrive à expiration. Pour prolonger sa validité, veuillez <a href="${url}">cliquer ici</a>.</br>Si vous ne le faites pas avant le ${date_valid}, il sera désactivé.');
INSERT INTO core_template VALUES ('mylutece_database_account_reactivated_mail', 'Bonjour ${first_name} ! Votre compte utilisateur a bien été réactivé. Il est désormais valable jusqu''au ${date_valid}.');
