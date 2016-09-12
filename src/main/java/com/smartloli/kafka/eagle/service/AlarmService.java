package com.smartloli.kafka.eagle.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.AlarmDomain;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.utils.DBZKDataUtils;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;

/**
 * @Date Sep 6, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class AlarmService {
	private static LRUCacheUtils<String, TupleDomain> map = new LRUCacheUtils<String, TupleDomain>(100000);

	public static String getTopics(String ip) {
		String key = "alarm_topic_" + ip;
		String ret = "";
		if (map.containsKey(key)) {
			TupleDomain tuple = map.get(key);
			ret = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 1) {// 1 mins
				map.remove(key);
			}
		} else {
			Map<String, List<String>> tmp = KafkaClusterUtils.getConsumers();
			JSONArray retArray = new JSONArray();
			for(Entry<String, List<String>> entry : tmp.entrySet()){
				JSONObject retObj = new JSONObject();
				retObj.put("group", entry.getKey());
				retObj.put("topics", entry.getValue());
				retArray.add(retObj);
			}
			ret = retArray.toJSONString();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(ret);
			tuple.setTimespan(System.currentTimeMillis());
			map.put(key, tuple);
		}
		return ret;
	}

	public static Map<String, Object> addAlarm(AlarmDomain alarm) {
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = "replace into alarm values(?,?,?,?,?)";
		int status = SQLiteService.insert(alarm, sql);
		if (status == -1) {
			map.put("status", "error");
			map.put("info", "insert [" + alarm + "] has error!");
		} else {
			map.put("status", "success");
			map.put("info", "insert success!");
		}
		return map;
	}

	public static String list() {
//		String sql = "select * from alarm";
//		return SQLiteService.select(sql).toString();
		return DBZKDataUtils.getAlarm();
	}

	public static void delete(String group, String topic, String owner) {
		String sql = "delete from alarm where groups='" + group + "' and topic='" + topic + "' and owner='" + owner + "'";
		SQLiteService.update(sql);
	}

	public static void main(String[] args) {
		System.out.println(KafkaClusterUtils.getConsumers());
	}

}
