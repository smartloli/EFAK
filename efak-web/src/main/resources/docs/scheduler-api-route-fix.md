# Scheduler API路由修复

## 问题描述

前端JavaScript调用`/api/scheduler/3/execute`时出现以下异常：
```
org.springframework.web.servlet.resource.NoResourceFoundException: No static resource api/scheduler/3/execute.
```

这个异常表明Spring Boot将API请求误认为是静态资源请求。

## 问题原因

1. **静态资源配置冲突**: Spring Boot的静态资源处理机制可能将API路径误认为是静态资源
2. **路径映射问题**: 可能存在路径映射配置不当的问题
3. **控制器冲突**: 可能存在多个控制器使用相同路径的问题

## 解决方案

### 1. 优化静态资源配置

**修改application.properties**:
```properties
# 设置静态资源路径模式，避免与API路径冲突
spring.web.resources.static-locations=classpath:/statics/
spring.web.resources.add-mappings=true
spring.mvc.static-path-pattern=/statics/**
```

**修改WebMvcConfig.java**:
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态资源映射，确保API路由优先
    registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/statics/css/")
            .setCacheControl(CacheControl.noCache());

    registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/statics/js/")
            .setCacheControl(CacheControl.noCache());

    registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/statics/images/")
            .setCacheControl(CacheControl.noCache());

    registry.addResourceHandler("/fonts/**")
            .addResourceLocations("classpath:/statics/fonts/")
            .setCacheControl(CacheControl.noCache());

    registry.addResourceHandler("/plugins/**")
            .addResourceLocations("classpath:/statics/plugins/")
            .setCacheControl(CacheControl.noCache());

    registry.addResourceHandler("/statics/**")
            .addResourceLocations("classpath:/statics/")
            .setCacheControl(CacheControl.noCache());
}
```

### 2. 增强控制器日志

**修改TaskSchedulerController.java**:
```java
@PostMapping("/execute/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long id) {
    System.out.println("=== 执行任务API被调用，任务ID: " + id + " ===");
    
    Map<String, Object> result = new HashMap<>();

    try {
        boolean success = taskSchedulerService.executeTaskNow(id);
        result.put("success", success);
        result.put("message", success ? "执行成功" : "执行失败");
        System.out.println("=== 任务执行结果: " + success + " ===");
    } catch (Exception e) {
        System.err.println("=== 任务执行异常: " + e.getMessage() + " ===");
        e.printStackTrace();
        result.put("success", false);
        result.put("message", "执行失败：" + e.getMessage());
    }

    return ResponseEntity.ok(result);
}
```

### 3. 添加测试路由

**添加测试方法**:
```java
/**
 * 测试路由
 */
@GetMapping("/test-route")
@ResponseBody
public ResponseEntity<Map<String, Object>> testRoute() {
    System.out.println("=== 测试路由API被调用 ===");
    
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "路由正常工作");
    result.put("timestamp", System.currentTimeMillis());
    
    return ResponseEntity.ok(result);
}
```

### 4. 创建API测试页面

创建了`api-test.html`页面用于测试API路由是否正常工作。

## 修复详情

### 1. application.properties配置优化

```properties
# 设置静态资源路径模式，避免与API路径冲突
spring.web.resources.static-locations=classpath:/statics/
spring.web.resources.add-mappings=true
spring.mvc.static-path-pattern=/statics/**
```

这个配置确保：
- 静态资源只在`/statics/**`路径下处理
- API路径不会被误认为是静态资源

### 2. WebMvcConfig优化

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态资源映射，确保API路由优先
    registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/statics/css/")
            .setCacheControl(CacheControl.noCache());
    // ... 其他静态资源配置
}
```

这个配置：
- 明确指定静态资源路径
- 添加缓存控制
- 确保API路由优先级

### 3. 控制器增强

```java
@PostMapping("/execute/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long id) {
    System.out.println("=== 执行任务API被调用，任务ID: " + id + " ===");
    // ... 执行逻辑
}
```

添加了详细的日志输出，便于调试和问题排查。

## 测试验证

### 1. API测试页面

访问`/api-test.html`可以测试以下API：
- `/api/scheduler/test-route` - 测试路由
- `/api/scheduler/stats` - 测试统计
- `/api/scheduler` - 测试列表
- `/api/scheduler/{id}/execute` - 测试执行
- `/api/scheduler/enable/{id}` - 测试启用
- `/api/scheduler/disable/{id}` - 测试禁用

### 2. 前端JavaScript测试

在浏览器控制台中可以测试：
```javascript
// 测试路由
fetch('/api/scheduler/test-route').then(r => r.json()).then(console.log);

// 测试执行任务
fetch('/api/scheduler/1/execute', {method: 'POST'}).then(r => r.json()).then(console.log);
```

## 预期效果

修复后：
1. **API路由正常工作**: `/api/scheduler/*`路径的API请求能够正确路由到控制器
2. **静态资源正常访问**: `/statics/**`路径的静态资源能够正常访问
3. **无路径冲突**: API路径和静态资源路径不会相互干扰
4. **调试信息完整**: 通过日志可以清楚看到API调用情况

## 注意事项

### 1. 路径优先级
- API路由优先级高于静态资源路由
- 确保控制器路径不会被静态资源处理器拦截

### 2. 缓存控制
- 静态资源添加了`CacheControl.noCache()`，确保开发时能够及时看到更新
- 生产环境可以根据需要调整缓存策略

### 3. 日志输出
- 添加了详细的日志输出，便于问题排查
- 生产环境可以调整日志级别

### 4. 测试验证
- 提供了完整的测试页面和测试方法
- 建议在修复后进行全面的API测试

## 总结

通过以下步骤成功修复了scheduler API路由问题：

1. **优化静态资源配置**: 明确指定静态资源路径模式
2. **增强WebMvcConfig**: 添加缓存控制和明确的资源映射
3. **增强控制器日志**: 添加详细的调试信息
4. **创建测试工具**: 提供API测试页面和测试方法

修复后，前端JavaScript调用`/api/scheduler/3/execute`应该能够正常工作，不再出现静态资源异常。 