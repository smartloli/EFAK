/**
 * Kafka性能监控页面JavaScript
 * 专门针对Kafka集群的性能监控和数据可视化
 */

class KafkaMonitoring {
    constructor() {
        this.charts = {};
        this.currentTimeRange = '24h';
        this.clusterId = this.getUrlParameter('cid');
        this.apiEndpoint = '/api/performance/monitors';

        if (!this.clusterId) {
            console.error('缺少集群ID参数(cid)');
            return;
        }

        this.init();
    }

    // 获取URL参数
    getUrlParameter(name) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(name);
    }

    init() {
        this.initEventListeners();
        this.loadInitialData();
    }

    // 初始化事件监听器
    initEventListeners() {
        // 时间范围选择器
        document.querySelectorAll('.time-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.handleTimeRangeChange(e);
            });
        });

        // 页面可见性变化处理 - 移除自动刷新功能
        document.addEventListener('visibilitychange', () => {
            // 不再需要自动刷新逻辑
        });

        // 窗口大小变化处理
        window.addEventListener('resize', () => {
            this.debounce(() => {
                this.resizeCharts();
            }, 300)();
        });
    }

    // 处理时间范围变化
    handleTimeRangeChange(event) {
        const button = event.target;
        const timeRange = button.dataset.time;
        const chartType = button.dataset.chart;

        // 更新按钮状态
        const container = button.parentElement;
        container.querySelectorAll('.time-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        button.classList.add('active');

        // 更新数据
        this.currentTimeRange = timeRange;
        this.updateChartData(chartType, timeRange);
    }

    // 加载初始数据
    async loadInitialData() {
        try {
            this.showLoadingState();
            await this.initAllCharts();
            await this.loadMonitoringData();
            // 加载图表趋势数据
            await this.loadTrendData();
        } catch (error) {
            console.error('数据加载失败:', error);
            this.showErrorState('数据加载失败，请刷新页面重试');
        }
    }

    // 加载趋势数据
    async loadTrendData() {
        try {
            // 为所有图表加载默认时间范围的数据
            await this.updateChartData('throughput', this.currentTimeRange);
            await this.updateChartData('consume', this.currentTimeRange);
            await this.updateChartData('produce', this.currentTimeRange);
        } catch (error) {
            console.error('趋势数据加载失败:', error);
        }
    }

    // 初始化所有图表
    async initAllCharts() {
        // 消息吞吐量趋势图
        this.initThroughputChart();

        // 消息读取趋势图
        this.initConsumeChart();

        // 消息写入趋势图
        this.initProduceChart();
    }

    // 初始化消息吞吐量趋势图
    initThroughputChart() {
        const ctx = document.getElementById('throughput-chart');
        if (!ctx) return;

        this.charts.throughput = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '总吞吐量',
                    data: [],
                    borderColor: '#7c3aed',
                    backgroundColor: 'rgba(124, 58, 237, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 0,         // 隐藏原点
                    pointHoverRadius: 4     // 悬停时显示点
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function (context) {
                                return context.dataset.label + ': ' + context.parsed.y.toLocaleString() + ' msg/sec';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '消息数/秒'
                        },
                        ticks: {
                            callback: function (value) {
                                return value.toLocaleString() + ' msg/s';
                            },
                            maxTicksLimit: 8  // 限制Y轴刻度数量
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: '时间'
                        },
                        ticks: {
                            maxTicksLimit: 10,  // 限制X轴刻度数量，避免太密集
                            maxRotation: 0      // 不旋转标签
                        }
                    }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });
    }

    // 初始化消息读取趋势图
    initConsumeChart() {
        const ctx = document.getElementById('consume-chart');
        if (!ctx) return;

        this.charts.consume = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '读取速率',
                    data: [],
                    borderColor: '#059669',
                    backgroundColor: 'rgba(5, 150, 105, 0.2)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 0,
                    pointHoverRadius: 4
                }, {
                    label: '消费延迟',
                    data: [],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    yAxisID: 'y1'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function (context) {
                                const label = context.dataset.label || '';
                                const value = context.parsed.y;

                                if (context.datasetIndex === 0) {
                                    // 读取速率 - 使用字节格式化，确保非负数
                                    const safeValue = Math.max(0, value || 0);
                                    // 直接实现字节格式化逻辑，避免依赖window.kafkaMonitoring
                                    let formattedBytes;
                                    if (safeValue === 0) {
                                        formattedBytes = '0 B';
                                    } else {
                                        const k = 1024;
                                        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
                                        const i = Math.max(0, Math.min(Math.floor(Math.log(safeValue) / Math.log(k)), sizes.length - 1));
                                        if (i === 0) {
                                            formattedBytes = Math.round(safeValue) + ' ' + sizes[0];
                                        } else {
                                            formattedBytes = (safeValue / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i];
                                        }
                                    }
                                    return label + ': ' + formattedBytes + '/s';
                                } else {
                                    // 消费延迟 - 使用毫秒
                                    return label + ': ' + value.toFixed(2) + ' ms';
                                }
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,  // 确保 Y 轴从 0 开始
                        type: 'linear',
                        display: true,
                        position: 'left',
                        title: {
                            display: true,
                            text: '读取速率'
                        },
                        ticks: {
                            maxTicksLimit: 8,
                            callback: function (value) {
                                // 确保值为非负数
                                const safeValue = Math.max(0, value || 0);
                                // 直接实现字节格式化逻辑，避免依赖window.kafkaMonitoring
                                if (safeValue === 0) return '0 B/s';
                                const k = 1024;
                                const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
                                const i = Math.max(0, Math.min(Math.floor(Math.log(safeValue) / Math.log(k)), sizes.length - 1));
                                if (i === 0) {
                                    return Math.round(safeValue) + ' ' + sizes[0] + '/s';
                                }
                                return (safeValue / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i] + '/s';
                            }
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        title: {
                            display: true,
                            text: '延迟 (ms)'
                        },
                        grid: {
                            drawOnChartArea: false
                        },
                        ticks: {
                            maxTicksLimit: 8
                        }
                    },
                    x: {
                        ticks: {
                            maxTicksLimit: 10,
                            maxRotation: 0
                        }
                    }
                }
            }
        });
    }

    // 初始化消息写入趋势图
    initProduceChart() {
        const ctx = document.getElementById('produce-chart');
        if (!ctx) return;

        this.charts.produce = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '写入速率',
                    data: [],
                    borderColor: '#3b82f6',
                    backgroundColor: 'rgba(59, 130, 246, 0.2)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 0,
                    pointHoverRadius: 4
                }, {
                    label: '写入延迟',
                    data: [],
                    borderColor: '#7c3aed',
                    backgroundColor: 'rgba(124, 58, 237, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4,
                    pointRadius: 0,
                    pointHoverRadius: 4,
                    yAxisID: 'y1'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function (context) {
                                const label = context.dataset.label || '';
                                const value = context.parsed.y;

                                if (context.datasetIndex === 0) {
                                    // 写入速率 - 使用字节格式化，确保非负数
                                    const safeValue = Math.max(0, value || 0);
                                    // 直接实现字节格式化逻辑，避免依赖window.kafkaMonitoring
                                    let formattedBytes;
                                    if (safeValue === 0) {
                                        formattedBytes = '0 B';
                                    } else {
                                        const k = 1024;
                                        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
                                        const i = Math.max(0, Math.min(Math.floor(Math.log(safeValue) / Math.log(k)), sizes.length - 1));
                                        if (i === 0) {
                                            formattedBytes = Math.round(safeValue) + ' ' + sizes[0];
                                        } else {
                                            formattedBytes = (safeValue / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i];
                                        }
                                    }
                                    return label + ': ' + formattedBytes + '/s';
                                } else {
                                    // 写入延迟 - 使用毫秒
                                    return label + ': ' + value.toFixed(2) + ' ms';
                                }
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,  // 确保 Y 轴从 0 开始
                        type: 'linear',
                        display: true,
                        position: 'left',
                        title: {
                            display: true,
                            text: '写入速率'
                        },
                        ticks: {
                            maxTicksLimit: 8,
                            callback: function (value) {
                                // 确保值为非负数
                                const safeValue = Math.max(0, value || 0);
                                // 直接实现字节格式化逻辑，避免依赖window.kafkaMonitoring
                                if (safeValue === 0) return '0 B/s';
                                const k = 1024;
                                const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
                                const i = Math.max(0, Math.min(Math.floor(Math.log(safeValue) / Math.log(k)), sizes.length - 1));
                                if (i === 0) {
                                    return Math.round(safeValue) + ' ' + sizes[0] + '/s';
                                }
                                return (safeValue / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i] + '/s';
                            }
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        title: {
                            display: true,
                            text: '延迟 (ms)'
                        },
                        grid: {
                            drawOnChartArea: false
                        },
                        ticks: {
                            maxTicksLimit: 8
                        }
                    },
                    x: {
                        ticks: {
                            maxTicksLimit: 10,
                            maxRotation: 0
                        }
                    }
                }
            }
        });
    }



    // 加载监控数据
    async loadMonitoringData() {
        try {
            const data = await this.fetchMonitoringData();
            this.updateStatistics(data);
            this.updateAllCharts(data);
        } catch (error) {
            console.error('监控数据加载失败:', error);
            this.showErrorState('监控数据加载失败');
        }
    }

    // 获取监控数据
    async fetchMonitoringData() {
        try {
            const response = await fetch(`${this.apiEndpoint}/latest/${this.clusterId}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            const result = await response.json();

            // 检查API响应结构
            if (result.success && result.data) {
                return this.processApiData(result.data);
            } else {
                throw new Error('API响应格式错误或请求失败');
            }
        } catch (error) {
            console.error('API调用失败:', error);
            throw error;
        }
    }

    // 处理API返回的数据
    processApiData(apiData) {
        // 如果没有数据，返回默认值
        if (!apiData) {
            return {
                throughput: { current: 0, average: 0, peak: 0 },
                consume: { rate: 0, lag: 0 },
                produce: { rate: 0, latency: 0 },
                charts: { labels: [], throughput: [], produce: [], consume: [], produceLatency: [], consumeLatency: [] }
            };
        }

        const processedData = {
            throughput: {
                current: parseFloat(apiData.messageIn) || 0,
                average: parseFloat(apiData.messageIn) || 0,
                peak: parseFloat(apiData.messageIn) || 0,
            },
            consume: {
                rate: parseFloat(apiData.byteOut) || 0,
                lag: parseFloat(apiData.timeMsConsumer) || 0,
            },
            produce: {
                rate: parseFloat(apiData.byteIn) || 0,
                latency: parseFloat(apiData.timeMsProduce) || 0,
            },
            charts: this.processChartData(apiData)
        };

        return processedData;
    }

    // 处理图表数据
    processChartData(apiData) {
        return {
            labels: this.generateTimeLabels(),
            throughput: apiData.historicalMessageIn || [],
            produce: apiData.historicalByteIn || [],
            consume: apiData.historicalByteOut || [],
            produceLatency: apiData.historicalTimeMsProduce || [],
            consumeLatency: apiData.historicalTimeMsConsumer || []
        };
    }

    // 生成时间标签
    generateTimeLabels() {
        const now = new Date();
        const labels = [];
        const points = this.getDataPointsByTimeRange(this.currentTimeRange);

        for (let i = points - 1; i >= 0; i--) {
            const time = new Date(now.getTime() - i * this.getIntervalByTimeRange(this.currentTimeRange));
            labels.push(this.formatTimeLabel(time));
        }

        return labels;
    }

    // 计算平均值
    calculateAverage(values) {
        if (!values || values.length === 0) return 0;
        return values.reduce((sum, val) => sum + val, 0) / values.length;
    }

    // 计算峰值
    calculatePeak(values) {
        if (!values || values.length === 0) return 0;
        return Math.max(...values);
    }

    // 更新统计数据
    updateStatistics(data) {
        // 更新顶部统计数据
        this.updateElement('message-throughput', this.formatNumber(data.throughput.current));
        this.updateElement('message-consume', this.formatBytes(data.consume.rate) + '/s');
        this.updateElement('message-produce', this.formatBytes(data.produce.rate) + '/s');
        this.updateElement('message-lag', data.produce.latency + 'ms');

        // 更新详细指标
        this.updateElement('throughput-current', this.formatNumber(data.throughput.current));
        this.updateElement('throughput-avg', this.formatNumber(data.throughput.average));
        this.updateElement('throughput-peak', this.formatNumber(data.throughput.peak));

        this.updateElement('consume-rate', this.formatBytes(data.consume.rate) + '/s');
        this.updateElement('consume-lag', data.consume.lag + 'ms');

        this.updateElement('produce-rate', this.formatBytes(data.produce.rate) + '/s');
        this.updateElement('produce-latency', data.produce.latency + 'ms');
    }

    // 更新所有图表
    updateAllCharts(data) {
        const chartData = data.charts;

        // 更新吞吐量图表
        if (this.charts.throughput) {
            this.charts.throughput.data.labels = chartData.labels;
            this.charts.throughput.data.datasets[0].data = chartData.throughput;
            this.charts.throughput.update('none');
        }

        // 更新消费图表
        if (this.charts.consume) {
            this.charts.consume.data.labels = chartData.labels;
            this.charts.consume.data.datasets[0].data = chartData.consume;
            this.charts.consume.data.datasets[1].data = chartData.consumeLatency;
            this.charts.consume.update('none');
        }

        // 更新生产图表
        if (this.charts.produce) {
            this.charts.produce.data.labels = chartData.labels;
            this.charts.produce.data.datasets[0].data = chartData.produce;
            this.charts.produce.data.datasets[1].data = chartData.produceLatency;
            this.charts.produce.update('none');
        }
    }

    // 更新特定图表数据
    async updateChartData(chartType, timeRange) {
        try {
            const response = await fetch(`${this.apiEndpoint}/trend/${this.clusterId}?timeRange=${timeRange}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const result = await response.json();

            // 检查API响应结构
            if (result.success && result.data) {
                const chartData = this.processChartDataFromTrend(result.data.trendData, timeRange);

                if (this.charts[chartType]) {
                    this.charts[chartType].data.labels = chartData.labels;

                    switch (chartType) {
                        case 'throughput':
                            this.charts[chartType].data.datasets[0].data = chartData.throughput;
                            break;
                        case 'consume':
                            this.charts[chartType].data.datasets[0].data = chartData.consume;
                            this.charts[chartType].data.datasets[1].data = chartData.consumeLatency;
                            break;
                        case 'produce':
                            this.charts[chartType].data.datasets[0].data = chartData.produce;
                            this.charts[chartType].data.datasets[1].data = chartData.produceLatency;
                            break;
                    }

                    this.charts[chartType].update();
                }
            } else {
                throw new Error('趋势数据API响应格式错误');
            }
        } catch (error) {
            console.error(`更新${chartType}图表失败:`, error);
        }
    }

    // 处理趋势数据用于图表显示
    processChartDataFromTrend(trendData, timeRange) {
        if (!trendData || trendData.length === 0) {
            return {
                labels: [],
                throughput: [],
                produce: [],
                consume: [],
                produceLatency: [],
                consumeLatency: []
            };
        }

        const labels = [];
        const throughput = [];
        const produce = [];
        const consume = [];
        const produceLatency = [];
        const consumeLatency = [];

        trendData.forEach(item => {
            labels.push(item.time || '');
            throughput.push(parseFloat(item.messageIn) || 0);
            produce.push(parseFloat(item.byteIn) || 0);
            consume.push(parseFloat(item.byteOut) || 0);
            produceLatency.push(parseFloat(item.timeMsProduce) || 0);
            consumeLatency.push(parseFloat(item.timeMsConsumer) || 0);
        });

        // 计算统计数据并更新到页面
        this.updateThroughputStatistics(throughput);

        return {
            labels,
            throughput,
            produce,
            consume,
            produceLatency,
            consumeLatency
        };
    }

    // 更新吞吐量统计数据
    updateThroughputStatistics(throughputData) {
        if (!throughputData || throughputData.length === 0) {
            this.updateElement('throughput-current', '0');
            this.updateElement('throughput-avg', '0');
            this.updateElement('throughput-peak', '0');
            return;
        }

        // 计算当前总吞吐量（取最新的数据点）
        const currentThroughput = throughputData[throughputData.length - 1] || 0;

        // 计算平均总吞吐量
        const validData = throughputData.filter(val => val > 0);
        const avgThroughput = validData.length > 0
            ? validData.reduce((sum, val) => sum + val, 0) / validData.length
            : 0;

        // 计算峰值总吞吐量
        const peakThroughput = Math.max(...throughputData, 0);

        // 更新页面显示
        this.updateElement('throughput-current', this.formatNumber(currentThroughput));
        this.updateElement('throughput-avg', this.formatNumber(avgThroughput));
        this.updateElement('throughput-peak', this.formatNumber(peakThroughput));
    }

    // 工具方法
    getDataPointsByTimeRange(range) {
        switch (range) {
            case '1h': return 60;
            case '6h': return 36;   // 6小时每10分钟一个点
            case '24h': return 24;  // 24小时每小时一个点
            case '3d': return 72;   // 3天每小时一个点
            case '7d': return 168;  // 7天每小时一个点
            default: return 24;
        }
    }

    getIntervalByTimeRange(range) {
        switch (range) {
            case '1h': return 60 * 1000;          // 1分钟
            case '6h': return 10 * 60 * 1000;     // 10分钟
            case '24h': return 60 * 60 * 1000;    // 1小时
            case '3d': return 60 * 60 * 1000;     // 1小时
            case '7d': return 60 * 60 * 1000;     // 1小时
            default: return 60 * 60 * 1000;
        }
    }

    formatTimeLabel(date) {
        switch (this.currentTimeRange) {
            case '1h':
                return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
            case '6h':
                return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
            case '24h':
                return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
            case '3d':
            case '7d':
                return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit' });
            default:
                return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        }
    }

    formatNumber(num) {
        if (num == null || num === 0) return '0';

        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }

        return num.toLocaleString();
    }

    // 格式化字节数，自动适配单位
    formatBytes(bytes) {
        if (bytes == null || bytes === undefined || isNaN(bytes) || bytes < 0) {
            return '0 B';
        }

        if (bytes === 0) return '0 B';

        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.max(0, Math.min(Math.floor(Math.log(Math.abs(bytes)) / Math.log(k)), sizes.length - 1));

        if (i === 0) {
            return Math.round(bytes) + ' ' + sizes[0];
        }

        return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i];
    }

    updateElement(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    // 显示加载状态
    showLoadingState() {
        // 可以在这里添加加载指示器的显示逻辑
    }

    // 显示错误状态
    showErrorState(message) {
        console.error('监控系统错误:', message);
        // 可以在这里添加错误提示的显示逻辑
    }

    // 重新调整图表大小
    resizeCharts() {
        Object.values(this.charts).forEach(chart => {
            if (chart) {
                chart.resize();
            }
        });
    }

    // 防抖函数
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // 销毁实例
    destroy() {
        // 销毁所有图表
        Object.values(this.charts).forEach(chart => {
            if (chart) {
                chart.destroy();
            }
        });

        this.charts = {};
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    window.kafkaMonitoring = new KafkaMonitoring();
});

// 页面卸载时清理资源
window.addEventListener('beforeunload', () => {
    if (window.kafkaMonitoring) {
        window.kafkaMonitoring.destroy();
    }
});
