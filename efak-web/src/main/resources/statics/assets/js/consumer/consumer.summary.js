if ($("#efak_consumer_group_list").length) {
    $("#efak_consumer_group_list").select2({
        placeholder: "请选择消费者名称", allowClear: true, ajax: {
            url: "/consumer/summary/groups/set", dataType: 'json', delay: 250, data: function (params) {
                params.offset = 10;
                params.page = params.page || 1;
                return {
                    name: params.term, page: params.page, offset: params.offset
                };
            }, cache: true, processResults: function (data, params) {
                if (data.items.length > 0) {
                    var datas = new Array();
                    $.each(data.items, function (index, e) {
                        var s = {};
                        s.id = index + 1;
                        s.text = e.text;
                        datas[index] = s;
                    });
                    return {
                        results: datas, pagination: {
                            more: (params.page * params.offset) < data.total
                        }
                    };
                } else {
                    return {
                        results: []
                    }
                }
            }, escapeMarkup: function (markup) {
                return markup;
            }, minimumInputLength: 0
        }
    });
}

// consumer group topology
var chartDom = document.getElementById('efak_cosumer_group_topology_chart');
var myChart = echarts.init(chartDom);
var option;

var charts = {
    nodes: [],
    linesData: []
};

function chart(nodes, charts) {
    charts.nodes = [];
    for (var j = 0; j < nodes.length; j++) {
        const {x, y, nodeName, svgPath, symbolSize} = nodes[j];
        var node = {
            nodeName, value: [x, y], symbolSize: symbolSize || 50, symbol: 'path://' + svgPath, itemStyle: {
                color: '#6571ff'
            }
        };
        charts.nodes.push(node);
    }
}


option = {
    backgroundColor: '#0B1321', xAxis: {
        min: 0, max: 1000, show: false, type: 'value'
    }, yAxis: {
        min: 0, max: 1000, show: false, type: 'value'
    }, series: [{
        type: 'graph', coordinateSystem: 'cartesian2d', label: {
            show: true, position: 'bottom', color: '#6571ff', formatter: function (item) {
                return item.data.nodeName;
            }
        }, data: []
    }, {
        type: 'lines', polyline: true, coordinateSystem: 'cartesian2d', lineStyle: {
            type: 'dashed', width: 2, color: '#6571ff', curveness: 0.3
        }, effect: {
            show: true, trailLength: 0.1, symbol: 'arrow', color: '#6571ff', symbolSize: 8
        }, data: []
    }]
};

option && myChart.setOption(option);

$('#efak_consumer_group_list').on('select2:select', function (evt) {
    var text = evt.params.data.text;
    $("#efak_consumer_group_list").val(text);
    // topology
    topology(text);
});

// get consumer groups summary
try {
    $.ajax({
        type: 'get', dataType: 'json', url: '/consumer/summary/groups/ajax', success: function (datas) {
            $("#efka_consumer_group_total").text(datas.total_group_size);
            $("#efka_consumer_group_active").text(datas.active_group_size);
        }
    });
} catch (e) {
    console.log(e);
}

// consumer group topology

try {
    $.ajax({
        type: 'get', dataType: 'json', url: '/consumer/summary/groups/one', success: function (datas) {
            topology(datas.groupId);
        }
    });
} catch (e) {
    console.log(e);
}

function topology(groupId) {
    try {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/consumer/summary/groups/topology/ajax?groupId=' + groupId,
            success: function (datas) {
                console.log(datas);
                charts.linesData=datas.linesData;
                chart(datas.nodes,charts);
                option.series[0].data = charts.nodes;
                option.series[1].data = charts.linesData;
                // update echart data
                myChart.setOption(option);

            }
        });
    } catch (e) {
        console.log(e);
    }
}