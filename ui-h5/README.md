# ui-h5

[TOC]

## 一、项目定位

移动端 H5 聊天前端。页面采用单列会话、独立聊天页和自定义数字键盘。
本次迁移保留手机号登录、会话查询、文本消息、表情、个人资料、随机用户、私聊和群聊流程。
聊天页支持通过 `channelId` 链接直接打开。
已移除新增的图片链接发送入口；仍能展示收到的图片消息。
技术栈迁移以原版 H5（Git 提交 `3dad3c4`）的页面结构、配色和交互为基线。
恢复底部“聊天 / 我的”导航、会话列表、资料菜单、随机用户列表、群聊勾选、数字键盘和居中弹窗。
聊天页保留灰色背景、左右气泡、时间标签、单行输入框、返回/表情/更多图标和 150px 表情面板。
不添加顶部品牌栏、桌面双栏布局或额外可见发送按钮。
消息通过输入法发送键或 Enter 提交。

## 二、技术选型

| 技术           | 用途                                    |
| -------------- | --------------------------------------- |
| Vite 7         | 开发服务、生产构建、HTTP/WebSocket 代理 |
| Vue 3          | Composition API 与 script setup         |
| TypeScript 5   | 严格类型检查、接口模型                  |
| Vue Router 4   | history 路由、登录保护                  |
| Pinia 3        | 登录态、用户资料、消息通知              |
| VueUse 14      | 本地存储响应式绑定、可释放的定时器      |
| Tailwind CSS 4 | 布局和通用样式                          |

H5 不引入桌面组件库，保持表单和聊天面板轻量。
使用浏览器 fetch 和 URLSearchParams，替代 axios、qs 及全局 Vue 原型注入。
原生滚动区域承接 vue-scroller 的下拉刷新、列表触底分页和历史消息下拉加载。
Tailwind 只加载主题与工具层，不启用会改变默认控件外观的 preflight。
页面组件沿用原版样式及本地 `iconfont.js` 图标。
表情图片沿用项目静态资源。

## 三、安装和运行

环境：Node.js 22.12+，pnpm 11.9.0。
在当前项目目录执行命令。

1. 开发者安装依赖。

```sh
pnpm install --frozen-lockfile
```

2. 开发者启动开发服务。

```sh
pnpm dev
```

访问 http://localhost:18889。
端口被占用时直接报错，不自动换端口。

3. 开发者构建生产产物。

```sh
pnpm build
```

产物位于 `dist/`。
构建包含 `vue-tsc --noEmit` 类型检查。
生产部署需要将未知前端路径回退到 `index.html`。
生产反向代理需要将 `/im` 和 `/im/webSocket` 转发到后端。
`pnpm preview` 仅预览静态产物，不提供开发代理。

## 四、接口与状态

开发代理在 `vite.config.ts` 中配置。
HTTP 和 WebSocket 均转发到 `127.0.0.1:18888`。
后端可以选择 chat-spring 或 chat-junco。

`src/api.ts` 负责业务 code 判断、令牌注入、错误提示和表单编码。
`sidList` 按重复参数提交。
ID 和后端 Long 字段保留为字符串。
code 666 会清除登录态并跳转登录页。
登录存储键沿用 `h5_im_token` 和 `h5_im_sid`。

`src/socket.ts` 在应用根组件维护唯一连接。
切换页面不会重建连接。
每 15 秒发送 ping，断线时尝试重连。
退出登录时关闭连接。
连接恢复后补查当前会话的最新消息。
聊天面板按消息 ID 去重。
发送成功后通过历史查询回补消息。
新用户资料未加载时显示文字占位。

登录数字键盘沿用原有四列布局和双行确认键。
支持数字、删除、清空、返回和确认。
手机号最多 11 位，验证码最多 6 位。
不弹出原生软键盘。
请求失败时保留输入内容。

## 五、目录与路由

| 文件                        | 职责                         |
| --------------------------- | ---------------------------- |
| src/main.ts                 | Vue、Pinia、Router 初始化    |
| src/router.ts               | 路由定义与登录保护           |
| src/store.ts                | 登录态、资料缓存、消息通知   |
| src/types.ts                | HTTP 与 WebSocket 类型       |
| src/views/LoginView.vue     | 手机号、验证码两步登录       |
| src/views/ChatView.vue      | 会话列表及分页               |
| src/components/ChatPane.vue | 历史记录、推送、发送、表情   |
| src/views/ProfileView.vue   | 个人资料、签名、换马甲、退出 |
| src/views/PeopleView.vue    | 随机用户、私聊和群聊         |
| tests/chat.spec.ts          | 浏览器回归测试               |

保留 `/index`、`/login`、`/chat`、`/chatbox?channelId=...`、`/profile`、`/stranger`、`/group`。
`/index` 跳转 `/chat`。

## 六、验证方法与边界

```sh
pnpm typecheck
pnpm build
pnpm test
```

Playwright 优先使用本机 Windows Edge。
其他环境先执行 `pnpm exec playwright install chromium`。
测试使用模拟 HTTP 和 WebSocket，验证前端协议和浏览器行为。
测试不代表真实 MySQL、后端推送或公网图片链路已经联调。

覆盖登录、字符串 ID、直接进入聊天、中文消息、群聊重复参数、令牌过期和发送失败。
另外覆盖实时推送去重、频道隔离、资料修改和退出登录。
布局回归覆盖底部导航、无顶部标题栏、原版图标、群聊弹窗、资料确认/取消、表情面板、下拉刷新和返回。
测试产物包含各页面截图，位于未跟踪的 `test-results/`。
H5 使用窄屏视口检查横向溢出和输入焦点。
实际移动设备软键盘仍需设备联调。

参考：[Vite 文档](https://vite.dev/guide/)、[Tailwind Vite 集成](https://tailwindcss.com/docs/installation/using-vite)。


## 七、已读确认

消息列表已渲染到最新消息时，前端调用 `POST /im/markRead`。
表单参数为 `channelId`、`messageId`，令牌沿用 `token` header。
成功返回原有 `DialogueModelDto`，前端采用服务端的 `model.unreadCount` 更新角标。
失败时不在本地强行清零。
页面隐藏、窗口关闭或正在浏览较早历史记录时，不自动确认新消息。

服务端校验当前用户的频道成员资格、会话归属和消息归属。
仅当提交的消息仍是频道最新消息时清零；旧确认保留未读数。
清零只更新当前用户的 `unread_count` 和 `last_read_time`，不改变会话排序时间与摘要。
发送与已读确认共用频道行锁并在事务中提交。
两个 Java 后端都需要使用包含此接口的新版本。
无数据库字段或表结构变更。

后端测试使用 H2；浏览器测试使用模拟接口。
实际 MySQL 事务隔离和部署后的前后端联调仍需验证。
