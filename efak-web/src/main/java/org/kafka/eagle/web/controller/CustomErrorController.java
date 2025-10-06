/**
 * <p>
 * 自定义错误页面控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/08 00:11:32
 * @version 5.0.0
 */
package org.kafka.eagle.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");

        // 记录错误信息
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "view/error-404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "view/error-500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "view/error-403";
            } else if (statusCode >= 500) {
                // 所有5xx错误都跳转到500页面
                return "view/error-500";
            }
        }

        // 默认跳转到500页面
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorMessage", "服务器内部错误");
        return "view/error-500";
    }

    @RequestMapping("/404")
    public String notFound() {
        return "view/error-404";
    }

    @RequestMapping("/500")
    public String serverError() {
        return "view/error-500";
    }

    @RequestMapping("/403")
    public String forbidden() {
        return "view/error-403";
    }

    @RequestMapping("/error-403")
    public String error403() {
        return "view/error-403";
    }
}