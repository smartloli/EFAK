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

    <title>Topic Message - KafkaEagle</title>
    <jsp:include page="../public/plus/css.jsp">
        <jsp:param value="plugins/codemirror/codemirror.css" name="css"/>
        <jsp:param value="plugins/codemirror/show-hint.css" name="css"/>
    </jsp:include>
    <jsp:include page="../public/plus/tcss.jsp"></jsp:include>
</head>
<style>
    .CodeMirror {
        border-top: 1px solid #ddd;
        border-bottom: 1px solid #ddd;
        border-right: 1px solid #ddd;
        border-left: 1px solid #ddd;
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
                <h1 class="mt-4">Topic</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Topic</a></li>
                    <li class="breadcrumb-item active">KSQL</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> Sample <a
                        href="http://www.kafka-eagle.org/articles/docs/quickstart/ksql.html" target="_blank">KSQL</a>
                    query: <strong>select * from ke_topic where `partition` in (0,1,2) limit 10</strong><br/> <i
                        class="fas fa-info-circle"></i> AutoComplete: Press <strong>Alt and /</strong>.
                </div>
                <!-- content body -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="far fa-file-code"></i> Kafka Query SQL
                            </div>
                            <div class="card-body">
                                <div>
                                    <form>
                                        <textarea id="code" name="code"></textarea>
                                    </form>
                                </div>
                                <br/>
                                <button id="ke_ksql_query" class="btn btn-success">Query</button>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- result -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="far fa-comments"></i> Tasks Job Info
                            </div>
                            <div class="card-body">
                                <nav>
                                    <div class="nav nav-tabs" id="nav-tab" role="tablist">
                                        <a class="nav-item nav-link active" id="ke_log_textarea" data-toggle="tab"
                                           href="#log_textarea" role="tab" aria-controls="log_textarea"
                                           aria-selected="true">Logs</a> <a class="nav-item nav-link"
                                                                            id="ke_result_textarea" data-toggle="tab"
                                                                            href="#result_textarea" role="tab"
                                                                            aria-controls="result_textarea"
                                                                            aria-selected="false">Result</a> <a
                                            class="nav-item nav-link" id="ke_ksql_history_textarea" data-toggle="tab"
                                            href="#ksql_history_textarea" role="tab"
                                            aria-controls="ksql_history_textarea"
                                            aria-selected="false">History</a>
                                    </div>
                                </nav>
                                <div class="tab-content" id="nav-tabContent">
                                    <div class="tab-pane fade show active" id="log_textarea" role="tabpanel"
                                         aria-labelledby="ke_log_textarea">
                                        <form>
                                            <textarea id="job_info" name="job_info"></textarea>
                                        </form>
                                    </div>
                                    <div class="tab-pane fade" id="result_textarea" role="tabpanel"
                                         aria-labelledby="ke_result_textarea"></div>
                                    <div class="tab-pane fade" id="ksql_history_textarea" role="tabpanel"
                                         aria-labelledby="ke_ksql_history_textarea">
                                        <div id="ksql_history_result_div">
                                            <div id="ksql_history_result0">
                                                <div class="table-responsive">
                                                    <table id="ksql_history_result"
                                                           class="table table-bordered table-condensed" width="100%">
                                                        <thead>
                                                        <tr>
                                                            <th>ID</th>
                                                            <th>User</th>
                                                            <th>Host</th>
                                                            <th>KSQL</th>
                                                            <th>Status</th>
                                                            <th>Spent</th>
                                                            <th>Created</th>
                                                        </tr>
                                                        </thead>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- More then detail -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_sql_query_detail"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">KSQL</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <div class="modal-body">
                                <fieldset class="form-horizontal">
                                    <div class="form-group">
                                        <textarea id="ke_sql_query_content" name="ke_sql_query_content"
                                                  class="form-control" readonly="readonly" rows="3"></textarea>
                                    </div>
                                </fieldset>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancle</button>
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
    <jsp:param value="plugins/magicsuggest/magicsuggest.js" name="loader"/>
    <jsp:param value="plugins/tokenfield/bootstrap-tokenfield.js" name="loader"/>
    <jsp:param value="plugins/codemirror/codemirror.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql.js" name="loader"/>
    <jsp:param value="plugins/codemirror/show-hint.js" name="loader"/>
    <jsp:param value="plugins/codemirror/sql-hint.js" name="loader"/>
    <jsp:param value="main/topic/ksql.js" name="loader"/>
    <jsp:param value="main/topic/ksql.history.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
