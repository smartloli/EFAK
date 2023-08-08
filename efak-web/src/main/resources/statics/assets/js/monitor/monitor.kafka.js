
// defined byte size
var KB_IN_BYTES = 1024;
var MB_IN_BYTES = 1024 * KB_IN_BYTES;
var GB_IN_BYTES = 1024 * MB_IN_BYTES;
var TB_IN_BYTES = 1024 * GB_IN_BYTES;

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

    mbeanRealtime(stime, etime, getCheckedModules());

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("至")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("至")[1].trim();
        mbeanRealtime(stime, etime, getCheckedModules());
    });
    setInterval(function () {
        mbeanRealtime(stime, etime, getCheckedModules());
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
    legend: {
        show: false
    }
};

initModuleVisualAndBindEvent();

var modules = getCheckedModules();

var mbean_msg_in = chartLineInit('mbean_msg_in');
var mbean_msg_byte_in = chartLineInit('mbean_msg_byte_in');
var mbean_msg_byte_out = chartLineInit('mbean_msg_byte_out');
var mbean_byte_rejected = chartLineInit('mbean_byte_rejected');
var mbean_failed_fetch_request = chartLineInit('mbean_failed_fetch_request');
var mbean_failed_produce_request = chartLineInit('mbean_failed_produce_request');
var mbean_produce_message_conversions = chartLineInit('mbean_produce_message_conversions');
var mbean_total_fetch_requests = chartLineInit('mbean_total_fetch_requests');
var mbean_total_produce_requests = chartLineInit('mbean_total_produce_requests');
var mbean_replication_bytes_out = chartLineInit('mbean_replication_bytes_out');
var mbean_replication_bytes_in = chartLineInit('mbean_replication_bytes_in');
var mbean_os_free_memory = chartLineInit('mbean_os_free_memory');

function mbeanRealtime(stime, etime, modules) {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/monitor/kafka/mbean/chart/ajax?stime=' + stime + '&etime=' + etime + '&modules=' + modules,
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                setTrendData(mbean_msg_in, 'message_in', datas);
                setTrendData(mbean_msg_byte_in, 'byte_in', datas);
                setTrendData(mbean_msg_byte_out, 'byte_out', datas);
                setTrendData(mbean_byte_rejected, 'byte_rejected', datas);
                setTrendData(mbean_failed_fetch_request, 'failed_fetch_request', datas);
                setTrendData(mbean_failed_produce_request, 'failed_produce_request', datas);
                setTrendData(mbean_produce_message_conversions, 'produce_message_conversions', datas);
                setTrendData(mbean_total_fetch_requests, 'total_fetch_requests', datas);
                setTrendData(mbean_total_produce_requests, 'total_produce_requests', datas);
                setTrendData(mbean_replication_bytes_out, 'replication_bytes_out', datas);
                setTrendData(mbean_replication_bytes_in, 'replication_bytes_in', datas);
                setTrendData(mbean_os_free_memory, 'os_free_memory', datas);
                datas = null;
            }
        }
    });
}

// Chart init
function chartLineInit(elment) {
    var efakMetricsChart = new ApexCharts(document.querySelector("#" + elment), lineChartOptions);
    efakMetricsChart.render();
    return efakMetricsChart;
}

// module show or hide
function module(id, display) {
    if (display) {
        $(id).css('display', 'block');
    } else {
        $(id).css('display', 'none');
    }
}

// choise module
function getCheckedModules() {
    var modules = '';
    $('#efak_chk_top').find('input[type="checkbox"]:checked').each(function () {
        modules += ($(this).attr('name')) + ',';
    });
    return modules.substring(0, modules.length - 1);
}

// init module show or hide & bind change event
function initModuleVisualAndBindEvent() {
    $('#efak_chk_top').find('input[type="checkbox"]').each(function () {
        var that = this;
        if ($(that).is(':checked')) {
            module('#' + $(that).attr('name'), true);
        } else {
            module('#' + $(that).attr('name'), false);
        }
        $(that).click(function () {
            if ($(that).is(':checked')) {
                module('#' + $(that).attr('name'), true);
                stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
                etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
                mbeanRealtime(stime, etime, getCheckedModules());
                return;
            }
            module('#' + $(that).attr('name'), false);
        });
    });
}

// set trend data
function setTrendData(mbean, filed, data) {
    lineChartOptions.xaxis.categories = filter(data, filed).x;
    lineChartOptions.series[0].data = filter(data, filed).y;
    lineChartOptions.series[0].name = filter(data, filed).name;
    lineChartOptions.legend.data = [filter(data, filed).name];
    mbean.updateOptions(lineChartOptions);
}

// filter data
function filter(datas, type) {
    var data = new Object();
    var datax = new Array();
    var datay = new Array();
    switch (type) {
        case "message_in":
            for (var i = 0; i < datas.messageIns.length; i++) {
                datax.push(datas.messageIns[i].x);
                datay.push((datas.messageIns[i].y * 60).toFixed(2));
            }
            data.name = "MessagesInPerSec (msg/min)";
            break;
        case "byte_in":
            var cunit = "";
            for (var i = 0; i < datas.byteIns.length; i++) {
                datax.push(datas.byteIns[i].x);
                var value = stringify(datas.byteIns[i].y).value;
                cunit = stringify(datas.byteIns[i].y).type;
                datay.push(value);
            }
            data.name = "BytesInPerSec" + cunit;
            break;
        case "byte_out":
            var cunit = "";
            for (var i = 0; i < datas.byteOuts.length; i++) {
                datax.push(datas.byteOuts[i].x);
                var value = stringify(datas.byteOuts[i].y).value;
                cunit = stringify(datas.byteOuts[i].y).type;
                datay.push(value);
            }
            data.name = "BytesOutPerSec" + cunit;
            break;
        case "byte_rejected":
            var cunit = "";
            for (var i = 0; i < datas.byteRejected.length; i++) {
                datax.push(datas.byteRejected[i].x);
                var value = stringify(datas.byteRejected[i].y).value;
                cunit = stringify(datas.byteRejected[i].y).type;
                datay.push(value);
            }
            data.name = "BytesRejectedPerSec" + cunit;
            break;
        case "failed_fetch_request":
            for (var i = 0; i < datas.failedFetchRequest.length; i++) {
                datax.push(datas.failedFetchRequest[i].x);
                datay.push((datas.failedFetchRequest[i].y * 60).toFixed(2));
            }
            data.name = "FailedFetchRequestsPerSec (msg/min)";
            break;
        case "failed_produce_request":
            for (var i = 0; i < datas.failedProduceRequest.length; i++) {
                datax.push(datas.failedProduceRequest[i].x);
                datay.push((datas.failedProduceRequest[i].y * 60).toFixed(2));
            }
            data.name = "FailedProduceRequestsPerSec (msg/min)";
            break;
        case "produce_message_conversions":
            for (var i = 0; i < datas.produceMessageConversions.length; i++) {
                datax.push(datas.produceMessageConversions[i].x);
                datay.push((datas.produceMessageConversions[i].y * 60).toFixed(2));
            }
            data.name = "ProduceMessageConversionsPerSec (msg/min)";
            break;
        case "total_fetch_requests":
            for (var i = 0; i < datas.totalFetchRequests.length; i++) {
                datax.push(datas.totalFetchRequests[i].x);
                datay.push((datas.totalFetchRequests[i].y * 60).toFixed(2));
            }
            data.name = "TotalFetchRequestsPerSec (msg/min)";
            break;
        case "total_produce_requests":
            for (var i = 0; i < datas.totalProduceRequests.length; i++) {
                datax.push(datas.totalProduceRequests[i].x);
                datay.push((datas.totalProduceRequests[i].y * 60).toFixed(2));
            }
            data.name = "TotalProduceRequestsPerSec (msg/min)";
            break;
        case "replication_bytes_out":
            var cunit = "";
            for (var i = 0; i < datas.replicationBytesOuts.length; i++) {
                datax.push(datas.replicationBytesOuts[i].x);
                var value = stringify(datas.replicationBytesOuts[i].y).value;
                cunit = stringify(datas.replicationBytesOuts[i].y).type;
                datay.push(value);
            }
            data.name = "ReplicationBytesOutPerSec" + cunit;
            break;
        case "replication_bytes_in":
            var cunit = "";
            for (var i = 0; i < datas.replicationBytesIns.length; i++) {
                datax.push(datas.replicationBytesIns[i].x);
                var value = stringify(datas.replicationBytesIns[i].y).value;
                cunit = stringify(datas.replicationBytesIns[i].y).type;
                datay.push(value);
            }
            data.name = "ReplicationBytesInPerSec" + cunit;
            break;
        case "os_free_memory":
            for (var i = 0; i < datas.osFreeMems.length; i++) {
                datax.push(datas.osFreeMems[i].x);
                var value = (datas.osFreeMems[i].y * 1.0 / GB_IN_BYTES).toFixed(2);
                datay.push(value);
            }
            data.name = "OSFreeMemory (GB/min)";
            break;
        default:
            break;
    }
    data.x = datax;
    data.y = datay;
    return data;
}

// formatter byte to kb,mb or gb etc.
function stringify(byteNumber) {
    var object = new Object();
    if (byteNumber / TB_IN_BYTES > 1) {
        object.value = (byteNumber / TB_IN_BYTES).toFixed(2);
        object.type = " (TB/sec)";
        return object;
    } else if (byteNumber / GB_IN_BYTES > 1) {
        object.value = (byteNumber / GB_IN_BYTES).toFixed(2);
        object.type = " (GB/sec)";
        return object;
    } else if (byteNumber / MB_IN_BYTES > 1) {
        object.value = (byteNumber / MB_IN_BYTES).toFixed(2);
        object.type = " (MB/sec)";
        return object;
    } else if (byteNumber / KB_IN_BYTES > 1) {
        object.value = (byteNumber / KB_IN_BYTES).toFixed(2);
        object.type = " (KB/sec)";
        return object;
    } else {
        object.value = (byteNumber / 1).toFixed(2);
        object.type = " (B/sec)";
        return object;
    }
}