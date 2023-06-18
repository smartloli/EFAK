$("#efak_topic_name_create").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

$("#efak_topic_name_create").maxlength({
    warningClass: "badge mt-1 bg-success",
    limitReachedClass: "badge mt-1 bg-danger"
});

if ($("#efak_topic_name_retain_unit_create").length) {
    $("#efak_topic_name_retain_unit_create").select2();
}

$("#efak_topic_create_cancle").click(function () {
    window.location.href = '/topic/list';
});