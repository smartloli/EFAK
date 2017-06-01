<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!DOCTYPE html>
<html lang="zh">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Role - KafkaEagle</title>
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
						Role <small>overview</small>
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
						<i class="fa fa-info-circle"></i> <strong>Display the
							role management list and configure the template directories that
							each role can access.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-cogs fa-fw"></i> Role Manager
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<table id="result" class="table table-bordered table-condensed"
								width="100%">
								<thead>
									<tr>
										<th>Name</th>
										<th>Describe</th>
										<th>Operate</th>
									</tr>
								</thead>
							</table>
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- modal -->
			<div class="modal fade" aria-labelledby="keModalLabel"
				aria-hidden="true" id="ke_setting_dialog" tabindex="-1"
				role="dialog">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<button class="close" type="button" data-dismiss="modal">×</button>
							<h4 class="modal-title" id="keModalLabel">
								Role Setting
							</h4>
							<div id="ke_role_alert_mssage"></div>
						</div>
						<!-- /.row -->
						<form role="form" action="#" method="post"
							onsubmit="return contextFormValid();return false;">
							<div class="modal-body">
								<div id="treeview-checkable" class=""></div>
							</div>
							<div id="remove_div" class="modal-footer">
								<button type="button" class="btn btn-primary"
									data-dismiss="modal">Close</button>
							</div>
						</form>
					</div>
				</div>
			</div>
			<!-- /#page-wrapper -->
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/system/role.js" name="loader" />
</jsp:include>
<jsp:include page="../public/tscript.jsp"></jsp:include>
</html>
