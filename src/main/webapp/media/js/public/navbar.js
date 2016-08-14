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
	} else if (ret == "/topic/list") {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_list").addClass('active');
	} else if (ret == "/topic/create") {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_create").addClass('active');
	}
});