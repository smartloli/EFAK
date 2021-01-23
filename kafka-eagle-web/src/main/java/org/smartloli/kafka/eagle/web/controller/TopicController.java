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
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.*;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.BrokerSever;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KConstants.Role;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Kafka topic controller to viewer data.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 6, 2016.
 * <p>
 * Update by hexiang 20170216
 */
@Controller
public class TopicController {

    /**
     * Kafka topic service interface.
     */
    @Autowired
    private TopicService topicService;

    /**
     * Kafka service interface.
     */
    private KafkaService kafkaService = new KafkaFactory().create();

    /**
     * BrokerService interface.
     */
    private BrokerService brokerService = new BrokerFactory().create();

    /**
     * Topic create viewer.
     */
    @RequiresPermissions("/topic/create")
    @RequestMapping(value = "/topic/create", method = RequestMethod.GET)
    public ModelAndView topicCreateView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/create");
        return mav;
    }

    /**
     * Topic message viewer.
     */
    @RequiresPermissions("/topic/message")
    @RequestMapping(value = "/topic/message", method = RequestMethod.GET)
    public ModelAndView topicMessageView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/ksql");
        return mav;
    }

    /**
     * Topic message manager.
     */
    @RequiresPermissions("/topic/manager")
    @RequestMapping(value = "/topic/manager", method = RequestMethod.GET)
    public ModelAndView topicManagerView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/manager");
        return mav;
    }

    /**
     * Topic mock viewer.
     */
    @RequiresPermissions("/topic/mock")
    @RequestMapping(value = "/topic/mock", method = RequestMethod.GET)
    public ModelAndView topicMockView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/mock");
        return mav;
    }

    /**
     * Topic mock viewer.
     */
    @RequiresPermissions("/topic/hub")
    @RequestMapping(value = "/topic/hub", method = RequestMethod.GET)
    public ModelAndView topicHubView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/hub");
        return mav;
    }

    /**
     * Topic list viewer.
     */
    @RequestMapping(value = "/topic/list", method = RequestMethod.GET)
    public ModelAndView topicListView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/list");
        return mav;
    }

    /**
     * Topic metadata viewer.
     */
    @RequestMapping(value = "/topic/meta/{tname}/", method = RequestMethod.GET)
    public ModelAndView topicMetaView(@PathVariable("tname") String tname, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        if (topicService.hasTopic(clusterAlias, tname)) {
            mav.setViewName("/topic/topic_meta");
        } else {
            mav.setViewName("/error/404");
        }

        return mav;
    }

    /**
     * Create topic success viewer.
     */
    @RequestMapping(value = "/topic/create/success", method = RequestMethod.GET)
    public ModelAndView successView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/add_success");
        return mav;
    }

    /**
     * Create topic failed viewer.
     */
    @RequestMapping(value = "/topic/create/failed", method = RequestMethod.GET)
    public ModelAndView failedView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/topic/add_failed");
        return mav;
    }

    /**
     * Get topic metadata by ajax.
     */
    @RequestMapping(value = "/topic/meta/{tname}/ajax", method = RequestMethod.GET)
    public void topicMetaAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request) {
        String aoData = request.getParameter("aoData");
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
            }
        }

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        Map<String, Object> map = new HashMap<>();
        map.put("start", iDisplayStart);
        map.put("length", iDisplayLength);
        long count = topicService.getPartitionNumbers(clusterAlias, tname);
        List<MetadataInfo> metadatas = topicService.metadata(clusterAlias, tname, map);
        JSONArray aaDatas = new JSONArray();
        for (MetadataInfo metadata : metadatas) {
            JSONObject object = new JSONObject();
            object.put("topic", tname);
            object.put("partition", metadata.getPartitionId());
            object.put("logsize", metadata.getLogSize());
            object.put("leader", metadata.getLeader());
            object.put("replicas", metadata.getReplicas());
            object.put("isr", metadata.getIsr());
            if (metadata.isPreferredLeader()) {
                object.put("preferred_leader", "<span class='badge badge-success'>true</span>");
            } else {
                object.put("preferred_leader", "<span class='badge badge-danger btn-xs'>false</span>");
            }
            if (metadata.isUnderReplicated()) {
                object.put("under_replicated", "<span class='badge badge-danger btn-xs'>true</span>");
            } else {
                object.put("under_replicated", "<span class='badge badge-success btn-xs'>false</span>");
            }
            aaDatas.add(object);
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
     * Get cluster data by ajax.
     */
    @RequestMapping(value = "/topic/meta/mbean/{tname}/ajax", method = RequestMethod.GET)
    public void topicMetaMetricsAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String target = topicService.getTopicMBean(clusterAlias, tname);

            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get cluster data by ajax.
     */
    @RequestMapping(value = "/topic/meta/jmx/{tname}/ajax", method = RequestMethod.GET)
    public void topicMsgByJmxAjax(@PathVariable("tname") String tname, HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String target = topicService.getTopicSizeAndCapacity(clusterAlias, tname);

            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get cluster producer and total capacity by ajax.
     */
    @RequestMapping(value = "/topic/list/total/jmx/ajax", method = RequestMethod.GET)
    public void producersCapacityByJmxAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String target = topicService.getProducersCapacity(clusterAlias);

            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/mock/list/ajax", method = RequestMethod.GET)
    public void topicMockAjax(HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String name = request.getParameter("name");
            JSONObject object = new JSONObject();
            object.put("items", JSON.parseArray(topicService.mockTopics(clusterAlias, name)));
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/manager/keys/ajax", method = RequestMethod.GET)
    public void getTopicProperties(HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String name = request.getParameter("name");
            JSONObject object = new JSONObject();
            object.put("items", JSON.parseArray(topicService.getTopicProperties(clusterAlias, name)));
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Balance topics by ajax.
     */
    @RequestMapping(value = "/topic/balance/generate/", method = RequestMethod.GET)
    public void topicGenerateAjax(HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String type = request.getParameter("type");
            List<String> topicList = new ArrayList<>();
            if (BrokerSever.BALANCE_SINGLE.equals(type)) {
                String topics = request.getParameter("topics");
                for (String topic : topics.split(",")) {
                    topicList.add(topic);
                }
            }
            byte[] output = topicService.getBalanceGenerate(clusterAlias, topicList, type).toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Execute topics by ajax.
     */
    @RequestMapping(value = "/topic/balance/execute/ajax", method = RequestMethod.POST)
    public void topicExecuteAjax(@RequestBody TopicBalanceJson topicBalanceJson, HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            byte[] output = topicService.setBalanceExecute(clusterAlias, topicBalanceJson.getJson()).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Verify topics by ajax.
     */
    @RequestMapping(value = "/topic/balance/verify/ajax", method = RequestMethod.GET)
    public void topicVerifyAjax(HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String type = request.getParameter("type");
            List<String> topicList = new ArrayList<>();
            if (BrokerSever.BALANCE_SINGLE.equals(type)) {
                String topics = request.getParameter("topics");
                for (String topic : topics.split(",")) {
                    topicList.add(topic);
                }
            }
            String result = "";
            if (topicService.getBalanceGenerate(clusterAlias, topicList, type).containsKey("current")) {
                String reassignTopicsJson = topicService.getBalanceGenerate(clusterAlias, topicList, type).getString("current");
                result = topicService.setBalanceVerify(clusterAlias, reassignTopicsJson);
            } else {
                result = topicService.getBalanceGenerate(clusterAlias, topicList, type).toJSONString();
            }
            byte[] output = result.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/manager/{type}/ajax", method = RequestMethod.GET)
    public void alterTopicConfigAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String topic = request.getParameter("topic");
            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setName(topic);
            topicConfig.setType(type.toUpperCase());
            if (KConstants.Topic.ADD.equals(topicConfig.getType())) {
                String key = request.getParameter("key");
                String value = request.getParameter("value");
                ConfigEntry configEntry = new ConfigEntry(key, value);
                topicConfig.setConfigEntry(configEntry);
            } else if (KConstants.Topic.DELETE.equals(topicConfig.getType())) {
                String key = request.getParameter("key");
                ConfigEntry configEntry = new ConfigEntry(key, "");
                topicConfig.setConfigEntry(configEntry);
            }
            JSONObject object = new JSONObject();
            object.put("result", topicService.changeTopicConfig(clusterAlias, topicConfig));
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Preferred replica leader election for a given topic
     */
    @RequestMapping(value = "/topic/manager/election/ajax", method = RequestMethod.GET)
    public void prefReplicaElection(HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String topic = request.getParameter("topic");

            JSONObject object = new JSONObject();
            object.put("result", topicService.prefReplicaElection(clusterAlias, topic));
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send mock data to topic.
     */
    @RequestMapping(value = "/topic/mock/send/message/topic/ajax", method = RequestMethod.POST)
    public void topicMockSend(@RequestBody TopicMockMessage topicMockMessage, HttpServletResponse response, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            JSONObject object = new JSONObject();
            object.put("status", topicService.mockSendMsg(clusterAlias, topicMockMessage.getTopic(), topicMockMessage.getMessage()));
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/list/table/ajax", method = RequestMethod.GET)
    public void topicListAjax(HttpServletResponse response, HttpServletRequest request) {
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

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Signiner signiner = (Signiner) session.getAttribute(KConstants.Login.SESSION_USER);

        Map<String, Object> map = new HashMap<>();
        map.put("search", search);
        map.put("start", iDisplayStart);
        map.put("length", iDisplayLength);
        long count = 0L;
        if (search != null && !"".equals(search)) {
            count = topicService.getTopicNumbers(clusterAlias, search);
        } else {
            count = topicService.getTopicNumbers(clusterAlias);
        }
        List<PartitionsInfo> topics = topicService.list(clusterAlias, map);
        JSONArray aaDatas = new JSONArray();
        for (PartitionsInfo partition : topics) {
            JSONObject object = new JSONObject();
            object.put("id", partition.getId());
            object.put("topic", "<a href='/topic/meta/" + partition.getTopic() + "/' target='_blank'>" + partition.getTopic() + "</a>");
            object.put("partitions", partition.getPartitionNumbers());
            try {
                long brokerSpread = partition.getBrokersSpread();
                if (brokerSpread < Topic.TOPIC_BROKER_SPREAD_ERROR) {
                    object.put("brokerSpread", "<span class='badge badge-danger'>" + brokerSpread + "%</span>");
                } else if (brokerSpread >= Topic.TOPIC_BROKER_SPREAD_ERROR && brokerSpread < Topic.TOPIC_BROKER_SPREAD_NORMAL) {
                    object.put("brokerSpread", "<span class='badge badge-warning'>" + brokerSpread + "%</span>");
                } else if (brokerSpread >= Topic.TOPIC_BROKER_SPREAD_NORMAL) {
                    object.put("brokerSpread", "<span class='badge badge-success'>" + brokerSpread + "%</span>");
                } else {
                    object.put("brokerSpread", "<span class='badge badge-primary'>" + brokerSpread + "%</span>");
                }

                long brokerSkewed = partition.getBrokersSkewed();
                if (brokerSkewed >= Topic.TOPIC_BROKER_SKEW_ERROR) {
                    object.put("brokerSkewed", "<span class='badge badge-danger'>" + brokerSkewed + "%</span>");
                } else if (brokerSkewed > Topic.TOPIC_BROKER_SKEW_NORMAL && brokerSkewed < Topic.TOPIC_BROKER_SKEW_ERROR) {
                    object.put("brokerSkewed", "<span class='badge badge-warning'>" + brokerSkewed + "%</span>");
                } else if (brokerSkewed <= Topic.TOPIC_BROKER_SKEW_NORMAL) {
                    object.put("brokerSkewed", "<span class='badge badge-success'>" + brokerSkewed + "%</span>");
                } else {
                    object.put("brokerSkewed", "<span class='badge badge-primary'>" + brokerSkewed + "%</span>");
                }

                long brokerLeaderSkewed = partition.getBrokersLeaderSkewed();
                if (brokerLeaderSkewed >= Topic.TOPIC_BROKER_LEADER_SKEW_ERROR) {
                    object.put("brokerLeaderSkewed", "<span class='badge badge-danger'>" + brokerLeaderSkewed + "%</span>");
                } else if (brokerLeaderSkewed > Topic.TOPIC_BROKER_LEADER_SKEW_NORMAL && brokerLeaderSkewed < Topic.TOPIC_BROKER_LEADER_SKEW_ERROR) {
                    object.put("brokerLeaderSkewed", "<span class='badge badge-warning'>" + brokerLeaderSkewed + "%</span>");
                } else if (brokerLeaderSkewed <= Topic.TOPIC_BROKER_LEADER_SKEW_NORMAL) {
                    object.put("brokerLeaderSkewed", "<span class='badge badge-success'>" + brokerLeaderSkewed + "%</span>");
                } else {
                    object.put("brokerLeaderSkewed", "<span class='badge badge-primary'>" + brokerLeaderSkewed + "%</span>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            object.put("created", partition.getCreated());
            object.put("modify", partition.getModify());
            Map<String, Object> topicStateParams = new HashMap<>();
            topicStateParams.put("cluster", clusterAlias);
            topicStateParams.put("topic", partition.getTopic());
            topicStateParams.put("tkey", Topic.TRUNCATE);
            List<TopicRank> topicStates = topicService.getCleanTopicState(topicStateParams);
            if (topicStates != null && topicStates.size() > 0) {
                if (topicStates.get(0).getTvalue() == 0) {
                    if (Role.ADMIN.equals(signiner.getUsername())) {
                        object.put("operate",
                                "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='topic_modify' href='#"
                                        + partition.getTopic() + "'><i class='fas fa-edit fa-sm fa-fw mr-1'></i>Alter</a><a class='dropdown-item' href='#" + partition.getTopic() + "' name='topic_remove'><i class='fas fa-minus-circle fa-sm fa-fw mr-1'></i>Drop</a><a class='dropdown-item' href='#" + partition.getTopic()
                                        + "' name='topic_clean'><i class='fas fa-trash-alt fa-sm fa-fw mr-1'></i>Truncating</a></div>");
                    } else {
                        object.put("operate", "");
                    }
                } else {
                    if (Role.ADMIN.equals(signiner.getUsername())) {
                        object.put("operate",
                                "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='topic_modify' href='#"
                                        + partition.getTopic() + "'><i class='fas fa-edit fa-sm fa-fw mr-1'></i>Alter</a><a class='dropdown-item' href='#" + partition.getTopic() + "' name='topic_remove'><i class='fas fa-minus-circle fa-sm fa-fw mr-1'></i>Drop</a><a class='dropdown-item' href='#" + partition.getTopic()
                                        + "' name='topic_clean'><i class='fas fa-trash-alt fa-sm fa-fw mr-1'></i>Truncating</a></div>");
                    } else {
                        object.put("operate", "");
                    }
                }
            } else {
                if (Role.ADMIN.equals(signiner.getUsername())) {
                    object.put("operate",
                            "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='topic_modify' href='#"
                                    + partition.getTopic() + "'><i class='fas fa-edit fa-sm fa-fw mr-1'></i>Alter</a><a class='dropdown-item' href='#" + partition.getTopic() + "' name='topic_remove'><i class='fas fa-minus-circle fa-sm fa-fw mr-1'></i>Drop</a><a class='dropdown-item' href='#" + partition.getTopic()
                                    + "' name='topic_clean'><i class='fas fa-trash-alt fa-sm fa-fw mr-1'></i>Truncating</a></div>");
                } else {
                    object.put("operate", "");
                }
            }

            aaDatas.add(object);
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
     * Get select topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/list/select/ajax", method = RequestMethod.POST)
    public void topicSelectListAjax(HttpServletResponse response, HttpServletRequest request) {
        try {
            String topic = request.getParameter("topic");
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            byte[] output = topicService.getSelectTopics(clusterAlias, topic).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get select filter topic datasets by ajax.
     */
    @RequestMapping(value = "/topic/list/filter/select/ajax", method = RequestMethod.GET)
    public void topicSelectFilterListAjax(HttpServletResponse response, HttpServletRequest request) {
        try {
            String topics = request.getParameter("topics");
            String stime = request.getParameter("stime");
            String etime = request.getParameter("etime");
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

            Map<String, Object> params = new HashMap<>();
            params.put("cluster", clusterAlias);
            params.put("stime", stime);
            params.put("etime", etime);
            if (!Strings.isNullOrEmpty(topics)) {
                String[] topicStrs = topics.split(",");
                Set<String> topicSets = new HashSet<>();
                for (String topic : topicStrs) {
                    topicSets.add(topic);
                }
                params.put("topics", topicSets);
            }

            byte[] output = topicService.getSelectTopicsLogSize(clusterAlias, params).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Clean topic data by ajax.
     */
    @RequestMapping(value = "/topic/clean/data/{topic}/", method = RequestMethod.GET)
    public ModelAndView cleanTopicDataAjax(@PathVariable("topic") String topic, HttpServletResponse response, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        try {
            HttpSession session = request.getSession();
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

            TopicRank tr = new TopicRank();
            tr.setCluster(clusterAlias);
            tr.setTopic(topic);
            tr.setTkey(Topic.TRUNCATE);
            tr.setTvalue(0);
            if (topicService.addCleanTopicData(Arrays.asList(tr)) > 0) {
                mav.setViewName("redirect:/topic/list");
            } else {
                mav.setViewName("redirect:/errors/500");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mav.setViewName("redirect:/errors/500");
        }
        return mav;
    }

    /**
     * Create topic form.
     */
    @RequestMapping(value = "/topic/create/form", method = RequestMethod.POST)
    public ModelAndView topicAddForm(HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        String ke_topic_name = request.getParameter("ke_topic_name");
        String ke_topic_partition = request.getParameter("ke_topic_partition");
        String ke_topic_repli = request.getParameter("ke_topic_repli");
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> respons = kafkaService.create(clusterAlias, ke_topic_name, ke_topic_partition, ke_topic_repli);
        if ("success".equals(respons.get("status"))) {
            session.removeAttribute("Submit_Status");
            session.setAttribute("Submit_Status", respons.get("info"));
            mav.setViewName("redirect:/topic/create/success");
        } else {
            session.removeAttribute("Submit_Status");
            session.setAttribute("Submit_Status", respons.get("info"));
            mav.setViewName("redirect:/topic/create/failed");
        }
        return mav;
    }

    /**
     * Delete topic.
     */
    @RequestMapping(value = "/topic/{topicName}/{token}/delete", method = RequestMethod.GET)
    public ModelAndView topicDelete(@PathVariable("topicName") String topicName, @PathVariable("token") String token, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        if (SystemConfigUtils.getProperty("kafka.eagle.topic.token").equals(token) && !Kafka.CONSUMER_OFFSET_TOPIC.equals(topicName)) {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            Map<String, Object> respons = kafkaService.delete(clusterAlias, topicName);
            if ("success".equals(respons.get("status"))) {
                mav.setViewName("redirect:/topic/list");
            } else {
                mav.setViewName("redirect:/errors/500");
            }
        } else {
            mav.setViewName("redirect:/errors/403");
        }
        return mav;
    }

    /**
     * Modify topic partitions.
     */
    @RequestMapping(value = "/topic/{topicName}/{partitions}/modify", method = RequestMethod.GET)
    public ModelAndView topicModifyPartitions(@PathVariable("topicName") String topicName, @PathVariable("partitions") int token, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> respons = brokerService.createTopicPartitions(clusterAlias, topicName, token);
        if ("success".equals(respons.get("status"))) {
            mav.setViewName("redirect:/topic/list");
        } else {
            mav.setViewName("redirect:/errors/500");
        }
        return mav;
    }

    /**
     * Realtime access to shard sub scan task execute log.
     */
    @RequestMapping(value = "/shard/sub/scan/log/", method = RequestMethod.GET)
    public void getShardSubScanLogAjax(@RequestParam String jobId, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        try {
            String logs = topicService.getShardLogs(jobId);
            JSONObject object = new JSONObject();
            object.put("logs", logs);
            byte[] output = object.toString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Logical execute kafka sql.
     */
    @RequestMapping(value = "/topic/logical/commit/", method = RequestMethod.GET)
    public void topicSqlLogicalAjax(@RequestParam String sql, @RequestParam String jobId, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
            String target = topicService.execute(clusterAlias, sql, jobId, KConstants.Protocol.KSQL_LOGICAL);
            JSONObject result = JSON.parseObject(target);
            try {
                TopicSqlHistory topicSql = new TopicSqlHistory();
                if (result.getBoolean("error")) {
                    topicSql.setStatus("FAILED");
                    topicSql.setSpendTime(0);
                    topicSql.setCluster(clusterAlias);
                    topicSql.setCreated(CalendarUtils.getDate());
                    topicSql.setHost(request.getRemoteHost());
                    topicSql.setKsql(sql);
                    topicSql.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
                    Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
                    topicSql.setUsername(signin.getUsername());
                    topicService.writeTopicSqlHistory(Arrays.asList(topicSql));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] output = result.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic page message from kafka.
     */
    @RequestMapping(value = "/topic/physics/commit/", method = RequestMethod.GET)
    public void topicSqlPhysicsAjax(@RequestParam String sql, @RequestParam String jobId, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        String aoData = request.getParameter("aoData");
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
            }
        }

        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        String text = topicService.execute(clusterAlias, sql, jobId, KConstants.Protocol.KSQL_PHYSICS);
        JSONObject result = JSON.parseObject(text);

        try {
            TopicSqlHistory topicSql = new TopicSqlHistory();
            topicSql.setCluster(clusterAlias);
            topicSql.setCreated(CalendarUtils.getDate());
            topicSql.setHost(request.getRemoteHost());
            topicSql.setKsql(sql);
            if (result.getBoolean("error")) {
                topicSql.setStatus("FAILED");
                topicSql.setSpendTime(0);
            } else {
                topicSql.setStatus("SUCCESSED");
                topicSql.setSpendTime(result.getLongValue("spent"));
            }
            topicSql.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
            Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
            topicSql.setUsername(signin.getUsername());
            topicService.writeTopicSqlHistory(Arrays.asList(topicSql));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray topics = JSON.parseArray(result.getString("msg"));
        JSONArray aaDatas = new JSONArray();
        int offset = 0;
        if (topics != null) {
            for (Object object : topics) {
                JSONObject topic = (JSONObject) object;
                if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                    JSONObject obj = new JSONObject();
                    for (String key : topic.keySet()) {
                        obj.put(key, topic.get(key));
                    }
                    aaDatas.add(obj);
                }
                offset++;
            }
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", topics.size());
        target.put("iTotalDisplayRecords", topics.size());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get topic sql history.
     */
    @RequestMapping(value = "/topic/sql/history/ajax", method = RequestMethod.GET)
    public void topicSqlHistoryAjax(HttpServletResponse response, HttpServletRequest request) {
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

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        Signiner signin = (Signiner) SecurityUtils.getSubject().getSession().getAttribute(KConstants.Login.SESSION_USER);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cluster", clusterAlias);
        map.put("search", search);
        map.put("username", signin.getUsername());
        map.put("start", iDisplayStart);
        map.put("size", iDisplayLength);
        long count = 0L;
        List<TopicSqlHistory> topicSqls = null;
        if (signin.getUsername().equals(KConstants.Role.ADMIN)) {
            topicSqls = topicService.readTopicSqlHistoryByAdmin(map);
            count = topicService.countTopicSqlHistoryByAdmin(map);
        } else {
            topicSqls = topicService.readTopicSqlHistory(map);
            count = topicService.countTopicSqlHistory(map);
        }

        JSONArray aaDatas = new JSONArray();
        if (topicSqls != null) {
            for (TopicSqlHistory topicSql : topicSqls) {
                JSONObject obj = new JSONObject();
                int id = topicSql.getId();
                String host = topicSql.getHost();
                String ksql = topicSql.getKsql();
                obj.put("id", id);
                obj.put("username", topicSql.getUsername());
                obj.put("host", "<a href='#" + id + "/host' name='ke_sql_query_detail'>" + (host.length() > 20 ? host.substring(0, 20) + "..." : host) + "</a>");
                obj.put("ksql", "<a href='#" + id + "/ksql' name='ke_sql_query_detail'>" + (ksql.length() > 60 ? ksql.substring(0, 60) + "..." : ksql) + "</a>");
                if (topicSql.getStatus().equals("SUCCESSED")) {
                    obj.put("status", "<span class='badge badge-success'>" + topicSql.getStatus() + "</span>");
                } else {
                    obj.put("status", "<span class='badge badge-danger'>" + topicSql.getStatus() + "</span>");
                }
                obj.put("spendTime", topicSql.getSpendTime() / 1000.0 + "s");
                obj.put("created", topicSql.getCreated());
                aaDatas.add(obj);
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
     * Get ksql host or sql detail.
     */
    @RequestMapping(value = "/topic/ksql/detail/{type}/{id}/ajax", method = RequestMethod.GET)
    public void getKSqlDetailByIdAjax(@PathVariable("id") int id, @PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
        try {
            JSONObject object = new JSONObject();
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            if ("host".equals(type)) {
                object.put("result", topicService.findTopicSqlByID(params).getHost());
            } else if ("ksql".equals(type)) {
                object.put("result", topicService.findTopicSqlByID(params).getKsql());
            }
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get producer chart data by ajax.
     */
    @RequestMapping(value = "/topic/producer/chart/ajax", method = RequestMethod.GET)
    public void topicProducerChartAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

            Map<String, Object> param = new HashMap<>();
            param.put("cluster", clusterAlias);
            param.put("stime", request.getParameter("stime"));
            param.put("etime", request.getParameter("etime"));
            param.put("topic", request.getParameter("topic"));
            String target = topicService.queryTopicProducerChart(param);
            if (StringUtils.isEmpty(target)) {
                target = "";
            }
            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
