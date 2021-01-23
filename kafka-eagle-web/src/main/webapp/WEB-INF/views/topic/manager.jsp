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

<title>Manager - KafkaEagle</title>
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
					<h1 class="mt-4">Manager</h1>
					<ol class="breadcrumb mb-4">
						<li class="breadcrumb-item"><a href="#">Manager</a></li>
						<li class="breadcrumb-item active">Topic</li>
					</ol>
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
						<i class="fas fa-info-circle"></i> <strong>Select kafka topic, then edit the topic config, such as clean topic data, modify topic config, describe topic config etc.</strong>
					</div>
					<!-- content body -->
					<div class="row">
						<div class="col-lg-12">
							<div class="card mb-4">
								<div class="card-header">
									<i class="fas fa-cogs"></i> Content
								</div>
								<div class="card-body">
									<div class="form-group">
										<label>Type (*)</label>
										<br />
										<label class="radio-inline">
											<input type="radio" name="ke_topic_alter" id="ke_topic_alter" value="add_config" checked="">Add Config
										</label>
										<label class="radio-inline">
											<input type="radio" name="ke_topic_alter" id="ke_topic_delete" value="del_config">Delete Config
										</label>
										<label class="radio-inline">
											<input type="radio" name="ke_topic_alter" id="ke_topic_describe" value="desc_config">Describe Config
										</label>
										<label class="radio-inline">
											<input type="radio" name="ke_topic_alter" id="ke_topic_election" value="pref_election">Preferred Replica Election
										</label>
										<br />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select operate type when you getter/setter topic .
										</label>
									</div>
									<div class="form-group">
										<label>Topic Name (*)</label>
										<select id="select2val" name="select2val" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_name" name="ke_topic_name" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select the topic you need to alter .
										</label>
									</div>
									<div id="div_topic_keys" class="form-group">
										<label>Key (*)</label>
										<select id="select2key" name="select2key" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
										<input id="ke_topic_key" name="ke_topic_key" type="hidden" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Select the topic property key you need to set .
										</label>
									</div>
									<div id="div_topic_value" class="form-group">
										<label>Value (*)</label>
										<input id="ke_topic_value" name="ke_topic_value" type="text" class="form-control" />
										<label for="inputError" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Set the topic property value when you submit setter.
										</label>
									</div>
									<div id="div_topic_msg" class="form-group">
										<label>Message (*)</label>
										<textarea id="ke_topic_config_content" name="ke_topic_config_content" class="form-control" placeholder="" rows="5" readonly maxlength="120"></textarea>
										<label for="inputSuccess" class="control-label text-danger">
											<i class="fa fa-info-circle"></i> Get result from server when you getter/setter topic .
										</label>
									</div>
									<button type="button" class="btn btn-primary" id="btn_send">Submit</button>
									<div id="alert_message_alter" style="display: none" class="alert alert-danger">
										<label>Oops! Please make some changes . (*) is required .</label>
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
	<jsp:param value="main/topic/manager.js" name="loader" />
</jsp:include>
</html>
