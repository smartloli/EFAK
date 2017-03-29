<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- Navigation -->
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<!-- Brand and toggle get grouped for better mobile display -->
	<div class="navbar-header">
		<button type="button" class="navbar-toggle" data-toggle="collapse"
			data-target=".navbar-ex1-collapse">
			<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span>
			<span class="icon-bar"></span> <span class="icon-bar"></span>
		</button>
		<a class="navbar-brand" href="/ke">Kafka Eagle</a>
	</div>
	<!-- Top Menu Items -->
	<ul class="nav navbar-right top-nav">
		<li class="dropdown"><a href="#" class="dropdown-toggle"
			data-toggle="dropdown"><i class="fa fa-sitemap"></i>
				${clusterAlias} </a></li>
		<li class="dropdown"><a href="#" class="dropdown-toggle"
			data-toggle="dropdown"><i class="fa fa-bookmark"></i> V1.1.2 </a></li>
	</ul>
	<!-- Sidebar Menu Items - These collapse to the responsive navigation menu on small screens -->
	<div class="collapse navbar-collapse navbar-ex1-collapse">
		<ul class="nav navbar-nav side-nav">
			<li id="navbar_dash"><a href="/ke"><i
					class="fa fa-fw fa-dashboard"></i> Dashboard</a></li>
			<li><a href="#" data-toggle="collapse" data-target="#demo"><i
					class="fa fa-fw fa-comments-o"></i> Topic <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo" class="collapse">
					<li id="navbar_create"><a href="/ke/topic/create"><i
							class="fa fa-edit fa-fw"></i> Create</a></li>
					<li id="navbar_list"><a href="/ke/topic/list"><i
							class="fa fa-table fa-fw"></i> List</a></li>
					<li id="navbar_list"><a href="/ke/topic/message"><i
							class="fa fa-file-text fa-fw"></i> Message</a></li>
				</ul></li>
			<li id="navbar_consumers"><a href="/ke/consumers"><i
					class="fa fa-fw fa-users"></i> Consumers</a></li>
			<li><a href="#" data-toggle="collapse" data-target="#demo2"><i
					class="fa fa-fw fa-cloud"></i> Cluster <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo2" class="collapse">
					<li id="navbar_cli"><a href="/ke/cluster/info"><i
							class="fa fa-sitemap fa-fw"></i> ZK & Kafka</a></li>
					<li id="navbar_cli"><a href="/ke/cluster/multi"><i
							class="fa fa-maxcdn fa-fw"></i> Multi-Clusters</a></li>
					<li id="navbar_zk"><a href="/ke/cluster/zkcli"><i
							class="fa fa-terminal fa-fw"></i> ZkCli</a></li>
				</ul></li>
			<li><a href="#" data-toggle="collapse" data-target="#demo1"><i
					class="fa fa-fw fa-bell"></i> Alarm <i
					class="fa fa-fw fa-caret-down"></i></a>
				<ul id="demo1" class="collapse">
					<li id="navbar_add"><a href="/ke/alarm/add"><i
							class="fa fa-info-circle fa-fw"></i> Add</a></li>
					<li id="navbar_modify"><a href="/ke/alarm/modify"><i
							class="fa fa-edit fa-fw"></i> Modify</a></li>
				</ul></li>
		</ul>
	</div>
	<!-- /.navbar-collapse -->
</nav>