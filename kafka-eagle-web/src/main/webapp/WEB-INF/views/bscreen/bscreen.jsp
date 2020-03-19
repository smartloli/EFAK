<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>BScreen - KafkaEagle</title>
<link rel="shortcut icon" href="/ke/media/img/favicon.ico" />
<link rel="stylesheet" href="/ke/media/css/bscreen/css/bscreen.css">
<script type="text/javascript" src="/ke/media/js/bscreen/js/jquery.js"></script>
<script>
	$(document).ready(function() {
		var whei = $(window).width()
		$("html").css({
			fontSize : whei / 20
		})
		$(window).resize(function() {
			var whei = $(window).width()
			$("html").css({
				fontSize : whei / 20
			})
		});
	});
</script>

<script type="text/javascript" src="/ke/media/js/bscreen/js/echarts.min.js"></script>
<script type="text/javascript" src="/ke/media/js/bscreen/js/bscreen.js"></script>
<body>
	<div class="head">
		<h1>Kafka Eagle BScreen</h1>
		<div class="weather">
			<span id="showTime"></span>
		</div>
		<script>
			var t = null;
			t = setTimeout(time, 1000);
			function time() {
				clearTimeout(t);
				dt = new Date();
				var y = dt.getFullYear();
				var mt = dt.getMonth() + 1;
				if (mt < 10) {
					mt = "0" + mt;
				}
				var day = dt.getDate();
				if (day < 10) {
					day = "0" + day;
				}
				var h = dt.getHours();
				var m = dt.getMinutes();
				if (m < 10) {
					m = "0" + m;
				}
				var s = dt.getSeconds();
				if (s < 10) {
					s = "0" + s;
				}
				document.getElementById("showTime").innerHTML = y + "-" + mt + "-" + day + " " + h + ":" + m + ":" + s;
				t = setTimeout(time, 1000);
			}
		</script>


	</div>
	<div class="mainbox">
		<ul class="clearfix">
			<li>
				<div class="boxall" style="height: 3.2rem">
					<div class="alltitle">Producer Records For The Last 7 Days (msg)</div>
					<div class="allnav" id="ke_bs_producer_history"></div>
					<div class="boxfoot"></div>
				</div>
				<div class="boxall" style="height: 3.2rem">
					<div class="alltitle">Consumer Records For The Last 7 Days (msg)</div>
					<div class="allnav" id="ke_bs_consumer_history"></div>
					<div class="boxfoot"></div>
				</div>
				<div class="boxall" style="height: 3.2rem">
					<div style="height: 100%; width: 100%;">
						<div class="alltitle">Topic History Total Records (msg)</div>
						<div id="ke_topics_total_logsize" style="width: 100%; height: 100%; margin-top: 10%; text-align: center; color: #fff; font-size: .7rem; font-family: electronicFont; font-weight: bold">0</div>
					</div>
				</div>
			</li>
			<li>
				<div class="bar">
					<div class="barbox">
						<ul class="clearfix">
							<li id="ke_bs_ins_rate" class="pulll_left counter">0</li>
							<li id="ke_bs_outs_rate" class="pulll_left counter">0</li>
						</ul>
					</div>
					<div class="barbox2">
						<ul class="clearfix">
							<li id="ke_bs_ins_rate_name" class="pulll_left">Brokers ByteIn (B/sec)</li>
							<li id="ke_bs_outs_rate_name" class="pulll_left">Brokers ByteOut (B/sec)</li>
						</ul>
					</div>
				</div>
				<div class="map">
					<div class="map1">
						<img src="/ke/media/js/bscreen/picture/lbx.png">
					</div>
					<div class="map2">
						<img src="/ke/media/js/bscreen/picture/jt.png">
					</div>
					<div class="map3">
						<img src="/ke/media/js/bscreen/picture/map.png">
					</div>
					<div class="map4">
						<div style="height: 100%; width: 100%;">
							<div id="ke_topics_total_capacity_unit" class="alltitleball">Topic Total Capacity</div>
							<div id="ke_topics_total_capacity" style="width: 100%; height: 100%; text-align: center; color: #fff; font-size: .7rem; font-family: electronicFont; font-weight: bold">0</div>
						</div>
					</div>
				</div>
			</li>
			<li>
				<div class="boxall" style="height: 3.4rem">
					<div class="alltitle">Today Producer (msg/min)</div>
					<div class="allnav" id="ke_bs_today_producer"></div>
					<div class="boxfoot"></div>
				</div>
				<div class="boxall" style="height: 3.4rem">
					<div class="alltitle">Today Consumer (msg/min)</div>
					<div class="allnav" id="ke_bs_today_consumer"></div>
					<div class="boxfoot"></div>
				</div>
				<div class="boxall" style="height: 3.4rem">
					<div class="alltitle">Today Lags (msg/min)</div>
					<div class="allnav" id="ke_bs_today_lag"></div>
					<div class="boxfoot"></div>
				</div>
			</li>
		</ul>
	</div>
	<div class="back"></div>
</body>
</html>
