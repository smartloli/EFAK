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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.plugins.excel.ExcelUtil;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ClusterController is responsible for handling requests related to viewing and
 * managing kafka clusters.
 *
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
    public String clusterView() {
        return "cluster/manage.html";
    }

    @GetMapping("/manage/create")
    public String createClusterView() {
        return "cluster/manage-create.html";
    }

    @PostMapping("/manage/add/batch")
    public String addClusterCreateBatch(@RequestParam("file") MultipartFile file,@RequestParam("cid") String cid) {
        InputStream inputStream;
        List<ClusterCreateInfo> list = new ArrayList<>();
        try {
            inputStream = file.getInputStream();
            ExcelUtil.readBrokerInfo(inputStream);
        } catch (Exception e) {
            log.error("Batch add broker has error, msg is {}", e);
        }
        boolean status = this.clusterCreateDaoService.batch(list);
        if (status) {
            return "redirect:/clusters/manage/create?cid=" + cid;
        } else {
            return "redirect:/error/500";
        }
    }

    // @ResponseBody
    @PostMapping("/manage/add/single")
    public String addClusterCreateSingle(HttpServletRequest request) {
        String clusterId = request.getParameter("efak_clusterid_name");
        String brokerId = request.getParameter("efak_brokerid_name");
        String brokerHost = request.getParameter("efak_brokerhost_name");
        String brokerPort = request.getParameter("efak_brokerport_name");
        String brokerJmxPort = request.getParameter("efak_brokerjmxport_name");
        List<ClusterCreateInfo> list = new ArrayList<>();
        ClusterCreateInfo clusterCreateInfo = new ClusterCreateInfo();
        clusterCreateInfo.setClusterId(clusterId);
        clusterCreateInfo.setBrokerId(brokerId);
        clusterCreateInfo.setBrokerHost(brokerHost);
        int brokerPortInt = Integer.parseInt(brokerPort);
        clusterCreateInfo.setBrokerPort(brokerPortInt);
        int brokerJmxPortInt = Integer.parseInt(brokerJmxPort);
        clusterCreateInfo.setBrokerJmxPort(brokerJmxPortInt);
        list.add(clusterCreateInfo);
        boolean status = this.clusterCreateDaoService.batch(list);
        if (status) {
            return "redirect:/clusters/manage/create?cid=" + clusterId;
        } else {
            return "redirect:/error/500";
        }
    }

    /**
     * Page broker list info.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/manage/brokers/table/ajax", method = RequestMethod.GET)
    public void pageBrokersAjax(HttpServletResponse response, HttpServletRequest request) {
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


        Page<ClusterCreateInfo> pages = this.clusterCreateDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (ClusterCreateInfo clusterCreateInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("brokerId", clusterCreateInfo.getBrokerId());
            target.put("brokerHost", clusterCreateInfo.getBrokerHost());
            target.put("brokerPort", clusterCreateInfo.getBrokerPort());
            target.put("brokerJmxPort", clusterCreateInfo.getBrokerJmxPort());
            target.put("modify", clusterCreateInfo.getBrokerJmxPort());
            target.put("operate", "<a href=\"#\" class=\"badge border border-primary text-primary\">编辑</a> <a href=\"#\" class=\"badge border border-danger text-danger\">删除</a>");
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
            ex.printStackTrace();
        }
    }

}
