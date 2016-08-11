$(document).ready(function() {
	var url = window.location.href;
	var articleId = url.split("edit/")[1];
	$("#article_id").val(articleId);
	var editor;
	// 实例化编辑器
	KindEditor.ready(function(K) {
		editor = K.create('textarea[name="article_content"]', {
			resizeType : 0,
			allowPreviewEmoticons : false,
			uploadJson : '/admin/resource/index',
			allowImageUpload : true,
			height : '500px',
			items : [ 'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold', 'italic', 'underline', 'strikethrough', 'removeformat', '|', 'justifyleft', 'justifycenter', 'justifyright', 'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'lineheight', '|', 'link', 'unlink', '|', 'image', 'table','preview' ],
			afterBlur : function() {
				this.sync();
			}
		});
	});

	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/cms/article/e/' + articleId,
		success : function(datas) {
			if (datas != null) {
				var title = datas.title;
				var type = datas.type;
				var tmpSDate = datas.sDate;
				var sDate = tmpSDate.substring(0, 4);
				tmpSDate = datas.sDate;
				sDate += "-" + tmpSDate.substring(4, 6);
				tmpSDate = datas.sDate;
				sDate += "-" + tmpSDate.substring(6, 8);
				var tmpEDate = datas.eDate;
				var eDate = tmpEDate.substring(0, 4);
				tmpEDate = datas.eDate;
				eDate += "-" + tmpEDate.substring(4, 6);
				tmpEDate = datas.eDate;
				eDate += "-" + tmpEDate.substring(6, 8);
				var chanle = datas.chanle;
				var isTop = datas.isTop;
				var content = datas.content;
				var isValid = datas.isValid;

				$("#article_title").val(title);
				$("#edit_article_type").find('input').each(function() {
					if (type == $(this).val()) {
						$(this).attr("checked", "checked");
					}
				});

				// 填充有效时间控件 -- start
				$('#valid_range_date').find('input[name="article_valid_date"]').attr("value", sDate + "," + eDate);
				$('#valid_range_date').find('span').text(sDate + " 至 " + eDate);
				$('#valid_range_date').daterangepicker({
					locale : {
						format : 'YYYY-MM-DD',
						applyLabel : '确定',
						cancelLabel : '取消',
						daysOfWeek : [ "日", "一", "二", "三", "四", "五", "六" ],
						monthNames : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" ]
					},
					"startDate" : sDate,
					"endDate" : eDate
				});

				$('#valid_range_date').on('apply.daterangepicker', function(ev, picker) {
					$('#valid_range_date').find('input[name="article_valid_date"]').attr("value", picker.startDate.format('YYYY-MM-DD') + "," + picker.endDate.format('YYYY-MM-DD'));
					$('#valid_range_date').find('span').text(picker.startDate.format('YYYY-MM-DD') + " 至 " + picker.endDate.format('YYYY-MM-DD'));
				});
				// 填充有效时间控件 -- end

				// 有效选择框
				if (isValid == "是") {
					$("input[name='article_valid']").attr("checked", "checked");
				}

				// 频道
				$("#article_chanle").find('option').each(function() {
					if (chanle == $(this).val()) {
						$(this).attr("selected", "selected");
						// 左右选择控件
						$('select[name="article_chanle"]').bootstrapDualListbox({
							showFilterInputs : false,
							infoText : false
						});
					}
				});

				// 是否置顶
				$("#article_is_top").find('input').each(function() {
					if (isTop == $(this).val()) {
						$(this).attr("checked", "checked");
					}
				});

				editor.html(content);
			}
		}
	});

});