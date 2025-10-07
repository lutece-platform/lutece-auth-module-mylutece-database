-- liquibase formatted sql
-- changeset mylutece-database:update_db_mylutece_database-2.2.2-2.2.3.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Table structure for table mylutece_database_user_parameter
--
DROP TABLE IF EXISTS mylutece_database_user_parameter;
CREATE TABLE mylutece_database_user_parameter (
	parameter_key varchar(100) NOT NULL,
	parameter_value varchar(100) NOT NULL,
	PRIMARY KEY (parameter_key)
);

--
-- Init  table mylutece_database_user_parameter
--
INSERT INTO mylutece_database_user_parameter VALUES ('enable_password_encryption', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('encryption_algorithm', '');
