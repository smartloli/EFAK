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
                <li><a href="#"><i class="bx bx-cube-alt"></i>Balance</a>
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
                <li><a href="ecommerce-products-list.html"><i class="bi bi-circle"></i>Groups</a>
                </li>
                <li><a href="ecommerce-products-grid.html"><i class="bi bi-circle"></i>StreamGraph</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Performance</li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-award-fill"></i>
                </div>
                <div class="menu-title">Node</div>
            </a>
            <ul>
                <li><a href="component-alerts.html"><i class="bi bi-circle"></i>Kafka</a>
                </li>
                <li><a href="component-accordions.html"><i class="bi bi-circle"></i>Zookeeper</a>
                </li>
                <li><a href="component-badges.html"><i class="bi bi-circle"></i>EfakServer</a>
                </li>
                <li><a href="component-buttons.html"><i class="bi bi-circle"></i>Cluster Manage</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-cloud-arrow-down-fill"></i>
                </div>
                <div class="menu-title">Monitor</div>
            </a>
            <ul>
                <li><a href="icons-line-icons.html"><i class="bi bi-circle"></i>MBean</a>
                </li>
                <li><a href="icons-boxicons.html"><i class="bi bi-circle"></i>Kafka</a>
                </li>
                <li><a href="icons-feather-icons.html"><i class="bi bi-circle"></i>Zookeeper</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Plugins</li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-file-earmark-break-fill"></i>
                </div>
                <div class="menu-title">Connect</div>
            </a>
            <ul>
                <li><a href="form-elements.html"><i class="bi bi-circle"></i>Config</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Notification</li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-file-earmark-spreadsheet-fill"></i>
                </div>
                <div class="menu-title">AlertChannel</div>
            </a>
            <ul>
                <li><a href="table-basic-table.html"><i class="bi bi-circle"></i>Config</a>
                </li>
                <li><a href="table-advance-tables.html"><i class="bi bi-circle"></i>List</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-file-earmark-spreadsheet-fill"></i>
                </div>
                <div class="menu-title">AlertConsumer</div>
            </a>
            <ul>
                <li><a href="table-basic-table.html"><i class="bi bi-circle"></i>Add</a>
                </li>
                <li><a href="table-advance-tables.html"><i class="bi bi-circle"></i>List</a>
                </li>
            </ul>
        </li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-file-earmark-spreadsheet-fill"></i>
                </div>
                <div class="menu-title">AlertCommon</div>
            </a>
            <ul>
                <li><a href="table-basic-table.html"><i class="bi bi-circle"></i>Add</a>
                </li>
                <li><a href="table-advance-tables.html"><i class="bi bi-circle"></i>List</a>
                </li>
            </ul>
        </li>
        <li class="menu-label">Administrator</li>
        <li>
            <a class="has-arrow" href="javascript:;">
                <div class="parent-icon"><i class="bi bi-lock-fill"></i>
                </div>
                <div class="menu-title">System</div>
            </a>
            <ul>
                <li><a href="#"><i class="bi bi-circle"></i>User</a>
                </li>
                <li><a href="#"><i class="bi bi-circle"></i>Role</a>
                </li>
                <li><a href="#"><i class="bi bi-circle"></i>Resource</a>
                </li>
            </ul>
        </li>
    </ul>
    <!--end navigation-->
</aside>