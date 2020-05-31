$(document).ready(function() {
	var url = window.location.href;
	var ret = url.split("/ke")[1];
	if (ret.indexOf("/cluster") > -1) {
		$("#ke_cluster_data").addClass('collapse in');
		$("#ke_cluster_data").attr("aria-expanded", true);
		if (ret.indexOf("/cluster/info") > -1) {
			$("#navbar_cluster_info_li").addClass('navbar_clicked');
			$("#navbar_cluster_info_a").css('color', '#fff');
		} else if (ret.indexOf("/cluster/multi") > -1) {
			$("#navbar_cluster_multi_li").addClass('navbar_clicked');
			$("#navbar_cluster_multi_a").css('color', '#fff');
		} else if (ret.indexOf("/cluster/zkcli") > -1) {
			$("#navbar_cluster_zkcli_li").addClass('navbar_clicked');
			$("#navbar_cluster_zkcli_a").css('color', '#fff');
		}
	} else if (ret == "/consumers") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_consumers").addClass('active');
	} else if (ret == "/") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_dash").addClass('active');
	} else if (ret.indexOf("/topic") > -1) {
		$("#ke_topic_data").addClass('collapse in');
		$("#ke_topic_data").attr("aria-expanded", true);
		if (ret.indexOf("/topic/create") > -1) {
			$("#navbar_topic_create_li").addClass('navbar_clicked');
			$("#navbar_topic_create_a").css('color', '#fff');
		} else if (ret.indexOf("/topic/list") > -1) {
			$("#navbar_topic_list_li").addClass('navbar_clicked');
			$("#navbar_topic_list_a").css('color', '#fff');
		} else if (ret.indexOf("/topic/message") > -1) {
			$("#navbar_topic_message_li").addClass('navbar_clicked');
			$("#navbar_topic_message_a").css('color', '#fff');
		} else if (ret.indexOf("/topic/mock") > -1) {
			$("#navbar_topic_mock_li").addClass('navbar_clicked');
			$("#navbar_topic_mock_a").css('color', '#fff');
		} else if (ret.indexOf("/topic/manager") > -1) {
			$("#navbar_topic_manager_li").addClass('navbar_clicked');
			$("#navbar_topic_manager_a").css('color', '#fff');
		} else if (ret.indexOf("/topic/hub") > -1) {
			$("#navbar_topic_hub_li").addClass('navbar_clicked');
			$("#navbar_topic_hub_a").css('color', '#fff');
		}
	} else if (ret.indexOf("/alarm") > -1) {
		$("#ke_alarm_data").addClass('collapse in');
		$("#ke_alarm_data").attr("aria-expanded", true);
		if (ret.indexOf("/alarm/add") > -1 || ret.indexOf("/alarm/modify") > -1) {
			$("#ke_alarm_consumer_data").addClass('collapse in');
			$("#ke_alarm_consumer_data").attr("aria-expanded", true);
			if (ret.indexOf("/add") > -1) {
				$("#navbar_alarm_add_li").addClass('navbar_clicked');
				$("#navbar_alarm_add_a").css('color', '#fff');
			} else if (ret.indexOf("/modify") > -1) {
				$("#navbar_alarm_modify_li").addClass('navbar_clicked');
				$("#navbar_alarm_modify_a").css('color', '#fff');
			}
		} else if (ret.indexOf("/alarm/create") > -1 || ret.indexOf("/alarm/history") > -1) {
			$("#ke_alarm_cluster_data").addClass('collapse in');
			$("#ke_alarm_cluster_data").attr("aria-expanded", true);
			if (ret.indexOf("/create") > -1) {
				$("#navbar_alarm_create_li").addClass('navbar_clicked');
				$("#navbar_alarm_create_a").css('color', '#fff');
			} else if (ret.indexOf("/history") > -1) {
				$("#navbar_alarm_history_li").addClass('navbar_clicked');
				$("#navbar_alarm_history_a").css('color', '#fff');
			}
		} else if (ret.indexOf("/alarm/config") > -1 || ret.indexOf("/alarm/list") > -1) {
			$("#ke_alarm_channel_data").addClass('collapse in');
			$("#ke_alarm_channel_data").attr("aria-expanded", true);
			if (ret.indexOf("/config") > -1) {
				$("#navbar_alarm_config_li").addClass('navbar_clicked');
				$("#navbar_alarm_config_a").css('color', '#fff');
			} else if (ret.indexOf("/list") > -1) {
				$("#navbar_alarm_list_li").addClass('navbar_clicked');
				$("#navbar_alarm_list_a").css('color', '#fff');
			}
		}
	} else if (ret.indexOf("/system") > -1) {
		$("#ke_system_data").addClass('collapse in');
		$("#ke_system_data").attr("aria-expanded", true);
		if (ret.indexOf("/system/user") > -1) {
			$("#navbar_system_user_li").addClass('navbar_clicked');
			$("#navbar_system_user_a").css('color', '#fff');
		} else if (ret.indexOf("/system/role") > -1) {
			$("#navbar_system_role_li").addClass('navbar_clicked');
			$("#navbar_system_role_a").css('color', '#fff');
		} else if (ret.indexOf("/system/resource") > -1) {
			$("#navbar_system_resource_li").addClass('navbar_clicked');
			$("#navbar_system_resource_a").css('color', '#fff');
		}
	} else if (ret.indexOf("/metrics") > -1) {
		$("#ke_metrics_data").addClass('collapse in');
		$("#ke_metrics_data").attr("aria-expanded", true);
		if (ret.indexOf("/metrics/brokers") > -1) {
			$("#navbar_metrics_brokers_li").addClass('navbar_clicked');
			$("#navbar_metrics_brokers_a").css('color', '#fff');
		} else if (ret.indexOf("/metrics/kafka") > -1) {
			$("#navbar_metrics_kafka_li").addClass('navbar_clicked');
			$("#navbar_metrics_kafka_a").css('color', '#fff');
		} else if (ret.indexOf("/metrics/zk") > -1) {
			$("#navbar_metrics_zk_li").addClass('navbar_clicked');
			$("#navbar_metrics_zk_a").css('color', '#fff');
		}
	}

	$(document).on('click', 'a[name=ke_account_reset]', function() {
		$('#ke_account_reset_dialog').modal('show');
		$(".modal-backdrop").css({
			"z-index" : "999"
		});
	});
});