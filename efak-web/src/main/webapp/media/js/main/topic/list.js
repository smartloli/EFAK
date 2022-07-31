$(document).ready(function () {

    // part1: produce and capacity
    try {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/list/total/jmx/ajax',
            success: function (datas) {
                if (datas != null) {
                    $("#efak_topic_producer_number").text(datas.producerSize + " (APP)");
                    $("#efak_topic_producer_total_capacity").text(datas.topicCapacity + " (" + datas.capacityType + ")");
                }
            }
        });
    } catch (e) {
        console.log(e.message);
    }

    // part2: topic table list
    $("#efak_topic_table_result").dataTable({
        // "searching" : false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/topic/list/table/ajax",
        "aoColumns": [{
            "mData": 'id'
        }, {
            "mData": 'topic'
        }, {
            "mData": 'partitions'
        }, {
            "mData": 'brokerSpread'
        }, {
            "mData": 'brokerSkewed'
        }, {
            "mData": 'brokerLeaderSkewed'
        }, {
            "mData": 'created'
        }, {
            "mData": 'modify'
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

    // part3: topic admin operate
    var topic = "";
    // delete topic
    $(document).on('click', 'a[name=efak_topic_remove]', function () {
        var href = $(this).attr("href");
        topic = href.split("#")[1];
        var token = $("#ke_admin_token").val();
        $("#remove_div").html("");
        $("#remove_div").append("<a id='ke_del_topic' href='#' class='btn btn-danger'>Delete</a>");
        $('#ke_topic_delete').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#ke_topic_delete').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });

        if (token.length == 0) {
            $("#ke_del_topic").attr("disabled", true);
            $("#ke_del_topic").attr("href", "#");
        }
    });

    // add partition
    $(document).on('click', 'a[name=efak_topic_modify]', function () {
        var href = $(this).attr("href");
        topic = href.split("#")[1];
        var partitions = $("#ke_modify_topic_partition").val();
        $("#ke_topic_submit_div").html("");
        $("#ke_topic_submit_div").append("<a id='ke_modify_topic_btn' href='#' class='btn btn-primary'>Modify</a>");
        $('#ke_topic_modify').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#ke_topic_modify').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });

        if (partitions.length == 0) {
            $("#ke_modify_topic_btn").attr("disabled", true);
            $("#ke_modify_topic_btn").attr("href", "#");
        }
    });

    // truncate topic
    $(document).on('click', 'a[name=efak_topic_clean]', function () {
        var href = $(this).attr("href");
        var topic = href.split("#")[1];
        $("#ke_topic_clean_content").html("");
        $("#ke_topic_clean_content").append("<div class='alert border-0 bg-light-warning alert-dismissable'><div class='text-warning'>Are you sure to truncate the data of topic [ <strong>" + topic + "</strong> ] ?</div></div>");
        $("#ke_topic_clean_data_div").html("");
        $("#ke_topic_clean_data_div").append("<a id='ke_del_topic' href='/topic/clean/data/" + topic + "/' class='btn btn-warning'>Submit</a>");
        $('#ke_topic_clean').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#ke_topic_clean').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });
    });

    $("#ke_admin_token").on('input', function (e) {
        var token = $("#ke_admin_token").val();
        if (token.length == 0) {
            $("#ke_del_topic").attr("disabled", true);
            $("#ke_del_topic").attr("href", "#");
        } else {
            $("#ke_del_topic").attr("disabled", false);
            $("#ke_del_topic").attr("href", "/topic/" + topic + "/" + token + "/delete");
        }
    });


    $("#ke_modify_topic_partition").on('input', function (e) {
        var partitions = $("#ke_modify_topic_partition").val();
        var reg = /^[1-9]\d*$/;
        if (!reg.test(partitions)) {
            $("#ke_modify_topic_btn").addClass("disabled");
            $("#ke_modify_topic_btn").attr("href", "#");
        } else {
            $("#ke_modify_topic_btn").removeClass("disabled");
            $("#ke_modify_topic_btn").attr("href", "/topic/" + topic + "/" + partitions + "/modify");
        }
    });

    $("#select2val").select2({
        placeholder: "Topic",
        theme: 'bootstrap4',
        width: $(this).data('width') ? $(this).data('width') : $(this).hasClass('w-100') ? '100%' : 'style',
        allowClear: true,
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
            minimumInputLength: 0
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
        $("#ke_topic_aggrate").val(arrs);
    });

    // Add daterangepicker
    try {

        var start = moment().subtract(6, 'days');
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
            producerSelect();
        });
    } catch (e) {
        console.log(e.message);
    }

    // topic produce logsize common options
    var efakTopicLogSizeOptions = {
        series: [{
            name: "",
            data: []
        }],
        chart: {
            foreColor: '#9a9797',
            type: "bar",
            //width: 130,
            stacked: true,
            height: 280,
            toolbar: {
                show: !1
            },
            zoom: {
                enabled: !1
            },
            dropShadow: {
                enabled: 0,
                top: 3,
                left: 15,
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
        plotOptions: {
            bar: {
                // horizontal: !1,
                columnWidth: "35%",
                //endingShape: "rounded"
            }
        },
        dataLabels: {
            enabled: !1
        },
        legend: {
            show: false,
        },
        stroke: {
            show: !0,
            width: 0,
            curve: "smooth"
        },
        colors: ["#3461ff"],
        xaxis: {
            // type: 'datetime',
            labels: {
                datetimeUTC: false,
                format: "yyyy-MM-dd"
            },
            categories: []
        },
        yaxis: [{
            labels: {
                // fixed number less than 10
                formatter: function (val) {
                    if (window.isNaN(val) || Math.floor(val) != val) {
                        return val;
                    }
                    try {
                        return val.toFixed(0);
                    } catch (e) {
                        return val;
                    }
                }
            }
        }],
        tooltip: {
            theme: "dark",
            x: {
                format: "yyyy-MM-dd"
            }
        }
    };

    // topic produce logsize
    var efak_topic_producer_logsize_chart = new ApexCharts(document.querySelector("#efak_topic_producer_logsize_chart"), efakTopicLogSizeOptions);
    efak_topic_producer_logsize_chart.render();

    function producerMsg(stime, etime) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/list/filter/select/ajax?stime=' + stime + '&etime=' + etime,
            beforeSend: function (xmlHttp) {
                xmlHttp.setRequestHeader("If-Modified-Since", "0");
                xmlHttp.setRequestHeader("Cache-Control", "no-cache");
            },
            success: function (datas) {
                if (datas != null) {
                    setProducerBarData(efak_topic_producer_logsize_chart, datas);
                    datas = null;
                }
            }
        });
    }

    // set trend data
    function setProducerBarData(mbean, data) {
        efakTopicLogSizeOptions.xaxis.categories = filter(data).x;
        efakTopicLogSizeOptions.series[0].data = filter(data).y;
        efakTopicLogSizeOptions.series[0].name = filter(data).name;
        mbean.updateOptions(efakTopicLogSizeOptions);
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

    $("#ke_topic_select_query").on('click', function () {
        producerSelect();
    });

    // auto refresh
    try {
        setInterval(function () {
            producerSelect()
        }, 1000 * 60 * 5);
    } catch (e) {
        console.log(e.message);
    }

    function producerSelect() {
        var topics = new Array();
        var topics = $("#ke_topic_aggrate").val().split(",");
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/list/filter/select/ajax?stime=' + stime + '&etime=' + etime + '&topics=' + topics,
            success: function (datas) {
                if (datas != null) {
                    setProducerBarData(efak_topic_producer_logsize_chart, datas);
                    datas = null;
                }
            }
        });
    }

});