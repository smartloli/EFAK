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

<title>Consumers - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
<jsp:include page="../public/tcss.jsp"></jsp:include>
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

.links line {
	stroke: #999;
	stroke-opacity: 0.6;
}

.nodes circle {
	stroke: #fff;
	stroke-width: 2.5px;
}
</style>
<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Consumers <small>overview</small>
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
						<i class="fa fa-info-circle"></i> <strong>List all
							consumers information.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-user fa-fw"></i> Consumers Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="kafka_cluster_info">
								<table id="result" class="table table-bordered table-hover">
									<thead>
										<tr>
											<th>ID</th>
											<th>Group</th>
											<th>Topics</th>
											<th>Consumer Numbers</th>
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
							<i class="fa fa-comment fa-fw"></i> Active Topic
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="active_topic"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<!-- /.row -->
			<div class="modal fade" style="background-color: #fff" id="doc_info"
				tabindex="-1" role="dialog">
				<div class="modal-header">
					<button class="close" type="button" data-dismiss="modal">×</button>
					<h3>Details of the consumer group</h3>
				</div>
				<!-- /.row -->
				<div class="row">
					<div class="col-lg-12">
						<div id="consumer_detail_children" class="panel panel-default">
							<div class="panel-heading">
								<i class="fa fa-comment fa-fw"></i> Consumer Topic
								<div class="pull-right"></div>
							</div>
							<!-- /.panel-heading -->
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="plugins/d3/d3.js" name="loader" />
	<jsp:param value="plugins/d3/d3.layout.js" name="loader" />
	<jsp:param value="main/consumers.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
