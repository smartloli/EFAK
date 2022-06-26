<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Kafka" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css"/>
        <jsp:param value="plugins/checkbox/metrics-checkbox.css" name="css"/>
    </jsp:include>

    <!-- Required table css -->
    <jsp:include page="../public/pro/tcss.jsp"></jsp:include>
</head>

<body>


<!--start wrapper-->
<div class="wrapper">

    <!--start top header-->
    <jsp:include page="../public/pro/navtop.jsp"></jsp:include>
    <!--end top header-->

    <!--start sidebar -->
    <jsp:include page="../public/pro/navbar.jsp"></jsp:include>
    <!--end sidebar -->

    <!--start content-->
    <main class="page-content">
        <!--breadcrumb-->
        <div class="page-breadcrumb d-none d-sm-flex align-items-center mb-3">
            <div class="breadcrumb-title pe-3">Metrics</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Kafka</li>
                    </ol>
                </nav>
            </div>
            <div id="reportrange" class="ms-auto" style="border: 1px solid #ccc;cursor: pointer; padding: 5px 10px;">
                <i class="bx bx-calendar"></i> &nbsp; <span></span> <b
                    class="caret"></b>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div id="efak_chk_top"
                     class="alert border-0 bg-light-info alert-dismissible fade show text-info efak-metrics-checkbox-alert">
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="message_in" id="iMI" checked>
                        <label class="form-check-label" for="iMI">Message In</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="byte_in" id="iBI" checked>
                        <label class="form-check-label" for="iBI">Topic Byte In</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="byte_out" id="iBO" checked>
                        <label class="form-check-label" for="iBO">Topic Byte Out</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="byte_rejected" id="iBR" checked>
                        <label class="form-check-label" for="iBR">Topic Byte Rejected</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="failed_fetch_request" id="iFFR" checked>
                        <label class="form-check-label" for="iFFR">Failed Fetch Request</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="failed_produce_request" id="iFPR" checked>
                        <label class="form-check-label" for="iFPR">Failed Produce Request</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="produce_message_conversions" id="iPMC"
                               checked>
                        <label class="form-check-label" for="iPMC">Produce Message Conversions</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="total_fetch_requests" id="iTFR"
                               checked>
                        <label class="form-check-label" for="iTFR">Total Fetch Requests</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="total_produce_requests" id="iTPR"
                               checked>
                        <label class="form-check-label" for="iTPR">Total Produce Requests</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="replication_bytes_out" id="iRBO"
                               checked>
                        <label class="form-check-label" for="iRBO">Replication Bytes Out</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="replication_bytes_in" id="iRBI"
                               checked>
                        <label class="form-check-label" for="iRBI">Replication Bytes In</label>
                    </div>
                    <div class="col-sm-3 form-check efak-metrics-checkbox">
                        <input class="form-check-input" type="checkbox" name="os_free_memory" id="iOFM"
                               checked>
                        <label class="form-check-label" for="iOFM">OS Free Memory</label>
                    </div>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->

        <hr/>
        <!-- topic lag chart -->
        <div class="row">
            <div class="col col-lg-12 mx-auto" id="message_in">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>MessagesInPerSec (per/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_msg_in" class="efak-metrics-chart-div"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="byte_in">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>BytesInPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_msg_byte_in" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="byte_out">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>BytesOutPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_msg_byte_out" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="byte_rejected">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>BytesRejectedPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_byte_rejected" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="failed_fetch_request">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>FailedFetchRequestsPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_failed_fetch_request" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="failed_produce_request">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>FailedProduceRequestsPerSec (/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_failed_produce_request" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="produce_message_conversions">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>ProduceMessageConversionsPerSec (/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_produce_message_conversions" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="total_fetch_requests">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>TotalFetchRequestsPerSec (/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_total_fetch_requests" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="total_produce_requests">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>TotalProduceRequestsPerSec (/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_total_produce_requests" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="replication_bytes_out">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>ReplicationBytesOutPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_replication_bytes_out" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="replication_bytes_in">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>ReplicationBytesInPerSec (byte/sec)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_replication_bytes_in" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto" id="os_free_memory">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>FreePhysicalMemorySize (byte/min)</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="mbean_os_free_memory" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>
    <!--end page main-->

    <!--Start Back To Top Button-->
    <a href="javaScript:;" class="back-to-top"><i class='bx bxs-up-arrow-alt'></i></a>
    <!--End Back To Top Button-->

</div>
<!--end wrapper-->

<!-- import js -->
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="main/metrics/kafka.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
