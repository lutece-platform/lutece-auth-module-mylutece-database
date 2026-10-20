-- liquibase formatted sql
-- changeset mylutece-database:update_db_core_mylutece_database-7.0.0-7.0.1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- LUT-34096 : the e-mails stored as templates write the link to the site from site_name and site_url (MailService.getSiteUrl):
-- site_link, HTML built in Java, is no longer in their model
UPDATE core_template SET template_value = REPLACE( template_value, '${site_link!}', '<#if site_url??><a title="${site_name!}" href="${site_url}">${site_name!}</a></#if>' ) WHERE template_value LIKE '%${site_link!}%';
UPDATE core_template SET template_value = REPLACE( template_value, '${site_link}', '<#if site_url??><a title="${site_name!}" href="${site_url}">${site_name!}</a></#if>' ) WHERE template_value LIKE '%${site_link}%';
