$(document).ready(function() {

	// Get producer and consumer rate data
	getRealProducerAndConsumerRate();

	// Get topic total logsize
	getTopicTotalLogSize();

	function getRealProducerAndConsumerRate() {
		try {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/bs/producer/consumer/realrate/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#ke_bs_producer_rate").text(datas.producer);
						$("#ke_bs_consumer_rate").text(datas.consumer);
					}
				}
			});
		} catch (e) {
			console.log(e.message);
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

	toDayConsumerProducerOption = {
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
			data : [ 'Producer', 'Consumer' ],
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
			data : [ '01', '02', '03', '04', '05', '06', '07', '08', '09', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24' ]
		}, {
			axisPointer : {
				show : false
			},
			axisLine : {
				show : false
			},
			position : 'bottom',
			offset : 20,

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

		}, {
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

	// Get today consumer and producer
	getTodayConsumerAndProducer();

	function getTodayConsumerAndProducer() {
		var ke_bs_today_consumer_producer = echarts.init(document.getElementById('ke_bs_today_consumer_producer'));
		ke_bs_today_consumer_producer.setOption(toDayConsumerProducerOption);
		window.addEventListener("resize", function() {
			ke_bs_today_consumer_producer.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/today/day/consuer/producer/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toDayConsumerProducerOption.series[0].data = datas.producers;
					toDayConsumerProducerOption.series[1].data = datas.consumers;
					ke_bs_today_consumer_producer.setOption(toDayConsumerProducerOption);
					datas = null;
				}
			}
		});
	}

	toHistoryConsumerProducerOption = {
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
			data : [ 'Producer', 'Consumer' ],
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
			offset : 20,
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

		}, {
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

	// Get today consumer and producer
	getHistoryConsumerAndProducer();

	function getHistoryConsumerAndProducer() {
		var ke_bs_history_consumer_producer = echarts.init(document.getElementById('ke_bs_history_consumer_producer'));
		ke_bs_history_consumer_producer.setOption(toHistoryConsumerProducerOption);
		window.addEventListener("resize", function() {
			ke_bs_history_consumer_producer.resize();
		});

		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/bs/history/day/consuer/producer/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toHistoryConsumerProducerOption.xAxis[0].data = datas.xAxis;
					toHistoryConsumerProducerOption.series[0].data = datas.producers;
					toHistoryConsumerProducerOption.series[1].data = datas.consumers;
					ke_bs_history_consumer_producer.setOption(toHistoryConsumerProducerOption);
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
			data : [ '01', '02', '03', '04', '05', '06', '07', '08', '09', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24' ]
		}, {
			axisPointer : {
				show : false
			},
			axisLine : {
				show : false
			},
			position : 'bottom',
			offset : 20,

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
			url : '/ke/bs/lag/day/consuer/producer/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					toDayLagOption.series[0].data = datas.lags;
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
		getTodayConsumerAndProducer();
		getHistoryConsumerAndProducer();
		getTodayLag();
	}, 1000 * 60);
});