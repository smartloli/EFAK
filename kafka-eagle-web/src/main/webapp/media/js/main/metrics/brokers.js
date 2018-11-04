$(document).ready(function() {
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/metrics/brokers/mbean/ajax',
		success : function(datas) {
			if (datas != null) {
				$("#kafka_metrics_tab").html("")
				var thead = "<thead><tr><th>Rate</th><th>Mean</th><th>1 Minute</th><th>5 Minute</th><th>15 Minute</th></tr></thead>";
				$("#kafka_metrics_tab").append(thead);
				var tbody = "<tbody>";
				var msg = brokerMetricData('Messages in /sec', datas.msg);

				msg += brokerMetricData('Bytes in /sec', datas.ins);

				msg += brokerMetricData('Bytes out /sec', datas.out);

				msg += brokerMetricData('Bytes rejected /sec', datas.rejected);

				msg += brokerMetricData('Failed fetch request /sec', datas.fetch);

				msg += brokerMetricData('Failed produce request /sec', datas.produce);

				msg += brokerMetricData('Total fetch requests /sec', datas.total_fetch_requests);

				msg += brokerMetricData('Total produce requests /sec', datas.total_produce_requests);

				msg += brokerMetricData('Replication byte in /sec', datas.replication_bytes_in);

				msg += brokerMetricData('Replication byte out /sec', datas.replication_bytes_out);

				msg += brokerMetricData('Produce message conversions /sec', datas.produce_message_conversions);

				tbody += msg + "</tbody>"
				$("#kafka_metrics_tab").append(tbody);
			}
		}
	});
});

function brokerMetricData(field, data) {
	var tr = '';
	if (data == null || data == undefined) {
		return tr;
	}

	if (field.toUpperCase().indexOf("BYTE") > -1) {
		console.log("dsdsd");
		tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + data.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.oneMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fiveMinute
				+ "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fifteenMinute + "</a></td></tr>";
	} else {
		tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + data.meanRate.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.oneMinute.split("B")[0]
				+ "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fiveMinute.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fifteenMinute.split("B")[0] + "</a></td></tr>";
	}

	return tr;
}