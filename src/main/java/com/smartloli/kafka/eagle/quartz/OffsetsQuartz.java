package com.smartloli.kafka.eagle.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.OffsetZkDomain;
import com.smartloli.kafka.eagle.domain.OffsetsSQLiteDomain;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.service.SQLiteService;
import com.smartloli.kafka.eagle.utils.CalendarUtils;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note Per 5 mins to stats offsets to offsets table
 */
public class OffsetsQuartz {
	private static LRUCacheUtils<String, TupleDomain> lruCache = new LRUCacheUtils<String, TupleDomain>(100000);

	public void cleanHistoryData() {
		System.out.println(CalendarUtils.getStatsPerDate());
		String sql = "delete from offsets where created<'" + CalendarUtils.getYestoday() + "'";
		SQLiteService.update(sql);
	}

	public void jobQuartz() {
		List<String> hosts = getBrokers();
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();
		Map<String, List<String>> consumers = KafkaClusterUtils.getConsumers();
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			String group = entry.getKey();
			for (String topic : entry.getValue()) {
				for (String partitionStr : KafkaClusterUtils.findTopicPartition(topic)) {
					int partition = Integer.parseInt(partitionStr);
					OffsetsSQLiteDomain offsetSQLite = new OffsetsSQLiteDomain();
					long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partition);
					OffsetZkDomain offsetZk = KafkaClusterUtils.getOffset(topic, group, partition);
					offsetSQLite.setGroup(group);
					offsetSQLite.setCreated(CalendarUtils.getStatsPerDate());
					offsetSQLite.setTopic(topic);
					offsetSQLite.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
					offsetSQLite.setLogSize(logSize);
					offsetSQLite.setOffsets(offsetZk.getOffset());
					list.add(offsetSQLite);
				}
			}
		}
		String sql = "INSERT INTO offsets values(?,?,?,?,?,?)";
		SQLiteService.insert(list, sql);
	}

	private static List<String> getBrokers() {
		// Add LRUCache per 3 min
		String key = "group_topic_offset_graph_consumer_brokers";
		String brokers = "";
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			brokers = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 30) {// 30 mins
				lruCache.remove(key);
			}
		} else {
			brokers = KafkaClusterUtils.getAllBrokersInfo();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(brokers);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}
		JSONArray arr = JSON.parseArray(brokers);
		List<String> list = new ArrayList<String>();
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			String host = obj.getString("host");
			int port = obj.getInteger("port");
			list.add(host + ":" + port);
		}
		return list;
	}

	public static void main(String[] args) {
		OffsetsQuartz offset = new OffsetsQuartz();
		offset.jobQuartz();
	}
}
