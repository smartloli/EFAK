$(document).ready(function() {
	var username = $("#username").text();
	$("#result").dataTable({
		"searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"oLanguage" : {
			"sSearch" : "搜索",
			"sLengthMenu" : "每页显示 _MENU_ 条记录",
			"sZeroRecords" : "没有检索到数据",
			"sInfo" : "显示 _START_ 至 _END_ 条 &nbsp;&nbsp;共 _TOTAL_ 条",
			"sInfoFiltered" : "(筛选自 _MAX_ 条数据)",
			"sInfoEmtpy" : "没有数据",
			"sProcessing" : "正在加载数据..."
		},
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/cms/article/" + username + "/table/ajax",
		"aoColumns" : [ {
			"mData" : 'title'
		}, {
			"mData" : 'chanle'
		}, {
			"mData" : 'isTop'
		}, {
			"mData" : 'author'
		}, {
			"mData" : 'createDate'
		}, {
			"mData" : 'status'
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
});
