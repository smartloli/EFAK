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
                                <p>Total Orders</p>
                                <h4 class="">8,542</h4>
                            </div>
                            <div class="w-50">
                                <p class="mb-3 float-end text-success">+ 16% <i class="bi bi-arrow-up"></i></p>
                                <div id="chart1"></div>
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
                                <p>Total Views</p>
                                <h4 class="">12.5M</h4>
                            </div>
                            <div class="w-50">
                                <p class="mb-3 float-end text-danger">- 3.4% <i class="bi bi-arrow-down"></i></p>
                                <div id="chart2"></div>
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
                                <p>Revenue</p>
                                <h4 class="">$64.5K</h4>
                            </div>
                            <div class="w-50">
                                <p class="mb-3 float-end text-success">+ 24% <i class="bi bi-arrow-up"></i></p>
                                <div id="chart3"></div>
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
                                <p>Customers</p>
                                <h4 class="">25.8K</h4>
                            </div>
                            <div class="w-50">
                                <p class="mb-3 float-end text-success">+ 8.2% <i class="bi bi-arrow-up"></i></p>
                                <div id="chart4"></div>
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
                            <h6 class="mb-0">Revenue</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
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
                            <h6 class="mb-0">By Device</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="row row-cols-1 row-cols-md-2 g-3 mt-2 align-items-center">
                            <div class="col-lg-7 col-xl-7 col-xxl-8">
                                <div class="by-device-container">
                                    <div class="piechart-legend">
                                        <h2 class="mb-1">85%</h2>
                                        <h6 class="mb-0">Total Visitors</h6>
                                    </div>
                                    <canvas id="chart6"></canvas>
                                </div>
                            </div>
                            <div class="col-lg-5 col-xl-5 col-xxl-4">
                                <div class="">
                                    <ul class="list-group list-group-flush">
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-tablet-landscape-fill me-2 text-primary"></i>
                                            <span>Tablet - </span> <span>22.5%</span>
                                        </li>
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-phone-fill me-2 text-primary-2"></i> <span>Mobile - </span>
                                            <span>62.3%</span>
                                        </li>
                                        <li class="list-group-item d-flex align-items-center justify-content-between border-0">
                                            <i class="bi bi-display-fill me-2 text-primary-3"></i>
                                            <span>Desktop - </span> <span>15.2%</span>
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
                            <h6 class="mb-0">Traffic Source</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="chart7" class=""></div>
                        <div class="traffic-widget">
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">Social <span class="float-end">8,475</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 80%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">Direct <span class="float-end">7,989</span></p>
                                <div class="progress rounded-0" style="height: 8px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 65%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-0">
                                <p class="mb-1">Search <span class="float-end">6,359</span></p>
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
                                        <p class="mb-1">Messages</p>
                                        <h4 class="mb-0 text-primary">289</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Another action</a>
                                            </li>
                                            <li>
                                                <hr class="dropdown-divider">
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Something else here</a>
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
                                        <p class="mb-1">Total Posts</p>
                                        <h4 class="mb-0 text-primary">489</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Another action</a>
                                            </li>
                                            <li>
                                                <hr class="dropdown-divider">
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Something else here</a>
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
                                        <p class="mb-1">New Tasks</p>
                                        <h4 class="mb-0 text-primary">149</h4>
                                    </div>
                                    <div class="dropdown ms-auto">
                                        <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                             data-bs-toggle="dropdown"><i class="bi bi-three-dots fs-4"></i>
                                        </div>
                                        <ul class="dropdown-menu">
                                            <li><a class="dropdown-item" href="javascript:;">Action</a>
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Another action</a>
                                            </li>
                                            <li>
                                                <hr class="dropdown-divider">
                                            </li>
                                            <li><a class="dropdown-item" href="javascript:;">Something else here</a>
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
                            <h6 class="mb-0">Visitors</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="chart11" class=""></div>
                        <div class="d-flex align-items-center gap-5 justify-content-center mt-3 p-2 radius-10 border">
                            <div class="text-center">
                                <h3 class="mb-2 text-primary">8,546</h3>
                                <p class="mb-0">New Visitors</p>
                            </div>
                            <div class="border-end sepration"></div>
                            <div class="text-center">
                                <h3 class="mb-2 text-primary-2">3,723</h3>
                                <p class="mb-0">Old Visitors</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div><!--end row-->

        <div class="row">
            <div class="col-12 col-lg-12 col-xl-8 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Recent Orders</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
                                </ul>
                            </div>
                        </div>
                        <div class="table-responsive mt-2">
                            <table class="table align-middle mb-0">
                                <thead class="table-light">
                                <tr>
                                    <th>#ID</th>
                                    <th>Product</th>
                                    <th>Quantity</th>
                                    <th>Price</th>
                                    <th>Date</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>#89742</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/11.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Smart Mobile Phone</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>2</td>
                                    <td>$214</td>
                                    <td>Apr 8, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>#68570</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/07.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Sports Time Watch</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>1</td>
                                    <td>$185</td>
                                    <td>Apr 9, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>#38567</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/17.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Women Red Heals</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>3</td>
                                    <td>$356</td>
                                    <td>Apr 10, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>#48572</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/04.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Yellow Winter Jacket</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>1</td>
                                    <td>$149</td>
                                    <td>Apr 11, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>#96857</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/10.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Orange Micro Headphone</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>2</td>
                                    <td>$199</td>
                                    <td>Apr 15, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td>#96857</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3">
                                            <div class="product-box border">
                                                <img src="assets/images/products/12.png" alt="">
                                            </div>
                                            <div class="product-info">
                                                <h6 class="product-name mb-1">Pro Samsung Laptop</h6>
                                            </div>
                                        </div>
                                    </td>
                                    <td>1</td>
                                    <td>$699</td>
                                    <td>Apr 18, 2021</td>
                                    <td>
                                        <div class="d-flex align-items-center gap-3 fs-6">
                                            <a href="javascript:;" class="text-primary" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="View detail"
                                               aria-label="Views"><i class="bi bi-eye-fill"></i></a>
                                            <a href="javascript:;" class="text-warning" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Edit info"
                                               aria-label="Edit"><i class="bi bi-pencil-fill"></i></a>
                                            <a href="javascript:;" class="text-danger" data-bs-toggle="tooltip"
                                               data-bs-placement="bottom" title="" data-bs-original-title="Delete"
                                               aria-label="Delete"><i class="bi bi-trash-fill"></i></a>
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-12 col-xl-4 d-flex">
                <div class="card radius-10 w-100">
                    <div class="card-body">
                        <div class="d-flex align-items-center">
                            <h6 class="mb-0">Sales By Country</h6>
                            <div class="fs-5 ms-auto dropdown">
                                <div class="dropdown-toggle dropdown-toggle-nocaret cursor-pointer"
                                     data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></div>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#">Action</a></li>
                                    <li><a class="dropdown-item" href="#">Another action</a></li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li><a class="dropdown-item" href="#">Something else here</a></li>
                                </ul>
                            </div>
                        </div>
                        <div id="geographic-map" class="mt-2"></div>
                        <div class="traffic-widget">
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">United States <span class="float-end">$2.5K</span></p>
                                <div class="progress rounded-0" style="height: 6px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 75%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-3">
                                <p class="mb-1">Russia <span class="float-end">$4.5K</span></p>
                                <div class="progress rounded-0" style="height: 6px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 55%;"></div>
                                </div>
                            </div>
                            <div class="progress-wrapper mb-0">
                                <p class="mb-1">Australia <span class="float-end">$8.5K</span></p>
                                <div class="progress rounded-0" style="height: 6px;">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 80%;"></div>
                                </div>
                            </div>
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
    <div class="switcher-body">
        <button class="btn btn-primary btn-switcher shadow-sm" type="button" data-bs-toggle="offcanvas"
                data-bs-target="#offcanvasScrolling" aria-controls="offcanvasScrolling"><i
                class="bi bi-paint-bucket me-0"></i></button>
        <div class="offcanvas offcanvas-end shadow border-start-0 p-2" data-bs-scroll="true" data-bs-backdrop="false"
             tabindex="-1" id="offcanvasScrolling">
            <div class="offcanvas-header border-bottom">
                <h5 class="offcanvas-title" id="offcanvasScrollingLabel">Theme Customizer</h5>
                <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas"></button>
            </div>
            <div class="offcanvas-body">
                <h6 class="mb-0">Theme Variation</h6>
                <hr>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="LightTheme"
                           value="option1" checked>
                    <label class="form-check-label" for="LightTheme">Light</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="DarkTheme"
                           value="option2">
                    <label class="form-check-label" for="DarkTheme">Dark</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="SemiDarkTheme"
                           value="option3">
                    <label class="form-check-label" for="SemiDarkTheme">Semi Dark</label>
                </div>
                <hr>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="inlineRadioOptions" id="MinimalTheme"
                           value="option3">
                    <label class="form-check-label" for="MinimalTheme">Minimal Theme</label>
                </div>
                <hr/>
                <h6 class="mb-0">Header Colors</h6>
                <hr/>
                <div class="header-colors-indigators">
                    <div class="row row-cols-auto g-3">
                        <div class="col">
                            <div class="indigator headercolor1" id="headercolor1"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor2" id="headercolor2"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor3" id="headercolor3"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor4" id="headercolor4"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor5" id="headercolor5"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor6" id="headercolor6"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor7" id="headercolor7"></div>
                        </div>
                        <div class="col">
                            <div class="indigator headercolor8" id="headercolor8"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!--end switcher-->

    <!-- add footer -->
    <%--    <jsp:include page="../public/pro/footer.jsp"></jsp:include>--%>
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
