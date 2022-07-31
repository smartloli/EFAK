<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Alarm" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">Channel</div>
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
                    <i class="bx bx-info-circle"></i> <strong>Manage different alarm channels.</strong><br/>
                    <i class="bx bx-info-circle"></i> <strong>Currently, it supports the alarms of dingding, wechat,
                    email and some webhook interfaces.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <!-- kafka table list -->
        <h6 class="mb-0 text-uppercase">Channel List</h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_alarm_channel_tab" class="table table-striped table-bordered" style="width:100%">
                        <thead>
                        <tr>
                            <th>#Cluster</th>
                            <th>Alarm Group</th>
                            <th>Alarm Type</th>
                            <th>Alarm URL</th>
                            <th>Http Method</th>
                            <th>Address</th>
                            <th>Created</th>
                            <th>Modify</th>
                            <th>Operate</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- delete connector -->
        <div class="modal fade" id="ke_alarm_config_delete" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Delete Config</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div id="ke_alarm_config_remove_content" class="modal-body"></div>
                    <div id="ke_alarm_config_remove_footer" class="modal-footer">
                    </div>
                </div>
            </div>
        </div>

        <!-- edit connector -->
        <div class="modal fade" id="ke_alarm_config_modify" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Edit Config URI</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/alarm/config/modify/form/" method="post"
                              onsubmit="return contextModifyConfigFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text">URI</span>
                                <input id="ke_alarm_config_m_url" name="ke_alarm_config_m_url" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
                                <input id="ke_alarm_group_m_name" name="ke_alarm_group_m_name"
                                       type="hidden">
                            </div>
                            <div id="efak_alert_config_address" style="display: none" class="input-group mb-3"><span
                                    class="input-group-text">Address</span>
                                <input id="ke_alarm_config_m_address" name="ke_alarm_config_m_address" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
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
        <div class="modal fade" id="ke_alarm_config_detail" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Detail Config</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <textarea id="ke_alarm_config_property"
                                  name="ke_alarm_config_property" class="form-control"
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
    <jsp:param value="main/alarm/list.js?v=3.0.0" name="loader"/>
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

    function contextModifyConfigFormValid() {
        var ke_alarm_config_m_url = $("#ke_alarm_config_m_url").val();

        if (ke_alarm_config_m_url.length == 0) {
            errorNoti("Edit alarm url cannot be empty.");
            return false;
        }

        return true;
    }

</script>
</body>
</html>
