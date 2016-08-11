<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- Navigation -->
<nav class="navbar navbar-default navbar-static-top" role="navigation"
	style="margin-bottom: 0">
	<div class="navbar-header">
		<button type="button" class="navbar-toggle" data-toggle="collapse"
			data-target=".navbar-collapse">
			<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span>
			<span class="icon-bar"></span> <span class="icon-bar"></span>
		</button>
		<a class="navbar-brand" href="/cms">CMS v1.0</a>
	</div>
	<!-- /.navbar-header -->

	<ul class="nav navbar-top-links navbar-right">
		<!-- /.dropdown -->
		<li class="dropdown"><a class="dropdown-toggle"
			data-toggle="dropdown" href="#"> <i class="fa fa-user fa-fw"></i>
				<i class="fa fa-caret-down"></i>
		</a>
			<ul class="dropdown-menu dropdown-user">
				<li><a href="#"><i class="fa fa-user fa-fw"></i>
						${user.username}</a><a id="username" style="display: none">${user.username}</a></li>
				<li><a href="#"><i class="fa fa-gear fa-fw"></i> 设置</a></li>
				<li class="divider"></li>
				<li><a href="/cms/signout"><i class="fa fa-sign-out fa-fw"></i>
						注销</a></li>
			</ul> <!-- /.dropdown-user --></li>
		<!-- /.dropdown -->
	</ul>
	<!-- /.navbar-top-links -->

	<div class="navbar-default sidebar" role="navigation">
		<div class="sidebar-nav navbar-collapse">
			<ul class="nav" id="side-menu">
				<li><a href="/cms"><i class="fa fa-dashboard fa-fw"></i> 面板</a></li>
				<li><a href="/cms/article"><i class="fa fa-wrench fa-fw"></i>
						咨询管理</a></li>
				<li><a href="/cms/article/add"><i class="fa fa-edit fa-fw"></i>
						咨询添加</a></li>
				<li><a href="/cms/chanle"><i class="fa fa-tags fa-fw"></i>
						频道管理</a></li>
				<li><a href="#"><i class="fa fa-comments-o fa-fw"></i> 常见问题<span
						class="fa arrow"></span></a>
					<ul class="nav nav-second-level">
						<li><a href="#"><i class="fa fa-qq fa-fw"></i> 微众银行 App</a></li>
						<li><a href="#"><i class="fa fa-yen fa-fw"></i> 微粒贷</a></li>
						<li><a href="#"><i class="fa fa-car fa-fw"></i> 微车贷</a></li>
					</ul> <!-- /.nav-second-level --></li>
			</ul>
		</div>
		<!-- /.sidebar-collapse -->
	</div>
	<!-- /.navbar-static-side -->
</nav>