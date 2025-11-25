-- liquibase formatted sql
-- changeset mylutece-database:init_db_mylutece_database.sql
-- preconditions onFail:MARK_RAN onError:WARN
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('login', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('name_given', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('name_family', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('email', 1);