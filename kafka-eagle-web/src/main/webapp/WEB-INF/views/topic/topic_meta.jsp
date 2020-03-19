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

.charttopicdiv {
	width: 100%;
	height: 400px;
}
</style>

<title>Topic Meta - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css" />
</jsp:include>
<jsp:include page="../public/tcss.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<div class="box">
						<p class="left">
							Topic
							<small>meta</small>
						</p>
						<div id="reportrange" class="right">
							<i class="glyphicon glyphicon-calendar fa fa-calendar"></i>
							&nbsp;
							<span></span>
							<b class="caret"></b>
						</div>
					</div>
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i>
						<strong>List all topic meta information. Leader -1 indicates that the partition is not available or is empty.</strong>
						The topic capacity does not include the replica capacity.
						<br />
						<i class="fa fa-info-circle"></i>
						<strong>Preferred Leader: true is preferred leader, false is risked leader.</strong>
						<br />
						<i class="fa fa-info-circle"></i>
						<strong>Under Replicated: false is normal, true is lost replicas.</strong>
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
									<i class="fa fa-file-text-o fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="producer_logsize" class="huge">0</div>
									<div>MSG</div>
								</div>
							</div>
						</div>
						<a>
							<div class="panel-footer">
								<span class="pull-left">LogSize</span>
								<span class="pull-right">
									<i class="fa fa-arrow-circle-right"></i>
								</span>
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
									<i class="fa fa-database fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="producer_topicsize" class="huge">0</div>
									<div id="producer_topicsize_type">B</div>
								</div>
							</div>
						</div>
						<a>
							<div class="panel-footer">
								<span class="pull-left">TopicCapacity</span>
								<span class="pull-right">
									<i class="fa fa-arrow-circle-right"></i>
								</span>
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
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i>
							Topic Meta Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<table id="result" class="table table-bordered table-condensed" width="100%">
								<thead>
									<tr>
										<th>Topic</th>
										<th>Partition</th>
										<th>LogSize</th>
										<th>Leader</th>
										<th>Replicas</th>
										<th>In Sync Replicas</th>
										<th>Preferred Leader</th>
										<th>Under Replicated</th>
									</tr>
								</thead>
							</table>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i>
							Topic MBean
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div>
								<table id="topic_metrics_tab" class="table table-bordered table-hover">
								</table>
							</div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<!-- Producer -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i>
							<strong> Producer Message </strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="topic_producer_msg" class="charttopicdiv"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
			</div>
			<!-- /.row -->
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/topic/topic.meta.js" name="loader" />
	<jsp:param value="plugins/echart/echarts.min.js" name="loader" />
	<jsp:param value="plugins/echart/macarons.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
