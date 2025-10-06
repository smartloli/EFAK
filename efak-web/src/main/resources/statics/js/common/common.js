// 公共功能模块
const CommonModule = {
    // 初始化公共功能
    init() {
        this.initSidebar();
        this.loadCurrentUserInfo();
        this.initUserMenu();
        this.initNotificationPanel();
        this.setActiveNavigation();
        this.initGlobalTooltips();
        this.setupGlobalAjaxHandler();
    },

    // 全局工具提示功能
    initGlobalTooltips() {
        // 绑定this上下文
        this.boundHandleTooltipShow = this.handleTooltipShow.bind(this);
        this.boundHandleTooltipHide = this.handleTooltipHide.bind(this);

        // 移除已存在的全局tooltip事件
        document.removeEventListener('mouseenter', this.boundHandleTooltipShow, true);
        document.removeEventListener('mouseleave', this.boundHandleTooltipHide, true);

        // 添加全局tooltip事件监听
        document.addEventListener('mouseenter', this.boundHandleTooltipShow, true);
        document.addEventListener('mouseleave', this.boundHandleTooltipHide, true);
    },

    handleTooltipShow(event) {
        try {
            const element = event.target;

            // 确保element是DOM元素
            if (!element || element.nodeType !== Node.ELEMENT_NODE) return;
            if (!element.hasAttribute || !element.hasAttribute('data-tooltip')) return;

            // 排除侧边栏元素（它们有自己的tooltip处理）
            if (element.closest && element.closest('.sidebar-nav-item')) return;

            // 排除已经被特定模块处理的元素
            if (element.closest && element.closest('.task-actions')) return;

            const tooltipText = element.getAttribute('data-tooltip');
            if (!tooltipText) return;

            // 确保element具有getBoundingClientRect方法
            if (!element.getBoundingClientRect) return;

            // 创建tooltip元素
            const tooltip = document.createElement('div');
            tooltip.className = 'global-tooltip fixed z-50 px-2 py-1 text-sm text-white bg-gray-800 rounded shadow-lg pointer-events-none transform -translate-x-1/2 -translate-y-full';
            tooltip.textContent = tooltipText;

            // 创建箭头
            const arrow = document.createElement('div');
            arrow.className = 'absolute top-full left-1/2 transform -translate-x-1/2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-gray-800';
            tooltip.appendChild(arrow);

            document.body.appendChild(tooltip);

            // 计算位置
            const rect = element.getBoundingClientRect();
            const tooltipRect = tooltip.getBoundingClientRect();

            let left = rect.left + (rect.width / 2);
            let top = rect.top - 8;

            // 确保tooltip不超出屏幕
            if (left - tooltipRect.width / 2 < 8) {
                left = tooltipRect.width / 2 + 8;
            } else if (left + tooltipRect.width / 2 > window.innerWidth - 8) {
                left = window.innerWidth - tooltipRect.width / 2 - 8;
            }

            if (top < 8) {
                // 如果上方空间不够，显示在下方
                top = rect.bottom + 8;
                tooltip.classList.remove('-translate-y-full');
                tooltip.classList.add('translate-y-0');
                arrow.classList.remove('top-full', 'border-t-gray-800', 'border-t-4');
                arrow.classList.add('bottom-full', 'border-b-gray-800', 'border-b-4');
            }

            tooltip.style.left = left + 'px';
            tooltip.style.top = top + 'px';

            // 存储tooltip引用
            element._tooltip = tooltip;
        } catch (error) {
            console.warn('Tooltip show error:', error);
        }
    },

    handleTooltipHide(event) {
        try {
            const element = event.target;

            // 确保element是DOM元素
            if (!element || element.nodeType !== Node.ELEMENT_NODE) return;

            if (element._tooltip) {
                element._tooltip.remove();
                delete element._tooltip;
            }
        } catch (error) {
            console.warn('Tooltip hide error:', error);
        }
    },

    // 侧边栏折叠/展开功能
    initSidebar() {
        const sidebar = document.getElementById('sidebar');
        const sidebarToggle = document.getElementById('sidebar-toggle');
        const sidebarExpand = document.getElementById('sidebar-expand');
        const mobileSidebarToggle = document.getElementById('mobile-sidebar-toggle');

        function toggleSidebar() {
            const mainContent = document.querySelector('.main-content');
            const header = document.querySelector('header');

            if (sidebar.classList.contains('w-64')) {
                // 折叠到小尺寸
                sidebar.classList.remove('w-64');
                sidebar.classList.add('w-20');
                sidebar.classList.add('sidebar-collapsed');

                // 调整主内容区域和头部的左边距为80px (w-20)
                if (mainContent) mainContent.style.marginLeft = '80px';
                if (header) header.style.left = '80px';

                // 隐藏文本和分组标题
                const sidebarTexts = sidebar.querySelectorAll('.sidebar-text');
                const sidebarTitles = sidebar.querySelectorAll('.sidebar-title');
                const sectionTitles = sidebar.querySelectorAll('.sidebar-section-title');

                sidebarTexts.forEach(text => text.classList.add('hidden'));
                sidebarTitles.forEach(title => title.classList.add('hidden'));
                sectionTitles.forEach(title => title.classList.add('hidden'));

                // 调整logo容器为居中显示
                const logoContainer = sidebar.querySelector('.sidebar-logo-container');
                if (logoContainer) {
                    logoContainer.classList.add('justify-center');
                    logoContainer.classList.remove('space-x-3');
                }

                // 隐藏折叠按钮，显示展开按钮
                const toggleBtn = sidebar.querySelector('.sidebar-toggle');
                const expandBtn = sidebar.querySelector('.sidebar-expand');
                if (toggleBtn) toggleBtn.classList.add('hidden');
                if (expandBtn) expandBtn.classList.remove('hidden');

                // 调整菜单项为居中显示图标
                const navItems = sidebar.querySelectorAll('.sidebar-nav-item');
                navItems.forEach(item => {
                    item.classList.add('justify-center');
                    const icon = item.querySelector('.sidebar-icon');
                    if (icon) {
                        icon.classList.remove('mr-3');
                        icon.classList.add('mx-auto');
                    }
                });

                // 初始化提示功能
                initTooltips();
            } else {
                // 展开到完整尺寸
                sidebar.classList.remove('w-20', 'sidebar-collapsed');
                sidebar.classList.add('w-64');

                // 恢复主内容区域和头部的左边距为256px (w-64)
                if (mainContent) mainContent.style.marginLeft = '256px';
                if (header) header.style.left = '256px';

                // 显示文本和分组标题
                const sidebarTexts = sidebar.querySelectorAll('.sidebar-text');
                const sidebarTitles = sidebar.querySelectorAll('.sidebar-title');
                const sectionTitles = sidebar.querySelectorAll('.sidebar-section-title');

                sidebarTexts.forEach(text => text.classList.remove('hidden'));
                sidebarTitles.forEach(title => title.classList.remove('hidden'));
                sectionTitles.forEach(title => title.classList.remove('hidden'));

                // 恢复logo容器布局
                const logoContainer = sidebar.querySelector('.sidebar-logo-container');
                if (logoContainer) {
                    logoContainer.classList.remove('justify-center');
                    logoContainer.classList.add('space-x-3');
                }

                // 显示折叠按钮，隐藏展开按钮
                const toggleBtn = sidebar.querySelector('.sidebar-toggle');
                const expandBtn = sidebar.querySelector('.sidebar-expand');
                if (toggleBtn) toggleBtn.classList.remove('hidden');
                if (expandBtn) expandBtn.classList.add('hidden');

                // 恢复菜单项布局
                const navItems = sidebar.querySelectorAll('.sidebar-nav-item');
                navItems.forEach(item => {
                    item.classList.remove('justify-center');
                    const icon = item.querySelector('.sidebar-icon');
                    if (icon) {
                        icon.classList.add('mr-3');
                        icon.classList.remove('mx-auto');
                    }
                });

                // 在展开状态下也保持工具提示功能
                // removeTooltips(); // 注释掉这行，让工具提示在展开状态下也能工作
                // 确保在展开状态下也初始化工具提示
                initTooltips();
            }
        }

        // 初始化提示功能
        function initTooltips() {
            const navItems = sidebar.querySelectorAll('.sidebar-nav-item[data-tooltip]');
            navItems.forEach(item => {
                item.addEventListener('mouseenter', showSidebarTooltip);
                item.addEventListener('mouseleave', hideSidebarTooltip);
            });
        }

        // 移除提示功能
        function removeTooltips() {
            const navItems = sidebar.querySelectorAll('.sidebar-nav-item[data-tooltip]');
            navItems.forEach(item => {
                item.removeEventListener('mouseenter', showSidebarTooltip);
                item.removeEventListener('mouseleave', hideSidebarTooltip);
            });
            // 移除所有现有的提示框
            const existingTooltips = document.querySelectorAll('.sidebar-tooltip');
            existingTooltips.forEach(tooltip => tooltip.remove());
        }

        // 显示侧边栏提示
        function showSidebarTooltip(event) {
            const item = event.currentTarget;
            const tooltipText = item.getAttribute('data-tooltip');
            if (!tooltipText) return;

            // 检查侧边栏是否处于折叠状态
            const isCollapsed = sidebar.classList.contains('sidebar-collapsed');

            // 创建提示框
            const tooltip = document.createElement('div');
            tooltip.className = 'sidebar-tooltip fixed bg-gray-800 text-white text-sm px-3 py-2 rounded-lg shadow-lg z-50 pointer-events-none';
            tooltip.textContent = tooltipText;
            document.body.appendChild(tooltip);

            // 定位提示框
            const rect = item.getBoundingClientRect();

            if (isCollapsed) {
                // 折叠状态下，提示框显示在右侧
                tooltip.style.left = (rect.right + 10) + 'px';
                tooltip.style.top = (rect.top + rect.height / 2 - tooltip.offsetHeight / 2) + 'px';
            } else {
                // 展开状态下，提示框显示在菜单项上方
                tooltip.style.left = (rect.left + rect.width / 2 - tooltip.offsetWidth / 2) + 'px';
                tooltip.style.top = (rect.top - tooltip.offsetHeight - 8) + 'px';
            }

            // 添加动画
            tooltip.style.opacity = '0';
            tooltip.style.transform = isCollapsed ? 'translateX(-10px)' : 'translateY(-10px)';
            setTimeout(() => {
                tooltip.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
                tooltip.style.opacity = '1';
                tooltip.style.transform = 'translateX(0) translateY(0)';
            }, 10);
        }

        // 隐藏侧边栏提示
        function hideSidebarTooltip(event) {
            const tooltips = document.querySelectorAll('.sidebar-tooltip');
            const isCollapsed = sidebar.classList.contains('sidebar-collapsed');

            tooltips.forEach(tooltip => {
                tooltip.style.opacity = '0';
                tooltip.style.transform = isCollapsed ? 'translateX(-10px)' : 'translateY(-10px)';
                setTimeout(() => tooltip.remove(), 200);
            });
        }

        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', toggleSidebar);
        }

        // 添加展开按钮事件监听器
        if (sidebarExpand) {
            sidebarExpand.addEventListener('click', function (e) {
                e.stopPropagation();
                toggleSidebar();
            });
        }

        // 确保菜单项在折叠状态下点击时不会展开侧边栏，只进行页面跳转
        const navItems = sidebar.querySelectorAll('.sidebar-nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', function (e) {
                // 在折叠状态下，阻止任何可能导致展开的事件传播
                if (sidebar.classList.contains('sidebar-collapsed')) {
                    e.stopPropagation();
                    e.preventDefault();

                    // 获取目标链接并确保跳转正常
                    const href = item.getAttribute('href');
                    if (href && href !== '#' && href !== '' && href !== 'javascript:void(0)') {
                        // 确保菜单保持折叠状态
                        sidebar.classList.add('sidebar-collapsed');
                        // 延迟跳转以确保状态保持
                        setTimeout(() => {
                            window.location.href = href;
                        }, 50);
                    }
                    return false; // 阻止任何进一步的事件处理
                }
            });
        });

        // 确保在折叠状态下点击侧边栏其他区域不会展开菜单
        sidebar.addEventListener('click', function (e) {
            if (sidebar.classList.contains('sidebar-collapsed')) {
                // 只允许展开按钮触发展开操作
                if (!e.target.closest('#sidebar-expand')) {
                    console.log('折叠状态下点击侧边栏其他区域，阻止展开');
                    e.stopPropagation();
                    e.preventDefault();
                    return false;
                }
            }
        });

        // 确保Logo区域在折叠状态下点击不会有任何展开行为
        const logoContainer = sidebar.querySelector('.sidebar-logo-container');
        if (logoContainer) {
            logoContainer.addEventListener('click', function (e) {
                if (sidebar.classList.contains('sidebar-collapsed')) {
                    e.stopPropagation();
                    e.preventDefault();
                    // 在折叠状态下点击Logo不执行任何操作
                }
            });
        }

        if (mobileSidebarToggle) {
            mobileSidebarToggle.addEventListener('click', function () {
                // 移动端切换侧边栏显示/隐藏
                if (window.innerWidth <= 1024) {
                    const overlay = document.getElementById('sidebar-overlay');
                    sidebar.classList.toggle('mobile-open');
                    if (sidebar.classList.contains('mobile-open')) {
                        overlay.classList.remove('hidden');
                    } else {
                        overlay.classList.add('hidden');
                    }
                } else {
                    toggleSidebar();
                }
            });
        }

        // 点击遮罩层关闭侧边栏
        const overlay = document.getElementById('sidebar-overlay');
        if (overlay) {
            overlay.addEventListener('click', function () {
                sidebar.classList.remove('mobile-open');
                overlay.classList.add('hidden');
            });
        }

        // 窗口大小变化时处理布局
        window.addEventListener('resize', function () {
            const mainContent = document.querySelector('.main-content');
            const header = document.querySelector('header');

            if (window.innerWidth <= 1024) {
                // 移动端布局
                mainContent.classList.add('main-content-mobile');
                header.classList.add('header-mobile');
                sidebar.classList.remove('mobile-open');
                // 移动端时重置边距
                mainContent.style.marginLeft = '0';
                header.style.left = '0';
            } else {
                // 桌面端布局
                mainContent.classList.remove('main-content-mobile');
                header.classList.remove('header-mobile');
                sidebar.classList.remove('mobile-open');
                // 桌面端时根据侧边栏状态设置边距
                if (sidebar.classList.contains('w-64')) {
                    mainContent.style.marginLeft = '256px';
                    header.style.left = '256px';
                } else {
                    mainContent.style.marginLeft = '80px';
                    header.style.left = '80px';
                }
            }
        });
    },

    // 用户菜单功能
    initUserMenu() {
        const userMenuButton = document.getElementById('user-menu-button');
        const userDropdown = document.getElementById('user-dropdown');
        const userMenuArrow = document.getElementById('user-menu-arrow');
        const userMenuContainer = userMenuButton?.parentElement;

        if (!userMenuButton || !userDropdown || !userMenuContainer) return;

        let isDropdownOpen = false;
        let hideTimeout = null;

        // 用户菜单按钮点击事件
        userMenuButton.addEventListener('click', function (e) {
            e.stopPropagation();
            toggleUserDropdown();
        });

        // 鼠标进入菜单按钮时显示下拉菜单
        userMenuButton.addEventListener('mouseenter', function () {
            clearTimeout(hideTimeout);
            showUserDropdown();
        });

        // 鼠标离开菜单按钮时延迟隐藏
        userMenuButton.addEventListener('mouseleave', function () {
            hideTimeout = setTimeout(() => {
                hideUserDropdown();
            }, 100);
        });

        // 鼠标进入下拉菜单时取消隐藏
        userDropdown.addEventListener('mouseenter', function () {
            clearTimeout(hideTimeout);
        });

        // 鼠标离开下拉菜单时隐藏
        userDropdown.addEventListener('mouseleave', function () {
            hideTimeout = setTimeout(() => {
                hideUserDropdown();
            }, 100);
        });

        // 点击页面其他地方关闭下拉菜单
        document.addEventListener('click', function (e) {
            if (!userMenuContainer.contains(e.target)) {
                hideUserDropdown();
            }
        });

        // 初始化个人信息和修改密码模态框
        this.initProfileModal();
        this.initPasswordModal();

        function toggleUserDropdown() {
            clearTimeout(hideTimeout);
            if (isDropdownOpen) {
                hideUserDropdown();
            } else {
                showUserDropdown();
            }
        }

        function showUserDropdown() {
            if (isDropdownOpen) return;

            userDropdown.classList.remove('hidden');
            // 强制重新渲染以触发CSS过渡动画
            requestAnimationFrame(() => {
                userDropdown.style.opacity = '1';
                userDropdown.style.transform = 'scale(1) translateY(0)';
            });

            if (userMenuArrow) {
                userMenuArrow.style.transform = 'rotate(180deg)';
            }
            isDropdownOpen = true;
        }

        function hideUserDropdown() {
            if (!isDropdownOpen) return;

            userDropdown.style.opacity = '0';
            userDropdown.style.transform = 'scale(0.95) translateY(-10px)';

            // 动画完成后隐藏元素
            setTimeout(() => {
                if (!isDropdownOpen) { // 确保在延迟期间没有重新打开
                    userDropdown.classList.add('hidden');
                }
            }, 200);

            if (userMenuArrow) {
                userMenuArrow.style.transform = 'rotate(0deg)';
            }
            isDropdownOpen = false;
        }
    },

    // 初始化个人信息模态框
    initProfileModal() {
        const profileBtn = document.getElementById('profile-btn');
        const profileModal = document.getElementById('profile-modal');
        const closeProfileModal = document.getElementById('close-profile-modal');

        if (!profileBtn || !profileModal) return;

        profileBtn.addEventListener('click', async function () {
            // 打开模态框前先刷新用户信息
            await CommonModule.loadCurrentUserInfo();
            profileModal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';
        });

        if (closeProfileModal) {
            closeProfileModal.addEventListener('click', function () {
                profileModal.classList.add('hidden');
                document.body.style.overflow = '';
            });
        }

        // 点击模态框外部关闭
        profileModal.addEventListener('click', function (e) {
            if (e.target === profileModal) {
                profileModal.classList.add('hidden');
                document.body.style.overflow = '';
            }
        });

        // ESC键关闭
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !profileModal.classList.contains('hidden')) {
                profileModal.classList.add('hidden');
                document.body.style.overflow = '';
            }
        });
    },

    // 初始化修改密码模态框
    initPasswordModal() {
        const changePasswordBtn = document.getElementById('change-password-btn');
        const changePasswordModal = document.getElementById('change-password-modal');
        const closePasswordModal = document.getElementById('close-password-modal');
        const cancelPasswordChange = document.getElementById('cancel-password-change');
        const changePasswordForm = document.getElementById('change-password-form');

        if (!changePasswordBtn || !changePasswordModal) return;

        // 使用全局用户信息，避免重复请求
        let currentUserId = null;
        if (window.currentUser && window.currentUser.id) {
            currentUserId = window.currentUser.id;
        }

        changePasswordBtn.addEventListener('click', function () {
            changePasswordModal.classList.remove('hidden');
            document.body.style.overflow = 'hidden';

            // 聚焦到当前密码输入框
            setTimeout(() => {
                const currentPasswordInput = document.getElementById('current-password');
                if (currentPasswordInput) {
                    currentPasswordInput.focus();
                }
            }, 100);
        });

        if (closePasswordModal) {
            closePasswordModal.addEventListener('click', function () {
                changePasswordModal.classList.add('hidden');
                document.body.style.overflow = '';
                if (changePasswordForm) changePasswordForm.reset();
            });
        }

        if (cancelPasswordChange) {
            cancelPasswordChange.addEventListener('click', function () {
                changePasswordModal.classList.add('hidden');
                document.body.style.overflow = '';
                if (changePasswordForm) changePasswordForm.reset();
            });
        }

        // 点击模态框外部关闭
        changePasswordModal.addEventListener('click', function (e) {
            if (e.target === changePasswordModal) {
                changePasswordModal.classList.add('hidden');
                document.body.style.overflow = '';
                if (changePasswordForm) changePasswordForm.reset();
            }
        });

        // ESC键关闭
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !changePasswordModal.classList.contains('hidden')) {
                changePasswordModal.classList.add('hidden');
                document.body.style.overflow = '';
                if (changePasswordForm) changePasswordForm.reset();
            }
        });

        // 修改密码表单处理 - 改为按钮点击事件
        const submitButton = changePasswordForm?.querySelector('button[type="submit"]');

        console.log('找到修改密码表单:', !!changePasswordForm);
        console.log('找到提交按钮:', !!submitButton);

        if (submitButton) {
            submitButton.addEventListener('click', async function (e) {
                e.preventDefault();
                console.log('修改密码按钮被点击');

                // 检查用户ID是否已获取
                if (!currentUserId) {
                    console.log('用户ID未获取，显示错误提示');
                    this.showNotification('用户信息未加载，请稍后重试', 'error');
                    return;
                }

                const currentPassword = document.getElementById('current-password').value;
                const newPassword = document.getElementById('new-password').value;
                const confirmPassword = document.getElementById('confirm-password').value;

                console.log('表单数据:', { currentPassword: !!currentPassword, newPassword: !!newPassword, confirmPassword: !!confirmPassword });

                // 表单验证
                if (!currentPassword || !newPassword || !confirmPassword) {
                    this.showNotification('请填写所有字段', 'error');
                    return;
                }

                if (newPassword.length < 8) {
                    this.showNotification('新密码长度至少8位', 'error');
                    return;
                }

                if (newPassword !== confirmPassword) {
                    this.showNotification('两次输入的新密码不一致', 'error');
                    return;
                }

                // 密码复杂度验证
                const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,}$/;
                if (!passwordRegex.test(newPassword)) {
                    this.showNotification('密码必须包含字母和数字，长度至少8位', 'error');
                    return;
                }

                try {
                    // 显示加载状态
                    const originalText = submitButton.textContent;
                    submitButton.textContent = '修改中...';
                    submitButton.disabled = true;

                    // 调试信息
                    console.log('当前用户ID:', currentUserId);
                    console.log('请求URL:', `/api/users/${currentUserId}/reset-password`);

                    // 调用重置密码接口
                    const response = await fetch(`/api/users/${currentUserId}/reset-password`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            currentPassword: currentPassword,
                            newPassword: newPassword
                        })
                    });

                    const data = await response.json();

                    // 调试信息
                    console.log('响应状态:', response.status);
                    console.log('响应数据:', data);

                    if (response.ok && data.message) {
                        this.showNotification('密码修改成功，请重新登录', 'success');

                        // 清空表单
                        changePasswordForm.reset();

                        // 关闭模态框
                        changePasswordModal.classList.add('hidden');
                        document.body.style.overflow = '';

                        // 延迟跳转到登录页
                        setTimeout(() => {
                            window.location.href = '/logout';
                        }, 2000);
                    } else {
                        this.showNotification(data.error || '密码修改失败', 'error');
                    }
                    // 恢复按钮状态
                    submitButton.textContent = originalText;
                    submitButton.disabled = false;
                } catch (err) {
                    console.error('请求失败:', err);
                    this.showNotification('网络请求失败，请重试', 'error');
                    // 恢复按钮状态（异常时也要恢复）
                    submitButton.textContent = '确认修改';
                    submitButton.disabled = false;
                }
            }.bind(this));
        }

        // 同时保留表单提交事件作为备用
        if (changePasswordForm) {
            changePasswordForm.addEventListener('submit', async function (e) {
                e.preventDefault();
                console.log('表单提交事件触发');

                // 检查用户ID是否已获取
                if (!currentUserId) {
                    console.log('用户ID未获取，显示错误提示');
                    this.showNotification('用户信息未加载，请稍后重试', 'error');
                    return;
                }

                const currentPassword = document.getElementById('current-password').value;
                const newPassword = document.getElementById('new-password').value;
                const confirmPassword = document.getElementById('confirm-password').value;

                console.log('表单数据:', { currentPassword: !!currentPassword, newPassword: !!newPassword, confirmPassword: !!confirmPassword });

                // 表单验证
                if (!currentPassword || !newPassword || !confirmPassword) {
                    this.showNotification('请填写所有字段', 'error');
                    return;
                }

                if (newPassword.length < 8) {
                    this.showNotification('新密码长度至少8位', 'error');
                    return;
                }

                if (newPassword !== confirmPassword) {
                    this.showNotification('两次输入的新密码不一致', 'error');
                    return;
                }

                // 密码复杂度验证
                const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,}$/;
                if (!passwordRegex.test(newPassword)) {
                    this.showNotification('密码必须包含字母和数字，长度至少8位', 'error');
                    return;
                }

                try {
                    // 显示加载状态
                    const submitButton = changePasswordForm.querySelector('button[type="submit"]');
                    const originalText = submitButton.textContent;
                    submitButton.textContent = '修改中...';
                    submitButton.disabled = true;

                    // 调试信息
                    console.log('当前用户ID:', currentUserId);
                    console.log('请求URL:', `/api/users/${currentUserId}/reset-password`);

                    // 调用重置密码接口
                    const response = await fetch(`/api/users/${currentUserId}/reset-password`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            currentPassword: currentPassword,
                            newPassword: newPassword
                        })
                    });

                    const data = await response.json();

                    // 调试信息
                    console.log('响应状态:', response.status);
                    console.log('响应数据:', data);

                    if (response.ok && data.message) {
                        this.showNotification('密码修改成功，请重新登录', 'success');

                        // 清空表单
                        changePasswordForm.reset();

                        // 关闭模态框
                        changePasswordModal.classList.add('hidden');
                        document.body.style.overflow = '';

                        // 延迟跳转到登录页
                        setTimeout(() => {
                            window.location.href = '/logout';
                        }, 2000);
                    } else {
                        this.showNotification(data.error || '密码修改失败', 'error');
                    }
                    // 恢复按钮状态
                    submitButton.textContent = originalText;
                    submitButton.disabled = false;
                } catch (err) {
                    console.error('请求失败:', err);
                    this.showNotification('网络请求失败，请重试', 'error');
                    // 恢复按钮状态（异常时也要恢复）
                    const submitButton = changePasswordForm.querySelector('button[type="submit"]');
                    submitButton.textContent = '确认修改';
                    submitButton.disabled = false;
                }
            }.bind(this));
        }
    },

    // 显示通知
    showNotification(message, type = 'info') {
        // 检查是否已存在toast-container
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'fixed top-4 right-4 z-50 space-y-2';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');

        // 根据类型设置样式
        let bgColor, textColor, icon;
        switch (type) {
            case 'success':
                bgColor = 'bg-green-500';
                textColor = 'text-white';
                icon = 'fa-check-circle';
                break;
            case 'error':
                bgColor = 'bg-red-500';
                textColor = 'text-white';
                icon = 'fa-exclamation-circle';
                break;
            case 'warning':
                bgColor = 'bg-yellow-500';
                textColor = 'text-white';
                icon = 'fa-exclamation-triangle';
                break;
            default:
                bgColor = 'bg-blue-500';
                textColor = 'text-white';
                icon = 'fa-info-circle';
        }

        toast.className = `${bgColor} ${textColor} px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2 transform translate-x-full transition-all duration-300 ease-in-out`;
        toast.innerHTML = `
            <i class="fa ${icon}"></i>
            <span>${message}</span>
            <button onclick="this.parentElement.remove()" class="ml-auto text-white hover:text-gray-200">
                <i class="fa fa-times"></i>
            </button>
        `;

        container.appendChild(toast);

        // 动画效果
        setTimeout(() => {
            toast.classList.remove('translate-x-full');
        }, 100);

        // 自动移除
        setTimeout(() => {
            toast.classList.add('translate-x-full');
            setTimeout(() => {
                if (toast.parentElement) {
                    toast.remove();
                }
            }, 300);
        }, 5000);
    },

    // 切换密码可见性
    togglePasswordVisibility(inputId) {
        const input = document.getElementById(inputId);
        if (!input) return;

        const button = input.nextElementSibling;
        if (!button) return;

        const icon = button.querySelector('i');
        if (!icon) return;

        if (input.type === 'password') {
            input.type = 'text';
            icon.className = 'fa fa-eye-slash';
        } else {
            input.type = 'password';
            icon.className = 'fa fa-eye';
        }
    },

    // 通知面板功能 - 完整版本，包含API调用和数据处理
    initNotificationPanel() {
        const notificationButton = document.getElementById('notification-button');
        const notificationPanel = document.getElementById('notification-panel');
        const notificationOverlay = document.getElementById('notification-overlay');
        const closeButton = document.getElementById('close-notification-panel');
        const markAllReadButton = document.getElementById('mark-all-read');
        const clearAllButton = document.getElementById('clear-all');
        const viewAllAlertsButton = document.getElementById('view-all-alerts');

        if (!notificationButton || !notificationPanel) return;

        let isNotificationPanelOpen = false;

        // 获取当前集群ID
        function getCurrentClusterId() {
            const params = new URLSearchParams(window.location.search);
            return params.get('cid') || sessionStorage.getItem('currentClusterId') || '5uTuNNn5dKvh0ZvF';
        }

        // 格式化时间差
        function formatTimeAgo(dateString) {
            const now = new Date();
            const date = new Date(dateString);
            const diffMs = now.getTime() - date.getTime();

            const diffMinutes = Math.floor(diffMs / (1000 * 60));
            const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

            if (diffMinutes < 1) return '刚刚';
            if (diffMinutes < 60) return `${diffMinutes}分钟前`;
            if (diffHours < 24) return `${diffHours}小时前`;
            return `${diffDays}天前`;
        }

        // 获取告警状态文本和样式
        function getAlertStatusInfo(status) {
            switch (status) {
                case 0:
                    return {
                        text: '待处理',
                        bgColor: 'bg-red-100',
                        textColor: 'text-red-600',
                        icon: 'fa-exclamation-circle',
                        dotColor: 'bg-red-500'
                    };
                case 1:
                    return {
                        text: '已处理',
                        bgColor: 'bg-yellow-100',
                        textColor: 'text-yellow-600',
                        icon: 'fa-check',
                        dotColor: 'bg-yellow-500'
                    };
                case 2:
                    return {
                        text: '已忽略',
                        bgColor: 'bg-gray-100',
                        textColor: 'text-gray-600',
                        icon: 'fa-times',
                        dotColor: 'bg-gray-500'
                    };
                case 3:
                    return {
                        text: '已解决',
                        bgColor: 'bg-green-100',
                        textColor: 'text-green-600',
                        icon: 'fa-check-circle',
                        dotColor: 'bg-green-500'
                    };
                default:
                    return {
                        text: '未知状态',
                        bgColor: 'bg-gray-100',
                        textColor: 'text-gray-600',
                        icon: 'fa-question',
                        dotColor: 'bg-gray-500'
                    };
            }
        }

        // 加载告警通知
        async function loadAlertNotifications() {
            console.log('=== loadAlertNotifications 函数开始执行 ===');

            // 显示加载状态
            const notificationList = document.getElementById('notification-list');
            const emptyState = document.getElementById('empty-notifications');

            if (notificationList) {
                notificationList.innerHTML = '<div class="p-4 text-center text-gray-500">加载中...</div>';
            }
            if (emptyState) {
                emptyState.classList.add('hidden');
            }

            try {
                const cid = getCurrentClusterId();
                const apiUrl = `/api/alerts/notifications?cid=${cid}&limit=5`;

                console.log('请求URL:', apiUrl);
                console.log('集群ID:', cid);

                const response = await fetch(apiUrl, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                console.log('响应状态:', response.status);

                if (response.ok) {
                    const alerts = await response.json();
                    console.log('告警数据数量:', alerts.length);

                    renderNotifications(alerts);
                    updateNotificationCount(alerts.length);

                    console.log('=== 数据渲染完成 ===');
                } else {
                    console.error('API请求失败，状态码:', response.status);
                    showEmptyState();
                }
            } catch (error) {
                console.error('=== API请求异常 ===', error);
                showEmptyState();
            }
        }

        // 渲染通知列表
        function renderNotifications(alerts) {
            const notificationList = document.getElementById('notification-list');
            const emptyState = document.getElementById('empty-notifications');

            if (alerts.length === 0) {
                showEmptyState();
                return;
            }

            emptyState.classList.add('hidden');
            notificationList.innerHTML = alerts.map(alert => {
                const statusInfo = getAlertStatusInfo(alert.status);
                return `
                    <div class="notification-item border-b border-gray-100 p-4 hover:bg-gray-50 cursor-pointer relative"
                         onclick="window.location.href='/alerts?cid=${getCurrentClusterId()}'">
                        <div class="flex items-start space-x-3">
                            <div class="w-8 h-8 rounded-full ${statusInfo.bgColor} flex items-center justify-center flex-shrink-0">
                                <i class="fa ${statusInfo.icon} ${statusInfo.textColor} text-sm"></i>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center justify-between">
                                    <p class="text-sm font-medium text-gray-900">${statusInfo.text}</p>
                                    <span class="text-xs text-gray-500">${formatTimeAgo(alert.updatedAt)}</span>
                                </div>
                                <p class="text-sm text-gray-600 mt-1" title="${alert.description}">${alert.title}</p>
                                ${alert.object ? `<p class="text-xs text-gray-500 mt-1">来源: ${alert.object}</p>` : ''}
                            </div>
                            <div class="w-2 h-2 ${statusInfo.dotColor} rounded-full absolute top-4 right-4"></div>
                        </div>
                    </div>
                `;
            }).join('');
        }

        // 显示空状态
        function showEmptyState() {
            const notificationList = document.getElementById('notification-list');
            const emptyState = document.getElementById('empty-notifications');

            notificationList.innerHTML = '';
            emptyState.classList.remove('hidden');
            updateNotificationCount(0);
        }

        // 点击通知按钮打开面板并加载数据
        notificationButton.addEventListener('click', function (e) {
            console.log('=== 通知按钮点击事件触发 ===');
            e.stopPropagation();

            // 显示面板
            showNotificationPanel();

            // 加载数据
            loadAlertNotifications();
        });

        // 点击关闭按钮
        if (closeButton) {
            closeButton.addEventListener('click', function () {
                hideNotificationPanel();
            });
        }

        // 点击遮罩层关闭
        if (notificationOverlay) {
            notificationOverlay.addEventListener('click', function () {
                hideNotificationPanel();
            });
        }

        // 查看所有告警按钮事件
        if (viewAllAlertsButton) {
            viewAllAlertsButton.addEventListener('click', function() {
                const cid = getCurrentClusterId();
                window.location.href = `/alerts?cid=${cid}`;
            });
        }

        // 全部已读功能
        if (markAllReadButton) {
            markAllReadButton.addEventListener('click', function () {
                markAllNotificationsAsRead();
            });
        }

        // 清空全部功能
        if (clearAllButton) {
            clearAllButton.addEventListener('click', function () {
                clearAllNotifications();
            });
        }

        // ESC键关闭面板
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && isNotificationPanelOpen) {
                hideNotificationPanel();
            }
        });

        // 面板显示控制
        function showNotificationPanel() {
            notificationPanel.classList.remove('translate-x-full');
            notificationPanel.classList.add('translate-x-0');

            if (notificationOverlay) {
                notificationOverlay.classList.remove('hidden');
                setTimeout(() => {
                    notificationOverlay.style.opacity = '1';
                }, 10);
            }

            isNotificationPanelOpen = true;

            // 阻止页面滚动
            document.body.style.overflow = 'hidden';
        }

        function hideNotificationPanel() {
            notificationPanel.classList.remove('translate-x-0');
            notificationPanel.classList.add('translate-x-full');

            if (notificationOverlay) {
                notificationOverlay.style.opacity = '0';
                setTimeout(() => {
                    notificationOverlay.classList.add('hidden');
                }, 300);
            }

            isNotificationPanelOpen = false;

            // 恢复页面滚动
            document.body.style.overflow = '';
        }

        function markAllNotificationsAsRead() {
            const unreadNotifications = document.querySelectorAll('.notification-item .bg-red-500, .notification-item .bg-yellow-500, .notification-item .bg-green-500');
            unreadNotifications.forEach(badge => {
                badge.remove();
            });

            // 更新所有未读通知的样式
            const unreadItems = document.querySelectorAll('.notification-item .font-medium:not(.text-gray-500)');
            unreadItems.forEach(item => {
                item.classList.remove('text-gray-900');
                item.classList.add('text-gray-500');
            });

            const unreadContents = document.querySelectorAll('.notification-item .text-gray-600');
            unreadContents.forEach(content => {
                content.classList.remove('text-gray-600');
                content.classList.add('text-gray-500');
            });

            // 更新计数
            updateNotificationCount();

            // 隐藏通知徽章
            const notificationBadge = document.querySelector('.notification-badge');
            if (notificationBadge) {
                notificationBadge.style.display = 'none';
            }
        }

        function clearAllNotifications() {
            const notificationList = document.querySelector('.notification-list');
            const emptyState = document.getElementById('empty-notifications');

            if (notificationList) {
                notificationList.innerHTML = '';
            }

            if (emptyState) {
                emptyState.classList.remove('hidden');
            }

            updateNotificationCount();

            // 隐藏通知徽章
            const notificationBadge = document.querySelector('.notification-badge');
            if (notificationBadge) {
                notificationBadge.style.display = 'none';
            }
        }

        function markNotificationAsRead(notificationItem) {
            // 移除未读标识
            const badge = notificationItem.querySelector('.bg-red-500, .bg-yellow-500, .bg-green-500');
            if (badge) {
                badge.remove();

                // 更新样式为已读状态
                const title = notificationItem.querySelector('.font-medium');
                const content = notificationItem.querySelector('.text-gray-600');

                if (title) {
                    title.classList.remove('text-gray-900');
                    title.classList.add('text-gray-500');
                }

                if (content) {
                    content.classList.remove('text-gray-600');
                    content.classList.add('text-gray-500');
                }

                updateNotificationCount();
            }
        }

        function updateNotificationCount() {
            const unreadCount = document.querySelectorAll('.notification-item .bg-red-500, .notification-item .bg-yellow-500, .notification-item .bg-green-500').length;
            const countElement = document.getElementById('notification-count');

            if (countElement) {
                if (unreadCount === 0) {
                    countElement.textContent = '暂无未读';
                } else {
                    countElement.textContent = `${unreadCount}条未读`;
                }
            }

            // 更新按钮上的通知徽章
            const notificationBadge = document.querySelector('.notification-badge');
            if (notificationBadge) {
                if (unreadCount === 0) {
                    notificationBadge.style.display = 'none';
                } else {
                    notificationBadge.style.display = 'block';
                }
            }
        }
    },

    // 加载当前用户信息
    async loadCurrentUserInfo() {
        try {
            const response = await fetch('/api/users/current', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (!response.ok) {
                console.warn('获取用户信息失败:', response.status);
                return;
            }

            const result = await response.json();

            if (result.success && result.data) {
                // 存储到全局变量，供其他模块使用
                window.currentUser = result.data;
                this.updateUserInfoDisplay(result.data);
            } else {
                console.warn('获取用户信息失败:', result.message);
            }
        } catch (error) {
            console.error('获取用户信息出错:', error);
        }
    },

    // 更新用户信息显示
    updateUserInfoDisplay(userData) {
        // 更新头部用户信息
        const userNameElement = document.querySelector('#user-menu-button .text-sm.font-medium');
        const userRoleElement = document.querySelector('#user-menu-button .text-xs.text-gray-500');
        const userAvatar = document.querySelector('#user-menu-button img');

        if (userNameElement) {
            userNameElement.textContent = userData.displayName || userData.username;
        }

        if (userRoleElement) {
            userRoleElement.textContent = userData.displayRole || '用户';
        }

        if (userAvatar && userData.avatar) {
            userAvatar.src = userData.avatar;
        }

        // 更新下拉菜单中的用户信息
        const dropdownUserName = document.querySelector('#user-dropdown .text-sm.font-medium.text-gray-900');
        const dropdownUserRole = document.querySelector('#user-dropdown .text-xs.text-gray-500');

        if (dropdownUserName) {
            dropdownUserName.textContent = userData.displayName || userData.username;
        }

        if (dropdownUserRole) {
            dropdownUserRole.textContent = userData.displayRole || '用户';
        }

        // 更新个人信息模态框中的用户信息
        const modalUserName = document.querySelector('#profile-modal .text-xl.font-semibold.text-gray-900');
        const modalUserRole = document.querySelector('#profile-modal .text-sm.text-gray-500');
        const modalLastLogin = document.querySelector('#profile-modal .text-sm.text-gray-900[data-field="lastLogin"]');
        const modalAvatar = document.querySelector('#profile-modal img');
        const userStatusBadge = document.querySelector('#user-status-badge');

        if (modalUserName) {
            modalUserName.textContent = userData.displayName || userData.username;
        }

        if (modalUserRole) {
            modalUserRole.textContent = userData.displayRole || '用户';
        }

        if (modalLastLogin) {
            modalLastLogin.textContent = userData.lastLoginTime || '未知';
        }

        // 更新用户状态显示
        if (userStatusBadge && userData.statusDisplay) {
            userStatusBadge.className = `inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${userData.statusClass || 'bg-green-100 text-green-800'}`;
            userStatusBadge.innerHTML = `<i class="fa ${userData.statusIcon || 'fa-check-circle'} mr-1"></i>${userData.statusDisplay}`;
        }

        if (modalAvatar && userData.avatar) {
            modalAvatar.src = userData.avatar;
        }

        // 存储用户信息到全局变量，供其他模块使用
        window.currentUser = userData;
    },

    // 设置当前页面的导航高亮
    setActiveNavigation() {
        const currentPage = this.getCurrentPageName();
        const navLinks = document.querySelectorAll('nav a[data-page]');

        navLinks.forEach(link => {
            const pageName = link.getAttribute('data-page');
            if (pageName === currentPage) {
                link.classList.add('sidebar-item-active');
                link.classList.remove('hover:bg-gray-100');
            } else {
                link.classList.remove('sidebar-item-active');
                link.classList.add('hover:bg-gray-100');
            }
        });
    },

    // 获取当前页面名称
    getCurrentPageName() {
        const path = window.location.pathname;
        const filename = path.split('/').pop();
        return filename.replace('.html', '') || 'index';
    },

    // 设置页面标题
    setPageTitle(title) {
        const titleElement = document.getElementById('page-title');
        if (titleElement) {
            titleElement.textContent = title;
        }
        document.title = `EFAK - ${title}`;
    },

    // 加载HTML模块
    async loadModule(selector, url) {
        try {
            const response = await fetch(url);
            const html = await response.text();
            document.querySelector(selector).innerHTML = html;
        } catch (error) {
            console.error(`Failed to load module ${url}:`, error);
        }
    },

    // 设置全局Ajax错误处理
    setupGlobalAjaxHandler() {
        // jQuery Ajax全局错误处理
        $(document).ajaxError(function (event, xhr, settings, thrownError) {
            console.log("xhr.status:" + xhr.status)
            if (xhr.status === 401) {
                // 会话过期，保存当前URL并跳转到登录页面
                const currentUrl = window.location.pathname + window.location.search;
                const loginUrl = `/login?targetUrl=${encodeURIComponent(currentUrl)}`;
                window.location.href = loginUrl;
            }
        });

        // 原生fetch请求拦截（如果有使用fetch的话）
        const originalFetch = window.fetch;
        window.fetch = function (...args) {
            return originalFetch.apply(this, args)
                .then(response => {
                    if (response.status === 401) {
                        // 会话过期，保存当前URL并跳转到登录页面
                        const currentUrl = window.location.pathname + window.location.search;
                        const loginUrl = `/login?targetUrl=${encodeURIComponent(currentUrl)}`;
                        window.location.href = loginUrl;
                        return Promise.reject(new Error('Unauthorized'));
                    }
                    return response;
                })
                .catch(error => {
                    return Promise.reject(error);
                });
        };
    }
};

// 全局函数：加载公共模块
async function loadCommonModules() {
    try {
        // 加载侧边栏
        const navbarResponse = await fetch('navbar.html');
        const navbarHtml = await navbarResponse.text();
        document.getElementById('navbar-container').innerHTML = navbarHtml;

        // 加载头部
        const headerResponse = await fetch('header.html');
        const headerHtml = await headerResponse.text();
        document.getElementById('header-container').innerHTML = headerHtml;

        // 重新初始化公共功能
        setTimeout(() => {
            CommonModule.init();
        }, 100);
    } catch (error) {
        console.error('Failed to load common modules:', error);
    }
}

// 全局函数：设置活跃导航项
function setActiveNavItem(pageId) {
    setTimeout(() => {
        const navLinks = document.querySelectorAll('nav a[data-page]');
        navLinks.forEach(link => {
            const pageName = link.getAttribute('data-page');
            if (pageName === pageId) {
                link.classList.add('sidebar-item-active');
                link.classList.remove('hover:bg-gray-100');
            } else {
                link.classList.remove('sidebar-item-active');
                link.classList.add('hover:bg-gray-100');
            }
        });
    }, 200);
}

// 全局函数：设置页面标题
function setPageTitle(title, subtitle) {
    setTimeout(() => {
        const titleElement = document.getElementById('page-title');
        const subtitleElement = document.getElementById('page-subtitle');

        if (titleElement) {
            titleElement.textContent = title;
        }
        if (subtitleElement && subtitle) {
            subtitleElement.textContent = subtitle;
        }

        document.title = `EFAK - ${title}`;
    }, 200);
}

// 全局函数 - 切换密码可见性
function togglePasswordVisibility(inputId) {
    CommonModule.togglePasswordVisibility(inputId);
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function () {
    CommonModule.init();
});