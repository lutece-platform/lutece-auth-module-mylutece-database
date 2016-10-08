--
-- Table struture for mylutece_database_user
--
DROP TABLE IF EXISTS mylutece_database_user;
CREATE TABLE mylutece_database_user (
  mylutece_database_user_id int NOT NULL,
  login varchar(100) DEFAULT '' NOT NULL,
  password long varchar DEFAULT '' NOT NULL,
  name_given varchar(100) DEFAULT '' NOT NULL,
  name_family varchar(100) DEFAULT '' NOT NULL,
  email varchar(100) DEFAULT NULL,
  is_active SMALLINT DEFAULT 0 NOT NULL,
  reset_password SMALLINT DEFAULT 0 NOT NULL,
  password_max_valid_date TIMESTAMP NULL,
  account_max_valid_date BIGINT NULL,
  nb_alerts_sent INTEGER DEFAULT 0 NOT NULL,
  last_login TIMESTAMP DEFAULT '1980-01-01',
  PRIMARY KEY (mylutece_database_user_id)
);


--
-- Table struture for mylutece_database_user_role
--
DROP TABLE IF EXISTS mylutece_database_user_role;
CREATE TABLE mylutece_database_user_role (
  mylutece_database_user_id int DEFAULT '0' NOT NULL,
  role_key varchar(50) DEFAULT '' NOT NULL,
  PRIMARY KEY  (mylutece_database_user_id,role_key)
);


--
-- Table struture for mylutece_database_user_group
--
DROP TABLE IF EXISTS mylutece_database_user_group;
CREATE TABLE mylutece_database_user_group (
  mylutece_database_user_id int DEFAULT '0' NOT NULL,
  group_key varchar(100) DEFAULT '' NOT NULL,
  PRIMARY KEY  (mylutece_database_user_id,group_key)
);

--
-- Table struture for table mylutece_database_group
--
DROP TABLE IF EXISTS mylutece_database_group;
CREATE TABLE mylutece_database_group (
	group_key varchar(100) default '0' NOT NULL,
	group_description varchar(200) default NULL,
	PRIMARY KEY (group_key)
);

--
-- Table struture for table mylutece_database_group_role
--
DROP TABLE IF EXISTS mylutece_database_group_role;
CREATE TABLE mylutece_database_group_role (
	group_key varchar(100) default '0' NOT NULL,
	role_key varchar(50) default '0' NOT NULL,
	PRIMARY KEY (group_key,role_key)
);

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
-- Table structure for table mylutece_database_key
--
DROP TABLE IF EXISTS mylutece_database_key;
CREATE TABLE mylutece_database_key(
	mylutece_database_user_key VARCHAR(255) NOT NULL,
	mylutece_database_user_id INT DEFAULT 0 NOT NULL,	
	PRIMARY KEY (mylutece_database_user_key)
);

DROP TABLE IF EXISTS mylutece_database_user_password_history;
CREATE  TABLE mylutece_database_user_password_history (
  mylutece_database_user_id INT NOT NULL ,
  password long VARCHAR NOT NULL ,
  date_password_change TIMESTAMP NOT NULL DEFAULT NOW() ,
  PRIMARY KEY (mylutece_database_user_id, date_password_change)
);

