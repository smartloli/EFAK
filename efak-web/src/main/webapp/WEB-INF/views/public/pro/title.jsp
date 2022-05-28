<%@ page pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Required meta tags -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="shortcut icon" href="/media/img/favicon.ico"/>

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
