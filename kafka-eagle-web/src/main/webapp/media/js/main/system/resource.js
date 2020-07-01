$(document).ready(function() {
	// tree option start
	keHomeTreeOption = {
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
	keHomeTree = echarts.init(document.getElementById("ke_graph_home"));

	// home tree
	function homeTree() {
		try {
			$.ajax({
				type: 'get',
				dataType: 'json',
				url: '/system/resource/graph/ajax',
				success: function (datas) {
					if (datas != null) {
						var homeTreeData = new Array();
						homeTreeData.push(datas);
						keHomeTreeOption.series.data = homeTreeData;
						keHomeTree.setOption(keHomeTreeOption);
					}
				}
			});
		} catch (e) {
			console.log(e);
		}
	}

	homeTree();

	$("#ke_graph_home").resize(function () {
		var optKeHomeTree=keHomeTree.getOption();
		keHomeTree.clear();
		keHomeTree.resize({width:$("#ke_graph_home").css('width')});
		keHomeTree.setOption(optKeHomeTree);
	});

	$("#config-home-btn").click(function() {
		$('#ke_home_dialog').modal('show');
	});

	$("#config-children-btn").click(function() {
		$('#ke_child_dialog').modal('show');
		selectResource("#res_parent_id", "/system/resource/parent/ajax");
	});

	$("#config-delete-btn").click(function() {
		$('#ke_delete_dialog').modal('show');
		selectResource("#res_child_root_id", "/system/resource/parent/ajax");
	});

	function selectResource(id, url) {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : url,
			success : function(datas) {
				if (datas != null) {
					$(id).html("");
					var option = "";
					for (var i = 0; i < datas.length; i++) {
						option += "<option value='" + datas[i].resourceId + "'>" + datas[i].name + "</option>"
					}
					$(id).append(option);
				}
			}
		});
	}

	$("#res_child_root_id").change(function() {
		selectResource("#res_child_id", "/system/resource/child/" + $("#res_child_root_id").val() + "/ajax");
	});
});