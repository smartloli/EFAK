// Cluster页面专用功能
window.ClusterModule = {
    // 分页配置
    pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
    },

    // 搜索和过滤
    searchKeyword: '',
    statusFilter: '',

    // 自动刷新配置
    autoRefreshInterval: null,
    refreshCountdown: 60,
    // 支持的时间维度配置
    timeRangeOptions: [
        { value: '5m', label: '最近5分钟', description: '显示最近5分钟的原始数据' },
        { value: '15m', label: '最近15分钟', description: '显示最近15分钟的原始数据' },
        { value: '30m', label: '最近30分钟', description: '显示最近30分钟的原始数据' },
        { value: '1h', label: '最近1小时', description: '显示最近1小时的原始数据' },
        { value: '6h', label: '最近6小时', description: '显示最近6小时的按小时聚合数据' },
        { value: '24h', label: '最近24小时', description: '显示最近24小时的按小时聚合数据' },
        { value: '7d', label: '最近7天', description: '显示最近7天的按天聚合数据' },
        { value: '30d', label: '最近30天', description: '显示最近30天的按天聚合数据' }
    ],

    // 默认时间范围
    defaultTimeRange: '1h',

    // 初始化
    async init() {

        // 设置页面标题（如果CommonModule存在）
        if (window.CommonModule && window.CommonModule.setPageTitle) {
            window.CommonModule.setPageTitle('集群管理');
        }

        // 先检查用户权限
        await this.checkUserPermissions();

        // 从URL解析cid（兼容旧的clusterId参数）
        try {
            const params = new URLSearchParams(window.location.search);
            this.clusterId = params.get('cid') || params.get('clusterId');
        } catch (e) {
            console.warn('解析URL参数失败:', e);
        }

        this.initTable();
        this.initCharts();
        this.initEventListeners();
        this.initCustomTooltips();
        this.initConfigSections();
        this.loadBrokerData();
        this.loadClusterStats();
        this.initDistributedNodes();
        this.startAutoRefresh();
        this.checkFontAwesome();
        this.initializeCharts();
        this.initBrokerPerformanceCharts();
        this.initTimeRangeSelectors();
    },

    // ===== 分布式节点管理功能 =====

    // 分布式节点数据
    distributedNodes: [],
    distributedPagination: {
        currentPage: 1,
        pageSize: 5,
        total: 0
    },
    distributedSearchKeyword: '',
    distributedAutoRefreshInterval: null,
    // 搜索防抖计时器
    distributedSearchTimer: null,
    distributedRefreshCountdown: 60,

    // 初始化分布式节点功能
    initDistributedNodes() {
        this.loadDistributedNodes();
        this.setupDistributedEventListeners();
        this.startDistributedAutoRefresh();
    },

    // 加载分布式节点数据
    async loadDistributedNodes() {
        try {
            const params = new URLSearchParams({
                page: this.distributedPagination.currentPage - 1, // 后端从0开始
                size: this.distributedPagination.pageSize,
                keyword: this.distributedSearchKeyword
            });

            const response = await fetch(`/api/distributed-task/services/details?${params}`);
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    this.distributedNodes = result.data || [];
                    this.distributedPagination.total = result.total || 0;
                } else {
                    this.distributedNodes = [];
                    this.distributedPagination.total = 0;
                }
                this.renderDistributedNodes();
                this.updateDistributedPaginationInfo();
                this.renderDistributedPaginationButtons();
            } else {
                console.error('加载分布式节点失败:', response.statusText);
                this.showToast('加载分布式节点失败', 'error');
                this.distributedNodes = [];
                this.distributedPagination.total = 0;
                this.renderDistributedNodes();
            }
        } catch (error) {
            console.error('加载分布式节点出错:', error);
            this.showToast('加载分布式节点出错', 'error');
            this.distributedNodes = [];
            this.distributedPagination.total = 0;
            this.renderDistributedNodes();
        }
    },

    // 渲染分布式节点列表
    renderDistributedNodes() {
        const tbody = document.getElementById('distributed-tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (this.distributedNodes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 40px; color: #6b7280;">
                        暂无分布式节点数据
                    </td>
                </tr>
            `;
            return;
        }

        this.distributedNodes.forEach(node => {
            const row = this.createDistributedNodeRow(node);
            tbody.appendChild(row);
        });
    },

    // 创建分布式节点行
    createDistributedNodeRow(node) {
        const row = document.createElement('tr');

        const statusClass = this.getDistributedStatusClass(node.status);
        const heartbeatClass = this.getHeartbeatClass(node.lastHeartbeat);

        row.innerHTML = `
            <td>${node.nodeId || '-'}</td>
            <td>${node.ipAddress || '-'}</td>
            <td>${node.pid || '-'}</td>
            <td>
                <span class="distributed-status ${statusClass}">
                    <i class="fa fa-circle"></i>
                    ${this.getStatusText(node.status)}
                </span>
            </td>
            <td>
                <span class="heartbeat-time ${heartbeatClass}">
                    ${this.formatHeartbeatTime(node.lastHeartbeat)}
                </span>
            </td>
        `;

        return row;
    },


    // 获取分布式状态样式类
    getDistributedStatusClass(status) {
        switch (status) {
            case 'ONLINE': return 'online';
            case 'OFFLINE': return 'offline';
            case 'WARNING': return 'warning';
            case 'BUSY': return 'busy';
            default: return 'offline';
        }
    },


    // 获取心跳时间样式类
    getHeartbeatClass(heartbeat) {
        if (!heartbeat) return 'danger';

        const now = new Date();
        const heartbeatTime = new Date(heartbeat);
        const diffMinutes = (now - heartbeatTime) / (1000 * 60);

        if (diffMinutes <= 1) return 'recent';
        if (diffMinutes <= 5) return 'warning';
        return 'danger';
    },

    // 获取状态文本
    getStatusText(status) {
        const statusMap = {
            'ONLINE': '在线',
            'OFFLINE': '离线',
            'WARNING': '警告',
            'BUSY': '忙碌'
        };
        return statusMap[status] || '未知';
    },


    // 格式化心跳时间
    formatHeartbeatTime(heartbeat) {
        if (!heartbeat) return '未知';

        const now = new Date();
        const heartbeatTime = new Date(heartbeat);
        const diffSeconds = Math.floor((now - heartbeatTime) / 1000);

        if (diffSeconds < 60) {
            return `${diffSeconds}秒前`;
        } else if (diffSeconds < 3600) {
            return `${Math.floor(diffSeconds / 60)}分钟前`;
        } else {
            return heartbeatTime.toLocaleString();
        }
    },

    // 更新分布式分页信息
    updateDistributedPaginationInfo() {
        const start = (this.distributedPagination.currentPage - 1) * this.distributedPagination.pageSize + 1;
        const end = Math.min(this.distributedPagination.currentPage * this.distributedPagination.pageSize, this.distributedPagination.total);

        const pageStartElement = document.getElementById('distributed-page-start');
        const pageEndElement = document.getElementById('distributed-page-end');
        const totalCountElement = document.getElementById('distributed-total-count');

        if (pageStartElement) pageStartElement.textContent = start;
        if (pageEndElement) pageEndElement.textContent = end;
        if (totalCountElement) totalCountElement.textContent = this.distributedPagination.total;
    },

    // 渲染分布式分页按钮
    renderDistributedPaginationButtons() {
        const container = document.getElementById('distributed-pagination-pages');
        if (!container) return;

        container.innerHTML = '';

        const totalPages = Math.ceil(this.distributedPagination.total / this.distributedPagination.pageSize);
        const currentPage = this.distributedPagination.currentPage;

        // 显示最多5个页码
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);

        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.className = `page-btn ${i === currentPage ? 'active' : ''}`;
            pageBtn.textContent = i;
            pageBtn.onclick = () => this.goToDistributedPage(i);
            container.appendChild(pageBtn);
        }

        // 更新分页按钮状态
        const firstPageBtn = document.getElementById('distributed-first-page');
        const prevPageBtn = document.getElementById('distributed-prev-page');
        const nextPageBtn = document.getElementById('distributed-next-page');
        const lastPageBtn = document.getElementById('distributed-last-page');

        if (firstPageBtn) firstPageBtn.disabled = currentPage === 1;
        if (prevPageBtn) prevPageBtn.disabled = currentPage === 1;
        if (nextPageBtn) nextPageBtn.disabled = currentPage === totalPages;
        if (lastPageBtn) lastPageBtn.disabled = currentPage === totalPages;
    },

    // 跳转到指定分布式页面
    goToDistributedPage(page) {
        this.distributedPagination.currentPage = page;
        this.loadDistributedNodes();
    },

    // 分布式节点搜索
    filterDistributedNodes() {
        const searchInput = document.getElementById('distributed-search-input');
        this.distributedSearchKeyword = searchInput ? searchInput.value.trim() : '';
        this.distributedPagination.currentPage = 1;

        // 添加搜索状态指示
        if (searchInput) {
            searchInput.style.borderColor = '#165DFF';
            setTimeout(() => {
                if (searchInput) {
                    searchInput.style.borderColor = '';
                }
            }, 500);
        }

        this.loadDistributedNodes();
    },

    // 清空分布式搜索
    clearDistributedSearch() {
        const searchInput = document.getElementById('distributed-search-input');
        if (searchInput) {
            searchInput.value = '';
        }

        // 清除防抖计时器
        if (this.distributedSearchTimer) {
            clearTimeout(this.distributedSearchTimer);
            this.distributedSearchTimer = null;
        }

        this.distributedSearchKeyword = '';
        this.distributedPagination.currentPage = 1;
        this.loadDistributedNodes();
    },

    // 开始分布式自动刷新
    startDistributedAutoRefresh() {
        if (this.distributedAutoRefreshInterval) {
            clearInterval(this.distributedAutoRefreshInterval);
        }

        this.distributedAutoRefreshInterval = setInterval(() => {
            this.distributedRefreshCountdown--;
            const countdownElement = document.getElementById('distributed-refresh-countdown');
            if (countdownElement) {
                countdownElement.textContent = this.distributedRefreshCountdown;
            }

            if (this.distributedRefreshCountdown <= 0) {
                this.loadDistributedNodes();
                this.distributedRefreshCountdown = 60;
            }
        }, 1000);
    },

    // 设置分布式事件监听器
    setupDistributedEventListeners() {
        // 搜索框事件
        const searchInput = document.getElementById('distributed-search-input');
        const clearSearchBtn = document.getElementById('clear-distributed-search');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                if (clearSearchBtn) {
                    if (e.target.value.trim()) {
                        clearSearchBtn.style.display = 'block';
                    } else {
                        clearSearchBtn.style.display = 'none';
                    }
                }

                // 清除之前的计时器
                if (this.distributedSearchTimer) {
                    clearTimeout(this.distributedSearchTimer);
                }

                // 设置防抖搜索（300ms后执行）
                this.distributedSearchTimer = setTimeout(() => {
                    this.filterDistributedNodes();
                }, 300);
            });

            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    // 立即执行搜索，清除防抖计时器
                    if (this.distributedSearchTimer) {
                        clearTimeout(this.distributedSearchTimer);
                        this.distributedSearchTimer = null;
                    }
                    this.filterDistributedNodes();
                }
            });
        }

        if (clearSearchBtn) {
            clearSearchBtn.addEventListener('click', () => {
                this.clearDistributedSearch();
                // 隐藏清除按钮
                clearSearchBtn.style.display = 'none';
            });
        }

        // 刷新按钮事件
        const refreshBtn = document.getElementById('refresh-distributed');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                // 显示加载状态
                refreshBtn.innerHTML = '<i class="fa fa-spin fa-refresh"></i>';
                refreshBtn.disabled = true;

                // 重置倒计时
                this.distributedRefreshCountdown = 60;

                // 加载数据
                this.loadDistributedNodes().finally(() => {
                    // 恢复按钮状态
                    refreshBtn.innerHTML = '<i class="fa fa-refresh"></i>';
                    refreshBtn.disabled = false;
                    this.showToast('分布式节点数据已刷新', 'success');
                });
            });
        }

        // 分页按钮事件
        const firstPageBtn = document.getElementById('distributed-first-page');
        const prevPageBtn = document.getElementById('distributed-prev-page');
        const nextPageBtn = document.getElementById('distributed-next-page');
        const lastPageBtn = document.getElementById('distributed-last-page');

        if (firstPageBtn) {
            firstPageBtn.addEventListener('click', () => this.goToDistributedPage(1));
        }
        if (prevPageBtn) {
            prevPageBtn.addEventListener('click', () => this.goToDistributedPage(this.distributedPagination.currentPage - 1));
        }
        if (nextPageBtn) {
            nextPageBtn.addEventListener('click', () => this.goToDistributedPage(this.distributedPagination.currentPage + 1));
        }
        if (lastPageBtn) {
            lastPageBtn.addEventListener('click', () => this.goToDistributedPage(Math.ceil(this.distributedPagination.total / this.distributedPagination.pageSize)));
        }
    },

    // ===== 节点管理功能 =====


    // 下载模板
    downloadTemplate() {
        const link = document.createElement('a');
        link.href = '/api/brokers/template';
        link.download = 'broker_template.xlsx';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    },

    // 切换Kafka配置显示
    toggleKafkaConfig() {
        const kafkaConfig = document.getElementById('kafka-config');
        const toggleBtn = document.getElementById('toggle-kafka-config');

        if (kafkaConfig && toggleBtn) {
            if (kafkaConfig.style.display === 'none') {
                kafkaConfig.style.display = 'block';
                toggleBtn.textContent = '隐藏Kafka配置';
            } else {
                kafkaConfig.style.display = 'none';
                toggleBtn.textContent = '显示Kafka配置';
            }
        }
    },


    // 打开节点模态框
    openNodeModal() {
        const modal = document.getElementById('node-modal');
        if (modal) {
            modal.style.display = 'block';
        }
    },

    // 关闭节点模态框
    closeNodeModal() {
        const modal = document.getElementById('node-modal');
        if (modal) {
            modal.style.display = 'none';
        }
    },



    // ===== 图表初始化功能 =====

    // 初始化图表
    initializeCharts() {
        this.initBrokerPerformanceCharts();
    },

    // ===== 字体检查功能 =====

    // 检查Font Awesome是否加载
    checkFontAwesome() {
        const testElement = document.createElement('i');
        testElement.className = 'fa fa-home';
        testElement.style.position = 'absolute';
        testElement.style.left = '-9999px';
        document.body.appendChild(testElement);

        const computedStyle = window.getComputedStyle(testElement);
        const fontFamily = computedStyle.getPropertyValue('font-family');

        document.body.removeChild(testElement);

        if (fontFamily.indexOf('FontAwesome') === -1) {
            console.warn('Font Awesome未正确加载，使用文本替代');
            // 可以在这里添加文本替代逻辑
        }
    },

    // 检查用户权限
    async checkUserPermissions() {
        try {
            const response = await fetch('/api/users/current');
            if (response.ok) {
                const result = await response.json();
                const userData = result.data; // 从data字段获取用户信息

                // 检查是否包含ROLE_ADMIN或管理员角色
                // 支持roles为字符串或数组，以及displayRole字段
                const roles = userData.roles;
                const displayRole = userData.displayRole;

                // 判断是否为管理员
                this.isAdmin = (
                    // roles字段判断（字符串或数组）
                    (typeof roles === 'string' && (
                        roles === 'ROLE_ADMIN' ||
                        roles === 'ADMIN' ||
                        roles === '管理员' ||
                        roles.includes('ROLE_ADMIN') ||
                        roles.includes('ADMIN') ||
                        roles.includes('管理员')
                    )) ||
                    (Array.isArray(roles) && (
                        roles.includes('ROLE_ADMIN') ||
                        roles.includes('ADMIN') ||
                        roles.includes('管理员')
                    )) ||
                    // 检查displayRole字段
                    (displayRole && (
                        displayRole === '管理员' ||
                        displayRole === 'ADMIN' ||
                        displayRole.toLowerCase().includes('admin')
                    ))
                );

                this.currentUser = userData;

                // 权限检查完成后无需更新新增按钮显示（已移除）
            } else {
                this.isAdmin = false;
                console.warn('获取用户信息失败');
            }
        } catch (error) {
            this.isAdmin = false;
            console.error('权限检查失败:', error);
        }
    },

    // 从API加载Broker数据
    async loadBrokerData() {
        try {
            const request = {
                page: this.pagination.currentPage,
                pageSize: this.pagination.pageSize,
                search: this.searchKeyword,
                status: this.statusFilter,
                sortField: 'createdAt',
                sortOrder: 'DESC'
            };
            if (this.clusterId) {
                request.clusterId = this.clusterId;
            }

            const response = await fetch('/api/brokers/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`获取数据失败: ${response.status} ${errorText}`);
            }

            const data = await response.json();

            this.pagination.total = data.total || 0;
            this.renderBrokerTable(data.brokers || []);
            this.updatePaginationInfo();
            this.renderPaginationButtons();

            // 构造updateMetrics需要的数据结构
            const metricsData = {
                onlineNodes: data.stats?.onlineCount || 0,
                totalNodes: data.stats?.totalCount || 0,
                avgCpuUsage: data.stats?.avgCpuUsage || 0,
                avgMemoryUsage: data.stats?.avgMemoryUsage || 0
            };
            this.updateMetrics(metricsData);

            // 加载分布式服务节点数据
            await this.loadDistributedNodes();

        } catch (error) {
            console.error('加载Broker数据失败:', error);
            this.showToast('加载数据失败: ' + error.message, 'error');

            // 如果API调用失败，显示空状态
            this.showSampleData();
        }
    },

    // 加载集群统计数据
    async loadClusterStats() {
        try {
            const url = this.clusterId
                ? `/api/brokers/cluster/overview?clusterId=${encodeURIComponent(this.clusterId)}`
                : '/api/brokers/cluster/overview';
            const response = await fetch(url);
            if (response.ok) {
                const result = await response.json();
                if (result) {
                    // 更新集群概览卡片
                    this.updateClusterOverview(result);
                    // 更新关键指标卡片
                    this.updateMetrics({
                        onlineNodes: result.onlineNodes,
                        totalNodes: result.totalNodes,
                        avgCpuUsage: result.averageCpuUsage,
                        avgMemoryUsage: result.averageMemoryUsage
                    });
                } else {
                    // 如果接口失败，显示默认值
                    this.updateClusterOverview({
                        clusterStatus: '未知',
                        runtime: '未知',
                        averageLoad: null
                    });
                }
            } else {
                console.error('加载集群统计数据失败:', response.statusText);
                this.updateClusterOverview({
                    clusterStatus: '异常',
                    runtime: '未知',
                    averageLoad: null
                });
            }
        } catch (error) {
            console.error('加载集群统计数据失败:', error);
            this.updateClusterOverview({
                clusterStatus: '异常',
                runtime: '未知',
                averageLoad: null
            });
        }
    },



    // 更新集群概览信息
    updateClusterOverview(stats) {
        // 更新集群状态
        const statusElement = document.getElementById('cluster-status');
        if (statusElement) {
            statusElement.textContent = stats.clusterStatus || '未知';
        }

        // 更新运行时长
        const runtimeElement = document.getElementById('cluster-runtime');
        if (runtimeElement) {
            runtimeElement.textContent = stats.runtime || '未知';
        }

        // 更新平均负载
        const loadElement = document.getElementById('cluster-load');
        if (loadElement) {
            const load = stats.averageLoad;
            if (load !== undefined && load !== null) {
                loadElement.textContent = load.toFixed(1) + '%';
            } else {
                loadElement.textContent = '未知';
            }
        }
    },



    // 显示示例数据（当API不可用时）
    showSampleData() {
        // 清理模拟数据，显示空状态
        this.pagination.total = 0;
        this.renderBrokerTable([]);
        this.updatePaginationInfo();
        this.renderPaginationButtons();

        // 更新指标为空状态
        const stats = {
            totalCount: 0,
            onlineCount: 0,
            offlineCount: 0,
            avgCpuUsage: 0,
            avgMemoryUsage: 0
        };
        this.updateMetrics(stats);
    },

    // 渲染Broker表格
    renderBrokerTable(brokers) {
        const tbody = document.getElementById('brokers-tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        brokers.forEach(broker => {
            const row = this.createTableRow(broker);
            tbody.appendChild(row);
        });
    },

    // 创建表格行
    createTableRow(broker) {
        const tr = document.createElement('tr');

        const statusClasses = {
            online: 'online',
            warning: 'warning',
            offline: 'offline'
        };

        const statusTexts = {
            online: '在线',
            warning: '警告',
            offline: '离线'
        };

        const statusClass = statusClasses[broker.status] || 'offline';
        const statusText = statusTexts[broker.status] || '离线';


        tr.innerHTML = `
            <td class="font-mono" title="${broker.brokerId}">${broker.brokerId}</td>
            <td class="font-mono" title="${broker.hostIp}">${broker.hostIp}</td>
            <td title="端口: ${broker.port}">${broker.port}</td>
            <td>
                <span class="node-status ${statusClass}">
                    <i class="fa fa-circle"></i>
                    ${statusText}
                </span>
            </td>
            <td>
                <div class="flex items-center gap-2">
                    <div class="usage-bar">
                        <div class="usage-fill ${this.getUsageClass(broker.cpuUsage)}" style="width: ${broker.cpuUsage || 0}%"></div>
                    </div>
                    <span class="text-xs font-medium">${(broker.cpuUsage || 0).toFixed(2)}%</span>
                </div>
            </td>
            <td>
                <div class="flex items-center gap-2">
                    <div class="usage-bar">
                        <div class="usage-fill ${this.getUsageClass(broker.memoryUsage)}" style="width: ${broker.memoryUsage || 0}%"></div>
                    </div>
                    <span class="text-xs font-medium">${(broker.memoryUsage || 0).toFixed(2)}%</span>
                </div>
            </td>
            <td class="text-xs" title="${broker.startupTime}">${this.formatStartTime(broker.startupTime)}</td>
            <td class="text-xs font-mono" title="版本: ${broker.version}">${broker.version || 'N/A'}</td>
        `;

        return tr;
    },


    // 获取使用率颜色类
    getUsageClass(usage) {
        if (!usage || usage < 60) return 'low';
        if (usage < 80) return 'medium';
        return 'high';
    },

    // 格式化启动时间
    formatStartTime(startupTime) {
        if (!startupTime) return 'N/A';

        const date = new Date(startupTime);
        const now = new Date();
        const diffMs = now - date;
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

        if (diffDays > 0) {
            return `${diffDays}天${diffHours}小时`;
        } else {
            return `${diffHours}小时`;
        }
    },

    // 更新分页信息
    updatePaginationInfo() {
        const startRecord = (this.pagination.currentPage - 1) * this.pagination.pageSize + 1;
        const endRecord = Math.min(this.pagination.currentPage * this.pagination.pageSize, this.pagination.total);

        const pageStart = document.getElementById('page-start');
        const pageEnd = document.getElementById('page-end');
        const totalCount = document.getElementById('total-count');

        if (pageStart) pageStart.textContent = startRecord;
        if (pageEnd) pageEnd.textContent = endRecord;
        if (totalCount) totalCount.textContent = this.pagination.total;
    },

    // 渲染分页按钮
    renderPaginationButtons() {
        const totalPages = Math.ceil(this.pagination.total / this.pagination.pageSize);
        const pagesContainer = document.getElementById('pagination-pages');
        if (!pagesContainer) return;

        pagesContainer.innerHTML = '';

        // 计算显示的页码范围
        let startPage = Math.max(1, this.pagination.currentPage - 2);
        let endPage = Math.min(totalPages, this.pagination.currentPage + 2);

        // 确保至少显示5个页码
        if (endPage - startPage < 4) {
            if (startPage === 1) {
                endPage = Math.min(totalPages, startPage + 4);
            } else {
                startPage = Math.max(1, endPage - 4);
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.className = `page-btn ${i === this.pagination.currentPage ? 'active' : ''}`;
            pageBtn.textContent = i;
            pageBtn.onclick = () => this.goToPage(i);
            pagesContainer.appendChild(pageBtn);
        }

        // 更新控制按钮状态
        const firstPage = document.getElementById('first-page');
        const prevPage = document.getElementById('prev-page');
        const nextPage = document.getElementById('next-page');
        const lastPage = document.getElementById('last-page');

        if (firstPage) firstPage.disabled = this.pagination.currentPage === 1;
        if (prevPage) prevPage.disabled = this.pagination.currentPage === 1;
        if (nextPage) nextPage.disabled = this.pagination.currentPage === totalPages;
        if (lastPage) lastPage.disabled = this.pagination.currentPage === totalPages;
    },

    // 跳转到指定页
    goToPage(page) {
        const totalPages = Math.ceil(this.pagination.total / this.pagination.pageSize);
        if (page >= 1 && page <= totalPages) {
            this.pagination.currentPage = page;
            this.loadBrokerData();
        }
    },

    // 初始化表格
    initTable() {
        // 表格初始化逻辑已在loadBrokerData中处理
    },

    // 初始化图表
    initCharts() {
        // 确保Chart库已加载
        if (typeof Chart === 'undefined') {
            console.warn('Chart库未加载，跳过图表初始化');
            return;
        }

        this.initBrokerPerformanceCharts();
    },





    // 初始化事件监听器
    initEventListeners() {
        // 搜索输入事件
        const searchInput = document.getElementById('search-input');
        const clearBtn = document.getElementById('clear-search');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.searchKeyword = e.target.value;
                if (this.searchKeyword.trim()) {
                    clearBtn.style.display = 'block';
                } else {
                    clearBtn.style.display = 'none';
                }
                this.pagination.currentPage = 1;
                this.loadBrokerData();
            });
        }

        // 清除搜索按钮
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                if (searchInput) {
                    searchInput.value = '';
                    this.searchKeyword = '';
                    clearBtn.style.display = 'none';
                    this.pagination.currentPage = 1;
                    this.loadBrokerData();
                    searchInput.focus();
                }
            });
        }

        // 刷新按钮
        const refreshBtn = document.getElementById('refresh-brokers');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => {
                refreshBtn.innerHTML = '<i class="fa fa-spin fa-refresh"></i> 刷新中...';
                this.loadBrokerData().finally(() => {
                    refreshBtn.innerHTML = '<i class="fa fa-refresh"></i> 手动刷新';
                    this.showToast('数据已刷新', 'success');
                    this.startAutoRefresh();
                });
            });
        }

        // 分页按钮事件
        const firstPage = document.getElementById('first-page');
        const prevPage = document.getElementById('prev-page');
        const nextPage = document.getElementById('next-page');
        const lastPage = document.getElementById('last-page');

        if (firstPage) firstPage.addEventListener('click', () => this.goToPage(1));
        if (prevPage) prevPage.addEventListener('click', () => this.goToPage(this.pagination.currentPage - 1));
        if (nextPage) nextPage.addEventListener('click', () => this.goToPage(this.pagination.currentPage + 1));
        if (lastPage) lastPage.addEventListener('click', () => {
            const totalPages = Math.ceil(this.pagination.total / this.pagination.pageSize);
            this.goToPage(totalPages);
        });

        // 弹出框关闭事件
        this.setupModalEvents();
    },

    // 设置弹出框事件
    setupModalEvents() {
        // Broker详情弹出框
        const detailModal = document.getElementById('broker-detail-modal');
        const closeModalBtn = document.getElementById('close-modal');

        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => this.closeModal());
        }

        if (detailModal) {
            detailModal.addEventListener('click', (e) => {
                if (e.target === detailModal) {
                    this.closeModal();
                }
            });
        }

        // 确认对话框
        const confirmModal = document.getElementById('confirm-modal');
        if (confirmModal) {
            confirmModal.addEventListener('click', (e) => {
                if (e.target === confirmModal) {
                    this.closeConfirmModal();
                }
            });
        }

        // 节点管理对话框
        const nodeModal = document.getElementById('node-management-modal');
        const closeNodeModalBtn = document.getElementById('close-node-modal');

        if (closeNodeModalBtn) {
            closeNodeModalBtn.addEventListener('click', () => this.closeNodeModal());
        }

        if (nodeModal) {
            nodeModal.addEventListener('click', (e) => {
                if (e.target === nodeModal) {
                    this.closeNodeModal();
                }
            });
        }
    },

    // 自动刷新
    startAutoRefresh() {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
        }

        this.refreshCountdown = 60;
        const countdownElement = document.getElementById('refresh-countdown');
        if (countdownElement) {
            countdownElement.textContent = this.refreshCountdown;
        }

        this.autoRefreshInterval = setInterval(() => {
            this.refreshCountdown--;
            if (countdownElement) {
                countdownElement.textContent = this.refreshCountdown;
            }

            if (this.refreshCountdown <= 0) {
                this.loadBrokerData();
                this.refreshCountdown = 60;
            }
        }, 1000);
    },

    // 更新指标
    updateMetrics(stats) {
        if (!stats) return;

        // 更新在线节点数
        const onlineNodesElement = document.getElementById('online-nodes');
        const onlineNodesChangeElement = document.getElementById('online-nodes-change');
        if (onlineNodesElement && stats.onlineNodes !== undefined) {
            onlineNodesElement.textContent = stats.onlineNodes;
        }
        if (onlineNodesChangeElement && stats.totalNodes !== undefined) {
            const onlineRate = stats.totalNodes > 0 ? ((stats.onlineNodes / stats.totalNodes) * 100).toFixed(1) : 0;
            onlineNodesChangeElement.innerHTML = `
                <i class="fa fa-circle"></i>
                ${onlineRate}% 可用
            `;
            onlineNodesChangeElement.className = onlineRate >= 80 ? 'metric-change positive' : 'metric-change negative';
        }

        // 更新离线节点数
        const offlineNodesElement = document.getElementById('offline-nodes');
        const offlineNodesChangeElement = document.getElementById('offline-nodes-change');
        if (offlineNodesElement && stats.totalNodes !== undefined && stats.onlineNodes !== undefined) {
            const offlineNodes = stats.totalNodes - stats.onlineNodes;
            offlineNodesElement.textContent = offlineNodes;
        }
        if (offlineNodesChangeElement && stats.totalNodes !== undefined && stats.onlineNodes !== undefined) {
            const offlineNodes = stats.totalNodes - stats.onlineNodes;
            const offlineRate = stats.totalNodes > 0 ? ((offlineNodes / stats.totalNodes) * 100).toFixed(1) : 0;
            offlineNodesChangeElement.innerHTML = `
                <i class="fa fa-exclamation-triangle"></i>
                ${offlineRate}% 离线
            `;
            offlineNodesChangeElement.className = offlineNodes === 0 ? 'metric-change positive' : offlineNodes <= stats.totalNodes * 0.2 ? 'metric-change warning' : 'metric-change negative';
        }

        // 更新CPU使用率
        const avgCpuUsageElement = document.getElementById('avg-cpu-usage');
        const cpuUsageChangeElement = document.getElementById('cpu-usage-change');
        if (avgCpuUsageElement && stats.avgCpuUsage !== undefined) {
            avgCpuUsageElement.textContent = (stats.avgCpuUsage).toFixed(2) + '%';
        }
        if (cpuUsageChangeElement) {
            const cpuUsage = stats.avgCpuUsage || 0;
            cpuUsageChangeElement.innerHTML = `
                <i class="fa fa-microchip"></i>
                ${cpuUsage < 70 ? '正常' : cpuUsage < 85 ? '偏高' : '过高'}
            `;
            cpuUsageChangeElement.className = cpuUsage < 70 ? 'metric-change positive' : cpuUsage < 85 ? 'metric-change warning' : 'metric-change negative';
        }

        // 更新内存使用率
        const avgMemoryUsageElement = document.getElementById('avg-memory-usage');
        const memoryUsageChangeElement = document.getElementById('memory-usage-change');
        if (avgMemoryUsageElement && stats.avgMemoryUsage !== undefined) {
            avgMemoryUsageElement.textContent = (stats.avgMemoryUsage).toFixed(2) + '%';
        }
        if (memoryUsageChangeElement) {
            const memoryUsage = stats.avgMemoryUsage || 0;
            memoryUsageChangeElement.innerHTML = `
                <i class="fa fa-hdd-o"></i>
                ${memoryUsage < 70 ? '正常' : memoryUsage < 85 ? '偏高' : '过高'}
            `;
            memoryUsageChangeElement.className = memoryUsage < 70 ? 'metric-change positive' : memoryUsage < 85 ? 'metric-change warning' : 'metric-change negative';
        }
    },

    // Broker操作函数
    async viewBrokerDetails(brokerId) {
        try {
            const response = await fetch(`/api/brokers/${brokerId}`);
            if (!response.ok) {
                throw new Error('获取Broker详情失败');
            }

            const broker = await response.json();
            this.fillBrokerDetailModal(broker);
            this.showModal('broker-detail-modal');

        } catch (error) {
            console.error('获取Broker详情失败:', error);
            this.showToast('获取详情失败: ' + error.message, 'error');
        }
    },

    // 编辑Broker节点
    async editBrokerNode(brokerId) {
        // 权限检查
        if (!this.isAdmin) {
            this.showToast('您没有编辑节点的权限', 'error');
            return;
        }

        try {
            const response = await fetch(`/api/brokers/${brokerId}`);
            if (!response.ok) {
                throw new Error('获取Broker信息失败');
            }

            const broker = await response.json();
            this.fillNodeForm(broker);
            this.openNodeModal('edit', brokerId);

        } catch (error) {
            console.error('获取Broker信息失败:', error);
            this.showToast('获取信息失败: ' + error.message, 'error');
        }
    },

    // 填充Broker详情弹出框
    fillBrokerDetailModal(broker) {
        document.getElementById('detail-id').textContent = `#${broker.id}`;
        document.getElementById('detail-host-id').textContent = broker.brokerId;
        document.getElementById('detail-host').textContent = broker.hostIp;
        document.getElementById('detail-port').textContent = broker.port;
        document.getElementById('detail-jmx-port').textContent = broker.jmxPort || 'N/A';
        document.getElementById('detail-version').textContent = broker.version || 'N/A';
        document.getElementById('detail-start-time').textContent = broker.startupTime || 'N/A';

        // 状态
        const statusClass = broker.status === 'online' ? 'online' :
            broker.status === 'warning' ? 'warning' : 'offline';
        const statusText = broker.status === 'online' ? '在线' :
            broker.status === 'warning' ? '警告' : '离线';
        document.getElementById('detail-status').innerHTML =
            `<span class="node-status ${statusClass}"><i class="fa fa-circle"></i> ${statusText}</span>`;

        // 资源使用率
        // CPU使用率 - 安全设置
        const detailCpu = document.getElementById('detail-cpu');
        const detailCpuFill = document.getElementById('detail-cpu-fill');
        if (detailCpu) {
            detailCpu.textContent = `${(broker.cpuUsage || 0).toFixed(2)}%`;
        }
        if (detailCpuFill) {
            detailCpuFill.style.width = `${broker.cpuUsage || 0}%`;
            detailCpuFill.className = `progress-fill ${this.getUsageClass(broker.cpuUsage)}`;
        }

        // 内存使用率 - 安全设置
        const detailMemory = document.getElementById('detail-memory');
        const detailMemoryFill = document.getElementById('detail-memory-fill');
        if (detailMemory) {
            detailMemory.textContent = `${(broker.memoryUsage || 0).toFixed(2)}%`;
        }
        if (detailMemoryFill) {
            detailMemoryFill.style.width = `${broker.memoryUsage || 0}%`;
            detailMemoryFill.className = `progress-fill ${this.getUsageClass(broker.memoryUsage)}`;
        }

        // 磁盘使用率（模拟数据） - 安全设置
        const diskUsage = Math.floor(Math.random() * 30) + 50; // 50-80%
        const detailDisk = document.getElementById('detail-disk');
        const detailDiskFill = document.getElementById('detail-disk-fill');
        if (detailDisk) {
            detailDisk.textContent = `${diskUsage.toFixed(2)}%`;
        }
        if (detailDiskFill) {
            detailDiskFill.style.width = `${diskUsage}%`;
            detailDiskFill.className = `progress-fill ${this.getUsageClass(diskUsage)}`;
        }

        // 网络统计（模拟数据） - 安全设置
        const detailNetworkIn = document.getElementById('detail-network-in');
        const detailNetworkOut = document.getElementById('detail-network-out');
        if (detailNetworkIn) {
            detailNetworkIn.textContent = `${(Math.random() * 200 + 100).toFixed(2)} MB/s`;
        }
        if (detailNetworkOut) {
            detailNetworkOut.textContent = `${(Math.random() * 200 + 100).toFixed(2)} MB/s`;
        }

        // 配置信息 - 安全设置
        const detailKafkaHome = document.getElementById('detail-kafka-home');
        const detailStartupScript = document.getElementById('detail-startup-script');
        if (detailKafkaHome) {
            detailKafkaHome.textContent = 'N/A';
        }
        if (detailStartupScript) {
            detailStartupScript.textContent = 'N/A';
        }
        // 其他配置信息 - 安全设置
        const detailConfigFile = document.getElementById('detail-config-file');
        const detailSshUser = document.getElementById('detail-ssh-user');
        const detailSshPort = document.getElementById('detail-ssh-port');
        const detailRemark = document.getElementById('detail-remark');

        if (detailConfigFile) {
            detailConfigFile.textContent = 'N/A';
        }
        if (detailSshUser) {
            detailSshUser.textContent = 'N/A';
        }
        if (detailSshPort) {
            detailSshPort.textContent = 'N/A';
        }
        if (detailRemark) {
            detailRemark.textContent = '暂无备注';
        }

        // 设置当前查看的broker ID
        window.currentBrokerId = broker.id;

        // 根据权限控制重启按钮显示
        const restartBtn = document.getElementById('restart-from-modal-btn');
        if (restartBtn) {
            restartBtn.style.display = this.isAdmin ? 'inline-flex' : 'none';
        }
    },


    // 填充节点表单
    fillNodeForm(broker) {
        if (!broker) return;

        document.getElementById('node-id').value = broker.brokerId;
        document.getElementById('node-ip').value = broker.hostIp;
        document.getElementById('kafka-port').value = broker.port;
        document.getElementById('jmx-port').value = broker.jmxPort || '9999';
        document.getElementById('kafka-home').value = '/opt/kafka';
        document.getElementById('start-script').value = 'bin/kafka-server-start.sh';
        document.getElementById('config-file').value = 'config/server.properties';
        document.getElementById('ssh-user').value = '';
        document.getElementById('ssh-port').value = '22';
        document.getElementById('ssh-password').value = '';
        document.getElementById('ssh-key-path').value = '';
        document.getElementById('node-remarks').value = '';

        // 根据Kafka配置是否有值来决定是否展开配置区域
        this.adjustKafkaConfigSection(broker);
        // 根据SSH配置是否有值来决定是否展开配置区域
        this.adjustSshConfigSection(broker);
    },

    // 根据broker数据调整Kafka配置区域状态
    adjustKafkaConfigSection(broker) {
        const content = document.getElementById('kafka-config-content');
        const toggleBtn = document.querySelector('#kafka-config-section .toggle-section-btn');
        const toggleIcon = document.getElementById('kafka-toggle-icon');

        // 检查是否有Kafka配置数据 (当前数据库中无此字段)
        const hasKafkaConfig = false;

        if (hasKafkaConfig) {
            // 有配置数据时展开
            content.classList.remove('collapsed');
            content.classList.add('expanded');
            toggleBtn.classList.remove('collapsed');
            toggleIcon.style.transform = 'rotate(0deg)';
        } else {
            // 没有配置数据时折叠
            content.classList.remove('expanded');
            content.classList.add('collapsed');
            toggleBtn.classList.add('collapsed');
            toggleIcon.style.transform = 'rotate(-90deg)';
        }
    },

    // 根据broker数据调整SSH配置区域状态
    adjustSshConfigSection(broker) {
        const content = document.getElementById('ssh-config-content');
        const toggleBtn = document.querySelector('#ssh-config-section .toggle-section-btn');
        const toggleIcon = document.getElementById('ssh-toggle-icon');

        // 检查是否有SSH配置数据 (当前数据库中无此字段)
        const hasSshConfig = false;

        if (hasSshConfig) {
            // 有配置数据时展开
            content.classList.remove('collapsed');
            content.classList.add('expanded');
            toggleBtn.classList.remove('collapsed');
            toggleIcon.style.transform = 'rotate(0deg)';
        } else {
            // 没有配置数据时折叠
            content.classList.remove('expanded');
            content.classList.add('collapsed');
            toggleBtn.classList.add('collapsed');
            toggleIcon.style.transform = 'rotate(-90deg)';
        }
    },


    // 显示确认对话框
    showConfirmModal(title, message, onConfirm) {
        document.getElementById('confirm-title').textContent = title;
        document.getElementById('confirm-message').textContent = message;

        const confirmBtn = document.getElementById('confirm-ok-btn');
        confirmBtn.onclick = () => {
            this.closeConfirmModal();
            onConfirm();
        };

        this.showModal('confirm-modal');
    },




    // 显示弹出框
    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'flex';
        }
    },

    // 关闭弹出框
    closeModal() {
        const modal = document.getElementById('broker-detail-modal');
        if (modal) {
            modal.style.display = 'none';
        }
    },

    // 关闭确认对话框
    closeConfirmModal() {
        const modal = document.getElementById('confirm-modal');
        if (modal) {
            modal.style.display = 'none';
        }
    },

    // 打开节点管理对话框
    openNodeModal(mode, nodeId = null) {
        // 权限检查
        if (!this.isAdmin) {
            this.showToast('您没有管理节点的权限', 'error');
            return;
        }

        window.currentEditNodeId = nodeId;
        const modal = document.getElementById('node-management-modal');
        const modalTitle = document.getElementById('node-modal-title');
        const saveBtn = document.getElementById('save-node-btn');

        if (mode === 'add') {
            modalTitle.innerHTML = '<i class="fa fa-plus mr-2"></i>添加新节点';
            saveBtn.innerHTML = '<i class="fa fa-save mr-1"></i>保存节点';
            this.clearNodeForm();
        } else {
            modalTitle.innerHTML = '<i class="fa fa-edit mr-2"></i>编辑节点';
            saveBtn.innerHTML = '<i class="fa fa-save mr-1"></i>更新节点';
        }

        this.showModal('node-management-modal');
    },

    // 关闭节点管理对话框
    closeNodeModal() {
        const modal = document.getElementById('node-management-modal');
        if (modal) {
            modal.style.display = 'none';
        }
        this.clearNodeForm();
        window.currentEditNodeId = null;
    },

    // 清空节点表单
    clearNodeForm() {
        const form = document.getElementById('node-form');
        if (form) {
            form.reset();
        }

        // 设置默认值
        const kafkaPort = document.getElementById('kafka-port');
        const jmxPort = document.getElementById('jmx-port');
        const startScript = document.getElementById('start-script');
        const configFile = document.getElementById('config-file');
        const sshPort = document.getElementById('ssh-port');

        if (kafkaPort) kafkaPort.value = '9092';
        if (jmxPort) jmxPort.value = '9999';
        if (startScript) startScript.value = 'bin/kafka-server-start.sh';
        if (configFile) configFile.value = 'config/server.properties';
        if (sshPort) sshPort.value = '22';

        // 重置Kafka配置区域状态
        this.resetKafkaConfigSection();
        // 重置SSH配置区域状态
        this.resetSshConfigSection();
    },

    // 重置Kafka配置区域状态
    resetKafkaConfigSection() {
        const content = document.getElementById('kafka-config-content');
        const toggleBtn = document.querySelector('#kafka-config-section .toggle-section-btn');
        const toggleIcon = document.getElementById('kafka-toggle-icon');

        if (content) {
            content.classList.remove('collapsed', 'expanded');
            content.classList.add('collapsed'); // 默认折叠
        }

        if (toggleBtn) {
            toggleBtn.classList.add('collapsed');
        }

        if (toggleIcon) {
            toggleIcon.style.transform = 'rotate(-90deg)';
        }
    },

    // 重置SSH配置区域状态
    resetSshConfigSection() {
        const content = document.getElementById('ssh-config-content');
        const toggleBtn = document.querySelector('#ssh-config-section .toggle-section-btn');
        const toggleIcon = document.getElementById('ssh-toggle-icon');

        if (content) {
            content.classList.remove('collapsed', 'expanded');
            content.classList.add('collapsed'); // 默认折叠
        }

        if (toggleBtn) {
            toggleBtn.classList.add('collapsed');
        }

        if (toggleIcon) {
            toggleIcon.style.transform = 'rotate(-90deg)';
        }
    },

    // 手动刷新标志
    isManualRefresh: false,

    // 显示图表加载状态
    showChartLoadingState(metricType, isLoading) {
        const chartElement = metricType === 'cpu'
            ? document.getElementById('brokerCpuChart')
            : document.getElementById('brokerMemoryChart');

        const refreshBtn = metricType === 'cpu'
            ? document.getElementById('brokerCpuRefresh')
            : document.getElementById('brokerMemoryRefresh');

        if (isLoading) {
            if (refreshBtn) {
                refreshBtn.innerHTML = '<i class="fa fa-spin fa-refresh"></i>';
                refreshBtn.disabled = true;
            }
        } else {
            if (refreshBtn) {
                refreshBtn.innerHTML = '<i class="fa fa-refresh"></i>';
                refreshBtn.disabled = false;
            }
        }
    },
    showToast(message, type = 'info') {
        // 移除已存在的toast
        const existingToast = document.querySelector('.cluster-toast');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        const icons = {
            'success': 'fa-check-circle',
            'error': 'fa-exclamation-circle',
            'warning': 'fa-exclamation-triangle',
            'info': 'fa-info-circle'
        };

        toast.className = `cluster-toast fixed top-4 right-4 z-[9999] px-6 py-3 rounded-lg text-white transform transition-all duration-300 shadow-lg flex items-center gap-2 ${type === 'success' ? 'bg-green-500' :
            type === 'error' ? 'bg-red-500' :
                type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500'
            }`;

        toast.innerHTML = `
            <i class="fa ${icons[type] || 'fa-info-circle'}"></i>
            <span>${message}</span>
        `;

        // 初始状态：从右侧滑入
        toast.style.transform = 'translateX(100%)';
        toast.style.opacity = '0';

        document.body.appendChild(toast);

        // 显示动画
        setTimeout(() => {
            toast.style.transform = 'translateX(0)';
            toast.style.opacity = '1';
        }, 10);

        // 自动隐藏
        setTimeout(() => {
            toast.style.transform = 'translateX(100%)';
            toast.style.opacity = '0';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, 3000);
    },

    // 初始化配置区域状态
    initConfigSections() {
        // 确保Kafka和SSH配置区域默认折叠
        this.resetKafkaConfigSection();
        this.resetSshConfigSection();
    },

    // 初始化自定义Tooltip
    initCustomTooltips() {
        // 为所有带有custom-tooltip类的元素添加延迟显示
        document.addEventListener('mouseover', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                tooltip.tooltipTimer = setTimeout(() => {
                    tooltip.classList.add('tooltip-active');
                }, 300); // 300ms延迟
            }
        });

        document.addEventListener('mouseout', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                tooltip.classList.remove('tooltip-active');
            }
        });

        // 键盘导航支持
        document.addEventListener('focusin', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                tooltip.tooltipTimer = setTimeout(() => {
                    tooltip.classList.add('tooltip-active');
                }, 100); // 键盘导航时延迟更短
            }
        });

        document.addEventListener('focusout', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                tooltip.classList.remove('tooltip-active');
            }
        });

        // 触摸设备支持
        document.addEventListener('touchstart', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                tooltip.tooltipTimer = setTimeout(() => {
                    tooltip.classList.add('tooltip-active');
                }, 500); // 触摸设备延迟更长
            }
        });

        document.addEventListener('touchend', (e) => {
            const tooltip = e.target.closest('.custom-tooltip');
            if (tooltip) {
                clearTimeout(tooltip.tooltipTimer);
                setTimeout(() => {
                    tooltip.classList.remove('tooltip-active');
                }, 1000); // 触摸设备显示时间更长
            }
        });
    },

    // Broker性能趋势图相关功能
    brokerCpuChart: null,
    brokerMemoryChart: null,

    // 初始化Broker性能趋势图
    initBrokerPerformanceCharts() {
        this.initBrokerCpuChart();
        this.initBrokerMemoryChart();
        this.setupBrokerChartEvents();

        // 初始化时加载默认时间范围的数据
        this.loadBrokerCpuData(this.defaultTimeRange);
        this.loadBrokerMemoryData(this.defaultTimeRange);
    },

    // 初始化Broker CPU使用率趋势图
    initBrokerCpuChart() {
        const ctx = document.getElementById('brokerCpuChart');
        if (!ctx) return;

        // 销毁已存在的图表实例
        if (this.brokerCpuChart) {
            this.brokerCpuChart.destroy();
            this.brokerCpuChart = null;
        }

        this.brokerCpuChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: []
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Broker CPU使用率趋势'
                    },
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: {
                            callback: function (value) {
                                return value + '%';
                            }
                        }
                    },
                    x: {
                        display: true,
                        ticks: {
                            maxTicksLimit: 8, // 限制最大刻度数量
                            autoSkip: true,
                            autoSkipPadding: 10,
                            callback: function(value, index, values) {
                                // 根据时间范围智能格式化显示
                                const label = this.getLabelForValue(value);
                                if (typeof label === 'string') {
                                    // 尝试解析时间字符串
                                    if (label.includes('-') || label.includes(':')) {
                                        const date = new Date(label);
                                        if (!isNaN(date.getTime())) {
                                            // 通过Select2获取当前选择的时间范围
                                            const timeRangeElement = document.getElementById('brokerCpuTimeRange');
                                            const currentRange = timeRangeElement ? timeRangeElement.value : '1h';

                                            // 天级维度：显示日期 (YYYY-MM-DD)
                                            if (currentRange === '7d' || currentRange === '30d') {
                                                return date.toLocaleDateString('zh-CN', {
                                                    year: 'numeric',
                                                    month: '2-digit',
                                                    day: '2-digit'
                                                }).replace(/\//g, '-');
                                            }
                                            // 小时级维度：显示小时 (HH)
                                            else if (currentRange === '1h' || currentRange === '6h' || currentRange === '24h') {
                                                return date.getHours().toString().padStart(2, '0');
                                            }
                                            // 分钟级维度：显示时分 (HH:MM)
                                            else if (currentRange === '5m' || currentRange === '15m' || currentRange === '30m') {
                                                return date.toLocaleTimeString('zh-CN', {
                                                    hour: '2-digit',
                                                    minute: '2-digit'
                                                });
                                            }
                                            // 默认显示小时
                                            else {
                                                return date.getHours().toString().padStart(2, '0');
                                            }
                                        }
                                    }
                                }
                                return label;
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
    },

    // 初始化Broker 内存使用率趋势图
    initBrokerMemoryChart() {
        const ctx = document.getElementById('brokerMemoryChart');
        if (!ctx) return;

        // 销毁已存在的图表实例
        if (this.brokerMemoryChart) {
            this.brokerMemoryChart.destroy();
            this.brokerMemoryChart = null;
        }

        this.brokerMemoryChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: []
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Broker 内存使用率趋势'
                    },
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        ticks: {
                            callback: function (value) {
                                return value + '%';
                            }
                        }
                    },
                    x: {
                        display: true,
                        ticks: {
                            maxTicksLimit: 8, // 限制最大刻度数量
                            autoSkip: true,
                            autoSkipPadding: 10,
                            callback: function(value, index, values) {
                                // 根据时间范围智能格式化显示
                                const label = this.getLabelForValue(value);
                                if (typeof label === 'string') {
                                    // 尝试解析时间字符串
                                    if (label.includes('-') || label.includes(':')) {
                                        const date = new Date(label);
                                        if (!isNaN(date.getTime())) {
                                            // 通过Select2获取当前选择的时间范围
                                            const timeRangeElement = document.getElementById('brokerMemoryTimeRange');
                                            const currentRange = timeRangeElement ? timeRangeElement.value : '1h';

                                            // 天级维度：显示日期 (YYYY-MM-DD)
                                            if (currentRange === '7d' || currentRange === '30d') {
                                                return date.toLocaleDateString('zh-CN', {
                                                    year: 'numeric',
                                                    month: '2-digit',
                                                    day: '2-digit'
                                                }).replace(/\//g, '-');
                                            }
                                            // 小时级维度：显示小时 (HH)
                                            else if (currentRange === '1h' || currentRange === '6h' || currentRange === '24h') {
                                                return date.getHours().toString().padStart(2, '0');
                                            }
                                            // 分钟级维度：显示时分 (HH:MM)
                                            else if (currentRange === '5m' || currentRange === '15m' || currentRange === '30m') {
                                                return date.toLocaleTimeString('zh-CN', {
                                                    hour: '2-digit',
                                                    minute: '2-digit'
                                                });
                                            }
                                            // 默认显示小时
                                            else {
                                                return date.getHours().toString().padStart(2, '0');
                                            }
                                        }
                                    }
                                }
                                return label;
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
    },

    // 设置Broker图表事件监听
    setupBrokerChartEvents() {
        // CPU图表时间范围选择
        const cpuTimeRange = document.getElementById('brokerCpuTimeRange');
        if (cpuTimeRange) {
            cpuTimeRange.addEventListener('change', () => {
                this.loadBrokerCpuData(cpuTimeRange.value);
            });
        }

        // CPU图表刷新按钮
        const cpuRefreshBtn = document.getElementById('brokerCpuRefresh');
        if (cpuRefreshBtn) {
            cpuRefreshBtn.addEventListener('click', () => {
                const timeRange = cpuTimeRange ? cpuTimeRange.value : '1h';
                this.isManualRefresh = true;
                this.loadBrokerCpuData(timeRange);
            });
        }

        // 内存图表时间范围选择
        const memoryTimeRange = document.getElementById('brokerMemoryTimeRange');
        if (memoryTimeRange) {
            memoryTimeRange.addEventListener('change', () => {
                this.loadBrokerMemoryData(memoryTimeRange.value);
            });
        }

        // 内存图表刷新按钮
        const memoryRefreshBtn = document.getElementById('brokerMemoryRefresh');
        if (memoryRefreshBtn) {
            memoryRefreshBtn.addEventListener('click', () => {
                const timeRange = memoryTimeRange ? memoryTimeRange.value : '1h';
                this.isManualRefresh = true;
                this.loadBrokerMemoryData(timeRange);
            });
        }
    },

    // 加载Broker趋势数据（统一方法）
    async loadBrokerTrendData(metricType, timeRange = '1h') {
        try {
            // 显示加载指示器
            this.showChartLoadingState(metricType, true);

            let url = `/api/brokers/metrics/trend?metricType=${metricType}&timeRange=${timeRange}`;

            // 如果有集群ID，添加到URL参数中
            if (this.clusterId) {
                url += `&clusterId=${this.clusterId}`;
            }

            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`Failed to fetch ${metricType} data: ${response.status} ${response.statusText}`);
            }
            const data = await response.json();

            // 检查数据结构
            if (data && data.success && data.data) {
                // Data structure validation completed
            }

            // 根据指标类型更新相应的图表
            if (metricType === 'cpu') {
                this.updateBrokerCpuChart(data);
            } else if (metricType === 'memory') {
                this.updateBrokerMemoryChart(data);
            }

            // 显示成功消息（仅在手动刷新时）
            if (this.isManualRefresh) {
                this.showToast(`${metricType.toUpperCase()}数据加载成功`, 'success');
                this.isManualRefresh = false;
            }

        } catch (error) {
            console.error(`Error loading broker ${metricType} data:`, error);
            this.showToast(`加载${metricType.toUpperCase()}数据失败: ${error.message}`, 'error');
        } finally {
            // 隐藏加载指示器
            this.showChartLoadingState(metricType, false);
        }
    },

    // 加载Broker CPU数据
    async loadBrokerCpuData(timeRange = '1h') {
        return this.loadBrokerTrendData('cpu', timeRange);
    },

    // 加载Broker 内存数据
    async loadBrokerMemoryData(timeRange = '1h') {
        return this.loadBrokerTrendData('memory', timeRange);
    },


    // 初始化时间范围选择器事件
    initTimeRangeSelectors() {
        // CPU时间范围选择器
        const cpuSelector = document.getElementById('cpuTimeRangeSelector');
        if (cpuSelector) {
            cpuSelector.addEventListener('change', (e) => {
                const newTimeRange = e.target.value;
                this.loadBrokerCpuData(newTimeRange);
            });
        }

        // 内存时间范围选择器
        const memorySelector = document.getElementById('memoryTimeRangeSelector');
        if (memorySelector) {
            memorySelector.addEventListener('change', (e) => {
                const newTimeRange = e.target.value;
                this.loadBrokerMemoryData(newTimeRange);
            });
        }
    },


    // 更新Broker CPU图表
    updateBrokerCpuChart(response) {
        if (!this.brokerCpuChart || !response) return;

        // 从响应中提取实际的图表数据
        const chartData = response.data || response;
        const labels = chartData.labels || [];
        const datasets = chartData.datasets || [];

        // 使用蓝色主题的渐变色系
        const blueColors = [
            '#1e40af', '#2563eb', '#3b82f6', '#60a5fa',
            '#93c5fd', '#1e3a8a', '#1d4ed8', '#2563eb'
        ];

        datasets.forEach((dataset, index) => {
            const baseColor = blueColors[index % blueColors.length];
            dataset.borderColor = baseColor;
            dataset.backgroundColor = baseColor + '40'; // 更明显的透明度用于area填充
            dataset.fill = true; // 启用area填充
            dataset.tension = 0.4; // 更平滑的曲线
            dataset.pointRadius = 0; // 移除圆点
            dataset.pointHoverRadius = 4; // 鼠标悬停时显示小点
            dataset.borderWidth = 2;
        });

        this.brokerCpuChart.data.labels = labels;
        this.brokerCpuChart.data.datasets = datasets;
        this.brokerCpuChart.update();
    },

    // 更新Broker 内存图表
    updateBrokerMemoryChart(response) {
        if (!this.brokerMemoryChart || !response) return;

        // 从响应中提取实际的图表数据
        const chartData = response.data || response;
        const labels = chartData.labels || [];
        const datasets = chartData.datasets || [];

        // 使用蓝色主题的渐变色系
        const blueColors = [
            '#1e40af', '#2563eb', '#3b82f6', '#60a5fa',
            '#93c5fd', '#1e3a8a', '#1d4ed8', '#2563eb'
        ];

        datasets.forEach((dataset, index) => {
            const baseColor = blueColors[index % blueColors.length];
            dataset.borderColor = baseColor;
            dataset.backgroundColor = baseColor + '40'; // 更明显的透明度用于area填充
            dataset.fill = true; // 启用area填充
            dataset.tension = 0.4; // 更平滑的曲线
            dataset.pointRadius = 0; // 移除圆点
            dataset.pointHoverRadius = 4; // 鼠标悬停时显示小点
            dataset.borderWidth = 2;
        });

        this.brokerMemoryChart.data.labels = labels;
        this.brokerMemoryChart.data.datasets = datasets;
        this.brokerMemoryChart.update();
    }
};

// 页面加载完成后初始化Cluster模块
document.addEventListener('DOMContentLoaded', function () {
    // 等待公共模块加载完成后再初始化
    setTimeout(async () => {
        await ClusterModule.init();
    }, 100);
});