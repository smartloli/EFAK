<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Consumers" name="loader"/>
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
            <div class="breadcrumb-title pe-3">Consumers</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Groups</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i>&nbsp;<strong>List all consumer groups.</strong>
                    <br/> <i class="bx bx-info-circle"></i> <strong>Active: this card shows how many groups are
                    currently consuming.</strong> <br/> <i class="bx bx-info-circle"></i> <strong>Standby: this card
                    shows how many groups are currently processing sleep status.</strong>
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
                        <div class="widget-icon mx-auto mb-3 bg-light-success text-success">
                            <i class="bx bx-pulse"></i>
                        </div>
                        <h3 id="efak_consumer_groups_active">0</h3>
                        <p class="mb-0">ACTIVE</p>
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
                        <div class="widget-icon mx-auto mb-3 bg-light-danger text-danger">
                            <i class="bx bx-power-off"></i>
                        </div>
                        <h3 id="efak_consumer_groups_standby">0</h3>
                        <p class="mb-0">STANDBY</p>
                    </div>
                </div>
            </div>
        </div>
        <!-- topic table list -->
        <h6 class="mb-0 text-uppercase">Consumers Info</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_consumer_table_result" class="table table-striped table-bordered"
                           style="width:100%">
                        <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Group</th>
                            <th>Topics</th>
                            <th>Node</th>
                            <th>Active Topics</th>
                            <th>Active Threads</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- consumer group detail modal -->
        <div class="modal fade" id="ke_consumer_topics_detail" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Consumer Group Topics</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="card">
                            <div id="consumer_detail_children" class="card-body"></div>
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
    <jsp:param value="main/consumer/groups.js?v=3.0.0" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
