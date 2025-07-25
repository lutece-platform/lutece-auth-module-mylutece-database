--liquibase formatted sql
--changeset mylutece-database:update_db_mylutece_database-3.0.2-3.1.1.sql
--preconditions onFail:MARK_RAN onError:WARN
DELETE FROM mylutece_database_user_parameter WHERE  parameter_key='password_format';
INSERT INTO mylutece_database_user_parameter VALUES ('password_format_upper_lower_case', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('password_format_numero', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('password_format_special_characters', 'false');