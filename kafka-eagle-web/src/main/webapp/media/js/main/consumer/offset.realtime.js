$(document).ready(function() {

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

	lagOption = {
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
			data : [ 'Lag' ]
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
			name : "Lag",
			smooth : true,
			areaStyle : {
				opacity : 0.1
			},
			data : []
		}
	};

	producerOption = {
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
			data : [ 'Producer' ]
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
			name : "Producer",
			smooth : true,
			areaStyle : {
				opacity : 0.1
			},
			data : []
		}
	};

	consumerOption = {
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
			data : [ 'Consumer' ]
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
			name : "Consumer",
			smooth : true,
			areaStyle : {
				opacity : 0.1
			},
			data : []
		}
	};

	var lagChart = echarts.init(document.getElementById('lag_chart'), 'macarons');
	var producerChart = echarts.init(document.getElementById('producer_chart'), 'macarons');
	var consumerChart = echarts.init(document.getElementById('consumer_chart'), 'macarons');
	lagChart.setOption(lagOption);
	producerChart.setOption(producerOption);
	consumerChart.setOption(consumerOption);

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

	function offserRealtime(stime, etime) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/consumer/offset/group/topic/realtime/ajax?group=' + group + '&topic=' + topic + '&stime=' + stime + '&etime=' + etime,
			success : function(datas) {
				if (datas != null) {
					// Area Chart
					console.log(datas);
					lagOption.xAxis.data = datas.lag.x;
					lagOption.series.data = datas.lag.y;
					lagChart.setOption(lagOption);
					producerOption.xAxis.data = datas.producer.x;
					producerOption.series.data = datas.producer.y;
					producerChart.setOption(producerOption);
					consumerOption.xAxis.data = datas.consumer.x;
					consumerOption.series.data = datas.consumer.y;
					consumerChart.setOption(consumerOption);
					datas = [];
				}
			}
		});
	}

	function offserRateRealtime() {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/consumer/offset/rate/group/topic/realtime/ajax?group=' + group + '&topic=' + topic,
			success : function(datas) {
				if (datas != null) {
					// Consumer & Producer Rate
					$("#producer_rate").text(datas.ins);
					$("#consumer_rate").text(datas.outs);
					datas = [];
				}
			}
		});
	}

	reportrange.on('apply.daterangepicker', function(ev, picker) {
		stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		offserRealtime(stime, etime);
	});

	offserRealtime(stime, etime);
	offserRateRealtime();
	setInterval(function() {
		offserRealtime(stime, etime)
	}, 1000 * 60 * 1);
	setInterval(offserRateRealtime, 1000 * 60 * 1);
});