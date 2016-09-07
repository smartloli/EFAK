$(document).ready(function() {

	// Initialization topic
	var ms = $("#ke_topic_alarm").magicSuggest({
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

	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/alarm/topic/ajax',
		success : function(datas) {
			if (datas != null) {
				ms.setData(datas);
			}
		}
	});
});