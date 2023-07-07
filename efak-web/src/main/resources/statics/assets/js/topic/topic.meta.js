var url = window.location.href;
var topicName;

try {
    topicName = url.split("meta/")[1].trim();
} catch (e) {
    console.log(e);
}

var topicTable = $("#efak_topic_meta_tbl").DataTable({
    "searching": false,
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/topic/meta/table/ajax?topic=" + topicName,
    "aoColumns": [{
        "mData": 'partitionId'
    }, {
        "mData": 'logsize'
    }, {
        "mData": 'leader'
    }, {
        "mData": 'replicas'
    }, {
        "mData": 'isr'
    }, {
        "mData": 'preferredLeader'
    }, {
        "mData": 'underReplicated'
    }, {
        "mData": 'preview'
    }],
    language: {
        "sProcessing": "处理中...",
        "sLengthMenu": "显示 _MENU_ 项结果",
        "sZeroRecords": "没有匹配结果",
        "sInfo": "显示第 _START_ 至 _END_ 项结果，共 _TOTAL_ 项",
        "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
        "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
        "sInfoPostFix": "",
        "sSearch": "搜索:",
        "sUrl": "",
        "sEmptyTable": "表中数据为空",
        "sLoadingRecords": "载入中...",
        "sInfoThousands": ",",
        "oPaginate": {
            "sFirst": "首页",
            "sPrevious": "上页",
            "sNext": "下页",
            "sLast": "末页"
        },
        "oAria": {
            "sSortAscending": ": 以升序排列此列",
            "sSortDescending": ": 以降序排列此列"
        }
    }
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

setInterval(function () {
    topicTable.ajax.reload();
}, 60000); // 1 min

try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/record/size/ajax?topic=' + topicName,
        success: function (datas) {
            if (datas.logsize >= 0 && datas.logsize < 10000000) {
                $("#efka_meta_panel1").addClass("col-md-4 grid-margin stretch-card");
                $("#efka_meta_panel2").addClass("col-md-4 grid-margin stretch-card");
                $("#efka_meta_panel3").addClass("col-md-4 grid-margin stretch-card");
            } else if (datas.logsize >= 10000000 && datas.logsize < 10000000000) {
                $("#efka_meta_panel1").addClass("col-md-5 grid-margin stretch-card");
                $("#efka_meta_panel2").addClass("col-md-2 grid-margin stretch-card");
                $("#efka_meta_panel3").addClass("col-md-5 grid-margin stretch-card");
            } else if (datas.logsize > 10000000000) {
                $("#efka_meta_panel1").addClass("col-md-5 grid-margin stretch-card");
                $("#efka_meta_panel2").remove();
                $("#efka_meta_panel3").addClass("col-md-5 grid-margin stretch-card");
            }
            $("#efka_topic_meta_logsize").text(datas.logsize);
            $("#efka_topic_meta_capacity").text(datas.capacity);
            $("#efka_topic_meta_capacity_unit").text(datas.unit);
        }
    });
} catch (e) {
    console.log(e);
}

$(document).on('click', 'a[name=efak_topic_partition_preview]', function (event) {
    event.preventDefault();
    var topic = $(this).attr("topic");
    var partition = $(this).attr("partition");

    $('#efak_topic_partition_msg_preview_modal').modal('show');

    try {
        $.ajax({
            url: '/topic/name/msg/preview',
            method: 'POST',
            dataType: 'json',
            contentType: 'application/json;charset=UTF-8',
            data: JSON.stringify({
                "topicName": topic,
                "partitionId": partition
            }),
            success: function (response) {
                $("#efak_topic_name_msg_preview").text(JSON.stringify(response, null, 2));
            }
        });
    } catch (e) {
        console.log(e);
    }

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

// Color val
var colors = {
    primary: "#6571ff",
    secondary: "#7987a1",
    success: "#05a34a",
    info: "#66d1d1",
    warning: "#fbbc06",
    danger: "#ff3366",
    light: "#e9ecef",
    dark: "#060c17",
    muted: "#7987a1",
    gridBorder: "rgba(77, 138, 240, .15)",
    bodyColor: "#b8c3d9",
    cardBg: "#0c1427"
}

// Chart
if ($('#efak_topic_meta_chart_msg').length) {
    var lineChartOptions = {
        chart: {
            type: "line",
            height: '400',
            parentHeightOffset: 0,
            foreColor: colors.bodyColor,
            background: colors.cardBg,
            toolbar: {
                show: false
            },
        },
        theme: {
            mode: 'light'
        },
        tooltip: {
            theme: 'light',
            x: {
                format: 'yyyy-MM-dd HH:mm'
            }
        },
        colors: [colors.primary, colors.danger, colors.warning],
        grid: {
            padding: {
                bottom: -4,
            },
            borderColor: colors.gridBorder,
            xaxis: {
                lines: {
                    show: true
                }
            }
        },
        series: [
            {
                name: "",
                data: []
            },
        ],
        xaxis: {
            type: "datetime",
            labels: {
                datetimeUTC: false,
            },
            categories: [],
            lines: {
                show: true
            },
            axisBorder: {
                color: colors.gridBorder,
            },
            axisTicks: {
                color: colors.gridBorder,
            },
            crosshairs: {
                stroke: {
                    color: colors.secondary,
                },
            },
        },
        yaxis: {
            title: {
                text: '消息量级 ( 条 )',
                style: {
                    size: 9,
                    color: colors.muted
                }
            },
            tickAmount: 4,
            tooltip: {
                enabled: true
            },
            crosshairs: {
                stroke: {
                    color: colors.secondary,
                },
            },
        },
        markers: {
            size: 0,
        },
        stroke: {
            width: 2,
            curve: "straight",
        },
    };
    var efak_topic_producer_msg = new ApexCharts(document.querySelector("#efak_topic_meta_chart_msg"), lineChartOptions);
    efak_topic_producer_msg.render();
}

// Chart - END

function producerMsg(stime, etime) {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/topic/meta/msg/chart/ajax?stime=' + stime + '&etime=' + etime + '&topic=' + topicName,
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

// set chart data
function setProducerChartData(mbean, data) {
    lineChartOptions.xaxis.categories = filter(data).x;
    lineChartOptions.series[0].data = filter(data).y;
    lineChartOptions.series[0].name = filter(data).name;
    mbean.updateOptions(lineChartOptions);
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