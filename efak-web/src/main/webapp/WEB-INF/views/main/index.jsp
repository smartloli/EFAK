<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!doctype html>
<html lang="en">

<head>
    <!-- Required meta tags -->
    <jsp:include page="../public/pro/title.jsp">
        <jsp:param value="Dashboard" name="loader"/>
    </jsp:include>

    <!-- Required common css -->
    <jsp:include page="../public/pro/css.jsp"></jsp:include>
</head>

<body>
<!--start wrapper-->
<div class="wrapper">
    <!--start top header-->
    <jsp:include page="../public/pro/navtop.jsp"></jsp:include>
    <!--end top header-->

    <!--start sidebar -->
    <jsp:include page="../public/pro/navbar.jsp"></jsp:include>
    <!--end sidebar -->

    <!--start content-->
    <main class="page-content">

        <div class="row row-cols-1 row-cols-lg-2 row-cols-xl-2 row-cols-xxl-4">
            <div class="col">
                <div class="card overflow-hidden radius-10">
                    <div class="card-body">
                        <div class="d-flex align-items-stretch justify-content-between overflow-hidden">
                            <div class="w-50">
                                <p class="text-uppercase font-weight-bold">Brokers</p>
                                <a href="/cluster/kafka"><h4 id="efak_dashboard_panel_brokers"
                                                             class="">0</h4></a>
                            </div>
                            <div class="ms-auto widget-icon bg-light-tiffany text-white">
                                <i class="bi bi-hdd"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card overflow-hidden radius-10">
                    <div class="card-body">
                        <div class="d-flex align-items-stretch justify-content-between overflow-hidden">
                            <div class="w-50">
                                <p class="text-uppercase font-weight-bold">Topics</p>
                                <a href="/topic/list"><h4 id="efak_dashboard_panel_topics" class="">0</h4></a>
                            </div>
                            <div class="ms-auto widget-icon bg-light-info text-white">
                                <i class="bi bi-messenger"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card overflow-hidden radius-10">
                    <div class="card-body">
                        <div class="d-flex align-items-stretch justify-content-between overflow-hidden">
                            <div class="w-50">
                                <p class="text-uppercase font-weight-bold">Zookeepers</p>
                                <a href="/cluster/zookeeper"><h4 id="efak_dashboard_panel_zookeepers" class="">0</h4>
                                </a>
                            </div>
                            <div class="ms-auto widget-icon bg-light-success text-white">
                                <i class="bx bx-sitemap"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card overflow-hidden radius-10">
                    <div class="card-body">
                        <div class="d-flex align-items-stretch justify-content-between overflow-hidden">
                            <div class="w-50">
                                <p class="text-uppercase font-weight-bold">Consumers</p>
                                <a href="/consumers/groups"><h4 id="efak_dashboard_panel_consumers" class="">0</h4>
                                </a>
                            </div>
                            <div class="ms-auto widget-icon bg-light-orange text-white">
                                <i class="lni lni-users"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->

        <div class="row">
            <div class="col-12 col-lg-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Broker MessageIn</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a>
                                </div>
                            </div>
                        </div>
                        <div id="efak_dashboard_msg_in_chart"></div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Kafka OS</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div class="row row-cols-1 row-cols-md-2 g-3 mt-2 align-items-center">
                            <div class="col-lg-6 col-xl-6 col-xxl-6">
                                <div class="by-device-container">
                                    <div class="piechart-legend">
                                        <h2 id="efak_dashboard_mem_chart_id" class="mb-1">0.0%</h2>
                                        <h6 class="mb-0">Memory</h6>
                                    </div>
                                    <canvas id="efak_dashboard_mem_chart"></canvas>
                                </div>
                            </div>
                            <div class="col-lg-6 col-xl-6 col-xxl-6">
                                <div class="by-device-container">
                                    <div class="piechart-legend">
                                        <h2 id="efak_dashboard_cpu_chart_id" class="mb-1">0.0%</h2>
                                        <h6 class="mb-0">CPU</h6>
                                    </div>
                                    <canvas id="efak_dashboard_cpu_chart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->


        <div class="row">
            <div class="col-12 col-lg-6 col-xl-4 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Active Topics</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div id="efak_dashboard_active_topic_chart" class=""></div>
                        <div class="traffic-widget">
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">0B-100MB <span id="efak_dashboard_active_topic_mb"
                                                               class="float-end">0</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div id="efak_dashboard_active_topic_mb_div" class="progress-bar bg-primary"
                                         role="progressbar" style=""></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">100MB-10GB <span id="efak_dashboard_active_topic_gb"
                                                                 class="float-end">0</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div id="efak_dashboard_active_topic_gb_div" class="progress-bar bg-primary"
                                         role="progressbar" style=""></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-0">
                                <p class="mb-1">10GB+ <span id="efak_dashboard_active_topic_tb"
                                                            class="float-end">0</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div id="efak_dashboard_active_topic_tb_div" class="progress-bar bg-primary"
                                         role="progressbar" style=""></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-6 col-xl-4 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="card radius-10 border shadow-none mb-3">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="">
                                        <p class="mb-1">Byte In</p>
                                        <h4 id="efak_dashboard_byte_in_lastest" class="mb-0 text-primary">0</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                                class="bi bi-three-dots fs-4"></i></a>
                                        </div>
                                    </div>
                                </div>
                                <div id="efak_dashboard_byte_in_chart"></div>
                            </div>
                        </div>
                        <div class="card radius-10 border shadow-none mb-3">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="">
                                        <p class="mb-1">Byte Out</p>
                                        <h4 id="efak_dashboard_byte_out_lastest" class="mb-0 text-primary">0</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                                class="bi bi-three-dots fs-4"></i></a>
                                        </div>
                                    </div>
                                </div>
                                <div id="efak_dashboard_byte_out_chart"></div>
                            </div>
                        </div>
                        <div class="card radius-10 border shadow-none mb-0">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="">
                                        <p class="mb-1">FreePhysicalMemory</p>
                                        <h4 id="efak_dashboard_osfreememory_lastest" class="mb-0 text-primary">0</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                                class="bi bi-three-dots fs-4"></i></a>
                                        </div>
                                    </div>
                                </div>
                                <div id="efak_dashboard_osfree_memory_chart"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-12 col-xl-4 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic LogSize</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div id="efak_dashboard_logsize_chart" class=""></div>
                        <div class="d-flex align-items-center gap-5 justify-content-center mt-3 p-2 radius-10 border">
                            <div class="text-center">
                                <h3 id="efak_dashboard_active_topic_nums" class="mb-2 text-primary">0</h3>
                                <p class="mb-0">Active Topics</p>
                            </div>
                            <div class="border-end sepration"></div>
                            <div class="text-center">
                                <h3 id="efak_dashboard_standby_topic_nums" class="mb-2 text-primary-2">0</h3>
                                <p class="mb-0">Standby Topics</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->

        <div class="row">
            <div class="col-12 col-lg-12 col-xl-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic LogSize Top10</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table id="efak_dashboard_logsize_table" class="table align-middle mb-0">
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-12 col-xl-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic Capacity Top10</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table id="efak_dashboard_capacity_table" class="table align-middle mb-0">
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->

        <div class="row">
            <div class="col-12 col-lg-12 col-xl-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic ByteIn Top10</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table id="efak_dashboard_bytein_table" class="table align-middle mb-0">
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-12 col-xl-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic ByteOut Top10</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><a href="" class="panel-detail-a"><i
                                        class="bi bi-three-dots"></i></a></div>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table id="efak_dashboard_byteout_table" class="table align-middle mb-0">
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->


    </main>
    <!--end page main-->

    <!--start overlay-->
    <%--    <div class="overlay nav-toggle-icon"></div>--%>
    <!--end overlay-->

    <!--Start Back To Top Button-->
    <a href="javaScript:;" class="back-to-top"><i class='bx bxs-up-arrow-alt'></i></a>
    <!--End Back To Top Button-->

    <!--start switcher-->
    <%--    <jsp:include page="../public/pro/switch.jsp"></jsp:include>--%>
    <!--end switcher-->

</div>
<!--end wrapper-->

</body>
<!--end wrapper-->

<!-- import js and plugins -->
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="plugins/chartjs/Chart.min.js" name="loader"/>
    <jsp:param value="plugins/chartjs/Chart.extension.js" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="main/index/index.js?v=3.0.0" name="loader"/>
</jsp:include>
</body>

</html>
