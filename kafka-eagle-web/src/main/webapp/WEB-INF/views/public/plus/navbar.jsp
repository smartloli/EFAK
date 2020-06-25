<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="fmt"%>

<!-- Navigation bars -->
<nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
	<div class="sb-sidenav-menu">
		<div class="nav">
			<div class="sb-sidenav-menu-heading">Views</div>
			<a class="nav-link" href="/">
				<div class="sb-nav-link-icon">
					<i class="fas fa-tachometer-alt"></i>
				</div> <fmt:message code="ke.navbar.dashboard" />
			</a> <a class="nav-link" href="/ke/bs">
				<div class="sb-nav-link-icon">
					<i class="fas fa-fw fa-desktop"></i>
				</div> <fmt:message code="ke.navbar.bscreen" />
			</a>
			<div class="sb-sidenav-menu-heading">Message</div>
			<a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseTopics" aria-expanded="false" aria-controls="collapseTopics">
				<div class="sb-nav-link-icon">
					<i class="fas fa-comment-alt"></i>
				</div> Topics
				<div class="sb-sidenav-collapse-arrow">
					<i class="fas fa-angle-down"></i>
				</div>
			</a>
			<div class="collapse" id="collapseTopics" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
				<nav class="sb-sidenav-menu-nested nav">
					<a class="nav-link" href="/topic/create"><i class="fas fa-edit fa-sm fa-fw mr-1"></i>Create</a> 
					<a class="nav-link" href="/topic/list"><i class="fas fa-table fa-sm fa-fw mr-1"></i>List</a> 
					<a class="nav-link" href="/topic/message"><i class="fas fa-code fa-sm fa-fw mr-1"></i>KSQL</a> 
					<a class="nav-link" href="/topic/mock"><i class="far fa-paper-plane fa-sm fa-fw mr-1"></i>Mock</a> 
					<a class="nav-link" href="/topic/manager"><i class="fas fa-tools fa-sm fa-fw mr-1"></i>Manager</a> 
					<a class="nav-link" href="/topic/hub"><i class="fas fa-cube fa-sm fa-fw mr-1"></i>Hub</a>
				</nav>
			</div>
			<a class="nav-link" href="/consumers">
				<div class="sb-nav-link-icon">
					<i class="fas fa-fw fa-users"></i>
				</div> Consumers
			</a>
			<div class="sb-sidenav-menu-heading">Quartz</div>
			<a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseCluster" aria-expanded="false" aria-controls="collapseCluster">
				<div class="sb-nav-link-icon">
					<i class="fas fa-cloud"></i>
				</div> Cluster
				<div class="sb-sidenav-collapse-arrow">
					<i class="fas fa-angle-down"></i>
				</div>
			</a>
			<div class="collapse" id="collapseCluster" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
				<nav class="sb-sidenav-menu-nested nav">
					<a class="nav-link" href="/cluster/info"><i class="fas fa-sitemap fa-sm fa-fw mr-1"></i>ZK & Kafka</a>
					<a class="nav-link" href="/cluster/multi"><i class="fab fa-maxcdn fa-sm fa-fw mr-1"></i>Multi-Clusters</a>
					<a class="nav-link" href="/cluster/zkcli"><i class="fas fa-terminal fa-code fa-sm fa-fw mr-1"></i>ZkCli</a>
				</nav>
			</div>
			<a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseMetrics" aria-expanded="false" aria-controls="collapseMetrics">
				<div class="sb-nav-link-icon">
					<i class="fas fa-eye"></i>
				</div> Metrics
				<div class="sb-sidenav-collapse-arrow">
					<i class="fas fa-angle-down"></i>
				</div>
			</a>
			<div class="collapse" id="collapseMetrics" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
				<nav class="sb-sidenav-menu-nested nav">
					<a class="nav-link" href="/metrics/brokers"><i class="fas fa-sitemap fa-sm fa-fw mr-1"></i>Brokers</a>
					<a class="nav-link" href="/metrics/kafka"><i class="fas fa-chart-bar fa-sm fa-fw mr-1"></i>Kafka</a>
					<a class="nav-link" href="/metrics/zk"><i class="fas fa-chart-area fa-code fa-sm fa-fw mr-1"></i>Zookeeper</a>
				</nav>
			</div>
			<a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseAlarm" aria-expanded="false" aria-controls="collapseAlarm">
				<div class="sb-nav-link-icon">
					<i class="fas fa-bell"></i>
				</div> Alarm
				<div class="sb-sidenav-collapse-arrow">
					<i class="fas fa-angle-down"></i>
				</div>
			</a>
			<div class="collapse" id="collapseAlarm" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
				<nav class="sb-sidenav-menu-nested nav">
					<a class="nav-link" href="layout-static.html">ZK & Kafka</a> <a class="nav-link" href="layout-sidenav-light.html">Multi-Clusters</a> <a class="nav-link" href="layout-sidenav-light.html">ZkCli</a>
				</nav>
			</div>
			<c:if test="${WHETHER_SYSTEM_ADMIN==1}">
				<div class="sb-sidenav-menu-heading">Administrator</div>
				<a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseSystem" aria-expanded="false" aria-controls="collapseSystem">
					<div class="sb-nav-link-icon">
						<i class="fas fa-user-cog"></i>
					</div> System
					<div class="sb-sidenav-collapse-arrow">
						<i class="fas fa-angle-down"></i>
					</div>
				</a>
				<div class="collapse" id="collapseSystem" aria-labelledby="headingOne" data-parent="#sidenavAccordion">
					<nav class="sb-sidenav-menu-nested nav">
						<a class="nav-link" href="#">User</a> <a class="nav-link" href="#">Role</a> <a class="nav-link" href="#">Resource</a>
					</nav>
				</div>
			</c:if>
		</div>
	</div>
	<!-- 
	<div class="sb-sidenav-footer">
		<div class="small">Logged in as:</div>
		Administrator
	</div>
	 -->
</nav>

<script type="text/javascript">
	function contextPasswdFormValid() {
		var ke_new_password_name = $("#ke_new_password_name").val();
		var resetRegular = /[\u4E00-\u9FA5]/;
		if (ke_new_password_name.length == 0 || resetRegular.test(ke_new_password_name)) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}

		return true;
	}
</script>