/**
 * AuthenticationSuccessHandler.java
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
package org.kafka.eagle.web.security.handle;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 自定义登录成功处理类
 *
 * @Author: smartloli
 * @Date: 2023/7/2 18:53
 * @Version: 3.4.0
 */
@Slf4j
@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        String remoteAddr = request.getRemoteAddr();
        HttpSession session = request.getSession();
        session.setAttribute("remoteAddr", remoteAddr);
        session.setAttribute("loginTime", System.currentTimeMillis());

        // 更新用户最后登录时间
        try {
            String username = authentication.getName();
            UserInfo userInfo = userService.getUserByUsername(username);
            if (userInfo != null) {
                userInfo.setModifyTime(LocalDateTime.now());
                userService.updateUser(userInfo);
            }
        } catch (Exception e) {
            log.warn("更新用户最后登录时间失败: {}", e.getMessage());
        }

        // 检查是否是AJAX请求
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // AJAX请求，返回JSON响应包含重定向URL
            String targetUrl = getTargetUrl(request, response);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"redirectUrl\":\"" + targetUrl + "\"}");
        } else {
            // 普通表单提交，直接重定向
            String targetUrl = getTargetUrl(request, response);
            response.sendRedirect(targetUrl);
        }
    }

    /**
     * 确定目标URL
     */
    private String getTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // 首先尝试从SavedRequest中获取
        RequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);

        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            if (redirectUrl != null && !redirectUrl.trim().isEmpty() && isValidTargetUrl(redirectUrl)) {
                // 清除已使用的SavedRequest
                cache.removeRequest(request, response);
                return redirectUrl;
            }
        }

        // 如果没有SavedRequest，尝试从URL参数中获取
        String targetUrl = request.getParameter("targetUrl");
        if (targetUrl != null && !targetUrl.trim().isEmpty() && isValidTargetUrl(targetUrl)) {
            return targetUrl;
        }

        // 尝试从session中获取原始请求URL
        HttpSession session = request.getSession(false);
        if (session != null) {
            String originalUrl = (String) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            if (originalUrl != null && !originalUrl.trim().isEmpty() && isValidTargetUrl(originalUrl)) {
                // 清除session中的URL
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
                return originalUrl;
            }
        }

        // 默认跳转到首页
        log.info("使用默认重定向URL: /dashboard");
        return "/dashboard";
    }

    /**
     * 验证目标URL的安全性
     */
    private boolean isValidTargetUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 如果是完整URL，提取路径部分
        String targetPath = url;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                java.net.URL urlObj = new java.net.URL(url);
                targetPath = urlObj.getPath();
                // 如果有查询参数，也包含进来
                if (urlObj.getQuery() != null) {
                    targetPath += "?" + urlObj.getQuery();
                }
            } catch (Exception e) {
                log.warn("解析URL失败: {}", url);
                return false;
            }
        }

        // 检查是否以/开头（相对路径）
        if (!targetPath.startsWith("/")) {
            return false;
        }

        // 排除登录相关页面，避免循环重定向
        if (targetPath.equals("/login") || targetPath.startsWith("/login?") || 
            targetPath.equals("/logout") || targetPath.startsWith("/logout?")) {
            return false;
        }

        // 检查是否包含危险字符
        String lowerUrl = targetPath.toLowerCase();
        if (lowerUrl.contains("javascript:") ||
                lowerUrl.contains("data:") ||
                lowerUrl.contains("vbscript:") ||
                lowerUrl.contains("onload=") ||
                lowerUrl.contains("onerror=") ||
                lowerUrl.contains("<script") ||
                lowerUrl.contains("</script")) {
            return false;
        }

        return true;
    }

}