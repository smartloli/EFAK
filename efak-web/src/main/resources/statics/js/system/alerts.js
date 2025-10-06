// 创建提示元素
const tooltip = document.createElement('div');
tooltip.className = 'action-btn-tooltip';
document.body.appendChild(tooltip);

// 显示提示
function showTooltip(e, text) {
    tooltip.textContent = text;
    tooltip.style.opacity = '1';

    // 计算位置
    const rect = e.target.getBoundingClientRect();
    const tooltipRect = tooltip.getBoundingClientRect();

    // 设置位置在按钮上方
    tooltip.style.left = rect.left + (rect.width - tooltipRect.width) / 2 + 'px';
    tooltip.style.top = rect.top - tooltipRect.height - 8 + 'px';
}

// 隐藏提示
function hideTooltip() {
    tooltip.style.opacity = '0';
}

/**
 * 系统告警管理器
 */
class AlertManager {
    constructor() {
        this.alerts = [];
        this.filteredAlerts = [];
        this.currentPage = 1;
        this.pageSize = 5;
        this.filters = {
            search: '',
            status: '',
            channel: '',
            timeRange: '24h'
        };
        this.channelList = [
            {
                key: 'dingtalk', name: '钉钉', icon: '/images/im/dingtalk.webp',
                apiTemplate: 'https://oapi.dingtalk.com/robot/send?access_token=your_webhook_token'
            },
            {
                key: 'feishu', name: '飞书', icon: '/images/im/feishu.webp',
                apiTemplate: 'https://open.feishu.cn/open-apis/bot/v2/hook/your_webhook_token'
            },
            {
                key: 'wechat', name: '企业微信', icon: '/images/im/weixin.webp',
                apiTemplate: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=your_webhook_token'
            },
            {
                key: 'webhook', name: 'Webhook', icon: '/images/im/api.webp',
                apiTemplate: 'https://example.com/api/bot/hook/your_webhook_token'
            }
        ];
        this.typeList = [
            { key: 'broker-availability', name: 'Broker可用性告警' },
            { key: 'consumer-lag', name: '消费者组积压告警' },
            { key: 'topic-capacity', name: '主题容量告警' },
            { key: 'topic-throughput', name: '主题写入速度告警' }
        ];

        // 告警类型配置分页状态
        this.alertTypeConfigPage = 1;
        this.alertTypeConfigPageSize = 5;
        this.alertTypeConfigTotal = 0;

        this.alertTypeConfigs = this.loadAlertTypeConfigs();
        this.init();
    }

    /**
     * 初始化
     */
    init() {
        this.initSelect2();
        this.bindEvents();
        this.fetchAlerts();
    }

    /**
     * 初始化Select2组件
     */
    initSelect2() {
        // 初始化所有select2下拉框
        $('.select2-filter').select2({
            theme: 'default',
            width: '100%',
            minimumResultsForSearch: Infinity, // 禁用搜索框
            placeholder: '请选择...'
        });

        // 监听select2变化事件
        $('.select2-filter').on('select2:select', (e) => {
            const element = e.target;
            const filterType = element.id.replace('-filter', '');
            if (filterType === 'time') {
                this.filters.timeRange = element.value;
            } else {
                this.filters[filterType] = element.value;
            }
            this.applyFilters();
        });
    }

    /**
     * 从后端API获取告警数据
     */
    async fetchAlerts() {
        try {
            const request = {
                search: this.filters.search,
                status: this.filters.status,
                channel: this.filters.channel,
                timeRange: this.filters.timeRange,
                page: this.currentPage,
                pageSize: this.pageSize,
                sortField: 'createdAt',
                sortOrder: 'desc'
            };

            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/query?cid=${cid}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });

            if (response.ok) {
                const data = await response.json();
                this.alerts = data.alerts || [];
                this.total = data.total || 0;
                this.currentPage = data.page || 1;
                this.pageSize = data.pageSize || 10;
                this.stats = data.stats || {};
                this.updateStats();
                this.renderTable();
                this.renderPagination();
            } else {
                this.showToast('获取告警数据失败', 'error');
            }
        } catch (error) {
            this.showToast('获取告警数据异常', 'error');
        }
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 搜索输入框实时搜索
        const searchInput = document.getElementById('search-input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filters.search = e.target.value;
                this.debounce(() => this.applyFilters(), 300)();
            });
        }

        // 筛选下拉框变化 (非Select2的备用方案)
        ['status-filter', 'channel-filter', 'time-filter'].forEach(id => {
            const element = document.getElementById(id);
            if (element && !element.classList.contains('select2-filter')) {
                element.addEventListener('change', (e) => {
                    const filterType = id.replace('-filter', '');
                    if (filterType === 'time') {
                        this.filters.timeRange = e.target.value;
                    } else {
                        this.filters[filterType] = e.target.value;
                    }
                    this.applyFilters();
                });
            }
        });

        // 模态框背景点击关闭
        const modal = document.getElementById('alert-detail-modal');
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeAlertDetail();
                }
            });
        }

        // ESC键关闭模态框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAlertDetail();
            }
        });
    }

    /**
     * 防抖函数
     */
    debounce(func, delay) {
        let timeoutId;
        return function (...args) {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        };
    }

    /**
     * 应用筛选条件
     */
    applyFilters() {
        this.currentPage = 1;
        this.fetchAlerts();
    }

    /**
     * 重置筛选条件
     */
    resetFilters() {
        // 重置搜索框
        const searchInput = document.getElementById('search-input');
        if (searchInput) {
            searchInput.value = '';
        }

        // 重置select2下拉框
        $('#status-filter').val('').trigger('change');
        $('#channel-filter').val('').trigger('change');
        $('#time-filter').val('24h').trigger('change');

        // 重置filters对象
        this.filters = {
            search: '',
            status: '',
            channel: '',
            timeRange: '24h'
        };

        // 重置页码并重新获取数据
        this.currentPage = 1;
        this.fetchAlerts();
    }

    /**
     * 更新统计信息
     */
    updateStats() {
        // 从后端响应中获取统计信息
        if (this.stats) {
            // critical-count 显示未处理告警数量
            this.animateNumber('critical-count', this.stats.unprocessed || 0);
            // warning-count 显示已处理告警数量
            this.animateNumber('warning-count', this.stats.processed || 0);
            // info-count 显示已忽略告警数量
            this.animateNumber('info-count', this.stats.ignored || 0);
            // resolved-count 显示已解决告警数量
            this.animateNumber('resolved-count', this.stats.resolved || 0);
        }
    }

    /**
     * 数字动画效果
     */
    animateNumber(elementId, targetNumber) {
        const element = document.getElementById(elementId);
        if (!element) return;

        let currentNumber = 0;
        const increment = Math.ceil(targetNumber / 20);
        const timer = setInterval(() => {
            currentNumber += increment;
            if (currentNumber >= targetNumber) {
                currentNumber = targetNumber;
                clearInterval(timer);
            }
            element.textContent = currentNumber;
        }, 50);
    }

    /**
     * 渲染表格
     */
    renderTable() {
        const tbody = document.getElementById('alerts-table-body');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (this.alerts.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; padding: 40px; color: #6b7280; white-space: normal;">
                        <i class="fa fa-search" style="font-size: 48px; margin-bottom: 16px; display: block; margin-left: auto; margin-right: auto;"></i>
                        暂无符合条件的告警数据
                    </td>
                </tr>
            `;
            return;
        }

        this.alerts.forEach(alert => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${this.formatDate(new Date(alert.createdAt))}</td>
                <td>
                    <span class="alert-type">${this.getTypeText(alert.alertType || alert.type)}</span>
                </td>
                <td>
                    <div style="font-weight: 600; margin-bottom: 4px;" title="${alert.title}">${alert.title}</div>
                    <div style="font-size: 0.75rem; color: #6b7280;" title="${alert.description}">${alert.description}</div>
                </td>
                <td>${alert.object}</td>
                <td>
                    <span class="alert-status ${alert.status}">${this.getStatusText(alert.status)}</span>
                </td>
                <td>
                    ${alert.channelName || '未配置'}
                </td>
                <td>${this.calculateDuration(alert.createdAt, alert.updatedAt)}</td>
                <td>
                    <button class="action-btn view" onclick="alertManager.viewAlert(${alert.id})" onmouseenter="showTooltip(event, '查看详情')" onmouseleave="hideTooltip()">
                        <i class="fa fa-eye"></i>
                    </button>
                    ${alert.status === 0 ? `<button class="action-btn acknowledge" onclick="alertManager.acknowledgeAlert(${alert.id})" onmouseenter="showTooltip(event, '确认告警')" onmouseleave="hideTooltip()">
                        <i class="fa fa-check"></i>
                    </button>` : ''}
                    ${alert.status === 0 ? `<button class="action-btn resolve" onclick="alertManager.resolveAlert(${alert.id})" onmouseenter="showTooltip(event, '解决告警')" onmouseleave="hideTooltip()">
                        <i class="fa fa-check-circle"></i>
                    </button>` : ''}
                    ${alert.status === 0 ? `<button class="action-btn ignore" onclick="alertManager.ignoreAlert(${alert.id})" onmouseenter="showTooltip(event, '忽略告警')" onmouseleave="hideTooltip()">
                        <i class="fa fa-times"></i>
                    </button>` : ''}
                    ${alert.alertType === 'topic-skew' && alert.status === 0 ? `<button class="action-btn rebalance" onclick="alertManager.performRebalance(${alert.id})" onmouseenter="showTooltip(event, '数据均衡')" onmouseleave="hideTooltip()">
                        <i class="fa fa-balance-scale"></i>
                    </button>` : ''}
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    /**
     * 渲染分页
     */
    renderPagination() {
        // 从后端响应中获取分页信息
        const totalPages = Math.ceil(this.total / this.pageSize);
        const startIndex = (this.currentPage - 1) * this.pageSize + 1;
        const endIndex = Math.min(this.currentPage * this.pageSize, this.total);

        // 更新分页信息
        document.getElementById('page-start').textContent = startIndex;
        document.getElementById('page-end').textContent = endIndex;
        document.getElementById('total-alerts').textContent = this.total;

        // 更新分页按钮状态
        const prevBtn = document.getElementById('prev-btn');
        const nextBtn = document.getElementById('next-btn');

        if (prevBtn) prevBtn.disabled = this.currentPage <= 1;
        if (nextBtn) nextBtn.disabled = this.currentPage >= totalPages;

        // 渲染页码
        const pageNumbers = document.getElementById('page-numbers');
        if (pageNumbers) {
            pageNumbers.innerHTML = '';

            const maxVisiblePages = 5;
            let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
            let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

            if (endPage - startPage < maxVisiblePages - 1) {
                startPage = Math.max(1, endPage - maxVisiblePages + 1);
            }

            for (let i = startPage; i <= endPage; i++) {
                const pageBtn = document.createElement('button');
                pageBtn.className = `pagination-btn ${i === this.currentPage ? 'active' : ''}`;
                pageBtn.textContent = i;
                pageBtn.onclick = () => this.goToPage(i);
                pageNumbers.appendChild(pageBtn);
            }
        }
    }

    /**
     * 跳转到指定页面
     */
    goToPage(page) {
        this.currentPage = page;
        this.fetchAlerts();
    }

    /**
     * 上一页
     */
    prevPage() {
        this.goToPage(this.currentPage - 1);
    }

    /**
     * 下一页
     */
    nextPage() {
        this.goToPage(this.currentPage + 1);
    }

    /**
 * 查看告警详情
 */
    async viewAlert(alertId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/${alertId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取告警详情失败', 'error');
                return;
            }

            const alert = await response.json();
            if (!alert) return;

            const modal = document.getElementById('alert-detail-modal');
            const content = document.getElementById('alert-detail-content');

            if (!modal || !content) return;

            content.innerHTML = `
                <!-- 告警基本信息 -->
                <div class="alert-detail-meta">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-tag"></i>
                            告警类型
                        </div>
                        <div class="alert-meta-value">
                            <span class="alert-type">${this.getTypeText(alert.type)}</span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-info-circle"></i>
                            当前状态
                        </div>
                        <div class="alert-meta-value">
                            <span class="alert-status ${alert.status}">${this.getStatusText(alert.status)}</span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-clock"></i>
                            持续时间
                        </div>
                        <div class="alert-meta-value">${this.calculateDuration(alert.createdAt, alert.updatedAt)}</div>
                    </div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa fa-file-text"></i>
                        告警标题
                    </div>
                    <div class="alert-detail-value">${alert.title}</div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa fa-align-left"></i>
                        详细描述
                    </div>
                    <div class="alert-detail-value">${alert.description}</div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa fa-server"></i>
                        告警来源
                    </div>
                    <div class="alert-detail-value">${alert.object}</div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa-solid fa-paper-plane"></i>
                        告警渠道
                    </div>
                    <div class="alert-channels-detail">
                        ${(alert.channels || []).map(channel => {
                // 优先显示渠道名称，如果没有则显示渠道类型对应的名称
                let channelDisplayName;
                if (channel.channelName) {
                    channelDisplayName = channel.channelName;
                } else {
                    const channelInfo = this.channelList.find(c => c.key === channel.channelType);
                    channelDisplayName = channelInfo?.name || channel.channelType;
                }
                return `
                                <div class="channel-detail-item">
                                    <div class="channel-detail-status ${channel.status}"></div>
                                    <span>${channelDisplayName}</span>
                                    <span style="margin-left: auto; font-size: 0.75rem; color: #6b7280;">
                                        ${channel.sendTime ? this.formatDate(new Date(channel.sendTime)) : '未发送'}
                                    </span>
                                </div>
                            `;
            }).join('')}
                    </div>
                </div>

                <!-- 时间信息 -->
                <div class="alert-detail-meta">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-calendar"></i>
                            告警时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(alert.createdAt))}</div>
                    </div>

                    ${alert.updatedAt ? `
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-edit"></i>
                            最后更新
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(alert.updatedAt))}</div>
                    </div>
                    ` : ''}

                    ${alert.resolvedAt ? `
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-check-circle"></i>
                            解决时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(alert.resolvedAt))}</div>
                    </div>
                    ` : ''}
                </div>

                ${alert.type === 'topic-skew' && alert.status === 0 ? `
                <div class="rebalance-section">
                    <div class="rebalance-header">
                        <i class="rebalance-icon fa fa-balance-scale"></i>
                        <div class="rebalance-title">数据均衡操作</div>
                    </div>
                    <div class="rebalance-description">
                        检测到 Topic "${alert.object}" 存在数据倾斜问题，建议执行数据均衡操作来解决。
                    </div>
                    <div class="rebalance-actions">
                        <button class="action-btn rebalance" onclick="alertManager.performRebalance()">
                            <i class="fa fa-balance-scale"></i>
                            执行数据均衡
                        </button>
                    </div>
                </div>
                ` : ''}
            `;

            // 设置模态框数据属性
            modal.dataset.alertId = alertId;

            // 更新按钮状态
            const acknowledgeBtn = document.getElementById('acknowledge-btn');
            const resolveBtn = document.getElementById('resolve-btn');
            const rebalanceBtn = document.getElementById('rebalance-btn');

            if (acknowledgeBtn) {
                acknowledgeBtn.style.display = alert.status === 0 ? 'inline-block' : 'none';
                acknowledgeBtn.onclick = () => this.acknowledgeAlert(alert.id);
            }

            if (resolveBtn) {
                resolveBtn.style.display = alert.status === 0 ? 'inline-block' : 'none';
                resolveBtn.onclick = () => this.resolveAlert(alert.id);
            }

            if (rebalanceBtn) {
                rebalanceBtn.style.display = alert.type === 'topic-skew' && alert.status === 0 ? 'inline-block' : 'none';
            }

            modal.classList.add('show');
        } catch (error) {
            this.showToast('获取告警详情异常', 'error');
        }
    }

    /**
     * 关闭告警详情
     */
    closeAlertDetail() {
        const modal = document.getElementById('alert-detail-modal');
        if (modal) {
            modal.classList.remove('show');
        }
    }

    /**
     * 确认告警
     */
    async acknowledgeAlert(alertId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/${alertId}/status?status=1&cid=${cid}`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showToast('告警已确认', 'success');
                this.closeAlertDetail();
                this.fetchAlerts();
            } else {
                this.showToast('确认告警失败', 'error');
            }
        } catch (error) {
            this.showToast('确认告警异常', 'error');
        }
    }

    /**
     * 解决告警
     */
    async resolveAlert(alertId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/${alertId}/status?status=3&cid=${cid}`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showToast('告警已解决', 'success');
                this.closeAlertDetail();
                this.fetchAlerts();
            } else {
                this.showToast('解决告警失败', 'error');
            }
        } catch (error) {
            this.showToast('解决告警异常', 'error');
        }
    }

    /**
     * 忽略告警
     */
    async ignoreAlert(alertId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/${alertId}/status?status=2&cid=${cid}`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showToast('告警已忽略', 'success');
                this.fetchAlerts();
            } else {
                this.showToast('忽略告警失败', 'error');
            }
        } catch (error) {
            this.showToast('忽略告警异常', 'error');
        }
    }

    /**
     * 刷新告警数据
     */
    refreshAlerts() {
        this.showToast('正在刷新告警数据...', 'info');
        this.fetchAlerts();
    }

    /**
     * 配置告警渠道
     */
    async configChannels() {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取渠道列表失败', 'error');
                return;
            }

            const channels = await response.json();
            const modal = document.getElementById('channel-config-modal');
            const content = document.getElementById('channel-config-content');

            content.innerHTML = `
                <div style="margin-bottom: 24px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                        <h3 style="font-size: 1.125rem; font-weight: 600; color: #1f2937;">告警渠道列表</h3>
                        <button class="filter-btn primary" onclick="createChannel()" style="display: flex; align-items: center; gap: 8px;">
                            <i class="fa fa-plus"></i>
                            创建渠道
                        </button>
                    </div>
                    <p class="text-gray-600 text-sm">管理不同的告警渠道，配置后可在告警时及时通知相关人员。</p>
                </div>
                
                <div style="background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden;">
                    <table style="width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0;">
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">渠道名称</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">渠道类型</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">创建时间</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">更新时间</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">状态</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${channels.length === 0 ? `
                            <tr>
                                <td colspan="6" style="text-align: center; padding: 40px; color: #6b7280;">
                                    <i class="fa fa-bell" style="font-size: 48px; margin-bottom: 16px; display: block; margin-left: auto; margin-right: auto;"></i>
                                    暂无告警渠道，请创建第一个渠道
                                </td>
                            </tr>
                        ` : channels.map(channel => {
                const channelInfo = this.channelList.find(c => c.key === channel.type);
                return `
                                <tr style="border-bottom: 1px solid #f1f5f9;">
                                    <td style="padding: 12px 16px; color: #1f2937; font-weight: 500;">${channel.name}</td>
                                    <td style="padding: 12px 16px;">
                                        <div style="display: flex; align-items: center; gap: 8px;">
                                            <img src="${channelInfo?.icon}" alt="${channelInfo?.name}" style="width: 20px; height: 20px; border-radius: 4px;">
                                            <span style="color: #374151;">${channelInfo?.name}</span>
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">${this.formatDate(new Date(channel.createdAt))}</td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">${this.formatDate(new Date(channel.updatedAt))}</td>
                                    <td style="padding: 12px 16px;">
                                        <span class="alert-status ${channel.enabled ? 'resolved' : 'active'}">
                                            ${channel.enabled ? '启用' : '禁用'}
                                        </span>
                                    </td>
                                    <td style="padding: 12px 16px;">
                                        <div style="display: flex; gap: 8px;">
                                            <button class="action-btn view" onclick="viewChannel(${channel.id})" title="查看">
                                                <i class="fa fa-eye"></i>
                                            </button>
                                            <button class="action-btn acknowledge" onclick="editChannel(${channel.id})" title="编辑">
                                                <i class="fa fa-edit"></i>
                                            </button>
                                            <button class="action-btn resolve" onclick="deleteChannel(${channel.id})" title="删除">
                                                <i class="fa fa-trash"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            `;
            }).join('')}
                    </tbody>
                </table>
            </div>
            
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px; background: #f8fafc; border-radius: 8px;">
                <div style="color: #6b7280; font-size: 0.875rem;">
                    共 ${channels.length} 条渠道
                </div>
            </div>
        `;

            modal.classList.add('show');
        } catch (error) {
            ('获取渠道列表异常:', error);
            this.showToast('获取渠道列表异常', 'error');
        }
    }

    /**
     * 关闭渠道配置
     */
    closeChannelConfig() {
        const modal = document.getElementById('channel-config-modal');
        modal.classList.remove('show');
    }

    /**
     * 创建渠道
     */
    createChannel() {
        const modal = document.getElementById('create-channel-modal');
        const content = document.getElementById('create-channel-content');

        content.innerHTML = `
            <div style="margin-bottom: 20px;">
                <p class="text-gray-600 text-sm">创建新的告警渠道，配置后可在告警时及时通知相关人员。</p>
            </div>
            
            <div style="display: grid; gap: 20px;">
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        渠道名称 <span style="color: #ef4444;">*</span>
                    </label>
                    <input type="text" id="channel-name" 
                           placeholder="请输入渠道名称，如：生产环境钉钉群"
                           style="width: 100%; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem;">
                </div>
                
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        渠道类型 <span style="color: #ef4444;">*</span>
                    </label>
                    <select id="channel-type" class="filter-input select2-filter" style="width: 100%;">
                        <option value="">请选择渠道类型</option>
                        ${this.channelList.map(channel => `
                            <option value="${channel.key}" data-api="${channel.apiTemplate}">
                                ${channel.name}
                            </option>
                        `).join('')}
                    </select>
                </div>
                
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        API地址 <span style="color: #ef4444;">*</span>
                    </label>
                    <input type="text" id="channel-api" 
                           placeholder="请先选择渠道类型"
                           style="width: 100%; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem;">
                </div>
                
                <div>
                    <label style="display: flex; align-items: center; gap: 8px; font-size: 0.875rem; color: #374151;">
                        <input type="checkbox" id="channel-enabled" checked style="width: 16px; height: 16px;">
                        立即启用此渠道
                    </label>
                </div>
            </div>
        `;

        // 初始化Select2
        const self = this;
        $('#channel-type').select2({
            theme: 'default',
            width: '100%',
            minimumResultsForSearch: Infinity,
            placeholder: '请选择渠道类型',
            templateResult: function (option) {
                return self.formatChannelOption(option);
            },
            templateSelection: function (option) {
                return self.formatChannelOption(option);
            }
        });

        // 监听渠道类型变化
        $('#channel-type').on('select2:select', (e) => {
            const selectedOption = e.params.data;
            const apiTemplate = selectedOption.element.dataset.api;
            document.getElementById('channel-api').value = apiTemplate;
        });

        modal.classList.add('show');
    }

    /**
 * 格式化告警类型选项
 */
    formatAlertTypeOption(option) {
        if (!option.id) return option.text;

        const typeInfo = this.typeList.find(t => t.key === option.id);
        if (typeInfo) {
            return $(`
                <div style="display: flex; align-items: center; gap: 12px; padding: 8px 0;">
                    <div style="width: 24px; height: 24px; border-radius: 4px; background: #f3f4f6; display: flex; align-items: center; justify-content: center;">
                        <i class="fa fa-bell" style="color: #6b7280; font-size: 12px;"></i>
                    </div>
                    <span>${typeInfo.name}</span>
                </div>
            `);
        }

        return option.text;
    }

    /**
     * 格式化渠道选项
     */
    formatChannelOption(option) {
        if (!option.id) return option.text;

        // 检查是否是告警渠道选项（有data-type属性）
        if (option.element && option.element.dataset.type) {
            const channelType = option.element.dataset.type;
            const channelInfo = this.channelList.find(c => c.key === channelType);
            if (channelInfo) {
                return $(`
                    <div style="display: flex; align-items: center; gap: 12px; padding: 8px 0;">
                        <img src="${channelInfo.icon}" alt="${channelInfo.name}" style="width: 24px; height: 24px; border-radius: 4px;">
                        <span>${option.text}</span>
                    </div>
                `);
            }
        }

        // 检查是否是告警类型选项（通过option.id匹配）
        const channelInfo = this.channelList.find(c => c.key === option.id);
        if (channelInfo) {
            return $(`
                <div style="display: flex; align-items: center; gap: 12px; padding: 8px 0;">
                    <img src="${channelInfo.icon}" alt="${channelInfo.name}" style="width: 24px; height: 24px; border-radius: 4px;">
                    <span>${channelInfo.name}</span>
                </div>
            `);
        }

        return option.text;
    }

    /**
     * 关闭创建渠道
     */
    closeCreateChannel() {
        const modal = document.getElementById('create-channel-modal');
        modal.classList.remove('show');
    }

    /**
     * 保存创建渠道
     */
    async saveCreateChannel() {
        const name = document.getElementById('channel-name').value.trim();
        const type = document.getElementById('channel-type').value;
        const api = document.getElementById('channel-api').value.trim();
        const enabled = document.getElementById('channel-enabled').checked;

        if (!name || !type || !api) {
            this.showToast('请填写完整的渠道信息', 'error');
            return;
        }

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels?cid=${cid}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: name,
                    type: type,
                    apiUrl: api,
                    enabled: enabled,
                    createdBy: 'admin' // 这里可以从用户会话中获取
                })
            });

            if (response.ok) {
                this.showToast('渠道创建成功', 'success');
                this.closeCreateChannel();
                this.configChannels(); // 刷新渠道列表
            } else {
                const errorText = await response.text();
                this.showToast(`渠道创建失败: ${errorText}`, 'error');
            }
        } catch (error) {
            ('创建渠道异常:', error);
            this.showToast('创建渠道异常', 'error');
        }
    }

    /**
     * 查看渠道详情
     */
    async viewChannel(channelId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels/${channelId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取渠道详情失败', 'error');
                return;
            }

            const channel = await response.json();
            const channelInfo = this.channelList.find(c => c.key === channel.type);

            const modal = document.getElementById('view-channel-modal');
            const content = document.getElementById('view-channel-content');

            content.innerHTML = `
                <div class="alert-detail-meta" style="grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 24px;">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-tag"></i>
                            渠道名称
                        </div>
                        <div class="alert-meta-value">${channel.name}</div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-bell"></i>
                            渠道类型
                        </div>
                        <div class="alert-meta-value">
                            <div style="display: flex; align-items: center; gap: 8px;">
                                <img src="${channelInfo?.icon}" alt="${channelInfo?.name}" style="width: 24px; height: 24px; border-radius: 4px;">
                                <span>${channelInfo?.name}</span>
                            </div>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-toggle-on"></i>
                            当前状态
                        </div>
                        <div class="alert-meta-value">
                            <span class="alert-status ${channel.enabled ? 'resolved' : 'active'}">
                                ${channel.enabled ? '启用' : '禁用'}
                            </span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-user"></i>
                            创建人
                        </div>
                        <div class="alert-meta-value">${channel.createdBy || 'admin'}</div>
                    </div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa fa-link"></i>
                        API地址
                    </div>
                    <div class="alert-detail-value">${channel.apiUrl}</div>
                </div>

                <div class="alert-detail-meta">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-calendar"></i>
                            创建时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(channel.createdAt))}</div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-edit"></i>
                            更新时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(channel.updatedAt))}</div>
                    </div>
                </div>
            `;

            modal.classList.add('show');
        } catch (error) {
            ('获取渠道详情异常:', error);
            this.showToast('获取渠道详情异常', 'error');
        }
    }

    /**
     * 编辑渠道
     */
    async editChannel(channelId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels/${channelId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取渠道信息失败', 'error');
                return;
            }

            const channel = await response.json();
            const modal = document.getElementById('edit-channel-modal');
            const content = document.getElementById('edit-channel-content');

            content.innerHTML = `
                <div style="margin-bottom: 20px;">
                    <p class="text-gray-600 text-sm">编辑告警渠道配置，只能修改API地址和启用状态。</p>
                </div>
                
                <div style="display: grid; gap: 20px;">
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            渠道名称
                        </label>
                        <input type="text" value="${channel.name}" disabled
                               style="width: 100%; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem; background: #f3f4f6; color: #6b7280;">
                        <small style="color: #6b7280; font-size: 0.75rem;">渠道名称不可修改</small>
                    </div>
                    
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            渠道类型
                        </label>
                        <div style="display: flex; align-items: center; gap: 8px; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; background: #f3f4f6;">
                            <img src="${this.channelList.find(c => c.key === channel.type)?.icon}" alt="${this.channelList.find(c => c.key === channel.type)?.name}" style="width: 20px; height: 20px; border-radius: 4px;">
                            <span style="color: #6b7280;">${this.channelList.find(c => c.key === channel.type)?.name}</span>
                        </div>
                        <small style="color: #6b7280; font-size: 0.75rem;">渠道类型不可修改</small>
                    </div>
                    
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            API地址 <span style="color: #ef4444;">*</span>
                        </label>
                        <input type="text" id="edit-channel-api" 
                               value="${channel.apiUrl}"
                               placeholder="请输入API地址"
                               style="width: 100%; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem;">
                    </div>
                    
                    <div>
                        <label style="display: flex; align-items: center; gap: 8px; font-size: 0.875rem; color: #374151;">
                            <input type="checkbox" id="edit-channel-enabled" ${channel.enabled ? 'checked' : ''} style="width: 16px; height: 16px;">
                            启用此渠道
                        </label>
                    </div>
                </div>
            `;

            // 设置模态框数据属性
            modal.dataset.channelId = channelId;

            modal.classList.add('show');
        } catch (error) {
            ('获取渠道信息异常:', error);
            this.showToast('获取渠道信息异常', 'error');
        }
    }

    /**
     * 删除渠道
     */
    async deleteChannel(channelId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels/${channelId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取渠道信息失败', 'error');
                return;
            }

            const channel = await response.json();
            const modal = document.getElementById('delete-confirm-modal');
            const content = document.getElementById('delete-confirm-content');

            content.innerHTML = `
                <div style="text-align: center; padding: 20px 0;">
                    <div style="width: 64px; height: 64px; border-radius: 50%; background: #fef2f2; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
                        <i class="fa fa-exclamation-triangle" style="color: #ef4444; font-size: 24px;"></i>
                    </div>
                    <h3 style="font-size: 1.125rem; font-weight: 600; color: #1f2937; margin-bottom: 8px;">确认删除渠道</h3>
                    <p style="color: #6b7280; font-size: 0.875rem; margin-bottom: 16px;">
                        您即将删除渠道 <strong style="color: #1f2937;">"${channel.name}"</strong>
                    </p>
                    <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 16px;">
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                            <img src="${this.channelList.find(c => c.key === channel.type)?.icon}" alt="${this.channelList.find(c => c.key === channel.type)?.name}" style="width: 20px; height: 20px; border-radius: 4px;">
                            <span style="font-weight: 500; color: #374151;">${this.channelList.find(c => c.key === channel.type)?.name}</span>
                        </div>
                        <div style="font-size: 0.875rem; color: #6b7280; text-align: left;">
                            API地址: ${channel.apiUrl}
                        </div>
                    </div>
                    <p style="color: #ef4444; font-size: 0.875rem; font-weight: 500;">
                        ⚠️ 删除后无法恢复，请谨慎操作
                    </p>
                </div>
            `;

            // 设置模态框数据属性
            modal.dataset.channelId = channelId;
            modal.dataset.channelName = channel.name;

            modal.classList.add('show');
        } catch (error) {
            ('获取渠道信息异常:', error);
            this.showToast('获取渠道信息异常', 'error');
        }
    }

    /**
     * 关闭查看渠道详情
     */
    closeViewChannel() {
        const modal = document.getElementById('view-channel-modal');
        modal.classList.remove('show');
    }

    /**
     * 关闭编辑渠道
     */
    closeEditChannel() {
        const modal = document.getElementById('edit-channel-modal');
        modal.classList.remove('show');
    }

    /**
     * 关闭删除确认
     */
    closeDeleteConfirm() {
        const modal = document.getElementById('delete-confirm-modal');
        modal.classList.remove('show');
    }

    /**
     * 保存编辑渠道
     */
    async saveEditChannel() {
        const modal = document.getElementById('edit-channel-modal');
        const channelId = modal.dataset.channelId;
        const apiUrl = document.getElementById('edit-channel-api').value.trim();
        const enabled = document.getElementById('edit-channel-enabled').checked;

        if (!apiUrl) {
            this.showToast('请填写API地址', 'error');
            return;
        }

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels/${channelId}?cid=${cid}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    apiUrl: apiUrl,
                    enabled: enabled
                })
            });

            if (response.ok) {
                this.showToast('渠道更新成功', 'success');
                this.closeEditChannel();
                this.configChannels(); // 刷新渠道列表
            } else {
                const errorText = await response.text();
                this.showToast(`渠道更新失败: ${errorText}`, 'error');
            }
        } catch (error) {
            ('更新渠道异常:', error);
            this.showToast('更新渠道异常', 'error');
        }
    }

    /**
     * 确认删除渠道
     */
    async confirmDeleteChannel() {
        const modal = document.getElementById('delete-confirm-modal');
        const channelId = modal.dataset.channelId;
        const channelName = modal.dataset.channelName;

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels/${channelId}?cid=${cid}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showToast(`渠道"${channelName}"删除成功`, 'success');
                this.closeDeleteConfirm();
                this.configChannels(); // 刷新渠道列表
            } else {
                const errorText = await response.text();
                this.showToast(`渠道删除失败: ${errorText}`, 'error');
            }
        } catch (error) {
            ('删除渠道异常:', error);
            this.showToast('删除渠道异常', 'error');
        }
    }

    /**
     * 执行数据均衡
     */
    async performRebalance(alertId) {
        if (!alertId) {
            this.showToast('无效的告警ID', 'error');
            return;
        }

        try {
            this.showToast('正在执行数据均衡操作，请稍候...', 'info');

            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/${alertId}/rebalance?cid=${cid}`, {
                method: 'POST'
            });

            if (response.ok) {
                this.closeAlertDetail();
                this.fetchAlerts();
                this.showToast('数据均衡操作完成，告警已自动解决', 'success');
            } else {
                this.showToast('数据均衡操作失败', 'error');
            }
        } catch (error) {
            this.showToast('数据均衡操作异常', 'error');
        }
    }

    /**
     * 加载告警类型配置
     */
    loadAlertTypeConfigs() {
        const defaultConfigs = {
            'topic-capacity': {
                enabled: true,
                threshold: 500,
                unit: 'MB',
                description: '监控主题存储容量，当超过阈值时触发告警'
            },
            'consumer-lag': {
                enabled: true,
                threshold: 1000,
                unit: '条',
                description: '监控消费者组消息积压，当积压数量超过阈值时触发告警'
            },
            'broker-availability': {
                enabled: true,
                threshold: 95,
                unit: '%',
                description: '监控Broker节点可用性，当可用性低于阈值时触发告警'
            }
        };

        const saved = localStorage.getItem('alertTypeConfigs');
        return saved ? { ...defaultConfigs, ...JSON.parse(saved) } : defaultConfigs;
    }

    /**
     * 配置告警类型
     */
    async configAlertTypes(page = 1) {
        try {
            this.alertTypeConfigPage = page;
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/page?cid=${cid}&page=${page}&pageSize=${this.alertTypeConfigPageSize}`);
            if (!response.ok) {
                this.showToast('获取告警类型配置失败', 'error');
                return;
            }

            const data = await response.json();
            const configs = data.alerts || [];
            this.alertTypeConfigTotal = data.total || 0;

            const modal = document.getElementById('alert-type-config-modal');
            const content = document.getElementById('alert-type-config-content');

            content.innerHTML = `
                <div style="margin-bottom: 24px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                        <h3 style="font-size: 1.125rem; font-weight: 600; color: #1f2937;">告警类型配置</h3>
                        <button class="filter-btn primary" onclick="createAlertType()" style="display: flex; align-items: center; gap: 8px;">
                            <i class="fa fa-plus"></i>
                            创建告警类型
                        </button>
                    </div>
                    <p class="text-gray-600 text-sm">配置不同类型的告警监控规则和阈值，当监控指标超过设定阈值时将触发告警。</p>
                </div>
                
                <div style="background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden;">
                    <table style="width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0;">
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">告警类型</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">告警渠道</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">告警阈值</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">创建时间</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">修改时间</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">管理员</th>
                                <th style="padding: 12px 16px; text-align: left; font-weight: 600; color: #374151; font-size: 0.875rem;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${configs.length === 0 ? `
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 40px; color: #6b7280;">
                                    <i class="fa fa-bell" style="font-size: 48px; margin-bottom: 16px; display: block; margin-left: auto; margin-right: auto;"></i>
                                    暂无告警类型配置，请创建第一个配置
                                </td>
                            </tr>
                        ` : configs.map(config => {
                const typeInfo = this.typeList.find(t => t.key === config.type);
                return `
                                <tr style="border-bottom: 1px solid #f1f5f9;">
                                    <td style="padding: 12px 16px;">
                                        <div style="display: flex; align-items: center; gap: 8px;">
                                            <span style="font-weight: 500; color: #1f2937;">${typeInfo?.name || config.type}</span>
                                        </div>
                                    </td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">
                                        ${config.channels && config.channels.length > 0 ?
                        config.channels.map(channel => {
                            // 优先显示渠道名称，如果没有则显示渠道类型对应的名称
                            if (channel.channelName) {
                                return channel.channelName;
                            } else {
                                const channelInfo = this.channelList.find(c => c.key === channel.channelType);
                                return channelInfo?.name || channel.channelType;
                            }
                        }).join(', ')
                        : '未配置'}
                                    </td>
                                    <td style="padding: 12px 16px;">
                                        <span style="font-weight: 500; color: #1f2937;">${config.threshold} ${config.unit}</span>
                                    </td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">${this.formatDate(new Date(config.createdAt))}</td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">${this.formatDate(new Date(config.updatedAt))}</td>
                                    <td style="padding: 12px 16px; color: #6b7280; font-size: 0.875rem;">${config.createdBy || 'admin'}</td>
                                    <td style="padding: 12px 16px;">
                                        <div style="display: flex; gap: 8px;">
                                            <button class="action-btn view" onclick="viewAlertType(${config.id})" title="查看">
                                                <i class="fa fa-eye"></i>
                                            </button>
                                            <button class="action-btn acknowledge" onclick="editAlertType(${config.id})" title="编辑">
                                                <i class="fa fa-edit"></i>
                                            </button>
                                            <button class="action-btn resolve" onclick="deleteAlertType(${config.id})" title="删除">
                                                <i class="fa fa-trash"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            `;
            }).join('')}
                    </tbody>
                </table>
            </div>
            
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px; background: #f8fafc; border-radius: 8px;">
                <div style="color: #6b7280; font-size: 0.875rem;">
                    共 ${this.alertTypeConfigTotal} 条配置
                </div>
                <div style="display: flex; gap: 8px;">
                    ${this.renderAlertTypeConfigPagination()}
                </div>
            </div>
        `;

            modal.classList.add('show');
        } catch (error) {
            ('获取告警类型配置异常:', error);
            this.showToast('获取告警类型配置异常', 'error');
        }
    }

    /**
     * 渲染告警类型配置分页
     */
    renderAlertTypeConfigPagination() {
        const totalPages = Math.ceil(this.alertTypeConfigTotal / this.alertTypeConfigPageSize);
        const currentPage = this.alertTypeConfigPage;

        let html = '';

        // 上一页按钮
        html += `<button class="pagination-btn" ${currentPage <= 1 ? 'disabled' : ''} onclick="alertManager.prevAlertTypeConfigPage()">
            <i class="fa fa-chevron-left"></i>
        </button>`;

        // 页码按钮
        const maxVisiblePages = 5;
        let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `<button class="pagination-btn ${i === currentPage ? 'active' : ''}" onclick="alertManager.goToAlertTypeConfigPage(${i})">${i}</button>`;
        }

        // 下一页按钮
        html += `<button class="pagination-btn" ${currentPage >= totalPages ? 'disabled' : ''} onclick="alertManager.nextAlertTypeConfigPage()">
            <i class="fa fa-chevron-right"></i>
        </button>`;

        return html;
    }

    /**
     * 跳转到告警类型配置指定页面
     */
    goToAlertTypeConfigPage(page) {
        this.configAlertTypes(page);
    }

    /**
     * 告警类型配置上一页
     */
    prevAlertTypeConfigPage() {
        if (this.alertTypeConfigPage > 1) {
            this.configAlertTypes(this.alertTypeConfigPage - 1);
        }
    }

    /**
     * 告警类型配置下一页
     */
    nextAlertTypeConfigPage() {
        const totalPages = Math.ceil(this.alertTypeConfigTotal / this.alertTypeConfigPageSize);
        if (this.alertTypeConfigPage < totalPages) {
            this.configAlertTypes(this.alertTypeConfigPage + 1);
        }
    }

    /**
     * 关闭告警类型配置
     */
    closeAlertTypeConfig() {
        const modal = document.getElementById('alert-type-config-modal');
        modal.classList.remove('show');
    }

    /**
     * 创建告警类型
     */
    createAlertType() {
        const modal = document.getElementById('create-alert-type-modal');
        const content = document.getElementById('create-alert-type-content');

        content.innerHTML = `
            <div style="margin-bottom: 20px;">
                <p class="text-gray-600 text-sm">创建新的告警类型配置，设置监控规则和阈值。</p>
            </div>
            
            <div style="display: grid; gap: 20px;">
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        告警类型 <span style="color: #ef4444;">*</span>
                    </label>
                    <select id="alert-type-select" class="filter-input select2-filter" style="width: 100%;">
                        <option value="">请选择告警类型</option>
                        <option value="broker-availability">Broker可用性</option>
                        <option value="consumer-lag">消费者组积压</option>
                        <option value="topic-capacity">主题容量</option>
                        <option value="topic-throughput">主题写入速度</option>
                    </select>
                </div>
                
                <div id="target-config" style="display: none;">
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        监控目标 <span style="color: #ef4444;">*</span>
                    </label>
                    <div id="target-select-container">
                        <!-- 动态生成目标选择器 -->
                    </div>
                </div>
                
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        告警阈值 <span style="color: #ef4444;">*</span>
                    </label>
                    <div style="display: flex; gap: 8px; align-items: center;">
                        <input type="number" id="alert-threshold" 
                               placeholder="请输入阈值"
                               style="flex: 1; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem;">
                        <select id="alert-unit" class="filter-input select2-filter" style="width: 120px;">
                            <option value="%">内存可用率(%)</option>
                            <option value="条">条数</option>
                            <option value="MB">MB</option>
                            <option value="字节/秒">字节/秒</option>
                        </select>
                    </div>
                </div>
                
                <div>
                    <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                        告警渠道 <span style="color: #ef4444;">*</span>
                    </label>
                    <div style="margin-bottom: 4px;">
                        <small style="color: #6b7280; font-size: 0.75rem;">选择告警触发时通知的渠道</small>
                    </div>
                    <select id="alert-channels" class="filter-input select2-filter" style="width: 100%;">
                        <option value="">请选择告警渠道</option>
                        <!-- 动态加载渠道列表 -->
                    </select>
                </div>
                
                <div>
                    <label style="display: flex; align-items: center; gap: 8px; font-size: 0.875rem; color: #374151;">
                        <input type="checkbox" id="alert-enabled" checked style="width: 16px; height: 16px;">
                        立即启用此配置
                    </label>
                </div>
            </div>
        `;

        // 初始化Select2
        $('#alert-type-select').select2({
            theme: 'default',
            width: '100%',
            placeholder: '请选择告警类型',
            allowClear: true,
            templateResult: (option) => this.formatAlertTypeOption(option),
            templateSelection: (option) => this.formatAlertTypeOption(option)
        });

        $('#alert-unit').select2({
            theme: 'default',
            width: '120px',
            minimumResultsForSearch: Infinity
        });

        // 监听告警类型变化
        $('#alert-type-select').on('select2:select', (e) => {
            const selectedType = e.params.data.id;
            this.updateTargetConfig(selectedType);
            this.updateUnitOptions(selectedType);
        });

        // 加载渠道列表
        this.loadChannelOptions();

        modal.classList.add('show');
    }

    /**
     * 更新目标配置
     */
    updateTargetConfig(type) {
        const targetConfig = document.getElementById('target-config');
        const targetContainer = document.getElementById('target-select-container');

        targetConfig.style.display = 'block';

        let html = '';
        switch (type) {
            case 'broker-availability':
                html = `
                    <select id="broker-select" class="filter-input select2-filter" style="width: 100%;">
                        <option value="">请选择Broker</option>
                    </select>
                `;
                break;
            case 'consumer-lag':
                html = `
                    <div style="display: grid; gap: 12px;">
                        <select id="group-select" class="filter-input select2-filter" style="width: 100%;">
                            <option value="">请选择消费者组</option>
                        </select>
                        <select id="topic-select" class="filter-input select2-filter" style="width: 100%;">
                            <option value="">请选择Topic</option>
                        </select>
                    </div>
                `;
                break;
            case 'topic-capacity':
            case 'topic-throughput':
                html = `
                    <select id="topic-select" class="filter-input select2-filter" style="width: 100%;">
                        <option value="">请选择Topic</option>
                    </select>
                `;
                break;
        }

        targetContainer.innerHTML = html;

        // 初始化新的Select2
        targetContainer.querySelectorAll('.select2-filter').forEach(select => {
            $(select).select2({
                theme: 'default',
                width: '100%',
                minimumInputLength: 0,
                allowClear: true,
                placeholder: '请选择...'
            });
        });

        // 根据类型加载数据
        if (type === 'broker-availability') {
            this.loadBrokerOptions();
        } else if (type === 'consumer-lag') {
            this.loadConsumerGroupOptions();
            // 监听消费者组变化
            const groupSelect = document.getElementById('group-select');
            if (groupSelect) {
                $(groupSelect).on('select2:select', (e) => {
                    const groupId = e.params.data.id;
                    this.loadTopicOptionsByGroup(groupId);
                });
            }
        } else if (type === 'topic-capacity') {
            this.loadTopicOptionsByMetricType('capacity');
        } else if (type === 'topic-throughput') {
            this.loadTopicOptionsByMetricType('byte_in');
        }
    }

    /**
     * 更新单位选项
     */
    updateUnitOptions(type) {
        const unitSelect = document.getElementById('alert-unit');
        unitSelect.innerHTML = '';

        let options = '';
        switch (type) {
            case 'broker-availability':
                options = '<option value="%">内存可用率(%)</option>';
                break;
            case 'consumer-lag':
                options = '<option value="条">条数</option>';
                break;
            case 'topic-capacity':
                options = '<option value="MB">MB</option><option value="GB">GB</option>';
                break;
            case 'topic-throughput':
                options = '<option value="字节/秒">字节/秒</option>';
                break;
        }

        unitSelect.innerHTML = options;
        $(unitSelect).trigger('change');
    }

    /**
     * 加载Broker选项
     */
    async loadBrokerOptions() {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/brokers?cid=${cid}`);
            if (response.ok) {
                const brokers = await response.json();
                const brokerSelect = document.getElementById('broker-select');

                brokerSelect.innerHTML = '<option value="">请选择Broker</option>' +
                    brokers.map(broker => {
                        // 提取IP地址（去掉端口）
                        const ip = broker.host.split(':')[0];
                        return `<option value="${ip}" data-broker-id="${broker.brokerId}">${broker.host}</option>`;
                    }).join('');

                $(brokerSelect).trigger('change');
            }
        } catch (error) {
            this.showToast('加载Broker列表失败', 'error');
        }
    }

    /**
     * 加载消费者组选项
     */
    async loadConsumerGroupOptions() {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/consumer-groups?cid=${cid}`);
            if (response.ok) {
                const groups = await response.json();
                const groupSelect = document.getElementById('group-select');

                groupSelect.innerHTML = '<option value="">请选择消费者组</option>' +
                    groups.map(group => `<option value="${group.groupId}">${group.groupId}</option>`).join('');

                $(groupSelect).trigger('change');
            }
        } catch (error) {
            this.showToast('加载消费者组列表失败', 'error');
        }
    }

    /**
     * 根据消费者组加载Topic选项
     */
    async loadTopicOptionsByGroup(groupId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/consumer-topics?cid=${cid}&groupId=${groupId}`);
            if (response.ok) {
                const topics = await response.json();
                const topicSelect = document.getElementById('topic-select');

                topicSelect.innerHTML = '<option value="">请选择Topic</option>' +
                    topics.map(topic => `<option value="${topic.topic}">${topic.topic}</option>`).join('');

                $(topicSelect).trigger('change');
            }
        } catch (error) {
            this.showToast('加载Topic列表失败', 'error');
        }
    }

    /**
     * 根据指标类型加载Topic选项
     */
    async loadTopicOptionsByMetricType(metricType) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/topics-by-metric?cid=${cid}&metricType=${metricType}`);
            if (response.ok) {
                const topics = await response.json();
                const topicSelect = document.getElementById('topic-select');

                topicSelect.innerHTML = '<option value="">请选择Topic</option>' +
                    topics.map(topic => `<option value="${topic.topic}">${topic.topic}</option>`).join('');

                $(topicSelect).trigger('change');
            }
        } catch (error) {
            this.showToast('加载Topic列表失败', 'error');
        }
    }

    /**
     * 获取当前集群ID
     */
    getCurrentClusterId() {
        // 从URL参数或session storage获取当前集群ID
        const params = new URLSearchParams(window.location.search);
        const cid = params.get('cid') || sessionStorage.getItem('currentClusterId') || '1';
        return cid;
    }

    /**
     * 加载渠道选项
     */
    async loadChannelOptions() {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels?cid=${cid}`);
            if (response.ok) {
                const channels = await response.json();
                const channelSelect = document.getElementById('alert-channels');

                channelSelect.innerHTML = channels.map(channel => {
                    const channelInfo = this.channelList.find(c => c.key === channel.type);
                    return `<option value="${channel.id}" data-type="${channel.type}">${channel.name}</option>`;
                }).join('');

                $(channelSelect).select2({
                    theme: 'default',
                    width: '100%',
                    placeholder: '请选择告警渠道',
                    allowClear: true,
                    minimumResultsForSearch: Infinity, // 禁用搜索功能
                    templateResult: (option) => this.formatChannelOption(option),
                    templateSelection: (option) => this.formatChannelOption(option)
                });
            }
        } catch (error) {
            ('加载渠道列表失败:', error);
        }
    }

    /**
     * 加载编辑渠道选项
     */
    async loadEditChannelOptions(selectedChannel = null) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/channels?cid=${cid}`);
            if (response.ok) {
                const channels = await response.json();
                const channelSelect = document.getElementById('edit-alert-channels');

                channelSelect.innerHTML = channels.map(channel => {
                    const channelInfo = this.channelList.find(c => c.key === channel.type);
                    // 检查是否选中 - 处理单个渠道
                    const isSelected = selectedChannel && (
                        (selectedChannel.id && selectedChannel.id === channel.id) ||
                        (selectedChannel.channelId && selectedChannel.channelId === channel.id)
                    );
                    return `<option value="${channel.id}" data-type="${channel.type}" ${isSelected ? 'selected' : ''}>${channel.name}</option>`;
                }).join('');

                $(channelSelect).select2({
                    theme: 'default',
                    width: '100%',
                    placeholder: '请选择告警渠道',
                    allowClear: true,
                    minimumResultsForSearch: Infinity, // 禁用搜索功能
                    templateResult: (option) => this.formatChannelOption(option),
                    templateSelection: (option) => this.formatChannelOption(option)
                });
            }
        } catch (error) {
            this.showToast('加载编辑渠道列表失败', 'error');
        }
    }

    /**
     * 关闭创建告警类型
     */
    closeCreateAlertType() {
        const modal = document.getElementById('create-alert-type-modal');
        modal.classList.remove('show');
    }

    /**
     * 保存创建告警类型
     */
    async saveCreateAlertType() {
        const type = document.getElementById('alert-type-select').value;
        const threshold = parseFloat(document.getElementById('alert-threshold').value);
        const unit = document.getElementById('alert-unit').value;
        const enabled = document.getElementById('alert-enabled').checked;
        const selectedChannel = document.getElementById('alert-channels').value;
        const channelId = selectedChannel ? parseInt(selectedChannel) : null;

        if (!type || !threshold || !unit || !channelId) {
            this.showToast('请填写完整的配置信息', 'error');
            return;
        }

        // 获取目标配置
        let target = 'all';
        const targetConfig = document.getElementById('target-config');
        if (targetConfig.style.display !== 'none') {
            switch (type) {
                case 'broker-availability':
                    const brokerIp = document.getElementById('broker-select')?.value;
                    target = brokerIp || 'all';
                    break;
                case 'consumer-lag':
                    const groupId = document.getElementById('group-select')?.value;
                    const consumerTopicName = document.getElementById('topic-select')?.value;
                    if (groupId && consumerTopicName) {
                        target = `${groupId},${consumerTopicName}`;
                    } else {
                        target = 'all';
                    }
                    break;
                case 'topic-capacity':
                case 'topic-throughput':
                    const topicTopicName = document.getElementById('topic-select')?.value;
                    target = topicTopicName || 'all';
                    break;
            }
        }

        // 检查监控目标是否存在
        if (target !== 'all') {
            try {
                const cid = this.getCurrentClusterId();
                const checkResponse = await fetch(`/api/alerts/check-target?cid=${cid}&target=${encodeURIComponent(target)}`);
                if (checkResponse.ok) {
                    const checkResult = await checkResponse.json();
                    if (!checkResult.exists) {
                        this.showToast(`监控目标不存在: ${target}`, 'error');
                        return;
                    }
                } else {
                    this.showToast('检查监控目标失败', 'error');
                    return;
                }
            } catch (error) {
                ('检查监控目标异常:', error);
                this.showToast('检查监控目标异常', 'error');
                return;
            }
        }

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs?cid=${cid}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    type: type,
                    name: this.getTypeText(type),
                    description: `监控${this.getTypeText(type)}，当超过阈值时触发告警`,
                    threshold: threshold,
                    unit: unit,
                    target: target,
                    channelId: channelId,
                    enabled: enabled,
                    createdBy: 'admin'
                })
            });

            if (response.ok) {
                this.showToast('告警类型配置创建成功', 'success');
                this.closeCreateAlertType();
                this.configAlertTypes(); // 刷新列表
            } else {
                const errorText = await response.text();
                this.showToast(`创建失败: ${errorText}`, 'error');
            }
        } catch (error) {
            ('创建告警类型配置异常:', error);
            this.showToast('创建告警类型配置异常', 'error');
        }
    }

    /**
     * 查看告警类型配置详情
     */
    async viewAlertType(configId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/${configId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取告警类型配置详情失败', 'error');
                return;
            }

            const config = await response.json();
            const typeInfo = this.typeList.find(t => t.key === config.type);

            const modal = document.getElementById('view-alert-type-modal');
            const content = document.getElementById('view-alert-type-content');

            content.innerHTML = `
                <div class="alert-detail-meta" style="grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 24px;">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-bell"></i>
                            告警类型
                        </div>
                        <div class="alert-meta-value">
                            <span style="font-weight: 500; color: #1f2937;">${typeInfo?.name || config.type}</span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-toggle-on"></i>
                            启用状态
                        </div>
                        <div class="alert-meta-value">
                            <span class="alert-status ${config.enabled ? 'resolved' : 'active'}">
                                ${config.enabled ? '启用' : '禁用'}
                            </span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-chart-line"></i>
                            告警阈值
                        </div>
                        <div class="alert-meta-value">
                            <span style="font-weight: 500; color: #1f2937;">${config.threshold} ${config.unit}</span>
                        </div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-user"></i>
                            创建人
                        </div>
                        <div class="alert-meta-value">${config.createdBy || 'admin'}</div>
                    </div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa fa-align-left"></i>
                        配置描述
                    </div>
                    <div class="alert-detail-value">${config.description || '暂无描述'}</div>
                </div>

                <div class="alert-detail-section">
                    <div class="alert-detail-label">
                        <i class="fa-solid fa-paper-plane"></i>
                        告警渠道
                    </div>
                    <div class="alert-channels-detail">
                        ${config.channelStatus ? (() => {
                            // 优先显示渠道名称，如果没有则显示渠道类型对应的名称
                            let channelDisplayName;
                            if (config.channelStatus.channelName) {
                                channelDisplayName = config.channelStatus.channelName;
                            } else {
                                const channelInfo = this.channelList.find(c => c.key === config.channelStatus.channelType);
                                channelDisplayName = channelInfo?.name || config.channelStatus.channelType;
                            }
                            return `
                                <div class="channel-detail-item">
                                    <div class="channel-detail-status ${config.channelStatus.status || 'success'}"></div>
                                    <span>${channelDisplayName}</span>
                                    <span style="margin-left: auto; font-size: 0.75rem; color: #6b7280;">
                                        ${config.channelStatus.status || '正常'}
                                    </span>
                                </div>
                            `;
                        })() : '<div style="color: #6b7280; font-size: 0.875rem;">未配置告警渠道</div>'}
                    </div>
                </div>

                <div class="alert-detail-meta">
                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-calendar"></i>
                            创建时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(config.createdAt))}</div>
                    </div>

                    <div class="alert-meta-item">
                        <div class="alert-meta-label">
                            <i class="fa fa-edit"></i>
                            更新时间
                        </div>
                        <div class="alert-meta-value">${this.formatFullDate(new Date(config.updatedAt))}</div>
                    </div>
                </div>
            `;

            modal.classList.add('show');
        } catch (error) {
            ('获取告警类型配置详情异常:', error);
            this.showToast('获取告警类型配置详情异常', 'error');
        }
    }

    /**
     * 编辑告警类型配置
     */
    async editAlertType(configId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/${configId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取告警类型配置失败', 'error');
                return;
            }

            const config = await response.json();
            const typeInfo = this.typeList.find(t => t.key === config.type);

            const modal = document.getElementById('edit-alert-type-modal');
            const content = document.getElementById('edit-alert-type-content');

            content.innerHTML = `
                <div style="margin-bottom: 20px;">
                    <p class="text-gray-600 text-sm">编辑告警类型配置，只能修改告警阈值和告警渠道。</p>
                </div>
                
                <div style="display: grid; gap: 20px;">
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            告警类型
                        </label>
                        <div style="display: flex; align-items: center; gap: 8px; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; background: #f3f4f6;">
                            <i class="fa fa-bell" style="color: #6b7280;"></i>
                            <span style="color: #6b7280;">${typeInfo?.name || config.type}</span>
                        </div>
                        <small style="color: #6b7280; font-size: 0.75rem;">告警类型不可修改</small>
                    </div>
                    
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            告警阈值 <span style="color: #ef4444;">*</span>
                        </label>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <input type="number" id="edit-alert-threshold" 
                                   value="${config.threshold}"
                                   placeholder="请输入阈值"
                                   style="flex: 1; padding: 12px 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 0.875rem;">
                            <select id="edit-alert-unit" class="filter-input select2-filter" style="width: 120px;">
                                <option value="%" ${config.unit === '%' ? 'selected' : ''}>内存可用率(%)</option>
                                <option value="条" ${config.unit === '条' ? 'selected' : ''}>条数</option>
                                <option value="MB" ${config.unit === 'MB' ? 'selected' : ''}>MB</option>
                                <option value="GB" ${config.unit === 'GB' ? 'selected' : ''}>GB</option>
                                <option value="字节/秒" ${config.unit === '字节/秒' ? 'selected' : ''}>字节/秒</option>
                            </select>
                        </div>
                    </div>
                    
                    <div>
                        <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #374151;">
                            告警渠道 <span style="color: #ef4444;">*</span>
                        </label>
                        <div style="margin-bottom: 4px;">
                            <small style="color: #6b7280; font-size: 0.75rem;">选择告警触发时通知的渠道</small>
                        </div>
                        <select id="edit-alert-channels" class="filter-input select2-filter" style="width: 100%;">
                            <!-- 动态加载渠道列表 -->
                        </select>
                    </div>
                    
                    <div>
                        <label style="display: flex; align-items: center; gap: 8px; font-size: 0.875rem; color: #374151;">
                            <input type="checkbox" id="edit-alert-enabled" ${config.enabled ? 'checked' : ''} style="width: 16px; height: 16px;">
                            启用此配置
                        </label>
                    </div>
                </div>
            `;

            // 初始化Select2
            $('#edit-alert-unit').select2({
                theme: 'default',
                width: '120px',
                minimumResultsForSearch: Infinity
            });

            // 加载渠道列表 - 如果有渠道状态，使用它
            await this.loadEditChannelOptions(config.channelStatus);

            // 设置模态框数据属性
            modal.dataset.configId = configId;

            modal.classList.add('show');
        } catch (error) {
            ('获取告警类型配置异常:', error);
            this.showToast('获取告警类型配置异常', 'error');
        }
    }

    /**
     * 关闭查看告警类型配置
     */
    closeViewAlertType() {
        const modal = document.getElementById('view-alert-type-modal');
        modal.classList.remove('show');
    }

    /**
     * 关闭编辑告警类型配置
     */
    closeEditAlertType() {
        const modal = document.getElementById('edit-alert-type-modal');
        modal.classList.remove('show');
    }

    /**
     * 保存编辑告警类型配置
     */
    async saveEditAlertType() {
        const modal = document.getElementById('edit-alert-type-modal');
        const configId = modal.dataset.configId;
        const threshold = parseFloat(document.getElementById('edit-alert-threshold').value);
        const unit = document.getElementById('edit-alert-unit').value;
        const enabled = document.getElementById('edit-alert-enabled').checked;
        const selectedChannel = document.getElementById('edit-alert-channels').value;
        const channelId = selectedChannel ? parseInt(selectedChannel) : null;

        if (!threshold || !unit || !channelId) {
            this.showToast('请填写完整的配置信息', 'error');
            return;
        }

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/${configId}?cid=${cid}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    threshold: threshold,
                    unit: unit,
                    channelId: channelId,
                    enabled: enabled
                })
            });

            if (response.ok) {
                this.showToast('告警类型配置更新成功', 'success');
                this.closeEditAlertType();
                this.configAlertTypes(); // 刷新列表
            } else {
                const errorText = await response.text();
                this.showToast(`更新失败: ${errorText}`, 'error');
            }
        } catch (error) {
            this.showToast('更新告警类型配置异常', 'error');
        }
    }

    /**
     * 删除告警类型配置
     */
    async deleteAlertType(configId) {
        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/${configId}?cid=${cid}`);
            if (!response.ok) {
                this.showToast('获取告警类型配置失败', 'error');
                return;
            }

            const config = await response.json();
            const typeInfo = this.typeList.find(t => t.key === config.type);

            const modal = document.getElementById('delete-alert-type-confirm-modal');
            const content = document.getElementById('delete-alert-type-confirm-content');

            content.innerHTML = `
                <div style="text-align: center; padding: 20px 0;">
                    <div style="width: 64px; height: 64px; border-radius: 50%; background: #fef2f2; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
                        <i class="fa fa-exclamation-triangle" style="color: #ef4444; font-size: 24px;"></i>
                    </div>
                    <h3 style="font-size: 1.125rem; font-weight: 600; color: #1f2937; margin-bottom: 8px;">确认删除告警类型配置</h3>
                    <p style="color: #6b7280; font-size: 0.875rem; margin-bottom: 16px;">
                        您即将删除告警类型配置 <strong style="color: #1f2937;">"${typeInfo?.name || config.type}"</strong>
                    </p>
                    <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 16px;">
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                            <i class="fa fa-chart-line" style="color: #6b7280;"></i>
                            <span style="font-weight: 500; color: #374151;">告警阈值: ${config.threshold} ${config.unit}</span>
                        </div>
                        <div style="font-size: 0.875rem; color: #6b7280; margin-bottom: 8px;">
                            配置描述: ${config.description || '暂无描述'}
                        </div>
                        ${config.channelStatus ? `
                        <div style="font-size: 0.875rem; color: #6b7280;">
                            告警渠道: ${config.channelStatus.channelName || (() => {
                                const channelInfo = this.channelList.find(c => c.key === config.channelStatus.channelType);
                                return channelInfo?.name || config.channelStatus.channelType;
                            })()}
                        </div>
                        ` : ''}
                    </div>
                    <p style="color: #ef4444; font-size: 0.875rem; font-weight: 500;">
                        ⚠️ 删除后无法恢复，请谨慎操作
                    </p>
                </div>
            `;

            // 设置模态框数据属性
            modal.dataset.configId = configId;
            modal.dataset.configName = typeInfo?.name || config.type;

            modal.classList.add('show');
        } catch (error) {
            ('获取告警类型配置异常:', error);
            this.showToast('获取告警类型配置异常', 'error');
        }
    }

    /**
     * 关闭删除告警类型配置确认
     */
    closeDeleteAlertTypeConfirm() {
        const modal = document.getElementById('delete-alert-type-confirm-modal');
        modal.classList.remove('show');
    }

    /**
     * 确认删除告警类型配置
     */
    async confirmDeleteAlertType() {
        const modal = document.getElementById('delete-alert-type-confirm-modal');
        const configId = modal.dataset.configId;
        const configName = modal.dataset.configName;

        try {
            const cid = this.getCurrentClusterId();
            const response = await fetch(`/api/alerts/type-configs/${configId}?cid=${cid}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                this.showToast(`告警类型配置"${configName}"删除成功`, 'success');
                this.closeDeleteAlertTypeConfirm();
                this.configAlertTypes(); // 刷新列表
            } else {
                const errorText = await response.text();
                this.showToast(`删除失败: ${errorText}`, 'error');
            }
        } catch (error) {
            ('删除告警类型配置异常:', error);
            this.showToast('删除告警类型配置异常', 'error');
        }
    }

    /**
     * 格式化日期
     */
    formatDate(date) {
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    /**
     * 格式化完整日期
     */
    formatFullDate(date) {
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    /**
     * 获取类型文本
     */
    getTypeText(type) {
        const typeMap = {
            'broker-availability': 'Broker可用性告警',
            'consumer-lag': '消费者组积压告警',
            'topic-capacity': '主题容量告警',
            'topic-throughput': '主题写入速度告警'
        };
        return typeMap[type] || type;
    }

    /**
     * 获取级别文本
     */
    getLevelText(level) {
        const levelMap = {
            'critical': '严重',
            'warning': '警告',
            'info': '信息'
        };
        return levelMap[level] || level;
    }

    /**
     * 获取状态文本
     */
    getStatusText(status) {
        const statusMap = {
            0: '未处理',
            1: '已处理',
            2: '已忽略',
            3: '已解决'
        };
        return statusMap[status] || status;
    }

    /**
     * 计算持续时间
     */
    calculateDuration(createdAt, updatedAt) {
        if (!createdAt || !updatedAt) {
            return '0秒';
        }

        const created = new Date(createdAt);
        const updated = new Date(updatedAt);
        const diffMs = updated.getTime() - created.getTime();

        if (diffMs < 0) {
            return '0秒';
        }

        const diffSeconds = Math.floor(diffMs / 1000);
        const diffMinutes = Math.floor(diffSeconds / 60);
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffDays > 0) {
            const remainingHours = diffHours % 24;
            return remainingHours > 0 ? `${diffDays}天${remainingHours}小时` : `${diffDays}天`;
        } else if (diffHours > 0) {
            const remainingMinutes = diffMinutes % 60;
            return remainingMinutes > 0 ? `${diffHours}小时${remainingMinutes}分钟` : `${diffHours}小时`;
        } else if (diffMinutes > 0) {
            const remainingSeconds = diffSeconds % 60;
            return remainingSeconds > 0 ? `${diffMinutes}分钟${remainingSeconds}秒` : `${diffMinutes}分钟`;
        } else {
            return `${diffSeconds}秒`;
        }
    }

    /**
     * 显示提示消息
     */
    showToast(message, type = 'info') {
        // 移除现有的toast
        const existingToast = document.querySelector('.toast');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="fa fa-${type === 'success' ? 'check' : type === 'error' ? 'times' : 'info'}"></i>
            ${message}
        `;

        document.body.appendChild(toast);

        // 显示动画
        setTimeout(() => toast.classList.add('show'), 100);

        // 自动隐藏
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
}

// 全局函数
function applyFilters() {
    alertManager.applyFilters();
}

function resetFilters() {
    alertManager.resetFilters();
}

function refreshAlerts() {
    alertManager.refreshAlerts();
}

function prevPage() {
    alertManager.prevPage();
}

function nextPage() {
    alertManager.nextPage();
}

function closeAlertDetail() {
    alertManager.closeAlertDetail();
}

function acknowledgeAlert(alertId) {
    // If no alertId provided, get it from the modal
    if (!alertId) {
        const modal = document.getElementById('alert-detail-modal');
        alertId = modal ? modal.dataset.alertId : null;
    }

    if (!alertId) {
        console.error('No alert ID available for acknowledge operation');
        return;
    }

    alertManager.acknowledgeAlert(alertId);
}

function resolveAlert(alertId) {
    // If no alertId provided, get it from the modal
    if (!alertId) {
        const modal = document.getElementById('alert-detail-modal');
        alertId = modal ? modal.dataset.alertId : null;
    }

    if (!alertId) {
        console.error('No alert ID available for resolve operation');
        return;
    }

    alertManager.resolveAlert(alertId);
}

function ignoreAlert(alertId) {
    alertManager.ignoreAlert(alertId);
}

function configChannels() {
    alertManager.configChannels();
}

function closeChannelConfig() {
    alertManager.closeChannelConfig();
}

function saveChannelConfig() {
    alertManager.saveChannelConfig();
}

function createChannel() {
    alertManager.createChannel();
}

function closeCreateChannel() {
    alertManager.closeCreateChannel();
}

function saveCreateChannel() {
    alertManager.saveCreateChannel();
}

function viewChannel(channelId) {
    alertManager.viewChannel(channelId);
}

function editChannel(channelId) {
    alertManager.editChannel(channelId);
}

function deleteChannel(channelId) {
    alertManager.deleteChannel(channelId);
}

function closeViewChannel() {
    alertManager.closeViewChannel();
}

function closeEditChannel() {
    alertManager.closeEditChannel();
}

function closeDeleteConfirm() {
    alertManager.closeDeleteConfirm();
}

function saveEditChannel() {
    alertManager.saveEditChannel();
}

function confirmDeleteChannel() {
    alertManager.confirmDeleteChannel();
}

function performRebalance() {
    alertManager.performRebalance();
}

function configAlertTypes() {
    alertManager.configAlertTypes();
}

function closeAlertTypeConfig() {
    alertManager.closeAlertTypeConfig();
}

function createAlertType() {
    alertManager.createAlertType();
}

function closeCreateAlertType() {
    alertManager.closeCreateAlertType();
}

function saveCreateAlertType() {
    alertManager.saveCreateAlertType();
}

function viewAlertType(configId) {
    alertManager.viewAlertType(configId);
}

function editAlertType(configId) {
    alertManager.editAlertType(configId);
}

function deleteAlertType(configId) {
    alertManager.deleteAlertType(configId);
}

function closeViewAlertType() {
    alertManager.closeViewAlertType();
}

function closeEditAlertType() {
    alertManager.closeEditAlertType();
}

function saveEditAlertType() {
    alertManager.saveEditAlertType();
}

function closeDeleteAlertTypeConfirm() {
    alertManager.closeDeleteAlertTypeConfirm();
}

function confirmDeleteAlertType() {
    alertManager.confirmDeleteAlertType();
}

function goToAlertTypeConfigPage(page) {
    alertManager.goToAlertTypeConfigPage(page);
}

function prevAlertTypeConfigPage() {
    alertManager.prevAlertTypeConfigPage();
}

function nextAlertTypeConfigPage() {
    alertManager.nextAlertTypeConfigPage();
}

// 初始化告警管理器
let alertManager;
document.addEventListener('DOMContentLoaded', () => {
    alertManager = new AlertManager();
});

// Toast样式
const style = document.createElement('style');
style.textContent = `
    .toast {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1100;
        padding: 12px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 8px;
        transform: translateX(100%);
        opacity: 0;
        transition: all 0.3s ease;
        min-width: 200px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .toast.show {
        transform: translateX(0);
        opacity: 1;
    }

    .toast.success {
        background: #10b981;
    }

    .toast.error {
        background: #ef4444;
    }

    .toast.info {
        background: #3b82f6;
    }
`;
document.head.appendChild(style); 