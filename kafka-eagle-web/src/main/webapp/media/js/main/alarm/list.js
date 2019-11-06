$(document).ready(function() {
	$("#result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/alarm/config/table/ajax",
		"aoColumns" : [ {
			"mData" : 'cluster'
		}, {
			"mData" : 'alarmGroup'
		}, {
			"mData" : 'alarmType'
		}, {
			"mData" : 'alarmUrl'
		}, {
			"mData" : 'httpMethod'
		}, {
			"mData" : 'alarmAddress'
		}, {
			"mData" : 'created'
		}, {
			"mData" : 'modify'
		}, {
			"mData" : 'operate'
		} ]
	});

	function retrieveData(sSource, aoData, fnCallback) {
		$.ajax({
			"type" : "get",
			"contentType" : "application/json",
			"url" : sSource,
			"dataType" : "json",
			"data" : {
				aoData : JSON.stringify(aoData)
			},
			"success" : function(data) {
				fnCallback(data)
			}
		});
	}

	$(document).on('click', 'a[name=ke_alarm_config_remove]', function() {
		var href = $(this).attr("href");
		var group = href.split("#")[1];
		$("#remove_div").html("");
		$("#remove_div").append("<a href='/ke/alarm/config/" + group + "/del' class='btn btn-danger'>Remove</a>");
		$('#ke_alarm_config_delete').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#ke_alarm_config_delete').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});
	});

	$(document).on('click', 'a[name=ke_alarm_config_detail]', function() {
		var href = $(this).attr("href");
		var group = href.split("#")[1].split("/")[0];
		var type = href.split("#")[1].split("/")[1];
		$('#ke_alarm_config_detail').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#ke_alarm_config_detail').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/config/get/' + type + '/' + group + '/ajax',
			success : function(datas) {
				$("#ke_alarm_config_property").val(datas.result);
			}
		});
	});

	$(document).on('click', 'a[name=ke_alarm_config_modify]', function() {
		var href = $(this).attr("href");
		var group = href.split("#")[1].split("/")[0];
		var type = href.split("#")[1].split("/")[1];
		$("#ke_alarm_group_m_name").val(group);
		$('#ke_alarm_config_modify').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#ke_alarm_config_modify').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/config/get/' + type + '/' + group + '/ajax',
			success : function(datas) {
				$("#ke_alarm_config_m_url").val(datas.url);
				$("#ke_alarm_config_m_address").val(datas.address);
			}
		});
	});

});