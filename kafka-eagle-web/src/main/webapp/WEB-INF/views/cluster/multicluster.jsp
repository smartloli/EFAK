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

<title>Multi-Cluster - KafkaEagle</title>
<jsp:include page="../public/plus/css.jsp"></jsp:include>
<jsp:include page="../public/plus/tcss.jsp"></jsp:include>
</head>

<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
<div id="layoutSidenav">
	<div id="layoutSidenav_nav">
		<jsp:include page="../public/plus/navbar.jsp"></jsp:include>
	</div>
	<div id="layoutSidenav_content">
		<main>
			<div class="container-fluid">
				<h1 class="mt-4">Kafka Multi-Cluster</h1>
				<ol class="breadcrumb mb-4">
					<li class="breadcrumb-item"><a href="#">Kafka Multi-Cluster</a></li>
					<li class="breadcrumb-item active">Overview</li>
				</ol>
				<div class="alert alert-info alert-dismissable">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
					<i class="fas fa-info-circle"></i> <strong>Cluster Support
					multiple Zookeeper under the Kafka cluster display, select
					different Zookeeper to monitor their corresponding Kafka cluster
					state.</strong>
				</div>
				<!-- content body -->
				<div class="row">
					<div class="col-lg-12">
						<div class="card mb-4">
							<div class="card-header">
								<i class="fas fa-server"></i> Kafka Multi-Cluster list
							</div>
							<div class="card-body">
								<div id="kafka_cluster_list" class="table-responsive">
									<table id="cluster_tab" class="table table-bordered table-hover">
										<thead>
										<tr>
											<th>ID</th>
											<th>Cluster Alias</th>
											<th>ZK Host</th>
											<th>Operate</th>
										</tr>
										</thead>
									</table>
								</div>
							</div>
						</div>
					</div>
				</div>
				<!-- Switch -->
				<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_cluster_switch" tabindex="-1" role="dialog">
					<div class="modal-dialog">
						<div class="modal-content">
							<div class="modal-header">
								<h4 class="modal-title" id="keModalLabel">Notify</h4>
								<button class="close" type="button" data-dismiss="modal">x</button>
							</div>
							<!-- /.row -->
							<div class="modal-body">
								<div class="alert alert-danger">Are you sure you want to change it?</div>
							</div>
							<div id="remove_div" class="modal-footer">
							</div>
						</div>
					</div>
				</div>
			</div>
		</main>
		<jsp:include page="../public/plus/footer.jsp"></jsp:include>
	</div>
</div>
	<div id="wrapper">
		<div id="page-wrapper">
			<!-- /.row -->

			<!-- /.row -->
			<!-- /.row -->
			<div class="modal fade" aria-labelledby="keModalLabel"
				aria-hidden="true" id="doc_info" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Notify</h4>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<p>Are you sure you want to change it?
							<p>
						</div>
						<div id="remove_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- /.row -->
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="main/cluster/multicluster.js" name="loader" />
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
