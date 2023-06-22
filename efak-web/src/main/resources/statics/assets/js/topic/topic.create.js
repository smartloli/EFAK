$("#efak_topic_name_create").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

$("#efak_topic_name_create").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

if ($("#efak_topic_name_retain_unit_create").length) {
    $("#efak_topic_name_retain_unit_create").select2();
}

$("#efak_topic_create_cancle").click(function () {
    window.location.href = '/topic/list';
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

function convertMs(retain,unit){
    var retainMs;
    switch (unit) {
        case "ms":
            retainMs = retain;
        case "min":
            retainMs = retain * 60 * 1000;
        case "hour":
            retainMs = retain * 60 * 60 * 1000;
        case "day":
            retainMs = retain * 24 * 60 * 60 * 1000;
    }
    return retainMs;
}

// create topic
$("#efak_topic_create_submit").click(function () {
    var topicName = $("#efak_topic_name_create").val();
    var partitions = $("#efak_topic_name_partition_create").val();
    var replication = $("#efak_topic_name_replicate_create").val();
    var retainMs = $("#efak_topic_name_retain_create").val();
    var retainMsUnit = $("#efak_topic_name_retain_unit_create").val();

    if (topicName.length == 0) {
        alertNoti("主题名称不能为空", "error");
        return;
    }
    if (!$.isNumeric(partitions) || !$.isNumeric(partitions)) {
        errorNoti("分区数必须为数字", "error");
        return;
    }
    if (!$.isNumeric(replication) || !$.isNumeric(replication)) {
        errorNoti("副本数必须为数字", "error");
        return;
    }
    if (!$.isNumeric(retainMs) || !$.isNumeric(retainMs)) {
        errorNoti("数据保存时间必须为数字", "error");
        return;
    }
    if (retainMsUnit.length == 0) {
        alertNoti("请选择数据保存时间单位", "error");
        return;
    }


    $.ajax({
        url: '/topic/name/create',
        method: 'POST',
        data: {
            topicName: topicName,
            partitions: partitions,
            replication: replication,
            retainMs: convertMs(retainMs,retainMsUnit)
        },
        success: function (response) {
            result = JSON.parse(response);
            if(result.status) {
                Swal.fire({
                    title: '成功',
                    icon: 'success',
                    html: '主题名称[<code>' + topicName + '</code>]创建完成',
                    allowOutsideClick: false
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/topic/list';
                    }
                });
            }else{
                Swal.fire('失败', result.msg, 'error');
            }
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '创建主题发生异常', 'error');
        }
    });
});