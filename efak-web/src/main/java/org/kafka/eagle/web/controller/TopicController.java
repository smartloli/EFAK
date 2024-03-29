/**
 * WelcomeController.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.web.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.constants.ResponseModuleType;
import org.kafka.eagle.common.utils.CalendarUtil;
import org.kafka.eagle.common.utils.HtmlAttributeUtil;
import org.kafka.eagle.common.utils.MathUtil;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.topic.*;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.ITopicDaoService;
import org.kafka.eagle.web.service.ITopicSummaryDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In the provided example, the TopicController handles the root URL request ("/") and is
 * responsible for displaying the cluster management interface. The welcome() method is
 * annotated with @GetMapping("/") to map the root URL request to this method.
 * It returns the name of the cluster management view template, which will be resolved by
 * the configured view resolver.
 *
 * @Author: smartloli
 * @Date: 2023/6/16 23:45
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/topic")
@Slf4j
public class TopicController {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicDaoService topicDaoService;

    @Autowired
    private ITopicSummaryDaoService topicSummaryDaoService;

    /**
     * Handles the root URL request and displays the cluster management interface.
     *
     * @return The name of the cluster management view template.
     */
    @Secured(value = "ROLE_ADMIN")
    @GetMapping("/create")
    public String createView() {
        return "topic/create.html";
    }

    /**
     * Topic manage view.
     *
     * @return
     */
    @GetMapping("/manage")
    public String topicManageView() {
        return "topic/manage.html";
    }

    @GetMapping("/meta/{topic}")
    public String topicMetaView(@PathVariable("topic") String topic, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        HttpSession session = request.getSession();
        log.info("Topic metadata:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        TopicInfo topicInfo = topicDaoService.topics(clusterInfo.getClusterId(), topic);
        if (topicInfo == null) {
            return "redirect:/error/404";
        }

        return "topic/meta.html";
    }

    /**
     * test topic view.
     *
     * @return
     */
    @GetMapping("/mock")
    public String topicMockView() {
        return "topic/mock.html";
    }

    /**
     * Create new topic.
     *
     * @param newTopicInfo
     * @param response
     * @param session
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/name/create", method = RequestMethod.POST)
    public String createTopicName(NewTopicInfo newTopicInfo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic create:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        JSONObject target = new JSONObject();
        if (brokerInfos == null || brokerInfos.size() == 0) {
            target.put("status", false);
            target.put("msg", ResponseModuleType.CREATE_TOPIC_NOBROKERS_ERROR.getName());
        } else {
            if (newTopicInfo.getReplication() > brokerInfos.size()) {
                target.put("status", false);
                target.put("msg", ResponseModuleType.CREATE_TOPIC_REPLICAS_ERROR.getName());
            } else {
                KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
                KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
                boolean status = ksf.createTopicName(kafkaClientInfo, newTopicInfo);
                if (status) {
                    target.put("status", status);
                } else {
                    target.put("status", status);
                    target.put("msg", ResponseModuleType.CREATE_TOPIC_SERVICE_ERROR.getName());
                }
            }
        }

        return target.toString();
    }

    /**
     * topic manage table data.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/manage/table/ajax", method = RequestMethod.GET)
    public void pageTopicNameAjax(HttpServletResponse response, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        HttpSession session = request.getSession();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic name list:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);

        String aoData = request.getParameter("aoData");
        JSONArray params = JSON.parseArray(aoData);
        int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
        String search = "";
        for (Object object : params) {
            JSONObject param = (JSONObject) object;
            if ("sEcho".equals(param.getString("name"))) {
                sEcho = param.getIntValue("value");
            } else if ("iDisplayStart".equals(param.getString("name"))) {
                iDisplayStart = param.getIntValue("value");
            } else if ("iDisplayLength".equals(param.getString("name"))) {
                iDisplayLength = param.getIntValue("value");
            } else if ("sSearch".equals(param.getString("name"))) {
                search = param.getString("value");
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("start", iDisplayStart / iDisplayLength + 1);
        map.put("size", iDisplayLength);
        map.put("search", search);
        map.put("cid", clusterInfo.getClusterId());


        Page<TopicInfo> pages = this.topicDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (TopicInfo topicInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("topicName", "<a href='/topic/meta/" + topicInfo.getTopicName() + "'>" + topicInfo.getTopicName() + "</a>");
            target.put("partition", topicInfo.getPartitions());
            target.put("replicas", topicInfo.getReplications());
            target.put("brokerSpread", HtmlAttributeUtil.getTopicSpreadHtml(topicInfo.getBrokerSpread()));
            target.put("brokerSkewed", HtmlAttributeUtil.getTopicSkewedHtml(topicInfo.getBrokerSkewed()));
            target.put("brokerLeaderSkewed", HtmlAttributeUtil.getTopicLeaderSkewedHtml(topicInfo.getBrokerLeaderSkewed()));
            target.put("retainMs", MathUtil.millis2Hours(topicInfo.getRetainMs()));
            target.put("operate", "<a href='' cid='" + topicInfo.getClusterId() + "' topic='" + topicInfo.getTopicName() + "' partitions='" + topicInfo.getPartitions() + "' name='efak_topic_manage_add_partition' class='badge border border-primary text-primary'>扩分区</a> <a href='' cid='" + topicInfo.getClusterId() + "' topic='" + topicInfo.getTopicName() + "' name='efak_topic_manage_del' class='badge border border-danger text-danger'>删除</a>");
            aaDatas.add(target);
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", pages.getTotal());
        target.put("iTotalDisplayRecords", pages.getTotal());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get topic name list has error, msg is {}", ex);
        }
    }


    /**
     * Add topic partition.
     *
     * @param newTopicInfo
     * @param response
     * @param session
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/partition/add", method = RequestMethod.POST)
    public String addTopicPartition(NewTopicInfo newTopicInfo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        JSONObject target = new JSONObject();
        if (brokerInfos == null || brokerInfos.size() == 0) {
            target.put("status", false);
            target.put("msg", ResponseModuleType.CREATE_PARTITION_NOBROKERS_ERROR.getName());
        } else {
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            boolean status = ksf.addTopicPartitions(kafkaClientInfo, newTopicInfo);
            if (status) {
                target.put("status", status);
            } else {
                target.put("status", status);
                target.put("msg", ResponseModuleType.CREATE_PARTITION_SERVICE_ERROR.getName());
            }
        }

        return target.toString();
    }

    /**
     * delete topic.
     *
     * @param newTopicInfo
     * @param response
     * @param session
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/manage/name/del", method = RequestMethod.POST)
    public String deleteTopic(NewTopicInfo newTopicInfo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        JSONObject target = new JSONObject();
        if (brokerInfos == null || brokerInfos.size() == 0) {
            target.put("status", false);
            target.put("msg", ResponseModuleType.CREATE_TOPIC_DEL_NOBROKERS_ERROR.getName());
        } else {
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            boolean status = ksf.deleteTopic(kafkaClientInfo, newTopicInfo);
            if (status) {
                target.put("status", status);
            } else {
                target.put("status", status);
                target.put("msg", ResponseModuleType.CREATE_TOPIC_DEL_SERVICE_ERROR.getName());
            }
        }

        return target.toString();
    }

    /**
     * topic metadata table info.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/meta/table/ajax", method = RequestMethod.GET)
    public void pageTopicMetaAjax(@RequestParam("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic meta list:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        HttpSession session = request.getSession();
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());

        String aoData = request.getParameter("aoData");
        JSONArray params = JSON.parseArray(aoData);
        int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
        String search = "";
        for (Object object : params) {
            JSONObject param = (JSONObject) object;
            if ("sEcho".equals(param.getString("name"))) {
                sEcho = param.getIntValue("value");
            } else if ("iDisplayStart".equals(param.getString("name"))) {
                iDisplayStart = param.getIntValue("value");
            } else if ("iDisplayLength".equals(param.getString("name"))) {
                iDisplayLength = param.getIntValue("value");
            } else if ("sSearch".equals(param.getString("name"))) {
                search = param.getString("value");
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("start", iDisplayStart);
        map.put("length", iDisplayLength);

        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
        KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);

        JSONArray aaDatas = new JSONArray();
        TopicRecordPageInfo topicRecordPageInfo = ksf.getTopicMetaPageOfRecord(kafkaClientInfo, topic, map);

        for (TopicRecordInfo topicRecordInfo : topicRecordPageInfo.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("partitionId", topicRecordInfo.getPartitionId());
            target.put("logsize", topicRecordInfo.getLogSize());
            target.put("leader", topicRecordInfo.getLeader());
            target.put("replicas", topicRecordInfo.getReplicas());
            target.put("isr", topicRecordInfo.getIsr());
            target.put("preferredLeader", HtmlAttributeUtil.getPreferredLeader(topicRecordInfo.getPreferredLeader()));
            target.put("underReplicated", HtmlAttributeUtil.getUnderReplicated(topicRecordInfo.getUnderReplicated()));
            target.put("preview", "<a href='' cid='" + cid + "' topic='" + topic + "' partition='" + topicRecordInfo.getPartitionId() + "' name='efak_topic_partition_preview' class='badge border border-primary text-primary'>预览</a>");
            aaDatas.add(target);
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", topicRecordPageInfo.getTotal());
        target.put("iTotalDisplayRecords", topicRecordPageInfo.getTotal());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get topic metadata table info has error, msg is {}", ex);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/record/size/ajax", method = RequestMethod.GET)
    public String getTopicRecordSize(@RequestParam("topic") String topic, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        JSONObject target = new JSONObject();
        if (brokerInfos == null || brokerInfos.size() == 0) {
            target.put("status", false);
            target.put("msg", ResponseModuleType.GET_TOPIC_RECORD_NOBROKERS_ERROR.getName());
        } else {
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            TopicJmxInfo topicJmxInfo = ksf.getTopicRecordCapacity(kafkaClientInfo, brokerInfos, topic);
            Long logsize = ksf.getTopicOfLogSize(kafkaClientInfo, topic);
            target.put("logsize", logsize);
            target.put("capacity", topicJmxInfo.getCapacity());
            target.put("unit", topicJmxInfo.getUnit());
        }
        return target.toString();
    }


    /**
     * get topic name mock list.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/name/mock/ajax", method = RequestMethod.GET)
    public void pageTopicNameMockAjax(HttpServletResponse response, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic name mock list:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        HttpSession session = request.getSession();
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();

        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
        KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
        Set<String> topicNames = ksf.getTopicNames(kafkaClientInfo);
        int offset = 0;
        JSONArray topics = new JSONArray();
        for (String topicName : topicNames) {
            if (StrUtil.isNotBlank(name)) {
                JSONObject topic = new JSONObject();
                if (topicName.contains(name)) {
                    topic.put("text", topicName);
                    topic.put("id", offset);
                }
                topics.add(topic);
            } else {
                JSONObject topic = new JSONObject();
                topic.put("text", topicName);
                topic.put("id", offset);
                topics.add(topic);
            }

            offset++;
        }

        object.put("items", topics);
        try {
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get topic name mock list has error, msg is {}", ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/name/mock/send", method = RequestMethod.POST)
    public String sendTopicRecord(@RequestBody TopicMockInfo topicMockInfo, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
        JSONObject target = new JSONObject();
        if (brokerInfos == null || brokerInfos.size() == 0) {
            target.put("status", false);
            target.put("msg", ResponseModuleType.ADD_TOPIC_NOBROKERS_ERROR.getName());
        } else {
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            boolean result = ksf.sendMsg(kafkaClientInfo, topicMockInfo.getTopicName(), topicMockInfo.getMessage());
            if (result) {
                target.put("status", true);
            } else {
                target.put("status", false);
                target.put("msg", ResponseModuleType.ADD_TOPCI_RECORD_SERVICE_ERROR.getName());
            }
        }
        return target.toString();
    }

    @RequestMapping(value = "/name/msg/preview", method = RequestMethod.POST)
    public void getTopicRecord(@RequestBody TopicPreviewInfo topicPreviewInfo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        try {
            String remoteAddr = request.getRemoteAddr();
            String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
            log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
            Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
            ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
            List<BrokerInfo> brokerInfos = brokerDaoService.clusters(clusterInfo.getClusterId());
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
            KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
            String content = ksf.getMsg(kafkaClientInfo, topicPreviewInfo.getTopicName(), topicPreviewInfo.getPartitionId());
            byte[] output = content.getBytes();
            BaseController.response(output, response);
        } catch (Exception e) {
            log.error("Get topic[{}],partitionId[] msg has error: {}", topicPreviewInfo.getTopicName(), topicPreviewInfo.getPartitionId(), e);
        }
    }


    /**
     * Get producer chart data by ajax.
     */
    @RequestMapping(value = "/meta/msg/chart/ajax", method = RequestMethod.GET)
    public void getTopicMetaMsgChartAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String remoteAddr = request.getRemoteAddr();
            String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
            log.info("Topic meta chart :: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
            Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
            ClusterInfo clusterInfo = clusterDaoService.clusters(cid);

            Map<String, Object> param = new HashMap<>();
            param.put("cid", clusterInfo.getClusterId());
            param.put("topic", request.getParameter("topic"));
            param.put("stime", request.getParameter("stime"));
            param.put("etime", request.getParameter("etime"));

            // topic summary
            List<TopicSummaryInfo> topicSummaryInfos = topicSummaryDaoService.pages(param);

            JSONArray arrays = new JSONArray();
            for (TopicSummaryInfo topicSummaryInfo : topicSummaryInfos) {
                JSONObject object = new JSONObject();
                object.put("x", CalendarUtil.convertUnixTime(topicSummaryInfo.getTimespan(), "yyyy-MM-dd HH:mm"));
                object.put("y", topicSummaryInfo.getLogSizeDiffVal());
                arrays.add(object);
            }

            String target = arrays.toJSONString();
            if (StrUtil.isBlank(target)) {
                target = "";
            }
            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception e) {
            log.error("Get topic meta msg chart has error, msg is {}", e);
        }
    }

}
