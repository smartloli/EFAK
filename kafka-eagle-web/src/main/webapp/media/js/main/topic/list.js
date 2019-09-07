$(document).ready(function() {
	$("#result").dataTable({
		// "searching" : false,
		"bSort" : false,
		"bLengthChange" : false,
		"bProcessing" : true,
		"bServerSide" : true,
		"fnServerData" : retrieveData,
		"sAjaxSource" : "/ke/topic/list/table/ajax",
		"aoColumns" : [ {
			"mData" : 'id'
		}, {
			"mData" : 'topic'
		}, {
			"mData" : 'partitions'
		}, {
			"mData" : 'partitionNumbers'
		}, {
			"mData" : 'created'
		}, {
			"mData" : 'modify'
		}, {
			"mData" : 'operate'
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

	var topic = "";
	$(document).on('click', 'a[name=remove]', function() {
		var href = $(this).attr("href");
		topic = href.split("#")[1];
		var token = $("#ke_admin_token").val();
		$("#remove_div").html("");
		$("#remove_div").append("<a id='ke_del_topic' href='#' class='btn btn-danger'>Remove</a>");
		$('#doc_info').modal({
			backdrop : 'static',
			keyboard : false
		});
		$('#doc_info').modal('show').css({
			position : 'fixed',
			left : '50%',
			top : '50%',
			transform : 'translateX(-50%) translateY(-50%)'
		});

		if (token.length == 0) {
			$("#ke_del_topic").attr("disabled", true);
			$("#ke_del_topic").attr("href", "#");
		} else {
			$("#ke_del_topic").attr("href", "/ke/topic/" + topic + "/" + token + "/delete");
		}
	});

	$("#ke_admin_token").on('input', function(e) {
		var token = $("#ke_admin_token").val();
		if (token.length == 0) {
			$("#ke_del_topic").attr("disabled", true);
			$("#ke_del_topic").attr("href", "#");
		} else {
			$("#ke_del_topic").attr("disabled", false);
			$("#ke_del_topic").attr("href", "/ke/topic/" + topic + "/" + token + "/delete");
		}
	});

	// Initialization topic
	var ms_topic = $("#ke_topic_filter_select").magicSuggest({
		allowFreeEntries : false,
		autoSelect : true,
		allowDuplicates : false,
		data : '/ke/topic/list/select/ajax',
		queryParam : 'topic'
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
				boundaryGap : true,
				data : []
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
				type : 'bar',
				symbol : "none",
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

		var start = moment().subtract(6, 'days');
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
			producerSelect();
		});
	} catch (e) {
		console.log(e.message);
	}

	try {
		function morrisBarInit(elment) {
			lagChart = echarts.init(document.getElementById(elment), 'macarons');
			lagChart.setOption(chartCommonOption);
			return lagChart;
		}
	} catch (e) {
		console.log(e.message);
	}

	var topic_producer_agg = morrisBarInit('topic_producer_agg');

	function producerMsg(stime, etime) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/topic/list/filter/select/ajax?stime=' + stime + '&etime=' + etime,
			beforeSend : function(xmlHttp) {
				xmlHttp.setRequestHeader("If-Modified-Since", "0");
				xmlHttp.setRequestHeader("Cache-Control", "no-cache");
			},
			success : function(datas) {
				if (datas != null) {
					setProducerBarData(topic_producer_agg, datas);
					datas = null;
				}
			}
		});
	}

	// set trend data
	function setProducerBarData(mbean, data) {
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

	$("#ke_topic_select_query").on('click', function() {
		producerSelect();
	});

	// auto refresh
	try {
		setInterval(function() {
			producerSelect()
		}, 1000 * 60 * 5);
	} catch (e) {
		console.log(e.message);
	}

	function producerSelect() {
		var topicsSets = $('#ke_topic_filter_select').magicSuggest().getSelection();
		var topics = new Array();
		for (var i = 0; i < topicsSets.length; i++) {
			topics.push(topicsSets[i].name);
		}
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/topic/list/filter/select/ajax?stime=' + stime + '&etime=' + etime + '&topics=' + topics,
			success : function(datas) {
				if (datas != null) {
					setProducerBarData(topic_producer_agg, datas);
					datas = null;
				}
			}
		});
	}

});