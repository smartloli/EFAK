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
import org.smartloli.kafka.eagle.common.protocol.plugins.ConnectConfigInfo;
import org.smartloli.kafka.eagle.common.util.*;
import org.smartloli.kafka.eagle.web.pojo.Signiner;
import org.smartloli.kafka.eagle.web.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
 * Used to manage and display Kafka connect information.
 *
 * @author smartloli.
 * <p>
 * Created by Aug 30, 2020
 */
@Controller
public class ConnectController {


    @Autowired
    private ConnectService connectService;

    /**
     * Kafka connect config uri address.
     */
    // @RequiresPermissions("/connect/config")
    @RequestMapping(value = "/connect/config", method = RequestMethod.GET)
    public ModelAndView connectConfigView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/connect/config");
        return mav;
    }

    @RequestMapping(value = "/connect/connectors/details/", method = RequestMethod.GET)
    public ModelAndView connectPluginsView(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        HttpSession session = request.getSession();
        String uri = StrUtils.convertNull(request.getParameter("uri"));
        String connector = StrUtils.convertNull(request.getParameter("connector"));

        try {
            uri = URLDecoder.decode(uri, "UTF-8");
            connector = URLDecoder.decode(connector, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (connectService.connectorHasAlive(uri)) {
            mav.setViewName("/connect/monitor");
        } else {
            mav.setViewName("/error/404");
        }
        return mav;
    }

    /**
     * Get kafka connect uri schema datasets by ajax.
     */
    @RequestMapping(value = "/connect/uri/table/ajax", method = RequestMethod.GET)
    public void connectUriListAjax(HttpServletResponse response, HttpServletRequest request) {
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
        map.put("size", iDisplayLength);
        map.put("cluster", clusterAlias);
        long count = connectService.connectConfigCount(map);
        List<ConnectConfigInfo> connectConfigs = connectService.getConnectConfigList(map);
        JSONArray aaDatas = new JSONArray();
        for (ConnectConfigInfo connectConfig : connectConfigs) {
            JSONObject object = new JSONObject();
            object.put("id", connectConfig.getId());
            object.put("uri", "<a class='connectors_link' href='#' uri='" + connectConfig.getConnectUri() + "'>" + connectConfig.getConnectUri() + "</a>");
            if (StrUtils.isNull(connectConfig.getVersion())) {
                object.put("version", "<span class='badge badge-warning'>NULL</span>");
            } else {
                object.put("version", "<span class='badge badge-primary'>" + connectConfig.getVersion() + "</span>");
            }
            if (KConstants.BrokerSever.CONNECT_URI_ALIVE.equals(connectConfig.getAlive())) {
                object.put("alive", "<span class='badge badge-success'>Alive</span>");
            } else {
                object.put("alive", "<span class='badge badge-danger'>Shutdown</span>");
            }
            object.put("created", connectConfig.getCreated());
            object.put("modify", connectConfig.getModify());
            if (KConstants.Role.ADMIN.equals(signiner.getUsername())) {
                object.put("operate",
                        "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='ke_connect_uri_modify' href='#"
                                + connectConfig.getId() + "'><i class='fas fa-edit fa-sm fa-fw mr-1'></i>Modify</a><a class='dropdown-item' href='#" + connectConfig.getId() + "' val='" + connectConfig.getConnectUri() + "' name='ke_connect_uri_del'><i class='fas fa-minus-circle fa-sm fa-fw mr-1'></i>Delete</a></div>");
            } else {
                object.put("operate", "");
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
     * Get kafka connect application datasets by ajax.
     */
    @RequestMapping(value = "/connect/connectors/table/ajax", method = RequestMethod.GET)
    public void getconnectorsListAjax(HttpServletResponse response, HttpServletRequest request) {
        String aoData = request.getParameter("aoData");
        JSONArray params = JSON.parseArray(aoData);
        int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
        String search = "";

        String uri = "";
        try {
            uri = URLDecoder.decode(request.getParameter("uri"), "UTF-8");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get uri has error, msg is ", e);
        }

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

        List<String> connectors = connectService.getConnectorsTableList(uri, search);
        JSONArray aaDatas = new JSONArray();
        int offset = 0;
        int id = iDisplayStart + 1;
        for (String connector : connectors) {
            if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                JSONObject object = new JSONObject();
                String uriStr = "";
                String connectorName = "";
                try {
                    uriStr = URLEncoder.encode(uri, "UTF-8");
                    connectorName = URLEncoder.encode(connector, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                object.put("id", id);
                object.put("connector", "<a href='/connect/connectors/details/?uri=" + uriStr + "&connector=" + connectorName + "'>" + connector + "</a>");
                id++;
                aaDatas.add(object);
            }
            offset++;
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", connectors.size());
        target.put("iTotalDisplayRecords", connectors.size());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Add kafka connect uri.
     */
    @RequestMapping(value = "/connect/uri/add/", method = RequestMethod.POST)
    public String addUser(HttpSession session, HttpServletRequest request) {
        String uri = request.getParameter("ke_connect_uri_name");
        if (!NetUtils.uri(uri)) {
            return "redirect:/errors/500";
        }
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        ConnectConfigInfo connectConfig = new ConnectConfigInfo();
        connectConfig.setCluster(clusterAlias);
        connectConfig.setConnectUri(uri);
        connectConfig.setCreated(CalendarUtils.getDate());
        connectConfig.setModify(CalendarUtils.getDate());
        String version = "";
        String commit = "";
        try {
            String result = HttpClientUtils.doGet(uri);
            JSONObject resultJson = JSON.parseObject(result);
            version = resultJson.getString("version");
            commit = resultJson.getString("commit");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get kafka connect version and commit has error: ", e);
        }
        connectConfig.setVersion(version);
        connectConfig.setAlive(KConstants.BrokerSever.CONNECT_URI_ALIVE);

        try {
            if (connectService.insertOrUpdateConnectConfig(connectConfig) > 0) {
                return "redirect:/connect/config";
            } else {
                return "redirect:/errors/500";
            }
        } catch (Exception ex) {
            ErrorUtils.print(this.getClass()).error("Add kafka connect config schema has error: ", ex);
            return "redirect:/errors/500";
        }
    }

    /**
     * Modify kafka connect uri address by id
     */
    @RequestMapping(value = "/connect/uri/modify/", method = RequestMethod.POST)
    public String modifyConnectUriById(HttpSession session, HttpServletRequest request) {
        String idStr = request.getParameter("ke_connect_uri_id_modify");
        String connectUri = request.getParameter("ke_connect_uri_name_modify");

        ConnectConfigInfo configInfo = new ConnectConfigInfo();
        configInfo.setModify(CalendarUtils.getDate());
        try {
            configInfo.setId(Integer.parseInt(idStr));
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get kafka connect uri id has error: ", e);
            return "redirect:/errors/500";
        }
        configInfo.setConnectUri(connectUri);
        if (connectService.modifyConnectConfigById(configInfo) > 0) {
            return "redirect:/connect/config";
        } else {
            return "redirect:/errors/500";
        }
    }

    /**
     * Get kafka connect uri info.
     */
    @RequestMapping(value = "/connect/uri/schema/{id}/ajax", method = RequestMethod.GET)
    public void findConnectUriByIdAjax(@PathVariable("id") int id, HttpServletResponse response, HttpServletRequest request) {
        try {
            byte[] output = connectService.findConnectUriById(id).toString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Delete kafka connect uri by id.
     */
    @RequestMapping(value = "/connect/uri/{id}/del", method = RequestMethod.GET)
    public ModelAndView delConnectUriConfigById(@PathVariable("id") int id, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> map = new HashMap<>();
        map.put("cluster", clusterAlias);
        map.put("id", id);

        int code = connectService.deleteConnectConfigById(map);
        if (code > 0) {
            return new ModelAndView("redirect:/connect/config");
        } else {
            return new ModelAndView("redirect:/errors/500");
        }
    }

    @RequestMapping(value = "/connect/plugins/result/ajax/", method = RequestMethod.GET)
    public void getConnectorsByNameAjax(HttpServletResponse response, HttpServletRequest request) {
        String uri = "";
        String connector = "";
        try {
            uri = URLDecoder.decode(request.getParameter("uri"), "UTF-8");
            connector = URLDecoder.decode(request.getParameter("connector"), "UTF-8");
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Get uri and connector has error, msg is ", e);
        }
        try {
            byte[] output = connectService.getConnectorPluginsSummary(uri, connector).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
