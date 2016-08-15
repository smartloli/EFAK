$(document).ready(function() {
	var url = window.location.href;
	var topicName = url.split("meta/")[1];

	$("#result").dataTable({
		"searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/topic/meta/" + topicName + "/ajax",
		"aoColumns" : [ {
			"mData" : 'topic'
		}, {
			"mData" : 'partition'
		}, {
			"mData" : 'leader'
		}, {
			"mData" : 'replicas'
		}, {
			"mData" : 'isr'
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

});