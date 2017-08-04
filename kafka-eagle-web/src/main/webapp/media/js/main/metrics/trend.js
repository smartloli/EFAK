$(document).ready(function() {
	var mbean_msg_in = Morris.Line({
		element : 'mbean_msg_in',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'MessageIn' ],
		labels : [ 'MessageIn' ],
		lineColors : [ '#7cb47c' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var mbean_msg_in_out = Morris.Line({
		element : 'mbean_msg_in_out',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'ByteIn', 'ByteOut' ],
		labels : [ 'ByteIn', 'ByteOut' ],
		lineColors : [ '#d43f3a', '#7cb47c' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var mbean_fetch_produce = Morris.Line({
		element : 'mbean_fetch_produce',
		data : [],
		xkey : 'xkey',
		ykeys : [ 'FailedFetchRequest', 'FailedProduceRequest' ],
		labels : [ 'FailedFetchRequest', 'FailedProduceRequest' ],
		lineColors : [ '#a7b3bc', '#2577b5' ],
		pointSize : 2,
		hideHover : 'auto',
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
					mbean_msg_in.setData(datas.message);
					mbean_msg_in_out.setData(datas.inout);
					mbean_fetch_produce.setData(datas.failed);
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
	var type = "daily";

	mbeanRealtime(stime, etime, type);
	$(".ranges").find("li[data-range-key='Custom Range']").remove();

	reportrange.on('apply.daterangepicker', function(ev, picker) {
		stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
		etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
		if (picker.chosenLabel == "Today" || picker.chosenLabel == "Yesterday") {
			type = "daily";
		} else {
			type = "day";
		}
		mbeanRealtime(stime, etime, type);
	});
	console.log(stime + "," + etime);
	setInterval(function() {
		mbeanRealtime(stime, etime, type)
	}, 1000 * 60 * 5);
});