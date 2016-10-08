ALTER TABLE mylutece_database_user MODIFY COLUMN password LONG VARCHAR default NULL;
ALTER TABLE mylutece_database_user_password_history MODIFY COLUMN password LONG VARCHAR default NULL;

-- update password storage
UPDATE mylutece_database_user SET password =
 CONCAT(COALESCE(
	(SELECT CONCAT(mdup1.parameter_value,':') FROM mylutece_database_user_parameter mdup1 CROSS JOIN mylutece_database_user_parameter mdup2 WHERE mdup1.parameter_key = 'encryption_algorithm' AND mdup2.parameter_key = 'enable_password_encryption' AND LOWER(mdup2.parameter_value) = 'true'),
	'PLAINTEXT:')
,password);

-- updating password history with best effort to guess format
-- for PostgreSQL, replace 'REGEXP' by '~*' and 'NOT REGEXP' by '!~*'
UPDATE mylutece_database_user_password_history SET password = CONCAT('MD5:',password) WHERE password REGEXP '^[0-9a-f]{32}$';
UPDATE mylutece_database_user_password_history SET password = CONCAT('SHA-1:',password) WHERE password REGEXP '^[0-9a-f]{40}$';
UPDATE mylutece_database_user_password_history SET password = CONCAT('SHA-256:',password) WHERE password REGEXP '^[0-9a-f]{64}$';
UPDATE mylutece_database_user_password_history SET password = CONCAT('PLAINTEXT:',password) WHERE password NOT REGEXP '^(MD5:[0-9a-f]{32}|SHA-1:[0-9a-f]{40}|SHA-256:[0-9a-f]{64})$';

DELETE FROM mylutece_database_user_parameter WHERE parameter_key='enable_password_encryption';
DELETE FROM mylutece_database_user_parameter WHERE parameter_key='encryption_algorithm';