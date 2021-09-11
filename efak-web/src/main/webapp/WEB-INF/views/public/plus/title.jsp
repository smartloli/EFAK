<%@ page pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    String[] loader = request.getParameterValues("loader");
    if (loader == null) {
        return;
    }
    for (String s : loader) {
%>
<title><%=s%> - EFAK</title>
<%
    }
%>
