$(document).ready(function() {
	// alarm cluster type
	$("#select2type").select2({
		placeholder : "Alarm Cluster Type",
		ajax : {
			url : "/ke/alarm/cluster/type/list/ajax",
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

	$('#select2type').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2type").val(text);
		$("#ke_alarm_cluster_type").val(text);
		if (text.indexOf("Topic") > -1) {
			$("#ke_alarm_topic_div").show();
			$("#ke_alarm_producer_div").hide();
			$("#ke_alarm_server_div").hide();
		} else if (text.indexOf("Producer") > -1) {
			$("#ke_alarm_topic_div").hide();
			$("#ke_alarm_producer_div").show();
			$("#ke_alarm_server_div").hide();
		} else {
			$("#ke_alarm_topic_div").hide();
			$("#ke_alarm_producer_div").hide();
			$("#ke_alarm_server_div").show();
		}
	});

	// alarm level
	$("#select2level").select2({
		placeholder : "Alarm Cluster Level",
		ajax : {
			url : "/ke/alarm/cluster/level/list/ajax",
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

	$('#select2level').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2level").val(text);
		$("#ke_alarm_cluster_level").val(text);
	});

	// alarm max times
	$("#select2maxtimes").select2({
		placeholder : "Alarm Cluster Max Times",
		ajax : {
			url : "/ke/alarm/cluster/maxtimes/list/ajax",
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

	$('#select2maxtimes').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2maxtimes").val(text);
		$("#ke_alarm_cluster_maxtimes").val(text);
	});

	// alarm group
	$("#select2group").select2({
		placeholder : "Alarm Cluster Group",
		ajax : {
			url : "/ke/alarm/cluster/group/list/ajax",
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

	$('#select2group').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2group").val(text);
		$("#ke_alarm_cluster_group").val(text);
	});

});