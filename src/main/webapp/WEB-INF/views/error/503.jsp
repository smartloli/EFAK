<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>503 - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>
<div class="container">
	<div class="row">
		<div class="col-md-4 col-md-offset-4">
			<div class="login-panel panel panel-default">
				<div class="panel-heading">
					<h3 class="panel-title">503...</h3>
				</div>
				<div class="panel-body">
					<form role="form">
						<fieldset>
							<h1 class="form-signin-heading">Service Unavailable ~~</h1>
							<!-- Change this to a button or input when using this as a form -->
							<a href="/ke" id="submit" class="btn btn-lg btn-primary btn-block">Return</a>
						</fieldset>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
<body>

</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/error/error.js" name="loader" />
</jsp:include>
</html>