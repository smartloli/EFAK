<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="User" name="loader"/>
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
            <div class="breadcrumb-title pe-3">System</div>
            <div class="ps-3">
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-0 p-0">
                        <li class="breadcrumb-item"><a href="/"><i class="bx bx-home-alt"></i></a>
                        </li>
                        <li class="breadcrumb-item active" aria-current="page">User</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i> <strong>EFAK visit user config and management.</strong><br/>
                    <i class="bx bx-info-circle"></i> <strong>Users need to be assigned roles after they are
                    created.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <h6 class="mb-0 text-uppercase">System User Manager
            <div style="float: right!important;margin-top: -9px;">
                <button id="ke-add-user-btn" type="button"
                        class="btn btn-primary px-5">ADD
                </button>
            </div>
        </h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table id="efak_system_user_tab" class="table table-striped table-bordered" style="width:100%">
                        <thead>
                        <tr>
                            <th>#ID</th>
                            <th>UserName</th>
                            <th>RealName</th>
                            <th>Email</th>
                            <th>Password</th>
                            <th>Operate</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
        </div>

        <!-- add user modal -->
        <div class="modal fade" id="ke_user_add_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Add System User</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/user/add/" method="post"
                              onsubmit="return contextFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text">ID</span>
                                <input id="ke_rtxno_name" name="ke_rtxno_name" type="text"
                                       class="form-control" readonly aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">RealName</span>
                                <input id="ke_real_name" name="ke_real_name" type="text"
                                       class="form-control" placeholder="efak" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">UserName</span>
                                <input id="ke_user_name" name="ke_user_name" type="text"
                                       class="form-control" placeholder="efak" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">Email</span>
                                <input id="ke_user_email" name="ke_user_email" type="text"
                                       class="form-control" placeholder="efak@email.com" aria-label=""
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

        <!-- edit user profile -->
        <div class="modal fade" id="ke_user_modify_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Edit User Profile</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/user/modify/" method="post"
                              onsubmit="return contextModifyFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text">ID</span>
                                <input id="ke_rtxno_name_modify" name="ke_rtxno_name_modify" type="text"
                                       class="form-control" readonly aria-label=""
                                       aria-describedby="">
                                <input id="ke_user_id_modify" name="ke_user_id_modify"
                                       type="hidden">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">RealName</span>
                                <input id="ke_real_name_modify" name="ke_real_name_modify" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">UserName</span>
                                <input id="ke_user_name_modify" name="ke_user_name_modify" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">Email</span>
                                <input id="ke_user_email_modify" name="ke_user_email_modify" type="text"
                                       class="form-control" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="modal-footer">
                                <button type="submit" class="btn btn-primary">Submit
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- reset password -->
        <div class="modal fade" id="ke_user_reset_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Reset User Password</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/user/reset/" method="post"
                              onsubmit="return contextResetFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text"><i
                                    class="bx bx-lock"></i></span>
                                <input id="ke_user_new_pwd_reset" name="ke_user_new_pwd_reset" type="text"
                                       class="form-control" placeholder="Enter Your New Password" aria-label=""
                                       aria-describedby="">
                                <input id="ke_user_rtxno_reset" name="ke_user_rtxno_reset" type="hidden"
                                       class="form-control">
                            </div>
                            <div class="modal-footer">
                                <button type="submit" class="btn btn-primary">Submit
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- assign user role -->
        <div class="modal fade" id="ke_user_assign_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Assign User Role</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div id="ke_role_list"></div>
                        <div id="ke_user_assign_result"></div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
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
    <jsp:param value="main/system/user.js?v=3.0.0" name="loader"/>
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

    function contextFormValid() {
        var ke_rtxno_name = $("#ke_rtxno_name").val();
        var ke_real_name = $("#ke_real_name").val();
        var ke_user_name = $("#ke_user_name").val();
        var ke_user_email = $("#ke_user_email").val();
        if (ke_real_name == "Administrator" || ke_user_name == "admin") {
            errorNoti("Oops! Administrator or admin is not available.");
            return false;
        }
        if (ke_rtxno_name.length == 0 || ke_real_name.length == 0 || ke_user_name.length == 0 || ke_user_email.length == 0) {
            errorNoti("Add user information cannot be empty.");
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
            errorNoti("Oops! Administrator or admin is not available.");
            return false;
        }

        if (ke_rtxno_name_modify.length == 0 || ke_real_name_modify.length == 0 || ke_user_name_modify.length == 0 || ke_user_email_modify.length == 0) {
            errorNoti("Edit user information cannot be empty.");
            return false;
        }

        return true;
    }

    function contextResetFormValid() {
        var ke_user_new_pwd_reset = $("#ke_user_new_pwd_reset").val();
        var userResetRegular = /[\u4E00-\u9FA5]/;
        if (ke_user_new_pwd_reset.length == 0 || userResetRegular.test(ke_user_new_pwd_reset)) {
            errorNoti("Password can only be number and letters or special symbols.");
            return false;
        }

        return true;
    }
</script>
</body>
</html>
