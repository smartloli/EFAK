$(document).ready(function() {

	// Initialization group
	var ms_type = $("#ke_type_alarm_name").magicSuggest({
		allowFreeEntries : false,
		autoSelect : true,
		maxSelection : 1,
		data : [ 'Zookeeper', 'Kafka' ]
	});

	$('#ke_cluster_email').tokenfield({
		autocomplete : {
			source : [ 'example1@email.com' ],
			delay : 100
		},
		showAutocompleteOnFocus : true
	});

});