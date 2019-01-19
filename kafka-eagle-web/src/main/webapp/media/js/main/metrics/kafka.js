$(document).ready(function() {

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
					console.log(datas);
					setTrendData(mbean_msg_in, 'message_in', datas.zks, datas.msg);
					setTrendData(mbean_msg_byte_in, 'byte_in', datas.zks, datas.ins);
					setTrendData(mbean_msg_byte_out, 'byte_out', datas.zks, datas.outs);
					setTrendData(mbean_byte_rejected, 'byteRejected', datas.zks, datas.byteRejected);
					setTrendData(mbean_failed_fetch_request, 'failed_fetch_request', datas.zks, datas.failedFetchRequest);
					setTrendData(mbean_failed_produce_request, 'failed_produce_request', datas.zks, datas.failedProduceRequest);
					setTrendData(mbean_produce_message_conversions, 'produce_message_conversions', datas.zks, datas.produceMessageConversions);
					setTrendData(mbean_total_fetch_requests, 'total_fetch_requests', datas.zks, datas.totalFetchRequests);
					setTrendData(mbean_total_produce_requests, 'total_produce_requests', datas.zks, datas.totalProduceRequests);
					setTrendData(mbean_replication_bytes_out, 'replication_bytes_out', datas.zks, datas.replicationBytesOuts);
					setTrendData(mbean_replication_bytes_in, 'replication_bytes_in', datas.zks, datas.replicationBytesIns);

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
	// $(".ranges").find("li[data-range-key='Custom Range']").remove();

	reportrange.on('apply.daterangepicker', function(ev, picker) {
		stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		mbeanRealtime(stime, etime, type, getCheckedModules());
	});
	setInterval(function() {
		mbeanRealtime(stime, etime, type, getCheckedModules())
	}, 1000 * 60 * 5);

	function morrisLineInit(elment) {
		return Morris.Line({
			element : elment,
			data : [],
			xkey : 'y',
			ykeys : [],
			labels : [],
			pointSize : 1,
			hideHover : 'auto',
			pointFillColors : [ '#ffffff' ],
			pointStrokeColors : [ '#C0C0C0' ],
			width : 'auto'
		// resize : true
		});
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
	function setTrendData(mbean, filed, zks, data) {
		mbean.options.ykeys = zks;
		mbean.options.labels = zks;
		mbean.setData(filter(data, filed));
	}

	// filter data
	function filter(datas, type) {
		var data = new Array();
		for (var i = 0; i < datas.length; i++) {
			switch (type) {
			case "message_in":
				data.push(JSON.parse(datas[i].message_in));
				break;
			case "byte_in":
				data.push(JSON.parse(datas[i].byte_in));
				break;
			case "byte_out":
				data.push(JSON.parse(datas[i].byte_out));
				break;
			case "byte_rejected":
				data.push(JSON.parse(datas[i].byte_rejected));
				break;
			case "failed_fetch_request":
				data.push(JSON.parse(datas[i].failed_fetch_request));
				break;
			case "failed_produce_request":
				data.push(JSON.parse(datas[i].failed_produce_request));
				break;
			case "produce_message_conversions":
				data.push(JSON.parse(datas[i].produce_message_conversions));
				break;
			case "total_fetch_requests":
				data.push(JSON.parse(datas[i].total_fetch_requests));
				break;
			case "total_produce_requests":
				data.push(JSON.parse(datas[i].total_produce_requests));
				break;
			case "replication_bytes_out":
				data.push(JSON.parse(datas[i].replication_bytes_out));
				break;
			case "replication_bytes_in":
				data.push(JSON.parse(datas[i].replication_bytes_in));
				break;
			default:
				break;
			}
		}
		return data;
	}
});