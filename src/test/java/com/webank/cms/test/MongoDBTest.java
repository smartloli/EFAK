package com.webank.cms.test;

import java.util.Date;
import java.util.List;

import com.mongodb.DBObject;
import com.webank.cms.domain.ArticleDomain;
import com.webank.cms.domain.TextDetailDomain;
import com.webank.cms.domain.UserDomain;
import com.webank.cms.factory.MongoDBFactory;
import com.webank.cms.service.ArticleService;
import com.webank.cms.service.DashBoardService;
import com.webank.cms.service.UserService;
import com.webank.cms.utils.MD5Utils;

/**
 * @Date Aug 1, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class MongoDBTest {

	public static void main(String[] args) {
		// MongoDBFactory.update(new BasicDBObject("_id", new
		// ObjectId("57a038643004f299c4270cd7")), new BasicDBObject("status",
		// "fasa"), "article_list");
//		UserDomain ret = UserService.get("zmm", MD5Utils.md5("123456"));
//		long count = MongoDBFactory.findDelCount("del_article_list", "zmm");
		DashBoardService.updateDelCount("zmm");
		System.out.println(DashBoardService.getDelCount("zmm"));
	}

	private static void find() {
		List<DBObject> list = MongoDBFactory.find("zmm", "news");
		System.out.println(list.size());
	}

	public static void addUser() {
		UserDomain user = new UserDomain();
		user.setUsername("zmm");
		user.setPassword(MD5Utils.md5("123456"));
		user.setAdminType(0);
		MongoDBFactory.save(user.toString(), "user_list");
	}

	private static void addArticle() {
		for (int i = 0; i < 25; i++) {
			ArticleDomain art = new ArticleDomain();
			art.setAuthor("zmm2");
			art.setChanle("新闻");
			art.setCreateDate(new Date().toString());
			art.setStatus("未发布");
			art.setTitle("台风" + i);
			MongoDBFactory.save(art.toString(), "article_list");
		}
	}

	private static void modify() {
		String json = "{\"articleID\":1470379061341,\"title\":\"我是是ss\",\"type\":\"新闻\",\"sDate\":\"20160728\",\"eDate\":\"20160804\",\"chanle\":\"新闻\",\"isTop\":\"是\",\"content\":\"\u003ch1\u003e\r\n\t1. QQ去\r\n\u003c/h1\u003e\r\n\u003cp\u003e\r\n\tqqqdsdsds\r\n\u003c/p\u003e\",\"isValid\":\"-1\"}";
		MongoDBFactory.modify(1470379061341L, json, "text_detail_list");
	}

	private static void save() {
		TextDetailDomain text = new TextDetailDomain();
		text.setChanle("新闻");
		text.setContent("大新闻，有台风来袭！");
		text.seteDate("20160801 15:48:22");
		text.setsDate("20160801 10:48:22");
		text.setTitle("台风");
		text.setType("新闻");
		MongoDBFactory.save(text.toString(), "news");
	}

}
