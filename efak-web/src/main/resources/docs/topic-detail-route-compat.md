# 主题详情页路由兼容修复记录

- 日期: 2025-08-18
- 模块: efak-web
- 变更类型: 路由兼容与编译验证

## 背景
前端页面 `templates/view/topics.html` 中存在跳转到 `/topic-detail?name=...` 的逻辑，而后端 `MenuController` 已提供的详情页路由为 `/topic/detail`，两者不一致导致可能出现 404 或页面无法打开的问题。

## 修改内容
1. 在 `MenuController` 新增 `/topic-detail` 兼容映射，并重定向到现有 `/topic/detail`：
   - 方法：`topicDetailCompat(String topicName)`
   - 行为：`redirect:/topic/detail?name=<URLEncoded(topicName)>`
   - 关键导入：`org.springframework.web.util.UriUtils` 与 `java.nio.charset.StandardCharsets`

2. 重新编译并验证：
   - 执行 `source ~/.bash_profile && mvn clean compile -DskipTests`（efak-web 模块编译通过）。
   - 启动 efak-web 进行页面验证，确认应用可正常启动，数据库连接成功。

## 验证点
- 访问 `http://localhost:8080/topics` 列表页可正常加载；
- 点击主题跳转或直接访问 `http://localhost:8080/topic-detail?name=<topic>` 可被重定向至 `/topic/detail?name=<topic>` 并展示详情页；
- 主题图标 icon 在列表页与详情页展示正常（当数据库 icon 为空时启用 fallback 逻辑）。

## 影响范围
- 仅影响路由进入逻辑，不改变原有视图模板 `view/topic-detail.html` 与数据接口。

## 回退方案
- 删除新增的 `/topic-detail` 兼容映射方法与相关导入即可回退。

## 备注
- 多模块父 POM 版本解析已修复（各子模块使用固定版本 `5.0.0` 且指定 `relativePath`）。
