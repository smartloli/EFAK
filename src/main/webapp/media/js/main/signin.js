$(document).ready(function() {
	var url = window.location.href;
	var ref_url = url.split("cms/")[1];
	$("a[id='submit']").click(function() {
		var username = $("#username").val();
		var password = $("#password").val();
		var param = "username=" + username + "&password=" + password + "&ref_url=" + ref_url;
		if (username.length == 0 || password.length == 0) {
			$("#alert_mssage").text("用户名或密码不能为空").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
		} else {
			$.ajax({
				type : 'post',
				dataType : 'json',
				data : param,
				url : '/cms/signin/user',
				success : function(datas) {
					var loginStatus = datas["/login/status/"];
					console.log(loginStatus);
					if (loginStatus.error == "without") {
						if (loginStatus.refUrl == "signout") {
							window.location.href = "/cms";
						} else {
							window.location.href = "/cms/" + loginStatus.refUrl;
						}
					} else {
						$("#alert_mssage").text("用户名或密码错误").show();
						setTimeout(function() {
							$("#alert_mssage").hide()
						}, 3000);
					}
				}
			});
		}
	});
})