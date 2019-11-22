$(document).ready(function() {
	
	$("#ksql_history_result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/topic/sql/history/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'username'
		}, {
			"mData" : 'host'
		}, {
			"mData" : 'ksql'
		}, {
			"mData" : 'status'
		}, {
			"mData" : 'spendTime'
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
	
	// Show detail content
	$(document).on('click', 'a[name=ke_sql_query_detail]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1].split("/")[0];
		var type = href.split("#")[1].split("/")[1];
		$('#ke_sql_query_detail').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#ke_sql_query_detail').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/topic/ksql/detail/' + type + '/' + id + '/ajax',
			success : function(datas) {
				$("#ke_sql_query_content").val(datas.result);
			}
		});
	});
	
});