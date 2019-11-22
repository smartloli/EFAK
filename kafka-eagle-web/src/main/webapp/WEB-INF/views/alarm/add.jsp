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
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
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
							<i class="fa fa-tasks fa-fw"></i> Consumer Setting
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<form role="form" action="/ke/alarm/add/form" method="post"
										onsubmit="return contextConsumerFormValid();return false;">
										<div class="form-group">
											<label>Consumer Group (*)</label> <select
												id="select2consumergroup" name="select2consumergroup"
												tabindex="-1"
												style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
											<input id="ke_alarm_consumer_group"
												name="ke_alarm_consumer_group" type="hidden" /><label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the consumer group you
												need to alarm .</label>
										</div>
										<div class="form-group">
											<label>Consumer Topic (*)</label>
											<div id="div_select_consumer_topic"></div>
											<input id="ke_alarm_consumer_topic"
												name="ke_alarm_consumer_topic" type="hidden" /><label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the consumer topic you
												need to alarm .</label>
										</div>
										<div class="form-group">
											<label>Lag Threshold (*)</label> <input id="ke_topic_lag"
												name="ke_topic_lag" class="form-control" maxlength=50
												value="1"> <label for="inputError"
												class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Setting the lag threshold,
												input must be numeric .</label>
										</div>
										<div class="form-group">
											<label>Alarm Level (*)</label> <select id="select2level"
												name="select2level" tabindex="-1"
												style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
											<input id="ke_alarm_cluster_level"
												name="ke_alarm_cluster_level" type="hidden" /> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the cluster level you
												need to alarm .</label>
										</div>
										<div class="form-group">
											<label>Alarm Max Times (*)</label> <select
												id="select2maxtimes" name="select2maxtimes" tabindex="-1"
												style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
											<input id="ke_alarm_cluster_maxtimes"
												name="ke_alarm_cluster_maxtimes" type="hidden" /> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the cluster alarm max
												times you need to alarm .</label>
										</div>
										<div class="form-group">
											<label>Alarm Group (*)</label> <select id="select2group"
												name="select2group" tabindex="-1"
												style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
											<input id="ke_alarm_cluster_group"
												name="ke_alarm_cluster_group" type="hidden" /> <label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Select the cluster alarm
												group you need to alarm .</label>
										</div>
										<button type="submit" class="btn btn-success">Add</button>
										<div id="alert_consumer_message" style="display: none"
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
	<jsp:param value="main/alarm/add.js" name="loader" />
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextConsumerFormValid() {
		var ke_alarm_consumer_group = $("#ke_alarm_consumer_group").val();
		var ke_alarm_consumer_topic = $("#ke_alarm_consumer_topic").val();
		var ke_topic_lag = $("#ke_topic_lag").val();
		var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();
		var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();
		var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();

		if (ke_alarm_consumer_group.length == 0 || ke_alarm_consumer_topic.length == 0 || ke_topic_lag.length == 0 || ke_alarm_cluster_level.length == 0 || ke_alarm_cluster_maxtimes.length == 0 || ke_alarm_cluster_group.length == 0) {
			$("#alert_consumer_message").show();
			setTimeout(function() {
				$("#alert_consumer_message").hide()
			}, 3000);
			return false;
		}
		
		if (isNaN(ke_topic_lag)) {
			$("#alert_consumer_message").show();
			setTimeout(function() {
				$("#alert_consumer_message").hide()
			}, 3000);
			return false;
		}

		return true;
	}
</script>
</html>
