$(document).ready(function() {

	function trim(str) {
		return str.replace(/^\s+|\s+$/g, "");
	}

	function dealCmd(command, term) {
		if (command.indexOf("ls") > -1) {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/cluster/zk/cmd/ajax?cmd=' + command + '&type=ls',
				success : function(datas) {
					if (datas != null) {
						term.echo(new String(datas.result));
					}
				}
			});
		}  else if (command.indexOf("get") > -1) {
			$.ajax({
				type : 'get',
				dataType : 'json',
				url : '/ke/cluster/zk/cmd/ajax?cmd=' + command + '&type=get',
				success : function(datas) {
					if (datas != null) {
						term.echo(new String(datas.result));
					}
				}
			});
		} else {
			throw new Error("Currently only supports ls, get commands, example: ls / ");
		}
	}

	$.ajax({
		type : 'get',
		dataType : 'json',
		url : '/ke/cluster/zk/islive/ajax',
		success : function(datas) {
			if (datas != null) {
				if (datas.live) {
					$('#zkcli_info').terminal(function(command, term) {
						if (command !== '') {
							try {
								dealCmd(command, term);
							} catch (e) {
								term.error(new String(e));
							}
						} else {
							term.echo('');
						}
					}, {
						greetings : '********************************************************************************\n' + 'Name :  Zookeeper Client Interpreter\n' + 'Server :  [' + datas.list + ']\n' + '********************************************************************************\n',
						height : 400,
						prompt : '[zk: (CONNECTED) ] > '
					});
				} else {
					$('#zkcli_info').terminal(function(command, term) {
					}, {
						greetings : '********************************************************************************\n' + 'Name :  Zookeeper Client Interpreter\n' + 'Server :  [' + datas.list + ']\n' + '********************************************************************************\n',
						height : 400,
						prompt : '[zk: (DISCONNECTED) ] > '
					});
				}
			}
		}
	});
});