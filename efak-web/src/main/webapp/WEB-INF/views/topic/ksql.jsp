<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="KSQL" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/codemirror/codemirror.css" name="css"/>
        <jsp:param value="plugins/codemirror/show-hint.css" name="css"/>
        <jsp:param value="plugins/codemirror/code-dark.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">Topics</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">KSQL</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->

        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i>&nbsp;Sample <a
                        href="http://www.kafka-eagle.org/articles/docs/quickstart/ksql.html" target="_blank">KSQL</a>
                    query: <strong>select * from efak_topic where `partition` in (0,1,2) limit 10</strong>
                    <br/><i class="bx bx-info-circle"></i>&nbsp;AutoComplete: Press <strong>Alt and /</strong>.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xl-12 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Kafka Query SQL</h6>
                            <hr/>
                            <textarea id="efak_ksql_code" name="efak_ksql_code"></textarea>
                            <br/>
                            <button id="ke_ksql_query" class="btn btn-primary">Query</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--end row-->

        <div class="row">
            <div class="col-xl-12 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Tasks Job Info</h6>
                            <hr/>
                            <div class="card">
                                <div class="card-body">
                                    <ul class="nav nav-tabs nav-primary" role="tablist">
                                        <li class="nav-item" role="presentation">
                                            <a class="nav-link active" data-bs-toggle="tab" href="#efak_ksql_logs_tab"
                                               role="tab" aria-selected="true">
                                                <div class="d-flex align-items-center">
                                                    <div class="tab-icon"><i class='bx bx-file font-18 me-1'></i>
                                                    </div>
                                                    <div class="tab-title">Logs</div>
                                                </div>
                                            </a>
                                        </li>
                                        <li class="nav-item" role="presentation">
                                            <a class="nav-link" data-bs-toggle="tab" href="#efak_ksql_result_tab"
                                               role="tab"
                                               aria-selected="false">
                                                <div class="d-flex align-items-center">
                                                    <div class="tab-icon"><i class='bx bx-table font-18 me-1'></i>
                                                    </div>
                                                    <div class="tab-title">Result</div>
                                                </div>
                                            </a>
                                        </li>
                                        <li class="nav-item" role="presentation">
                                            <a class="nav-link" data-bs-toggle="tab" href="#efak_ksql_history_tab"
                                               role="tab"
                                               aria-selected="false">
                                                <div class="d-flex align-items-center">
                                                    <div class="tab-icon"><i class='bx bx-history font-18 me-1'></i>
                                                    </div>
                                                    <div class="tab-title">History</div>
                                                </div>
                                            </a>
                                        </li>
                                    </ul>
                                    <div class="tab-content py-3">
                                        <div class="tab-pane fade show active" id="efak_ksql_logs_tab" role="tabpanel">
                                            <form>
                                                <textarea id="job_info" name="job_info"></textarea>
                                            </form>
                                        </div>
                                        <div class="tab-pane fade" id="efak_ksql_result_tab" role="tabpanel">
                                        </div>
                                        <div class="tab-pane fade" id="efak_ksql_history_tab" role="tabpanel">
                                            <div id="ksql_history_result_div">
                                                <div id="ksql_history_result0">
                                                    <div class="table-responsive">
                                                        <table id="ksql_history_result"
                                                               class="table table-striped table-bordered"
                                                               width="100%">
                                                            <thead>
                                                            <tr>
                                                                <th>#ID</th>
                                                                <th>User</th>
                                                                <th>Host</th>
                                                                <th>KSQL</th>
                                                                <th>Status</th>
                                                                <th>Spent</th>
                                                                <th>Created</th>
                                                            </tr>
                                                            </thead>
                                                        </table>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ksql detail -->
        <div class="modal fade" id="ke_sql_query_detail" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">KSQL Detail</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <textarea id="ke_sql_query_content" name="ke_sql_query_content" class="form-control" rows="3"
                                  cols="10" placeholder="" readonly style="height: 420px;"></textarea>
                    </div>
                    <div id="remove_div" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>
    </main>
    <!--end page main-->


    <!--start overlay-->
    <div class="overlay nav-toggle-icon"></div>
    <!--end overlay-->

    <!--Start Back To Top Button-->
    <a href="javaScript:;" class="back-to-top"><i class='bx bxs-up-arrow-alt'></i></a>
    <!--End Back To Top Button-->

</div>
<!--end wrapper-->

<!-- import js and plugins -->
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="main/topic/ksql.js?v=3.0.0" name="loader"/>
    <jsp:param value="main/topic/ksql.history.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/codemirror/codemirror.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql.js" name="loader"/>
    <jsp:param value="plugins/codemirror/show-hint.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql-hint.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
</body>
</html>
