$(document).ready(function () {
    $("#select2val").select2({
        placeholder: "Alarm Type",
        theme: 'bootstrap4',
        ajax: {
            url: "/alarm/type/list/ajax",
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
            minimumInputLength: 1
        }
    });

    $('#select2val').on('select2:select', function (evt) {
        var text = evt.params.data.text;
        $("#select2val").val(text);
        $("#ke_alarm_type").val(text);
        if (text.indexOf("Email") > -1) {
            $("#div_alarm_http").hide();
            $("#div_alarm_address").show();
            $("#label_alarm_url").show();
            $("#label_alarm_topic").hide();
            $("#ke_alarm_url").attr('placeholder', "http://127.0.0.1:10086/email");
        } else if (text.indexOf("WebHook") > -1) {
            $("#div_alarm_http").hide();
            $("#div_alarm_address").show();
            $("#label_alarm_url").show();
            $("#label_alarm_topic").hide();
            $("#ke_alarm_url").attr('placeholder', "http://127.0.0.1:10086/webhook");
        } else if (text.indexOf("DingDing") > -1) {
            $("#div_alarm_http").hide();
            $("#div_alarm_address").hide();
            $("#label_alarm_url").show();
            $("#label_alarm_topic").hide();
            $("#ke_alarm_url").attr('placeholder', "https://oapi.dingtalk.com/robot/send?access_token=");
        } else if (text.indexOf("WeChat") > -1) {
            $("#div_alarm_http").hide();
            $("#div_alarm_address").hide();
            $("#label_alarm_url").show();
            $("#label_alarm_topic").hide();
            $("#ke_alarm_url").attr('placeholder', "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=");
        } else if (text.indexOf("Kafka") > -1) {
            $("#div_alarm_http").hide();
            $("#div_alarm_address").hide();
            $("#label_alarm_url").hide();
            $("#label_alarm_topic").show();
            $("#ke_alarm_url").attr('placeholder', "kafka topic");
        }
    });

    function errorNoti(errorMsg) {
        console.log(errorMsg)
        Lobibox.notify('error', {
            pauseDelayOnHover: true,
            continueDelayOnInactiveTab: false,
            position: 'top right',
            icon: 'bx bx-x-circle',
            msg: errorMsg
        });
    }

    function successNoti(successMsg) {
        Lobibox.notify('success', {
            pauseDelayOnHover: true,
            continueDelayOnInactiveTab: false,
            position: 'top right',
            icon: 'bx bx-check-circle',
            msg: successMsg
        });
    }

    $("#btn_send_test").click(function () {
        var type = $("#ke_alarm_type").val();
        var url = $("#ke_alarm_url").val();
        var address = $("#ke_alarm_address").val();
        var msg = $("#ke_test_msg").val();
        if (type.length == 0) {
            errorNoti("Alarm type cannot be empty.");
        } else if (url.length == 0) {
            errorNoti("Alarm url cannot be empty.");
        } else if (msg.length == 0) {
            errorNoti("Alarm msg cannot be empty.");
        } else {
            $.ajax({
                type: 'post',
                dataType: 'json',
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify({
                    "url": url,
                    "type": type,
                    "address": address,
                    "msg": msg
                }),
                url: '/alarm/config/test/send/ajax',
                success: function (datas) {
                    console.log(datas)
                    if (type.indexOf("DingDing") > -1 || type.indexOf("WeChat") > -1 || type.indexOf("Email") > -1|| type.indexOf("Kafka") > -1) {
                        if (datas.errcode == 0) {
                            successNoti("Send test msg has successed.");
                        } else {
                            errorNoti("Send test msg has failed.");
                        }
                    }

                }
            });
        }
    });

});