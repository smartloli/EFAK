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

<title>Topic List - KafkaEagle</title>
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
						Topic <small>list</small>
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
						<i class="fa fa-info-circle"></i> <strong>List all topic
							information.</strong> <br /> R: Remove Topic<br /> E: Edit Topic, Such
						as set <strong>log.retention.ms</strong> or <strong>cleanup.policy</strong>
						etc.<br /> D: Describe Topic Property
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Topic List Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<table id="result" class="table table-bordered table-condensed"
								width="100%">
								<thead>
									<tr>
										<th>ID</th>
										<th>Topic Name</th>
										<th>Partition Indexes</th>
										<th>Partition Numbers</th>
										<th>Size(All-Brokers)</th>
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
							<p>
								Are you sure you want to delete it? Admin Token : <input
									id="ke_admin_token" name="ke_admin_token"
									style="width: 100px; float: right; margin-right: 150px; margin-top: -5px"
									class="form-control" placeholder="Enter Token" />
							<p>
						</div>
						<div id="remove_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- edit -->
			<div class="modal fade" aria-labelledby="keEditModalLabel"
				aria-hidden="true" id="edit_topic" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keEditModalLabel">Configure
								Property</h4>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Key</label>
									<div class="col-sm-9">
										<input id="ke_topic_property_key" name="ke_topic_property_key"
											type="text" class="form-control"
											placeholder="log.retention.bytes">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Value</label>
									<div class="col-sm-9">
										<input id="ke_topic_property_value"
											name="ke_topic_property_value" type="text"
											class="form-control" placeholder="-1">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Result</label>
									<div class="col-sm-9">
										<textarea id="ke_topic_setter_result"
											name="ke_topic_setter_result" class="form-control"
											placeholder="" rows="3" style="resize:none" readonly></textarea>
									</div>
								</div>
							</fieldset>
						</div>
						<div id="edit_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- describe -->
			<div class="modal fade" aria-labelledby="keDescModalLabel"
				aria-hidden="true" id="desc_topic" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keDescModalLabel">Describe Topic
								Property</h4>
						</div>
						<!-- /.row -->
						<div class="modal-body">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Describe</label>
									<div class="col-sm-9">
										<textarea id="ke_topic_property_docs"
											name="ke_topic_property_docs" class="form-control"
											placeholder="" rows="10" style="resize:none" readonly></textarea>
									</div>
								</div>
							</fieldset>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/topic/list.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
