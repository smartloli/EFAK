var topicTable = $("#efak_system_job_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/system/job/table/ajax",
    "aoColumns": [{
        "mData": 'group'
    }, {
        "mData": 'name'
    }, {
        "mData": 'cron'
    }, {
        "mData": 'start_time'
    }, {
        "mData": 'next_fire_time'
    }, {
        "mData": 'status'
    }, {
        "mData": 'time_zone'
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
    topicTable.ajax.reload();
}, 60000); // 1 min

// Job name list
if ($("#efak_system_job_task_name").length) {
    $("#efak_system_job_task_name").select2({
        placeholder: "请选择任务名称",
        dropdownParent: $("#efak_system_job_add_modal"),
        allowClear: true,
        ajax: {
            url: "/system/job/name/list/ajax",
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

$('#efak_system_job_task_name').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_system_job_task_name_hidden").val(text);
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

// add job
$("#efak_system_job_submit").click(function () {
    var groupName = $("#efak_system_job_group_name").val();
    var jobName = $("#efak_system_job_task_name_hidden").val();
    var cronName = $("#efak_system_job_cron_name").val();
    if (jobName.length == 0) {
        alertNoti("任务名称不能为空", "error");
        return;
    }
    if (cronName.length == 0) {
        alertNoti("执行频率不能为空", "error");
        return;
    }

    $.ajax({
        url: '/system/job/task/add',
        method: 'POST',
        data: {
            jobGroupName: groupName,
            jobName: jobName,
            cronName: cronName
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '任务名称[<code>' + jobName + '</code>]创建完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/system/job';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '创建集群发生异常', 'error');
        }
    });
});

// edit topic
$(document).on('click', 'a[name=efak_system_job_edit]', function (event) {
    event.preventDefault();
    var jobGroup = $(this).attr("job_group");
    var jobName = $(this).attr("job_name");
    var jobNameDesc = $(this).attr("job_name_desc");
    var cronName = $(this).attr("cron_name");
    $('#efak_system_job_edit_modal').modal('show');
    $("#efak_system_job_group_name_edit").val(jobGroup);
    $("#efak_system_job_task_name_edit").val(jobNameDesc);
    $("#efak_system_job_cron_name_edit").val(cronName);
});

// edit sumbit
$("#efak_system_job_edit_submit").click(function () {
    var groupName = $("#efak_system_job_group_name_edit").val();
    var jobName = $("#efak_system_job_task_name_edit").val();
    var cronName = $("#efak_system_job_cron_name_edit").val();
    if (cronName.length == 0) {
        alertNoti("执行频率不能为空", "error");
        return;
    }

    $.ajax({
        url: '/system/job/task/add',
        method: 'POST',
        data: {
            jobGroupName: groupName,
            jobName: jobName,
            cronName: cronName
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '任务名称[<code>' + jobName + '</code>]修改完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/system/job';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '创建集群发生异常', 'error');
        }
    });
});

function optNoti(jobGroup, jobName, jobNameDesc, action, actionDesc, html) {
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
                url: '/system/job/task/' + action,
                method: 'POST',
                data: {
                    jobGroup: jobGroup,
                    jobName: jobName
                },
                success: function (response) {
                    Swal.fire({
                        title: '成功',
                        icon: 'success',
                        html: '任务名称 [<code>' + jobNameDesc + '</code>] 已被' + actionDesc,
                        allowOutsideClick: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/system/job';
                        }
                    });
                },
                error: function (xhr, status, error) {
                    Swal.fire('失败', '任务' + actionDesc + '发生异常', 'error');
                }
            });
        }
    })
}

// delete job
$(document).on('click', 'a[name=efak_system_job_delete]', function (event) {
    event.preventDefault();
    var jobGroup = $(this).attr("job_group");
    var jobName = $(this).attr("job_name");
    var jobNameDesc = $(this).attr("job_name_desc");
    optNoti(jobGroup, jobName, jobNameDesc, 'delete', '删除', '任务名称 [<code>' + jobNameDesc + '</code>] 删除后不能定时执行任务!');
});

// pause job
$(document).on('click', 'a[name=efak_system_job_pause]', function (event) {
    event.preventDefault();
    var jobGroup = $(this).attr("job_group");
    var jobName = $(this).attr("job_name");
    var jobNameDesc = $(this).attr("job_name_desc");
    optNoti(jobGroup, jobName, jobNameDesc, 'pause', '暂停', '任务名称 [<code>' + jobNameDesc + '</code>] 暂停后不能定时执行任务!');
});

// resume job
$(document).on('click', 'a[name=efak_system_job_resume]', function (event) {
    event.preventDefault();
    var jobGroup = $(this).attr("job_group");
    var jobName = $(this).attr("job_name");
    var jobNameDesc = $(this).attr("job_name_desc");
    optNoti(jobGroup, jobName, jobNameDesc, 'resume', '恢复', '任务名称 [<code>' + jobNameDesc + '</code>] 恢复后将定时执行任务!');
});