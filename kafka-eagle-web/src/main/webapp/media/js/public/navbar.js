$(document).ready(function() {
	var url = window.location.href;
	var ret = url.split("ke")[1];
	if (ret.indexOf("/cluster") > -1) {
		$("#demo2").addClass('collapse in');
		$("#demo2").attr("aria-expanded", true);
	} else if (ret == "/consumers") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_consumers").addClass('active');
	} else if (ret == "/") {
		$("div[id='navbar_click'] li").removeClass('active')
		$("#navbar_dash").addClass('active');
	} else if (ret.indexOf("/topic") > -1) {
		$("#demo").addClass('collapse in');
		$("#demo").attr("aria-expanded", true);
	} else if (ret.indexOf("/alarm") > -1) {
		$("#demo1").addClass('collapse in');
		$("#demo1").attr("aria-expanded", true);
		if (ret.indexOf("/alarm/add") > -1 || ret.indexOf("/alarm/modify") > -1) {
			$("#demo1_1").addClass('collapse in');
			$("#demo1_1").attr("aria-expanded", true);
		} else {
			$("#demo1_2").addClass('collapse in');
			$("#demo1_2").attr("aria-expanded", true);
		}
	} else if (ret.indexOf("/system") > -1) {
		$("#demo3").addClass('collapse in');
		$("#demo3").attr("aria-expanded", true);
	} else if (ret.indexOf("/metrics") > -1) {
		$("#demo4").addClass('collapse in');
		$("#demo4").attr("aria-expanded", true);
	}

	$(document).on('click', 'a[name=ke_account_reset]', function() {
		$('#ke_account_reset_dialog').modal('show');
		$(".modal-backdrop").css({
			"z-index" : "999"
		});
	});
});