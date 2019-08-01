$(document).ready(
		function() {
			var url = window.location.href;
			var topicName = url.split("meta/")[1].split("/")[0];

			$("#result").dataTable({
				"searching" : false,
				"bSort" : false,
				"bLengthChange" : false,
				"bProcessing" : true,
				"bServerSide" : true,
				"fnServerData" : retrieveData,
				"sAjaxSource" : "/ke/topic/meta/" + topicName + "/ajax",
				"aoColumns" : [ {
					"mData" : 'topic'
				}, {
					"mData" : 'partition'
				}, {
					"mData" : 'logsize'
				}, {
					"mData" : 'leader'
				}, {
					"mData" : 'replicas'
				}, {
					"mData" : 'isr'
				} ]
			});

			function retrieveData(sSource, aoData, fnCallback) {
				$.ajax({
					"type" : "get",
					"contentType" : "application/json",
					"url" : sSource,
					"dataType" : "json",
					"data" : {
						aoData : JSON.stringify(aoData)
					},
					"success" : function(data) {
						fnCallback(data)
					}
				});
			}

			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/topic/meta/mbean/' + topicName + '/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#topic_metrics_tab").html("")
						var thead = "<thead><tr><th>Rate</th><th>Mean</th><th>1 Minute</th><th>5 Minute</th><th>15 Minute</th></tr></thead>";
						$("#topic_metrics_tab").append(thead);
						var tbody = "<tbody>";
						var msg = topicMetricData('Messages in /sec', datas.msg);

						msg += topicMetricData('Bytes in /sec', datas.ins);
						msg += topicMetricData('Bytes out /sec', datas.out);
						msg += topicMetricData('Bytes rejected /sec', datas.rejected);
						msg += topicMetricData('Failed fetch request /sec', datas.fetch);
						msg += topicMetricData('Failed produce request /sec', datas.produce);
						msg += topicMetricData('Total fetch requests /sec', datas.total_fetch_requests);
						msg += topicMetricData('Total produce requests /sec', datas.total_produce_requests);
						msg += topicMetricData('Produce message conversions /sec', datas.produce_message_conversions);

						tbody += msg + "</tbody>"
						$("#topic_metrics_tab").append(tbody);
					}
				}
			});

			function topicMetricData(field, data) {
				var tr = '';
				if (data == null || data == undefined) {
					return tr;
				}

				if (field.toUpperCase().indexOf("BYTE") > -1) {
					tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + data.meanRate + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.oneMinute + "</a></td><td><a class='btn btn-primary btn-xs'>"
							+ data.fiveMinute + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fifteenMinute + "</a></td></tr>";
				} else {
					tr += "<tr><td>" + field + "</td><td><a class='btn btn-primary btn-xs'>" + data.meanRate.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.oneMinute.split("B")[0]
							+ "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fiveMinute.split("B")[0] + "</a></td><td><a class='btn btn-primary btn-xs'>" + data.fifteenMinute.split("B")[0] + "</a></td></tr>";
				}

				return tr;
			}
			
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/topic/meta/jmx/' + topicName + '/ajax',
				success : function(datas) {
					if (datas != null) {
						$("#producer_logsize").text(datas.logsize);
						$("#producer_topicsize").text(datas.topicsize);
						$("#producer_topicsize_type").text(datas.sizetype);
					}
				}
			});
			
		});