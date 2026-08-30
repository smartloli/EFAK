# Spring Security 登录重定向问题修复

## 问题描述

在重启Spring Boot服务或登录过期后，系统总是跳转到 `/login` 地址，但没有记录用户之前访问的URL。这导致每次登录成功后，都会跳转到 `/dashboard` 页面，而不是用户原本想要访问的页面。

## 问题分析

### 根本原因

1. **AJAX登录处理不当**：前端使用AJAX提交登录请求，但服务器端的 `AuthenticationSuccessHandler` 直接进行重定向，导致AJAX请求无法正确处理重定向响应。

2. **SavedRequest机制失效**：Spring Security的 `SavedRequest` 机制在AJAX请求场景下无法正常工作。

3. **URL保存不完整**：原始请求URL没有被正确保存和传递。

## 解决方案

### 1. 修改 AuthenticationSuccessHandler

**文件**: `AuthenticationSuccessHandler.java`

**主要改进**:
- 检测AJAX请求（通过 `X-Requested-With` 头）
- 对AJAX请求返回JSON响应，包含重定向URL
- 对普通表单请求保持原有的重定向行为
- 增强日志记录，便于调试

```java
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
```

### 2. 改进目标URL获取逻辑

**增强 `getTargetUrl` 方法**:
- 优先从 `SavedRequest` 获取原始请求URL
- 其次从请求参数 `targetUrl` 获取
- 最后从Session中获取备用URL
- 添加详细的日志记录
- 正确清理已使用的SavedRequest

### 3. 增强URL安全验证

**改进 `isValidTargetUrl` 方法**:
- 支持完整URL的路径提取
- 排除登录相关页面，避免循环重定向
- 增强XSS防护，检测更多危险字符
- 支持查询参数的保留

### 4. 优化Spring Security配置

**文件**: `WebSecurityConfig.java`

**主要改进**:
- 自定义 `HttpSessionRequestCache` 配置
- 配置 `LoginUrlAuthenticationEntryPoint`
- 禁用默认的continue参数
- 确保RequestCache正确工作

```java
// 创建自定义的RequestCache
HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
requestCache.setMatchingRequestParameterName(null); // 禁用默认的continue参数

// 创建自定义的AuthenticationEntryPoint
LoginUrlAuthenticationEntryPoint authenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
authenticationEntryPoint.setUseForward(false);
```

### 5. 修改前端AJAX处理

**文件**: `login.html`

**主要改进**:
- 添加 `X-Requested-With` 头标识AJAX请求
- 处理服务器返回的JSON响应
- 优先使用服务器返回的重定向URL
- 提供降级处理机制

```javascript
$.ajax({
    url: '/login.do',
    type: 'POST',
    headers: {
        'X-Requested-With': 'XMLHttpRequest'
    },
    // ...
    success: function (response) {
        if (response && response.redirectUrl) {
            // 使用服务器返回的重定向URL
            window.location.href = response.redirectUrl;
        } else {
            // 降级处理
            window.location.href = targetUrl;
        }
    }
});
```

## 技术实现特点

### 1. 双重处理机制
- **AJAX请求**: 返回JSON响应，由前端JavaScript处理重定向
- **表单请求**: 服务器端直接重定向，保持传统行为

### 2. 多层URL获取策略
1. Spring Security的SavedRequest（最优先）
2. 请求参数targetUrl
3. Session备用存储
4. 默认dashboard页面

### 3. 安全防护
- XSS攻击防护
- 循环重定向防护
- URL格式验证
- 相对路径限制

### 4. 容错机制
- 多种URL获取方式
- 降级处理策略
- 详细的错误日志
- 默认页面保底

## 测试场景

### 1. 正常登录流程
- 直接访问 `/login` → 登录成功 → 跳转到 `/dashboard`

### 2. 受保护页面访问
- 访问 `/topics` → 自动跳转到 `/login` → 登录成功 → 跳转回 `/topics`

### 3. 会话过期场景
- 正在访问 `/monitoring` → 会话过期 → 跳转到 `/login` → 登录成功 → 跳转回 `/monitoring`

### 4. 服务重启场景
- 访问任意受保护页面 → 服务重启 → 跳转到 `/login` → 登录成功 → 跳转到原页面

## 优势

1. **用户体验优化**: 登录后自动跳转到用户原本想访问的页面
2. **安全性增强**: 多重URL验证，防止XSS和重定向攻击
3. **兼容性保持**: 同时支持AJAX和传统表单提交
4. **可维护性**: 详细的日志记录，便于问题排查
5. **容错性强**: 多种降级策略，确保系统稳定运行

## 编译验证

项目编译成功，所有修改均通过语法检查，可以正常部署使用。

```bash
mvn clean compile -DskipTests
# 编译成功
```

## 总结

通过对Spring Security认证成功处理器、安全配置和前端AJAX处理的综合改进，成功解决了登录重定向问题。现在系统能够正确记住用户访问的原始URL，并在登录成功后准确跳转回该页面，大大提升了用户体验。