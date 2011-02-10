--
-- Table struture for mylutece_database_user
--
DROP TABLE IF EXISTS mylutece_database_user;
CREATE TABLE mylutece_database_user (
  mylutece_database_user_id int NOT NULL,
  login varchar(100) DEFAULT '' NOT NULL,
  password varchar(100) DEFAULT '' NOT NULL,
  name_given varchar(100) DEFAULT '' NOT NULL,
  name_family varchar(100) DEFAULT '' NOT NULL,
  email varchar(100) DEFAULT NULL,
  PRIMARY KEY  (mylutece_database_user_id)
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
