/**
 * MenuController.java
 * <p>
 * Copyright 2025 smartloli
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

import org.kafka.eagle.tool.constant.KeConst;
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Description: Menu page controller for EFAK web interface
 * Author: Mr.SmartLoli
 * Date: 2025/6/23 00:27
 * Version: 1.0
 */
@Controller
public class MenuController {

    @Autowired
    private UserService userService;

    @GetMapping("/topics")
    public String topics(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "topics");
        model.addAttribute("pageTitle", "主题管理");
        return "view/topics";
    }

    @GetMapping("/topic/view")
    public ModelAndView topicDetail(Model model, @RequestParam("name") String topicName) {
        addCommonAttributes(model);
        ModelAndView mav = new ModelAndView("view/topic-detail");
        model.addAttribute("activePage", "topic-detail");
        model.addAttribute("pageTitle", "主题详情");
        mav.addObject("topicName", topicName);
        return mav;
    }

    @GetMapping("/ai-assistant")
    public String aiAssistant(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "ai-assistant");
        model.addAttribute("pageTitle", "AI助手");
        return "view/ai-assistant";
    }

    @GetMapping({"/dashboard"})
    public String dashboard(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "仪表盘");
        return "view/dashboard";
    }

    @GetMapping("/cluster")
    public String cluster(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "cluster");
        model.addAttribute("pageTitle", "集群管理");
        return "view/cluster";
    }

    @GetMapping("/consumers")
    public String consumers(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "consumers");
        model.addAttribute("pageTitle", "消费者组");
        return "view/consumers";
    }

    @GetMapping("/consumer/view")
    public ModelAndView consumersDetail(Model model,@RequestParam("group") String group, @RequestParam("topic") String topic) {
        addCommonAttributes(model);
        ModelAndView mav = new ModelAndView("view/consumer-detail");
        model.addAttribute("activePage", "consumer-detail");
        model.addAttribute("pageTitle", "消费者详情详情");
        mav.addObject("group", group);
        mav.addObject("topic", topic);
        return mav;
    }

    @GetMapping("/monitoring")
    public String monitoring(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "monitoring");
        model.addAttribute("pageTitle", "性能监控");
        return "view/monitoring";
    }

    @GetMapping("/config")
    public String config(Model model) {
        // 检查用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UserInfo userInfo = userService.getUserByUsername(username);

        // 检查是否为管理员
        if (userInfo == null || userInfo.getRoles() == null || !userInfo.getRoles().contains("ROLE_ADMIN")) {
            // 非管理员用户重定向到403页面
            return "redirect:/error-403";
        }

        addCommonAttributes(model);
        model.addAttribute("activePage", "config");
        model.addAttribute("pageTitle", "配置管理");
        return "view/config";
    }

    @GetMapping("/users")
    public String users(Model model) {
        // 检查用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UserInfo userInfo = userService.getUserByUsername(username);

        // 检查是否为管理员
        if (userInfo == null || userInfo.getRoles() == null || !userInfo.getRoles().contains("ROLE_ADMIN")) {
            // 非管理员用户重定向到403页面
            return "redirect:/error-403";
        }

        addCommonAttributes(model);
        model.addAttribute("activePage", "users");
        model.addAttribute("pageTitle", "用户管理");
        return "view/users";
    }

    @GetMapping("/scheduler")
    public String scheduler(Model model) {
        // 检查用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UserInfo userInfo = userService.getUserByUsername(username);

        // 检查是否为管理员
        if (userInfo == null || userInfo.getRoles() == null || !userInfo.getRoles().contains("ROLE_ADMIN")) {
            // 非管理员用户重定向到403页面
            return "redirect:/error-403";
        }

        addCommonAttributes(model);
        model.addAttribute("activePage", "scheduler");
        model.addAttribute("pageTitle", "任务调度");
        return "view/scheduler";
    }

    @GetMapping("/")
    public String manager(Model model) {
        // 管理员权限校验
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        UserInfo userInfo = userService.getUserByUsername(username);
        if (userInfo == null || userInfo.getRoles() == null || !userInfo.getRoles().contains("ROLE_ADMIN")) {
            return "redirect:/error-403";
        }

        addCommonAttributes(model);
        model.addAttribute("activePage", "manager");
        model.addAttribute("pageTitle", "多集群管理");
        return "view/manager";
    }

    @GetMapping("/alerts")
    public String alerts(Model model) {
        addCommonAttributes(model);
        model.addAttribute("activePage", "alerts");
        model.addAttribute("pageTitle", "系统告警");
        return "view/alerts";
    }

    /**
     * Add common attributes to the model
     *
     * @param model Spring MVC model
     */
    private void addCommonAttributes(Model model) {
        model.addAttribute("appVersion", KeConst.APP_VERSION.getValue());
        model.addAttribute("appName", "EFAK");
        model.addAttribute("pageTitle", "EFAK Monitoring System");

        // 获取当前登录用户信息
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                UserInfo userInfo = userService.getUserByUsername(username);

                if (userInfo != null) {
                    String displayName = userInfo.getUsername();
                    String displayRole = "用户";
                    String email = username + "@efak.ai";

                    if (userInfo.getRoles() != null && userInfo.getRoles().contains("ROLE_ADMIN")) {
                        displayRole = "管理员";
                        email = "admin@efak.ai";
                    }

                    model.addAttribute("userName", displayName);
                    model.addAttribute("userRole", displayRole);
                    model.addAttribute("userEmail", email);
                    model.addAttribute("lastLoginTime",
                            userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "未知");
                } else {
                    // 如果获取用户信息失败，使用默认值
                    setDefaultUserAttributes(model);
                }
            } else {
                // 如果未登录，使用默认值
                setDefaultUserAttributes(model);
            }
        } catch (Exception e) {
            // 如果出现异常，使用默认值
            setDefaultUserAttributes(model);
        }

        model.addAttribute("userInitials", "AD");
        model.addAttribute("hasNotifications", true);
        model.addAttribute("hasUserManagementPermission", true);
    }

    /**
     * 设置默认用户属性
     */
    private void setDefaultUserAttributes(Model model) {
        model.addAttribute("userName", "Admin");
        model.addAttribute("userRole", "管理员");
        model.addAttribute("userEmail", "admin@efak.ai");
        model.addAttribute("lastLoginTime", "未知");
    }
}
