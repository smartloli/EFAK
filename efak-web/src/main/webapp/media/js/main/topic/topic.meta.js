$(document).ready(function () {
    var url = window.location.href;
    var topicName = url.split("meta/")[1].split("/")[0];

    // topic meta
    $("#efak_topic_meta_table_result").dataTable({
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
    $("#efak_topic_consumer_tab_result").dataTable({
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
            tr += "<tr><td>" + field + "</td><td><span class='badge bg-secondary'>" + data.meanRate + "</span></td><td><span class='badge bg-secondary'>" + data.oneMinute + "</span></td><td><span class='badge bg-secondary'>" + data.fiveMinute + "</span></td><td><span class='badge bg-secondary'>" + data.fifteenMinute + "</span></td></tr>";
        } else {
            tr += "<tr><td>" + field + "</td><td><span class='badge bg-secondary'>" + data.meanRate.split("B")[0] + "</span></td><td><span class='badge bg-secondary'>" + data.oneMinute.split("B")[0] + "</span></td><td><span class='badge bg-secondary'>" + data.fiveMinute.split("B")[0] + "</span></td><td><span class='badge bg-secondary'>" + data.fifteenMinute.split("B")[0] + "</span></td></tr>";
        }

        return tr;
    }

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/meta/jmx/' + topicName + '/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#efak_topic_producer_logsize").text(datas.logsize);
                $("#efak_topic_producer_capacity").text(datas.topicsize + " (" + datas.sizetype + ")");
            }
        }
    });

    // add daterangepicker
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

    // topic msg chart
    var chartCommonOption = {
        series: [{
            name: '',
            data: []
        }],
        chart: {
            type: "area",
            // width: 130,
            stacked: true,
            height: 280,
            toolbar: {
                show: true,
                tools: {
                    download: false,
                    selection: true,
                    zoom: true,
                    zoomin: true,
                    zoomout: true,
                    pan: true,
                    reset: true
                }
            },
            zoom: {
                enabled: true
            },
            dropShadow: {
                enabled: 0,
                top: 3,
                left: 14,
                blur: 4,
                opacity: .12,
                color: "#3461ff"
            },
            sparkline: {
                enabled: !1
            }
        },
        markers: {
            size: 0,
            colors: ["#3461ff"],
            strokeColors: "#fff",
            strokeWidth: 2,
            hover: {
                size: 7
            }
        },
        grid: {
            row: {
                colors: ["transparent", "transparent"],
                opacity: .2
            },
            borderColor: "#f1f1f1"
        },
        plotOptions: {
            bar: {
                horizontal: !1,
                columnWidth: "25%",
                //endingShape: "rounded"
            }
        },
        dataLabels: {
            enabled: !1
        },
        stroke: {
            show: !0,
            width: [2.5],
            //colors: ["#3461ff"],
            curve: "smooth"
        },
        fill: {
            type: 'gradient',
            gradient: {
                shade: 'light',
                type: 'vertical',
                shadeIntensity: 0.5,
                gradientToColors: ['#3461ff'],
                inverseColors: false,
                opacityFrom: 0.5,
                opacityTo: 0.1,
                // stops: [0, 100]
            }
        },
        colors: ["#3461ff"],
        xaxis: {
            type: 'datetime',
            labels: {
                datetimeUTC: false,
            },
            categories: []
        },
        responsive: [
            {
                breakpoint: 1000,
                options: {
                    chart: {
                        type: "area",
                        // width: 130,
                        stacked: true,
                    }
                }
            }
        ],
        legend: {
            show: false
        },
        tooltip: {
            theme: "dark",
            x: {
                format: 'yyyy-MM-dd HH:mm'
            }
        }
    };

    var efak_topic_producer_msg = new ApexCharts(document.querySelector("#efak_topic_producer_msg"), chartCommonOption);
    efak_topic_producer_msg.render();

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
                    setProducerChartData(efak_topic_producer_msg, datas);
                    datas = null;
                }
            }
        });
    }

    // set trend data
    function setProducerChartData(mbean, data) {
        chartCommonOption.xaxis.categories = filter(data).x;
        chartCommonOption.series[0].data = filter(data).y;
        chartCommonOption.series[0].name = filter(data).name;
        mbean.updateOptions(chartCommonOption);
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

    $(document).on('click', 'a[name=efak_topic_preview]', function () {
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
                        $("#ke_tp_preview_message").text(JSON.stringify(datas, null, 2));
                    }
                }
            });
        } catch (e) {
            console.log(e.message)
        }
    });

    // reset offsets
    $(document).on('click', 'a[name=topic_reset_offsets]', function () {
        $("#ke_reset_offset_value").hide();
        $('#ke_reset_offsets').modal('show');
        var group = $(this).attr("group");
        var topic = $(this).attr("topic");
        $("#select2val").select2({
            placeholder: "Reset Type",
            theme: 'bootstrap4',
            width: $(this).data('width') ? $(this).data('width') : $(this).hasClass('w-100') ? '100%' : 'style',
            allowClear: true,
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
                    if (datas.hasOwnProperty("success") && datas.success) {
                        $("#topicResetOffsets").text(datas.result);
                    }
                    if (datas.hasOwnProperty("error")) {
                        $("#topicResetOffsets").text(datas.error);
                    }
                }
            }
        });
    }
});