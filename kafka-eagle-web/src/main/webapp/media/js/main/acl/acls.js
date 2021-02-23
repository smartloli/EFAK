function deleteM(data, that) {
	if (confirm('确定要删除吗') != true) return;
	
	var row = JSON.parse(decodeURI(data));
	
	var url = '/acls/delete';
	$(that).attr('disabled',"true");
    $.ajax({
        url: url,
        type: 'POST',
        processData: false,
        contentType: "application/json",
        data: JSON.stringify(row),
        success: function(response) {
        	$(that).removeAttr("disabled");
        	alert(response);
        	location.reload();
        },
        error: function(a,b) {
        	$(that).removeAttr("disabled");
        	alert("失败！");
            return false;
        }
    });		
}


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
				           	{
					           	"orderable": false,
				                mRender: function(data, type, row) {
				                    if (type == 'display') {
				                    	var sJson = encodeURI(JSON.stringify(row));
				                    	return  '<a href="#" class="btn btn-danger " onclick="deleteM(' + "'" + sJson + "'" +  ', this)">删除</a>';
				                    }
				                    return ""; 
				                }
				           	},
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