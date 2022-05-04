$(document).ready(function () {
    var url = window.location.href;
    var topicName = url.split("meta/")[1].split("/")[0];

    // add preview
    var previewMsg;
    try {
        var mime = 'text/x-mariadb';
        // get mime type
        if (window.location.href.indexOf('mime=') > -1) {
            mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
        }
        previewMsg = CodeMirror.fromTextArea(document.getElementById('ke_tp_preview_message'), {
            mode: mime,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: false,
            matchBrackets: true,
            autofocus: true,
            readOnly: true
        });
    } catch (e) {
        console.info(e.message);
    }

    // topic meta
    $("#result").dataTable({
        "searching": false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/topic/meta/" + topicName + "/ajax",
        "aoColumns": [{
            "mData": 'topic'
        }, {
            "mData": 'partition'
        }, {
            "mData": 'logsize'
        }, {
            "mData": 'leader'
        }, {
            "mData": 'replicas'
        }, {
            "mData": 'isr'
        }, {
            "mData": 'preferred_leader'
        }, {
            "mData": 'under_replicated'
        }, {
            "mData": 'preview'
        }]
    });

    // topic consumer group
    $("#topic_consumer_tab_result").dataTable({
        // "searching": false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/topic/consumer/group/" + topicName + "/ajax",
        "aoColumns": [{
            "mData": 'group'
        }, {
            "mData": 'topic'
        }, {
            "mData": 'lag'
        }, {
            "mData": 'status'
        }, {
            "mData": 'operate'
        }]
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

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/meta/mbean/' + topicName + '/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#topic_metrics_tab").html("")
                var thead = "<thead><tr><th>Rate</th><th>Mean</th><th>1 Minute</th><th>5 Minute</th><th>15 Minute</th></tr></thead>";
                $("#topic_metrics_tab").append(thead);
                var tbody = "<tbody>";
                var msg = topicMetricData('Messages in /sec', datas.msg);

                msg += topicMetricData('Bytes in /sec', datas.ins);
                msg += topicMetricData('Bytes out /sec', datas.out);
                msg += topicMetricData('Bytes rejected /sec', datas.rejected);
                msg += topicMetricData('Failed fetch request /sec', datas.fetch);
                msg += topicMetricData('Failed produce request /sec', datas.produce);
                msg += topicMetricData('Total fetch requests /sec', datas.total_fetch_requests);
                msg += topicMetricData('Total produce requests /sec', datas.total_produce_requests);
                msg += topicMetricData('Produce message conversions /sec', datas.produce_message_conversions);

                tbody += msg + "</tbody>"
                $("#topic_metrics_tab").append(tbody);
            }
        }
    });

    function topicMetricData(field, data) {
        var tr = '';
        if (data == null || data == undefined) {
            return tr;
        }

        if (field.toUpperCase().indexOf("BYTE") > -1) {
            tr += "<tr><td>" + field + "</td><td><span class='badge badge-secondary'>" + data.meanRate + "</span></td><td><span class='badge badge-secondary'>" + data.oneMinute + "</span></td><td><span class='badge badge-secondary'>" + data.fiveMinute + "</span></td><td><span class='badge badge-secondary'>" + data.fifteenMinute + "</span></td></tr>";
        } else {
            tr += "<tr><td>" + field + "</td><td><span class='badge badge-secondary'>" + data.meanRate.split("B")[0] + "</span></td><td><span class='badge badge-secondary'>" + data.oneMinute.split("B")[0] + "</span></td><td><span class='badge badge-secondary'>" + data.fiveMinute.split("B")[0] + "</span></td><td><span class='badge badge-secondary'>" + data.fifteenMinute.split("B")[0] + "</span></td></tr>";
        }

        return tr;
    }

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/meta/jmx/' + topicName + '/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#producer_logsize").text(datas.logsize);
                $("#producer_topicsize").text(datas.topicsize + " (" + datas.sizetype + ")");
            }
        }
    });

    // Init chart common
    try {
        chartCommonOption = {
            backgroundColor: "#fff",
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                }
            },
            legend: {
                data: []
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: []
            },
            dataZoom: {
                show: true,
                start: 30
            },
            grid: {
                bottom: "70px",
                left: "90px",
                right: "90px"
            },
            yAxis: {
                type: 'value'
            },
            series: {
                type: 'line',
                symbol: "none",
                // name : "",
                smooth: true,
                areaStyle: {
                    opacity: 0.1
                },
                data: []
            }
        };
    } catch (e) {
        console.log(e.message);
    }

    // Add data control
    try {

        var start = moment();
        var end = moment();

        function cb(start, end) {
            $('#reportrange span').html(start.format('YYYY-MM-DD') + ' To ' + end.format('YYYY-MM-DD'));
        }

        var reportrange = $('#reportrange').daterangepicker({
            startDate: start,
            endDate: end,
            ranges: {
                'Today': [moment(), moment()],
                'Yesterday': [moment().subtract(1, 'days'), moment()],
                'Lastest 3 days': [moment().subtract(3, 'days'), moment()],
                'Lastest 7 days': [moment().subtract(6, 'days'), moment()]
            }
        }, cb);

        cb(start, end);
        var stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
        var etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();

        producerMsg(stime, etime);

        reportrange.on('apply.daterangepicker', function (ev, picker) {
            stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
            etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
            producerMsg(stime, etime);
        });
        setInterval(function () {
            producerMsg(stime, etime)
        }, 1000 * 60 * 5);
    } catch (e) {
        console.log(e.message);
    }

    try {
        function morrisLineInit(elment) {
            lagChart = echarts.init(document.getElementById(elment), 'macarons');
            lagChart.setOption(chartCommonOption);
            return lagChart;
        }
    } catch (e) {
        console.log(e.message);
    }

    var topic_producer_msg = morrisLineInit('topic_producer_msg');

    $("#topic_producer_msg").resize(function () {
        var opt_topic_producer_msg = topic_producer_msg.getOption();
        topic_producer_msg.clear();
        topic_producer_msg.resize({width: $("#topic_producer_msg").css('width')});
        topic_producer_msg.setOption(opt_topic_producer_msg);
    });

    function producerMsg(stime, etime) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/producer/chart/ajax?stime=' + stime + '&etime=' + etime + '&topic=' + topicName,
            beforeSend: function (xmlHttp) {
                xmlHttp.setRequestHeader("If-Modified-Since", "0");
                xmlHttp.setRequestHeader("Cache-Control", "no-cache");
            },
            success: function (datas) {
                if (datas != null) {
                    setProducerChartData(topic_producer_msg, datas);
                    datas = null;
                }
            }
        });
    }

    // set trend data
    function setProducerChartData(mbean, data) {
        chartCommonOption.xAxis.data = filter(data).x;
        chartCommonOption.series.data = filter(data).y;
        mbean.setOption(chartCommonOption);
    }

    // filter data
    function filter(datas) {
        var data = new Object();
        var datax = new Array();
        var datay = new Array();
        for (var i = 0; i < datas.length; i++) {
            datax.push(datas[i].x);
            datay.push(datas[i].y);
        }
        data.x = datax;
        data.y = datay;
        return data;
    }

    $(document).on('click', 'a[name=preview]', function () {
        var topic = $(this).attr("topic");
        var partition = $(this).attr("partition");
        $('#ke_topic_preview').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#ke_topic_preview').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });

        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/topic/meta/preview/msg/ajax?topic=' + topic + '&partition=' + partition,
                success: function (datas) {
                    if (datas != null) {
                        previewMsg.setValue(JSON.stringify(datas, null, 2));
                    }
                }
            });
        } catch (e) {
            console.log(e.message)
        }
    });

    // reset offset result
    var topicResetOffsets = CodeMirror.fromTextArea(document.getElementById('ke_reset_offset_result'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: false,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    // reset offsets
    $(document).on('click', 'a[name=topic_reset_offsets]', function () {
        $("#ke_reset_offset_value").hide();
        $('#ke_reset_offsets').modal('show');
        var group = $(this).attr("group");
        var topic = $(this).attr("topic");
        $("#select2val").select2({
            placeholder: "Reset Type",
            ajax: {
                url: "/topic/reset/offset/type/list/ajax",
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

        var text = "";
        $('#select2val').on('select2:select', function (evt) {
            text = evt.params.data.text;
            $("#select2val").val(text);
            if (text.indexOf("--to-earliest") > -1 || text.indexOf("--to-latest") > -1 || text.indexOf("--to-current") > -1) {
                $("#ke_reset_offset_value").hide();
            } else {
                $("#ke_reset_offset_value").show();
            }
        });

        // get reset offset result
        $("#ke_reset_offset_btn").on('click', function () {
            if (text.indexOf("--to-earliest") > -1 || text.indexOf("--to-latest") > -1 || text.indexOf("--to-current") > -1) {
                var json = {"group": group, "topic": topic, "cmd": text};
                execute(JSON.stringify(json));
            } else {
                var json = {"group": group, "topic": topic, "cmd": text, "value": $("#ke_reset_offset_val").val()};
                execute(JSON.stringify(json));
            }
        });
    });

    function execute(json) {
        console.log(json)
        $.ajax({
            type: 'post',
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify({
                "json": json
            }),
            url: '/topic/reset/offsets/execute/result/ajax',
            success: function (datas) {
                if (datas != null) {
                    console.log(datas)
                    if (datas.hasOwnProperty("success") && datas.success) {
                        topicResetOffsets.setValue(datas.result);
                    }
                    if (datas.hasOwnProperty("error")) {
                        topicResetOffsets.setValue(datas.error);
                    }
                }
            }
        });
    }
});