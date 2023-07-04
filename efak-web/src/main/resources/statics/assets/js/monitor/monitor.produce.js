
if ($("#efak_topic_name_produce_list").length) {
    $("#efak_topic_name_produce_list").select2({
        placeholder: "请选择主题名称",
        // theme: 'bootstrap4',
        // width: $(this).data('width') ? $(this).data('width') : $(this).hasClass('w-100') ? '100%' : 'style',
        allowClear: true,
        ajax: {
            url: "/topic/name/mock/ajax",
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

$('#efak_topic_name_produce_list').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    //$("#efak_topic_name_produce_list").val(text);
    //$("#efak_topic_name_mock_hidden").val(text);
});

// create topic
$("#efak_topic_mock_submit").click(function () {
    var topicName = $("#efak_topic_name_mock_hidden").val();
    var message = $("#efak_topic_name_mock_textarea").val();

    if (topicName.length == 0) {
        alertNoti("主题名称不能为空", "error");
        return;
    }
    if (message.length == 0) {
        alertNoti("发送内容不能为空", "error");
        return;
    }

    $.ajax({
        url: '/topic/name/mock/send',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json;charset=UTF-8',
        data: JSON.stringify({
            "topicName": topicName,
            "message": message
        }),
        success: function (response) {
            // result = JSON.parse(response);
            if (response.status) {
                Swal.fire({
                    title: '成功',
                    icon: 'success',
                    html: '主题名称[<code>' + topicName + '</code>]内容发送完成，<br/>请跳转到主题详情页预览数据！',
                    allowOutsideClick: false
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/topic/meta/' + topicName;
                    }
                });
            } else {
                Swal.fire('失败', result.msg, 'error');
            }
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '测试主题发生异常', 'error');
        }
    });
});

// plugins by daterangepicker
try {

    var start = moment();
    var end = moment();

    function cb(start, end) {
        $('#efak_topic_meta_date span').html(start.format('YYYY-MM-DD') + ' 至 ' + end.format('YYYY-MM-DD'));
    }

    // daterangepicker
    var reportrange = $('#efak_topic_meta_date').daterangepicker({
        startDate: start,
        endDate: end,
        ranges: {
            '今天': [moment(), moment()],
            '昨天': [moment().subtract(1, 'days'), moment()],
            '最近3天': [moment().subtract(3, 'days'), moment()],
            '最近7天': [moment().subtract(6, 'days'), moment()]
        },
        locale: {
            applyLabel: '确定',
            cancelLabel: '取消',
            customRangeLabel: '自定义时间'
        },
        applyClass: 'btn-sm btn-primary',
        cancelClass: 'btn-sm btn-secondary'

    }, cb);

    cb(start, end);
    var stime = reportrange[0].innerText.replace(/-/g, '').split("至")[0].trim();
    var etime = reportrange[0].innerText.replace(/-/g, '').split("至")[1].trim();

    producerMsg(stime, etime);

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("至")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("至")[1].trim();
        producerMsg(stime, etime);
    });
    setInterval(function () {
        producerMsg(stime, etime)
    }, 1000 * 60 * 5); // 5min
} catch (e) {
    console.log(e);
}