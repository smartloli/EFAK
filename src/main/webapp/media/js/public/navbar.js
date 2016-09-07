$(document).ready(function() {
	var url = window.location.href;
	var ret = url.split("ke")[1];
	if (ret == "/cluster") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_cluster").addClass('active');
	} else if (ret == "/consumers") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_consumers").addClass('active');
	} else if (ret == "/") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_dash").addClass('active');
	} else if (ret == "/topic/list" || ret.indexOf("meta") > -1) {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_list").addClass('active');
	} else if (ret == "/topic/create" || ret == "/topic/create/failed" || ret == "/topic/create/success") {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_create").addClass('active');
	}else if (ret == "/alarm/modify") {
		$("#demo1").addClass('collapse in');
		$("#demo1").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_modify").addClass('active');
	} else if (ret == "/alarm/add" || ret == "/alarm/add/failed" || ret == "/alarm/add/success") {
		$("#demo1").addClass('collapse in');
		$("#demo1").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_add").addClass('active');
	}
});