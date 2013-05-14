ALTER TABLE mylutece_database_user ADD COLUMN last_login TIMESTAMP DEFAULT '1980-01-01';

INSERT INTO mylutece_database_user_parameter VALUES ('access_failures_captcha', '1');
INSERT INTO mylutece_database_user_parameter VALUES ('unblock_user_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('unblock_user_mail_subject', 'Votre IP a été bloquée');
INSERT INTO mylutece_database_user_parameter VALUES ('enable_unblock_ip', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('notify_user_password_expired', '');
INSERT INTO mylutece_database_user_parameter VALUES ('password_expired_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('password_expired_mail_subject', 'Votre mot de passe a expiré');
INSERT INTO mylutece_database_user_parameter VALUES ('mail_lost_password_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('mail_lost_password_subject', 'Votre mot de passe a été réinitialisé');
INSERT INTO mylutece_database_user_parameter VALUES ('mail_password_encryption_changed_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('mail_password_encryption_changed_subject', 'Votre mot de passe a été réinitialisé');

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
