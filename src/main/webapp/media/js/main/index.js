$(document).ready(function() {
	// Morris.Area({
	// element : 'morris-area-chart',
	// data : [ {
	// period : '2016-08-03 09:00',
	// release : 10,
	// edit : 20,
	// add : 10,
	// remove : 1
	// }, {
	// period : '2016-08-03 10:00',
	// release : 4,
	// edit : 9,
	// add : 4,
	// remove : 1
	// }, {
	// period : '2016-08-03 11:00',
	// release : 1,
	// edit : 3,
	// add : 1,
	// remove : 0
	// }, {
	// period : '2016-08-03 12:00',
	// release : 1,
	// edit : 8,
	// add : 2,
	// remove : 0
	// }, {
	// period : '2016-08-03 13:00',
	// release : 1,
	// edit : 5,
	// add : 1,
	// remove : 0
	// } ],
	// xkey : 'period',
	// ykeys : [ 'release', 'edit', 'add', 'remove' ],
	// labels : [ '发布', '编辑', '添加', '删除' ],
	// pointSize : 2,
	// hideHover : 'auto',
	// resize : true
	// });
	// -- start
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/cms/dash/ajax',
		success : function(datas) {
			if (datas != null) {
				$("#release_count").text(datas.release)
				$("#unrelease_count").text(datas.unRelease)
				$("#add_count").text(datas.add)
				$("#del_count").text(datas.del)
			}
		}
	});
});