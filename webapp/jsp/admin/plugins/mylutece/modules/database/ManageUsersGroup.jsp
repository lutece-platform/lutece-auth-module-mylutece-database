<%@ page errorPage="../../../../ErrorPage.jsp" %>
<jsp:include page="../../../../AdminHeader.jsp" />

<%@page import="fr.paris.lutece.plugins.mylutece.modules.database.authentication.web.GroupJspBean"%>

${ mylutecedatabase_groupJspBean.init( pageContext.request, GroupJspBean.RIGHT_GROUPS_MANAGEMENT ) }
${ mylutecedatabase_groupJspBean.getManageUsersGroup( pageContext.request ) }

<%@ include file="../../../../AdminFooter.jsp" %>
