$(document).ready(function() {

	// Initialization group
	var ms_group = $("#ke_group_alarm").magicSuggest({
		allowFreeEntries : false,
		autoSelect : true,
		maxSelection : 1
	});

	// Initialization topic
	var ms_topic = $("#ke_topic_alarm").magicSuggest({
		allowFreeEntries : false,
		autoSelect : true,
		maxSelection : 1
	});

	$('#ke_topic_email').tokenfield({
		autocomplete : {
			source : [ 'example1@email.com' ],
			delay : 100
		},
		showAutocompleteOnFocus : true
	});

	var ms_data;
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/alarm/topic/ajax',
		success : function(datas) {
			if (datas != null) {
				ms_data = datas;
				var groups = new Array();
				for (var i = 0; i < datas.length; i++) {
					var obj = new Object();
					obj.id = i + 1;
					obj.name = datas[i].group;
					groups.push(obj)
				}
				ms_group.setData(groups);
			}
		}
	});

	$(ms_group).on('selectionchange', function(e, m) {
		var val = this.getSelection();
		var topics = new Array();
		for (var i = 0; i < ms_data.length; i++) {
			if (ms_data[i].group == val[0].name) {
				for (var j = 0; j < ms_data[i].topics.length; j++) {
					var obj = new Object();
					obj.id = j + 1;
					obj.name = ms_data[i].topics[j];
					topics.push(obj);
				}
			}
		}
		ms_topic.setData(topics);
	});
});