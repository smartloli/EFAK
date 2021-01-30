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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Kafka & Zookeeper controller to viewer data.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 6, 2016.
 * <p>
 * Update by hexiang 20170216
 */
@Controller
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    /**
     * Cluster viewer.
     */
    @RequestMapping(value = "/cluster/info", method = RequestMethod.GET)
    public ModelAndView clusterView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/cluster/cluster");
        return mav;
    }

    /**
     * Cluster viewer.
     */
    @RequestMapping(value = "/cluster/multi", method = RequestMethod.GET)
    public ModelAndView clustersView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/cluster/multicluster");
        return mav;
    }

    /**
     * Zookeeper client viewer.
     */
    @RequiresPermissions("/cluster/zkcli")
    @RequestMapping(value = "/cluster/zkcli", method = RequestMethod.GET)
    public ModelAndView zkCliView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/cluster/zkcli");
        return mav;
    }

    /**
     * Cluster worknodes viewer.
     */
    @RequestMapping(value = "/cluster/worknodes", method = RequestMethod.GET)
    public ModelAndView workNodesView(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/cluster/worknodes");
        return mav;
    }

    /**
     * Get cluster data by ajax.
     */
    @RequestMapping(value = "/cluster/info/{type}/ajax", method = RequestMethod.GET)
    public void clusterAjax(@PathVariable("type") String type, HttpServletResponse response, HttpServletRequest request) {
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

        JSONObject deserializeClusters = JSON.parseObject(clusterService.get(clusterAlias, type));
        JSONArray clusters = deserializeClusters.getJSONArray(type);
        int offset = 0;
        JSONArray aaDatas = new JSONArray();

        for (Object object : clusters) {
            JSONObject cluster = (JSONObject) object;
            if (search.length() > 0 && search.equals(cluster.getString("host"))) {
                JSONObject obj = new JSONObject();
                obj.put("id", cluster.getInteger("id"));
                obj.put("port", cluster.getInteger("port"));
                obj.put("ip", cluster.getString("host"));
                if ("kafka".equals(type)) {
                    obj.put("brokerId", "<a href='/metrics/brokers/" + cluster.getString("ids") + "/' target='_blank'>" + cluster.getString("ids") + "</a>");
                    obj.put("jmxPort", cluster.getInteger("jmxPort"));
                    obj.put("memory", clusterService.getUsedMemory(clusterAlias, cluster.getString("host"), cluster.getInteger("jmxPort")));
                    obj.put("cpu", clusterService.getUsedCpu(clusterAlias, cluster.getString("host"), cluster.getInteger("jmxPort")));
                    obj.put("created", cluster.getString("created"));
                    obj.put("modify", cluster.getString("modify"));
                    String version = clusterService.getKafkaVersion(cluster.getString("host"), cluster.getInteger("jmxPort"), cluster.getString("ids"), clusterAlias);
                    version = (version == "" ? Kafka.UNKOWN : version);
                    if (Kafka.UNKOWN.equals(version)) {
                        obj.put("version", "<span class='badge badge-danger'>" + version + "</span>");
                    } else {
                        obj.put("version", "<span class='badge badge-success'>" + version + "</span>");
                    }
                } else if ("zk".equals(type)) {
                    String mode = cluster.getString("mode");
                    if ("death".equals(mode)) {
                        obj.put("mode", "<span class='badge badge-danger'>" + mode + "</span>");
                    } else {
                        obj.put("mode", "<span class='badge badge-success'>" + mode + "</span>");
                    }
                    String version = cluster.getString("version");
                    if (StrUtils.isNull(version)) {
                        obj.put("version", "<span class='badge badge-danger'>unkown</span>");
                    } else {
                        obj.put("version", "<span class='badge badge-success'>" + version + "</span>");
                    }
                } else if ("worknodes".equals(type)) {
                    if (cluster.getBoolean("isAlive")) {
                        obj.put("memory", cluster.getString("memory"));
                        obj.put("cpu", cluster.getString("cpu"));
                        obj.put("created", cluster.getString("startTime"));
                        obj.put("status", "<span class='badge badge-success'>" + KConstants.WorkNode.ALIVE + "</span>");
//                        obj.put("operate",
//                                "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='ke_worknodes_start' href='#?ip="
//                                        + obj.getString("ip") + "&port=" + obj.getInteger("port") + "'><i class='far fa-play-circle fa-sm fa-fw mr-1'></i>Start</a><a class='dropdown-item' href='#?ip=" + obj.getString("ip") + "&port=" + obj.getInteger("port") + "' name='ke_worknodes_shutdown'><i class='far fa-stop-circle fa-sm fa-fw mr-1'></i>Shutdown</a></div>");
                    } else {
                        obj.put("memory", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                        obj.put("cpu", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                        obj.put("created", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                        obj.put("status", "<span class='badge badge-danger'>" + KConstants.WorkNode.SHUTDOWN + "</span>");
//                        obj.put("operate",
//                                "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='ke_worknodes_start' href='#?ip="
//                                        + obj.getString("ip") + "&port=" + obj.getInteger("port") + "'><i class='far fa-play-circle fa-sm fa-fw mr-1'></i>Start</a><a class='dropdown-item' href='#?ip=" + obj.getString("ip") + "&port=" + obj.getInteger("port") + "' name='ke_worknodes_shutdown'><i class='far fa-stop-circle fa-sm fa-fw mr-1'></i>Shutdown</a></div>");
                    }
                }
                aaDatas.add(obj);
            } else if (search.length() == 0) {
                if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", cluster.getInteger("id"));
                    obj.put("port", cluster.getInteger("port"));
                    obj.put("ip", cluster.getString("host"));
                    if ("kafka".equals(type)) {
                        obj.put("brokerId", "<a href='/metrics/brokers/" + cluster.getString("ids") + "/' target='_blank'>" + cluster.getString("ids") + "</a>");
                        obj.put("jmxPort", cluster.getInteger("jmxPort"));
                        obj.put("memory", clusterService.getUsedMemory(clusterAlias, cluster.getString("host"), cluster.getInteger("jmxPort")));
                        obj.put("cpu", clusterService.getUsedCpu(clusterAlias, cluster.getString("host"), cluster.getInteger("jmxPort")));
                        obj.put("created", cluster.getString("created"));
                        obj.put("modify", cluster.getString("modify"));
                        String version = clusterService.getKafkaVersion(cluster.getString("host"), cluster.getInteger("jmxPort"), cluster.getString("ids"), clusterAlias);
                        version = (version == "" ? Kafka.UNKOWN : version);
                        if (Kafka.UNKOWN.equals(version)) {
                            obj.put("version", "<span class='badge badge-danger'>" + version + "</span>");
                        } else {
                            obj.put("version", "<span class='badge badge-success'>" + version + "</span>");
                        }
                    } else if ("zk".equals(type)) {
                        String mode = cluster.getString("mode");
                        if ("death".equals(mode)) {
                            obj.put("mode", "<span class='badge badge-danger'>" + mode + "</span>");
                        } else {
                            obj.put("mode", "<span class='badge badge-success'>" + mode + "</span>");
                        }
                        String version = cluster.getString("version");
                        if (StrUtils.isNull(version)) {
                            obj.put("version", "<span class='badge badge-danger'>unkown</span>");
                        } else {
                            obj.put("version", "<span class='badge badge-success'>" + version + "</span>");
                        }
                    } else if ("worknodes".equals(type)) {
                        if (cluster.getBoolean("isAlive")) {
                            obj.put("memory", cluster.getString("memory"));
                            obj.put("cpu", cluster.getString("cpu"));
                            obj.put("created", cluster.getString("startTime"));
                            obj.put("status", "<span class='badge badge-success'>" + KConstants.WorkNode.ALIVE + "</span>");
//                            obj.put("operate",
//                                    "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='ke_worknodes_start' href='#?ip="
//                                            + obj.getString("ip") + "&port=" + obj.getInteger("port") + "'><i class='far fa-play-circle fa-sm fa-fw mr-1'></i>Start</a><a class='dropdown-item' href='#?ip=" + obj.getString("ip") + "&port=" + obj.getInteger("port") + "' name='ke_worknodes_shutdown'><i class='far fa-stop-circle fa-sm fa-fw mr-1'></i>Shutdown</a></div>");
                        } else {
                            obj.put("memory", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                            obj.put("cpu", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                            obj.put("created", "<span class='badge badge-secondary'>" + KConstants.WorkNode.UNKOWN + "</span>");
                            obj.put("status", "<span class='badge badge-danger'>" + KConstants.WorkNode.SHUTDOWN + "</span>");
//                            obj.put("operate",
//                                    "<div class='btn-group btn-group-sm' role='group'><button id='ke_btn_action' class='btn btn-primary dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><div aria-labelledby='ke_btn_action' class='dropdown-menu dropdown-menu-right'><a class='dropdown-item' name='ke_worknodes_start' href='#?ip="
//                                            + obj.getString("ip") + "&port=" + obj.getInteger("port") + "'><i class='far fa-play-circle fa-sm fa-fw mr-1'></i>Start</a><a class='dropdown-item' href='#?ip=" + obj.getString("ip") + "&port=" + obj.getInteger("port") + "' name='ke_worknodes_shutdown'><i class='far fa-stop-circle fa-sm fa-fw mr-1'></i>Stop</a></div>");
                        }
                    }
                    aaDatas.add(obj);
                }
                offset++;
            }
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", clusters.size());
        target.put("iTotalDisplayRecords", clusters.size());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Change cluster viewer address.
     */
    @RequestMapping(value = "/cluster/info/{clusterAlias}/change", method = RequestMethod.GET)
    public ModelAndView clusterChangeAjax(@PathVariable("clusterAlias") String clusterAlias, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        if (!clusterService.hasClusterAlias(clusterAlias)) {
            return new ModelAndView("redirect:/error/404");
        } else {
            session.removeAttribute(KConstants.SessionAlias.CLUSTER_ALIAS);
            session.setAttribute(KConstants.SessionAlias.CLUSTER_ALIAS, clusterAlias);
            String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
            String dropList = "<div class='dropdown-menu dropdown-menu-right' aria-labelledby='clusterDropdown'>";
            int i = 0;
            for (String clusterAliasStr : clusterAliass) {
                if (!clusterAliasStr.equals(clusterAlias) && i < KConstants.SessionAlias.CLUSTER_ALIAS_LIST_LIMIT) {
                    dropList += "<a class='dropdown-item' href='/cluster/info/" + clusterAliasStr + "/change'><i class='fas fa-feather-alt fa-sm fa-fw mr-1'></i>" + clusterAliasStr + "</a>";
                    i++;
                }
            }
            dropList += "<a class='dropdown-item' href='/cluster/multi'><i class='fas fa-server fa-sm fa-fw mr-1'></i>More...</a></div>";
            session.removeAttribute(KConstants.SessionAlias.CLUSTER_ALIAS_LIST);
            session.setAttribute(KConstants.SessionAlias.CLUSTER_ALIAS_LIST, dropList);
            return new ModelAndView("redirect:/");
        }
    }

    /**
     * Get multicluster information.
     */
    @RequestMapping(value = "/cluster/info/multicluster/ajax", method = RequestMethod.GET)
    public void multiClusterAjax(HttpServletResponse response, HttpServletRequest request) {
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

        JSONArray clusterAliass = clusterService.clusterAliass();
        int offset = 0;
        JSONArray aaDatas = new JSONArray();
        for (Object object : clusterAliass) {
            JSONObject cluster = (JSONObject) object;
            if (search.length() > 0 && cluster.getString("clusterAlias").contains(search)) {
                JSONObject target = new JSONObject();
                target.put("id", cluster.getInteger("id"));
                target.put("clusterAlias", cluster.getString("clusterAlias"));
                target.put("zkhost", cluster.getString("zkhost"));
                target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='badge badge-primary'>Change</a>");
                aaDatas.add(target);
            } else if (search.length() == 0) {
                if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
                    JSONObject target = new JSONObject();
                    target.put("id", cluster.getInteger("id"));
                    target.put("clusterAlias", cluster.getString("clusterAlias"));
                    target.put("zkhost", cluster.getString("zkhost"));
                    target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='badge badge-primary'>Change</a>");
                    aaDatas.add(target);
                }
                offset++;
            }
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", clusterAliass.size());
        target.put("iTotalDisplayRecords", clusterAliass.size());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get zookeeper client whether live data by ajax.
     */
    @RequestMapping(value = "/cluster/zk/islive/ajax", method = RequestMethod.GET)
    public void zkCliLiveAjax(HttpServletResponse response, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            byte[] output = clusterService.status(clusterAlias).toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Execute zookeeper command by ajax.
     */
    @RequestMapping(value = "/cluster/zk/cmd/ajax", method = RequestMethod.GET)
    public void zkCliCmdAjax(HttpServletResponse response, HttpServletRequest request) {
        String cmd = request.getParameter("cmd");
        String type = request.getParameter("type");

        HttpSession session = request.getSession();
        String clusterAlias = session.getAttribute(KConstants.SessionAlias.CLUSTER_ALIAS).toString();

        try {
            byte[] output = clusterService.execute(clusterAlias, cmd, type).getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
