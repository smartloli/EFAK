$(document).ready(function() {
	var url = window.location.href;
	var tmp = url.split("offset/")[1];
	var group = tmp.split("/")[0];
	var topic = tmp.split("/")[1];
	$("#topic_name_header").find("strong").text(topic);

	var graph = Morris.Area({
		element : 'morris-area-chart',
		data : [],
		xkey : 'period',
		ykeys : [ 'Lag', 'Offsets', 'LogSize' ],
		labels : [ 'Lag', 'Offsets', 'LogSize' ],
		lineColors : [ '#d43f3a', '#7cb47c', '#2577b5' ],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});

	function offserRealtime() {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/consumer/offset/' + group + '/' + topic + '/realtime/ajax',
			success : function(datas) {
				if (datas != null) {
					// Consumer & Producer Rate
					var producer = 0, consumer = 0;
					var consumerArrays = new Array();
					var producerArrays = new Array();
					for (var i = 0; i < datas.length; i++) {
						consumerArrays.push(datas[i].offsets);
						producerArrays.push(datas[i].lagsize);
					}

					consumerArrays.sort(function(a, b) {
						return b - a;
					});

					producerArrays.sort(function(a, b) {
						return b - a;
					});
					consumer = consumerArrays.length == 0 ? 0 : (consumerArrays[0] - consumerArrays[1]);
					producer = producerArrays.length == 0 ? 0 : (producerArrays[0] - producerArrays[1]);
					$("#producer_rate").text((producer / (5 * 60)).toFixed(3));
					$("#consumer_rate").text((consumer / (5 * 60)).toFixed(3));

					var data = new Array();
					for (var i = 0; i < datas.length; i++) {
						var obj = new Object();
						obj.period = datas[i].created;
						obj.LogSize = datas[i].lagsize;
						obj.Offsets = datas[i].offsets;
						obj.Lag = datas[i].lag;
						data.push(obj);
					}
					// Area Chart
					graph.setData(data);
					data = [];
				}
			}
		});
	}
	offserRealtime();
	setInterval(offserRealtime, 1000 * 60 * 5);
});