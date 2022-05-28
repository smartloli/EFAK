<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--plugins-->
<link href="/media/css/plugins/simplebar/simplebar.css" rel="stylesheet"/>
<link href="/media/css/plugins/perfect-scrollbar/perfect-scrollbar.css" rel="stylesheet"/>
<link href="/media/css/plugins/metismenu/metisMenu.min.css" rel="stylesheet"/>
<!-- Bootstrap CSS -->
<link href="/media/css/public/pro/bootstrap.min.css" rel="stylesheet"/>
<link href="/media/css/public/pro/bootstrap-extended.css" rel="stylesheet"/>
<link href="/media/css/public/pro/style.css" rel="stylesheet"/>
<link href="/media/css/public/pro/icons.css" rel="stylesheet"/>
<link rel="stylesheet" href="/media/css/public/pro/bootstrap-icons.css">


<!-- loader-->
<link href="/media/css/public/pro/pace.min.css" rel="stylesheet"/>

<!--Theme Styles-->
<link href="/media/css/public/pro/dark-theme.css" rel="stylesheet"/>

<%
    String[] loader = request.getParameterValues("css");
    if (loader == null) {
        return;
    }
    for (String s : loader) {
%>
<link href="/media/css/<%=s%>" rel="stylesheet"/>
<%
    }
%>