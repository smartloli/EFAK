<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>登录 - 微众银行</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>
<div class="container">
	<div class="row">
		<div class="col-md-4 col-md-offset-4">
			<div class="login-panel panel panel-default">
				<div class="panel-heading">
					<h3 class="panel-title">请登录...</h3>
				</div>
				<div class="panel-body">
					<form role="form">
						<fieldset>
							<div class="form-group">
								<input id="username" class="form-control" placeholder="username"
									name="username" autofocus>
							</div>
							<div class="form-group">
								<input id="password" class="form-control" placeholder="Password"
									name="password" type="password" value="">
							</div>
							<!-- Change this to a button or input when using this as a form -->
							<a href="#" id="submit" class="btn btn-lg btn-success btn-block">登录</a>
							<div id="alert_mssage" style="display: none"
								class="alert alert-danger"></div>
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
	<jsp:param value="main/signin.js" name="loader" />
</jsp:include>
</html>