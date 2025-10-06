/**
 * PerformanceController.java
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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 * Kafka性能监控Controller，用于EFAK Web界面
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/06/28 18:00:13
 * @version 5.0.0
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final Random random = new Random();

    /**
     * 获取Kafka性能概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getKafkaPerformanceOverview() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kafka吞吐量数据
            Map<String, Object> throughput = new HashMap<>();
            throughput.put("current", 25600 + random.nextInt(5000));
            throughput.put("average", 23100 + random.nextInt(3000));
            throughput.put("peak", 32400);
            throughput.put("efficiency", 92.3 + random.nextDouble() * 5);
            
            // 消息消费数据
            Map<String, Object> consume = new HashMap<>();
            consume.put("rate", 18900 + random.nextInt(3000));
            consume.put("lag", 125 + random.nextInt(50));
            consume.put("groups", 89 + random.nextInt(10));
            
            // 消息生产数据
            Map<String, Object> produce = new HashMap<>();
            produce.put("rate", 22400 + random.nextInt(4000));
            produce.put("latency", 12 + random.nextInt(8));
            produce.put("success", 99.8 + random.nextDouble() * 0.2);
            
            // Cluster状态数据
            Map<String, Object> cluster = new HashMap<>();
            cluster.put("nodes", 3);
            cluster.put("topics", 156 + random.nextInt(10));
            cluster.put("partitions", 468 + random.nextInt(20));
            cluster.put("activeTopics", 142);
            cluster.put("idleTopics", 14);
            cluster.put("errorTopics", 0);
            
            // 系统健康状态
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("score", 98.5 + random.nextDouble() * 1.5);
            health.put("uptime", "15 days 6 hours 32 minutes");
            health.put("brokerStatus", "All brokers running normally");
            
            response.put("success", true);
            response.put("throughput", throughput);
            response.put("consume", consume);
            response.put("produce", produce);
            response.put("cluster", cluster);
            response.put("health", health);
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get Kafka performance data: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取图表时间序列数据
     */
    @GetMapping("/charts")
    public ResponseEntity<Map<String, Object>> getChartData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 生成时间序列数据
            long now = System.currentTimeMillis();
            List<String> labels = new ArrayList<>();
            List<Integer> throughputData = new ArrayList<>();
            List<Integer> produceData = new ArrayList<>();
            List<Integer> consumeData = new ArrayList<>();
            List<Integer> produceLatencyData = new ArrayList<>();
            List<Integer> consumeLatencyData = new ArrayList<>();
            
            // 生成过去60分钟的数据
            for (int i = 59; i >= 0; i--) {
                long timestamp = now - i * 60 * 1000; // 每分钟一个数据点
                labels.add(new java.text.SimpleDateFormat("HH:mm").format(new Date(timestamp)));
                
                int baseValue = 20000;
                int variation = random.nextInt(10000);
                
                throughputData.add(baseValue + variation);
                produceData.add((int)((baseValue + variation) * 0.6));
                consumeData.add((int)((baseValue + variation) * 0.4));
                produceLatencyData.add(10 + random.nextInt(15));
                consumeLatencyData.add(100 + random.nextInt(100));
            }
            
            response.put("success", true);
            response.put("labels", labels);
            response.put("throughput", throughputData);
            response.put("produce", produceData);
            response.put("consume", consumeData);
            response.put("produceLatency", produceLatencyData);
            response.put("consumeLatency", consumeLatencyData);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get chart data: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 