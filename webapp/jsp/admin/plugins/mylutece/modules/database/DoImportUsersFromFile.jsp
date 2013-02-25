<%@page import="fr.paris.lutece.portal.web.pluginaction.DefaultPluginActionResult"%>
<%@ page errorPage="../../../../ErrorPage.jsp" %>

<jsp:useBean id="databaseJspBean" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.DatabaseJspBean" />

<%
	databaseJspBean.init( request, databaseJspBean.RIGHT_MANAGE_DATABASE_USERS ) ;
	DefaultPluginActionResult result = databaseJspBean.doImportUsersFromFile( request );  
	if( result.getHtmlContent( ) == null || "".equals( result.getHtmlContent( ) ) )
	{
	    response.sendRedirect( result.getRedirect( ) );
	}
%>
<jsp:include page="../../../../AdminHeader.jsp"  flush="true" />

<%= result.getHtmlContent( ) %>

<%@ include file="../../../../AdminFooter.jsp" %>
