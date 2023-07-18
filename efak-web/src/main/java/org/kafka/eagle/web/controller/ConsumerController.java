/**
 * ConsumerController.java
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
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.kafka.eagle.web.service.IConsumerGroupDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The controller class that handles consumer-related operations.
 * This class is responsible for handling requests and providing responses related to consumers.
 *
 * @Author: smartloli
 * @Date: 2023/7/12 21:39
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    @Autowired
    private IClusterDaoService clusterDaoService;

    @Autowired
    private IConsumerGroupDaoService consumerGroupDaoService;

    @GetMapping("/summary")
    public String consumerSummaryView() {
        return "consumer/summary.html";
    }

    @ResponseBody
    @RequestMapping(value = "/summary/groups/ajax", method = RequestMethod.GET)
    public String getConsumerGroupsSummary(HttpSession session, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Topic partition add:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        JSONObject target = new JSONObject();
        ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
        consumerGroupInfo.setClusterId(clusterInfo.getClusterId());
        consumerGroupInfo.setStatus(KConstants.Topic.ALL);
        Integer totalGroupSize = this.consumerGroupDaoService.totalOfConsumerGroups(consumerGroupInfo);
        consumerGroupInfo.setStatus(KConstants.Topic.RUNNING);
        Integer ActiveGroupSize = this.consumerGroupDaoService.totalOfConsumerGroups(consumerGroupInfo);
        target.put("total_group_size", totalGroupSize);
        target.put("active_group_size", ActiveGroupSize);
        return target.toString();
    }

}
