$(document).ready(function () {
    $("#cluster_tab").dataTable({
        // "searching" : false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/cluster/info/worknodes/ajax",
        "aoColumns": [{
            "mData": 'id'
        }, {
            "mData": 'ip'
        }, {
            "mData": 'port'
        }, {
            "mData": 'memory'
        }, {
            "mData": 'cpu'
        }, {
            "mData": 'status'
        }, {
            "mData": 'created'
        }]
    });

    function retrieveData(sSource, aoData, fnCallback) {
        $.ajax({
            "type": "get",
            "contentType": "application/json",
            "url": sSource,
            "dataType": "json",
            "data": {
                aoData: JSON.stringify(aoData)
            },
            "success": function (data) {
                fnCallback(data)
            }
        });
    }
});