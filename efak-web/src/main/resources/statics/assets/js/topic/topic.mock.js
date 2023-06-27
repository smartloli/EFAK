$("#efak_topic_name_mock_textarea").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

if ($("#efak_topic_name_mock_list").length) {
    $("#efak_topic_name_mock_list").select2({
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

$("#efak_topic_create_cancle").click(function () {
    window.location.href = '/topic/manage';
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

function convertMs(retain, unit) {
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

$('#efak_topic_name_mock_list').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_topic_name_mock_list").val(text);
    $("#efak_topic_name_mock_hidden").val(text);
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