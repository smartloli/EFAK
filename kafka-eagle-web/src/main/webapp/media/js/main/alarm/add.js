$(document).ready(function() {

	// select2consumertopic
	var select2arrays = [ "select2consumergroup", "select2consumertopic", "select2level", "select2maxtimes", "select2group" ];
	var select2placeholder = [ "Alarm Consumer Group", "Alarm Consumer Topic", "Alarm Cluster Level", "Alarm Cluster Max Times", "Alarm Cluster Group" ];

	init();

	function init() {
		for (var i = 0; i < select2arrays.length; i++) {
			var id = select2arrays[i];
			var placeholder = "";
			var url = "";
			if (id.indexOf("select2consumergroup") > -1) {
				placeholder = select2placeholder[0];
				url = "/ke/alarm/consumer/group/ajax";
			} else if (id.indexOf("select2consumertopic") > -1) {
				placeholder = select2placeholder[1];
				$("#div_select_consumer_topic").html("<select id='select2consumertopic' name='select2consumertopic' tabindex='-1' style='width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;'></select>");
			} else if (id.indexOf("select2level") > -1) {
				placeholder = select2placeholder[2];
				url = "/ke/alarm/cluster/level/list/ajax";
			} else if (id.indexOf("select2maxtimes") > -1) {
				placeholder = select2placeholder[3];
				url = "/ke/alarm/cluster/maxtimes/list/ajax";
			} else if (id.indexOf("select2group") > -1) {
				placeholder = select2placeholder[4];
				url = "/ke/alarm/cluster/group/list/ajax";
			}
			select2common(id, url, placeholder);
			select2select(id);
		}
	}

	// $("#select2consumertopic")
	function getConsumerTopicURL() {
		var url = "";
		var group = $("#ke_alarm_consumer_group").val();
		if (group.length > 0) {
			url = "/ke/alarm/consumer/" + group + "/topic/ajax";
		}
		return url;
	}

	// <select id="select2consumertopic" name="select2consumertopic" tabindex="-1" style="width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;"></select>

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
			if (id.indexOf("select2consumergroup") > -1) {
				$("#ke_alarm_consumer_group").val(text);
				var placeholder = select2placeholder[1];
				var url = "/ke/alarm/consumer/" + text + "/topic/ajax";
				$("#div_select_consumer_topic").html("");
				$("#div_select_consumer_topic").html("<select id='select2consumertopic' name='select2consumertopic' tabindex='-1' style='width: 100%; font-family: 'Microsoft Yahei', 'HelveticaNeue', Helvetica, Arial, sans-serif; font-size: 1px;'></select>");
				select2common("select2consumertopic", url, placeholder);
				$("#select2consumertopic").on('select2:select', function(e) {
					var value = e.params.data.text;
					$("#select2consumertopic").val(value).select();
					$("#ke_alarm_consumer_topic").val(value);
				});
			} else if (id.indexOf("select2level") > -1) {
				$("#ke_alarm_cluster_level").val(text);
			} else if (id.indexOf("select2maxtimes") > -1) {
				$("#ke_alarm_cluster_maxtimes").val(text);
			} else if (id.indexOf("select2group") > -1) {
				$("#ke_alarm_cluster_group").val(text);
			}
		});
	}

});