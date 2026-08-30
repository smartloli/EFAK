# 多集群管理页面访问异常（No static resource manger）排查与修复记录

- 时间：2025-08-25
- 模块：efak-web
- 问题现象：访问多集群管理页面时抛出异常：`org.springframework.web.servlet.resource.NoResourceFoundException: No static resource manger`。

## 根因分析
- 报错关键字为 `manger`（少了字母 `a`），这并不是一个有效的控制器路由或静态资源路径。
- 当请求路径没有匹配到任何控制器映射时，Spring 会尝试按静态资源处理；由于本项目只暴露了 `/statics/**` 为静态资源路径，`/manger` 最终以“静态资源查找失败”告终，从而抛出上述异常。
- 全局检索未发现项目中有对 `/manger` 的链接或跳转逻辑，推测为手动输入地址时的拼写误差。

## 本次改动
1. 新增兼容重定向（容错）：
   - 在菜单控制器中增加 `/manger` 到 `/manager` 的重定向映射，避免再次出现该拼写错误导致的异常。
   - 位置：<mcfile name="MenuController.java" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/controller/MenuController.java"></mcfile>
2. 新增侧边栏菜单入口：
   - 在侧边栏导航新增“多集群管理”菜单项，指向 `/manager`，提升可发现性，减少手动输入 URL 的概率。
   - 位置：<mcfile name="navbar.html" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/templates/public/navbar.html"></mcfile>
3. 现有页面与脚本：
   - 页面：<mcfile name="manager.html" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/templates/view/manager.html"></mcfile>
   - 脚本：<mcfile name="manager.js" path="/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/resources/statics/js/system/manager.js"></mcfile>
4. 编译验证：
   - 已执行 `mvn clean compile -DskipTests` 编译通过，无语法/依赖错误。

## 使用与验证
- 访问路径（两者皆可）：
  - 正确入口：`/manager`
  - 容错入口：`/manger`（将自动 302 重定向到 `/manager`）
- 侧边栏导航中，系统管理分组下新增了“多集群管理”菜单，点击即可进入。
- 若仍遇到 403 或跳转登录，请确认当前账号具备管理员角色（ROLE_ADMIN）。

## 其他说明
- 静态资源仅在 `/statics/**` 下提供；`manager.js` 已通过 `th:src="@{/statics/js/system/manager.js}"` 正确引入。
- 未对后端 API 做变更，`/api/manager/cluster/list` 接口保持不变。

## 后续可选
- 如需进一步防呆，也可在全局异常页中对常见拼写误差进行友好提示或统一跳转。
