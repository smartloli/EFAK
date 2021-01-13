<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Alarm - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp">
        <jsp:param value="plugins/select2/select2.min.css" name="css"/>
    </jsp:include>
</head>

<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
<div id="layoutSidenav">
    <div id="layoutSidenav_nav">
        <jsp:include page="../public/plus/navbar.jsp"></jsp:include>
    </div>
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid">
                <h1 class="mt-4">AlarmConsumer</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">AlarmConsumer</a></li>
                    <li class="breadcrumb-item active">Add</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Add an alarm to
                    the topic being consumed.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Consumer Setting
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-lg-12">
                                        <form role="form" action="/alarm/add/form" method="post"
                                              onsubmit="return contextConsumerFormValid();return false;">
                                            <div class="form-group">
                                                <label>Consumer Group (*)</label> <select
                                                    id="select2consumergroup" name="select2consumergroup"
                                                    tabindex="-1"
                                                    style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                                <input id="ke_alarm_consumer_group"
                                                       name="ke_alarm_consumer_group" type="hidden"/><label
                                                    for="inputError" class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Select the consumer group you
                                                need to alarm .</label>
                                            </div>
                                            <div class="form-group">
                                                <label>Consumer Topic (*)</label>
                                                <div id="div_select_consumer_topic"></div>
                                                <input id="ke_alarm_consumer_topic"
                                                       name="ke_alarm_consumer_topic" type="hidden"/><label
                                                    for="inputError" class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Select the consumer topic you
                                                need to alarm .</label>
                                            </div>
                                            <div class="form-group">
                                                <label>Lag Threshold (*)</label> <input id="ke_topic_lag"
                                                                                        name="ke_topic_lag"
                                                                                        class="form-control"
                                                                                        maxlength=50
                                                                                        value="1"> <label
                                                    for="inputError"
                                                    class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Setting the lag threshold,
                                                input must be numeric .</label>
                                            </div>
                                            <div class="form-group">
                                                <label>Alarm Level (*)</label> <select id="select2level"
                                                                                       name="select2level" tabindex="-1"
                                                                                       style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                                <input id="ke_alarm_cluster_level"
                                                       name="ke_alarm_cluster_level" type="hidden"/> <label
                                                    for="inputError" class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Select the cluster level you
                                                need to alarm .</label>
                                            </div>
                                            <div class="form-group">
                                                <label>Alarm Max Times (*)</label> <select
                                                    id="select2maxtimes" name="select2maxtimes" tabindex="-1"
                                                    style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                                <input id="ke_alarm_cluster_maxtimes"
                                                       name="ke_alarm_cluster_maxtimes" type="hidden"/> <label
                                                    for="inputError" class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Select the cluster alarm max
                                                times you need to alarm .</label>
                                            </div>
                                            <div class="form-group">
                                                <label>Alarm Group (*)</label> <select id="select2group"
                                                                                       name="select2group" tabindex="-1"
                                                                                       style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>
                                                <input id="ke_alarm_cluster_group"
                                                       name="ke_alarm_cluster_group" type="hidden"/> <label
                                                    for="inputError" class="control-label text-danger"><i
                                                    class="fa fa-info-circle"></i> Select the cluster alarm
                                                group you need to alarm .</label>
                                            </div>
                                            <button type="submit" class="btn btn-success">Add</button>
                                            <div id="alert_consumer_message" style="display: none"
                                                 class="alert alert-danger">
                                                <label>Oops! Please make some changes . (*) is
                                                    required .</label>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <jsp:include page="../public/plus/footer.jsp"></jsp:include>
    </div>
</div>
</body>
<jsp:include page="../public/plus/script.jsp">
    <jsp:param value="main/alarm/add.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
</jsp:include>
<script type="text/javascript">
    function contextConsumerFormValid() {
        var ke_alarm_consumer_group = $("#ke_alarm_consumer_group").val();
        var ke_alarm_consumer_topic = $("#ke_alarm_consumer_topic").val();
        var ke_topic_lag = $("#ke_topic_lag").val();
        var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();
        var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();
        var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();

        if (ke_alarm_consumer_group.length == 0 || ke_alarm_consumer_topic.length == 0 || ke_topic_lag.length == 0 || ke_alarm_cluster_level.length == 0 || ke_alarm_cluster_maxtimes.length == 0 || ke_alarm_cluster_group.length == 0) {
            $("#alert_consumer_message").show();
            setTimeout(function () {
                $("#alert_consumer_message").hide()
            }, 3000);
            return false;
        }

        if (isNaN(ke_topic_lag)) {
            $("#alert_consumer_message").show();
            setTimeout(function () {
                $("#alert_consumer_message").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
