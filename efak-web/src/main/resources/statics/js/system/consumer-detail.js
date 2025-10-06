/**
 * 消费者组详情页面JavaScript
 * 提供消费者组数据管理、图表显示、操作控制等功能
 *
 * @author smartloli
 * @since 2024-01-20
 */

// 全局变量
let lagTrendChart = null;
let refreshInterval = null;
let currentGroupId = null;
let currentClusterId = null;
let currentTopic = null;

// 页面初始化
$(document).ready(function () {
    initializePage();
    loadInitialData();
    startAutoRefresh();
});

/**
 * 初始化页面
 */
function initializePage() {
    // 获取URL参数
    currentGroupId = getUrlParameter('group');
    currentClusterId = getUrlParameter('cid');
    currentTopic = getUrlParameter('topic');

    if (!currentGroupId || !currentClusterId || !currentTopic) {
        showToast('缺少必要参数', 'error');
        return;
    }

    // 立即显示URL解析的值
    updateTopDisplayInfo();

    // 初始化图表
    initializeLagTrendChart();

    // 初始化时间选择器
    initializeTimeRangeSelect();
}

/**
 * 更新顶部显示信息
 */
function updateTopDisplayInfo() {
    $('#clusterName').text(currentClusterId || '-');
    $('#consumerGroupName').text(currentGroupId || '-');
    $('#topicName').text(currentTopic || '-');
}

/**
 * 初始化时间范围选择器
 */
function initializeTimeRangeSelect() {
    $('#timeRangeSelect').select2({
        placeholder: '选择时间范围',
        allowClear: false,
        width: '100%',
        minimumResultsForSearch: Infinity // 禁用搜索框
    });
}

/**
 * 更新时间范围
 */
function updateTimeRange() {
    loadLagTrendData();
}

/**
 * 加载初始数据
 */
function loadInitialData() {
    Promise.all([
        loadConsumerGroupInfo(),
        loadConsumerSpeedData(),
        loadLagTrendData()
    ]).catch(() => {
        showToast('加载数据失败，请稍后重试', 'error');
    });
}

/**
 * 加载消费者组基本信息
 */
function loadConsumerGroupInfo() {
    return $.ajax({
        url: '/consumers/api/detail',
        method: 'GET',
        data: {
            cid: currentClusterId,
            groupId: currentGroupId,
            topic: currentTopic
        },
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            updateConsumerGroupInfo(response.data);
        } else {
            showToast(response.message || '获取消费者组信息失败', 'error');
        }
    }).fail(function () {
        showToast('获取消费者组信息失败', 'error');
    });
}

/**
 * 加载消费者组速度数据
 */
function loadConsumerSpeedData() {
    return $.ajax({
        url: '/consumers/api/speed',
        method: 'GET',
        data: {
            cid: currentClusterId,
            groupId: currentGroupId,
            topic: currentTopic
        },
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            updateSpeedDisplay(response.data);
        }
    });
}

/**
 * 加载消息积压趋势数据
 */
function loadLagTrendData() {
    const timeRange = $('#timeRangeSelect').val() || '1d';

    return $.ajax({
        url: '/consumers/api/lag-trend',
        method: 'GET',
        data: {
            cid: currentClusterId,
            groupId: currentGroupId,
            topic: currentTopic,
            timeRange: timeRange
        },
        dataType: 'json'
    }).done(function (response) {
        if (response.success && lagTrendChart) {
            updateLagTrendChart(response.data);
        }
    });
}

/**
 * 初始化消息积压趋势图表
 */
function initializeLagTrendChart() {
    const ctx = document.getElementById('lagTrendChart');
    if (!ctx) return;

    lagTrendChart = new Chart(ctx.getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: '消息积压数量',
                    data: [],
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.1)',
                    tension: 0,
                    fill: true,
                    pointRadius: 0,
                    pointHoverRadius: 0,
                    pointBackgroundColor: 'transparent',
                    pointBorderColor: 'transparent',
                    pointBorderWidth: 0,
                    borderWidth: 2,
                    borderCapStyle: 'butt',
                    borderJoinStyle: 'miter'
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
                        boxWidth: 12,
                        boxHeight: 12
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                    titleColor: '#1f2937',
                    bodyColor: '#374151',
                    borderColor: '#e5e7eb',
                    borderWidth: 1,
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${context.parsed.y.toLocaleString()}条`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        drawBorder: false
                    },
                    ticks: {
                        callback: function (value) {
                            return value.toLocaleString();
                        }
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        drawBorder: false
                    },
                    ticks: {
                        maxTicksLimit: 8, // 控制X轴标签密度
                        maxRotation: 0,
                        minRotation: 0,
                        callback: function(value, index, values) {
                            const label = this.getLabelForValue(value);
                            // 只显示小时:分钟部分
                            return label ? label.split(' ')[1] || label : '';
                        }
                    }
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            },
            elements: {
                line: {
                    borderWidth: 2,
                    borderCapStyle: 'butt',
                    borderJoinStyle: 'miter'
                },
                point: {
                    radius: 0,
                    hoverRadius: 0,
                    backgroundColor: 'transparent',
                    borderColor: 'transparent',
                    borderWidth: 0
                }
            }
        }
    });
}

/**
 * 更新消息积压趋势图表
 */
function updateLagTrendChart(data) {
    if (!lagTrendChart || !data || data.length === 0) {
        return;
    }

    try {
        // 提取时间标签和数据
        const labels = data.map(item => {
            if (item.timePoint) {
                // 格式: "2024-01-15 14:30"
                const parts = item.timePoint.split(' ');
                return parts.length > 1 ? parts[1] : parts[0]; // 只显示时间部分
            }
            // 备用：从timestamp创建时间标签
            const date = new Date(item.timestamp);
            return date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: false
            });
        });

        const lagData = data.map(item => item.totalLag || 0);

        // 更新图表数据
        lagTrendChart.data.labels = labels;
        lagTrendChart.data.datasets[0].data = lagData;

        // 使用平滑动画更新
        lagTrendChart.update('active');
    } catch (error) {
        // 静默处理错误，避免控制台污染
    }
}

/**
 * 更新消费者组信息显示
 */
function updateConsumerGroupInfo(info) {
    // Top区域的信息已经在initializePage中设置，不需要更新

    // 只更新详细信息区域
    $('#detailGroupId').text(currentGroupId || '-');
    $('#detailGroupState').text(info.state || '-');
    $('#avgOffsetRate').text(info.avgOffsetRate || '-');
    $('#totalLag').text(info.totalLag ? info.totalLag.toLocaleString() : '-');
    $('#avgLagRate').text(info.avgLagRate ? info.avgLagRate : '-');
    $('#lastUpdated').text(info.lastUpdated ? formatTimestamp(new Date(info.lastUpdated).getTime()) : '-');
}

/**
 * 更新速度显示
 */
function updateSpeedDisplay(speedData) {
    const writeSpeed = speedData.writeSpeed || 0;
    const readSpeed = speedData.readSpeed || 0;

    $('#writeSpeed').text(writeSpeed.toLocaleString() + ' 条/s');
    $('#readSpeed').text(readSpeed.toLocaleString() + ' 条/s');
}

/**
 * 刷新消费者组数据
 */
function refreshConsumerData() {
    const button = $('.refresh-btn');
    const originalHtml = button.html();

    button.html('<i class="fa fa-spin fa-sync-alt"></i> 刷新中...').prop('disabled', true);

    Promise.all([
        loadConsumerGroupInfo(),
        loadConsumerSpeedData(),
        loadLagTrendData()
    ]).then(() => {
        showToast('数据已刷新', 'success');
    }).catch(() => {
        showToast('刷新数据失败', 'error');
    }).finally(() => {
        button.html(originalHtml).prop('disabled', false);
    });
}

/**
 * 开始自动刷新
 */
function startAutoRefresh() {
    // 每30秒自动刷新一次
    refreshInterval = setInterval(() => {
        loadConsumerSpeedData();
        loadLagTrendData();
    }, 30000);
}

/**
 * 停止自动刷新
 */
function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
    }
}

// 工具函数
function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 B';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function formatTimestamp(timestamp) {
    if (!timestamp) return '-';

    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / (1000 * 60));

    if (diffMins < 1) return '刚刚';
    if (diffMins < 60) return `${diffMins}分钟前`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}小时前`;

    return date.toLocaleDateString('zh-CN') + ' ' +
        date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
}

function showToast(message, type = 'info') {
    const toast = $(`
        <div class="fixed top-4 right-4 z-50 px-4 py-3 rounded-lg text-white transform transition-all duration-300 ${type === 'success' ? 'bg-green-500' :
            type === 'error' ? 'bg-red-500' :
                type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500'
        } translate-x-full">
            <div class="flex items-center gap-2">
                <i class="fa ${type === 'success' ? 'fa-check-circle' :
            type === 'error' ? 'fa-exclamation-circle' :
                type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info-circle'
        }"></i>
                <span>${message}</span>
            </div>
        </div>
    `);

    $('body').append(toast);

    // 动画显示
    setTimeout(() => {
        toast.removeClass('translate-x-full');
    }, 100);

    // 自动隐藏
    setTimeout(() => {
        toast.addClass('translate-x-full');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 页面离开时清理资源
$(window).on('beforeunload', function () {
    stopAutoRefresh();
    if (lagTrendChart) {
        lagTrendChart.destroy();
    }
});