<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Zookeeper" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css"/>
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
                        <li class="breadcrumb-item active" aria-current="page">Zookeeper</li>
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
        <!--end row-->
        <!-- content body -->
        <!-- topic lag chart -->
        <div class="row">
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>Zookeeper Alive Connections</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="zk_alives_connections" class="efak-metrics-chart-div"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>Zookeeper Send Packets</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="zk_send_packets" class="efak-metrics-chart-div"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>Zookeeper Received Packets</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="zk_recevied_packets" class="efak-metrics-chart-div"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0"><strong>Zookeeper Queue Requests</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="zk_queue_requests" class="efak-metrics-chart-div"></div>
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
    <jsp:param value="main/metrics/zookeeper.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
