var pathname = window.location.pathname;
console.log(pathname);

// load dashboard panel: node,topics,consumers
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/panel/ajax',
        success: function (datas) {
            if (JSON.stringify(datas) === '{}') {
            } else {
                $("#efak_dashboard_brokers_panel").text(datas.brokers);
                $("#efak_dashboard_brokers_onlines_panel").text("在线: " + datas.onlines);
                $("#efak_dashboard_topic_total_panel").text(datas.topic_total_nums);
                $("#efak_dashboard_topic_active_panel").text("空闲: " + datas.topic_free_nums);
                $("#efak_dashboard_group_total_panel").text(datas.group_total_nums);
                $("#efak_dashboard_group_active_panel").text("活跃: " + datas.group_active_nums);

                // topic scatter
                var scatter = ((datas.topic_total_nums - datas.topic_free_nums) * 100.0 / datas.topic_total_nums).toFixed(2);
                setTopicScatterData(efak_dashboard_topic_scatter, scatter);
                $("#efak_dashboard_topic_scatter_active").text(datas.topic_total_nums - datas.topic_free_nums);
                $("#efak_dashboard_topic_scatter_standby").text(datas.topic_free_nums);

                var mb = (datas.mb * 100.0 / (datas.topic_total_nums - datas.topic_free_nums)).toFixed(0) + "%";
                var gb = (datas.gb * 100.0 / (datas.topic_total_nums - datas.topic_free_nums)).toFixed(0) + "%";
                var tb = (datas.tb * 100.0 / (datas.topic_total_nums - datas.topic_free_nums)).toFixed(0) + "%";
                $("#efak_dashboard_topic_scatter_mb").text(mb);
                $("#efak_dashboard_topic_scatter_mb").css("width", mb);
                $("#efak_dashboard_topic_scatter_gb").text(gb);
                $("#efak_dashboard_topic_scatter_gb").css("width", gb);
                $("#efak_dashboard_topic_scatter_tb").text(tb);
                $("#efak_dashboard_topic_scatter_tb").css("width", tb);
            }

        }
    });
} catch (e) {
    console.log(e);
}

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
if ($('#efak_dashboard_message_in_chart').length) {
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
    var efak_dashboard_message_in_chart = new ApexCharts(document.querySelector("#efak_dashboard_message_in_chart"), lineChartOptions);
    efak_dashboard_message_in_chart.render();
}

function messageInChart() {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/messagein/chart/ajax',
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                setMessageInChartData(efak_dashboard_message_in_chart, datas.messageIns);
                datas = null;
            }
        }
    });
}

// set chart data
function setMessageInChartData(mbean, data) {
    var dataSets = filter(data);
    lineChartOptions.xaxis.categories = dataSets.x;
    lineChartOptions.series[0].data = dataSets.y;
    lineChartOptions.series[0].name = dataSets.name;
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

// load messagein chart
messageInChart();

// producer logsize chart start
if ($('#efak_dashboard_producer_logsize_chart').length) {
    var lineProducerChartOptions = {
        chart: {
            type: "bar",
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
                format: 'yyyy-MM-dd'
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
            // type: "datetime",
            labels: {
                datetimeUTC: false,
                format: "yyyy-MM-dd"
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
        },
        markers: {
            size: 0,
        },
        stroke: {
            width: 2,
            curve: "straight",
        },
        plotOptions: {
            bar: {
                columnWidth: "50%",
                borderRadius: 4,

            }
        },
        dataLabels: {
            enabled: false
        }
    };
    var efak_dashboard_producer_logsize_chart = new ApexCharts(document.querySelector("#efak_dashboard_producer_logsize_chart"), lineProducerChartOptions);
    efak_dashboard_producer_logsize_chart.render();
}

// Chart - END

function producerLogSizeChart() {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/producer/chart/ajax',
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                setProducerChartData(efak_dashboard_producer_logsize_chart, datas);
                datas = null;
            }
        }
    });
}

// topic scatter
if ($('#efak_dashboard_topic_scatter').length) {
    var topicScatterOptions = {
        chart: {
            height: 260,
            type: "radialBar"
        },
        series: [],
        colors: [colors.primary],
        plotOptions: {
            radialBar: {
                hollow: {
                    margin: 15,
                    size: "70%"
                },
                track: {
                    show: true,
                    background: colors.dark,
                    strokeWidth: '100%',
                    opacity: 1,
                    margin: 5,
                },
                dataLabels: {
                    showOn: "always",
                    name: {
                        offsetY: -11,
                        show: true,
                        color: colors.muted,
                        fontSize: "13px"
                    },
                    value: {
                        color: colors.bodyColor,
                        fontSize: "30px",
                        show: true
                    }
                }
            }
        },
        fill: {
            opacity: 1
        },
        stroke: {
            lineCap: "round",
        },
        labels: ["活跃主题占比"]
    };

    var efak_dashboard_topic_scatter = new ApexCharts(document.querySelector("#efak_dashboard_topic_scatter"), topicScatterOptions);
    efak_dashboard_topic_scatter.render();
}

// set chart data
function setProducerChartData(mbean, data) {
    var dataSets = filter(data);
    lineProducerChartOptions.xaxis.categories = dataSets.x;
    lineProducerChartOptions.series[0].data = dataSets.y;
    lineProducerChartOptions.series[0].name = dataSets.name;
    mbean.updateOptions(lineProducerChartOptions);
}

// load producer logsize chart
producerLogSizeChart();

// Storage Chart
if ($('#efak_dashboard_cpu_used').length) {
    var cpuUserOptions = {
        chart: {
            height: 180,
            type: "radialBar"
        },
        series: [0],
        colors: [colors.primary],
        plotOptions: {
            radialBar: {
                hollow: {
                    margin: 15,
                    size: "70%"
                },
                track: {
                    show: true,
                    background: colors.dark,
                    strokeWidth: '100%',
                    opacity: 1,
                    margin: 5,
                },
                dataLabels: {
                    showOn: "always",
                    name: {
                        offsetY: -11,
                        show: true,
                        color: colors.muted,
                        fontSize: "12px"
                    },
                    value: {
                        color: colors.bodyColor,
                        fontSize: "20px",
                        show: true
                    }
                }
            }
        },
        fill: {
            opacity: 1
        },
        stroke: {
            lineCap: "round",
        },
        labels: ["CPU使用率"]
    };

    var efak_dashboard_cpu_used = new ApexCharts(document.querySelector("#efak_dashboard_cpu_used"), cpuUserOptions);
    efak_dashboard_cpu_used.render();
}

if ($('#efak_dashboard_mem_used').length) {
    var memUsedOptions = {
        chart: {
            height: 180,
            type: "radialBar"
        },
        series: [0],
        colors: [colors.primary],
        plotOptions: {
            radialBar: {
                hollow: {
                    margin: 15,
                    size: "70%"
                },
                track: {
                    show: true,
                    background: colors.dark,
                    strokeWidth: '100%',
                    opacity: 1,
                    margin: 5,
                },
                dataLabels: {
                    showOn: "always",
                    name: {
                        offsetY: -11,
                        show: true,
                        color: colors.muted,
                        fontSize: "12px"
                    },
                    value: {
                        color: colors.bodyColor,
                        fontSize: "20px",
                        show: true
                    }
                }
            }
        },
        fill: {
            opacity: 1
        },
        stroke: {
            lineCap: "round",
        },
        labels: ["内存使用率"]
    };

    var efak_dashboard_mem_used = new ApexCharts(document.querySelector("#efak_dashboard_mem_used"), memUsedOptions);
    efak_dashboard_mem_used.render();
}

// Storage Chart - END

function getOSUsedChart() {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/os/chart/ajax',
        beforeSend: function (xmlHttp) {
            xmlHttp.setRequestHeader("If-Modified-Since", "0");
            xmlHttp.setRequestHeader("Cache-Control", "no-cache");
        },
        success: function (datas) {
            if (datas != null) {
                setOsUsedChartData(efak_dashboard_mem_used, datas.mem.toFixed(2));
                setOsUsedChartData(efak_dashboard_cpu_used, datas.cpu.toFixed(2));
                $("#efak_dashboard_cluster_capacity").text(datas.capacity)
                datas = null;
            }
        }
    });
}

function setOsUsedChartData(mbean, data) {
    mbean.updateOptions({
        series: [data]
    });
}

// load os used chart
getOSUsedChart();

function setTopicScatterData(mbean, data) {
    mbean.updateOptions({
        series: [data]
    });
}

function loadTopicScatterRank(topicOrderKey) {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/topic/scatter/ajax?topic_order_key=' + topicOrderKey,
        success: function (datas) {
            if (datas != null) {
                console.log(datas)
                $("#efak_dashboard_topic_scatter_tbody").html("")
                for (var i = 0; i < datas.length; i++) {
                    var id = i + 1;
                    var topic = datas[i].topicName;
                    var logsize = datas[i].topicLogSize;
                    var capacity = datas[i].topicCapacity;
                    var byte_in = datas[i].topicByteIn;
                    var byte_out = datas[i].topicByteOut;
                    var tr = "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + logsize + "</td><td>" + capacity + "</td><td>" + byte_in + "</td><td>" + byte_out + "</td></tr>"
                    $("#efak_dashboard_topic_scatter_tbody").append(tr);
                }
            }
        }
    });
}

// load topic scatter table,default load topic logsize
loadTopicScatterRank('logsize');

// change topic scatter table order
$(".topic-scatter-rank").click(function() {
    var parameter = $(this).data("param");
    loadTopicScatterRank(parameter);
});