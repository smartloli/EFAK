$(document).ready(function() {
	$("#result").dataTable({
		// "searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/topic/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'topic'
		}, {
			"mData" : 'partitions'
		}, {
			"mData" : 'partitionNumbers'
		}, {
			"mData" : 'created'
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

	var topic = "";
	$(document).on('click', 'a[name=remove]', function() {
		var href = $(this).attr("href");
		topic = href.split("#")[1];
		var token = $("#ke_admin_token").val();
		$("#remove_div").html("");
		$("#remove_div").append("<a id='ke_del_topic' href='#' class='btn btn-danger'>Remove</a>");
		$('#doc_info').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#doc_info').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		if (token.length == 0) {
			$("#ke_del_topic").attr("disabled", true);
			$("#ke_del_topic").attr("href", "#");
		} else {
			$("#ke_del_topic").attr("href", "/ke/topic/" + topic + "/" + token + "/delete");
		}
	});

	$("#ke_admin_token").on('input', function(e) {
		var token = $("#ke_admin_token").val();
		if (token.length == 0) {
			$("#ke_del_topic").attr("disabled", true);
			$("#ke_del_topic").attr("href", "#");
		} else {
			$("#ke_del_topic").attr("disabled", false);
			$("#ke_del_topic").attr("href", "/ke/topic/" + topic + "/" + token + "/delete");
		}
	});
});