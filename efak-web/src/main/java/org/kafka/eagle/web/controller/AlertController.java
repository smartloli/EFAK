/**
 * AlertController.java
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
import org.kafka.eagle.common.utils.HtmlAttributeUtil;
import org.kafka.eagle.common.utils.Md5Util;
import org.kafka.eagle.pojo.alert.AlertChannelInfo;
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.web.service.IAlertChannelDaoService;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The `AlertController` class handles incoming requests related to alerts.
 * This controller provides endpoints to trigger alerts through various communication channels
 * such as WeChat, DingTalk, and email.
 * <p>
 * The class implements a set of methods for sending alerts, allowing users to select the desired
 * communication channel for alert notifications. The available channels are defined by their
 * respective services: `WeChatAlertService`, `DingTalkAlertService`, and `EmailAlertService`.
 * <p>
 * This controller aims to provide a flexible and extensible way to manage and distribute alerts
 * to relevant parties based on the chosen communication channel.
 * <p>
 * Note that appropriate authentication and authorization mechanisms should be implemented to
 * ensure that only authorized users can trigger alerts through this controller.
 *
 * @Author: smartloli
 * @Date: 2023/8/20 11:35
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/alert")
@Slf4j
public class AlertController {

    @Autowired
    private IAlertChannelDaoService alertChannelDaoService;

    @Autowired
    private IClusterDaoService clusterDaoService;

    @GetMapping("/channel")
    public String channelView() {
        return "alert/channel.html";
    }

    @GetMapping("/rule")
    public String rulelView() {
        return "alert/rule.html";
    }

    @RequestMapping(value = "/channel/table/ajax", method = RequestMethod.GET)
    public void pageAlertChannelAjax(HttpServletResponse response, HttpServletRequest request) {

        String remoteAddr = request.getRemoteAddr();
        HttpSession session = request.getSession();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Alert channel table list:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // get user roles
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

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
        map.put("cid", clusterInfo.getClusterId());
        map.put("roles", roles);
        map.put("search", search);

        Page<AlertChannelInfo> pages = this.alertChannelDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (AlertChannelInfo alertChannelInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("id", alertChannelInfo.getId());
            target.put("channel_name", alertChannelInfo.getChannelName());
            target.put("channel_type", HtmlAttributeUtil.getAlertChannelHtml(alertChannelInfo.getChannelType()));
            target.put("channel_url", HtmlAttributeUtil.getAlertChannelUrlHtml(alertChannelInfo.getChannelUrl(), alertChannelInfo.getId(), "channel_view_url"));
            target.put("channel_auth_json", HtmlAttributeUtil.getAlertChannelUrlHtml(alertChannelInfo.getAuthJson(), alertChannelInfo.getId(), "channel_view_auth"));
            target.put("modify_time", alertChannelInfo.getModifyTime());
            target.put("operate", "<a href='' alert_channel_id='" + alertChannelInfo.getId() + "' channel_name='" + alertChannelInfo.getChannelName() + "' name='efak_alert_channel_edit' class='badge border border-warning text-warning'>编辑</a> <a href='' channel_name='" + alertChannelInfo.getChannelName() + "' alert_channel_id='" + alertChannelInfo.getId() + "' name='efak_alert_channel_delete' class='badge border border-danger text-danger'>删除</a>");

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
            log.error("Get alert channel info has error,msg is {}", ex);
        }
    }

    /**
     * Get alert channel type list.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/channel/type/list/ajax", method = RequestMethod.GET)
    public void pageAlertChannelTypeAjax(HttpServletResponse response, HttpServletRequest request) {
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();

        int offset = 0;
        JSONArray topics = new JSONArray();
        for (String role : KConstants.ALERT_CHANNEL_LIST) {
            if (StrUtil.isNotBlank(name)) {
                JSONObject topic = new JSONObject();
                if (role.contains(name)) {
                    topic.put("text", role);
                    topic.put("id", offset);
                }
                topics.add(topic);
            } else {
                JSONObject topic = new JSONObject();
                topic.put("text", role);
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
            log.error("Get alert channel name list has error,msg is {}", ex);
        }
    }

    /**
     * Add or edit alert channel.
     *
     * @param action
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/channel/info/{action}", method = RequestMethod.POST)
    public boolean addOrEditChannelAjax(@PathVariable("action") String action, HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") Long uid, @RequestParam("channelName") String channelName, @RequestParam("channelType") String channelType, @RequestParam("channelUrl") String channelUrl, @RequestParam("channelAuthJson") String channelAuthJson) {
        String remoteAddr = request.getRemoteAddr();
        HttpSession session = request.getSession();
        String clusterAlias = Md5Util.generateMD5(KConstants.SessionClusterId.CLUSTER_ID + remoteAddr);
        log.info("Alert channel name list:: get remote[{}] clusterAlias from session md5 = {}", remoteAddr, clusterAlias);
        Long cid = Long.parseLong(session.getAttribute(clusterAlias).toString());
        ClusterInfo clusterInfo = clusterDaoService.clusters(cid);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // get user roles
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Boolean status = false;
        try {
            AlertChannelInfo alertChannelInfo = new AlertChannelInfo();
            alertChannelInfo.setChannelName(channelName);
            alertChannelInfo.setChannelType(KConstants.ALERT_CHANNEL_MAP.get(channelType));
            alertChannelInfo.setChannelUrl(channelUrl);
            alertChannelInfo.setAuthJson(channelAuthJson);
            alertChannelInfo.setClusterId(clusterInfo.getClusterId());
            alertChannelInfo.setChannelUserRoles(roles);
            if ("add".equals(action)) {
                status = this.alertChannelDaoService.insert(alertChannelInfo);
            } else if ("edit".equals(action)) {
                alertChannelInfo.setId(uid);
                status = this.alertChannelDaoService.update(alertChannelInfo);
            }
        } catch (Exception e) {
            log.error("Add or update alert channel info has error, msg is {}", e);
        }
        return status;
    }

    @RequestMapping(value = "/channel/get/info/ajax", method = RequestMethod.GET)
    public void getAlertChannelInfoAjax(HttpServletResponse response, @RequestParam("uid") Long uid) {
        JSONObject object = new JSONObject();
        AlertChannelInfo alertChannelInfo = this.alertChannelDaoService.channel(uid);
        object.put("name", alertChannelInfo.getChannelName());
        object.put("type", KConstants.ALERT_CHANNEL_MAP.get(alertChannelInfo.getChannelType()));
        object.put("url", alertChannelInfo.getChannelUrl());
        object.put("auth", alertChannelInfo.getAuthJson());

        try {
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get alert channel name one has error,msg is {}", ex);
        }
    }

    /**
     * delete alert channel.
     *
     * @param response
     * @param uid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/channel/info/delete", method = RequestMethod.POST)
    public boolean delAlertChannelAjax(HttpServletResponse response, @RequestParam("uid") Long uid) {
        Boolean status = false;
        try {
            status = this.alertChannelDaoService.delete(uid);
        } catch (Exception e) {
            log.error("Delete alert channel info has error, msg is {}", e);
        }
        return status;
    }

}
