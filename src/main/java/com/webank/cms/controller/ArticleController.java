package com.webank.cms.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.webank.cms.service.ArticleService;
import com.webank.cms.service.DashBoardService;
import com.webank.cms.utils.GzipUtils;

@Controller
public class ArticleController {

	private final Logger LOG = LoggerFactory.getLogger(ArticleController.class);

	@RequestMapping(value = "/article", method = RequestMethod.GET)
	public ModelAndView indexView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/article/article");
		return mav;
	}

	@RequestMapping(value = "/article/{username}/table/ajax", method = RequestMethod.GET)
	public void articleListAjax(@PathVariable("username") String username, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Charset", "utf-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Content-Encoding", "gzip");

		String ip = request.getHeader("x-forwarded-for");
		LOG.info("IP:" + (ip == null ? request.getRemoteAddr() : ip));

		String aoData = request.getParameter("aoData");
		JSONArray jsonArray = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		for (Object obj : jsonArray) {
			JSONObject jsonObj = (JSONObject) obj;
			if ("sEcho".equals(jsonObj.getString("name"))) {
				sEcho = jsonObj.getIntValue("value");
			} else if ("iDisplayStart".equals(jsonObj.getString("name"))) {
				iDisplayStart = jsonObj.getIntValue("value");
			} else if ("iDisplayLength".equals(jsonObj.getString("name"))) {
				iDisplayLength = jsonObj.getIntValue("value");
			}
		}

		JSONArray ret = JSON.parseArray(ArticleService.getArticleList(username));
		int offset = 0;
		JSONArray retArr = new JSONArray();
		for (Object tmp : ret) {
			JSONObject tmp2 = (JSONObject) tmp;
			if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
				JSONObject obj = new JSONObject();
				String id = JSON.parseObject(tmp2.getString("_id")).getString("$oid");
				long articleID = tmp2.getLong("articleID");
				String status = tmp2.getString("status");
				obj.put("title", tmp2.getString("title"));
				obj.put("articleID", articleID);
				obj.put("chanle", tmp2.getString("chanle"));
				obj.put("isTop", tmp2.getString("isTop"));
				obj.put("author", tmp2.getString("author"));
				obj.put("createDate", tmp2.getString("createDate"));
				obj.put("status", status);
				if ("发布".equals(status)) {
					obj.put("operate", "<a href='/cms/article/edit/" + articleID + "' class='btn btn-primary btn-xs'>编辑</a>&nbsp;<a href='/cms/article/preview/" + articleID
							+ "' class='btn btn-primary btn-xs'>预览</a>&nbsp;<a href='/cms/article/" + id + "/del' class='btn btn-danger btn-xs'>删除</a>&nbsp;<a readonly=true href='/cms/article/" + id
							+ "/unrelease/ajax' class='btn btn-primary btn-xs'>取消发布</a>");
				} else {
					obj.put("operate", "<a href='/cms/article/edit/" + articleID + "' class='btn btn-primary btn-xs'>编辑</a>&nbsp;<a href='/cms/article/preview/" + articleID
							+ "' class='btn btn-primary btn-xs'>预览</a>&nbsp;<a href='/cms/article/" + id + "/del' class='btn btn-danger btn-xs'>删除</a>&nbsp;<a href='/cms/article/" + id
							+ "/release/ajax' class='btn btn-primary btn-xs'>发布</a>");
				}
				retArr.add(obj);
			}
			offset++;
		}

		JSONObject obj = new JSONObject();
		obj.put("sEcho", sEcho);
		obj.put("iTotalRecords", ret.size());
		obj.put("iTotalDisplayRecords", ret.size());
		obj.put("aaData", retArr);
		try {
			byte[] output = GzipUtils.compressToByte(obj.toJSONString());
			response.setContentLength(output.length);
			OutputStream out = response.getOutputStream();
			out.write(output);

			out.flush();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/article/{id}/del", method = RequestMethod.GET)
	public ModelAndView articleDelete(@PathVariable("id") String id, HttpSession session) {
		JSONObject obj = JSON.parseObject(session.getAttribute("user").toString());
		String username = obj.getString("username");
		DashBoardService.updateDelCount(username);
		ArticleService.delArticle(id);
		return new ModelAndView("redirect:/");
	}

	@ResponseBody
	@RequestMapping(value = "/article/{id}/{status}/ajax", method = RequestMethod.GET)
	public ModelAndView statusUpdateAjax(@PathVariable("id") String id, @PathVariable("status") String status) {
		if ("release".equals(status)) {
			status = "发布";
		} else {
			status = "未发布";
		}
		ArticleService.updateStatus(id, status);
		return new ModelAndView("redirect:/article");
	}

	public static void main(String[] args) {
		String ret = ArticleService.getArticleList("zmm2");
		JSONArray arr = JSON.parseArray(ret);
		for (Object obj : arr) {
			JSONObject obj1 = (JSONObject) obj;
			String id = JSON.parseObject(obj1.getString("_id")).getString("$oid");
			System.out.println(id);
		}
	}

}
