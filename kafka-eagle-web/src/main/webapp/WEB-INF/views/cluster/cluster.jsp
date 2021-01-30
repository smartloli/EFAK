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

    <title>ZK & Kafka - KafkaEagle</title>
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
                <h1 class="mt-4">ZK & Kafka</h1>
                <ol class="breadcrumb mb-4">
                    <li class="breadcrumb-item"><a href="#">ZK & Kafka</a></li>
                    <li class="breadcrumb-item active">Overview</li>
                </ol>
                <div class="alert alert-info alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <i class="fas fa-info-circle"></i> <strong>Cluster
                    information, in the form of tables to demonstrate the Kafka and
                    Zookeeper cluster node IP, port, and its version number</strong> If you
                    don't know the usage of Kafka and Zookeeper, you can visit the
                    website of <a href="http://kafka.apache.org/" target="_blank"
                                  class="alert-link">Kafka</a> and <a
                        href="http://zookeeper.apache.org/" target="_blank"
                        class="alert-link">Zookeeper</a> to view the relevant usage.<br/>
                    <i class="fas fa-info-circle"></i><strong>Note</strong>:&nbsp;Kafka version is <strong>"-"</strong>
                    or JMX Port is <strong>"-1"</strong> maybe kafka broker jmxport disable.<br/>
                </div>
                <!-- content body -->
                <!-- Kafka -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-sitemap"></i> Kafka Cluster Info
                                <%--                                <c:if test="${WHETHER_SYSTEM_ADMIN==1}">--%>
                                <%--                                    <div style="float: right!important;">--%>
                                <%--                                        <button id="ke-add-acl-user-btn" type="button"--%>
                                <%--                                                class="btn btn-primary btn-sm">ACL--%>
                                <%--                                        </button>--%>
                                <%--                                    </div>--%>
                                <%--                                </c:if>--%>
                            </div>
                            <div class="card-body">
                                <div id="kafka_cluster_info" class="table-responsive">
                                    <table id="kafka_tab" class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>BrokerID</th>
                                            <th>IP</th>
                                            <th>Port</th>
                                            <th>JMX Port</th>
                                            <th>Memory(Used | Percent)</th>
                                            <th>CPU</th>
                                            <th>Created</th>
                                            <th>Modify</th>
                                            <th>Version</th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Zookeeper -->
                <div class="row">
                    <div class="col-lg-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-sitemap"></i> Kafka Zookeeper Info
                            </div>
                            <div class="card-body">
                                <div id="zookeeper_cluster_info" class="table-responsive">
                                    <table id="zk_tab" class="table table-bordered table-hover">
                                        <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>IP</th>
                                            <th>Port</th>
                                            <th>Mode</th>
                                            <th>Version</th>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- row -->
                <div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_acl_add_dialog"
                     tabindex="-1" role="dialog">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title" id="keModalLabel">Add ACL User</h4>
                                <button class="close" type="button" data-dismiss="modal">x</button>
                            </div>
                            <!-- /.row -->
                            <!-- add modal -->
                            <div class="modal-body">
                                <form role="form" action="/cluster/info/user/add/acl/" method="post"
                                      onsubmit="return contextFormValid();return false;">
                                    <fieldset class="form-horizontal">
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Mechanism</span>
                                            </div>
                                            <input id="ke_mechanism_name" name="ke_mechanism_name" type="text"
                                                   class="form-control" placeholder="SCRAM-SHA-256"
                                                   aria-describedby="basic-addon3" readonly="readonly">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">RealName</span>
                                            </div>
                                            <input id="ke_real_name" name="ke_real_name" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">UserName</span>
                                            </div>
                                            <input id="ke_user_name" name="ke_user_name" type="text"
                                                   class="form-control" placeholder="kafka-eagle"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div class="input-group mb-3">
                                            <div class="input-group-prepend">
                                                <span class="input-group-text" id="basic-addon3">Email</span>
                                            </div>
                                            <input id="ke_user_email" name="ke_user_email" type="text"
                                                   class="form-control" placeholder="kafka-eagle@email.com"
                                                   aria-describedby="basic-addon3">
                                        </div>
                                        <div id="alert_mssage_add" style="display: none"
                                             class="alert alert-danger">
                                            <label id="alert_mssage_add_label"></label>
                                        </div>
                                    </fieldset>

                                    <div id="remove_div" class="modal-footer">
                                        <button type="button" class="btn btn-secondary"
                                                data-dismiss="modal">Cancle
                                        </button>
                                        <button type="submit" class="btn btn-primary" id="create-add">Submit
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
    <jsp:param value="main/cluster/cluster.js?v1.4.8" name="loader"/>
</jsp:include>
<jsp:include page="../public/plus/tscript.jsp"></jsp:include>
<script type="text/javascript">
    function contextFormValid() {
        var ke_rtxno_name = $("#ke_rtxno_name").val();
        var ke_real_name = $("#ke_real_name").val();
        var ke_user_name = $("#ke_user_name").val();
        var ke_user_email = $("#ke_user_email").val();
        if (ke_real_name == "Administrator" || ke_user_name == "admin") {
            $("#alert_mssage_add").show();
            $("#alert_mssage_add_label").text("Oops! Administrator or admin is not available.");
            setTimeout(function () {
                $("#alert_mssage_add").hide()
            }, 3000);
            return false;
        }
        if (ke_rtxno_name.length == 0 || ke_real_name.length == 0 || ke_user_name.length == 0 || ke_user_email.length == 0) {
            $("#alert_mssage_add").show();
            $("#alert_mssage_add_label").text("Oops! Please enter the complete information.");
            setTimeout(function () {
                $("#alert_mssage_add").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>
</html>
