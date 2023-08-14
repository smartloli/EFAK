/**
 * DashboardController.java
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
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.CalendarUtil;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaSchemaInitialize;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.cluster.KafkaMBeanInfo;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;
import org.kafka.eagle.pojo.topic.TopicSummaryInfo;
import org.kafka.eagle.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DashboardController handles requests for dashboard pages. This controller handles the following requests:
 * - /dashboard: returns the main dashboard page.
 * - /dashboard/{id}: returns a specific dashboard page with the given ID.
 * - /dashboard/create: returns a page for creating a new dashboard.
 * - /dashboard/save: handles POST requests to save a dashboard.
 * <p>
 * This controller uses the DashboardService to manage the creation, editing, and saving of dashboards. When
 * handling requests, this controller also uses the Thymeleaf template engine to render pages and pass necessary data
 * to the pages.
 *
 * @Author: smartloli
 * @Date: 2023/5/13 23:38
 * @Version: 3.4.0
 */
@Slf4j
@Controller
@RequestMapping("/dataspace")
public class DataSpaceController {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IBrokerDaoService brokerDaoService;

    @Autowired
    private ITopicSummaryDaoService topicSummaryDaoService;

    @Autowired
    private IConsumerGroupDaoService consumerGroupDaoService;

    @Autowired
    private IKafkaMBeanDaoService kafkaMBeanDaoService;


    @GetMapping("/dashboard/{cid}")
    public String dashboardView(@PathVariable("cid") Long cid, HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        session.removeAttribute(clusterAlias);
        session.setAttribute(clusterAlias, cid);
        return "dataspace/dashboard.html";
    }

    @RequestMapping(value = "/dashboard/{cid}/panel/ajax", method = RequestMethod.GET)
    public void getDashboardPanelAjax(HttpServletResponse response, @PathVariable("cid") Long cid) {
        ClusterInfo clusterInfo = this.clusterDaoService.clusters(cid);

        // broker size
        List<BrokerInfo> brokerInfos = this.brokerDaoService.clusters(clusterInfo.getClusterId());
        List<BrokerInfo> onlineBrokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));

        // topic size
        Integer topicOfActiveNums = topicSummaryDaoService.topicOfActiveNums(clusterInfo.getClusterId(), CalendarUtil.getCustomLastDay(2), CalendarUtil.getCustomLastDay(0));
        KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());
        KafkaClientInfo kafkaClientInfo = KafkaSchemaInitialize.init(brokerInfos, clusterInfo);
        Integer topicOfTotal = ksf.getTopicNames(kafkaClientInfo).size();

        // consumer size
        ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
        consumerGroupInfo.setClusterId(clusterInfo.getClusterId());
        consumerGroupInfo.setStatus(KConstants.Topic.ALL);
        Long totalGroupSize = this.consumerGroupDaoService.totalOfConsumerGroups(consumerGroupInfo);
        consumerGroupInfo.setStatus(KConstants.Topic.RUNNING);
        Long ActiveGroupSize = this.consumerGroupDaoService.totalOfConsumerGroups(consumerGroupInfo);


        JSONObject target = new JSONObject();
        target.put("brokers", brokerInfos.size());
        target.put("onlines", onlineBrokerInfos.size());
        target.put("topic_total_nums", topicOfTotal);
        target.put("topic_free_nums", topicOfTotal - topicOfActiveNums);
        target.put("group_total_nums", totalGroupSize);
        target.put("group_active_nums", ActiveGroupSize);


        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get dashboard panel ajax has error,msg is {}", ex);
        }
    }

    /**
     * Get dashboard messagein chart data.
     *
     * @param cid
     * @param response
     * @param request
     * @param session
     */
    @RequestMapping(value = "/dashboard/{cid}/messagein/chart/ajax", method = RequestMethod.GET)
    public void getMessageInChartAjax(@PathVariable("cid") Long cid, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String remoteAddr = request.getRemoteAddr();
            String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
            log.info("Dashboard messagein chart :: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
            ClusterInfo clusterInfo = clusterDaoService.clusters(cid);

            Map<String, Object> param = new HashMap<>();
            param.put("cid", clusterInfo.getClusterId());
            param.put("stime", CalendarUtil.getCustomLastDay(0));
            param.put("etime", CalendarUtil.getCustomLastDay(0));
            param.put("modules", Arrays.asList("message_in"));

            // get message in chart data
            List<KafkaMBeanInfo> kafkaMBeanInfos = kafkaMBeanDaoService.pages(param);
            JSONArray messageIns = new JSONArray();
            for (KafkaMBeanInfo kafkaMBeanInfo : kafkaMBeanInfos) {
                assembly(messageIns, kafkaMBeanInfo);
            }
            JSONObject target = new JSONObject();
            target.put("messageIns", messageIns);

            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception e) {
            log.error("Get kafka mbean chart has error, msg is {}", e);
        }
    }

    private void assembly(JSONArray assemblys, KafkaMBeanInfo kafkaMBeanInfo) {
        JSONObject object = new JSONObject();
        object.put("x", CalendarUtil.convertUnixTime(kafkaMBeanInfo.getTimespan(), "yyyy-MM-dd HH:mm"));
        object.put("y", kafkaMBeanInfo.getMbeanValue());
        assemblys.add(object);
    }

    /**
     * Get dashboard producer chart data.
     *
     * @param response
     * @param request
     * @param session
     */
    @RequestMapping(value = "/dashboard/{cid}/producer/chart/ajax", method = RequestMethod.GET)
    public void getProduceMsgChartAjax(@PathVariable("cid") Long cid, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String remoteAddr = request.getRemoteAddr();
            String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
            log.info("Get dashboard os used chart :: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
            ClusterInfo clusterInfo = clusterDaoService.clusters(cid);

            Map<String, Object> param = new HashMap<>();
            param.put("cid", clusterInfo.getClusterId());
            param.put("topic", "");// all topics
            param.put("stime", CalendarUtil.getCustomLastDay(6));
            param.put("etime", CalendarUtil.getCustomLastDay(0));

            // topic summary
            List<TopicSummaryInfo> topicSummaryInfos = topicSummaryDaoService.pagesOfDay(param);

            Map<String, Long> logSizeOfDayMaps = new HashMap<>();
            for (TopicSummaryInfo topicSummaryInfo : topicSummaryInfos) {
                logSizeOfDayMaps.put(topicSummaryInfo.getDay(), topicSummaryInfo.getLogSizeDiffVal());
            }

            int index = 0;
            try {
                index = CalendarUtil.getDiffDay(param.get("stime").toString(), param.get("etime").toString());
            } catch (Exception e) {
                log.error("Get dashboard producer msg chart diff day has error, msg is {}", e);
            }

            JSONArray arrays = new JSONArray();

            for (int i = index; i >= 0; i--) {
                String day = CalendarUtil.getCustomLastDay(i);
                if (logSizeOfDayMaps.containsKey(day)) {
                    JSONObject object = new JSONObject();
                    object.put("x", CalendarUtil.getCustomLastDay("yyyy-MM-dd", i));
                    object.put("y", logSizeOfDayMaps.get(day).toString());
                    arrays.add(object);
                } else {
                    JSONObject object = new JSONObject();
                    object.put("x", CalendarUtil.getCustomLastDay("yyyy-MM-dd", i));
                    object.put("y", 0);
                    arrays.add(object);
                }
            }

            String target = arrays.toJSONString();
            if (StrUtil.isBlank(target)) {
                target = "";
            }
            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception e) {
            log.error("Get dashboard producer total msg chart has error, msg is {}", e);
        }
    }

    /**
     * Get kafka used rate chart.
     *
     * @param cid
     * @param response
     * @param request
     * @param session
     */
    @RequestMapping(value = "/dashboard/{cid}/os/chart/ajax", method = RequestMethod.GET)
    public void getOSUsedChartAjax(@PathVariable("cid") Long cid, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String remoteAddr = request.getRemoteAddr();
            String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
            log.info("Get dashboard os used chart :: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
            ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
            List<BrokerInfo> onlineBrokerInfos = this.brokerDaoService.brokerStatus(clusterInfo.getClusterId(), Short.valueOf("1"));
            Integer brokerSize = 1;
            if (onlineBrokerInfos != null && onlineBrokerInfos.size() > 0) {
                brokerSize = onlineBrokerInfos.size();
            }

            Map<String, Object> params = new HashMap<>();
            params.put("cid", clusterInfo.getClusterId());
            params.put("modules", Arrays.asList("os_used_memory"));
            params.put("limit", 2);

            // get memory used chart data
            List<KafkaMBeanInfo> mems = kafkaMBeanDaoService.pagesOfLastest(params);
            JSONObject object = new JSONObject();
            if (mems != null && mems.size() == 2) {
                Double valueFirst = StrUtils.numberic(mems.get(0).getMbeanValue(),"###.####");
                Double valueSecond = StrUtils.numberic(mems.get(1).getMbeanValue(),"###.####");
                if (valueFirst >= valueSecond) {
                    object.put("mem", StrUtils.numberic(((valueFirst - valueSecond) * 100.0 / valueFirst) + "") / brokerSize);
                } else {
                    object.put("mem", StrUtils.numberic(((valueSecond - valueFirst) * 100.0 / valueSecond) + "") / brokerSize);
                }
            } else {
                object.put("mem", "0.0");
            }

            // get cpu used chart data
            params.put("modules", Arrays.asList("cpu_used"));
            params.put("limit", 1);
            List<KafkaMBeanInfo> cpus = kafkaMBeanDaoService.pagesOfLastest(params);
            if (cpus != null && params.size() > 0) {
                object.put("cpu", StrUtils.numberic(cpus.get(0).getMbeanValue()) / brokerSize);
            } else {
                object.put("cpu", "0.0");
            }

            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception e) {
            log.error("Get dashboard os used chart has error, msg is {}", e);
        }
    }

}
