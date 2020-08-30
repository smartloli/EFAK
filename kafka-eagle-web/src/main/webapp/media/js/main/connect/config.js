$(document).ready(function () {
    $("#result").dataTable({
        "bSort": false,
        "bLengthChange": false,
        "bProcessing": true,
        "bServerSide": true,
        "fnServerData": retrieveData,
        "sAjaxSource": "/connect/uri/table/ajax",
        "aoColumns": [{
            "mData": 'uri'
        }, {
            "mData": 'version'
        }, {
            "mData": 'commit'
        }, {
            "mData": 'created'
        }, {
            "mData": 'modify'
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

    $("#ke-add-connect-uri-btn").click(function () {
        $('#ke_connect_uri_add_dialog').modal('show');
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/signin/rtxno/ajax/',
            success: function (datas) {
                $("#ke_rtxno_name").val(datas.rtxno);
                $("#ke_rtxno_name").attr("readonly", "readonly");
            }
        });
    });

    $(document).on('click', 'a[name=operater_modify_modal]', function () {
        $('#ke_user_modify_dialog').modal('show');
        var href = $(this).attr("href");
        var id = href.split("#")[1];
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/system/user/signin/' + id + '/ajax',
            success: function (datas) {
                $("#ke_rtxno_name_modify").val(datas.rtxno);
                $("#ke_rtxno_name_modify").attr("readonly", "readonly");
                $("#ke_real_name_modify").val(datas.realname);
                $("#ke_user_name_modify").val(datas.username);
                $("#ke_user_email_modify").val(datas.email);
                $("#ke_user_id_modify").val(id);
            }
        });

    });
});