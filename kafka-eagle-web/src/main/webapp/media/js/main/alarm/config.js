$(document).ready(function() {
	$("#select2val").select2({
		placeholder : "Alarm Type",
		ajax : {
			url : "/ke/alarm/type/list/ajax",
			dataType : 'json',
			delay : 250,
			data : function(params) {
				params.offset = 10;
				params.page = params.page || 1;
				return {
					name : params.term,
					page : params.page,
					offset : params.offset
				};
			},
			cache : true,
			processResults : function(data, params) {
				if (data.items.length > 0) {
					var datas = new Array();
					$.each(data.items, function(index, e) {
						var s = {};
						s.id = index + 1;
						s.text = e.text;
						datas[index] = s;
					});
					return {
						results : datas,
						pagination : {
							more : (params.page * params.offset) < data.total
						}
					};
				} else {
					return {
						results : []
					}
				}
			},
			escapeMarkup : function(markup) {
				return markup;
			},
			minimumInputLength : 1
		}
	});

	$('#select2val').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2val").val(text);
		$("#ke_alarm_type").val(text);
		if (text.indexOf("Email") > -1) {
			$("#div_alarm_http").hide();
			$("#div_alarm_address").show();
			$("#ke_alarm_url").attr('placeholder', "http://127.0.0.1:10086/email");
		} else if (text.indexOf("WebHook") > -1) {
			$("#div_alarm_http").hide();
			$("#div_alarm_address").show();
			$("#ke_alarm_url").attr('placeholder', "http://127.0.0.1:10086/webhook");
		} else if (text.indexOf("DingDing") > -1) {
			$("#div_alarm_http").hide();
			$("#div_alarm_address").hide();
			$("#ke_alarm_url").attr('placeholder', "https://oapi.dingtalk.com/robot/send?access_token=");
		} else if (text.indexOf("WeChat") > -1) {
			$("#div_alarm_http").hide();
			$("#div_alarm_address").hide();
			$("#ke_alarm_url").attr('placeholder', "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=");
		}
	});

	$("#btn_send_test").click(function() {
		var type = $("#ke_alarm_type").val();
		var url = $("#ke_alarm_url").val();
		var http = $('input:radio:checked').val();
		var msg = $("#ke_test_msg").val();
		console.log(url);
		if (type.length == 0 || url.length == 0 || msg.length == 0) {
			$("#alert_msg_alarm_send").show();
			setTimeout(function() {
				$("#alert_msg_alarm_send").hide()
			}, 3000);
		} else {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/alarm/config/test/send/ajax?type=' + type + '&url=' + url + '&http=' + http + '&msg=' + msg,
				success : function(datas) {
					if (type.indexOf("DingDing") > -1 || type.indexOf("WeChat") > -1 || type.indexOf("Email") > -1) {
						if (datas.errcode == 0) {
							$("#success_mssage_alarm_config").show();
							setTimeout(function() {
								$("#success_mssage_alarm_config").hide()
							}, 5000);
						} else {
							$("#failed_mssage_alarm_config").show();
							setTimeout(function() {
								$("#failed_mssage_alarm_config").hide()
							}, 5000);
						}
					}

				}
			});
		}
	});

});