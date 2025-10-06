package org.kafka.eagle.web.controller;

import org.kafka.eagle.web.service.BrokerService;
import org.kafka.eagle.dto.broker.*;
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Broker节点管理控制器
 * 提供Broker节点的查询、创建、更新、删除、状态管理等功能接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 00:01:36
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/brokers")
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private UserService userService;

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        UserInfo userInfo = userService.getUserByUsername(username);

        return userInfo != null && userInfo.getRoles() != null &&
                userInfo.getRoles().contains("ROLE_ADMIN");
    }

    /**
     * 分页查询Broker列表
     */
    @PostMapping("/query")
    public ResponseEntity<BrokerPageResponse> queryBrokers(@RequestBody BrokerQueryRequest request) {
        BrokerPageResponse response = brokerService.queryBrokers(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询Broker详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrokerInfo> getBrokerById(@PathVariable Long id) {
        BrokerInfo broker = brokerService.getBrokerById(id);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(broker);
    }

    /**
     * 根据Broker ID查询Broker信息
     */
    @GetMapping("/by-broker-id/{brokerId}")
    public ResponseEntity<BrokerInfo> getBrokerByBrokerId(@PathVariable Integer brokerId) {
        BrokerInfo broker = brokerService.getBrokerByBrokerId(brokerId);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(broker);
    }

    /**
     * 创建Broker
     */
    @PostMapping
    public ResponseEntity<String> createBroker(@RequestBody BrokerInfo broker) {
        // 权限检查：只有管理员可以创建Broker
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body("权限不足：只有管理员可以创建节点");
        }

        try {
            boolean success = brokerService.createBroker(broker);
            if (success) {
                return ResponseEntity.ok("Broker创建成功");
            } else {
                return ResponseEntity.badRequest().body("Broker创建失败");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新Broker
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBroker(@PathVariable Long id, @RequestBody BrokerInfo broker) {
        // 权限检查：只有管理员可以更新Broker
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body("权限不足：只有管理员可以编辑节点");
        }

        broker.setId(id);
        try {
            boolean success = brokerService.updateBroker(broker);
            if (success) {
                return ResponseEntity.ok("Broker更新成功");
            } else {
                return ResponseEntity.badRequest().body("Broker更新失败");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除Broker
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBroker(@PathVariable Long id) {
        // 权限检查：只有管理员可以删除Broker
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body("权限不足：只有管理员可以删除节点");
        }

        boolean success = brokerService.deleteBroker(id);
        if (success) {
            return ResponseEntity.ok("Broker删除成功");
        } else {
            return ResponseEntity.badRequest().body("Broker删除失败");
        }
    }

    /**
     * 更新Broker状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateBrokerStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) java.math.BigDecimal cpuUsage,
            @RequestParam(required = false) java.math.BigDecimal memoryUsage) {
        // 权限检查：只有管理员可以更新Broker状态（启动/停止/重启）
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(403).body("权限不足：只有管理员可以操作节点状态");
        }

        boolean success = brokerService.updateBrokerStatus(id, status, cpuUsage, memoryUsage, null);
        if (success) {
            return ResponseEntity.ok("状态更新成功");
        } else {
            return ResponseEntity.badRequest().body("状态更新失败");
        }
    }

    /**
     * 检查Broker连接状态
     */
    @GetMapping("/check-connection")
    public ResponseEntity<Map<String, Object>> checkBrokerConnection(
            @RequestParam String hostIp,
            @RequestParam Integer port) {
        boolean isOnline = brokerService.checkBrokerConnection(hostIp, port);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("hostIp", hostIp);
        response.put("port", port);
        response.put("isOnline", isOnline);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有Broker列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<BrokerInfo>> getAllBrokers() {
        BrokerQueryRequest request = new BrokerQueryRequest();
        request.setPage(1);
        request.setPageSize(1000); // 设置一个较大的值来获取所有数据
        BrokerPageResponse response = brokerService.queryBrokers(request);
        return ResponseEntity.ok(response.getBrokers());
    }

    /**
     * 获取集群统计信息
     */
    @GetMapping("/cluster/stats")
    public ResponseEntity<Map<String, Object>> getClusterStats(@RequestParam(required = false) String clusterId) {
        Map<String, Object> stats = brokerService.getClusterStats(clusterId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取集群概览信息
     */
    @GetMapping("/cluster/overview")
    public ResponseEntity<Map<String, Object>> getClusterOverview(@RequestParam(required = false) String clusterId) {
        Map<String, Object> overview = brokerService.getClusterStats(clusterId);
        return ResponseEntity.ok(overview);
    }


}