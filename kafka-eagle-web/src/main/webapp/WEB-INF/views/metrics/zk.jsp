<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
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

        .chartzkdiv {
            width: 100%;
            height: 300px;
        }
    </style>

    <title>Trend - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp">
        <jsp:param value="plugins/datatimepicker/daterangepicker.css"
                   name="css"/>
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
                    <h1 class="mt-4">Zookeeper Monitor</h1>
                    <ol class="breadcrumb mb-4">
                        <li class="breadcrumb-item"><a href="#">Zookeeper Monitor</a></li>
                        <li class="breadcrumb-item active">Details</li>
                    </ol>
                    <div id="reportrange" class="right">
                        <i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp;
                        <span></span> <b class="caret"></b>
                    </div>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> Zookeeper Send Packets
                            </div>
                            <div class="card-body">
                                <div id="zk_send_packets" class="chartzkdiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> Zookeeper Received Packets
                            </div>
                            <div class="card-body">
                                <div id="zk_recevied_packets" class="chartzkdiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> Zookeeper Alive Connections
                            </div>
                            <div class="card-body">
                                <div id="zk_alives_connections" class="chartzkdiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> Zookeeper Queue Requests
                            </div>
                            <div class="card-body">
                                <div id="zk_queue_requests" class="chartzkdiv"></div>
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
    <jsp:param value="main/metrics/zk.js?v=1.4.9" name="loader"/>
    <jsp:param value="plugins/echart/echarts.min.js" name="loader"/>
    <jsp:param value="plugins/echart/macarons.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js"
               name="loader"/>
</jsp:include>
</html>
