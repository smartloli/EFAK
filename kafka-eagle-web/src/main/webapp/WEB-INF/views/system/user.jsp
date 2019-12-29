<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html lang="zh">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>User - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
<jsp:include page="../public/tcss.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						User System Manager <small>overview</small>
					</h1>
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert"
							aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i> <strong>Assign roles
							and display to users.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-cogs fa-fw"></i> User Manager
							<div class="pull-right">
								<button id="ke-add-user-btn" type="button"
									class="btn btn-primary btn-xs">Add</button>
							</div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<table id="result" class="table table-bordered table-condensed"
								width="100%">
								<thead>
									<tr>
										<th>RtxNo</th>
										<th>UserName</th>
										<th>RealName</th>
										<th>Email</th>
										<th>Password</th>
										<th>Operate</th>
									</tr>
								</thead>
							</table>
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- add modal -->
			<div class="modal fade" aria-labelledby="keModalLabel"
				aria-hidden="true" id="ke_user_add_dialog" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Add User</h4>
						</div>
						<!-- /.row -->
						<form role="form" action="/ke/system/user/add/" method="post"
							onsubmit="return contextFormValid();return false;">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">RtxNo</label>
									<div class="col-sm-9">
										<input id="ke_rtxno_name" name="ke_rtxno_name" type="text"
											class="form-control" placeholder="1000">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">RealName</label>
									<div class="col-sm-9">
										<input id="ke_real_name" name="ke_real_name" type="text"
											class="form-control" placeholder="kafka-eagle">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">UserName</label>
									<div class="col-sm-9">
										<input id="ke_user_name" name="ke_user_name" type="text"
											class="form-control" placeholder="kafka-eagle">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Email</label>
									<div class="col-sm-9">
										<input id="ke_user_email" name="ke_user_email" type="text"
											class="form-control" placeholder="kafka-eagle@email.com">
									</div>
								</div>
								<div id="alert_mssage_add" style="display: none"
									class="alert alert-danger">
									<label id="alert_mssage_add_label"></label>
								</div>
							</fieldset>

							<div id="remove_div" class="modal-footer">
								<button type="button" class="btn btn-default"
									data-dismiss="modal">Cancle</button>
								<button type="submit" class="btn btn-primary" id="create-add">Submit
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
			<!-- modal setting -->
			<div class="modal fade" aria-labelledby="keModalLabel"
				aria-hidden="true" id="ke_setting_dialog" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">User Setting</h4>
							<div id="alert_mssage_info"></div>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<div id="ke_role_list"></div>
						</div>
						<div id="remove_div" class="modal-footer">
							<button type="button" class="btn btn-primary"
								data-dismiss="modal">Close</button>
						</div>
					</div>
				</div>
			</div>
			<!-- modal modify -->
			<div class="modal fade" aria-labelledby="keModalLabel"
				aria-hidden="true" id="ke_user_modify_dialog" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Modify User</h4>
						</div>
						<!-- /.row -->
						<form role="form" action="/ke/system/user/modify/" method="post"
							onsubmit="return contextModifyFormValid();return false;">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">RtxNo</label>
									<div class="col-sm-9">
										<input id="ke_user_id_modify" name="ke_user_id_modify"
											type="hidden" class="form-control" placeholder="1000">
										<input id="ke_rtxno_name_modify" name="ke_rtxno_name_modify"
											type="text" class="form-control" disabled placeholder="1000">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">RealName</label>
									<div class="col-sm-9">
										<input id="ke_real_name_modify" name="ke_real_name_modify"
											type="text" class="form-control" placeholder="kafka-eagle">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">UserName</label>
									<div class="col-sm-9">
										<input id="ke_user_name_modify" name="ke_user_name_modify"
											type="text" class="form-control" placeholder="kafka-eagle">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Email</label>
									<div class="col-sm-9">
										<input id="ke_user_email_modify" name="ke_user_email_modify"
											type="text" class="form-control"
											placeholder="kafka-eagle@email.com">
									</div>
								</div>
								<div id="alert_mssage_modify" style="display: none"
									class="alert alert-danger">
									<label id="alert_mssage_modify_label"></label>
								</div>
							</fieldset>

							<div id="remove_div" class="modal-footer">
								<button type="button" class="btn btn-default"
									data-dismiss="modal">Cancle</button>
								<button type="submit" class="btn btn-primary" id="create-modify">Submit
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
			<!-- Reset -->
			<div class="modal fade" aria-labelledby="keUserModalLabel"
				aria-hidden="true" id="ke_user_reset_dialog" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keUserModalLabel">Reset User
								Password</h4>
						</div>
						<!-- /.row -->
						<form role="form" action="/ke/system/user/reset/" method="post"
							onsubmit="return contextResetFormValid();return false;">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">New</label>
									<div class="col-sm-9">
										<input id="ke_user_new_pwd_reset" name="ke_user_new_pwd_reset"
											type="password" class="form-control"
											placeholder="New Password"> <input
											id="ke_user_rtxno_reset" name="ke_user_rtxno_reset" type="hidden"
											class="form-control">
									</div>
								</div>
								<div id="alert_mssage_reset" style="display: none"
									class="alert alert-danger">
									<label id="alert_mssage_reset_label"></label>
								</div>
							</fieldset>

							<div id="remove_div" class="modal-footer">
								<button type="button" class="btn btn-default"
									data-dismiss="modal">Cancle</button>
								<button type="submit" class="btn btn-primary" id="create-modify">Submit
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
			<!-- /#page-wrapper -->
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/system/user.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var ke_rtxno_name = $("#ke_rtxno_name").val();
		var ke_real_name = $("#ke_real_name").val();
		var ke_user_name = $("#ke_user_name").val();
		var ke_user_email = $("#ke_user_email").val();
		if (ke_real_name == "Administrator" || ke_user_name == "admin") {
			$("#alert_mssage_add").show();
			$("#alert_mssage_add_label").text("Oops! Administrator or admin is not available.");
			setTimeout(function() {
				$("#alert_mssage_add").hide()
			}, 3000);
			return false;
		}
		if (ke_rtxno_name.length == 0 || ke_real_name.length == 0 || ke_user_name.length == 0 || ke_user_email.length == 0) {
			$("#alert_mssage_add").show();
			$("#alert_mssage_add_label").text("Oops! Please enter the complete information.");
			setTimeout(function() {
				$("#alert_mssage_add").hide()
			}, 3000);
			return false;
		}

		return true;
	}

	function contextModifyFormValid() {
		var ke_rtxno_name_modify = $("#ke_rtxno_name_modify").val();
		var ke_real_name_modify = $("#ke_real_name_modify").val();
		var ke_user_name_modify = $("#ke_user_name_modify").val();
		var ke_user_email_modify = $("#ke_user_email_modify").val();

		if (ke_real_name_modify == "Administrator" || ke_user_name_modify == "admin") {
			$("#alert_mssage_modify").show();
			$("#alert_mssage_modify_label").text("Oops! Administrator or admin is not available.");
			setTimeout(function() {
				$("#alert_mssage_modify").hide()
			}, 3000);
			return false;
		}

		if (ke_rtxno_name_modify.length == 0 || ke_real_name_modify.length == 0 || ke_user_name_modify.length == 0 || ke_user_email_modify.length == 0) {
			$("#alert_mssage_modify").show();
			$("#alert_mssage_modify_label").text("Oops! Please enter the complete information.");
			setTimeout(function() {
				$("#alert_mssage_modify").hide()
			}, 3000);
			return false;
		}

		return true;
	}

	function contextResetFormValid() {
		var ke_user_new_pwd_reset = $("#ke_user_new_pwd_reset").val();
		var userResetRegular = /[\u4E00-\u9FA5]/;
		if (ke_user_new_pwd_reset.length == 0 || userResetRegular.test(ke_user_new_pwd_reset)) {
			$("#alert_mssage_reset").show();
			$("#alert_mssage_reset_label").text("Passwords can only be number and letters or special symbols .");
			setTimeout(function() {
				$("#alert_mssage_reset").hide()
			}, 3000);
			return false;
		}

		return true;
	}
</script>
</html>
