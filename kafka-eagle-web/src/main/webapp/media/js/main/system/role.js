$(document).ready(function() {

	$("#result").dataTable({
		"searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/system/role/table/ajax",
		"aoColumns" : [ {
			"mData" : 'name'
		}, {
			"mData" : 'describer'
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

	$(document).on('click', 'a[name=operater_modal]', function() {
		var href = $(this).attr("href");
		var id = href.split("#")[1];
		$('#ke_setting_dialog').modal('show');
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/system/role/resource/' + id + '/ajax',
			success : function(datas) {
				if (datas != null) {
					console.log(datas);
					$('#treeview-checkable').treeview({
						data : datas,
						showIcon : false,
						showCheckbox : true,
						onNodeChecked : function(event, node) {
							updateRole('/ke/system/role/insert/' + id + '/' + node.href + '/');
						},
						onNodeUnchecked : function(event, node) {
							updateRole('/ke/system/role/delete/' + id + '/' + node.href + '/');
						}
					});
				}
			}
		});
	});

	function updateRole(url) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : url,
			success : function(datas) {
				if (datas != null) {
					console.log(datas);
					$("#ke_role_alert_mssage").html("");
					$("#ke_role_alert_mssage").append("<label>" + datas.info + "</label>")
					$("#ke_role_alert_mssage").show();
					if (datas.code > 0) {
						$("#ke_role_alert_mssage").addClass("alert alert-success");
					} else {
						$("#ke_role_alert_mssage").addClass("alert alert-danger");
					}
					setTimeout(function() {
						$("#ke_role_alert_mssage").hide()
					}, 3000);
				}
			}
		});
	}

});