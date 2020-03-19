$(document).ready(function() {

	function getQueryString(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		var context = "";
		if (r != null)
			context = r[2];
		reg = null;
		r = null;
		return context == null || context == "" || context == "undefined" ? "" : context;
	}

	var group = getQueryString("group");
	var topic = getQueryString("topic");
	$("#topic_name_header").find("strong").html("<a href='/ke/consumers/offset/realtime/?group=" + group + "&topic=" + topic + "'>" + topic + "</a>");

	var offset = 0;

	function offsetDetail() {
		$("#offset_topic_info").append("<div id='div_children" + offset + "'><table id='result_children" + offset + "' class='table table-bordered table-hover' width='100%'><thead><tr><th>Partition</th><th>LogSize</th><th>Offset</th><th>Lag</th><th>Owner</th><th>Created</th><th>Modify</th></tr></thead></table></div>");
		if (offset > 0) {
			$("#div_children" + (offset - 1)).remove();
		}
		$("#result_children" + offset).dataTable({
			"searching" : false,
			"bSort" : false,
			"bLengthChange" : false,
			"bProcessing" : true,
			"bServerSide" : true,
			"fnServerData" : retrieveData,
			"sAjaxSource" : "/ke/consumer/offset/group/topic/ajax",
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

		offset++;
	}

	function retrieveData(sSource, aoData, fnCallback) {
		$.ajax({
			"type" : "get",
			"contentType" : "application/json",
			"url" : sSource,
			"dataType" : "json",
			"data" : {
				aoData : JSON.stringify(aoData),
				group : group,
				topic : topic
			},
			"success" : function(data) {
				fnCallback(data)
			}
		});
	}

	offsetDetail();

	// 5s/per to the background service request details of the state of offset .
	// setInterval(offsetDetail, 1000 * 30);
});