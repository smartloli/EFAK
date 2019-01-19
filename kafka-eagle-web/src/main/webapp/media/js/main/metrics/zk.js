$(document).ready(function() {
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
		// $(".ranges").find("li[data-range-key='Custom Range']").remove();

		reportrange.on('apply.daterangepicker', function(ev, picker) {
			stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
			etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
			zkRealtime(stime, etime, type);
		});
		console.log(stime + "," + etime);
		setInterval(function() {
			zkRealtime(stime, etime, type)
		}, 1000 * 60 * 5);
	} catch (e) {
		console.log(e.message);
	}

	var zk_send_packets = Morris.Line({
		element : 'zk_send_packets',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		pointSize : 2,
		hideHover : 'auto',
		//pointFillColors : [ '#ffffff' ],
		//pointStrokeColors : [ 'black' ],
		resize : true
	});

	var zk_recevied_packets = Morris.Line({
		element : 'zk_recevied_packets',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		//pointFillColors : [ '#ffffff' ],
		//pointStrokeColors : [ 'black' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_alives_connections = Morris.Line({
		element : 'zk_alives_connections',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		//pointFillColors : [ '#ffffff' ],
		//pointStrokeColors : [ 'black' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_queue_requests = Morris.Line({
		element : 'zk_queue_requests',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		//pointFillColors : [ '#ffffff' ],
		//pointStrokeColors : [ 'black' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

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
					zk_send_packets.options.ykeys = datas.zks;
					zk_send_packets.options.labels = datas.zks;
					zk_send_packets.setData(filter(datas.send, "zk_packets_sent"));

					zk_recevied_packets.options.ykeys = datas.zks;
					zk_recevied_packets.options.labels = datas.zks;
					zk_recevied_packets.setData(filter(datas.received, "zk_packets_received"));

					zk_alives_connections.options.ykeys = datas.zks;
					zk_alives_connections.options.labels = datas.zks;
					zk_alives_connections.setData(filter(datas.alive, "zk_num_alive_connections"));

					zk_queue_requests.options.ykeys = datas.zks;
					zk_queue_requests.options.labels = datas.zks;
					zk_queue_requests.setData(filter(datas.queue, "zk_outstanding_requests"));
					datas = null;
				}
			}
		});
	}

	function filter(datas, type) {
		var data = new Array();
		for (var i = 0; i < datas.length; i++) {
			switch (type) {
			case "zk_packets_sent":
				data.push(JSON.parse(datas[i].zk_packets_sent));
				break;
			case "zk_num_alive_connections":
				data.push(JSON.parse(datas[i].zk_num_alive_connections));
				break;
			case "zk_outstanding_requests":
				data.push(JSON.parse(datas[i].zk_outstanding_requests));
				break;
			case "zk_packets_received":
				data.push(JSON.parse(datas[i].zk_packets_received));
				break;
			default:
				break;
			}
		}
		return data;
	}

});