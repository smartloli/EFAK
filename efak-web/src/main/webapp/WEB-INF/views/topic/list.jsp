<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="List" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css"/>
        <jsp:param value="plugins/select2/select2.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2-bootstrap4.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">Topic</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">List</li>
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
                    <i class="bx bx-info-circle"></i>&nbsp;<strong>List all topic
                    information.</strong>
                    <br/> <i class="bx bx-info-circle"></i> <strong>Broker Spread: the higher the coverage, the
                    higher
                    the
                    resource usage of kafka broker nodes.</strong> <br/> <i class="bx bx-info-circle"></i> <strong>Broker
                    Skewed: the larger the skewed, the higher the pressure on the broker node of kafka.</strong>
                    <br/>
                    <i class="bx bx-info-circle"></i> <strong>Broker Leader Skewed: the higher the leader skewed,
                    the
                    higher the
                    pressure on the kafka broker leader node.</strong>
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
                            <i class="bi bi-archive-fill"></i>
                        </div>
                        <h3 id="efak_topic_producer_number">0</h3>
                        <p class="mb-0">APP</p>
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
                            <i class="bx bx-data"></i>
                        </div>
                        <h3 id="efak_topic_producer_total_capacity">0</h3>
                        <p class="mb-0">CAPACITY</p>
                    </div>
                </div>
            </div>
        </div>
        <!-- topic table list -->
        <h6 class="mb-0 text-uppercase">Topic List Info</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_topic_table_result" class="table table-striped table-bordered" style="width:100%">
                        <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Topic Name</th>
                            <th>Partitions</th>
                            <th>Broker Spread</th>
                            <th>Broker Skewed</th>
                            <th>Broker Leader Skewed</th>
                            <th>Created</th>
                            <th>Modify</th>
                            <th>Operate</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- add partition modal -->
        <div class="modal fade" id="ke_topic_modify" tabindex="-1" aria-labelledby="keModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Add Partition</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="input-group mb-3"><span class="input-group-text">+</span>
                            <input id="ke_modify_topic_partition" name="ke_modify_topic_partition" type="text"
                                   class="form-control" placeholder="Enter Partitions (Number >= 1)" aria-label=""
                                   aria-describedby="">
                        </div>
                    </div>
                    <div id="ke_topic_submit_div" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- truncate topic -->
        <div class="modal fade" id="ke_topic_clean" tabindex="-1" aria-labelledby="keModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Truncate Topic</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div id="ke_topic_clean_content" class="modal-body"></div>
                    <div id="ke_topic_clean_data_div" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- delete topic -->
        <div class="modal fade" id="ke_topic_delete" tabindex="-1" aria-labelledby="keModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Delete Topic</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="input-group mb-3"><span class="input-group-text"><i
                                class="bx bx-lock-alt"></i></span>
                            <input id="ke_admin_token" name="ke_admin_token" type="text"
                                   class="form-control" placeholder="Enter Admin Token" aria-label=""
                                   aria-describedby="">
                        </div>
                    </div>
                    <div id="remove_div" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- topic filter select -->
        <div class="row">
            <div class="col col-lg-12 mx-auto">
                <div class="card radius-10">
                    <div class="card-body">
                        <div class="card-title">
                            <h6 class="mb-0 text-uppercase">Topic Filter</h6>
                        </div>
                        <hr/>
                        <div class="mb-3">
                            <label class="form-label">Topic Name (*)</label>
                            <select class="multiple-select" multiple="multiple" id="select2val" name="select2val"
                                    tabindex="-1"
                                    data-placeholder="Topic"></select>
                            <input id="ke_topic_aggrate" name="ke_topic_aggrate" type="hidden"/>
                            <label for="inputError" class="control-label text-danger">
                                <i class="bx bx-info-circle"></i> Choice the topic you want to aggregate .
                            </label>
                        </div>
                        <div class="mb-3">
                            <div class="col-sm-3">
                                <button id="ke_topic_select_query" class="btn btn-primary">Query</button>
                            </div>
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
                            <h6 class="mb-0 text-uppercase">Producer LogSize</h6>
                        </div>
                        <hr/>
                        <div class="row">
                            <div id="efak_topic_producer_logsize_chart" class=""></div>
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
    <jsp:param value="main/topic/list.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
