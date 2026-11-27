-- liquibase formatted sql
-- changeset mylutece-database:update_db_mylutece_database-7.0.0-7.1.0.sql
-- preconditions onFail:MARK_RAN onError:WARN

--
-- The actions of the account pages are posted to the XPage mylutecedatabase (MVC), the validation link of the e-mails opens its view validateAccount
--
DELETE FROM core_datastore WHERE entity_key IN ('mylutece.security.public_url.mylutece-database.url.doActionsXPage','mylutece.security.public_url.mylutece-database.url.validateAccount.page');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.doActionsXPage','jsp/site/Portal.jsp?page=mylutecedatabase&action=do*');
INSERT INTO core_datastore(entity_key,entity_value) VALUES('mylutece.security.public_url.mylutece-database.url.validateAccount.page','jsp/site/Portal.jsp?page=mylutecedatabase&view=validateAccount');

--
-- The public pages of the XPage mylutecedatabase are named with the view parameter (the core checks the security token of every action)
--
UPDATE core_datastore SET entity_value = 'jsp/site/Portal.jsp?page=mylutecedatabase&view=createAccount' WHERE entity_key = 'mylutece.security.public_url.mylutece-database.url.createAccount.page';
UPDATE core_datastore SET entity_value = 'jsp/site/Portal.jsp?page=mylutecedatabase&view=lostPassword' WHERE entity_key = 'mylutece.security.public_url.mylutece-database.url.lostPassword.page';
UPDATE core_datastore SET entity_value = 'jsp/site/Portal.jsp?page=mylutecedatabase&view=lostLogin' WHERE entity_key = 'mylutece.security.public_url.mylutece-database.url.lostLogin.page';
UPDATE core_datastore SET entity_value = 'jsp/site/Portal.jsp?page=mylutecedatabase&view=reinitPassword' WHERE entity_key = 'mylutece.security.public_url.mylutece-database.url.reinitPassword.page';

-- changeset mylutece-database:update_db_mylutece_database-7.0.0-7.1.0.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM mylutece_database_user_parameter WHERE parameter_key = 'access_failures_max_ip'
-- comment The failures of a login lock this login only; blocking an address after a number of failures on any logins is off by default (0), the administrator enables it
INSERT INTO mylutece_database_user_parameter VALUES ('access_failures_max_ip', '0');
