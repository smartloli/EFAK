$(document).ready(function () {
    $("#cluster_tab").dataTable({
        // "searching" : false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/cluster/info/multicluster/ajax",
        "aoColumns": [{
            "mData": 'id'
        }, {
            "mData": 'clusterAlias'
        }, {
            "mData": 'zkhost'
        }, {
            "mData": 'operate'
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

    $(document).on('click', 'a[name=change]', function () {
        var href = $(this).attr("href");
        var clusterAlias = href.split("#")[1];
        $("#remove_div").html("");
        $("#remove_div").append("<a href='/cluster/info/" + clusterAlias + "/change' class='btn btn-success'>Sure</a>");
        $('#ke_cluster_switch').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#ke_cluster_switch').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });
    });
});