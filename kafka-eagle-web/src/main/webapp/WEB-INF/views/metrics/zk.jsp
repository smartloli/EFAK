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
.box{
	border-bottom:1px solid #eee;
	margin-bottom:20px;
	margin-top:30px;
	overflow:hidden;
}
.box .left{
	font-size: 36px;
	float:left
}
.box .left small{
	font-size: 24px;
	color:#777
}
.box  .right{
	float:right;
	width: 230px;
	margin-top:20px; 
	background: #fff; 
	cursor: pointer; 
	padding: 5px 10px; 
	border: 1px solid #ccc;
}
</style>

<title>Trend - KafkaEagle</title>
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
					  <p   class="left">
							Zookeeper Monitor <small>details</small>
						</p>
						<div id="reportrange"
						class="right">
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
						<i class="fa fa-info-circle"></i> <strong>Monitor
							Zookeeper transmission, receive packets, connection numbers, file
							open number, respond to client request processing time and other
							performance monitoring .</strong>
					</div>
				</div>
			</div>

			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>Zookeeper
								Send Packets</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="zk_send_packets"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>Zookeeper
								Received Packets</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="zk_recevied_packets"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- <div class="col-lg-6">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>
								Zookeeper Avg Latency </strong>
							<div class="pull-right"></div>
						</div>
						/.panel-heading
						<div class="panel-body">
							<div id="zk_avg_latency"></div>
						</div>
						/.panel-body
					</div>
				</div> -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>
								Zookeeper Alive Connections </strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="zk_alives_connections"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>
								Zookeeper Queue Requests </strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="zk_queue_requests"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- <div class="col-lg-6">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>
								Zookeeper OpenFile Counts </strong>
							<div class="pull-right"></div>
						</div>
						/.panel-heading
						<div class="panel-body">
							<div id="zk_openfile_counts"></div>
						</div>
						/.panel-body
					</div>
				</div> -->
				<!-- /.col-lg-4 -->
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/metrics/zk.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js"
		name="loader" />
</jsp:include>
</html>
