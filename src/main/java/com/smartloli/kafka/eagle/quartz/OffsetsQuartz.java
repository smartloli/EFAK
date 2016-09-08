package com.smartloli.kafka.eagle.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.AlarmDomain;
import com.smartloli.kafka.eagle.domain.OffsetZkDomain;
import com.smartloli.kafka.eagle.domain.OffsetsSQLiteDomain;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.service.SQLiteService;
import com.smartloli.kafka.eagle.utils.CalendarUtils;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;
import com.smartloli.kafka.eagle.utils.SendMessageUtils;
import com.smartloli.kafka.eagle.utils.SystemConfigUtils;

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
	private static Logger LOG = LoggerFactory.getLogger(OffsetsQuartz.class);

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
				OffsetsSQLiteDomain offsetSQLite = new OffsetsSQLiteDomain();
				for (String partitionStr : KafkaClusterUtils.findTopicPartition(topic)) {
					int partition = Integer.parseInt(partitionStr);
					long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partition);
					OffsetZkDomain offsetZk = KafkaClusterUtils.getOffset(topic, group, partition);
					offsetSQLite.setGroup(group);
					offsetSQLite.setCreated(CalendarUtils.getStatsPerDate());
					offsetSQLite.setTopic(topic);
					if (logSize == 0) {
						offsetSQLite.setLag(0L + offsetSQLite.getLag());
					} else {
						long lag = offsetSQLite.getLag() + (offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
						offsetSQLite.setLag(lag);
					}
					offsetSQLite.setLogSize(logSize + offsetSQLite.getLogSize());
					offsetSQLite.setOffsets(offsetZk.getOffset() + offsetSQLite.getOffsets());
				}
				list.add(offsetSQLite);
			}
		}
		String sql = "INSERT INTO offsets values(?,?,?,?,?,?)";
		SQLiteService.insert(list, sql);
		boolean alarmEnable = SystemConfigUtils.getBooleanProperty("kafka.eagel.mail.enable");
		if (alarmEnable) {
			List<AlarmDomain> listAlarm = alarmConfigure();
			for (AlarmDomain alarm : listAlarm) {
				for (OffsetsSQLiteDomain offset : list) {
					if (offset.getGroup().equals(alarm.getGroup()) && offset.getTopic().equals(alarm.getTopics()) && offset.getLag() > alarm.getLag()) {
						try {
							SendMessageUtils.send(alarm.getOwners(), "Alarm Lag", "Lag exceeds a specified threshold,Topic is [" + alarm.getTopics() + "],current lag is [" + offset.getLag() + "],expired lag is [" + alarm.getLag() + "].");
						} catch (Exception ex) {
							LOG.error("Topic[" + alarm.getTopics() + "] Send alarm mail has error,msg is " + ex.getMessage());
						}
					}
				}
			}
		}
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

	private static List<AlarmDomain> alarmConfigure() {
		String sql = "SELECT * FROM alarm";
		String ret = SQLiteService.select(sql);
		List<AlarmDomain> list = new ArrayList<>();
		JSONArray array = JSON.parseArray(ret);
		for (Object object : array) {
			AlarmDomain alarm = new AlarmDomain();
			JSONObject obj = (JSONObject) object;
			alarm.setGroup(obj.getString("groups"));
			alarm.setTopics(obj.getString("topic"));
			alarm.setLag(obj.getLong("lag"));
			alarm.setOwners(obj.getString("owner"));
			list.add(alarm);
		}
		return list;
	}

	public static void main(String[] args) {
		OffsetsQuartz offsets = new OffsetsQuartz();
		offsets.jobQuartz();
	}
}
