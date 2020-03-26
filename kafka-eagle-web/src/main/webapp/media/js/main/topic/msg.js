$(document).ready(function() {
	var mime = 'text/x-mariadb';
	// get mime type
	if (window.location.href.indexOf('mime=') > -1) {
		mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
	}
	var sqlEditor = CodeMirror.fromTextArea(document.getElementById('code'), {
		mode : mime,
		indentWithTabs : true,
		smartIndent : true,
		lineNumbers : true,
		matchBrackets : true,
		autofocus : true,
		extraKeys : {
			"Alt-/" : "autocomplete"
		}
	});

	var logEditor = CodeMirror.fromTextArea(document.getElementById('job_info'), {
		mode : mime,
		indentWithTabs : true,
		smartIndent : true,
		lineNumbers : true,
		matchBrackets : true,
		autofocus : true,
		readOnly : true
	});

	$('#result_tab li:eq(0) a').tab('show');

	var offset = 0;
	function viewerTopics(sql, dataSets) {
		var ret = JSON.parse(dataSets);
		var tabHeader = "<div class='panel-body' id='div_children" + offset + "'><table id='result_children" + offset + "' class='table table-bordered table-hover' width='100%'><thead><tr>"
		var mData = [];
		var i = 0;
		for ( var key in ret[0]) {
			tabHeader += "<th>" + key + "</th>";
			var obj = {
				mData : key
			};
			mData.push(obj);
		}

		tabHeader += "</tr></thead></table></div>";
		$("#result_textarea").append(tabHeader);
		if (offset > 0) {
			$("#div_children" + (offset - 1)).remove();
		}

		$("#result_children" + offset).dataTable({
			"searching" : false,
			"bSort" : false,
			"retrieve" : true,
			"bLengthChange" : false,
			"bProcessing" : true,
			"bServerSide" : true,
			"fnServerData" : retrieveData,
			"sAjaxSource" : '/ke/topic/physics/commit/?sql=' + sql,
			"aoColumns" : mData
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

		offset++;
	}

	$(document).on('click', 'a[name=run_task]', function() {
		var sql = encodeURI(sqlEditor.getValue());
		logEditor.setValue("");
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/topic/logical/commit/?sql=' + sql,
			success : function(datas) {
				if (datas != null) {
					if (datas.error) {
						logEditor.setValue(datas.msg);
					} else {
						logEditor.setValue(datas.status);
						viewerTopics(sql, datas.msg);
					}
					viewerTopicSqlHistory();
				}
			}
		});
	});

	var historyOffset = 0;
	function viewerTopicSqlHistory() {
		var thList = [ {
			th : "ID",
			column : "id"
		}, {
			th : "User",
			column : "username"
		}, {
			th : "Host",
			column : "host"
		}, {
			th : "KSQL",
			column : "ksql"
		}, {
			th : "Status",
			column : "status"
		}, {
			th : "Spent",
			column : "spendTime"
		}, {
			th : "Created",
			column : "created"
		} ];
		var ksqlTabHeader = "<div class='panel-body' id='div_ksql_children" + historyOffset + "'><table id='result_ksql_children" + historyOffset + "' class='table table-bordered table-hover' width='100%'><thead><tr>"
		var ksqlMData = [];
		var i = 0;
		for (var i = 0; i < thList.length; i++) {
			ksqlTabHeader += "<th>" + thList[i].th + "</th>";
			var obj = {
				mData : thList[i].column
			};
			ksqlMData.push(obj);
		}

		ksqlTabHeader += "</tr></thead></table></div>";
		$("#ksql_history_result_div").append(ksqlTabHeader);
		if (historyOffset > 0) {
			$("#div_ksql_children" + (historyOffset - 1)).remove();
		} else {
			$("#ksql_history_result0").remove("");
		}

		$("#result_ksql_children" + historyOffset).dataTable({
			"bSort" : false,
			"retrieve" : true,
			"bLengthChange" : false,
			"bProcessing" : true,
			"bServerSide" : true,
			"fnServerData" : retrieveData,
			"sAjaxSource" : '/ke/topic/sql/history/ajax',
			"aoColumns" : ksqlMData
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

		historyOffset++;
	}

});