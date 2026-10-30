# chat-spring

[TOC]

## 一、项目定位

`chat-spring` 是 jabber-im 的 Spring 后端实现。
`chat-junco` 使用 Junco 组件实现相同业务。
两个项目共用 `../sql/chat-mysql.sql` 表结构。
两个项目对前端提供相同的业务 HTTP 接口。

项目支持手机号登录、个人资料、私聊、群聊、历史消息、未读计数和实时推送。
前端为 `ui-admin` 和 `ui-h5`。
两个前端已移除图片链接发送入口。
后端保留原有图片消息协议，前端保留历史图片展示。

| 项目项 | 当前实现 |
| --- | --- |
| Java | 8 |
| 应用框架 | Spring Boot 2.2.5.RELEASE |
| HTTP | Spring MVC |
| 持久化 | Spring Data JPA / Hibernate、HikariCP、MySQL |
| 实时通信 | Spring WebSocket、SockJS |
| JSON | Fastjson，Long 输出为字符串 |
| 接口文档 | Swagger / Knife4j，入口 `/doc.html` |
| 启动类 | `cn.xpride.framework.ChatSpringApp` |
| 默认端口 | 18888 |

## 二、配置与数据库

应用配置位于 `src/main/resources/application.yaml`。
连接业务数据库前，数据库须具备以下四张表。

| 表 | 用途 |
| --- | --- |
| `im_subscriber` | 用户资料 |
| `im_channel` | 频道与成员配置 |
| `im_dialogue` | 用户会话、摘要和未读数 |
| `im_channel_msg` | 频道消息 |

初始化脚本为 `../sql/chat-mysql.sql`。
脚本包含 `DROP TABLE`，不能向已有业务库重复导入。
已读修复不需要修改表结构。

Spring 使用以下环境变量覆盖本地配置。
这些变量与 `chat-junco` 的 `CHAT_DB_*` 变量不同。

```powershell
# MySQL 连接地址；替换主机、端口和库名。
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3306/chat?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true'
# 数据库账号；替换为本机可用账号。
$env:SPRING_DATASOURCE_USERNAME = '<数据库账号>'
# 数据库密码；替换为对应账号密码。
$env:SPRING_DATASOURCE_PASSWORD = '<数据库密码>'
# HTTP 和 WebSocket 监听端口；默认 18888。
$env:SERVER_PORT = '18888'
```

两个后端不能同时占用同一个端口。
前端 Vite 默认将 `/im` 和 WebSocket 代理到 `http://127.0.0.1:18888`。
切换后端时，可让选用的后端监听 18888。
修改后端端口时，需要同步修改前端 `vite.config.ts` 中的代理目标。

## 三、构建与启动

1. 开发者选择 JDK 8。

```powershell
$env:JAVA_HOME = 'D:\dev-soft\corretto-1.8.0_472'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

2. 开发者进入项目目录。

```powershell
cd E:\jabber-im\chat-spring
```

3. 开发者配置数据库连接。

使用上一节的环境变量。

4. 开发者启动应用。

```powershell
mvn spring-boot:run
```

IDE 可直接运行 `ChatSpringApp#main`。
启动类当前没有向 Spring 转发命令行 `args`，配置覆盖请使用环境变量。
默认接口文档地址为 `http://127.0.0.1:18888/doc.html`。

5. 开发者构建可执行包。

```powershell
mvn package spring-boot:repackage
```

POM 没有将 `repackage` 绑定到 `package`。
生成可执行包时，需要显式执行 `spring-boot:repackage`。
包输出到 `target` 目录。
POM 默认跳过测试，测试命令见第六节。

## 四、HTTP 与实时通信

业务接口统一以 `/im/` 为前缀。
POST 参数使用表单或查询参数。
`sendMsgToChannel` 接收平铺字段，不接收 JSON 请求体。
分页参数 `pageNow` 默认 1，每页 10 条。

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

登录令牌字段名为 `token`。
读取顺序为 header、请求参数、cookie。
业务响应通过 `code` 判断成功与失败，不能只检查 HTTP 200。
业务错误、参数错误、令牌过期分别使用 code 10、400、666。
Long ID 和时间戳输出为字符串。

原生 WebSocket 路径为 `/im/webSocket`。
SockJS 入口为 `/im/webSocketJs`。
连接通过 `token` 鉴权。
客户端发送 `ping`，服务端回复 `pong`。
发送聊天消息使用 HTTP，WebSocket 用于服务端推送。
WebSocket 收到非心跳客户端消息时会关闭连接。

## 五、已读状态

`ChatHandler#markRead` 接收当前用户已经展示的最新消息 ID。
前端在聊天窗口可见且滚动到最新消息时调用该接口。
用户无需发送消息即可清除未读红点。

`ChatBizService#markRead` 校验频道成员资格和消息所属频道。
提交的消息仍是频道最新消息时，才清零当前用户的未读数。
旧消息确认不会清除后续新消息的未读数。
重复确认不会重复改变业务状态。
确认不修改会话摘要、会话时间或其他成员的未读数。

`ChatBizService#sendMsgToChannel` 和 `ChatBizService#markRead` 共用频道行锁。
两条路径均在事务中执行。
消息落库、未读计数和频道计数一起提交或回滚。
同毫秒消息使用 ID 长度和字符串排序确定先后。

`chat-spring` 和 `chat-junco` 已同步这套逻辑。
使用新前端前，需要重启升级后的后端。
同库运行的发送端不能混用旧版本，否则不能保证并发已读行为。

## 六、测试与边界

开发者运行已读回归测试。

```powershell
mvn test '-Dmaven.test.skip=false' '-Dtest=ReadReceiptTest'
```

`ReadReceiptTest` 使用 Spring JPA 上下文和 H2 数据库。
测试不连接业务 MySQL。
测试覆盖未读清零、重复确认、旧消息确认、新消息确认和非成员拒绝。
测试类不使用外层事务，业务方法通过真实事务代理执行。

Junco HTTP、WebSocket、并发和回滚测试见 `../chat-junco/README.md`。
两个后端的测试通过不代表真实 MySQL 联调已经完成。
最新消息查询沿用按频道查询，原 SQL 没有频道索引。
大数据量场景需要单独检查实际 MySQL 查询计划。

项目保留原有固定验证码 `123456`。
历史消息接口仍沿用原有成员校验行为。

## 七、代码目录

| 目录 | 职责 |
| --- | --- |
| `application/handler` | HTTP 参数和响应 |
| `application/biz` | 聊天业务与事务 |
| `application/repository` | JPA 数据访问 |
| `application/entity` | 数据库实体 |
| `application/model`、`application/dto` | 业务模型与接口响应 |
| `application/ws` | WebSocket 和 SockJS |
| `base` | 鉴权、异常处理和基础配置 |
| `utility` | ID、JSON、图片等工具 |

以上目录位于 `src/main/java/cn/xpride/framework` 下。
