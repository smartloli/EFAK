$(document).ready(function () {
    // tree option start
    keBrokerTreeOption = {
        series: {
            type: 'tree',
            //initialTreeDepth: -1,
            data: [],
            top: '1%',
            left: '20%',
            bottom: '1%',
            right: '40%',
            symbolSize: 10,
            itemStyle: {
                color: "rgb(176, 196, 222)", // fold color
                borderColor: 'steelblue'
            },
            label: {
                position: 'left',
                verticalAlign: 'middle',
                align: 'right',
                fontSize: 14
            },
            leaves: {
                label: {
                    normal: {
                        position: 'right',
                        verticalAlign: 'middle',
                        align: 'left',
                    }
                }
            }
        }
    }
    // tree option end

    keBrokerTree = echarts.init(document.getElementById("ke_dash_brokers_graph"));

    function dashPanel() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/dash/kafka/ajax',
                success: function (datas) {
                    if (datas != null) {
                        dashboard = JSON.parse(datas.dashboard);
                        kafka = JSON.parse(datas.kafka);
                        var kafkaData = new Array();
                        kafkaData.push(kafka);
                        $("#ke_dash_brokers_count").text(dashboard.brokers);
                        $("#ke_dash_topics_count").text(dashboard.topics);
                        $("#ke_dash_zks_count").text(dashboard.zks);
                        $("#ke_dash_consumers_count").text(dashboard.consumers);
                        keBrokerTreeOption.series.data = kafkaData;
                        keBrokerTree.setOption(keBrokerTreeOption);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    dashPanel();

    $("#ke_dash_brokers_graph").resize(function () {
        var optKeBrokerTree = keBrokerTree.getOption();
        keBrokerTree.clear();
        keBrokerTree.resize({width: $("#ke_dash_brokers_graph").css('width')});
        keBrokerTree.setOption(optKeBrokerTree);
    });

    function fillgaugeGrahpPie(datas, id) {
        var config = liquidFillGaugeDefaultSettings();
        config.circleThickness = 0.1;
        config.circleFillGap = 0.2;
        config.textVertPosition = 0.8;
        config.waveAnimateTime = 2000;
        config.waveHeight = 0.3;
        config.waveCount = 1;
        if (datas > 65 && datas < 80) {
            config.circleColor = "#D4AB6A";
            config.textColor = "#553300";
            config.waveTextColor = "#805615";
            config.waveColor = "#AA7D39";
        } else if (datas >= 80) {
            config.circleColor = "#d9534f";
            config.textColor = "#d9534f";
            config.waveTextColor = "#d9534f";
            config.waveColor = "#FFDDDD";
        }
        loadLiquidFillGauge(id, datas, config);
    }

    function osMem() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/dash/os/mem/ajax',
                success: function (datas) {
                    if (datas != null) {
                        fillgaugeGrahpPie(datas.mem, "fillgauge_kafka_memory");
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    function cpu() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/dash/used/cpu/ajax',
                success: function (datas) {
                    if (datas != null) {
                        fillgaugeGrahpPie(datas.cpu, "fillgauge_kafka_cpu");
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    osMem();
    cpu();

    $("#ke_dash_os_memory_div").resize(function () {
        $("#ke_dash_os_memory_div").html("<svg id='fillgauge_kafka_memory' width='97%' height='424'></svg>");
        osMem();
    });
    $("#ke_dash_cpu_div").resize(function () {
        $("#ke_dash_cpu_div").html("<svg id='fillgauge_kafka_cpu' width='97%' height='424'></svg>");
        cpu();
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/dash/logsize/table/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#topic_logsize").html("")
                var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>LogSize</th></tr></thead>";
                $("#topic_logsize").append(thead);
                var tbody = "<tbody>";
                var tr = '';
                for (var i = 0; i < datas.length; i++) {
                    var id = datas[i].id;
                    var topic = datas[i].topic;
                    var logsize = datas[i].logsize;
                    tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + logsize + "</td></tr>"
                }
                tbody += tr + "</tbody>"
                $("#topic_logsize").append(tbody);
            }
        }
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/dash/capacity/table/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#topic_capacity").html("")
                var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>Capacity</th></tr></thead>";
                $("#topic_capacity").append(thead);
                var tbody = "<tbody>";
                var tr = '';
                for (var i = 0; i < datas.length; i++) {
                    var id = datas[i].id;
                    var topic = datas[i].topic;
                    var capacity = datas[i].capacity;
                    tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + capacity + "</td></tr>"
                }
                tbody += tr + "</tbody>"
                $("#topic_capacity").append(tbody);
            }
        }
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/dash/byte_in/table/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#topic_byte_in").html("")
                var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>Throughput</th></tr></thead>";
                $("#topic_byte_in").append(thead);
                var tbody = "<tbody>";
                var tr = '';
                for (var i = 0; i < datas.length; i++) {
                    var id = datas[i].id;
                    var topic = datas[i].topic;
                    var byte_in = datas[i].byte_in;
                    tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + byte_in + "</td></tr>"
                }
                tbody += tr + "</tbody>"
                $("#topic_byte_in").append(tbody);
            }
        }
    });

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/dash/byte_out/table/ajax',
        success: function (datas) {
            if (datas != null) {
                $("#topic_byte_out").html("")
                var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>Throughput</th></tr></thead>";
                $("#topic_byte_out").append(thead);
                var tbody = "<tbody>";
                var tr = '';
                for (var i = 0; i < datas.length; i++) {
                    var id = datas[i].id;
                    var topic = datas[i].topic;
                    var byte_out = datas[i].byte_out;
                    tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + byte_out + "</td></tr>"
                }
                tbody += tr + "</tbody>"
                $("#topic_byte_out").append(tbody);
            }
        }
    });

});