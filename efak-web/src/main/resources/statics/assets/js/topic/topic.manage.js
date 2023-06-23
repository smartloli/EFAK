
var topicTable = $("#efak_topic_manage_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/topic/manage/table/ajax",
    "aoColumns": [{
        "mData": 'topicName'
    }, {
        "mData": 'partition'
    }, {
        "mData": 'replicas'
    }, {
        "mData": 'brokerSpread'
    }, {
        "mData": 'brokerSkewed'
    }, {
        "mData": 'brokerLeaderSkewed'
    }, {
        "mData": 'retainMs'
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
}); // ._fnAjaxUpdate()

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

function delNoti(dataid, clusterName) {
    Swal.fire({
        customClass: {
            confirmButton: 'efak-noti-custom-common-btn-submit'
        },
        buttonsStyling: false,
        title: '确定执行删除操作吗?',
        html: "集群名称 [<code>" + clusterName + "</code>] 删除后不能被恢复!",
        icon: 'warning',
        showCloseButton: true,
        showCancelButton: false,
        focusConfirm: false,
        cancelButtonClass: 'me-2',
        confirmButtonText: '删除',
        reverseButtons: true,
        scrollbarPadding: false
    }).then((result) => {
        if (result.isConfirmed) {
            // send ajax request
            $.ajax({
                url: '/clusters/manage/cluster/del',
                method: 'POST',
                data: {
                    dataid: dataid
                },
                success: function (response) {
                    Swal.fire({
                        title: '成功',
                        icon: 'success',
                        html: '集群名称 [<code>' + clusterName + '</code>] 已被删除',
                        allowOutsideClick: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.reload();
                        }
                    });
                },
                error: function (xhr, status, error) {
                    Swal.fire('失败', '数据删除发生异常', 'error');
                }
            });
        }
    })
}

// delete cluster
$(document).on('click', 'a[name=efak_cluster_node_manage_del]', function (event) {
    event.preventDefault();
    var dataid = $(this).attr("dataid");
    var clusterName = $(this).attr("clusterName");
    delNoti(dataid, clusterName);
});


// edit topic
$(document).on('click', 'a[name=efak_topic_manage_add_partition]', function (event) {
    event.preventDefault();
    var clusterId = $(this).attr("cid");
    var topic = $(this).attr("topic");
    var partitions = $(this).attr("partitions");

    $('#efak_topic_partition_add_modal').modal('show');
    $("#efak_clusterid").val(clusterId);
    $("#efak_topic_name_manage").val(topic);
    $("#efak_topic_name_manage_partition_current").val(partitions);
    $("#efak_topic_name_manage_partition_new").val(partitions);
});