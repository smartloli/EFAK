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
						Alarm <small>add</small>
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
							the topic being consumed.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Topic Setting
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<form role="form" action="/ke/alarm/add/form" method="post"
										onsubmit="return contextFormValid();return false;">
										<div class="form-group">
											<label>Topic Group (*)</label> <input id="ke_group_alarm"
												name="ke_group_alarm" class="form-control" maxlength=50
												value="1"><input id="ke_group_alarms"
												name="ke_group_alarms" type="hidden"> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the group you need to
												alarm .</label>
										</div>
										<div class="form-group">
											<label>Topic Name (*)</label> <input id="ke_topic_alarm"
												name="ke_topic_alarm" class="form-control" maxlength=50
												value="1"><input id="ke_topic_alarms"
												name="ke_topic_alarms" type="hidden"> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the topic you need to
												alarm .</label>
										</div>
										<div class="form-group">
											<label>Lag Threshold (*)</label> <input id="ke_topic_lag"
												name="ke_topic_lag" class="form-control" maxlength=50
												value="1"> <label for="inputError"
												class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Setting the blocking
												threshold, Parameters must be numeric .</label>
										</div>
										<div class="form-group">
											<label>Owner Email (*)</label> <input id="ke_topic_email"
												name="ke_topic_email" class="form-control" maxlength=50
												value="example1@email.com"><label for="inputError"
												class="control-label text-danger"><i
												class="fa fa-info-circle"></i> To whom the alarm topic
												information, Such as 'example@email.com' .</label>
										</div>
										<button type="submit" class="btn btn-success">Add</button>
										<div id="alert_mssage" style="display: none"
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
	<jsp:param value="main/alarm/add.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var ke_topic_lag = $("#ke_topic_lag").val();
		var ke_topic_email = $("#ke_topic_email").val();
		var ke_topic_alarms = JSON.stringify($('#ke_topic_alarm').magicSuggest().getSelection());
		var ke_group_alarms = JSON.stringify($('#ke_group_alarm').magicSuggest().getSelection());

		if (ke_group_alarms.length == 2) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}
		if (ke_topic_alarms.length == 2) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}
		if (isNaN(ke_topic_lag)) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}
		var reg = /^((([A-Za-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6}\, ))*(([A-Za-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6})))$/;
		if (!reg.test(ke_topic_email)) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}

		$('#ke_topic_alarms').val(ke_topic_alarms);
		$('#ke_group_alarms').val(ke_group_alarms);
		return true;
	}
</script>
</html>
