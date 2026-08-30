# 多集群管理页右上角用户菜单悬停不显示问题修复

## 问题概述
- 页面：/view/manager.html
- 现象：鼠标移动到右上角用户区域时，用户菜单未显示。
- 额外现象：预览环境报错 “ReferenceError: tailwind is not defined”。

## 根因分析
1. 用户菜单显示/隐藏仅通过点击事件切换 hidden 类，未实现悬停交互，导致“仅移动鼠标”不会出现下拉菜单。
2. 预览环境未完整加载 Tailwind CDN 脚本时，直接给 tailwind.config 赋值会触发 ReferenceError，使后续脚本（含菜单逻辑）中断执行。

## 修复内容
1. 顶部导航片段增加悬停交互：
   - 在 header 片段中为用户菜单容器添加 mouseenter/mouseleave 事件，使鼠标悬停即显示、移出即隐藏；同时保留点击切换能力。
   - 位置：
     - <mcfile name="header_main.html" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/templates/public/header_main.html"></mcfile>
   - 说明：header 标签已声明 th:fragment="header"，页面通过 <th:block th:replace="~{public/header_main :: header}"></th:block> 引用。

2. Tailwind 配置安全防护（避免预览报错中断）：
   - 在公共片段与 manager 页面中，赋值前确保 window.tailwind 存在：window.tailwind = window.tailwind || {};
   - 位置：
     - <mcfile name="common.html" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/templates/public/common.html"></mcfile>
     - <mcfile name="manager.html" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/templates/view/manager.html"></mcfile>

## 验证结果
- 通过本地预览验证：
  - 悬停在右上角用户头像区域，下拉菜单正常显示；移出后隐藏；点击页面空白处收起。
  - 点击“个人信息”“修改密码”可正常打开对应模态框。
  - 不再出现 “tailwind is not defined” 的脚本错误。

## 影响范围
- 顶部导航公共片段（header_main.html）及公共资源片段（common.html）对全站引用该片段的页面生效。
- manager.html 自身的 Tailwind 配置同样做了防护，避免在独立预览时报错。

## 测试建议
- 在右上角用户区域进行以下操作回归：
  - 悬停显示/移出隐藏
  - 点击页面空白处隐藏
  - 点击“个人信息”“修改密码”功能可用
  - 反复切换/移动，菜单状态与箭头方向同步

## 备注
- 后端构建在预览环境可能出现第三方依赖解析问题，不影响前端模板交互验证；页面静态资源映射由 WebMvcConfig 指向 classpath:/statics/，本地资源路径正常。