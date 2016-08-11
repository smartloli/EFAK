$(document).ready(function() {
	var url = window.location.href;
	var articleId = url.split("preview/")[1];
	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/cms/article/p/' + articleId,
		success : function(datas) {
			if (datas != null) {
				var title = datas.title;
				var content = datas.content;
				var type = datas.type;
				var chanle = datas.chanle;
				$("#title").find("label").text(title);
				$("#content").html(content);
				$("#type").find("span").text(type);
				$("#chanle").find("span").text(chanle);
			}
		}
	});
});