$(document).ready(function () {

    // defined byte size
    var KB_IN_BYTES = 1024;
    var MB_IN_BYTES = 1024 * KB_IN_BYTES;
    var GB_IN_BYTES = 1024 * MB_IN_BYTES;
    var TB_IN_BYTES = 1024 * GB_IN_BYTES;

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

    initModuleVisualAndBindEvent();

    var modules = getCheckedModules();

    var mbean_msg_in = morrisLineInit('mbean_msg_in');
    var mbean_msg_byte_in = morrisLineInit('mbean_msg_byte_in');
    var mbean_msg_byte_out = morrisLineInit('mbean_msg_byte_out');
    var mbean_byte_rejected = morrisLineInit('mbean_byte_rejected');
    var mbean_failed_fetch_request = morrisLineInit('mbean_failed_fetch_request');
    var mbean_failed_produce_request = morrisLineInit('mbean_failed_produce_request');
    var mbean_produce_message_conversions = morrisLineInit('mbean_produce_message_conversions');
    var mbean_total_fetch_requests = morrisLineInit('mbean_total_fetch_requests');
    var mbean_total_produce_requests = morrisLineInit('mbean_total_produce_requests');
    var mbean_replication_bytes_out = morrisLineInit('mbean_replication_bytes_out');
    var mbean_replication_bytes_in = morrisLineInit('mbean_replication_bytes_in');
    var mbean_os_free_memory = morrisLineInit('mbean_os_free_memory');

    function mbeanRealtime(stime, etime, type, modules) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/metrics/trend/mbean/ajax?stime=' + stime + '&etime=' + etime + '&type=' + type + '&modules=' + modules,
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
    var type = "kafka";

    mbeanRealtime(stime, etime, type, getCheckedModules());

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
        mbeanRealtime(stime, etime, type, getCheckedModules());
    });
    setInterval(function () {
        mbeanRealtime(stime, etime, type, getCheckedModules())
    }, 1000 * 60 * 1);

    function morrisLineInit(elment) {
        var efakMetricsChart = new ApexCharts(document.querySelector("#" + elment), chartCommonOption);
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
                    mbeanRealtime(stime, etime, type, getCheckedModules());
                    return;
                }
                module('#' + $(that).attr('name'), false);
            });
        });
    }

    // set trend data
    function setTrendData(mbean, filed, data) {
        chartCommonOption.xaxis.categories = filter(data, filed).x;
        chartCommonOption.series[0].data = filter(data, filed).y;
        chartCommonOption.series[0].name = filter(data, filed).name;
        chartCommonOption.legend.data = [filter(data, filed).name];
        mbean.updateOptions(chartCommonOption);
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
});