/**
 * 仪表盘页面JavaScript
 * 提供全面的Kafka集群监控功能
 * 
 * @author smartloli
 * @since 2024-01-20
 */

// 全局变量
let charts = {};
let updateInterval = null;
let currentPeriod = '1h';
let currentResource = 'cpu';
let currentMetric = 'activity';
let currentDimension = 'messages';
let currentClusterId = ''; // 当前集群ID

/**
 * 格式化数字显示，支持单位缩放
 * @param {number} num 要格式化的数字
 * @param {string} unit 单位（如 'msg/s', '/s'）
 * @returns {string} 格式化后的字符串
 */
function formatNumber(num, unit = '') {
    if (num == null || isNaN(num)) return '0' + unit;

    const number = parseFloat(num);

    if (number >= 1000000000) {
        return (number / 1000000000).toFixed(1) + '十亿' + unit;
    } else if (number >= 100000000) {
        return (number / 100000000).toFixed(1) + '亿' + unit;
    } else if (number >= 10000000) {
        return (number / 10000000).toFixed(1) + '千万' + unit;
    } else if (number >= 1000000) {
        return (number / 1000000).toFixed(1) + '百万' + unit;
    } else if (number >= 10000) {
        return (number / 10000).toFixed(1) + '万' + unit;
    } else if (number >= 1000) {
        return (number / 1000).toFixed(1) + '千' + unit;
    } else if (number >= 1) {
        return Math.round(number) + unit;
    } else {
        return number.toFixed(2) + unit;
    }
}

// 页面初始化
$(document).ready(function () {
    initializeClusterId();
    initializeDashboard();
    setupEventListeners();
    loadInitialData();
    startAutoRefresh();
    window.addEventListener('efak:themechange', syncResourceChartTheme);
});

function syncResourceChartTheme() {
    Object.values(charts.resource || {}).forEach(function (chart) {
        if (!chart || !chart.data || !chart.data.datasets || !chart.data.datasets[0]) return;
        const ds = chart.data.datasets[0];
        if (Array.isArray(ds.backgroundColor) && ds.backgroundColor.length > 1) {
            ds.backgroundColor[1] = 'rgba(0,0,0,0)';
            ds.borderColor = 'transparent';
            if (typeof chart.update === 'function') chart.update('none');
        }
    });
}

/**
 * 初始化集群ID
 */
function initializeClusterId() {
    if (window.efakCluster && typeof window.efakCluster.get === 'function') {
        currentClusterId = window.efakCluster.get() || '';
    } else {
        const urlParams = new URLSearchParams(window.location.search);
        currentClusterId = urlParams.get('cid') || '';
    }
}

/**
 * 初始化仪表盘
 */
function initializeDashboard() {
    initializeCharts();
}

/**
 * 设置事件监听器
 */
function setupEventListeners() {
    // 图表时间段切换
    $('.chart-filters .filter-btn[data-period]').on('click', function () {
        const period = $(this).data('period');
        $('.chart-filters .filter-btn[data-period]').removeClass('active');
        $(this).addClass('active');
        currentPeriod = period;
        updateThroughputChart();
    });

    // 表格维度切换
    $('.table-tabs .table-tab').on('click', function () {
        const dimension = $(this).data('dimension');
        $('.table-tabs .table-tab').removeClass('active');
        $(this).addClass('active');
        currentDimension = dimension;
        loadMetricsTableData(); // Load new data for the selected dimension
    });
}

/**
 * 加载初始数据
 */
function loadInitialData() {
    Promise.all([
        loadHeroStats(),
        loadMetricCards(),
        loadClusterOverview(), // 合并的集群概览请求
        loadThroughputData(),
        loadTopicsData(),
        loadMetricsTableData()
    ]).then(function(responses) {
        const [heroStats, metricCards, clusterOverview, throughputData, topicsData, metricsTable] = responses;

        // 处理指标卡片数据（需要集群概览数据）
        if (metricCards && clusterOverview) {
            const [performanceResponse, consumerResponse] = metricCards;

            const metricsData = {
                throughput: {
                    value: performanceResponse.success && performanceResponse.data ?
                        formatNumber(performanceResponse.data.messageIn, 'msg/s') : '0msg/s'
                },
                health: {
                    value: clusterOverview.clusterStatus || '未知'
                },
                latency: {
                    value: consumerResponse.success && consumerResponse.data ?
                        `${consumerResponse.data.avgLagRate}ms` : '0.00ms'
                },
                storage: {
                    value: consumerResponse.success && consumerResponse.data ?
                        formatNumber(consumerResponse.data.avgConsumerRate, '/s') : '0.00/s'
                }
            };

            updateMetricCards(metricsData);
        }

        // 更新资源图表
        if (clusterOverview && charts.resource) {
            updateResourceChart(clusterOverview);
        }
    }).catch(error => {
        console.error('Error loading initial data:', error);
        showToast('加载数据失败，请稍后重试', 'error');
    });
}

/**
 * Load cluster inventory metrics (brokers, topics, partitions, consumer groups).
 */
function loadHeroStats() {
    return $.ajax({
        url: '/dashboard/api/hero-stats',
        method: 'GET',
        data: { cid: currentClusterId },
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            updateHeroStats(response.data);
        }
    });
}

/**
 * Render cluster inventory metrics.
 */
function updateHeroStats(stats) {
    $('#hero-clusters').text(stats.onlineNodes || 0);
    $('#hero-topics').text(stats.totalTopics || 0);
    $('#hero-partitions').text(stats.totalPartitions || 0);
    $('#hero-consumers').text(stats.consumerGroups || 0);
}

/**
 * 加载指标卡片数据
 */
function loadMetricCards() {
    // 并行请求性能监控和消费者统计API，集群概览数据将在loadClusterOverview中获取
    return Promise.all([
        // 消息吞吐量 - 从性能监控API获取
        $.ajax({
            url: `/api/performance/monitors/latest/${currentClusterId}`,
            method: 'GET',
            dataType: 'json'
        }),
        // 消费者统计 - 从消费者API获取平均延迟和平均消费速度
        $.ajax({
            url: '/consumers/api/stats',
            method: 'GET',
            data: { cid: currentClusterId },
            dataType: 'json'
        })
    ]);
}

/**
 * 更新指标卡片
 */
function updateMetricCards(metrics) {
    $('#throughput-value').text(metrics.throughput.value);
    $('#health-value').text(metrics.health.value);
    $('#latency-value').text(metrics.latency.value);
    $('#storage-value').text(metrics.storage.value);
}

/**
 * 初始化图表
 */
function initializeCharts() {
    initializeThroughputChart();
    initializeResourceChart();
    initializeTopicsChart();
    initializeCapacityChart();
}

/**
 * 初始化消息吞吐量图表
 */
function initializeThroughputChart() {
    const ctx = document.getElementById('throughputChart');
    if (!ctx) return;

    charts.throughput = new Chart(ctx.getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: '写入耗时',
                    data: [],
                    borderColor: '#165DFF',
                    backgroundColor: 'rgba(22, 93, 255, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 0,
                    pointHoverRadius: 4
                },
                {
                    label: '读取耗时',
                    data: [],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 0,
                    pointHoverRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        color: chartLegendTextColor()
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        title: function (tooltipItems) {
                            return `时间: ${tooltipItems[0].label}`;
                        },
                        label: function (context) {
                            const value = context.parsed.y;
                            const unit = value >= 1000 ? `${(value / 1000).toFixed(2)}s` : `${value}ms`;
                            return `${context.dataset.label}: ${unit}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    title: {
                        display: true,
                        text: '时间'
                    },
                    ticks: {
                        maxTicksLimit: 8
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    },
                    title: {
                        display: true,
                        text: '耗时 (ms/s)'
                    },
                    ticks: {
                        callback: function(value) {
                            return value >= 1000 ? `${(value / 1000).toFixed(1)}s` : `${value}ms`;
                        }
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            }
        }
    });
}

/**
 * 初始化资源使用率图表
 */
function initializeResourceChart() {
    // 环形图将动态创建，这里不需要预先初始化
    charts.resource = {}; // 用于存储多个图表实例
}

/**
 * 创建单个环形图
 */
function chartTrackColor() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? '#3a4658' : '#e5e7eb';
}

function createDoughnutChart(canvasId, value, label, color) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    const remaining = Math.max(0, 100 - value);

    return new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [value, remaining],
                backgroundColor: [color, 'rgba(0,0,0,0)'],
                borderWidth: 0,
                hoverBorderWidth: 0,
                borderColor: 'transparent',
                spacing: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            clip: false,
            layout: {
                padding: 6
            },
            cutout: '72%',
            radius: '90%',
            rotation: -90,
            circumference: 360,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    enabled: false
                }
            },
            animation: {
                animateRotate: true,
                animateScale: false
            }
        }
    });
}

/**
 * 获取使用率对应的颜色
 */
function getUsageColor(value, type) {
    if (type === 'cpu') {
        if (value >= 80) return '#ef4444'; // 红色 - 高使用率
        if (value >= 60) return '#f59e0b'; // 黄色 - 中等使用率
        return '#10b981'; // 绿色 - 低使用率
    } else { // memory
        if (value >= 85) return '#ef4444'; // 红色 - 高使用率
        if (value >= 70) return '#f59e0b'; // 黄色 - 中等使用率
        return '#165DFF'; // 蓝色 - 低使用率
    }
}

function chartLegendTextColor(chart) {
    const fromChart = chart && chart.options && chart.options.plugins
        && chart.options.plugins.legend && chart.options.plugins.legend.labels
        && chart.options.plugins.legend.labels.color;
    if (typeof fromChart === 'string' && fromChart) return fromChart;
    if (window.efakTheme && typeof window.efakTheme.colors === 'function') {
        return window.efakTheme.colors().text;
    }
    return document.documentElement.getAttribute('data-theme') === 'dark' ? '#f3f6fa' : '#374151';
}

function doughnutLegendLabels(chart) {
    const data = chart.data;
    const textColor = chartLegendTextColor(chart);
    if (!data.labels.length || !data.datasets.length) return [];
    return data.labels.map(function (label, i) {
        const value = data.datasets[0].data[i];
        const total = data.datasets[0].data.reduce(function (a, b) { return a + b; }, 0);
        const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
        return {
            text: label + ': ' + value + '个 (' + percentage + '%)',
            fillStyle: data.datasets[0].backgroundColor[i],
            strokeStyle: data.datasets[0].backgroundColor[i],
            fontColor: textColor,
            pointStyle: 'circle',
            hidden: false,
            index: i
        };
    });
}

/**
 * 初始化主题占比图表
 */
function initializeTopicsChart() {
    const ctx = document.getElementById('topicsChart');
    if (!ctx) return;

    charts.topics = new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: [
                    '#10b981', // 活跃主题 - 绿色
                    '#6b7280'  // 空闲主题 - 灰色
                ],
                borderWidth: 3,
                borderColor: '#ffffff',
                cutout: '60%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        color: chartLegendTextColor(),
                        font: {
                            size: 14,
                            weight: 'bold'
                        },
                        generateLabels: doughnutLegendLabels
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const label = context.label;
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                            return `${label}: ${value}个主题 (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

/**
 * 初始化容量分布图表
 */
function initializeCapacityChart() {
    const ctx = document.getElementById('capacityChart');
    if (!ctx) return;

    charts.capacity = new Chart(ctx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: [
                    '#10b981', // 0-100MB - 绿色
                    '#165DFF', // 100MB-1GB - 蓝色
                    '#f59e0b', // 1GB-10GB - 黄色
                    '#ef4444'  // 10GB+ - 红色
                ],
                borderWidth: 3,
                borderColor: '#ffffff',
                cutout: '60%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        color: chartLegendTextColor(),
                        font: {
                            size: 14,
                            weight: 'bold'
                        },
                        generateLabels: doughnutLegendLabels
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const label = context.label;
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                            return `${label}: ${value}个主题 (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

/**
 * 加载吞吐量数据
 */
function loadThroughputData() {
    return $.ajax({
        url: '/dashboard/api/performance-trend',
        method: 'GET',
        data: {
            cid: currentClusterId,
            period: currentPeriod
        },
        dataType: 'json'
    }).done(function (response) {
        if (response.success && charts.throughput) {
            updateThroughputChart(response.data);
        }
    });
}

/**
 * 更新吞吐量图表
 */
function updateThroughputChart(data) {
    if (!charts.throughput || !data) return;

    charts.throughput.data.labels = data.labels;
    charts.throughput.data.datasets[0].data = data.writeLatency;  // 写入耗时
    charts.throughput.data.datasets[1].data = data.readLatency;   // 读取耗时
    charts.throughput.update();
}

/**
 * 加载集群概览数据（合并请求）
 */
function loadClusterOverview() {
    return $.ajax({
        url: '/api/brokers/cluster/overview',
        method: 'GET',
        data: {
            clusterId: currentClusterId
        },
        dataType: 'json'
    });
}

/**
 * 更新资源图表
 */
function updateResourceChart(data) {
    if (!data || data.averageCpuUsage === undefined || data.averageMemoryUsage === undefined) return;

    const container = $('#resourceChartsContainer');
    container.empty();

    // 销毁现有的图表
    Object.values(charts.resource).forEach(chart => {
        if (chart && typeof chart.destroy === 'function') {
            chart.destroy();
        }
    });
    charts.resource = {};

    // 直接使用百分比值，数据已经是计算好的百分比
    const cpuValue = parseFloat(data.averageCpuUsage.toFixed(2));
    const memoryValue = parseFloat(data.averageMemoryUsage.toFixed(2));

    // 创建集群整体资源使用率卡片（优化大小和布局）
    const healthy = data.clusterStatus === '健康';
    const clusterCardHtml = `
        <div class="resource-node-card" style="max-width: 600px; width: 100%;">
            <div class="resource-node-title" style="font-size: 1.25rem; margin-bottom: 24px;">集群整体资源使用率</div>
            <div class="resource-charts-row" style="gap: 40px;">
                <div class="resource-chart-item">
                    <div class="resource-chart-wrapper">
                        <canvas id="cluster-cpu-chart"></canvas>
                        <div class="resource-chart-center-text">
                            <div class="resource-chart-value" style="font-size: 1.75rem;">${cpuValue}%</div>
                            <div class="resource-chart-label" style="font-size: 0.875rem;">平均CPU</div>
                        </div>
                    </div>
                </div>
                <div class="resource-chart-item">
                    <div class="resource-chart-wrapper">
                        <canvas id="cluster-memory-chart"></canvas>
                        <div class="resource-chart-center-text">
                            <div class="resource-chart-value" style="font-size: 1.75rem;">${memoryValue}%</div>
                            <div class="resource-chart-label" style="font-size: 0.875rem;">平均内存</div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="resource-node-meta">
                <div class="resource-meta-row">
                    <span class="resource-meta-item">
                        <i class="fas fa-server"></i>
                        在线节点: <strong>${data.onlineNodes}/${data.totalNodes}</strong>
                    </span>
                    <span class="resource-meta-item">
                        <i class="fas fa-clock"></i>
                        运行时间: <strong>${data.runtime}</strong>
                    </span>
                </div>
                <div class="resource-meta-row">
                    <span class="resource-meta-item">
                        <i class="fas fa-chart-line"></i>
                        在线率: <strong>${data.onlineRate}%</strong>
                    </span>
                    <span class="resource-meta-item ${healthy ? 'is-ok' : 'is-bad'}">
                        <i class="fas fa-heartbeat"></i>
                        集群状态: <strong>${data.clusterStatus}</strong>
                    </span>
                </div>
            </div>
        </div>
    `;

    container.append(clusterCardHtml);

    // 创建CPU环形图
    setTimeout(() => {
        const cpuColor = getUsageColor(cpuValue, 'cpu');
        const cpuChart = createDoughnutChart('cluster-cpu-chart', cpuValue, 'CPU', cpuColor);
        if (cpuChart) {
            charts.resource['cluster-cpu'] = cpuChart;
        }

        // 创建内存环形图
        const memoryColor = getUsageColor(memoryValue, 'memory');
        const memoryChart = createDoughnutChart('cluster-memory-chart', memoryValue, '内存', memoryColor);
        if (memoryChart) {
            charts.resource['cluster-memory'] = memoryChart;
        }
    }, 100);
}

/**
 * 加载主题分析数据
 */
function loadTopicsData() {
    return $.ajax({
        url: '/dashboard/api/topics',
        method: 'GET',
        data: { cid: currentClusterId },
        dataType: 'json'
    }).done(function (response) {
        if (response.success && response.data) {
            updateTopicAnalysisData(response.data);
        }
    });
}

/**
 * 更新主题分析数据
 * @param {Object} data - 主题分析数据
 */
function updateTopicAnalysisData(data) {
    if (!data) return;

    // 更新统计卡片
    updateTopicStatsCards(data.stats);

    // 更新主题状态图表
    updateTopicsChart(data);

    // 更新容量分布图表
    updateCapacityChart(data.capacityDistribution);
}

/**
 * 更新主题统计卡片
 * @param {Object} stats - 统计数据
 */
function updateTopicStatsCards(stats) {
    if (!stats) return;

    $('#total-topics-count').text(stats.totalTopics || 0);
    $('#active-topics-count').text(stats.activeTopics || 0);
    $('#idle-topics-count').text(stats.idleTopics || 0);
    $('#active-percentage').text((stats.activePercentage || 0) + '%');
    $('#total-capacity').text(stats.totalCapacityFormatted || '0B');
}

/**
 * 更新主题状态图表
 * @param {Object} data - 主题分析数据
 */
function updateTopicsChart(data) {
    if (!charts.topics || !data) return;

    charts.topics.data.labels = data.labels || [];
    charts.topics.data.datasets[0].data = data.values || [];
    charts.topics.update();
}

/**
 * 更新容量分布图表
 * @param {Object} capacityData - 容量分布数据
 */
function updateCapacityChart(capacityData) {
    if (!charts.capacity || !capacityData) return;

    charts.capacity.data.labels = capacityData.labels || [];
    charts.capacity.data.datasets[0].data = capacityData.values || [];
    charts.capacity.data.datasets[0].backgroundColor = capacityData.colors || ['#10b981', '#165DFF', '#f59e0b', '#ef4444'];
    charts.capacity.update();
}

/**
 * 加载指标表格数据
 */
function loadMetricsTableData() {
    return $.ajax({
        url: '/dashboard/api/metrics-table',
        method: 'GET',
        data: { dimension: currentDimension, cid: currentClusterId },
        dataType: 'json'
    }).done(function (response) {
        if (response.success && response.data) {
            updateMetricsTable(response.data);
        } else {
            console.error('Failed to load metrics table data:', response.message);
            // Show empty table if no data
            updateMetricsTable([]);
        }
    }).fail(function(xhr, status, error) {
        console.error('Failed to load metrics table data:', error);
        // Show empty table on error
        updateMetricsTable([]);
    });
}

/**
 * 更新指标表格
 */
function updateMetricsTable(data) {
    if (!data) return;

    updateTableHeader();

    const tbody = $('#metrics-table-body');
    tbody.empty();

    data.forEach((item, index) => {
        const row = createTableRow(item, index + 1);
        tbody.append(row);
    });
}

/**
 * 更新表格头部
 */
function updateTableHeader() {
    const headers = {
        messages: ['排名', '主题名称', '消息数量', '数据倾斜', '分区数', '副本数', '状态'],
        size: ['排名', '主题名称', '存储大小', '数据倾斜', '分区数', '副本数', '状态'],
        read: ['排名', '主题名称', '读取速率', '数据倾斜', '分区数', '副本数', '状态'],
        write: ['排名', '主题名称', '写入速率', '数据倾斜', '分区数', '副本数', '状态']
    };

    const headerRow = $('#table-header');
    headerRow.empty();

    headers[currentDimension].forEach(header => {
        headerRow.append(`<th>${header}</th>`);
    });
}

/**
 * 创建表格行
 */
function createTableRow(item, rank) {
    const rankClass = rank <= 3 ? `rank-${rank}` : 'rank-other';

    // Determine skew styling based on skewLevel
    const skewClass = getSkewClass(item.skewLevel);
    const skewIcon = getSkewIcon(item.skewLevel);

    // Only show topic name without description
    const topicNameCell = item.description ?
        `<div class="font-medium text-gray-900">${item.topic}</div>
         <div class="text-sm text-gray-500">${item.description}</div>` :
        `<div class="font-medium text-gray-900">${item.topic}</div>`;

    return $(`
        <tr>
            <td>
                <span class="rank-badge ${rankClass}">${rank}</span>
            </td>
            <td>
                ${topicNameCell}
            </td>
            <td>
                <span class="font-semibold text-gray-900">${item.value}</span>
            </td>
            <td>
                <span class="skew-indicator ${skewClass}">
                    <i class="fas ${skewIcon}"></i>
                    ${item.brokerSkewed}
                </span>
            </td>
            <td>${item.partitions}</td>
            <td>${item.replicas}</td>
            <td>
                <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${item.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}">
                    ${item.status === 'active' ? '活跃' : '空闲'}
                </span>
            </td>
        </tr>
    `);
}

/**
 * 获取数据倾斜样式类
 */
function getSkewClass(skewLevel) {
    switch (skewLevel) {
        case 'normal':
            return 'skew-normal';
        case 'warning':
            return 'skew-warning';
        case 'error':
            return 'skew-error';
        default:
            return 'skew-normal';
    }
}

/**
 * 获取数据倾斜图标
 */
function getSkewIcon(skewLevel) {
    switch (skewLevel) {
        case 'normal':
            return 'fa-check-circle';
        case 'warning':
            return 'fa-exclamation-triangle';
        case 'error':
            return 'fa-times-circle';
        default:
            return 'fa-check-circle';
    }
}

/**
 * 开始自动刷新
 */
function startAutoRefresh() {
    updateInterval = setInterval(() => {
        // 加载集群概览数据，然后更新相关组件
        loadClusterOverview().then(function(clusterOverview) {
            // 更新资源图表
            if (clusterOverview && charts.resource) {
                updateResourceChart(clusterOverview);
            }
        });

        // 加载其他数据
        loadThroughputData();
        loadTopicsData();
        loadHeroStats();
        loadMetricsTableData(); // 添加指标表格数据自动刷新

        // 加载指标卡片数据
        loadMetricCards().then(function(responses) {
            return loadClusterOverview().then(function(clusterOverview) {
                const [performanceResponse, consumerResponse] = responses;

                const metricsData = {
                    throughput: {
                        value: performanceResponse.success && performanceResponse.data ?
                            formatNumber(performanceResponse.data.messageIn, 'msg/s') : '0msg/s'
                    },
                    health: {
                        value: clusterOverview.clusterStatus || '未知'
                    },
                    latency: {
                        value: consumerResponse.success && consumerResponse.data ?
                            `${consumerResponse.data.avgLagRate}ms` : '0.00ms'
                    },
                    storage: {
                        value: consumerResponse.success && consumerResponse.data ?
                            formatNumber(consumerResponse.data.avgConsumerRate, '/s') : '0.00/s'
                    }
                };

                updateMetricCards(metricsData);
            });
        });
    }, 30000);
}

/**
 * 停止自动刷新
 */
function stopAutoRefresh() {
    if (updateInterval) {
        clearInterval(updateInterval);
        updateInterval = null;
    }
}

/**
 * 显示提示消息
 */
function showToast(message, type = 'info') {
    if (window.efakShowToast) {
        window.efakShowToast(message, type);
    }
}

// 页面离开时清理资源
$(window).on('beforeunload', function () {
    stopAutoRefresh();
    Object.values(charts).forEach(chart => {
        if (chart) {
            chart.destroy();
        }
    });
});
