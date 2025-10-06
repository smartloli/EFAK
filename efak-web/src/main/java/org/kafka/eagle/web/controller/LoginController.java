package org.kafka.eagle.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * Login 控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/20 21:19:38
 * @version 5.0.0
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "targetUrl", required = false) String targetUrl,
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (targetUrl != null && !targetUrl.isEmpty()) {
            // 验证URL安全性，只允许相对路径
            if (isValidTargetUrl(targetUrl)) {
                model.addAttribute("targetUrl", targetUrl);
            }
        }

        // 处理错误信息
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }

        return "view/login";
    }

    /**
     * 验证目标URL的安全性
     * 只允许相对路径，防止重定向攻击
     */
    private boolean isValidTargetUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 检查是否以/开头（相对路径）
        if (!url.startsWith("/")) {
            return false;
        }

        // 检查是否包含危险字符
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("javascript:") ||
                lowerUrl.contains("data:") ||
                lowerUrl.contains("vbscript:") ||
                lowerUrl.contains("onload=") ||
                lowerUrl.contains("onerror=")) {
            return false;
        }

        return true;
    }

    @GetMapping("/default")
    public String defaultPage() {
        return "view/dashboard";
    }

    @GetMapping("/password-tool")
    public String passwordToolPage() {
        return "view/password-tool";
    }

    @GetMapping("/test-login-redirect")
    public String testLoginRedirectPage() {
        return "view/test-login-redirect";
    }

}
