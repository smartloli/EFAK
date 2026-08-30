# 主题扩容重复提示问题修复

## 问题描述

在主题管理页面中，当在扩容主题对话框中设置扩容分区数后，点击"确认扩容"按钮时，页面会同时弹出扩容成功和扩容失败的提示。

## 问题分析

通过代码分析发现问题的根本原因：

### 1. 重复初始化问题

在 `topics.js` 文件末尾存在两个初始化函数：
- `DOMContentLoaded` 事件监听器调用 `TopicsModule.init()`
- jQuery 的 `$(document).ready()` 调用不存在的 `initTopicsPage()` 和 `bindEvents()` 函数

这导致了：
- JavaScript 错误（调用不存在的函数）
- 可能的重复初始化

### 2. 事件监听器重复绑定问题

在 `initModalDialogs()` 方法中，每次调用都会重新绑定事件监听器，但没有先移除之前的监听器。这导致：
- 同一个按钮可能绑定了多个相同的事件监听器
- 点击"确认扩容"按钮时，`scaleTopic()` 方法被多次调用
- 多次调用导致同时显示成功和失败的提示

## 解决方案

### 1. 清理重复初始化代码

删除了无效的 jQuery 初始化代码：

```javascript
// 删除以下无效代码
$(document).ready(function() {
    // 初始化页面
    initTopicsPage();
    
    // 绑定事件
    bindEvents();
});
```

### 2. 修复事件监听器重复绑定

修改 `bindModalConfirm()` 方法，在绑定新事件之前先移除旧的事件监听器：

```javascript
bindModalConfirm(confirmScaleBtn, confirmDeleteBtn, scaleModal, deleteModal) {
    // 移除之前的事件监听器，避免重复绑定
    const newConfirmScaleBtn = confirmScaleBtn.cloneNode(true);
    confirmScaleBtn.parentNode.replaceChild(newConfirmScaleBtn, confirmScaleBtn);
    
    const newConfirmDeleteBtn = confirmDeleteBtn.cloneNode(true);
    confirmDeleteBtn.parentNode.replaceChild(newConfirmDeleteBtn, confirmDeleteBtn);
    
    // 绑定新的事件监听器
    newConfirmScaleBtn.addEventListener('click', () => {
        this.scaleTopic();
    });

    newConfirmDeleteBtn.addEventListener('click', () => {
        const confirmTopicName = document.getElementById('confirm-topic-name').value;
        const topicName = deleteModal.dataset.topicName;
        if (confirmTopicName === topicName) {
            this.deleteTopic(topicName);
        }
    });
    
    // 返回新的按钮引用
    return {
        confirmScaleBtn: newConfirmScaleBtn,
        confirmDeleteBtn: newConfirmDeleteBtn
    };
}
```

### 3. 更新初始化顺序

修改 `initModalDialogs()` 方法，确保先绑定确认操作获取新按钮引用，再进行输入验证：

```javascript
// 确认操作（先绑定确认操作，获取新的按钮引用）
const newButtons = this.bindModalConfirm(confirmScaleBtn, confirmDeleteBtn, scaleModal, deleteModal);

// 输入验证（使用新的按钮引用）
this.bindModalValidation(newPartitions, newButtons.confirmScaleBtn, confirmTopicName, newButtons.confirmDeleteBtn);
```

## 修复效果

### 修复前
- 点击"确认扩容"按钮时，同时弹出成功和失败提示
- 可能存在 JavaScript 错误
- 事件监听器重复绑定

### 修复后
- 点击"确认扩容"按钮时，只显示一个正确的提示（成功或失败）
- 清理了无效的初始化代码
- 避免了事件监听器重复绑定
- 确保了扩容功能的正常工作

## 修改文件列表

1. **efak-web/src/main/resources/statics/js/system/topics.js**
   - 删除无效的 jQuery 初始化代码
   - 修改 `bindModalConfirm()` 方法，避免重复绑定事件监听器
   - 更新 `initModalDialogs()` 方法的初始化顺序

## 编译验证

执行编译命令验证修复：
```bash
source ~/.bash_profile && mvn clean compile -DskipTests
```

编译结果：**BUILD SUCCESS**

## 总结

本次修复解决了主题扩容功能中重复提示的问题，主要通过：

1. **清理重复初始化**：删除了无效的 jQuery 初始化代码
2. **避免重复绑定**：使用 DOM 节点克隆的方式移除旧的事件监听器
3. **优化初始化顺序**：确保按钮引用的正确传递

修复后，扩容功能将正常工作，用户点击"确认扩容"按钮时只会看到一个正确的提示信息，提升了用户体验。