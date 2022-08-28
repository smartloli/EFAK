<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="fmt" %>

<aside class="sidebar-wrapper" data-simplebar="true">
    <div class="sidebar-header">
        <div>
            <img src="/media/img/ke_login.png" class="logo-icon" alt="efak icon">
        </div>
        <div>
            <h4 class="logo-text">EFAK&nbsp;&nbsp;<span class="efak-header-version">${efakVersion}</span></h4>
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
                <li><a href="/"><i class="bx bx-laptop"></i>Overview</a>
                </li>
                <li><a href="/tv"><i class="bx bx-desktop"></i>TV Dashboard</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Message</li>
        <li>
            <a href="javascript:;" class="has-arrow">
                <div class="parent-icon"><i class="bi bi-droplet-fill"></i>
                </div>
                <div class="menu-title">Topics</div>
            </a>
            <ul>
                <li><a href="/topic/create"><i class="bx bx-message-square-edit"></i>Create</a>
                </li>
                <li><a href="/topic/list"><i class="bx bx-collection"></i>List</a>
                </li>
                <li><a href="/topic/ksql"><i class="bx bx-code"></i>KSQL</a>
                </li>
                <li><a href="/topic/mock"><i class="bx bx-send"></i>Mock</a>
                </li>
                <li><a href="/topic/metadata"><i class="bx bx-cylinder"></i>Metadata</a>
                </li>
                <li><a href="/topic/balance"><i class="bx bx-cube-alt"></i>Balance</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Application</li>
        <li>
            <a href="javascript:;" class="has-arrow">
                <div class="parent-icon"><i class="bi bi-basket2-fill"></i>
                </div>
                <div class="menu-title">Consumers</div>
            </a>
            <ul>
                <li><a href="/consumers/groups"><i class="bx bx-group"></i>Groups</a>
                </li>
                <%--                <li><a href="#"><i class="bi bi-circle"></i>StreamGraph</a>--%>
                <%--                </li>--%>
            </ul>
        </li>
        <li class="menu-label">Performance</li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bx bx-cloud-download"></i>
                </div>
                <div class="menu-title">Node</div>
            </a>
            <ul>
                <li><a href="/cluster/kafka"><i class="lni lni-ux"></i>Kafka</a>
                </li>
                <li><a href="/cluster/zookeeper"><i class="bx bx-sitemap"></i>Zookeeper</a>
                </li>
                <li><a href="/cluster/efakserver"><i class="bx bx-server"></i>EfakServer</a>
                </li>
                <li><a href="/cluster/management"><i class="bx bx-cube"></i>Management</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="#">
                <div class="parent-icon"><i class="bx bx-chart"></i>
                </div>
                <div class="menu-title">Monitor</div>
            </a>
            <ul>
                <li><a href="/metrics/mbean"><i class="bx bx-table"></i>MBean</a>
                </li>
                <li><a href="/metrics/kafka"><i class="lni lni-ux"></i>Kafka</a>
                </li>
                <li><a href="/metrics/zookeeper"><i class="bx bx-sitemap"></i>Zookeeper</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Plugins</li>
        <li>
            <a class="has-arrow" href="#">
                <div class="parent-icon"><i class="bi bi-file-earmark-break-fill"></i>
                </div>
                <div class="menu-title">Connector</div>
            </a>
            <ul>
                <li><a href="/connect/config"><i class="bx bx-plug"></i>Config</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Notification</li>
        <li>
            <a class="has-arrow" href="#">
                <div class="parent-icon"><i class="bx bx-alarm"></i>
                </div>
                <div class="menu-title">AlertChannel</div>
            </a>
            <ul>
                <li><a href="/alarm/config"><i class="bx bx-wrench"></i>Config</a>
                </li>
                <li><a href="/alarm/list"><i class="bx bx-list-ol"></i>List</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="#">
                <div class="parent-icon"><i class="bx bx-user-voice"></i>
                </div>
                <div class="menu-title">AlertConsumer</div>
            </a>
            <ul>
                <li><a href="/alarm/add"><i class="bx bx-user-plus"></i>Add</a>
                </li>
                <li><a href="/alarm/modify"><i class="bx bx-list-ol"></i>List</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="#">
                <div class="parent-icon"><i class="bx bx-navigation"></i>
                </div>
                <div class="menu-title">AlertCommon</div>
            </a>
            <ul>
                <li><a href="/alarm/create"><i class="bx bx-user-plus"></i>Add</a>
                </li>
                <li><a href="/alarm/history"><i class="bx bx-list-ol"></i>List</a>
                </li>
            </ul>
        </li>
        <c:if test="${WHETHER_SYSTEM_ADMIN==1}">
            <li class="menu-label">Administrator</li>
            <li>
                <a class="has-arrow" href="javascript:;">
                    <div class="parent-icon"><i class="bi bi-lock-fill"></i>
                    </div>
                    <div class="menu-title">System</div>
                </a>
                <ul>
                    <li><a href="/system/user"><i class="bx bx-user"></i>User</a>
                    </li>
                    <li><a href="/system/role"><i class="bx bx-lock"></i>Role</a>
                    </li>
                    <li><a href="/system/resource"><i class="bx bx-file-find"></i>Resource</a>
                    </li>
                </ul>
            </li>
        </c:if>
    </ul>
    <!--end navigation-->
</aside>