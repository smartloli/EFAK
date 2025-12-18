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
    let isUserStopRequested = false; // 用户是否主动点击“停止”（用于抑制onerror误报）

    // 从URL参数中获取集群ID
    function getClusterIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('cid') || '';
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
        // 加载最近一次对话历史
        loadLatestChatHistory();
    });

    // 初始化AI助手
    function initAIAssistant() {
        const chatInput = document.getElementById('chat-input');
        const sendBtn = document.getElementById('send-btn');
        const chatMessages = document.getElementById('chat-messages');
        const modelOptions = document.querySelectorAll('.model-option');
        const quickActions = document.querySelectorAll('.quick-action');

        // 监听输入变化
        chatInput.addEventListener('input', function () {
            if (!isStreaming) {
                sendBtn.disabled = this.value.trim() === '';
            }
            autoResize(this);
        });

        // 监听键盘事件
        chatInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (!isTyping && !isStreaming && this.value.trim()) {
                    sendMessage();
                }
            }
        });

        // 发送按钮点击
        sendBtn.addEventListener('click', handleSendButtonClick);

        // 模型选择事件委托
        document.getElementById('model-options').addEventListener('click', function (e) {
            const modelOption = e.target.closest('.model-option');
            if (modelOption) {
                // 如果正在流式传输，不允许切换模型
                if (isStreaming) {
                    showToast('请等待当前AI回答完成', 'warning');
                    return;
                }

                document.querySelectorAll('.model-option').forEach(opt => opt.classList.remove('active'));
                modelOption.classList.add('active');
                currentModel = modelOption.getAttribute('data-model');
                currentModelId = modelOption.getAttribute('data-model-id');
                updateModelDisplay();

                // 关闭之前的SSE连接
                if (eventSource) {
                    eventSource.close();
                }
            }
        });

        // 快捷操作
        quickActions.forEach(action => {
            action.addEventListener('click', function () {
                const actionType = this.getAttribute('data-action');
                handleQuickAction(actionType);
            });
        });

        // 创建新会话
        document.getElementById('new-chat-btn').addEventListener('click', function () {
            // 如果正在流式传输，不允许创建新会话
            if (isStreaming) {
                showToast('请等待当前AI回答完成', 'warning');
                return;
            }

            showConfirmDialog('确定要创建新会话吗？当前会话将被保存。', function () {
                createNewChat();
            });
        });

        // 加载对话历史
        loadChatHistory();

        // 监听设置变更
        document.getElementById('enable-highlight').addEventListener('change', function () {
            // 重新初始化Markdown渲染器
            reinitMarkdownRenderer();

            // 重新应用代码高亮到所有现有消息
            const chatMessages = document.getElementById('chat-messages');
            const assistantMessages = chatMessages.querySelectorAll('.message-bubble.assistant');
            assistantMessages.forEach(message => {
                applyCodeHighlighting(message);
            });

            // 如果正在流式传输，也要重新应用代码高亮
            const typingIndicator = document.getElementById('typing-indicator');
            if (typingIndicator) {
                const markdownContent = typingIndicator.querySelector('.markdown-content');
                if (markdownContent) {
                    applyCodeHighlighting(markdownContent);
                }
            }
        });
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

        // 添加用户消息到界面
        addMessage(message, 'user');

        // 保存用户消息到数据库
        await saveMessageToDatabase(message, 'user');

        // 清空输入框
        chatInput.value = '';
        document.getElementById('send-btn').disabled = true;

        // 重置高度
        chatInput.style.height = 'auto';

        // 禁用输入框，防止重复发送
        chatInput.disabled = true;

        // 显示AI正在输入
        showTypingIndicator();

        // 调用AI API获取回复
        await callAIAPI(message);
    }

    // 处理快捷操作
    async function handleQuickAction(actionType) {
        // 如果正在流式传输，不允许快捷操作
        if (isStreaming) {
            showToast('请等待当前AI回答完成', 'warning');
            return;
        }

        // 确保流式传输状态已重置
        if (eventSource) {
            eventSource.close();
            eventSource = null;
        }
        isStreaming = false;
        currentStreamId = null;

        const actionMap = {
            'alert-analysis': `请分析当前集群的告警情况并提供优化建议：
1. 查询集群的所有告警信息（使用get_alerts函数）
2. 统计告警类型、严重程度和发生频率
3. 分析告警产生的根本原因
4. 提供针对性的解决方案和预防措施
5. 如果有告警，请用表格展示关键告警信息`,
            'analyze-performance': `查询所有Broker的CPU和内存使用率`,
            'cluster-health': '请检查集群健康状态',
            'explain-kafka': '请解释Kafka的核心概念和架构'
        };

        const message = actionMap[actionType];
        if (message) {
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

            // 添加用户消息到界面
            addMessage(message, 'user');

            // 保存用户消息到数据库
            await saveMessageToDatabase(message, 'user');

            // 清空输入框并禁用
            const chatInput = document.getElementById('chat-input');
            if (chatInput) {
                chatInput.value = '';
                chatInput.disabled = true;
            }
            document.getElementById('send-btn').disabled = true;

            // 显示AI正在输入
            showTypingIndicator();

            // 调用AI API获取回复
            await callAIAPI(message);
        }
    }

    // 添加消息到聊天区域
    function addMessage(text, sender, enableMarkdown = false, thinkingContent = '') {
        const chatMessages = document.getElementById('chat-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message-container';
        // 确保消息容器不会超出父容器宽度
        messageDiv.style.maxWidth = '100%';
        messageDiv.style.overflowX = 'hidden';

        const avatar = document.createElement('div');
        avatar.className = `message-avatar ${sender}`;

        if (sender === 'user') {
            // 使用当前登录用户头像
            avatar.innerHTML = '<img src="/images/user_profile.jpg" alt="用户" class="w-full h-full object-cover rounded-full">';
        } else {
            // 使用图片作为AI助手头像
            avatar.innerHTML = '<img src="/images/ai_robot.jpg" alt="AI助手" class="w-full h-full object-cover rounded-full">';
        }

        const bubble = document.createElement('div');
        bubble.className = `message-bubble ${sender} p-4 rounded-lg`;

        const content = document.createElement('div');
        content.className = enableMarkdown ? 'markdown-content' : '';

        if (enableMarkdown && sender === 'assistant') {
            let displayContent = '';

            if (thinkingContent) {
                // 显示思考内容和正式回答
                displayContent = `
                    <div class="thinking-section mb-3">
                        <div class="thinking-header" onclick="toggleThinking(this)">
                            <i class="fa fa-cog text-gray-300 mr-2"></i>
                            <span class="thinking-title">AI思考过程</span>
                            <i class="fa fa-chevron-up thinking-icon ml-auto"></i>
                        </div>
                        <div class="thinking-content">
                            <div class="thinking-text">${thinkingContent}</div>
                        </div>
                    </div>
                    <div class="answer-content">
                        ${renderMarkdown(text)}
                    </div>
                `;
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
                <button class="action-btn" onclick="copyMessage(this)">
                    <i class="fa fa-copy"></i> 复制
                </button>
                <button class="action-btn" onclick="regenerateResponse(this)">
                    <i class="fa fa-refresh"></i> 重新生成
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'good')">
                    <i class="fa fa-thumbs-up"></i>
                </button>
                <button class="action-btn" onclick="rateMessage(this, 'bad')">
                    <i class="fa fa-thumbs-down"></i>
                </button>
            `;
            content.appendChild(actions);
        }

        bubble.appendChild(content);

        if (sender === 'user') {
            messageDiv.className += ' flex flex-col items-end';
            // 创建用户消息容器
            const userMessageContainer = document.createElement('div');
            userMessageContainer.className = 'flex items-start space-x-3';
            userMessageContainer.style.maxWidth = '100%';
            messageDiv.appendChild(userMessageContainer);

            // 将消息和头像添加到容器中（头像在消息末尾）
            userMessageContainer.appendChild(bubble);
            userMessageContainer.appendChild(avatar);
        } else {
            messageDiv.className += ' flex items-start space-x-3';
            messageDiv.style.maxWidth = '100%';
            messageDiv.appendChild(avatar);
            messageDiv.appendChild(bubble);
        }

        // 添加时间戳（仅用户消息）
        if (sender === 'user') {
            const timestampDiv = document.createElement('div');
            timestampDiv.className = 'text-xs text-gray-400 mt-1';
            timestampDiv.textContent = new Date().toLocaleTimeString();
            messageDiv.appendChild(timestampDiv);
        }

        chatMessages.appendChild(messageDiv);

        // 只有在添加新消息时才滚动到底部，加载历史消息时不滚动
        if (!isLoadingHistory) {
            scrollToBottom(chatMessages);
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
            // 新一轮请求开始，重置“主动停止”标记
            isUserStopRequested = false;

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
            const es = new EventSource(url);
            eventSource = es;

            // 设置流式传输状态
            isStreaming = true;

            // 更新发送按钮为停止按钮
            updateSendButtonToStop();

            let aiResponse = '';
            let thinkingContent = '';
            let isThinking = false;
            let isTerminal = false; // 是否已收到end/error

            es.onmessage = async function (event) {
                let data;
                try {
                    // 服务端可能发送ping/非JSON帧，这里做容错，避免一次解析失败导致“无返回”
                    data = JSON.parse(event.data);
                } catch (e) {
                    console.warn('SSE消息解析失败，已忽略:', event.data, e);
                    return;
                }

                if (!data || !data.type) {
                    return;
                }

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
                    // 思考结束，准备开始正式回答
                    isThinking = false;
                    updateTypingMessage('', thinkingContent, false);
                } else if (data.type === 'end') {
                    isTerminal = true;
                    es.close();
                    if (eventSource === es) {
                        eventSource = null;
                    }
                    hideTypingIndicator();
                    addMessage(aiResponse, 'assistant', true, thinkingContent);
                    await saveMessageToDatabase(aiResponse, 'assistant');
                    // 自动折叠思考内容
                    autoCollapseThinking();
                    // 重置发送按钮
                    resetSendButton();
                } else if (data.type === 'error') {
                    isTerminal = true;
                    es.close();
                    if (eventSource === es) {
                        eventSource = null;
                    }
                    hideTypingIndicator();
                    addMessage('AI回复出错: ' + data.message, 'assistant');
                    await saveMessageToDatabase('AI回复出错: ' + data.message, 'assistant');
                    // 重置发送按钮
                    resetSendButton();
                } else if (data.type === 'ping') {
                    // SSE保活帧：前端无需处理
                    return;
                }
            };

            es.onerror = async function (event) {
                // 正常结束/主动停止时，浏览器也可能触发onerror，这里避免误报
                if (isTerminal || isUserStopRequested) {
                    return;
                }

                try {
                    es.close();
                } catch (e) {
                }
                if (eventSource === es) {
                    eventSource = null;
                }
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
                    // 显示思考内容
                    displayContent = `
                        <div class="thinking-section mb-3">
                            <div class="thinking-header" onclick="toggleThinking(this)">
                                <i class="fa fa-cog text-gray-300 mr-2"></i>
                                <span class="thinking-title">AI思考过程</span>
                                <i class="fa fa-chevron-down thinking-icon ml-auto"></i>
                            </div>
                            <div class="thinking-content">
                                <div class="thinking-text">${thinkingContent}</div>
                            </div>
                        </div>
                    `;
                } else if (thinkingContent && content) {
                    // 显示思考内容和正式回答
                    displayContent = `
                        <div class="thinking-section mb-3">
                            <div class="thinking-header" onclick="toggleThinking(this)">
                                <i class="fa fa-cog text-gray-300 mr-2"></i>
                                <span class="thinking-title">AI思考过程</span>
                                <i class="fa fa-chevron-up thinking-icon ml-auto"></i>
                            </div>
                            <div class="thinking-content">
                                <div class="thinking-text">${thinkingContent}</div>
                            </div>
                        </div>
                        <div class="answer-content">
                            ${renderMarkdown(content)}
                        </div>
                    `;
                } else if (content) {
                    // 只显示正式回答
                    displayContent = renderMarkdown(content);
                } else if (thinkingContent) {
                    // 只有思考内容，没有正式回答
                    displayContent = `
                        <div class="thinking-section mb-3">
                            <div class="thinking-header" onclick="toggleThinking(this)">
                                <i class="fa fa-cog text-gray-300 mr-2"></i>
                                <span class="thinking-title">AI思考过程</span>
                                <i class="fa fa-chevron-up thinking-icon ml-auto"></i>
                            </div>
                            <div class="thinking-content">
                                <div class="thinking-text">${thinkingContent}</div>
                            </div>
                        </div>
                    `;
                }

                markdownContent.innerHTML = displayContent;

                // 应用代码高亮
                const enableHighlight = document.getElementById('enable-highlight');
                if (enableHighlight && enableHighlight.checked) {
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

    // 平滑滚动到底部
    function scrollToBottom(element) {
        // 确保元素存在且有滚动功能
        if (element) {
            // 强制滚动到底部，确保新消息可见
            setTimeout(() => {
                element.scrollTo({
                    top: element.scrollHeight,
                    behavior: 'smooth'
                });
            }, 10);
        }
    }

    // 初始化Markdown渲染器
    function initMarkdownRenderer() {
        if (typeof marked !== 'undefined') {
            marked.setOptions({
                highlight: function (code, lang) {
                    // 检查是否启用了代码高亮
                    const enableHighlight = document.getElementById('enable-highlight');
                    if (enableHighlight && enableHighlight.checked && typeof hljs !== 'undefined' && lang && hljs.getLanguage(lang)) {
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
                    const enableHighlight = document.getElementById('enable-highlight');
                    if (enableHighlight && enableHighlight.checked && typeof hljs !== 'undefined' && lang && hljs.getLanguage(lang)) {
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

    // 应用代码高亮
    function applyCodeHighlighting(container) {
        const enableHighlight = document.getElementById('enable-highlight');
        if (!enableHighlight || !enableHighlight.checked || typeof hljs === 'undefined') {
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
        typingDiv.className = 'flex items-start space-x-3';

        const avatar = document.createElement('div');
        avatar.className = 'message-avatar assistant';
        avatar.innerHTML = '<img src="/images/ai_robot.jpg" alt="AI助手" class="w-full h-full object-cover rounded-full">';

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble assistant p-4 rounded-lg';

        const typingContent = document.createElement('div');
        typingContent.className = 'typing-indicator';
        typingContent.innerHTML = `
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="ml-2 text-sm text-gray-500">AI正在思考...</span>
            `;

        // 添加Markdown内容容器
        const markdownContent = document.createElement('div');
        markdownContent.className = 'markdown-content mt-2';
        markdownContent.style.display = 'none';

        bubble.appendChild(typingContent);
        bubble.appendChild(markdownContent);
        typingDiv.appendChild(avatar);
        typingDiv.appendChild(bubble);

        chatMessages.appendChild(typingDiv);
        scrollToBottom(chatMessages);

        // 禁用输入框，防止重复发送
        const chatInput = document.getElementById('chat-input');
        if (chatInput) {
            chatInput.disabled = true;
        }
    }

    // 停止流式传输
    async function stopStreaming() {
        if (!isStreaming || !currentStreamId) {
            return;
        }

        try {
            // 标记为用户主动停止，避免关闭连接触发onerror误报
            isUserStopRequested = true;

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
            isUserStopRequested = false;
            hideTypingIndicator();
            resetSendButton();
        }
    }

    // 更新发送按钮为停止按钮
    function updateSendButtonToStop() {
        const sendBtn = document.getElementById('send-btn');
        sendBtn.setAttribute('data-action', 'stop');
        sendBtn.innerHTML = '<i class="fa fa-stop-circle"></i>';
        sendBtn.className = 'text-black hover:text-gray-800 transition-colors text-xl';
        sendBtn.disabled = false;
    }

    // 重置发送按钮
    function resetSendButton() {
        const sendBtn = document.getElementById('send-btn');
        const chatInput = document.getElementById('chat-input');

        sendBtn.setAttribute('data-action', 'send');
        sendBtn.innerHTML = '<i class="fa fa-arrow-up"></i>';
        sendBtn.className = 'text-gray-400 hover:text-blue-500 transition-colors text-xl disabled:opacity-50 disabled:cursor-not-allowed';
        sendBtn.disabled = chatInput.value.trim() === '';

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

    // 更新模型显示
    function updateModelDisplay() {
        // 这里可以添加模型切换的UI反馈
        showToast(`已切换到${getModelDisplayName(currentModel)}`, 'success');
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

                // 设置默认选中的模型
                if (modelConfigs.length > 0) {
                    currentModel = modelConfigs[0].modelName;
                    currentModelId = modelConfigs[0].id;
                    const firstOption = document.querySelector('.model-option');
                    if (firstOption) {
                        firstOption.classList.add('active');
                    }

                    // 更新欢迎消息中的模型名称
                    const welcomeModelName = document.getElementById('welcome-model-name');
                    if (welcomeModelName) {
                        welcomeModelName.textContent = currentModel;
                    }
                }
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

        modelConfigs.forEach((model, index) => {
            const modelOption = document.createElement('div');
            modelOption.className = `model-option ${index === 0 ? 'active' : ''}`;
            modelOption.setAttribute('data-model', model.modelName);
            modelOption.setAttribute('data-model-id', model.id);

            const statusClass = model.status === 1 ? 'bg-green-400' :
                model.status === 2 ? 'bg-red-400' : 'bg-gray-400';
            const statusText = model.status === 1 ? '在线' :
                model.status === 2 ? '错误' : '离线';

            modelOption.innerHTML = `
                    <div class="flex items-center justify-between">
                        <div>
                            <div class="font-medium">${model.modelName}</div>
                            <div class="text-xs">${model.description || model.apiType}</div>
                        </div>
                        <div class="w-2 h-2 ${statusClass} rounded-full" title="${statusText}"></div>
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

    // 显示提示消息
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        let bgColor = 'bg-blue-500';

        switch (type) {
            case 'success':
                bgColor = 'bg-green-500';
                break;
            case 'error':
                bgColor = 'bg-red-500';
                break;
            case 'warning':
                bgColor = 'bg-yellow-500';
                break;
            default:
                bgColor = 'bg-blue-500';
        }

        toast.className = `fixed top-4 right-4 z-50 px-4 py-2 rounded-lg text-white transform transition-all duration-300 ${bgColor}`;
        toast.textContent = message;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => document.body.removeChild(toast), 300);
        }, 2000);
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

                // 重新添加欢迎消息
                const welcomeMessageHTML = `
                    <div class="flex items-start space-x-3">
                        <div class="message-avatar assistant">
                            <img src="/images/ai_robot.jpg" alt="AI助手"
                                class="w-full h-full object-cover rounded-full">
                        </div>
                        <div class="message-bubble assistant p-4 rounded-lg">
                            <div class="flex items-center justify-between mb-2">
                                <div class="text-sm text-gray-600">AI助手</div>
                                <div class="text-xs text-gray-400" id="new-welcome-model-name">${currentModel || '正在加载...'}</div>
                            </div>
                            <div class="markdown-content">
                                <p>您好！我是 EFAK AI 智能助手。我支持：</p>
                                <ul>
                                    <li>🤖 <strong>多种大模型</strong> - OpenAI, Claude, DeepSeek等</li>
                                    <li>📊 <strong>数据可视化</strong> - 自动生成图表和统计分析</li>
                                    <li>📝 <strong>Markdown渲染</strong> - 支持代码高亮、表格、数学公式</li>
                                    <li>🔍 <strong>Kafka专家</strong> - 集群分析、性能优化、故障诊断</li>
                                </ul>
                                <p>请选择您偏好的大模型，然后告诉我您需要什么帮助！</p>
                            </div>
                        </div>
                    </div>
                `;
                chatMessages.innerHTML = welcomeMessageHTML;

                // 清空对话历史
                conversationHistory = [];

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
                renderChatHistory(data.sessions);
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
                console.log('没有找到历史对话，显示欢迎消息');
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

    // 渲染对话历史
    function renderChatHistory(sessions) {
        const chatHistoryContainer = document.getElementById('chat-history');
        if (!chatHistoryContainer) return;

        chatHistoryContainer.innerHTML = '';

        if (sessions.length === 0) {
            chatHistoryContainer.innerHTML = `
                <div class="text-center text-gray-500 text-sm py-4">
                    暂无对话历史
                </div>
            `;
            return;
        }

        sessions.forEach(session => {
            const sessionDiv = document.createElement('div');
            sessionDiv.className = 'p-2 hover:bg-gray-50 rounded cursor-pointer text-sm';
            sessionDiv.setAttribute('data-session-id', session.sessionId);

            const timeAgo = getTimeAgo(session.updateTime);

            sessionDiv.innerHTML = `
                <div class="font-medium text-gray-700">${session.title}</div>
                <div class="text-gray-500 text-xs">${timeAgo} · ${session.modelName}</div>
            `;

            sessionDiv.addEventListener('click', () => {
                loadSession(session.sessionId);
            });

            chatHistoryContainer.appendChild(sessionDiv);
        });
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
            // 设置加载历史标志
            isLoadingHistory = true;

            // 显示历史消息
            history.messages.forEach(message => {
                addMessage(message.content, message.sender, message.enableMarkdown === 1);
            });

            // 重置加载历史标志
            isLoadingHistory = false;

            // 滚动到底部，确保所有内容都加载完成
            setTimeout(() => {
                scrollToBottom(chatMessages);
            }, 200);
        } else {
            // 显示欢迎消息
            const welcomeMessageHTML = `
                <div class="flex items-start space-x-3">
                    <div class="message-avatar assistant">
                        <img src="/images/ai_robot.jpg" alt="AI助手"
                            class="w-full h-full object-cover rounded-full">
                    </div>
                    <div class="message-bubble assistant p-4 rounded-lg">
                        <div class="flex items-center justify-between mb-2">
                            <div class="text-sm text-gray-600">AI助手</div>
                            <div class="text-xs text-gray-400">${history.modelName || '正在加载...'}</div>
                            <div class="text-xs text-gray-400" id="welcome-model-name">${currentModel || '正在加载...'}</div>
                        </div>
                        <div class="markdown-content">
                            <p>您好！我是 EFAK AI 智能助手。我支持：</p>
                            <ul>
                                <li>🤖 <strong>多种大模型</strong> - OpenAI, Claude, DeepSeek等</li>
                                <li>📊 <strong>数据可视化</strong> - 自动生成图表和统计分析</li>
                                <li>📝 <strong>Markdown渲染</strong> - 支持代码高亮、表格、数学公式</li>
                                <li>🔍 <strong>Kafka专家</strong> - 集群分析、性能优化、故障诊断</li>
                            </ul>
                            <p>请选择您偏好的大模型，然后告诉我您需要什么帮助！</p>
                        </div>
                    </div>
                </div>
            `;
            chatMessages.innerHTML = welcomeMessageHTML;
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
        const thinkingContent = thinkingSection.querySelector('.thinking-content');
        const thinkingIcon = header.querySelector('.thinking-icon');
        const thinkingTitle = header.querySelector('.thinking-title');

        if (thinkingContent.style.display === 'none' || !thinkingContent.style.display) {
            // 展开
            thinkingContent.style.display = 'block';
            thinkingIcon.className = 'fa fa-chevron-up thinking-icon ml-auto';
            thinkingTitle.textContent = 'AI思考过程';
        } else {
            // 折叠
            thinkingContent.style.display = 'none';
            thinkingIcon.className = 'fa fa-chevron-down thinking-icon ml-auto';
            thinkingTitle.textContent = 'AI思考过程 (已折叠)';
        }
    };

    // 自动折叠思考内容
    function autoCollapseThinking() {
        setTimeout(() => {
            const thinkingSections = document.querySelectorAll('.thinking-section');
            thinkingSections.forEach(section => {
                const header = section.querySelector('.thinking-header');
                const thinkingContent = section.querySelector('.thinking-content');
                const thinkingIcon = header.querySelector('.thinking-icon');
                const thinkingTitle = header.querySelector('.thinking-title');

                // 自动折叠
                thinkingContent.style.display = 'none';
                thinkingIcon.className = 'fa fa-chevron-down thinking-icon ml-auto';
                thinkingTitle.textContent = 'AI思考过程 (已折叠)';
            });
        }, 2000); // 2秒后自动折叠
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
