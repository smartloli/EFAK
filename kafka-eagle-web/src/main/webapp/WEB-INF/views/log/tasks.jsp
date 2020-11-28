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

    <title>Tasks - KafkaEagle</title>
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
                <h1 class="mt-4">Topic Tasks Manager</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Topic Tasks Manager</a></li>
                    <li class="breadcrumb-item active">Overview</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Add topic task management, query or export data
                    task.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-users-cog"></i> Job Manager
                                <div style="float: right!important;">
                                    <a href="/log/add" class="btn btn-primary btn-sm">Add</a>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed"
                                           width="100%">
                                        <thead>
                                        <tr>
                                            <th>JobId</th>
                                            <th>KSQL</th>
                                            <th>Progress</th>
                                            <th>Spent</th>
                                            <th>Create</th>
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
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_user_add_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Add User</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <!-- add modal -->
                            <div class="modal-body">
                                <form role="form" action="/system/user/add/" method="post"
                                      onsubmit="return contextFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">RtxNo</span>
                                            </div>
                                            <input id="ke_rtxno_name" name="ke_rtxno_name" type="text"
                                                   class="form-control" placeholder="1000"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">RealName</span>
                                            </div>
                                            <input id="ke_real_name" name="ke_real_name" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">UserName</span>
                                            </div>
                                            <input id="ke_user_name" name="ke_user_name" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Email</span>
                                            </div>
                                            <input id="ke_user_email" name="ke_user_email" type="text"
                                                   class="form-control" placeholder="kafka-eagle@email.com"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div id="alert_mssage_add" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_add_label"></label>
                                        </div>
                                    </fieldset>

                                    <div id="remove_div" class="modal-footer">
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
                <!-- modal setting -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_setting_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">User Assign</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <div id="ke_role_list"></div>
                                <div id="alert_mssage_info"></div>
                            </div>
                            <div id="remove_div" class="modal-footer">
                                <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- modal modify -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_user_modify_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Modify User</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/system/user/modify/" method="post"
                                      onsubmit="return contextModifyFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">RtxNo</span>
                                            </div>
                                            <input id="ke_user_id_modify" name="ke_user_id_modify"
                                                   type="hidden" class="form-control" placeholder="1000">
                                            <input id="ke_rtxno_name_modify" name="ke_rtxno_name_modify" type="text"
                                                   class="form-control" placeholder="1000"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">RealName</span>
                                            </div>
                                            <input id="ke_real_name_modify" name="ke_real_name_modify" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">UserName</span>
                                            </div>
                                            <input id="ke_user_name_modify" name="ke_user_name_modify" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Email</span>
                                            </div>
                                            <input id="ke_user_email_modify" name="ke_user_email_modify" type="text"
                                                   class="form-control" placeholder="kafka-eagle@email.com"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div id="alert_mssage_modify" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_modify_label"></label>
                                        </div>
                                    </fieldset>
                                    <div id="remove_div" class="modal-footer">
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
                <!-- Reset -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_user_reset_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Reset User Password</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/system/user/reset/" method="post"
                                      onsubmit="return contextResetFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="form-group">
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <span class="input-group-text"><i class="fas fa-lock"></i></span>
                                                </div>
                                                <input id="ke_user_new_pwd_reset" name="ke_user_new_pwd_reset"
                                                       type="password" class="form-control" maxlength="16"
                                                       placeholder="Enter Your New Password">
                                                <input id="ke_user_rtxno_reset" name="ke_user_rtxno_reset" type="hidden"
                                                       class="form-control">
                                            </div>
                                        </div>
                                        <div id="alert_mssage_reset" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_reset_label"></label>
                                        </div>
                                    </fieldset>
                                    <div id="remove_div" class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-modify">Submit
                                        </button>
                                    </div>
                                </form>
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
    <jsp:param value="main/log/tasks.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
<script type="text/javascript">
    function contextFormValid() {
        var ke_rtxno_name = $("#ke_rtxno_name").val();
        var ke_real_name = $("#ke_real_name").val();
        var ke_user_name = $("#ke_user_name").val();
        var ke_user_email = $("#ke_user_email").val();
        if (ke_real_name == "Administrator" || ke_user_name == "admin") {
            $("#alert_mssage_add").show();
            $("#alert_mssage_add_label").text("Oops! Administrator or admin is not available.");
            setTimeout(function () {
                $("#alert_mssage_add").hide()
            }, 3000);
            return false;
        }
        if (ke_rtxno_name.length == 0 || ke_real_name.length == 0 || ke_user_name.length == 0 || ke_user_email.length == 0) {
            $("#alert_mssage_add").show();
            $("#alert_mssage_add_label").text("Oops! Please enter the complete information.");
            setTimeout(function () {
                $("#alert_mssage_add").hide()
            }, 3000);
            return false;
        }

        return true;
    }

    function contextModifyFormValid() {
        var ke_rtxno_name_modify = $("#ke_rtxno_name_modify").val();
        var ke_real_name_modify = $("#ke_real_name_modify").val();
        var ke_user_name_modify = $("#ke_user_name_modify").val();
        var ke_user_email_modify = $("#ke_user_email_modify").val();

        if (ke_real_name_modify == "Administrator" || ke_user_name_modify == "admin") {
            $("#alert_mssage_modify").show();
            $("#alert_mssage_modify_label").text("Oops! Administrator or admin is not available.");
            setTimeout(function () {
                $("#alert_mssage_modify").hide()
            }, 3000);
            return false;
        }

        if (ke_rtxno_name_modify.length == 0 || ke_real_name_modify.length == 0 || ke_user_name_modify.length == 0 || ke_user_email_modify.length == 0) {
            $("#alert_mssage_modify").show();
            $("#alert_mssage_modify_label").text("Oops! Please enter the complete information.");
            setTimeout(function () {
                $("#alert_mssage_modify").hide()
            }, 3000);
            return false;
        }

        return true;
    }

    function contextResetFormValid() {
        var ke_user_new_pwd_reset = $("#ke_user_new_pwd_reset").val();
        var userResetRegular = /[\u4E00-\u9FA5]/;
        if (ke_user_new_pwd_reset.length == 0 || userResetRegular.test(ke_user_new_pwd_reset)) {
            $("#alert_mssage_reset").show();
            $("#alert_mssage_reset_label").text("Passwords can only be number and letters or special symbols .");
            setTimeout(function () {
                $("#alert_mssage_reset").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
