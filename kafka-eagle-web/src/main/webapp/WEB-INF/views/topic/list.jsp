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
	/* margin-top: 30px; */
	overflow: hidden;
}

.box .left {
	font-size: 36px;
	float: left
}

.box .left small {
	/* font-size: 24px;
	color: #777 */

}

.box  .right {
	float: right;
	width: 260px;
	margin-top: -120px;
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
<title>Topic List - KafkaEagle</title>
<jsp:include page="../public/plus/css.jsp">
	<jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css" />
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
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
					<div class="box">
						<h1 class="mt-4">Topic</h1>
						<ol class="breadcrumb mb-4">
							<li class="breadcrumb-item"><a href="#">Topic</a></li>
							<li class="breadcrumb-item active">List</li>
						</ol>
						<div id="reportrange" class="right">
							<i class="glyphicon glyphicon-calendar fa fa-calendar"></i> &nbsp; <span></span> <b class="caret"></b>
						</div>
					</div>
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fas fa-info-circle"></i> <strong>List all topic information.</strong> <br /> <i class="fa fa-info-circle"></i> <strong>Broker Spread: the higher the coverage, the higher the resource usage of kafka broker nodes.</strong> <br /> <i class="fa fa-info-circle"></i> <strong>Broker Skewed: the larger the skewed, the higher the pressure on the broker node of kafka.</strong> <br /> <i class="fa fa-info-circle"></i> <strong>Broker Leader Skewed: the higher the leader skewed, the higher the
							pressure on the kafka broker leader node.</strong>
					</div>
					<!-- content body -->
					<div class="row">
						<div class="col-xl-3 col-md-6 mb-4">
							<div class="card border-left-primary shadow h-100 py-2">
								<div class="card-body">
									<div class="row no-gutters align-items-center">
										<div class="col mr-2">
											<div class="text-xs font-weight-bold text-primary text-uppercase mb-1">APP</div>
											<a id="producer_number" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
										</div>
										<div class="col-auto">
											<i class="fab fa-adn fa-4x text-primary-panel"></i>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="col-xl-3 col-md-6 mb-4 col-md-offset-6">
							<div class="card border-left-success shadow h-100 py-2">
								<div class="card-body">
									<div class="row no-gutters align-items-center">
										<div class="col mr-2">
											<div class="text-xs font-weight-bold text-success text-uppercase mb-1">Total Capacity</div>
											<a id="producer_total_capacity" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
										</div>
										<div class="col-auto">
											<i class="fas fa-database fa-4x text-success-panel"></i>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<!-- topic list -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="fas fa-server"></i> Topic List Info
								</div>
								<div class="card-body">
									<div class="table-responsive">
										<table id="result" class="table table-bordered table-condensed" width="100%">
											<thead>
												<tr>
													<th>ID</th>
													<th>Topic Name</th>
													<th>Partitions</th>
													<th>Broker Spread</th>
													<th>Broker Skewed</th>
													<th>Broker Leader Skewed</th>
													<th>Created</th>
													<th>Modify</th>
													<th>Operate</th>
												</tr>
											</thead>
										</table>
									</div>
								</div>
							</div>
						</div>
					</div>
					<!-- row -->
					<!-- drop topic -->
					<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_delete" tabindex="-1" role="dialog">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<h4 class="modal-title" id="keModalLabel">Notify</h4>
									<button class="close" type="button" data-dismiss="modal">x</button>
								</div>
								<!-- /.row -->
								<div class="modal-body">
									<fieldset class="form-horizontal">
										<div class="form-group">
											<div class="input-group mb-3">
												<div class="input-group-prepend">
													<span class="input-group-text"><i class="fas fa-lock"></i></span>
												</div>
												<input id="ke_admin_token" name="ke_admin_token" type="text" class="form-control" placeholder="Enter Admin Token">
											</div>
										</div>
									</fieldset>
								</div>
								<div id="remove_div" class="modal-footer">
								</div>
							</div>
						</div>
					</div>
					<!-- alter topic partition -->
					<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_modify" tabindex="-1" role="dialog">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<h4 class="modal-title" id="keModalLabel">Modify</h4>
									<button class="close" type="button" data-dismiss="modal">x</button>
								</div>
								<!-- /.row -->
								<div class="modal-body">
									<fieldset class="form-horizontal">
										<div class="form-group">
											<div class="input-group mb-3">
												<div class="input-group-prepend">
													<span class="input-group-text"><i class="fas fa-plus-circle"></i></span>
												</div>
												<input id="ke_modify_topic_partition" name="ke_modify_topic_partition" type="text" class="form-control" placeholder="Enter Partitions (Number >= 1)">
											</div>
										</div>
									</fieldset>
								</div>
								<div id="ke_topic_submit_div" class="modal-footer">
								</div>
							</div>
						</div>
					</div>
					<!-- truncate topic data -->
					<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_clean" tabindex="-1" role="dialog">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<h4 class="modal-title" id="keModalLabel">Notify</h4>
									<button class="close" type="button" data-dismiss="modal">x</button>
								</div>
								<!-- /.row -->
								<div id="ke_topic_clean_content" class="modal-body"></div>
								<div id="ke_topic_clean_data_div" class="modal-footer"></div>
							</div>
						</div>
					</div>
					<!-- row filter -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="fas fa-filter"></i> Topic Filter
								</div>
								<div class="card-body">
									<div class="form-group">
										<label>Topic Name (*)</label>
										<select multiple="multiple" id="select2val" name="select2val" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_aggrate" name="ke_topic_aggrate" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select the topic you need to aggregate .
										</label>
									</div>
									<button id="ke_topic_select_query" class="btn btn-success">Query</button>
								</div>
							</div>
						</div>
					</div>
					<!-- row producer -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="far fa-chart-bar"></i> Producer Message Aggregate
								</div>
								<div class="card-body">
									<div id="topic_producer_agg" class="charttopicdiv"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</main>
			<jsp:include page="../public/plus/footer.jsp"></jsp:include>
		</div>
	</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="main/topic/list.js?v=1.4.9" name="loader" />
	<jsp:param value="plugins/echart/echarts.min.js" name="loader" />
	<jsp:param value="plugins/echart/macarons.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader" />
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
