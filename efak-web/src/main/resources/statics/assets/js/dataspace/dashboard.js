var pathname = window.location.pathname;
console.log(pathname);

// load dashboard panel: node,topics,consumers
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: pathname + '/panel/ajax',
        success: function (datas) {
            console.log(datas)
            if (JSON.stringify(datas) === '{}') {
            } else {

            }

        }
    });
} catch (e) {
    console.log(e);
}