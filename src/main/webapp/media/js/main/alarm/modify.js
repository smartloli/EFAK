$(document).ready(function() {
	$("#result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/alarm/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'topic'
		}, {
			"mData" : 'lag'
		}, {
			"mData" : 'owner'
		}, {
			"mData" : 'modify'
		}, {
			"mData" : 'operate'
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

	$(document).on('click', 'a[name=remove]', function() {
		var href = $(this).attr("href");
		var topic = href.split("#")[1].split("/")[0];
		var owner = href.split("#")[1].split("/")[1];
		$("#remove_div").html("");
		$("#remove_div").append("<a href='/ke/alarm/" + href.split("#")[0] + "/" + href.split("#")[1] + "/del' class='btn btn-danger'>Remove</a>");
		$('#doc_info').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#doc_info').modal('show').css({
			width : '200px',
			height : '150px',
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});
	});
});