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

<title>Dashboard - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>
<style type="text/css">
.node circle {
	cursor: pointer;
	fill: #fff;
	stroke: steelblue;
	stroke-width: 1.5px;
}

.node text {
	font-size: 14px;
}

path.link {
	fill: none;
	stroke: #ccc;
	stroke-width: 1.5px;
}

.liquidFillGaugeText {
	font-family: Helvetica;
	font-weight: bold;
}
</style>
<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Dashboard <small>overview</small>
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
						<i class="fa fa-info-circle"></i> <strong>Dashboard
							display topic Kafka related information and Kafka cluster
							information as well as Zookeeper cluster information</strong> If you don't
						know the usage of Kafka and Zookeeper, you can visit the website
						of <a href="http://kafka.apache.org/" target="_blank"
							class="alert-link">Kafka</a> and <a
							href="http://zookeeper.apache.org/" target="_blank"
							class="alert-link">Zookeeper</a> to view the relevant usage.
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
									<i class="fa fa-tasks fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="brokers_count" class="huge">0</div>
									<div>Brokers</div>
								</div>
							</div>
						</div>
						<a href="/ke/cluster/info">
							<div class="panel-footer">
								<span class="pull-left">View Details</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
				<div class="col-lg-3 col-md-6">
					<div class="panel panel-green">
						<div class="panel-heading">
							<div class="row">
								<div class="col-xs-3">
									<i class="fa fa-comment-o fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="topics_count" class="huge">0</div>
									<div>Topics</div>
								</div>
							</div>
						</div>
						<a href="/ke/topic/list">
							<div class="panel-footer">
								<span class="pull-left">View Details</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
				<div class="col-lg-3 col-md-6">
					<div class="panel panel-info">
						<div class="panel-heading">
							<div class="row">
								<div class="col-xs-3">
									<i class="fa fa-sitemap fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="zks_count" class="huge">0</div>
									<div>Zookeepers</div>
								</div>
							</div>
						</div>
						<a href="/ke/cluster/info">
							<div class="panel-footer">
								<span class="pull-left">View Details</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
				<div class="col-lg-3 col-md-6">
					<div class="panel panel-default">
						<div class="panel-heading">
							<div class="row">
								<div class="col-xs-3">
									<i class="fa fa-users fa-5x"></i>
								</div>
								<div class="col-xs-9 text-right">
									<div id="consumers_count" class="huge">0</div>
									<div>ConsumerGroups</div>
								</div>
							</div>
						</div>
						<a href="/ke/consumers">
							<div class="panel-footer">
								<span class="pull-left">View Details</span> <span
									class="pull-right"><i class="fa fa-arrow-circle-right"></i></span>
								<div class="clearfix"></div>
							</div>
						</a>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-4">
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">
								<i class="fa fa-hdd-o fa-fw"></i> Kafka OS Used Memory (%)
							</h3>
						</div>
						<div class="panel-body">
							<div>
								<svg id="fillgauge_kafka_memory" width="97%" height="424"></svg>
							</div>
							<div class="text-right">
								<a href="/ke/metrics/kafka#os_free_memory">View Details <i
									class="fa fa-arrow-circle-right"></i></a>
							</div>
						</div>
					</div>
				</div>
				<div class="col-lg-4">
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">
								<i class="fa fa-file-text-o fa-fw"></i> Topic LogSize Top10
							</h3>
						</div>
						<div class="panel-body">
							<table id="topic_logsize"
								class="table table-bordered table-hover table-striped">
							</table>
							<div class="text-right">
								<a href="/ke/topic/list">View Details <i
									class="fa fa-arrow-circle-right"></i></a>
							</div>
						</div>
					</div>
				</div>
				<div class="col-lg-4">
					<div class="panel panel-default">
						<div class="panel-heading">
							<h3 class="panel-title">
								<i class="fa fa-database fa-fw"></i> Topic Capacity Top10
							</h3>
						</div>
						<div class="panel-body">
							<div class="table-responsive">
								<table id="topic_capacity"
									class="table table-bordered table-hover table-striped">
								</table>
							</div>
							<div class="text-right">
								<a href="/ke/topic/list">View Details <i
									class="fa fa-arrow-circle-right"></i></a>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Kafka Active Brokers
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="kafka_brokers"></div>
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
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="plugins/d3/d3.v3.min.js" name="loader" />
	<jsp:param value="plugins/d3/liquidFillGauge.js" name="loader" />
	<jsp:param value="main/index.js" name="loader" />
</jsp:include>
</html>
