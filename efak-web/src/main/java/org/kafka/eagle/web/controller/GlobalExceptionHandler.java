package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * GlobalException 处理器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/08 00:59:25
 * @version 5.0.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("未捕获的异常: {}", e.getMessage(), e);

        // 检查是否是API请求
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.startsWith("/api/")) {
            // API请求返回JSON格式错误信息
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "error", "服务器内部错误",
                    "message", e.getMessage(),
                    "status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } else {
            // 页面请求返回错误页面
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("statusCode", 500);
            modelAndView.addObject("errorMessage", e.getMessage());
            modelAndView.addObject("exception", e);
            modelAndView.setViewName("view/error-500");
            return modelAndView;
        }
    }

    /**
     * 处理RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {}", e.getMessage(), e);

        // 检查是否是API请求
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.startsWith("/api/")) {
            // API请求返回JSON格式错误信息
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "error", "运行时错误",
                    "message", e.getMessage(),
                    "status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } else {
            // 页面请求返回错误页面
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("statusCode", 500);
            modelAndView.addObject("errorMessage", e.getMessage());
            modelAndView.addObject("exception", e);
            modelAndView.setViewName("view/error-500");
            return modelAndView;
        }
    }
}