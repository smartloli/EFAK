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

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.ResponseModuleType;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.core.kafka.KafkaClusterFetcher;
import org.kafka.eagle.core.kafka.KafkaSchemaFactory;
import org.kafka.eagle.core.kafka.KafkaStoragePlugin;
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.cluster.KafkaClientInfo;
import org.kafka.eagle.pojo.topic.NewTopicInfo;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

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

    /**
     * Handles the root URL request and displays the cluster management interface.
     *
     * @return The name of the cluster management view template.
     */
    @GetMapping("/create")
    public String createView() {
        return "topic/create.html";
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
    public String createClusterById(NewTopicInfo newTopicInfo, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
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
                KafkaClientInfo kafkaClientInfo = new KafkaClientInfo();
                kafkaClientInfo.setBrokerServer(KafkaClusterFetcher.parseBrokerServer(brokerInfos));
                boolean status = ksf.createTableName(kafkaClientInfo, newTopicInfo);
                if(status){
                    target.put("status", status);
                }else{
                    target.put("status", status);
                    target.put("msg", ResponseModuleType.CREATE_TOPIC_SERVICE_ERROR.getName());
                }
            }
        }

        return target.toString();
    }
}
