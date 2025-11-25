<%@ page errorPage="../../../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.GroupJspBean"%>

${ mylutecedatabase_groupJspBean.init( pageContext.request, GroupJspBean.RIGHT_GROUPS_MANAGEMENT ) }
${ pageContext.response.sendRedirect( mylutecedatabase_groupJspBean.doRemoveGroup( pageContext.request )) }
