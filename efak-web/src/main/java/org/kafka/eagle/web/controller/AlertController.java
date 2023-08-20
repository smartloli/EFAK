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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.utils.HtmlAttributeUtil;
import org.kafka.eagle.pojo.alert.AlertChannelInfo;
import org.kafka.eagle.web.service.IAlertChannelDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/channel")
    public String channelView() {
        return "alert/channel.html";
    }

    @RequestMapping(value = "/channel/table/ajax", method = RequestMethod.GET)
    public void pageAlertChannelAjax(HttpServletResponse response, HttpServletRequest request) {

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

        Page<AlertChannelInfo> pages = this.alertChannelDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (AlertChannelInfo alertChannelInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("id", alertChannelInfo.getId());
            target.put("channel_name", alertChannelInfo.getChannelName());
            target.put("channel_type", HtmlAttributeUtil.getAlertChannelHtml(alertChannelInfo.getChannelName()));
            target.put("channel_url", HtmlAttributeUtil.getAlertChannelUrlHtml(alertChannelInfo.getChannelUrl(), alertChannelInfo.getId()));
            target.put("channel_auth_json", HtmlAttributeUtil.getAlertChannelUrlHtml(alertChannelInfo.getAuthJson(), alertChannelInfo.getId()));
            target.put("modify_time", alertChannelInfo.getModifyTime());
            if ("ROLE_ADMIN".equals(alertChannelInfo.getChannelUserRoles())) {
                target.put("operate", "");
            } else {
                target.put("operate", "<a href='' alert_channel_id='" + alertChannelInfo.getId() + "' channel_name='" + alertChannelInfo.getChannelName() + "' name='efak_alert_channel_edit' class='badge border border-warning text-warning'>编辑</a> <a href='' alert_channel_id='" + alertChannelInfo.getId() + "' name='efak_alert_channel_delete' class='badge border border-danger text-danger'>删除</a>");
            }
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

}
