var userTable = $("#efak_system_user_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/system/user/table/ajax",
    "aoColumns": [{
        "mData": 'id'
    }, {
        "mData": 'username'
    }, {
        "mData": 'password'
    }, {
        "mData": 'roles'
    }, {
        "mData": 'modify_time'
    }, {
        "mData": 'operate'
    }],
    language: {
        "sProcessing": "处理中...",
        "sLengthMenu": "显示 _MENU_ 项结果",
        "sZeroRecords": "没有匹配结果",
        "sInfo": "显示第 _START_ 至 _END_ 项结果，共 _TOTAL_ 项",
        "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
        "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
        "sInfoPostFix": "",
        "sSearch": "搜索:",
        "sUrl": "",
        "sEmptyTable": "表中数据为空",
        "sLoadingRecords": "载入中...",
        "sInfoThousands": ",",
        "oPaginate": {
            "sFirst": "首页",
            "sPrevious": "上页",
            "sNext": "下页",
            "sLast": "末页"
        },
        "oAria": {
            "sSortAscending": ": 以升序排列此列",
            "sSortDescending": ": 以降序排列此列"
        }
    }
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

setInterval(function () {
    userTable.ajax.reload();
}, 60000); // 1 min

$("#efak_system_user_name").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

$("#efak_system_user_password").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

// username role list
if ($("#efak_system_user_roles").length) {
    $("#efak_system_user_roles").select2({
        placeholder: "请选择用户角色",
        dropdownParent: $("#efak_system_user_add_modal"),
        allowClear: true,
        ajax: {
            url: "/system/user/roles/list/ajax",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.offset = 10;
                params.page = params.page || 1;
                return {
                    name: params.term,
                    page: params.page,
                    offset: params.offset
                };
            },
            cache: true,
            processResults: function (data, params) {
                if (data.items.length > 0) {
                    var datas = new Array();
                    $.each(data.items, function (index, e) {
                        var s = {};
                        s.id = index + 1;
                        s.text = e.text;
                        datas[index] = s;
                    });
                    return {
                        results: datas,
                        pagination: {
                            more: (params.page * params.offset) < data.total
                        }
                    };
                } else {
                    return {
                        results: []
                    }
                }
            },
            escapeMarkup: function (markup) {
                return markup;
            },
            minimumInputLength: 0
        }
    });
}

$('#efak_system_user_roles').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_system_user_roles_hidden").val(text);
});

function alertNoti(msg, icon) {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer)
            toast.addEventListener('mouseleave', Swal.resumeTimer)
        }
    })

    // error or success
    Toast.fire({
        icon: icon,
        title: msg
    })
}

// add user
$("#efak_system_user_submit").click(function () {
    var username = $("#efak_system_user_name").val();
    var password = $("#efak_system_user_password").val();
    var roles = $("#efak_system_user_roles_hidden").val();
    if (username.length == 0) {
        alertNoti("用户名不能为空", "error");
        return;
    }
    if (password.length == 0) {
        alertNoti("密码不能为空", "error");
        return;
    }
    if (roles.length == 0) {
        alertNoti("角色不能为空", "error");
        return;
    }

    $.ajax({
        url: '/system/user/info/add',
        method: 'POST',
        data: {
            username: username,
            password: password,
            roles: roles,
            uid: 0
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '用户名[<code>' + username + '</code>]创建完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/system/user';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '新增用户发生异常', 'error');
        }
    });
});

// edit user
$(document).on('click', 'a[name=efak_system_user_edit]', function (event) {
    event.preventDefault();
    var username = $(this).attr("username");
    var password = $(this).attr("password");
    var uid = $(this).attr("uid");
    $('#efak_system_user_edit_modal').modal('show');
    $("#efak_system_user_name_edit").val(username);
    $("#efak_system_user_password_edit").val(password);
    $("#efak_system_user_id_edit_hidden").val(uid);
});

// edit sumbit
$("#efak_system_user_edit_submit").click(function () {
    var username = $("#efak_system_user_name_edit").val();
    var password = $("#efak_system_user_password_edit").val();
    var roles = $("#efak_system_user_roles_edit_hidden").val();
    var uid = $("#efak_system_user_id_edit_hidden").val();
    if (username.length == 0) {
        alertNoti("用户名不能为空", "error");
        return;
    }

    if (password.length == 0) {
        alertNoti("密码不能为空", "error");
        return;
    }

    if (roles.length == 0) {
        alertNoti("角色不能为空", "error");
        return;
    }

    $.ajax({
        url: '/system/user/info/edit',
        method: 'POST',
        data: {
            username: username,
            password: password,
            roles: roles,
            uid: uid
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '用户名[<code>' + username + '</code>]修改完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/system/user';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '修改用户信息发生异常', 'error');
        }
    });
});

function optNoti(uid, username, action, actionDesc, html) {
    Swal.fire({
        customClass: {
            confirmButton: 'efak-noti-custom-common-btn-submit'
        },
        buttonsStyling: false,
        title: '确定执行' + actionDesc + '操作吗?',
        html: html,
        icon: 'warning',
        showCloseButton: true,
        showCancelButton: false,
        focusConfirm: false,
        cancelButtonClass: 'me-2',
        confirmButtonText: actionDesc,
        reverseButtons: true,
        scrollbarPadding: false
    }).then((result) => {
        if (result.isConfirmed) {
            // send ajax request
            $.ajax({
                url: '/system/user/info/' + action,
                method: 'POST',
                data: {
                    uid: uid
                },
                success: function (response) {
                    Swal.fire({
                        title: '成功',
                        icon: 'success',
                        html: '用户名 [<code>' + username + '</code>] 已被' + actionDesc,
                        allowOutsideClick: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/system/user';
                        }
                    });
                },
                error: function (xhr, status, error) {
                    Swal.fire('失败', '用户' + actionDesc + '发生异常', 'error');
                }
            });
        }
    })
}

// delete user
$(document).on('click', 'a[name=efak_system_user_delete]', function (event) {
    event.preventDefault();
    var uid = $(this).attr("uid");
    var username = $(this).attr("username");
    optNoti(uid, username, 'delete', '删除', '用户 [<code>' + username + '</code>] 删除后不能被恢复!');
});

// edit user
// username role list
if ($("#efak_system_user_roles_edit").length) {
    $("#efak_system_user_roles_edit").select2({
        placeholder: "请选择用户角色",
        dropdownParent: $("#efak_system_user_edit_modal"),
        allowClear: true,
        ajax: {
            url: "/system/user/roles/list/ajax",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.offset = 10;
                params.page = params.page || 1;
                return {
                    name: params.term,
                    page: params.page,
                    offset: params.offset
                };
            },
            cache: true,
            processResults: function (data, params) {
                if (data.items.length > 0) {
                    var datas = new Array();
                    $.each(data.items, function (index, e) {
                        var s = {};
                        s.id = index + 1;
                        s.text = e.text;
                        datas[index] = s;
                    });
                    return {
                        results: datas,
                        pagination: {
                            more: (params.page * params.offset) < data.total
                        }
                    };
                } else {
                    return {
                        results: []
                    }
                }
            },
            escapeMarkup: function (markup) {
                return markup;
            },
            minimumInputLength: 0
        }
    });
}

$('#efak_system_user_roles_edit').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_system_user_roles_edit_hidden").val(text);
});

