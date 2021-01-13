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
    <jsp:include page="../public/plus/tcss.jsp"></jsp:include>
    <!-- switch css -->
    <style type="text/css">
        .chooseBtn {
            display: none;
        }

        .choose-label:hover {
            cursor: pointer
        }

        .choose-label {
            box-shadow: #ccc 0px 0px 0px 1px;
            width: 40px;
            height: 20px;
            display: inline-block;
            border-radius: 20px;
            position: relative;
            background-color: #bdbdbd;
            overflow: hidden;
        }

        .choose-label:before {
            content: '';
            position: absolute;
            left: 0;
            width: 20px;
            height: 20px;
            display: inline-block;
            border-radius: 20px;
            background-color: #fff;
            z-index: 20;
            -webkit-transition: all 0.5s;
            transition: all 0.5s;
        }

        .chooseBtn:checked + label.choose-label:before {
            left: 20px;
        }

        .chooseBtn:checked + label.choose-label {
            background-color: #2196F3;
        }
    </style>
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
                <h1 class="mt-4">AlarmCluster</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">AlarmCluster</a></li>
                    <li class="breadcrumb-item active">History</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Manage cluster alarm records .</strong><br/>
                    <i class="fas fa-info-circle"></i> <strong>MaxTime: -1 means no limit.</strong><br/>
                    <i class="fas fa-info-circle"></i> <strong>Level: P0 is the highest level.</strong><br/>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Alarm Cluster History
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed"
                                           width="100%">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
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
                    </div>
                </div>
                <!-- modal -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="alarm_cluster_remove"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Notify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div id="alarm_cluster_remove_content" class="modal-body"></div>
                            <div id="remove_div" class="modal-footer">
                            </div>
                        </div>
                    </div>
                </div>
                <!-- modify -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="alarm_cluster_modify"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Modify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/alarm/history/modify/" method="post"
                                      onsubmit="return contextModifyFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Server</span>
                                            </div>
                                            <input id="ke_alarm_cluster_id_server" name="ke_alarm_cluster_id_server"
                                                   type="hidden" class="form-control" placeholder="">
                                            <input id="ke_alarm_cluster_name_server" name="ke_alarm_cluster_name_server"
                                                   type="text"
                                                   class="form-control" placeholder="1000"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <label class="input-group-text" for="select2group">AlarmGroup</label>
                                            </div>
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
                                        <div id="alert_message_modify" style="display: none"
                                             class="alert alert-danger">
                                            <label> Oops! Please make some changes .</label>
                                        </div>
                                    </fieldset>

                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-modify">Submit
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- More then detail -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_alarm_cluster_detail"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Detail</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <fieldset class="form-horizontal">
                                    <div class="form-group">
                                        <div class="col-sm-12">
									        <textarea id="ke_alarm_cluster_property"
                                                      name="ke_alarm_cluster_property" class="form-control"
                                                      readonly="readonly" rows="3"></textarea>
                                        </div>
                                    </div>
                                </fieldset>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary"
                                        data-dismiss="modal">Cancle
                                </button>
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
    <jsp:param value="main/alarm/history.js" name="loader"/>
    <jsp:param value="plugins/select2/select2.min.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
<script type="text/javascript">
    function contextModifyFormValid() {
        var ke_alarm_cluster_name_server = $("#ke_alarm_cluster_name_server").val();
        var ke_alarm_cluster_group = $("#ke_alarm_cluster_group").val();
        var ke_alarm_cluster_maxtimes = $("#ke_alarm_cluster_maxtimes").val();
        var ke_alarm_cluster_level = $("#ke_alarm_cluster_level").val();

        if (ke_alarm_cluster_name_server.length == 0 || ke_alarm_cluster_group.length == 0 || ke_alarm_cluster_maxtimes.length == 0 || ke_alarm_cluster_level.length == 0) {
            $("#alert_message_modify").show();
            setTimeout(function () {
                $("#alert_message_modify").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
