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

import org.apache.commons.lang3.StringUtils;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.smartloli.kafka.eagle.web.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Various charts used to show key indicators monitored by Kafka.
 *
 * @author smartloli.
 * <p>
 * Created by Jun 02, 2022
 */
@Controller
public class DashboardController {

    /**
     * Dashboard data generator interface.
     */
    @Autowired
    private DashboardService dashboradService;

    @Autowired
    private MetricsService metricsService;

    /**
     * Index viewer.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView indexView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/main/index");
        return mav;
    }

    /**
     * Obtain the indicator data to be displayed on the panel.
     */
    @RequestMapping(value = "/get/dashboard/panel/ajax", method = RequestMethod.GET)
    public void getDashboardPanelAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            byte[] output = dashboradService.getDashboardPanel(clusterAlias).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequestMapping(value = "/get/dashboard/areachart/ajax", method = RequestMethod.GET)
    public void getDashboardAreaChartAjax(HttpServletResponse response, HttpServletRequest request, HttpSession session) {
        try {
            String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

            Map<String, Object> param = new HashMap<>();
            param.put("cluster", clusterAlias);
            param.put("stime", CalendarUtils.getCustomLastDay(0));
            param.put("etime", CalendarUtils.getCustomLastDay(0));
            param.put("type", KConstants.CollectorType.KAFKA);
            param.put("modules", Arrays.asList(KConstants.MBean.MESSAGEIN, KConstants.MBean.BYTEIN, KConstants.MBean.BYTEOUT, KConstants.MBean.OSFREEMEMORY));
            String target = metricsService.query(param);
            if (StringUtils.isEmpty(target)) {
                target = "";
            }
            byte[] output = target.getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get data from Kafka in dashboard by ajax.
     */
    @RequestMapping(value = "/dash/{tkey}/table/ajax", method = RequestMethod.GET)
    public void dashTopicRankAjax(@PathVariable("tkey") String tkey, HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("cluster", clusterAlias);
            params.put("tkey", tkey);
            byte[] output = dashboradService.getTopicRank(params).toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get memory data from Kafka in dashboard by ajax.
     */
    @RequestMapping(value = "/dash/os/mem/ajax", method = RequestMethod.GET)
    public void dashOSMemAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("cluster", clusterAlias);
            params.put("key", "os%");
            byte[] output = dashboradService.getOSMem(params).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get cpu data from Kafka in dashboard by ajax.
     */
    @RequestMapping(value = "/dash/used/cpu/ajax", method = RequestMethod.GET)
    public void dashUsedCPUAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("cluster", clusterAlias);
            params.put("key", "cpu_used");
            byte[] output = dashboradService.getUsedCPU(params).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
