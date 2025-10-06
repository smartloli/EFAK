package org.kafka.eagle.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.kafka.eagle.dto.cluster.ClusterPageResponse;
import org.kafka.eagle.dto.cluster.ClusterQueryRequest;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;
import org.kafka.eagle.dto.cluster.CreateClusterRequest;
import org.kafka.eagle.web.service.ClusterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * Cluster集群管理控制器
 * 提供Kafka集群的创建、查询、更新、删除等管理功能接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/09 01:28:20
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/manager/cluster")
@RequiredArgsConstructor
@Slf4j
public class ClusterController {

    private final ClusterService clusterService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getClusterStats() {
        Map<String, Object> stats = clusterService.getClusterStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/list")
    public ResponseEntity<ClusterPageResponse> list(@RequestBody ClusterQueryRequest req) {
        ClusterPageResponse result = clusterService.list(req);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<KafkaClusterInfo> create(@RequestBody CreateClusterRequest request) {
        try {
            KafkaClusterInfo result = clusterService.create(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("创建集群失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<Map<String, Object>> getClusterByClusterId(@PathVariable String clusterId) {
        try {
            KafkaClusterInfo cluster = clusterService.findByClusterId(clusterId);
            Map<String, Object> response = new HashMap<>();
            if (cluster != null) {
                response.put("code", 200);
                response.put("message", "成功");
                response.put("data", cluster);
            } else {
                response.put("code", 404);
                response.put("message", "集群不存在");
                response.put("data", null);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取集群信息失败: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取集群信息失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Integer> update(@RequestBody KafkaClusterInfo info) {
        int updated = clusterService.update(info);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{clusterId}")
    public ResponseEntity<Map<String, Object>> updateByClusterId(@PathVariable String clusterId,
                                                                 @RequestBody KafkaClusterInfo info) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (info == null) {
                response.put("code", 400);
                response.put("message", "请求体不能为空");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            // 以路径参数为准
            info.setClusterId(clusterId);
            int updated = clusterService.update(info);
            if (updated > 0) {
                response.put("code", 200);
                response.put("message", "更新成功");
                response.put("data", updated);
            } else {
                response.put("code", 404);
                response.put("message", "未找到指定集群或无变更");
                response.put("data", updated);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新集群失败: {}", clusterId, e);
            response.put("code", 500);
            response.put("message", "更新集群失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        int deleted = clusterService.delete(id);
        return ResponseEntity.ok(deleted);
    }

    @DeleteMapping("/delete/by-cluster-id/{clusterId}")
    public ResponseEntity<Map<String, Object>> deleteByClusterId(@PathVariable String clusterId) {
        try {
            int deleted = clusterService.deleteByClusterId(clusterId);
            Map<String, Object> response = new HashMap<>();
            if (deleted > 0) {
                response.put("success", true);
                response.put("message", "集群删除成功");
                response.put("deletedCount", deleted);
            } else {
                response.put("success", false);
                response.put("message", "集群不存在或删除失败");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除集群失败: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除集群失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}