<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>503 - KafkaEagle</title>
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
					<!-- 404 Error Text -->
					<div class="text-center">
						<div class="error mx-auto" data-text="503">503</div>
						<p class="lead text-gray-800 mb-5">Service Unavailable</p>
						<p class="text-gray-500 mb-0">
							It looks like you found a glitch in the <strong>Kafka Eagle</strong>...
						</p>
						<a href="/" style="text-decoration: none;">&larr; Back to Dashboard</a>
					</div>
				</div>
			</main>
			<jsp:include page="../public/plus/footer.jsp"></jsp:include>
		</div>
	</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="main/error/e503.js" name="loader" />
</jsp:include>
</html>