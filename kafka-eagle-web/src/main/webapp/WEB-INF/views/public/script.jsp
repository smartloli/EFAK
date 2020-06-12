<%@ page pageEncoding="UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<script src="/ke/media/js/public/jquery.js" type="text/javascript"></script>
<script src="/ke/media/js/public/bootstrap.min.js"
	type="text/javascript"></script>
<script src="/ke/media/js/public/navbar.js" type="text/javascript"></script>
<script src="/ke/media/js/public/bootstrap-treeview.min.js" type="text/javascript"></script>
<script type="text/javascript">
$("#sidebarToggleOff").on("click", function(e) {
    e.preventDefault();
    $(".side-nav").css('left','0px');
    $("#wrapper").css('padding-left','0px');
    $("#sidebarToggleOn").show();
    $("#sidebarToggleOff").hide();
});
$("#sidebarToggleOn").on("click", function(e) {
    e.preventDefault();
    $(".side-nav").css('left','225px');
    $("#wrapper").css('padding-left','225px');
    $("#sidebarToggleOn").hide();
    $("#sidebarToggleOff").show();
});
</script>
<%
	String[] loader = request.getParameterValues("loader");
	if (loader == null) {
		return;
	}
	for (String s : loader) {
%>
<script src="/ke/media/js/<%=s%>"></script>
<%
	}
%>
