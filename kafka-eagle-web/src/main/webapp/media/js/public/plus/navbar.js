(function($) {
	// Add active state to sidbar nav links
	// because the 'href' property of the DOM element is the absolute path
	var path = window.location.href;
	$("#layoutSidenav_nav .sb-sidenav a.nav-link").each(function() {
		if (this.href+"/" === path) {
			$(this).addClass("active");
		}else if(this.href === path){
			$(this.parentNode.parentNode).addClass('show')
			$(this).addClass("active");
		}
	});

	// Toggle the side navigation
	$("#sidebarToggle").on("click", function(e) {
		e.preventDefault();
		$("body").toggleClass("sb-sidenav-toggled");
	});
	
	// Reset account
	$(document).on('click', 'a[name=ke_account_reset]', function() {
		$('#ke_account_reset_dialog').modal('show');
		$(".modal-backdrop").css({
			"z-index" : "999"
		});
	});
})(jQuery);
