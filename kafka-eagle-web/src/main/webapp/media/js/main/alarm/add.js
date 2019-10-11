$(document).ready(function() {

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
				select2common("select2consumertopic", url, placeholder);
				$("#select2consumertopic").on('select2:select', function(e) {
					var value = e.params.data.text;
					// $("#select2consumertopic").val(null).trigger('change');
					$("#select2consumertopic").val(value);
					$("#ke_alarm_consumer_topic").val(text);
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

	// // Initialization group
	// var ms_group = $("#ke_group_alarm").magicSuggest({
	// allowFreeEntries : false,
	// autoSelect : true,
	// maxSelection : 1
	// });

	// // Initialization topic
	// var ms_topic = $("#ke_topic_alarm").magicSuggest({
	// allowFreeEntries : false,
	// autoSelect : true,
	// maxSelection : 1
	// });
	//
	// $('#ke_topic_email').tokenfield({
	// autocomplete : {
	// source : [ 'example1@email.com' ],
	// delay : 100
	// },
	// showAutocompleteOnFocus : true
	// });
	//
	// var ms_data;
	// $.ajax({
	// type : 'get',
	// dataType : 'json',
	// url : '/ke/alarm/topic/ajax',
	// success : function(datas) {
	// if (datas != null) {
	// ms_data = datas;
	// var groups = new Array();
	// for (var i = 0; i < datas.length; i++) {
	// var obj = new Object();
	// obj.id = i + 1;
	// obj.name = datas[i].group;
	// groups.push(obj)
	// }
	// ms_group.setData(groups);
	// }
	// }
	// });
	//
	// $(ms_group).on('selectionchange', function(e, m) {
	// var val = this.getSelection();
	// var topics = new Array();
	// for (var i = 0; i < ms_data.length; i++) {
	// if (ms_data[i].group == val[0].name) {
	// for (var j = 0; j < ms_data[i].topics.length; j++) {
	// var obj = new Object();
	// obj.id = j + 1;
	// obj.name = ms_data[i].topics[j];
	// topics.push(obj);
	// }
	// }
	// }
	// ms_topic.setData(topics);
	// });
});