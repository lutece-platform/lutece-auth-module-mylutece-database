--liquibase formatted sql
--changeset mylutece-database:update_db_mylutece_database-2.3.5-3.0.2.sql
--preconditions onFail:MARK_RAN onError:WARN
INSERT INTO mylutece_database_user_parameter VALUES ('auto_login_after_validation_email', 'false');
