<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Meta" name="loader"/>
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
                        <li class="breadcrumb-item active" aria-current="page">MetaData</li>
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
                    <i class="bx bx-info-circle"></i>&nbsp;<strong>Leader -1 indicates that the partition is not
                    available or is empty.</strong>
                    <br/> <i class="bx bx-info-circle"></i> <strong>Preferred Leader: true is preferred leader, false is
                    risked leader.</strong> <br/> <i class="bx bx-info-circle"></i> <strong>Under Replicated: false is
                    normal, true is lost replicas.</strong>
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
                            <i class="bx bx-file"></i>
                        </div>
                        <h3 id="efak_topic_producer_logsize">0</h3>
                        <p class="mb-0">LOGSIZE (MSG)</p>
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
                        <h3 id="efak_topic_producer_capacity">0</h3>
                        <p class="mb-0">CAPACITY</p>
                    </div>
                </div>
            </div>
        </div>
        <!-- topic table list -->
        <h6 class="mb-0 text-uppercase">Topic Meta Info</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_topic_meta_table_result" class="table table-striped table-bordered"
                           style="width:100%">
                        <thead>
                        <tr>
                            <th>Topic</th>
                            <th>Partition</th>
                            <th>LogSize</th>
                            <th>Leader</th>
                            <th>Replicas</th>
                            <th>In Sync Replicas</th>
                            <th>Preferred Leader</th>
                            <th>Under Replicated</th>
                            <th>Preview</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- modal preview topic -->
        <div class="modal fade" id="ke_topic_preview" tabindex="-1" aria-labelledby="keModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">The latest 10 pieces of data</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <textarea id="ke_tp_preview_message" name="ke_tp_preview_message" class="form-control" rows="10"
                                  cols="10" placeholder="" readonly style="height: 420px;"></textarea>
                    </div>
                    <div id="remove_div" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- topic consumer group list -->
        <h6 class="mb-0 text-uppercase">Topic Consumer Groups</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_topic_consumer_tab_result" class="table table-striped table-bordered"
                           style="width:100%">
                        <thead>
                        <tr>
                            <th>Group</th>
                            <th>Topic</th>
                            <th>Lag</th>
                            <th>Status</th>
                            <%--  <th>Operate</th>--%>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- modal reset consumer topic -->
        <div class="modal fade" id="ke_reset_offsets" tabindex="-1" aria-labelledby="keModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Reset Offsets</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div id="ke_topic_reset_offsets" class="card-body">
                            <div class="mb-3">
                                <label>Reset Type (*)</label>
                                <select id="select2val"
                                        name="select2val"
                                        class="single-select"
                                        tabindex="-1"></select>
                                <label for="inputError" class="control-label text-danger"><i
                                        class="bx bx-info-circle"></i> Select the type you need to
                                    reset offsets .</label>
                            </div>
                            <div id="ke_reset_offset_value" class="mb-3">
                                <label>Reset Offsets Value (*)</label> <input
                                    id="ke_reset_offset_val" name="ke_reset_offset_val"
                                    class="form-control"
                                    placeholder="Input content must be number" maxlength="64">
                                <label for="inputError" class="control-label text-danger"><i
                                        class="bx bx-info-circle"></i> Input the reset offset value
                                    .</label>
                            </div>
                            <div class="mb-3">
                                <form>
                                    <textarea id="ke_reset_offset_result" name="ke_reset_offset_result"
                                              class="form-control" rows="10"
                                              cols="10" placeholder="" readonly style="height: 420px;"></textarea>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div id="ke_reset_offset_btn_div" class="modal-footer">
                        <button type="button" class="btn btn-primary" id="ke_reset_offset_btn">Execute
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- topic mbean -->
        <div class="row">
            <div class="col-12 col-lg-12 col-xl-12 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0 text-uppercase">Topic MBean</h6>
                        </div>
                        <div class="table-responsive mt-2">
                            <table id="topic_metrics_tab" class="table align-middle mb-0">
                            </table>
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
                            <div id="efak_topic_producer_msg" class=""></div>
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
    <jsp:param value="main/topic/topic.meta.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
