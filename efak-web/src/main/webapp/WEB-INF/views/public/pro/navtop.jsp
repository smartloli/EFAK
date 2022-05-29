<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="fmt" %>

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