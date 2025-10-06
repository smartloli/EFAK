// 用户数据 - 从后端API获取
let users = [];
let filteredUsers = [];
let currentPage = 1; // 确保初始化为第1页
const pageSize = 5;
let totalPages = 1;
let totalUsers = 0;

// 初始化页面
document.addEventListener('DOMContentLoaded', function () {
    loadUsers();
    initializeEventListeners();
});

// 从后端API加载用户数据
async function loadUsers() {
    try {
        const response = await fetch('/api/users/page?page=' + currentPage + '&size=' + pageSize);
        if (!response.ok) {
            throw new Error('获取用户数据失败');
        }

        const data = await response.json();
        users = data.users || [];
        totalUsers = data.total || 0;
        totalPages = data.totalPages || 1;
        // 初始加载时确保第1页按钮显示蓝色状态
        currentPage = 1;

        renderUsers(users);
        updateStats();
        renderPagination();
        updatePaginationInfo();
        // 检查当前用户权限，控制添加用户按钮的显示
        checkUserPermissions();

    } catch (error) {
        console.error('加载用户数据失败:', error);
        showToast('加载用户数据失败: ' + error.message, 'error');
    }
}

// 初始化事件监听器
function initializeEventListeners() {
    // 搜索功能
    const searchInput = document.getElementById('userSearch');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function () {
            filterUsers();
        }, 300));
    }
}

// 防抖函数
function debounce(func, wait) {
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

// 角色和状态辅助函数
function getRoleIcon(role) {
    const icons = {
        'admin': 'crown',
        'moderator': 'shield-alt',
        'user': 'user'
    };
    return icons[role] || 'user';
}

function getRoleName(role) {
    const names = {
        'admin': '管理员',
        'user': '普通用户'
    };
    return names[role] || '未知';
}

function getStatusIcon(status) {
    const icons = {
        'active': 'check-circle',
        'inactive': 'times-circle',
        'pending': 'clock'
    };
    return icons[status] || 'question-circle';
}

function getStatusName(status) {
    const names = {
        'active': '活跃',
        'inactive': '非活跃',
        'pending': '待审核'
    };
    return names[status] || '未知';
}

// 搜索和过滤功能
async function filterUsers() {
    const searchTerm = document.getElementById('userSearch').value;

    // 构建查询参数
    const params = new URLSearchParams();
    params.append('page', '1'); // 重置到第一页
    params.append('size', pageSize);
    if (searchTerm) {
        params.append('search', searchTerm);
    }

    try {
        const response = await fetch('/api/users/page?' + params.toString());
        if (!response.ok) {
            throw new Error('搜索用户失败');
        }

        const data = await response.json();
        users = data.users || [];
        totalUsers = data.total || 0;
        totalPages = data.totalPages || 1;
        // 搜索时重置到第一页
        currentPage = 1;

        renderUsers(users);
        updateStats();
        renderPagination();
        updatePaginationInfo();
        // 检查当前用户权限，控制添加用户按钮的显示
        checkUserPermissions();

    } catch (error) {
        console.error('搜索用户失败:', error);
        showToast('搜索用户失败: ' + error.message, 'error');
    }
}

function updateStats() {
    document.getElementById('totalUsers').textContent = totalUsers;

    // 计算活跃用户数
    const activeUsers = users.filter(u => u.status === 'active').length;
    document.getElementById('activeUsers').textContent = activeUsers;

    // 计算管理员数
    const adminUsers = users.filter(u => u.role === 'admin').length;
    document.getElementById('adminUsers').textContent = adminUsers;

    // 计算新用户数（最近30天）
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    const newUsers = users.filter(u => {
        if (!u.createdAt) return false;
        const createdDate = new Date(u.createdAt);
        return createdDate >= thirtyDaysAgo;
    }).length;
    document.getElementById('newUsers').textContent = newUsers;
}

function renderUsers(userList) {
    const tbody = document.querySelector('.user-table tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    if (userList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-8 text-gray-500">
            <i class="fa fa-users text-4xl mb-4"></i>
            <p>暂无用户数据</p>
        </td></tr>`;
    } else {
        userList.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <div class="user-info">
                        <img src="${user.avatar || '/images/user_profile.jpg'}" alt="${user.username}" class="user-avatar">
                        <div>
                            <h4 class="font-semibold text-gray-900">${user.username}</h4>
                            <p class="text-sm text-gray-500">用户ID: ${user.id}</p>
                        </div>
                    </div>
                </td>
                <td><span class="user-role ${user.role}"><i class="fa fa-${getRoleIcon(user.role)}"></i>${getRoleName(user.role)}</span></td>
                <td><span class="user-status ${user.status}"><i class="fa fa-${getStatusIcon(user.status)}"></i>${getStatusName(user.status)}</span></td>
                <td>
                    ${user.originPassword ?
                    `<span class="text-sm text-gray-900 font-mono bg-gray-100 px-2 py-1 rounded" title="原始密码">${user.originPassword}</span>` :
                    `<span class="text-sm text-gray-600">••••••••</span>`
                }
                </td>
                <td>${user.lastLogin || '从未登录'}</td>
                <td>
                    <div class="user-actions">
                        <button class="user-btn view" onclick="viewUser(${user.id})" title="查看详情">
                            <i class="fa fa-eye"></i>
                        </button>
                        ${user.canEdit ? `<button class="user-btn edit" onclick="editUser(${user.id})" title="编辑用户">
                            <i class="fa fa-edit"></i>
                        </button>` : ''}
                        ${user.canDelete ? `<button class="user-btn delete" onclick="deleteUser(${user.id})" title="删除用户">
                            <i class="fa fa-trash"></i>
                        </button>` : ''}
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }
}

// 渲染分页控件
function renderPagination() {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    // 计算显示的页码范围
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, currentPage + 2);

    // 确保至少显示5个页码
    if (endPage - startPage < 4) {
        if (startPage === 1) {
            endPage = Math.min(totalPages, startPage + 4);
        } else {
            startPage = Math.max(1, endPage - 4);
        }
    }

    let html = '';

    // 首页按钮
    html += `<button id="first-page" class="pagination-btn" title="首页" ${currentPage === 1 ? 'disabled' : ''} onclick="gotoPage(1)">
        <i class="fa fa-angle-double-left"></i>
    </button>`;

    // 上一页按钮
    html += `<button id="prev-page" class="pagination-btn" title="上一页" ${currentPage === 1 ? 'disabled' : ''} onclick="gotoPage(${currentPage - 1})">
        <i class="fa fa-angle-left"></i>
    </button>`;

    // 页码按钮
    html += `<div class="pagination-pages">`;
    for (let i = startPage; i <= endPage; i++) {
        const isActive = i === currentPage;
        html += `<button class="page-btn ${isActive ? 'active' : ''}" onclick="gotoPage(${i})">${i}</button>`;
    }
    html += `</div>`;

    // 下一页按钮
    html += `<button id="next-page" class="pagination-btn" title="下一页" ${currentPage === totalPages ? 'disabled' : ''} onclick="gotoPage(${currentPage + 1})">
        <i class="fa fa-angle-right"></i>
    </button>`;

    // 末页按钮
    html += `<button id="last-page" class="pagination-btn" title="末页" ${currentPage === totalPages ? 'disabled' : ''} onclick="gotoPage(${totalPages})">
        <i class="fa fa-angle-double-right"></i>
    </button>`;

    pagination.innerHTML = html;
}

// 页面跳转
async function gotoPage(page) {
    if (page < 1 || page > totalPages) return;

    currentPage = page;

    // 构建查询参数
    const params = new URLSearchParams();
    params.append('page', currentPage);
    params.append('size', pageSize);

    const searchTerm = document.getElementById('userSearch').value;

    if (searchTerm) {
        params.append('search', searchTerm);
    }

    try {
        const response = await fetch('/api/users/page?' + params.toString());
        if (!response.ok) {
            throw new Error('获取用户数据失败');
        }

        const data = await response.json();
        users = data.users || [];
        totalUsers = data.total || 0;
        totalPages = data.totalPages || 1;
        // 确保当前页状态正确，使用请求的页面
        currentPage = page;

        renderUsers(users);
        renderPagination();
        updatePaginationInfo();
        // 检查当前用户权限，控制添加用户按钮的显示
        checkUserPermissions();

    } catch (error) {
        console.error('页面跳转失败:', error);
        showToast('页面跳转失败: ' + error.message, 'error');
    }
}

// 更新分页信息
function updatePaginationInfo() {
    const startRecord = (currentPage - 1) * pageSize + 1;
    const endRecord = Math.min(currentPage * pageSize, totalUsers);

    document.getElementById('page-start').textContent = startRecord;
    document.getElementById('page-end').textContent = endRecord;
    document.getElementById('total-count').textContent = totalUsers;
}

// 查看用户详情
window.viewUser = function (userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;

    // 填充用户详情
    document.getElementById('detailUserId').textContent = user.id;
    document.getElementById('detailUsername').textContent = user.username;
    document.getElementById('detailRole').textContent = getRoleName(user.role);
    document.getElementById('detailStatus').textContent = getStatusName(user.status);
    document.getElementById('detailLastLogin').textContent = user.lastLogin;
    document.getElementById('detailCreatedAt').textContent = user.createdAt;

    // 处理原始密码显示
    const originPasswordSection = document.getElementById('originPasswordSection');
    const detailOriginPassword = document.getElementById('detailOriginPassword');

    if (user.originPassword) {
        detailOriginPassword.textContent = user.originPassword;
        originPasswordSection.style.display = 'block';
    } else {
        originPasswordSection.style.display = 'none';
    }

    // 处理编辑按钮显示
    const editUserFromDetailBtn = document.getElementById('editUserFromDetailBtn');
    if (user.canEdit) {
        editUserFromDetailBtn.style.display = 'inline-flex';
    } else {
        editUserFromDetailBtn.style.display = 'none';
    }

    // 显示详情弹窗
    document.getElementById('userDetailModal').classList.remove('hidden');
};

// 编辑用户
window.editUser = function (userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;

    // 权限检查：只有admin用户才能编辑其他用户
    if (!user.canEdit) {
        showToast('您没有权限编辑此用户', 'error');
        return;
    }

    // 创建编辑弹窗
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50';
    modal.innerHTML = `
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 transform transition-all">
            <!-- 弹窗头部 -->
            <div class="flex items-center justify-between p-6 border-b border-gray-200">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                        <i class="fa fa-edit text-blue-600 text-lg"></i>
                    </div>
                    <h2 class="text-xl font-bold text-gray-900">编辑用户</h2>
                </div>
                <button onclick="this.closest('.fixed').remove()" class="text-gray-400 hover:text-gray-700 text-2xl transition-colors">
                    <i class="fa fa-times"></i>
                </button>
            </div>

            <!-- 弹窗内容 -->
            <form id="editUserForm" class="p-6">
                <div class="space-y-6">
                    <!-- 用户名 -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            <i class="fa fa-user text-gray-400 mr-2"></i>用户名
                        </label>
                        <input type="text" name="username" value="${user.username}" 
                            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                            readonly>
                    </div>

                    <!-- 角色选择 -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            <i class="fa fa-tag text-gray-400 mr-2"></i>用户角色
                        </label>
                        <select name="roles" id="editUserRoleSelect" class="form-input select2-user-role" required>
                            <option value="">请选择用户角色</option>
                            <option value="ROLE_USER" data-icon="fa-user" data-color="text-blue-600" ${user.role === 'user' ? 'selected' : ''}>普通用户</option>
                            <option value="ROLE_ADMIN" data-icon="fa-shield" data-color="text-orange-600" ${user.role === 'admin' ? 'selected' : ''}>管理员</option>
                        </select>
                    </div>

                    <!-- 状态选择 -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            <i class="fa fa-toggle-on text-gray-400 mr-2"></i>用户状态
                        </label>
                        <select name="status" id="editUserStatusSelect" class="form-input select2-user-status" required>
                            <option value="">请选择用户状态</option>
                            <option value="ACTIVE" data-icon="fa-check-circle" data-color="text-green-600" ${user.status === 'active' ? 'selected' : ''}>活跃</option>
                            <option value="INACTIVE" data-icon="fa-times-circle" data-color="text-red-600" ${user.status === 'inactive' ? 'selected' : ''}>非活跃</option>
                        </select>
                    </div>
                </div>

                <!-- 弹窗底部按钮 -->
                <div class="flex justify-end gap-3 mt-8 pt-6 border-t border-gray-200">
                    <button type="button" onclick="this.closest('.fixed').remove()"
                        class="px-6 py-3 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors font-medium">
                        <i class="fa fa-times mr-2"></i>取消
                    </button>
                    <button type="submit"
                        class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium">
                        <i class="fa fa-save mr-2"></i>保存更改
                    </button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    // 初始化select2组件
    setTimeout(() => {
        initializeEditUserSelect2();
    }, 50);

    // 处理表单提交
    document.getElementById('editUserForm').onsubmit = function (e) {
        e.preventDefault();
        const formData = new FormData(e.target);
        const userData = Object.fromEntries(formData);

        // 调试信息
        console.log('编辑用户数据:', userData);

        // 调用后端API更新用户
        updateUserInBackend(userId, userData, modal);
    };
};

// 调用后端API更新用户
async function updateUserInBackend(userId, userData, modal) {
    try {
        console.log('发送到后端的数据:', userData);

        const response = await fetch('/api/users/' + userId, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || '更新用户失败');
        }

        showToast('用户信息已更新', 'success');

        // 重新加载用户列表
        await loadUsers();

        // 关闭弹窗
        modal.remove();

    } catch (error) {
        console.error('更新用户失败:', error);
        showToast('更新用户失败: ' + error.message, 'error');
    }
}

// 删除用户
window.deleteUser = function (userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;

    // 权限检查：只有admin用户才能删除其他用户
    if (!user.canDelete) {
        showToast('您没有权限删除此用户', 'error');
        return;
    }

    // 创建确认弹窗
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50';
    modal.innerHTML = `
        <div class="bg-white rounded-lg shadow-lg w-full max-w-md mx-4 p-6">
            <h3 class="text-xl font-bold text-gray-900 mb-4">确认删除</h3>
            <p class="text-gray-600 mb-6">确定要删除用户 "${user.username}" 吗？此操作无法撤销。</p>
            <div class="flex justify-end gap-3">
                <button onclick="this.closest('.fixed').remove()" 
                    class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">取消</button>
                <button onclick="confirmDelete(${userId}, this)" 
                    class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">确认删除</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
};

// 确认删除用户
window.confirmDelete = async function (userId, button) {
    try {
        const response = await fetch('/api/users/' + userId, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('删除用户失败');
        }

        const result = await response.json();
        showToast('用户已删除', 'success');

        // 重新加载用户列表
        await loadUsers();

        // 关闭弹窗
        button.closest('.fixed').remove();

    } catch (error) {
        console.error('删除用户失败:', error);
        showToast('删除用户失败: ' + error.message, 'error');
    }
};

// 显示提示信息
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    const icons = {
        'success': 'check-circle',
        'error': 'times-circle',
        'warning': 'exclamation-circle',
        'info': 'info-circle'
    };
    const colors = {
        'success': 'bg-green-50 text-green-800 border-green-200',
        'error': 'bg-red-50 text-red-800 border-red-200',
        'warning': 'bg-yellow-50 text-yellow-800 border-yellow-200',
        'info': 'bg-blue-50 text-blue-800 border-blue-200'
    };

    toast.className = `fixed top-4 right-4 flex items-center gap-2 px-4 py-3 rounded-lg shadow-lg border ${colors[type]} transform transition-all duration-300 z-50`;
    toast.innerHTML = `
        <i class="fa fa-${icons[type]} text-lg"></i>
        <span class="font-medium">${message}</span>
    `;

    // 添加进场动画
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(-20px)';

    document.body.appendChild(toast);

    // 触发进场动画
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 10);

    // 3秒后开始退场动画
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 关闭用户详情弹窗
window.closeUserDetailModal = function () {
    document.getElementById('userDetailModal').classList.add('hidden');
};

// 从详情页面编辑用户
window.editUserFromDetail = function () {
    const userId = parseInt(document.getElementById('detailUserId').textContent);
    closeUserDetailModal();
    editUser(userId);
};

// 添加用户
window.addUser = function () {
    document.getElementById('userModal').classList.remove('hidden');

    // 初始化select2组件
    setTimeout(() => {
        initializeSelect2();
    }, 50);

    // 聚焦到用户名输入框
    setTimeout(() => {
        const nameInput = document.getElementById('usernameInput');
        if (nameInput) nameInput.focus();
    }, 100);

    // 初始化用户名验证
    initializeUsernameValidation();
};

// 关闭添加用户弹窗
window.closeUserModal = function () {
    // 销毁select2组件
    if ($('.select2-user-role').hasClass('select2-hidden-accessible')) {
        $('.select2-user-role').select2('destroy');
    }

    document.getElementById('userModal').classList.add('hidden');
    document.getElementById('addUserForm').reset();

    // 重置用户名验证状态
    const usernameInput = document.getElementById('usernameInput');
    const usernameError = document.getElementById('usernameError');
    if (usernameInput) {
        usernameInput.classList.remove('border-red-500');
        usernameInput.classList.add('border-gray-300');
    }
    if (usernameError) {
        usernameError.classList.add('hidden');
    }

    // 重新初始化select2组件
    setTimeout(() => {
        initializeSelect2();
    }, 100);
};

// 角色管理弹窗
window.manageRoles = function () {
    document.getElementById('roleModal').classList.remove('hidden');
};

// 关闭角色管理弹窗
window.closeRoleModal = function () {
    document.getElementById('roleModal').classList.add('hidden');
};

// 管理权限


// 处理添加用户表单提交
document.addEventListener('DOMContentLoaded', function () {
    const addUserForm = document.getElementById('addUserForm');
    if (addUserForm) {
        addUserForm.addEventListener('submit', async function (e) {
            e.preventDefault();

            // 验证用户名格式
            const usernameInput = document.getElementById('usernameInput');
            const username = usernameInput.value.trim();
            const usernamePattern = /^[a-zA-Z0-9_]+$/;

            if (!usernamePattern.test(username)) {
                // 显示错误信息
                const usernameError = document.getElementById('usernameError');
                usernameError.classList.remove('hidden');
                usernameInput.classList.add('border-red-500');
                usernameInput.classList.remove('border-gray-300');
                usernameInput.focus();
                showToast('用户名格式不正确，只能包含英文、数字和下划线', 'error');
                return;
            }

            const formData = new FormData(e.target);
            const userData = Object.fromEntries(formData);

            try {
                const response = await fetch('/api/users', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(userData)
                });

                if (!response.ok) {
                    throw new Error('添加用户失败');
                }

                const result = await response.json();

                // 显示生成的密码
                if (result.generatedPassword) {
                    showPasswordModal(result.generatedPassword, userData.username);
                } else {
                    showToast('用户添加成功', 'success');
                }

                // 重新加载用户列表
                await loadUsers();

                // 关闭弹窗
                closeUserModal();

            } catch (error) {
                console.error('添加用户失败:', error);
                showToast('添加用户失败: ' + error.message, 'error');
            }
        });
    }

    // 初始化select2组件
    initializeSelect2();
});

// 显示生成的密码弹窗
function showPasswordModal(password, username) {
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50';
    modal.innerHTML = `
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 transform transition-all">
            <div class="flex items-center justify-between p-6 border-b border-gray-200">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                        <i class="fa fa-check-circle text-green-600 text-lg"></i>
                    </div>
                    <h2 class="text-xl font-bold text-gray-900">用户创建成功</h2>
                </div>
                <button onclick="this.closest('.fixed').remove()" class="text-gray-400 hover:text-gray-700 text-2xl transition-colors">
                    <i class="fa fa-times"></i>
                </button>
            </div>
            <div class="p-6">
                <div class="space-y-4">
                    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                        <div class="flex items-start">
                            <i class="fa fa-info-circle text-blue-500 mt-1 mr-3"></i>
                            <div>
                                <h4 class="text-sm font-medium text-blue-800 mb-2">用户信息</h4>
                                <p class="text-sm text-blue-700 mb-2">用户名: <span class="font-semibold">${username}</span></p>
                                <p class="text-sm text-blue-700">系统已自动生成8位随机密码</p>
                            </div>
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">
                            <i class="fa fa-key text-gray-400 mr-2"></i>生成的密码
                        </label>
                        <div class="flex items-center gap-2">
                            <input type="text" value="${password}" readonly 
                                class="flex-1 px-4 py-3 border border-gray-300 rounded-lg bg-gray-50 font-mono text-lg"
                                id="generatedPasswordInput">
                            <button onclick="copyPassword()" 
                                class="px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                                <i class="fa fa-copy mr-2"></i>复制
                            </button>
                        </div>
                        <p class="text-xs text-gray-500 mt-2">请妥善保管此密码，用户首次登录后建议修改密码</p>
                    </div>
                </div>
                <div class="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-200">
                    <button onclick="this.closest('.fixed').remove()" 
                        class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium">
                        <i class="fa fa-check mr-2"></i>确定
                    </button>
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

// 复制密码到剪贴板
window.copyPassword = function () {
    const passwordInput = document.getElementById('generatedPasswordInput');
    if (passwordInput) {
        passwordInput.select();
        passwordInput.setSelectionRange(0, 99999); // 兼容移动设备
        document.execCommand('copy');
        showToast('密码已复制到剪贴板', 'success');
    }
};

// 初始化用户名验证
function initializeUsernameValidation() {
    const usernameInput = document.getElementById('usernameInput');
    const usernameError = document.getElementById('usernameError');

    if (usernameInput && usernameError) {
        // 实时验证输入
        usernameInput.addEventListener('input', function () {
            const value = this.value;
            const pattern = /^[a-zA-Z0-9_]*$/;

            if (value && !pattern.test(value)) {
                // 移除非法字符
                this.value = value.replace(/[^a-zA-Z0-9_]/g, '');
                usernameError.classList.remove('hidden');
                this.classList.add('border-red-500');
                this.classList.remove('border-gray-300');
            } else {
                usernameError.classList.add('hidden');
                this.classList.remove('border-red-500');
                this.classList.add('border-gray-300');
            }
        });

        // 失去焦点时验证
        usernameInput.addEventListener('blur', function () {
            const value = this.value;
            const pattern = /^[a-zA-Z0-9_]+$/;

            if (value && !pattern.test(value)) {
                usernameError.classList.remove('hidden');
                this.classList.add('border-red-500');
                this.classList.remove('border-gray-300');
            } else {
                usernameError.classList.add('hidden');
                this.classList.remove('border-red-500');
                this.classList.add('border-gray-300');
            }
        });

        // 粘贴时验证
        usernameInput.addEventListener('paste', function (e) {
            setTimeout(() => {
                const value = this.value;
                const pattern = /^[a-zA-Z0-9_]*$/;

                if (value && !pattern.test(value)) {
                    this.value = value.replace(/[^a-zA-Z0-9_]/g, '');
                    usernameError.classList.remove('hidden');
                    this.classList.add('border-red-500');
                    this.classList.remove('border-gray-300');
                }
            }, 10);
        });
    }
}

// 初始化select2组件
function initializeSelect2() {
    // 初始化用户角色选择框
    if ($('.select2-user-role').length) {
        $('.select2-user-role').select2({
            placeholder: '请选择用户角色',
            allowClear: false,
            width: '100%',
            minimumResultsForSearch: Infinity, // 禁用搜索功能
            dropdownParent: $('#userModal'),
            templateResult: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            },
            templateSelection: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            }
        });
    }
}

// 初始化编辑用户select2组件
function initializeEditUserSelect2() {
    // 初始化编辑用户角色选择框
    if ($('#editUserRoleSelect').length) {
        $('#editUserRoleSelect').select2({
            placeholder: '请选择用户角色',
            allowClear: false,
            width: '100%',
            minimumResultsForSearch: Infinity, // 禁用搜索功能
            dropdownParent: $('body'), // 确保下拉菜单正确显示
            templateResult: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            },
            templateSelection: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            }
        });
    }

    // 初始化编辑用户状态选择框
    if ($('#editUserStatusSelect').length) {
        $('#editUserStatusSelect').select2({
            placeholder: '请选择用户状态',
            allowClear: false,
            width: '100%',
            minimumResultsForSearch: Infinity, // 禁用搜索功能
            dropdownParent: $('body'), // 确保下拉菜单正确显示
            templateResult: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            },
            templateSelection: function (option) {
                if (!option.id) return option.text;
                const icon = $(option.element).data('icon');
                const color = $(option.element).data('color');
                if (icon) {
                    return $('<span><i class="fa ' + icon + ' ' + color + ' mr-2"></i>' + option.text + '</span>');
                }
                return option.text;
            }
        });
    }
}

// 权限检查：控制添加用户按钮显示
function checkUserPermissions() {
    // 只要有一个user.canEdit为true且user.role为'admin'，则显示添加用户按钮
    const isAdmin = users.some(u => u.role === 'admin' && u.canEdit);
    const addUserBtn = document.getElementById('addUserBtn');
    if (addUserBtn) {
        addUserBtn.style.display = isAdmin ? '' : 'none';
    }
}
