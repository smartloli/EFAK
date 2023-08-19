/**
 * SystemController.java
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
import org.kafka.eagle.common.constants.JobConstans;
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.common.utils.HtmlAttributeUtil;
import org.kafka.eagle.pojo.audit.AuditLogInfo;
import org.kafka.eagle.pojo.page.PageInfo;
import org.kafka.eagle.pojo.user.UserInfo;
import org.kafka.eagle.web.quartz.manager.QuartzManager;
import org.kafka.eagle.web.quartz.pojo.JobDetails;
import org.kafka.eagle.web.service.IAuditDaoService;
import org.kafka.eagle.web.service.IUserDaoService;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SystemController class handles system-related operations and functionalities.
 * It provides methods for managing system settings, configurations, and behaviors.
 * This controller is responsible for coordinating actions between various system components.
 *
 * @Author: smartloli
 * @Date: 2023/7/1 12:53
 * @Version: 3.4.0
 */
@Controller
@RequestMapping("/system")
@Slf4j
public class SystemController {

    @Autowired
    private IAuditDaoService auditDaoService;

    @Autowired
    private QuartzManager qtzManager;

    @Autowired
    private IUserDaoService userDaoService;

    @GetMapping("/profile")
    public String profileView() {
        return "system/profile.html";
    }

    @GetMapping("/user")
    public String userView() {
        return "system/user.html";
    }

    @GetMapping("/job")
    public String jobView() {
        return "system/job.html";
    }


    @GetMapping("/audit")
    public String auditView() {
        return "system/audit.html";
    }

    @RequestMapping(value = "/audit/table/ajax", method = RequestMethod.GET)
    public void pageAuditAjax(HttpServletResponse response, HttpServletRequest request) {

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


        Page<AuditLogInfo> pages = this.auditDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (AuditLogInfo auditLogInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("host", auditLogInfo.getHost());
            target.put("uri", auditLogInfo.getUri().length() > KConstants.TableCommon.TR_LEN ? auditLogInfo.getUri().substring(0, KConstants.TableCommon.TR_LEN) + "..." : auditLogInfo.getUri());
            target.put("params", auditLogInfo.getParams().length() > KConstants.TableCommon.TR_LEN ? auditLogInfo.getParams().substring(0, KConstants.TableCommon.TR_LEN) + "..." : auditLogInfo.getParams());
            target.put("method", auditLogInfo.getMethod());
            target.put("spent", auditLogInfo.getSpentTime());
            target.put("code", auditLogInfo.getCode());
            target.put("modify", auditLogInfo.getModifyTime());
            target.put("operate", "<a href='' id='" + auditLogInfo.getId() + "' name='efak_system_audit' class='badge border border-primary text-primary'>查看详情</a>");
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
            log.error("Get audit log info has error,msg is {}", ex);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/audit/log/detail/ajax", method = RequestMethod.GET)
    public String getAuditLogDetail(@RequestParam("id") Long id, HttpServletResponse response, HttpSession session, HttpServletRequest request) {
        AuditLogInfo auditLogInfo = this.auditDaoService.auditById(id);
        return JSON.toJSONString(auditLogInfo);
    }

    @RequestMapping(value = "/job/table/ajax", method = RequestMethod.GET)
    public void pageJobAjax(HttpServletResponse response, HttpServletRequest request) {

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
        Integer pageNum = iDisplayStart;
        Integer pageSize = iDisplayLength;

        List<JobDetails> jobAndTrigger = this.qtzManager.queryAllJobBean(search);
        PageInfo<JobDetails> pageInfo = new PageInfo<>(jobAndTrigger, pageNum, pageSize);

        JSONArray aaDatas = new JSONArray();

        for (Object object : pageInfo.getList()) {
            JobDetails jobDetails = (JobDetails) object;
            JSONObject target = new JSONObject();
            target.put("group", jobDetails.getJobGroupName());
            target.put("name", JobConstans.JOBS.get(jobDetails.getJobName()) == null ? JobConstans.UNKOWN : JobConstans.JOBS.get(jobDetails.getJobName()));
            target.put("cron", jobDetails.getCronExpression());
            target.put("start_time", jobDetails.getStartTime());
            target.put("next_fire_time", jobDetails.getNextFireTime());
            target.put("status", jobDetails.getStatus());
            target.put("time_zone", jobDetails.getTimeZone());
            if (Trigger.TriggerState.NORMAL.name().equals(jobDetails.getStatus())) {
                target.put("operate", "<a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' name='efak_system_job_pause' class='badge border border-secondary text-secondary'>暂停</a> <a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' cron_name='" + jobDetails.getCronExpression() + "' name='efak_system_job_edit' class='badge border border-warning text-warning'>编辑</a> <a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' name='efak_system_job_delete' class='badge border border-danger text-danger'>删除</a>");
            } else {
                target.put("operate", "<a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' name='efak_system_job_resume' class='badge border border-primary text-primary'>恢复</a> <a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' cron_name='" + jobDetails.getCronExpression() + "' name='efak_system_job_edit' class='badge border border-warning text-warning'>编辑</a> <a href='' job_group='" + jobDetails.getJobGroupName() + "' job_name='" + jobDetails.getJobName() + "' job_name_desc='" + JobConstans.JOBS.get(jobDetails.getJobName()) + "' name='efak_system_job_delete' class='badge border border-danger text-danger'>删除</a>");
            }
            aaDatas.add(target);
        }

        JSONObject target = new JSONObject();
        target.put("sEcho", sEcho);
        target.put("iTotalRecords", pageInfo.getTotal());
        target.put("iTotalDisplayRecords", pageInfo.getTotal());
        target.put("aaData", aaDatas);
        try {
            byte[] output = target.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get job info has error,msg is {}", ex);
        }
    }

    /**
     * Get job name list.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/job/name/list/ajax", method = RequestMethod.GET)
    public void pageJobNamesAjax(HttpServletResponse response, HttpServletRequest request) {
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();

        int offset = 0;
        JSONArray topics = new JSONArray();
        for (String jobName : JobConstans.JOBS.values()) {
            if (!this.qtzManager.getAllJobNames().contains(jobName)) {
                if (StrUtil.isNotBlank(name)) {
                    JSONObject topic = new JSONObject();
                    if (jobName.contains(name)) {
                        topic.put("text", jobName);
                        topic.put("id", offset);
                    }
                    topics.add(topic);
                } else {
                    JSONObject topic = new JSONObject();
                    topic.put("text", jobName);
                    topic.put("id", offset);
                    topics.add(topic);
                }
            }

            offset++;
        }

        object.put("items", topics);
        try {
            byte[] output = object.toJSONString().getBytes();
            BaseController.response(output, response);
        } catch (Exception ex) {
            log.error("Get job name list has error,msg is {}", ex);
        }
    }

    // add job task
    @ResponseBody
    @RequestMapping(value = "/job/task/add", method = RequestMethod.POST)
    public boolean addJobTask(HttpServletResponse response, @RequestParam("jobGroupName") String jobGroupName, @RequestParam("jobName") String jobName, @RequestParam("cronName") String cronName) {
        Boolean status = false;
        try {
            String jobClass = "";
            for (Map.Entry<String, String> job : JobConstans.JOBS.entrySet()) {
                if (job.getValue().equals(jobName)) {
                    jobClass = job.getKey();
                    break;
                }
            }
            if (StrUtil.isNotBlank(jobClass)) {
                status = this.qtzManager.addOrUpdateJob(this.getJobClass(jobClass), jobClass, jobGroupName, cronName);
            }
        } catch (Exception e) {
            log.error("Add or update job name has error, msg is {}", e);
        }
        return status;
    }

    private Class<? extends QuartzJobBean> getJobClass(String classname) throws Exception {
        Class<?> clazz = Class.forName(classname);
        return (Class<? extends QuartzJobBean>) clazz;
    }

    /**
     * Delete, pause, resume job task
     *
     * @param response
     * @param jobName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/job/task/{action}", method = RequestMethod.POST)
    public boolean actionJobTask(@PathVariable("action") String action, HttpServletResponse response, @RequestParam("jobGroup") String jobGroup, @RequestParam("jobName") String jobName) {
        Boolean status = false;
        try {
            if (StrUtil.isNotBlank(jobName) && StrUtil.isNotBlank(jobGroup)) {
                if (JobConstans.DELETE.equals(action)) {
                    status = this.qtzManager.deleteJob(jobName, jobGroup);
                } else if (JobConstans.PAUSE.equals(action)) {
                    status = this.qtzManager.pauseJob(jobName, jobGroup);
                } else if (JobConstans.RESUME.equals(action)) {
                    status = this.qtzManager.resumeJob(jobName, jobGroup);
                }
            }
        } catch (Exception e) {
            log.error("Delete job name has error, msg is {}", e);
        }
        return status;
    }

    /**
     * page user list
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/user/table/ajax", method = RequestMethod.GET)
    public void pageUserAjax(HttpServletResponse response, HttpServletRequest request) {

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

        Page<UserInfo> pages = this.userDaoService.pages(map);
        JSONArray aaDatas = new JSONArray();

        for (UserInfo userInfo : pages.getRecords()) {
            JSONObject target = new JSONObject();
            target.put("id", userInfo.getId());
            target.put("username", userInfo.getUsername());
            target.put("password", userInfo.getPassword());
            target.put("roles", HtmlAttributeUtil.getUserRoleHtml(userInfo.getRoles()));
            target.put("modify_time", userInfo.getModifyTime());
            if ("ROLE_ADMIN".equals(userInfo.getRoles())) {
                target.put("operate", "");
            } else {
                target.put("operate", "<a href='' uid='" + userInfo.getId() + "' username='" + userInfo.getUsername() + "' password='" + userInfo.getOriginPassword() + "' roles='" + userInfo.getRoles() + "' name='efak_system_user_edit' class='badge border border-warning text-warning'>编辑</a> <a href='' username='" + userInfo.getUsername() + "' uid='" + userInfo.getId() + "' name='efak_system_user_delete' class='badge border border-danger text-danger'>删除</a>");
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
            log.error("Get user info has error,msg is {}", ex);
        }
    }

    /**
     * get user roles list.
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/user/roles/list/ajax", method = RequestMethod.GET)
    public void pageUserRolesAjax(HttpServletResponse response, HttpServletRequest request) {
        String name = request.getParameter("name");
        JSONObject object = new JSONObject();

        int offset = 0;
        JSONArray topics = new JSONArray();
        for (String role : KConstants.USER_ROLES_LIST) {
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
            log.error("Get job name list has error,msg is {}", ex);
        }
    }

    /**
     * add or update user info.
     *
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/info/{action}", method = RequestMethod.POST)
    public boolean addOrEditUserAjax(@PathVariable("action") String action, HttpServletResponse response, @RequestParam("uid") Long uid, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("roles") String roles) {
        Boolean status = false;
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(username);
            userInfo.setOriginPassword(password);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            userInfo.setPassword(encoder.encode(password));
            userInfo.setRoles(KConstants.USER_ROLES_MAP.get(roles));
            if ("add".equals(action)) {
                status = this.userDaoService.insert(userInfo);
            } else if ("edit".equals(action)) {
                userInfo.setId(uid);
                status = this.userDaoService.update(userInfo);
            }
        } catch (Exception e) {
            log.error("Add or update user info has error, msg is {}", e);
        }
        return status;
    }

    /**
     * delete user info by uid.
     *
     * @param response
     * @param uid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/info/delete", method = RequestMethod.POST)
    public boolean delUserAjax(HttpServletResponse response, @RequestParam("uid") Long uid) {
        Boolean status = false;
        try {
            status = this.userDaoService.delete(uid);
        } catch (Exception e) {
            log.error("Delete user info has error, msg is {}", e);
        }
        return status;
    }

    @ResponseBody
    @RequestMapping(value = "/user/password/reset", method = RequestMethod.POST)
    public boolean resetUserPasswordAjax(HttpServletResponse response, @RequestParam("username") String username, @RequestParam("passwordOld") String passwordOld, @RequestParam("passwordNew") String passwordNew) {
        Boolean status = false;
        try {
            UserInfo userInfo = this.userDaoService.users(username, passwordOld);
            if (userInfo != null) {
                userInfo.setPassword(new BCryptPasswordEncoder().encode(passwordNew));
                userInfo.setOriginPassword(passwordNew);
                status = this.userDaoService.reset(userInfo);
            }
        } catch (Exception e) {
            log.error("Reset user password has error, msg is {}", e);
        }
        return status;
    }
}
