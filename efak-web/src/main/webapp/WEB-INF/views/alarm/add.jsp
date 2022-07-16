<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Add" name="loader"/>
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
            <div class="breadcrumb-title pe-3">AlarmConsumer</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">Add</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->

        <div class="row">
            <div class="col-xl-6 mx-auto">

                <div class="card">
                    <form role="form" action="/alarm/add/form"
                          method="post"
                          onsubmit="return contextConsumerFormValid();return false;">
                        <div class="card-body">
                            <div class="border p-3 rounded">
                                <h6 class="mb-0 text-uppercase">Configure Consumer Groups</h6>
                                <hr/>
                                <div class="col-12">
                                    <label class="form-label">Consumer Group (*)</label>
                                    <select id="select2consumergroup" name="select2consumergroup" tabindex="-1"
                                            style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                    <input id="ke_alarm_consumer_group" name="ke_alarm_consumer_group" type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the consumer group you need to alarm .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Consumer Topic (*)</label>
                                    <div id="div_select_consumer_topic"></div>
                                    <input id="ke_alarm_consumer_topic" name="ke_alarm_consumer_topic" type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the consumer topic you need to alarm .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Lag Threshold (*)</label>
                                    <input id="ke_topic_lag" name="ke_topic_lag" value="1" class="form-control">
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Setting the lag threshold,
                                        input must be numeric .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Alarm Level (*)</label>
                                    <select id="select2level" name="select2level" tabindex="-1"
                                            style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                    <input id="ke_alarm_cluster_level" name="ke_alarm_cluster_level" type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the alarm level you need to alarm .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Alarm Max Times (*)</label>
                                    <select id="select2maxtimes" name="select2maxtimes" tabindex="-1"
                                            style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                    <input id="ke_alarm_cluster_maxtimes" name="ke_alarm_cluster_maxtimes"
                                           type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the alarm max times you need to
                                        alarm .
                                    </label>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Alarm Group (*)</label>
                                    <select id="select2group" name="select2group" tabindex="-1"
                                            style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                    <input id="ke_alarm_cluster_group" name="ke_alarm_cluster_group"
                                           type="hidden"/>
                                    <label for="inputError" class="control-label text-danger">
                                        <i class="bx bx-info-circle"></i> Choice the alarm alarm channel group you need
                                        to
                                        alarm .
                                    </label>
                                </div>
                                <hr/>
                                <button id="btn_send" type="submit" class="btn btn-primary">Save</button>
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
    <jsp:param value="main/alarm/add.js?v=3.0.0" name="loader"/>
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

    function contextConsumerFormValid() {
        var ke_alarm_consumer_group = $("#ke_alarm_consumer_group").val();
        var ke_alarm_consumer_topic = $("#ke_alarm_consumer_topic").val();
        var ke_topic_lag = $("#ke_topic_lag").val();
        var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();
        var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();
        var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();

        if (ke_alarm_consumer_group.length == 0) {
            errorNoti("Alarm consumer group cannot be empty.");
            return false;
        }

        if (ke_alarm_consumer_topic.length == 0) {
            errorNoti("Alarm consumer topic cannot be empty.");
            return false;
        }

        if (ke_topic_lag.length == 0 || isNaN(ke_topic_lag)) {
            errorNoti("Alarm consumer topic lag cannot be empty.");
            return false;
        }

        if (ke_alarm_cluster_level.length == 0) {
            errorNoti("Alarm level cannot be empty.");
            return false;
        }

        if (ke_alarm_cluster_maxtimes.length == 0) {
            errorNoti("Alarm maxtimes cannot be empty.");
            return false;
        }

        if (ke_alarm_cluster_group.length == 0) {
            errorNoti("Alarm channel group cannot be empty.");
            return false;
        }

        return true;
    }
</script>
</html>
