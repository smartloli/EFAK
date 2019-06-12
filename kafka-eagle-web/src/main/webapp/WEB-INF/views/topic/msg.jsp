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
<jsp:include page="../public/css.jsp"></jsp:include>
<jsp:include page="../public/tcss.jsp"></jsp:include>
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
						<i class="fa fa-info-circle"></i> Sample SQL query: <strong>select * from "ke_topic" where "partition" in (0,1,2) limit 10</strong><br/> 
						<i class="fa fa-info-circle"></i> AutoComplete: Press <strong>Alt and /</strong>. 
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
								</ul>
							</div>
							<div class="tab-content">
								<div id="log_textarea" class="tab-pane fade in active">
									<form>
										<textarea id="job_info" name="job_info"></textarea>
									</form>
								</div>
								<div id="result_textarea" class="tab-pane fade"></div>
							</div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="plugins/magicsuggest/magicsuggest.js" name="loader" />
	<jsp:param value="plugins/tokenfield/bootstrap-tokenfield.js"
		name="loader" />
	<jsp:param value="plugins/codemirror/codemirror.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql.js" name="loader" />
	<jsp:param value="plugins/codemirror/show-hint.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql-hint.js" name="loader" />
	<jsp:param value="main/topic/msg.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
