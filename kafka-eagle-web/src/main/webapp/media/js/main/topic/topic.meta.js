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
				}, {
					"mData" : 'preferred_leader'
				}, {
					"mData" : 'under_replicated'
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

			// Init chart common
			try {
				chartCommonOption = {
					backgroundColor : "#fff",
					tooltip : {
						trigger : 'axis',
						axisPointer : {
							type : 'cross',
							label : {
								backgroundColor : '#6a7985'
							}
						}
					},
					legend : {
						data : []
					},
					xAxis : {
						type : 'category',
						boundaryGap : false,
						data : []
					},
					dataZoom : {
						show : true,
						start : 30
					},
					grid : {
						bottom : "70px",
						left : "90px",
						right : "90px"
					},
					yAxis : {
						type : 'value'
					},
					series : {
						type : 'line',
						symbol : "none",
						// name : "",
						smooth : true,
						areaStyle : {
							opacity : 0.1
						},
						data : []
					}
				};
			} catch (e) {
				console.log(e.message);
			}

			// Add data control
			try {

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

				producerMsg(stime, etime);

				reportrange.on('apply.daterangepicker', function(ev, picker) {
					stime = reportrange[0].innerText.replace(/-/g, '').split("To")[0].trim();
					etime = reportrange[0].innerText.replace(/-/g, '').split("To")[1].trim();
					producerMsg(stime, etime);
				});
				setInterval(function() {
					producerMsg(stime, etime)
				}, 1000 * 60 * 5);
			} catch (e) {
				console.log(e.message);
			}

			try {
				function morrisLineInit(elment) {
					lagChart = echarts.init(document.getElementById(elment), 'macarons');
					lagChart.setOption(chartCommonOption);
					return lagChart;
				}
			} catch (e) {
				console.log(e.message);
			}

			var topic_producer_msg = morrisLineInit('topic_producer_msg');

			function producerMsg(stime, etime) {
				$.ajax({
					type : 'get',
					dataType : 'json',
					url : '/ke/topic/producer/chart/ajax?stime=' + stime + '&etime=' + etime + '&topic=' + topicName,
					beforeSend : function(xmlHttp) {
						xmlHttp.setRequestHeader("If-Modified-Since", "0");
						xmlHttp.setRequestHeader("Cache-Control", "no-cache");
					},
					success : function(datas) {
						if (datas != null) {
							setProducerChartData(topic_producer_msg, datas);
							datas = null;
						}
					}
				});
			}

			// set trend data
			function setProducerChartData(mbean, data) {
				chartCommonOption.xAxis.data = filter(data).x;
				chartCommonOption.series.data = filter(data).y;
				mbean.setOption(chartCommonOption);
			}

			// filter data
			function filter(datas) {
				var data = new Object();
				var datax = new Array();
				var datay = new Array();
				for (var i = 0; i < datas.length; i++) {
					datax.push(datas[i].x);
					datay.push(datas[i].y);
				}
				data.x = datax;
				data.y = datay;
				return data;
			}

		});