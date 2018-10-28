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

<title>Alarm - KafkaEagle</title>
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
						Alarm <small>History</small>
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
						<i class="fa fa-info-circle"></i> <strong>Manage cluster alarm
							records .</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-pencil"></i> Alarm list
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<table id="result" class="table table-bordered table-condensed"
								width="100%">
								<thead>
									<tr>
										<th>ID</th>
										<th>Type</th>
										<th>Cluster</th>
										<th>Server</th>
										<th>Owner</th>
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
							<p>Are you sure you want to delete it?
							<p>
						</div>
						<div id="remove_div" class="modal-footer"></div>
					</div>
				</div>
			</div>
			<!-- modify -->
			<div class="modal fade" aria-labelledby="keModalLabelModify"
				aria-hidden="true" id="modfiy_info" tabindex="-1" role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabelModify">Modify</h4>
						</div>
						<!-- /.row -->
						<form role="form" action="/ke/alarm/history/modify/"
							method="post"
							onsubmit="return contextModifyFormValid();return false;">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Server</label>
									<div class="col-sm-9">
										<input id="ke_history_id_lag" name="ke_history_id_lag"
											type="hidden" class="form-control" placeholder=""> <input
											id="ke_history_name_lag" name="ke_history_name_lag"
											type="text" class="form-control" placeholder="">
									</div>
								</div>
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">Owners</label>
									<div class="col-sm-9">
										<textarea id="ke_owners_modify" name="ke_owners_modify"
											class="form-control" rows="3"></textarea>
									</div>
								</div>
								<div id="alert_message_modify" style="display: none"
									class="alert alert-danger">
									<label> Oops! Please make some changes .</label>
								</div>
							</fieldset>

							<div class="modal-footer">
								<button type="button" class="btn btn-default"
									data-dismiss="modal">Cancle</button>
								<button type="submit" class="btn btn-primary" id="create-modify">Submit
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		<!-- /#page-wrapper -->
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/alarm/history.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
<script type="text/javascript">
	function contextModifyFormValid() {
		var ke_history_name_lag = $("#ke_history_name_lag").val();
		var ke_owners_modify = $("#ke_owners_modify").val();
		
		if (ke_history_name_lag.length == 0 || ke_owners_modify.length == 0) {
			$("#alert_message_modify").show();
			setTimeout(function() {
				$("#alert_message_modify").hide()
			}, 3000);
			return false;
		}

		return true;
	}
</script>
</html>
