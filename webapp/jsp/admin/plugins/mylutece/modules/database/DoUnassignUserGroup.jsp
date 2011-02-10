<%@ page errorPage="../../../../ErrorPage.jsp" %>

<jsp:useBean id="group" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.GroupJspBean" />

<%
	group.init( request, group.RIGHT_GROUPS_MANAGEMENT );
   	response.sendRedirect( group.doUnAssignUserGroup( request ) ); 
%>
