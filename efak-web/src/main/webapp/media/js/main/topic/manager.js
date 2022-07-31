$(document).ready(function () {

    $("#select2val").select2({
        placeholder: "Topic",
        theme: 'bootstrap4',
        ajax: {
            url: "/topic/mock/list/ajax",
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
        $("#ke_topic_name").val(text);
    });

    $("#select2key").select2({
        placeholder: "Key",
        theme: 'bootstrap4',
        ajax: {
            url: "/topic/manager/keys/ajax",
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

    $('#select2key').on('select2:select', function (evt) {
        var text = evt.params.data.text;
        $("#select2key").val(text);
        $("#ke_topic_key").val(text);
    });

    $(document).on("click", "#btn_send", function () {
        var topic = $("#ke_topic_name").val();
        var type = $('input[name="ke_topic_alter"]:checked').val();
        console.log(topic + "," + type);
        if (type == "add_config") {
            var key = $("#ke_topic_key").val();
            var value = $("#ke_topic_value").val();
            if (topic.length == 0) {
                errorNoti("Add topic property select name cannot be empty.");
            } else if (key.length == 0) {
                errorNoti("Add topic property key select cannot be empty.");
            } else if (value.length == 0) {
                errorNoti("Add topic property value input cannot be empty.");
            } else {
                alterTopicConfig('add', topic, key, value);
            }
        } else if (type == "del_config") {
            var key = $("#ke_topic_key").val();
            if (topic.length == 0) {
                errorNoti("Delete topic property select name cannot be empty.");
            } else if (key.length == 0) {
                errorNoti("Delete topic property key select cannot be empty.");
            } else {
                alterTopicConfig('delete', topic, key, '');
            }
        } else if (type == "desc_config") {
            if (topic.length == 0) {
                errorNoti("Describe topic property select name cannot be empty.");
            } else {
                alterTopicConfig('describe', topic, '', '');
            }
        }
    });

    function alterTopicConfig(type, topic, key, value) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/manager/' + type + '/ajax?topic=' + topic + '&key=' + key + '&value=' + value,
            success: function (datas) {
                if (datas != null) {
                    $("#ke_topic_config_content").text(datas.result);
                }
            }
        });
    }

    $(":radio").click(function () {
        console.log($(this).val());
        if ($(this).val() == "add_config") {
            $("#div_topic_keys").show();
            $("#div_topic_value").show();
            $("#div_topic_msg").show();
            $("#efak_topic_metadata_key_hr").show();
            $("#efak_topic_metadata_value_hr").show();
            $("#ke_topic_config_content").text("");
        } else if ($(this).val() == "del_config") {
            $("#div_topic_keys").show();
            $("#div_topic_value").hide();
            $("#efak_topic_metadata_key_hr").show();
            $("#efak_topic_metadata_value_hr").hide();
            $("#div_topic_msg").show();
            $("#ke_topic_config_content").text("");
        } else if ($(this).val() == "desc_config") {
            $("#div_topic_keys").hide();
            $("#div_topic_value").hide();
            $("#efak_topic_metadata_key_hr").hide();
            $("#efak_topic_metadata_value_hr").hide();
            $("#div_topic_msg").show();
            $("#ke_topic_config_content").text("");
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

});