<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Topic Create - KafkaEagle</title>
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
					<h1 class="mt-4">Topic</h1>
					<ol class="breadcrumb mb-4">
						<li class="breadcrumb-item"><a href="#">ACL</a></li>
						<li class="breadcrumb-item active">CreateGroup</li>
					</ol>
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fas fa-info-circle"></i> <strong>Create a new kafka's topic.</strong>
					</div>
					<!-- content body -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="fas fa-book-open"></i> Topic Property
								</div>
								<div class="card-body">
									<div class="row">
										<div class="col-lg-12">
											<form role="form" action="/acls/createGroup/form" method="post" onsubmit="return contextFormValid();return false;">
												<div class="form-group">
													<label>用户 </label> 
													<select id="ke_user_name" name="ke_user_name"  class="form-control">
													  <option value="">选择用户</option>
													  <option value="gs">GS</option>
													  <option value="cs">CS</option>
													  <option value="bi">BI</option>
													</select>
													<label for="inputError" class="control-label text-danger">
														<i class="fa fa-info-circle"></i> * 选择用户.
													</label>
												</div>
												
												<div class="form-group">
													<label>Group Name (*)</label>
													<input id="ke_group_name" name="ke_group_name" class="form-control" maxlength=50>
													<label for="inputError" class="control-label text-danger">
														<i class="fa fa-info-circle"></i> * 命名组名.
													</label>
												</div>
																								
												<button type="submit" class="btn btn-success">Create</button>
												<div id="create_alert_msg" style="display: none" class="alert alert-danger">
													<label>Error! Please make some changes. (*) is required.</label>
												</div>
											</form>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</main>
			<jsp:include page="../public/plus/footer.jsp"></jsp:include>
		</div>
	</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="main/acl/createGroup.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var ke_group_name = $("#ke_group_name").val();
		var ke_user_name = $("#ke_user_name").val();
		debugger;
		var reg = /^[A-Za-z0-9_-]+$/;
		var digit = /^[0-9]+$/;
		if (ke_group_name.length == 0 || !reg.test(ke_group_name) || ke_user_name.length == 0 || !reg.test(ke_user_name)) {
			$("#create_alert_msg").show();
			setTimeout(function() {
				$("#create_alert_msg").hide()
			}, 3000);
			return false;
		}
		return true;
	}
</script>
</html>