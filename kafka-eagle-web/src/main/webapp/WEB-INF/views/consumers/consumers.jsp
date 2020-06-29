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

    <title>Consumers - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp"></jsp:include>
    <jsp:include page="../public/plus/tcss.jsp"></jsp:include>
</head>
<!-- active graph -->
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
                <h1 class="mt-4">Consumers</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Consumers</a></li>
                    <li class="breadcrumb-item active">Groups</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>List all consumer groups.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-users"></i> Consumers Info
                            </div>
                            <div class="card-body">
                                <div id="kafka_cluster_info" class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed" width="100%">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Group</th>
                                            <th>Topics</th>
                                            <th>Node</th>
                                            <th>Active Topics</th>
                                            <th>Active Threads</th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Active Consumer -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-comments"></i> Active Topic
                            </div>
                            <div class="card-body">
                                <div id="active_topic" style="width: 100%;height: 600px"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Consumer Topic Detail -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_consumer_topics_detail"
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
                                        <i class="fas fa-comments"></i> Consumer Topic
                                    </div>
                                    <div id="consumer_detail_children" class="card-body">

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
    <jsp:param value="plugins/echart/echarts.min.js" name="loader" />
    <jsp:param value="main/consumer/consumers.js?v=1.4.5" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
