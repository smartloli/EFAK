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

<title>ZK & Kafka - KafkaEagle</title>
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
						ZK & Kafka Info <small>overview</small>
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
						<i class="fa fa-info-circle"></i> <strong>Cluster
							information, in the form of tables to demonstrate the Kafka and
							Zookeeper cluster node IP, port, and its version number</strong> If you
						don't know the usage of Kafka and Zookeeper, you can visit the
						website of <a href="http://kafka.apache.org/" target="_blank"
							class="alert-link">Kafka</a> and <a
							href="http://zookeeper.apache.org/" target="_blank"
							class="alert-link">Zookeeper</a> to view the relevant usage.
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Kafka Cluster Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="kafka_cluster_info">
								<table id="kafka_tab" class="table table-bordered table-hover">
									<thead>
										<tr>
											<th>ID</th>
											<th>IP</th>
											<th>Port</th>
											<th>Created</th>
											<th>Modify</th>
											<th>Version</th>
										</tr>
									</thead>
								</table>
							</div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-sitemap fa-fw"></i> Zookeeper Cluster Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="zookeeper_cluster_info">
								<table id="zk_tab" class="table table-bordered table-hover">
									<thead>
										<tr>
											<th>ID</th>
											<th>IP</th>
											<th>Port</th>
											<th>Mode</th>
										</tr>
									</thead>
								</table>
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
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/cluster/cluster.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
