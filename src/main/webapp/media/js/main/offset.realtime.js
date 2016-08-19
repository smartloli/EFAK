$(document).ready(function() {
	var url = window.location.href;
	var tmp = url.split("offset/")[1];
	var group = tmp.split("/")[0];
	var topic = tmp.split("/")[1];
	$("#topic_name_header").find("strong").text(topic);

	// Area Chart
	Morris.Area({
		element : 'morris-area-chart',
		data : [ {
			period : '2016-08-09 15:00',
			LogSize1 : 2666,
			LogSize : 2666,
			Offsets : null,
			Lag : 2647
		}, {
			period : '2016-08-09 15:05',
			LogSize : 2778,
			Offsets : 2294,
			Lag : 2441
		}, {
			period : '2016-08-09 15:10',
			LogSize : 4912,
			Offsets : 1969,
			Lag : 2501
		}, {
			period : '2016-08-09 15:15',
			LogSize : 3767,
			Offsets : 3597,
			Lag : 5689
		}, {
			period : '2016-08-09 15:20',
			LogSize : 6810,
			Offsets : 1914,
			Lag : 2293
		}, {
			period : '2016-08-09 15:25',
			LogSize : 5670,
			Offsets : 4293,
			Lag : 1881
		}, {
			period : '2016-08-09 15:30',
			LogSize : 4820,
			Offsets : 3795,
			Lag : 1588
		}, {
			period : '2016-08-09 15:35',
			LogSize : 15073,
			Offsets : 5967,
			Lag : 5175
		}, {
			period : '2016-08-09 15:40',
			LogSize : 10687,
			Offsets : 4460,
			Lag : 2028
		}, {
			period : '2016-08-09 15:45',
			LogSize : 8432,
			Offsets : 5713,
			Lag : 1791
		}, {
			period : '2016-08-09 15:50',
			LogSize : 8432,
			Offsets : 5713,
			Lag : 1791
		}, {
			period : '2016-08-09 15:55',
			LogSize : 8432,
			Offsets : 5713,
			Lag : 1791
		} ],
		xkey : 'period',
		ykeys : [ 'LogSize', 'Offsets', 'Lag' ],
		labels : [ 'LogSize', 'Offsets', 'Lag' ],
		lineColors:['#2577b5','#7cb47c','#d43f3a'],
		pointSize : 2,
		hideHover : 'auto',
		resize : true
	});
});