var url = window.location.href;
var gid;

try {
    gid = url.split("offset/")[1].trim();
} catch (e) {
    console.log(e);
}

var topicTable = $("#efak_consumer_group_offset_tbl").DataTable({
    "searching": false,
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/consumer/offset/table/ajax?id=" + gid,
    "aoColumns": [{
        "mData": 'groupId'
    }, {
        "mData": 'topicName'
    }, {
        "mData": 'partitionId'
    }, {
        "mData": 'logsize'
    }, {
        "mData": 'offset'
    }, {
        "mData": 'lag'
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

// plugins by daterangepicker
try {

    var start = moment();
    var end = moment();

    function cb(start, end) {
        $('#efak_offsets_chart_date span').html(start.format('YYYY-MM-DD') + ' 至 ' + end.format('YYYY-MM-DD'));
    }

    // daterangepicker
    var reportrange = $('#efak_offsets_chart_date').daterangepicker({
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

    chartMsg(stime, etime);

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("至")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("至")[1].trim();
        chartMsg(stime, etime);
    });
    setInterval(function () {
        chartMsg(stime, etime)
    }, 1000 * 60 * 1); // 1min
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

var efak_offsets_lag_chart = new ApexCharts(document.querySelector("#efak_offsets_lag_chart"), lineChartOptions);
efak_offsets_lag_chart.render();

var efak_offsets_consumer_chart = new ApexCharts(document.querySelector("#efak_offsets_consumer_chart"), lineChartOptions);
efak_offsets_consumer_chart.render();

var efak_offsets_producer_chart = new ApexCharts(document.querySelector("#efak_offsets_producer_chart"), lineChartOptions);
efak_offsets_producer_chart.render();

// Chart - END

function chartMsg(stime, etime) {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/consumer/offsets/realtime/chart/ajax?stime=' + stime + '&etime=' + etime + '&id=' + gid,
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                setChartData(efak_offsets_lag_chart, datas.lags);
                setChartData(efak_offsets_consumer_chart, datas.consumers);
                setChartData(efak_offsets_producer_chart, datas.producers);
                datas = null;
            }
        }
    });
}

// set chart data
function setChartData(mbean, data) {
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
