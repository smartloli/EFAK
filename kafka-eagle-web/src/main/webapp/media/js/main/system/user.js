$(document).ready(function() {
	$("#result").dataTable({
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/system/user/role/table/ajax",
		"aoColumns" : [ {
			"mData" : 'rtxno'
		}, {
			"mData" : 'username'
		}, {
			"mData" : 'realname'
		}, {
			"mData" : 'email'
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

	$("#ke-add-user-btn").click(function() {
		$('#ke_user_add_dialog').modal('show');
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/system/user/signin/rtxno/ajax/',
			success : function(datas) {
				$("#ke_rtxno_name").val(datas.rtxno);
				$("#ke_rtxno_name").attr("readonly","readonly");
			}
		});
	});

	$(document).on('click', 'a[name=operater_modify_modal]', function() {
		$('#ke_user_modify_dialog').modal('show');
		var href = $(this).attr("href");
		var id = href.split("#")[1];
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/system/user/signin/' + id + '/ajax',
			success : function(datas) {
				$("#ke_rtxno_name_modify").val(datas.rtxno);
				$("#ke_real_name_modify").val(datas.realname);
				$("#ke_user_name_modify").val(datas.username);
				$("#ke_user_email_modify").val(datas.email);
				$("#ke_user_id_modify").val(id);
			}
		});

	});

	var id = "";
	$(document).on('click', 'a[name=operater_modal]', function() {
		var href = $(this).attr("href");
		id = href.split("#")[1];
		$('#ke_setting_dialog').modal('show');
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/system/user/role/' + id + '/ajax',
			success : function(datas) {
				$("#ke_role_list").html("");
				console.log(datas);
				var chk = "";
				for (var i = 0; i < datas.role.length; i++) {
					chk += "<input class='actionRole' type='checkbox' value='" + datas.role[i].id + "'>" + datas.role[i].roleName + "&nbsp;&nbsp;";
				}
				$("#ke_role_list").append(chk);
				if (datas.userRole.length > 0) {
					$("input[type=checkbox]").each(function() {
						for (var i = 0; i < datas.userRole.length; i++) {
							if ($(this).is(":checked")) {
								$(this).attr("checked", false);
							}
							if ($(this).val() == datas.userRole[i].roleId) {
								$(this).attr("checked", true);
								break;
							}
						}
					});
				}
			}
		});
	});

	$(document).on("click", ".actionRole", function() {
		if ($(this).is(":checked")) {
			updateRole('/ke/system/user/role/add/' + id + '/' + $(this).val() + '/ajax')
		} else {
			updateRole('/ke/system/user/role/delete/' + id + '/' + $(this).val() + '/ajax')
		}
	});

	function updateRole(url) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : url,
			success : function(datas) {
				if (datas != null) {
					$("#alert_mssage_info").html("");
					$("#alert_mssage_info").append("<label>" + datas.info + "</label>")
					$("#alert_mssage_info").show();
					if (datas.code > 0) {
						$("#alert_mssage_info").addClass("alert alert-success");
					} else {
						$("#alert_mssage_info").addClass("alert alert-danger");
					}
					setTimeout(function() {
						$("#alert_mssage_info").hide()
					}, 3000);
				}
			}
		});
	}

});