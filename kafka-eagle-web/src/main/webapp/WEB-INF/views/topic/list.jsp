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
<title>Topic List - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css" />
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
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
							<small>list</small>
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
						<strong>List all topic information.</strong>
						<br />
						<i class="fa fa-info-circle"></i>
						<strong>Broker Spread: the higher the coverage, the higher the resource usage of kafka broker nodes.</strong>
						<br />
						<i class="fa fa-info-circle"></i>
						<strong>Broker Skewed: the larger the skewed, the higher the pressure on the broker node of kafka.</strong>
						<br />
						<i class="fa fa-info-circle"></i>
						<strong>Broker Leader Skewed: the higher the leader skewed, the higher the pressure on the kafka broker leader node.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i>
							Topic List Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
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
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<!-- filter topic -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-filter fa-fw"></i>
							Topic Filter
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<div class="form-group">
										<label>Topic Name (*)</label>
										<select multiple="multiple" id="select2val" name="select2val" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_aggrate" name="ke_topic_aggrate" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i>
											Select the topic you need to aggregate .
										</label>
									</div>
									<button id="ke_topic_select_query" class="btn btn-success">Query</button>
								</div>
							</div>
							<!-- /.panel-body -->
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- producer history -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i>
							<strong> Producer Message Aggregate</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="topic_producer_agg" class="charttopicdiv"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_delete" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Notify</h4>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<p>
								Are you sure you want to delete it? Admin Token :
								<input id="ke_admin_token" name="ke_admin_token" style="width: 100px; float: right; margin-right: 150px; margin-top: -5px" class="form-control" placeholder="Enter Token" />
							<p>
						</div>
						<div id="remove_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- modify topic partitions -->
			<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_modify" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Modify</h4>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<p>
								Add Partitions :
								<input id="ke_modify_topic_partition" name="ke_admin_token" style="width: 100%;" class="form-control" placeholder="Partition Numbers" />
								<label for="inputError" class="control-label text-danger">
									<i class="fa fa-info-circle"></i>
									Please enter a positive integer greater than 1 .
								</label>
							<p>
						</div>
						<div id="ke_topic_submit_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- clean topic data -->
			<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_clean" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">Notify</h4>
						</div>
						<!-- /.row -->
						<div id="ke_topic_clean_content" class="modal-body">
						</div>
						<div id="ke_topic_clean_data_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/topic/list.js?v=1.4.6" name="loader" />
	<jsp:param value="plugins/echart/echarts.min.js" name="loader" />
	<jsp:param value="plugins/echart/macarons.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader" />
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
