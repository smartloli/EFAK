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

	sqlEditor.setValue("-- Kafka SQL Example: \r\n-- SELECT \"partition\",\"offset\",\"msg\" FROM \"KE_Test_Topic_NAME\" WHERE \"partition\" IN (0,1,2) AND \"offsets\"=10001 limit 10");
});