// AI助手 JavaScript 模块
(function () {
    'use strict';

    // 全局变量
    let currentModel = '';
    let currentModelId = '';
    let currentClusterId = ''; // 从URL参数cid获取的集群ID
    let conversationHistory = [];
    let isTyping = false;
    let modelConfigs = [];
    let currentSessionId = null;
    let currentUsername = 'admin'; // 默认用户名，实际应该从登录信息获取
    let isLoadingHistory = false; // 是否正在加载历史消息
    let eventSource = null; // SSE连接
    let isStreaming = false; // 是否正在流式传输
    let currentStreamId = null; // 当前流式传输ID
    let historySessions = [];
    let historyPage = 1;
    const historyPageSize = 8;
    let historyQuery = '';
    let isAdminUser = false;
    let lastSkillId = '';
    const INPUT_HISTORY_KEY = 'efak.ai.inputHistory';
    const INPUT_HISTORY_MAX = 80;
    const SLASH_COMMANDS = [
        { cmd: '/new', label: '新建对话', desc: '清空当前会话并开始新对话' }
    ];
    let inputHistory = [];
    let inputHistoryIndex = -1;
    let inputDraft = '';
    let slashActiveIndex = 0;
    let stickToBottom = true;
    let scrollFrame = 0;

    const INTENT_CARDS = [
        { id: 'topic_inspect', label: 'Topic 体检', desc: '存在性、分区、ISR 与副本', icon: 'fa-layer-group', adminOnly: false,
            prompt: '帮我看看当前选中集群里，我关心的 topic 是否存在，分区、ISR 和副本是否正常。',
            followUps: ['查这个 topic 的最近消息', '谁在消费这个 topic？', '生产和消费速度是否匹配？'] },
        { id: 'consumer_lag', label: '消费积压', desc: 'lag 与消费速度是否卡住', icon: 'fa-gauge-high', adminOnly: false,
            prompt: '帮我诊断当前选中集群的消费积压（lag）和消费速度。',
            followUps: ['这个消费者组是否卡住？', '看一下生产和消费速度', '列出这个 topic 的消费者'] },
        { id: 'message_lookup', label: '查最近消息', desc: '按关键字查看最近 100 条', icon: 'fa-inbox', adminOnly: false,
            prompt: '帮我查当前选中集群某个 topic 的最近消息（默认最近 100 条）。',
            followUps: ['缩小到某个分区再查', '对照消费积压看一下', '检查 topic 分区是否倾斜'] },
        { id: 'alert_triage', label: '当前告警', desc: '未处理告警与关联对象', icon: 'fa-bell', adminOnly: false,
            prompt: '当前选中集群有哪些告警？',
            followUps: ['告警对应的 topic 是否健康？', '和 Broker 在线情况对照一下', '给出处理建议'] },
        { id: 'cluster_health', label: '集群健康简报', desc: 'Broker 在线、资源与性能', icon: 'fa-heart-pulse', adminOnly: true,
            prompt: '给一份当前选中集群的健康简报：broker 在线、资源与性能，不要编造磁盘或 controller。',
            followUps: ['哪些 Broker 资源最紧张？', '最近有哪些告警？', '告警渠道是否启用？'] },
        { id: 'alert_channels', label: '告警渠道', desc: '渠道是否已启用', icon: 'fa-tower-broadcast', adminOnly: true,
            prompt: '当前选中集群的告警渠道是否启用？',
            followUps: ['看看当前有哪些告警', '给一份集群健康简报', '哪些告警还没处理？'] }
    ];

    const TOOL_LABELS = {
        get_topic_info: 'Topic 信息',
        get_topic_partitions: '分区与 ISR',
        get_topic_messages: '最近消息',
        get_topic_consumers: 'Topic 消费者',
        get_topic_consume_speed: '生产/消费速度',
        get_topic_consumer_lag: '消费积压',
        get_topic_instant_metrics: '即时指标',
        get_topic_metrics_history: '历史指标',
        get_topic_config: 'Topic 配置',
        get_consumer_groups: '消费者组',
        get_consumer_members: '消费者成员',
        get_cluster_info: '集群概况',
        get_cluster_brokers: 'Broker 列表',
        get_cluster_health_snapshot: '健康快照',
        get_alerts: '告警',
        get_alert_channels: '告警渠道',
        get_broker_metrics: 'Broker 指标',
        get_performance_monitor: '性能监控'
    };

    function getClusterIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('cid') || '';
    }

    function setStage(mode) {
        const page = document.querySelector('.ai-page');
        if (!page) return;
        page.classList.toggle('is-empty', mode === 'empty');
        clearFollowUps();
    }

    function isEmptyStage() {
        const page = document.querySelector('.ai-page');
        return page ? page.classList.contains('is-empty') : false;
    }

    function renderIntentGrid() {
        const grid = document.getElementById('intent-grid');
        if (!grid) return;
        const cards = INTENT_CARDS.filter(card => !card.adminOnly || isAdminUser);
        grid.innerHTML = cards.map(card => `
            <button type="button" class="intent-card" data-intent="${card.id}">
                <div class="intent-card-label"><i class="fa-solid ${card.icon}"></i>${card.label}</div>
                <div class="intent-card-desc">${card.desc}</div>
            </button>
        `).join('');
    }

    function bindIntentGrid() {
        const grid = document.getElementById('intent-grid');
        if (!grid) return;
        grid.addEventListener('click', function (e) {
            const card = e.target.closest('.intent-card');
            if (!card) return;
            const intent = INTENT_CARDS.find(item => item.id === card.getAttribute('data-intent'));
            if (intent) {
                sendUserPrompt(intent.prompt, intent.id);
            }
        });
    }

    async function loadAgentRole() {
        try {
            const response = await fetch('/api/mcp/tools');
            if (response.ok) {
                const data = await response.json();
                isAdminUser = !!data.admin;
            }
        } catch (e) {
            isAdminUser = false;
        }
        renderIntentGrid();
        const hint = document.getElementById('agent-cluster-hint');
        if (hint) {
            hint.textContent = currentClusterId ? `集群 ${currentClusterId}` : '未选择集群';
        }
        const sub = document.getElementById('agent-empty-sub');
        if (sub && !currentClusterId) {
            sub.textContent = '先在侧栏选择集群，再描述 Topic / 消费者 / 集群问题。概念问题可以随时问。';
        }
    }

    function clearFollowUps() {
        const box = document.getElementById('follow-ups');
        if (!box) return;
        box.hidden = true;
        box.innerHTML = '';
    }

    function renderFollowUps(items) {
        const box = document.getElementById('follow-ups');
        if (!box) return;
        const list = (items || []).filter(Boolean).slice(0, 3);
        if (!list.length) {
            clearFollowUps();
            return;
        }
        box.hidden = false;
        box.innerHTML = list.map(text => `<button type="button" class="follow-up">${escapeHtml(text)}</button>`).join('');
    }

    function defaultFollowUps() {
        const skill = INTENT_CARDS.find(item => item.id === lastSkillId);
        return skill ? skill.followUps : ['帮我看看这个 topic 是否健康', '诊断一下消费积压', '当前集群有哪些告警？'];
    }

    function toolLabel(name) {
        return TOOL_LABELS[name] || name || '数据';
    }

    function appendAgentStep(name, pending) {
        const typing = document.getElementById('typing-indicator');
        if (!typing) return;
        let trace = typing.querySelector('.agent-trace');
        if (!trace) {
            trace = document.createElement('div');
            trace.className = 'agent-trace';
            const bubble = typing.querySelector('.message-bubble');
            if (bubble) bubble.insertBefore(trace, bubble.firstChild);
        }
        let step = trace.querySelector('[data-tool="' + String(name).replace(/"/g, '') + '"]');
        if (!step) {
            step = document.createElement('div');
            step.className = 'agent-step';
            step.setAttribute('data-tool', name);
            trace.appendChild(step);
        }
        step.classList.toggle('is-pending', !!pending);
        step.innerHTML = pending
            ? `<i class="fa-solid fa-circle-notch fa-spin"></i><span>正在查询${toolLabel(name)}</span>`
            : `<i class="fa-solid fa-circle-nodes"></i><span>已查询${toolLabel(name)}</span>`;
        scrollToBottom();
    }

    // 初始化页面
    document.addEventListener('DOMContentLoaded', function () {
        // 从URL获取集群ID
        currentClusterId = getClusterIdFromUrl();
        if (currentClusterId) {
            console.log('当前集群ID:', currentClusterId);
        }

        initAIAssistant();
        initMarkdownRenderer();
        initMermaid();
        loadModelConfigs();
        loadAgentRole();
        loadChatHistory();
    });

    // 初始化AI助手
    function initAIAssistant() {
        const chatInput = document.getElementById('chat-input');
        const sendBtn = document.getElementById('send-btn');
        const chatMessages = document.getElementById('chat-messages');
        const modelOptions = document.querySelectorAll('.model-option');

        loadInputHistory();
        bindStickToBottom();

        chatInput.addEventListener('input', function () {
            inputHistoryIndex = -1;
            if (!isStreaming) {
                sendBtn.disabled = this.value.trim() === '';
            }
            autoResize(this);
            updateSlashMenu(this.value);
        });

        chatInput.addEventListener('keydown', function (e) {
            const slashOpen = isSlashMenuOpen();
            if (slashOpen && (e.key === 'ArrowUp' || e.key === 'ArrowDown')) {
                e.preventDefault();
                moveSlashHighlight(e.key === 'ArrowUp' ? -1 : 1);
                return;
            }
            if (slashOpen && e.key === 'Escape') {
                e.preventDefault();
                hideSlashMenu();
                return;
            }
            if (slashOpen && e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                runActiveSlashCommand();
                return;
            }
            if (e.key === 'ArrowUp' && shouldRecallInputHistory(this, 'up')) {
                e.preventDefault();
                recallInputHistory('up', this);
                return;
            }
            if (e.key === 'ArrowDown' && shouldRecallInputHistory(this, 'down')) {
                e.preventDefault();
                recallInputHistory('down', this);
                return;
            }
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (!isTyping && !isStreaming && this.value.trim()) {
                    sendMessage();
                }
            }
        });

        // 发送按钮点击
        sendBtn.addEventListener('click', handleSendButtonClick);

        const modelPicker = document.getElementById('model-picker');
        const modelPickerBtn = document.getElementById('model-picker-btn');
        if (modelPickerBtn && modelPicker) {
            modelPickerBtn.addEventListener('click', function (e) {
                e.stopPropagation();
                modelPicker.classList.toggle('open');
            });
            document.addEventListener('click', function (e) {
                if (!modelPicker.contains(e.target)) {
                    modelPicker.classList.remove('open');
                }
            });
        }

        // 模型选择事件委托
        document.getElementById('model-options').addEventListener('click', function (e) {
            const modelOption = e.target.closest('.model-option');
            if (modelOption) {
                if (isStreaming) {
                    showToast('请等待当前AI回答完成', 'warning');
                    return;
                }

                document.querySelectorAll('.model-option').forEach(opt => opt.classList.remove('active'));
                modelOption.classList.add('active');
                currentModel = modelOption.getAttribute('data-model');
                currentModelId = modelOption.getAttribute('data-model-id');
                saveSelectedModel(currentModelId, currentModel);
                updateModelDisplay();
                if (modelPicker) modelPicker.classList.remove('open');

                if (eventSource) {
                    eventSource.close();
                }
            }
        });

        bindIntentGrid();
        const slashMenu = document.getElementById('slash-menu');
        if (slashMenu) {
            slashMenu.addEventListener('mousedown', function (e) {
                const item = e.target.closest('.slash-item');
                if (!item) return;
                e.preventDefault();
                runSlashCommand(item.getAttribute('data-cmd'));
            });
        }
        const followUps = document.getElementById('follow-ups');
        if (followUps) {
            followUps.addEventListener('click', function (e) {
                const btn = e.target.closest('.follow-up');
                if (!btn) return;
                sendUserPrompt(btn.textContent.trim(), lastSkillId);
            });
        }

        document.getElementById('new-chat-btn').addEventListener('click', function () {
            if (isStreaming) {
                showToast('请等待当前AI回答完成', 'warning');
                return;
            }
            createNewChat();
        });

        initHistoryDialog();
        loadChatHistory();
    }

    // 处理发送按钮点击
    function handleSendButtonClick() {
        const sendBtn = document.getElementById('send-btn');
        const action = sendBtn.getAttribute('data-action');

        if (action === 'send') {
            sendMessage();
        } else if (action === 'stop') {
            stopStreaming();
        }
    }

    // 发送消息
    async function sendMessage() {
        const chatInput = document.getElementById('chat-input');
        const message = chatInput.value.trim();

        if (!message || isTyping || isStreaming) return;

        const slashCmd = matchSlashCommand(message);
        if (slashCmd) {
            await runSlashCommand(slashCmd);
            return;
        }

        setStage('thread');
        clearFollowUps();

        // 确保流式传输状态已重置
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
        isStreaming = false;
        currentStreamId = null;

        // 如果没有当前会话，先创建一个
        if (!currentSessionId) {
            try {
                const response = await fetch('/api/chat/session', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        title: message.length > 50 ? message.substring(0, 50) + '...' : message,
                        modelName: currentModel || 'GPT-4'
                    })
                });

                const data = await response.json();
                if (data.success) {
                    currentSessionId = data.session.sessionId;
                } else {
                    showToast('创建会话失败: ' + data.message, 'error');
                    return;
                }
            } catch (error) {
                console.error('创建会话失败:', error);
                showToast('创建会话失败', 'error');
                return;
            }
        }

        pushInputHistory(message);

        // 添加用户消息到界面
        addMessage(message, 'user');

        // 保存用户消息到数据库
        await saveMessageToDatabase(message, 'user');

        // 清空输入框
        chatInput.value = '';
        document.getElementById('send-btn').disabled = true;

        // 重置高度
        chatInput.style.height = 'auto';

        showTypingIndicator();

        // 调用AI API获取回复
        await callAIAPI(message);
    }

    async function sendUserPrompt(message, skillId) {
        if (!message || isTyping || isStreaming) {
            if (isStreaming) {
                showToast('请等待当前AI回答完成', 'warning');
            }
            return;
        }

        lastSkillId = skillId || lastSkillId;
        const chatInput = document.getElementById('chat-input');
        if (chatInput) {
            chatInput.value = message;
        }
        await sendMessage();
    }

    function loadInputHistory() {
        try {
            const raw = JSON.parse(localStorage.getItem(INPUT_HISTORY_KEY) || '[]');
            inputHistory = Array.isArray(raw) ? raw.filter(item => typeof item === 'string' && item.trim()) : [];
        } catch (e) {
            inputHistory = [];
        }
        inputHistoryIndex = -1;
        inputDraft = '';
    }

    function pushInputHistory(text) {
        const value = (text || '').trim();
        if (!value || matchSlashCommand(value)) return;
        if (inputHistory[inputHistory.length - 1] === value) {
            inputHistoryIndex = -1;
            inputDraft = '';
            return;
        }
        inputHistory.push(value);
        if (inputHistory.length > INPUT_HISTORY_MAX) {
            inputHistory = inputHistory.slice(-INPUT_HISTORY_MAX);
        }
        try {
            localStorage.setItem(INPUT_HISTORY_KEY, JSON.stringify(inputHistory));
        } catch (e) { /* ignore quota */ }
        inputHistoryIndex = -1;
        inputDraft = '';
    }

    function caretLineInfo(textarea) {
        const value = textarea.value;
        const start = textarea.selectionStart || 0;
        const before = value.slice(0, start);
        const after = value.slice(start);
        return {
            onFirstLine: before.indexOf('\n') === -1,
            onLastLine: after.indexOf('\n') === -1
        };
    }

    function shouldRecallInputHistory(textarea, direction) {
        if (!inputHistory.length) return false;
        const lines = caretLineInfo(textarea);
        if (direction === 'up') return lines.onFirstLine;
        return inputHistoryIndex !== -1 || lines.onLastLine;
    }

    function recallInputHistory(direction, textarea) {
        if (!inputHistory.length) return;
        if (inputHistoryIndex === -1) {
            inputDraft = textarea.value;
        }
        if (direction === 'up') {
            if (inputHistoryIndex === -1) {
                inputHistoryIndex = inputHistory.length - 1;
            } else if (inputHistoryIndex > 0) {
                inputHistoryIndex -= 1;
            }
        } else {
            if (inputHistoryIndex === -1) return;
            if (inputHistoryIndex < inputHistory.length - 1) {
                inputHistoryIndex += 1;
            } else {
                inputHistoryIndex = -1;
                textarea.value = inputDraft;
                autoResize(textarea);
                document.getElementById('send-btn').disabled = !textarea.value.trim() || isStreaming;
                return;
            }
        }
        textarea.value = inputHistory[inputHistoryIndex] || '';
        autoResize(textarea);
        const sendBtn = document.getElementById('send-btn');
        if (sendBtn && !isStreaming) {
            sendBtn.disabled = textarea.value.trim() === '';
        }
        const end = textarea.value.length;
        textarea.setSelectionRange(end, end);
        hideSlashMenu();
    }

    function matchSlashCommand(text) {
        const value = (text || '').trim().toLowerCase();
        if (!value.startsWith('/')) return null;
        const token = value.split(/\s+/)[0];
        const found = SLASH_COMMANDS.find(item => item.cmd === token);
        return found ? found.cmd : null;
    }

    function filteredSlashCommands(text) {
        const value = (text || '').trim().toLowerCase();
        if (!value.startsWith('/')) return [];
        return SLASH_COMMANDS.filter(item => item.cmd.indexOf(value.split(/\s+/)[0]) === 0);
    }

    function isSlashMenuOpen() {
        const menu = document.getElementById('slash-menu');
        return !!(menu && menu.classList.contains('open'));
    }

    function hideSlashMenu() {
        const menu = document.getElementById('slash-menu');
        if (!menu) return;
        menu.classList.remove('open');
        menu.hidden = true;
        menu.innerHTML = '';
    }

    function updateSlashMenu(text) {
        const menu = document.getElementById('slash-menu');
        if (!menu) return;
        const items = filteredSlashCommands(text);
        if (!items.length) {
            hideSlashMenu();
            return;
        }
        slashActiveIndex = 0;
        menu.hidden = false;
        menu.classList.add('open');
        menu.innerHTML = items.map((item, index) => `
            <button type="button" class="slash-item${index === slashActiveIndex ? ' active' : ''}" data-cmd="${item.cmd}">
                <span class="slash-item-cmd">${item.cmd}</span>
                <span class="slash-item-desc">${item.desc}</span>
            </button>
        `).join('');
    }

    function moveSlashHighlight(delta) {
        const menu = document.getElementById('slash-menu');
        if (!menu) return;
        const items = menu.querySelectorAll('.slash-item');
        if (!items.length) return;
        slashActiveIndex = (slashActiveIndex + delta + items.length) % items.length;
        items.forEach((item, index) => item.classList.toggle('active', index === slashActiveIndex));
    }

    function runActiveSlashCommand() {
        const menu = document.getElementById('slash-menu');
        const active = menu && menu.querySelector('.slash-item.active');
        const cmd = active ? active.getAttribute('data-cmd') : matchSlashCommand(document.getElementById('chat-input').value);
        if (cmd) {
            runSlashCommand(cmd);
        }
    }

    async function runSlashCommand(cmd) {
        hideSlashMenu();
        const chatInput = document.getElementById('chat-input');
        if (cmd === '/new') {
            if (isStreaming) {
                showToast('请等待当前AI回答完成', 'warning');
                return;
            }
            if (chatInput) {
                chatInput.value = '';
                autoResize(chatInput);
                document.getElementById('send-btn').disabled = true;
            }
            await createNewChat();
            if (chatInput) chatInput.focus();
        }
    }

    function escapeHtml(text) {
        return String(text || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    function buildThinkingHtml(thinkingContent, isStreamingThink, isOpen) {
        const stateClass = isStreamingThink ? 'is-streaming open' : (isOpen ? 'is-done open' : 'is-done');
        const title = isStreamingThink ? '正在思考' : '已完成思考';
        const icon = isOpen || isStreamingThink ? 'fa-chevron-up' : 'fa-chevron-down';
        return `
            <div class="thinking-section ${stateClass}">
                <button type="button" class="thinking-header" onclick="toggleThinking(this)">
                    <span class="thinking-pulse"></span>
                    <span class="thinking-title">${title}</span>
                    <i class="fa-solid ${icon} thinking-icon ml-auto"></i>
                </button>
                <div class="thinking-content">
                    <div class="thinking-text">${escapeHtml(thinkingContent)}</div>
                </div>
            </div>
        `;
    }

    // 添加消息到聊天区域
    function addMessage(text, sender, enableMarkdown = false, thinkingContent = '') {
        const chatMessages = document.getElementById('chat-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message-container';
        messageDiv.style.maxWidth = '100%';
        messageDiv.style.overflowX = 'hidden';

        const bubble = document.createElement('div');
        bubble.className = `message-bubble ${sender}`;

        const content = document.createElement('div');
        content.className = enableMarkdown ? 'markdown-content' : '';

        if (enableMarkdown && sender === 'assistant') {
            let displayContent = '';

            if (thinkingContent) {
                displayContent = buildThinkingHtml(thinkingContent, false, false) +
                    `<div class="answer-content">${renderMarkdown(text)}</div>`;
            } else {
                displayContent = renderMarkdown(text);
            }

            content.innerHTML = displayContent;
            setTimeout(() => {
                processCharts(content);
                processTables(content);
                // 应用代码高亮
                applyCodeHighlighting(content);
            }, 100);
        } else {
            content.textContent = text;
        }

        // 添加消息操作按钮（仅AI回复）
        if (sender === 'assistant') {
            const actions = document.createElement('div');
            actions.className = 'message-actions';
            actions.innerHTML = `
                <button class="action-btn" onclick="copyMessage(this)" title="复制">
                    <i class="fa fa-copy"></i>
                </button>
                <button class="action-btn" onclick="regenerateResponse(this)" title="重新生成">
                    <i class="fa fa-refresh"></i>
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'good')" title="有帮助">
                    <i class="fa fa-thumbs-up"></i>
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'bad')" title="没帮助">
                    <i class="fa fa-thumbs-down"></i>
                </button>
            `;
            content.appendChild(actions);
        }

        bubble.appendChild(content);

        if (sender === 'user') {
            messageDiv.className += ' user';
        } else {
            messageDiv.className += ' assistant';
        }
        messageDiv.appendChild(bubble);

        chatMessages.appendChild(messageDiv);

        if (!isLoadingHistory) {
            stickToBottom = true;
            scrollToBottom();
        }

        // 保存到对话历史
        conversationHistory.push({
            sender,
            text,
            timestamp: new Date(),
            model: currentModel
        });
    }

    // 调用AI API获取回复
    async function callAIAPI(userMessage) {
        if (!currentModelId) {
            hideTypingIndicator();
            addMessage('请先选择一个AI模型', 'assistant');
            return;
        }

        try {
            // 生成流式传输ID
            currentStreamId = Date.now().toString();

            // 获取图表开关状态
            const enableCharts = document.getElementById('enable-charts');
            const enableChartsValue = enableCharts ? enableCharts.checked : false;

            // 构建URL，添加clusterId和enableCharts参数
            let url = `/api/chat/stream?modelId=${currentModelId}&message=${encodeURIComponent(userMessage)}&streamId=${currentStreamId}&enableCharts=${enableChartsValue}`;
            if (currentClusterId) {
                url += `&clusterId=${encodeURIComponent(currentClusterId)}`;
            }

            // 建立SSE连接
            eventSource = new EventSource(url);

            // 设置流式传输状态
            isStreaming = true;

            // 更新发送按钮为停止按钮
            updateSendButtonToStop();

            let aiResponse = '';
            let thinkingContent = '';
            let isThinking = false;

            eventSource.onmessage = async function (event) {
                const data = JSON.parse(event.data);

                if (data.type === 'thinking') {
                    // 处理思考内容
                    isThinking = true;
                    thinkingContent += data.content;
                    updateTypingMessage('', thinkingContent, true);
                } else if (data.type === 'content') {
                    // 处理正式回答内容
                    if (isThinking) {
                        // 思考结束，开始正式回答
                        isThinking = false;
                        aiResponse = data.content;
                        updateTypingMessage(aiResponse, thinkingContent, false);
                    } else {
                        aiResponse += data.content;
                        updateTypingMessage(aiResponse, thinkingContent, false);
                    }
                } else if (data.type === 'chart') {
                    // 处理图表数据
                    aiResponse += '\n\n```chart\n' + data.chartData + '\n```\n\n';
                    updateTypingMessage(aiResponse, thinkingContent, false);
                } else if (data.type === 'thinking_end') {
                    isThinking = false;
                    updateTypingMessage('', thinkingContent, false);
                } else if (data.type === 'function_call' || data.type === 'agent_step') {
                    appendAgentStep(data.name || data.tool || data.title || '数据', data.phase !== 'observe');
                } else if (data.type === 'function_result') {
                    appendAgentStep(data.name || '数据', false);
                } else if (data.type === 'agent_skill') {
                    lastSkillId = data.skillId || lastSkillId;
                } else if (data.type === 'follow_ups') {
                    renderFollowUps(data.items || data.follow_ups || []);
                } else if (data.type === 'end') {
                    eventSource.close();
                    eventSource = null;
                    finalizeStreamingMessage(aiResponse, thinkingContent);
                    await saveMessageToDatabase(aiResponse, 'assistant');
                    autoCollapseThinking();
                    const followBox = document.getElementById('follow-ups');
                    if (!followBox || followBox.hidden) {
                        renderFollowUps(defaultFollowUps());
                    }
                    resetSendButton();
                } else if (data.type === 'error') {
                    eventSource.close();
                    eventSource = null;
                    hideTypingIndicator();
                    addMessage('AI回复出错: ' + data.message, 'assistant');
                    await saveMessageToDatabase('AI回复出错: ' + data.message, 'assistant');
                    // 重置发送按钮
                    resetSendButton();
                }
            };

            eventSource.onerror = async function (event) {
                eventSource.close();
                eventSource = null;
                hideTypingIndicator();
                addMessage('AI服务连接失败，请稍后重试', 'assistant');
                await saveMessageToDatabase('AI服务连接失败，请稍后重试', 'assistant');
                // 重置发送按钮
                resetSendButton();
            };

        } catch (error) {
            console.error('调用AI API失败:', error);
            hideTypingIndicator();
            addMessage('AI服务调用失败，请稍后重试', 'assistant');
            await saveMessageToDatabase('AI服务调用失败，请稍后重试', 'assistant');
            // 重置发送按钮
            resetSendButton();
        }
    }

    // 更新正在输入的消息
    function updateTypingMessage(content, thinkingContent, isThinking) {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            const typingContent = typingIndicator.querySelector('.typing-indicator');
            const markdownContent = typingIndicator.querySelector('.markdown-content');

            if (markdownContent) {
                markdownContent.style.display = 'block';

                let displayContent = '';

                if (isThinking && thinkingContent) {
                    displayContent = buildThinkingHtml(thinkingContent, true, true);
                } else if (thinkingContent && content) {
                    displayContent = buildThinkingHtml(thinkingContent, false, false) +
                        `<div class="answer-content">${renderMarkdown(content)}</div>`;
                } else if (content) {
                    displayContent = renderMarkdown(content);
                } else if (thinkingContent) {
                    displayContent = buildThinkingHtml(thinkingContent, false, true);
                }

                markdownContent.innerHTML = displayContent;

                if (isHighlightEnabled()) {
                    setTimeout(() => {
                        applyCodeHighlighting(markdownContent);
                    }, 50);
                }

                // 处理图表和表格
                setTimeout(() => {
                    processCharts(markdownContent);
                    processTables(markdownContent);
                }, 50);

                // 隐藏打字指示器
                if (typingContent) {
                    typingContent.style.display = 'none';
                }
                scrollToBottom();
            }
        }
    }

    // 保存消息到数据库
    async function saveMessageToDatabase(content, sender) {
        if (!currentSessionId) return;

        try {
            const response = await fetch('/api/chat/message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    sessionId: currentSessionId,
                    sender: sender,
                    content: content,
                    modelName: currentModel || 'GPT-4'
                })
            });

            if (!response.ok) {
                console.error('保存消息失败: HTTP', response.status);
                return;
            }

            const data = await response.json();
            if (!data.success) {
                console.error('保存消息失败:', data.message);
            }
        } catch (error) {
            console.error('保存消息失败:', error);
        }
    }

    function getScrollRoot() {
        return document.getElementById('agent-scroll') || document.getElementById('chat-messages');
    }

    function bindStickToBottom() {
        const root = getScrollRoot();
        if (!root) return;
        root.addEventListener('scroll', function () {
            const gap = root.scrollHeight - root.scrollTop - root.clientHeight;
            stickToBottom = gap < 72;
        }, { passive: true });
    }

    function scrollToBottom() {
        const root = getScrollRoot();
        if (!root || !stickToBottom) return;
        if (scrollFrame) return;
        scrollFrame = requestAnimationFrame(function () {
            scrollFrame = 0;
            root.scrollTop = root.scrollHeight;
        });
    }

    // 初始化Markdown渲染器
    function initMarkdownRenderer() {
        if (typeof marked !== 'undefined') {
            marked.setOptions({
                highlight: function (code, lang) {
                    // 检查是否启用了代码高亮
                    if (isHighlightEnabled() && typeof hljs !== 'undefined' && lang && hljs.getLanguage(lang)) {
                        try {
                            return hljs.highlight(code, { language: lang }).value;
                        } catch (e) {
                            console.warn('代码高亮失败:', e);
                            return code;
                        }
                    }
                    return code;
                },
                breaks: true,
                gfm: true,
                tables: true,
                headerIds: false,
                mangle: false
            });
        }
    }

    // 重新初始化Markdown渲染器（当代码高亮设置改变时调用）
    function reinitMarkdownRenderer() {
        if (typeof marked !== 'undefined') {
            marked.setOptions({
                highlight: function (code, lang) {
                    // 检查是否启用了代码高亮
                    if (isHighlightEnabled() && typeof hljs !== 'undefined' && lang && hljs.getLanguage(lang)) {
                        try {
                            return hljs.highlight(code, { language: lang }).value;
                        } catch (e) {
                            console.warn('代码高亮失败:', e);
                            return code;
                        }
                    }
                    return code;
                },
                breaks: true,
                gfm: true,
                tables: true,
                headerIds: false,
                mangle: false
            });
        }
    }

    // 渲染Markdown
    function renderMarkdown(text) {
        if (typeof marked === 'undefined') {
            return text.replace(/\n/g, '<br>');
        }

        let html = marked.parse(text);

        // 为代码块添加复制按钮
        html = html.replace(/<pre><code class="([^"]*)">([\s\S]*?)<\/code><\/pre>/g, function (match, className, codeContent) {
            const language = className.replace('language-', '');
            const languageDisplay = language || 'text';
            return `
                <div class="code-block-wrapper relative">
                    <div class="code-header flex items-center justify-between bg-gray-800 text-white px-4 py-2 rounded-t-lg">
                        <span class="text-sm font-medium">${languageDisplay}</span>
                        <button class="copy-code-btn text-gray-300 hover:text-white transition-colors" onclick="copyCodeBlock(this)" title="复制代码">
                            <i class="fa fa-copy"></i>
                        </button>
                    </div>
                    <pre><code class="${className}">${codeContent}</code></pre>
                </div>
            `;
        });

        // 为表格添加包装器和样式
        html = html.replace(/<table([^>]*)>([\s\S]*?)<\/table>/g, function (match, tableAttrs, tableContent) {
            return `
                <div class="table-wrapper">
                    <div class="table-header flex items-center justify-between bg-gray-100 px-4 py-2 border-b border-gray-200">
                        <span class="text-sm font-medium text-gray-700">数据表格</span>
                        <button class="copy-table-btn text-gray-500 hover:text-gray-700 transition-colors" onclick="copyTable(this)" title="复制表格">
                            <i class="fa fa-copy"></i>
                        </button>
                    </div>
                    <div class="table-container">
                        <table class="markdown-table"${tableAttrs}>
                            ${tableContent}
                        </table>
                    </div>
                </div>
            `;
        });

        return html;
    }

    function isHighlightEnabled() {
        const enableHighlight = document.getElementById('enable-highlight');
        return !enableHighlight || enableHighlight.checked;
    }

    // 应用代码高亮
    function applyCodeHighlighting(container) {
        if (!isHighlightEnabled() || typeof hljs === 'undefined') {
            return;
        }

        try {
            // 查找所有代码块
            const codeBlocks = container.querySelectorAll('pre code');
            codeBlocks.forEach(block => {
                // 跳过已经是图表的代码块
                if (block.className.includes('language-chart')) {
                    return;
                }

                // 从class中提取语言
                const languageClass = Array.from(block.classList).find(cls => cls.startsWith('language-'));
                if (languageClass) {
                    const language = languageClass.replace('language-', '');
                    if (hljs.getLanguage(language)) {
                        try {
                            hljs.highlightElement(block);
                        } catch (e) {
                            console.warn('代码高亮失败:', e);
                        }
                    }
                }
            });

            // 也处理内联代码
            const inlineCodes = container.querySelectorAll('code:not(pre code)');
            inlineCodes.forEach(code => {
                if (typeof hljs !== 'undefined') {
                    try {
                        hljs.highlightElement(code);
                    } catch (e) {
                        console.warn('内联代码高亮失败:', e);
                    }
                }
            });
        } catch (e) {
            console.warn('应用代码高亮时出错:', e);
        }
    }

    // 处理图表
    function processCharts(container) {
        const codeBlocks = container.querySelectorAll('pre code');

        codeBlocks.forEach(block => {
            if (block.className.includes('language-chart')) {
                try {
                    const chartConfig = JSON.parse(block.textContent);
                    renderChart(block.parentElement, chartConfig);
                } catch (e) {
                    console.error('图表配置解析失败:', e);
                }
            }
        });
    }

    // 处理表格
    function processTables(container) {
        const tables = container.querySelectorAll('.markdown-table');

        tables.forEach(table => {
            // 为表格添加响应式处理
            const wrapper = table.closest('.table-wrapper');
            if (wrapper) {
                // 检查表格是否需要横向滚动
                const tableWidth = table.scrollWidth;
                const containerWidth = wrapper.clientWidth;

                if (tableWidth > containerWidth) {
                    wrapper.classList.add('table-scrollable');
                }
            }

            // 为表格行添加交替颜色
            const rows = table.querySelectorAll('tbody tr');
            rows.forEach((row, index) => {
                if (index % 2 === 1) {
                    row.classList.add('table-row-alternate');
                }
            });

            // 确保表格内容正确换行
            const cells = table.querySelectorAll('th, td');
            cells.forEach(cell => {
                cell.style.wordWrap = 'break-word';
                cell.style.wordBreak = 'break-word';
                cell.style.overflowWrap = 'break-word';
            });
        });
    }

    // 渲染图表
    function renderChart(element, config) {
        const chartContainer = document.createElement('div');
        chartContainer.className = 'chart-container';

        const canvas = document.createElement('canvas');
        chartContainer.appendChild(canvas);

        element.parentNode.replaceChild(chartContainer, element);

        if (typeof Chart !== 'undefined') {
            new Chart(canvas.getContext('2d'), config);
        }
    }

    // 初始化Mermaid
    function initMermaid() {
        if (typeof mermaid !== 'undefined') {
            mermaid.initialize({
                startOnLoad: false,
                theme: 'default',
                securityLevel: 'loose'
            });
        }
    }

    // 显示正在输入指示器
    function showTypingIndicator() {
        if (isTyping) return;

        isTyping = true;

        const chatMessages = document.getElementById('chat-messages');

        const typingDiv = document.createElement('div');
        typingDiv.id = 'typing-indicator';
        typingDiv.className = 'message-container assistant';

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble assistant';

        const trace = document.createElement('div');
        trace.className = 'agent-trace';

        const typingContent = document.createElement('div');
        typingContent.className = 'typing-indicator';
        typingContent.innerHTML = `
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="ml-2 text-sm text-gray-500">正在排查...</span>
            `;

        // 添加Markdown内容容器
        const markdownContent = document.createElement('div');
        markdownContent.className = 'markdown-content mt-2';
        markdownContent.style.display = 'none';

        bubble.appendChild(trace);
        bubble.appendChild(typingContent);
        bubble.appendChild(markdownContent);
        typingDiv.appendChild(bubble);

        chatMessages.appendChild(typingDiv);
        stickToBottom = true;
        scrollToBottom();
    }

    function assistantActionsHtml() {
        return `
                <button class="action-btn" onclick="copyMessage(this)" title="复制">
                    <i class="fa fa-copy"></i>
                </button>
                <button class="action-btn" onclick="regenerateResponse(this)" title="重新生成">
                    <i class="fa fa-refresh"></i>
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'good')" title="有帮助">
                    <i class="fa fa-thumbs-up"></i>
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'bad')" title="没帮助">
                    <i class="fa fa-thumbs-down"></i>
                </button>
            `;
    }

    function finalizeStreamingMessage(text, thinkingContent) {
        const typing = document.getElementById('typing-indicator');
        if (!typing) {
            addMessage(text, 'assistant', true, thinkingContent);
            return;
        }
        typing.removeAttribute('id');
        const dots = typing.querySelector('.typing-indicator');
        if (dots) dots.remove();
        const markdown = typing.querySelector('.markdown-content');
        if (markdown) {
            markdown.style.display = 'block';
            let html = '';
            if (thinkingContent) {
                html += buildThinkingHtml(thinkingContent, false, false);
            }
            html += `<div class="answer-content">${renderMarkdown(text || '')}</div>`;
            markdown.innerHTML = html;
            const actions = document.createElement('div');
            actions.className = 'message-actions';
            actions.innerHTML = assistantActionsHtml();
            markdown.appendChild(actions);
            processCharts(markdown);
            processTables(markdown);
            applyCodeHighlighting(markdown);
        }
        isTyping = false;
        stickToBottom = true;
        scrollToBottom();
        conversationHistory.push({
            sender: 'assistant',
            text: text || '',
            timestamp: new Date(),
            model: currentModel
        });
    }

    // 停止流式传输
    async function stopStreaming() {
        if (!isStreaming || !currentStreamId) {
            return;
        }

        try {
            // 关闭SSE连接
            if (eventSource) {
                eventSource.close();
                eventSource = null;
            }

            // 调用后端停止流式传输
            const response = await fetch('/api/chat/stop-stream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    streamId: currentStreamId
                })
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    showToast('已停止AI回答', 'info');
                } else {
                    console.error('停止流式传输失败:', data.message);
                }
            } else {
                console.error('停止流式传输失败: HTTP', response.status);
            }
        } catch (error) {
            console.error('停止流式传输失败:', error);
        } finally {
            // 重置状态
            isStreaming = false;
            currentStreamId = null;
            hideTypingIndicator();
            resetSendButton();
        }
    }

    // 更新发送按钮为停止按钮
    function updateSendButtonToStop() {
        const sendBtn = document.getElementById('send-btn');
        sendBtn.setAttribute('data-action', 'stop');
        sendBtn.innerHTML = '<i class="fa fa-stop"></i>';
        sendBtn.className = 'composer-send';
        sendBtn.disabled = false;
        sendBtn.title = '停止';
    }

    // 重置发送按钮
    function resetSendButton() {
        const sendBtn = document.getElementById('send-btn');
        const chatInput = document.getElementById('chat-input');

        sendBtn.setAttribute('data-action', 'send');
        sendBtn.innerHTML = '<i class="fa fa-arrow-up"></i>';
        sendBtn.className = 'composer-send';
        sendBtn.title = '发送';
        sendBtn.disabled = !chatInput || chatInput.value.trim() === '';

        // 确保流式传输状态重置
        isStreaming = false;
        currentStreamId = null;

        // 确保输入框状态正确
        if (chatInput) {
            chatInput.disabled = false;
            chatInput.focus();
        }

        // 确保isTyping状态重置
        isTyping = false;
    }

    // 隐藏正在输入指示器
    function hideTypingIndicator() {
        const typingIndicator = document.getElementById('typing-indicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
        isTyping = false;

        // 确保输入框重新启用
        const chatInput = document.getElementById('chat-input');
        if (chatInput) {
            chatInput.disabled = false;
        }
    }

    // 自动调整输入框高度
    function autoResize(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    }

    // 获取模型显示名称
    function getModelDisplayName(model) {
        const modelConfig = modelConfigs.find(m => m.modelName === model);
        return modelConfig ? modelConfig.modelName : model;
    }

    const SELECTED_MODEL_KEY = 'efak.ai.selectedModel';

    function saveSelectedModel(modelId, modelName) {
        try {
            localStorage.setItem(SELECTED_MODEL_KEY, JSON.stringify({
                id: String(modelId || ''),
                name: modelName || ''
            }));
        } catch (e) {
            console.warn('保存模型选择失败:', e);
        }
    }

    function readSelectedModel() {
        try {
            const raw = localStorage.getItem(SELECTED_MODEL_KEY);
            if (!raw) return null;
            const parsed = JSON.parse(raw);
            if (!parsed || (!parsed.id && !parsed.name)) return null;
            return parsed;
        } catch (e) {
            return null;
        }
    }

    function applySelectedModel(model, silent) {
        currentModel = model.modelName;
        currentModelId = model.id;
        document.querySelectorAll('.model-option').forEach((opt) => {
            opt.classList.toggle('active', String(opt.getAttribute('data-model-id')) === String(model.id));
        });
        const pickerLabel = document.getElementById('model-picker-label');
        if (pickerLabel) {
            pickerLabel.textContent = currentModel;
            pickerLabel.title = currentModel;
        }
        if (!silent) {
            showToast(`已切换到${getModelDisplayName(currentModel)}`, 'success');
        }
    }

    // 更新模型显示
    function updateModelDisplay() {
        applySelectedModel({ id: currentModelId, modelName: currentModel }, false);
    }

    // 加载模型配置
    async function loadModelConfigs() {
        try {
            const response = await fetch('/api/model-config/enabled', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            if (data.modelConfigs && data.modelConfigs.length > 0) {
                modelConfigs = data.modelConfigs;
                renderModelOptions();

                const saved = readSelectedModel();
                const savedModel = saved
                    ? modelConfigs.find((m) => String(m.id) === String(saved.id) || m.modelName === saved.name)
                    : null;
                applySelectedModel(savedModel || modelConfigs[0], true);
            } else {
                showToast('未找到可用的模型配置', 'error');
            }
        } catch (error) {
            console.error('加载模型配置失败:', error);
            showToast('加载模型配置失败', 'error');
        }
    }

    // 渲染模型选项
    function renderModelOptions() {
        const modelOptionsContainer = document.getElementById('model-options');
        if (!modelOptionsContainer) return;

        modelOptionsContainer.innerHTML = '';

        modelConfigs.forEach((model) => {
            const modelOption = document.createElement('div');
            modelOption.className = 'model-option';
            modelOption.setAttribute('data-model', model.modelName);
            modelOption.setAttribute('data-model-id', model.id);

            const statusClass = model.status === 1 ? 'bg-green-400' :
                model.status === 2 ? 'bg-red-400' : 'bg-gray-400';
            const statusText = model.status === 1 ? '在线' :
                model.status === 2 ? '错误' : '离线';

            const providerIcon = {
                OpenAI: '/images/icons/openai.svg',
                Anthropic: '/images/icons/anthropic.svg',
                DeepSeek: '/images/icons/deepseek.svg',
                Kimi: '/images/icons/kimi.svg',
                Qwen: '/images/icons/qwen.svg',
                Doubao: '/images/icons/doubao.svg',
                GLM: '/images/icons/glm.png',
                Custom: '/images/icons/custom.svg',
                'Custom-OpenAI': '/images/icons/custom.svg',
                'Custom-Anthropic': '/images/icons/custom.svg',
                'Custom-Ollama': '/images/icons/custom.svg',
                Ollama: '/images/icons/ollama.svg'
            }[model.apiType] || '/images/icons/openai.svg';

            modelOption.innerHTML = `
                    <div class="flex items-center justify-between gap-2">
                        <div class="flex items-center gap-2 min-w-0">
                            <img src="${providerIcon}" alt="" class="w-5 h-5 flex-shrink-0 object-contain">
                            <div class="min-w-0">
                                <div class="font-medium text-sm truncate">${model.modelName}</div>
                                <div class="text-xs text-gray-500 truncate">${model.apiType || ''}${model.description ? ' · ' + model.description : ''}</div>
                            </div>
                        </div>
                        <div class="w-2 h-2 ${statusClass} rounded-full flex-shrink-0" title="${statusText}"></div>
                    </div>
                `;

            modelOptionsContainer.appendChild(modelOption);
        });
    }

    // 消息操作函数
    window.copyMessage = function (btn) {
        const messageContent = btn.closest('.message-bubble').querySelector('.markdown-content');
        const text = messageContent.textContent || messageContent.innerText;

        navigator.clipboard.writeText(text).then(() => {
            showToast('已复制到剪贴板', 'success');
        });
    };

    // 复制代码块
    window.copyCodeBlock = function (btn) {
        const codeWrapper = btn.closest('.code-block-wrapper');
        if (!codeWrapper) {
            showToast('复制失败，未找到代码块', 'error');
            return;
        }

        const codeBlock = codeWrapper.querySelector('code') || codeWrapper.querySelector('pre');
        if (!codeBlock) {
            showToast('复制失败，未找到代码内容', 'error');
            return;
        }

        const codeText = codeBlock.textContent || codeBlock.innerText;
        if (!codeText) {
            showToast('复制失败，代码内容为空', 'error');
            return;
        }

        navigator.clipboard.writeText(codeText).then(() => {
            // 临时改变按钮图标显示复制成功
            const icon = btn.querySelector('i');
            if (icon) {
                const originalClass = icon.className;
                icon.className = 'fa fa-check';
                btn.style.color = '#10b981'; // 绿色

                setTimeout(() => {
                    icon.className = originalClass;
                    btn.style.color = '';
                }, 1000);
            }

            showToast('代码已复制到剪贴板', 'success');
        }).catch((err) => {
            console.error('复制失败:', err);
            showToast('复制失败，请手动复制', 'error');
        });
    };

    // 复制表格
    window.copyTable = function (btn) {
        const table = btn.closest('.table-wrapper').querySelector('table');
        const tableText = table.textContent || table.innerText;

        navigator.clipboard.writeText(tableText).then(() => {
            // 临时改变按钮图标显示复制成功
            const icon = btn.querySelector('i');
            const originalClass = icon.className;
            icon.className = 'fa fa-check';
            btn.style.color = '#10b981'; // 绿色

            setTimeout(() => {
                icon.className = originalClass;
                btn.style.color = '';
            }, 1000);

            showToast('表格已复制到剪贴板', 'success');
        }).catch(() => {
            showToast('复制失败，请手动复制', 'error');
        });
    };

    window.regenerateResponse = async function (btn) {
        // 如果正在流式传输，不允许重新生成
        if (isStreaming) {
            showToast('请等待当前AI回答完成', 'warning');
            return;
        }

        // 获取最后一条用户消息
        const lastUserMessage = getLastUserMessage();
        if (!lastUserMessage) {
            showToast('没有找到可重新生成的消息', 'error');
            return;
        }

        // 显示重新生成提示
        showToast('正在重新生成回复...', 'info');

        // 删除最后一条AI回复
        await removeLastAIResponse();

        // 重新调用AI API
        regenerateAIResponse(lastUserMessage);
    };

    window.rateMessage = function (btn, rating) {
        // 实现消息评分逻辑
        const icon = rating === 'good' ? 'thumbs-up' : 'thumbs-down';
        btn.innerHTML = `<i class="fa fa-${icon} text-green-500"></i>`;
        btn.disabled = true;
    };

    // 获取最后一条用户消息
    function getLastUserMessage() {
        const chatMessages = document.getElementById('chat-messages');
        const messages = chatMessages.querySelectorAll('.message-container');

        // 从后往前查找最后一条用户消息
        for (let i = messages.length - 1; i >= 0; i--) {
            const message = messages[i];
            const userBubble = message.querySelector('.message-bubble.user');
            if (userBubble) {
                // 获取用户消息内容（排除操作按钮等）
                const contentDiv = userBubble.querySelector('div:not(.message-actions)');
                if (contentDiv) {
                    const content = contentDiv.textContent || contentDiv.innerText;
                    return content.trim();
                }
            }
        }
        return null;
    }

    // 删除最后一条AI回复
    async function removeLastAIResponse() {
        const chatMessages = document.getElementById('chat-messages');
        const messages = chatMessages.querySelectorAll('.message-container');

        // 从后往前查找最后一条AI回复
        for (let i = messages.length - 1; i >= 0; i--) {
            const message = messages[i];
            const assistantBubble = message.querySelector('.message-bubble.assistant');
            if (assistantBubble) {
                // 删除这条AI回复
                message.remove();

                // 同时从对话历史中删除
                if (conversationHistory.length > 0) {
                    conversationHistory.pop();
                }

                // 从数据库中删除最后一条AI消息（可选，如果API可用）
                if (currentSessionId) {
                    try {
                        const response = await fetch('/api/chat/message/last', {
                            method: 'DELETE',
                            headers: {
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify({
                                sessionId: currentSessionId,
                                sender: 'assistant'
                            })
                        });

                        if (!response.ok) {
                            console.warn('删除最后一条AI消息失败: HTTP', response.status);
                        }
                    } catch (error) {
                        console.warn('删除最后一条AI消息失败:', error);
                        // 不抛出异常，继续执行重新生成
                    }
                }
                break;
            }
        }
    }

    // 重新生成AI回复
    async function regenerateAIResponse(userMessage) {
        try {
            // 确保流式传输状态重置
            if (eventSource) {
                eventSource.close();
                eventSource = null;
            }
            isStreaming = false;
            currentStreamId = null;

            // 显示AI正在输入
            showTypingIndicator();

            // 调用AI API获取回复
            await callAIAPI(userMessage);
        } catch (error) {
            console.error('重新生成AI回复失败:', error);
            hideTypingIndicator();
            showToast('重新生成失败，请稍后重试', 'error');
            resetSendButton();
        }
    }

    function showToast(message, type = 'info') {
        if (window.efakShowToast) {
            window.efakShowToast(message, type);
        }
    }

    function welcomeMessageHTML() {
        return '';
    }

    // 创建新会话
    async function createNewChat() {
        try {
            // 如果正在流式传输，先停止
            if (isStreaming) {
                await stopStreaming();
            }

            const response = await fetch('/api/chat/session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: '新对话',
                    modelName: currentModel || 'GPT-4'
                })
            });

            const data = await response.json();
            if (data.success) {
                currentSessionId = data.session.sessionId;

                // 清空聊天区域
                const chatMessages = document.getElementById('chat-messages');
                chatMessages.innerHTML = '';
                conversationHistory = [];
                lastSkillId = '';
                inputHistoryIndex = -1;
                inputDraft = '';
                hideSlashMenu();
                const chatInput = document.getElementById('chat-input');
                if (chatInput) {
                    chatInput.value = '';
                    autoResize(chatInput);
                    document.getElementById('send-btn').disabled = true;
                }
                setStage('empty');
                renderIntentGrid();

                // 显示创建成功提示
                showToast('新会话已创建', 'success');

                // 重新加载对话历史
                loadChatHistory();

                // 重置加载历史标志
                isLoadingHistory = false;
            } else {
                showToast('创建会话失败: ' + data.message, 'error');
            }
        } catch (error) {
            console.error('创建会话失败:', error);
            showToast('创建会话失败', 'error');
        }
    }

    // 加载对话历史
    async function loadChatHistory() {
        try {
            const response = await fetch('/api/chat/sessions', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                console.error('加载对话历史失败: HTTP', response.status);
                return;
            }

            const data = await response.json();
            if (data.success && data.sessions) {
                historySessions = data.sessions;
                renderChatHistory();
            } else {
                console.error('加载对话历史失败:', data.message);
            }
        } catch (error) {
            console.error('加载对话历史失败:', error);
        }
    }

    // 加载最近一次对话历史
    async function loadLatestChatHistory() {
        try {
            const response = await fetch('/api/chat/sessions', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                console.error('加载最近对话历史失败: HTTP', response.status);
                return;
            }

            const data = await response.json();
            if (data.success && data.sessions && data.sessions.length > 0) {
                // 获取最近一次会话
                const latestSession = data.sessions[0];
                currentSessionId = latestSession.sessionId;

                // 加载该会话的详细历史
                await loadSessionHistory(latestSession.sessionId);

                console.log('已加载最近一次对话历史:', latestSession.title);
            } else {
                console.log('No conversation history; rendering initial prompt');
            }
        } catch (error) {
            console.error('加载最近对话历史失败:', error);
        }
    }

    // 加载会话历史内容
    async function loadSessionHistory(sessionId) {
        try {
            const response = await fetch(`/api/chat/session/${sessionId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                console.error('加载会话历史失败: HTTP', response.status);
                return;
            }

            const data = await response.json();
            if (data.success && data.history) {
                displaySessionHistory(data.history);
                console.log('会话历史加载成功');
            } else {
                console.error('加载会话历史失败:', data.message);
            }
        } catch (error) {
            console.error('加载会话历史失败:', error);
        }
    }

    function getFilteredHistory() {
        const query = historyQuery.trim().toLowerCase();
        if (!query) return historySessions.slice();
        return historySessions.filter((session) => {
            const title = (session.title || '').toLowerCase();
            const model = (session.modelName || '').toLowerCase();
            return title.includes(query) || model.includes(query);
        });
    }

    function initHistoryDialog() {
        const modal = document.getElementById('history-modal');
        const openBtn = document.getElementById('history-chat-btn');
        const closeBtn = document.getElementById('history-close-btn');
        const searchInput = document.getElementById('history-search-input');
        const prevBtn = document.getElementById('history-prev-btn');
        const nextBtn = document.getElementById('history-next-btn');
        if (!modal || !openBtn) return;

        openBtn.addEventListener('click', async function () {
            await loadChatHistory();
            historyPage = 1;
            renderChatHistory();
            modal.classList.add('open');
            modal.setAttribute('aria-hidden', 'false');
            if (searchInput) searchInput.focus();
        });

        function closeHistory() {
            modal.classList.remove('open');
            modal.setAttribute('aria-hidden', 'true');
        }

        if (closeBtn) closeBtn.addEventListener('click', closeHistory);
        modal.addEventListener('click', function (e) {
            if (e.target === modal) closeHistory();
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && modal.classList.contains('open')) {
                closeHistory();
            }
        });

        if (searchInput) {
            searchInput.addEventListener('input', function () {
                historyQuery = this.value || '';
                historyPage = 1;
                renderChatHistory();
            });
        }
        if (prevBtn) {
            prevBtn.addEventListener('click', function () {
                if (historyPage > 1) {
                    historyPage -= 1;
                    renderChatHistory();
                }
            });
        }
        if (nextBtn) {
            nextBtn.addEventListener('click', function () {
                const totalPages = Math.max(1, Math.ceil(getFilteredHistory().length / historyPageSize));
                if (historyPage < totalPages) {
                    historyPage += 1;
                    renderChatHistory();
                }
            });
        }
    }

    // 渲染对话历史
    function renderChatHistory() {
        const chatHistoryContainer = document.getElementById('chat-history');
        if (!chatHistoryContainer) return;

        const filtered = getFilteredHistory();
        const totalPages = Math.max(1, Math.ceil(filtered.length / historyPageSize));
        if (historyPage > totalPages) historyPage = totalPages;
        const start = (historyPage - 1) * historyPageSize;
        const pageItems = filtered.slice(start, start + historyPageSize);

        chatHistoryContainer.innerHTML = '';
        if (pageItems.length === 0) {
            chatHistoryContainer.innerHTML = `<div class="history-empty">暂无匹配的对话</div>`;
        } else {
            pageItems.forEach((session) => {
                const sessionDiv = document.createElement('div');
                sessionDiv.className = 'history-item' + (session.sessionId === currentSessionId ? ' active' : '');
                sessionDiv.setAttribute('data-session-id', session.sessionId);
                const timeAgo = getTimeAgo(session.updateTime);
                sessionDiv.innerHTML = `
                    <div class="history-item-title">${escapeHtml(session.title || '未命名对话')}</div>
                    <div class="history-item-meta">${timeAgo} · ${escapeHtml(session.modelName || '')}</div>
                `;
                sessionDiv.addEventListener('click', () => {
                    loadSession(session.sessionId);
                    const modal = document.getElementById('history-modal');
                    if (modal) {
                        modal.classList.remove('open');
                        modal.setAttribute('aria-hidden', 'true');
                    }
                });
                chatHistoryContainer.appendChild(sessionDiv);
            });
        }

        const pageInfo = document.getElementById('history-page-info');
        const prevBtn = document.getElementById('history-prev-btn');
        const nextBtn = document.getElementById('history-next-btn');
        if (pageInfo) pageInfo.textContent = `第 ${historyPage} / ${totalPages} 页 · ${filtered.length} 条`;
        if (prevBtn) prevBtn.disabled = historyPage <= 1;
        if (nextBtn) nextBtn.disabled = historyPage >= totalPages;
    }

    // 加载指定会话
    async function loadSession(sessionId) {
        // 如果正在流式传输，不允许切换会话
        if (isStreaming) {
            showToast('请等待当前AI回答完成', 'warning');
            return;
        }

        try {
            const response = await fetch(`/api/chat/session/${sessionId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                console.error('加载会话失败: HTTP', response.status);
                showToast('加载会话失败: HTTP ' + response.status, 'error');
                return;
            }

            const data = await response.json();
            if (data.success && data.history) {
                currentSessionId = sessionId;
                displaySessionHistory(data.history);
                showToast('会话已加载', 'success');

                // 重置加载历史标志
                isLoadingHistory = false;
            } else {
                showToast('加载会话失败: ' + data.message, 'error');
            }
        } catch (error) {
            console.error('加载会话失败:', error);
            showToast('加载会话失败', 'error');
        }
    }

    // 显示会话历史
    function displaySessionHistory(history) {
        const chatMessages = document.getElementById('chat-messages');
        chatMessages.innerHTML = '';

        if (history.messages && history.messages.length > 0) {
            isLoadingHistory = true;
            setStage('thread');
            history.messages.forEach(message => {
                addMessage(message.content, message.sender, message.enableMarkdown === 1);
            });
            isLoadingHistory = false;
            stickToBottom = true;
            setTimeout(() => {
                scrollToBottom();
            }, 50);
        } else {
            setStage('empty');
            renderIntentGrid();
        }
    }

    // 获取时间差
    function getTimeAgo(dateTime) {
        const now = new Date();
        const time = new Date(dateTime);
        const diffMs = now - time;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return '刚刚';
        if (diffMins < 60) return `${diffMins}分钟前`;
        if (diffHours < 24) return `${diffHours}小时前`;
        if (diffDays < 7) return `${diffDays}天前`;
        return time.toLocaleDateString();
    }

    // 切换思考内容显示/隐藏
    window.toggleThinking = function (header) {
        const thinkingSection = header.closest('.thinking-section');
        if (!thinkingSection) return;
        const thinkingIcon = header.querySelector('.thinking-icon');
        const isOpen = thinkingSection.classList.toggle('open');
        if (thinkingIcon) {
            thinkingIcon.className = `fa-solid ${isOpen ? 'fa-chevron-up' : 'fa-chevron-down'} thinking-icon ml-auto`;
        }
    };

    // 自动折叠思考内容
    function autoCollapseThinking() {
        setTimeout(() => {
            document.querySelectorAll('.thinking-section').forEach((section) => {
                section.classList.remove('open');
                section.classList.add('is-done');
                const thinkingIcon = section.querySelector('.thinking-icon');
                const thinkingTitle = section.querySelector('.thinking-title');
                if (thinkingIcon) {
                    thinkingIcon.className = 'fa-solid fa-chevron-down thinking-icon ml-auto';
                }
                if (thinkingTitle && !section.classList.contains('is-streaming')) {
                    thinkingTitle.textContent = '已完成思考';
                }
            });
        }, 400);
    }

    // 显示确认对话框
    function showConfirmDialog(message, onConfirm) {
        // 创建遮罩层
        const overlay = document.createElement('div');
        overlay.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';

        // 创建对话框
        const dialog = document.createElement('div');
        dialog.className = 'bg-white rounded-lg shadow-xl p-6 max-w-sm w-full mx-4 transform transition-all duration-300 scale-95 opacity-0';

        dialog.innerHTML = `
                <div class="flex items-center mb-4">
                    <div class="flex-shrink-0 w-10 h-10 bg-red-100 rounded-full flex items-center justify-center">
                        <i class="fa fa-exclamation-triangle text-red-600"></i>
                    </div>
                    <div class="ml-3">
                        <h3 class="text-lg font-medium text-gray-900">确认操作</h3>
                    </div>
                </div>
                <div class="mb-6">
                    <p class="text-sm text-gray-600">${message}</p>
                </div>
                <div class="flex justify-end space-x-3">
                    <button class="cancel-btn px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors">
                        取消
                    </button>
                    <button class="confirm-btn px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors">
                        确认
                    </button>
                </div>
            `;

        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        // 显示动画
        setTimeout(() => {
            dialog.classList.remove('scale-95', 'opacity-0');
            dialog.classList.add('scale-100', 'opacity-100');
        }, 10);

        // 绑定事件
        const cancelBtn = dialog.querySelector('.cancel-btn');
        const confirmBtn = dialog.querySelector('.confirm-btn');

        function closeDialog() {
            dialog.classList.add('scale-95', 'opacity-0');
            setTimeout(() => {
                document.body.removeChild(overlay);
            }, 200);
        }

        cancelBtn.addEventListener('click', closeDialog);
        confirmBtn.addEventListener('click', () => {
            closeDialog();
            onConfirm();
        });

        // 点击遮罩层关闭
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeDialog();
            }
        });

        // ESC键关闭
        document.addEventListener('keydown', function escHandler(e) {
            if (e.key === 'Escape') {
                closeDialog();
                document.removeEventListener('keydown', escHandler);
            }
        });
    }
})();