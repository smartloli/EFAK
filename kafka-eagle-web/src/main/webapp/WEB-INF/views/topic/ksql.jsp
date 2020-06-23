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

<title>Topic Message - KafkaEagle</title>
<jsp:include page="../public/plus/css.jsp"></jsp:include>
<jsp:include page="../public/plus/tcss.jsp"></jsp:include>
</head>
<style>
.CodeMirror {
	border-top: 1px solid #ddd;
	border-bottom: 1px solid #ddd;
	border-right: 1px solid #ddd;
	border-left: 1px solid #ddd;
}
</style>
<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
	<div id="layoutSidenav">
		<div id="layoutSidenav_nav">
			<jsp:include page="../public/plus/navbar.jsp"></jsp:include>
		</div>
		<div id="layoutSidenav_content">
			<main>
				<div class="container-fluid">
					<h1 class="mt-4">Topic</h1>
					<ol class="breadcrumb mb-4">
						<li class="breadcrumb-item"><a href="#">Topic</a></li>
						<li class="breadcrumb-item active">KSQL</li>
					</ol>
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fas fa-info-circle"></i> Sample <a href="http://www.kafka-eagle.org/articles/docs/quickstart/ksql.html" target="_blank">KSQL</a> query: <strong>select
							* from ke_topic where `partition` in (0,1,2) limit 10</strong><br /> <i
							class="fas fa-info-circle"></i> AutoComplete: Press <strong>Alt
							and /</strong>.
					</div>
					<!-- content body -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="fas fa-book-open"></i> Topic Property
								</div>
								<div class="card-body">
									<div class="row">
										<div class="col-lg-12">
											<form role="form" action="/topic/create/form" method="post" onsubmit="return contextFormValid();return false;">
												<div class="form-group">
													<label>Topic Name (*)</label>
													<input id="ke_topic_name" name="ke_topic_name" class="form-control" maxlength=50>
													<label for="inputError" class="control-label text-danger">
														<i class="fa fa-info-circle"></i> Made up of letters and digits or underscores . Such as "demo_kafka_topic_1" .
													</label>
												</div>
												<div class="form-group">
													<label>Partitions (*)</label>
													<input id="ke_topic_partition" name="ke_topic_partition" class="form-control" maxlength=50 value="1">
													<label for="inputError" class="control-label text-danger">
														<i class="fa fa-info-circle"></i> Partition parameters must be numeric .
													</label>
												</div>
												<div class="form-group">
													<label>Replication Factor (*)</label>
													<input id="ke_topic_repli" name="ke_topic_repli" class="form-control" maxlength=50 value="1">
													<label for="inputError" class="control-label text-danger">
														<i class="fa fa-info-circle"></i> Replication Factor parameters must be numeric . Pay attention to available brokers must be larger than replication factor .
													</label>
												</div>
												<button type="submit" class="btn btn-success">Create</button>
												<div id="create_alert_msg" style="display: none" class="alert alert-danger">
													<label>Error! Please make some changes. (*) is required.</label>
												</div>
											</form>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</main>
			<jsp:include page="../public/plus/footer.jsp"></jsp:include>
		</div>
	</div>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Topic <small>message</small>
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
						<i class="fa fa-info-circle"></i> Sample SQL query: <strong>select
							* from ke_topic where `partition` in (0,1,2) limit 10</strong><br /> <i
							class="fa fa-info-circle"></i> AutoComplete: Press <strong>Alt
							and /</strong>.
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Kafka Query SQL
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="ke_sql_query">
								<form>
									<textarea id="code" name="code"></textarea>
								</form>
								<a name="run_task" class="btn btn-success">Query</a>
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
							<i class="fa fa-comments fa-fw"></i> Tasks Job Info
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div>
								<ul id="result_tab" class="nav nav-tabs">
									<li class="active"><a href="#log_textarea"
										data-toggle="tab">Logs</a></li>
									<li><a href="#result_textarea" data-toggle="tab">Result</a></li>
									<li><a href="#ksql_history_textarea" data-toggle="tab">History</a></li>
								</ul>
							</div>
							<div class="tab-content">
								<div id="log_textarea" class="tab-pane fade in active">
									<form>
										<textarea id="job_info" name="job_info"></textarea>
									</form>
								</div>
								<div id="result_textarea" class="tab-pane fade"></div>
								<div id="ksql_history_textarea" class="tab-pane fade">
									<div id="ksql_history_result_div">
										<div id="ksql_history_result0">
											<table id="ksql_history_result"
												class="table table-bordered table-condensed" width="100%">
												<thead>
													<tr>
														<th>ID</th>
														<th>User</th>
														<th>Host</th>
														<th>KSQL</th>
														<th>Status</th>
														<th>Spent</th>
														<th>Created</th>
													</tr>
												</thead>
											</table>
										</div>
									</div>
								</div>
							</div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
			<!-- More then detail -->
			<div class="modal fade" aria-labelledby="keModalLabelModify"
				aria-hidden="true" id="ke_sql_query_detail" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabelModify">Detail</h4>
						</div>
						<!-- /.row -->
						<fieldset class="form-horizontal">
							<div class="form-group">
								<label for="path" class="col-sm-2 control-label">Content</label>
								<div class="col-sm-9">
									<textarea id="ke_sql_query_content" name="ke_sql_query_content"
										class="form-control" readonly="readonly" rows="3"></textarea>
								</div>
							</div>
						</fieldset>

						<div class="modal-footer">
							<button type="button" class="btn btn-default"
								data-dismiss="modal">Cancle</button>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="plugins/magicsuggest/magicsuggest.js" name="loader" />
	<jsp:param value="plugins/tokenfield/bootstrap-tokenfield.js"
		name="loader" />
	<jsp:param value="plugins/codemirror/codemirror.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql.js" name="loader" />
	<jsp:param value="plugins/codemirror/show-hint.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql-hint.js" name="loader" />
	<jsp:param value="main/topic/ksql.js" name="loader" />
	<jsp:param value="main/topic/ksql.history.js" name="loader" />
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
