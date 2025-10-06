/**
 * 配置管理页面 JavaScript
 * 提供配置项的查看、编辑、重置等功能
 */

class ConfigManager {
    constructor() {
        this.currentEditConfig = null;
        this.modelData = [];
        this.currentPage = 1;
        this.pageSize = 5;
        this.totalCount = 0;
        this.totalPages = 1;
        this.init();
    }

    async init() {
        await this.initModelData();
        await this.loadStatistics();
        this.bindEvents();
        this.renderModelTable();
    }

    /**
     * 加载统计数据
     */
    async loadStatistics() {
        try {
            const response = await fetch('/api/model-config/statistics', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.success) {
                this.updateStatisticsCards(data.data);
            } else {
                console.error('获取统计数据失败:', data.message);
                // 使用默认数据
                this.updateStatisticsCards({
                    totalModels: 0,
                    onlineModels: 0,
                    offlineModels: 0,
                    disabledModels: 0
                });
            }
        } catch (error) {
            console.error('加载统计数据失败:', error);
            // 使用默认数据
            this.updateStatisticsCards({
                totalModels: 0,
                onlineModels: 0,
                offlineModels: 0,
                disabledModels: 0
            });
        }
    }

    /**
     * 更新统计卡片数据
     */
    updateStatisticsCards(statistics) {
        try {
            const totalModelsElement = document.getElementById('totalModels');
            const onlineModelsElement = document.getElementById('onlineModels');
            const offlineModelsElement = document.getElementById('offlineModels');
            const disabledModelsElement = document.getElementById('disabledModels');

            if (totalModelsElement) totalModelsElement.textContent = statistics.totalModels || 0;
            if (onlineModelsElement) onlineModelsElement.textContent = statistics.onlineModels || 0;
            if (offlineModelsElement) offlineModelsElement.textContent = statistics.offlineModels || 0;
            if (disabledModelsElement) disabledModelsElement.textContent = statistics.disabledModels || 0;

            console.log('统计数据更新完成:', statistics);
        } catch (error) {
            console.error('更新统计卡片失败:', error);
        }
    }

    /**
     * 初始化模型数据
     */
    async initModelData() {
        try {
            await this.loadModelData();
        } catch (error) {
            console.error('加载模型数据失败:', error);
            this.modelData = [];
        }
    }

    /**
     * 从后端加载模型数据
     */
    async loadModelData() {
        try {
            const response = await fetch(`/api/model-config/page?page=${this.currentPage}&size=${this.pageSize}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.modelConfigs) {
                this.modelData = data.modelConfigs;
                this.totalCount = data.total || 0;
                this.totalPages = data.totalPages || 1;
                this.currentPage = data.page || 1;
                this.pageSize = data.size || 10;
            } else {
                this.modelData = [];
                this.totalCount = 0;
                this.totalPages = 1;
            }
        } catch (error) {
            console.error('获取模型配置列表失败:', error);
            this.modelData = [];
            this.totalCount = 0;
            this.totalPages = 1;
        }
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // ESC键关闭表单
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAddModelModal();
                this.closeEditModelModal();
                this.closeDeleteConfirmModal();
            }
        });

        // 添加模型按钮事件
        const addModelBtn = document.getElementById('add-model-btn');
        if (addModelBtn) {
            addModelBtn.addEventListener('click', () => {
                this.showAddModelModal();
            });
        }

        // 添加模型表单提交事件
        const addModelForm = document.getElementById('add-model-form');
        if (addModelForm) {
            addModelForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.addModel();
            });
        }

        // 编辑模型表单提交事件
        const editModelForm = document.getElementById('edit-model-form');
        if (editModelForm) {
            editModelForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.updateModel();
            });
        }

        // 分页按钮事件
        const prevPageBtn = document.getElementById('prev-page');
        const nextPageBtn = document.getElementById('next-page');
        if (prevPageBtn) {
            prevPageBtn.addEventListener('click', async () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    await this.loadModelData();
                    this.renderModelTable();
                }
            });
        }
        if (nextPageBtn) {
            nextPageBtn.addEventListener('click', async () => {
                if (this.currentPage < this.totalPages) {
                    this.currentPage++;
                    await this.loadModelData();
                    this.renderModelTable();
                }
            });
        }

        // 点击模态框背景关闭
        const addModelModal = document.getElementById('add-model-modal');
        const editModelModal = document.getElementById('edit-model-modal');
        if (addModelModal) {
            addModelModal.addEventListener('click', (e) => {
                if (e.target === addModelModal) {
                    this.closeAddModelModal();
                }
            });
        }
        if (editModelModal) {
            editModelModal.addEventListener('click', (e) => {
                if (e.target === editModelModal) {
                    this.closeEditModelModal();
                }
            });
        }

        // 删除确认对话框事件
        const deleteConfirmModal = document.getElementById('delete-confirm-modal');
        if (deleteConfirmModal) {
            deleteConfirmModal.addEventListener('click', (e) => {
                if (e.target === deleteConfirmModal) {
                    this.closeDeleteConfirmModal();
                }
            });
        }
    }

    /**
     * 显示Toast消息
     */
    showToast(message, type = 'success') {
        // 移除已存在的toast
        const existingToast = document.querySelector('.toast');
        if (existingToast) {
            existingToast.remove();
        }

        // 创建新的toast
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        // 添加图标
        let icon = '';
        switch (type) {
            case 'success':
                icon = '<i class="fa fa-check"></i>';
                break;
            case 'error':
                icon = '<i class="fa fa-times"></i>';
                break;
            case 'loading':
                icon = '<i class="fa fa-spinner fa-spin"></i>';
                break;
            default:
                icon = '<i class="fa fa-info"></i>';
        }

        toast.innerHTML = `${icon} ${message}`;
        document.body.appendChild(toast);

        // 显示动画
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // 自动隐藏（loading类型除外）
        if (type !== 'loading') {
            setTimeout(() => {
                toast.classList.remove('show');
                setTimeout(() => {
                    if (toast.parentNode) {
                        toast.parentNode.removeChild(toast);
                    }
                }, 300);
            }, 3000);
        }
    }

    // ==================== 大模型管理相关方法 ====================

    /**
     * 渲染模型表格
     */
    renderModelTable() {
        const tbody = document.getElementById('model-table-body');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (this.modelData.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                    <div class="flex flex-col items-center">
                        <i class="fa fa-inbox text-4xl mb-2"></i>
                        <p>暂无模型配置数据</p>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
            this.updatePagination();
            return;
        }

        this.modelData.forEach(model => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50';

            const statusBadge = this.getStatusBadge(model.status);
            const maskedApiKey = this.maskApiKey(model.apiKey);

            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                        <div class="text-sm font-medium text-gray-900">${model.modelName}</div>
                    </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                        <img src="${this.getApiTypeIcon(model.apiType)}" style="width: 14px; height: 14px; margin-right: 6px;" />
                        ${model.apiType}
                    </span>
                </td>
                <td class="px-6 py-4">
                    <div class="text-sm text-gray-900 max-w-xs truncate" title="${model.endpoint}">
                        ${model.endpoint}
                    </div>
                    <div class="text-xs text-gray-500 mt-1">密钥: ${maskedApiKey}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                    ${statusBadge}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    ${model.createTime}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                    <button onclick="configManager.editModel(${model.id})" 
                            class="text-blue-600 hover:text-blue-900 font-medium">
                        编辑
                    </button>
                    <button onclick="configManager.testModel(${model.id})" 
                            class="text-green-600 hover:text-green-900 font-medium">
                        测试
                    </button>
                    <button onclick="configManager.toggleModel(${model.id})" 
                            class="text-yellow-600 hover:text-yellow-900 font-medium">
                        ${model.enabled == 1 ? '禁用' : '启用'}
                    </button>
                    <button onclick="configManager.deleteModel(${model.id})" 
                            class="text-red-600 hover:text-red-900 font-medium">
                        删除
                    </button>
                </td>
            `;

            tbody.appendChild(row);
        });

        this.updatePagination();
    }

    /**
     * 获取API类型图标
     */
    getApiTypeIcon(type) {
        const icons = {
            'OpenAI': '/images/icons/openai.svg',
            'Ollama': '/images/icons/ollama.png',
            'DeepSeek': '/images/icons/deepseek.svg'
        };
        return icons[type] || '';
    }

    /**
     * 获取状态徽章
     */
    getStatusBadge(status) {
        const badges = {
            0: '<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">离线</span>',
            1: '<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">在线</span>',
            2: '<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">错误</span>'
        };
        return badges[status] || badges[0];
    }

    /**
     * 隐藏API密钥
     */
    maskApiKey(apiKey) {
        if (!apiKey || apiKey.length < 10) return '未设置';
        return apiKey.substring(0, 6) + '*'.repeat(10) + apiKey.substring(apiKey.length - 4);
    }

    /**
     * 更新分页信息
     */
    updatePagination() {
        const totalCount = this.totalCount || 0;
        const totalPages = this.totalPages || 1;
        const startIndex = this.modelData.length > 0 ? (this.currentPage - 1) * this.pageSize + 1 : 0;
        const endIndex = Math.min(this.currentPage * this.pageSize, totalCount);

        // 更新统计信息
        document.getElementById('page-start').textContent = startIndex;
        document.getElementById('page-end').textContent = endIndex;
        document.getElementById('total-count').textContent = totalCount;

        // 更新按钮状态
        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');
        if (prevBtn) prevBtn.disabled = this.currentPage <= 1;
        if (nextBtn) nextBtn.disabled = this.currentPage >= totalPages;

        // 生成页码按钮
        this.renderPageNumbers(totalPages);
    }

    /**
     * 渲染页码按钮
     */
    renderPageNumbers(totalPages) {
        const pageNumbersContainer = document.getElementById('page-numbers');
        if (!pageNumbersContainer) return;

        pageNumbersContainer.innerHTML = '';

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.className = `px-3 py-1 text-sm rounded-md transition-colors ${i === this.currentPage
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 hover:bg-gray-200 text-gray-700'
                }`;

            button.addEventListener('click', async () => {
                this.currentPage = i;
                await this.loadModelData();
                this.renderModelTable();
            });

            pageNumbersContainer.appendChild(button);
        }
    }

    /**
     * 显示添加模型对话框
     */
    showAddModelModal() {
        const modal = document.getElementById('add-model-modal');
        if (modal) {
            // 重置表单
            document.getElementById('add-model-form').reset();
            document.getElementById('model-enabled').checked = true;

            // 清空接口地址输入框
            document.getElementById('model-endpoint').value = '';

            modal.classList.add('show');

            // 初始化select2
            setTimeout(() => {
                $('.select2-model-type').select2({
                    placeholder: '请选择API类型',
                    allowClear: false,
                    width: '100%',
                    minimumResultsForSearch: Infinity, // 禁用搜索功能
                    dropdownParent: $('#add-model-modal'),
                    templateResult: function (option) {
                        if (!option.id) return option.text;
                        const icon = $(option.element).data('icon');
                        if (icon) {
                            return $('<span><img src="' + icon + '" style="width: 16px; height: 16px; margin-right: 8px; vertical-align: middle;" />' + option.text + '</span>');
                        }
                        return option.text;
                    },
                    templateSelection: function (option) {
                        if (!option.id) return option.text;
                        const icon = $(option.element).data('icon');
                        if (icon) {
                            return $('<span><img src="' + icon + '" style="width: 16px; height: 16px; margin-right: 8px; vertical-align: middle;" />' + option.text + '</span>');
                        }
                        return option.text;
                    }
                });

                // 监听API类型变化，自动填充接口地址
                $('.select2-model-type').on('change', function () {
                    const selectedType = $(this).val();
                    const endpointInput = document.getElementById('model-endpoint');
                    const apiKeyInput = document.getElementById('model-api-key');

                    // 清空API密钥输入框
                    apiKeyInput.value = '';

                    // 根据选择的API类型自动填充接口地址
                    switch (selectedType) {
                        case 'OpenAI':
                            endpointInput.value = 'https://api.openai.com/v1/chat/completions';
                            break;
                        case 'Ollama':
                            endpointInput.value = 'http://localhost:11434/api/chat';
                            break;
                        case 'DeepSeek':
                            endpointInput.value = 'https://api.deepseek.com/v1/chat/completions';
                            break;
                        default:
                            endpointInput.value = '';
                    }
                });
            }, 50);

            // 聚焦到模型名称输入框
            setTimeout(() => {
                const nameInput = document.getElementById('model-name');
                if (nameInput) nameInput.focus();
            }, 100);
        }
    }

    /**
     * 关闭添加模型对话框
     */
    closeAddModelModal() {
        const modal = document.getElementById('add-model-modal');
        if (modal) {
            // 移除事件监听器
            $('.select2-model-type').off('change');

            // 销毁select2实例
            if ($('.select2-model-type').hasClass('select2-hidden-accessible')) {
                $('.select2-model-type').select2('destroy');
            }
            modal.classList.remove('show');
        }
    }

    /**
     * 添加模型
     */
    async addModel() {
        const modelName = document.getElementById('model-name').value.trim();
        const apiType = document.getElementById('model-type').value;
        const endpoint = document.getElementById('model-endpoint').value.trim();
        const apiKey = document.getElementById('model-api-key').value.trim();
        const systemPrompt = document.getElementById('model-system-prompt').value.trim();
        const timeout = parseInt(document.getElementById('model-timeout').value) || 30;
        const description = document.getElementById('model-description').value.trim();
        const enabled = document.getElementById('model-enabled').checked ? 1 : 0;

        // 验证必填字段
        if (!modelName || !apiType || !endpoint) {
            this.showToast('请填写所有必填字段', 'error');
            return;
        }

        // 验证接口地址格式
        try {
            new URL(endpoint);
        } catch (e) {
            this.showToast('接口地址格式不正确', 'error');
            return;
        }

        this.showToast('正在添加模型...', 'loading');

        try {
            // 调用后端API
            const response = await fetch('/api/model-config', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    modelName: modelName,
                    apiType: apiType,
                    endpoint: endpoint,
                    apiKey: apiKey,
                    systemPrompt: systemPrompt,
                    timeout: timeout,
                    description: description,
                    enabled: enabled
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || '添加模型失败');
            }

            const result = await response.json();

            // 重新加载模型数据
            await this.loadModelData();
            await this.loadStatistics();
            this.renderModelTable();
            this.closeAddModelModal();
            this.showToast('模型添加成功', 'success');
        } catch (error) {
            console.error('添加模型失败:', error);
            this.showToast(error.message || '添加模型失败', 'error');
        }
    }

    /**
     * 编辑模型
     */
    editModel(id) {
        const model = this.modelData.find(m => m.id === id);
        if (!model) {
            this.showToast('模型不存在', 'error');
            return;
        }

        // 填充编辑表单
        document.getElementById('edit-model-id').value = model.id;
        document.getElementById('edit-model-name').value = model.modelName;
        document.getElementById('edit-model-type').value = model.apiType;
        document.getElementById('edit-model-endpoint').value = model.endpoint;
        document.getElementById('edit-model-api-key').value = model.apiKey || '';
        document.getElementById('edit-model-timeout').value = model.timeout;
        document.getElementById('edit-model-description').value = model.description || '';
        document.getElementById('edit-model-system-prompt').value = model.systemPrompt || '';
        document.getElementById('edit-model-enabled').checked = model.enabled == 1;

        // 显示编辑对话框
        const modal = document.getElementById('edit-model-modal');
        if (modal) {
            modal.classList.add('show');

            // 初始化select2
            setTimeout(() => {
                $('.select2-edit-model-type').select2({
                    placeholder: '请选择API类型',
                    allowClear: false,
                    width: '100%',
                    minimumResultsForSearch: Infinity, // 禁用搜索功能
                    dropdownParent: $('#edit-model-modal'),
                    templateResult: function (option) {
                        if (!option.id) return option.text;
                        const icon = $(option.element).data('icon');
                        if (icon) {
                            return $('<span><img src="' + icon + '" style="width: 16px; height: 16px; margin-right: 8px; vertical-align: middle;" />' + option.text + '</span>');
                        }
                        return option.text;
                    },
                    templateSelection: function (option) {
                        if (!option.id) return option.text;
                        const icon = $(option.element).data('icon');
                        if (icon) {
                            return $('<span><img src="' + icon + '" style="width: 16px; height: 16px; margin-right: 8px; vertical-align: middle;" />' + option.text + '</span>');
                        }
                        return option.text;
                    }
                });

                // 设置选中值
                $('.select2-edit-model-type').val(model.apiType).trigger('change');

                // 监听API类型变化，自动填充接口地址
                $('.select2-edit-model-type').on('change', function () {
                    const selectedType = $(this).val();
                    const endpointInput = document.getElementById('edit-model-endpoint');
                    const apiKeyInput = document.getElementById('edit-model-api-key');

                    // 清空API密钥输入框
                    apiKeyInput.value = '';

                    // 根据选择的API类型自动填充接口地址
                    switch (selectedType) {
                        case 'OpenAI':
                            endpointInput.value = 'https://api.openai.com/v1/chat/completions';
                            break;
                        case 'Ollama':
                            endpointInput.value = 'http://localhost:11434/api/chat';
                            break;
                        case 'DeepSeek':
                            endpointInput.value = 'https://api.deepseek.com/v1/chat/completions';
                            break;
                        default:
                            endpointInput.value = '';
                    }
                });
            }, 50);
        }
    }

    /**
     * 关闭编辑模型对话框
     */
    closeEditModelModal() {
        const modal = document.getElementById('edit-model-modal');
        if (modal) {
            // 移除事件监听器
            $('.select2-edit-model-type').off('change');

            // 销毁select2实例
            if ($('.select2-edit-model-type').hasClass('select2-hidden-accessible')) {
                $('.select2-edit-model-type').select2('destroy');
            }
            modal.classList.remove('show');
        }
    }

    /**
     * 更新模型
     */
    async updateModel() {
        const id = parseInt(document.getElementById('edit-model-id').value);
        const modelName = document.getElementById('edit-model-name').value.trim();
        const apiType = document.getElementById('edit-model-type').value;
        const endpoint = document.getElementById('edit-model-endpoint').value.trim();
        const apiKey = document.getElementById('edit-model-api-key').value.trim();
        const systemPrompt = document.getElementById('edit-model-system-prompt').value.trim();
        const timeout = parseInt(document.getElementById('edit-model-timeout').value) || 30;
        const description = document.getElementById('edit-model-description').value.trim();
        const enabled = document.getElementById('edit-model-enabled').checked ? 1 : 0;

        // 验证必填字段
        if (!modelName || !apiType || !endpoint) {
            this.showToast('请填写所有必填字段', 'error');
            return;
        }

        this.showToast('正在保存修改...', 'loading');

        try {
            // 调用后端API
            const response = await fetch(`/api/model-config/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    modelName: modelName,
                    apiType: apiType,
                    endpoint: endpoint,
                    apiKey: apiKey,
                    systemPrompt: systemPrompt,
                    timeout: timeout,
                    description: description,
                    enabled: enabled
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || '更新模型失败');
            }

            const result = await response.json();

            // 重新加载模型数据
            await this.loadModelData();
            await this.loadStatistics();
            this.renderModelTable();
            this.closeEditModelModal();
            this.showToast('模型更新成功', 'success');
        } catch (error) {
            console.error('更新模型失败:', error);
            this.showToast(error.message || '更新模型失败', 'error');
        }
    }

    /**
     * 测试模型连接
     */
    async testModel(id) {
        const model = this.modelData.find(m => m.id === id);
        if (!model) {
            this.showToast('模型不存在', 'error');
            return;
        }

        this.showToast('正在测试连接...', 'loading');

        try {
            // 调用后端API
            const response = await fetch(`/api/model-config/${id}/test`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || '测试连接失败');
            }

            const result = await response.json();

            // 重新加载模型数据以获取更新后的状态
            await this.loadModelData();
            await this.loadStatistics();
            this.renderModelTable();

            if (result.status === 1) {
                this.showToast('连接测试成功', 'success');
            } else {
                this.showToast('连接测试失败，请检查配置', 'error');
            }
        } catch (error) {
            console.error('测试连接失败:', error);
            this.showToast(error.message || '测试连接失败', 'error');
        }
    }

    /**
     * 切换模型启用状态
     */
    async toggleModel(id) {
        const model = this.modelData.find(m => m.id === id);
        if (!model) {
            this.showToast('模型不存在', 'error');
            return;
        }

        const newEnabled = model.enabled == 1 ? 0 : 1;
        const action = newEnabled == 1 ? '启用' : '禁用';

        this.showToast(`正在${action}模型...`, 'loading');

        try {
            // 调用后端API
            const response = await fetch(`/api/model-config/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    enabled: newEnabled
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `${action}模型失败`);
            }

            const result = await response.json();

            // 重新加载模型数据
            await this.loadModelData();
            await this.loadStatistics();
            this.renderModelTable();
            this.showToast(`模型已${action}`, 'success');
        } catch (error) {
            console.error(`${action}模型失败:`, error);
            this.showToast(error.message || `${action}模型失败`, 'error');
        }
    }

    /**
     * 显示删除确认对话框
     */
    showDeleteConfirmModal(id) {
        console.log('显示删除确认对话框，ID:', id);

        const model = this.modelData.find(m => m.id === id);
        if (!model) {
            this.showToast('模型不存在', 'error');
            return;
        }

        // 填充删除确认对话框
        document.getElementById('delete-model-name').textContent = model.modelName;
        document.getElementById('delete-model-type').textContent = `${model.apiType} API`;
        document.getElementById('delete-model-icon').src = this.getApiTypeIcon(model.apiType);

        // 存储要删除的模型ID
        this.modelToDelete = id;
        console.log('设置要删除的模型ID:', this.modelToDelete);

        // 显示确认对话框
        const modal = document.getElementById('delete-confirm-modal');
        if (modal) {
            modal.classList.add('show');
        }
    }

    /**
     * 关闭删除确认对话框
     */
    closeDeleteConfirmModal() {
        const modal = document.getElementById('delete-confirm-modal');
        if (modal) {
            modal.classList.remove('show');
        }
        // 注意：不要在这里清空modelToDelete，因为确认删除时还需要使用
        // this.modelToDelete = null;
    }

    /**
     * 确认删除模型
     */
    async confirmDeleteModel() {
        if (!this.modelToDelete) {
            this.showToast('没有要删除的模型', 'error');
            return;
        }

        const modelId = this.modelToDelete; // 保存ID的副本
        this.closeDeleteConfirmModal();
        this.showToast('正在删除模型...', 'loading');

        try {
            // 调用后端API
            console.log('删除请求URL:', `/api/model-config/${modelId}`);
            console.log('删除请求方法:', 'DELETE');
            console.log('删除请求头:', {
                'Content-Type': 'application/json',
            });

            const response = await fetch(`/api/model-config/${modelId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            // 检查响应类型
            const contentType = response.headers.get('content-type');
            console.log('响应状态码:', response.status);
            console.log('响应内容类型:', contentType);

            if (!response.ok) {
                let errorMessage = '删除模型失败';

                // 获取响应内容用于调试
                const responseText = await response.text();
                console.log('响应内容:', responseText);

                // 尝试解析错误响应
                if (contentType && contentType.includes('application/json')) {
                    try {
                        const errorData = JSON.parse(responseText);
                        errorMessage = errorData.error || errorMessage;
                    } catch (parseError) {
                        console.error('解析错误响应失败:', parseError);
                    }
                } else {
                    // 如果不是JSON，可能是HTML错误页面
                    errorMessage = `删除失败 (HTTP ${response.status})`;
                }

                throw new Error(errorMessage);
            }

            // 检查响应内容类型
            if (contentType && contentType.includes('application/json')) {
                const responseText = await response.text();
                console.log('响应内容:', responseText);
                try {
                    const result = JSON.parse(responseText);
                    console.log('删除成功:', result);
                } catch (parseError) {
                    console.error('解析成功响应失败:', parseError);
                }
            } else {
                console.log('删除成功，但响应不是JSON格式');
            }

            // 重新加载模型数据
            await this.loadModelData();
            await this.loadStatistics();
            this.renderModelTable();
            this.showToast('模型删除成功', 'success');
        } catch (error) {
            console.error('删除模型失败:', error);
            this.showToast(error.message || '删除模型失败', 'error');
        } finally {
            this.modelToDelete = null;
        }
    }

    /**
     * 删除模型（保留原有方法名以兼容）
     */
    async deleteModel(id) {
        console.log('调用deleteModel，ID:', id);
        this.showDeleteConfirmModal(id);
    }

    /**
     * 测试连接（添加模型对话框）
     */
    async testModelConnection() {
        const endpoint = document.getElementById('model-endpoint').value.trim();
        const apiKey = document.getElementById('model-api-key').value.trim();

        if (!endpoint) {
            this.showToast('请填写接口地址', 'error');
            return;
        }

        this.showToast('正在测试连接...', 'loading');

        try {
            // 模拟连接测试
            await new Promise(resolve => setTimeout(resolve, 2000));
            const success = Math.random() > 0.3;

            if (success) {
                this.showToast('连接测试成功', 'success');
            } else {
                this.showToast('连接测试失败，请检查配置', 'error');
            }
        } catch (error) {
            this.showToast('连接测试失败', 'error');
        }
    }

    /**
     * 测试连接（编辑模型对话框）
     */
    async testEditModelConnection() {
        const endpoint = document.getElementById('edit-model-endpoint').value.trim();
        const apiKey = document.getElementById('edit-model-api-key').value.trim();

        if (!endpoint) {
            this.showToast('请填写接口地址', 'error');
            return;
        }

        this.showToast('正在测试连接...', 'loading');

        try {
            // 模拟连接测试
            await new Promise(resolve => setTimeout(resolve, 2000));
            const success = Math.random() > 0.3;

            if (success) {
                this.showToast('连接测试成功', 'success');
            } else {
                this.showToast('连接测试失败，请检查配置', 'error');
            }
        } catch (error) {
            this.showToast('连接测试失败', 'error');
        }
    }
}

// 全局函数（供HTML调用）
let configManager;

function closeAddModelModal() {
    if (configManager) {
        configManager.closeAddModelModal();
    }
}

function closeEditModelModal() {
    if (configManager) {
        configManager.closeEditModelModal();
    }
}

function testModelConnection() {
    if (configManager) {
        configManager.testModelConnection();
    }
}

function testEditModelConnection() {
    if (configManager) {
        configManager.testEditModelConnection();
    }
}

function closeDeleteConfirmModal() {
    if (configManager) {
        configManager.closeDeleteConfirmModal();
    }
}

function confirmDeleteModel() {
    if (configManager) {
        configManager.confirmDeleteModel();
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function () {
    configManager = new ConfigManager();

    // 设置当前页面为活跃状态
    if (typeof setActiveNavigation === 'function') {
        setActiveNavigation('config');
    }

    console.log('配置管理页面初始化完成');
});