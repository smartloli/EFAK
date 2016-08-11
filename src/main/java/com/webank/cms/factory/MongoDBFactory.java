package com.webank.cms.factory;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.webank.cms.utils.MongdbUtils;
import com.webank.cms.utils.SystemConfigUtils;

/**
 * @Date Mar 3, 2015
 *
 * @Author dengjie
 */
public class MongoDBFactory {

	private static Logger LOG = LoggerFactory.getLogger(MongoDBFactory.class);

	// save data to mongodb
	public static void save(String mgs, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			DBObject dbo = (DBObject) com.mongodb.util.JSON.parse(mgs);
			coll.insert(dbo);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
	}

	// batch insert
	public static void save(List<?> mgsList, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			if (db.collectionExists(tabName)) {
				db.getCollection(tabName).drop();
			}
			DBCollection coll = db.getCollection(tabName);
			// {"id":1},{"unique":true,"dropDups":true}
			BasicDBObject index = new BasicDBObject("id", 1);
			BasicDBObject opt = new BasicDBObject("unique", "true").append("dropDups", "true");
			coll.ensureIndex(index, opt);
			BasicDBList data = (BasicDBList) JSON.parse(mgsList.toString());
			List<DBObject> list = new ArrayList<DBObject>();
			int commitSize = SystemConfigUtils.getIntProperty("cms.webank.mongodb.commit.size");
			int rowCount = 0;
			long start = System.currentTimeMillis();
			for (Object dbo : data) {
				rowCount++;
				list.add((DBObject) dbo);
				if (rowCount % commitSize == 0) {
					try {
						coll.insert(list);
						list.clear();
						LOG.info(String.format("current commit rowCount = [%d],commit spent time = [%s]s", rowCount, (System.currentTimeMillis() - start) / 1000.0));
					} catch (Exception ex) {
						ex.printStackTrace();
						LOG.error(String.format("batch commit data to mongodb has error,msg is %s", ex.getMessage()));
					}
				}
			}
			if (rowCount % commitSize != 0) {
				try {
					coll.insert(list);
					LOG.info(String.format("insert data to mongo has spent total time = [%s]s", (System.currentTimeMillis() - start) / 1000.0));
				} catch (Exception ex) {
					ex.printStackTrace();
					LOG.error(String.format("commit end has error,msg is %s", ex.getMessage()));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object list to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
	}

	public static boolean modify(long articleID, String mgs, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			DBObject dbo = (DBObject) JSON.parse(mgs);
			coll.update(new BasicDBObject("articleID", articleID), dbo);
			LOG.info("modify articleID[" + articleID + "],tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
			return false;
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return true;
	}

	public static boolean edit(BasicDBObject query, BasicDBObject update, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			coll.update(query, new BasicDBObject("$set", update), true, false);
			LOG.info("modify tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
			return false;
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return true;
	}

	public static void update(BasicDBObject query, BasicDBObject update, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			coll.update(query, new BasicDBObject("$set", update), true, false);
			LOG.info("modify tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
	}

	public static void delete(String id, String tabName) {
		DB db = null;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			coll.remove(new BasicDBObject("_id", new ObjectId(id)));
			LOG.info("delete id[" + id + "],tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
	}

	public static List<DBObject> find(String author, String tabName) {
		DB db = null;
		List<DBObject> ret = new ArrayList<DBObject>();
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			ret = coll.find(new BasicDBObject("author", author)).toArray();
			LOG.info("find author[" + author + "],tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return ret;
	}

	public static String login(String username, String password, String tabName) {
		DB db = null;
		String ret = "";
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			BasicDBObject user = new BasicDBObject();
			user.put("username", username);
			user.put("password", password);
			DBObject obj = coll.findOne(user);
			ret = obj == null ? "" : obj.toString();
			LOG.info("find username[" + username + "],password[" + password + "],tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return ret;
	}

	public static List<DBObject> findByID(long articleId, String tabName) {
		DB db = null;
		List<DBObject> ret = new ArrayList<DBObject>();
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			ret = coll.find(new BasicDBObject("articleID", articleId)).toArray();
			LOG.info("find articleID[" + articleId + "],tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return ret;
	}

	public static long findDelCount(String tabName, String username) {
		DB db = null;
		Long ret = 0L;
		try {
			db = MongdbUtils.getDB();
			DBCollection coll = db.getCollection(tabName);
			ret = Long.parseLong(coll.findOne(new BasicDBObject("username", username)) == null ? "0" : com.alibaba.fastjson.JSON.parseObject(coll.findOne(new BasicDBObject("username", username)).toString()).getString("count"));
			LOG.info("find tabName[" + tabName + "] has success");
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error(String.format("save object to mongodb has error,msg is %s", ex.getMessage()));
		} finally {
			if (db != null) {
				db.requestDone();
				db = null;
			}
		}
		return ret;
	}
	
}
