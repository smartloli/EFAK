$(document).ready(function() {

	// defined byte size
	var KB_IN_BYTES = 1024;
	var MB_IN_BYTES = 1024 * KB_IN_BYTES;
	var GB_IN_BYTES = 1024 * MB_IN_BYTES;
	var TB_IN_BYTES = 1024 * GB_IN_BYTES;

	// Get producer and consumer rate data
	getRealProducerAndConsumerRate();

	// Get topic total logsize
	getTopicTotalLogSize();

	function getRealProducerAndConsumerRate() {
		try {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/bs/brokers/ins/outs/realrate/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#ke_bs_ins_rate").text(stringify(datas.ins).value);
						$("#ke_bs_ins_rate_name").text("ByteIn" + stringify(datas.ins).type);
						$("#ke_bs_outs_rate").text(stringify(datas.outs).value);
						$("#ke_bs_outs_rate_name").text("ByteOut" + stringify(datas.outs).type);
					}
				}
			});
		} catch (e) {
			console.log(e.message);
		}
	}

	// Get topic total capacity
	getTopicTotalCapacity();

	function getTopicTotalCapacity() {
		try {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/bs/topic/total/capacity/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#ke_topics_total_capacity_unit").text("Topic Total Capacity (" + datas.type + ")");
						$("#ke_topics_total_capacity").text(datas.size);
					}
				}
			});
		} catch (e) {
			console.log(e.message);
		}
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

	function getTopicTotalLogSize() {
		try {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/bs/topic/total/logsize/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#ke_topics_total_logsize").text(datas.total);
					}
				}
			});
		} catch (e) {
			console.log(e.message);
		}
	}

	producerHistoryOption = {
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				type : 'shadow'
			}
		},
		grid : {
			left : '0%',
			top : '10px',
			right : '0%',
			bottom : '4%',
			containLabel : true
		},
		xAxis : {
			type : 'category',
			data : [],
			axisLine : {
				show : true,
				lineStyle : {
					color : "rgba(255,255,255,.1)",
					width : 1,
					type : "solid"
				},
			},
			axisTick : {
				show : false,
			},
			axisLabel : {
				interval : 0,
				show : true,
				splitNumber : 15,
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : '12',
				},
			},
		},
		yAxis : [ {
			type : 'value',
			axisLabel : {
				show : true,
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : '12',
				},
			},
			axisTick : {
				show : false,
			},
			axisLine : {
				show : true,
				lineStyle : {
					color : "rgba(255,255,255,.1	)",
					width : 1,
					type : "solid"
				},
			},
			splitLine : {
				lineStyle : {
					color : "rgba(255,255,255,.1)",
				}
			}
		} ],
		series : {
			type : 'bar',
			data : [],
			barWidth : '35%',
			itemStyle : {
				normal : {
					color : '#2f89cf',
					opacity : 1,
					barBorderRadius : 5,
				}
			}
		}
	};

	proudcerHistory();

	function proudcerHistory() {
		var ke_bs_producer_history = echarts.init(document.getElementById('ke_bs_producer_history'));

		ke_bs_producer_history.setOption(producerHistoryOption);
		window.addEventListener("resize", function() {
			ke_bs_producer_history.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/producer/history/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					producerHistoryOption.xAxis.data = filter(datas).x;
					producerHistoryOption.series.data = filter(datas).y;
					ke_bs_producer_history.setOption(producerHistoryOption);
					datas = null;
				}
			}
		});
	}

	function filter(datas) {
		var data = new Object();
		var datax = new Array();
		var datay = new Array();
		for (var i = 0; i < datas.length; i++) {
			datax.push(datas[i].x);
			datay.push(datas[i].y);
		}
		data.x = datax;
		data.y = datay;
		return data;
	}

	consumerHistoryOption = {
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				type : 'shadow'
			}
		},
		grid : {
			left : '0%',
			top : '10px',
			right : '0%',
			bottom : '4%',
			containLabel : true
		},
		xAxis : {
			type : 'category',
			data : [],
			axisLine : {
				show : true,
				lineStyle : {
					color : "rgba(255,255,255,.1)",
					width : 1,
					type : "solid"
				},
			},
			axisTick : {
				show : false,
			},
			axisLabel : {
				interval : 0,
				show : true,
				splitNumber : 15,
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : '12',
				},
			},
		},
		yAxis : [ {
			type : 'value',
			axisLabel : {
				show : true,
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : '12',
				},
			},
			axisTick : {
				show : false,
			},
			axisLine : {
				show : true,
				lineStyle : {
					color : "rgba(255,255,255,.1	)",
					width : 1,
					type : "solid"
				},
			},
			splitLine : {
				lineStyle : {
					color : "rgba(255,255,255,.1)",
				}
			}
		} ],
		series : {
			type : 'bar',
			data : [],
			barWidth : '35%',
			itemStyle : {
				normal : {
					color : '#27d08a',
					opacity : 1,
					barBorderRadius : 5,
				}
			}
		}
	};

	consumerHistory();

	function consumerHistory() {
		var ke_bs_consumer_history = echarts.init(document.getElementById('ke_bs_consumer_history'));
		ke_bs_consumer_history.setOption(consumerHistoryOption);
		window.addEventListener("resize", function() {
			ke_bs_consumer_history.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/consumer/history/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					consumerHistoryOption.xAxis.data = filter(datas).x;
					consumerHistoryOption.series.data = filter(datas).y;
					ke_bs_consumer_history.setOption(consumerHistoryOption);
					datas = null;
				}
			}
		});
	}

	toDayProducerOption = {
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				lineStyle : {
					color : '#dddc6b'
				}
			}
		},
		legend : {
			top : '0%',
			data : [ 'Producer' ],
			textStyle : {
				color : 'rgba(255,255,255,.5)',
				fontSize : '12',
			}
		},
		grid : {
			left : '10',
			top : '30',
			right : '10',
			bottom : '10',
			containLabel : true
		},
		xAxis : [ {
			type : 'category',
			boundaryGap : false,
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.2)'
				}
			},
			data : []
		}, {
			axisPointer : {
				show : false
			},
			axisLine : {
				show : false
			},
			position : 'bottom',
			offset : 20
		} ],
		yAxis : [ {
			type : 'value',
			axisTick : {
				show : false
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			},
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			splitLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			}
		} ],
		series : [ {
			name : 'Producer',
			type : 'line',
			smooth : true,
			symbol : 'circle',
			symbolSize : 5,
			showSymbol : false,
			lineStyle : {
				normal : {
					color : '#0184d5',
					width : 2
				}
			},
			areaStyle : {
				normal : {
					color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
						offset : 0,
						color : 'rgba(1, 132, 213, 0.4)'
					}, {
						offset : 0.8,
						color : 'rgba(1, 132, 213, 0.1)'
					} ], false),
					shadowColor : 'rgba(0, 0, 0, 0.1)',
				}
			},
			itemStyle : {
				normal : {
					color : '#0184d5',
					borderColor : 'rgba(221, 220, 107, .1)',
					borderWidth : 12
				}
			},
			data : []
		} ]
	};

	// Get today producer
	getTodayProducer();

	function getTodayProducer() {
		var ke_bs_today_producer = echarts.init(document.getElementById('ke_bs_today_producer'));
		ke_bs_today_producer.setOption(toDayProducerOption);
		window.addEventListener("resize", function() {
			ke_bs_today_producer.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/producers/day/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toDayProducerOption.xAxis[0].data = filter(datas).x;
					toDayProducerOption.series[0].data = filter(datas).y;
					ke_bs_today_producer.setOption(toDayProducerOption);
					datas = null;
				}
			}
		});
	}

	toDayConsumerOption = {
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				lineStyle : {
					color : '#dddc6b'
				}
			}
		},
		legend : {
			top : '0%',
			data : [ 'Consumer' ],
			textStyle : {
				color : 'rgba(255,255,255,.5)',
				fontSize : '12',
			}
		},
		grid : {
			left : '10',
			top : '30',
			right : '10',
			bottom : '10',
			containLabel : true
		},
		xAxis : [ {
			type : 'category',
			boundaryGap : false,
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.2)'
				}

			},
			data : []
		}, {
			axisPointer : {
				show : false
			},
			axisLine : {
				show : false
			},
			position : 'bottom',
			offset : 20
		} ],
		yAxis : [ {
			type : 'value',
			axisTick : {
				show : false
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			},
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			splitLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			}
		} ],
		series : [ {
			name : 'Consumer',
			type : 'line',
			smooth : true,
			symbol : 'circle',
			symbolSize : 5,
			showSymbol : false,
			lineStyle : {
				normal : {
					color : '#00d887',
					width : 2
				}
			},
			areaStyle : {
				normal : {
					color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
						offset : 0,
						color : 'rgba(0, 216, 135, 0.4)'
					}, {
						offset : 0.8,
						color : 'rgba(0, 216, 135, 0.1)'
					} ], false),
					shadowColor : 'rgba(0, 0, 0, 0.1)',
				}
			},
			itemStyle : {
				normal : {
					color : '#00d887',
					borderColor : 'rgba(221, 220, 107, .1)',
					borderWidth : 12
				}
			},
			data : []
		} ]
	};

	// Get today consumer
	getTodayConsumer();

	function getTodayConsumer() {
		var ke_bs_today_consumer = echarts.init(document.getElementById('ke_bs_today_consumer'));
		ke_bs_today_consumer.setOption(toDayConsumerOption);
		window.addEventListener("resize", function() {
			ke_bs_today_consumer.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/consumers/day/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toDayConsumerOption.xAxis[0].data = filter(datas).x;
					toDayConsumerOption.series[0].data = filter(datas).y;
					ke_bs_today_consumer.setOption(toDayConsumerOption);
					datas = null;
				}
			}
		});
	}

	toDayLagOption = {
		tooltip : {
			trigger : 'axis',
			axisPointer : {
				lineStyle : {
					color : '#dddc6b'
				}
			}
		},
		legend : {
			top : '0%',
			data : [ 'Lag' ],
			textStyle : {
				color : 'rgba(255,255,255,.5)',
				fontSize : '12',
			}
		},
		grid : {
			left : '10',
			top : '30',
			right : '10',
			bottom : '10',
			containLabel : true
		},
		xAxis : [ {
			type : 'category',
			boundaryGap : false,
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.2)'
				}
			},
			data : []
		}, {
			axisPointer : {
				show : false
			},
			axisLine : {
				show : false
			},
			position : 'bottom',
			offset : 20
		} ],
		yAxis : [ {
			type : 'value',
			axisTick : {
				show : false
			},
			axisLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			},
			axisLabel : {
				textStyle : {
					color : "rgba(255,255,255,.6)",
					fontSize : 12,
				},
			},
			splitLine : {
				lineStyle : {
					color : 'rgba(255,255,255,.1)'
				}
			}
		} ],
		series : [ {
			name : 'Lag',
			type : 'line',
			smooth : true,
			symbol : 'circle',
			symbolSize : 5,
			showSymbol : false,
			lineStyle : {
				normal : {
					color : '#d9534f',
					width : 2
				}
			},
			areaStyle : {
				normal : {
					color : new echarts.graphic.LinearGradient(0, 0, 0, 1, [ {
						offset : 0,
						color : 'rgba(1, 132, 213, 0.4)'
					}, {
						offset : 0.8,
						color : 'rgba(1, 132, 213, 0.1)'
					} ], false),
					shadowColor : 'rgba(0, 0, 0, 0.1)',
				}
			},
			itemStyle : {
				normal : {
					color : '#d9534f',
					borderColor : 'rgba(221, 220, 107, .1)',
					borderWidth : 12
				}
			},
			data : []
		} ]
	};

	// Get today consumer and producer
	getTodayLag();

	function getTodayLag() {
		var ke_bs_today_lag = echarts.init(document.getElementById('ke_bs_today_lag'));
		ke_bs_today_lag.setOption(toDayLagOption);
		window.addEventListener("resize", function() {
			ke_bs_today_lag.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/lag/day/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toDayLagOption.xAxis[0].data = filter(datas).x;
					toDayLagOption.series[0].data = filter(datas).y;
					ke_bs_today_lag.setOption(toDayLagOption);
					datas = null;
				}
			}
		});
	}

	setInterval(function() {
		getRealProducerAndConsumerRate();
		getTopicTotalLogSize();
		proudcerHistory();
		consumerHistory();
		getTodayProducer();
		getTodayConsumer();
		getTodayLag();
	}, 1000 * 60);
});