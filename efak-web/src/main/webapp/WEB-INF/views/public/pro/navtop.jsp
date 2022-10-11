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
                            <img src="/media/img/efak-logo.png" class="user-img" alt="">
                        </div>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li>
                            <a class="dropdown-item" href="#">
                                <div class="d-flex align-items-center">
                                    <img src="/media/img/efak-logo.png" alt="" class="rounded-circle"
                                         width="54" height="54">
                                    <div class="ms-3">
                                        <h6 class="mb-0 dropdown-user-name">${LOGIN_USER_SESSION.username}</h6>
                                        <small class="mb-0 dropdown-user-designation text-secondary">${LOGIN_USER_SESSION.realname}</small>
                                    </div>
                                </div>
                            </a>
                        </li>
                        <li>
                            <hr class="dropdown-divider">
                        </li>
                        <li>
                            <a name="ke_account_reset" class="dropdown-item" href="#">
                                <div class="d-flex align-items-center">
                                    <div class=""><i class="bi bi-person-fill"></i></div>
                                    <div class="ms-3"><span>Password</span></div>
                                </div>
                            </a>
                        </li>
                        <li>
                            <hr class="dropdown-divider">
                        </li>
                        <li>
                            <a class="dropdown-item" href="/account/signout">
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
                                <a href="/topic/list">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-purple">
                                            <i class="bx bx-data"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Topics</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/system/user">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-info">
                                            <i class="bi bi-person-circle"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Users</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/consumers/groups">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-success">
                                            <i class="lni lni-users"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Consumers</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/cluster/kafka">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-danger">
                                            <i class="lni lni-ux"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Kafka</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/cluster/zookeeper">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-warning">
                                            <i class="bx bx-sitemap"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Zookeeper</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/metrics/mbean">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-voilet">
                                            <i class="bx bx-table"></i>
                                        </div>
                                        <p class="mb-0 apps-name">MBean</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/alarm/list">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-branding">
                                            <i class="bi bi-bell-fill"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Alert</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/connect/config">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-desert">
                                            <i class="bx bx-plug"></i>
                                        </div>
                                        <p class="mb-0 apps-name">Connect</p>
                                    </div>
                                </a>
                            </div>
                            <div class="col">
                                <a href="/cluster/efakserver">
                                    <div class="apps p-2 radius-10 text-center">
                                        <div class="apps-icon-box mb-1 text-white bg-gradient-amour">
                                            <i class="bx bx-server"></i>
                                        </div>
                                        <p class="mb-0 apps-name">EfakServer</p>
                                    </div>
                                </a>
                            </div>
                        </div><!--end row-->
                    </div>
                </li>
                <li class="nav-item dropdown dropdown-large">
                    <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                        <div class="messages">
                            <i class="bx bx-cube"></i>
                        </div>
                    </a>
                    <div class="dropdown-menu dropdown-menu-end p-0">
                        <div class="p-2 border-bottom m-2">
                            <h5 class="h5 mb-0">Cluster List</h5>
                        </div>
                        <div class="header-message-list p-2">
                            ${clusterAliasList}
                        </div>
                        <div class="p-2">
                            <div>
                                <hr class="dropdown-divider">
                            </div>
                            <a class="dropdown-item" href="/cluster/management">
                                <div class="text-center">View All Clusters</div>
                            </a>
                        </div>
                    </div>
                </li>
                <li class="nav-item dropdown dropdown-large">
                    <a class="nav-link dropdown-toggle dropdown-toggle-nocaret" href="#" data-bs-toggle="dropdown">
                        <div class="notifications">
                            <%-- <span class="notify-badge">8</span>--%>
                            <i class="bi bi-question-lg"></i>
                        </div>
                    </a>
                    <div class="dropdown-menu dropdown-menu-end p-0">
                        <div class="p-2 border-bottom m-2">
                            <h5 class="h5 mb-0">FAQ</h5>
                        </div>
                        <div class="header-notifications-list p-2">
                            <a class="dropdown-item" href="https://github.com/smartloli/EFAK" target="_blank">
                                <div class="d-flex align-items-center">
                                    <div class="notification-box bg-light-primary text-primary"><i
                                            class="lni lni-github"></i></div>
                                    <div class="ms-3 flex-grow-1">
                                        <h6 class="mb-0 dropdown-msg-user">Github</h6>
                                        <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">You
                                            can visit efak open source code</small>
                                    </div>
                                </div>
                            </a>
                            <a class="dropdown-item" href="http://www.kafka-eagle.org/"
                               target="_blank">
                                <div class="d-flex align-items-center">
                                    <div class="notification-box bg-light-info text-info"><i
                                            class="bx bx-book-bookmark"></i></div>
                                    <div class="ms-3 flex-grow-1">
                                        <h6 class="mb-0 dropdown-msg-user">Document</h6>
                                        <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Quick
                                            installation guide</small>
                                    </div>
                                </div>
                            </a>
                            <a class="dropdown-item" href="http://www.kafka-eagle.org/" target="_blank">
                                <div class="d-flex align-items-center">
                                    <div class="notification-box bg-light-success text-success"><i
                                            class="bx bx-download"></i></div>
                                    <div class="ms-3 flex-grow-1">
                                        <h6 class="mb-0 dropdown-msg-user">Download</h6>
                                        <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Installation
                                            package download address</small>
                                    </div>
                                </div>
                            </a>
                            <a class="dropdown-item" href="https://github.com/smartloli/EFAK/discussions"
                               target="_blank">
                                <div class="d-flex align-items-center">
                                    <div class="notification-box bg-light-bronze text-bronze"><i
                                            class="bi bi-people-fill"></i></div>
                                    <div class="ms-3 flex-grow-1">
                                        <h6 class="mb-0 dropdown-msg-user">Forum</h6>
                                        <small class="mb-0 dropdown-msg-text text-secondary d-flex align-items-center">Discuss
                                            issues in the Forum</small>
                                    </div>
                                </div>
                            </a>
                        </div>
                    </div>
                </li>
            </ul>
        </div>

        <!-- add partition modal -->
        <div class="modal fade" id="efak_account_reset_modal" tabindex="-1" aria-labelledby="keModalLabel"
             aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="keModalLabel">Reset Password</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <form role="form" action="/account/reset/" method="post"
                          onsubmit="return contextPasswdFormValid();return false;">
                        <div class="modal-body">
                            <div class="input-group mb-3"><span class="input-group-text"><i
                                    class="bx bx-lock"></i></span>
                                <input id="ke_new_password_name" name="ke_new_password_name" type="text"
                                       class="form-control" placeholder="Enter Your New Password" aria-label=""
                                       aria-describedby="">
                            </div>
                            <div id="efak_account_alert_error_message" style="display: none" class="alert alert-danger">
                                <label> Passwords can only be number and letters or special symbols .</label>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="submit" class="btn btn-primary" id="create-btn">Submit</button>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </nav>
</header>
<!--end top header-->
<script type="text/javascript">
    function contextPasswdFormValid() {
        var ke_new_password_name = $("#ke_new_password_name").val();
        var resetRegular = /[\u4E00-\u9FA5]/;
        if (ke_new_password_name.length == 0 || resetRegular.test(ke_new_password_name)) {
            $("#efak_account_alert_error_message").show();
            setTimeout(function () {
                $("#efak_account_alert_error_message").hide()
            }, 3000);
            return false;
        }

        return true;
    }
</script>