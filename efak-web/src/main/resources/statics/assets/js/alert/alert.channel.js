var channelTable = $("#efak_alert_channel_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/alert/channel/table/ajax",
    "aoColumns": [{
        "mData": 'id'
    }, {
        "mData": 'channel_name'
    }, {
        "mData": 'channel_type'
    }, {
        "mData": 'channel_url'
    }, {
        "mData": 'channel_auth_json'
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
    channelTable.ajax.reload();
}, 60000); // 1 min

$("#efak_alert_channel_name").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

// username role list
if ($("#efak_alert_channel_type").length) {
    $("#efak_alert_channel_type").select2({
        placeholder: "请选择告警渠道",
        dropdownParent: $("#efak_alert_channel_add_modal"),
        allowClear: true,
        ajax: {
            url: "/alert/channel/type/list/ajax",
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

$('#efak_alert_channel_type').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_alert_channel_type_hidden").val(text);
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
$("#efak_alert_channel_submit").click(function () {
    var channel_name = $("#efak_alert_channel_name").val();
    var channel_url = $("#efak_alert_channel_url").val();
    var channel_type = $("#efak_alert_channel_type_hidden").val();
    var channel_auth_json = $("#efak_alert_channel_auth_json").val();
    if (channel_name.length == 0) {
        alertNoti("名称不能为空", "error");
        return;
    }
    if (channel_url.length == 0) {
        alertNoti("地址不能为空", "error");
        return;
    }
    if (channel_type.length == 0) {
        alertNoti("类型不能为空", "error");
        return;
    }

    $.ajax({
        url: '/alert/channel/info/add',
        method: 'POST',
        data: {
            channelName: channel_name,
            channelType: channel_type,
            channelUrl: channel_url,
            channelAuthJson: channel_auth_json,
            uid: 0
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '名称[<code>' + channel_name + '</code>]创建完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/alert/channel';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '新增渠道名称发生异常', 'error');
        }
    });
});

// edit channel
$(document).on('click', 'a[name=efak_alert_channel_edit]', function (event) {
    event.preventDefault();
    var uid = $(this).attr("alert_channel_id");
    $('#efak_alert_channel_edit_modal').modal('show');

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/alert/channel/get/info/ajax?uid=' + uid,
        success: function (datas) {
            $("#efak_alert_channel_name_edit").val(datas.name);
            $("#efak_alert_channel_url_edit").val(datas.url);
            $("#efak_alert_channel_type_edit_hidden").val(datas.type);
            $("#efak_alert_channel_auth_json_edit").val(datas.auth);
            $("#efak_alert_channel_id_edit_hidden").val(uid);
        }
    });

});

// edit sumbit
$("#efak_alert_channel_edit_submit").click(function () {
    var channel_name = $("#efak_alert_channel_name_edit").val();
    var channel_url = $("#efak_alert_channel_url_edit").val();
    var channel_type = $("#efak_alert_channel_type_edit_hidden").val();
    var channel_auth_json = $("#efak_alert_channel_auth_json_edit").val();
    var uid = $("#efak_alert_channel_id_edit_hidden").val();
    if (channel_name.length == 0) {
        alertNoti("名称不能为空", "error");
        return;
    }

    if (channel_url.length == 0) {
        alertNoti("地址不能为空", "error");
        return;
    }

    if (channel_type.length == 0) {
        alertNoti("类型不能为空", "error");
        return;
    }

    $.ajax({
        url: '/alert/channel/info/edit',
        method: 'POST',
        data: {
            channelName: channel_name,
            channelType: channel_type,
            channelUrl: channel_url,
            channelAuthJson: channel_auth_json,
            uid: uid
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '渠道名称[<code>' + channel_name + '</code>]修改完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/alert/channel';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '修改渠道信息发生异常', 'error');
        }
    });
});

function optNoti(uid, channelName, action, actionDesc, html) {
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
                url: '/alert/channel/info/' + action,
                method: 'POST',
                data: {
                    uid: uid
                },
                success: function (response) {
                    Swal.fire({
                        title: '成功',
                        icon: 'success',
                        html: '渠道名称 [<code>' + channelName + '</code>] 已被' + actionDesc,
                        allowOutsideClick: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/alert/channel';
                        }
                    });
                },
                error: function (xhr, status, error) {
                    Swal.fire('失败', '渠道名称' + actionDesc + '发生异常', 'error');
                }
            });
        }
    })
}

// delete user
$(document).on('click', 'a[name=efak_alert_channel_delete]', function (event) {
    event.preventDefault();
    var uid = $(this).attr("alert_channel_id");
    var channelName = $(this).attr("channel_name");
    optNoti(uid, channelName, 'delete', '删除', '渠道名称 [<code>' + channelName + '</code>] 删除后不能被恢复!');
});

// edit user
// username role list
if ($("#efak_alert_channel_type_edit").length) {
    $("#efak_alert_channel_type_edit").select2({
        placeholder: "请选择告警渠道",
        dropdownParent: $("#efak_alert_channel_edit_modal"),
        allowClear: true,
        ajax: {
            url: "/alert/channel/type/list/ajax",
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

$('#efak_alert_channel_type_edit').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_alert_channel_type_edit_hidden").val(text);
});

// view url details
$(document).on('click', 'a[name=channel_view_url]', function (event) {
    event.preventDefault();
    var uid = $(this).attr("alert_channel_url_len_id");
    $('#efak_alert_channel_view_modal').modal('show');

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/alert/channel/get/info/ajax?uid=' + uid,
        success: function (datas) {
            $("#efak_alert_channel_name_view").text(datas.url);
        }
    });

});
