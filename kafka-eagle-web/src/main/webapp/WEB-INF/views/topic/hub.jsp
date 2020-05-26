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

<title>Topic Hub - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
<jsp:include page="../public/tcss.jsp"></jsp:include>
</head>
<style>
.CodeMirror {
	border-top: 1px solid #ddd;
	border-bottom: 1px solid #ddd;
	border-right: 1px solid #ddd;
	border-left: 1px solid #ddd;
}

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
</style>
<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Topic <small>hub</small>
					</h1>
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i> It is used to migrate the topic data or balance the expanded broker.
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-filter fa-fw"></i> Topic Balance
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<div class="form-group">
										<label>Balance Type (*)</label>
										<br />
										<label class="radio-inline">
											<input type="radio" name="ke_topic_balance_type" id="ke_topic_balance_type_single" value="balance_single" checked="">Single
										</label>
										<label class="radio-inline">
											<input type="radio" name="ke_topic_balance_type" id="ke_topic_balance_type_all" value="balance_all">All
										</label>
										<br />
										<br />
										<label>Topic Name (*)</label>
										<select multiple="multiple" id="select2val" name="select2val" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_balance" name="ke_topic_balance" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select the topic you need to balance .
										</label>
									</div>
									<button id="ke_balancer_generate" class="btn btn-success">Generate</button>
									<button id="ke_balancer_execute" class="btn btn-primary" style="display: none">Execute</button>
									<button id="ke_balancer_verify" class="btn btn-info" style="display: none">Verify</button>
								</div>
							</div>
							<!-- /.panel-body -->
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-6">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Proposed Partition Reassignment Configuartion
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="ke_sql_query">
								<form>
									<textarea id="code_proposed" name="code_proposed"></textarea>
								</form>
							</div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<div class="col-lg-6">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Current Partition Replica Assignment
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="ke_sql_query">
								<form>
									<textarea id="code_current" name="code_current"></textarea>
								</form>
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
							<i class="fa fa-comments fa-fw"></i> Result
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="ke_sql_query">
								<form>
									<textarea id="code_result" name="code_result"></textarea>
								</form>
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
	<jsp:param value="plugins/tokenfield/bootstrap-tokenfield.js" name="loader" />
	<jsp:param value="plugins/codemirror/codemirror.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql.js" name="loader" />
	<jsp:param value="plugins/codemirror/show-hint.js" name="loader" />
	<jsp:param value="plugins/codemirror/sql-hint.js" name="loader" />
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
	<jsp:param value="main/topic/hub.js" name="loader" />
	<jsp:param value="main/topic/ksql.history.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
