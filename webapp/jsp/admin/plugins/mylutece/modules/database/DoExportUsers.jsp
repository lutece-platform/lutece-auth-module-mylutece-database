<%@page import="fr.paris.lutece.portal.web.pluginaction.DefaultPluginActionResult"%>
<%@ page errorPage="../../../../ErrorPage.jsp" %>

<jsp:useBean id="databaseJspBean" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.DatabaseJspBean" />

<%
	databaseJspBean.init( request, databaseJspBean.RIGHT_MANAGE_DATABASE_USERS ) ;
	DefaultPluginActionResult result = databaseJspBean.doExportUsers( request, response );  
	if( result != null && result.getRedirect( ) != null && !"".equals( result.getRedirect( ) ) && ( result.getHtmlContent( ) == null || "".equals( result.getHtmlContent( ) ) ) )
	{
	    response.sendRedirect( result.getRedirect( ) );
	}
	else if ( result != null && result.getHtmlContent( ) != null && "".equals( result.getHtmlContent( ) ) )
	{
%>
<jsp:include page="../../../../AdminHeader.jsp"  flush="true" />

<%= result.getHtmlContent( ) %>

<%@ include file="../../../../AdminFooter.jsp" %>
<%
	}
%>