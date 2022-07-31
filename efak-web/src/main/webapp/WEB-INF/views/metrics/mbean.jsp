<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="MBean" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp"></jsp:include>

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
                        <li class="breadcrumb-item active" aria-current="page">MBean</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i> <strong>MBean: MeanRate, OneMinuteRate, FiveMinuteRate,
                    FifteenMinuteRate.</strong><br/> <i class="bx bx-info-circle"></i> <strong>RateUnit:
                    SECONDS.</strong><br/> <i class="bx bx-info-circle"></i> <strong>TableValue: RateUnit * 60.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <!-- kafka table list -->
        <h6 class="mb-0 text-uppercase">Kafka Brokers MBean</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_metrics_mbean_tab" class="table table-striped table-bordered" style="width:100%">
                    </table>
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
    <jsp:param value="main/metrics/mbean.js?v=3.0.0" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
