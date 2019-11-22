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

<title>Alarm - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Config <small>message</small>
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
						<i class="fa fa-info-circle"></i> <strong>Choose
							different warning types to warn consumers and cluster health.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-cogs fa-fw"></i> Content
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<form role="form" action="/ke/alarm/config/storage/form/"
								method="post"
								onsubmit="return contextAlarmConfigFormValid();return false;">
								<div class="form-group">
									<label>Alarm Group Name (*)</label> <input
										id="ke_alarm_group_name" name="ke_alarm_group_name"
										class="form-control"
										placeholder="Alarm Group Name Limit 64 words" maxlength="64">
									<label for="inputError" class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Input the alarm group name .</label>
								</div>
								<div class="form-group">
									<label>Alarm Type (*)</label> <select id="select2val"
										name="select2val" tabindex="-1"
										style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
									<input id="ke_alarm_type" name="ke_alarm_type" type="hidden" />
									<label for="inputError" class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Select the type you need to
										alarm .</label>
								</div>
								<div class="form-group">
									<label>Alarm URL (*)</label> <input id="ke_alarm_url"
										name="ke_alarm_url" class="form-control"
										placeholder="Enter Alarm URL"> <label for="inputError"
										class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Input the alarm url .</label>
								</div>
								<div id="div_alarm_http" style="display: none"
									class="form-group">
									<label>Http Method</label> <br /> <label class="radio-inline">
										<input type="radio" name="ke_alarm_http"
										id="ke_alarm_http_get" value="get">GET
									</label><label class="radio-inline"> <input type="radio"
										name="ke_alarm_http" id="ke_alarm_http_post" value="post" checked="">POST
									</label> <br /> <label for="inputError"
										class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Select the alarm http method .</label>
								</div>
								<div id="div_alarm_address" style="display: none"
									class="form-group">
									<label>Address (*)</label> <input id="ke_alarm_address"
										name="ke_alarm_address" class="form-control" placeholder="">
									<label for="inputError" class="control-label text-danger"><i
										class="fa fa-info-circle"></i> You can enter multiple email or
										im addresses using a ";" separator .</label>
								</div>
								<div class="form-group">
									<label>Test Message</label>
									<textarea id="ke_test_msg" name="ke_test_msg"
										class="form-control" placeholder="Limit 200 words." rows="4"
										maxlength="200"></textarea>
									<label for="inputError" class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Write something and send
										message to owner .</label>
								</div>
								<button type="submit" class="btn btn-success" id="btn_send">Save
								</button>
								<button type="button" class="btn btn-primary" id="btn_send_test">Send
									Test</button>
								<div id="alert_msg_alarm_config" style="display: none"
									class="alert alert-danger">
									<label>Oops! Please make some changes . (*) is required
										.</label>
								</div>
								<div id="alert_msg_alarm_send" style="display: none"
									class="alert alert-danger">
									<label>Oops! Alarm type, url and message is required.</label>
								</div>
								<div id="success_mssage_alarm_config" style="display: none"
									class="alert alert-success">
									<label>Message sent success .</label>
								</div>
								<div id="failed_mssage_alarm_config" style="display: none"
									class="alert alert-danger">
									<label>Message sent failed .</label>
								</div>
							</form>
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
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
	<jsp:param value="main/alarm/config.js" name="loader" />
</jsp:include>

<script type="text/javascript">
	function contextAlarmConfigFormValid() {
		var ke_alarm_group_name = $("#ke_alarm_group_name").val();
		var ke_alarm_type = $("#ke_alarm_type").val();
		var ke_alarm_url = $("#ke_alarm_url").val();
		var ke_alarm_http = $("input:radio:checked").val(); // option
		var ke_alarm_address = $("#ke_alarm_address").val();

		if (ke_alarm_group_name.length == 0 || ke_alarm_type.length == 0 || ke_alarm_url.length == 0) {
			$("#alert_msg_alarm_config").show();
			setTimeout(function() {
				$("#alert_msg_alarm_config").hide()
			}, 3000);
			return false;
		}

		return true;
	}
</script>
</html>
