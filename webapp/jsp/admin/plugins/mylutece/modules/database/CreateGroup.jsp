<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<jsp:useBean id="group" scope="session" class="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.GroupJspBean" />

<% group.init( request, group.RIGHT_GROUPS_MANAGEMENT ); %>
<%= group.getCreateGroup( request ) %>

<%@ include file="../../../../AdminFooter.jsp" %>