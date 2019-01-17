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
<style>
.box {
	border-bottom: 1px solid #eee;
	margin-bottom: 20px;
	margin-top: 30px;
	overflow: hidden;
}

.box .left {
	font-size: 36px;
	float: left
}

.box .left small {
	font-size: 24px;
	color: #777
}

.box  .right {
	float: right;
	width: 230px;
	margin-top: 20px;
	background: #fff;
	cursor: pointer;
	padding: 5px 10px;
	border: 1px solid #ccc;
}
</style>

<title>Offsets - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/datatimepicker/daterangepicker.css"
		name="css" />
</jsp:include>
</head>
<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<div class="box">
						<p class="left">
							Offsets Realtime <small>details</small>
						</p>
						<div id="reportrange" class="right">
							<i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp;
							<span></span> <b class="caret"></b>
						</div>
					</div>

				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert"
							aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i> <strong>List the
							current consumers's application realtime offsets of topic. Observe whether there is blocking by the trend of lag value.</strong>
					</div>
				</div>
			</div>

			<!-- /.row -->
			<div class="row">
				<div class="col-lg-3 col-md-6">
					<div class="panel panel-primary">
						<div class="panel-heading">
							<div class="row">
								<div class="col-xs-3">
									<i class="fa fa-sign-in fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="producer_rate" class="huge">0</div>
									<div>bytes / s</div>
								</div>
							</div>
						</div>
						<a>
							<div class="panel-footer">
								<span class="pull-left">Producer Rates</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
				<!-- row -->
				<div class="col-lg-3 col-md-6 col-md-offset-6">
					<div class="panel panel-green">
						<div class="panel-heading">
							<div class="row">
								<div class="col-xs-3">
									<i class="fa fa-sign-out fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="consumer_rate" class="huge">0</div>
									<div>bytes / s</div>
								</div>
							</div>
						</div>
						<a>
							<div class="panel-footer">
								<span class="pull-left">Consumer Rates</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div id="topic_name_header" class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>{TopicName}</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div id="offset_topic_realtime" class="panel-body">
							<div id="morris-area-chart"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/consumer/offset.realtime.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js"
		name="loader" />
</jsp:include>
</html>
