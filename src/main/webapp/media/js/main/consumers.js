$(document).ready(function() {
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/cluster/info/ajax',
		success : function(datas) {
			if (datas != null) {
				var kafka = datas.kafka;
				var zk = datas.zk;
				$("#kafka_tab").html("");
				$("#zk_tab").html("");
				var kafka_tab = "<thead><tr><th>ID</th><th>IP</th><th>Port</th><th>Version</th></tr></thead><tbody>";
				for (var i = 0; i < kafka.length; i++) {
					kafka_tab += " <tr><td>" + kafka[i].id + "</td><td>" + kafka[i].ip + "</td><td>" + kafka[i].port + "</td><td>" + kafka[i].version + "</td></tr>";
				}
				kafka_tab += "</tbody>"
				$("#kafka_tab").append(kafka_tab);
				
				var zk_tab = "<thead><tr><th>ID</th><th>IP</th><th>Port</th><th>Version</th></tr></thead><tbody>";
				for (var i = 0; i < zk.length; i++) {
					zk_tab += " <tr><td>" + zk[i].id + "</td><td>" + zk[i].ip + "</td><td>" + zk[i].port + "</td><td>" + zk[i].version + "</td></tr>";
				}
				zk_tab += "</tbody>"
				$("#zk_tab").append(zk_tab);
			}
		}
	});
});