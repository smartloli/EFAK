package com.webank.cms.service;

import com.mongodb.BasicDBObject;
import com.webank.cms.domain.TextDetailDomain;
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
public class TextDetailService {

	private final static String TAB_NAME = "text_detail_list";

	public static void addTextDetail(TextDetailDomain text) {
		MongoDBFactory.save(text.toString(), TAB_NAME);
	}

	public static String getArticleByID(long articleId) {
		return MongoDBFactory.findByID(articleId, TAB_NAME).toString();
	}

	public static void delArticle(String id) {
		MongoDBFactory.delete(id, TAB_NAME);
	}

	public static boolean edit(TextDetailDomain text) {
		BasicDBObject query = new BasicDBObject();
		query.put("articleID", text.getArticleID());
		BasicDBObject update = new BasicDBObject();
		update.put("title", text.getTitle());
		update.put("type", text.getType());
		update.put("sDate", text.getsDate());
		update.put("eDate", text.geteDate());
		update.put("chanle", text.getChanle());
		update.put("isTop", text.getIsTop());
		update.put("content", text.getContent());
		update.put("isValid", text.getIsValid());

		return MongoDBFactory.edit(query, update, TAB_NAME);
	}

}
