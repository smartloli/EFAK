<%@ page pageEncoding="UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script src="/cms/media/js/public/jquery.min.js" type="text/javascript"></script>
<script src="/cms/media/js/public/bootstrap.min.js"
	type="text/javascript"></script>
<script src="/cms/media/js/public/metisMenu.min.js"
	type="text/javascript"></script>
<script src="/cms/media/js/public/raphael-min.js" type="text/javascript"></script>
<script src="/cms/media/js/public/morris.min.js" type="text/javascript"></script>
<script src="/cms/media/js/public/sb-admin-2.js" type="text/javascript"></script>
<script type="text/javascript" charset="utf-8"
	src="/cms/media/js/plugins/datepicker/moment.js"></script>
<script type="text/javascript" charset="utf-8"
	src="/cms/media/js/plugins/datepicker/daterangepicker.js"></script>
<script type="text/javascript" charset="utf-8"
	src="/cms/media/js/public/jquery.bootstrap-duallistbox.js"></script>	
<%
	String loader = request.getParameter("loader");
%>
<script src="/cms/media/js/<%=loader%>">
	
</script>
