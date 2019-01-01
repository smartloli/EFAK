$(document).ready(function() {
	$("#kafka_tab").dataTable({
		// "searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/cluster/info/kafka/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'ip'
		}, {
			"mData" : 'port'
		}, {
			"mData" : 'created'
		}, {
			"mData" : 'modify'
		}, {
			"mData" : 'version'
		} ]
	});

	$("#zk_tab").dataTable({
		// "searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/cluster/info/zk/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'ip'
		}, {
			"mData" : 'port'
		}, {
			"mData" : 'mode'
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