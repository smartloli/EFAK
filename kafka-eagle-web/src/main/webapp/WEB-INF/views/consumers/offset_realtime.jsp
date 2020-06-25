<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <style>
        .box {
            border-bottom: 1px solid #eee;
            margin-bottom: 20px;
            /* margin-top: 30px; */
            overflow: hidden;
        }

        .box .left {
            font-size: 36px;
            float: left
        }

        .box .left small {
            /* font-size: 24px;
            color: #777 */

        }

        .box .right {
            float: right;
            width: 260px;
            margin-top: -120px;
            background: #fff;
            cursor: pointer;
            padding: 5px 10px;
            border: 1px solid #ccc;
        }

        .chartdiv {
            width: 100%;
            height: 500px;
        }
    </style>

    <title>Offsets - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp">
        <jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css"/>
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
                <div class="box">
                    <h1 class="mt-4">Offsets Realtime</h1>
                    <ol class="breadcrumb mb-4">
                        <li class="breadcrumb-item"><a href="#">Consumer</a></li>
                        <li class="breadcrumb-item"><a href="/consumers">Groups</a></li>
                        <li class="breadcrumb-item"><a id="ke_consumer_offsets_a" href="#">Offsets</a></li>
                        <li class="breadcrumb-item active">Realtime</li>
                    </ol>
                    <div id="reportrange" class="right">
                        <i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp; <span></span> <b
                            class="caret"></b>
                    </div>
                </div>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>List the current consumers's application realtime offsets
                    of topic. Observe whether there is blocking by the trend of lag value.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Producer (msg/min)</div>
                                        <a id="producer_rate" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-sign-in-alt fa-4x text-primary-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xl-3 col-md-6 mb-4 col-md-offset-6">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Consumer (msg/min)</div>
                                        <a id="consumer_rate" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-sign-out-alt fa-4x text-success-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- charts -->
                <!-- Lag -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div id="topic_lag_name_header" class="card-header">
                                <i class="fas fa-chart-bar"></i> <strong>{TopicName}</strong>
                            </div>
                            <div class="card-body">
                                <div id="lag_chart" class="chartdiv"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Producer -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div id="topic_producer_name_header" class="card-header">
                                <i class="fas fa-chart-bar"></i> <strong>{TopicName}</strong>
                            </div>
                            <div class="card-body">
                                <div id="producer_chart" class="chartdiv"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Consumer -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div id="topic_consumer_name_header" class="card-header">
                                <i class="fas fa-chart-bar"></i> <strong>{TopicName}</strong>
                            </div>
                            <div class="card-body">
                                <div id="consumer_chart" class="chartdiv"></div>
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
    <jsp:param value="main/consumer/offset.realtime.js?v=1.4.9" name="loader"/>
    <jsp:param value="plugins/echart/echarts.min.js" name="loader"/>
    <jsp:param value="plugins/echart/macarons.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
</jsp:include>
</html>
