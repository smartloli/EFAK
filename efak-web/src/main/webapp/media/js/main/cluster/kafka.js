$(document).ready(function () {
    $("#efak_cluster_kafka_tab").dataTable({
        // "searching" : false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/cluster/info/kafka/ajax",
        "aoColumns": [{
            "mData": 'brokerId'
        }, {
            "mData": 'ip'
        }, {
            "mData": 'port'
        }, {
            "mData": 'jmxPort'
        }, {
            "mData": 'jmxPortStatus'
        }, {
            "mData": 'memory'
        }, {
            "mData": 'cpu'
        }, {
            "mData": 'created'
        }, {
            "mData": 'modify'
        }, {
            "mData": 'version'
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