(function ($) {
    var path = window.location.href;
    var url = path.split(window.location.host)[1];
    if ("/".indexOf(url) > -1) { // dashboard
        $("#ke_navbar_dash").addClass("active");
    } else if (url.indexOf("/topic") > -1) {
        $("#collapseTopics").addClass('show')
        if (url.indexOf("/topic/create") > -1) {
            $("#ke_navbar_topic_create").addClass("active");
        } else if (url.indexOf("/topic/list") > -1 || url.indexOf("/topic/meta") > -1) {
            $("#ke_navbar_topic_list").addClass("active");
        } else if (url.indexOf("/topic/message") > -1) {
            $("#ke_navbar_topic_ksql").addClass("active");
        } else if (url.indexOf("/topic/mock") > -1) {
            $("#ke_navbar_topic_mock").addClass("active");
        } else if (url.indexOf("/topic/manager") > -1) {
            $("#ke_navbar_topic_manager").addClass("active");
        } else if (url.indexOf("/topic/hub") > -1) {
            $("#ke_navbar_topic_hub").addClass("active");
        }
    } else if (url.indexOf("/log") > -1) {
        $("#collapseLog").addClass('show')
        $("#ke_navbar_topic_tasks").addClass("active");
    } else if (url.indexOf("/consumers") > -1) {
        $("#ke_navbar_consumers").addClass("active");
    } else if ((url.indexOf("/cluster") > -1)) {
        $("#collapseCluster").addClass('show')
        if (url.indexOf("/cluster/info") > -1) {
            $("#ke_navbar_cluster_info").addClass("active");
        } else if (url.indexOf("/cluster/multi") > -1) {
            $("#ke_navbar_cluster_multi").addClass("active");
        } else if (url.indexOf("/cluster/zkcli") > -1) {
            $("#ke_navbar_cluster_zkcli").addClass("active");
        } else if (url.indexOf("/cluster/worknodes") > -1) {
            $("#ke_navbar_cluster_worknodes").addClass("active");
        }
    } else if ((url.indexOf("/metrics") > -1)) {
        $("#collapseMetrics").addClass('show')
        if (url.indexOf("/metrics/brokers") > -1) {
            $("#ke_navbar_metrics_brokers").addClass("active");
        } else if (url.indexOf("/metrics/kafka") > -1) {
            $("#ke_navbar_metrics_kafka").addClass("active");
        } else if (url.indexOf("/metrics/zk") > -1) {
            $("#ke_navbar_metrics_zk").addClass("active");
        }
    } else if (url.indexOf("/connect") > -1) {
        $("#collapseConnect").addClass('show')
        if (url.indexOf("/connect/config") > -1 || url.indexOf("/connect/connectors") > -1) {
            $("#ke_navbar_connect_config").addClass("active");
        }
    } else if ((url.indexOf("/alarm/config") > -1) || (url.indexOf("/alarm/list") > -1)) {
        $("#collapseAlarmChannel").addClass('show')
        if (url.indexOf("/alarm/config") > -1) {
            $("#ke_navbar_alarm_config").addClass("active");
        } else if (url.indexOf("/alarm/list") > -1) {
            $("#ke_navbar_alarm_list").addClass("active");
        }
    } else if ((url.indexOf("/alarm/add") > -1) || (url.indexOf("/alarm/modify") > -1)) {
        $("#collapseAlarmConsumer").addClass('show')
        if (url.indexOf("/alarm/add") > -1) {
            $("#ke_navbar_alarm_add").addClass("active");
        } else if (url.indexOf("/alarm/modify") > -1) {
            $("#ke_navbar_alarm_modify").addClass("active");
        }
    } else if ((url.indexOf("/alarm/create") > -1) || (url.indexOf("/alarm/history") > -1)) {
        $("#collapseAlarmCluster").addClass('show')
        if (url.indexOf("/alarm/create") > -1) {
            $("#ke_navbar_alarm_create").addClass("active");
        } else if (url.indexOf("/alarm/history") > -1) {
            $("#ke_navbar_alarm_history").addClass("active");
        }
    } else if ((url.indexOf("/system") > -1)) {
        $("#collapseSystem").addClass('show')
        if (url.indexOf("/system/user") > -1) {
            $("#ke_navbar_system_user").addClass("active");
        } else if (url.indexOf("/system/role") > -1) {
            $("#ke_navbar_system_role").addClass("active");
        } else if (url.indexOf("/system/resource") > -1) {
            $("#ke_navbar_system_resource").addClass("active");
        }
    }


    // Toggle the side navigation
    $("#sidebarToggle").on("click", function (e) {
        e.preventDefault();
        $("body").toggleClass("sb-sidenav-toggled");
    });

    // Reset account
    $(document).on('click', 'a[name=ke_account_reset]', function () {
        $('#ke_account_reset_dialog').modal('show');
        $(".modal-backdrop").css({
            "z-index": "999"
        });
    });

    // resize listen
    (function ($, h, c) {
        var a = $([]),
            e = $.resize = $.extend($.resize, {}),
            i,
            k = "setTimeout",
            j = "resize",
            d = j + "-special-event",
            b = "delay",
            f = "throttleWindow";
        e[b] = 250;
        e[f] = true;
        $.event.special[j] = {
            setup: function () {
                if (!e[f] && this[k]) {
                    return false;
                }
                var l = $(this);
                a = a.add(l);
                $.data(this, d, {
                    w: l.width(),
                    h: l.height()
                });
                if (a.length === 1) {
                    g();
                }
            },
            teardown: function () {
                if (!e[f] && this[k]) {
                    return false;
                }
                var l = $(this);
                a = a.not(l);
                l.removeData(d);
                if (!a.length) {
                    clearTimeout(i);
                }
            },
            add: function (l) {
                if (!e[f] && this[k]) {
                    return false;
                }
                var n;

                function m(s, o, p) {
                    var q = $(this),
                        r = $.data(this, d);
                    r.w = o !== c ? o : q.width();
                    r.h = p !== c ? p : q.height();
                    n.apply(this, arguments);
                }

                if ($.isFunction(l)) {
                    n = l;
                    return m;
                } else {
                    n = l.handler;
                    l.handler = m;
                }
            }
        };

        function g() {
            i = h[k](function () {
                    a.each(function () {
                        var n = $(this),
                            m = n.width(),
                            l = n.height(),
                            o = $.data(this, d);
                        if (m !== o.w || l !== o.h) {
                            n.trigger(j, [o.w = m, o.h = l]);
                        }
                    });
                    g();
                },
                e[b]);
        }
    })(jQuery, this);

})(jQuery);
