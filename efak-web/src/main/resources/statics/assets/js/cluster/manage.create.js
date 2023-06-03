// go clusters manage create
// batch import
try {
    var dr = $("#efak_kafka_broker_batch_import").dropify({
        messages: {
            'default': '将文件拖放到此处或单击',
            'replace': '拖放或单击文件来替换',
            'remove': '删除文件',
            'error': '抱歉，上传出现异常'
        },
        error: {
            'fileSize': '上传文件大小超过最大值 ({{ value }})',
            'fileExtension': '文件类型不正确，仅支持 {{ value }} 文件类型',
        }
    });
    dr.on('change', function () {
        var file = this.files[0];
        console.log(file);
    });
} catch (e) {
    console.log(e)
}

var cid = "";

try {
    url = window.location.href;
    if(url.indexOf("?")){
        cid = url.split("?")[1].split("=")[1]
    }
    $("#efak_clusterid").val(cid);

    console.log(cid);
}catch (e){
    console.log(e);
}

$("#efak_cluster_manage_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/clusters/manage/brokers/table/ajax",
    "aoColumns": [{
        "mData": 'brokerId'
    }, {
        "mData": 'brokerHost'
    }, {
        "mData": 'brokerPort'
    }, {
        "mData": 'brokerJmxPort'
    }, {
        "mData": 'modify'
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

$("#efka_kafka_kraft_enable").click(function () {
    $("#efak_kraft_enable_checked").show();
    $("#efak_kraft_disable_checked").hide();
    $("#efka_kafka_kraft_enable").css({border: "1px solid #4372ff"});
    $("#efka_kafka_kraft_disable").css({border: ""});
});

$("#efka_kafka_kraft_disable").click(function () {
    $("#efak_kraft_enable_checked").hide();
    $("#efak_kraft_disable_checked").show();
    $("#efka_kafka_kraft_enable").css({border: ""});
    $("#efka_kafka_kraft_disable").css({border: "1px solid #4372ff"});
});

$("#efak_cluster_create_cancle").click(function () {
    window.location.href = '/clusters/manage';
});

// import is null alert
function importFilesIsNull(){
    var file = $("#efak_kafka_broker_batch_import")[0];
    if (file.value == "") {
        alert("请选择文件");
        return false;
    }
    return true;
}
