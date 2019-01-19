$(document).ready(function() {
	var url = window.location.href;
	var tmp = url.split("offset/")[1];
	var group = tmp.split("/")[0];
	var topic = tmp.split("/")[1];
	$("#topic_name_header").find("strong").text(topic);

	var graph = Morris.Area({
		element : 'morris-area-chart',
		data : [],
		xkey : 'y',
		ykeys : [ 'lag' ],
		labels : [ 'lag' ],
		// lineColors : [ '#d43f3a', '#7cb47c', '#2577b5' ],
		pointSize : 1,
		hideHover : 'auto',
		behaveLikeLine : true,
		resize : true
	});

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
			url : '/ke/consumer/offset/' + group + '/' + topic + '/realtime/ajax?stime=' + stime + '&etime=' + etime,
			success : function(datas) {
				if (datas != null) {
					// Area Chart
					graph.setData(filter(datas.graph));
					datas = [];
				}
			}
		});
	}

	function offserRateRealtime() {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/consumer/offset/rate/' + topic + '/realtime/ajax',
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

	function filter(datas) {
		var data = new Array();
		for (var i = 0; i < datas.length; i++) {
			data.push(JSON.parse(datas[i].lag));
		}
		return data;
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