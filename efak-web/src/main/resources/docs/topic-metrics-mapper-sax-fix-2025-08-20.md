# 修复记录：TopicMetricsMapper SAXParseException 异常

- 问题摘要：
  - 启动时报错：Error creating bean with name 'topicMetricsMapper' ... Cause: org.xml.sax.SAXParseException: 元素内容必须由格式正确的字符数据或标记组成。
  - 触发位置：`TopicMetricsMapper.class` 注解 SQL 的 `<script>` 片段解析。

- 根因分析：
  - 在 MyBatis 注解方式的 `<script>` 动态 SQL 中，XML 文本节点里不允许出现裸的 `<` 符号；语句 `AND collect_time <= #{endTime}` 中的 `<` 会被 XML 解析器当成标签起始符，导致 `SAXParseException`。
  - 需要将 `<` 转义为 `&lt;`，或使用 `<![CDATA[ ... ]]>` 包裹。注解中使用转义更直观稳定。

- 修复内容：
  - 文件：/Users/smartloli/workspace/EFAK-AI/efak-web/src/main/java/org/kafka/eagle/web/mapper/TopicMetricsMapper.java
  - 变更点（均位于 `<script>` 的 `<if test='endTime != null'>` 分支内）：
    - 将 `AND collect_time <= #{endTime}` 改为 `AND collect_time &lt;= #{endTime}`（出现两处：分页查询与统计总数各一处）。
  - 同时核查：
    - `<if test="topicName != null and topicName != ''">` 的引号用法规范，双引号用于 test 属性，空字符串使用两个单引号，已正确；
    - 其它 `<if>` 条件（startTime、offset/limit 等）表达式语法与参数占位符均正确；
    - `@Select` 非 `<script>` 的纯 SQL 片段中保留了 `<=` 原样（无需转义，因其不走 XML 解析）。

- 验证结果：
  - 执行：`source ~/.bash_profile && mvn clean compile -DskipTests`
  - 结果：BUILD SUCCESS（efak-web 模块编译通过），异常消除。

- 后续建议：
  - 注解中使用 `<script>` + 动态标签时，凡是 SQL 文本包含 `<`（如 `<`、`<=`），务必写为 `&lt;`；
  - 或者采用 `<![CDATA[ ... ]]>` 包裹文本，但在注解字符串中需注意转义与拼接，可读性略差；
  - 统一团队规范：
    - test 属性使用双引号；
    - 字符串字面量使用单引号；
    - SQL 文本中的 `<` 统一转义为 `&lt;`。

- 影响范围评估：
  - 本次仅修改 TopicMetricsMapper 的 2 处 `<=`，未改变 SQL 逻辑与入参与出参映射，行为等价，安全可回归。