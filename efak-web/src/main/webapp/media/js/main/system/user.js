$(document).ready(function () {
    $("#efak_system_user_tab").dataTable({
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/system/user/role/table/ajax",
        "aoColumns": [{
            "mData": 'rtxno'
        }, {
            "mData": 'username'
        }, {
            "mData": 'realname'
        }, {
            "mData": 'email'
        },{
            "mData": 'operate'
        }]
    });

    function retrieveData(sSource, aoData, fnCallback) {
        $.ajax({
            "type": "get",
            "contentType": "application/json",
            "url": sSource,
            "dataType": "json",
            "data": {
                aoData: JSON.stringify(aoData)
            },
            "success": function (data) {
                fnCallback(data)
            }
        });
    }

    $("#ke-add-user-btn").click(function () {
        $('#ke_user_add_dialog').modal('show');
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/signin/rtxno/ajax/',
            success: function (datas) {
                $("#ke_rtxno_name").val(datas.rtxno);
            }
        });
    });

    $(document).on('click', 'a[name=ke_user_edit]', function () {
        $('#ke_user_modify_dialog').modal('show');
        var href = $(this).attr("href");
        var id = href.split("#")[1];
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/signin/' + id + '/ajax',
            success: function (datas) {
                $("#ke_rtxno_name_modify").val(datas.rtxno);
                $("#ke_real_name_modify").val(datas.realname);
                $("#ke_user_name_modify").val(datas.username);
                $("#ke_user_email_modify").val(datas.email);
                $("#ke_user_id_modify").val(id);
            }
        });

    });

    $(document).on('click', 'a[name=ke_user_reset]', function () {
        $('#ke_user_reset_dialog').modal('show');
        var href = $(this).attr("href");
        var id = href.split("#")[1];
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/signin/' + id + '/ajax',
            success: function (datas) {
                $("#ke_user_rtxno_reset").val(datas.rtxno);
            }
        });
    });

    var id = "";
    $(document).on('click', 'a[name=ke_user_assign]', function () {
        var href = $(this).attr("href");
        id = href.split("#")[1];
        $('#ke_user_assign_dialog').modal('show');
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/role/' + id + '/ajax',
            success: function (datas) {
                $("#ke_role_list").html("");
                var chk = "";
                for (var i = 0; i < datas.role.length; i++) {
                    chk += "<input class='actionRole' type='checkbox' value='" + datas.role[i].id + "'>" + datas.role[i].roleName + "&nbsp;&nbsp;";
                }
                $("#ke_role_list").append(chk);
                if (datas.userRole.length > 0) {
                    $("input[type=checkbox]").each(function () {
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

    $(document).on("click", ".actionRole", function () {
        if ($(this).is(":checked")) {
            updateRole('/system/user/role/add/' + id + '/' + $(this).val() + '/ajax')
        } else {
            updateRole('/system/user/role/delete/' + id + '/' + $(this).val() + '/ajax')
        }
    });

    function updateRole(url) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: url,
            success: function (datas) {
                if (datas != null) {
                    $("#ke_user_assign_result").html("");
                    $("#ke_user_assign_result").append("<label>" + datas.info + "</label>")
                    $("#ke_user_assign_result").show();
                    if (datas.code > 0) {
                        $("#ke_user_assign_result").addClass("alert alert-success");
                    } else {
                        $("#ke_user_assign_result").addClass("alert alert-danger");
                    }
                    setTimeout(function () {
                        $("#ke_user_assign_result").hide()
                    }, 3000);
                }
            }
        });
    }
});