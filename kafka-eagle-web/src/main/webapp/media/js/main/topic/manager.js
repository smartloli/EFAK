$(document).ready(function() {

	$("#select2val").select2({
		placeholder : "Topic",
		ajax : {
			url : "/ke/topic/mock/list/ajax",
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
				return {
					results : data.items,
					pagination : {
						more : (params.page * params.offset) < data.total
					}
				};
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
		$("#ke_topic_name").val(text);
	});

	$("#select2key").select2({
		placeholder : "Key",
		ajax : {
			url : "/ke/topic/manager/keys/ajax",
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
				return {
					results : data.items,
					pagination : {
						more : (params.page * params.offset) < data.total
					}
				};
			},
			escapeMarkup : function(markup) {
				return markup;
			},
			minimumInputLength : 1
		}
	});

	$('#select2key').on('select2:select', function(evt) {
		var text = evt.params.data.text;
		$("#select2key").val(text);
		$("#ke_topic_key").val(text);
	});

	$(document).on("click", "#btn_send", function() {
		var topic = $("#ke_topic_name").val();
		var message = $("#ke_mock_content").val();
		if (topic.length == 0 || message.length == 0) {
			$("#alert_mssage_mock").show();
			setTimeout(function() {
				$("#alert_mssage_mock").hide()
			}, 3000);
		} else {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/topic/mock/send/message/' + topic + '/ajax?message=' + message,
				success : function(datas) {
					if (datas != null) {
						console.log(datas)
						if (datas.status) {
							$("#success_mssage_mock").show();
							setTimeout(function() {
								$("#success_mssage_mock").hide()
							}, 3000);
						}
					}
				}
			});
		}
	});

	$(":radio").click(function() {
		if ($(this).val() == "add_config") {
			$("#div_topic_keys").show();
			$("#div_topic_value").show();
			$("#div_topic_msg").show();
		} else if ($(this).val() == "del_config") {
			$("#div_topic_keys").show();
			$("#div_topic_value").hide();
			$("#div_topic_msg").show();
		} else if ($(this).val() == "desc_config") {
			$("#div_topic_keys").hide();
			$("#div_topic_value").hide();
			$("#div_topic_msg").show();
		} else if ($(this).val() == "clean_data") {
			$("#div_topic_keys").hide();
			$("#div_topic_value").hide();
			$("#div_topic_msg").hide();
		}
	});

});