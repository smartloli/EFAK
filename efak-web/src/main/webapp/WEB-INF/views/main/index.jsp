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
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Detail</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="chart5"></div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-6 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic Capacity Ratio</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Detail</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="row row-cols-1 row-cols-md-2 g-3 mt-2 align-items-center">
                            <div class="col-lg-7 col-xl-7 col-xxl-8">
                                <div class="by-device-container">
                                    <div class="piechart-legend">
                                        <h2 class="mb-1">85%</h2>
                                        <h6 class="mb-0">Total Capacity</h6>
                                    </div>
                                    <canvas id="chart6"></canvas>
                                </div>
                            </div>
                            <div class="col-lg-5 col-xl-5 col-xxl-4">
                                <div class="">
                                    <ul class="list-group list-group-flush">
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-tablet-landscape-fill me-2 text-primary"></i>
                                            <span>0B~1M - </span> <span>20%</span>
                                        </li>
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-phone-fill me-2 text-primary-2"></i>
                                            <span>1M~1G - </span> <span>40%</span>
                                        </li>
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-display-fill me-2 text-primary-3"></i>
                                            <span>1G~1T - </span> <span>30%</span>
                                        </li>
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-display-fill me-2 text-primary-4"></i>
                                            <span>1T~1P - </span> <span>10%</span>
                                        </li>
                                    </ul>
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
                            <h6 class="mb-0">Consumer Application</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Detail</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="chart7" class=""></div>
                        <div class="traffic-widget">
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">Running <span class="float-end">100</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 80%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">Shutdown <span class="float-end">20</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 65%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-0">
                                <p class="mb-1">Pending <span class="float-end">40</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 50%;"></div>
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
                                        <h4 class="mb-0 text-primary">289</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                                <div id="chart8"></div>
                            </div>
                        </div>
                        <div class="card radius-10 border shadow-none mb-3">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="">
                                        <p class="mb-1">Byte Out</p>
                                        <h4 class="mb-0 text-primary">489</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                                <div id="chart9"></div>
                            </div>
                        </div>
                        <div class="card radius-10 border shadow-none mb-0">
                            <div class="card-body">
                                <div class="d-flex align-items-center">
                                    <div class="">
                                        <p class="mb-1">Zookeeper Connections</p>
                                        <h4 class="mb-0 text-primary">149</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                                <div id="chart10"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-12 col-xl-4 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Topic Message</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="chart11" class=""></div>
                        <div class="d-flex align-items-center gap-5 justify-content-center mt-3 p-2 radius-10 border">
                            <div class="text-center">
                                <h3 class="mb-2 text-primary">8,546</h3>
                                <p class="mb-0">Active Topics</p>
                            </div>
                            <div class="border-end sepration"></div>
                            <div class="text-center">
                                <h3 class="mb-2 text-primary-2">3,723</h3>
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
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table class="table align-middle mb-0">
                                <thead class="table-light">
                                <tr>
                                    <th>RankID</th>
                                    <th>Topic Name</th>
                                    <th>LogSize</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>1</td>
                                    <td><a href="#">topic001</a></td>
                                    <td>2</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td><a href="#">topic002</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>3</td>
                                    <td><a href="#">topic003</a></td>
                                    <td>3</td>
                                </tr>
                                <tr>
                                    <td>4</td>
                                    <td><a href="#">topic004</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>5</td>
                                    <td><a href="#">topic005</a></td>
                                    <td>2</td>
                                </tr>
                                <tr>
                                    <td>6</td>
                                    <td><a href="#">topic006</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>7</td>
                                    <td><a href="#">topic007</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>8</td>
                                    <td><a href="#">topic001</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>9</td>
                                    <td><a href="#">topic009</a></td>
                                    <td>1</td>
                                </tr>
                                <tr>
                                    <td>10</td>
                                    <td><a href="#">topic010</a></td>
                                    <td>1</td>
                                </tr>
                                </tbody>
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
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table class="table align-middle mb-0">
                                <thead class="table-light">
                                <tr>
                                    <th>RankID</th>
                                    <th>Topic Name</th>
                                    <th>Capacity</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>1</td>
                                    <td><a href="#">topic001</a></td>
                                    <td>2MB</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td><a href="#">topic002</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>3</td>
                                    <td><a href="#">topic003</a></td>
                                    <td>3MB</td>
                                </tr>
                                <tr>
                                    <td>4</td>
                                    <td><a href="#">topic004</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>5</td>
                                    <td><a href="#">topic005</a></td>
                                    <td>2MB</td>
                                </tr>
                                <tr>
                                    <td>6</td>
                                    <td><a href="#">topic006</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>7</td>
                                    <td><a href="#">topic007</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>8</td>
                                    <td><a href="#">topic008</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>9</td>
                                    <td><a href="#">topic009</a></td>
                                    <td>1MB</td>
                                </tr>
                                <tr>
                                    <td>10</td>
                                    <td><a href="#">topic010</a></td>
                                    <td>1MB</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->


    </main>
    <!--end page main-->

    <!--start overlay-->
    <div class="overlay nav-toggle-icon"></div>
    <!--end overlay-->

    <!--Start Back To Top Button-->
    <a href="javaScript:;" class="back-to-top"><i class='bx bxs-up-arrow-alt'></i></a>
    <!--End Back To Top Button-->

    <!--start switcher-->
    <%--    <jsp:include page="../public/pro/switch.jsp"></jsp:include>--%>
    <!--end switcher-->

    <!-- start footer -->
    <jsp:include page="../public/pro/footer.jsp"></jsp:include>
    <!-- end footer -->
</div>
<!--end wrapper-->

</body>
<!--end wrapper-->

<!-- import js and plugins -->
<jsp:include page="../public/pro/script.jsp">
    <jsp:param value="plugins/chartjs/Chart.min.js" name="loader"/>
    <jsp:param value="plugins/chartjs/Chart.extension.js" name="loader"/>
    <jsp:param value="plugins/apexcharts-bundle/apexcharts.min.js" name="loader"/>
    <jsp:param value="main/index/index.js" name="loader"/>
    <jsp:param value="main/index/data.js" name="loader"/>
</jsp:include>
</body>

</html>
