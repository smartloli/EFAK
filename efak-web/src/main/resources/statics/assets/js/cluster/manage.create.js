// go clusters manage create
$("#efka_kafka_kraft_enable").click(function () {
    $("#efak_kraft_enable_checked").show();
    $("#efak_kraft_disable_checked").hide();
    $("#efka_kafka_kraft_enable").css({border: "1px solid #4372ff"});
    $("#efka_kafka_kraft_disable").css({border: ""});
});

$("#efka_kafka_kraft_disable").click(function () {
    $("#efak_kraft_enable_checked").hide();
    $("#efak_kraft_disable_checked").show();
    $("#efka_kafka_kraft_enable").css({border: ""});
    $("#efka_kafka_kraft_disable").css({border: "1px solid #4372ff"});
});

$("#efak_cluster_create_cancle").click(function () {
    window.location.href = '/clusters/manage';
});