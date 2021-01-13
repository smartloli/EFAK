$(document).ready(function () {
    var mime = 'text/x-mariadb';
    // get mime type
    if (window.location.href.indexOf('mime=') > -1) {
        mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
    }
    var topicBalanceProposed = CodeMirror.fromTextArea(document.getElementById('code_proposed'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true
    });

    var topicBalanceCurrent = CodeMirror.fromTextArea(document.getElementById('code_current'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    var topicBalanceResult = CodeMirror.fromTextArea(document.getElementById('code_result'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    $("#select2val").select2({
        placeholder: "Topic",
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
            minimumInputLength: 0,
            allowClear: true
        }
    });

    $('#select2val').on('change', function (evt) {
        var o = document.getElementById('select2val').getElementsByTagName('option');
        var arrs = [];
        for (var i = 0; i < o.length; i++) {
            if (o[i].selected) {
                arrs.push(o[i].innerText);
            }
        }
        $("#ke_topic_balance").val(arrs);
    });

    $("#ke_balancer_generate").on('click', function () {
        var radio = $('input:radio[name="ke_topic_balance_type"]:checked').val();
        if (radio == "balance_single") {
            if ($("#ke_topic_balance").val().length > 0) {
                var topics = $("#ke_topic_balance").val().split(",");
                generate(topics, "SINGLE");
            } else {
                topicBalanceResult.setValue("Balance topic generate can not null.");
            }
        } else if (radio == "balance_all") {
            topicBalanceResult.setValue("Balance all topic generate will be running.");
            generate("", "ALL");
        }
    });

    function isJson(str) {
        try {
            if (typeof JSON.parse(str) == 'object')
                return true;
            return false;
        } catch (e) {
            topicBalanceResult.setValue(e.message);
            return false;
        }
    }

    $("#ke_balancer_execute").on('click', function () {
        var proposedValue = topicBalanceProposed.getValue();
        if (isJson(proposedValue)) {
            var json = proposedValue.replace(/\ +/g, "").replace(/[\r\n]/g, "")
            $.ajax({
                type: 'post',
                dataType: 'json',
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify({
                    "json": json
                }),
                url: '/topic/balance/execute/ajax',
                success: function (datas) {
                    if (datas != null) {
                        if (datas.hasOwnProperty("success") && datas.success) {
                            document.getElementById("ke_balancer_verify").style.display = "";
                            topicBalanceResult.setValue(datas.result);
                        }
                        if (datas.hasOwnProperty("error")) {
                            topicBalanceResult.setValue(datas.error);
                        }
                    }
                }
            });
        }
    });

    $("#ke_balancer_verify").on('click', function () {
        var radio = $('input:radio[name="ke_topic_balance_type"]:checked').val();
        if (radio == "balance_single") {
            if ($("#ke_topic_balance").val().length > 0) {
                var topics = $("#ke_topic_balance").val().split(",");
                verify(topics, "SINGLE");
            } else {
                topicBalanceResult.setValue("Balance topic verify can not null.");
            }
        } else if (radio == "balance_all") {
            topicBalanceResult.setValue("Balance all topic verify will be running.");
            verify("", "ALL");
        }
    });

    function verify(topics, type) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/balance/verify/ajax/?topics=' + topics + '&type=' + type,
            success: function (datas) {
                if (datas != null) {
                    var result = "";
                    if (datas.hasOwnProperty("error_result")) {
                        result += datas.error_result;
                    } else if (datas.hasOwnProperty("error_proposed")) {
                        result += "\n" + datas.error_proposed;
                    } else if (datas.hasOwnProperty("error_current")) {
                        result += "\n" + datas.error_current;
                    } else if (datas.hasOwnProperty("result")) {
                        result += "\n" + datas.result;
                    }
                    if (result.length > 0) {
                        topicBalanceResult.setValue(result);
                    }
                }
            }
        });
    }

    function generate(topics, type) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/balance/generate/?topics=' + topics + '&type=' + type,
            success: function (datas) {
                if (datas != null) {
                    var result = "";
                    if (datas.hasOwnProperty("error_result")) {
                        result += datas.error_result;
                    } else if (datas.hasOwnProperty("error_proposed")) {
                        result += "\n" + datas.error_proposed;
                    } else if (datas.hasOwnProperty("error_current")) {
                        result += "\n" + datas.error_current;
                    }
                    if (result.length > 0) {
                        topicBalanceResult.setValue(result);
                        document.getElementById("ke_balancer_execute").style.display = "none";
                        document.getElementById("ke_balancer_verify").style.display = "none";
                    }
                    if (datas.hasOwnProperty("proposed")) {
                        topicBalanceProposed.setValue(JSON.stringify(JSON.parse(datas.proposed), null, 2));
                    }
                    if (datas.hasOwnProperty("current")) {
                        topicBalanceCurrent.setValue(JSON.stringify(JSON.parse(datas.current), null, 2));
                    }
                    if (datas.hasOwnProperty("proposed_status") && datas.hasOwnProperty("current_status")) {
                        if (datas.proposed_status && datas.current_status) {
                            document.getElementById("ke_balancer_execute").style.display = "";
                            document.getElementById("ke_balancer_verify").style.display = "";
                        }
                    }
                }
            }
        });
    }

});