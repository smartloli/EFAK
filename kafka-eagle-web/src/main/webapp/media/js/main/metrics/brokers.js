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
		tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + data.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.oneMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fiveMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fifteenMinute + "</a></td></tr>";
	} else {
		tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + revertString2Long(data.meanRate) + "</a></td><td><a class='btn btn-primary btn-xs'>" + revertString2Long(data.oneMinute) + "</a></td><td><a class='btn btn-primary btn-xs'>" + revertString2Long(data.fiveMinute) + "</a></td><td><a class='btn btn-primary btn-xs'>" + revertString2Long(data.fifteenMinute) + "</a></td></tr>";
	}

	return tr;
}

// defined byte size
var KB_IN_BYTES = 1024;
var MB_IN_BYTES = 1024 * KB_IN_BYTES;
var GB_IN_BYTES = 1024 * MB_IN_BYTES;
var TB_IN_BYTES = 1024 * GB_IN_BYTES;
var PB_IN_BYTES = 1024 * TB_IN_BYTES;

function revertString2Long(dataSource) {
	var data = dataSource.split("B")[0];
	 if (data.indexOf("K") > -1) {
		var value = data.split("K")[0];
		return (value * KB_IN_BYTES).toFixed(2);
	} else if (data.indexOf("M") > -1) {
		var value = data.split("M")[0];
		return (value * MB_IN_BYTES).toFixed(2);
	} else if (data.indexOf("G") > -1) {
		var value = data.split("G")[0];
		return (value * GB_IN_BYTES).toFixed(2);
	} else if (data.indexOf("T") > -1) {
		var value = data.split("T")[0];
		return (value * TB_IN_BYTES).toFixed(2);
	} else if (data.indexOf("P") > -1) {
		var value = data.split("P")[0];
		return (value * PB_IN_BYTES).toFixed(2);
	}else{ // Byte
		return data;
	}
}