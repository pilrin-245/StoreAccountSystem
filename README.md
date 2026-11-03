# 小型店铺收支记账与库存管理系统

基于 **JavaEE（Servlet + JSP + MySQL + MVC + Maven）** 的小型店铺管理系统，包含收支流水、商品库存、审核提醒、营业报表与三级角色权限控制。

---

## 1. 技术栈与环境

| 项目 | 说明 |
| --- | --- |
| 语言 / 编译级别 | Java 11（本机 JDK 21.0.12 运行） |
| Web 容器 | Tomcat 9.0.121（Servlet 4.0 / JSP 2.3） |
| 数据库 | MySQL 9.6（端口 3306，库名 `store_db`） |
| 连接池 | Alibaba Druid 1.2.20 |
| 前端 | JSP + JSTL 1.2 + 原生 CSS/JS |
| 构建 | Maven 3.9.11（IDEA 内置） |
| 测试 | JUnit 4.13.2 |
| 架构 | MVC：entity / dao / service / servlet / filter / util |

本机已完成并验证：

- JDK 21、MySQL 9.6、Tomcat 9.0.121 均可正常工作。
- `mvn clean package` 打包成功，8 个单元测试全部通过。
- WAR 已部署到 Tomcat 并以 HTTP 方式完成登录、销售扣库存、超卖拦截、审核通过、驳回回滚、进货入库、用户/商品新增、系统设置、CSV 导出等联调。

---

## 2. 快速启动

### 方式一：一键脚本（已测试）

双击项目根目录的 **`run.bat`**：

1. 自动执行 `mvn clean package`；
2. 删除旧部署并把 `target/StoreAccountSystem.war` 复制到 Tomcat 的 `webapps`；
3. 启动 Tomcat。

访问：<http://localhost:8080/StoreAccountSystem/>

停止服务：双击 **`stop.bat`**。

> 脚本中使用的是本机实际路径（JDK、Tomcat、Maven）。若换机器，请修改 `run.bat` / `stop.bat` 顶部的 `JAVA_HOME`、`CATALINA_HOME`、`MAVEN_HOME`。

### 方式二：IntelliJ IDEA 运行

1. 用 IDEA 打开项目根目录 `StoreAccountSystem`（识别 `pom.xml`，等待 Maven 依赖导入完成）。
2. 项目已预置 `.idea` 配置：JDK 21、语言级别 Java 11、UTF-8 编码、Web Facet（`src/main/webapp`）。
3. `Run → Edit Configurations → + → Tomcat Server → Local`：
   - **Application server**：`C:\Users\Administrator\Desktop\apache-tomcat-9\apache-tomcat-9.0.121`
   - **HTTP port**：`8080`
4. 切到 **Deployment** 页 → `+ → Artifact` → 选择 `StoreAccountSystem:war exploded`；
   - **Application context**：`/StoreAccountSystem`
5. 保存后点击运行，浏览器访问 <http://localhost:8080/StoreAccountSystem/>。

> Tomcat 运行项属于 IDEA 本机配置，首次需按上面 4 步选择一次；其余环境（JDK/Maven/编码/Web 模块）已随项目配置好。若使用 IDEA Community 版本（不带 Tomcat 集成），请直接使用 `run.bat`。

### 数据库初始化

项目已导入过脚本，正常无需重做。需要恢复演示数据时双击 **`db-init.bat`**，或执行：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p245478 --default-character-set=utf8mb4 < sql/store.sql
```

### 测试账号（密码均为 `123456`）

| 账号 | 角色 | 权限概览 |
| --- | --- | --- |
| `admin` | 管理员 | 全部数据、用户管理、系统配置、报表导出 |
| `manager` | 店长 | 商品档案、出入库、单据审核、报表、提醒 |
| `cashier` | 收银员 | 销售登记（自动扣库存）、库存查询、仅本人流水 |

---

## 3. 目录结构

```
StoreAccountSystem
├── pom.xml
├── run.bat / stop.bat / db-init.bat
├── sql/store.sql                     # 建库建表 + 演示数据
├── src/main/java/com/store
│   ├── entity      User / Goods / IncomeExpense / StockRecord / StatRow / ReportResult
│   ├── dao         BaseDao / UserDao / GoodsDao / IEDao / StockDao / ConfigDao
│   ├── service     UserService / GoodsService / StockService / IEService / RemindService / ReportService
│   ├── servlet     Login / Logout / Dashboard / User / Goods / IE / Stock / Report / Remind / Profile / Setting
│   ├── filter      EncodingFilter（UTF-8）、LoginFilter（登录 + 角色鉴权）
│   └── util        DBUtil（Druid + 事务）、MD5Util、DateUtil、TokenUtil、WebUtil
├── src/main/resources/db.properties  # 数据库连接与连接池参数
├── src/main/webapp
│   ├── WEB-INF/web.xml
│   ├── common/     header / sidebar / footer / pagination
│   ├── login.jsp   home.jsp   profile.jsp
│   ├── ie/         ieList.jsp  ieForm.jsp
│   ├── goods/      goodsList.jsp  goodsForm.jsp
│   ├── stock/      stockList.jsp  stockInOut.jsp  recordList.jsp
│   ├── report/     report.jsp
│   ├── remind/     remindList.jsp
│   ├── user/       userList.jsp  userForm.jsp
│   ├── setting/    setting.jsp
│   ├── error/      403.jsp  404.jsp  500.jsp
│   ├── css/style.css   js/common.js
└── src/test/java/com/store          # 单元测试 / 集成测试
```

---

## 4. 功能与权限

| 功能 | 管理员 | 店长 | 收银员 |
| --- | :---: | :---: | :---: |
| 首页仪表盘（今日/本月营收、预警数） | ✅ | ✅ | ✅（仅本人流水口径） |
| 销售登记（自动扣库存） | ✅ | ✅ | ✅ |
| 进货 / 杂费登记 | ✅ | ✅ | ❌ |
| 单据审核（通过 / 驳回） | ✅ | ✅ | ❌ |
| 查看收支流水 | 全部 | 全部 | 仅本人 |
| 库存查询 / 库存流水 | ✅ | ✅ | ✅ |
| 出入库操作 | ✅ | ✅ | ❌ |
| 商品档案维护 | ✅ | ✅ | ❌（仅查询库存） |
| 报表中心 / CSV 导出 | ✅ | ✅ | ❌ |
| 提醒中心（低库存 + 待审核） | ✅ | ✅ | ❌ |
| 用户管理（新增 / 禁用 / 重置密码） | ✅ | ❌ | ❌ |
| 系统基础配置 | ✅ | ❌ | ❌ |
| 个人中心 / 修改密码 | ✅ | ✅ | ✅ |

权限落地方式：`LoginFilter` 做 URL + action 级校验，Service 层再次校验关键操作（防御式），页面按钮按角色渲染。

---

## 5. 数据库设计（store_db）

| 表 | 说明 | 关键字段 |
| --- | --- | --- |
| `t_user` | 用户 | username、password(MD5)、role、status |
| `t_goods` | 商品 | price、cost_price、stock_num、warn_num |
| `t_income_expense` | 收支单据 | order_no、type、category、money、goods_id、quantity、status、audit_* |
| `t_stock_record` | 库存流水 | goods_id、change_num、in_out_type、source_type |
| `t_config` | 系统配置 | store_name、default_warn_num |

演示数据包含 4 个账号、8 个商品（其中 3 个处于低库存）、10 张收支单据、8 条库存流水。

---

## 6. 核心业务规则

1. **销售即扣库存**：收银员提交销售单后，事务内完成「插入单据 → 行锁读取库存 → 扣减 → 写库存流水」，任一步失败整体回滚。
2. **审核**：收银员的销售单为 `pending`（待审核）；店长/管理员审核通过后置为 `approved`；驳回置为 `rejected` 并**自动把库存加回**，同时写一条来源为 `reject` 的入库流水。
3. **超卖拦截**：出库数量大于当前库存时抛出业务提示（如“库存不足：当前库存 4，本次需要出库 9999”），库存不变。
4. **库存修改唯一入口**：商品库存只能通过销售出库、进货入库、手工出入库、审核回滚变更；编辑商品档案不能直接改库存。
5. **报表口径**：营收类报表只统计 `approved` 单据；收银员看板上的金额仅统计本人经办的已通过单据。
6. **防重复提交**：新增商品、收支、出入库、用户、系统设置、个人资料等表单均带一次性 `_token`，提交后失效；同时采用 PRG（Post-Redirect-Get）避免刷新重复入库。
7. **登录安全**：密码 MD5 存储（初始密码 123456）；账号禁用后无法登录；Session 30 分钟超时；未登录访问自动跳转登录页。

---

## 7. 测试

### 自动化测试

```bash
mvn test
```

结果：`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`

- `MD5UtilTest`：MD5 结果、密码匹配、空值安全。
- `BusinessFlowTest`（连库）：销售扣减库存、超卖拦截、出库越界拦截、驳回回滚库存、待审单据审核通过；测试数据自动清理。

### 已完成的接口联调（HTTP 实测）

- 收银员登录 → 仪表盘 / 销售登记页 / 库存查询 正常；
- 销售 2 件：库存 48 → 46，生成待审核单据；
- 超卖 9999 件：提示库存不足，库存不变；
- 店长驳回：库存 4 → 6 回滚，单据状态 `rejected`；
- 店长进货 5 件：库存 12 → 17，单据直接 `approved` 并写入库流水；
- 管理员新增收银员、新增商品（期初库存 10）、入库 5 → 库存 15；
- 同一表单令牌二次提交被拦截（库存未重复增加）；
- 系统配置保存成功；日报表 CSV 导出带 BOM、中文列名与合计行；
- 越权测试：收银员访问 `/user`、`/report` 返回 403；店长访问 `/user` 返回 403。

---

## 8. 配置说明

`src/main/resources/db.properties`：

```properties
db.url=jdbc:mysql://127.0.0.1:3306/store_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
db.username=root
db.password=245478
```

- 数据库账号/端口不同：修改上述配置后重新打包部署。
- 修改端口：编辑 Tomcat `conf/server.xml` 中 `Connector port="8080"`；同时调整 `run.bat` 顶部 `HTTP_PORT`（仅提示用）。
- 修改店铺名称、默认预警阈值：登录管理员 → 系统设置。

---

## 9. 常见问题

| 现象 | 排查 |
| --- | --- |
| `mvn` 报 `JAVA_HOME is not defined correctly` | 已修正用户级 `JAVA_HOME`；命令行执行前也可 `set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1` |
| 登录页报数据库连接失败 | 确认 MySQL80 / MySQL96 服务运行；确认 `db.properties` 端口与密码；本机 3306 为 MySQL 9.6 |
| 启动 Tomcat 报 8080 端口占用 | 先执行 `stop.bat`，或修改 `conf/server.xml` 端口 |
| 页面中文乱码 | `web.xml` 已加 `<jsp-config>` 强制 JSP 以 UTF-8 读写；修改后必须重启 Tomcat（或重新部署）才会重新编译 JSP |
| 部署后访问 404 | 确认 WAR 名为 `StoreAccountSystem.war`，访问路径 `/StoreAccountSystem/` |
| 收银员看不到报表 / 用户管理 | 设计如此（权限隔离），管理员或店长可见 |

---

## 10. 后续可迭代

- 小票打印（已预留 `data-print` 样式钩子）与扫码枪录入；
- 单据详情页与导出 Excel（当前为 CSV，Excel 可直接打开）；
- 库存盘点单、供应商与会员管理；
- 预警规则细化（按分类设置阈值、临期提醒）；
- 报表图表化（ECharts 趋势图）。
