<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="zh">

<head>
<title>编辑 - 微众银行</title>
<jsp:include page="../public/css.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="../public/navbar.jsp"></jsp:include>
	<div id="wrapper">
		<div id="page-wrapper">
			<div class="row">
				<div class="col-lg-12">
					<h1 class="page-header">
						咨询添加 <small>编辑详情</small>
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
						<i class="fa fa-info-circle"></i> <strong>咨询添加模块，</strong>
						由管理员编辑已存在的内容。
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-lg-12">
					<div class="panel panel-default">
						<div class="panel-heading">咨询基本信息</div>
						<div class="panel-body">
							<div class="row">
								<div class="col-lg-12">
									<form role="form" action="/cms/article/edit/content"
										method="post"
										onsubmit="return contextFormValid();return false;">
										<div class="form-group">
											<label>咨询标题 (*)</label> <input id="article_title"
												name="article_title" class="form-control" maxlength=50>
											<label for="inputError" class="control-label text-danger"><i
												class="fa fa-times-circle-o"></i> 5-50个非特殊字符</label>
										</div>
										<div class="form-group">
											<label>咨询类型 (*)</label>
											<div id="edit_article_type" class="radio">
												<label> <input type="radio" name="article_type"
													id="article_type" value="新闻">新闻
												</label> <label> <input type="radio" name="article_type"
													id="article_type" value="公告">公告
												</label> <label> <input type="radio" name="article_type"
													id="article_type" value="常见问题">常见问题
												</label>
											</div>
										</div>
										<div class="form-group">
											<label>有效时间</label>
											<div id="valid_range_date" style="width: 200px"
												class="date-input">
												<i class="glyphicon glyphicon-calendar fa fa-calendar"></i>
												<span></span> <b class="caret"></b> <input type="hidden"
													name="article_valid_date" value="" />
											</div>
											<input type="checkbox" name="article_valid" value="是"><label
												for="inputError" class="control-label text-danger">&nbsp;<i
												class="fa fa-times-circle-o"></i>是否长期有效
											</label>
										</div>
										<div class="form-group">
											<label>咨询频道 (*)</label>
											<div>
												<select id="article_chanle" multiple="multiple" size="3"
													name="article_chanle">
													<option value="新闻">新闻</option>
													<option value="公告">公告</option>
													<option value="常见问题">常见问题</option>
												</select>
											</div>
										</div>
										<div class="form-group">
											<label>是否置顶</label>
											<div id="article_is_top" class="radio">
												<label> <input type="radio" name="article_is_top"
													id="optionsRadios1" value="是">是
												</label> <label> <input type="radio" name="article_is_top"
													id="optionsRadios1" value="否">否
												</label>
											</div>
										</div>
										<div class="form-group">
											<label>咨询内容 (*)</label>
											<textarea class="form-control kindeditor"
												name="article_content" id="content"></textarea>
										</div>
										<input id="article_id" name="article_id" type="hidden">
										<!-- 
										<button type="button" onclick="contextFormValid()"
											class="btn btn-default">预览</button>
										 -->
										<button type="submit" class="btn btn-success">保存</button>
										<div id="alert_mssage" style="display: none"
											class="alert alert-danger">错误！请进行一些更改。(*) 为必填项。</div>
									</form>
								</div>
								<!-- /.col-lg-6 (nested) -->
							</div>
							<!-- /.row (nested) -->
						</div>
						<!-- /.panel-body -->
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
<jsp:include page="../public/kindeditor.jsp"></jsp:include>
<jsp:include page="../public/script.jsp">
	<jsp:param value="main/edit.js" name="loader" />
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