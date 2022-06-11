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

    var efak_dashboard_msg_in_chart = new ApexCharts(document.querySelector("#efak_dashboard_msg_in_chart"), chartCommonOption);
    efak_dashboard_msg_in_chart.render();


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

    var efak_dashboard_byte_in_chart = new ApexCharts(document.querySelector("#efak_dashboard_byte_in_chart"), chartPanelCommonOption);
    efak_dashboard_byte_in_chart.render();

    var efak_dashboard_byte_out_chart = new ApexCharts(document.querySelector("#efak_dashboard_byte_out_chart"), chartPanelCommonOption);
    efak_dashboard_byte_out_chart.render();

    var efak_dashboard_osfree_memory_chart = new ApexCharts(document.querySelector("#efak_dashboard_osfree_memory_chart"), chartPanelCommonOption);
    efak_dashboard_osfree_memory_chart.render();

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
                chartCommonOption.xaxis.categories = filter(data, filed).x;
                chartCommonOption.series[0].data = filter(data, filed).y;
                chartCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartCommonOption);
                break;
            case "byte_in":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = stringify(data.byteIns[data.byteIns.length - 1].y).value;
                cunit = stringify(data.byteIns[data.byteIns.length - 1].y).type;
                $("#efak_dashboard_byte_in_lastest").text(value + cunit);
                break;
            case "byte_out":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = stringify(data.byteOuts[data.byteOuts.length - 1].y).value;
                cunit = stringify(data.byteOuts[data.byteOuts.length - 1].y).type;
                $("#efak_dashboard_byte_out_lastest").text(value + cunit);
                break;
            case "os_free_memory":
                chartPanelCommonOption.xaxis.categories = filter(data, filed).x;
                chartPanelCommonOption.series[0].data = filter(data, filed).y;
                chartPanelCommonOption.series[0].name = filter(data, filed).name;
                mbean.updateOptions(chartPanelCommonOption);
                var value = (data.osFreeMems[data.osFreeMems.length - 1].y * 1.0 / GB_IN_BYTES).toFixed(2);
                cunit = " (GB/min)";
                $("#efak_dashboard_osfreememory_lastest").text(value + cunit);
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
                // var init = (datas.messageIns.length - 10) > 0 ? (datas.messageIns.length - 10) : 0;
                for (var i = 0; i < datas.messageIns.length; i++) {
                    datax.push(datas.messageIns[i].x);
                    datay.push((datas.messageIns[i].y * 60).toFixed(2));
                }
                data.name = "MessagesInPerSec (msg/min)";
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
    var kafkaOsMemOption = {
        type: 'doughnut',
        data: {
            labels: ["Free", "Used"],
            datasets: [{
                label: "",
                backgroundColor: ["#8ea8fd", "#3461ff"],
                data: []
            }]
        },
        options: {
            maintainAspectRatio: false,
            cutoutPercentage: 85,
            responsive: true,
            legend: {
                display: false
            }
        }
    }
    var efak_dashboard_mem_chart = new Chart(document.getElementById('efak_dashboard_mem_chart'), kafkaOsMemOption);

    var kafkaOsCpuOption = {
        type: 'doughnut',
        data: {
            labels: ["Free", "Used"],
            datasets: [{
                label: "",
                backgroundColor: ["#8ea8fd", "#3461ff"],
                data: []
            }]
        },
        options: {
            maintainAspectRatio: false,
            cutoutPercentage: 85,
            responsive: true,
            legend: {
                display: false
            }
        }
    }
    var efak_dashboard_cpu_chart = new Chart(document.getElementById('efak_dashboard_cpu_chart'), kafkaOsCpuOption);

    function getDashboardMem() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/mem/ajax',
                success: function (datas) {
                    if (datas != null) {
                        $("#efak_dashboard_mem_chart_id").text(datas.mem + "%");
                        refreshMemOrCpu(efak_dashboard_mem_chart, "mem", datas.mem);
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
                        $("#efak_dashboard_cpu_chart_id").text(datas.cpu + "%");
                        refreshMemOrCpu(efak_dashboard_cpu_chart, "cpu", datas.cpu);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    function refreshMemOrCpu(chartObject, type, value) {
        switch (type) {
            case "mem":
                kafkaOsMemOption.data.datasets[0].label = "Kafka Memory";
                kafkaOsMemOption.data.datasets[0].data = [100 - value, value];
                chartObject.update();
                break;
            case "cpu":
                kafkaOsCpuOption.data.datasets[0].label = "Kafka CPU";
                kafkaOsCpuOption.data.datasets[0].data = [100 - value, value];
                chartObject.update();
                break;
            default:
                break;
        }
    }

    getDashboardMem();
    getDashboardCpu();

    // get active topic scatter
    var activeTopicOptions = {
        chart: {
            height: 300,
            type: 'radialBar',
            toolbar: {
                show: false
            }
        },
        plotOptions: {
            radialBar: {
                //startAngle: -135,
                //endAngle: 225,
                hollow: {
                    margin: 0,
                    size: '80%',
                    background: 'transparent',
                    image: undefined,
                    imageOffsetX: 0,
                    imageOffsetY: 0,
                    position: 'front',
                    dropShadow: {
                        enabled: true,
                        top: 3,
                        left: 0,
                        blur: 4,
                        color: 'rgba(0, 169, 255, 0.85)',
                        opacity: 0.65
                    }
                },
                track: {
                    background: '#e8edff',
                    strokeWidth: '67%',
                    margin: 0, // margin is in pixels
                    dropShadow: {
                        enabled: 0,
                        top: -3,
                        left: 0,
                        blur: 4,
                        color: 'rgba(0, 169, 255, 0.85)',
                        opacity: 0.65
                    }
                },
                dataLabels: {
                    showOn: 'always',
                    name: {
                        offsetY: -20,
                        show: true,
                        color: '#212529',
                        fontSize: '16px'
                    },
                    value: {
                        formatter: function (val) {
                            return val + "%";
                        },
                        color: '#212529',
                        fontSize: '35px',
                        show: true,
                        offsetY: 10,
                    }
                }
            }
        },
        fill: {
            type: 'gradient',
            gradient: {
                shade: 'light',
                type: 'horizontal',
                shadeIntensity: 0.5,
                gradientToColors: ['#3461ff'],
                inverseColors: false,
                opacityFrom: 1,
                opacityTo: 1,
                stops: [0, 100]
            }
        },
        colors: ["#3461ff"],
        series: [],
        stroke: {
            lineCap: 'round',
            //dashArray: 4
        },
        labels: ['Active'],
        responsive: [
            {
                breakpoint: 1281,
                options: {
                    chart: {
                        height: 280,
                    }
                }
            }
        ],

    }

    var efak_dashboard_active_topic_chart = new ApexCharts(
        document.querySelector("#efak_dashboard_active_topic_chart"),
        activeTopicOptions
    );

    efak_dashboard_active_topic_chart.render();


    function getDashboardTopicActive() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/get/dashboard/active/topic/ajax',
                success: function (datas) {
                    if (datas != null) {
                        $("#efak_dashboard_active_topic_nums").text(datas.active);
                        $("#efak_dashboard_standby_topic_nums").text(datas.standby);
                        $("#efak_dashboard_active_topic_mb").text(datas.mb);
                        $("#efak_dashboard_active_topic_gb").text(datas.gb);
                        $("#efak_dashboard_active_topic_tb").text(datas.tb);
                        activeTopicOptions.series = [(datas.active * 100.0 / datas.total).toFixed(2)];
                        var mb = (datas.mb * 100.0 / datas.active).toFixed(0) + "%";
                        var gb = (datas.gb * 100.0 / datas.active).toFixed(0) + "%";
                        var tb = (datas.tb * 100.0 / datas.active).toFixed(0) + "%";

                        $("#efak_dashboard_active_topic_mb_div").css({
                            width: mb
                        });
                        $("#efak_dashboard_active_topic_gb_div").css({
                            width: gb
                        });
                        $("#efak_dashboard_active_topic_tb_div").css({
                            width: tb
                        });
                        efak_dashboard_active_topic_chart.updateOptions(activeTopicOptions);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    getDashboardTopicActive();


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
            type: 'datetime',
            labels: {
                datetimeUTC: false,
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
            theme: "dark"
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

});