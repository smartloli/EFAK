$(document).ready(function() {
	var mbean_msg_in = Morris.Line({
		element : 'mbean_msg_in',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		pointSize : 2,
		hideHover : 'auto',
		pointFillColors : [ '#ffffff' ],
		pointStrokeColors : [ 'black' ],
		resize : true
	});

	var mbean_msg_byte_in = Morris.Line({
		element : 'mbean_msg_byte_in',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		pointSize : 2,
		hideHover : 'auto',
		pointFillColors : [ '#ffffff' ],
		pointStrokeColors : [ 'black' ],
		resize : true
	});

	var mbean_msg_byte_out = Morris.Line({
		element : 'mbean_msg_byte_out',
		data : [],
		xkey : 'y',
		ykeys : [],
		labels : [],
		pointSize : 2,
		hideHover : 'auto',
		pointFillColors : [ '#ffffff' ],
		pointStrokeColors : [ 'black' ],
		resize : true
	});

	function mbeanRealtime(stime, etime, type) {
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
					mbean_msg_in.options.ykeys = datas.zks;
					mbean_msg_in.options.labels = datas.zks;
					mbean_msg_in.setData(filter(datas.msg, "message_in"));

					mbean_msg_byte_in.options.ykeys = datas.zks;
					mbean_msg_byte_in.options.labels = datas.zks;
					mbean_msg_byte_in.setData(filter(datas.ins, "byte_in"));

					mbean_msg_byte_out.options.ykeys = datas.zks;
					mbean_msg_byte_out.options.labels = datas.zks;
					mbean_msg_byte_out.setData(filter(datas.outs, "byte_out"));
					datas = null;
				}
			}
		});
	}

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
			default:
				break;
			}
		}
		return data;
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

	mbeanRealtime(stime, etime, type);
	$(".ranges").find("li[data-range-key='Custom Range']").remove();

	reportrange.on('apply.daterangepicker', function(ev, picker) {
		stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		mbeanRealtime(stime, etime, type);
	});
	console.log(stime + "," + etime);
	setInterval(function() {
		mbeanRealtime(stime, etime, type)
	}, 1000 * 60 * 5);
});