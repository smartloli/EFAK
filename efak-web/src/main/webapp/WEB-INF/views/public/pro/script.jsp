<%@ page pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Bootstrap bundle JS -->
<script src="/media/js/public/pro/bootstrap.bundle.min.js"></script>
<!--plugins-->
<script src="/media/js/public/pro/jquery.min.js"></script>
<script src="/media/js/plugins/simplebar/simplebar.min.js"></script>
<script src="/media/js/plugins/metismenu/metisMenu.min.js"></script>
<script src="/media/js/plugins/perfect-scrollbar/perfect-scrollbar.js"></script>
<script src="/media/js/public/pro/pace.min.js"></script>

<!-- Dark theme -->
<script src="/media/js/public/pro/navbar.js"></script>

<%
    String[] loader = request.getParameterValues("loader");
    if (loader == null) {
        return;
    }
    for (String s : loader) {
%>
<script src="/media/js/<%=s%>"></script>
<%
    }
%>
