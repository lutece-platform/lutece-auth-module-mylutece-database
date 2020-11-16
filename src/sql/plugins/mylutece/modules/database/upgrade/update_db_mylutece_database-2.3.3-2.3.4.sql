--
-- Add security parameters to mylutece_database_user_parameter
--
INSERT INTO mylutece_database_user_parameter VALUES ('force_change_password_reinit', '');
INSERT INTO mylutece_database_user_parameter VALUES ('password_minimum_length', '8');
INSERT INTO mylutece_database_user_parameter VALUES ('password_format', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('password_duration', '');
INSERT INTO mylutece_database_user_parameter VALUES ('password_history_size', '');
INSERT INTO mylutece_database_user_parameter VALUES ('maximum_number_password_change', '');
INSERT INTO mylutece_database_user_parameter VALUES ('tsw_size_password_change', '');
INSERT INTO mylutece_database_user_parameter VALUES ('use_advanced_security_parameters', 'false');
INSERT INTO mylutece_database_user_parameter VALUES ('account_life_time', '12');
INSERT INTO mylutece_database_user_parameter VALUES ('time_before_alert_account', '30');
INSERT INTO mylutece_database_user_parameter VALUES ('nb_alert_account', '2');
INSERT INTO mylutece_database_user_parameter VALUES ('time_between_alerts_account', '10');
INSERT INTO mylutece_database_user_parameter VALUES ('access_failures_max', '3');
INSERT INTO mylutece_database_user_parameter VALUES ('access_failures_interval', '10');
INSERT INTO mylutece_database_user_parameter VALUES ('expired_alert_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('expired_alert_mail_subject', 'Votre compte a expiré');
INSERT INTO mylutece_database_user_parameter VALUES ('first_alert_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('first_alert_mail_subject', 'Votre compte va bientot expirer');
INSERT INTO mylutece_database_user_parameter VALUES ('other_alert_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('other_alert_mail_subject', 'Votre compte va bientot expirer');
INSERT INTO mylutece_database_user_parameter VALUES ('account_reactivated_mail_sender', 'lutece@nowhere.com');
INSERT INTO mylutece_database_user_parameter VALUES ('account_reactivated_mail_subject', 'Votre compte a bien été réactivé');



ALTER TABLE mylutece_database_user ADD COLUMN reset_password SMALLINT DEFAULT 0 NOT NULL;
ALTER TABLE mylutece_database_user ADD COLUMN password_max_valid_date TIMESTAMP NULL;
ALTER TABLE mylutece_database_user ADD COLUMN account_max_valid_date BIGINT NULL;
ALTER TABLE mylutece_database_user ADD COLUMN nb_alerts_sent INTEGER DEFAULT 0 NOT NULL;

DROP TABLE IF EXISTS mylutece_database_user_password_history;
CREATE  TABLE mylutece_database_user_password_history (
  mylutece_database_user_id INT NOT NULL ,
  password VARCHAR(100) NOT NULL ,
  date_password_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (mylutece_database_user_id, date_password_change)
  );

INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('login', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('name_given', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('name_family', 1);
INSERT INTO mylutece_user_anonymize_field (field_name, anonymize) VALUES ('email', 1);