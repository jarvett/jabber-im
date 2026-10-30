# chat-junco

[TOC]

## 一、实现范围

这是 chat-spring 的独立 Junco 实现。
运行环境为 Java 17。
Junco 版本为本地 junco-framework 的 1.1.2。

| 能力 | 使用组件 |
| --- | --- |
| 启动、配置、构造器注入、资源关闭 | junco-core |
| 15 个业务 HTTP 接口、异常处理、参数适配 | junco-web |
| 数据源、连接池、SQL 执行 | junco-jdbc |
| 原生 WebSocket、SockJS WebSocket | junco-websocket |
| 容器启动、随机端口集成测试 | junco-test |

项目不依赖 Spring Boot、Spring MVC 或 Spring Data JPA。
ChatBizService 保留原有业务流程。
模型、令牌算法、ID 算法和头像合成沿用 chat-spring。
数据库结构使用仓库的 `../sql/chat-mysql.sql`。
应用启动不建表，也不修改表结构。

## 二、HTTP 契约

所有业务路径都以 `/im/` 开头。
POST 参数使用查询参数、表单或 multipart 字段。
`sendMsgToChannel` 使用平铺字段，不接收 JSON 请求体。
`sidList` 支持逗号分隔值及重复参数。
`pageNow` 默认值为 1。
每页最多返回 10 条。

| 方法 | 路径 | 参数 |
| --- | --- | --- |
| GET | getPhoneCode | phone |
| GET | getToken | phone、code |
| GET | getMyProfile | 无 |
| GET | getMyProfileByPhone | phone |
| POST | changeAvatarAndNickName | 无 |
| POST | changeIntro | intro |
| GET | getRandomStranger | 无 |
| POST | createPrivateDialogue | sid |
| POST | createGroupDialogue | sidList、groupName |
| GET | getMyDialogueList | pageNow |
| GET | getDialogueByChannelId | channelId |
| GET | getSubscriberModelListByChannelId | channelId |
| POST | sendMsgToChannel | channelId、type、text、imageUrl、eventKey |
| GET | getMsgListByChannelId | channelId、pageNow |
| POST | markRead | channelId、messageId |

令牌按 header、请求参数、cookie 的顺序读取。
字段名均为 `token`。
HTTP 响应中的 Long 保持字符串类型。
普通 DTO 的 `msg: null` 保持原状。
业务失败仍返回 HTTP 200。
业务错误、参数错误、令牌过期分别沿用 code 10、400、666。
错误方法沿用 code 405。
未知路径返回 HTTP 404。

## 三、实时推送

原生 WebSocket 地址为 `/im/webSocket?token=...`。
`ping` 返回 `pong`。
其他客户端消息关闭连接。
消息仍通过 HTTP 发送。
服务端向发送者和接收者的连接推送原有 JSON。
握手阶段拒绝无效 token。

SockJS 地址前缀为 `/im/webSocketJs`。
已接入 info、原始 WebSocket、SockJS WebSocket、xhr、xhr_streaming 和 xhr_send。
HTTP 回退使用 Junco 异步响应。
同一用户的多个连接均可接收推送。
旧式 JSONP、iframe、htmlfile 和 EventSource 回退未实现。
现有 ui-h5 使用原生 WebSocket。
Swagger/Knife4j 页面不在本项目中迁移。

## 四、构建与运行

1. 开发者选择 JDK 17。

```powershell
$env:JAVA_HOME = 'D:\dev-soft\corretto-17.0.15'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

2. 开发者安装本地组件。

```powershell
mvn -f E:\junco-framework\pom.xml -pl junco-web,junco-websocket,junco-jdbc,junco-test,junco-bom -am -DskipTests install
```

3. 开发者构建 chat-junco。

```powershell
cd E:\jabber-im\chat-junco
mvn clean package
```

4. 开发者配置数据库连接。

```powershell
$env:CHAT_DB_URL = 'jdbc:mysql://127.0.0.1:3306/chat?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true'
$env:CHAT_DB_USER = 'root'
$env:CHAT_DB_PASSWORD = '<数据库密码>'
$env:CHAT_PORT = '18888'
```

数据库须事先具备 `chat-mysql.sql` 中的四张表。
该 SQL 含 DROP TABLE，不能对已有业务库重复导入。
两个服务不能同时占用 18888 端口。

5. 开发者启动应用。

```powershell
java -jar target/chat-junco-1.0.0.jar
```

本机 Corretto 17 的 Unix Domain Socket 存在 `Invalid argument: connect`。
本次验证用下面的进程参数触发 JDK 的 TCP 回退。
`no-unix-socket` 必须保持为不存在的目录。
该参数不修改系统配置。

```powershell
mvn test '-DargLine=-Djdk.net.unixdomain.tmpdir=E:/jabber-im/chat-junco/target/no-unix-socket'
java -Djdk.net.unixdomain.tmpdir=E:/jabber-im/chat-junco/target/no-unix-socket -jar target/chat-junco-1.0.0.jar
```

## 五、验证与边界

`ChatIntegrationTest` 使用真实 Junco 容器与随机 HTTP 端口。
数据库使用 H2 MySQL 模式。
测试从仓库总 SQL 提取表结构。
测试只去除 H2 不支持的 MySQL 排序规则和存储声明。
测试不会连接业务 MySQL。

覆盖 15 个 HTTP 接口、重复登录、令牌来源、私聊、群聊、文本和图片消息。
覆盖发送方已读、接收方未读、消息落库、频道计数、分页和非法发送拒绝。
覆盖真实 WebSocket 握手、心跳、推送及 SockJS 帧。
覆盖 SockJS HTTP 轮询、流式回退和会话令牌隔离。
真实 MySQL 和前端浏览器联调尚未验证。

兼容迁移保留了原实现的固定验证码 123456。
历史消息查询也保留原有成员校验行为。
本次没有调整这些业务规则。


## 六、已读状态修复

新增 `POST /im/markRead`，接收表单 `channelId` 和 `messageId`。
返回当前用户的 `DialogueModelDto`。
用户只能确认自己所属频道的消息。
提交的 ID 不是最新消息时不会清零，避免旧确认吞掉并发新消息。
发送消息与已读确认均在事务中锁定频道主键行。
重复确认幂等；确认不改动 `last_time`、摘要或其他成员的未读数。
同毫秒消息按 ID 长度及字符串排序，避免 Long 精度损失。

表结构保持不变。
频道锁和消息 ID 校验使用主键。
最新消息读取沿用现有按频道查询，原表未提供频道索引；大数据量场景仍需对实际 MySQL 的查询计划单独评估。
Spring 和 Junco 必须同时升级发送与已读逻辑，不能混用旧发送端来验证并发保证。
H2 测试已覆盖幂等、旧消息确认、成员隔离、并发发送和事务回滚。
真实 MySQL 尚未验证。
