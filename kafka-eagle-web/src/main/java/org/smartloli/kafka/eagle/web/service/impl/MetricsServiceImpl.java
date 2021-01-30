/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.protocol.plugins.ConnectConfigInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;
import org.smartloli.kafka.eagle.common.util.KConstants.ZK;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jFactory;
import org.smartloli.kafka.eagle.core.factory.Mx4jService;
import org.smartloli.kafka.eagle.web.dao.BrokerDao;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Achieve access to the kafka monitoring data interface through jmx.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 17, 2017 Update by No 3, 2018 by cocodroid
 */
@Service
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private MBeanDao mbeanDao;

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private BrokerDao brokerDao;

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * Mx4j service interface.
     */
    private Mx4jService mx4jService = new Mx4jFactory().create();

    /**
     * Gets summary monitoring data for all broker.
     */
    public String getAllBrokersMBean(String clusterAlias) {
        String result = "";
        List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
        int brokerSize = SystemConfigUtils.getIntProperty(clusterAlias + ".kafka.eagle.broker.size");
        if (brokers.size() <= brokerSize) {
            result = getOnlineAllBrokersMBean(clusterAlias, brokers);
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            result = getOfflineAllBrokersMBean(params);
        }
        return result;
    }

    /**
     * Gets summary monitoring data for given brokerIds
     */
    public String getBrokersMBeanByIds(String clusterAlias, List<String> brokerIds) {
        List<BrokersInfo> brokersInfoByIds = kafkaService.getBrokersInfoByIds(clusterAlias, null, brokerIds);
        return getOnlineAllBrokersMBean(clusterAlias, brokersInfoByIds);

    }

    /**
     * Gets summary offline monitoring data for all broker.
     */
    private String getOfflineAllBrokersMBean(Map<String, Object> params) {
        Map<String, MBeanInfo> mbeans = new HashMap<>();
        List<MBeanOfflineInfo> mbeanOfflines = mbeanDao.getMBeanOffline(params);
        for (MBeanOfflineInfo mbeanOffline : mbeanOfflines) {
            MBeanInfo mbeanInfo = new MBeanInfo();
            mbeanInfo.setOneMinute(mbeanOffline.getOneMinute());
            mbeanInfo.setMeanRate(mbeanOffline.getMeanRate());
            mbeanInfo.setFiveMinute(mbeanOffline.getFiveMinute());
            mbeanInfo.setFifteenMinute(mbeanOffline.getFifteenMinute());
            switch (mbeanOffline.getKey()) {
                case MBean.MESSAGEIN:
                    mbeans.put(MBean.MESSAGES_IN, mbeanInfo);
                    break;
                case MBean.BYTEIN:
                    mbeans.put(MBean.BYTES_IN, mbeanInfo);
                    break;
                case MBean.BYTEOUT:
                    mbeans.put(MBean.BYTES_OUT, mbeanInfo);
                    break;
                case MBean.BYTESREJECTED:
                    mbeans.put(MBean.BYTES_REJECTED, mbeanInfo);
                    break;
                case MBean.FAILEDFETCHREQUEST:
                    mbeans.put(MBean.FAILED_FETCH_REQUEST, mbeanInfo);
                    break;
                case MBean.FAILEDPRODUCEREQUEST:
                    mbeans.put(MBean.FAILED_PRODUCE_REQUEST, mbeanInfo);
                    break;
                case MBean.TOTALFETCHREQUESTSPERSEC:
                    mbeans.put(MBean.TOTALFETCHREQUESTSPERSEC, mbeanInfo);
                    break;
                case MBean.TOTALPRODUCEREQUESTSPERSEC:
                    mbeans.put(MBean.TOTALPRODUCEREQUESTSPERSEC, mbeanInfo);
                    break;
                case MBean.REPLICATIONBYTESINPERSEC:
                    mbeans.put(MBean.REPLICATIONBYTESINPERSEC, mbeanInfo);
                    break;
                case MBean.REPLICATIONBYTESOUTPERSEC:
                    mbeans.put(MBean.REPLICATIONBYTESOUTPERSEC, mbeanInfo);
                    break;
                case MBean.PRODUCEMESSAGECONVERSIONS:
                    mbeans.put(MBean.PRODUCEMESSAGECONVERSIONS, mbeanInfo);
                    break;
                default:
                    break;
            }
        }

        return new Gson().toJson(mbeans);
    }

    /**
     * Gets summary online monitoring data for all broker.
     */
    private String getOnlineAllBrokersMBean(String clusterAlias, List<BrokersInfo> brokers) {
        Map<String, MBeanInfo> mbeans = new HashMap<>();
        for (BrokersInfo broker : brokers) {
            String uri = broker.getHost() + ":" + broker.getJmxPort();
            MBeanInfo bytesIn = mx4jService.bytesInPerSec(clusterAlias, uri);
            MBeanInfo bytesOut = mx4jService.bytesOutPerSec(clusterAlias, uri);
            MBeanInfo bytesRejected = mx4jService.bytesRejectedPerSec(clusterAlias, uri);
            MBeanInfo failedFetchRequest = mx4jService.failedFetchRequestsPerSec(clusterAlias, uri);
            MBeanInfo failedProduceRequest = mx4jService.failedProduceRequestsPerSec(clusterAlias, uri);
            MBeanInfo messageIn = mx4jService.messagesInPerSec(clusterAlias, uri);
            MBeanInfo produceMessageConversions = mx4jService.produceMessageConversionsPerSec(clusterAlias, uri);
            MBeanInfo totalFetchRequests = mx4jService.totalFetchRequestsPerSec(clusterAlias, uri);
            MBeanInfo totalProduceRequests = mx4jService.totalProduceRequestsPerSec(clusterAlias, uri);
            MBeanInfo replicationBytesInPerSec = mx4jService.replicationBytesInPerSec(clusterAlias, uri);
            MBeanInfo replicationBytesOutPerSec = mx4jService.replicationBytesOutPerSec(clusterAlias, uri);

            assembleMBeanInfo(mbeans, MBean.MESSAGES_IN, messageIn);

            assembleMBeanInfo(mbeans, MBean.BYTES_IN, bytesIn);

            assembleMBeanInfo(mbeans, MBean.BYTES_OUT, bytesOut);

            assembleMBeanInfo(mbeans, MBean.BYTES_REJECTED, bytesRejected);

            assembleMBeanInfo(mbeans, MBean.FAILED_FETCH_REQUEST, failedFetchRequest);

            assembleMBeanInfo(mbeans, MBean.FAILED_PRODUCE_REQUEST, failedProduceRequest);

            assembleMBeanInfo(mbeans, MBean.PRODUCEMESSAGECONVERSIONS, produceMessageConversions);

            assembleMBeanInfo(mbeans, MBean.TOTALFETCHREQUESTSPERSEC, totalFetchRequests);

            assembleMBeanInfo(mbeans, MBean.TOTALPRODUCEREQUESTSPERSEC, totalProduceRequests);

            assembleMBeanInfo(mbeans, MBean.REPLICATIONBYTESINPERSEC, replicationBytesInPerSec);

            assembleMBeanInfo(mbeans, MBean.REPLICATIONBYTESOUTPERSEC, replicationBytesOutPerSec);

        }
        for (Entry<String, MBeanInfo> entry : mbeans.entrySet()) {
            if (entry == null || entry.getValue() == null) {
                continue;
            }
            entry.getValue().setFifteenMinute(StrUtils.assembly(entry.getValue().getFifteenMinute()));
            entry.getValue().setFiveMinute(StrUtils.assembly(entry.getValue().getFiveMinute()));
            entry.getValue().setMeanRate(StrUtils.assembly(entry.getValue().getMeanRate()));
            entry.getValue().setOneMinute(StrUtils.assembly(entry.getValue().getOneMinute()));
        }
        return new Gson().toJson(mbeans);
    }

    private void assembleMBeanInfo(Map<String, MBeanInfo> mbeans, String mBeanInfoKey, MBeanInfo mBeanInfo) {
        if (mbeans.containsKey(mBeanInfoKey) && mBeanInfo != null) {
            MBeanInfo mbeanInfo = mbeans.get(mBeanInfoKey);
            String fifteenMinuteOld = mbeanInfo.getFifteenMinute() == null ? "0.0" : mbeanInfo.getFifteenMinute();
            String fifteenMinuteLastest = mBeanInfo.getFifteenMinute() == null ? "0.0" : mBeanInfo.getFifteenMinute();
            String fiveMinuteOld = mbeanInfo.getFiveMinute() == null ? "0.0" : mbeanInfo.getFiveMinute();
            String fiveMinuteLastest = mBeanInfo.getFiveMinute() == null ? "0.0" : mBeanInfo.getFiveMinute();
            String meanRateOld = mbeanInfo.getMeanRate() == null ? "0.0" : mbeanInfo.getMeanRate();
            String meanRateLastest = mBeanInfo.getMeanRate() == null ? "0.0" : mBeanInfo.getMeanRate();
            String oneMinuteOld = mbeanInfo.getOneMinute() == null ? "0.0" : mbeanInfo.getOneMinute();
            String oneMinuteLastest = mBeanInfo.getOneMinute() == null ? "0.0" : mBeanInfo.getOneMinute();
            long fifteenMinute = Math.round(StrUtils.numberic(fifteenMinuteOld)) + Math.round(StrUtils.numberic(fifteenMinuteLastest));
            long fiveMinute = Math.round(StrUtils.numberic(fiveMinuteOld)) + Math.round(StrUtils.numberic(fiveMinuteLastest));
            long meanRate = Math.round(StrUtils.numberic(meanRateOld)) + Math.round(StrUtils.numberic(meanRateLastest));
            long oneMinute = Math.round(StrUtils.numberic(oneMinuteOld)) + Math.round(StrUtils.numberic(oneMinuteLastest));
            mbeanInfo.setFifteenMinute(String.valueOf(fifteenMinute));
            mbeanInfo.setFiveMinute(String.valueOf(fiveMinute));
            mbeanInfo.setMeanRate(String.valueOf(meanRate));
            mbeanInfo.setOneMinute(String.valueOf(oneMinute));
        } else {
            mbeans.put(mBeanInfoKey, mBeanInfo);
        }
    }

    /**
     * Collection statistics data from kafka jmx & insert into table.
     */
    public int insert(List<KpiInfo> kpi) {
        return mbeanDao.insert(kpi);
    }

    /**
     * Query MBean data in different dimensions.
     */
    public String query(Map<String, Object> params) throws ParseException {

        List<KpiInfo> kpis = mbeanDao.query(params);

        JSONArray messageIns = new JSONArray();
        JSONArray byteIns = new JSONArray();
        JSONArray byteOuts = new JSONArray();
        JSONArray byteRejected = new JSONArray();
        JSONArray failedFetchRequest = new JSONArray();
        JSONArray failedProduceRequest = new JSONArray();
        JSONArray produceMessageConversions = new JSONArray();
        JSONArray totalFetchRequests = new JSONArray();
        JSONArray totalProduceRequests = new JSONArray();
        JSONArray replicationBytesOuts = new JSONArray();
        JSONArray replicationBytesIns = new JSONArray();

        JSONArray osFreeMems = new JSONArray();

        JSONArray zkSendPackets = new JSONArray();
        JSONArray zkReceivedPackets = new JSONArray();
        JSONArray zkNumAliveConnections = new JSONArray();
        JSONArray zkOutstandingRequests = new JSONArray();
        for (KpiInfo kpi : kpis) {
            switch (kpi.getKey()) {
                case ZK.ZK_SEND_PACKETS:
                    assembly(zkSendPackets, kpi);
                    break;
                case ZK.ZK_RECEIVEDPACKETS:
                    assembly(zkReceivedPackets, kpi);
                    break;
                case ZK.ZK_OUTSTANDING_REQUESTS:
                    assembly(zkOutstandingRequests, kpi);
                    break;
                case ZK.ZK_NUM_ALIVECONNRCTIONS:
                    assembly(zkNumAliveConnections, kpi);
                    break;
                case MBean.MESSAGEIN:
                    assembly(messageIns, kpi);
                    break;
                case MBean.BYTEIN:
                    assembly(byteIns, kpi);
                    break;
                case MBean.BYTEOUT:
                    assembly(byteOuts, kpi);
                    break;
                case MBean.BYTESREJECTED:
                    assembly(byteRejected, kpi);
                    break;
                case MBean.FAILEDFETCHREQUEST:
                    assembly(failedFetchRequest, kpi);
                    break;
                case MBean.FAILEDPRODUCEREQUEST:
                    assembly(failedProduceRequest, kpi);
                    break;
                case MBean.PRODUCEMESSAGECONVERSIONS:
                    assembly(produceMessageConversions, kpi);
                    break;
                case MBean.TOTALFETCHREQUESTSPERSEC:
                    assembly(totalFetchRequests, kpi);
                    break;
                case MBean.TOTALPRODUCEREQUESTSPERSEC:
                    assembly(totalProduceRequests, kpi);
                    break;
                case MBean.REPLICATIONBYTESINPERSEC:
                    assembly(replicationBytesOuts, kpi);
                    break;
                case MBean.REPLICATIONBYTESOUTPERSEC:
                    assembly(replicationBytesIns, kpi);
                    break;
                case MBean.OSFREEMEMORY:
                    assembly(osFreeMems, kpi);
                    break;
                default:
                    break;
            }
        }
        JSONObject target = new JSONObject();
        target.put("send", zkSendPackets);
        target.put("received", zkReceivedPackets);
        target.put("queue", zkOutstandingRequests);
        target.put("alive", zkNumAliveConnections);
        target.put("messageIns", messageIns);
        target.put("byteIns", byteIns);
        target.put("byteOuts", byteOuts);
        target.put("byteRejected", byteRejected);
        target.put("failedFetchRequest", failedFetchRequest);
        target.put("failedProduceRequest", failedProduceRequest);
        target.put("produceMessageConversions", produceMessageConversions);
        target.put("totalFetchRequests", totalFetchRequests);
        target.put("totalProduceRequests", totalProduceRequests);
        target.put("replicationBytesIns", replicationBytesIns);
        target.put("replicationBytesOuts", replicationBytesOuts);
        target.put("osFreeMems", osFreeMems);

        return target.toJSONString();
    }

    private void assembly(JSONArray assemblys, KpiInfo kpi) throws ParseException {
        JSONObject object = new JSONObject();
        object.put("x", CalendarUtils.convertUnixTime(kpi.getTimespan(), "yyyy-MM-dd HH:mm"));
        object.put("y", kpi.getValue());
        assemblys.add(object);
    }

    /**
     * Crontab clean data.
     */
    public void remove(int tm) {
        mbeanDao.remove(tm);
    }

    @Override
    public void cleanTopicLogSize(int tm) {
        topicDao.cleanTopicLogSize(tm);
    }

    @Override
    public void cleanTopicRank(int tm) {
        topicDao.cleanTopicRank(tm);
    }

    @Override
    public void cleanBScreenConsumerTopic(int tm) {
        topicDao.cleanBScreenConsumerTopic(tm);
    }

    @Override
    public int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers) {
        return topicDao.writeBSreenConsumerTopic(bscreenConsumers);
    }

    @Override
    public BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params) {
        return topicDao.readBScreenLastTopic(params);
    }

    @Override
    public void cleanTopicSqlHistory(int tm) {
        topicDao.cleanTopicSqlHistory(tm);
    }

    @Override
    public int mbeanOfflineInsert(List<MBeanOfflineInfo> kpis) {
        return mbeanDao.mbeanOfflineInsert(kpis);
    }

    @Override
    public int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups) {
        return topicDao.writeConsumerGroupTopics(consumerGroups);
    }

    @Override
    public List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params) {
        return topicDao.getAllConsumerGroups(params);
    }

    @Override
    public int cleanConsumerGroupTopic(Map<String, Object> params) {
        return topicDao.cleanConsumerGroupTopic(params);
    }

    @Override
    public int cleanConsumerSummaryTopic(Map<String, Object> params) {
        return topicDao.cleanConsumerSummaryTopic(params);
    }

    @Override
    public int writeConsumerSummaryTopics(List<ConsumerSummaryInfo> consumerSummarys) {
        return topicDao.writeConsumerSummaryTopics(consumerSummarys);
    }

    @Override
    public List<ConsumerSummaryInfo> getAllConsumerSummary(Map<String, Object> params) {
        return topicDao.getAllConsumerSummary(params);
    }

    @Override
    public List<ConnectConfigInfo> detectConnectConfigList(Map<String, Object> params) {
        return brokerDao.detectConnectConfigList(params);
    }

    @Override
    public int insertOrUpdateConnectConfig(ConnectConfigInfo connectConfig) {
        return brokerDao.insertOrUpdateConnectConfig(connectConfig);
    }

    @Override
    public int modifyConnectConfigStatusById(ConnectConfigInfo connectConfig) {
        return brokerDao.modifyConnectConfigStatusById(connectConfig);
    }

    @Override
    public boolean hasBrokerId(String clusterAlias, String brokerId) {
        List<BrokersInfo> allBrokersInfo = kafkaService.getAllBrokersInfo(clusterAlias);
        for (BrokersInfo brokerInfo : allBrokersInfo) {
            if (brokerInfo.getIds().equals(brokerId)) {
                return true;
            }
        }
        return false;
    }

}
