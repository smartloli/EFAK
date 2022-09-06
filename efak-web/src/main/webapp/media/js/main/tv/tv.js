$(function () {

    // defined byte size
    var KB_IN_BYTES = 1024;
    var MB_IN_BYTES = 1024 * KB_IN_BYTES;
    var GB_IN_BYTES = 1024 * MB_IN_BYTES;
    var TB_IN_BYTES = 1024 * GB_IN_BYTES;

    // load panel
    getDashboardPanel();

    function getDashboardPanel() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/panel/ajax',
                success: function (datas) {
                    if (datas != null) {
                        dashboard = JSON.parse(datas.dashboard);
                        $("#efak_dashboard_panel_brokers").text(dashboard.brokers);
                        $("#efak_dashboard_panel_topics").text(dashboard.topics);
                        $("#efak_dashboard_panel_zookeepers").text(dashboard.zks);
                        $("#efak_dashboard_panel_consumers").text(dashboard.consumers);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    var chartPanelCommonOption = {
        series: [{
            name: "",
            data: []
        }],
        chart: {
            type: "area",
            //width: 130,
            height: 55,
            toolbar: {
                show: !1
            },
            zoom: {
                enabled: !1
            },
            dropShadow: {
                enabled: 0,
                top: 3,
                left: 14,
                blur: 4,
                opacity: .12,
                color: "#e72e2e"
            },
            sparkline: {
                enabled: !0
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
                horizontal: !1,
                columnWidth: "35%",
                endingShape: "rounded"
            }
        },
        dataLabels: {
            enabled: !1
        },
        stroke: {
            show: !0,
            width: 2.5,
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
                opacityFrom: 0.6,
                opacityTo: 0.1,
                //stops: [0, 100]
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
        tooltip: {
            theme: "dark",
            fixed: {
                enabled: !1
            },
            x: {
                show: !1
            },
            y: {
                title: {
                    formatter: function (e) {
                        return ""
                    }
                }
            },
            marker: {
                show: !1
            }
        }
    };

    var efak_dashboard_msg_in_chart = new ApexCharts(document.querySelector("#efak_dashboard_msg_in_chart"), chartPanelCommonOption);
    efak_dashboard_msg_in_chart.render();

    var efak_dashboard_byte_in_chart = new ApexCharts(document.querySelector("#efak_dashboard_byte_in_chart"), chartPanelCommonOption);
    efak_dashboard_byte_in_chart.render();

    var efak_dashboard_byte_out_chart = new ApexCharts(document.querySelector("#efak_dashboard_byte_out_chart"), chartPanelCommonOption);
    efak_dashboard_byte_out_chart.render();

    var efak_dashboard_osfree_memory_chart = new ApexCharts(document.querySelector("#efak_dashboard_osfree_memory_chart"), chartPanelCommonOption);
    efak_dashboard_osfree_memory_chart.render();

    var efak_dashboard_cpu_used_chart = new ApexCharts(document.querySelector("#efak_dashboard_cpu_used_chart"), chartPanelCommonOption);
    efak_dashboard_cpu_used_chart.render();

    var efak_dashboard_failed_fetch_request_chart = new ApexCharts(document.querySelector("#efak_dashboard_failed_fetch_request_chart"), chartPanelCommonOption);
    efak_dashboard_failed_fetch_request_chart.render();

    var efak_dashboard_total_fetch_request_chart = new ApexCharts(document.querySelector("#efak_dashboard_total_fetch_request_chart"), chartPanelCommonOption);
    efak_dashboard_total_fetch_request_chart.render();

    var efak_dashboard_total_produce_request_chart = new ApexCharts(document.querySelector("#efak_dashboard_total_produce_request_chart"), chartPanelCommonOption);
    efak_dashboard_total_produce_request_chart.render();

    getDashboardAreaChart();

    function getDashboardAreaChart() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/areachart/ajax',
                success: function (datas) {
                    if (datas != null) {
                        setTrendData(efak_dashboard_msg_in_chart, 'message_in', datas);
                        setTrendData(efak_dashboard_byte_in_chart, 'byte_in', datas);
                        setTrendData(efak_dashboard_byte_out_chart, 'byte_out', datas);
                        setTrendData(efak_dashboard_osfree_memory_chart, 'os_free_memory', datas);
                        setTrendData(efak_dashboard_cpu_used_chart, 'cpu_used', datas);
                        setTrendData(efak_dashboard_failed_fetch_request_chart, 'failed_fetch_request', datas);
                        setTrendData(efak_dashboard_total_fetch_request_chart, 'total_fetch_requests', datas);
                        setTrendData(efak_dashboard_total_produce_request_chart, 'total_produce_requests', datas);
                        datas = null;
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    // set trend data
    function setTrendData(mbean, filed, data) {
        switch (filed) {
            case "message_in":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.messageIns[data.messageIns.length - 1].y * 60).toFixed(1);
                cunit = " (MSG/min)";
                $("#efak_dashboard_message_in_lastest").text("[ " + value + cunit + " ]");
                break;
            case "failed_fetch_request":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.failedFetchRequest[data.failedFetchRequest.length - 1].y * 60).toFixed(1);
                cunit = " (MSG/min)";
                $("#efak_dashboard_faild_fetch_request_lastest").text("[" + value + cunit + "]");
                break;
            case "total_fetch_requests":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.totalFetchRequests[data.totalFetchRequests.length - 1].y * 60).toFixed(1);
                cunit = " (MSG/min)";
                $("#efak_dashboard_total_fetch_request_lastest").text("[" + value + cunit + "]");
                break;
            case "total_produce_requests":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.totalProduceRequests[data.totalProduceRequests.length - 1].y * 60).toFixed(1);
                cunit = " (MSG/min)";
                $("#efak_dashboard_total_produce_request_lastest").text("[" + value + cunit + "]");
                break;
            case "byte_in":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = stringify(data.byteIns[data.byteIns.length - 1].y).value;
                cunit = stringify(data.byteIns[data.byteIns.length - 1].y).type;
                $("#efak_dashboard_byte_in_lastest").text("[" + value + cunit + "]");
                break;
            case "byte_out":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = stringify(data.byteOuts[data.byteOuts.length - 1].y).value;
                cunit = stringify(data.byteOuts[data.byteOuts.length - 1].y).type;
                $("#efak_dashboard_byte_out_lastest").text("[ " + value + cunit + " ]");
                break;
            case "os_free_memory":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.osFreeMems[data.osFreeMems.length - 1].y * 1.0 / GB_IN_BYTES).toFixed(2);
                cunit = " (GB/min)";
                $("#efak_dashboard_osfreememory_lastest").text("[ " + value + cunit + " ]");
                break;
            case "cpu_used":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = data.cpuUsed[data.cpuUsed.length - 1].y;
                $("#efak_dashboard_cpu_used_lastest").text("[ " + value.toFixed(1) + "% ]");
                break;
            default:
                break;
        }
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
            case "cpu_used":
                for (var i = 0; i < datas.cpuUsed.length; i++) {
                    datax.push(datas.cpuUsed[i].x);
                    datay.push(datas.cpuUsed[i].y);
                }
                data.name = "CpuUsed (%)";
                break;
            case "byte_in":
                var cunit = "";
                var init = (datas.byteIns.length - 10) > 0 ? (datas.byteIns.length - 10) : 0;
                for (var i = init; i < datas.byteIns.length; i++) {
                    datax.push(datas.byteIns[i].x);
                    var value = stringify(datas.byteIns[i].y).value;
                    cunit = stringify(datas.byteIns[i].y).type;
                    datay.push(value);
                }
                data.name = "BytesInPerSec" + cunit;
                break;
            case "byte_out":
                var cunit = "";
                var init = (datas.byteOuts.length - 10) > 0 ? (datas.byteOuts.length - 10) : 0;
                for (var i = init; i < datas.byteOuts.length; i++) {
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
                var init = (datas.osFreeMems.length - 10) > 0 ? (datas.osFreeMems.length - 10) : 0;
                for (var i = init; i < datas.osFreeMems.length; i++) {
                    datax.push(datas.osFreeMems[i].x);
                    var value = (datas.osFreeMems[i].y * 1.0 / GB_IN_BYTES).toFixed(2);
                    datay.push(value);
                }
                data.name = "OSFreeMemory (GB/min)";
                break;
            case "topic_logsize":
                for (var i = 0; i < datas.length; i++) {
                    datax.push(datas[i].x);
                    datay.push(parseInt(datas[i].y, 10));
                }
                data.name = "LogSize";
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

    // mem and cpu rate
    function getDashboardMem() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/mem/ajax',
                success: function (datas) {
                    if (datas != null) {
                        $("#efak_tv_monitor_mem_usage").text(datas.mem.toFixed(1) + "%");
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    function getDashboardCpu() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/cpu/ajax',
                success: function (datas) {
                    if (datas != null) {
                        $("#efak_tv_monitor_cpu_usage").text(datas.cpu.toFixed(1) + "%");
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    getDashboardMem();
    getDashboardCpu();

    // Topic logsize
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
                horizontal: !1,
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

    var efak_dashboard_logsize_chart = new ApexCharts(document.querySelector("#efak_dashboard_logsize_chart"), efakTopicLogSizeOptions);
    efak_dashboard_logsize_chart.render();

    function getDashboardTopicLogSize() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/topic/logsize/ajax',
                success: function (datas) {
                    if (datas != null) {
                        efakTopicLogSizeOptions.xaxis.categories = filter(datas, "topic_logsize").x;
                        efakTopicLogSizeOptions.series[0].data = filter(datas, "topic_logsize").y;
                        efakTopicLogSizeOptions.series[0].name = filter(datas, "topic_logsize").name;
                        efak_dashboard_logsize_chart.updateOptions(efakTopicLogSizeOptions);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    getDashboardTopicLogSize();

    // table topic by capacity and cluster info
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/get/dashboard/rank/capacity/table/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#efak_dashboard_capacity_table").html("")
                var count = 0;
                for (var i = 0; i < datas.length; i++) {
                    if (count > 2) {
                        break;
                    }
                    var topic = datas[i].topic_text;
                    var capacity = datas[i].capacity;
                    $("#efak_dashboard_capacity_table").append("<div class='service-item__RCcpv'><span>" + topic + " (" + capacity + ")" + "</span></div>");
                    count++;
                }
            }
        }
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/get/tv/dashboard/mid/result/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#efak_tv_cluster").text(datas.cluster);
                $("#efak_tv_version").text(datas.version);
                $("#efak_tv_capacity").text(datas.capacity + " (" + datas.capacityType + ")");
                $("#efak_tv_mode").text(datas.mode);
                $("#efak_tv_app").text(datas.app);
            }
        }
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/get/tv/dashboard/mid/result/worknode/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#efak_tv_worknode").text(datas.worknode);
            }
        }
    });

});