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

    <title>WorkNodes - KafkaEagle</title>
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
                <h1 class="mt-4">WorkNodes</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">WorkNodes</a></li>
                    <li class="breadcrumb-item active">Overview</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Kafka Eagle worknodes is used for ksql distributed query
                    and distributed task statistics.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Kafka Eagle WorkNodes
                            </div>
                            <div class="card-body">
                                <div id="kafka_cluster_worknodes_list" class="table-responsive">
                                    <table id="cluster_tab" class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>IP</th>
                                            <th>Port</th>
                                            <th>Memory(Used | Percent)</th>
                                            <th>CPU</th>
                                            <th>Status</th>
                                            <th>Created</th>
                                            <!--
                                            <th>Operate</th>
                                            -->
                                        </tr>
                                        </thead>
                                    </table>
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
    <jsp:param value="main/cluster/worknodes.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
