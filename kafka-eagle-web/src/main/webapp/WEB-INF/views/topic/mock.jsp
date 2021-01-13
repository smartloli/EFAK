<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<jsp:include page="../public/plus/css.jsp">
	<jsp:param value="plugins/select2/select2.min.css" name="css" />
</jsp:include>
</head>

<body>
	<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
	<div id="layoutSidenav">
		<div id="layoutSidenav_nav">
			<jsp:include page="../public/plus/navbar.jsp"></jsp:include>
		</div>
		<div id="layoutSidenav_content">
			<main>
				<div class="container-fluid">
					<h1 class="mt-4">Mock</h1>
					<ol class="breadcrumb mb-4">
						<li class="breadcrumb-item"><a href="#">Mock</a></li>
						<li class="breadcrumb-item active">Message</li>
					</ol>
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fas fa-info-circle"></i> <strong>Select kafka topic, then edit the simulation message, and then click send to produce the message.</strong>
					</div>
					<!-- content body -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="far fa-envelope"></i> Content
								</div>
								<div class="card-body">
									<div class="form-group">
										<label>Topic Name (*)</label>
										<select id="select2val" name="select2val" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_mock" name="ke_topic_mock" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select the topic you need to send .
										</label>
									</div>
									<div class="form-group">
										<label>Message (*)</label>
										<textarea id="ke_mock_content" name="ke_mock_content" class="form-control" placeholder="Send mock data" rows="10"></textarea>
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Write something and send message to topic .
										</label>
									</div>
									<button type="button" class="btn btn-primary" id="btn_send">Send</button>
									<div id="alert_mssage_mock" style="display: none" class="alert alert-danger">
										<label>Oops! Please make some changes . (*) is required .</label>
									</div>
									<div id="success_mssage_mock" style="display: none" class="alert alert-success">
										<label>Message sent success .</label>
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
</body>
<jsp:include page="../public/plus/script.jsp">
	<jsp:param value="plugins/select2/select2.min.js" name="loader" />
	<jsp:param value="main/topic/mock.js" name="loader" />
</jsp:include>
</html>
