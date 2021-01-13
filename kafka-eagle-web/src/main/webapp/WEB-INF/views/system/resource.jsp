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

    <title>Resource - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp"></jsp:include>
</head>
<style type="text/css">
    .node circle {
        cursor: pointer;
        fill: #fff;
        stroke: steelblue;
        stroke-width: 1.5px;
    }

    .node text {
        font-size: 14px;
    }

    path.link {
        fill: none;
        stroke: #ccc;
        stroke-width: 1.5px;
    }
</style>
<body>
<jsp:include page="../public/plus/navtop.jsp"></jsp:include>
<div id="layoutSidenav">
    <div id="layoutSidenav_nav">
        <jsp:include page="../public/plus/navbar.jsp"></jsp:include>
    </div>
    <div id="layoutSidenav_content">
        <main>
            <div class="container-fluid">
                <h1 class="mt-4">Resource</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Resource</a></li>
                    <li class="breadcrumb-item active">Manager</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Display system
                    resource module, you can add, modify and delete it.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Resource Manager
                            </div>
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
                    </div>
                </div>
                <!-- graph -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Resource Graph
                            </div>
                            <div class="card-body">
                                <div id="ke_graph_home" style="width: 100%;height: 600px"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- home -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_home_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Home</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/system/resource/add/home/"
                                      method="post" onsubmit="return contextFormValid();return false;">
                                    <div class="modal-body">
                                        <fieldset class="form-horizontal">
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <span class="input-group-text" id="basic-addon3">Name</span>
                                                </div>
                                                <input id="ke_resource_home_name" name="ke_resource_home_name"
                                                       type="text"
                                                       class="form-control" placeholder="Resource"
                                                       aria-describedby="basic-addon3">
                                            </div>
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <span class="input-group-text" id="basic-addon3">URL</span>
                                                </div>
                                                <input id="ke_resource_home_url" name="ke_resource_home_url" type="text"
                                                       class="form-control" placeholder="/system/resource"
                                                       aria-describedby="basic-addon3">
                                            </div>
                                            <div id="alert_mssage" style="display: none"
                                                 class="alert alert-danger">
                                                <label> Oops! Please make some changes .</label>
                                            </div>
                                        </fieldset>
                                    </div>
                                    <div id="remove_div" class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-btn">Submit
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- children -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_child_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Children</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/system/resource/add/children/"
                                      method="post" onsubmit="return contextChildFormValid();return false;">
                                    <div class="modal-body">
                                        <fieldset class="form-horizontal">
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <label class="input-group-text" for="res_parent_id">Home</label>
                                                </div>
                                                <select class="custom-select" id="res_parent_id" name="res_parent_id">
                                                </select>
                                            </div>
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <span class="input-group-text" id="basic-addon3">Name</span>
                                                </div>
                                                <input id="ke_resource_child_name" name="ke_resource_child_name"
                                                       type="text"
                                                       class="form-control" placeholder="Resource"
                                                       aria-describedby="basic-addon3">
                                            </div>
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <span class="input-group-text" id="basic-addon3">URL</span>
                                                </div>
                                                <input id="ke_resource_child_url" name="ke_resource_child_url"
                                                       type="text"
                                                       class="form-control" placeholder="/system/resource/add"
                                                       aria-describedby="basic-addon3">
                                            </div>
                                            <div id="alert_mssage_child" style="display: none"
                                                 class="alert alert-danger">
                                                <label> Oops! Please make some changes .</label>
                                            </div>
                                        </fieldset>
                                    </div>
                                    <div id="remove_div" class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-btn">Submit
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- delete -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_delete_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Delete</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <form role="form" action="/system/resource/delete/parent/or/children/"
                                      method="post" onsubmit="return contextDeleteFormValid();return false;">
                                    <div class="modal-body">
                                        <fieldset class="form-horizontal">
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <label class="input-group-text" for="res_child_root_id">Home</label>
                                                </div>
                                                <select class="custom-select" id="res_child_root_id"
                                                        name="res_child_root_id">
                                                </select>
                                            </div>
                                            <div class="input-group mb-3">
                                                <div class="input-group-prepend">
                                                    <label class="input-group-text" for="res_child_id">Children</label>
                                                </div>
                                                <select class="custom-select" id="res_child_id" name="res_child_id">
                                                </select>
                                            </div>
                                            <div id="alert_mssage_delete" style="display: none"
                                                 class="alert alert-danger">
                                                <label> Oops! Please make some changes .</label>
                                            </div>
                                        </fieldset>
                                    </div>
                                    <div id="remove_div" class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-danger" id="create-btn">Delete
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
    <jsp:param value="plugins/echart/echarts.min.js" name="loader" />
    <jsp:param value="main/system/resource.js" name="loader"/>
</jsp:include>
<script type="text/javascript">
    function contextFormValid() {
        var ke_resource_home_name = $("#ke_resource_home_name").val();
        var ke_resource_home_url = $("#ke_resource_home_url").val();
        var reg = /^[\u4e00-\u9fa5]+$/;
        if (ke_resource_home_url.length == 0 || ke_resource_home_name.length == 0 || reg.test(ke_resource_home_url)) {
            $("#alert_mssage").show();
            setTimeout(function () {
                $("#alert_mssage").hide()
            }, 3000);
            return false;
        }

        return true;
    }

    function contextChildFormValid() {
        var ke_resource_child_name = $("#ke_resource_child_name").val();
        var ke_resource_child_url = $("#ke_resource_child_url").val();
        var reg = /^[\u4e00-\u9fa5]+$/;
        if (ke_resource_child_url.length == 0 || ke_resource_child_name.length == 0 || reg.test(ke_resource_child_url)) {
            $("#alert_mssage_child").show();
            setTimeout(function () {
                $("#alert_mssage_child").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
