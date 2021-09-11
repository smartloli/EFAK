$(document).ready(function() {
	try {
		// Initialize consumer table list --start
		$("#result").dataTable({
			// "searching" : false,
			"bSort" : false,
			"bLengthChange" : false,
			"bProcessing" : true,
			"bServerSide" : true,
			"fnServerData" : retrieveData,
			"sAjaxSource" : "/consumer/list/table/ajax",
			"aoColumns" : [ {
				"mData" : 'id'
			}, {
				"mData" : 'group'
			}, {
				"mData" : 'topics'
			}, {
				"mData" : 'node'
			}, {
				"mData" : 'activeTopics'
			}, {
				"mData" : 'activeThreads'
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
		// --end
	} catch (e) {
		console.log("Get consumer group has error, msg is " + e)
	}

	// tree option start
	keConsumerTreeOption = {
		series: {
			type: 'tree',
			data: [],
			top: '1%',
			left: '20%',
			bottom: '1%',
			right: '40%',
			symbolSize: 10,
			itemStyle: {
				color: "rgb(176, 196, 222)", // fold color
				borderColor: 'steelblue'
			},
			label: {
				position: 'left',
				verticalAlign: 'middle',
				align: 'right',
				fontSize: 14
			},
			leaves: {
				label: {
					normal: {
						position: 'right',
						verticalAlign: 'middle',
						align: 'left',
					}
				}
			}
		}
	}
	// tree option end
	keConsumerTree = echarts.init(document.getElementById("active_topic"));
	// consumer tree
	function consumerTree() {
		try {
			$.ajax({
				type: 'get',
				dataType: 'json',
				url: '/consumers/info/ajax',
				success: function (datas) {
					if (datas != null) {
						consumerTree = JSON.parse(datas.active);
						var consumerTreeData = new Array();
						consumerTreeData.push(consumerTree);
						keConsumerTreeOption.series.data = consumerTreeData;
						keConsumerTree.setOption(keConsumerTreeOption);
					}
				}
			});
		} catch (e) {
			console.log(e);
		}
	}

	consumerTree();

	$("#active_topic").resize(function () {
		var optKeConsumerTree=keConsumerTree.getOption();
		keConsumerTree.clear();
		keConsumerTree.resize({width:$("#active_topic").css('width')});
		keConsumerTree.setOption(optKeConsumerTree);
	});

	// Children div show details of the consumer group
	var offset = 0;
	$(document).on('click', 'a[class=link]', function() {
		var group = $(this).attr("group");
		$('#ke_consumer_topics_detail').modal('show');

		$("#consumer_detail_children").append("<div class='table-responsive' id='div_children" + offset + "'><table id='result_children" + offset + "' class='table table-bordered table-condensed' width='100%'><thead><tr><th>ID</th><th>Topic</th><th>Consumer Status</th></tr></thead></table></div>");
		if (offset > 0) {
			$("#div_children" + (offset - 1)).remove();
		}

		// Initialize consumer table list --start
		$("#result_children" + offset).dataTable({
			// "searching" : false,
			"bSort" : false,
			"bLengthChange" : false,
			"bProcessing" : true,
			"bServerSide" : true,
			"fnServerData" : retrieveData,
			"sAjaxSource" : "/consumer/group/table/ajax",
			"aoColumns" : [ {
				"mData" : 'id'
			}, {
				"mData" : 'topic'
			}, {
				"mData" : 'isConsumering'
			} ]
		});

		function retrieveData(sSource, aoData, fnCallback) {
			$.ajax({
				"type" : "get",
				"contentType" : "application/json",
				"url" : sSource,
				"dataType" : "json",
				"data" : {
					aoData : JSON.stringify(aoData),
					group : group
				},
				"success" : function(data) {
					fnCallback(data)
				}
			});
		}
		// --end

		offset++;
	});
});