if ($("#efak_consumer_group_list").length) {
    $("#efak_consumer_group_list").select2({
        placeholder: "请选择消费者名称",
        allowClear: true,
        ajax: {
            url: "/topic/name/mock/ajax",
            dataType: 'json',
            delay: 250,
            data: function (params) {
                params.offset = 10;
                params.page = params.page || 1;
                return {
                    name: params.term,
                    page: params.page,
                    offset: params.offset
                };
            },
            cache: true,
            processResults: function (data, params) {
                if (data.items.length > 0) {
                    var datas = new Array();
                    $.each(data.items, function (index, e) {
                        var s = {};
                        s.id = index + 1;
                        s.text = e.text;
                        datas[index] = s;
                    });
                    return {
                        results: datas,
                        pagination: {
                            more: (params.page * params.offset) < data.total
                        }
                    };
                } else {
                    return {
                        results: []
                    }
                }
            },
            escapeMarkup: function (markup) {
                return markup;
            },
            minimumInputLength: 0
        }
    });
}

// consumer group topology
var chartDom = document.getElementById('efak_cosumer_group_topology_chart');
var myChart = echarts.init(chartDom);
var option;

var nodes = [
    {
        x: 500,
        y: 1000,
        nodeName: 'test_app_group',
        svgPath:
            'M1172.985723 682.049233l-97.748643-35.516964a32.583215 32.583215 0 0 0-21.830134 61.582735l25.7398 9.123221-488.744218 238.181638L115.670112 741.349163l47.245961-19.223356a32.583215 32.583215 0 0 0-22.808051-60.604819l-119.579777 47.896905a32.583215 32.583215 0 0 0 0 59.952875l557.820313 251.540496a32.583215 32.583215 0 0 0 27.695632 0l570.527227-278.584184a32.583215 32.583215 0 0 0-3.258721-59.952875z,M1185.041693 482.966252l-191.587622-68.749123a32.583215 32.583215 0 1 0-21.831133 61.254764l118.927833 43.010323-488.744218 237.855666-471.474695-213.744727 116.973-47.244961a32.583215 32.583215 0 1 0-24.111938-60.604819l-190.609705 75.593537a32.583215 32.583215 0 0 0-20.528246 29.650465 32.583215 32.583215 0 0 0 20.528246 30.30141l557.819313 251.866468a32.583215 32.583215 0 0 0 27.695632 0l570.201254-278.584184a32.583215 32.583215 0 0 0 18.24744-30.953354 32.583215 32.583215 0 0 0-21.505161-29.651465z,M32.583215 290.075742l557.819313 251.540496a32.583215 32.583215 0 0 0 27.695632 0l570.201254-278.584184a32.583215 32.583215 0 0 0-3.257721-59.952875L626.244463 2.042365a32.583215 32.583215 0 0 0-23.134022 0l-570.527226 228.080502a32.583215 32.583215 0 0 0-19.224357 30.627382 32.583215 32.583215 0 0 0 19.224357 29.325493zM615.817355 67.534767l474.733416 170.408432-488.744218 238.180638-471.474695-215.372588z',
        symbolSize: 70
    },
    {
        x: 100,
        y: 600,
        nodeName: 'topic_name_1',
        svgPath:
            'M544 552.325V800a32 32 0 0 1-32 32 31.375 31.375 0 0 1-32-32V552.325L256 423.037a32 32 0 0 1-11.525-43.512A31.363 31.363 0 0 1 288 368l224 128 222.075-128a31.363 31.363 0 0 1 43.525 11.525 31.988 31.988 0 0 1-11.525 43.513L544 551.038z m0 0,M64 256v512l448 256 448-256V256L512 0z m832 480L512 960 128 736V288L512 64l384 224z m0 0'
    },
    {
        x: 500,
        y: 600,
        nodeName: 'topic_name_2',
        svgPath:
            'M544 552.325V800a32 32 0 0 1-32 32 31.375 31.375 0 0 1-32-32V552.325L256 423.037a32 32 0 0 1-11.525-43.512A31.363 31.363 0 0 1 288 368l224 128 222.075-128a31.363 31.363 0 0 1 43.525 11.525 31.988 31.988 0 0 1-11.525 43.513L544 551.038z m0 0,M64 256v512l448 256 448-256V256L512 0z m832 480L512 960 128 736V288L512 64l384 224z m0 0'
    },
    {
        x: 900,
        y: 600,
        nodeName: 'topic_name_3',
        svgPath:
            'M544 552.325V800a32 32 0 0 1-32 32 31.375 31.375 0 0 1-32-32V552.325L256 423.037a32 32 0 0 1-11.525-43.512A31.363 31.363 0 0 1 288 368l224 128 222.075-128a31.363 31.363 0 0 1 43.525 11.525 31.988 31.988 0 0 1-11.525 43.513L544 551.038z m0 0,M64 256v512l448 256 448-256V256L512 0z m832 480L512 960 128 736V288L512 64l384 224z m0 0'
    },
    {
        x: 500,
        y: 300,
        nodeName: '127.0.0.1:9092',
        svgPath:
            'M1.333 2.667C1.333 1.194 4.318 0 8 0s6.667 1.194 6.667 2.667V4c0 1.473-2.985 2.667-6.667 2.667S1.333 5.473 1.333 4V2.667z,M1.333 6.334v3C1.333 10.805 4.318 12 8 12s6.667-1.194 6.667-2.667V6.334a6.51 6.51 0 0 1-1.458.79C11.81 7.684 9.967 8 8 8c-1.966 0-3.809-.317-5.208-.876a6.508 6.508 0 0 1-1.458-.79z,M14.667 11.668a6.51 6.51 0 0 1-1.458.789c-1.4.56-3.242.876-5.21.876-1.966 0-3.809-.316-5.208-.876a6.51 6.51 0 0 1-1.458-.79v1.666C1.333 14.806 4.318 16 8 16s6.667-1.194 6.667-2.667v-1.665z'
    }
];
var charts = {
    nodes: [],
    linesData: [
        {
            coords: [
                [500, 1000],
                [500, 800]
            ]
        },
        {
            coords: [
                [500, 800],
                [100, 800],
                [100, 600]
            ]
        },
        {
            coords: [
                [500, 800],
                [500, 600]
            ]
        },
        {
            coords: [
                [500, 800],
                [900, 800],
                [900, 600]
            ]
        },
        {
            coords: [
                [100, 600],
                [500, 300]
            ]
        },
        {
            coords: [
                [500, 600],
                [500, 300]
            ]
        },
        {
            coords: [
                [900, 600],
                [500, 300]
            ]
        }
    ]
};
for (var j = 0; j < nodes.length; j++) {
    const { x, y, nodeName, svgPath, symbolSize } = nodes[j];
    var node = {
        nodeName,
        value: [x, y],
        symbolSize: symbolSize || 50,
        symbol: 'path://' + svgPath,
        itemStyle: {
            color: '#6571ff'
        }
    };
    charts.nodes.push(node);
}

option = {
    backgroundColor: '#0B1321',
    xAxis: {
        min: 0,
        max: 1000,
        show: false,
        type: 'value'
    },
    yAxis: {
        min: 0,
        max: 1000,
        show: false,
        type: 'value'
    },
    series: [
        {
            type: 'graph',
            coordinateSystem: 'cartesian2d',
            label: {
                show: true,
                position: 'bottom',
                color: '#6571ff',
                formatter: function (item) {
                    return item.data.nodeName;
                }
            },
            data: charts.nodes
        },
        {
            type: 'lines',
            polyline: true,
            coordinateSystem: 'cartesian2d',
            lineStyle: {
                type: 'dashed',
                width: 2,
                color: '#6571ff',
                curveness: 0.3
            },
            effect: {
                show: true,
                trailLength: 0.1,
                symbol: 'arrow',
                color: '#6571ff',
                symbolSize: 8
            },
            data: charts.linesData
        }
    ]
};

option && myChart.setOption(option);


// get consumer groups summary
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/consumer/summary/groups/ajax',
        success: function (datas) {
            $("#efka_consumer_group_total").text(datas.total_group_size);
            $("#efka_consumer_group_active").text(datas.active_group_size);
        }
    });
}catch (e){
    console.log(e);
}