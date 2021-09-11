<%@ page pageEncoding="UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script src="/media/js/public/plus/jquery-3.4.1.min.js" type="text/javascript"></script>
<script src="/media/js/public/plus/bootstrap.bundle.min.js" type="text/javascript"></script>
<script src="/media/js/public/plus/navbar.js" type="text/javascript"></script>
<script src="/media/js/public/plus/all.min.js" type="text/javascript"></script>

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
