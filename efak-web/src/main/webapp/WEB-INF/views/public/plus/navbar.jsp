<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="fmt" %>

<!-- Navigation bars -->
<nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
    <div class="sb-sidenav-menu">
        <div class="nav">
            <div class="sb-sidenav-menu-heading">Views</div>
            <a id="ke_navbar_dash" class="nav-link" href="/">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-tachometer-alt"></i>
                </div>
                Dashboard <%--<fmt:message code="ke.navbar.dashboard" />--%>
            </a> <a class="nav-link" href="/bs" target="_blank">
            <div class="sb-nav-link-icon">
                <i class="fas fa-fw fa-desktop"></i>
            </div>
            BScreen
        </a>
            <div class="sb-sidenav-menu-heading">Message</div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseTopics"
               aria-expanded="false" aria-controls="collapseTopics">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-comment-alt"></i>
                </div>
                Topics
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseTopics" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_topic_create" class="nav-link" href="/topic/create"><i
                            class="fas fa-edit fa-sm fa-fw mr-1"></i>Create</a>
                    <a id="ke_navbar_topic_list" class="nav-link" href="/topic/list"><i
                            class="fas fa-table fa-sm fa-fw mr-1"></i>List</a>
                    <a id="ke_navbar_topic_ksql" class="nav-link" href="/topic/message"><i
                            class="fas fa-code fa-sm fa-fw mr-1"></i>KSQL</a>
                    <a id="ke_navbar_topic_mock" class="nav-link" href="/topic/mock"><i
                            class="far fa-paper-plane fa-sm fa-fw mr-1"></i>Mock</a>
                    <a id="ke_navbar_topic_manager" class="nav-link" href="/topic/manager"><i
                            class="fas fa-tools fa-sm fa-fw mr-1"></i>Manager</a>
                    <a id="ke_navbar_topic_hub" class="nav-link" href="/topic/hub"><i
                            class="fas fa-cube fa-sm fa-fw mr-1"></i>Hub</a>
                </nav>
            </div>
            <!--
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseLog"
               aria-expanded="false" aria-controls="collapseLog">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-envelope"></i>
                </div>
                Log
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseLog" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_topic_tasks" class="nav-link" href="/log/tasks"><i
                            class="fab fa-buffer fa-sm fa-fw mr-1"></i>Task</a>
                </nav>
            </div>
            -->
            <a id="ke_navbar_consumers" class="nav-link" href="/consumers">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-fw fa-users"></i>
                </div>
                Consumers
            </a>
            <div class="sb-sidenav-menu-heading">Performance</div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseCluster"
               aria-expanded="false" aria-controls="collapseCluster">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-cloud"></i>
                </div>
                Cluster
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseCluster" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_cluster_info" class="nav-link" href="/cluster/info"><i
                            class="fas fa-sitemap fa-sm fa-fw mr-1"></i>ZK & Kafka</a>
                    <a id="ke_navbar_cluster_multi" class="nav-link" href="/cluster/multi"><i
                            class="fab fa-maxcdn fa-sm fa-fw mr-1"></i>Multi-Clusters</a>
                    <a id="ke_navbar_cluster_zkcli" class="nav-link" href="/cluster/zkcli"><i
                            class="fas fa-terminal fa-code fa-sm fa-fw mr-1"></i>ZkCli</a>
                    <!--
                    <a id="ke_navbar_cluster_worknodes" class="nav-link" href="/cluster/worknodes"><i
                            class="fas fa-bezier-curve fa-sm fa-fw mr-1"></i>WorkNodes</a>
                    -->
                </nav>
            </div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseMetrics"
               aria-expanded="false" aria-controls="collapseMetrics">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-eye"></i>
                </div>
                Metrics
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseMetrics" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_metrics_brokers" class="nav-link" href="/metrics/brokers"><i
                            class="fas fa-sitemap fa-sm fa-fw mr-1"></i>Brokers</a>
                    <a id="ke_navbar_metrics_kafka" class="nav-link" href="/metrics/kafka"><i
                            class="fas fa-chart-bar fa-sm fa-fw mr-1"></i>Kafka</a>
                    <a id="ke_navbar_metrics_zk" class="nav-link" href="/metrics/zk"><i
                            class="fas fa-chart-area fa-code fa-sm fa-fw mr-1"></i>Zookeeper</a>
                </nav>
            </div>
            <div class="sb-sidenav-menu-heading">Plugins</div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseConnect"
               aria-expanded="false" aria-controls="collapseConnect">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-plug"></i>
                </div>
                Connect
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseConnect" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_connect_config" class="nav-link" href="/connect/config"><i
                            class="fas fa-link fa-sm fa-fw mr-1"></i>Config</a>
                    <%--                    <a id="ke_navbar_connect_monitor" class="nav-link" href="/connect/monitor"><i--%>
                    <%--                            class="fas fa-eye fa-sm fa-fw mr-1"></i>Monitor</a>--%>
                </nav>
            </div>
            <div class="sb-sidenav-menu-heading">Alarm</div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseAlarmChannel"
               aria-expanded="false" aria-controls="collapseAlarm">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-bullhorn"></i>
                </div>
                Channel
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseAlarmChannel" aria-labelledby="headingOne"
                 data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_alarm_config" class="nav-link" href="/alarm/config"><i
                            class="fas fa-copy fa-sm fa-fw mr-1"></i>Config</a>
                    <a id="ke_navbar_alarm_list" class="nav-link" href="/alarm/list"><i
                            class="fas fa-user-edit fa-sm fa-fw mr-1"></i>List</a>
                </nav>
            </div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseAlarmConsumer"
               aria-expanded="false" aria-controls="collapseAlarm">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-envelope"></i>
                </div>
                AlarmConsumer
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseAlarmConsumer" aria-labelledby="headingOne"
                 data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_alarm_add" class="nav-link" href="/alarm/add"><i
                            class="fas fa-plus-square fa-sm fa-fw mr-1"></i>Add</a>
                    <a id="ke_navbar_alarm_modify" class="nav-link" href="/alarm/modify"><i
                            class="fas fa-pen-square fa-sm fa-fw mr-1"></i>Modify</a>
                </nav>
            </div>
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseAlarmCluster"
               aria-expanded="false" aria-controls="collapseAlarm">
                <div class="sb-nav-link-icon">
                    <i class="fas fa-hdd"></i>
                </div>
                AlarmCluster
                <div class="sb-sidenav-collapse-arrow">
                    <i class="fas fa-angle-down"></i>
                </div>
            </a>
            <div class="collapse" id="collapseAlarmCluster" aria-labelledby="headingOne"
                 data-parent="#sidenavAccordion">
                <nav class="sb-sidenav-menu-nested nav">
                    <a id="ke_navbar_alarm_create" class="nav-link" href="/alarm/create"><i
                            class="fas fa-edit fa-sm fa-fw mr-1"></i>Create</a>
                    <a id="ke_navbar_alarm_history" class="nav-link" href="/alarm/history"><i
                            class="fas fa-history fa-sm fa-fw mr-1"></i>History</a>
                </nav>
            </div>
            <c:if test="${WHETHER_SYSTEM_ADMIN==1}">
                <div class="sb-sidenav-menu-heading">Administrator</div>
                <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseSystem"
                   aria-expanded="false" aria-controls="collapseSystem">
                    <div class="sb-nav-link-icon">
                        <i class="fas fa-user-cog"></i>
                    </div>
                    System
                    <div class="sb-sidenav-collapse-arrow">
                        <i class="fas fa-angle-down"></i>
                    </div>
                </a>
                <div class="collapse" id="collapseSystem" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
                    <nav class="sb-sidenav-menu-nested nav">
                        <a id="ke_navbar_system_user" class="nav-link" href="/system/user"><i
                                class="fas fa-user fa-sm fa-fw mr-1"></i>User</a>
                        <a id="ke_navbar_system_role" class="nav-link" href="/system/role"><i
                                class="fas fa-key fa-sm fa-fw mr-1"></i>Role</a>
                        <a id="ke_navbar_system_resource" class="nav-link" href="/system/resource"><i
                                class="fas fa-folder-open fa-sm fa-fw mr-1"></i>Resource</a>
                    </nav>
                </div>
            </c:if>
        </div>
    </div>
    <div class="sb-sidenav-footer">
        <div class="small">Logged in time is:</div>
        ${LOGIN_USER_SESSION_TIME}
    </div>
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