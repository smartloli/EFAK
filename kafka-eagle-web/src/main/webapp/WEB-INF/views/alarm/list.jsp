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
    <jsp:include page="../public/plus/css.jsp"></jsp:include>
    <jsp:include page="../public/plus/tcss.jsp"></jsp:include>
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
                    <li class="breadcrumb-item active">List</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Manage the alarm policy of the consumer group.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Channel List
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed"
                                           width="100%">
                                        <thead>
                                        <tr>
                                            <th>Cluster</th>
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
                    </div>
                </div>
                <!-- delete -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_alarm_config_delete"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Notify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div id="ke_alarm_consumer_remove_content" class="modal-body"></div>
                            <div id="remove_div" class="modal-footer">
                            </div>
                        </div>
                    </div>
                </div>
                <!-- modify -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_alarm_config_modify"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Modify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/alarm/config/modify/form/"
                                      method="post"
                                      onsubmit="return contextModifyConfigFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Server</span>
                                            </div>
                                            <input id="ke_alarm_group_m_name" name="ke_alarm_group_m_name"
                                                   type="hidden" class="form-control" placeholder="">
                                            <input id="ke_alarm_config_m_url" name="ke_alarm_config_m_url"
                                                   type="text"
                                                   class="form-control" placeholder=""
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text">Address</span>
                                            </div>
                                            <textarea id="ke_alarm_config_m_address" name="ke_alarm_config_m_address"
                                                      rows="3" class="form-control"
                                                      aria-label="With textarea"></textarea>
                                        </div>
                                        <div id="alert_config_message_modify" style="display: none"
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
                <!-- More detail info -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_alarm_config_detail"
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
									        <textarea id="ke_alarm_config_property"
                                                      name="ke_alarm_config_property" class="form-control"
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
    <jsp:param value="main/alarm/list.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
<script type="text/javascript">
    function contextModifyConfigFormValid() {
        var ke_alarm_config_m_url = $("#ke_alarm_config_m_url").val();

        if (ke_alarm_config_m_url.length == 0) {
            $("#alert_config_message_modify").show();
            setTimeout(function () {
                $("#alert_config_message_modify").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
