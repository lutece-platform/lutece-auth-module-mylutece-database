<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp"  flush="true" />

<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.DatabaseJspBean"%>

${ mylutecedatabase_databaseJspBean.init( pageContext.request, DatabaseJspBean.RIGHT_MANAGE_DATABASE_USERS ) }
${ mylutecedatabase_databaseJspBean.getExportUsers( pageContext.request ) }

<%@ include file="../../../../AdminFooter.jsp" %>