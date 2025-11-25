<%@ page errorPage="../../../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.DatabaseJspBean"%>

${ mylutecedatabase_databaseJspBean.init( pageContext.request, DatabaseJspBean.RIGHT_MANAGE_DATABASE_USERS ) }
${ pageContext.response.sendRedirect( mylutecedatabase_databaseJspBean.doChangeFieldAnonymizeUsers( pageContext.request )) }
