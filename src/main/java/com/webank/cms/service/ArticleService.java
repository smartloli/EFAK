package com.webank.cms.service;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.webank.cms.domain.ArticleDomain;
import com.webank.cms.factory.MongoDBFactory;

/**
 * @Date Aug 1, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class ArticleService {

	private final static String TAB_NAME = "article_list";

	public static void addArticle(ArticleDomain art) {
		MongoDBFactory.save(art.toString(), TAB_NAME);
	}

	public static String getArticleList(String username) {
		return MongoDBFactory.find(username, TAB_NAME).toString();
	}

	public static void delArticle(String id) {
		MongoDBFactory.delete(id, TAB_NAME);
	}
	
	public static void updateStatus(String id, String status) {
		MongoDBFactory.update(new BasicDBObject("_id", new ObjectId(id)), new BasicDBObject("status", status), TAB_NAME);
	}

	public static boolean edit(ArticleDomain arts) {
		BasicDBObject query = new BasicDBObject();
		query.put("articleID", arts.getArticleID());
		BasicDBObject update = new BasicDBObject();
		update.put("title", arts.getTitle());
		update.put("chanle", arts.getChanle());
		update.put("isTop", arts.getIsTop());
		update.put("createDate", arts.getCreateDate());

		return MongoDBFactory.edit(query, update, TAB_NAME);
	}

}
