$(document).ready(function() {
	try {
		function showTables(rows) {
			var columns = [
				           	{"data": "pattern.resourceType" },
				           	{"data": "pattern.name" },
				           	{"data": "entry.principal" },
				           	{"data": "entry.host" },
				           	{"data": "entry.operation" },
				           	{"data": "entry.permissionType" },
		    			   ];
	
			var table = $('#result').DataTable({
				"aaSorting":[
				             [0, "desc"]
			                ],	
				"data": rows,
				"columns": columns,
			});		
		
		}
		
		function getdata() {
			$.ajax({
				"type" : "get",
				"contentType" : "application/json",
				"url" : "/acls/list",
				"dataType" : "json",
				"success" : function(data) {
					console.dir(data);
					showTables(data);
				}
			});		
		}
		// --end
	} catch (e) {
		console.log("Get acl  has error, msg is " + e)
	}

	getdata();
});