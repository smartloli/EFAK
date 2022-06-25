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
        <jsp:param value="plugins/terminal/jquery.terminal.min.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">Cluster</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Zookeeper</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i> <strong>Zookeeper version is "3.5+" or version is "-" maybe
                    zookeeper client command disable.</strong> <br/> <i
                        class="bx bx-info-circle"></i> <strong>When you encounter one of
                    the above problems, you can visit the <a
                            href="http://www.kafka-eagle.org/articles/docs/quickstart/metrics.html"
                            target="_blank">EFAK</a> document to see the opening steps.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <!-- zookeeper table list -->
        <h6 class="mb-0 text-uppercase">Zookeeper Server Info</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_cluster_zookeeper_tab" class="table table-striped table-bordered"
                           style="width:100%">
                        <thead>
                        <tr>
                            <th>#IP</th>
                            <th>Port</th>
                            <th>Mode</th>
                            <th>Version</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>
        <h6 class="mb-0 text-uppercase">Zookeeper Client Info</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div id="efak_zookeeper_client_info" class="terminal" style="height: 400px;">

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
    <jsp:param value="main/cluster/zookeeper.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/terminal/jquery.terminal.min.js" name="loader"/>
    <jsp:param value="main/cluster/zkcli.js?v=3.0.0" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
