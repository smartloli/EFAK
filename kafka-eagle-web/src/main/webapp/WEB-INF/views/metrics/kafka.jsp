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

        .chartkafkadiv {
            width: 100%;
            height: 300px;
        }

        .kechk {
            float: left;
        }
    </style>

    <title>Kafka - KafkaEagle</title>
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
                    <h1 class="mt-4">Kafka Performance</h1>
                    <ol class="breadcrumb mb-4">
                        <li class="breadcrumb-item"><a href="#">Kafka Performance</a></li>
                        <li class="breadcrumb-item active">Details</li>
                    </ol>
                    <div id="reportrange" class="right">
                        <i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp;
                        <span></span> <b class="caret"></b>
                    </div>
                </div>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Through JMX to
                    obtain data, monitor the Kafka client, the production side, the
                    number of messages, the number of requests, processing time and
                    other data to visualize performance .</strong>
                </div>
                <!-- metrics -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="alert alert-info alert-dismissable"
                             style="height: 110px;">
                            <div id="ke_chk_top" class="checkbox">
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iMI" name="message_in" checked>
                                    <label for="iMI">Message In</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iBI" name="byte_in" checked>
                                    <label for="iBI">Topic Byte In</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iBO" name="byte_out" checked>
                                    <label for="iBO">Topic Byte Out</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iBR" name="byte_rejected" checked>
                                    <label for="iBR">Topic Byte Rejected</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iFFR" name="failed_fetch_request"
                                           checked> <label for="iFFR">Failed Fetch Request</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iFPR" name="failed_produce_request"
                                           checked> <label for="iFPR">Failed Produce
                                    Request</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iPMC"
                                           name="produce_message_conversions" checked> <label
                                        for="iPMC">Produce Message Conversions</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iTFR" name="total_fetch_requests"
                                           checked> <label for="iTFR">Total Fetch Requests</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iTPR" name="total_produce_requests"
                                           checked> <label for="iTPR">Total Produce
                                    Requests</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iRBO" name="replication_bytes_out"
                                           checked> <label for="iRBO">Replication Bytes
                                    Out</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iRBI" name="replication_bytes_in"
                                           checked> <label for="iRBI">Replication Bytes In</label>
                                </div>
                                <div class="col-sm-3 checkbox checkbox-primary kechk">
                                    <input type="checkbox" id="iOFM" name="os_free_memory"
                                           checked> <label for="iOFM">OS Free Memory</label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12" id="message_in">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> MessagesInPerSec (per/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_msg_in" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="byte_in">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> BytesInPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_msg_byte_in" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="byte_out">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> BytesOutPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_msg_byte_out" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="byte_rejected">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> BytesRejectedPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_byte_rejected" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="failed_fetch_request">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> FailedFetchRequestsPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_failed_fetch_request" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="failed_produce_request">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> FailedProduceRequestsPerSec (/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_failed_produce_request" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="produce_message_conversions">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> ProduceMessageConversionsPerSec (/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_produce_message_conversions" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="total_fetch_requests">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> TotalFetchRequestsPerSec (/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_total_fetch_requests" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="total_produce_requests">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> TotalProduceRequestsPerSec (/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_total_produce_requests" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="replication_bytes_out">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> ReplicationBytesOutPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_replication_bytes_out" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="replication_bytes_in">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> ReplicationBytesInPerSec (byte/sec)
                            </div>
                            <div class="card-body">
                                <div id="mbean_replication_bytes_in" class="chartkafkadiv"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-12" id="os_free_memory">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-line"></i> FreePhysicalMemorySize (byte/min)
                            </div>
                            <div class="card-body">
                                <div id="mbean_os_free_memory" class="chartkafkadiv"></div>
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
    <jsp:param value="main/metrics/kafka.js?v=1.4.9" name="loader"/>
    <jsp:param value="plugins/echart/echarts.min.js" name="loader"/>
    <jsp:param value="plugins/echart/macarons.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js"
               name="loader"/>
</jsp:include>
</html>
