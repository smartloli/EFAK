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
			left : "40px",
			right : "40px"
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

	initModuleVisualAndBingding();

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

	function mbeanRealtime(stime, etime, type, modules) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/metrics/trend/mbean/ajax?stime=' + stime + '&etime=' + etime + '&type=' + type + '&modules=' + modules,
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					setTrendData(mbean_msg_in, 'message_in', datas);
					setTrendData(mbean_msg_byte_in, 'byte_in', datas);
					setTrendData(mbean_msg_byte_out, 'byte_out', datas);
					setTrendData(mbean_byte_rejected, 'byteRejected', datas);
					setTrendData(mbean_failed_fetch_request, 'failed_fetch_request', datas);
					setTrendData(mbean_failed_produce_request, 'failed_produce_request', datas);
					setTrendData(mbean_produce_message_conversions, 'produce_message_conversions', datas);
					setTrendData(mbean_total_fetch_requests, 'total_fetch_requests', datas);
					setTrendData(mbean_total_produce_requests, 'total_produce_requests', datas);
					setTrendData(mbean_replication_bytes_out, 'replication_bytes_out', datas);
					setTrendData(mbean_replication_bytes_in, 'replication_bytes_in', datas);
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
	var type = "kafka";

	mbeanRealtime(stime, etime, type, getCheckedModules());

	reportrange.on('apply.daterangepicker', function(ev, picker) {
		stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		mbeanRealtime(stime, etime, type, getCheckedModules());
	});
	setInterval(function() {
		mbeanRealtime(stime, etime, type, getCheckedModules())
	}, 1000 * 60 * 1);

	function morrisLineInit(elment) {
		lagChart = echarts.init(document.getElementById(elment), 'macarons');
		lagChart.setOption(chartCommonOption);
		return lagChart;
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
		$('.checkbox').find('input[type="checkbox"]:checked').each(function() {
			modules += ($(this).attr('name')) + ',';
		});
		return modules.substring(0, modules.length - 1);
	}

	// init module show or hide & bind change event
	function initModuleVisualAndBingding() {
		$('.checkbox').find('input[type="checkbox"]').each(function() {
			var that = this;
			if ($(that).is(':checked')) {
				module('#' + $(that).attr('name'), true);
			} else {
				module('#' + $(that).attr('name'), false);
			}
			$(that).click(function() {
				if ($(that).is(':checked')) {
					module('#' + $(that).attr('name'), true);
					stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
					etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
					mbeanRealtime(stime, etime, type, getCheckedModules());
					$('svg').css('width', '100%');
					return;
				}
				module('#' + $(that).attr('name'), false);
			});
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
		case "message_in":
			for (var i = 0; i < datas.messageIns.length; i++) {
				datax.push(datas.messageIns[i].x);
				datay.push(datas.messageIns[i].y);
			}
			break;
		case "byte_in":
			for (var i = 0; i < datas.byteIns.length; i++) {
				datax.push(datas.byteIns[i].x);
				datay.push(datas.byteIns[i].y);
			}
			break;
		case "byte_out":
			for (var i = 0; i < datas.byteOuts.length; i++) {
				datax.push(datas.byteOuts[i].x);
				datay.push(datas.byteOuts[i].y);
			}
			break;
		case "byte_rejected":
			for (var i = 0; i < datas.byteRejected.length; i++) {
				datax.push(datas.byteRejected[i].x);
				datay.push(datas.byteRejected[i].y);
			}
			break;
		case "failed_fetch_request":
			for (var i = 0; i < datas.failedFetchRequest.length; i++) {
				datax.push(datas.failedFetchRequest[i].x);
				datay.push(datas.failedFetchRequest[i].y);
			}
			break;
		case "failed_produce_request":
			for (var i = 0; i < datas.failedProduceRequest.length; i++) {
				datax.push(datas.failedProduceRequest[i].x);
				datay.push(datas.failedProduceRequest[i].y);
			}
			break;
		case "produce_message_conversions":
			for (var i = 0; i < datas.produceMessageConversions.length; i++) {
				datax.push(datas.produceMessageConversions[i].x);
				datay.push(datas.produceMessageConversions[i].y);
			}
			break;
		case "total_fetch_requests":
			for (var i = 0; i < datas.totalFetchRequests.length; i++) {
				datax.push(datas.totalFetchRequests[i].x);
				datay.push(datas.totalFetchRequests[i].y);
			}
			break;
		case "total_produce_requests":
			for (var i = 0; i < datas.totalProduceRequests.length; i++) {
				datax.push(datas.totalProduceRequests[i].x);
				datay.push(datas.totalProduceRequests[i].y);
			}
			break;
		case "replication_bytes_out":
			for (var i = 0; i < datas.replicationBytesOuts.length; i++) {
				datax.push(datas.replicationBytesOuts[i].x);
				datay.push(datas.replicationBytesOuts[i].y);
			}
			break;
		case "replication_bytes_in":
			for (var i = 0; i < datas.replicationBytesIns.length; i++) {
				datax.push(datas.replicationBytesIns[i].x);
				datay.push(datas.replicationBytesIns[i].y);
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