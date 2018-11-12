$(document).ready(function() {
//	$('#pwd').bind('focus', function() {
//		$(this).attr('type', 'password');
//	});

	$('#pwd').bind('keypress', function(event) {
		if (event.keyCode == "13") {
			contextFormValid();
		}
	});
	$("a[id='submit']").click(function() {
		contextFormValid();
	});
});