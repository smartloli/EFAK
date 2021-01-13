<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html lang="zh">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Config - KafkaEagle</title>
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
                <h1 class="mt-4">Connect</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Config</a></li>
                    <li class="breadcrumb-item active">Overview</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Kafka connect uri config and manager.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-link"></i> Connect Config Manager
                                <div style="float: right!important;">
                                    <button id="ke-add-connect-uri-btn" type="button"
                                            class="btn btn-primary btn-sm">Add
                                    </button>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed"
                                           width="100%">
                                        <thead>
                                        <tr>
                                            <th>URI</th>
                                            <th>Version</th>
                                            <th>Alive</th>
                                            <th>Create</th>
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
                <!-- row -->
                <!-- Add User -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_connect_uri_add_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Add Connect URI</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <!-- add modal -->
                            <div class="modal-body">
                                <form role="form" action="/connect/uri/add/" method="post"
                                      onsubmit="return contextFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">URI</span>
                                            </div>
                                            <input id="ke_connect_uri_name" name="ke_connect_uri_name" type="text"
                                                   class="form-control" placeholder="http://127.0.0.1:8083/"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div id="alert_mssage_connect_uri_add" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_connect_uri_add_label"></label>
                                        </div>
                                    </fieldset>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-add">Submit
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- modal modify -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true"
                     id="ke_connect_uri_modify_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Modify Connect URI</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/connect/uri/modify/" method="post"
                                      onsubmit="return contextModifyFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">URI</span>
                                            </div>
                                            <input id="ke_connect_uri_name_modify" name="ke_connect_uri_name_modify"
                                                   type="text"
                                                   class="form-control" placeholder="http://127.0.0.1:8083/"
                                                   aria-describedby="basic-addon3">
                                            <input id="ke_connect_uri_id_modify" name="ke_connect_uri_id_modify"
                                                   type="hidden">
                                        </div>
                                        <div id="alert_mssage_connect_uri_modify" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_connect_uri_modify_label"></label>
                                        </div>
                                    </fieldset>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary">Submit</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Delete -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_connect_config_delete"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Notify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div id="ke_connect_config_remove_content" class="modal-body"></div>
                            <div id="ke_connect_config_footer" class="modal-footer">
                            </div>
                        </div>
                    </div>
                </div>
                <!-- connectors -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_connectors_detail"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Notify</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <div class="card mb-4">
                                    <div class="card-header">
                                        <i class="fas fa-comments"></i> Connect Detail
                                    </div>
                                    <div id="ke_connectors_detail_children" class="card-body">

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
    <jsp:param value="main/connect/config.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
<script type="text/javascript">
    function contextFormValid() {
        var ke_connect_uri_name = $("#ke_connect_uri_name").val();
        if (ke_connect_uri_name.length == 0) {
            $("#alert_mssage_connect_uri_add").show();
            $("#alert_mssage_connect_uri_add_label").text("Oops! Kafka connect uri cannot be empty.");
            setTimeout(function () {
                $("#alert_mssage_connect_uri_add").hide()
            }, 3000);
            return false;
        }

        return true;
    }

    function contextModifyFormValid() {
        var ke_connect_uri_name_modify = $("#ke_connect_uri_name_modify").val();
        if (ke_connect_uri_name_modify.length == 0) {
            $("#alert_mssage_connect_uri_modify").show();
            $("#alert_mssage_connect_uri_modify_label").text("Oops! Kafka connect uri cannot be empty.");
            setTimeout(function () {
                $("#alert_mssage_connect_uri_modify").hide()
            }, 3000);
            return false;
        }

        return true;
    }

</script>
</html>
