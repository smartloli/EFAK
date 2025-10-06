/**
 * 消费者组管理页面JavaScript
 * 提供消费者组数据管理、图表显示、操作控制等功能
 * 
 * @author smartloli
 * @since 2024-01-20
 */

// 全局变量
let idleGroupsTrendChart = null;
let consumerGroupsData = [];
let filteredData = [];
let refreshInterval = null;
let currentPage = 1;
let pageSize = 10;
let totalRecords = 0;

// 页面初始化
$(document).ready(function () {
    initializePage();
    setupEventListeners();
    loadInitialData();
    startAutoRefresh();
});

/**
 * 初始化页面
 */
function initializePage() {
    // 初始化图表
    initializeIdleGroupsTrendChart();

    // 初始化Select2组件
    initializeSelect2();

    // 初始化搜索框
    setupSearchInput();
}

/**
 * 初始化Select2组件
 */
function initializeSelect2() {
    $('#status-filter').select2({
        placeholder: '选择状态',
        allowClear: false,
        width: '100%',
        minimumResultsForSearch: Infinity, // 禁用搜索框
        templateResult: formatStatusOption,
        templateSelection: formatStatusSelection,
        escapeMarkup: function(markup) { return markup; } // 允许HTML标记
    });

    // 设置默认选中"所有状态"
    $('#status-filter').val('').trigger('change');

    // 初始化状态指示器
    updateStatusIndicator('');
}

/**
 * 格式化状态选项显示
 */
function formatStatusOption(state) {
    if (!state.id || state.id === '') {
        return $(`
            <span data-status="all" style="display: flex; align-items: center; gap: 8px;">
                <i class="fa fa-list" style="color: #6b7280; width: 14px;"></i>
                <span>所有状态</span>
            </span>
        `);
    }

    const iconMap = {
        'active': 'fa-play',
        'inactive': 'fa-pause',
        'slow': 'fa-clock',
        'lagging': 'fa-warning'
    };

    const colorMap = {
        'active': '#059669',
        'inactive': '#6b7280',
        'slow': '#d97706',
        'lagging': '#dc2626'
    };

    const icon = iconMap[state.id] || 'fa-circle';
    const color = colorMap[state.id] || '#6b7280';

    return $(`
        <span data-status="${state.id}" style="display: flex; align-items: center; gap: 8px;">
            <i class="fa ${icon}" style="color: ${color}; width: 14px;"></i>
            <span>${state.text}</span>
        </span>
    `);
}

/**
 * 格式化状态选择显示
 */
function formatStatusSelection(state) {
    if (!state.id || state.id === '') {
        return $(`
            <span style="display: flex; align-items: center; gap: 8px;">
                <i class="fa fa-list" style="color: #6b7280; width: 14px;"></i>
                <span>所有状态</span>
            </span>
        `);
    }

    const iconMap = {
        'active': 'fa-play',
        'inactive': 'fa-pause',
        'slow': 'fa-clock',
        'lagging': 'fa-warning'
    };

    const icon = iconMap[state.id] || 'fa-circle';

    return $(`
        <span style="display: flex; align-items: center; gap: 8px;">
            <i class="fa ${icon}" style="width: 14px;"></i>
            <span>${state.text}</span>
        </span>
    `);
}

/**
 * 更新状态指示器
 */
function updateStatusIndicator(status) {
    const indicator = $('#status-indicator');

    // 移除所有状态类
    indicator.removeClass('active inactive slow lagging all');

    // 根据状态添加对应的类
    if (status && status !== '') {
        indicator.addClass(status);
    } else {
        // 当选择"所有状态"时，添加默认样式
        indicator.addClass('all');
    }
}

/**
 * 设置事件监听器
 */
function setupEventListeners() {
    // 搜索功能
    $('#consumer-search').on('input debounce', function () {
        applyFilters();
    });

    // 状态过滤 - 使用Select2事件
    $('#status-filter').on('select2:select change', function () {
        const selectedValue = $(this).val();

        // 更新状态指示器
        updateStatusIndicator(selectedValue);

        // 应用过滤
        applyFilters();
    });


    // 刷新按钮
    $('#refresh-consumers').on('click', function () {
        refreshConsumerData();
    });

    // 表格排序
    $('.data-table th[data-sort]').on('click', function () {
        const sortBy = $(this).data('sort');
        const currentOrder = $(this).data('order') || 'asc';
        const newOrder = currentOrder === 'asc' ? 'desc' : 'asc';

        sortConsumerGroups(sortBy, newOrder);

        // 更新排序指示器
        $('.data-table th').removeClass('sort-asc sort-desc');
        $(this).addClass(`sort-${newOrder}`).data('order', newOrder);
    });

    // 分页控件事件
    $('#prev-page').on('click', function () {
        if (currentPage > 1) {
            currentPage--;
            loadConsumerGroups();
        }
    });

    $('#next-page').on('click', function () {
        const totalPages = Math.ceil(totalRecords / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            loadConsumerGroups();
        }
    });

    $('#page-size').on('change', function () {
        pageSize = parseInt($(this).val());
        currentPage = 1; // 重置到第一页
        loadConsumerGroups();
    });
}

/**
 * 加载初始数据
 */
function loadInitialData() {
    Promise.all([
        loadConsumerStats(),
        loadConsumerGroups(),
        loadIdleGroupsTrendData()
    ]).catch(() => {
        showToast('加载数据失败，请稍后重试', 'error');
    });
}

/**
 * 加载消费者组统计信息
 */
function loadConsumerStats() {
    const clusterId = getClusterIdFromUrl();
    if (!clusterId) {
        showToast('无法获取集群ID', 'error');
        return Promise.reject('Missing cluster ID');
    }

    return $.ajax({
        url: '/consumers/api/stats',
        method: 'GET',
        data: { cid: clusterId },
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            updateStatsDisplay(response.data);
        } else {
            showToast(response.message || '获取统计信息失败', 'error');
        }
    });
}

/**
 * 加载消费者组列表
 */
function loadConsumerGroups() {
    const clusterId = getClusterIdFromUrl();
    if (!clusterId) {
        showToast('无法获取集群ID', 'error');
        return Promise.reject('Missing cluster ID');
    }

    const params = {
        cid: clusterId,
        search: $('#consumer-search').val(),
        page: currentPage,
        pageSize: pageSize
    };

    return $.ajax({
        url: '/consumers/api/list',
        method: 'GET',
        data: params,
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            consumerGroupsData = response.data.records || [];
            filteredData = [...consumerGroupsData];
            totalRecords = response.data.total || 0;
            renderConsumerGroupsTable();
            updatePaginationInfo();
        } else {
            showToast(response.message || '加载消费者组数据失败', 'error');
        }
    }).fail(function () {
        showToast('加载消费者组数据失败', 'error');
    });
}

/**
 * 加载空闲消费者组趋势数据
 */
function loadIdleGroupsTrendData() {
    const clusterId = getClusterIdFromUrl();
    if (!clusterId) {
        return Promise.reject('Missing cluster ID');
    }

    return $.ajax({
        url: '/consumers/api/idle-groups-trend',
        method: 'GET',
        data: {
            cid: clusterId,
            timeRange: '1d' // 默认监控最近一天
        },
        dataType: 'json'
    }).done(function (response) {
        if (response.success && idleGroupsTrendChart) {
            updateIdleGroupsTrendChart(response.data);
        }
    });
}

/**
 * 初始化空闲消费者组趋势图表
 */
function initializeIdleGroupsTrendChart() {
    const ctx = document.getElementById('idleGroupsTrendChart');
    if (!ctx) return;

    idleGroupsTrendChart = new Chart(ctx.getContext('2d'), {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: '空闲消费者组',
                    data: [],
                    borderColor: '#6b7280',
                    backgroundColor: 'rgba(107, 114, 128, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 0, // 移除原点
                    pointHoverRadius: 0, // 鼠标悬停时也不显示原点
                    pointBackgroundColor: 'transparent',
                    pointBorderColor: 'transparent',
                    pointBorderWidth: 0,
                    borderWidth: 2,
                    borderCapStyle: 'round', // 使用圆角效果
                    borderJoinStyle: 'round' // 使用圆角连接
                },
                {
                    label: '活跃消费者组',
                    data: [],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 0, // 移除原点
                    pointHoverRadius: 0, // 鼠标悬停时也不显示原点
                    pointBackgroundColor: 'transparent',
                    pointBorderColor: 'transparent',
                    pointBorderWidth: 0,
                    borderWidth: 2,
                    borderCapStyle: 'round', // 使用圆角效果
                    borderJoinStyle: 'round' // 使用圆角连接
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
                            return `${context.dataset.label}: ${context.parsed.y}个`;
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
                        stepSize: 1,
                        callback: function (value) {
                            return Math.floor(value) + '个';
                        }
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        drawBorder: false
                    },
                    ticks: {
                        maxTicksLimit: 8, // 控制X轴标签密度，最多显示8个刻度
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
                    borderCapStyle: 'round', // 全局设置使用圆角
                    borderJoinStyle: 'round',
                    tension: 0.4 // 设置全局张力值
                },
                point: {
                    radius: 0, // 确保没有原点显示
                    hoverRadius: 0, // 鼠标悬停时也不显示原点
                    backgroundColor: 'transparent',
                    borderColor: 'transparent',
                    borderWidth: 0
                }
            }
        }
    });
}

/**
 * 更新空闲消费者组趋势图表
 */
function updateIdleGroupsTrendChart(data) {
    if (!idleGroupsTrendChart || !data || data.length === 0) {
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

        const idleGroupsData = data.map(item => item.idleGroups || 0);
        const activeGroupsData = data.map(item => item.activeGroups || 0);

        // 更新图表数据
        idleGroupsTrendChart.data.labels = labels;
        idleGroupsTrendChart.data.datasets[0].data = idleGroupsData;
        idleGroupsTrendChart.data.datasets[1].data = activeGroupsData;

        // 使用平滑动画更新
        idleGroupsTrendChart.update('active');

        console.debug('趋势图表更新成功，数据点数量：', data.length);
    } catch (error) {
        console.error('更新趋势图表失败：', error);
    }
}

/**
 * 更新统计信息显示
 */
function updateStatsDisplay(stats) {
    $('#total-groups').text(stats.totalGroups || 0);
    $('#active-groups').text(stats.activeGroups || 0);
    $('#avg-lag-rate').text((stats.avgLagRate || '0') + '%');
    $('#avg-consumer-rate').text((stats.avgConsumerRate || '0') + '/s');

    // 更新指标卡片
    $('#active-card-count').text(stats.activeGroups || 0);
    $('#inactive-card-count').text(stats.inactiveGroups || 0);
    $('#slow-card-count').text(stats.slowGroups || 0);
    $('#lagging-card-count').text(stats.laggingGroups || 0);
}

/**
 * 渲染消费者组表格
 */
function renderConsumerGroupsTable() {
    const tbody = $('#consumers-tbody');
    tbody.empty();

    if (filteredData.length === 0) {
        tbody.append(`
            <tr>
                <td colspan="6" class="text-center py-8 text-gray-500">
                    <div class="flex flex-col items-center">
                        <i class="fa fa-inbox text-4xl mb-4"></i>
                        <span>暂无数据</span>
                    </div>
                </td>
            </tr>
        `);
        updatePaginationInfo();
        return;
    }

    // 计算分页数据
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize, filteredData.length);
    const pageData = filteredData.slice(startIndex, endIndex);

    pageData.forEach(group => {
        const row = createConsumerGroupRow(group);
        tbody.append(row);
    });

    // 重新绑定操作按钮事件
    bindActionButtons();
    updatePaginationInfo();
}

/**
 * 创建消费者组表格行
 */
function createConsumerGroupRow(group) {
    const statusText = getStatusDisplayText(group.state, group.lagRate);
    const statusClass = getStatusDisplayClass(group.state, group.lagRate);
    const lagLevelText = group.lagLevel || '低';
    const lagClass = getLagDisplayClass(group.lagLevel);

    // 计算延迟率百分比用于进度条显示
    const lagPercentage = Math.min((group.lagRate || 0), 100);

    return $(`
        <tr class="hover:bg-gray-50" data-group-id="${group.groupId}" data-topic-name="${group.topicName || ''}">
            <td class="font-mono font-medium">${group.groupId || '-'}</td>
            <td class="text-sm">${group.topicName || '-'}</td>
            <td>
                <span class="consumer-status ${statusClass}">
                    <i class="fa fa-circle text-xs"></i>
                    ${statusText}
                </span>
            </td>
            <td class="font-mono text-left">${(group.totalLag || 0).toLocaleString()}</td>
            <td style="white-space: nowrap;">
                <div class="flex items-center gap-2">
                    <div class="lag-indicator">
                        <div class="lag-fill ${lagClass}" style="width: ${lagPercentage}%"></div>
                    </div>
                    <span class="text-xs text-gray-600">${(group.lagRate || 0).toFixed(2)}%</span>
                    <span class="text-xs text-gray-500">${lagLevelText}</span>
                </div>
            </td>
            <td>
                <div class="flex items-center gap-1">
                    <button class="action-btn btn-view-details" data-group-id="${group.groupId}" data-topic-name="${group.topicName || ''}" title="查看详情">
                        <i class="fa fa-eye"></i>
                    </button>
                    <button class="action-btn btn-reset-offset" data-group-id="${group.groupId}" data-topic-name="${group.topicName || ''}" title="重置偏移量">
                        <i class="fa fa-refresh"></i>
                    </button>
                </div>
            </td>
        </tr>
    `);
}

/**
 * 绑定操作按钮事件
 */
function bindActionButtons() {
    // 查看详情
    $('.btn-view-details').off('click').on('click', function () {
        const groupId = $(this).data('group-id');
        const topicName = $(this).data('topic-name');
        viewConsumerDetails(groupId,topicName);
    });

    // 重置偏移量
    $('.btn-reset-offset').off('click').on('click', function () {
        const groupId = $(this).data('group-id');
        const topicName = $(this).data('topic-name');
        showResetOffsetDialog(groupId, topicName);
    });
}

/**
 * 应用过滤条件
 */
function applyFilters() {
    // 重新加载数据，因为现在是服务端分页和过滤
    currentPage = 1; // 重置到第一页
    loadConsumerGroups();
}

/**
 * 排序消费者组
 */
function sortConsumerGroups(sortBy, order) {
    filteredData.sort((a, b) => {
        let valueA = a[sortBy];
        let valueB = b[sortBy];

        // 处理不同数据类型
        if (typeof valueA === 'string') {
            valueA = valueA.toLowerCase();
            valueB = valueB.toLowerCase();
        }

        if (order === 'asc') {
            return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
        } else {
            return valueA > valueB ? -1 : valueA < valueB ? 1 : 0;
        }
    });

    renderConsumerGroupsTable();
}

/**
 * 更新分页信息显示
 */
function updatePaginationInfo() {
    const startRecord = totalRecords === 0 ? 0 : (currentPage - 1) * pageSize + 1;
    const endRecord = Math.min(currentPage * pageSize, totalRecords);
    const totalPages = Math.ceil(totalRecords / pageSize);

    $('#page-start').text(startRecord);
    $('#page-end').text(endRecord);
    $('#total-records').text(totalRecords);

    // 更新分页按钮状态
    $('#prev-page').prop('disabled', currentPage <= 1);
    $('#next-page').prop('disabled', currentPage >= totalPages || totalRecords === 0);
}


/**
 * 查看消费者组详情
 */
function viewConsumerDetails(groupId,topicName) {
    // 跳转到消费者详情页面
    const clusterId = getClusterIdFromUrl();
    window.location.href = `/consumer/view?group=${encodeURIComponent(groupId)}&topic=${encodeURIComponent(topicName)}&cid=${clusterId}`;
}

/**
 * 显示重置偏移量对话框
 */
function showResetOffsetDialog(groupId, topicName) {
    const dialog = $(`
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg p-6 w-96 max-w-md">
                <h3 class="text-lg font-semibold mb-4">重置消费者组偏移量</h3>
                <div class="mb-4">
                    <label class="block text-sm font-medium mb-2">重置类型</label>
                    <select id="reset-type" class="w-full">
                        <option value="earliest">重置到最早</option>
                        <option value="latest">重置到最新</option>
                        <option value="specific">指定偏移量</option>
                        <option value="timestamp">指定时间</option>
                    </select>
                </div>
                <div id="specific-offset" class="mb-4 hidden">
                    <label class="block text-sm font-medium mb-2">偏移量值</label>
                    <input type="number" id="offset-value" class="w-full p-2 border border-gray-300 rounded" placeholder="输入偏移量">
                </div>
                <div id="timestamp-input" class="mb-4 hidden">
                    <label class="block text-sm font-medium mb-2">时间</label>
                    <input type="datetime-local" id="timestamp-value" class="w-full p-2 border border-gray-300 rounded focus:border-blue-500 focus:outline-none"
                           step="1"
                           style="color-scheme: light;">
                    <div class="text-xs text-gray-500 mt-1">请选择要重置到的具体时间点</div>
                </div>
                <div class="flex justify-end gap-2">
                    <button id="cancel-reset" class="px-4 py-2 text-gray-600 border border-gray-300 rounded hover:bg-gray-50">取消</button>
                    <button id="confirm-reset" class="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600">确认重置</button>
                </div>
            </div>
        </div>
    `);

    $('body').append(dialog);

    // 初始化Select2组件
    $('#reset-type').select2({
        placeholder: '选择重置类型',
        allowClear: false,
        width: '100%',
        minimumResultsForSearch: Infinity, // 禁用搜索框
        templateResult: formatResetTypeOption,
        templateSelection: formatResetTypeSelection,
        escapeMarkup: function(markup) { return markup; }
    });

    // 事件绑定
    $('#reset-type').on('select2:select change', function () {
        const selectedValue = $(this).val();
        if (selectedValue === 'specific') {
            $('#specific-offset').removeClass('hidden');
            $('#timestamp-input').addClass('hidden');
        } else if (selectedValue === 'timestamp') {
            $('#timestamp-input').removeClass('hidden');
            $('#specific-offset').addClass('hidden');

            // 设置默认时间为当前时间
            const now = new Date();
            // 格式化为 YYYY-MM-DDTHH:MM:SS 格式
            const formattedTime = now.getFullYear() + '-' +
                String(now.getMonth() + 1).padStart(2, '0') + '-' +
                String(now.getDate()).padStart(2, '0') + 'T' +
                String(now.getHours()).padStart(2, '0') + ':' +
                String(now.getMinutes()).padStart(2, '0') + ':' +
                String(now.getSeconds()).padStart(2, '0');
            $('#timestamp-value').val(formattedTime);

            // 确保时间输入框可以获得焦点
            setTimeout(() => {
                $('#timestamp-value').focus();
            }, 100);
        } else {
            $('#specific-offset').addClass('hidden');
            $('#timestamp-input').addClass('hidden');
        }
    });

    $('#cancel-reset').on('click', function () {
        dialog.remove();
    });

    // 为时间输入框添加额外的事件处理
    $(document).on('click', '#timestamp-value', function() {
        // 确保在点击时能够显示时间选择器
        this.showPicker && this.showPicker();
    });

    // 为时间输入框添加焦点事件
    $(document).on('focus', '#timestamp-value', function() {
        // 在某些浏览器中，焦点可能需要触发选择器显示
        if (this.showPicker) {
            setTimeout(() => {
                this.showPicker();
            }, 50);
        }
    });

    $('#confirm-reset').on('click', function () {
        const resetType = $('#reset-type').val();
        const offsetValue = $('#offset-value').val();
        const timestampValue = $('#timestamp-value').val();

        resetConsumerGroupOffset(groupId, topicName, resetType, offsetValue, timestampValue);
        dialog.remove();
    });
}

/**
 * 格式化重置类型选项显示
 */
function formatResetTypeOption(state) {
    if (!state.id) {
        return state.text;
    }

    const iconMap = {
        'earliest': 'fa-fast-backward',
        'latest': 'fa-fast-forward',
        'specific': 'fa-edit',
        'timestamp': 'fa-clock'
    };

    const colorMap = {
        'earliest': '#059669',
        'latest': '#3b82f6',
        'specific': '#f59e0b',
        'timestamp': '#8b5cf6'
    };

    const icon = iconMap[state.id] || 'fa-circle';
    const color = colorMap[state.id] || '#6b7280';

    return $(`
        <span style="display: flex; align-items: center; gap: 8px;">
            <i class="fa ${icon}" style="color: ${color}; width: 14px;"></i>
            <span>${state.text}</span>
        </span>
    `);
}

/**
 * 格式化重置类型选择显示
 */
function formatResetTypeSelection(state) {
    if (!state.id) {
        return state.text;
    }

    const iconMap = {
        'earliest': 'fa-fast-backward',
        'latest': 'fa-fast-forward',
        'specific': 'fa-edit',
        'timestamp': 'fa-clock'
    };

    const icon = iconMap[state.id] || 'fa-circle';

    return $(`
        <span style="display: flex; align-items: center; gap: 8px;">
            <i class="fa ${icon}" style="width: 14px;"></i>
            <span>${state.text}</span>
        </span>
    `);
}

/**
 * 重置消费者组偏移量
 */
function resetConsumerGroupOffset(groupId, topicName, resetType, offsetValue, timestampValue) {
    // 获取集群ID
    const clusterId = getClusterIdFromUrl();
    if (!clusterId) {
        showToast('无法获取集群ID', 'error');
        return;
    }

    // 构建请求数据
    const requestData = {
        cid: clusterId,
        groupId: groupId,
        topic: topicName,
        resetType: resetType
    };

    // 根据重置类型设置重置值
    if (resetType === 'specific' && offsetValue) {
        requestData.offsetValue = parseInt(offsetValue);
    } else if (resetType === 'timestamp' && timestampValue) {
        // 将datetime-local格式转换为时间戳（毫秒）
        requestData.timestamp = new Date(timestampValue).getTime();
    }

    // 发送重置请求
    $.ajax({
        url: '/consumers/api/reset-offset',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            showToast(`消费者组 ${groupId} 偏移量重置成功`, 'success');
            loadConsumerGroups();
        } else {
            showToast(response.message || '重置偏移量失败', 'error');
        }
    }).fail(function () {
        showToast('重置偏移量失败', 'error');
    });
}

/**
 * 停止消费者组
 */
function stopConsumerGroup(groupId) {
    if (!confirm(`确定要停止消费者组 ${groupId} 吗？`)) {
        return;
    }

    $.ajax({
        url: `/consumers/api/groups/${groupId}/stop`,
        method: 'POST',
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            showToast(`消费者组 ${groupId} 已停止`, 'success');
            loadConsumerGroups();
        } else {
            showToast(response.message || '停止消费者组失败', 'error');
        }
    }).fail(function () {
        showToast('停止消费者组失败', 'error');
    });
}

/**
 * 启动消费者组
 */
function startConsumerGroup(groupId) {
    $.ajax({
        url: `/consumers/api/groups/${groupId}/start`,
        method: 'POST',
        dataType: 'json'
    }).done(function (response) {
        if (response.success) {
            showToast(`消费者组 ${groupId} 已启动`, 'success');
            loadConsumerGroups();
        } else {
            showToast(response.message || '启动消费者组失败', 'error');
        }
    }).fail(function () {
        showToast('启动消费者组失败', 'error');
    });
}

/**
 * 刷新消费者数据
 */
function refreshConsumerData() {
    const button = $('#refresh-consumers');
    const originalText = button.html();

    button.html('<i class="fa fa-spin fa-refresh"></i> 刷新中...').prop('disabled', true);

    Promise.all([
        loadConsumerStats(),
        loadConsumerGroups(),
        loadIdleGroupsTrendData()
    ]).then(() => {
        showToast('数据已刷新', 'success');
    }).catch(() => {
        showToast('刷新数据失败', 'error');
    }).finally(() => {
        button.html(originalText).prop('disabled', false);
    });
}

/**
 * 开始自动刷新
 */
function startAutoRefresh() {
    // 每30秒自动刷新一次
    refreshInterval = setInterval(() => {
        loadIdleGroupsTrendData();
        loadConsumerStats();
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

/**
 * 设置搜索输入防抖
 */
function setupSearchInput() {
    let searchTimeout;
    $('#consumer-search').on('input', function () {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            $(this).trigger('debounce');
        }, 300);
    });
}

// 工具函数
function getStatusDisplayText(state, lagRate) {
    // 首先根据状态判断
    if (state === 'STABLE') {
        // 活跃状态下根据延迟率判断细分状态
        if (lagRate > 10) {
            return '延迟';
        } else if (lagRate > 0 && lagRate <= 10) {
            return '缓慢';
        } else {
            return '活跃';
        }
    } else if (state === 'EMPTY') {
        return '空闲';
    } else {
        return state || '未知';
    }
}

function getStatusDisplayClass(state, lagRate) {
    if (state === 'STABLE') {
        if (lagRate > 10) {
            return 'lagging';
        } else if (lagRate > 0 && lagRate <= 10) {
            return 'slow';
        } else {
            return 'active';
        }
    } else if (state === 'EMPTY') {
        return 'inactive';
    } else {
        return 'inactive';
    }
}

function getLagDisplayClass(lagLevel) {
    const lagMap = {
        '低': 'low',
        '缓慢': 'medium',
        '延迟': 'high'
    };
    return lagMap[lagLevel] || 'low';
}

function getStatusClass(status) {
    const statusMap = {
        'active': 'active',
        'inactive': 'inactive',
        'slow': 'slow',
        'lagging': 'lagging'
    };
    return statusMap[status] || 'inactive';
}

function getStatusText(status) {
    const statusMap = {
        'active': '活跃',
        'inactive': '空闲',
        'slow': '缓慢',
        'lagging': '延迟'
    };
    return statusMap[status] || '未知';
}

function getLagClass(lagLevel) {
    const lagMap = {
        'low': 'low',
        'medium': 'medium',
        'high': 'high'
    };
    return lagMap[lagLevel] || 'low';
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

/**
 * 从URL中获取集群ID
 */
function getClusterIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('cid');
}

// 页面离开时清理资源
$(window).on('beforeunload', function () {
    stopAutoRefresh();
    if (idleGroupsTrendChart) {
        idleGroupsTrendChart.destroy();
    }
}); 