

// plugins by daterangepicker
var start;
var end;
try {

    start = moment();
    end = moment();

    function cb(start, end) {
        $('#efak_kafka_mbean_chart_date span').html(start.format('YYYY-MM-DD') + ' 至 ' + end.format('YYYY-MM-DD'));
    }

    // daterangepicker
    var reportrange = $('#efak_kafka_mbean_chart_date').daterangepicker({
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

    producerMsg(stime, etime, topicName);

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("至")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("至")[1].trim();
        producerMsg(stime, etime);
    });
    setInterval(function () {
        producerMsg(stime, etime, topicName)
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
            text: '量级 ( 条 )',
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

var efak_kafka_message_in_chart = new ApexCharts(document.querySelector("#efak_kafka_message_in_chart"), lineChartOptions);
efak_kafka_message_in_chart.render();

// Chart - END

function producerMsg(stime, etime) {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/monitor/produce/msg/chart/ajax?stime=' + stime + '&etime=' + etime,
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                console.log(datas);
                setProducerChartData(efak_kafka_message_in_chart, datas);
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