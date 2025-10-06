let messageFlowChart;
let currentPartitionPage = 1;
let partitionPageSize = 10;
let allPartitions = [];
let totalPartitions = 0;
let currentConsumerGroupPage = 1;
let consumerGroupPageSize = 5;
let allConsumerGroups = [];
let totalConsumerGroups = 0;
let totalConsumerGroupPages = 0;

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function () {
    // 初始化Select2
    $('#timeRangeSelect').select2({
        minimumResultsForSearch: Infinity,
        width: '100%'
    });

    const urlParams = new URLSearchParams(window.location.search);
    const topicName = urlParams.get('name') || document.getElementById('topicName').textContent;

    if (!topicName || topicName === '主题加载中...') {
        window.location.href = '/topics';
        return;
    }

    initializeTopicDetail(topicName);
});

// 初始化主题详情
async function initializeTopicDetail(topicName, timeRange = '1d') {
    showLoading(true);

    try {
        // 获取URL参数中的集群ID
        const urlParams = new URLSearchParams(window.location.search);
        const clusterId = urlParams.get('cid');

        // 并行获取主题详情数据
        const [topicInfo, partitionResult, messageFlow, consumerGroups, configInfo] = await Promise.all([
            fetchTopicInfo(topicName, clusterId),
            fetchPartitionInfo(topicName, clusterId),
            fetchMessageFlow(topicName, clusterId, timeRange),
            fetchConsumerGroups(topicName, clusterId),
            fetchTopicConfig(topicName, clusterId)
        ]);

        updateTopicInfo(topicInfo);
        updatePartitionTable(partitionResult.data, partitionResult.total);
        initializeMessageFlowChart(messageFlow);
        updateConsumerGroups(consumerGroups.data, consumerGroups.total, consumerGroups.totalPages);
        updateConfigInfo(configInfo);
    } catch (error) {
        showErrorMessage('加载主题详情失败，请重试');
    } finally {
        showLoading(false);
    }
}

// API调用函数
async function fetchTopicInfo(topicName, clusterId) {
    const params = new URLSearchParams();
    if (clusterId) params.append('clusterId', clusterId);

    const response = await fetch(`/topic/api/info/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取主题信息失败');
    }

    return result.data;
}

async function fetchPartitionInfo(topicName, clusterId) {
    const params = new URLSearchParams();
    if (clusterId) params.append('clusterId', clusterId);

    // 添加分页参数
    const start = (currentPartitionPage - 1) * partitionPageSize;
    params.append('start', start);
    params.append('length', partitionPageSize);

    const response = await fetch(`/topic/api/partitions/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取分区信息失败');
    }

    return {
        data: result.data,
        total: result.total
    };
}

async function fetchMessageFlow(topicName, clusterId, timeRange) {
    const params = new URLSearchParams({
        timeRange: timeRange
    });
    if (clusterId) params.append('clusterId', clusterId);

    const response = await fetch(`/topic/api/flow/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取消息流量失败');
    }

    return result.data;
}

async function fetchConsumerGroups(topicName, clusterId) {
    const params = new URLSearchParams({
        page: currentConsumerGroupPage,
        pageSize: consumerGroupPageSize
    });
    if (clusterId) params.append('clusterId', clusterId);

    const response = await fetch(`/topic/api/consumer-groups/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取消费者组失败');
    }

    return {
        data: result.data || [],
        total: result.total || 0,
        page: result.page || 1,
        pageSize: result.pageSize || 5,
        totalPages: result.totalPages || 0
    };
}

async function fetchTopicConfig(topicName, clusterId) {
    const params = new URLSearchParams();
    if (clusterId) params.append('clusterId', clusterId);

    const response = await fetch(`/topic/api/config/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取主题配置失败');
    }

    return result.data;
}

// 更新主题基本信息
function updateTopicInfo(data) {
    document.getElementById('topicName').textContent = data.topicName;
    document.getElementById('partitionCount').textContent = data.partitionCount;
    document.getElementById('replicationFactor').textContent = data.replicationFactor;

    document.getElementById('messageCount').textContent = formatNumber(data.totalRecords);
    document.getElementById('messageSize').textContent = formatSize(data.totalSize);
    document.getElementById('writeSpeed').textContent = formatSpeedMBps(data.writeSpeed);
    document.getElementById('readSpeed').textContent = formatSpeedMBps(data.readSpeed);
}

// 更新分区表格
function updatePartitionTable(partitions, total) {
    allPartitions = partitions || [];
    // 设置总数用于分页计算
    totalPartitions = total || 0;
    renderPartitionTable();
}

// 渲染分区表格
function renderPartitionTable() {
    const tbody = document.getElementById('partitionTableBody');
    tbody.innerHTML = '';

    if (!allPartitions || allPartitions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">
                    <i class="fa fa-inbox"></i>
                    <div>暂无分区信息</div>
                </td>
            </tr>
        `;
        updatePartitionPagination();
        return;
    }

    // 直接渲染当前页的数据（已从后端分页获取）
    allPartitions.forEach(partition => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${partition.partitionId}</strong></td>
            <td>
                <span class="status-badge leader">
                    <i class="fa fa-crown"></i>
                    Broker ${partition.leader}
                </span>
            </td>
            <td>${partition.replicas || '-'}</td>
            <td>${partition.isr || '-'}</td>
            <td>${formatNumber(partition.logSize || 0)}</td>
            <td>
                <span class="status-badge ${partition.preferredLeader ? 'stable' : 'rebalancing'}">
                    <i class="fa ${partition.preferredLeader ? 'fa-check-circle' : 'fa-exclamation-triangle'}"></i>
                    ${partition.preferredLeader ? '正常' : '异常'}
                </span>
            </td>
            <td>
                <span class="status-badge ${partition.underReplicated ? 'rebalancing' : 'stable'}">
                    <i class="fa ${partition.underReplicated ? 'fa-exclamation-triangle' : 'fa-check-circle'}"></i>
                    ${partition.underReplicated ? '异常' : '正常'}
                </span>
            </td>
            <td>
                <div class="custom-tooltip">
                    <button class="text-blue-600 hover:text-blue-800 p-1" onclick="previewMessages(${partition.partitionId})">
                        <i class="fa fa-eye"></i>
                    </button>
                    <div class="tooltip-content">预览最近消息</div>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });

    updatePartitionPagination();
}

// 更新分区分页信息
function updatePartitionPagination() {
    const totalItems = totalPartitions;
    const totalPages = Math.ceil(totalItems / partitionPageSize);
    const startIndex = (currentPartitionPage - 1) * partitionPageSize + 1;
    const endIndex = Math.min(currentPartitionPage * partitionPageSize, totalItems);

    // 更新分页信息
    document.getElementById('partitionPaginationInfo').textContent =
        `显示 ${startIndex} - ${endIndex} 条，共 ${totalItems} 条`;

    // 更新分页按钮
    document.getElementById('partitionPrevBtn').disabled = currentPartitionPage <= 1;
    document.getElementById('partitionNextBtn').disabled = currentPartitionPage >= totalPages;

    // 更新页码
    const pageNumbers = document.getElementById('partitionPageNumbers');
    pageNumbers.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPartitionPage - 1 && i <= currentPartitionPage + 1)) {
            const button = document.createElement('button');
            button.textContent = i;
            button.onclick = () => goToPartitionPage(i);
            if (i === currentPartitionPage) {
                button.classList.add('active');
            }
            pageNumbers.appendChild(button);
        } else if ((i === currentPartitionPage - 2 && i > 1) || (i === currentPartitionPage + 2 && i < totalPages)) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.className = 'px-2 text-gray-400';
            pageNumbers.appendChild(ellipsis);
        }
    }
}

// 分区分页操作
async function changePartitionPage(direction) {
    const totalPages = Math.ceil(totalPartitions / partitionPageSize);
    if (direction === 'prev' && currentPartitionPage > 1) {
        currentPartitionPage--;
    } else if (direction === 'next' && currentPartitionPage < totalPages) {
        currentPartitionPage++;
    }
    await refreshPartitionData();
}

async function goToPartitionPage(page) {
    currentPartitionPage = page;
    await refreshPartitionData();
}

// 刷新分区数据
async function refreshPartitionData() {
    const topicName = document.getElementById('topicName').textContent;
    const urlParams = new URLSearchParams(window.location.search);
    const clusterId = urlParams.get('cid');

    try {
        const partitionResult = await fetchPartitionInfo(topicName, clusterId);
        updatePartitionTable(partitionResult.data, partitionResult.total);
    } catch (error) {
        console.error('刷新分区数据失败:', error);
        showErrorMessage('刷新分区数据失败，请重试');
    }
}

// 初始化消息流量图表
function initializeMessageFlowChart(messageFlow) {
    const ctx = document.getElementById('messageFlowChart');

    if (messageFlowChart) {
        messageFlowChart.destroy();
    }

    // 存储数据供tooltip使用
    window.currentMessageFlowData = messageFlow;

    messageFlowChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: messageFlow.map(point => formatTimeForChart(point.timestamp)),
            datasets: [{
                label: '生产消息数',
                data: messageFlow.map(point => point.produced),
                borderColor: '#165DFF',
                backgroundColor: 'rgba(22, 93, 255, 0.08)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.4,
                pointRadius: 0, // 移除原点
                pointHoverRadius: 6, // 悬停时显示点
                pointHoverBackgroundColor: '#165DFF',
                pointHoverBorderColor: '#FFFFFF',
                pointHoverBorderWidth: 3,
                // 添加渐变效果
                segment: {
                    borderColor: ctx => {
                        const gradient = ctx.chart.ctx.createLinearGradient(0, 0, 0, ctx.chart.height);
                        gradient.addColorStop(0, '#165DFF');
                        gradient.addColorStop(1, '#4F8AFF');
                        return gradient;
                    }
                }
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            elements: {
                line: {
                    borderJoinStyle: 'round',
                    borderCapStyle: 'round'
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        font: {
                            size: 12,
                            weight: '500'
                        },
                        color: '#374151'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    titleColor: '#FFFFFF',
                    bodyColor: '#FFFFFF',
                    borderColor: '#165DFF',
                    borderWidth: 1,
                    cornerRadius: 12,
                    displayColors: true,
                    padding: 12,
                    titleFont: {
                        size: 13,
                        weight: '600'
                    },
                    bodyFont: {
                        size: 12,
                        weight: '400'
                    },
                    callbacks: {
                        title: function(context) {
                            // 在tooltip中显示完整的时间信息
                            const data = window.currentMessageFlowData;
                            if (data && data[context[0].dataIndex]) {
                                const timestamp = data[context[0].dataIndex].timestamp;
                                return `时间: ${formatDate(timestamp)}`;
                            }
                            return `时间: ${context[0].label}`;
                        },
                        label: function (context) {
                            return `${context.dataset.label}: ${formatNumber(context.parsed.y)} 条`;
                        }
                    },
                    animation: {
                        duration: 200
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: true,
                        color: 'rgba(148, 163, 184, 0.1)',
                        lineWidth: 1
                    },
                    ticks: {
                        color: '#64748B',
                        font: {
                            size: 11,
                            weight: '400'
                        },
                        maxTicksLimit: 6,
                        maxRotation: 0,
                        minRotation: 0,
                        autoSkip: true,
                        autoSkipPadding: 15,
                        callback: function(value, index, values) {
                            // 智能显示时间标签，避免过于密集
                            const totalLabels = values.length;
                            if (totalLabels <= 6) {
                                // 如果标签数量少于等于6个，显示所有
                                return this.getLabelForValue(value);
                            } else {
                                // 如果标签数量多，智能跳过显示
                                const step = Math.ceil(totalLabels / 5);
                                if (index % step === 0 || index === totalLabels - 1) {
                                    return this.getLabelForValue(value);
                                }
                                return '';
                            }
                        }
                    },
                    border: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        display: true,
                        color: 'rgba(148, 163, 184, 0.1)',
                        lineWidth: 1,
                        drawTicks: false
                    },
                    ticks: {
                        color: '#64748B',
                        font: {
                            size: 11,
                            weight: '400'
                        },
                        padding: 8,
                        callback: function (value) {
                            return formatNumber(value);
                        }
                    },
                    border: {
                        display: false
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            },
            hover: {
                animationDuration: 200
            },
            animation: {
                duration: 800,
                easing: 'easeInOutQuart'
            }
        }
    });
}

// 更新消费者组信息
function updateConsumerGroups(groups, total, totalPages) {
    allConsumerGroups = groups || [];
    totalConsumerGroups = total || 0;
    totalConsumerGroupPages = totalPages || 0;
    renderConsumerGroupTable();
}

// 渲染消费者组表格
function renderConsumerGroupTable() {
    const container = document.getElementById('consumerGroupsContent');
    const paginationContainer = document.getElementById('consumerGroupsPagination');

    if (!allConsumerGroups || allConsumerGroups.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa fa-users-slash"></i>
                <div>暂无消费者组信息</div>
            </div>
        `;
        paginationContainer.style.display = 'none';
        return;
    }

    // 直接渲染当前页的数据（已从后端分页获取）
    const table = document.createElement('table');
    table.className = 'partition-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>消费者组</th>
                <th>主题名称</th>
                <th>Logsize</th>
                <th>Offsets</th>
                <th>Lag</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            ${allConsumerGroups.map(group => `
                <tr>
                    <td><strong>${group.groupId}</strong></td>
                    <td><span class="text-gray-600">${group.topicName}</span></td>
                    <td>
                        <span class="font-medium text-gray-900">
                            ${formatNumber(group.logsize || 0)}
                        </span>
                    </td>
                    <td>
                        <span class="font-medium text-gray-900">
                            ${formatNumber(group.offsets || 0)}
                        </span>
                    </td>
                    <td>
                        <span class="${group.lag > 100 ? 'text-red-600 font-semibold' : 'text-green-600'}">
                            ${formatNumber(group.lag || 0)} 条
                        </span>
                    </td>
                    <td>
                        <span class="status-badge ${getConsumerGroupStatusClass(group.stateCode)}">
                            <i class="fa ${getConsumerGroupStatusIcon(group.stateCode)}"></i>
                            ${group.state}
                        </span>
                    </td>
                    <td>
                        <div class="custom-tooltip">
                            <button class="text-blue-600 hover:text-blue-800 p-1" onclick="goToConsumerGroupAdjust('${group.groupId}', '${group.topicName}')">
                                <i class="fa-solid fa-arrow-up-right-from-square"></i>
                            </button>
                            <div class="tooltip-content">跳转消费者组</div>
                        </div>
                    </td>
                </tr>
            `).join('')}
        </tbody>
    `;
    container.innerHTML = '';
    container.appendChild(table);

    paginationContainer.style.display = 'flex';
    updateConsumerGroupPagination();
}

// 更新消费者组分页信息
function updateConsumerGroupPagination() {
    const totalItems = totalConsumerGroups;
    const totalPages = totalConsumerGroupPages;
    const startIndex = (currentConsumerGroupPage - 1) * consumerGroupPageSize + 1;
    const endIndex = Math.min(currentConsumerGroupPage * consumerGroupPageSize, totalItems);

    // 更新分页信息
    document.getElementById('consumerGroupsPaginationInfo').textContent =
        `显示 ${startIndex} - ${endIndex} 条，共 ${totalItems} 条`;

    // 更新分页按钮
    document.getElementById('consumerGroupPrevBtn').disabled = currentConsumerGroupPage <= 1;
    document.getElementById('consumerGroupNextBtn').disabled = currentConsumerGroupPage >= totalPages;

    // 更新页码
    const pageNumbers = document.getElementById('consumerGroupPageNumbers');
    pageNumbers.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentConsumerGroupPage - 1 && i <= currentConsumerGroupPage + 1)) {
            const button = document.createElement('button');
            button.textContent = i;
            button.onclick = () => goToConsumerGroupPage(i);
            if (i === currentConsumerGroupPage) {
                button.classList.add('active');
            }
            pageNumbers.appendChild(button);
        } else if ((i === currentConsumerGroupPage - 2 && i > 1) || (i === currentConsumerGroupPage + 2 && i < totalPages)) {
            const ellipsis = document.createElement('span');
            ellipsis.textContent = '...';
            ellipsis.className = 'px-2 text-gray-400';
            pageNumbers.appendChild(ellipsis);
        }
    }
}

// 消费者组分页操作
async function changeConsumerGroupPage(direction) {
    const totalPages = totalConsumerGroupPages;
    if (direction === 'prev' && currentConsumerGroupPage > 1) {
        currentConsumerGroupPage--;
    } else if (direction === 'next' && currentConsumerGroupPage < totalPages) {
        currentConsumerGroupPage++;
    }
    await refreshConsumerGroupData();
}

async function goToConsumerGroupPage(page) {
    currentConsumerGroupPage = page;
    await refreshConsumerGroupData();
}

// 刷新消费者组数据
async function refreshConsumerGroupData() {
    const topicName = document.getElementById('topicName').textContent;
    const urlParams = new URLSearchParams(window.location.search);
    const clusterId = urlParams.get('cid');

    try {
        const consumerGroupsResult = await fetchConsumerGroups(topicName, clusterId);
        updateConsumerGroups(consumerGroupsResult.data, consumerGroupsResult.total, consumerGroupsResult.totalPages);
    } catch (error) {
        console.error('刷新消费者组数据失败:', error);
        showErrorMessage('刷新消费者组数据失败，请重试');
    }
}

// 获取消费者组状态样式类
function getConsumerGroupStatusClass(stateCode) {
    switch (stateCode) {
        case 'STABLE':
            return 'stable';
        case 'EMPTY':
            return 'follower'; // 使用蓝色样式表示空闲
        case 'DEAD':
            return 'rebalancing'; // 使用黄色样式表示停止
        default:
            return 'follower';
    }
}

// 获取消费者组状态图标
function getConsumerGroupStatusIcon(stateCode) {
    switch (stateCode) {
        case 'STABLE':
            return 'fa-check-circle';
        case 'EMPTY':
            return 'fa-clock';
        case 'DEAD':
            return 'fa-times-circle';
        default:
            return 'fa-question-circle';
    }
}

// 跳转到消费者组调整页面
function goToConsumerGroupAdjust(groupId, topicName) {
    const urlParams = new URLSearchParams(window.location.search);
    const clusterId = urlParams.get('cid');
    const adjustUrl = `/consumer/view?group=${encodeURIComponent(groupId)}&topic=${encodeURIComponent(topicName)}&cid=${encodeURIComponent(clusterId)}`;
    window.open(adjustUrl, '_blank');
}

// 跳转到消费者组详情
function goToConsumerGroupDetail(groupId) {
    window.open(`/consumer-group/detail?groupId=${encodeURIComponent(groupId)}`, '_blank');
}

// 更新配置信息
function updateConfigInfo(configs) {
    const container = document.getElementById('configContent');

    if (!configs || Object.keys(configs).length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa fa-cog"></i>
                <div>暂无配置信息</div>
            </div>
        `;
        return;
    }

    const configList = Object.entries(configs).map(([key, value]) => `
        <div class="flex justify-between items-center py-3 px-4 border-b border-gray-100 hover:bg-gray-50">
            <div class="font-medium text-gray-700">${key}</div>
            <div class="text-gray-900 bg-gray-100 px-3 py-1 rounded-lg font-mono text-sm">${value}</div>
        </div>
    `).join('');

    container.innerHTML = `
        <div class="border border-gray-200 rounded-lg overflow-hidden">
            ${configList}
        </div>
    `;
}

// 刷新主题数据
async function refreshTopicData() {
    const topicName = document.getElementById('topicName').textContent;
    const timeRange = $('#timeRangeSelect').val();
    await initializeTopicDetail(topicName, timeRange);
}

// 更新时间范围
async function updateTimeRange() {
    const topicName = document.getElementById('topicName').textContent;
    const timeRange = $('#timeRangeSelect').val();

    showLoading(true);
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const clusterId = urlParams.get('cid');

        const messageFlow = await fetchMessageFlow(topicName, clusterId, timeRange);
        initializeMessageFlowChart(messageFlow);
    } catch (error) {
        console.error('更新时间范围失败:', error);
        showErrorMessage('更新图表数据失败，请重试');
    } finally {
        showLoading(false);
    }
}

// 切换配置区域显示/隐藏
function toggleConfigSection() {
    const content = document.getElementById('configContent');
    const icon = document.getElementById('configToggleIcon');

    if (content.classList.contains('hidden')) {
        content.classList.remove('hidden');
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-up');
    } else {
        content.classList.add('hidden');
        icon.classList.remove('fa-chevron-up');
        icon.classList.add('fa-chevron-down');
    }
}

// 预览消息
async function previewMessages(partitionId) {
    const topicName = document.getElementById('topicName').textContent;
    document.getElementById('previewTopicName').textContent = topicName;
    document.getElementById('previewPartitionId').textContent = partitionId;

    // 显示对话框
    document.getElementById('messagePreviewModal').classList.add('active');

    try {
        // 获取集群ID
        const urlParams = new URLSearchParams(window.location.search);
        const clusterId = urlParams.get('cid');

        // 调用API获取消息数据
        const messages = await fetchPartitionMessages(topicName, partitionId, clusterId);
        renderMessageList(messages);
    } catch (error) {
        console.error('加载消息失败:', error);
        renderMessageList([]); // 显示空状态
    }
}

// 获取分区消息
async function fetchPartitionMessages(topicName, partitionId, clusterId) {
    const params = new URLSearchParams({
        partition: partitionId,
        limit: 10 // 限制最多显示10条消息
    });
    if (clusterId) params.append('clusterId', clusterId);

    const response = await fetch(`/topic/api/messages/${encodeURIComponent(topicName)}?${params}`);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '获取消息失败');
    }

    return result.data || [];
}

// 关闭消息预览对话框
function closeMessagePreview() {
    document.getElementById('messagePreviewModal').classList.remove('active');
}

// 点击遮罩层关闭对话框
function closeMessagePreviewOnOverlay(event) {
    if (event.target.classList.contains('modal-overlay')) {
        closeMessagePreview();
    }
}

// 渲染消息列表
function renderMessageList(messages) {
    const container = document.getElementById('messageList');

    if (!messages || messages.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8">
                <i class="fa fa-inbox text-2xl text-gray-400"></i>
                <div class="mt-2 text-gray-600">暂无消息数据</div>
            </div>
        `;
        return;
    }

    container.innerHTML = messages.map((message, index) => `
        <div class="message-item">
            <div class="message-header">
                <span><strong>Offset:</strong> ${message.offset || 'N/A'}</span>
                <span><strong>时间:</strong> ${message.timestamp ? formatDate(message.timestamp) : 'N/A'}</span>
            </div>
            ${message.key ? `
                <div class="mb-2">
                    <div class="text-sm font-medium text-gray-700 mb-1">Key:</div>
                    <div class="message-content">${message.key}</div>
                </div>
            ` : ''}
            <div>
                <div class="text-sm font-medium text-gray-700 mb-1 flex justify-between items-center">
                    <span>Value:</span>
                    <div class="flex gap-2">
                        <button class="text-blue-600 hover:text-blue-800 text-xs" onclick="formatJson(${index})">
                            <i class="fa fa-code"></i> 格式化JSON
                        </button>
                        <button class="text-gray-600 hover:text-gray-800 text-xs" onclick="copyMessageValue(${index})">
                            <i class="fa fa-copy"></i> 复制
                        </button>
                    </div>
                </div>
                <div class="message-content" id="message-content-${index}">${message.value || 'N/A'}</div>
            </div>
        </div>
    `).join('');
}

// 格式化JSON
function formatJson(messageIndex) {
    const contentElement = document.getElementById(`message-content-${messageIndex}`);
    const content = contentElement.textContent;

    try {
        // 尝试解析JSON
        const parsed = JSON.parse(content);
        const formatted = JSON.stringify(parsed, null, 2);
        contentElement.textContent = formatted;
        contentElement.style.whiteSpace = 'pre-wrap';
        contentElement.style.fontSize = '0.8rem';
        contentElement.style.lineHeight = '1.4';

        // 添加语法高亮效果
        contentElement.style.backgroundColor = '#f8f9fa';
        contentElement.style.border = '1px solid #e9ecef';
        contentElement.style.borderRadius = '0.375rem';
        contentElement.style.padding = '1rem';

        // 显示成功提示
        const button = contentElement.previousElementSibling.querySelectorAll('button')[0];
        if (button) {
            button.innerHTML = '<i class="fa fa-check"></i> 已格式化';
            button.className = 'text-green-600 hover:text-green-800 text-xs';
            setTimeout(() => {
                button.innerHTML = '<i class="fa fa-code"></i> 格式化JSON';
                button.className = 'text-blue-600 hover:text-blue-800 text-xs';
            }, 2000);
        }
    } catch (e) {
        // 如果解析失败，尝试其他格式化方法
        try {
            // 尝试移除可能的转义字符
            let cleanedContent = content.replace(/\\"/g, '"').replace(/\\n/g, '\n');
            const parsed = JSON.parse(cleanedContent);
            const formatted = JSON.stringify(parsed, null, 2);
            contentElement.textContent = formatted;
            contentElement.style.whiteSpace = 'pre-wrap';
            contentElement.style.fontSize = '0.8rem';
            contentElement.style.lineHeight = '1.4';
            contentElement.style.backgroundColor = '#f8f9fa';
            contentElement.style.border = '1px solid #e9ecef';
            contentElement.style.borderRadius = '0.375rem';
            contentElement.style.padding = '1rem';
        } catch (e2) {
            // 如果仍然失败，显示错误信息
            const button = contentElement.previousElementSibling.querySelectorAll('button')[0];
            if (button) {
                button.innerHTML = '<i class="fa fa-exclamation-triangle"></i> 格式化失败';
                button.className = 'text-red-600 hover:text-red-800 text-xs';
                setTimeout(() => {
                    button.innerHTML = '<i class="fa fa-code"></i> 格式化JSON';
                    button.className = 'text-blue-600 hover:text-blue-800 text-xs';
                }, 3000);
            }

            // 显示详细错误信息
            console.error('JSON格式化失败:', e2);
            showNotification('无法格式化JSON数据，请检查数据格式是否正确', 'error');
        }
    }
}

// 复制消息内容
function copyMessageValue(messageIndex) {
    const contentElement = document.getElementById(`message-content-${messageIndex}`);
    const text = contentElement.textContent;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            showCopySuccess(messageIndex);
        }, () => {
            showNotification('复制失败，请手动复制', 'error');
        });
    } else {
        // 兼容旧浏览器
        const textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            showCopySuccess(messageIndex);
        } catch (err) {
            showNotification('复制失败，请手动复制', 'error');
        }
        document.body.removeChild(textarea);
    }
}

function showCopySuccess(messageIndex) {
    const btns = document.querySelectorAll(`#message-content-${messageIndex}`)[0]
        .previousElementSibling.querySelectorAll('button');
    const copyBtn = btns[1];
    if (copyBtn) {
        const oldHtml = copyBtn.innerHTML;
        copyBtn.innerHTML = '<i class="fa fa-check"></i> 已复制';
        copyBtn.className = 'text-green-600 hover:text-green-800 text-xs';
        setTimeout(() => {
            copyBtn.innerHTML = '<i class="fa fa-copy"></i> 复制';
            copyBtn.className = 'text-gray-600 hover:text-gray-800 text-xs';
        }, 2000);
    }
}

// 显示/隐藏加载状态
function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (show) {
        overlay.classList.add('active');
    } else {
        overlay.classList.remove('active');
    }
}

// 工具函数
function formatNumber(num) {
    if (num === null || num === undefined) return '0';
    return new Intl.NumberFormat('zh-CN').format(num);
}

function formatSize(bytes) {
    if (bytes === null || bytes === undefined || bytes === 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let size = bytes;
    let unitIndex = 0;
    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }
    return `${size.toFixed(2)} ${units[unitIndex]}`;
}

function formatDate(timestamp) {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatTimeForChart(timestamp) {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    const now = new Date();
    const diffHours = Math.abs(now - date) / (1000 * 60 * 60);

    // 根据时间差智能选择显示格式
    if (diffHours <= 1) {
        // 1小时内：显示分钟
        return date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    } else if (diffHours <= 24) {
        // 1天内：显示小时:分钟
        return date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    } else if (diffHours <= 72) {
        // 3天内：显示月-日 时:分
        return date.toLocaleString('zh-CN', {
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).replace(/(\d+)\/(\d+)\s+(\d+:\d+)/, '$1-$2 $3');
    } else {
        // 超过3天：显示月-日
        return date.toLocaleDateString('zh-CN', {
            month: '2-digit',
            day: '2-digit'
        }).replace(/(\d+)\/(\d+)/, '$1-$2');
    }
}

function formatTime(timestamp) {
    if (!timestamp) return '-';
    return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatSpeedMBps(bytesPerSec) {
    if (bytesPerSec === null || bytesPerSec === undefined || bytesPerSec === 0) return '0 B/s';

    const units = ['B/s', 'KB/s', 'MB/s', 'GB/s', 'TB/s'];
    let speed = bytesPerSec;
    let unitIndex = 0;

    while (speed >= 1024 && unitIndex < units.length - 1) {
        speed /= 1024;
        unitIndex++;
    }

    return `${speed.toFixed(2)} ${units[unitIndex]}`;
}

function showErrorMessage(message) {
    showNotification(message, 'error');
}

// 显示美化的通知提示
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    // 根据类型设置不同的样式和图标
    let icon, bgColor, textColor, borderColor;
    switch (type) {
        case 'error':
            icon = 'fa-exclamation-circle';
            bgColor = '#FEF2F2';
            textColor = '#DC2626';
            borderColor = '#FECACA';
            break;
        case 'success':
            icon = 'fa-check-circle';
            bgColor = '#F0FDF4';
            textColor = '#16A34A';
            borderColor = '#BBF7D0';
            break;
        case 'warning':
            icon = 'fa-exclamation-triangle';
            bgColor = '#FFFBEB';
            textColor = '#D97706';
            borderColor = '#FDE68A';
            break;
        default: // info
            icon = 'fa-info-circle';
            bgColor = '#EFF6FF';
            textColor = '#2563EB';
            borderColor = '#DBEAFE';
    }

    notification.innerHTML = `
        <div style="
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10001;
            max-width: 400px;
            background: ${bgColor};
            border: 1px solid ${borderColor};
            border-radius: 0.75rem;
            padding: 1rem 1.25rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            display: flex;
            align-items: center;
            gap: 0.75rem;
            animation: slideIn 0.3s ease-out;
        ">
            <i class="fa ${icon}" style="color: ${textColor}; font-size: 1.125rem;"></i>
            <span style="color: ${textColor}; font-weight: 500; flex: 1;">${message}</span>
            <button onclick="this.parentElement.parentElement.remove()"
                    style="
                        color: ${textColor};
                        background: none;
                        border: none;
                        font-size: 1rem;
                        cursor: pointer;
                        padding: 0;
                        opacity: 0.7;
                        transition: opacity 0.2s;
                    "
                    onmouseover="this.style.opacity='1'"
                    onmouseout="this.style.opacity='0.7'">
                <i class="fa fa-times"></i>
            </button>
        </div>
    `;

    // 添加CSS动画
    if (!document.getElementById('notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(notification);

    // 3秒后自动消失
    setTimeout(() => {
        const notificationEl = notification.querySelector('div');
        if (notificationEl) {
            notificationEl.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }
    }, 3000);
}