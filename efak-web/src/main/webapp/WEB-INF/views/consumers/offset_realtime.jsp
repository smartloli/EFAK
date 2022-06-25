<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Realtime" name="loader"/>
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
            <div class="breadcrumb-title pe-3">Consumer</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Realtime</li>
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
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i>&nbsp;<strong>List the current consumer topic chart.</strong>
                    <br/> <i class="bx bx-info-circle"></i> <strong>Producer: the speed of writing messages for the
                    current topic.</strong> <br/> <i class="bx bx-info-circle"></i> <strong>Consumer: the speed of
                    reading messages for the
                    current topic.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <div class="row row-cols-2 row-cols-sm-3 row-cols-md-4 row-cols-xl-6 row-cols-xxl-6">
            <div class="col">
                <div class="card radius-10">
                    <div class="card-body text-center">
                        <div class="widget-icon mx-auto mb-3 bg-light-primary text-primary">
                            <i class="bx bx-log-in"></i>
                        </div>
                        <h3 id="efak_topic_producer_rate">0</h3>
                        <p class="mb-0">PRODUCER (msg/min)</p>
                    </div>
                </div>
            </div>
            <div class="col"></div>
            <div class="col"></div>
            <div class="col"></div>
            <div class="col"></div>
            <div class="col">
                <div class="card radius-10">
                    <div class="card-body text-center">
                        <div class="widget-icon mx-auto mb-3 bg-light-success text-success">
                            <i class="bx bx-log-out"></i>
                        </div>
                        <h3 id="efak_topic_consumer_rate">0</h3>
                        <p class="mb-0">CONSUMER (msg/min)</p>
                    </div>
                </div>
            </div>
        </div>
        <hr/>
        <!-- topic lag chart -->
        <div class="row">
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 id="topic_lag_name_header" class="mb-0"><strong>{TopicName}</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="efak_topic_lag_chart" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- topic producer chart -->
        <div class="row">
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 id="topic_producer_name_header" class="mb-0"><strong>{TopicName}</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="efak_topic_producer_chart" class=""></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- topic consumer chart -->
        <div class="row">
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 id="topic_consumer_name_header" class="mb-0"><strong>{TopicName}</strong></h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="efak_topic_consumer_chart" class=""></div>
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
    <jsp:param value="main/consumer/offset.realtime.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
