$(document).ready(function () {

    try {
        var path = window.location.href;
        $("#ke_consumer_offsets_a").attr("href", "/consumers/offset/?" + path.split("?")[1]);
    } catch (e) {
        console.error(e);
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        var context = "";
        if (r != null)
            context = r[2];
        reg = null;
        r = null;
        return context == null || context == "" || context == "undefined" ? "" : context;
    }

    var group = getQueryString("group");
    var topic = getQueryString("topic");

    $("#topic_lag_name_header").find("strong").text("Consumer Blocking Metrics (" + topic + ")");
    $("#topic_producer_name_header").find("strong").text("Producer Performance Metrics (" + topic + ")");
    $("#topic_consumer_name_header").find("strong").text("Consumer Performance Metrics (" + topic + ")");

    // chart lag option
    var chartLagOption = {
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

    // chart producer option
    var chartProducerOption = {
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

    // chart consumer option
    var chartConsumerOption = {
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

    // topic lag chart
    var efak_topic_lag_chart = new ApexCharts(document.querySelector("#efak_topic_lag_chart"), chartLagOption);
    efak_topic_lag_chart.render();

    // topic producer chart
    var efak_topic_producer_chart = new ApexCharts(document.querySelector("#efak_topic_producer_chart"), chartProducerOption);
    efak_topic_producer_chart.render();

    // topic consumer chart
    var efak_topic_consumer_chart = new ApexCharts(document.querySelector("#efak_topic_consumer_chart"), chartConsumerOption);
    efak_topic_consumer_chart.render();

    // daterangepicker
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

    function offserRealtime(stime, etime) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/consumer/offset/group/topic/realtime/ajax?group=' + group + '&topic=' + topic + '&stime=' + stime + '&etime=' + etime,
            success: function (datas) {
                if (datas != null) {
                    // lag
                    chartLagOption.xaxis.categories = datas.lag.x;
                    chartLagOption.series[0].data = datas.lag.y;
                    chartLagOption.series[0].name = datas.lag.name;
                    efak_topic_lag_chart.updateOptions(chartLagOption);
                    // producer
                    chartProducerOption.xaxis.categories = datas.producer.x;
                    chartProducerOption.series[0].data = datas.producer.y;
                    chartProducerOption.series[0].name = datas.producer.name;
                    efak_topic_producer_chart.updateOptions(chartProducerOption);
                    // consumer
                    chartConsumerOption.xaxis.categories = datas.consumer.x;
                    chartConsumerOption.series[0].data = datas.consumer.y;
                    chartConsumerOption.series[0].name = datas.consumer.name;
                    efak_topic_consumer_chart.updateOptions(chartConsumerOption);
                    datas = [];
                }
            }
        });
    }

    function offserRateRealtime() {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/consumer/offset/rate/group/topic/realtime/ajax?group=' + group + '&topic=' + topic,
            success: function (datas) {
                if (datas != null) {
                    // Consumer & Producer Rate
                    $("#efak_topic_producer_rate").text(datas.ins);
                    $("#efak_topic_consumer_rate").text(datas.outs);
                    datas = [];
                }
            }
        });
    }

    reportrange.on('apply.daterangepicker', function (ev, picker) {
        stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
        etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
        offserRealtime(stime, etime);
    });

    offserRealtime(stime, etime);
    offserRateRealtime();
    setInterval(function () {
        offserRealtime(stime, etime)
    }, 1000 * 60 * 1);
    setInterval(offserRateRealtime, 1000 * 60 * 1);
});