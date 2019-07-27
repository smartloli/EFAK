$(document).ready(function() {
	var m = [ 20, 240, 20, 240 ], w = 1280 - m[1] - m[3], h = 600 - m[0] - m[2], i = 0, root;

	var tree = d3.layout.tree().size([ h, w ]);

	var diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.y, d.x ];
	});

	var vis = d3.select("#kafka_brokers").append("svg:svg").attr("width", w + m[1] + m[3]).attr("height", h + m[0] + m[2]).append("svg:g").attr("transform", "translate(" + m[3] + "," + m[0] + ")");

	d3.json('/ke/dash/kafka/ajax', function(json) {
		dashboard = JSON.parse(json.dashboard);
		root = JSON.parse(json.kafka);
		root.x0 = h / 2;
		root.y0 = 0;

		$("#brokers_count").text(dashboard.brokers);
		$("#topics_count").text(dashboard.topics);
		$("#zks_count").text(dashboard.zks);
		$("#consumers_count").text(dashboard.consumers);

		function toggleAll(d) {
			if (d.children) {
				d.children.forEach(toggleAll);
				toggle(d);
			}
		}

		// Initialize the display to show a few nodes.
		root.children.forEach(toggleAll);
		toggle(root.children[0]);
		update(root);
	});

	function update(source) {
		var duration = d3.event && d3.event.altKey ? 5000 : 500;

		// Compute the new tree layout.
		var nodes = tree.nodes(root).reverse();

		// Normalize for fixed-depth.
		nodes.forEach(function(d) {
			d.y = d.depth * 580;
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

	function fillgaugeGrahpPie(datas, id) {
		var config = liquidFillGaugeDefaultSettings();
		config.circleThickness = 0.1;
		config.circleFillGap = 0.2;
		config.textVertPosition = 0.8;
		config.waveAnimateTime = 2000;
		config.waveHeight = 0.3;
		config.waveCount = 1;
		if (datas > 65 && datas < 80) {
			config.circleColor = "#D4AB6A";
			config.textColor = "#553300";
			config.waveTextColor = "#805615";
			config.waveColor = "#AA7D39";
		} else if (datas >= 80) {
			config.circleColor = "#d9534f";
			config.textColor = "#d9534f";
			config.waveTextColor = "#d9534f";
			config.waveColor = "#FFDDDD";
		}
		loadLiquidFillGauge(id, datas, config);
	}

	try {
		$.ajax({
			type : 'get',
			dataType : 'json',
			url : '/ke/dash/os/mem/ajax',
			success : function(datas) {
				if (datas != null) {
					fillgaugeGrahpPie(datas.mem, "fillgauge_kafka_memory");
				}
			}
		});
	} catch (e) {
		console.log(e);
	}

	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/dash/logsize/table/ajax',
		success : function(datas) {
			if (datas != null) {
				$("#topic_logsize").html("")
				var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>LogSize</th></tr></thead>";
				$("#topic_logsize").append(thead);
				var tbody = "<tbody>";
				var tr = '';
				for (var i = 0; i < datas.length; i++) {
					var id = datas[i].id;
					var topic = datas[i].topic;
					var logsize = datas[i].logsize;
					tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + logsize + "</td></tr>"
				}
				tbody += tr + "</tbody>"
				$("#topic_logsize").append(tbody);
			}
		}
	});

	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/dash/capacity/table/ajax',
		success : function(datas) {
			if (datas != null) {
				$("#topic_capacity").html("")
				var thead = "<thead><tr><th>RankID</th><th>Topic Name</th><th>Capacity</th></tr></thead>";
				$("#topic_capacity").append(thead);
				var tbody = "<tbody>";
				var tr = '';
				for (var i = 0; i < datas.length; i++) {
					var id = datas[i].id;
					var topic = datas[i].topic;
					var capacity = datas[i].capacity;
					tr += "<tr><td>" + id + "</td><td>" + topic + "</td><td>" + capacity + "</td></tr>"
				}
				tbody += tr + "</tbody>"
				$("#topic_capacity").append(tbody);
			}
		}
	});

});