<jsp:useBean id="myluteceDatabaseApp" scope="request" class="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.MyLuteceDatabaseApp" />

<%
	response.sendRedirect( myluteceDatabaseApp.doCreateAccount( request ) );
%>
