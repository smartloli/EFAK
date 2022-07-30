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
package org.smartloli.kafka.eagle.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka consumer controller to viewer data.
 *
 * @author smartloli.
 * <p>
 * Created by Jun 25, 2022.
 */
@Controller
public class ConsumersController {

    /**
     * Kafka consumer service interface.
     */
    @Autowired
    private ConsumerService consumerService;

    @RequestMapping(value = "/consumers/groups", method = RequestMethod.GET)
    public ModelAndView consumersGroupsView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/consumers/groups");
        return mav;
    }

    /**
     * Get consumer data by ajax.
     */
    @RequestMapping(value = "/consumers/info/ajax", method = RequestMethod.GET)
    public void consumersGraphAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            String formatter = SystemConfigUtils.getProperty(clusterAlias + ".efak.offset.storage");
            String result = consumerService.getKafkaConsumerGraph(clusterAlias);// offline
            if (StrUtils.isListNull(JSON.parseObject(result).getJSONObject("active").getString("children"))) {
                result = consumerService.getActiveTopic(clusterAlias, formatter);// online
            }
            byte[] output = result.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get consumer group status data by ajax.
     */
    @RequestMapping(value = "/consumers/group/status/info/ajax", method = RequestMethod.GET)
    public void consumersGroupStatusAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            String result = consumerService.getConsumerGroupsCnt(params);
            byte[] output = result.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic consumers data by ajax.
     */
    @RequestMapping(value = "/consumer/topics/info/ajax", method = RequestMethod.GET)
    public void topicsGraphAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            String formatter = SystemConfigUtils.getProperty(clusterAlias + ".efak.offset.storage");
            String result = consumerService.getKafkaTopicGraph(clusterAlias);
            if (StrUtils.isListNull(JSON.parseObject(result).getJSONObject("active").getString("children"))) {
                // TODO
            }
            byte[] output = result.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get consumer datasets by ajax.
     */
    @RequestMapping(value = "/consumer/list/table/ajax", method = RequestMethod.GET)
    public void consumerTableAjax(HttpServletResponse response, HttpServletRequest request) {
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

        DisplayInfo page = new DisplayInfo();
        page.setSearch(search);
        page.setiDisplayLength(iDisplayLength);
        page.setiDisplayStart(iDisplayStart);

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        String formatter = SystemConfigUtils.getProperty(clusterAlias + ".efak.offset.storage");

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("cluster", clusterAlias);
        param.put("search", search);

        long count = consumerService.countConsumerSummaryPages(param);
        List<ConsumerSummaryInfo> consumerSummarys = consumerService.getConsumerSummaryPages(clusterAlias, page);
        JSONArray aaDatas = new JSONArray();
        if (count == 0L && (consumerSummarys == null || consumerSummarys.size() == 0)) {
            // online
            count = consumerService.getConsumerCount(clusterAlias, formatter);
            JSONArray consumers = JSON.parseArray(consumerService.getConsumer(clusterAlias, formatter, page));
            for (Object object : consumers) {
                JSONObject consumer = (JSONObject) object;
                JSONObject obj = new JSONObject();
                obj.put("id", consumer.getInteger("id"));
                String group = "";
                try {
                    group = URLEncoder.encode(consumer.getString("group"), "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                obj.put("group", "<a class='link' group='" + group + "' href='#'>" + consumer.getString("group") + "</a>");
                obj.put("topics", consumer.getString("topics"));
                obj.put("node", consumer.getString("node"));
                int activeTopics = consumer.getInteger("activeTopics");
                int activeThreads = consumer.getInteger("activeThreads");
                if (activeTopics > 0) {
                    obj.put("activeTopics", "<span class='badge badge-success'>" + consumer.getInteger("activeTopics") + "</span>");
                } else {
                    obj.put("activeTopics", "<span class='badge badge-danger'>" + consumer.getInteger("activeTopics") + "</span>");
                }
                if (activeThreads > 0) {
                    obj.put("activeThreads", "<span class='badge badge-success'>" + consumer.getInteger("activeThreads") + "</span>");
                } else {
                    obj.put("activeThreads", "<span class='badge badge-danger'>" + consumer.getInteger("activeThreads") + "</span>");
                }

                aaDatas.add(obj);
            }
        } else {
            // offline
            int id = iDisplayStart + 1;
            for (ConsumerSummaryInfo consumerSummary : consumerSummarys) {
                JSONObject obj = new JSONObject();
                obj.put("id", id);
                String group = "";
                try {
                    group = URLEncoder.encode(consumerSummary.getGroup(), "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                obj.put("group", "<a class='link' group='" + group + "' href='#'>" + group + "</a>");
                obj.put("topics", consumerSummary.getTopics());
                obj.put("node", consumerSummary.getCoordinator());
                int activeTopics = consumerSummary.getActiveTopic();
                int activeThreads = consumerSummary.getActiveThread();
                if (activeTopics > 0) {
                    obj.put("activeTopics", "<span class='badge badge-success'>" + activeTopics + "</span>");
                } else {
                    obj.put("activeTopics", "<span class='badge badge-danger'>" + activeTopics + "</span>");
                }
                if (activeThreads > 0) {
                    obj.put("activeThreads", "<span class='badge badge-success'>" + activeThreads + "</span>");
                } else {
                    obj.put("activeThreads", "<span class='badge badge-danger'>" + activeThreads + "</span>");
                }

                aaDatas.add(obj);
                id++;
            }
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", count);
        target.put("iTotalDisplayRecords", count);
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get consumer data through group by ajax.
     */
    @RequestMapping(value = "/consumer/group/table/ajax", method = RequestMethod.GET)
    public void consumerTableListAjax(HttpServletResponse response, HttpServletRequest request) {
        String aoData = request.getParameter("aoData");
        String group = "";
        String search = "";
        try {
            group = URLDecoder.decode(request.getParameter("group"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONArray params = JSON.parseArray(aoData);
        int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
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

        DisplayInfo page = new DisplayInfo();
        page.setSearch(search);
        page.setiDisplayLength(iDisplayLength);
        page.setiDisplayStart(iDisplayStart);

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        String formatter = SystemConfigUtils.getProperty(clusterAlias + ".efak.offset.storage");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("cluster", clusterAlias);
        param.put("group", group);
        param.put("search", search);
        long count = consumerService.countConsumerGroupPages(param);
        List<ConsumerGroupsInfo> consumerGroups = consumerService.getConsumerGroupPages(clusterAlias, group, page);
        JSONArray aaDatas = new JSONArray();
        if (count == 0L && (consumerGroups == null || consumerGroups.size() == 0)) {
            JSONArray consumerDetails = JSON.parseArray(consumerService.getConsumerDetail(clusterAlias, formatter, group, search));
            count = consumerDetails.size();
            int offset = 0;
            for (Object object : consumerDetails) {
                JSONObject consumerDetail = (JSONObject) object;
                if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                    JSONObject obj = new JSONObject();
                    String topic = consumerDetail.getString("topic");
                    obj.put("id", consumerDetail.getInteger("id"));
                    obj.put("topic", topic);

                    try {
                        group = URLEncoder.encode(group, "UTF-8");
                        topic = URLEncoder.encode(topic, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (consumerDetail.getInteger("isConsumering") == Topic.RUNNING) {
                        obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-success text-success'>Running</a>");
                    } else if (consumerDetail.getInteger("isConsumering") == Topic.SHUTDOWN) {
                        obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-danger text-danger'>Shutdown</a>");
                    } else {
                        obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-warning text-warning'>Pending</a>");
                    }
                    aaDatas.add(obj);
                }
                offset++;
            }
        } else {
            int id = iDisplayStart + 1;
            for (ConsumerGroupsInfo consumerGroup : consumerGroups) {
                JSONObject obj = new JSONObject();
                String topic = consumerGroup.getTopic();
                obj.put("id", id);
                obj.put("topic", topic);

                try {
                    group = URLEncoder.encode(group, "UTF-8");
                    topic = URLEncoder.encode(topic, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int isConsumering = consumerGroup.getStatus();
                if (isConsumering == Topic.RUNNING) {
                    obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-success text-success'>Running</a>");
                } else if (isConsumering == Topic.SHUTDOWN) {
                    obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-danger text-danger'>Shutdown</a>");
                } else {
                    obj.put("isConsumering", "<a href='/consumers/offset/?group=" + group + "&topic=" + topic + "' target='_blank' class='badge bg-light-warning text-warning'>Pending</a>");
                }
                aaDatas.add(obj);
                id++;
            }
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", count);
        target.put("iTotalDisplayRecords", count);
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
