<%@page import="jakarta.inject.Inject"%>
<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.MyLuteceDatabaseApp"%>

<%! @Inject private MyLuteceDatabaseApp myLuteceDatabaseApp; %>
<%
	response.sendRedirect( myLuteceDatabaseApp.doSendLogin( request ) );
%>