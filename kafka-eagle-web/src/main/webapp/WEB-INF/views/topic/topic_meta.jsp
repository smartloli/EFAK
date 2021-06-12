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

    <style>
        .box {
            border-bottom: 1px solid #eee;
            margin-bottom: 20px;
            /* margin-top: 30px; */
            overflow: hidden;
        }

        .box .left {
            font-size: 36px;
            float: left
        }

        .box .left small {
            /* font-size: 24px;
            color: #777 */

        }

        .box .right {
            float: right;
            width: 260px;
            margin-top: -120px;
            background: #fff;
            cursor: pointer;
            padding: 5px 10px;
            border: 1px solid #ccc;
        }

        .charttopicdiv {
            width: 100%;
            height: 400px;
        }

        .CodeMirror {
            border-top: 1px solid #ddd;
            border-bottom: 1px solid #ddd;
            border-right: 1px solid #ddd;
            border-left: 1px solid #ddd;
        }
    </style>

    <title>Topic Meta - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp">
        <jsp:param value="plugins/codemirror/codemirror.css" name="css"/>
        <jsp:param value="plugins/datatimepicker/daterangepicker.css" name="css"/>
    </jsp:include>
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
                <div class="box">
                    <h1 class="mt-4">Topic</h1>
                    <ol class="breadcrumb mb-4">
                        <li class="breadcrumb-item"><a href="#">Topic</a></li>
                        <li class="breadcrumb-item"><a href="/topic/list">List</a></li>
                        <li class="breadcrumb-item active">MetaData</li>
                    </ol>
                    <div id="reportrange" class="right">
                        <i class="glyphicon glyphicon-calendar fa fa-calendar"></i> &nbsp; <span></span> <b
                            class="caret"></b>
                    </div>
                </div>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fa fa-info-circle"></i> <strong>Leader -1 indicates that the partition is not available or
                    is empty.</strong> <br/> <i class="fa fa-info-circle"></i> <strong>Preferred Leader: true is
                    preferred leader, false is risked leader.</strong> <br/> <i class="fa fa-info-circle"></i> <strong>Under
                    Replicated: false is normal, true is lost replicas.</strong>
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-xl-3 col-md-6 mb-4">
                        <div class="card border-left-primary shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">LogSize
                                            (MSG)
                                        </div>
                                        <a id="producer_logsize" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-file-alt fa-4x text-primary-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-xl-3 col-md-6 mb-4 col-md-offset-6">
                        <div class="card border-left-success shadow h-100 py-2">
                            <div class="card-body">
                                <div class="row no-gutters align-items-center">
                                    <div class="col mr-2">
                                        <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Total
                                            Capacity
                                        </div>
                                        <a id="producer_topicsize" class="h3 mb-0 font-weight-bold text-gray-800">0</a>
                                    </div>
                                    <div class="col-auto">
                                        <i class="fas fa-database fa-4x text-success-panel"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Topic Meta Info
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table id="result" class="table table-bordered table-condensed" width="100%">
                                        <thead>
                                        <tr>
                                            <th>Topic</th>
                                            <th>Partition</th>
                                            <th>LogSize</th>
                                            <th>Leader</th>
                                            <th>Replicas</th>
                                            <th>In Sync Replicas</th>
                                            <th>Preferred Leader</th>
                                            <th>Under Replicated</th>
                                            <th>Preview</th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- MBean -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-server"></i> Topic MBean
                            </div>
                            <div class="card-body">
                                <table id="topic_metrics_tab" class="table table-bordered table-hover">
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Producer -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-chart-bar"></i> Producer Message
                            </div>
                            <div class="card-body">
                                <div id="topic_producer_msg" class="charttopicdiv table-responsive"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Preview -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_topic_preview"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Message</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <div>
                                    <form>
                                        <textarea id="ke_tp_preview_message" name="ke_tp_message"></textarea>
                                    </form>
                                </div>
                            </div>
                            <div id="remove_div" class="modal-footer">
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
    <jsp:param value="main/topic/topic.meta.js?v=2.0.6" name="loader"/>
    <jsp:param value="plugins/echart/echarts.min.js" name="loader"/>
    <jsp:param value="plugins/echart/macarons.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/moment.min.js" name="loader"/>
    <jsp:param value="plugins/datatimepicker/daterangepicker.js" name="loader"/>
    <jsp:param value="plugins/codemirror/codemirror.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql.js" name="loader"/>
    <jsp:param value="plugins/codemirror/show-hint.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql-hint.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
