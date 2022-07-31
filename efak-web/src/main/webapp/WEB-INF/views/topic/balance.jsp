<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Balance" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2-bootstrap4.css" name="css"/>
        <jsp:param value="plugins/codemirror/codemirror.css" name="css"/>
        <jsp:param value="plugins/codemirror/show-hint.css" name="css"/>
        <jsp:param value="plugins/codemirror/code-dark.css" name="css"/>
    </jsp:include>
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
                        <li class="breadcrumb-item active" aria-current="page">Balance</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->

        <div class="row">
            <div class="col-xl-12 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Topic Property</h6>
                            <hr/>
                            <div class="col-12">
                                <label class="form-label">Topic Type (*)</label>
                                <br/>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="ke_topic_balance_type"
                                           id="ke_topic_balance_type_single" value="balance_single" checked="">
                                    <label class="form-check-label" for="ke_topic_balance_type_single">Single</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="ke_topic_balance_type"
                                           id="ke_topic_balance_type_all" value="balance_all">
                                    <label class="form-check-label" for="ke_topic_balance_type_all">All</label>
                                </div>
                            </div>
                            <hr/>
                            <div class="col-12">
                                <label class="form-label">Topic Name (*)</label>
                                <br/>
                                <select id="select2val" name="select2val" tabindex="-1"
                                        style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_topic_balance" name="ke_topic_balance" type="hidden"/>
                                <label for="inputError" class="control-label text-danger">
                                    <i class="bx bx-info-circle"></i> Choice the topic you need to balance .
                                </label>
                            </div>
                            <br/>
                            <button id="ke_balancer_generate" type="button" class="btn btn-primary">Generate
                            </button>
                            <button id="ke_balancer_execute" type="button" class="btn btn-success"
                                    style="display: none">Execute
                            </button>
                            <button id="ke_balancer_verify" type="button" class="btn btn-info"
                                    style="display: none">Verify
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!--end row-->

        <!-- Content -->
        <div class="row">
            <!-- Proposed -->
            <div class="col-xl-6 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Proposed Partition Reassignment Configuartion</h6>
                            <hr/>
                            <div class="card">
                                <div class="card-body">
                                    <form>
                                        <textarea id="code_proposed" name="code_proposed"></textarea>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Current -->
            <div class="col-xl-6 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Current Partition Replica Assignment</h6>
                            <hr/>
                            <div class="card">
                                <div class="card-body">
                                    <form>
                                        <textarea id="code_current" name="code_current"></textarea>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Balance logs -->
        <div class="row">
            <!-- Result -->
            <div class="col-xl-12 mx-auto">
                <div class="card">
                    <div class="card-body">
                        <div class="border p-3 rounded">
                            <h6 class="mb-0 text-uppercase">Result</h6>
                            <hr/>
                            <div class="card">
                                <div class="card-body">
                                    <form>
                                        <textarea id="code_result" name="code_result"></textarea>
                                    </form>
                                </div>
                            </div>
                        </div>
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
    <jsp:param value="plugins/notifications/lobibox.min.js" name="loader"/>
    <jsp:param value="plugins/notifications/notifications.min.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
    <jsp:param value="main/topic/hub.js?v=3.0.0" name="loader"/>
    <jsp:param value="plugins/codemirror/codemirror.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql.js" name="loader"/>
    <jsp:param value="plugins/codemirror/show-hint.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql-hint.js" name="loader"/>
</jsp:include>
</body>
<script type="text/javascript">
    function contextFormValid() {
        var ke_topic_name = $("#ke_topic_name").val();
        var ke_topic_partition = $("#ke_topic_partition").val();
        var ke_topic_repli = $("#ke_topic_repli").val();
        var reg = /^[A-Za-z0-9_-]+$/;
        var digit = /^[0-9]+$/;
        if (ke_topic_name.length == 0 || !reg.test(ke_topic_name)) {
            error_noti("Topic name input does not conform to naming conventions.")
            return false;
        }
        if (ke_topic_partition.length == 0 || !digit.test(ke_topic_partition)) {
            error_noti("Topic partitions is not null or must be number.")
            return false;
        }
        if (ke_topic_repli.length == 0 || !digit.test(ke_topic_repli)) {
            error_noti("Topic replication is not null or must be number.")
            return false;
        }
        return true;
    }

    function error_noti(errorMsg) {
        console.log(errorMsg)
        Lobibox.notify('error', {
            pauseDelayOnHover: true,
            continueDelayOnInactiveTab: false,
            position: 'top right',
            icon: 'bx bx-x-circle',
            msg: errorMsg
        });
    }
</script>
</html>
