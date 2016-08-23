$(document).ready(function() {
	var url = window.location.href;
	var tmp = url.split("offset/")[1];
	var group = tmp.split("/")[0];
	var topic = tmp.split("/")[1];
	$("#topic_name_header").find("strong").text(topic);
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/consumer/offset/' + group + '/' + topic + '/realtime/ajax',
		success : function(datas) {
			if (datas != null) {
				// Consumer & Producer Rate
				var consumer = 0, producer = 0;
				for (var i = 0; i < datas.length; i++) {
					consumer += datas[i].offsets;
					producer += datas[i].lag;
				}
				
				$("#producer_rate").text((producer / (datas.length * 5 * 60)).toFixed(3));
				$("#consumer_rate").text((consumer / (datas.length * 5 * 60)).toFixed(3));

				var data = new Array();
				for (var i = 0; i < datas.length; i++) {
					var obj = new Object();
					obj.period = datas[i].created;
					obj.LogSize = datas[i].logSize;
					obj.Offsets = datas[i].offsets;
					obj.Lag = datas[i].lag;
					data.push(obj);
				}
				// Area Chart
				Morris.Area({
					element : 'morris-area-chart',
					data : data,
					xkey : 'period',
					ykeys : [ 'LogSize', 'Offsets', 'Lag' ],
					labels : [ 'LogSize', 'Offsets', 'Lag' ],
					lineColors : [ '#2577b5', '#7cb47c', '#d43f3a' ],
					pointSize : 2,
					hideHover : 'auto',
					resize : true
				});
			}
		}
	});
});