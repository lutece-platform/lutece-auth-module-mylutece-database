<%@ page errorPage="../../../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.DatabaseJspBean"%>

${ mylutecedatabase_databaseJspBean.init( pageContext.request, DatabaseJspBean.RIGHT_MANAGE_DATABASE_USERS ) }

${ pageContext.setAttribute( 'pluginActionResult', mylutecedatabase_databaseJspBean.doImportUsersFromFile( pageContext.request ) ) }
${ not empty pageContext.getAttribute( 'pluginActionResult' ).redirect ? pageContext.response.sendRedirect( pageContext.getAttribute( 'pluginActionResult' ).redirect ) : '' }

<jsp:include page="../../../../AdminHeader.jsp"  flush="true" />

${ pageContext.getAttribute( 'pluginActionResult' ).htmlContent }

<%@ include file="../../../../AdminFooter.jsp" %>
