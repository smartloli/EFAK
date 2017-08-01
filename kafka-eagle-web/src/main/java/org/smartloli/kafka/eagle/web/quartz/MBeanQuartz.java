/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz;

import java.util.ArrayList;
import java.util.List;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Per 5 mins to stats mbean from kafka jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 19, 2017
 */
public class MBeanQuartz {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Mx4j service interface. */
	private Mx4jService mx4jService = new Mx4jFactory().create();

	public void mbeanQuartz() {
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			execute(clusterAlias);
		}
	}

	private void execute(String clusterAlias) {
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		List<KpiInfo> list = new ArrayList<>();
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			String uri = broker.getString("host") + ":" + broker.getInteger("jmxPort");
			MBeanInfo bytesIn = mx4jService.bytesInPerSec(uri);
			KpiInfo kpiByteIn = new KpiInfo();
			kpiByteIn.setCluster(clusterAlias);
			kpiByteIn.setKey("ByteIn");
			kpiByteIn.setValue(bytesIn.getMeanRate());
			kpiByteIn.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiByteIn);

			MBeanInfo bytesOut = mx4jService.bytesOutPerSec(uri);
			KpiInfo kpiByteOut = new KpiInfo();
			kpiByteOut.setCluster(clusterAlias);
			kpiByteOut.setKey("ByteOut");
			kpiByteOut.setValue(bytesOut.getMeanRate());
			kpiByteOut.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiByteOut);

			MBeanInfo bytesRejected = mx4jService.bytesRejectedPerSec(uri);
			KpiInfo kpiByteRejected = new KpiInfo();
			kpiByteRejected.setCluster(clusterAlias);
			kpiByteRejected.setKey("ByteRejected");
			kpiByteRejected.setValue(bytesRejected.getMeanRate());
			kpiByteRejected.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiByteRejected);

			MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(uri);
			KpiInfo kpiFailedFetchRequest = new KpiInfo();
			kpiFailedFetchRequest.setCluster(clusterAlias);
			kpiFailedFetchRequest.setKey("FailedFetchRequest");
			kpiFailedFetchRequest.setValue(failedFetchRequest.getMeanRate());
			kpiFailedFetchRequest.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiFailedFetchRequest);

			MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(uri);
			KpiInfo kpiFailedProduceRequest = new KpiInfo();
			kpiFailedProduceRequest.setCluster(clusterAlias);
			kpiFailedProduceRequest.setKey("FailedProduceRequest");
			kpiFailedProduceRequest.setValue(failedProduceRequest.getMeanRate());
			kpiFailedProduceRequest.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiFailedProduceRequest);

			MBeanInfo messageIn = mx4jService.messagesInPerSec(uri);
			KpiInfo kpiMessageIn = new KpiInfo();
			kpiMessageIn.setCluster(clusterAlias);
			kpiMessageIn.setKey("MessageIn");
			kpiMessageIn.setValue(messageIn.getMeanRate());
			kpiMessageIn.setTm(CalendarUtils.getCustomDate("yyyyMMddHH"));
			list.add(kpiMessageIn);
		}

		MetricsServiceImpl metrics = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		metrics.insert(list);
	}

}
