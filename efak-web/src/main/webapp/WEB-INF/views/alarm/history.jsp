<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="History" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">AlarmCommon</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">List</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i> <strong>MaxTime: -1 means no limit.</strong><br/>
                    <i class="bx bx-info-circle"></i> <strong>Level: P0 is the highest level.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <!-- kafka table list -->
        <h6 class="mb-0 text-uppercase">Alarm Common List</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_alarm_common_tab" class="table table-striped table-bordered" style="width:100%">
                        <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Type</th>
                            <th>Value</th>
                            <th>Name</th>
                            <th>Times</th>
                            <th>MaxTimes</th>
                            <th>Level</th>
                            <th>IsNormal</th>
                            <th>IsEnable</th>
                            <th>Created</th>
                            <th>Modify</th>
                            <th>Operate</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- delete consumer -->
        <div class="modal fade" id="alarm_cluster_remove" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Delete Common Alert</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div id="efak_alarm_cluster_remove_content" class="modal-body"></div>
                    <div id="efak_alarm_cluster_remove_footer" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- edit consumer -->
        <div class="modal fade" id="alarm_cluster_modify" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Edit Common Alert</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/alarm/history/modify/" method="post"
                              onsubmit="return contextModifyFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text">Server</span>
                                <input id="ke_alarm_cluster_name_server" name="ke_alarm_cluster_name_server" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
                                <input id="ke_alarm_cluster_id_server" name="ke_alarm_cluster_id_server"
                                       type="hidden">
                            </div>
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="select2group">AlarmGroup</label>
                                <select class="custom-select" id="select2group" name="select2group"
                                        tabindex="-1"
                                        style="width: 353px;height: 38px; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_alarm_cluster_group"
                                       name="ke_alarm_cluster_group" type="hidden"/>
                            </div>
                            <div class="input-group mb-3">
                                <div class="input-group-prepend">
                                    <label class="input-group-text" for="select2maxtimes">MaxTimes</label>
                                </div>
                                <select class="custom-select" id="select2maxtimes" name="select2maxtimes"
                                        tabindex="-1"
                                        style="width: 367px;height: 38px; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_alarm_cluster_maxtimes"
                                       name="ke_alarm_cluster_maxtimes" type="hidden"/>
                            </div>
                            <div class="input-group mb-3">
                                <div class="input-group-prepend">
                                    <label class="input-group-text" for="select2level">Levels</label>
                                </div>
                                <select class="custom-select" id="select2level" name="select2level"
                                        tabindex="-1"
                                        style="width: 394px;height: 38px; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                <input id="ke_alarm_cluster_level"
                                       name="ke_alarm_cluster_level" type="hidden"/>
                            </div>
                            <div class="modal-footer">
                                <button type="submit" class="btn btn-primary" id="create-add">Submit
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- detail -->
        <div class="modal fade" id="ke_alarm_cluster_detail" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Detail Common Alert</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <textarea id="ke_alarm_cluster_property"
                                  name="ke_alarm_cluster_property" class="form-control"
                                  readonly="readonly" rows="3"></textarea>
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
    <jsp:param value="plugins/notifications/lobibox.min.js" name="loader"/>
    <jsp:param value="plugins/notifications/notifications.min.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
    <jsp:param value="main/alarm/history.js?v=3.0.0" name="loader"/>
</jsp:include>
<jsp:include page="../public/pro/tscript.jsp"></jsp:include>
<script type="text/javascript">

    function errorNoti(errorMsg) {
        console.log(errorMsg)
        Lobibox.notify('error', {
            pauseDelayOnHover: true,
            continueDelayOnInactiveTab: false,
            position: 'top right',
            icon: 'bx bx-x-circle',
            msg: errorMsg
        });
    }

    function contextModifyFormValid() {
        var ke_alarm_cluster_name_server = $("#ke_alarm_cluster_name_server").val();
        var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();
        var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();
        var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();

        if (ke_alarm_cluster_name_server.length == 0 || ke_alarm_cluster_group.length == 0 || ke_alarm_cluster_maxtimes.length == 0 || ke_alarm_cluster_level.length == 0) {
            errorNoti("Alarm common type edit cannot be empty.");
            return false;
        }

        return true;
    }

</script>
</body>
</html>
