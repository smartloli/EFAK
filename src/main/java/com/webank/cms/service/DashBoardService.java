package com.webank.cms.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.webank.cms.domain.DashBoardDomain;
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
public class DashBoardService {

	private final static String TAB_NAME = "del_article_list";

	public static void updateDelCount(String username) {
		long count = MongoDBFactory.findDelCount(TAB_NAME, username);
		MongoDBFactory.update(new BasicDBObject("username", username), new BasicDBObject("count", count + 1), TAB_NAME);
	}

	public static long getDelCount(String username) {
		return MongoDBFactory.findDelCount(TAB_NAME, username);
	}

	public static DashBoardDomain getRelease(String username) {
		List<DBObject> list = MongoDBFactory.find(username, "article_list");
		DashBoardDomain dash = new DashBoardDomain();
		dash.setDel(getDelCount(username));
		dash.setAdd(list.size());
		long release = 0L;
		JSONArray ret = JSON.parseArray(list.toString());
		for (Object tmp : ret) {
			JSONObject obj = (JSONObject) tmp;
			if ("发布".equals(obj.getString("status"))) {
				release++;
			}
		}
		dash.setRelease(release);
		dash.setUnRelease(list.size() - release);
		return dash;
	}

}
