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
		var type = "daily";

		zkRealtime(stime, etime, type);
		$(".ranges").find("li[data-range-key='Custom Range']").remove();

		reportrange.on('apply.daterangepicker', function(ev, picker) {
			stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
			etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
			if (picker.chosenLabel == "Today" || picker.chosenLabel == "Yesterday") {
				type = "daily";
			} else {
				type = "day";
			}
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
		xkey : 'xkey',
		ykeys : [ 'ZKSendPackets' ],
		labels : [ 'ZKSendPackets' ],
		// lineColors : [ '#7cb47c' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_recevied_packets = Morris.Line({
		element : 'zk_recevied_packets',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ZKReceivedPackets' ],
		labels : [ 'ZKReceivedPackets' ],
		// lineColors : [ '#d43f3a' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_avg_latency = Morris.Line({
		element : 'zk_avg_latency',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ZKAvgLatency' ],
		labels : [ 'ZKAvgLatency' ],
		// lineColors : [ '#a7b3bc' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_alives_connections = Morris.Line({
		element : 'zk_alives_connections',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ZKNumAliveConnections' ],
		labels : [ 'ZKNumAliveConnections' ],
		// lineColors : [ '#a7b3bc' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_queue_requests = Morris.Line({
		element : 'zk_queue_requests',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ZKOutstandingRequests' ],
		labels : [ 'ZKOutstandingRequests' ],
		// lineColors : [ '#a7b3bc' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var zk_openfile_counts = Morris.Line({
		element : 'zk_openfile_counts',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ZKOpenFileDescriptorCount' ],
		labels : [ 'ZKOpenFileDescriptorCount' ],
		// lineColors : [ '#a7b3bc' ],
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
					console.log(datas);
					zk_send_packets.setData(datas.send);
					zk_recevied_packets.setData(datas.received);
					zk_avg_latency.setData(datas.avg);
					zk_alives_connections.setData(datas.alive);
					zk_queue_requests.setData(datas.queue);
					zk_openfile_counts.setData(datas.openfile);
					datas = null;
				}
			}
		});
	}

});