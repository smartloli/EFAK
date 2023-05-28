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
