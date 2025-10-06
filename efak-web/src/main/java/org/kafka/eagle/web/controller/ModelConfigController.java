package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.kafka.eagle.web.service.ModelConfigService;
import org.kafka.eagle.dto.config.ModelConfig;
import org.kafka.eagle.dto.config.ModelConfigStatisticsDTO;

import java.util.*;

/**
 * <p>
 * ModelConfig 控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/15 23:34:18
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/model-config")
public class ModelConfigController {

    @Autowired
    private ModelConfigService modelConfigService;

    /**
     * 分页查询模型配置列表
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getModelConfigList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String apiType,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        try {
            Map<String, Object> result = modelConfigService.getModelConfigList(page, size, search, apiType, enabled);

            // 转换ModelConfig对象为前端需要的格式
            List<ModelConfig> modelConfigList = (List<ModelConfig>) result.get("modelConfigs");
            List<Map<String, Object>> modelList = new ArrayList<>();

            for (ModelConfig modelConfig : modelConfigList) {
                Map<String, Object> modelMap = new HashMap<>();
                modelMap.put("id", modelConfig.getId());
                modelMap.put("modelName", modelConfig.getModelName());
                modelMap.put("apiType", modelConfig.getApiType());
                modelMap.put("endpoint", modelConfig.getEndpoint());
                modelMap.put("apiKey", modelConfig.getApiKey());
                modelMap.put("timeout", modelConfig.getTimeout());
                modelMap.put("description", modelConfig.getDescription());
                modelMap.put("systemPrompt", modelConfig.getSystemPrompt());
                modelMap.put("enabled", modelConfig.getEnabled());
                modelMap.put("status", modelConfig.getStatus());
                modelMap.put("createTime", modelConfig.getCreateTime());
                modelMap.put("updateTime", modelConfig.getUpdateTime());
                modelMap.put("createBy", modelConfig.getCreateBy());
                modelMap.put("updateBy", modelConfig.getUpdateBy());

                // 设置状态显示信息
                String statusDisplay = "离线";
                String statusClass = "bg-gray-100 text-gray-800";
                String statusIcon = "fa-times-circle";

                if (modelConfig.getStatus() != null) {
                    switch (modelConfig.getStatus()) {
                        case 0:
                            statusDisplay = "离线";
                            statusClass = "bg-gray-100 text-gray-800";
                            statusIcon = "fa-times-circle";
                            break;
                        case 1:
                            statusDisplay = "在线";
                            statusClass = "bg-green-100 text-green-800";
                            statusIcon = "fa-check-circle";
                            break;
                        case 2:
                            statusDisplay = "错误";
                            statusClass = "bg-red-100 text-red-800";
                            statusIcon = "fa-exclamation-circle";
                            break;
                        default:
                            statusDisplay = "未知";
                            statusClass = "bg-yellow-100 text-yellow-800";
                            statusIcon = "fa-question-circle";
                            break;
                    }
                }

                modelMap.put("statusDisplay", statusDisplay);
                modelMap.put("statusClass", statusClass);
                modelMap.put("statusIcon", statusIcon);

                // 设置启用状态显示信息
                String enabledDisplay = modelConfig.getEnabled() == 1 ? "启用" : "禁用";
                String enabledClass = modelConfig.getEnabled() == 1 ? "bg-green-100 text-green-800"
                        : "bg-gray-100 text-gray-800";
                String enabledIcon = modelConfig.getEnabled() == 1 ? "fa-check-circle" : "fa-times-circle";

                modelMap.put("enabledDisplay", enabledDisplay);
                modelMap.put("enabledClass", enabledClass);
                modelMap.put("enabledIcon", enabledIcon);

                modelList.add(modelMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("modelConfigs", modelList);
            response.put("total", result.get("total"));
            response.put("page", result.get("page"));
            response.put("size", result.get("size"));
            response.put("totalPages", result.get("totalPages"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取模型配置列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 根据ID查询模型配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModelConfigById(@PathVariable Long id) {
        try {
            ModelConfig modelConfig = modelConfigService.getModelConfigById(id);
            if (modelConfig == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置不存在");
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("modelConfig", modelConfig);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取模型配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 添加模型配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addModelConfig(@RequestBody Map<String, Object> modelData) {
        try {
            // 验证必填字段
            if (!modelData.containsKey("modelName")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必填字段：模型名称");
                return ResponseEntity.badRequest().body(error);
            }

            if (!modelData.containsKey("apiType")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必填字段：API类型");
                return ResponseEntity.badRequest().body(error);
            }

            if (!modelData.containsKey("endpoint")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必填字段：接口地址");
                return ResponseEntity.badRequest().body(error);
            }

            // 检查模型名称是否已存在
            ModelConfig existingModel = modelConfigService.getModelConfigByName((String) modelData.get("modelName"));
            if (existingModel != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型名称已存在");
                return ResponseEntity.badRequest().body(error);
            }

            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication != null ? authentication.getName() : "system";

            // 创建模型配置对象
            ModelConfig modelConfig = new ModelConfig();
            modelConfig.setModelName((String) modelData.get("modelName"));
            modelConfig.setApiType((String) modelData.get("apiType"));
            modelConfig.setEndpoint((String) modelData.get("endpoint"));
            modelConfig.setApiKey((String) modelData.get("apiKey"));
            modelConfig.setSystemPrompt((String) modelData.get("systemPrompt"));
            modelConfig.setTimeout((Integer) modelData.get("timeout"));
            modelConfig.setDescription((String) modelData.get("description"));
            modelConfig.setEnabled((Integer) modelData.get("enabled"));
            modelConfig.setCreateBy(currentUsername);
            modelConfig.setUpdateBy(currentUsername);

            boolean success = modelConfigService.addModelConfig(modelConfig);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "模型配置添加成功");
                response.put("modelConfig", modelConfig);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置添加失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "添加模型配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 更新模型配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateModelConfig(
            @PathVariable Long id,
            @RequestBody Map<String, Object> modelData) {

        try {
            ModelConfig existingModel = modelConfigService.getModelConfigById(id);
            if (existingModel == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置不存在");
                return ResponseEntity.notFound().build();
            }

            // 检查模型名称是否已被其他模型使用
            if (modelData.containsKey("modelName")) {
                ModelConfig modelWithSameName = modelConfigService
                        .getModelConfigByName((String) modelData.get("modelName"));
                if (modelWithSameName != null && !modelWithSameName.getId().equals(existingModel.getId())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "模型名称已存在");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication != null ? authentication.getName() : "system";

            // 更新模型配置
            if (modelData.containsKey("modelName")) {
                existingModel.setModelName((String) modelData.get("modelName"));
            }
            if (modelData.containsKey("apiType")) {
                existingModel.setApiType((String) modelData.get("apiType"));
            }
            if (modelData.containsKey("endpoint")) {
                existingModel.setEndpoint((String) modelData.get("endpoint"));
            }
            if (modelData.containsKey("apiKey")) {
                existingModel.setApiKey((String) modelData.get("apiKey"));
            }
            if (modelData.containsKey("systemPrompt")) {
                existingModel.setSystemPrompt((String) modelData.get("systemPrompt"));
            }
            if (modelData.containsKey("timeout")) {
                existingModel.setTimeout((Integer) modelData.get("timeout"));
            }
            if (modelData.containsKey("description")) {
                existingModel.setDescription((String) modelData.get("description"));
            }
            if (modelData.containsKey("enabled")) {
                existingModel.setEnabled((Integer) modelData.get("enabled"));
            }
            existingModel.setUpdateBy(currentUsername);

            boolean success = modelConfigService.updateModelConfig(existingModel);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "模型配置更新成功");
                response.put("modelConfig", existingModel);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置更新失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新模型配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 删除模型配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModelConfig(@PathVariable Long id) {
        try {
            ModelConfig modelConfig = modelConfigService.getModelConfigById(id);
            if (modelConfig == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置不存在");
                return ResponseEntity.notFound().build();
            }

            boolean success = modelConfigService.deleteModelConfig(id);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "模型配置删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置删除失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除模型配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 测试模型连接
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testModelConnection(@PathVariable Long id) {
        try {
            ModelConfig modelConfig = modelConfigService.getModelConfigById(id);
            if (modelConfig == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型配置不存在");
                return ResponseEntity.notFound().build();
            }

            boolean success = modelConfigService.testModelConnection(id);
            if (success) {
                // 更新状态为在线
                modelConfigService.updateModelStatus(id, 1);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "模型连接测试成功");
                response.put("status", 1);
                return ResponseEntity.ok(response);
            } else {
                // 更新状态为错误
                modelConfigService.updateModelStatus(id, 2);

                Map<String, Object> error = new HashMap<>();
                error.put("error", "模型连接测试失败");
                error.put("status", 2);
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "测试模型连接失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取所有启用的模型配置
     */
    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledModelConfigs() {
        try {
            List<ModelConfig> modelConfigs = modelConfigService.getEnabledModelConfigs();

            Map<String, Object> response = new HashMap<>();
            response.put("modelConfigs", modelConfigs);
            response.put("total", modelConfigs.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取启用的模型配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取模型配置统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getModelConfigStatistics() {
        try {

            ModelConfigStatisticsDTO statistics = modelConfigService.getModelConfigStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "获取模型配置统计数据成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取模型配置统计数据失败", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取模型配置统计数据失败: " + e.getMessage());
            error.put("data", new ModelConfigStatisticsDTO()); // 返回默认数据

            return ResponseEntity.status(500).body(error);
        }
    }
}