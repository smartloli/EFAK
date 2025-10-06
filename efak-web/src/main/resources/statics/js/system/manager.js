// Main Module 逻辑

window.ManagerModule = {
    // 配置参数
    config: {
        pageSize: 5,
        refreshInterval: 30000, // 30秒自动刷新
        apiEndpoints: {
            clusterList: '/api/manager/cluster/list',
            clusterStats: '/api/manager/cluster/stats',
            clusterCreate: '/api/manager/cluster/create'
        }
    },

    // 页面状态
    state: {
        page: 1,
        pageSize: 5,
        search: '',
        environment: '',
        sortField: 'updatedAt',
        sortOrder: 'DESC',
        totalPages: 1,
        totalRecords: 0,
        isLoading: false
    },

    init: function () {
        console.log('ManagerModule 初始化开始');
        const self = this;
        this.bindEvents();
        this.loadClusterStats();
        this.load();
        this.initSelect2();

        // 绑定安全配置JSON实时校验
        const securityJsonElement = document.getElementById('securityJson');
        if (securityJsonElement) {
            securityJsonElement.addEventListener('input', function () {
                self.validateJSON();
            });
        }

        console.log('ManagerModule 初始化完成');
    },

    // 绑定事件
    bindEvents: function () {
        const self = this;

        // 创建集群按钮
        $('#add-cluster-btn, #btn-create-cluster').off('click').on('click', function () {
            console.log('打开创建集群对话框');
            if (typeof openCreateClusterModal === 'function') {
                openCreateClusterModal();
            }
        });

        // 查询按钮
        $('#btn-query').off('click').on('click', function () {
            console.log('执行查询');
            self.state.search = $('#cluster_search').val() || '';
            self.state.environment = $('#sel-environment').val() || '';
            self.state.page = 1;
            self.load();
        });

        // 搜索框回车事件
        $('#cluster_search').off('keypress').on('keypress', function (e) {
            if (e.which === 13) {
                self.state.search = $('#cluster_search').val() || '';
                self.state.page = 1;
                self.load();
            }
        });

        // 输入内容时，防抖实时搜索
        $('#cluster_search').off('input').on('input', self.debounce(function () {
            self.state.search = $('#cluster_search').val() || '';
            self.state.page = 1;
            self.load();
        }, 300));

        // 筛选条件变化事件
        $('#sel-environment').off('change').on('change', function () {
            self.state.page = 1;
            self.load();
        });

        // 分页按钮事件
        document.getElementById('prev-page').addEventListener('click', () => {
            if (self.state.page > 1) {
                self.state.page--;
                self.load();
            }
        });

        document.getElementById('next-page').addEventListener('click', () => {
            if (self.state.page < self.state.totalPages) {
                self.state.page++;
                self.load();
            }
        });

        // 用户下拉菜单交互
        this.initUserDropdown();
    },

    // 初始化Select2组件
    initSelect2: function () {
        // 初始化环境类型筛选器（页面顶部的筛选下拉框）
        if (typeof $ !== 'undefined' && $('#sel-environment').length) {
            $('#sel-environment').select2({
                placeholder: '选择环境类型',
                allowClear: true,
                width: '200px'
            });
        }
    },

    // 初始化创建集群模态框中的Select2组件
    initCreateClusterSelect2: function () {
        if (typeof $ !== 'undefined') {
            const clusterEnvElement = $('#cluster-environment');
            if (clusterEnvElement.length && !clusterEnvElement.hasClass('select2-hidden-accessible')) {
                clusterEnvElement.select2({
                    dropdownParent: $('#create-cluster-modal'),
                    minimumResultsForSearch: Infinity,
                    width: '100%',
                    dropdownCssClass: 'topic-icon-dropdown',
                    templateResult: this.formatEnvironmentOption,
                    templateSelection: this.formatEnvironmentSelection,
                    escapeMarkup: function (m) {
                        return m;
                    }
                });
            }
        }
    },

    // 初始化用户下拉菜单
    initUserDropdown: function () {
        const userMenuButton = document.getElementById('user-menu-button');
        const userDropdown = document.getElementById('user-dropdown');
        const userMenuArrow = document.getElementById('user-menu-arrow');

        // 新增：直接绑定下拉菜单项按钮点击事件，兼容 button 结构
        const profileBtn = document.getElementById('profile-btn');
        const changePasswordBtn = document.getElementById('change-password-btn');
        if (profileBtn) {
            profileBtn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                hideDropdown();
                if (window.ManagerModule && typeof window.ManagerModule.openProfileModal === 'function') {
                    window.ManagerModule.openProfileModal();
                }
            });
        }
        if (changePasswordBtn) {
            changePasswordBtn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                hideDropdown();
                if (window.ManagerModule && typeof window.ManagerModule.openChangePasswordModal === 'function') {
                    window.ManagerModule.openChangePasswordModal();
                }
            });
        }

        if (!userMenuButton || !userDropdown) {
            console.log('用户菜单元素未找到');
            return;
        }

        let isDropdownOpen = false;
        let hoverTimeout;

        // 鼠标进入用户菜单按钮
        userMenuButton.addEventListener('mouseenter', function () {
            clearTimeout(hoverTimeout);
            if (!isDropdownOpen) {
                showDropdown();
            }
        });

        // 鼠标离开用户菜单按钮
        userMenuButton.addEventListener('mouseleave', function () {
            hoverTimeout = setTimeout(function () {
                if (isDropdownOpen && !userDropdown.matches(':hover')) {
                    hideDropdown();
                }
            }, 200);
        });

        // 鼠标进入下拉菜单
        userDropdown.addEventListener('mouseenter', function () {
            clearTimeout(hoverTimeout);
        });

        // 鼠标离开下拉菜单
        userDropdown.addEventListener('mouseleave', function () {
            hoverTimeout = setTimeout(function () {
                hideDropdown();
            }, 200);
        });

        // 点击用户菜单按钮
        userMenuButton.addEventListener('click', function (e) {
            e.stopPropagation();
            if (isDropdownOpen) {
                hideDropdown();
            } else {
                showDropdown();
            }
        });

        // 点击菜单项时处理相应功能（仅拦截带 data-action 的元素）
        userDropdown.addEventListener('click', function (e) {
            const actionEl = e.target.closest('[data-action]');
            if (!actionEl) {
                // 不拦截普通链接（例如退出登录）
                return;
            }
            e.preventDefault();
            e.stopPropagation();
            const action = actionEl.getAttribute('data-action');

            // 隐藏用户菜单
            hideDropdown();

            // 根据不同的操作执行相应功能
            switch (action) {
                case 'profile':
                    if (window.ManagerModule && typeof window.ManagerModule.openProfileModal === 'function') {
                        window.ManagerModule.openProfileModal();
                    }
                    break;
                case 'change-password':
                    if (window.ManagerModule && typeof window.ManagerModule.openChangePasswordModal === 'function') {
                        window.ManagerModule.openChangePasswordModal();
                    }
                    break;
                case 'logout':
                    if (window.ManagerModule && typeof window.ManagerModule.logout === 'function') {
                        window.ManagerModule.logout();
                    }
                    break;
            }
        });

        // 点击页面其他地方关闭下拉菜单
        document.addEventListener('click', function (e) {
            if (!userMenuButton.contains(e.target) && !userDropdown.contains(e.target)) {
                hideDropdown();
            }
        });

        function showDropdown() {
            isDropdownOpen = true;
            userDropdown.classList.remove('hidden');
            userDropdown.style.opacity = '1';
            userDropdown.style.transform = 'scale(1) translateY(0)';
            if (userMenuArrow) {
                userMenuArrow.style.transform = 'rotate(180deg)';
            }
        }

        function hideDropdown() {
            isDropdownOpen = false;
            userDropdown.style.opacity = '0';
            userDropdown.style.transform = 'scale(0.95) translateY(-10px)';
            if (userMenuArrow) {
                userMenuArrow.style.transform = 'rotate(0deg)';
            }
            setTimeout(function () {
                if (!isDropdownOpen) {
                    userDropdown.classList.add('hidden');
                }
            }, 200);
        }

        // 初始化模态框事件
        this.initModalEvents();
    },

    // 初始化模态框事件
    initModalEvents: function () {
        // 个人信息模态框事件
        const profileModal = document.getElementById('profile-modal');
        const closeProfileBtn = document.getElementById('close-profile-modal');

        if (closeProfileBtn) {
            closeProfileBtn.addEventListener('click', () => {
                this.closeProfileModal();
            });
        }

        if (profileModal) {
            profileModal.addEventListener('click', (e) => {
                if (e.target === profileModal) {
                    this.closeProfileModal();
                }
            });
        }

        // 修改密码模态框事件
        const passwordModal = document.getElementById('change-password-modal');
        const closePasswordBtn = document.getElementById('close-password-modal');
        const cancelPasswordBtn = document.getElementById('cancel-password-change');
        const passwordForm = document.getElementById('change-password-form');

        if (closePasswordBtn) {
            closePasswordBtn.addEventListener('click', () => {
                this.closeChangePasswordModal();
            });
        }

        if (cancelPasswordBtn) {
            cancelPasswordBtn.addEventListener('click', () => {
                this.closeChangePasswordModal();
            });
        }

        if (passwordModal) {
            passwordModal.addEventListener('click', (e) => {
                if (e.target === passwordModal) {
                    this.closeChangePasswordModal();
                }
            });
        }

        if (passwordForm) {
            passwordForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitChangePassword();
            });
        }

        // 创建集群模态框事件（点击遮罩关闭）
        const createClusterModal = document.getElementById('create-cluster-modal');
        if (createClusterModal) {
            createClusterModal.addEventListener('click', (e) => {
                if (e.target === createClusterModal) {
                    this.closeCreateClusterModal();
                }
            });

            // 关闭按钮（右上角 X）
            const createCloseBtn = createClusterModal.querySelector('.modal-close');
            if (createCloseBtn) {
                createCloseBtn.addEventListener('click', () => this.closeCreateClusterModal());
            }

            // 底部取消按钮
            const createCancelBtn = createClusterModal.querySelector('.modal-footer .btn-secondary');
            if (createCancelBtn) {
                createCancelBtn.addEventListener('click', () => this.closeCreateClusterModal());
            }
        }

        // 编辑集群模态框事件（点击遮罩或按钮关闭）
        const editClusterModal = document.getElementById('editClusterModal');
        if (editClusterModal) {
            // 点击遮罩关闭
            editClusterModal.addEventListener('click', (e) => {
                if (e.target === editClusterModal) {
                    this.closeEditClusterModal();
                }
            });
            // 右上角关闭按钮
            const editCloseBtn = editClusterModal.querySelector('.modal-close');
            if (editCloseBtn) {
                editCloseBtn.addEventListener('click', () => this.closeEditClusterModal());
            }
            // 底部取消按钮
            const editCancelBtn = editClusterModal.querySelector('.modal-footer .btn-secondary');
            if (editCancelBtn) {
                editCancelBtn.addEventListener('click', () => this.closeEditClusterModal());
            }
        }

        // ESC键关闭模态框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeProfileModal();
                this.closeChangePasswordModal();
                this.closeCreateClusterModal();
                this.closeEditClusterModal();
            }
        });
    },

    // 打开个人信息模态框
    openProfileModal: function () {
        const modal = document.getElementById('profile-modal');
        if (modal) {
            modal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        }
    },

    // 关闭个人信息模态框
    closeProfileModal: function () {
        const modal = document.getElementById('profile-modal');
        if (modal) {
            modal.classList.add('hidden');
            document.body.style.overflow = '';
        }
    },

    // 打开修改密码模态框
    openChangePasswordModal: function () {
        const modal = document.getElementById('change-password-modal');
        if (modal) {
            modal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
            // 清空表单
            const form = document.getElementById('change-password-form');
            if (form) {
                form.reset();
            }
        }
    },

    // 关闭修改密码模态框
    closeChangePasswordModal: function () {
        const modal = document.getElementById('change-password-modal');
        if (modal) {
            modal.classList.add('hidden');
            document.body.style.overflow = '';
        }
    },

    // 切换密码显示/隐藏
    togglePasswordVisibility: function (inputId) {
        const input = document.getElementById(inputId);
        const button = input.nextElementSibling;
        const icon = button.querySelector('i');

        if (input.type === 'password') {
            input.type = 'text';
            icon.className = 'fa fa-eye-slash';
        } else {
            input.type = 'password';
            icon.className = 'fa fa-eye';
        }
    },

    // 提交修改密码
    submitChangePassword: function () {
        const form = document.getElementById('change-password-form');
        const formData = new FormData(form);

        const currentPassword = formData.get('currentPassword');
        const newPassword = formData.get('newPassword');
        const confirmPassword = formData.get('confirmPassword');

        // 表单验证
        if (!currentPassword || !newPassword || !confirmPassword) {
            this.showToast('请填写所有密码字段', 'error');
            return;
        }

        if (newPassword !== confirmPassword) {
            this.showToast('新密码和确认密码不一致', 'error');
            return;
        }

        if (newPassword.length < 8) {
            this.showToast('新密码长度至少8位', 'error');
            return;
        }

        if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(newPassword)) {
            this.showToast('新密码必须包含字母和数字', 'error');
            return;
        }

        // 提交到后端
        const submitData = {
            currentPassword: currentPassword,
            newPassword: newPassword
        };

        // 这里应该调用实际的API
        // 暂时模拟成功响应
        setTimeout(() => {
            this.showToast('密码修改成功', 'success');
            this.closeChangePasswordModal();
        }, 1000);

        // 实际的API调用代码（需要根据后端接口调整）
        /*
        fetch('/api/user/change-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(submitData)
        })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            this.showToast('密码修改成功', 'success');
            this.closeChangePasswordModal();
          } else {
            this.showToast(data.message || '密码修改失败', 'error');
          }
        })
        .catch(error => {
          console.error('Error:', error);
          this.showToast('网络错误，请稍后重试', 'error');
        });
        */
    },

    // 退出登录
    logout: function () {
        if (confirm('确定要退出登录吗？')) {
            // 这里应该调用实际的退出登录API
            window.location.href = '/login';
        }
    },

    // 加载集群统计数据
    loadClusterStats: function () {
        const self = this;

        $.ajax({
            url: self.config.apiEndpoints.clusterStats,
            type: 'GET',
            timeout: 10000,
            success: function (response) {
                console.log('获取集群统计信息成功:', response);
                // 检查响应格式，支持两种格式：直接数据对象或包含code和data的响应
                if (response && typeof response === 'object') {
                    let statsData;
                    if (response.code === 200 && response.data) {
                        // 标准响应格式：{code: 200, data: {...}}
                        statsData = response.data;
                    } else if (response.totalClusters !== undefined) {
                        // 直接数据格式：{totalClusters: 1, onlineClusters: 1, ...}
                        statsData = response;
                    } else {
                        console.warn('获取集群统计信息失败:', response.message || '数据格式不正确');
                        statsData = {
                            totalClusters: 0,
                            onlineClusters: 0,
                            offlineClusters: 0,
                            avgAvailability: 0
                        };
                    }
                    self.updateClusterStats(statsData);
                } else {
                    console.warn('获取集群统计信息失败: 响应数据为空或格式错误');
                    self.updateClusterStats({
                        totalClusters: 0,
                        onlineClusters: 0,
                        offlineClusters: 0,
                        avgAvailability: 0
                    });
                }
            },
            error: function (xhr, status, error) {
                console.error('请求集群统计信息失败:', error);
                self.updateClusterStats({
                    totalClusters: 0,
                    onlineClusters: 0,
                    offlineClusters: 0,
                    avgAvailability: 0
                });
            }
        });
    },

    // 更新集群统计信息
    updateClusterStats: function (stats) {
        $('#totalClusters').text(stats.totalClusters || 0);
        $('#onlineClusters').text(stats.onlineClusters || 0);
        $('#offlineClusters').text(stats.offlineClusters || 0);
        $('#avgAvailability').text((stats.avgAvailability || 0) + '%');

        // 更新页面上的显示卡片
        const totalElement = document.querySelector('.grid .bg-white:nth-child(1) h3');
        const healthyElement = document.querySelector('.grid .bg-white:nth-child(2) h3');
        const warningElement = document.querySelector('.grid .bg-white:nth-child(3) h3');
        const nodesElement = document.querySelector('.grid .bg-white:nth-child(4) h3');

        if (totalElement) totalElement.textContent = stats.totalClusters || 0;
        if (healthyElement) healthyElement.textContent = stats.onlineClusters || 0;
        if (warningElement) warningElement.textContent = stats.offlineClusters || 0;

        // 更新进度条和百分比
        const progressText = document.querySelector('.grid .bg-white:nth-child(2) .text-success');
        if (progressText && stats.totalClusters > 0) {
            const healthRate = Math.round((stats.onlineClusters / stats.totalClusters) * 100);
            progressText.innerHTML = `<i class="fa fa-check-circle mr-1"></i> ${healthRate}% 健康率`;
        }
    },

    load: function () {
        const self = this;

        if (self.state.isLoading) {
            console.log('正在加载中，跳过重复请求');
            return;
        }

        self.state.isLoading = true;
        self.showLoading();

        $.ajax({
            url: '/api/manager/cluster/list',
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            timeout: 15000,
            data: JSON.stringify(self.state),
            success: function (resp) {
                self.state.isLoading = false;
                self.hideLoading();
                console.log('数据加载成功:', resp);
                self.render(resp);
            },
            error: function (xhr, status, error) {
                self.state.isLoading = false;
                self.hideLoading();
                console.error('请求失败:', {status: status, error: error, xhr: xhr});

                let errorMsg = '加载失败';
                if (status === 'timeout') {
                    errorMsg = '请求超时，请检查网络连接';
                } else if (xhr.status === 404) {
                    errorMsg = 'API接口不存在';
                } else if (xhr.status === 500) {
                    errorMsg = '服务器内部错误';
                } else if (xhr.status === 0) {
                    errorMsg = '网络连接失败';
                } else {
                    errorMsg = '加载失败: ' + (xhr.responseText || xhr.status);
                }

                self.showError(errorMsg);

                // 显示空状态
                $('#cluster-tbody').html('<tr><td class="text-center" colspan="9" style="padding: 40px; color: #6b7280;"><div style="display:flex; flex-direction:column; align-items:center; gap:8px;"><i class="fas fa-exclamation-triangle" style="font-size: 24px;"></i><div>网络连接失败，请稍后重试</div></div></td></tr>');
            }
        });
    },

    // 环境类型映射函数
    getEnvironmentTypeDisplay: function (clusterType) {
        const typeMap = {
            'test': {text: '测试集群', class: 'env-test'},
            'pre': {text: '预发环境', class: 'env-pre'},
            'prd': {text: '生产环境', class: 'env-prd'}
        };
        return typeMap[clusterType] || {text: clusterType || '-', class: 'env-default'};
    },

    // 根据可用度计算状态
    getStatusByAvailability: function (availability) {
        const avail = parseFloat(availability || 0);
        if (avail > 80) {
            return {text: '正常', class: 'status-normal'};
        } else if (avail > 60) {
            return {text: '警告', class: 'status-warning'};
        } else {
            return {text: '异常', class: 'status-error'};
        }
    },

    render: function (resp) {
        const list = (resp && resp.clusters) || [];

        // 更新分页信息
        this.state.totalPages = resp.totalPages || Math.ceil((resp.total || 0) / this.state.pageSize) || 1;
        this.state.totalRecords = resp.total || 0;

        // 如果没有数据，设置默认值
        if (!list.length && !resp.total) {
            this.state.totalRecords = 0;
            this.state.totalPages = 1;
            this.updatePagination();
            $('#cluster-tbody').html('<tr><td class="text-center" colspan="9" style="padding: 40px; color: #6b7280;"><div style="display:flex; flex-direction:column; align-items:center; gap:8px;"><i class="fas fa-inbox" style="font-size: 24px;"></i><div>暂无集群数据</div></div></td></tr>');
            return;
        }

        this.updatePagination();

        if (!list.length) {
            $('#cluster-tbody').html('<tr><td class="text-center" colspan="9" style="padding: 40px; color: #6b7280;"><div style="display:flex; flex-direction:column; align-items:center; gap:8px;"><i class="fas fa-inbox" style="font-size: 24px;"></i><div>暂无集群数据</div></div></td></tr>');
            return;
        }

        const rows = list.map(function (c, index) {
            const online = c.onlineNodes != null ? c.onlineNodes : 0;
            const total = c.totalNodes != null ? c.totalNodes : 0;
            const avail = c.availability != null ? (c.availability + '%') : (total > 0 ? (Math.round(online * 10000 / total) / 100 + '%') : '-');

            // 节点状态样式
            const nodeStatusClass = online === total ? 'text-success' : 'text-warning';

            // 可用度样式
            const availability = parseFloat(c.availability || 0);
            const availabilityClass = availability >= 95 ? 'text-success' : availability >= 80 ? 'text-warning' : 'text-danger';

            // 使用新的环境类型映射
            const envType = window.ManagerModule.getEnvironmentTypeDisplay(c.clusterType);

            // 使用新的状态计算逻辑
            const statusInfo = window.ManagerModule.getStatusByAvailability(c.availability);

            const auth = (c.auth === 'Y') ? '<span class="status-pill bg-green-100 text-green-800"><i class="fa fa-lock mr-1"></i>已开启</span>' : (c.auth === 'N' ? '<span class="status-pill bg-gray-100 text-gray-800"><i class="fa fa-unlock mr-1"></i>未开启</span>' : '-');
            const updated = window.ManagerModule.formatDateTime(c.updatedAt || '');

            // 操作按钮
            const actionButtons = '<div class="flex justify-end space-x-4">' +
                '<button class="text-primary hover:text-primary/80 p-2 rounded-lg hover:bg-primary/10 transition-all duration-200" onclick="ManagerModule.viewCluster(\'' + (c.clusterId || '') + '\')" title="查看详情">' +
                '<i class="fa fa-eye text-lg"></i></button>' +
                '<button class="text-info hover:text-info/80 p-2 rounded-lg hover:bg-info/10 transition-all duration-200" onclick="ManagerModule.editCluster(\'' + (c.clusterId || '') + '\')" title="编辑集群">' +
                '<i class="fa fa-pencil text-lg"></i></button>' +
                '<button class="text-danger hover:text-danger/80 p-2 rounded-lg hover:bg-danger/10 transition-all duration-200" onclick="ManagerModule.deleteCluster(\'' + (c.clusterId || '') + '\')" title="删除集群">' +
                '<i class="fa fa-trash text-lg"></i></button>' +
                '</div>';

            return '<tr data-cluster-id="' + (c.clusterId || '') + '" class="hover:bg-gray-50 transition-colors">' +
                '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">' + (c.clusterId || '-') + '</td>' +
                '<td class="px-6 py-4 whitespace-nowrap">' +
                '<div class="flex items-center">' +
                '<div class="w-8 h-8 rounded-md bg-primary/10 flex items-center justify-center mr-3">' +
                '<i class="fa fa-database text-primary"></i></div>' +
                '<div class="text-sm font-medium text-gray-900">' + (c.name || '-') + '</div></div></td>' +
                '<td class="px-6 py-4 whitespace-nowrap"><span class="status-pill bg-blue-100 text-blue-800">' + envType.text + '</span></td>' +
                '<td class="px-6 py-4 whitespace-nowrap text-sm ' + nodeStatusClass + '">' + online + '/' + total + '</td>' +
                '<td class="px-6 py-4 whitespace-nowrap">' +
                '<div class="flex items-center">' +
                '<div class="w-full bg-gray-200 rounded-full h-2 mr-2">' +
                '<div class="' + (availability >= 90 ? 'bg-success' : availability >= 70 ? 'bg-warning' : 'bg-danger') + ' h-2 rounded-full" style="width: ' + availability + '%"></div>' +
                '</div><span class="text-sm text-gray-500">' + avail + '</span></div></td>' +
                '<td class="px-6 py-4 whitespace-nowrap"><span class="status-pill ' + (availability >= 90 ? 'bg-green-100 text-green-800 status-normal' : availability >= 70 ? 'bg-yellow-100 text-yellow-800 status-warning' : 'bg-red-100 text-red-800 status-error') + '">' +
                '<i class="fa fa-' + (availability >= 90 ? 'check-circle' : availability >= 70 ? 'exclamation-circle' : 'times-circle') + ' mr-1"></i>' + statusInfo.text + '</span></td>' +
                '<td class="px-6 py-4 whitespace-nowrap">' + auth + '</td>' +
                '<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">' + updated + '</td>' +
                '<td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">' + actionButtons + '</td>' +
                '</tr>';
        }).join('');

        $('#cluster-tbody').html(rows);
    },

    // 更新分页信息
    updatePagination: function () {
        const startItem = (this.state.page - 1) * this.state.pageSize + 1;
        const endItem = Math.min(this.state.page * this.state.pageSize, this.state.totalRecords);

        document.getElementById('start-item').textContent = this.state.totalRecords > 0 ? startItem : 0;
        document.getElementById('end-item').textContent = endItem;
        document.getElementById('total-items').textContent = this.state.totalRecords;

        const prevBtn = document.getElementById('prev-page');
        const nextBtn = document.getElementById('next-page');

        prevBtn.disabled = this.state.page === 1;
        nextBtn.disabled = this.state.page === this.state.totalPages || this.state.totalPages === 0;

        const pageNumbersContainer = document.getElementById('page-numbers');
        pageNumbersContainer.innerHTML = '';

        if (this.state.totalPages <= 7) {
            for (let i = 1; i <= this.state.totalPages; i++) {
                pageNumbersContainer.appendChild(this.createPageButton(i, i === this.state.page));
            }
        } else {
            pageNumbersContainer.appendChild(this.createPageButton(1, this.state.page === 1));

            if (this.state.page > 3) {
                pageNumbersContainer.appendChild(this.createEllipsis());
            }

            const startPage = Math.max(2, this.state.page - 1);
            const endPage = Math.min(this.state.totalPages - 1, this.state.page + 1);

            for (let i = startPage; i <= endPage; i++) {
                pageNumbersContainer.appendChild(this.createPageButton(i, i === this.state.page));
            }

            if (this.state.page < this.state.totalPages - 2) {
                pageNumbersContainer.appendChild(this.createEllipsis());
            }

            pageNumbersContainer.appendChild(this.createPageButton(this.state.totalPages, this.state.page === this.state.totalPages));
        }
    },

    // 创建页码按钮
    createPageButton: function (pageNum, isActive) {
        const button = document.createElement('button');
        button.textContent = pageNum;
        button.className = `px-3 py-1 text-sm border border-gray-300 rounded-lg transition-colors ${isActive
            ? 'bg-primary text-white border-primary'
            : 'hover:bg-gray-50'
        }`;

        if (!isActive) {
            const self = this;
            button.addEventListener('click', () => {
                self.state.page = pageNum;
                self.load();
            });
        }

        return button;
    },

    // 创建省略号
    createEllipsis: function () {
        const span = document.createElement('span');
        span.textContent = '...';
        span.className = 'px-2 py-1 text-sm text-gray-500';
        return span;
    },

    // 格式化日期时间
    formatDateTime: function (dateTimeStr) {
        if (!dateTimeStr) return '-';

        try {
            const date = new Date(dateTimeStr);
            if (isNaN(date.getTime())) return dateTimeStr;

            const now = new Date();
            const diff = now.getTime() - date.getTime();
            const minutes = Math.floor(diff / 60000);

            if (minutes < 1) return '刚刚';
            if (minutes < 60) return minutes + '分钟前';
            if (minutes < 1440) return Math.floor(minutes / 60) + '小时前';
            if (minutes < 10080) return Math.floor(minutes / 1440) + '天前';

            return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
        } catch (e) {
            return dateTimeStr;
        }
    },

    // 显示加载状态
    showLoading: function () {
        $('#cluster-tbody').html('<tr><td class="text-center" colspan="9" style="padding: 40px;"><div style="display:flex; flex-direction:column; align-items:center; gap:8px;"><i class="fas fa-spinner fa-spin" style="font-size: 24px;"></i><div>加载中...</div></div></td></tr>');
        $('#btn-refresh').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 加载中...');
    },

    // 隐藏加载状态
    hideLoading: function () {
        $('#btn-refresh').prop('disabled', false).html('<i class="fas fa-sync-alt"></i> 刷新数据');
    },

    // 通用防抖函数，防止频繁请求接口
    debounce: function (func, wait) {
        var timeout;
        return function () {
            var context = this, args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(function () {
                func.apply(context, args);
            }, wait);
        };
    },

    // 显示错误信息
    showError: function (message) {
        $('#cluster-tbody').html('<tr><td class="text-center text-danger" colspan="9" style="padding: 40px;"><div style="display:flex; flex-direction:column; align-items:center; gap:8px;"><i class="fas fa-exclamation-triangle" style="font-size: 24px;"></i><div>' + message + '</div></div></td></tr>');
    },

    // 编辑集群
    editCluster: function (clusterId) {
        console.log('编辑集群:', clusterId);
        // 获取集群详细信息
        $.ajax({
            url: '/api/manager/cluster/' + clusterId,
            type: 'GET',
            success: function (response) {
                if (response && response.code === 200) {
                    window.ManagerModule.openEditClusterModal(response.data);
                } else {
                    window.ManagerModule.showToast('获取集群信息失败', 'error', '错误', 4000);
                }
            },
            error: function () {
                window.ManagerModule.showToast('网络错误，请稍后重试', 'error', '获取集群信息失败', 4000);
            }
        });
    },

    // 查看集群详情
    viewCluster: function (clusterId) {
        window.location.href = `/cluster?cid=${clusterId}`;
    },

    // 删除集群
    deleteCluster: function (clusterId) {
        const self = this;
        const row = $(`tr[data-cluster-id="${clusterId}"]`);
        const clusterName = row.find('td:nth-child(2) .text-gray-900').text() || '未知集群';

        // 显示美化的确认对话框
        this.showDeleteConfirmModal({
            title: '删除集群确认',
            message: `确定要删除集群 "${clusterName}" (${clusterId}) 吗？`,
            description: '此操作将同时删除集群下的所有 Broker 节点信息，且不可恢复！',
            confirmText: '确认删除',
            cancelText: '取消',
            onConfirm: function () {
                self.performDeleteCluster(clusterId, clusterName);
            }
        });
    },

    // 执行删除集群操作
    performDeleteCluster: function (clusterId, clusterName) {
        const self = this;

        // 显示删除进度
        this.showToast('正在删除集群，请稍候...', 'info', '删除中', 3000);

        $.ajax({
            url: `/api/manager/cluster/delete/by-cluster-id/${clusterId}`,
            type: 'DELETE',
            timeout: 30000, // 30秒超时
            success: function (response) {
                if (response && response.success) {
                    self.showToast(
                        `集群 "${clusterName}" 及其相关节点已成功删除`,
                        'success',
                        '删除成功'
                    );
                    // 重新加载数据
                    self.load();
                    self.loadClusterStats();
                } else {
                    self.showToast(
                        response.message || '删除失败，请稍后重试',
                        'error',
                        '删除失败'
                    );
                }
            },
            error: function (xhr, status, error) {
                console.error('删除集群失败:', {xhr, status, error});
                let errorMsg = '删除失败';
                if (status === 'timeout') {
                    errorMsg = '删除操作超时，请检查网络连接或稍后重试';
                } else if (xhr.status === 403) {
                    errorMsg = '权限不足，无法删除集群';
                } else if (xhr.status === 500) {
                    errorMsg = '服务器内部错误，请联系管理员';
                } else if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMsg = xhr.responseJSON.message;
                }

                self.showToast(errorMsg, 'error', '删除失败', 5000);
            }
        });
    },

    // 销毁模块
    destroy: function () {
        console.log('ManagerModule 已销毁');
    }
};

$(function () {
    if (window.ManagerModule) {
        window.ManagerModule.init();
    }
});


// ===== 创建集群对话框相关方法（由 manager.html 迁移） =====
window.ManagerModule.openCreateClusterModal = function () {
    const modal = document.getElementById('create-cluster-modal');
    if (modal) modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // 延迟初始化 Select2，确保 DOM 完全渲染
    setTimeout(() => {
        window.ManagerModule.initCreateClusterSelect2();
    }, 100);
};

window.ManagerModule.closeCreateClusterModal = function () {
    // 销毁 Select2 实例
    const clusterEnvElement = $('#cluster-environment');
    if (clusterEnvElement.length && clusterEnvElement.hasClass('select2-hidden-accessible')) {
        clusterEnvElement.select2('destroy');
    }

    const modal = document.getElementById('create-cluster-modal');
    if (modal) modal.style.display = 'none';
    document.body.style.overflow = '';
    const form = document.getElementById('create-cluster-form');
    if (form) form.reset();
    this.switchTab('single');
    const sec = document.getElementById('securityEnabled');
    if (sec) sec.checked = false;
    this.toggleSecurityConfig();
    const fileInfo = document.getElementById('fileInfo');
    if (fileInfo) fileInfo.innerHTML = '';
    this.hideModalMessage();
};

window.ManagerModule.showModalMessage = function (message, type) {
    const messageEl = document.getElementById('cluster-modal-message');
    if (messageEl) {
        const cls = type || 'success';
        messageEl.textContent = message;
        if (typeof cls === 'string' && cls.indexOf('text-') === 0) {
            messageEl.className = cls;
        } else {
            messageEl.className = 'modal-message ' + cls;
        }
        messageEl.style.display = 'flex';
    }
};

window.ManagerModule.hideModalMessage = function () {
    const messageEl = document.getElementById('cluster-modal-message');
    if (messageEl) {
        messageEl.style.display = 'none';
        messageEl.className = 'modal-message';
        messageEl.textContent = '';
    }
};

window.ManagerModule.switchTab = function (tabName) {
    // 取消所有active
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    // 添加当前active
    const evt = (typeof window !== 'undefined' && window.event) ? window.event : null;
    if (evt && evt.target) {
        evt.target.classList.add('active');
    } else {
        const firstBtn = document.querySelector('.tab-btn');
        if (firstBtn) firstBtn.classList.add('active');
    }
    const panel = document.getElementById(tabName + 'Tab');
    if (panel) panel.classList.add('active');
};

window.ManagerModule.toggleSecurityConfig = function () {
    const checkbox = document.getElementById('securityEnabled');
    const configDiv = document.getElementById('securityConfig');
    if (!checkbox || !configDiv) return;
    if (checkbox.checked) {
        configDiv.style.display = 'block';
    } else {
        configDiv.style.display = 'none';
        const jsonEl = document.getElementById('securityJson');
        const valEl = document.getElementById('jsonValidation');
        if (jsonEl) jsonEl.value = '';
        if (valEl) valEl.innerHTML = '';
    }
};

window.ManagerModule.validateJSON = function () {
    const jsonInput = document.getElementById('securityJson');
    const validationDiv = document.getElementById('jsonValidation');
    try {
        if (jsonInput && jsonInput.value.trim()) {
            JSON.parse(jsonInput.value);
            if (validationDiv) validationDiv.innerHTML = '<span style="color: green;">✓ JSON格式正确</span>';
            return true;
        }
        if (validationDiv) validationDiv.innerHTML = '';
        return true;
    } catch (e) {
        if (validationDiv) validationDiv.innerHTML = '✗ JSON格式错误: ' + e.message;
        return false;
    }
};

window.ManagerModule.handleFileSelect = function (event) {
    const file = event && event.target && event.target.files ? event.target.files[0] : null;
    if (file) {
        const fileInfo = document.getElementById('fileInfo');
        if (fileInfo) fileInfo.innerHTML = '已选择文件: ' + file.name + ' (' + (file.size / 1024 / 1024).toFixed(2) + ' MB)';
    }
};

window.ManagerModule.handleDragOver = function (event) {
    if (!event) return;
    event.preventDefault();
    if (event.currentTarget) event.currentTarget.classList.add('dragover');
};

window.ManagerModule.handleDragLeave = function (event) {
    if (!event) return;
    if (event.currentTarget) event.currentTarget.classList.remove('dragover');
};

window.ManagerModule.handleFileDrop = function (event) {
    if (!event) return;
    event.preventDefault();
    if (event.currentTarget) event.currentTarget.classList.remove('dragover');
    const files = event.dataTransfer ? event.dataTransfer.files : null;
    if (files && files.length > 0) {
        const excelInput = document.getElementById('excelFile');
        if (excelInput) excelInput.files = files;
        this.handleFileSelect({target: {files: files}});
    }
};

window.ManagerModule.downloadTemplate = function () {
    const templateData = [
        ['Broker ID', '主机IP', 'Kafka端口', 'JMX端口', '安全配置(JSON)'],
        ['1', '192.168.1.100', '9092', '9999', ''],
        ['2', '192.168.1.101', '9092', '9999', '{"security.protocol":"SASL_PLAINTEXT","sasl.mechanism":"PLAIN","sasl.jaas.config":"org.apache.kafka.common.security.plain.PlainLoginModule required username=\\"user\\" password=\\"password\\";"}']
    ];
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet(templateData);
    ws['!cols'] = [{wch: 12}, {wch: 15}, {wch: 12}, {wch: 12}, {wch: 30}];
    XLSX.utils.book_append_sheet(wb, ws, 'Kafka集群模板');
    XLSX.writeFile(wb, 'kafka_cluster_template.xlsx');
};

// 统一获取创建人
window.ManagerModule.getCreator = function () {
    try {
        if (window.currentUser && window.currentUser.username) {
            return window.currentUser.username;
        }
    } catch (e) {
    }
    return 'admin';
};

// 提交单个节点
window.ManagerModule.submitSingleNode = async function () {
    const clusterName = (document.getElementById('cluster-name') || {}).value;
    const environmentType = (document.getElementById('cluster-environment') || {}).value;
    const brokerId = (document.getElementById('brokerId') || {}).value;
    const hostIp = (document.getElementById('hostIp') || {}).value;
    const kafkaPort = (document.getElementById('kafkaPort') || {}).value;
    const jmxPort = (document.getElementById('jmxPort') || {}).value;
    const securityEnabled = (document.getElementById('securityEnabled') || {}).checked;
    const securityJson = (document.getElementById('securityJson') || {}).value;

    if (!clusterName || !environmentType || !brokerId || !hostIp) {
        this.showModalMessage('请填写所有必填字段', 'error');
        return;
    }
    if (securityEnabled && !this.validateJSON()) return;

    const payload = {
        name: clusterName,
        clusterType: environmentType,
        auth: securityEnabled ? 'Y' : 'N',
        authConfig: securityEnabled ? securityJson : null,
        creator: this.getCreator(),
        nodes: [{
            brokerId: parseInt(brokerId, 10),
            hostIp: hostIp,
            port: kafkaPort ? parseInt(kafkaPort, 10) : 9092,
            jmxPort: jmxPort ? parseInt(jmxPort, 10) : 9999
        }]
    };

    try {
        const resp = await fetch(this.config.apiEndpoints.clusterCreate, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!resp.ok) {
            const msg = await resp.text();
            throw new Error(msg || '创建集群失败');
        }
        const result = await resp.json();
        console.log('集群创建成功:', result);
        this.showModalMessage(`单个节点[${brokerId}]录入成功！`, 'text-success');
        if (this.load) {
            this.load();
            this.loadClusterStats();
        }
    } catch (e) {
        console.error('提交单个节点失败:', e);
        this.showModalMessage('提交失败：' + (e.message || '未知错误'), 'error');
    }
};

// 提交批量导入
window.ManagerModule.submitBatchImport = async function () {
    const clusterName = (document.getElementById('cluster-name') || {}).value;
    const environmentType = (document.getElementById('cluster-environment') || {}).value;
    const fileInput = document.getElementById('excelFile');
    const securityEnabled = (document.getElementById('securityEnabled') || {}).checked;
    const securityJson = (document.getElementById('securityJson') || {}).value;

    if (!clusterName || !environmentType) {
        this.showModalMessage('请填写集群名称和环境类型', 'error');
        return;
    }
    if (!fileInput || !fileInput.files || !fileInput.files[0]) {
        this.showModalMessage('请选择要上传的Excel文件', 'error');
        return;
    }
    if (securityEnabled && !this.validateJSON()) return;

    try {
        const file = fileInput.files[0];
        const data = await file.arrayBuffer();
        const workbook = XLSX.read(data, {type: 'array'});
        const firstSheetName = workbook.SheetNames[0];
        const sheet = workbook.Sheets[firstSheetName];
        const rows = XLSX.utils.sheet_to_json(sheet, {header: 1, defval: ''});

        const nodes = [];
        for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            if (!row || row.length === 0) continue;
            const bId = parseInt(row[0], 10);
            const ip = (row[1] || '').toString().trim();
            const kPort = row[2] !== '' ? parseInt(row[2], 10) : 9092;
            const jPort = row[3] !== '' ? parseInt(row[3], 10) : 9999;
            if (!isNaN(bId) && ip) {
                nodes.push({brokerId: bId, hostIp: ip, port: kPort, jmxPort: jPort});
            }
        }
        if (nodes.length === 0) {
            this.showModalMessage('Excel中未解析到有效的Broker数据', 'warning');
            return;
        }

        const payload = {
            name: clusterName,
            clusterType: environmentType,
            auth: securityEnabled ? 'Y' : 'N',
            authConfig: securityEnabled ? securityJson : null,
            creator: this.getCreator(),
            nodes: nodes
        };

        const resp = await fetch(this.config.apiEndpoints.clusterCreate, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (!resp.ok) {
            const msg = await resp.text();
            throw new Error(msg || '创建集群失败');
        }
        const result = await resp.json();
        console.log('批量集群创建成功:', result);

        this.showModalMessage(`批量导入成功！共创建 ${nodes.length} 个节点`, 'text-success');
        if (this.load) {
            this.load();
            this.loadClusterStats();
        }
    } catch (e) {
        console.error('提交批量导入失败:', e);
        this.showModalMessage('批量导入失败：' + (e.message || '未知错误'), 'error');
    }
};

// ===== 美化提示框函数 =====
window.ManagerModule.showToast = function (message, type, title, duration) {
    const container = document.getElementById('toast-container') || this.createToastContainer();
    const toast = document.createElement('div');
    toast.className = 'toast ' + (type || 'info');
    const iconMap = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };
    const titleMap = {
        success: title || '成功',
        error: title || '错误',
        warning: title || '警告',
        info: title || '提示'
    };
    toast.innerHTML = '<div class="toast-icon"><i class="' + (iconMap[type || 'info']) + '"></i></div>' +
        '<div class="toast-content">' +
        '<div class="toast-title">' + (titleMap[type || 'info']) + '</div>' +
        '<div class="toast-message">' + (message || '') + '</div>' +
        '</div>' +
        '<button class="toast-close" onclick="closeToast(this)"><i class="fas fa-times"></i></button>';
    container.appendChild(toast);
    setTimeout(() => {
        const btn = toast.querySelector('.toast-close');
        if (btn) this.closeToast(btn);
    }, duration || 4000);
};

// 显示删除确认对话框
window.ManagerModule.showDeleteConfirmModal = function (options) {
    const modalHtml = `
    <div id="delete-confirm-modal" class="modal-overlay">
      <div class="delete-confirm-container">
        <div class="delete-confirm-header">
          <div class="delete-confirm-icon">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <h3 class="delete-confirm-title">${options.title || '确认删除'}</h3>
        </div>
        <div class="delete-confirm-body">
          <p class="delete-confirm-message">${options.message || '确定要执行此操作吗？'}</p>
          ${options.description ? `<p class="delete-confirm-description">${options.description}</p>` : ''}
        </div>
        <div class="delete-confirm-footer">
          <button type="button" class="btn-cancel" onclick="closeDeleteConfirmModal()">
            ${options.cancelText || '取消'}
          </button>
          <button type="button" class="btn-danger" onclick="confirmDelete()">
            <i class="fas fa-trash mr-2"></i>${options.confirmText || '确认删除'}
          </button>
        </div>
      </div>
    </div>
  `;

    // 移除之前的模态框（如果存在）
    const existingModal = document.getElementById('delete-confirm-modal');
    if (existingModal) {
        existingModal.remove();
    }

    // 添加新的模态框
    document.body.insertAdjacentHTML('beforeend', modalHtml);

    // 存储回调函数
    window.deleteConfirmCallback = options.onConfirm;

    // 显示模态框
    const modal = document.getElementById('delete-confirm-modal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // ESC 键关闭
    const handleEsc = function (e) {
        if (e.key === 'Escape') {
            closeDeleteConfirmModal();
            document.removeEventListener('keydown', handleEsc);
        }
    };
    document.addEventListener('keydown', handleEsc);

    // 点击模态框外部关闭
    modal.addEventListener('click', function (e) {
        if (e.target === modal) {
            closeDeleteConfirmModal();
        }
    });
};

// 关闭删除确认对话框
window.closeDeleteConfirmModal = function () {
    const modal = document.getElementById('delete-confirm-modal');
    if (modal) {
        modal.remove();
        document.body.style.overflow = '';
        window.deleteConfirmCallback = null;
    }
};

// 确认删除
window.confirmDelete = function () {
    if (window.deleteConfirmCallback && typeof window.deleteConfirmCallback === 'function') {
        window.deleteConfirmCallback();
    }
    closeDeleteConfirmModal();
};

window.ManagerModule.createToastContainer = function () {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
};

window.ManagerModule.closeToast = function (closeBtn) {
    const toast = closeBtn.closest('.toast');
    if (!toast) return;
    toast.classList.add('slide-out');
    setTimeout(() => {
        toast.remove();
    }, 300);
};

window.ManagerModule.showSuccess = function (message, title) {
    this.showToast(message, 'success', title);
};
window.ManagerModule.showError = function (message, title) {
    this.showToast(message, 'error', title);
};
window.ManagerModule.showWarning = function (message, title) {
    this.showToast(message, 'warning', title);
};
window.ManagerModule.showInfo = function (message, title) {
    this.showToast(message, 'info', title);
};


// 全局方法兼容HTML内联调用
window.openCreateClusterModal = function () {
    if (window.ManagerModule) window.ManagerModule.openCreateClusterModal();
};
window.closeCreateClusterModal = function () {
    if (window.ManagerModule) window.ManagerModule.closeCreateClusterModal();
};
window.showModalMessage = function (m, t) {
    if (window.ManagerModule) window.ManagerModule.showModalMessage(m, t);
};
window.hideModalMessage = function () {
    if (window.ManagerModule) window.ManagerModule.hideModalMessage();
};
window.switchTab = function (tab) {
    if (window.ManagerModule) window.ManagerModule.switchTab(tab);
};
window.toggleSecurityConfig = function () {
    if (window.ManagerModule) window.ManagerModule.toggleSecurityConfig();
};
window.validateJSON = function () {
    if (window.ManagerModule) return window.ManagerModule.validateJSON();
};
window.handleFileSelect = function (e) {
    if (window.ManagerModule) window.ManagerModule.handleFileSelect(e);
};
window.handleDragOver = function (e) {
    if (window.ManagerModule) window.ManagerModule.handleDragOver(e);
};
window.handleDragLeave = function (e) {
    if (window.ManagerModule) window.ManagerModule.handleDragLeave(e);
};
window.handleFileDrop = function (e) {
    if (window.ManagerModule) window.ManagerModule.handleFileDrop(e);
};
window.downloadTemplate = function () {
    if (window.ManagerModule) window.ManagerModule.downloadTemplate();
};
window.submitSingleNode = function () {
    if (window.ManagerModule) window.ManagerModule.submitSingleNode();
};
window.submitBatchImport = function () {
    if (window.ManagerModule) window.ManagerModule.submitBatchImport();
};
window.showToast = function (m, t, title, d) {
    if (window.ManagerModule) window.ManagerModule.showToast(m, t, title, d);
};
window.createToastContainer = function () {
    if (window.ManagerModule) return window.ManagerModule.createToastContainer();
};
window.closeToast = function (btn) {
    if (window.ManagerModule) window.ManagerModule.closeToast(btn);
};
window.showSuccess = function (m, t) {
    if (window.ManagerModule) window.ManagerModule.showSuccess(m, t);
};
window.showError = function (m, t) {
    if (window.ManagerModule) window.ManagerModule.showError(m, t);
};
window.showWarning = function (m, t) {
    if (window.ManagerModule) window.ManagerModule.showWarning(m, t);
};
window.showInfo = function (m, t) {
    if (window.ManagerModule) window.ManagerModule.showInfo(m, t);
};

// ===== 编辑集群对话框相关方法 =====

// 打开编辑集群对话框
window.ManagerModule.openEditClusterModal = function (clusterData) {
    // 填充基本信息
    $('#editClusterId').val(clusterData.clusterId);
    $('#editClusterName').val(clusterData.name);

    // 初始化并设置环境类型下拉（Select2 美化）
    if (typeof $ !== 'undefined') {
        const editTypeEl = $('#editClusterType');
        if (editTypeEl.length && !editTypeEl.hasClass('select2-hidden-accessible')) {
            editTypeEl.select2({
                dropdownParent: $('#editClusterModal'),
                minimumResultsForSearch: Infinity,
                width: '100%',
                dropdownCssClass: 'topic-icon-dropdown',
                templateResult: window.ManagerModule.formatEnvironmentOption,
                templateSelection: window.ManagerModule.formatEnvironmentSelection,
                escapeMarkup: function (m) {
                    return m;
                }
            });
        }
        editTypeEl.val(clusterData.type || 'test').trigger('change');
    } else {
        $('#editClusterType').val(clusterData.type || 'test');
    }

    // 填充安全认证信息
    const hasSecurityConfig = clusterData.securityConfig && clusterData.securityConfig.trim() !== '';
    $('#editSecurityEnabled').prop('checked', hasSecurityConfig);
    if (hasSecurityConfig) {
        $('#editSecurityJson').val(clusterData.securityConfig);
        $('#editSecurityConfig').show();
    } else {
        $('#editSecurityJson').val('');
        $('#editSecurityConfig').hide();
    }

    // 填充broker节点信息（渲染为带表头的表格，单元格可编辑）
    this.loadBrokerNodes(clusterData.brokers || []);

    // 显示对话框
    $('#editClusterModal').show();
};

// 关闭编辑集群对话框
window.ManagerModule.closeEditClusterModal = function () {
    $('#editClusterModal').hide();
    $('#editClusterForm')[0].reset();
    $('#brokerNodesList').empty();
    $('#edit-cluster-modal-message').hide();
};

// 切换安全认证配置显示
window.ManagerModule.toggleEditSecurityConfig = function () {
    const isEnabled = $('#editSecurityEnabled').is(':checked');
    if (isEnabled) {
        $('#editSecurityConfig').show();
    } else {
        $('#editSecurityConfig').hide();
        $('#editSecurityJson').val('');
    }
};

// 加载broker节点列表（表格渲染）
window.ManagerModule.loadBrokerNodes = function (brokers) {
    const container = $('#brokerNodesList');
    container.empty();

    const tableHtml = `
    <table class="min-w-full divide-y divide-gray-200 broker-nodes-table">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">节点ID</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IP</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">端口</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">JMX端口</th>
        </tr>
      </thead>
      <tbody id="brokerNodesTbody" class="bg-white divide-y divide-gray-200"></tbody>
    </table>`;

    container.append(tableHtml);
    const tbody = $('#brokerNodesTbody');

    if (!brokers || brokers.length === 0) {
        tbody.html(`<tr class="empty-brokers"><td colspan="4" class="px-4 py-6 text-center text-gray-500">暂无Broker节点，点击上方\"添加节点\"按钮添加</td></tr>`);
        return;
    }

    brokers.forEach((broker, index) => {
        window.ManagerModule.addBrokerNodeItem(broker, index);
    });
};

// 添加broker节点行
window.ManagerModule.addBrokerNodeItem = function (broker, index) {
    const tbody = $('#brokerNodesTbody');
    if (tbody.find('.empty-brokers').length > 0) {
        tbody.empty();
    }

    const rowHtml = `
    <tr class="broker-node-row" data-index="${index}">
      <td class="px-4 py-2">
        <div class="flex items-center gap-2">
          <input type="number" class="form-input" name="brokerId_${index}" value="${broker.brokerId || ''}" placeholder="如: 0" style="max-width: 140px;">
          <button type="button" class="text-red-500 hover:text-red-600" title="删除" onclick="ManagerModule.removeBrokerNode(${index})">
            <i class="fas fa-trash"></i>
          </button>
        </div>
      </td>
      <td class="px-4 py-2">
        <input type="text" class="form-input" name="hostIp_${index}" value="${broker.hostIp || ''}" placeholder="如: 192.168.1.100">
      </td>
      <td class="px-4 py-2">
        <input type="number" class="form-input" name="kafkaPort_${index}" value="${broker.kafkaPort || 9092}" placeholder="9092">
      </td>
      <td class="px-4 py-2">
        <input type="number" class="form-input" name="jmxPort_${index}" value="${broker.jmxPort || 9999}" placeholder="9999">
      </td>
    </tr>`;

    tbody.append(rowHtml);
};

// 添加新的broker节点
window.ManagerModule.addNewBrokerNode = function () {
    let tbody = $('#brokerNodesTbody');
    // 若还未创建表结构（极少数情况），先初始化空表
    if (!tbody.length) {
        this.loadBrokerNodes([]);
        tbody = $('#brokerNodesTbody');
    }
    const currentRows = tbody.find('.broker-node-row').length;
    const newBroker = {brokerId: '', hostIp: '', kafkaPort: 9092, jmxPort: 9999};
    this.addBrokerNodeItem(newBroker, currentRows);
};

// 删除broker节点
window.ManagerModule.removeBrokerNode = function (index) {
    const tbody = $('#brokerNodesTbody');
    const row = tbody.find(`.broker-node-row[data-index="${index}"]`);
    row.remove();

    const rows = tbody.find('.broker-node-row');
    if (!rows.length) {
        tbody.html(`<tr class="empty-brokers"><td colspan="4" class="px-4 py-6 text-center text-gray-500">暂无Broker节点，点击上方\"添加节点\"按钮添加</td></tr>`);
        return;
    }

    // 重新编号与更新name/onclick
    rows.each(function (newIndex) {
        const $tr = $(this);
        $tr.attr('data-index', newIndex);
        // 更新删除按钮索引
        $tr.find('button[onclick^="ManagerModule.removeBrokerNode("]').attr('onclick', `ManagerModule.removeBrokerNode(${newIndex})`);
        // 更新input的name属性
        $tr.find('input').each(function () {
            const $input = $(this);
            const name = $input.attr('name');
            if (name) {
                const baseName = name.split('_')[0];
                $input.attr('name', `${baseName}_${newIndex}`);
            }
        });
    });
};

// 提交编辑集群
window.ManagerModule.submitEditCluster = function () {
    const formData = this.collectEditFormData();

    if (!this.validateEditFormData(formData)) {
        return;
    }

    // 提交数据
    $.ajax({
        url: '/api/manager/cluster/' + formData.clusterId,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function (response) {
            if (response && response.code === 200) {
                window.ManagerModule.showToast('集群信息已更新', 'success', '编辑成功');
                window.ManagerModule.closeEditClusterModal();
                window.ManagerModule.load(); // 重新加载数据
                window.ManagerModule.loadClusterStats();
            } else {
                window.ManagerModule.showToast(response.message || '请稍后重试', 'error', '编辑失败');
            }
        },
        error: function () {
            window.ManagerModule.showToast('网络错误，请稍后重试', 'error', '编辑失败');
        }
    });
};

// 收集编辑表单数据
window.ManagerModule.collectEditFormData = function () {
    const formData = {
        clusterId: $('#editClusterId').val(),
        name: $('#editClusterName').val(),
        type: $('#editClusterType').val(),
        securityEnabled: $('#editSecurityEnabled').is(':checked'),
        securityConfig: $('#editSecurityEnabled').is(':checked') ? $('#editSecurityJson').val() : '',
        brokers: []
    };

    // 收集broker节点数据（表格每行）
    $('#brokerNodesTbody .broker-node-row').each(function (index) {
        const brokerId = $(this).find(`input[name="brokerId_${index}"]`).val();
        const hostIp = $(this).find(`input[name="hostIp_${index}"]`).val();
        const kafkaPort = $(this).find(`input[name="kafkaPort_${index}"]`).val();
        const jmxPort = $(this).find(`input[name="jmxPort_${index}"]`).val();

        if (brokerId && hostIp) {
            formData.brokers.push({
                brokerId: parseInt(brokerId, 10),
                hostIp: (hostIp || '').trim(),
                kafkaPort: parseInt(kafkaPort, 10) || 9092,
                jmxPort: parseInt(jmxPort, 10) || 9999
            });
        }
    });

    return formData;
};

// 验证编辑表单数据
window.ManagerModule.validateEditFormData = function (formData) {
    if (!formData.name || formData.name.trim() === '') {
        this.showToast('集群名称不能为空', 'error', '验证失败');
        return false;
    }

    if (formData.brokers.length === 0) {
        this.showToast('至少需要添加一个Broker节点', 'error', '验证失败');
        return false;
    }

    // 验证安全配置JSON格式
    if (formData.securityEnabled && formData.securityConfig) {
        try {
            JSON.parse(formData.securityConfig);
        } catch (e) {
            this.showToast('安全配置JSON格式不正确', 'error', '验证失败');
            $('#editJsonValidation').text('JSON格式错误: ' + e.message);
            return false;
        }
        $('#editJsonValidation').text('');
    }

    // 验证broker节点数据
    for (let i = 0; i < formData.brokers.length; i++) {
        const broker = formData.brokers[i];
        if (!broker.hostIp || broker.hostIp.trim() === '') {
            this.showToast(`Broker节点 ${i + 1} 的主机IP不能为空`, 'error', '验证失败');
            return false;
        }
        if (!broker.brokerId && broker.brokerId !== 0) {
            this.showToast(`Broker节点 ${i + 1} 的Broker ID不能为空`, 'error', '验证失败');
            return false;
        }
    }

    return true;
};

// 全局方法兼容HTML内联调用
window.openEditClusterModal = function (data) {
    if (window.ManagerModule) window.ManagerModule.openEditClusterModal(data);
};
window.closeEditClusterModal = function () {
    if (window.ManagerModule) window.ManagerModule.closeEditClusterModal();
};
window.toggleEditSecurityConfig = function () {
    if (window.ManagerModule) window.ManagerModule.toggleEditSecurityConfig();
};
window.addNewBrokerNode = function () {
    if (window.ManagerModule) window.ManagerModule.addNewBrokerNode();
};
window.submitEditCluster = function () {
    if (window.ManagerModule) window.ManagerModule.submitEditCluster();
};

// 格式化环境类型选项（参考topics.js）
// 格式化环境类型选项（参考topics.js的formatIconOption）
window.ManagerModule.formatEnvironmentOption = function (env) {
    if (!env.id) return env.text;
    const $env = $(env.element);
    const iconClass = $env.data('icon');
    const iconColor = $env.data('color');

    return $(`
    <div class="flex items-center">
      <div class="icon-preview icon-color-${iconColor}">
        <i class="fa ${iconClass}"></i>
      </div>
      <div class="icon-info">
        <div class="icon-title">${env.text}</div>
      </div>
    </div>
  `);
};

// 格式化选中的环境类型（参考topics.js的formatIconSelection）
window.ManagerModule.formatEnvironmentSelection = function (env) {
    if (!env.id) return env.text;
    const $env = $(env.element);
    const iconClass = $env.data('icon');
    const iconColor = $env.data('color');

    return $(`
    <div class="flex items-center">
      <div class="icon-preview icon-color-${iconColor}">
        <i class="fa ${iconClass}"></i>
      </div>
      <span>${env.text}</span>
    </div>
  `);
};
// 全局方法兼容HTML内联调用
window.formatEnvironmentOption = function (env) {
    if (window.ManagerModule) return window.ManagerModule.formatEnvironmentOption(env);
};
window.formatEnvironmentSelection = function (env) {
    if (window.ManagerModule) return window.ManagerModule.formatEnvironmentSelection(env);
};
