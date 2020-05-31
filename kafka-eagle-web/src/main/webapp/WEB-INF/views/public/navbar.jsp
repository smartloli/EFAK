<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!-- Navigation -->
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<!-- Brand and toggle get grouped for better mobile display -->
	<div class="navbar-header">
		<img class="user-avatar" style="border: 3px solid #fff; border-radius: 50%; margin-top: 6px; margin-left: 10px; float: left;" src="/ke/media/img/ke_login.png" width="40px">
		<a class="navbar-brand" href="/ke"> Kafka Eagle</a>
		<div class="modal fade" aria-labelledby="keModalLabel" aria-hidden="true" id="ke_account_reset_dialog" tabindex="-1" role="dialog">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button class="close" type="button" data-dismiss="modal">×</button>
						<h4 class="modal-title" id="keModalLabel">Reset password</h4>
					</div>
					<!-- /.row -->
					<form role="form" action="/ke/account/reset/" method="post" onsubmit="return contextPasswdFormValid();return false;">
						<div class="modal-body">
							<fieldset class="form-horizontal">
								<div class="form-group">
									<label for="path" class="col-sm-2 control-label">New</label>
									<div class="col-sm-10">
										<input id="ke_new_password_name" name="ke_new_password_name" type="password" class="form-control" maxlength="16" placeholder="New Password">
									</div>
								</div>
								<div id="alert_mssage" style="display: none" class="alert alert-danger">
									<label> Passwords can only be number and letters or special symbols .</label>
								</div>
							</fieldset>
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default" data-dismiss="modal">Cancle</button>
							<button type="submit" class="btn btn-primary" id="create-btn">Submit</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	<!-- Top Menu Items -->
	<ul class="nav navbar-right top-nav">
		<li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				<i class="fa fa-sitemap"></i>
				${clusterAlias}
				<b class="caret"></b>
			</a>${clusterAliasList}</li>
		<li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				<i class="fa fa-bookmark"></i>
				V1.4.8
			</a>
		</li>
		<li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
				<i class="fa fa-user"></i>
				${LOGIN_USER_SESSION.realname}
				<b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li>
					<a name="ke_account_reset" href="#">
						<i class="fa fa-fw fa-gear"></i>
						Reset
					</a>
				</li>
				<li>
					<a href="/ke/account/signout">
						<i class="fa fa-fw fa-power-off"></i>
						Signout
					</a>
				</li>
			</ul>
		</li>
	</ul>
	<!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
	<div class="collapse navbar-collapse navbar-ex1-collapse">
		<ul class="nav navbar-nav side-nav">
			<li id="navbar_dash">
				<a href="/ke">
					<i class="fa fa-fw fa-dashboard"></i>
					Dashboard
				</a>
			</li>
			<li>
				<a href="#" data-toggle="collapse" data-target="#ke_topic_data">
					<i class="fa fa-fw fa-comments-o"></i>
					Topic
					<i class="fa fa-fw fa-caret-down"></i>
				</a>
				<ul id="ke_topic_data" class="collapse">
					<li id="navbar_topic_create_li">
						<a id="navbar_topic_create_a" href="/ke/topic/create">
							<i class="fa fa-edit fa-fw"></i>
							Create
						</a>
					</li>
					<li id="navbar_topic_list_li">
						<a id="navbar_topic_list_a" href="/ke/topic/list">
							<i class="fa fa-table fa-fw"></i>
							List
						</a>
					</li>
					<li id="navbar_topic_message_li">
						<a id="navbar_topic_message_a" href="/ke/topic/message">
							<i class="fa fa-file-text fa-fw"></i>
							KSQL
						</a>
					</li>
					<li id="navbar_topic_mock_li">
						<a id="navbar_topic_mock_a" href="/ke/topic/mock">
							<i class="fa fa-maxcdn fa-fw"></i>
							Mock
						</a>
					</li>
					<li id="navbar_topic_manager_li">
						<a id="navbar_topic_manager_a" href="/ke/topic/manager">
							<i class="fa fa-tasks fa-fw"></i>
							Manager
						</a>
					</li>
					<li id="navbar_topic_hub_li">
						<a id="navbar_topic_hub_a" href="/ke/topic/hub">
							<i class="fa fa-cube fa-fw"></i>
							Hub
						</a>
					</li>
				</ul>
			</li>
			<li id="navbar_consumers">
				<a href="/ke/consumers">
					<i class="fa fa-fw fa-users"></i>
					Consumers
				</a>
			</li>
			<li>
				<a href="#" data-toggle="collapse" data-target="#ke_cluster_data">
					<i class="fa fa-fw fa-cloud"></i>
					Cluster
					<i class="fa fa-fw fa-caret-down"></i>
				</a>
				<ul id="ke_cluster_data" class="collapse">
					<li id="navbar_cluster_info_li">
						<a id="navbar_cluster_info_a" href="/ke/cluster/info">
							<i class="fa fa-sitemap fa-fw"></i>
							ZK & Kafka
						</a>
					</li>
					<li id="navbar_cluster_multi_li">
						<a id="navbar_cluster_multi_a" href="/ke/cluster/multi">
							<i class="fa fa-maxcdn fa-fw"></i>
							Multi-Clusters
						</a>
					</li>
					<li id="navbar_cluster_zkcli_li">
						<a id="navbar_cluster_zkcli_a" href="/ke/cluster/zkcli">
							<i class="fa fa-terminal fa-fw"></i>
							ZkCli
						</a>
					</li>
				</ul>
			</li>
			<li>
				<a href="#" data-toggle="collapse" data-target="#ke_metrics_data">
					<i class="fa fa-fw fa-eye"></i>
					Metrics
					<i class="fa fa-fw fa-caret-down"></i>
				</a>
				<ul id="ke_metrics_data" class="collapse">
					<li id="navbar_metrics_brokers_li">
						<a id="navbar_metrics_brokers_a" href="/ke/metrics/brokers">
							<i class="fa fa-sitemap fa-fw"></i>
							Brokers
						</a>
					</li>
					<li id="navbar_metrics_kafka_li">
						<a id="navbar_metrics_kafka_a" href="/ke/metrics/kafka">
							<i class="fa fa-bar-chart-o fa-fw"></i>
							Kafka
						</a>
					</li>
					<li id="navbar_metrics_zk_li">
						<a id="navbar_metrics_zk_a" href="/ke/metrics/zk">
							<i class="fa fa-area-chart fa-fw"></i>
							Zookeeper
						</a>
					</li>
				</ul>
			</li>
			<li>
				<a href="#" data-toggle="collapse" data-target="#ke_alarm_data">
					<i class="fa fa-fw fa-bell"></i>
					Alarm
					<i class="fa fa-fw fa-caret-down"></i>
				</a>
				<ul id="ke_alarm_data" class="collapse">
					<li>
						<a href="#" data-toggle="collapse" data-target="#ke_alarm_channel_data">
							<i class="fa fa-fw fa-bullhorn"></i>
							Channel
							<i class="fa fa-fw fa-caret-down"></i>
						</a>
						<ul id="ke_alarm_channel_data" class="collapse" style="list-style: none; margin-left: -40px">
							<li id="navbar_alarm_config_li">
								<a id="navbar_alarm_config_a" href="/ke/alarm/config" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-info-circle fa-fw"></i>
									Config
								</a>
							</li>
							<li id="navbar_alarm_list_li">
								<a id="navbar_alarm_list_a" href="/ke/alarm/list" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-edit fa-fw"></i>
									List
								</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="#" data-toggle="collapse" data-target="#ke_alarm_consumer_data">
							<i class="fa fa-fw fa-users"></i>
							Consumer
							<i class="fa fa-fw fa-caret-down"></i>
						</a>
						<ul id="ke_alarm_consumer_data" class="collapse" style="list-style: none; margin-left: -40px">
							<li id="navbar_alarm_add_li">
								<a id="navbar_alarm_add_a" href="/ke/alarm/add" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-info-circle fa-fw"></i>
									Add
								</a>
							</li>
							<li id="navbar_alarm_modify_li">
								<a id="navbar_alarm_modify_a" href="/ke/alarm/modify" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-edit fa-fw"></i>
									Modify
								</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="#" data-toggle="collapse" data-target="#ke_alarm_cluster_data">
							<i class="fa fa-fw fa-cloud"></i>
							Cluster
							<i class="fa fa-fw fa-caret-down"></i>
						</a>
						<ul id="ke_alarm_cluster_data" class="collapse" style="list-style: none; margin-left: -40px">
							<li id="navbar_alarm_create_li">
								<a id="navbar_alarm_create_a" href="/ke/alarm/create" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-info-circle fa-fw"></i>
									Create
								</a>
							</li>
							<li id="navbar_alarm_history_li">
								<a id="navbar_alarm_history_a" href="/ke/alarm/history" style="display: block; padding: 10px 15px 10px 68px; text-decoration: none; color: #999;">
									<i class="fa fa-edit fa-fw"></i>
									History
								</a>
							</li>
						</ul>
					</li>
				</ul>
			</li>

			<c:if test="${WHETHER_SYSTEM_ADMIN==1}">
				<li>
					<a href="#" data-toggle="collapse" data-target="#ke_system_data">
						<i class="fa fa-fw fa-cog"></i>
						System
						<i class="fa fa-fw fa-caret-down"></i>
					</a>
					<ul id="ke_system_data" class="collapse">
						<li id="navbar_system_user_li">
							<a id="navbar_system_user_a" href="/ke/system/user">
								<i class="fa fa-user fa-fw"></i>
								User
							</a>
						</li>
						<li id="navbar_system_role_li">
							<a id="navbar_system_role_a" href="/ke/system/role">
								<i class="fa fa-key fa-fw"></i>
								Role
							</a>
						</li>
						<li id="navbar_system_resource_li">
							<a id="navbar_system_resource_a" href="/ke/system/resource">
								<i class="fa fa-folder-open fa-fw"></i>
								Resource
							</a>
						</li>
					</ul>
				</li>
			</c:if>
			<li>
				<a href="/ke/bs" target="_blank">
					<i class="fa fa-fw fa-desktop"></i>
					BScreen
				</a>
			</li>
		</ul>
	</div>
	<!-- /.navbar-collapse -->
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