
var clusterTable = $("#efak_cluster_kafka_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/clusters/kafka/table/ajax",
    "aoColumns": [{
        "mData": 'brokerId'
    }, {
        "mData": 'brokerHost'
    }, {
        "mData": 'brokerPort'
    }, {
        "mData": 'brokerPortStatus'
    }, {
        "mData": 'brokerJmxPort'
    }, {
        "mData": 'brokerJmxPortStatus'
    }, {
        "mData": 'mem_used'
    }, {
        "mData": 'cpu_used'
    }, {
        "mData": 'startup'
    }, {
        "mData": 'version'
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
    clusterTable.ajax.reload();
}, 60000); // 1 min
