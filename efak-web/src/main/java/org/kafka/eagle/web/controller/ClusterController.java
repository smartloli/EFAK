/**
 * ClusterController.java
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

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * The ClusterController is responsible for handling requests related to viewing and
 * managing kafka clusters.
 * @Author: smartloli
 * @Date: 2023/5/27 14:21
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/clusters")
@Slf4j
public class ClusterController {

    @Autowired
    private IClusterCreateDaoService clusterCreateDaoService;

    @GetMapping("/manage")
    public String clusterView(){
        return "cluster/manage.html";
    }

    @GetMapping("/manage/create")
    public String createClusterView(){
        return "cluster/manage-create.html";
    }

    @ResponseBody
    @PostMapping("/manage/add/batch")
    public boolean addClusterCreateInfo(){
        List<ClusterCreateInfo> list = new ArrayList<>();
        ClusterCreateInfo clusterCreateInfo = new ClusterCreateInfo();
        clusterCreateInfo.setClusterId("asdfqwer");
        clusterCreateInfo.setBrokerId("1000");
        clusterCreateInfo.setBrokerHost("127.0.0.1");
        clusterCreateInfo.setBrokerPort(9092);
        clusterCreateInfo.setBrokerJmxPort(9999);
        list.add(clusterCreateInfo);
        return this.clusterCreateDaoService.batch(list);
    }

}
