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
			"sAjaxSource" : "/ke/consumer/list/table/ajax",
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

	var m = [ 20, 140, 20, 140 ], w = 1100 - m[1] - m[3], h = 600 - m[0] - m[2], i = 0, root;

	var tree = d3.layout.tree().size([ h, w ]);

	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	var vis = d3.select("#active_topic").append("svg:svg").attr("width", w + m[1] + m[3]).attr("height", h + m[0] + m[2]).append("svg:g").attr("transform", "translate(" + m[3] + "," + m[0] + ")");

	d3.json('/ke/consumers/info/ajax', function(json) {
		root = JSON.parse(json.active);
		root.x0 = h / 2;
		root.y0 = 0;

		function toggleAll(d) {
			if (d.children) {
				d.children.forEach(toggleAll);
				toggle(d);
			}
		}

		// Initialize the display to show a few nodes.
		root.children.forEach(toggleAll);
		for (var i = 0; i < root.children.length; i++) {
			toggle(root.children[i]);
		}
		update(root);
	});

	function update(source) {
		var duration = d3.event && d3.event.altKey ? 5000 : 500;

		// Compute the new tree layout.
		var nodes = tree.nodes(root).reverse();

		// Normalize for fixed-depth.
		nodes.forEach(function(d) {
			d.y = d.depth * 380;
		});

		// Update the nodes…
		var node = vis.selectAll("g.node").data(nodes, function(d) {
			return d.id || (d.id = ++i);
		});

		// Enter any new nodes at the parent's previous position.
		var nodeEnter = node.enter().append("svg:g").attr("class", "node").attr("transform", function(d) {
			return "translate(" + source.y0 + "," + source.x0 + ")";
		}).on("click", function(d) {
			toggle(d);
			update(d);
		});

		nodeEnter.append("svg:circle").attr("r", 1e-6).style("fill", function(d) {
			return d._children ? "lightsteelblue" : "#fff";
		});

		nodeEnter.append("svg:text").attr("x", function(d) {
			return d.children || d._children ? -10 : 10;
		}).attr("dy", ".35em").attr("text-anchor", function(d) {
			return d.children || d._children ? "end" : "start";
		}).text(function(d) {
			return d.name;
		}).style("fill-opacity", 1e-6);

		// Transition nodes to their new position.
		var nodeUpdate = node.transition().duration(duration).attr("transform", function(d) {
			return "translate(" + d.y + "," + d.x + ")";
		});

		nodeUpdate.select("circle").attr("r", 4.5).style("fill", function(d) {
			return d._children ? "lightsteelblue" : "#fff";
		});

		nodeUpdate.select("text").style("fill-opacity", 1);

		// Transition exiting nodes to the parent's new position.
		var nodeExit = node.exit().transition().duration(duration).attr("transform", function(d) {
			return "translate(" + source.y + "," + source.x + ")";
		}).remove();

		nodeExit.select("circle").attr("r", 1e-6);

		nodeExit.select("text").style("fill-opacity", 1e-6);

		// Update the links…
		var link = vis.selectAll("path.link").data(tree.links(nodes), function(d) {
			return d.target.id;
		});

		// Enter any new links at the parent's previous position.
		link.enter().insert("svg:path", "g").attr("class", "link").attr("d", function(d) {
			var o = {
				x : source.x0,
				y : source.y0
			};
			return diagonal({
				source : o,
				target : o
			});
		}).transition().duration(duration).attr("d", diagonal);

		// Transition links to their new position.
		link.transition().duration(duration).attr("d", diagonal);

		// Transition exiting nodes to the parent's new position.
		link.exit().transition().duration(duration).attr("d", function(d) {
			var o = {
				x : source.x,
				y : source.y
			};
			return diagonal({
				source : o,
				target : o
			});
		}).remove();

		// Stash the old positions for transition.
		nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		});
	}

	// Toggle children.
	function toggle(d) {
		if (d.children) {
			d._children = d.children;
			d.children = null;
		} else {
			d.children = d._children;
			d._children = null;
		}
	}

	// Children div show details of the consumer group
	var offset = 0;
	$(document).on('click', 'a[class=link]', function() {
		var group = $(this).attr("group");
		$('#doc_info').modal('show');

		$("#consumer_detail_children").append("<div class='panel-body' id='div_children" + offset + "'><table id='result_children" + offset + "' class='table table-bordered table-hover' width='100%'><thead><tr><th>ID</th><th>Topic</th><th>Consumer Status</th></tr></thead></table></div>");
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
			"sAjaxSource" : "/ke/consumer/group/table/ajax",
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