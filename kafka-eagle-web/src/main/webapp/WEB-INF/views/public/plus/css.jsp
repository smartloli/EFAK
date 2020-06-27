<%@ page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<link href="/media/css/public/plus/common.css" rel="stylesheet"/>
<link href="/media/css/public/plus/custom-color.css" rel="stylesheet"/>
<link rel="shortcut icon" href="/media/img/favicon.ico" />
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