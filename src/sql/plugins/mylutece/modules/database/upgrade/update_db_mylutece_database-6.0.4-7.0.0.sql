-- liquibase formatted sql
-- changeset mylutece-database:update_db_mylutece_database-6.0.4-7.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_file SET origin = 'defaultDatabaseFileStoreProvider' WHERE id_file = 127;
UPDATE core_file SET origin = 'defaultDatabaseFileStoreProvider' WHERE id_file = 128;