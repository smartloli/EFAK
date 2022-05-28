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
    <header class="top-header">
        <nav class="navbar navbar-expand gap-3">
            <div class="mobile-toggle-icon fs-3">
                <i class="bi bi-list"></i>
            </div>
            <div class="top-navbar-right ms-auto">
                <ul class="navbar-nav align-items-center">
                    <li class="nav-item search-toggle-icon">
                        <a class="nav-link" href="#">
                            <div class="">
                                <i class="bi bi-search"></i>
                            </div>
                        </a>
                    </li>
                    <li class="nav-item dropdown dropdown-user-setting">
                        <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                            <div class="user-setting d-flex align-items-center">
                                <img src="assets/images/avatars/avatar-1.png" class="user-img" alt="">
                            </div>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-1.png" alt="" class="rounded-circle"
                                             width="54" height="54">
                                        <div class="ms-3">
                                            <h6 class="mb-0 dropdown-user-name">Jhon Deo</h6>
                                            <small class="mb-0 dropdown-user-designation text-secondary">HR
                                                Manager</small>
                                        </div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <hr class="dropdown-divider">
                            </li>
                            <li>
                                <a class="dropdown-item" href="pages-user-profile.html">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-person-fill"></i></div>
                                        <div class="ms-3"><span>Profile</span></div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-gear-fill"></i></div>
                                        <div class="ms-3"><span>Setting</span></div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item" href="index2.html">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-speedometer"></i></div>
                                        <div class="ms-3"><span>Dashboard</span></div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-piggy-bank-fill"></i></div>
                                        <div class="ms-3"><span>Earnings</span></div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-cloud-arrow-down-fill"></i></div>
                                        <div class="ms-3"><span>Downloads</span></div>
                                    </div>
                                </a>
                            </li>
                            <li>
                                <hr class="dropdown-divider">
                            </li>
                            <li>
                                <a class="dropdown-item" href="authentication-signup-with-header-footer.html">
                                    <div class="d-flex align-items-center">
                                        <div class=""><i class="bi bi-lock-fill"></i></div>
                                        <div class="ms-3"><span>Logout</span></div>
                                    </div>
                                </a>
                            </li>
                        </ul>
                    </li>
                    <li class="nav-item dropdown dropdown-large">
                        <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                            <div class="projects">
                                <i class="bi bi-grid-3x3-gap-fill"></i>
                            </div>
                        </a>
                        <div class="dropdown-menu dropdown-menu-end">
                            <div class="row row-cols-3 gx-2">
                                <div class="col">
                                    <a href="ecommerce-orders.html">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-purple">
                                                <i class="bi bi-basket2-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Orders</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="javascript:;">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-info">
                                                <i class="bi bi-people-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Users</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="ecommerce-products-grid.html">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-success">
                                                <i class="bi bi-trophy-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Products</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="component-media-object.html">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-danger">
                                                <i class="bi bi-collection-play-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Media</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="pages-user-profile.html">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-warning">
                                                <i class="bi bi-person-circle"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Account</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="javascript:;">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-voilet">
                                                <i class="bi bi-file-earmark-text-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Docs</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="ecommerce-orders-detail.html">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-branding">
                                                <i class="bi bi-credit-card-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Payment</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="javascript:;">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-desert">
                                                <i class="bi bi-calendar-check-fill"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Events</p>
                                        </div>
                                    </a>
                                </div>
                                <div class="col">
                                    <a href="javascript:;">
                                        <div class="apps p-2 radius-10 text-center">
                                            <div class="apps-icon-box mb-1 text-white bg-gradient-amour">
                                                <i class="bi bi-book-half"></i>
                                            </div>
                                            <p class="mb-0 apps-name">Story</p>
                                        </div>
                                    </a>
                                </div>
                            </div><!--end row-->
                        </div>
                    </li>
                    <li class="nav-item dropdown dropdown-large">
                        <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                            <div class="messages">
                                <span class="notify-badge">5</span>
                                <i class="bi bi-chat-right-fill"></i>
                            </div>
                        </a>
                        <div class="dropdown-menu dropdown-menu-end p-0">
                            <div class="p-2 border-bottom m-2">
                                <h5 class="h5 mb-0">Messages</h5>
                            </div>
                            <div class="header-message-list p-2">
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-1.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Amelio Joly <span
                                                    class="msg-time float-end text-secondary">1 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">The
                                                standard chunk of lorem...</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-2.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Althea Cabardo <span
                                                    class="msg-time float-end text-secondary">7 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Many
                                                desktop publishing</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-3.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Katherine Pechon <span
                                                    class="msg-time float-end text-secondary">2 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Making
                                                this the first true</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-4.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Peter Costanzo <span
                                                    class="msg-time float-end text-secondary">3 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">It
                                                was popularised in the 1960</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-5.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Thomas Wheeler <span
                                                    class="msg-time float-end text-secondary">1 d</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">If
                                                you are going to use a passage</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-6.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Johnny Seitz <span
                                                    class="msg-time float-end text-secondary">2 w</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">All
                                                the Lorem Ipsum generators</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-1.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Amelio Joly <span
                                                    class="msg-time float-end text-secondary">1 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">The
                                                standard chunk of lorem...</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-2.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Althea Cabardo <span
                                                    class="msg-time float-end text-secondary">7 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Many
                                                desktop publishing</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <img src="assets/images/avatars/avatar-3.png" alt="" class="rounded-circle"
                                             width="50" height="50">
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Katherine Pechon <span
                                                    class="msg-time float-end text-secondary">2 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Making
                                                this the first true</small>
                                        </div>
                                    </div>
                                </a>
                            </div>
                            <div class="p-2">
                                <div>
                                    <hr class="dropdown-divider">
                                </div>
                                <a class="dropdown-item" href="#">
                                    <div class="text-center">View All Messages</div>
                                </a>
                            </div>
                        </div>
                    </li>
                    <li class="nav-item dropdown dropdown-large">
                        <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                            <div class="notifications">
                                <span class="notify-badge">8</span>
                                <i class="bi bi-bell-fill"></i>
                            </div>
                        </a>
                        <div class="dropdown-menu dropdown-menu-end p-0">
                            <div class="p-2 border-bottom m-2">
                                <h5 class="h5 mb-0">Notifications</h5>
                            </div>
                            <div class="header-notifications-list p-2">
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-primary text-primary"><i
                                                class="bi bi-basket2-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">New Orders <span
                                                    class="msg-time float-end text-secondary">1 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">You
                                                have recived new orders</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-purple text-purple"><i
                                                class="bi bi-people-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">New Customers <span
                                                    class="msg-time float-end text-secondary">7 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">5
                                                new user registered</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-success text-success"><i
                                                class="bi bi-file-earmark-bar-graph-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">24 PDF File <span
                                                    class="msg-time float-end text-secondary">2 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">The
                                                pdf files generated</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-orange text-orange"><i
                                                class="bi bi-collection-play-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Time Response <span
                                                    class="msg-time float-end text-secondary">3 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">5.1
                                                min avarage time response</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-info text-info"><i
                                                class="bi bi-cursor-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">New Product Approved <span
                                                    class="msg-time float-end text-secondary">1 d</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Your
                                                new product has approved</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-pink text-pink"><i
                                                class="bi bi-gift-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">New Comments <span
                                                    class="msg-time float-end text-secondary">2 w</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">New
                                                customer comments recived</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-warning text-warning"><i
                                                class="bi bi-droplet-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">New 24 authors<span
                                                    class="msg-time float-end text-secondary">1 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">24
                                                new authors joined last week</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-primary text-primary"><i
                                                class="bi bi-mic-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Your item is shipped <span
                                                    class="msg-time float-end text-secondary">7 m</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Successfully
                                                shipped your item</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-success text-success"><i
                                                class="bi bi-lightbulb-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">Defense Alerts <span
                                                    class="msg-time float-end text-secondary">2 h</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">45%
                                                less alerts last 4 weeks</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-info text-info"><i
                                                class="bi bi-bookmark-heart-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">4 New Sign Up <span
                                                    class="msg-time float-end text-secondary">2 w</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">New
                                                4 user registartions</small>
                                        </div>
                                    </div>
                                </a>
                                <a class="dropdown-item" href="#">
                                    <div class="d-flex align-items-center">
                                        <div class="notification-box bg-light-bronze text-bronze"><i
                                                class="bi bi-briefcase-fill"></i></div>
                                        <div class="ms-3 flex-grow-1">
                                            <h6 class="mb-0 dropdown-msg-user">All Documents Uploaded <span
                                                    class="msg-time float-end text-secondary">1 mo</span></h6>
                                            <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Sussessfully
                                                uploaded all files</small>
                                        </div>
                                    </div>
                                </a>
                            </div>
                            <div class="p-2">
                                <div>
                                    <hr class="dropdown-divider">
                                </div>
                                <a class="dropdown-item" href="#">
                                    <div class="text-center">View All Notifications</div>
                                </a>
                            </div>
                        </div>
                    </li>
                </ul>
            </div>
        </nav>
    </header>
    <!--end top header-->

    <!--start sidebar -->
    <aside class="sidebar-wrapper" data-simplebar="true">
        <div class="sidebar-header">
            <div>
                <img src="/media/img/ke_login.png" class="logo-icon" alt="efak icon">
            </div>
            <div>
                <h4 class="logo-text">EFAK</h4>
            </div>
            <div class="toggle-icon ms-auto"><i class="bi bi-list"></i>
            </div>
        </div>
        <!--navigation-->
        <ul class="metismenu" id="menu">
            <li>
                <a href="javascript:;" class="has-arrow">
                    <div class="parent-icon"><i class="bi bi-house-fill"></i>
                    </div>
                    <div class="menu-title">Dashboard</div>
                </a>
                <ul>
                    <li><a href="index.html"><i class="bi bi-circle"></i>Blue Dashboard</a>
                    </li>
                    <li><a href="index2.html"><i class="bi bi-circle"></i>Color Dashboard</a>
                    </li>
                </ul>
            </li>
            <li>
                <a href="javascript:;" class="has-arrow">
                    <div class="parent-icon"><i class="bi bi-grid-fill"></i>
                    </div>
                    <div class="menu-title">Application</div>
                </a>
                <ul>
                    <li><a href="app-emailbox.html"><i class="bi bi-circle"></i>Email</a>
                    </li>
                    <li><a href="app-chat-box.html"><i class="bi bi-circle"></i>Chat Box</a>
                    </li>
                    <li><a href="app-file-manager.html"><i class="bi bi-circle"></i>File Manager</a>
                    </li>
                    <li><a href="app-to-do.html"><i class="bi bi-circle"></i>Todo List</a>
                    </li>
                    <li><a href="app-invoice.html"><i class="bi bi-circle"></i>Invoice</a>
                    </li>
                    <li><a href="app-fullcalender.html"><i class="bi bi-circle"></i>Calendar</a>
                    </li>
                </ul>
            </li>
            <li class="menu-label">UI Elements</li>
            <li>
                <a href="javascript:;" class="has-arrow">
                    <div class="parent-icon"><i class="bi bi-droplet-fill"></i>
                    </div>
                    <div class="menu-title">Widgets</div>
                </a>
                <ul>
                    <li><a href="widgets-static-widgets.html"><i class="bi bi-circle"></i>Static Widgets</a>
                    </li>
                    <li><a href="widgets-data-widgets.html"><i class="bi bi-circle"></i>Data Widgets</a>
                    </li>
                </ul>
            </li>
            <li>
                <a href="javascript:;" class="has-arrow">
                    <div class="parent-icon"><i class="bi bi-basket2-fill"></i>
                    </div>
                    <div class="menu-title">eCommerce</div>
                </a>
                <ul>
                    <li><a href="ecommerce-products-list.html"><i class="bi bi-circle"></i>Products List</a>
                    </li>
                    <li><a href="ecommerce-products-grid.html"><i class="bi bi-circle"></i>Products Grid</a>
                    </li>
                    <li><a href="ecommerce-products-categories.html"><i class="bi bi-circle"></i>Categories</a>
                    </li>
                    <li><a href="ecommerce-orders.html"><i class="bi bi-circle"></i>Orders</a>
                    </li>
                    <li><a href="ecommerce-orders-detail.html"><i class="bi bi-circle"></i>Order details</a>
                    </li>
                    <li><a href="ecommerce-add-new-product.html"><i class="bi bi-circle"></i>Add New Product</a>
                    </li>
                    <li><a href="ecommerce-add-new-product-2.html"><i class="bi bi-circle"></i>Add New Product 2</a>
                    </li>
                    <li><a href="ecommerce-transactions.html"><i class="bi bi-circle"></i>Transactions</a>
                    </li>
                </ul>
            </li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-award-fill"></i>
                    </div>
                    <div class="menu-title">Components</div>
                </a>
                <ul>
                    <li><a href="component-alerts.html"><i class="bi bi-circle"></i>Alerts</a>
                    </li>
                    <li><a href="component-accordions.html"><i class="bi bi-circle"></i>Accordions</a>
                    </li>
                    <li><a href="component-badges.html"><i class="bi bi-circle"></i>Badges</a>
                    </li>
                    <li><a href="component-buttons.html"><i class="bi bi-circle"></i>Buttons</a>
                    </li>
                    <li><a href="component-cards.html"><i class="bi bi-circle"></i>Cards</a>
                    </li>
                    <li><a href="component-carousels.html"><i class="bi bi-circle"></i>Carousels</a>
                    </li>
                    <li><a href="component-list-groups.html"><i class="bi bi-circle"></i>List Groups</a>
                    </li>
                    <li><a href="component-media-object.html"><i class="bi bi-circle"></i>Media Objects</a>
                    </li>
                    <li><a href="component-modals.html"><i class="bi bi-circle"></i>Modals</a>
                    </li>
                    <li><a href="component-navs-tabs.html"><i class="bi bi-circle"></i>Navs & Tabs</a>
                    </li>
                    <li><a href="component-navbar.html"><i class="bi bi-circle"></i>Navbar</a>
                    </li>
                    <li><a href="component-paginations.html"><i class="bi bi-circle"></i>Pagination</a>
                    </li>
                    <li><a href="component-popovers-tooltips.html"><i class="bi bi-circle"></i>Popovers & Tooltips</a>
                    </li>
                    <li><a href="component-progress-bars.html"><i class="bi bi-circle"></i>Progress</a>
                    </li>
                    <li><a href="component-spinners.html"><i class="bi bi-circle"></i>Spinners</a>
                    </li>
                    <li><a href="component-notifications.html"><i class="bi bi-circle"></i>Notifications</a>
                    </li>
                    <li><a href="component-avtars-chips.html"><i class="bi bi-circle"></i>Avatrs & Chips</a>
                    </li>
                </ul>
            </li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-cloud-arrow-down-fill"></i>
                    </div>
                    <div class="menu-title">Icons</div>
                </a>
                <ul>
                    <li><a href="icons-line-icons.html"><i class="bi bi-circle"></i>Line Icons</a>
                    </li>
                    <li><a href="icons-boxicons.html"><i class="bi bi-circle"></i>Boxicons</a>
                    </li>
                    <li><a href="icons-feather-icons.html"><i class="bi bi-circle"></i>Feather Icons</a>
                    </li>
                </ul>
            </li>
            <li class="menu-label">Forms & Tables</li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-file-earmark-break-fill"></i>
                    </div>
                    <div class="menu-title">Forms</div>
                </a>
                <ul>
                    <li><a href="form-elements.html"><i class="bi bi-circle"></i>Form Elements</a>
                    </li>
                    <li><a href="form-input-group.html"><i class="bi bi-circle"></i>Input Groups</a>
                    </li>
                    <li><a href="form-layouts.html"><i class="bi bi-circle"></i>Forms Layouts</a>
                    </li>
                    <li><a href="form-validations.html"><i class="bi bi-circle"></i>Form Validation</a>
                    </li>
                    <li><a href="form-wizard.html"><i class="bi bi-circle"></i>Form Wizard</a>
                    </li>
                    <li><a href="form-date-time-pickes.html"><i class="bi bi-circle"></i>Date Pickers</a>
                    </li>
                    <li><a href="form-select2.html"><i class="bi bi-circle"></i>Select2</a>
                    </li>
                </ul>
            </li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-file-earmark-spreadsheet-fill"></i>
                    </div>
                    <div class="menu-title">Tables</div>
                </a>
                <ul>
                    <li><a href="table-basic-table.html"><i class="bi bi-circle"></i>Basic Table</a>
                    </li>
                    <li><a href="table-advance-tables.html"><i class="bi bi-circle"></i>Advance Tables</a>
                    </li>
                    <li><a href="table-datatable.html"><i class="bi bi-circle"></i>Data Table</a>
                    </li>
                </ul>
            </li>
            <li class="menu-label">Pages</li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-lock-fill"></i>
                    </div>
                    <div class="menu-title">Authentication</div>
                </a>
                <ul>
                    <li><a href="authentication-signin.html" target="_blank"><i class="bi bi-circle"></i>Sign In</a>
                    </li>
                    <li><a href="authentication-signup.html" target="_blank"><i class="bi bi-circle"></i>Sign Up</a>
                    </li>
                    <li><a href="authentication-signin-with-header-footer.html" target="_blank"><i
                            class="bi bi-circle"></i>Sign In with Header & Footer</a>
                    </li>
                    <li><a href="authentication-signup-with-header-footer.html" target="_blank"><i
                            class="bi bi-circle"></i>Sign Up with Header & Footer</a>
                    </li>
                    <li><a href="authentication-forgot-password.html" target="_blank"><i class="bi bi-circle"></i>Forgot
                        Password</a>
                    </li>
                    <li><a href="authentication-reset-password.html" target="_blank"><i class="bi bi-circle"></i>Reset
                        Password</a>
                    </li>
                </ul>
            </li>
            <li>
                <a href="pages-user-profile.html">
                    <div class="parent-icon"><i class="bi bi-person-lines-fill"></i>
                    </div>
                    <div class="menu-title">User Profile</div>
                </a>
            </li>
            <li>
                <a href="pages-timeline.html">
                    <div class="parent-icon"><i class="bi bi-collection-play-fill"></i>
                    </div>
                    <div class="menu-title">Timeline</div>
                </a>
            </li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-exclamation-triangle-fill"></i>
                    </div>
                    <div class="menu-title">Errors</div>
                </a>
                <ul>
                    <li><a href="pages-errors-404-error.html" target="_blank"><i class="bi bi-circle"></i>404 Error</a>
                    </li>
                    <li><a href="pages-errors-500-error.html" target="_blank"><i class="bi bi-circle"></i>500 Error</a>
                    </li>
                    <li><a href="pages-errors-coming-soon.html" target="_blank"><i class="bi bi-circle"></i>Coming Soon</a>
                    </li>
                    <li><a href="pages-blank-page.html" target="_blank"><i class="bi bi-circle"></i>Blank Page</a>
                    </li>
                </ul>
            </li>
            <li>
                <a href="pages-faq.html">
                    <div class="parent-icon"><i class="bi bi-question-lg"></i>
                    </div>
                    <div class="menu-title">FAQ</div>
                </a>
            </li>
            <li>
                <a href="pages-pricing-tables.html">
                    <div class="parent-icon"><i class="bi bi-tags-fill"></i>
                    </div>
                    <div class="menu-title">Pricing Tables</div>
                </a>
            </li>
            <li class="menu-label">Charts & Maps</li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-bar-chart-line-fill"></i>
                    </div>
                    <div class="menu-title">Charts</div>
                </a>
                <ul>
                    <li><a href="charts-apex-chart.html"><i class="bi bi-circle"></i>Apex</a>
                    </li>
                    <li><a href="charts-chartjs.html"><i class="bi bi-circle"></i>Chartjs</a>
                    </li>
                    <li><a href="charts-highcharts.html"><i class="bi bi-circle"></i>Highcharts</a>
                    </li>
                </ul>
            </li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-pin-map-fill"></i>
                    </div>
                    <div class="menu-title">Maps</div>
                </a>
                <ul>
                    <li><a href="map-google-maps.html"><i class="bi bi-circle"></i>Google Maps</a>
                    </li>
                    <li><a href="map-vector-maps.html"><i class="bi bi-circle"></i>Vector Maps</a>
                    </li>
                </ul>
            </li>
            <li class="menu-label">Others</li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-music-note-list"></i>
                    </div>
                    <div class="menu-title">Menu Levels</div>
                </a>
                <ul>
                    <li><a class="has-arrow" href="javascript:;"><i class="bi bi-circle"></i>Level One</a>
                        <ul>
                            <li><a class="has-arrow" href="javascript:;"><i class="bi bi-circle"></i>Level Two</a>
                                <ul>
                                    <li><a href="javascript:;"><i class="bi bi-circle"></i>Level Three</a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ul>
            </li>
            <li>
                <a href="" target="_blank">
                    <div class="parent-icon"><i class="bi bi-file-code-fill"></i>
                    </div>
                    <div class="menu-title">Documentation</div>
                </a>
            </li>
            <li>
                <a href="http://www.bootstrapmb.com" target="_blank">
                    <div class="parent-icon"><i class="bi bi-telephone-fill"></i>
                    </div>
                    <div class="menu-title">Support</div>
                </a>
            </li>
        </ul>
        <!--end navigation-->
    </aside>
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
