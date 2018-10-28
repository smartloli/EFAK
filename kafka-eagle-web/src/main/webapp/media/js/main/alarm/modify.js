$(document).ready(function() {
	$("#result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/alarm/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'group'
		}, {
			"mData" : 'topic'
		}, {
			"mData" : 'lag'
		}, {
			"mData" : 'owner'
		}, {
			"mData" : 'created'
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
		var id = href.split("#")[1].split("/")[0];
		$("#remove_div").html("");
		$("#remove_div").append("<a href='/ke/alarm/" + id + "/del' class='btn btn-danger'>Remove</a>");
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
	});

	$(document).on('click', 'a[name=modify]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1].split("/")[0];
		$("#ke_consumer_id_lag").val(id);
		$('#modfiy_info').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#modfiy_info').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});
		
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/alarm/consumer/modify/' + id + '/ajax',
			success : function(datas) {
				$("#ke_consumer_name_lag").val(datas.lag);
				$("#ke_owners_modify").val(datas.owners);
			}
		});
	});
});