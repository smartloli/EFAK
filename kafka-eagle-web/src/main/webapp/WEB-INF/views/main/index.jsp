<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Dashboard - KafkaEagle</title>
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

    .liquidFillGaugeText {
        font-family: Helvetica;
        font-weight: bold;
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
                <h1 class="mt-4">Dashboard</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item active">Dashboard</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Dashboard display topic Kafka related information and
                    Kafka cluster information as well as Zookeeper cluster information.</strong> If you don't know the
                    usage of Kafka and Zookeeper, you can visit the website of <a href="http://kafka.apache.org/"
                                                                                  target="_blank" class="alert-link">Kafka</a>
                    and <a href="http://zookeeper.apache.org/" target="_blank" class="alert-link">Zookeeper</a> to view
                    the relevant usage.
                </div>
                <div class="row">
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Brokers
                                        </div>
                                        <a id="ke_dash_brokers_count" href="/cluster/info"
                                           class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-server fa-4x text-primary-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Topics
                                        </div>
                                        <a id="ke_dash_topics_count" href="/topic/list"
                                           class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-comment fa-4x text-success-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-info shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Zookeepers
                                        </div>
                                        <a id="ke_dash_zks_count" href="/cluster/info"
                                           class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-sitemap fa-4x text-info-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-dark shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-dark text-uppercase mb-1">
                                            ConsumerGroups
                                        </div>
                                        <a id="ke_dash_consumers_count" href="/consumers"
                                           class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-users fa-4x text-dark-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- panel end -->
                <!-- logsize and capacity -->
                <div class="row">
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-file-alt"></i> Topic LogSize Top10
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="topic_logsize" class="table table-bordered table-hover table-striped">
                                    </table>
                                </div>
                                <div class="text-right">
                                    <a href="/topic/list">View Details <i class="far fa-arrow-alt-circle-right"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-database"></i> Topic Capacity Top10
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="topic_capacity" class="table table-bordered table-hover table-striped">
                                    </table>
                                </div>
                                <div class="text-right">
                                    <a href="/topic/list">View Details <i class="far fa-arrow-alt-circle-right"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- byte in and byte out -->
                <div class="row">
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-sign-in-alt"></i> Topic ByteIn Top10
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="topic_byte_in" class="table table-bordered table-hover table-striped">
                                    </table>
                                </div>
                                <div class="text-right">
                                    <a href="/topic/list">View Details <i class="far fa-arrow-alt-circle-right"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-sign-out-alt"></i> Topic ByteOut Top10
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="topic_byte_out" class="table table-bordered table-hover table-striped">
                                    </table>
                                </div>
                                <div class="text-right">
                                    <a href="/topic/list">View Details <i class="far fa-arrow-alt-circle-right"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- memory and cpu -->
                <div class="row">
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-hdd"></i> Kafka OS Used Memory (%)
                            </div>
                            <div class="card-body">
                                <div id="ke_dash_os_memory_div">
                                    <svg id="fillgauge_kafka_memory" width="97%" height="424"></svg>
                                </div>
                                <div class="text-right">
                                    <a href="/metrics/kafka#os_free_memory"></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-hdd"></i> Kafka CPU Used Memory (%)
                            </div>
                            <div class="card-body">
                                <div id="ke_dash_cpu_div">
                                    <svg id="fillgauge_kafka_cpu" width="97%" height="424"></svg>
                                </div>
                                <div class="text-right">
                                    <a href="#"><i class=""></i></a>
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
                                <i class="fas fa-project-diagram"></i> Kafka Active Brokers
                            </div>
                            <div class="card-body">
                                <div id="ke_dash_brokers_graph" style="width: 100%;height: 600px"></div>
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
    <jsp:param value="plugins/d3/d3.v3.min.js" name="loader"/>
    <jsp:param value="plugins/d3/liquidFillGauge.js" name="loader"/>
    <jsp:param value="plugins/echart/echarts.min.js" name="loader"/>
    <jsp:param value="main/index.js" name="loader"/>
</jsp:include>
</html>
