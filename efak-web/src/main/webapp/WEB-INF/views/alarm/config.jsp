<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Config" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp">
        <jsp:param value="plugins/notifications/lobibox.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2.min.css" name="css"/>
        <jsp:param value="plugins/select2/select2-bootstrap4.css" name="css"/>
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
            <div class="breadcrumb-title pe-3">Alert</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Config</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->

        <div class="row">
            <div class="col-xl-6 mx-auto">

                <div class="card">
                    <form role="form" action="/alarm/config/storage/form/"
                          method="post"
                          onsubmit="return contextAlarmConfigFormValid();return false;">
                        <div class="card-body">
                            <div class="border p-3 rounded">
                                <h6 class="mb-0 text-uppercase">Configure alarm channels</h6>
                                <hr/>
                                <div class="col-12">
                                    <label class="form-label">Alarm Group Name (*)</label>
                                    <input id="ke_alarm_group_name" name="ke_alarm_group_name" class="form-control">
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Made up of letters and digits or
                                        underscores . Such as "alert_email_group" .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Alarm Type (*)</label>
                                    <select id="select2val" name="select2val" tabindex="-1"
                                            style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                    <input id="ke_alarm_type" name="ke_alarm_type" type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the type you need to alarm .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label id="label_alarm_url" class="form-label">Alarm URL (*)</label>
                                    <label id="label_alarm_topic"  class="form-label">Topic (*)</label>
                                    <input id="ke_alarm_url" name="ke_alarm_url" class="form-control" placeholder="Enter Alarm URL">
                                </div>
                                <div id="div_alarm_http" style="display: none" class="col-12">
                                    <label class="form-label">Http Method</label>
                                    <br/>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="ke_alarm_http"
                                               id="ke_alarm_http_get" value="get" checked="">
                                        <label class="form-check-label" for="ke_alarm_http_get">GET</label>
                                    </div>
                                    <div class="form-check form-check-inline">
                                        <input class="form-check-input" type="radio" name="ke_topic_balance_type"
                                               id="ke_alarm_http_post" value="post">
                                        <label class="form-check-label" for="ke_alarm_http_post">POST</label>
                                    </div>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the alarm http method.
                                    </label>
                                </div>
                                <div id="div_alarm_address" style="display: none" class="col-12">
                                    <label class="form-label">Address (*)</label>
                                    <input id="ke_alarm_address" name="ke_alarm_address" class="form-control">
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> You can enter multiple email or
                                        im addresses using a ";" separator.
                                    </label>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Test Message</label>
                                    <textarea id="ke_test_msg" name="ke_test_msg" class="form-control"
                                              placeholder="Limit 200 words." rows="4" maxlength="200"></textarea>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Write something and send
                                        message to owner .
                                    </label>
                                </div>
                                <hr/>
                                <button id="btn_send" type="submit" class="btn btn-primary">Save</button>
                                <button id="btn_send_test" type="button" class="btn btn-success">Send Test</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <!--end row-->
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
    <jsp:param value="main/alarm/config.js?v=3.0.0" name="loader"/>
</jsp:include>
</body>
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

    function contextAlarmConfigFormValid() {
        var ke_alarm_group_name = $("#ke_alarm_group_name").val();
        var ke_alarm_type = $("#ke_alarm_type").val();
        var ke_alarm_url = $("#ke_alarm_url").val();
        var ke_alarm_http = $("input:radio:checked").val(); // option
        var ke_alarm_address = $("#ke_alarm_address").val();

        if (ke_alarm_group_name.length == 0) {
            errorNoti("Alarm group name cannot be empty.");
            return false;
        }

        if (ke_alarm_type.length == 0) {
            errorNoti("Alarm type cannot be empty.");
            return false;
        }

        if (ke_alarm_url.length == 0) {
            errorNoti("Alarm url cannot be empty.");
            return false;
        }

        return true;
    }
</script>
</html>
