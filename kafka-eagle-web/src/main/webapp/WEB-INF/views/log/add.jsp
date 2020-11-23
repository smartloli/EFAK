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

    <title>Add - KafkaEagle</title>
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
                <h1 class="mt-4">Add Topic Tasks</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">Add Topic Tasks</a></li>
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
                                <i class="far fa-file-code"></i> Kafka SQL Edit
                            </div>
                            <div class="card-body">
                                <div>
                                    <form>
                                        <textarea id="code" name="code"></textarea>
                                    </form>
                                </div>
                                <br/>
                                <button id="ke_ksql_submit" class="btn btn-primary">Submit</button>
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
    <jsp:param value="main/log/add.ksql.js" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
</html>
