$(document).ready(function() {
	$("#result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/alarm/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'group'
		}, {
			"mData" : 'topic'
		}, {
			"mData" : 'lag'
		}, {
			"mData" : 'alarmGroup'
		}, {
			"mData" : 'alarmTimes'
		}, {
			"mData" : 'alarmMaxTimes'
		}, {
			"mData" : 'alarmLevel'
		}, {
			"mData" : 'alarmIsNormal'
		}, {
			"mData" : 'alarmIsEnable'
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

	// Alarm is enable
	$(document).on('click', 'label[name=is_enable_label]', function() {
		var id = $(this).attr("val");
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/list/modify/switch/' + id + '/ajax',
			success : function(datas) {
			}
		});
	});
	
	// Show detail content
	$(document).on('click', 'a[name=ke_alarm_consumer_detail]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1].split("/")[0];
		var type = href.split("#")[1].split("/")[1];
		$('#ke_alarm_consumer_detail').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#ke_alarm_consumer_detail').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/consumer/detail/' + type + '/' + id + '/ajax',
			success : function(datas) {
				$("#ke_alarm_consumer_property").val(datas.result);
			}
		});
	});
	
	// Remove
	$(document).on('click', 'a[name=alarm_consumer_remove]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1].split("/")[0];
		$("#alarm_consumer_remove_content").html("<p>Are you sure you want to delete id [" + id + "] ?<p>");
		$("#remove_div").html("<a href='/ke/alarm/consumer/" + id + "/del' class='btn btn-danger'>Remove</a>");
		$('#alarm_consumer_remove').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#alarm_consumer_remove').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});
	});

	// Select
	var select2arrays = [ "select2level", "select2maxtimes", "select2group" ];
	var select2placeholder = [ "Alarm Cluster Level", "Alarm Cluster Max Times", "Alarm Cluster Group" ];

	function select2common(id, url, placeholder) {
		return $("#" + id).select2({
			placeholder : placeholder,
			ajax : {
				url : url,
				dataType : 'json',
				delay : 250,
				data : function(params) {
					params.offset = 10;
					params.page = params.page || 1;
					return {
						name : params.term,
						page : params.page,
						offset : params.offset
					};
				},
				cache : true,
				processResults : function(data, params) {
					if (data.items.length > 0) {
						var datas = new Array();
						$.each(data.items, function(index, e) {
							var s = {};
							s.id = index + 1;
							s.text = e.text;
							datas[index] = s;
						});
						return {
							results : datas,
							pagination : {
								more : (params.page * params.offset) < data.total
							}
						};
					} else {
						return {
							results : []
						}
					}
				},
				escapeMarkup : function(markup) {
					return markup;
				},
				minimumInputLength : 1
			}
		});
	}

	function select2select(id) {
		$("#" + id).on('select2:select', function(evt) {
			var text = evt.params.data.text;
			$("#" + id).val(text);
			if (id.indexOf("select2level") > -1) {
				$("#ke_alarm_consumer_level").val(text);
			} else if (id.indexOf("select2maxtimes") > -1) {
				$("#ke_alarm_consumer_maxtimes").val(text);
			} else if (id.indexOf("select2group") > -1) {
				$("#ke_alarm_consumer_group").val(text);
			}
		});
	}
	
	// Modify
	$(document).on('click', 'a[name=alarm_consumer_modify]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1].split("/")[0];
		$("#ke_consumer_id_lag").val(id);
		$('#alarm_consumer_modify').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#alarm_consumer_modify').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});
		
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/consumer/modify/' + id + '/ajax',
			success : function(datas) {
				$.fn.modal.Constructor.prototype.enforceFocus = function () { };
				$("#ke_consumer_name_lag").val(datas.lag);
				for (var i = 0; i < select2arrays.length; i++) {
					var id = select2arrays[i];
					var placeholder = "";
					var url = "";
					var option;
					if (id.indexOf("select2level") > -1) {
						placeholder = select2placeholder[0];
						url = "/ke/alarm/cluster/level/list/ajax";
						option = new Option(datas.alarmLevel, "0", true, true);
						$("#ke_alarm_consumer_level").val(datas.alarmLevel);
					} else if (id.indexOf("select2maxtimes") > -1) {
						placeholder = select2placeholder[1];
						url = "/ke/alarm/cluster/maxtimes/list/ajax";
						option = new Option(datas.alarmMaxTimes, "0", true, true);
						$("#ke_alarm_consumer_maxtimes").val(datas.alarmMaxTimes);
					} else if (id.indexOf("select2group") > -1) {
						placeholder = select2placeholder[2];
						url = "/ke/alarm/cluster/group/list/ajax";
						option = new Option(datas.alarmGroup, "0", true, true);
						$("#ke_alarm_consumer_group").val(datas.alarmGroup);
					}
					var select2object = select2common(id, url, placeholder);
					select2select(id);

					// change exist value
					if (select2object.find("option[value='0']").length) {
						select2object.val(null).trigger('change');
						select2object.append(option).trigger('change');
					} else {
						// append it to the select
						select2object.append(option).trigger('change');
					}
				}
			}
		});
	});
});