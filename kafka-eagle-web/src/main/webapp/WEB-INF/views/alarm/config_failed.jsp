<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>Failed - KafkaEagle</title>
<jsp:include page="../public/plus/css.jsp"></jsp:include>
</head>

<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
<div id="layoutSidenav">
	<div id="layoutSidenav_nav">
		<jsp:include page="../public/plus/navbar.jsp"></jsp:include>
	</div>
	<div id="layoutSidenav_content">
		<main>
			<div class="container-fluid">
				<h1 class="mt-4">Channel</h1>
				<ol class="breadcrumb mb-4">
					<li class="breadcrumb-item"><a href="#">Channel</a></li>
					<li class="breadcrumb-item active">Config</li>
				</ol>
				<div class="alert alert-danger alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
					<i class="fa fa-info-circle"></i> <strong>${Alarm_Config_Status}</strong> <a class="btn btn-large btn-primary" href="/alarm/config"> <span class="ui-button-text">Create Again</span>
				</a>
				</div>
			</div>
		</main>
		<jsp:include page="../public/plus/footer.jsp"></jsp:include>
	</div>
</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="main/topic/add.result.js" name="loader" />
</jsp:include>
</html>