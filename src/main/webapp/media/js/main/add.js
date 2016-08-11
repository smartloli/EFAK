$(document).ready(function() {
	// 实例化编辑器
	KindEditor.ready(function(K) {
		editor = K.create('.kindeditor', {
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

	// 初始化选择日期控件 -- start
	function getDateDef(AddDayCount) {
		var dd = new Date();
		dd.setTime(dd.getTime() + AddDayCount * 24 * 60 * 60 * 1000);
		var y = dd.getFullYear();
		var m = dd.getMonth() + 1;
		var d = dd.getDate();
		if (m < 10) {
			m = "0".concat(m);
		}
		if (d < 10) {
			d = "0".concat(d);
		}
		return y + "-" + m + "-" + d;
	}

	function today() {
		var now = new Date();
		var y = now.getFullYear();
		var m = ((now.getMonth() + 1) < 10 ? "0" : "") + (now.getMonth() + 1);
		var d = (now.getDate() < 10 ? "0" : "") + now.getDate();
		return y + "-" + m + "-" + d;
	}
	$('#valid_range_date').find('input[name="article_valid_date"]').attr("value", getDateDef(-7) + "," + today());
	$('#valid_range_date').find('span').text(getDateDef(-7) + " 至 " + today());
	$('#valid_range_date').daterangepicker({
		locale : {
			format : 'YYYY-MM-DD',
			applyLabel : '确定',
			cancelLabel : '取消',
			daysOfWeek : [ "日", "一", "二", "三", "四", "五", "六" ],
			monthNames : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月" ]
		},
		"startDate" : getDateDef(-7),
		"endDate" : today()
	});

	$('#valid_range_date').on('apply.daterangepicker', function(ev, picker) {
		$('#valid_range_date').find('input[name="article_valid_date"]').attr("value", picker.startDate.format('YYYY-MM-DD') + "," + picker.endDate.format('YYYY-MM-DD'));
		$('#valid_range_date').find('span').text(picker.startDate.format('YYYY-MM-DD') + " 至 " + picker.endDate.format('YYYY-MM-DD'));
	});

	// 初始化选择日期控件 -- end

	// 左右选择控件
	var chanle_select = $('select[name="article_chanle"]').bootstrapDualListbox({
		showFilterInputs : false,
		infoText : false
	});

});