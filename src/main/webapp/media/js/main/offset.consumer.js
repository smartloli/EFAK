$(document).ready(function() {
	var url = window.location.href;
	var tmp = url.split("offset/")[1];
	var group = tmp.split("/")[0];
	var topic = tmp.split("/")[1];
	$("#topic_name_header").find("strong").text(topic);

	$("#result").dataTable({
		"searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/consumer/offset/" + group + "/" + topic + "/ajax",
		"aoColumns" : [ {
			"mData" : 'partition'
		}, {
			"mData" : 'logsize'
		}, {
			"mData" : 'offset'
		}, {
			"mData" : 'lag'
		}, {
			"mData" : 'owner'
		}, {
			"mData" : 'created'
		}, {
			"mData" : 'modify'
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