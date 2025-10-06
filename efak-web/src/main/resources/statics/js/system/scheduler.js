/**
* 任务调度管理模块
* Author: Mr.SmartLoli
* Date: 2025/1/27 10:00
* Version: 1.0
*/

class TaskSchedulerManager {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 5;
        this.totalTasks = 0;
        this.totalPages = 1;
        this.currentTask = null;
        this.historyCurrentPage = 1;
        this.historyPageSize = 5;
        this.historyTotalTasks = 0;
        this.historyTotalPages = 1;
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadTaskStats();
        this.loadTaskList();
        this.loadHistoryList();
    }

    bindEvents() {
        // 搜索按钮
        document.getElementById('searchBtn').addEventListener('click', () => {
            this.currentPage = 1;
            this.loadTaskList();
        });

        // 重置按钮
        document.getElementById('resetBtn').addEventListener('click', () => {
            this.resetFilters();
            this.currentPage = 1;
            this.loadTaskList();
        });


        // 关闭模态框
        document.getElementById('closeTaskDetailModal').addEventListener('click', () => {
            this.hideTaskDetailModal();
        });

        document.getElementById('closeTaskFormModal').addEventListener('click', () => {
            this.hideTaskFormModal();
        });

        // 取消按钮
        document.getElementById('cancelTaskBtn').addEventListener('click', () => {
            this.hideTaskFormModal();
        });

        // 表单提交
        document.getElementById('taskForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveTask();
        });

        // 点击模态框外部关闭
        document.getElementById('taskDetailModal').addEventListener('click', (e) => {
            if (e.target === e.currentTarget) {
                this.hideTaskDetailModal();
            }
        });

        document.getElementById('taskFormModal').addEventListener('click', (e) => {
            if (e.target === e.currentTarget) {
                this.hideTaskFormModal();
            }
        });

        // 历史执行相关事件
        document.getElementById('searchHistoryBtn').addEventListener('click', () => {
            this.historyCurrentPage = 1;
            this.loadHistoryList();
        });

        document.getElementById('resetHistoryBtn').addEventListener('click', () => {
            this.resetHistoryFilters();
            this.historyCurrentPage = 1;
            this.loadHistoryList();
        });

        document.getElementById('refreshHistoryBtn').addEventListener('click', () => {
            this.loadHistoryList();
        });
    }

    resetFilters() {
        document.getElementById('taskNameFilter').value = '';
        $('#taskTypeFilter').val('').trigger('change');
        $('#statusFilter').val('').trigger('change');
    }

    resetHistoryFilters() {
        document.getElementById('historyTaskNameFilter').value = '';
        $('#historyStatusFilter').val('').trigger('change');
        document.getElementById('historyStartDate').value = '';
        document.getElementById('historyEndDate').value = '';
    }

    async loadTaskStats() {
        try {
            const response = await fetch('/api/scheduler/stats');
            const data = await response.json();

            if (response.ok) {
                document.getElementById('totalTasks').textContent = data.totalTasks || 0;
                document.getElementById('enabledTasks').textContent = data.enabledTasks || 0;
                document.getElementById('runningTasks').textContent = data.runningTasks || 0;
                document.getElementById('errorTasks').textContent = data.errorTasks || 0;
            } else {
                console.error('Failed to load task stats:', data);
                // 设置默认值
                document.getElementById('totalTasks').textContent = '0';
                document.getElementById('enabledTasks').textContent = '0';
                document.getElementById('runningTasks').textContent = '0';
                document.getElementById('errorTasks').textContent = '0';
            }
        } catch (error) {
            console.error('Failed to load task stats:', error);
            // 设置默认值
            document.getElementById('totalTasks').textContent = '0';
            document.getElementById('enabledTasks').textContent = '0';
            document.getElementById('runningTasks').textContent = '0';
            document.getElementById('errorTasks').textContent = '0';
        }
    }

    async loadTaskList() {
        try {
            const params = new URLSearchParams({
                page: this.currentPage,
                size: this.pageSize,
                taskName: document.getElementById('taskNameFilter').value,
                taskType: $('#taskTypeFilter').val(),
                status: $('#statusFilter').val()
            });

            const response = await fetch(`/api/scheduler?${params}`);
            const data = await response.json();

            if (response.ok) {
                this.totalTasks = data.total || 0;
                this.totalPages = data.totalPages || 1;
                this.renderTaskList(data.tasks || data.data || []);
                this.renderPagination(data.total, data.page, data.size, data.totalPages);
                this.updatePaginationInfo();
            } else {
                console.error('Failed to load task list:', data);
                this.showMessage('加载任务列表失败: ' + (data.message || data.error || '未知错误'), 'error');
            }
        } catch (error) {
            console.error('Failed to load task list:', error);
            this.showMessage('加载任务列表失败: ' + error.message, 'error');
        }
    }

    async loadHistoryList() {
        try {
            const params = new URLSearchParams({
                page: this.historyCurrentPage,
                size: this.historyPageSize,
                taskName: document.getElementById('historyTaskNameFilter').value,
                status: $('#historyStatusFilter').val(),
                startDate: document.getElementById('historyStartDate').value,
                endDate: document.getElementById('historyEndDate').value
            });

            const response = await fetch(`/api/scheduler/history?${params}`);
            const data = await response.json();

            if (response.ok) {
                this.historyTotalTasks = data.total || 0;
                this.historyTotalPages = data.totalPages || 1;
                this.renderHistoryList(data.history || data.data || []);
                this.renderHistoryPagination(data.total, data.page, data.size, data.totalPages);
                this.updateHistoryPaginationInfo();
            } else {
                this.showMessage('加载历史记录失败: ' + (data.message || data.error || '未知错误'), 'error');
            }
        } catch (error) {
            this.showMessage('加载历史记录失败: ' + error.message, 'error');
        }
    }

    renderTaskList(tasks) {
        const tbody = document.getElementById('schedulerTableBody');
        tbody.innerHTML = '';

        if (!tasks || tasks.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center py-8 text-gray-500">暂无数据</td></tr>';
            return;
        }

        tasks.forEach(task => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <div class="font-medium text-gray-900">${task.taskName}</div>
                </td>
                <td>
                    <span class="task-type ${task.taskType}">${this.getTaskTypeName(task.taskType)}</span>
                </td>
                <td>
                    <span class="cron-expression">${task.cronExpression}</span>
                </td>
                <td>
                    <div class="task-description" title="${task.description || '-'}">${task.description || '-'}</div>
                </td>
                <td>
                    <span class="task-status ${task.status}">${this.getStatusName(task.status)}</span>
                </td>
                <td>
                    <div class="text-center">
                        <span class="execution-count">${task.executeCount || 0}</span>
                    </div>
                </td>
                <td>
                    <div class="text-sm text-gray-600">${task.lastExecuteTime || '-'}</div>
                </td>
                <td>
                    <div class="text-sm text-gray-600">${task.nextExecuteTime || '-'}</div>
                </td>
                <td>
                    <div class="task-actions">
                        <button class="action-btn-icon view" onclick="taskSchedulerManager.viewTaskDetail(${task.id})">
                            <i class="fa fa-eye"></i>
                        </button>
                        ${task.status === 'enabled' ?
                    `<button class="action-btn-icon disable" onclick="taskSchedulerManager.disableTask(${task.id})">
                        <i class="fa fa-pause"></i>
                    </button>` :
                    `<button class="action-btn-icon enable" onclick="taskSchedulerManager.enableTask(${task.id})">
                        <i class="fa fa-play"></i>
                    </button>`
                }
                        <button class="action-btn-icon execute" onclick="taskSchedulerManager.executeTask(${task.id})">
                            <i class="fa fa-bolt"></i>
                        </button>
                        <button class="action-btn-icon edit" onclick="taskSchedulerManager.editTask(${task.id})">
                            <i class="fa fa-edit"></i>
                        </button>

                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    renderPagination(total, currentPage, pageSize, totalPages) {
        const pagination = document.getElementById('pagination');
        if (!pagination) {
            return;
        }

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
        html += `<button id="first-page" class="pagination-btn" title="首页" ${currentPage === 1 ? 'disabled' : ''} onclick="taskSchedulerManager.gotoPage(1)">
            <i class="fa fa-angle-double-left"></i>
        </button>`;

        // 上一页按钮
        html += `<button id="prev-page" class="pagination-btn" title="上一页" ${currentPage === 1 ? 'disabled' : ''} onclick="taskSchedulerManager.gotoPage(${currentPage - 1})">
            <i class="fa fa-angle-left"></i>
        </button>`;

        // 页码按钮
        html += `<div class="pagination-pages">`;
        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            html += `<button class="page-btn ${isActive ? 'active' : ''}" onclick="taskSchedulerManager.gotoPage(${i})">${i}</button>`;
        }
        html += `</div>`;

        // 下一页按钮
        html += `<button id="next-page" class="pagination-btn" title="下一页" ${currentPage === totalPages ? 'disabled' : ''} onclick="taskSchedulerManager.gotoPage(${currentPage + 1})">
            <i class="fa fa-angle-right"></i>
        </button>`;

        // 末页按钮
        html += `<button id="last-page" class="pagination-btn" title="末页" ${currentPage === totalPages ? 'disabled' : ''} onclick="taskSchedulerManager.gotoPage(${totalPages})">
            <i class="fa fa-angle-double-right"></i>
        </button>`;

        pagination.innerHTML = html;
    }

    renderHistoryList(history) {
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '';

        if (!history || history.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-8 text-gray-500">暂无历史执行记录</td></tr>';
            return;
        }

        history.forEach(record => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <div class="font-medium text-gray-900" title="${record.task_name || record.taskName || '-'}">${record.task_name || record.taskName || '-'}</div>
                </td>
                <td>
                    <span class="task-type ${record.task_type || record.taskType}" title="${this.getTaskTypeName(record.task_type || record.taskType)}">${this.getTaskTypeName(record.task_type || record.taskType)}</span>
                </td>
                <td>
                    <span class="execution-status ${record.execution_status || record.executionStatus}" title="${this.getExecutionStatusName(record.execution_status || record.executionStatus)}">${this.getExecutionStatusName(record.execution_status || record.executionStatus)}</span>
                </td>
                <td>
                    <div class="end-time" title="${this.formatDateTime(record.end_time || record.endTime)}">${this.formatDateTimeCompact(record.end_time || record.endTime)}</div>
                </td>
                <td>
                    <div class="executor-node" title="${record.executor_node || record.executorNode || '-'}">${record.executor_node || record.executorNode || '-'}</div>
                </td>
                <td>
                    <div class="trigger-type" title="${this.getTriggerTypeName(record.trigger_type || record.triggerType)}">${this.getTriggerTypeName(record.trigger_type || record.triggerType)}</div>
                </td>
                <td>
                    <div class="task-actions">
                        <button class="action-btn-icon view" onclick="taskSchedulerManager.viewHistoryDetail(${record.id})" title="查看详情">
                            <i class="fa fa-eye"></i>
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    renderHistoryPagination(total, currentPage, pageSize, totalPages) {
        const pagination = document.getElementById('historyPagination');
        if (!pagination) {
            return;
        }

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
        html += `<button id="history-first-page" class="pagination-btn" title="首页" ${currentPage === 1 ? 'disabled' : ''} onclick="taskSchedulerManager.gotoHistoryPage(1)">
            <i class="fa fa-angle-double-left"></i>
        </button>`;

        // 上一页按钮
        html += `<button id="history-prev-page" class="pagination-btn" title="上一页" ${currentPage === 1 ? 'disabled' : ''} onclick="taskSchedulerManager.gotoHistoryPage(${currentPage - 1})">
            <i class="fa fa-angle-left"></i>
        </button>`;

        // 页码按钮
        html += `<div class="pagination-pages">`;
        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            html += `<button class="page-btn ${isActive ? 'active' : ''}" onclick="taskSchedulerManager.gotoHistoryPage(${i})">${i}</button>`;
        }
        html += `</div>`;

        // 下一页按钮
        html += `<button id="history-next-page" class="pagination-btn" title="下一页" ${currentPage === totalPages ? 'disabled' : ''} onclick="taskSchedulerManager.gotoHistoryPage(${currentPage + 1})">
            <i class="fa fa-angle-right"></i>
        </button>`;

        // 末页按钮
        html += `<button id="history-last-page" class="pagination-btn" title="末页" ${currentPage === totalPages ? 'disabled' : ''} onclick="taskSchedulerManager.gotoHistoryPage(${totalPages})">
            <i class="fa fa-angle-double-right"></i>
        </button>`;

        pagination.innerHTML = html;
    }

    getTaskTypeName(taskType) {
        const typeNames = {
            'topic_monitor': '主题监控',
            'consumer_monitor': '消费者监控',
            'cluster_monitor': '集群监控',
            'alert_monitor': '告警监控',
            'data_cleanup': '数据清理',
            'performance_stats': '性能统计'
        };
        return typeNames[taskType] || taskType;
    }

    // 专门用于历史表格的紧凑任务类型名称
    getTaskTypeNameCompact(taskType) {
        const typeNames = {
            'topic_monitor': '主题',
            'consumer_monitor': '消费者',
            'cluster_monitor': '集群',
            'alert_cleanup': '告警',
            'data_cleanup': '数据',
            'performance_stats': '性能'
        };
        return typeNames[taskType] || taskType;
    }

    getStatusName(status) {
        const statusNames = {
            'enabled': '启用',
            'disabled': '禁用',
            'running': '运行中',
            'error': '错误'
        };
        return statusNames[status] || status;
    }

    getExecutionStatusName(status) {
        const statusNames = {
            'SUCCESS': '成功',
            'FAILED': '失败',
            'RUNNING': '运行中',
            'CANCELLED': '已取消'
        };
        return statusNames[status] || status;
    }

    // 专门用于历史表格的紧凑执行状态名称
    getExecutionStatusNameCompact(status) {
        const statusNames = {
            'SUCCESS': '成功',
            'FAILED': '失败',
            'RUNNING': '运行',
            'CANCELLED': '取消'
        };
        return statusNames[status] || status;
    }

    getTriggerTypeName(triggerType) {
        const typeNames = {
            'SCHEDULED': '定时触发',
            'MANUAL': '手动触发'
        };
        return typeNames[triggerType] || triggerType;
    }

    // 专门用于历史表格的紧凑触发类型名称
    getTriggerTypeNameCompact(triggerType) {
        const typeNames = {
            'SCHEDULED': '定时',
            'MANUAL': '手动'
        };
        return typeNames[triggerType] || triggerType;
    }

    formatDuration(duration) {
        if (!duration) return '-';

        const seconds = Math.floor(duration / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);

        if (hours > 0) {
            return `${hours}小时${minutes % 60}分钟`;
        } else if (minutes > 0) {
            return `${minutes}分钟${seconds % 60}秒`;
        } else {
            return `${seconds}秒`;
        }
    }

    // 专门用于历史表格的紧凑持续时间格式化
    formatDurationCompact(duration) {
        if (!duration) return '-';

        const seconds = Math.floor(duration / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);

        if (hours > 0) {
            return `${hours}h${minutes % 60}m`;
        } else if (minutes > 0) {
            return `${minutes}m${seconds % 60}s`;
        } else {
            return `${seconds}s`;
        }
    }

    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';

        try {
            const date = new Date(dateTimeStr);
            if (isNaN(date.getTime())) return dateTimeStr;

            return date.toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        } catch (error) {
            return dateTimeStr;
        }
    }

    // 专门用于历史表格的紧凑日期时间格式化
    formatDateTimeCompact(dateTimeStr) {
        if (!dateTimeStr) return '-';

        try {
            const date = new Date(dateTimeStr);
            if (isNaN(date.getTime())) return dateTimeStr;

            const now = new Date();
            const diffMs = now - date;
            const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

            // 如果是今天，只显示时间
            if (diffDays === 0) {
                return date.toLocaleTimeString('zh-CN', {
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                });
            }
            // 如果是昨天，显示"昨天 + 时间"
            else if (diffDays === 1) {
                return '昨天 ' + date.toLocaleTimeString('zh-CN', {
                    hour: '2-digit',
                    minute: '2-digit'
                });
            }
            // 如果是一周内，显示"周几 + 时间"
            else if (diffDays < 7) {
                const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
                return weekdays[date.getDay()] + ' ' + date.toLocaleTimeString('zh-CN', {
                    hour: '2-digit',
                    minute: '2-digit'
                });
            }
            // 其他情况显示完整日期时间
            else {
                return date.toLocaleString('zh-CN', {
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            }
        } catch (error) {
            return dateTimeStr;
        }
    }

    async enableTask(id) {
        try {
            const response = await fetch(`/api/scheduler/enable/${id}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (response.ok && data.success) {
                this.showMessage('任务已成功启用', 'success');
                this.loadTaskList();
                this.loadTaskStats();
            } else {
                this.showMessage(data.message || '启用任务失败', 'error');
            }
        } catch (error) {
            console.error('Failed to enable task:', error);
            this.showMessage('启用任务失败: ' + error.message, 'error');
        }
    }

    async disableTask(id) {
        try {
            const response = await fetch(`/api/scheduler/disable/${id}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (response.ok && data.success) {
                this.showMessage('任务已成功暂停', 'success');
                this.loadTaskList();
                this.loadTaskStats();
            } else {
                this.showMessage(data.message || '暂停任务失败', 'error');
            }
        } catch (error) {
            console.error('Failed to disable task:', error);
            this.showMessage('暂停任务失败: ' + error.message, 'error');
        }
    }

    async executeTask(id) {
        // 显示确认对话框
        const confirmed = await this.showConfirmDialog('确认立即执行', '您确定要立即执行这个定时任务吗？');

        if (!confirmed) {
            return;
        }

        try {
            const response = await fetch(`/api/scheduler/execute/${id}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (response.ok && data.success) {
                this.showMessage('任务已开始执行', 'success');
                this.loadTaskList();
            } else {
                this.showMessage(data.message || '执行任务失败', 'error');
            }
        } catch (error) {
            console.error('Failed to execute task:', error);
            this.showMessage('执行任务失败', 'error');
        }
    }

    // 显示确认对话框
    showConfirmDialog(title, message) {
        return new Promise((resolve) => {
            // 创建遮罩层
            const overlay = document.createElement('div');
            overlay.className = 'confirm-overlay';
            overlay.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 10000;
                backdrop-filter: blur(4px);
            `;

            // 创建对话框
            const dialog = document.createElement('div');
            dialog.className = 'confirm-dialog';
            dialog.style.cssText = `
                background: white;
                border-radius: 12px;
                padding: 24px;
                max-width: 400px;
                width: 90%;
                box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                transform: scale(0.9);
                opacity: 0;
                transition: all 0.3s ease;
                position: relative;
            `;

            // 创建图标
            const icon = document.createElement('div');
            icon.className = 'confirm-icon';
            icon.innerHTML = '<i class="fa fa-bolt" style="color: #f59e0b; font-size: 24px;"></i>';
            icon.style.cssText = `
                text-align: center;
                margin-bottom: 16px;
            `;

            // 创建标题
            const titleElement = document.createElement('h3');
            titleElement.textContent = title;
            titleElement.style.cssText = `
                margin: 0 0 12px 0;
                font-size: 18px;
                font-weight: 600;
                color: #1f2937;
                text-align: center;
            `;

            // 创建消息
            const messageElement = document.createElement('p');
            messageElement.textContent = message;
            messageElement.style.cssText = `
                margin: 0 0 24px 0;
                font-size: 14px;
                color: #6b7280;
                text-align: center;
                line-height: 1.5;
            `;

            // 创建按钮容器
            const buttonContainer = document.createElement('div');
            buttonContainer.style.cssText = `
                display: flex;
                gap: 12px;
                justify-content: center;
            `;

            // 创建取消按钮
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '取消';
            cancelButton.className = 'confirm-btn cancel';
            cancelButton.style.cssText = `
                padding: 10px 20px;
                border: 1px solid #d1d5db;
                background: white;
                color: #374151;
                border-radius: 6px;
                font-size: 14px;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.2s ease;
                min-width: 80px;
            `;

            // 创建确认按钮
            const confirmButton = document.createElement('button');
            confirmButton.textContent = '确认执行';
            confirmButton.className = 'confirm-btn confirm';
            confirmButton.style.cssText = `
                padding: 10px 20px;
                border: none;
                background: #f59e0b;
                color: white;
                border-radius: 6px;
                font-size: 14px;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.2s ease;
                min-width: 80px;
            `;

            // 添加按钮悬停效果
            cancelButton.addEventListener('mouseenter', () => {
                cancelButton.style.background = '#f3f4f6';
                cancelButton.style.borderColor = '#9ca3af';
            });
            cancelButton.addEventListener('mouseleave', () => {
                cancelButton.style.background = 'white';
                cancelButton.style.borderColor = '#d1d5db';
            });

            confirmButton.addEventListener('mouseenter', () => {
                confirmButton.style.background = '#d97706';
            });
            confirmButton.addEventListener('mouseleave', () => {
                confirmButton.style.background = '#f59e0b';
            });

            // 添加按钮点击事件
            cancelButton.addEventListener('click', () => {
                this.closeConfirmDialog(overlay, false);
                resolve(false);
            });

            confirmButton.addEventListener('click', () => {
                this.closeConfirmDialog(overlay, true);
                resolve(true);
            });

            // 添加ESC键关闭
            const handleEscKey = (e) => {
                if (e.key === 'Escape') {
                    this.closeConfirmDialog(overlay, false);
                    resolve(false);
                    document.removeEventListener('keydown', handleEscKey);
                }
            };
            document.addEventListener('keydown', handleEscKey);

            // 添加遮罩层点击关闭
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    this.closeConfirmDialog(overlay, false);
                    resolve(false);
                }
            });

            // 组装对话框
            buttonContainer.appendChild(cancelButton);
            buttonContainer.appendChild(confirmButton);
            dialog.appendChild(icon);
            dialog.appendChild(titleElement);
            dialog.appendChild(messageElement);
            dialog.appendChild(buttonContainer);
            overlay.appendChild(dialog);

            // 添加到页面
            document.body.appendChild(overlay);

            // 显示动画
            setTimeout(() => {
                dialog.style.transform = 'scale(1)';
                dialog.style.opacity = '1';
            }, 10);
        });
    }

    // 关闭确认对话框
    closeConfirmDialog(overlay, confirmed) {
        const dialog = overlay.querySelector('.confirm-dialog');

        // 关闭动画
        dialog.style.transform = 'scale(0.9)';
        dialog.style.opacity = '0';

        setTimeout(() => {
            if (overlay.parentNode) {
                overlay.parentNode.removeChild(overlay);
            }
        }, 300);
    }


    async editTask(id) {
        try {
            const response = await fetch(`/api/scheduler/${id}`);
            const data = await response.json();

            if (response.ok && data.success) {
                this.currentTask = data.data;
                this.showTaskForm(true);
            } else {
                this.showMessage(data.message || '获取任务信息失败', 'error');
            }
        } catch (error) {
            console.error('Failed to get task:', error);
            this.showMessage('获取任务信息失败', 'error');
        }
    }

    async viewTaskDetail(id) {
        try {
            const response = await fetch(`/api/scheduler/${id}`);
            const data = await response.json();

            if (response.ok && data.success) {
                this.showTaskDetailModal(data.data);
            } else {
                this.showMessage(data.message || '获取任务详情失败', 'error');
            }
        } catch (error) {
            console.error('Failed to get task detail:', error);
            this.showMessage('获取任务详情失败', 'error');
        }
    }

    showTaskForm(isEdit = true) {
        const modal = document.getElementById('taskFormModal');
        const title = document.getElementById('taskFormTitle');
        const form = document.getElementById('taskForm');

        title.textContent = '编辑任务';

        if (isEdit && this.currentTask) {
            // 填充表单数据
            document.getElementById('cronExpression').value = this.currentTask.cronExpression;
            document.getElementById('description').value = this.currentTask.description || '';
            document.getElementById('timeout').value = this.currentTask.timeout || 300;
        } else {
            // 清空表单
            form.reset();
            this.currentTask = null;
        }

        modal.classList.remove('hidden');
    }

    hideTaskFormModal() {
        document.getElementById('taskFormModal').classList.add('hidden');
        this.currentTask = null;
    }

    async saveTask() {
        // 只发送需要更新的字段
        const formData = {
            cronExpression: document.getElementById('cronExpression').value,
            description: document.getElementById('description').value,
            timeout: parseInt(document.getElementById('timeout').value)
        };

        try {
            const url = `/api/scheduler/${this.currentTask.id}`;
            const method = 'PUT';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                this.showMessage('任务已成功保存', 'success');
                this.hideTaskFormModal();
                this.loadTaskList();
                this.loadTaskStats();
            } else {
                this.showMessage(data.message || '更新任务失败', 'error');
            }
        } catch (error) {
            console.error('Failed to update task:', error);
            this.showMessage('更新任务失败', 'error');
        }
    }

    showTaskDetailModal(task) {
        const modal = document.getElementById('taskDetailModal');
        const content = document.getElementById('taskDetailContent');

        // 检查task是否存在
        if (!task) {
            this.showMessage('任务信息不存在', 'error');
            return;
        }

        content.innerHTML = `
            <div class="task-detail-container">
                <!-- 基本信息卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-info-circle text-blue-500"></i>
                        <h4 class="detail-card-title">基本信息</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-tag text-gray-400"></i>
                                    任务名称
                                </label>
                                <div class="detail-value">${task.taskName || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-cog text-gray-400"></i>
                                    任务类型
                                </label>
                                <div class="detail-value">
                                    <span class="task-type ${task.taskType || ''}">${this.getTaskTypeName(task.taskType) || '-'}</span>
                                </div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-clock text-gray-400"></i>
                                    任务状态
                                </label>
                                <div class="detail-value">
                                    <span class="task-status ${task.status || ''}">${this.getStatusName(task.status) || '-'}</span>
                                </div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-calendar text-gray-400"></i>
                                    创建时间
                                </label>
                                <div class="detail-value">${task.createTime || '-'}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 调度配置卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-clock text-green-500"></i>
                        <h4 class="detail-card-title">调度配置</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-grid">
                            <div class="detail-item full-width">
                                <label class="detail-label">
                                    <i class="fa fa-code text-gray-400"></i>
                                    Cron表达式
                                </label>
                                <div class="detail-value">
                                    <code class="cron-code">${task.cronExpression || '-'}</code>
                                </div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-hourglass-half text-gray-400"></i>
                                    超时时间
                                </label>
                                <div class="detail-value">${task.timeout || 300} 秒</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-calendar-check text-gray-400"></i>
                                    下次执行
                                </label>
                                <div class="detail-value">${task.nextExecuteTime || '-'}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 执行统计卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-chart-bar text-purple-500"></i>
                        <h4 class="detail-card-title">执行统计</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="stats-grid-detail">
                            <div class="stat-item">
                                <div class="stat-number">${task.executeCount || 0}</div>
                                <div class="stat-label">总执行次数</div>
                            </div>
                            <div class="stat-item success">
                                <div class="stat-number">${task.successCount || 0}</div>
                                <div class="stat-label">成功次数</div>
                            </div>
                            <div class="stat-item error">
                                <div class="stat-number">${task.failCount || 0}</div>
                                <div class="stat-label">失败次数</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-number">${task.executeCount && task.successCount ? Math.round((task.successCount / task.executeCount) * 100) : 0}%</div>
                                <div class="stat-label">成功率</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 最近执行卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-history text-orange-500"></i>
                        <h4 class="detail-card-title">最近执行</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-calendar-alt text-gray-400"></i>
                                    上次执行时间
                                </label>
                                <div class="detail-value">${task.lastExecuteTime || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-check-circle text-gray-400"></i>
                                    执行结果
                                </label>
                                <div class="detail-value">
                                    <span class="execution-status ${task.lastExecuteResult === 'SUCCESS' ? 'SUCCESS' : task.lastExecuteResult === 'FAILED' ? 'FAILED' : 'RUNNING'}">
                                        ${task.lastExecuteResult || '-'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 任务描述卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-align-left text-indigo-500"></i>
                        <h4 class="detail-card-title">任务描述</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-description">
                            ${task.description || '暂无描述'}
                        </div>
                    </div>
                </div>

                <!-- 错误信息卡片 -->
                ${task.errorMessage ? `
                <div class="detail-card error-card">
                    <div class="detail-card-header">
                        <i class="fa fa-exclamation-triangle text-red-500"></i>
                        <h4 class="detail-card-title">错误信息</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="error-message">
                            ${task.errorMessage}
                        </div>
                    </div>
                </div>
                ` : ''}

                <!-- 任务配置卡片 -->
                ${task.config ? `
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-cogs text-teal-500"></i>
                        <h4 class="detail-card-title">任务配置</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="config-content">
                            <pre class="config-code">${task.config}</pre>
                        </div>
                    </div>
                </div>
                ` : ''}
            </div>
        `;

        modal.classList.remove('hidden');
    }

    hideTaskDetailModal() {
        document.getElementById('taskDetailModal').classList.add('hidden');
    }

    async viewHistoryDetail(id) {
        try {
            const response = await fetch(`/api/scheduler/history/detail/${id}`);
            const data = await response.json();

            if (response.ok) {
                this.showHistoryDetailModal(data.history);
            } else {
                this.showMessage(data.error || '获取历史详情失败', 'error');
            }
        } catch (error) {
            console.error('Failed to get history detail:', error);
            this.showMessage('获取历史详情失败', 'error');
        }
    }

    showHistoryDetailModal(record) {
        const modal = document.getElementById('taskDetailModal');
        const content = document.getElementById('taskDetailContent');

        content.innerHTML = `
            <div class="task-detail-container">
                <!-- 基本信息卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-info-circle text-blue-500"></i>
                        <h4 class="detail-card-title">基本信息</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-tag text-gray-400"></i>
                                    任务名称
                                </label>
                                <div class="detail-value">${record.task_name || record.taskName || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-cog text-gray-400"></i>
                                    任务类型
                                </label>
                                <div class="detail-value">
                                    <span class="task-type ${record.task_type || record.taskType || ''}">${this.getTaskTypeName(record.task_type || record.taskType) || '-'}</span>
                                </div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-check-circle text-gray-400"></i>
                                    执行状态
                                </label>
                                <div class="detail-value">
                                    <span class="execution-status ${record.execution_status || record.executionStatus || ''}">${this.getExecutionStatusName(record.execution_status || record.executionStatus) || '-'}</span>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>

                <!-- 执行信息卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-clock text-green-500"></i>
                        <h4 class="detail-card-title">执行信息</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-play text-gray-400"></i>
                                    开始时间
                                </label>
                                <div class="detail-value">${this.formatDateTime(record.start_time || record.startTime) || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-stop text-gray-400"></i>
                                    结束时间
                                </label>
                                <div class="detail-value">${this.formatDateTime(record.end_time || record.endTime) || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-bolt text-gray-400"></i>
                                    触发类型
                                </label>
                                <div class="detail-value">${this.getTriggerTypeName(record.trigger_type || record.triggerType) || '-'}</div>
                            </div>
                            <div class="detail-item">
                                <label class="detail-label">
                                    <i class="fa fa-calendar text-gray-400"></i>
                                    触发时间
                                </label>
                                <div class="detail-value">${this.formatDateTime(record.trigger_time || record.triggerTime || record.start_time || record.startTime) || '-'}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 执行结果卡片 -->
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-chart-line text-purple-500"></i>
                        <h4 class="detail-card-title">执行结果</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="detail-description">
                            ${record.result_message || record.resultMessage || '暂无执行结果'}
                        </div>
                    </div>
                </div>

                <!-- 错误信息卡片 -->
                ${(record.error_message || record.errorMessage) ? `
                <div class="detail-card error-card">
                    <div class="detail-card-header">
                        <i class="fa fa-exclamation-triangle text-red-500"></i>
                        <h4 class="detail-card-title">错误信息</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="error-message">
                            ${record.error_message || record.errorMessage}
                        </div>
                    </div>
                </div>
                ` : ''}

                <!-- 输入参数卡片 -->
                ${(record.input_params || record.inputParams) ? `
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-sign-in-alt text-indigo-500"></i>
                        <h4 class="detail-card-title">输入参数</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="config-content">
                            <pre class="config-code">${record.input_params || record.inputParams}</pre>
                        </div>
                    </div>
                </div>
                ` : ''}

                <!-- 输出结果卡片 -->
                ${(record.output_result || record.outputResult) ? `
                <div class="detail-card">
                    <div class="detail-card-header">
                        <i class="fa fa-sign-out-alt text-teal-500"></i>
                        <h4 class="detail-card-title">输出结果</h4>
                    </div>
                    <div class="detail-card-content">
                        <div class="config-content">
                            <pre class="config-code">${record.output_result || record.outputResult}</pre>
                        </div>
                    </div>
                </div>
                ` : ''}
            </div>
        `;

        modal.classList.remove('hidden');
    }

    gotoPage(page) {
        if (page < 1 || page > this.totalPages) return;

        this.currentPage = page;
        this.loadTaskList();
        this.updatePaginationInfo();
    }

    gotoHistoryPage(page) {
        if (page < 1 || page > this.historyTotalPages) return;

        this.historyCurrentPage = page;
        this.loadHistoryList();
        this.updateHistoryPaginationInfo();
    }

    updatePaginationInfo() {
        const startRecord = (this.currentPage - 1) * this.pageSize + 1;
        const endRecord = Math.min(this.currentPage * this.pageSize, this.totalTasks);

        const pageStart = document.getElementById('page-start');
        const pageEnd = document.getElementById('page-end');
        const totalCount = document.getElementById('total-count');

        if (pageStart) pageStart.textContent = startRecord;
        if (pageEnd) pageEnd.textContent = endRecord;
        if (totalCount) totalCount.textContent = this.totalTasks;
    }

    updateHistoryPaginationInfo() {
        const startRecord = (this.historyCurrentPage - 1) * this.historyPageSize + 1;
        const endRecord = Math.min(this.historyCurrentPage * this.historyPageSize, this.historyTotalTasks);

        const pageStart = document.getElementById('history-page-start');
        const pageEnd = document.getElementById('history-page-end');
        const totalCount = document.getElementById('history-total-count');

        if (pageStart) pageStart.textContent = startRecord;
        if (pageEnd) pageEnd.textContent = endRecord;
        if (totalCount) totalCount.textContent = this.historyTotalTasks;
    }

    showMessage(message, type = 'info') {
        // 创建消息提示元素
        const messageDiv = document.createElement('div');
        messageDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            max-width: 400px;
            word-wrap: break-word;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            transition: all 0.3s ease;
        `;

        // 根据类型设置样式
        if (type === 'error') {
            messageDiv.style.backgroundColor = '#ef4444';
        } else if (type === 'success') {
            messageDiv.style.backgroundColor = '#10b981';
        } else {
            messageDiv.style.backgroundColor = '#3b82f6';
        }

        messageDiv.textContent = message;
        document.body.appendChild(messageDiv);

        // 3秒后自动移除
        setTimeout(() => {
            messageDiv.style.opacity = '0';
            messageDiv.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (messageDiv.parentNode) {
                    messageDiv.parentNode.removeChild(messageDiv);
                }
            }, 300);
        }, 3000);
    }
}

// 初始化任务调度管理器
const taskSchedulerManager = new TaskSchedulerManager();