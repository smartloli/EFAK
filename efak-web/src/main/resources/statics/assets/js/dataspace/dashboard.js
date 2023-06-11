var pathname = window.location.pathname;
console.log(pathname);

// load dashboard panel: node,topics,consumers
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/panel/ajax',
        success: function (datas) {
            if (JSON.stringify(datas) === '{}') {
            } else {
                console.log(datas.brokers)
                $("#efak_dashboard_brokers_panel").text(datas.brokers);
                $("#efak_dashboard_brokers_onlines_panel").text(datas.onlines);
            }

        }
    });
} catch (e) {
    console.log(e);
}