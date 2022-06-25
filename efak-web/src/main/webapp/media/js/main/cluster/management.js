$(document).ready(function () {
    $("#efak_cluster_management_tab").dataTable({
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

    $(document).on('click', 'a[name=efak_cluster_switch]', function () {
        var href = $(this).attr("href");
        var clusterAlias = href.split("#")[1];
        $("#efak_cluster_switch_content").html("");
        $("#efak_cluster_switch_content").append("<div class='alert border-0 bg-light-warning alert-dismissable'><div class='text-warning'>Are you sure you want to switch cluster [ <strong>" + clusterAlias + "</strong> ] ?</div></div>");
        $("#efak_cluster_switch_submit_div").html("");
        $("#efak_cluster_switch_submit_div").append("<a href='/cluster/info/" + clusterAlias + "/change' class='btn btn-warning'>Submit</a>");
        $('#efak_cluster_switch_modal').modal({
            backdrop: 'static',
            keyboard: false
        });
        $('#efak_cluster_switch_modal').modal('show').css({
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translateX(-50%) translateY(-50%)'
        });
    });
});