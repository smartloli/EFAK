<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="fmt" %>

<!-- Navigation -->
<nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
    <a class="navbar-brand" href="/"><img src="/media/img/ke_login.png"
                                          style="width: 25px; border: 2px solid #fff; border-radius: 50%;"/> Kafka Eagle<sup>+</sup></a>
    <button class="btn btn-link btn-sm order-1 order-lg-0" id="sidebarToggle" href="#">
        <i class="fas fa-bars"></i>
    </button>
    <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_account_reset_dialog" tabindex="-1"
         role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" id="keModalLabel">Reset password</h4>
                    <button class="close" type="button" data-dismiss="modal">x</button>
                </div>
                <!-- /.row -->
                <form role="form" action="/account/reset/" method="post"
                      onsubmit="return contextPasswdFormValid();return false;">
                    <div class="modal-body">
                        <fieldset class="form-horizontal">
                            <div class="form-group">
                                <div class="input-group mb-3">
                                    <div class="input-group-prepend">
                                        <span class="input-group-text"><i class="fas fa-lock"></i></span>
                                    </div>
                                    <input id="ke_new_password_name" name="ke_new_password_name" type="password"
                                           class="form-control" maxlength="16" placeholder="Enter Your New Password">
                                </div>
                            </div>
                            <div id="alert_mssage" style="display: none" class="alert alert-danger">
                                <label> Passwords can only be number and letters or special symbols .</label>
                            </div>
                        </fieldset>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancle</button>
                        <button type="submit" class="btn btn-primary" id="create-btn">Submit</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="d-none d-md-inline-block form-inline ml-auto mr-0 mr-md-3 my-2 my-md-0"></div>
    <!-- Navtop Cluster -->
    <ul class="navbar-nav ml-auto ml-md-0">
        <li class="nav-item dropdown"><a class="nav-link dropdown-toggle" id="clusterDropdown" href="#" role="button"
                                         data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i
                class="fas fa-sitemap fa-sm fa-fw mr-1"></i>${clusterAlias}</a>${clusterAliasList}</li>
    </ul>
    <ul class="navbar-nav ml-auto ml-md-0">
        <li class="nav-item dropdown"><a class="nav-link"><i class="fas fa-bookmark fa-sm fa-fw mr-1"></i>V2.0.6</a>
        </li>
    </ul>
    <!-- Navtop Login -->
    <ul class="navbar-nav ml-auto ml-md-0">
        <li class="nav-item dropdown"><a class="nav-link dropdown-toggle" id="userDropdown" href="#" role="button"
                                         data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i
                class="fas fa-user fa-sm fa-fw mr-1"></i>${LOGIN_USER_SESSION.realname}</a>
            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="userDropdown">
                <a name="ke_account_reset" class="dropdown-item" href="#"><i class="fas fa-cogs fa-sm fa-fw mr-1"></i>Reset</a>
                <a class="dropdown-item" href="/account/signout"><i class="fas fa-sign-out-alt fa-sm fa-fw mr-1"></i>Signout</a>
            </div>
        </li>
    </ul>
</nav>
<script type="text/javascript">
    function contextPasswdFormValid() {
        var ke_new_password_name = $("#ke_new_password_name").val();
        var resetRegular = /[\u4E00-\u9FA5]/;
        if (ke_new_password_name.length == 0 || resetRegular.test(ke_new_password_name)) {
            $("#alert_mssage").show();
            setTimeout(function () {
                $("#alert_mssage").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>