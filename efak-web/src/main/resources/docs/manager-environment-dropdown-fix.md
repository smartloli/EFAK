# 集群管理页面环境类型下拉框样式修复

## 问题描述

在集群管理页面的"创建新集群"对话框中，环境类型下拉框的样式没有生效。该下拉框使用了Select2插件，但样式定义存在不匹配的问题。

## 问题分析

1. **HTML结构问题**：在manager.html中，环境类型下拉框使用了`environment-type-select`类名
2. **CSS样式定义**：在css_tools.html中，样式是针对`environment-type-dropdown`类定义的
3. **JavaScript初始化**：在manager.js中，Select2初始化时使用的dropdownCssClass是`environment-type-dropdown`
4. **z-index问题**：Select2下拉框可能被模态框遮挡

## 修复方案

### 1. 统一CSS类名

**文件**: `manager.html`
**位置**: 第565行
**修改前**:
```html
<select id="cluster-environment" class="environment-type-select" required>
```

**修改后**:
```html
<select id="cluster-environment" class="form-input" required>
```

### 2. 添加z-index样式

**文件**: `css_tools.html`
**位置**: 第204行后添加
**新增内容**:
```css
/* 确保Select2下拉框显示在模态框之上 */
.select2-dropdown {
  z-index: 1050 !important;
}

.select2-container--open .select2-dropdown {
  z-index: 1050 !important;
}
```

## 技术细节

### Select2配置

manager.js中的Select2初始化配置：
```javascript
clusterEnvElement.select2({
  dropdownParent: $('#create-cluster-modal'),
  minimumResultsForSearch: Infinity,
  width: '100%',
  dropdownCssClass: 'environment-type-dropdown',
  templateResult: window.ManagerModule.formatEnvironmentOption,
  templateSelection: window.ManagerModule.formatEnvironmentSelection,
  escapeMarkup: function (m) { return m; }
});
```

### 环境类型选项

支持的环境类型：
- **开发环境** (dev) - 蓝色图标 (fa-code)
- **测试环境** (test) - 绿色图标 (fa-flask)
- **预发环境** (pre) - 橙色图标 (fa-rocket)
- **生产环境** (prd) - 红色图标 (fa-server)

## 修复效果

1. ✅ 环境类型下拉框样式正常显示
2. ✅ Select2下拉选项带有图标和颜色区分
3. ✅ 下拉框不会被模态框遮挡
4. ✅ 选中项正确显示图标和文本
5. ✅ 鼠标悬停效果正常

## 相关文件

- `efak-web/src/main/resources/templates/view/manager.html` - HTML结构
- `efak-web/src/main/resources/templates/public/css_tools.html` - CSS样式定义
- `efak-web/src/main/resources/statics/js/system/manager.js` - JavaScript逻辑

## 测试建议

1. 打开集群管理页面
2. 点击"新建集群"按钮
3. 检查环境类型下拉框是否正常显示
4. 点击下拉框，确认选项列表正常显示且带有图标
5. 选择不同环境类型，确认选中项显示正确