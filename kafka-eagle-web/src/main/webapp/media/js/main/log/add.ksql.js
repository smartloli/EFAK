$(document).ready(function () {
    var mime = 'text/x-mariadb';
    // get mime type
    if (window.location.href.indexOf('mime=') > -1) {
        mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
    }
    var sqlEditor = CodeMirror.fromTextArea(document.getElementById('code'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        extraKeys: {
            "Alt-/": "autocomplete"
        }
    });

    $(document).on('click', 'button[id=ke_ksql_submit]', function () {
        var sql = encodeURI(sqlEditor.getValue());
        var jobId = "job_id_" + new Date().getTime();
        if (sql.length == 0) {
            return;
        }
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/topic/logical/commit/?sql=' + sql + '&jobId=' + jobId,
            success: function (datas) {
                if (datas != null) {
                    if (datas.error) {
                    } else {
                    }
                }
            }
        });
    });

});