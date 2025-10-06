// Topics页面专用功能
const TopicsModule = {
    // 数据存储
    allTopics: [],
    currentPage: 1,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0,
    searchKeyword: '',

    // 排序相关
    sortField: 'createTime',
    sortOrder: 'DESC',
    loading: false,

    // 初始化
    init() {
        // 设置页面标题
        CommonModule.setPageTitle('主题管理');

        this.initTableFeatures();
        this.initModalDialogs();
        this.initSearchAndFilter();
        this.initChartFeatures();
        this.loadTopicData();
        this.loadTopicStatistics();
    },

    // 初始化搜索和筛选功能
    initSearchAndFilter() {
        // 表格搜索框事件，使用防抖动
        const searchInput = document.querySelector('#topic-search');
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => {
                    this.searchKeyword = e.target.value.trim();
                    this.currentPage = 1;
                    this.loadTopicData();
                }, 300); // 300ms防抖动
            });
        }

        // 初始化主题筛选输入框
        this.initTopicFilterInput();

        // 初始化自动刷新
        this.initAutoRefresh();
    },

    // 初始化主题筛选输入框
    initTopicFilterInput() {
        const topicSearchInput = document.getElementById('topic-search-input');
        const topicSuggestions = document.getElementById('topic-suggestions');
        const clearBtn = document.getElementById('clear-topic-search');
        const loadingElement = document.getElementById('topic-search-loading');

        if (!topicSearchInput || !topicSuggestions) return;

        let debounceTimer;
        let selectedTopicName = '';
        let highlightedIndex = -1;
        let suggestions = [];
        let isSearching = false;

        // 输入事件处理
        topicSearchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();

            // 如果有选中的主题，先清除选择状态
            if (selectedTopicName && query !== selectedTopicName) {
                this.clearSelection();
            }

            if (query === '') {
                this.hideSuggestions();
                this.hideLoading();
                return;
            }

            // 如果输入的就是已选择的主题名称，不需要搜索
            if (query === selectedTopicName) {
                return;
            }

            isSearching = true;
            // 显示加载状态
            this.showLoading();

            // 防抖处理
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                this.searchTopicSuggestions(query);
            }, 300);
        });

        // 焦点事件
        topicSearchInput.addEventListener('focus', () => {
            // 如果输入框是只读状态（已选择主题），点击时使其可编辑
            if (topicSearchInput.hasAttribute('readonly')) {
                topicSearchInput.removeAttribute('readonly');
                topicSearchInput.select(); // 选中所有文本，方便用户重新搜索
            }

            const query = topicSearchInput.value.trim();
            if (query && suggestions.length > 0 && !selectedTopicName) {
                this.showSuggestions(suggestions);
            }
        });

        // 键盘导航
        topicSearchInput.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                highlightedIndex = Math.min(highlightedIndex + 1, suggestions.length - 1);
                this.updateHighlight();
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                highlightedIndex = Math.max(highlightedIndex - 1, -1);
                this.updateHighlight();
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (highlightedIndex >= 0 && suggestions[highlightedIndex]) {
                    this.selectTopic(suggestions[highlightedIndex]);
                }
            } else if (e.key === 'Escape') {
                this.hideSuggestions();
                topicSearchInput.blur();
            } else if (e.key === 'Backspace' && selectedTopicName) {
                // 如果有选中的主题，按退格键可以清除选择
                this.clearTopicFilter();
            }
        });

        // 清除按钮事件
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                this.clearTopicFilter();
                topicSearchInput.focus();
            });
        }

        // 点击外部关闭建议
        document.addEventListener('click', (e) => {
            if (!topicSearchInput.contains(e.target) && !topicSuggestions.contains(e.target)) {
                this.hideSuggestions();
            }
        });

        // 辅助方法
        this.showLoading = () => {
            if (loadingElement) {
                loadingElement.classList.remove('hidden');
            }
            this.hideSuggestions();
        };

        this.hideLoading = () => {
            if (loadingElement) {
                loadingElement.classList.add('hidden');
            }
        };

        this.showSuggestions = (topics) => {
            const suggestionsContent = topicSuggestions.querySelector('.topic-suggestions-content');
            if (!suggestionsContent) return;

            suggestions = topics.slice(0, 5); // 最多显示5个建议
            highlightedIndex = -1;

            if (suggestions.length === 0) {
                suggestionsContent.innerHTML = '<div class="no-results"><i class="fa fa-search"></i>未找到匹配的主题</div>';
            } else {
                suggestionsContent.innerHTML = suggestions.map((topic, index) => `
                    <div class="suggestion-item" data-topic="${topic}" data-index="${index}">
                        <div class="topic-icon">
                            <i class="fa fa-layer-group"></i>
                        </div>
                        <div class="topic-info">
                            <div class="topic-name">${topic}</div>
                            <div class="topic-desc">主题名称</div>
                        </div>
                    </div>
                `).join('');

                // 绑定点击事件
                suggestionsContent.querySelectorAll('.suggestion-item').forEach(item => {
                    item.addEventListener('click', () => {
                        this.selectTopic(item.dataset.topic);
                    });
                });
            }

            topicSuggestions.classList.remove('hidden');
            this.hideLoading();
        };

        this.hideSuggestions = () => {
            topicSuggestions.classList.add('hidden');
            highlightedIndex = -1;
        };

        this.updateHighlight = () => {
            const items = topicSuggestions.querySelectorAll('.suggestion-item');
            items.forEach((item, index) => {
                item.classList.toggle('highlighted', index === highlightedIndex);
            });
        };

        this.selectTopic = (topicName) => {
            selectedTopicName = topicName;
            isSearching = false;

            // 在输入框中显示选中的主题
            topicSearchInput.value = topicName;
            topicSearchInput.classList.add('has-selection');
            topicSearchInput.setAttribute('readonly', true);

            this.hideSuggestions();

            // 显示清除按钮
            if (clearBtn) {
                clearBtn.classList.remove('hidden');
            }

            this.loadChartData(); // 重新加载图表数据
        };

        this.clearSelection = () => {
            selectedTopicName = '';
            topicSearchInput.classList.remove('has-selection');
            topicSearchInput.removeAttribute('readonly');
            if (clearBtn) {
                clearBtn.classList.add('hidden');
            }
        };

        this.clearTopicFilter = () => {
            this.clearSelection();
            topicSearchInput.value = '';
            topicSearchInput.placeholder = '搜索并选择主题...';
            this.hideSuggestions();
            this.loadChartData(); // 重新加载图表数据
        };
    },

    async searchTopicSuggestions(query) {
        try {
            const response = await fetch(`/topic/api/names?search=${encodeURIComponent(query)}`);
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    // 过滤匹配的主题
                    const filteredTopics = result.data.filter(topic =>
                        topic.toLowerCase().includes(query.toLowerCase())
                    );
                    this.showSuggestions(filteredTopics);
                } else {
                    this.showSuggestions([]);
                }
            } else {
                this.showSuggestions([]);
            }
        } catch (error) {
            this.showSuggestions([]);
        }
    },

    // 加载Topic数据
    async loadTopicData() {
        if (this.loading) return;

        this.loading = true;
        this.showLoading();

        try {
            const params = new URLSearchParams({
                page: this.currentPage,
                pageSize: this.pageSize,
                sortField: this.sortField,
                sortOrder: this.sortOrder
            });

            if (this.searchKeyword) {
                params.append('search', this.searchKeyword);
            }

            const clusterId = this.getClusterId();
            if (clusterId) {
                params.append('clusterId', clusterId);
            }

            const response = await fetch(`/topic/api/list?${params}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result.success) {
                this.allTopics = result.data || [];
                this.totalItems = result.total || 0;
                this.totalPages = result.totalPages || 0;
                this.currentPage = result.page || 1;

                // 为每个topic处理图标信息
                this.allTopics.forEach(topic => {
                    // 优先使用后端返回的icon字段，如果为空则使用基于名称的fallback
                    topic.icon = topic.icon || this.getTopicIcon(topic.topicName);

                    // 将icon值转换为FontAwesome类名（如果需要）
                    topic.icon = this.convertIconToFontAwesome(topic.icon);

                    // 同样处理iconColor
                    topic.iconColor = topic.iconColor || this.getTopicIconColor(topic.topicName);
                });

                this.updateTable();
                this.updatePagination();
            } else {
                this.showError('加载数据失败: ' + (result.message || '服务器错误'));
            }
        } catch (error) {
            this.showError('加载数据异常: ' + error.message);
        } finally {
            this.loading = false;
            this.hideLoading();
        }
    },

    // 将图标值转换为FontAwesome类名
    convertIconToFontAwesome(iconValue) {
        if (!iconValue) return 'fa-database';
        
        // 如果已经是fa-开头的类名，直接返回
        if (iconValue.startsWith('fa-')) {
            return iconValue;
        }
        
        // 图标映射表：HTML select option值 -> FontAwesome类名
        const iconMap = {
            'list-ul': 'fa-list-ul',
            'shopping-cart': 'fa-shopping-cart', 
            'user': 'fa-user',
            'bell': 'fa-bell',
            'file-text': 'fa-file-text',
            'line-chart': 'fa-line-chart',
            'cogs': 'fa-cogs',
            'database': 'fa-database'
        };
        
        // 先返回映射结果，其次保留fa-前缀，其次自动补全fa-
        if (iconMap[iconValue]) return iconMap[iconValue];
        if (iconValue.startsWith('fa-')) return iconValue;
        return 'fa-' + iconValue;
    },

    // 格式化保留时间
    formatRetentionTime(retentionTimeMs) {
        if (!retentionTimeMs || retentionTimeMs <= 0) {
            return 'N/A';
        }
        
        const ms = parseInt(retentionTimeMs);
        const days = Math.floor(ms / (1000 * 60 * 60 * 24));
        const hours = Math.floor(ms / (1000 * 60 * 60));
        
        if (days > 0) {
            return `${days}天`;
        } else if (hours > 0) {
            return `${hours}小时`;
        } else {
            const minutes = Math.floor(ms / (1000 * 60));
            return minutes > 0 ? `${minutes}分钟` : '< 1分钟';
        }
    },

    // 获取Topic图标
    getTopicIcon(topicName) {
        const iconMap = {
            'order': 'fa-list-ul',
            'user': 'fa-user',
            'payment': 'fa-credit-card',
            'system': 'fa-tachometer',
            'error': 'fa-exclamation-triangle',
            'analytics': 'fa-bar-chart',
            'notification': 'fa-bell',
            'audit': 'fa-shield',
            'session': 'fa-clock-o',
            'recommendation': 'fa-star',
            'inventory': 'fa-cube',
            'customer': 'fa-comment',
            'marketing': 'fa-bullhorn',
            'search': 'fa-search',
            'api': 'fa-code',
            'database': 'fa-database'
        };
        
        for (const [key, icon] of Object.entries(iconMap)) {
            if (topicName.toLowerCase().includes(key)) {
                return icon;
            }
        }
        return 'fa-database'; // 默认图标
    },

    // 获取Topic图标颜色
    getTopicIconColor(topicName) {
        const colorMap = {
            'order': 'blue',
            'user': 'green',
            'payment': 'yellow',
            'system': 'purple',
            'error': 'red',
            'analytics': 'blue',
            'notification': 'green',
            'audit': 'purple',
            'session': 'yellow',
            'recommendation': 'blue',
            'inventory': 'green',
            'customer': 'purple',
            'marketing': 'red',
            'search': 'blue',
            'api': 'yellow'
        };
        
        for (const [key, color] of Object.entries(colorMap)) {
            if (topicName.toLowerCase().includes(key)) {
                return color;
            }
        }
        return 'gray'; // 默认颜色
    },

    // 显示加载状态
    showLoading() {
        const tbody = document.querySelector('tbody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-8"><i class="fa fa-spinner fa-spin mr-2"></i>加载中...</td></tr>';
        }
    },

    // 隐藏加载状态
    hideLoading() {
        // 加载完成后会调用updateTable更新表格内容
    },

    // 显示错误信息
    showError(message) {
        const tbody = document.querySelector('tbody');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-8 text-red-600"><i class="fa fa-exclamation-triangle mr-2"></i>${message}</td></tr>`;
        }
    },

    // 显示创建主题模态框
    showCreateTopicModal() {
        const modal = document.querySelector('#create-topic-modal');
        if (modal) {
            modal.classList.remove('hidden');
            // 重置表单
            const form = modal.querySelector('form');
            if (form) {
                form.reset();
            }
        }
    },

    // 隐藏创建主题模态框
    hideCreateTopicModal() {
        const modal = document.querySelector('#create-topic-modal');
        if (modal) {
            modal.classList.add('hidden');
        }
    },

    // 创建主题 - 统一的创建函数
    async createTopic(formData = null) {
        let form, data;

        if (formData) {
            // 如果传入了formData，直接使用
            data = formData;
            form = document.querySelector('#create-topic-form');
        } else {
            // 否则从表单获取数据
            form = document.querySelector('#create-topic-form');
            if (!form) return;
            data = new FormData(form);
        }

        // 读取用户选择的图标值
        const selectedIcon = data.get('topicIcon') || '';
        const normalizedIcon = this.convertIconToFontAwesome(selectedIcon) || this.getTopicIcon(data.get('topicName'));

        // 获取集群ID
        const clusterId = this.getClusterId();

        // 处理保留时间
        const retentionVal = parseInt(data.get('retentionTime'));
        const retentionUnit = data.get('retentionTimeUnit') || 'days';
        let retentionMs = '';
        if (!isNaN(retentionVal) && retentionVal > 0) {
            const unitToMs = { hours: 3600000, days: 86400000, weeks: 604800000, months: 2592000000 };
            const factor = unitToMs[retentionUnit] || unitToMs['days'];
            retentionMs = String(retentionVal * factor);
        }

        // 构建请求数据
        const topicData = {
            topicName: data.get('topicName'),
            partitions: parseInt(data.get('partitionCount')),
            replicas: parseInt(data.get('replicationFactor')),
            retentionTime: retentionMs,
            icon: normalizedIcon,
            clusterId: clusterId,
            createBy: (window.currentUser && window.currentUser.username) ? window.currentUser.username : 'admin'
        };

        // 统一的表单验证
        if (!topicData.topicName || !topicData.topicName.trim()) {
            this.showAlert('请输入主题名称', 'error');
            return false;
        }

        // 验证主题名称格式
        const topicNameRegex = /^[a-zA-Z0-9_-]+$/;
        if (!topicNameRegex.test(topicData.topicName)) {
            this.showAlert('主题名称只能包含字母、数字、下划线和中划线', 'warning');
            return false;
        }

        if (!Number.isInteger(topicData.partitions) || topicData.partitions <= 0) {
            this.showAlert('分区数必须为大于0的整数', 'error');
            return false;
        }

        if (!Number.isInteger(topicData.replicas) || topicData.replicas <= 0) {
            this.showAlert('副本数必须为大于0的整数', 'error');
            return false;
        }

        try {
            const response = await fetch('/topic/api/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(topicData)
            });

            const result = await response.json();

            if (result.success) {
                this.showAlert(result.message || '主题创建成功！', 'success');
                this.hideCreateTopicModal();
                this.loadTopicData();
                return true;
            } else {
                this.showAlert('创建失败: ' + (result.message || '未知错误'), 'error');
                return false;
            }
        } catch (error) {
            this.showAlert('创建异常: ' + error.message, 'error');
            return false;
        }
    },

    // 显示扩容对话框
    showScaleModal(topicName, currentPartitions) {
        const modal = document.querySelector('#scale-modal');
        if (modal) {
            // 存储主题名称和当前分区数
            modal.dataset.topicName = topicName;
            modal.dataset.currentPartitions = currentPartitions;
            
            // 设置显示的主题名称和当前分区数
            const scaleTopicNameElement = document.querySelector('#scale-topic-name');
            const currentPartitionsElement = document.querySelector('#current-partitions');
            
            if (scaleTopicNameElement) {
                scaleTopicNameElement.textContent = topicName;
            }
            
            if (currentPartitionsElement) {
                currentPartitionsElement.textContent = currentPartitions;
            }
            
            // 设置新分区数的最小值和默认值
            const newPartitionsInput = document.querySelector('#new-partitions');
            if (newPartitionsInput) {
                newPartitionsInput.value = parseInt(currentPartitions) + 1;
                newPartitionsInput.min = parseInt(currentPartitions) + 1;
            }
            
            modal.classList.remove('hidden');
        }
    },

    // 隐藏扩容对话框
    hideScaleModal() {
        const modal = document.querySelector('#scale-modal');
        if (modal) {
            modal.classList.add('hidden');
        }
    },

    // 显示编辑保留时间对话框
    showEditRetentionModal(topicName, currentRetention) {
        const modal = document.querySelector('#edit-retention-modal');
        if (modal) {
            // 存储主题名称（不显示）
            modal.dataset.topicName = topicName;
            // 设置当前保留时间，转换为小时
            const retentionInput = document.querySelector('#retention-time-input');
            if (retentionInput) {
                retentionInput.value = Math.floor(currentRetention / (1000 * 60 * 60)) || 168; // 转换为小时，默认7天=168小时
            }
            
            modal.classList.remove('hidden');
        }
    },

    // 隐藏编辑保留时间对话框
    hideEditRetentionModal() {
        const modal = document.querySelector('#edit-retention-modal');
        if (modal) {
            modal.classList.add('hidden');
        }
    },



    // 执行主题扩容
    async scaleTopic() {
        const modal = document.querySelector('#scale-modal');
        const topicName = modal.dataset.topicName;
        const currentPartitions = parseInt(modal.dataset.currentPartitions);
        const newPartitions = parseInt(document.querySelector('#new-partitions').value);

        if (!newPartitions || newPartitions <= currentPartitions) {
            this.showAlert('新分区数必须大于当前分区数', 'error');
            return;
        }

        try {
            // 构建请求参数，包含cluster ID
            const params = new URLSearchParams({
                newPartitions: newPartitions
            });

            const clusterId = this.getClusterId();
            if (clusterId) {
                params.append('clusterId', clusterId);
            }

            const response = await fetch(`/topic/api/scale/${topicName}?${params}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const result = await response.json();

            if (result.success) {
                this.showAlert('主题扩容成功', 'success');
                this.hideScaleModal();
                this.loadTopicData(); // 重新加载数据
            } else {
                this.showAlert('扩容失败: ' + result.message, 'error');
            }
        } catch (error) {
            this.showAlert('扩容异常: ' + error.message, 'error');
        }
    },

    // 执行保留时间更新
    async updateRetentionTime() {
        const modal = document.querySelector('#edit-retention-modal');
        const topicName = modal.dataset.topicName;
        const retentionHours = parseInt(document.querySelector('#retention-time-input').value);

        if (!retentionHours || retentionHours <= 0) {
            this.showAlert('请输入有效的保留时间（小时）', 'error');
            return;
        }

        // 转换为毫秒（小时 * 60分钟 * 60秒 * 1000毫秒）
        const retentionMs = retentionHours * 60 * 60 * 1000;

        // 获取集群ID
        const clusterId = this.getClusterId();

        try {
            const requestBody = {
                topicName: topicName,
                retentionMs: retentionMs
            };

            // 如果有集群ID，添加到请求体中
            if (clusterId) {
                requestBody.clusterId = clusterId;
            }

            const response = await fetch('/topic/api/retention', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const result = await response.json();

            if (result.success) {
                this.showAlert('保留时间更新成功', 'success');
                this.hideEditRetentionModal();
                this.loadTopicData(); // 重新加载数据
            } else {
                this.showAlert('更新失败: ' + result.message, 'error');
            }
        } catch (error) {
            this.showAlert('更新异常: ' + error.message, 'error');
        }
    },

    // 显示删除主题对话框
    showDeleteModal(topicName) {
        const modal = document.querySelector('#delete-modal');
        if (modal) {
            // 存储主题名称
            modal.dataset.topicName = topicName;
            
            // 设置显示的主题名称
            const deleteTopicNameElement = document.querySelector('#delete-topic-name');
            if (deleteTopicNameElement) {
                deleteTopicNameElement.textContent = topicName;
            }
            
            // 清空确认输入框
            const confirmTopicNameInput = document.querySelector('#confirm-topic-name');
            if (confirmTopicNameInput) {
                confirmTopicNameInput.value = '';
            }
            
            // 禁用确认按钮
            const confirmDeleteBtn = document.querySelector('#confirm-delete-btn');
            if (confirmDeleteBtn) {
                confirmDeleteBtn.disabled = true;
            }
            
            modal.classList.remove('hidden');
        }
    },

    // 隐藏删除主题对话框
    hideDeleteModal() {
        const modal = document.querySelector('#delete-modal');
        if (modal) {
            modal.classList.add('hidden');
        }
    },

    // 删除主题
    async deleteTopic(topicName) {
        try {
            // 构建请求参数，包含cluster ID
            const params = new URLSearchParams();

            const clusterId = this.getClusterId();
            if (clusterId) {
                params.append('clusterId', clusterId);
            }

            const url = `/topic/api/delete/${topicName}${params.toString() ? '?' + params.toString() : ''}`;
            const response = await fetch(url, {
                method: 'DELETE'
            });

            const result = await response.json();

            if (result.success) {
                this.showAlert('删除成功', 'success');
                this.hideDeleteModal();
                this.loadTopicData(); // 重新加载数据
            } else {
                this.showAlert('删除失败: ' + result.message, 'error');
            }
        } catch (error) {
            this.showAlert('删除异常: ' + error.message, 'error');
        }
    },

    // 显示提示信息
    showAlert(message, type = 'info') {
        // 创建提示框
        const alert = document.createElement('div');
        alert.className = `fixed top-4 right-4 z-50 px-4 py-3 rounded-md shadow-lg transition-all duration-300 transform translate-x-full`;
        
        const typeClasses = {
            'success': 'bg-green-100 border border-green-400 text-green-700',
            'error': 'bg-red-100 border border-red-400 text-red-700',
            'warning': 'bg-yellow-100 border border-yellow-400 text-yellow-700',
            'info': 'bg-blue-100 border border-blue-400 text-blue-700'
        };
        
        alert.className += ' ' + (typeClasses[type] || typeClasses['info']);
        
        const icons = {
            'success': 'fa-check-circle',
            'error': 'fa-exclamation-circle',
            'warning': 'fa-exclamation-triangle',
            'info': 'fa-info-circle'
        };
        
        alert.innerHTML = `
            <div class="flex items-center">
                <i class="fa ${icons[type] || icons['info']} mr-2"></i>
                <span>${message}</span>
                <button class="ml-4 text-gray-400 hover:text-gray-600" onclick="this.parentElement.parentElement.remove()">
                    <i class="fa fa-times"></i>
                </button>
            </div>
        `;
        
        document.body.appendChild(alert);
        
        // 显示动画
        setTimeout(() => {
            alert.classList.remove('translate-x-full');
        }, 100);
        
        // 自动隐藏
        setTimeout(() => {
            alert.classList.add('translate-x-full');
            setTimeout(() => {
                if (alert.parentElement) {
                    alert.remove();
                }
            }, 300);
        }, 3000);
    },

    // 初始化表格功能
    initTableFeatures() {
        // 分页按钮事件
        const prevPageBtn = document.getElementById('prev-page');
        const nextPageBtn = document.getElementById('next-page');

        if (prevPageBtn) {
            prevPageBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.loadTopicData();
                }
            });
        }

        if (nextPageBtn) {
            nextPageBtn.addEventListener('click', () => {
                if (this.currentPage < this.totalPages) {
                    this.currentPage++;
                    this.loadTopicData();
                }
            });
        }

        // 初始化表格
        this.updateTable();
        this.updatePagination();
    },

    // 更新表格
    updateTable() {
        const tbody = document.querySelector('tbody');
        if (!tbody) return;

        tbody.innerHTML = '';
        
        if (!this.allTopics || this.allTopics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-8 text-gray-500">暂无数据</td></tr>';
            return;
        }
        
        this.allTopics.forEach(topic => {
            const row = this.createTableRow(topic);
            tbody.appendChild(row);
        });

    },

    // 获取broker状态信息
    getBrokerStatus(value, type) {
        let status, text, cssClass, icon;
        
        if (type === 'spread') {
            // brokerSpread: <60(异常), 60-79(警告), >=80(正常)
            if (value < 60) {
                status = 'error';
                text = '异常';
                cssClass = 'text-red-600';
                icon = 'fa-times-circle';
            } else if (value >= 60 && value < 80) {
                status = 'warning';
                text = '警告';
                cssClass = 'text-yellow-600';
                icon = 'fa-exclamation-triangle';
            } else {
                status = 'normal';
                text = '正常';
                cssClass = 'text-green-600';
                icon = 'fa-check-circle';
            }
        } else if (type === 'skewed' || type === 'leaderSkewed') {
            // brokerSkewed/brokerLeaderSkewed: <=30(正常), 31-79(警告), >=80(异常)
            if (value <= 30) {
                status = 'normal';
                text = '正常';
                cssClass = 'text-green-600';
                icon = 'fa-check-circle';
            } else if (value > 30 && value < 80) {
                status = 'warning';
                text = '警告';
                cssClass = 'text-yellow-600';
                icon = 'fa-exclamation-triangle';
            } else {
                status = 'error';
                text = '异常';
                cssClass = 'text-red-600';
                icon = 'fa-times-circle';
            }
        }
        
        return { status, text, cssClass, icon };
    },

    // 创建表格行
    createTableRow(topic) {
        const tr = document.createElement('tr');
        tr.className = 'hover:bg-gray-50 transition-colors duration-200';
        
        // 获取broker状态信息
        const brokerSpreadStatus = this.getBrokerStatus(topic.brokerSpread || 0, 'spread');
        const brokerSkewedStatus = this.getBrokerStatus(topic.brokerSkewed || 0, 'skewed');
        const leaderSkewedStatus = this.getBrokerStatus(topic.brokerLeaderSkewed || 0, 'leaderSkewed');
        
        // 格式化保留时间
        const retentionTimeText = this.formatRetentionTime(topic.retentionTime);

        // 规范化icon与iconColor
        const iconClass = this.convertIconToFontAwesome(topic.icon || this.getTopicIcon(topic.topicName));
        const iconColor = topic.iconColor || this.getTopicIconColor(topic.topicName);

        tr.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                    <div class="flex-shrink-0 h-8 w-8">
                        <div class="h-8 w-8 rounded-full bg-${iconColor}-100 flex items-center justify-center">
                            <i class="fa ${iconClass} text-${iconColor}-600 text-sm"></i>
                        </div>
                    </div>
                    <div class="ml-4">
                        <div class="text-sm font-medium text-gray-900">
                            <a href="/topic/view?name=${topic.topicName}&cid=${topic.clusterId || this.getClusterId() || ''}" class="hover:text-blue-600 transition-colors">
                                ${topic.topicName}
                            </a>
                        </div>
                    </div>
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    ${topic.partitions || 0}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    ${topic.replicas || 0}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm ${brokerSpreadStatus.cssClass}">
                <div class="flex items-center">
                    <i class="fa ${brokerSpreadStatus.icon} mr-2"></i>
                    ${brokerSpreadStatus.text}
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm ${brokerSkewedStatus.cssClass}">
                <div class="flex items-center">
                    <i class="fa ${brokerSkewedStatus.icon} mr-2"></i>
                    ${brokerSkewedStatus.text}
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm ${leaderSkewedStatus.cssClass}">
                <div class="flex items-center">
                    <i class="fa ${leaderSkewedStatus.icon} mr-2"></i>
                    ${leaderSkewedStatus.text}
                </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                    ${retentionTimeText}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center space-x-2">
                    <button class="text-blue-600 hover:text-blue-900 transition-colors" 
                            onclick="TopicsModule.showScaleModal('${topic.topicName}', ${topic.partitions || 0})" 
                            title="扩容">
                        <i class="fa fa-expand"></i>
                    </button>
                    <button class="text-green-600 hover:text-green-900 transition-colors" 
                            onclick="TopicsModule.showEditRetentionModal('${topic.topicName}', ${topic.retentionTime || 0})" 
                            title="编辑保留时间">
                        <i class="fa fa-edit"></i>
                    </button>
                    <button class="text-red-600 hover:text-red-900 transition-colors" 
                            onclick="TopicsModule.showDeleteModal('${topic.topicName}')" 
                            title="删除">
                        <i class="fa fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        return tr;
    },


    // 更新分页
    updatePagination() {
        const startItem = (this.currentPage - 1) * this.pageSize + 1;
        const endItem = Math.min(this.currentPage * this.pageSize, this.totalItems);

        const startItemElement = document.getElementById('start-item');
        const endItemElement = document.getElementById('end-item');
        const totalItemsElement = document.getElementById('total-items');

        if (startItemElement) {
            startItemElement.textContent = this.totalItems > 0 ? startItem : 0;
        }
        if (endItemElement) {
            endItemElement.textContent = endItem;
        }
        if (totalItemsElement) {
            totalItemsElement.textContent = this.totalItems;
        }

        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');

        if (prevBtn) {
            prevBtn.disabled = this.currentPage === 1;
        }
        if (nextBtn) {
            nextBtn.disabled = this.currentPage === this.totalPages || this.totalPages === 0;
        }

        const pageNumbersContainer = document.getElementById('page-numbers');
        if (pageNumbersContainer) {
            pageNumbersContainer.innerHTML = '';

            if (this.totalPages <= 7) {
                for (let i = 1; i <= this.totalPages; i++) {
                    pageNumbersContainer.appendChild(this.createPageButton(i, i === this.currentPage));
                }
            } else {
                pageNumbersContainer.appendChild(this.createPageButton(1, this.currentPage === 1));

                if (this.currentPage > 3) {
                    pageNumbersContainer.appendChild(this.createEllipsis());
                }

                const startPage = Math.max(2, this.currentPage - 1);
                const endPage = Math.min(this.totalPages - 1, this.currentPage + 1);

                for (let i = startPage; i <= endPage; i++) {
                    pageNumbersContainer.appendChild(this.createPageButton(i, i === this.currentPage));
                }

                if (this.currentPage < this.totalPages - 2) {
                    pageNumbersContainer.appendChild(this.createEllipsis());
                }

                pageNumbersContainer.appendChild(this.createPageButton(this.totalPages, this.currentPage === this.totalPages));
            }
        }
    },

    // 创建页码按钮
    createPageButton(pageNum, isActive) {
        const button = document.createElement('button');
        button.textContent = pageNum;
        button.className = `px-3 py-1 text-sm border border-gray-300 rounded-lg transition-colors ${isActive
            ? 'bg-primary text-white border-primary'
            : 'hover:bg-gray-50'
            }`;

        if (!isActive) {
            button.addEventListener('click', () => {
                this.currentPage = pageNum;
                this.loadTopicData();
            });
        }

        return button;
    },

    // 创建省略号
    createEllipsis() {
        const span = document.createElement('span');
        span.textContent = '...';
        span.className = 'px-2 py-1 text-sm text-gray-500';
        return span;
    },

    // 初始化对话框功能
    initModalDialogs() {
        const scaleModal = document.getElementById('scale-modal');
        const deleteModal = document.getElementById('delete-modal');
        const editRetentionModal = document.getElementById('edit-retention-modal');

        if (!scaleModal || !deleteModal) return;
        
        // 初始化编辑保留时间对话框事件
        if (editRetentionModal) {
            // 关闭按钮事件
            const closeEditRetentionBtn = document.getElementById('close-edit-retention-modal');
            const cancelEditRetentionBtn = document.getElementById('cancel-edit-retention-btn');
            const confirmEditRetentionBtn = document.getElementById('confirm-edit-retention-btn');
            
            if (closeEditRetentionBtn) {
                closeEditRetentionBtn.addEventListener('click', () => {
                    this.hideEditRetentionModal();
                });
            }
            
            if (cancelEditRetentionBtn) {
                cancelEditRetentionBtn.addEventListener('click', () => {
                    this.hideEditRetentionModal();
                });
            }
            
            if (confirmEditRetentionBtn) {
                confirmEditRetentionBtn.addEventListener('click', () => {
                    this.updateRetentionTime();
                });
            }
            
            // 点击遮罩层关闭对话框
            editRetentionModal.addEventListener('click', (e) => {
                if (e.target === editRetentionModal) {
                    this.hideEditRetentionModal();
                }
            });
        }

        const scaleTopicName = document.getElementById('scale-topic-name');
        const currentPartitions = document.getElementById('current-partitions');
        const newPartitions = document.getElementById('new-partitions');
        const confirmScaleBtn = document.getElementById('confirm-scale-btn');

        const deleteTopicName = document.getElementById('delete-topic-name');
        const confirmTopicName = document.getElementById('confirm-topic-name');
        const confirmDeleteBtn = document.getElementById('confirm-delete-btn');

        // 注意：扩容和删除按钮的事件处理已经在 createTableRow 方法中通过 onclick 属性绑定

        // 关闭对话框事件
        this.bindModalCloseEvents(scaleModal, deleteModal);

        // 确认操作（先绑定确认操作，获取新的按钮引用）
        const newButtons = this.bindModalConfirm(confirmScaleBtn, confirmDeleteBtn, scaleModal, deleteModal);

        // 输入验证（使用新的按钮引用）
        this.bindModalValidation(newPartitions, newButtons.confirmScaleBtn, confirmTopicName, newButtons.confirmDeleteBtn);
        
        // 初始化创建主题模态框
        this.initCreateTopicModal();
        
    },

    // 绑定对话框关闭事件
    bindModalCloseEvents(scaleModal, deleteModal) {
        // 关闭扩容对话框
        const closeScaleBtn = document.getElementById('close-scale-modal');
        const cancelScaleBtn = document.getElementById('cancel-scale-btn');
        if (closeScaleBtn) {
            closeScaleBtn.addEventListener('click', () => this.closeScaleModal(scaleModal));
        }
        if (cancelScaleBtn) {
            cancelScaleBtn.addEventListener('click', () => this.closeScaleModal(scaleModal));
        }

        // 关闭删除对话框
        const closeDeleteBtn = document.getElementById('close-delete-modal');
        const cancelDeleteBtn = document.getElementById('cancel-delete-btn');
        if (closeDeleteBtn) {
            closeDeleteBtn.addEventListener('click', () => this.closeDeleteModal(deleteModal));
        }
        if (cancelDeleteBtn) {
            cancelDeleteBtn.addEventListener('click', () => this.closeDeleteModal(deleteModal));
        }

        // 点击遮罩层关闭对话框
        if (scaleModal) {
            scaleModal.addEventListener('click', (e) => {
                if (e.target === scaleModal) {
                    this.closeScaleModal(scaleModal);
                }
            });
        }

        if (deleteModal) {
            deleteModal.addEventListener('click', (e) => {
                if (e.target === deleteModal) {
                    this.closeDeleteModal(deleteModal);
                }
            });
        }

        // ESC键关闭对话框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeScaleModal(scaleModal);
                this.closeDeleteModal(deleteModal);
            }
        });
    },

    // 绑定对话框验证
    bindModalValidation(newPartitions, confirmScaleBtn, confirmTopicName, confirmDeleteBtn) {
        // 扩容输入验证
        if (newPartitions && confirmScaleBtn) {
            newPartitions.addEventListener('input', function () {
                const value = parseInt(this.value);
                const currentPartitionsElement = document.getElementById('current-partitions');
                const currentPartitions = currentPartitionsElement ? parseInt(currentPartitionsElement.textContent) || 0 : 0;
                const isValid = value && value > currentPartitions;
                confirmScaleBtn.disabled = !isValid;

                if (this.value && value <= currentPartitions) {
                    this.setCustomValidity('新分区数必须大于当前分区数');
                } else {
                    this.setCustomValidity('');
                }
            });
        }

        // 删除确认输入验证
        if (confirmTopicName && confirmDeleteBtn) {
            confirmTopicName.addEventListener('input', function () {
                const deleteModal = document.getElementById('delete-modal');
                const topicName = deleteModal ? deleteModal.dataset.topicName : '';
                const isValid = this.value === topicName;
                confirmDeleteBtn.disabled = !isValid;
            });
        }
    },

    // 绑定对话框确认
    bindModalConfirm(confirmScaleBtn, confirmDeleteBtn, scaleModal, deleteModal) {
        // 移除之前的事件监听器，避免重复绑定
        let newConfirmScaleBtn = null;
        let newConfirmDeleteBtn = null;

        if (confirmScaleBtn) {
            newConfirmScaleBtn = confirmScaleBtn.cloneNode(true);
            confirmScaleBtn.parentNode.replaceChild(newConfirmScaleBtn, confirmScaleBtn);

            // 确认扩容
            newConfirmScaleBtn.addEventListener('click', () => {
                this.scaleTopic();
            });
        }

        if (confirmDeleteBtn) {
            newConfirmDeleteBtn = confirmDeleteBtn.cloneNode(true);
            confirmDeleteBtn.parentNode.replaceChild(newConfirmDeleteBtn, confirmDeleteBtn);

            // 确认删除
            newConfirmDeleteBtn.addEventListener('click', () => {
                const confirmTopicNameInput = document.getElementById('confirm-topic-name');
                const confirmTopicName = confirmTopicNameInput ? confirmTopicNameInput.value : '';
                const topicName = deleteModal ? deleteModal.dataset.topicName : '';
                if (confirmTopicName === topicName) {
                    this.deleteTopic(topicName);
                }
            });
        }

        // 返回新的按钮引用
        return {
            confirmScaleBtn: newConfirmScaleBtn,
            confirmDeleteBtn: newConfirmDeleteBtn
        };
    },

    // 关闭扩容对话框
    closeScaleModal(modal) {
        if (modal) {
            modal.classList.add('hidden');
        }
        document.body.style.overflow = 'auto';
        const newPartitionsInput = document.getElementById('new-partitions');
        if (newPartitionsInput) {
            newPartitionsInput.value = '';
        }
        const confirmScaleBtn = document.getElementById('confirm-scale-btn');
        if (confirmScaleBtn) {
            confirmScaleBtn.disabled = false;
        }
    },

    // 关闭删除对话框
    closeDeleteModal(modal) {
        if (modal) {
            modal.classList.add('hidden');
        }
        document.body.style.overflow = 'auto';
        const confirmTopicNameInput = document.getElementById('confirm-topic-name');
        if (confirmTopicNameInput) {
            confirmTopicNameInput.value = '';
        }
        const confirmDeleteBtn = document.getElementById('confirm-delete-btn');
        if (confirmDeleteBtn) {
            confirmDeleteBtn.disabled = true;
        }
    },

    // 初始化创建主题模态框
    initCreateTopicModal() {
        const createTopicModal = document.getElementById('create-topic-modal');
        const createTopicBtn = document.getElementById('create-topic-btn');
        const closeModalBtns = createTopicModal?.querySelectorAll('.close-modal');
        const advancedConfigToggle = document.getElementById('advanced-config-toggle');
        const advancedConfigPanel = document.getElementById('advanced-config-panel');
        const createTopicForm = document.getElementById('create-topic-form');
        const createTopicSubmit = document.getElementById('create-topic-submit');

        if (!createTopicModal || !createTopicBtn) return;

        // 初始化Select2组件
        const initSelect2Components = () => {
            // 初始化主题类型图标选择器
            if (typeof $ !== 'undefined' && $('#topic-icon').length) {
                $('#topic-icon').select2({
                    minimumResultsForSearch: Infinity,
                    dropdownParent: $('#create-topic-modal'),
                    width: 'resolve',
                    dropdownCssClass: 'topic-icon-dropdown',
                    templateResult: this.formatIconOption,
                    templateSelection: this.formatIconSelection,
                    escapeMarkup: function (m) { return m; }
                });
            }

            // 初始化保留时间单位选择器
            if (typeof $ !== 'undefined' && $('#retention-time-unit').length) {
                $('#retention-time-unit').select2({
                    minimumResultsForSearch: Infinity,
                    dropdownParent: $('#create-topic-modal'),
                    width: 'resolve'
                });
            }

            // 初始化清理策略选择器
            if (typeof $ !== 'undefined' && $('#cleanup-policy').length) {
                $('#cleanup-policy').select2({
                    minimumResultsForSearch: Infinity,
                    dropdownParent: $('#create-topic-modal'),
                    width: 'resolve',
                    dropdownCssClass: 'cleanup-policy-dropdown'
                });
            }
        };

        // 打开对话框
        createTopicBtn.addEventListener('click', () => {
            createTopicModal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
            setTimeout(() => {
                initSelect2Components();
            }, 50);
        });

        // 关闭对话框
        closeModalBtns?.forEach(btn => {
            btn.addEventListener('click', () => {
                createTopicModal.classList.add('hidden');
                document.body.style.overflow = 'auto';
                createTopicForm?.reset();
                advancedConfigPanel?.classList.add('hidden');
                advancedConfigToggle?.classList.remove('active');
                // 销毁Select2实例
                if (typeof $ !== 'undefined') {
                    if ($('#retention-time-unit').hasClass('select2-hidden-accessible')) {
                        $('#retention-time-unit').select2('destroy');
                    }
                    if ($('#cleanup-policy').hasClass('select2-hidden-accessible')) {
                        $('#cleanup-policy').select2('destroy');
                    }
                }
            });
        });

        // 切换高级配置面板
        advancedConfigToggle?.addEventListener('click', () => {
            advancedConfigPanel?.classList.toggle('hidden');
            advancedConfigToggle?.classList.toggle('active');
        });

        // 表单验证和提交
        createTopicForm?.addEventListener('submit', async (e) => {
            e.preventDefault();

            // 调用统一的创建函数
            const formData = new FormData(createTopicForm);
            const success = await this.createTopic(formData);

            if (success) {
                // 关闭对话框
                createTopicModal.classList.add('hidden');
                document.body.style.overflow = 'auto';
                createTopicForm.reset();
                advancedConfigPanel?.classList.add('hidden');
                advancedConfigToggle?.classList.remove('active');

                // 销毁Select2实例
                if (typeof $ !== 'undefined') {
                    if ($('#retention-time-unit').hasClass('select2-hidden-accessible')) {
                        $('#retention-time-unit').select2('destroy');
                    }
                    if ($('#cleanup-policy').hasClass('select2-hidden-accessible')) {
                        $('#cleanup-policy').select2('destroy');
                    }
                }
            }
        });

        // 阻止表单默认提交
        createTopicSubmit?.addEventListener('click', (e) => {
            e.preventDefault();
            createTopicForm?.dispatchEvent(new Event('submit'));
        });
    },

    // 格式化图标选项
    formatIconOption(icon) {
        if (!icon.id) return icon.text;
        const $icon = $(icon.element);
        const iconClass = $icon.data('icon');
        const iconColor = $icon.data('color');

        return $(`
            <div class="flex items-center">
                <div class="icon-preview icon-color-${iconColor}">
                    <i class="fa ${iconClass}"></i>
                </div>
                <div class="icon-info">
                    <div class="icon-title">${icon.text}</div>
                </div>
            </div>
        `);
    },

    // 格式化选中的图标
    formatIconSelection(icon) {
        if (!icon.id) return icon.text;
        const $icon = $(icon.element);
        const iconClass = $icon.data('icon');
        const iconColor = $icon.data('color');

        return $(`
            <div class="flex items-center">
                <div class="icon-preview icon-color-${iconColor}">
                    <i class="fa ${iconClass}"></i>
                </div>
                <span>${icon.text}</span>
            </div>
        `);
    },


    // 初始化图表功能
    initChartFeatures() {
        // 设置默认时间范围（最近7天）
        this.setDefaultDateRange();

        // 初始化图表
        this.initChart();

        // 绑定维度切换按钮事件
        this.bindDimensionButtons();

        // 绑定时间范围选择事件
        this.bindDateRangeEvents();

        // 初始加载图表数据
        this.loadChartData();
    },

    // 设置默认时间范围（最近7天）
    setDefaultDateRange() {
        const endDate = new Date();
        const startDate = new Date();
        startDate.setDate(endDate.getDate() - 6); // 最近7天

        const startDateStr = startDate.toISOString().split('T')[0];
        const endDateStr = endDate.toISOString().split('T')[0];

        const startDateInput = document.getElementById('start-date');
        const endDateInput = document.getElementById('end-date');

        if (startDateInput) startDateInput.value = startDateStr;
        if (endDateInput) endDateInput.value = endDateStr;
    },

    // 初始化图表
    initChart() {
        const ctx = document.getElementById('topicTrendChart');
        if (!ctx) return;

        this.chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: []
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                plugins: {
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        borderColor: 'rgba(255, 255, 255, 0.1)',
                        borderWidth: 1
                    },
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    }
                },
                scales: {
                    x: {
                        display: true,
                        grid: {
                            display: true,
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    },
                    y: {
                        display: true,
                        beginAtZero: true,
                        grid: {
                            display: true,
                            color: 'rgba(0, 0, 0, 0.1)'
                        }
                    }
                },
                elements: {
                    line: {
                        tension: 0.4,
                        borderWidth: 2
                    },
                    point: {
                        radius: 4,
                        hoverRadius: 6
                    }
                }
            }
        });
    },

    // 绑定维度切换按钮事件
    bindDimensionButtons() {
        const capacityBtn = document.getElementById('capacity-btn');
        const messagesBtn = document.getElementById('messages-btn');

        if (capacityBtn) {
            capacityBtn.addEventListener('click', () => {
                this.switchDimension('capacity', capacityBtn, messagesBtn);
            });
        }

        if (messagesBtn) {
            messagesBtn.addEventListener('click', () => {
                this.switchDimension('messages', messagesBtn, capacityBtn);
            });
        }
    },

    // 切换数据维度
    switchDimension(dimension, activeBtn, inactiveBtn) {
        // 更新按钮样式
        activeBtn.className = 'px-4 py-1.5 text-sm bg-primary/10 text-primary rounded-lg border border-primary/20 hover:bg-primary/20 transition-colors';
        inactiveBtn.className = 'px-4 py-1.5 text-sm bg-gray-100 text-gray-600 rounded-lg border border-gray-200 hover:bg-gray-200 transition-colors';

        // 重新加载图表数据
        this.loadChartData();
    },

    // 绑定时间范围选择事件
    bindDateRangeEvents() {
        const startDateInput = document.getElementById('start-date');
        const endDateInput = document.getElementById('end-date');

        if (startDateInput) {
            startDateInput.addEventListener('change', () => {
                this.loadChartData();
            });
        }

        if (endDateInput) {
            endDateInput.addEventListener('change', () => {
                this.loadChartData();
            });
        }
    },

    // 获取当前选择的维度
    getCurrentDimension() {
        const capacityBtn = document.getElementById('capacity-btn');
        if (capacityBtn && capacityBtn.classList.contains('bg-primary/10')) {
            return 'capacity';
        }
        return 'messages';
    },

    // 获取时间范围
    getDateRange() {
        const startDateInput = document.getElementById('start-date');
        const endDateInput = document.getElementById('end-date');

        return {
            startDate: startDateInput ? startDateInput.value : null,
            endDate: endDateInput ? endDateInput.value : null
        };
    },

    // 加载图表数据
    async loadChartData() {
        try {
            const dimension = this.getCurrentDimension();
            const dateRange = this.getDateRange();
            const selectedTopics = this.getSelectedTopics();
            const clusterId = this.getClusterId();

            // 参数验证
            if (!dateRange.startDate || !dateRange.endDate) {
                console.warn('日期范围未设置，使用默认值');
                return;
            }

            const params = new URLSearchParams();

            // 必需参数
            params.append('dimension', dimension);
            params.append('startDate', dateRange.startDate);
            params.append('endDate', dateRange.endDate);

            // 可选参数
            if (clusterId && clusterId.trim() !== '') {
                params.append('clusterId', clusterId.trim());
            }

            // 主题筛选参数 - 如果有选中的主题才添加
            if (selectedTopics && selectedTopics.length > 0) {
                // 确保主题名称非空且去除空白字符
                const validTopics = selectedTopics.filter(topic => topic && topic.trim() !== '');
                if (validTopics.length > 0) {
                    params.append('topics', validTopics.join(','));
                }
            }

            // console.log('请求图表数据参数:', {
            //     dimension: dimension,
            //     startDate: dateRange.startDate,
            //     endDate: dateRange.endDate,
            //     clusterId: clusterId,
            //     topics: selectedTopics
            // });

            const response = await fetch(`/topic/api/trend?${params}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result.success) {
                this.updateChartData(result.data, dimension);
            } else {
                this.showError('加载图表数据失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('加载图表数据失败:', error);
            this.showError('加载图表数据异常: ' + error.message);
        }
    },

    // 获取选中的主题
    getSelectedTopics() {
        const topicSearchInput = document.getElementById('topic-search-input');
        if (topicSearchInput && topicSearchInput.classList.contains('has-selection')) {
            return [topicSearchInput.value];
        }
        return [];
    },

    // 获取集群ID
    getClusterId() {
        // 从URL参数中获取cid
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('cid');
    },

    // 更新图表数据
    updateChartData(data, dimension) {
        if (!this.chart || !data) return;

        const labels = data.labels || [];
        const datasets = (data.datasets || []).map(dataset => ({
            label: dataset.label,
            data: dataset.data,
            borderColor: dataset.borderColor || '#3b82f6',
            backgroundColor: dataset.backgroundColor || 'rgba(59, 130, 246, 0.1)',
            fill: true, // 使用area样式
            tension: 0.4
        }));

        this.chart.data.labels = labels;
        this.chart.data.datasets = datasets;

        // 更新Y轴标签和图表标题
        const yAxisLabel = dimension === 'capacity' ? '容量增量 (Bytes)' : '记录数增量';
        this.chart.options.scales.y.title = {
            display: true,
            text: yAxisLabel
        };

        // 更新图表标题
        this.chart.options.plugins.title = {
            display: true,
            text: `总体趋势分析 - ${dimension === 'capacity' ? '容量' : '记录数'}维度`
        };

        // 格式化tooltip显示
        this.chart.options.plugins.tooltip.callbacks = {
            label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                    label += ': ';
                }
                const value = context.parsed.y;
                if (dimension === 'capacity') {
                    // 格式化容量单位
                    label += TopicsModule.formatCapacity(value);
                } else {
                    // 格式化记录数
                    label += TopicsModule.formatRecordCount(value);
                }
                return label;
            }
        };

        // console.log('图表数据更新完成:', {
        //     labels: labels.length,
        //     datasets: datasets.length,
        //     dimension: dimension
        // });

        this.chart.update();
    },

    // 根据主题筛选更新图表（简化版）
    updateChartByTopicFilter(selectedTopics) {
        this.loadChartData();
    },

    // 初始化自动刷新
    initAutoRefresh() {
        this.autoRefreshInterval = null;
        this.refreshCountdown = 60; // 60秒倒计时
        this.startAutoRefresh();

        // 防止Cmd+R被JS拦截，移除页面beforeunload事件
        window.onbeforeunload = null;

        // 监听页面隐藏/显示事件
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.pauseAutoRefresh();
            } else {
                this.resumeAutoRefresh();
            }
        });
    },

    // 开始自动刷新
    startAutoRefresh() {
        this.refreshCountdown = 60;
        this.updateCountdownDisplay();

        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
        }

        this.autoRefreshInterval = setInterval(() => {
            this.refreshCountdown--;
            this.updateCountdownDisplay();

            if (this.refreshCountdown <= 0) {
                this.performAutoRefresh();
                this.refreshCountdown = 60; // 重置倒计时
            }
        }, 1000);
    },

    // 暂停自动刷新
    pauseAutoRefresh() {
        if (this.autoRefreshInterval) {
            clearInterval(this.autoRefreshInterval);
            this.autoRefreshInterval = null;
        }
    },

    // 恢复自动刷新
    resumeAutoRefresh() {
        if (!this.autoRefreshInterval) {
            this.startAutoRefresh();
        }
    },

    // 更新倒计时显示
    updateCountdownDisplay() {
        const countdownElement = document.getElementById('refresh-countdown');
        if (countdownElement) {
            countdownElement.textContent = this.refreshCountdown;
        }
    },

    // 执行自动刷新
    async performAutoRefresh() {
        try {
            // 显示刷新状态
            const statusElement = document.getElementById('auto-refresh-status');
            if (statusElement) {
                statusElement.classList.remove('bg-green-50', 'border-green-200');
                statusElement.classList.add('bg-blue-50', 'border-blue-200');
                const iconElement = statusElement.querySelector('i');
                const textElement = statusElement.querySelector('.text-green-700');
                if (iconElement) {
                    iconElement.className = 'fa fa-spinner fa-spin text-blue-600';
                }
                if (textElement) {
                    textElement.className = 'text-sm text-blue-700';
                    textElement.innerHTML = '正在刷新数据...';
                }
            }

            // 刷新主题数据
            await this.loadTopicData();

            // 刷新统计数据
            await this.loadTopicStatistics();

            // 恢复正常状态
            setTimeout(() => {
                if (statusElement) {
                    statusElement.classList.remove('bg-blue-50', 'border-blue-200');
                    statusElement.classList.add('bg-green-50', 'border-green-200');
                    const iconElement = statusElement.querySelector('i');
                    const textElement = statusElement.querySelector('.text-blue-700');
                    if (iconElement) {
                        iconElement.className = 'fa fa-clock-o text-green-600';
                    }
                    if (textElement) {
                        textElement.className = 'text-sm text-green-700';
                        textElement.innerHTML = '自动刷新: <span id="refresh-countdown">60</span>s';
                    }
                }
            }, 1000);

        } catch (error) {
            // 显示错误状态
            const statusElement = document.getElementById('auto-refresh-status');
            if (statusElement) {
                statusElement.classList.remove('bg-green-50', 'border-green-200', 'bg-blue-50', 'border-blue-200');
                statusElement.classList.add('bg-red-50', 'border-red-200');
                const iconElement = statusElement.querySelector('i');
                const textElement = statusElement.querySelector('span');
                if (iconElement) {
                    iconElement.className = 'fa fa-exclamation-triangle text-red-600';
                }
                if (textElement) {
                    textElement.className = 'text-sm text-red-700';
                    textElement.innerHTML = '刷新失败，将继续重试...';
                }

                // 3秒后恢复正常状态
                setTimeout(() => {
                    statusElement.classList.remove('bg-red-50', 'border-red-200');
                    statusElement.classList.add('bg-green-50', 'border-green-200');
                    if (iconElement) {
                        iconElement.className = 'fa fa-clock-o text-green-600';
                    }
                    if (textElement) {
                        textElement.className = 'text-sm text-green-700';
                        textElement.innerHTML = '自动刷新: <span id="refresh-countdown">60</span>s';
                    }
                }, 3000);
            }
        }
    },

    // 加载topic统计数据
    async loadTopicStatistics() {
        try {
            // 构建请求参数，包含clusterId
            const params = new URLSearchParams();
            const clusterId = this.getClusterId();

            if (clusterId) {
                params.append('clusterId', clusterId);
            }

            const response = await fetch(`/topic/api/statistics?${params}`);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const result = await response.json();

            if (result.success && result.data) {
                this.updateStatisticsCards(result.data);
            }
        } catch (error) {
            // 静默处理统计数据加载失败
        }
    },

    // 更新统计卡片
    updateStatisticsCards(stats) {
        // 更新总容量
        this.updateCard('total-capacity', stats.totalCapacity);

        // 更新总记录数
        this.updateCard('total-records', stats.totalRecordCount);

        // 更新读取速度
        this.updateCard('read-speed', stats.avgReadSpeed);

        // 更新写入速度
        this.updateCard('write-speed', stats.avgWriteSpeed);
    },

    // 更新单个卡片
    updateCard(cardType, currentValue) {
        let valueElement;

        // 使用更简单的选择器，基于卡片在grid中的位置
        const cards = document.querySelectorAll('.grid.grid-cols-1 > div');

        switch (cardType) {
            case 'total-capacity':
                // 更新总容量卡片 - 第一个卡片
                if (cards[0]) {
                    valueElement = cards[0].querySelector('h3');
                }
                if (valueElement) {
                    valueElement.textContent = this.formatCapacity(currentValue);
                }
                break;
            case 'total-records':
                // 更新总记录数卡片 - 第二个卡片
                if (cards[1]) {
                    valueElement = cards[1].querySelector('h3');
                }
                if (valueElement) {
                    valueElement.textContent = this.formatRecordCount(currentValue);
                }
                break;
            case 'read-speed':
                // 更新读取速度卡片 - 第三个卡片
                if (cards[2]) {
                    valueElement = cards[2].querySelector('h3');
                }
                if (valueElement) {
                    valueElement.textContent = this.formatSpeed(currentValue);
                }
                break;
            case 'write-speed':
                // 更新写入速度卡片 - 第四个卡片
                if (cards[3]) {
                    valueElement = cards[3].querySelector('h3');
                }
                if (valueElement) {
                    valueElement.textContent = this.formatSpeed(currentValue);
                }
                break;
        }
    },

    // 格式化容量
    formatCapacity(bytes) {
        if (!bytes || bytes === 0) return '0B';

        const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
        const i = Math.floor(Math.log(bytes) / Math.log(1024));
        const value = bytes / Math.pow(1024, i);

        // 根据大小决定小数位数
        if (value >= 100) {
            return `${value.toFixed(0)}${sizes[i]}`;
        } else if (value >= 10) {
            return `${value.toFixed(1)}${sizes[i]}`;
        } else {
            return `${value.toFixed(2)}${sizes[i]}`;
        }
    },

    // 格式化记录数
    formatRecordCount(count) {
        if (!count || count === 0) return '0';

        if (count >= 1000000000000) {
            // 万亿级别
            const value = count / 1000000000000;
            return value >= 100 ? `${value.toFixed(0)}T` :
                   value >= 10 ? `${value.toFixed(1)}T` : `${value.toFixed(2)}T`;
        } else if (count >= 1000000000) {
            // 十亿级别
            const value = count / 1000000000;
            return value >= 100 ? `${value.toFixed(0)}B` :
                   value >= 10 ? `${value.toFixed(1)}B` : `${value.toFixed(2)}B`;
        } else if (count >= 1000000) {
            // 百万级别
            const value = count / 1000000;
            return value >= 100 ? `${value.toFixed(0)}M` :
                   value >= 10 ? `${value.toFixed(1)}M` : `${value.toFixed(2)}M`;
        } else if (count >= 1000) {
            // 千级别
            const value = count / 1000;
            return value >= 100 ? `${value.toFixed(0)}K` :
                   value >= 10 ? `${value.toFixed(1)}K` : `${value.toFixed(2)}K`;
        }

        return count.toString();
    },

    // 格式化速度
    formatSpeed(bytesPerSecond) {
        if (!bytesPerSecond || bytesPerSecond === 0) return '0B/s';

        const sizes = ['B/s', 'KB/s', 'MB/s', 'GB/s', 'TB/s', 'PB/s'];
        const i = Math.floor(Math.log(bytesPerSecond) / Math.log(1024));
        const value = bytesPerSecond / Math.pow(1024, i);

        // 根据大小决定小数位数
        if (value >= 100) {
            return `${value.toFixed(0)}${sizes[i]}`;
        } else if (value >= 10) {
            return `${value.toFixed(1)}${sizes[i]}`;
        } else {
            return `${value.toFixed(2)}${sizes[i]}`;
        }
    }
};

// 页面初始化
document.addEventListener('DOMContentLoaded', function () {
    // 设置当前页面活跃状态
    if (typeof setActiveNavItem === 'function') {
        setActiveNavItem('topics');
    }

    // 初始化页面功能
    if (typeof TopicsModule !== 'undefined') {
        TopicsModule.init();
    }
});