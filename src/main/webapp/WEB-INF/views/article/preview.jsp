<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>预览 - 微众银行</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						预览 <small>详情</small>
					</h1>
				</div>
				<!-- /.col-lg-12 -->
			</div>
			<!-- /.row -->
			<div class="row">
				<div class="col-lg-12">
					<div class="alert alert-info alert-dismissable">
						<button type="button" class="close" data-dismiss="alert"
							aria-hidden="true">×</button>
						<i class="fa fa-info-circle"></i> <strong>咨询预览模块，</strong>
						由管理员预览所编辑的内容。
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">咨询内容信息</div>
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<div id="title" class="form-group">
										<label></label>
									</div>
									<hr style="margin-top: -10px;" />
									<div id="content" class="form-group"></div>
								</div>
								<!-- /.col-lg-6 (nested) -->
							</div>
							<!-- /.row (nested) -->
						</div>
						<!-- /.panel-body -->
					</div>
					<div class="col-lg-12">
						<div id="type" style="float: left;">
							<span class="label label-info"></span>
						</div>
						<div id="chanle" style="float: left;margin-left: 10px;">
							<span class="label label-info"></span>
						</div>
					</div>
					<!-- /.tag -->
				</div>
			</div>
		</div>
	</div>
</body>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/preview.js" name="loader" />
</jsp:include>
<script type="text/javascript">
	//验证提交表单内容
	function contextFormValid() {
		var article_title = $("#article_title").val();
		var article_type = $("input[name='article_type']:checked").val();
		var article_range_date = $('#valid_range_date').find('span').text();
		// var article_valid = $("input[name='article_valid']:checked").text();
		var article_chanle = $("#article_chanle option:selected").text();
		var article_is_top = $("input[name='article_is_top']:checked").val();
		var article_content = "";
		KindEditor.ready(function(K) {
			editor = K.create('textarea[name="article_content"]')
			article_content = editor.html();
		});
		if (article_title.length == 0 || article_content.length == 0) {
			$("#alert_mssage").show();
			setTimeout(function() {
				$("#alert_mssage").hide()
			}, 3000);
			return false;
		}
		return true;
	}
</script>
</html>