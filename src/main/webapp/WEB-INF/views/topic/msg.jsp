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
						<i class="fa fa-info-circle"></i> <strong>Filter the
							desired topic message results based on conditions.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-filter fa-fw"></i> Condition
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="form-group">
								<label>Type (*)</label> <input id="ke_condition"
									name="ke_condition" class="form-control" maxlength=50 value="1"><input
									id="ke_conditions" name="ke_conditions" type="hidden">
								<label for="inputError" class="control-label text-danger"><i
									class="fa fa-info-circle"></i> Select the condition you need to
									query .</label>
							</div>
							<div id="ke_customer_filter">
								<div class="form-group">
									<label>Topic Name (*)</label> <input id="ke_topic"
										name="ke_topic" class="form-control" maxlength=50 value="">
									<label for="inputError" class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Write the topic you need to
										query .</label>
								</div>
								<div class="form-group">
									<label>Partitions (*)</label> <input id="ke_topic_lag"
										name="ke_topic_lag" class="form-control" maxlength=50
										value="0,1,2,3,4,5"> <label for="inputError"
										class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Setting the topic partitions,
										such as "0,1,2,3,4,5" .</label>
								</div>
								<div class="form-group">
									<label>Offsets (*)</label> <input id="ke_topic_lag"
										name="ke_topic_lag" class="form-control" maxlength=50
										value="0,10"> <label for="inputError"
										class="control-label text-danger"><i
										class="fa fa-info-circle"></i> Setting the topic offset you
										need to query, start and end offset you can write like this
										"0,10" . Query maximum record cannot exceed 5000 .</label>
								</div>
								<button type="submit" class="btn btn-success">Query</button>
								<div id="alert_mssage" style="display: none"
									class="alert alert-danger">
									<label>Oops! Please make some changes . (*) is required
										.</label>
								</div>
							</div>
							<div id="ke_sql_query">
								<form>
									<textarea id="code" name="code"></textarea>
								</form>
								<button type="submit" class="btn btn-success">Query</button>
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
