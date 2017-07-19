$(document).ready(
		function() {
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
						var msg = "<tr><td>Messages in /sec</td><td><a class='btn btn-primary btn-xs'>" + datas.msg.meanRate.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.msg.oneMinute.split("B")[0]
								+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.msg.fiveMinute.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.msg.fifteenMinute.split("B")[0] + "</a></td></tr>";
						msg += "<tr><td>Bytes in /sec</td><td><a class='btn btn-primary btn-xs'>" + datas.in.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.in.oneMinute
								+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.in.fiveMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.in.fifteenMinute + "</a></td></tr>";
						msg += "<tr><td>Bytes out /sec</td><td><a class='btn btn-primary btn-xs'>" + datas.out.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.out.oneMinute
								+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.out.fiveMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.out.fifteenMinute + "</a></td></tr>";
						msg += "<tr><td>Bytes rejected</td><td><a class='btn btn-primary btn-xs'>" + datas.rejected.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.rejected.oneMinute
								+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.rejected.fiveMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.rejected.fifteenMinute + "</a></td></tr>";
						msg += "<tr><td>Failed fetch request /sec</td><td><a class='btn btn-primary btn-xs'>" + datas.fetch.meanRate.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.fetch.oneMinute.split("B")[0]
						+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.fetch.fiveMinute.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.fetch.fifteenMinute.split("B")[0] + "</a></td></tr>";
						msg += "<tr><td>Failed produce request /sec</td><td><a class='btn btn-primary btn-xs'>" + datas.produce.meanRate.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.produce.oneMinute.split("B")[0]
						+ "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.produce.fiveMinute.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + datas.produce.fifteenMinute.split("B")[0] + "</a></td></tr>";
						tbody += msg + "</tbody>"
						$("#kafka_metrics_tab").append(tbody);
					}
				}
			});
		});