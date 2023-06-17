// go clusters manage create
// batch import
try {
    var dr = $("#efak_kafka_broker_batch_import").dropify({
        messages: {
            'default': '将文件拖放到此处或单击',
            'replace': '拖放或单击文件来替换',
            'remove': '删除文件',
            'error': '抱歉，上传出现异常'
        },
        error: {
            'fileSize': '上传文件大小超过最大值 ({{ value }})',
            'fileExtension': '文件类型不正确，仅支持 {{ value }} 文件类型',
        }
    });
    dr.on('change', function () {
        var file = this.files[0];
        console.log(file);
    });
} catch (e) {
    console.log(e)
}

var cid = "";

try {
    url = window.location.href;
    url_cid = url;
    if (url.indexOf("#") > -1) {
        url_cid = url.split("#")[1];
    }
    if (url_cid.indexOf("?") > -1) {
        cid = url_cid.split("=")[1]
    }
    $("#efak_clusterid").val(cid);
    $("#cid").val(cid);

    console.log(cid);
} catch (e) {
    console.log(e);
}

$("#efak_cluster_name_create").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

$("#efak_cluster_manage_tbl").DataTable({
    "bSort": false,
    "bLengthChange": false,
    "bProcessing": true,
    "bServerSide": true,
    "fnServerData": retrieveData,
    "sAjaxSource": "/clusters/manage/brokers/table/ajax?cid=" + cid,
    "aoColumns": [{
        "mData": 'brokerId'
    }, {
        "mData": 'brokerHost'
    }, {
        "mData": 'brokerPort'
    }, {
        "mData": 'brokerJmxPort'
    }, {
        "mData": 'modify'
    }, {
        "mData": 'operate'
    }],
    language: {
        "sProcessing": "处理中...",
        "sLengthMenu": "显示 _MENU_ 项结果",
        "sZeroRecords": "没有匹配结果",
        "sInfo": "显示第 _START_ 至 _END_ 项结果，共 _TOTAL_ 项",
        "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
        "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
        "sInfoPostFix": "",
        "sSearch": "搜索:",
        "sUrl": "",
        "sEmptyTable": "表中数据为空",
        "sLoadingRecords": "载入中...",
        "sInfoThousands": ",",
        "oPaginate": {
            "sFirst": "首页",
            "sPrevious": "上页",
            "sNext": "下页",
            "sLast": "末页"
        },
        "oAria": {
            "sSortAscending": ": 以升序排列此列",
            "sSortDescending": ": 以降序排列此列"
        }
    }
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

function enableAuth() {
    $("#efak_kraft_enable_checked").show();
    $("#efak_kraft_disable_checked").hide();
    $("#efak_kafka_kraft_enable").css({border: "1px solid #4372ff"});
    $("#efak_kafka_kraft_disable").css({border: ""});
    $("#efak_cluster_auth_isopen").val("Y");
}

function disableAuth() {
    $("#efak_kraft_enable_checked").hide();
    $("#efak_kraft_disable_checked").show();
    $("#efak_kafka_kraft_enable").css({border: ""});
    $("#efak_kafka_kraft_disable").css({border: "1px solid #4372ff"});
    $("#efak_cluster_auth_isopen").val("N");
}

// load cluster info
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/clusters/manage/cluster/info/ajax?cid=' + cid,
        success: function (datas) {
            console.log()
            if (JSON.stringify(datas) === '{}') {
                $("#efak_cluster_edit").val("newCreate");
            } else {
                $("#efak_cluster_name_create").val(datas.clusterName);
                $("#efak_cluster_auth_isopen").val(datas.auth);
                $("#efak_kafka_auth_config").val(datas.authConfig);
                $("#efak_cluster_edit").val("oldCreate");
                if (datas.auth.indexOf("Y") > -1) {
                    enableAuth();
                } else if (datas.auth.indexOf("N") > -1) {
                    disableAuth()
                }
            }

        }
    });
} catch (e) {
    console.log(e);
}


// load already nodes
try {
    $.ajax({
        type: 'get',
        dataType: 'json',
        url: '/clusters/manage/cluster/size/ajax?cid=' + cid,
        success: function (datas) {
            $("#efak_cluster_create_exist_nodes").html(datas.nodes);
        }
    });
} catch (e) {
    console.log(e);
}

$("#efak_kafka_kraft_enable").click(function () {
    enableAuth();
});

$("#efak_kafka_kraft_disable").click(function () {
    disableAuth();
});

$("#efak_cluster_create_cancle").click(function () {
    window.location.href = '/clusters/manage';
});

// import is null alert
function importFilesIsNull() {
    var file = $("#efak_kafka_broker_batch_import")[0];
    if (file.value == "") {
        // alert("请选择文件");
        return false;
    }
    return true;
}

// download template
$("#efak_batch_module_download").click(function () {
    downloadFiles('../../assets/files/kafka_broker_example.xlsx', 'Kafka集群批量导入模板.xlsx');
});

function downloadFiles(url, filename) {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    document.body.removeChild(link);
}


function delNoti(dataid, brokerId, brokerHost) {
    Swal.fire({
        customClass: {
            confirmButton: 'efak-noti-custom-btn-submit'
        },
        buttonsStyling: false,
        title: '确定执行删除操作吗?',
        html: "节点 ID[<code>" + brokerId + "</code>] 和节点 IP[<code>" + brokerHost + "</code>] 删除后不能被恢复!",
        icon: 'warning',
        showCloseButton: true,
        showCancelButton: false,
        focusConfirm: false,
        cancelButtonClass: 'me-2',
        confirmButtonText: '删除',
        reverseButtons: true,
        scrollbarPadding: false
    }).then((result) => {
        if (result.isConfirmed) {
            // send ajax request
            $.ajax({
                url: '/clusters/manage/broker/del',
                method: 'POST',
                data: {
                    dataid: dataid
                },
                success: function (response) {
                    Swal.fire({
                        title: '成功',
                        icon: 'success',
                        html: '节点ID[<code>' + brokerId + '</code>]和节点IP[<code>' + brokerHost + '</code>]已被删除',
                        allowOutsideClick: false
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.reload();
                        }
                    });
                },
                error: function (xhr, status, error) {
                    Swal.fire('失败', '数据删除发生异常', 'error');
                }
            });
        }
    })
}

// alert noti
function alertNoti(msg, icon) {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer)
            toast.addEventListener('mouseleave', Swal.resumeTimer)
        }
    })

    // error or success
    Toast.fire({
        icon: icon,
        title: msg
    })
}

// delete broker
$(document).on('click', 'a[name=efak_cluster_node_manage_del]', function (event) {
    event.preventDefault();
    var dataid = $(this).attr("dataid");
    var brokerId = $(this).attr("brokerId");
    var brokerHost = $(this).attr("brokerHost");
    delNoti(dataid, brokerId, brokerHost);
});

// edit node
$(document).on('click', 'a[name=efak_cluster_node_manage_edit]', function (event) {
    event.preventDefault();
    var dataid = $(this).attr("dataid");
    var clusterId = $(this).attr("cid");
    var brokerId = $(this).attr("brokerId");
    var brokerHost = $(this).attr("brokerHost");
    var brokerPort = $(this).attr("brokerPort");
    var brokerJmxPort = $(this).attr("brokerJmxPort");

    $('#efak_kafka_broker_add_modal').modal('show');
    $("#efak_clusterid").val(clusterId);
    $("#efak_brokerid_label").val(brokerId);
    $("#efak_brokerhost_label").val(brokerHost);
    $("#efak_brokerport_label").val(brokerPort);
    $("#efak_brokerjmxport_label").val(brokerJmxPort);
    $("#efak_dataid").val(dataid);
});

// create cluster
$("#efak_cluster_create_submit").click(function () {
    var clusterName = $("#efak_cluster_name_create").val();
    var auth = $("#efak_cluster_auth_isopen").val();
    var authConfig = $("#efak_kafka_auth_config").val();
    var newCreate = $("#efak_cluster_edit").val(); // newCreate
    if (clusterName.length == 0) {
        alertNoti("集群名称不能为空", "error");
        return;
    }
    if (auth.length == 0) {
        alertNoti("请选择是否开启认证", "error");
        return;
    }
    if (auth.length > 0 && auth.indexOf("Y") > -1) {
        if (authConfig.length == 0) {
            alertNoti("请输入认证配置", "error");
            return;
        }
    }

    $.ajax({
        url: '/clusters/manage/cluster/create',
        method: 'POST',
        data: {
            cid: cid,
            clusterName: clusterName,
            auth: auth,
            authConfig: authConfig,
            newCreate: newCreate
        },
        success: function (response) {
            Swal.fire({
                title: '成功',
                icon: 'success',
                html: '集群名称[<code>' + clusterName + '</code>]创建完成',
                allowOutsideClick: false
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = '/clusters/manage';
                }
            });
        },
        error: function (xhr, status, error) {
            Swal.fire('失败', '创建集群发生异常', 'error');
        }
    });
});