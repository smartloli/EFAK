package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.api.KafkaSchemaFactory;
import org.kafka.eagle.core.api.KafkaStoragePlugin;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;
import org.kafka.eagle.dto.consumer.*;
import org.kafka.eagle.web.mapper.ConsumerGroupTopicMapper;
import org.kafka.eagle.web.service.BrokerService;
import org.kafka.eagle.web.service.ConsumerGroupTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * <p>
 * 消费者组主题数据采集服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 13:30:04
 * @version 5.0.0
 */
@Slf4j
@Service
public class ConsumerGroupTopicServiceImpl implements ConsumerGroupTopicService {

    @Autowired
    private ConsumerGroupTopicMapper consumerGroupTopicMapper;

    @Autowired
    private BrokerService brokerService;

    @Override
    public ConsumerGroupTopicPageResponse getConsumerGroupTopicPage(ConsumerGroupTopicQueryRequest request) {
        if (request == null) {
            return new ConsumerGroupTopicPageResponse(List.of(), 0L, 1, 10);
        }

        Integer page = request.getPage() != null ? request.getPage() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 10;

        // 查询总数
        Long total = consumerGroupTopicMapper.countConsumerGroupTopic(request);
        if (total == null || total == 0) {
            return new ConsumerGroupTopicPageResponse(List.of(), 0L, page, pageSize);
        }

        // 分页查询
        int offset = (page - 1) * pageSize;
        List<ConsumerGroupTopicInfo> list = consumerGroupTopicMapper.selectConsumerGroupTopicPage(request, offset, pageSize);

        return new ConsumerGroupTopicPageResponse(list, total, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertConsumerGroupTopic(ConsumerGroupTopicInsertRequest request) {
        try {
            if (request == null) {
                log.error("插入消费者组主题数据失败：请求参数不能为空");
                return false;
            }

            // 参数校验
            if (request.getClusterId() == null || request.getClusterId().trim().isEmpty()) {
                log.error("插入消费者组主题数据失败：集群ID不能为空");
                return false;
            }

            if (request.getGroupId() == null || request.getGroupId().trim().isEmpty()) {
                log.error("插入消费者组主题数据失败：消费者组ID不能为空");
                return false;
            }

            if (request.getTopicName() == null || request.getTopicName().trim().isEmpty()) {
                log.error("插入消费者组主题数据失败：主题名称不能为空");
                return false;
            }

            int result = consumerGroupTopicMapper.insertConsumerGroupTopic(request);
            if (result > 0) {
                return true;
            } else {
                log.error("插入消费者组主题数据失败：数据库操作返回0");
                return false;
            }

        } catch (Exception e) {
            log.error("插入消费者组主题数据失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchInsertConsumerGroupTopic(List<ConsumerGroupTopicInsertRequest> requests) {
        try {
            if (requests == null || requests.isEmpty()) {
                log.error("批量插入消费者组主题数据失败：请求参数不能为空");
                return false;
            }

            // 参数校验
            for (ConsumerGroupTopicInsertRequest request : requests) {
                if (request.getClusterId() == null || request.getClusterId().trim().isEmpty() ||
                    request.getGroupId() == null || request.getGroupId().trim().isEmpty() ||
                    request.getTopicName() == null || request.getTopicName().trim().isEmpty()) {
                    log.error("批量插入消费者组主题数据失败：存在无效的请求参数");
                    return false;
                }
            }

            int result = consumerGroupTopicMapper.batchInsertConsumerGroupTopic(requests);
            if (result > 0) {
                return true;
            } else {
                log.error("批量插入消费者组主题数据失败：数据库操作返回0");
                return false;
            }

        } catch (Exception e) {
            log.error("批量插入消费者组主题数据失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ConsumerGroupTopicInfo getConsumerGroupTopicById(Long id) {
        if (id == null) {
            log.error("根据ID查询消费者组主题数据失败：ID不能为空");
            return null;
        }

        try {
            return consumerGroupTopicMapper.selectConsumerGroupTopicById(id);
        } catch (Exception e) {
            log.error("根据ID查询消费者组主题数据失败：{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanDataBeforeDay(String beforeDay) {
        try {
            if (beforeDay == null || beforeDay.trim().isEmpty()) {
                log.error("清理消费者组主题数据失败：日期参数不能为空");
                return false;
            }

            // 验证日期格式（YYYY-MM-DD）
            if (!beforeDay.matches("\\d{4}-\\d{2}-\\d{2}")) {
                log.error("清理消费者组主题数据失败：日期格式错误，应为YYYY-MM-DD格式，输入：{}", beforeDay);
                return false;
            }

            int result = consumerGroupTopicMapper.deleteDataBeforeDate(beforeDay);
            return true;

        } catch (Exception e) {
            log.error("清理消费者组主题数据失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByClusterId(String clusterId) {
        try {
            if (clusterId == null || clusterId.trim().isEmpty()) {
                log.error("根据集群ID删除消费者组主题数据失败：集群ID不能为空");
                return false;
            }

            int result = consumerGroupTopicMapper.deleteByClusterId(clusterId);
            return true;

        } catch (Exception e) {
            log.error("根据集群ID删除消费者组主题数据失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getConsumerStats(String clusterId) {
        Map<String, Object> stats = new HashMap<>();
        String collectDate = getCurrentDate();

        try {
            // 1. 获取消费者组总数
            Integer totalGroups = consumerGroupTopicMapper.getTotalGroups(clusterId, collectDate);
            stats.put("totalGroups", totalGroups != null ? totalGroups : 0);

            // 2. 获取活跃消费者组数（state=STABLE）
            Integer activeGroups = consumerGroupTopicMapper.getActiveGroups(clusterId, collectDate);
            stats.put("activeGroups", activeGroups != null ? activeGroups : 0);

            // 3. 获取空闲消费者组数（state=EMPTY）
            Integer inactiveGroups = consumerGroupTopicMapper.getInactiveGroups(clusterId, collectDate);
            stats.put("inactiveGroups", inactiveGroups != null ? inactiveGroups : 0);

            // 4. 计算平均延迟率
            String avgLagRate = calculateAvgLagRate(clusterId, collectDate);
            stats.put("avgLagRate", avgLagRate);

            // 5. 计算平均消费速度
            String avgConsumerRate = calculateAvgConsumerRate(clusterId, collectDate);
            stats.put("avgConsumerRate", avgConsumerRate);

            // 6. 计算慢速和延迟消费者组数
            Map<String, Integer> groupCounts = calculateGroupCounts(clusterId, collectDate);
            stats.put("slowGroups", groupCounts.get("slowGroups"));
            stats.put("laggingGroups", groupCounts.get("laggingGroups"));

        } catch (Exception e) {
            log.error("获取集群 {} 消费者组统计信息失败: {}", clusterId, e.getMessage(), e);
            // 出现异常时返回默认值
            stats.put("totalGroups", 0);
            stats.put("activeGroups", 0);
            stats.put("inactiveGroups", 0);
            stats.put("avgLagRate", "0.00");
            stats.put("avgConsumerRate", "0.00");
            stats.put("slowGroups", 0);
            stats.put("laggingGroups", 0);
        }

        return stats;
    }

    /**
     * 计算平均延迟率
     */
    private String calculateAvgLagRate(String clusterId, String collectDate) {
        try {
            Map<String, Object> data = consumerGroupTopicMapper.getLatestLagsAndLogsize(clusterId, collectDate);
            if (data != null) {
                Long totalLags = ((Number) data.get("totalLags")).longValue();
                Long totalLogsize = ((Number) data.get("totalLogsize")).longValue();

                if (totalLogsize > 0) {
                    BigDecimal lagRate = BigDecimal.valueOf(totalLags)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalLogsize), 2, RoundingMode.HALF_UP);
                    return lagRate.toString();
                }
            }
        } catch (Exception e) {
        }
        return "0.00";
    }

    /**
     * 计算平均消费速度
     */
    private String calculateAvgConsumerRate(String clusterId, String collectDate) {
        try {
            List<Map<String, Object>> offsetsData = consumerGroupTopicMapper.getOffsetsForConsumerRate(clusterId, collectDate);
            if (offsetsData == null || offsetsData.size() < 2) {
                return "0.00";
            }

            // 找出最近的两个不同的时间点
            Set<Long> distinctTimes = new LinkedHashSet<>();
            for (Map<String, Object> record : offsetsData) {
                long timestamp = getTimestamp(record.get("collect_time"));
                distinctTimes.add(timestamp);
                if (distinctTimes.size() >= 2) {
                    break; // 只需要最近的两个时间点
                }
            }

            if (distinctTimes.size() < 2) {
                return "0.00";
            }

            // 转换为List以便按索引访问
            List<Long> timesList = new ArrayList<>(distinctTimes);
            Long latestTime = timesList.get(0);
            Long previousTime = timesList.get(1);

            // 按 group_id + topic 分组，只保留最近两个时间点的数据
            Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();
            for (Map<String, Object> record : offsetsData) {
                long timestamp = getTimestamp(record.get("collect_time"));
                if (timestamp == latestTime || timestamp == previousTime) {
                    String key = record.get("group_id") + ":" + record.get("topic");
                    groupedData.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
                }
            }

            BigDecimal totalRate = BigDecimal.ZERO;
            int validGroups = 0;

            for (List<Map<String, Object>> records : groupedData.values()) {
                if (records.size() >= 2) {
                    // 按时间排序，处理LocalDateTime和Date两种类型
                    records.sort((a, b) -> {
                        long timeA = getTimestamp(a.get("collect_time"));
                        long timeB = getTimestamp(b.get("collect_time"));
                        return Long.compare(timeB, timeA); // 降序排列，最新的在前
                    });

                    Map<String, Object> current = records.get(0); // 最新的数据
                    Map<String, Object> previous = records.get(1); // 前一个时间点的数据

                    Long currentOffsets = ((Number) current.get("offsets")).longValue();
                    Long previousOffsets = ((Number) previous.get("offsets")).longValue();

                    long timeDiffMs = getTimestamp(current.get("collect_time")) - getTimestamp(previous.get("collect_time"));
                    if (timeDiffMs > 0) {
                        long offsetDiff = currentOffsets - previousOffsets;
                        // 转换为每分钟的速率
                        BigDecimal rate = BigDecimal.valueOf(offsetDiff)
                                .multiply(BigDecimal.valueOf(60000))
                                .divide(BigDecimal.valueOf(timeDiffMs), 2, RoundingMode.HALF_UP);
                        totalRate = totalRate.add(rate.max(BigDecimal.ZERO));
                        validGroups++;
                    }
                }
            }

            if (validGroups > 0) {
                BigDecimal avgRate = totalRate.divide(BigDecimal.valueOf(validGroups), 2, RoundingMode.HALF_UP);
                return avgRate.toString();
            }

        } catch (Exception e) {
        }
        return "0.00";
    }

    /**
     * 从时间对象获取时间戳，兼容LocalDateTime和Date
     */
    private long getTimestamp(Object timeObj) {
        if (timeObj instanceof LocalDateTime) {
            return ((LocalDateTime) timeObj).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else if (timeObj instanceof Date) {
            return ((Date) timeObj).getTime();
        } else {
            log.warn("未知的时间类型: {}", timeObj != null ? timeObj.getClass() : "null");
            return System.currentTimeMillis();
        }
    }

    /**
     * 计算慢速和延迟消费者组数量
     */
    private Map<String, Integer> calculateGroupCounts(String clusterId, String collectDate) {
        Map<String, Integer> result = new HashMap<>();
        result.put("slowGroups", 0);
        result.put("laggingGroups", 0);

        try {
            List<Map<String, Object>> lagRates = consumerGroupTopicMapper.getGroupTopicLagRates(clusterId, collectDate);
            if (lagRates != null && !lagRates.isEmpty()) {
                Set<String> slowGroupSet = new HashSet<>();
                Set<String> laggingGroupSet = new HashSet<>();

                for (Map<String, Object> record : lagRates) {
                    String groupId = (String) record.get("group_id");
                    BigDecimal lagRate = (BigDecimal) record.get("lag_rate");

                    if (lagRate != null) {
                        if (lagRate.compareTo(BigDecimal.valueOf(10)) > 0) {
                            // lag_rate > 10% 为延迟消费者组
                            laggingGroupSet.add(groupId);
                        } else if (lagRate.compareTo(BigDecimal.valueOf(0)) > 0 && lagRate.compareTo(BigDecimal.valueOf(10)) <= 0) {
                            // 0 < lag_rate <= 10% 为缓慢消费者组
                            slowGroupSet.add(groupId);
                        }
                    }
                }

                result.put("slowGroups", slowGroupSet.size());
                result.put("laggingGroups", laggingGroupSet.size());
            }
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * 获取当前日期字符串
     */
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    @Override
    public List<Map<String, Object>> getIdleGroupsTrend(String clusterId, String timeRange) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        try {
            // 解析时间范围，默认为24小时
            int hours = parseTimeRange(timeRange);

            // 查询数据库获取趋势数据
            List<Map<String, Object>> rawData = consumerGroupTopicMapper.getIdleGroupsTrend(clusterId, hours);

            if (rawData != null && !rawData.isEmpty()) {
                // 对数据进行采样处理，避免X轴过于密集
                List<Map<String, Object>> sampledData = sampleTrendData(rawData, 24); // 最多显示24个点

                for (Map<String, Object> record : sampledData) {
                    Map<String, Object> point = new HashMap<>();

                    // 处理时间字段，支持LocalDateTime和Date两种类型
                    Object collectTimeObj = record.get("collect_time");
                    long timestamp;

                    if (collectTimeObj instanceof LocalDateTime) {
                        LocalDateTime localDateTime = (LocalDateTime) collectTimeObj;
                        timestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    } else if (collectTimeObj instanceof Date) {
                        timestamp = ((Date) collectTimeObj).getTime();
                    } else {
                        // 如果时间字段为空或类型不匹配，使用当前时间
                        timestamp = System.currentTimeMillis();
                        log.warn("无法解析时间字段，使用当前时间: {}", collectTimeObj);
                    }

                    point.put("timestamp", timestamp);
                    point.put("timePoint", (String) record.get("time_point"));
                    point.put("activeGroups", ((Number) record.getOrDefault("active_groups", 0)).intValue());
                    point.put("idleGroups", ((Number) record.getOrDefault("idle_groups", 0)).intValue());
                    trendData.add(point);
                }
            }

        } catch (Exception e) {
            log.error("获取集群 {} 消费者组趋势数据失败: {}", clusterId, e.getMessage(), e);
        }

        return trendData;
    }

    /**
     * 解析时间范围参数
     */
    private int parseTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return 24; // 默认24小时
        }

        timeRange = timeRange.toLowerCase().trim();
        if (timeRange.endsWith("h")) {
            try {
                return Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
            } catch (NumberFormatException e) {
                return 24;
            }
        } else if (timeRange.endsWith("d")) {
            try {
                int days = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
                return days * 24;
            } catch (NumberFormatException e) {
                return 24;
            }
        } else if (timeRange.equals("1d")) {
            return 24;
        } else {
            return 24;
        }
    }

    /**
     * 对趋势数据进行采样，避免X轴过于密集
     */
    private List<Map<String, Object>> sampleTrendData(List<Map<String, Object>> rawData, int maxPoints) {
        if (rawData.size() <= maxPoints) {
            return rawData;
        }

        List<Map<String, Object>> sampledData = new ArrayList<>();
        int step = rawData.size() / maxPoints;

        for (int i = 0; i < rawData.size(); i += step) {
            sampledData.add(rawData.get(i));
        }

        // 确保包含最后一个数据点
        if (!sampledData.isEmpty() && !sampledData.get(sampledData.size() - 1).equals(rawData.get(rawData.size() - 1))) {
            sampledData.add(rawData.get(rawData.size() - 1));
        }

        return sampledData;
    }

    @Override
    public ConsumerGroupPageResponse getConsumerGroupsList(String clusterId, String search, int page, int pageSize) {
        try {
            String collectDate = getCurrentDate();

            // 查询总数
            Long total = consumerGroupTopicMapper.countConsumerGroups(clusterId, collectDate, search);
            if (total == null || total == 0) {
                return new ConsumerGroupPageResponse(new ArrayList<>(), 0L, page, pageSize);
            }

            // 分页查询
            int offset = (page - 1) * pageSize;
            List<Map<String, Object>> rawData = consumerGroupTopicMapper.getConsumerGroupsList(
                    clusterId, collectDate, search, offset, pageSize);

            List<ConsumerGroupInfo> consumerGroups = new ArrayList<>();
            for (Map<String, Object> record : rawData) {
                ConsumerGroupInfo info = convertToConsumerGroupInfo(record);
                consumerGroups.add(info);
            }

            return new ConsumerGroupPageResponse(consumerGroups, total, page, pageSize);

        } catch (Exception e) {
            log.error("获取集群 {} 消费者组列表失败: {}", clusterId, e.getMessage(), e);
            return new ConsumerGroupPageResponse(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    /**
     * 转换数据库记录为ConsumerGroupInfo对象
     */
    private ConsumerGroupInfo convertToConsumerGroupInfo(Map<String, Object> record) {
        ConsumerGroupInfo info = new ConsumerGroupInfo();

        info.setGroupId((String) record.get("group_id"));
        info.setTopicName((String) record.get("topic_name"));
        info.setState((String) record.get("state"));
        info.setTotalLag(((Number) record.getOrDefault("total_lag", 0)).longValue());
        info.setLogsize(((Number) record.getOrDefault("logsize", 0)).longValue());

        // 处理延迟率
        BigDecimal lagRate = (BigDecimal) record.get("lag_rate");
        if (lagRate == null) {
            lagRate = BigDecimal.ZERO;
        }
        info.setLagRate(lagRate);

        // 计算延迟等级
        String lagLevel = calculateLagLevel(lagRate);
        info.setLagLevel(lagLevel);

        // 处理采集时间
        Object collectTimeObj = record.get("collect_time");
        if (collectTimeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) collectTimeObj;
            Date collectTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            info.setCollectTime(collectTime);
        } else if (collectTimeObj instanceof Date) {
            info.setCollectTime((Date) collectTimeObj);
        }

        return info;
    }

    /**
     * 计算延迟等级
     *
     * @param lagRate 延迟率
     * @return 延迟等级（低/缓慢/延迟）
     */
    private String calculateLagLevel(BigDecimal lagRate) {
        if (lagRate == null) {
            return "低";
        }

        if (lagRate.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "延迟";
        } else if (lagRate.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "缓慢";
        } else {
            return "低";
        }
    }

    @Override
    public Map<String, Object> getConsumerGroupDetail(String clusterId, String groupId, String topic) {
        Map<String, Object> detailInfo = new HashMap<>();

        try {
            // 获取当天日期
            String collectDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 获取当天的所有记录用于计算详细指标
            List<Map<String, Object>> todayRecords = consumerGroupTopicMapper.getTodayRecordsForDetail(clusterId, groupId, topic, collectDate);

            // 获取最新状态
            Map<String, Object> latestStateRecord = consumerGroupTopicMapper.getLatestStateForDetail(clusterId, groupId, topic);

            if (!todayRecords.isEmpty() && latestStateRecord != null) {

                // 1. 获取最新状态
                String state = (String) latestStateRecord.get("state");
                Object lastUpdated = latestStateRecord.get("collect_time");

                // 2. 计算平均延迟率
                String avgLagRate = calculateAverageLagRate(todayRecords);

                // 3. 计算平均消费速率
                String avgConsumptionRate = calculateAverageConsumptionRate(todayRecords);

                // 4. 获取最新的总延迟
                long totalLag = getTotalLagFromLatestRecord(todayRecords);

                // 设置详细信息
                detailInfo.put("state", getStateDisplayText(state));
                detailInfo.put("avgOffsetRate", avgConsumptionRate + "/min"); // 将平均消费速率显示在avgOffsetRate字段
                detailInfo.put("totalLag", totalLag);
                detailInfo.put("avgLagRate", avgLagRate);
                detailInfo.put("lastUpdated", lastUpdated);
            } else {
                // 返回默认值
                detailInfo.put("state", "-");
                detailInfo.put("avgOffsetRate", "-");
                detailInfo.put("totalLag", 0);
                detailInfo.put("avgLagRate", "0.00%");
                detailInfo.put("lastUpdated", null);

                log.warn("消费者组 {} 当天无数据，返回默认值", groupId);
            }

        } catch (Exception e) {
            log.error("获取消费者组 {} 详细信息失败: {}", groupId, e.getMessage(), e);
            // 返回默认值
            detailInfo.put("state", "-");
            detailInfo.put("avgOffsetRate", "-");
            detailInfo.put("totalLag", 0);
            detailInfo.put("avgLagRate", "0.00%");
            detailInfo.put("lastUpdated", null);
        }

        return detailInfo;
    }

    /**
     * 计算平均延迟率
     * 公式：lags / logsize = 延迟率
     */
    private String calculateAverageLagRate(List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            return "0.00%";
        }

        long totalLags = 0;
        long totalLogsize = 0;
        int validRecords = 0;

        for (Map<String, Object> record : records) {
            Long lags = ((Number) record.getOrDefault("lags", 0L)).longValue();
            Long logsize = ((Number) record.getOrDefault("logsize", 0L)).longValue();

            if (logsize > 0) { // 只考虑logsize大于0的记录
                totalLags += lags;
                totalLogsize += logsize;
                validRecords++;
            }
        }

        if (validRecords == 0 || totalLogsize == 0) {
            return "0.00%";
        }

        // 计算平均延迟率
        double avgLagRate = (double) totalLags / totalLogsize * 100;
        return String.format("%.2f%%", Math.max(0, avgLagRate));
    }

    /**
     * 计算平均消费速率
     * 公式：(offsets[t] - offsets[t-1]) / (collect_time[t] - collect_time[t-1])
     */
    private String calculateAverageConsumptionRate(List<Map<String, Object>> records) {
        if (records.size() < 2) {
            return "0.00";
        }

        double totalRate = 0;
        int validCalculations = 0;

        for (int i = 1; i < records.size(); i++) {
            Map<String, Object> currentRecord = records.get(i);
            Map<String, Object> previousRecord = records.get(i - 1);

            Long currentOffsets = ((Number) currentRecord.getOrDefault("offsets", 0L)).longValue();
            Long previousOffsets = ((Number) previousRecord.getOrDefault("offsets", 0L)).longValue();

            Object currentTimeObj = currentRecord.get("collect_time");
            Object previousTimeObj = previousRecord.get("collect_time");

            if (currentTimeObj != null && previousTimeObj != null) {
                try {
                    long currentTime = getTimestampFromObject(currentTimeObj);
                    long previousTime = getTimestampFromObject(previousTimeObj);

                    long timeDiffSeconds = (currentTime - previousTime) / 1000;
                    long offsetsDiff = currentOffsets - previousOffsets;

                    if (timeDiffSeconds > 0 && offsetsDiff >= 0) {
                        double rate = (double) offsetsDiff / timeDiffSeconds;
                        totalRate += rate;
                        validCalculations++;
                    }
                } catch (Exception e) {
                    log.warn("计算消费速率时时间转换失败: {}", e.getMessage());
                }
            }
        }

        if (validCalculations == 0) {
            return "0.00";
        }

        double avgRate = totalRate / validCalculations;
        return String.format("%.2f", Math.max(0, avgRate));
    }

    /**
     * 从时间对象获取时间戳
     */
    private long getTimestampFromObject(Object timeObj) {
        if (timeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        } else if (timeObj instanceof Date) {
            Date date = (Date) timeObj;
            return date.getTime();
        } else if (timeObj instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) timeObj;
            return timestamp.getTime();
        }
        throw new IllegalArgumentException("Unsupported time object type: " + timeObj.getClass());
    }

    /**
     * 获取最新记录的总延迟
     */
    private long getTotalLagFromLatestRecord(List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            return 0;
        }

        // 获取最后一条记录（最新的）
        Map<String, Object> latestRecord = records.get(records.size() - 1);
        return ((Number) latestRecord.getOrDefault("lags", 0L)).longValue();
    }

    /**
     * 获取状态显示文本
     */
    private String getStateDisplayText(String state) {
        if (state == null) {
            return "未知";
        }

        switch (state.toUpperCase()) {
            case "EMPTY":
                return "空闲";
            case "STABLE":
                return "活跃";
            case "DEAD":
                return "已删除";
            default:
                return state;
        }
    }

    @Override
    public Map<String, Object> getConsumerGroupSpeed(String clusterId, String groupId, String topic) {
        Map<String, Object> speedData = new HashMap<>();

        try {
            // 获取最新的2条记录用于计算速度
            List<Map<String, Object>> records = consumerGroupTopicMapper.getLatestTwoRecordsForSpeed(clusterId, groupId, topic);

            if (records.size() >= 2) {
                Map<String, Object> latest = records.get(0);  // 最新第1条记录
                Map<String, Object> previous = records.get(1); // 最新第2条记录

                // 获取logsize和offsets值
                Long latestLogsize = ((Number) latest.getOrDefault("logsize", 0L)).longValue();
                Long previousLogsize = ((Number) previous.getOrDefault("logsize", 0L)).longValue();
                Long latestOffsets = ((Number) latest.getOrDefault("offsets", 0L)).longValue();
                Long previousOffsets = ((Number) previous.getOrDefault("offsets", 0L)).longValue();

                // 计算差值
                long logsizeDiff = latestLogsize - previousLogsize;
                long offsetsDiff = latestOffsets - previousOffsets;

                // 计算速度（差值/60秒）
                double writeSpeed = logsizeDiff / 60.0;  // 写入速度：logsize差值/60s
                double readSpeed = offsetsDiff / 60.0;   // 读取速度：offsets差值/60s

                // 确保速度不为负数
                writeSpeed = Math.max(0, writeSpeed);
                readSpeed = Math.max(0, readSpeed);

                speedData.put("writeSpeed", String.format("%.2f", writeSpeed)); // 写入速度（记录/秒）
                speedData.put("readSpeed", String.format("%.2f", readSpeed));   // 读取速度（记录/秒）
            } else {
                // 数据不足，返回0
                speedData.put("writeSpeed", 0L);
                speedData.put("readSpeed", 0L);

                log.warn("消费者组 {} 数据不足，无法计算速度，记录数量: {}", groupId, records.size());
            }

        } catch (Exception e) {
            log.error("获取消费者组 {} 速度数据失败: {}", groupId, e.getMessage(), e);
            speedData.put("writeSpeed", 0L);
            speedData.put("readSpeed", 0L);
        }

        return speedData;
    }

    @Override
    public List<Map<String, Object>> getConsumerGroupLagTrend(String clusterId, String groupId, String topic, String timeRange) {
        List<Map<String, Object>> trendData = new ArrayList<>();

        try {
            // 解析时间范围并转换为时间戳
            long[] timeRangeTimestamps = parseTimeRangeToTimestamps(timeRange);
            long startTimestamp = timeRangeTimestamps[0];
            long endTimestamp = timeRangeTimestamps[1];

            // 调用Mapper获取趋势数据
            List<Map<String, Object>> rawData = consumerGroupTopicMapper.getConsumerGroupLagTrend(
                clusterId, groupId, topic, startTimestamp, endTimestamp);

            // 转换数据格式
            for (Map<String, Object> record : rawData) {
                Map<String, Object> trendPoint = new HashMap<>();

                // 处理时间点
                Object collectTime = record.get("collect_time");
                if (collectTime != null) {
                    if (collectTime instanceof LocalDateTime) {
                        LocalDateTime localDateTime = (LocalDateTime) collectTime;
                        trendPoint.put("timePoint", localDateTime.format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        trendPoint.put("timestamp", localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000);
                    } else if (collectTime instanceof Date) {
                        Date date = (Date) collectTime;
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        trendPoint.put("timePoint", formatter.format(date));
                        trendPoint.put("timestamp", date.getTime());
                    }
                }

                // 处理延迟值
                Object lags = record.get("lags");
                trendPoint.put("totalLag", lags != null ? ((Number) lags).longValue() : 0L);

                trendData.add(trendPoint);
            }

        } catch (Exception e) {
            log.error("获取消费者组 {} 延迟趋势数据失败: {}", groupId, e.getMessage(), e);
        }

        return trendData;
    }

    /**
     * 解析时间范围字符串并返回开始和结束时间戳
     *
     * @param timeRange 时间范围字符串 (如 "1h", "6h", "1d", "3d", "7d")
     * @return 长度为2的数组，[开始时间戳, 结束时间戳]（毫秒）
     */
    private long[] parseTimeRangeToTimestamps(String timeRange) {
        long endTime = System.currentTimeMillis();
        long startTime;

        switch (timeRange.toLowerCase()) {
            case "1h":
                startTime = endTime - (1 * 60 * 60 * 1000L); // 1小时
                break;
            case "6h":
                startTime = endTime - (6 * 60 * 60 * 1000L); // 6小时
                break;
            case "1d":
                startTime = endTime - (24 * 60 * 60 * 1000L); // 24小时
                break;
            case "3d":
                startTime = endTime - (3 * 24 * 60 * 60 * 1000L); // 3天
                break;
            case "7d":
                startTime = endTime - (7 * 24 * 60 * 60 * 1000L); // 7天
                break;
            default:
                // 默认为24小时
                startTime = endTime - (24 * 60 * 60 * 1000L);
                break;
        }

        return new long[]{startTime, endTime};
    }

    @Override
    public boolean resetConsumerGroupOffsetWithClientInfo(KafkaClientInfo kafkaClientInfo, String groupId, String topic, String resetType, Long resetValue) {
        try {
            // 创建KafkaSchemaFactory
            KafkaSchemaFactory ksf = new KafkaSchemaFactory(new KafkaStoragePlugin());

            // 调用重置方法
            boolean result;
            if (topic != null && !topic.trim().isEmpty()) {
                // 重置特定topic的偏移量
                result = ksf.resetConsumerGroupOffsets(kafkaClientInfo, groupId, topic, resetType, resetValue);
            } else {
                // 重置所有topic的偏移量
                result = ksf.resetConsumerGroupOffsetsForAllTopics(kafkaClientInfo, groupId, resetType, resetValue);
            }

            if (result) {
            } else {
                log.warn("消费者组偏移量重置失败，groupId: {}, topic: {}, resetType: {}", groupId, topic, resetType);
            }

            return result;

        } catch (Exception e) {
            log.error("重置消费者组偏移量失败，groupId: {}, topic: {}, resetType: {}, error: {}",
                    groupId, topic, resetType, e.getMessage(), e);
            return false;
        }
    }
}