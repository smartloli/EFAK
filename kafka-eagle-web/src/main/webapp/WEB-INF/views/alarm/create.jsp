<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Alarm - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
<jsp:include page="../public/tagcss.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Alarm <small>Create</small>
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
						<i class="fa fa-info-circle"></i> <strong>Add an alert to
							the cluster.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Cluster Setting
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<form role="form" action="/ke/alarm/create/form" method="post"
										onsubmit="return contextFormValid();return false;">
										<div class="form-group">
											<label>Type (*)</label> <input id="ke_type_alarm_name"
												name="ke_type_alarm_name" class="form-control" maxlength=50
												value="1"><input id="ke_type_alarm_id"
												name="ke_type_alarm_id" type="hidden"> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the type you need to
												alarm .</label>
										</div>
										<div class="form-group">
											<label>Server (*)</label>
											<textarea id="ke_server_alarm" name="ke_server_alarm"
												class="form-control" rows="3"></textarea>
											<label for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Such as zookeeper:
												dn1:2181,dn2:2181,dn3:2181.</label>
										</div>
										<div class="form-group">
											<label>Owner Email (*)</label> <input id="ke_cluster_email"
												name="ke_cluster_email" class="form-control" maxlength=50
												value="example1@email.com"><label for="inputError"
												class="control-label text-danger"><i
												class="fa fa-info-circle"></i> To whom the alarm topic
												information, Such as 'example@email.com' .</label>
										</div>
										<button type="submit" class="btn btn-success">Add</button>
										<div id="alert_message" style="display: none"
											class="alert alert-danger">
											<label>Oops! Please make some changes . (*) is
												required .</label>
										</div>
									</form>
								</div>
							</div>
							<!-- /.panel-body -->
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /#page-wrapper -->
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="plugins/magicsuggest/magicsuggest.js" name="loader" />
	<jsp:param value="plugins/tokenfield/bootstrap-tokenfield.js"
		name="loader" />
	<jsp:param value="main/alarm/create.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var ke_server_alarm = $("#ke_server_alarm").val();
		var ke_cluster_email = $("#ke_cluster_email").val();
		var ke_type_alarm_name = JSON.stringify($('#ke_type_alarm_name').magicSuggest().getSelection());

		if (ke_type_alarm_name.length == 2) {
			$("#alert_message").show();
			setTimeout(function() {
				$("#alert_message").hide()
			}, 3000);
			return false;
		}
		if (ke_server_alarm.length == 2) {
			$("#alert_message").show();
			setTimeout(function() {
				$("#alert_message").hide()
			}, 3000);
			return false;
		}
		
		var reg = /^((([A-Za-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6}\, ))*(([A-Za-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6})))$/;
		if (!reg.test(ke_cluster_email)) {
			$("#alert_message").show();
			setTimeout(function() {
				$("#alert_message").hide()
			}, 3000);
			return false;
		}

		$('#ke_type_alarm_id').val(ke_type_alarm_name);
		return true;
	}
</script>
</html>
