$(document).ready(function() {
	$("#ke_customer_filter").hide();
	$("#ke_sql_query").hide();
	
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
	
	sqlEditor.setValue("-- Kafka SQL Example: \r\n-- SELECT * FROM KE_Test_Topic WHERE partition IN (0,1,2) AND offsets in (0,10)\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
	
	// Initialization 
	var ms_condition = $("#ke_condition").magicSuggest({
		allowFreeEntries : false,
		autoSelect : true,
		maxSelection : 1,
		data:["Customer Filter","SQL Query"]
	});
	
	$(ms_condition).on("selectionchange", function(e) {
		var condition = this.getValue();
		if (condition.indexOf("Customer Filter") > -1) {
			$("#ke_customer_filter").show();
			$("#ke_sql_query").hide();
		} else if(condition.indexOf("SQL Query") > -1){
			$("#ke_customer_filter").hide();
			$("#ke_sql_query").show();
		}else{
			$("#ke_customer_filter").hide();
			$("#ke_sql_query").hide();
		}
	});
});