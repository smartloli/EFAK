<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Resource" name="loader"/>
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
                        <li class="breadcrumb-item active" aria-current="page">Resource</li>
                    </ol>
                </nav>
            </div>
        </div>
        <!--end breadcrumb-->
        <hr/>
        <div class="row">
            <div class="container">
                <div class="alert border-0 bg-light-info alert-dismissible fade show py-2 text-info">
                    <i class="bx bx-info-circle"></i> <strong>EFAK display system resource module.</strong><br/>
                    <i class="bx bx-info-circle"></i> <strong>Administrator role can add, modify and delete it.</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </div>
        </div>
        <!--end row-->
        <!-- content body -->
        <h6 class="mb-0 text-uppercase">System Resource Manager
        </h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div class="row">
                    <div class="col-lg-12">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <button type="button" id="config-home-btn"
                                        class="btn btn-sm btn-success">
                                    <span class="fas fa-edit"></span> Home
                                </button>
                                <button type="button" id="config-children-btn"
                                        class="btn btn-sm btn-primary">
                                    <span class="fas fa-sitemap"></span> Children
                                </button>
                                <button type="button" id="config-delete-btn"
                                        class="btn btn-sm btn-danger">
                                    <span class="far fa-trash-alt"></span> Delete
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <h6 class="mb-0 text-uppercase">System Resource Graph
        </h6>
        <hr/>
        <div class="card">
            <div class="card-body">
                <div id="ke_graph_home" style="width: 100%;height: 600px"></div>
            </div>
        </div>

        <!-- home modal -->
        <div class="modal fade" id="ke_home_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Resource Home</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/resource/add/home/"
                              method="post" onsubmit="return contextFormValid();return false;">
                            <div class="input-group mb-3"><span class="input-group-text">Name</span>
                                <input id="ke_resource_home_name" name="ke_resource_home_name" type="text"
                                       class="form-control" aria-label="" placeholder="Resource"
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">URL</span>
                                <input id="ke_resource_home_url" name="ke_resource_home_url" type="text"
                                       class="form-control" placeholder="/system/resource" aria-label=""
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

        <!-- children modal -->
        <div class="modal fade" id="ke_child_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Resource Children</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/resource/add/children/"
                              method="post" onsubmit="return contextChildFormValid();return false;">
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="res_parent_id">Home</label>
                                <select class="form-select" id="res_parent_id" name="res_parent_id">
                                </select>
                            </div>

                            <div class="input-group mb-3"><span class="input-group-text">Name</span>
                                <input id="ke_resource_child_name" name="ke_resource_child_name" type="text"
                                       class="form-control" placeholder="Resource" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div class="input-group mb-3"><span class="input-group-text">URL</span>
                                <input id="ke_resource_child_url" name="ke_resource_child_url" type="text"
                                       class="form-control" placeholder="/system/resource/add" aria-label=""
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

        <!-- delete dialog -->
        <div class="modal fade" id="ke_delete_dialog" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Resource Delete</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form role="form" action="/system/resource/delete/parent/or/children/"
                              method="post" onsubmit="return contextDeleteFormValid();return false;">
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="res_child_root_id">Home</label>
                                <select class="form-select" id="res_child_root_id" name="res_child_root_id">
                                </select>
                            </div>
                            <div class="input-group mb-3">
                                <label class="input-group-text" for="res_child_id">Children</label>
                                <select class="form-select" id="res_child_id" name="res_child_id">
                                </select>
                            </div>
                            <div class="modal-footer">
                                <button type="submit" class="btn btn-danger">Delete
                                </button>
                            </div>
                        </form>
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
    <jsp:param value="plugins/echart/echarts.min.js?v=3.0.0" name="loader"/>
    <jsp:param value="main/system/resource.js?v=3.0.0" name="loader"/>
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
        var ke_resource_home_name = $("#ke_resource_home_name").val();
        var ke_resource_home_url = $("#ke_resource_home_url").val();
        var reg = /^[\u4e00-\u9fa5]+$/;
        if (ke_resource_home_url.length == 0 || ke_resource_home_name.length == 0 || reg.test(ke_resource_home_url)) {
            errorNoti("Add resource name or url cannot be null.");
            return false;
        }

        return true;
    }

    function contextChildFormValid() {
        var ke_resource_child_name = $("#ke_resource_child_name").val();
        var ke_resource_child_url = $("#ke_resource_child_url").val();
        var reg = /^[\u4e00-\u9fa5]+$/;
        if (ke_resource_child_url.length == 0 || ke_resource_child_name.length == 0 || reg.test(ke_resource_child_url)) {
            errorNoti("Add resource children name or url cannot be null.");
            return false;
        }

        return true;
    }

    function contextDeleteFormValid() {
        var res_child_root_id = $("#res_child_root_id").val();
        var res_child_id = $("#res_child_id").val();
        if (res_child_root_id.length == 0 || res_child_id.length == 0) {
            errorNoti("Delete resource root or children name cannot be null.");
            return false;
        }

        return true;
    }
</script>
</body>
</html>
