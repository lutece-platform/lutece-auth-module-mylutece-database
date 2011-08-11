--
-- Add column to mylutece_database_user
--
ALTER TABLE mylutece_database_user ADD COLUMN is_active SMALLINT DEFAULT 0 NOT NULL;

--
-- Dump data in tabel mylutece_database_user_parameter
--
INSERT INTO mylutece_database_user_parameter VALUES ('account_creation_validation_email', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('enable_jcaptcha', 'false');

--
-- Table structure for table mylutece_database_key
--
DROP TABLE IF EXISTS mylutece_database_key;
CREATE TABLE mylutece_database_key(
	mylutece_database_user_key VARCHAR(255) DEFAULT NULL,
	mylutece_database_user_id INT DEFAULT 0 NOT NULL,	
	PRIMARY KEY (mylutece_database_user_key)
);
