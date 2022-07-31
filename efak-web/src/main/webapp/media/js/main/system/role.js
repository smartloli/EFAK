$(document).ready(function () {

    $("#efak_system_role_tab").dataTable({
        "searching": false,
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/system/role/table/ajax",
        "aoColumns": [{
            "mData": 'name'
        }, {
            "mData": 'describer'
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

    $(document).on('click', 'a[name=operater_modal]', function () {
        var href = $(this).attr("href");
        var id = href.split("#")[1];
        $('#ke_user_role_dialog').modal('show');
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/role/resource/' + id + '/ajax',
            success: function (datas) {
                if (datas != null) {
                    $('#ke_treeview_checkable').treeview({
                        data: datas,
                        showIcon: false,
                        showCheckbox: true,
                        onNodeChecked: function (event, node) {
                            updateRole('/system/role/insert/' + id + '/' + node.href + '/');
                        },
                        onNodeUnchecked: function (event, node) {
                            updateRole('/system/role/delete/' + id + '/' + node.href + '/');
                        }
                    });
                }
            }
        });
    });

    function updateRole(url) {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: url,
            success: function (datas) {
                if (datas != null) {
                    $("#ke_role_alert_info").html("");
                    $("#ke_role_alert_info").append("<label>" + datas.info + "</label>")
                    $("#ke_role_alert_info").show();
                    if (datas.code > 0) {
                        $("#ke_role_alert_info").addClass("alert alert-success");
                    } else {
                        $("#ke_role_alert_info").addClass("alert alert-danger");
                    }
                    setTimeout(function () {
                        $("#ke_role_alert_info").hide()
                    }, 3000);
                }
            }
        });
    }

});