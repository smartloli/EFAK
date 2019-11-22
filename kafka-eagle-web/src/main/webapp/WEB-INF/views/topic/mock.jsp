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

<title>Mock - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						Mock <small>message</small>
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
						<i class="fa fa-info-circle"></i> <strong>Select kafka
							topic, then edit the simulation message, and then click send to
							produce the message.</strong>
					</div>
				</div>
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-cogs fa-fw"></i> Content
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div class="form-group">
								<label>Topic Name (*)</label> <select id="select2val"
									name="select2val" tabindex="-1"
									style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
								<input id="ke_topic_mock" name="ke_topic_mock" type="hidden" />
								<label for="inputError" class="control-label text-danger"><i
									class="fa fa-info-circle"></i> Select the topic you need to
									alarm .</label>
							</div>
							<div class="form-group">
								<label>Message (*)</label>
								<textarea id="ke_mock_content" name="ke_mock_content"
									class="form-control" placeholder="Send mock data" rows="10"></textarea>
								<label for="inputError" class="control-label text-danger"><i
									class="fa fa-info-circle"></i> Write something and send message
									to topic .</label>
							</div>
							<button type="button" class="btn btn-primary" id="btn_send">Send
							</button>
							<div id="alert_mssage_mock" style="display: none"
								class="alert alert-danger">
								<label>Oops! Please make some changes . (*) is required
									.</label>
							</div>
							<div id="success_mssage_mock" style="display: none"
								class="alert alert-success">
								<label>Message sent success .</label>
							</div>
						</div>
					</div>
					<!-- /.col-lg-4 -->
				</div>
				<!-- /.row -->
			</div>
			<!-- /#page-wrapper -->
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
	<jsp:param value="main/topic/mock.js" name="loader" />
</jsp:include>
</html>
