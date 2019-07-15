$(document).ready(function() {

	chartCommonOption = {
		backgroundColor : "#fff",
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				type : 'cross',
				label : {
					backgroundColor : '#6a7985'
				}
			}
		},
		legend : {
			data : []
		},
		xAxis : {
			type : 'category',
			boundaryGap : false,
			data : []
		},
		dataZoom : {
			show : true,
			start : 30
		},
		grid : {
			bottom : "70px",
			left : "90px",
			right : "90px"
		},
		yAxis : {
			type : 'value'
		},
		series : {
			type : 'line',
			symbol : "none",
			// name : "",
			smooth : true,
			areaStyle : {
				opacity : 0.1
			},
			data : []
		}
	};

	try {

		var start = moment();
		var end = moment();

		function cb(start, end) {
			$('#reportrange span').html(start.format('YYYY-MM-DD') + ' To ' + end.format('YYYY-MM-DD'));
		}

		var reportrange = $('#reportrange').daterangepicker({
			startDate : start,
			endDate : end,
			ranges : {
				'Today' : [ moment(), moment() ],
				'Yesterday' : [ moment().subtract(1, 'days'), moment().subtract(1, 'days') ],
				'Lastest 3 days' : [ moment().subtract(3, 'days'), moment() ],
				'Lastest 7 days' : [ moment().subtract(6, 'days'), moment() ]
			}
		}, cb);

		cb(start, end);
		var stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		var etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		var type = "zookeeper";

		zkRealtime(stime, etime, type);

		reportrange.on('apply.daterangepicker', function(ev, picker) {
			stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
			etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
			zkRealtime(stime, etime, type);
		});
		setInterval(function() {
			zkRealtime(stime, etime, type)
		}, 1000 * 60 * 1);
	} catch (e) {
		console.log(e.message);
	}

	function morrisLineInit(elment) {
		lagChart = echarts.init(document.getElementById(elment), 'macarons');
		lagChart.setOption(chartCommonOption);
		return lagChart;
	}

	var zk_packets_sent = morrisLineInit('zk_send_packets');
	var zk_packets_received = morrisLineInit('zk_recevied_packets');
	var zk_num_alive_connections = morrisLineInit('zk_alives_connections');
	var zk_outstanding_requests = morrisLineInit('zk_queue_requests');

	function zkRealtime(stime, etime, type) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/metrics/trend/mbean/ajax?stime=' + stime + '&etime=' + etime + '&type=' + type,
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
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
		chartCommonOption.xAxis.data = filter(data, filed).x;
		chartCommonOption.series.data = filter(data, filed).y;
		mbean.setOption(chartCommonOption);
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
			break;
		case "zk_num_alive_connections":
			for (var i = 0; i < datas.alive.length; i++) {
				datax.push(datas.alive[i].x);
				datay.push(datas.alive[i].y);
			}
			break;
		case "zk_outstanding_requests":
			for (var i = 0; i < datas.queue.length; i++) {
				datax.push(datas.queue[i].x);
				datay.push(datas.queue[i].y);
			}
			break;
		case "zk_packets_received":
			for (var i = 0; i < datas.received.length; i++) {
				datax.push(datas.received[i].x);
				datay.push(datas.received[i].y);
			}
			break;
		default:
			break;
		}
		data.x = datax;
		data.y = datay;
		return data;
	}
});