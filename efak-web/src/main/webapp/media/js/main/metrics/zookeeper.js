$(document).ready(function () {

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
        var type = "zookeeper";

        zkRealtime(stime, etime, type);

        reportrange.on('apply.daterangepicker', function (ev, picker) {
            stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
            etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
            zkRealtime(stime, etime, type);
        });
        setInterval(function () {
            zkRealtime(stime, etime, type)
        }, 1000 * 60 * 1);
    } catch (e) {
        console.log(e.message);
    }

    function morrisLineInit(elment) {
        var efakMetricsChart = new ApexCharts(document.querySelector("#" + elment), chartCommonOption);
        efakMetricsChart.render();
        return efakMetricsChart;
    }

    var zk_packets_sent = morrisLineInit('zk_send_packets');
    var zk_packets_received = morrisLineInit('zk_recevied_packets');
    var zk_num_alive_connections = morrisLineInit('zk_alives_connections');
    var zk_outstanding_requests = morrisLineInit('zk_queue_requests');

    function zkRealtime(stime, etime, type) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/metrics/trend/mbean/ajax?stime=' + stime + '&etime=' + etime + '&type=' + type,
            beforeSend: function (xmlHttp) {
                xmlHttp.setRequestHeader("If-Modified-Since", "0");
                xmlHttp.setRequestHeader("Cache-Control", "no-cache");
            },
            success: function (datas) {
                if (datas != null) {
                    setTrendData(zk_packets_sent, 'zk_packets_sent', datas);
                    setTrendData(zk_packets_received, 'zk_packets_received', datas);
                    setTrendData(zk_num_alive_connections, 'zk_num_alive_connections', datas);
                    setTrendData(zk_outstanding_requests, 'zk_outstanding_requests', datas);
                    datas = null;
                }
            }
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
            case "zk_packets_sent":
                for (var i = 0; i < datas.send.length; i++) {
                    datax.push(datas.send[i].x);
                    datay.push(datas.send[i].y);
                }
                data.name = "ZookeeperPacketsSent (B/min)";
                break;
            case "zk_num_alive_connections":
                for (var i = 0; i < datas.alive.length; i++) {
                    datax.push(datas.alive[i].x);
                    datay.push(datas.alive[i].y);
                }
                data.name = "ZookeeperAliveConnections (/min)";
                break;
            case "zk_outstanding_requests":
                for (var i = 0; i < datas.queue.length; i++) {
                    datax.push(datas.queue[i].x);
                    datay.push(datas.queue[i].y);
                }
                data.name = "ZookeeperOutstandingRequests (/min)";
                break;
            case "zk_packets_received":
                for (var i = 0; i < datas.received.length; i++) {
                    datax.push(datas.received[i].x);
                    datay.push(datas.received[i].y);
                }
                data.name = "ZookeeperPacketsReceived (B/min)";
                break;
            default:
                break;
        }
        data.x = datax;
        data.y = datay;
        return data;
    }
});