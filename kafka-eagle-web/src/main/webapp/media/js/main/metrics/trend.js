$(document).ready(function() {
	var mbean_msg_in = Morris.Area({
		element : 'mbean_msg_in',
		data : [],
		xkey : 'period',
		ykeys : [ 'Message In' ],
		labels : [ 'Message In' ],
		lineColors : [ '#7cb47c' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var mbean_msg_in_out = Morris.Area({
		element : 'mbean_msg_in_out',
		data : [],
		xkey : 'period',
		ykeys : [ 'Bytes In & Out' ],
		labels : [ 'Bytes In & Out' ],
		lineColors : [ '#d43f3a' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	var mbean_fetch_produce = Morris.Area({
		element : 'mbean_fetch_produce',
		data : [],
		xkey : 'period',
		ykeys : [ 'Fetch & Produce' ],
		labels : [ 'Fetch & Produce' ],
		lineColors : [ '#2577b5' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	function mbeanRealtime() {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/hc/metrics/trend/ajax',
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					mbean_msg_in.setData(datas.msgIn);
					mbean_msg_in_out.setData(datas.msgInOut);
					mbean_fetch_produce.setData(datas.fetchProduce);
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
			'Lastest 7 days' : [ moment().subtract(6, 'days'), moment() ],
			'Lastest 30 days' : [ moment().subtract(29, 'days'), moment() ],
			'This Month' : [ moment().startOf('month'), moment().endOf('month') ],
			'Last Month' : [ moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month') ]
		}
	}, cb);

	cb(start, end);
	
	mbeanRealtime();
	setInterval(mbeanRealtime, 1000 * 60 * 1);
});