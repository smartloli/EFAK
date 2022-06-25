$(document).ready(function () {
    try {
        // Initialize consumer table list --start
        $("#efak_consumer_table_result").dataTable({
            // "searching" : false,
            "bSort": false,
            "bLengthChange": false,
            "bProcessing": true,
            "bServerSide": true,
            "fnServerData": retrieveData,
            "sAjaxSource": "/consumer/list/table/ajax",
            "aoColumns": [{
                "mData": 'id'
            }, {
                "mData": 'group'
            }, {
                "mData": 'topics'
            }, {
                "mData": 'node'
            }, {
                "mData": 'activeTopics'
            }, {
                "mData": 'activeThreads'
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

        // --end
    } catch (e) {
        console.log("Get consumer group has error, msg is " + e)
    }

    // Children div show details of the consumer group
    var offset = 0;
    $(document).on('click', 'a[class=link]', function () {
        var group = $(this).attr("group");
        $('#ke_consumer_topics_detail').modal('show');

        $("#consumer_detail_children").append("<div class='table-responsive' id='div_children" + offset + "'><table id='result_children" + offset + "' class='table table-striped table-bordered' width='100%'><thead><tr><th>#ID</th><th>Topic</th><th>Consumer Status</th></tr></thead></table></div>");
        if (offset > 0) {
            $("#div_children" + (offset - 1)).remove();
        }

        // Initialize consumer table list --start
        $("#result_children" + offset).dataTable({
            // "searching" : false,
            "bSort": false,
            "bLengthChange": false,
            "bProcessing": true,
            "bServerSide": true,
            "fnServerData": retrieveData,
            "sAjaxSource": "/consumer/group/table/ajax",
            "aoColumns": [{
                "mData": 'id'
            }, {
                "mData": 'topic'
            }, {
                "mData": 'isConsumering'
            }]
        });

        function retrieveData(sSource, aoData, fnCallback) {
            $.ajax({
                "type": "get",
                "contentType": "application/json",
                "url": sSource,
                "dataType": "json",
                "data": {
                    aoData: JSON.stringify(aoData),
                    group: group
                },
                "success": function (data) {
                    fnCallback(data)
                }
            });
        }

        // --end

        offset++;
    });

    // consumer active and standby
    function consumerStatusCnt() {
        try {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/consumers/group/status/info/ajax',
                success: function (datas) {
                    if (datas != null) {
                        console.log(datas);
                        $("#efak_consumer_groups_active").text(datas.active);
                        $("#efak_consumer_groups_standby").text(datas.standby);
                    }
                }
            });
        } catch (e) {
            console.log(e);
        }
    }

    consumerStatusCnt();
});