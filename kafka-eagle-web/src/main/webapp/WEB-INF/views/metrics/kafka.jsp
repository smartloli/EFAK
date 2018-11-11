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
<style>
.box{
	border-bottom:1px solid #eee;
	margin-bottom:20px;
	margin-top:30px;
	overflow:hidden;
}
.box .left{
	font-size: 36px;
	float:left
}
.box .left small{
	font-size: 24px;
	color:#777
}
.box  .right{
	float:right;
	width: 230px;
	margin-top:20px; 
	background: #fff; 
	cursor: pointer; 
	padding: 5px 10px; 
	border: 1px solid #ccc;
}
</style>

<title>Kafka - KafkaEagle</title>
<jsp:include page="../public/css.jsp">
	<jsp:param value="plugins/datatimepicker/daterangepicker.css"
		name="css" />
</jsp:include>
</head>
<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12" >
					<div class="box">
					  <p   class="left">
							Kafka Performance <small>details</small>
						</p>
						<div id="reportrange"
						class="right">
							<i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp;
							<span></span> <b class="caret"></b>
						</div>
					</div>
					
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert"
							aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i> <strong>Through JMX to
							obtain data, monitor the Kafka client, the production side, the
							number of messages, the number of requests, processing time and
							other data to visualize performance .</strong>
					</div>
				</div>
			</div>

            <div class="row">
                <div class="col-lg-12">
                    <div class="alert alert-info alert-dismissable"  style="height: 110px;">
                        <div class="checkbox">
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iMI" name="message_in" checked>
                                <label for="iMI">Message In</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iBI"  name="byte_in" checked>
                                <label for="iBI">Topic Byte In</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iBO"  name="byte_out" checked>
                                <label for="iBO">Topic Byte Out</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iBR"  name="byte_rejected">
                                <label for="iBR">Topic Byte Rejected</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iFFR"  name="failed_fetch_request">
                                <label for="iFFR">Failed Fetch Request</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iFPR"  name="failed_produce_request">
                                <label for="iFPR">Failed Produce Request</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iPMC"  name="produce_message_conversions">
                                <label for="iPMC">Produce Message Conversions</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iTFR"  name="total_fetch_requests">
                                <label for="iTFR">Total Fetch Requests</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iTPR"  name="total_produce_requests">
                                <label for="iTPR">Total Produce Requests</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iRBO"  name="replication_bytes_out">
                                <label for="iRBO">Replication Bytes Out</label>
                            </div>
                            <div class="col-sm-3 checkbox checkbox-primary">
                                <input type="checkbox" id="iRBI"  name="replication_bytes_in">
                                <label for="iRBI">Replication Bytes In</label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12" id="message_in" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>Kafka
								Message In (per/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_msg_in"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="byte_in" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong>Kafka
								Topic Byte In (byte/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_msg_byte_in"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="byte_out" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
								Topic Byte Out (byte/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_msg_byte_out"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="byte_rejected" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Byte Rejected (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_byte_rejected"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="failed_fetch_request" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Failed Fetch Request (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_failed_fetch_request"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="failed_produce_request" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Failed Produce Request (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_failed_produce_request"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="produce_message_conversions" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Produce Message Conversions (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_produce_message_conversions"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="total_fetch_requests" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Total Fetch Requests (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_total_fetch_requests"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="total_produce_requests" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Total Produce Requests (/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_total_produce_requests"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="replication_bytes_out" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Replication Bytes Out (byte/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_replication_bytes_out"></div>
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
				<!-- /.col-lg-4 -->
				<!-- /.col-lg-4 -->
				<div class="col-lg-12" id="replication_bytes_in" >
					<div class="panel panel-default">
						<div class="panel-heading">
							<i class="fa fa-bar-chart-o fa-fw"></i> <strong> Kafka
                            Replication Bytes In (byte/sec)</strong>
							<div class="pull-right"></div>
						</div>
						<!-- /.panel-heading -->
						<div class="panel-body">
							<div id="mbean_replication_bytes_in"></div>
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
	<jsp:param value="main/metrics/kafka.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/moment.min.js" name="loader" />
	<jsp:param value="plugins/datatimepicker/daterangepicker.js"
		name="loader" />
</jsp:include>
</html>
