$(document).ready(function () {
    var mime = 'text/x-mariadb';
    // get mime type
    if (window.location.href.indexOf('mime=') > -1) {
        mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
    }
    var ke_connector_result_status = CodeMirror.fromTextArea(document.getElementById('ke_connect_result_status'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    var ke_connector_result_config = CodeMirror.fromTextArea(document.getElementById('ke_connect_result_config'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    var ke_connector_result_tasks = CodeMirror.fromTextArea(document.getElementById('ke_connect_result_tasks'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true
    });

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        var context = "";
        if (r != null)
            context = r[2];
        reg = null;
        r = null;
        return context == null || context == "" || context == "undefined" ? "" : context;
    }

    var uri = getQueryString("uri");
    var connector = getQueryString("connector");

    console.log(uri);
    console.log(connector);

    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/connect/plugins/result/ajax/?uri=' + uri + '&connector=' + connector,
        success: function (datas) {
            if (datas != null) {
                ke_connector_result_status.setValue(JSON.stringify(JSON.parse(datas.status), null, 2));
                ke_connector_result_config.setValue(JSON.stringify(JSON.parse(datas.config), null, 2));
                ke_connector_result_tasks.setValue(JSON.stringify(JSON.parse(datas.tasks), null, 2));
            }
        }
    });

});