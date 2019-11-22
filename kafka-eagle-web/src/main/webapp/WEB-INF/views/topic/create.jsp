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

<title>Topic Create - KafkaEagle</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Topic <small>create</small>
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
						<i class="fa fa-info-circle"></i> <strong>Create a new
							kafka's topic.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-tasks fa-fw"></i> Topic Property
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<form role="form" action="/ke/topic/create/form" method="post"
										onsubmit="return contextFormValid();return false;">
										<div class="form-group">
											<label>Topic Name (*)</label> <input id="ke_topic_name"
												name="ke_topic_name" class="form-control" maxlength=50>
											<label for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Made up of letters and digits
												or underscores . Such as "demo_kafka_topic_1" .</label>
										</div>
										<div class="form-group">
											<label>Partitions (*)</label> <input id="ke_topic_partition"
												name="ke_topic_partition" class="form-control" maxlength=50
												value="1"> <label for="inputError"
												class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Partition parameters must be
												numeric .</label>
										</div>
										<div class="form-group">
											<label>Replication Factor (*)</label> <input
												id="ke_topic_repli" name="ke_topic_repli"
												class="form-control" maxlength=50 value="1"><label
												for="inputError" class="control-label text-danger"><i
												class="fa fa-info-circle"></i> Replication Factor parameters
												must be numeric . Pay attention to available brokers must be larger than replication factor .</label>
										</div>
										<button type="submit" class="btn btn-success">Create</button>
										<div id="create_alert_msg" style="display: none"
											class="alert alert-danger">
											<label>Error! Please make some changes . (*) is
												required .</label>
										</div>
									</form>
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
	<jsp:param value="main/topic/create.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	function contextFormValid() {
		var ke_topic_name = $("#ke_topic_name").val();
		var ke_topic_partition = $("#ke_topic_partition").val();
		var ke_topic_repli = $("#ke_topic_repli").val();
		var reg = /^[A-Za-z0-9_-]+$/;
		var digit = /^[0-9]+$/;
		if (ke_topic_name.length == 0 || !reg.test(ke_topic_name)) {
			$("#create_alert_msg").show();
			setTimeout(function() {
				$("#create_alert_msg").hide()
			}, 3000);
			return false;
		}
		if (isNaN(ke_topic_partition) || isNaN(ke_topic_repli)) {
			$("#create_alert_msg").show();
			setTimeout(function() {
				$("#create_alert_msg").hide()
			}, 3000);
			return false;
		}
		return true;
	}
</script>
</html>
