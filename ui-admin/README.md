# ui-admin

[TOC]

## 一、项目定位

桌面端聊天前端。会话列表和聊天窗口是同一页面上可独立拖动、关闭和重开的浮动组件。
本次迁移保留手机号登录、会话查询、文本消息、表情、个人资料、随机用户、私聊和群聊流程。
聊天页支持通过 `channelId` 链接直接打开。
已移除新增的图片链接发送入口；仍能展示收到的图片消息。
旧管理端引用的 `/file/uploadOne` 不在两个 Java 后端中实现，因此不展示无法使用的文件上传入口。
保留原有浮窗设计：会话列表默认 250×600，聊天窗口默认 800×600。
只拖动标题栏，输入、文本选择、滚动和按钮交互不触发拖动。
点击窗口置顶；浏览器缩小时位置会收敛到可视区域。
选择会话直接打开同页聊天窗口，不切换路由。

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
| Element Plus 2 | 登录输入框和按钮，替代 Element UI       |

Element Plus 按组件导入；聊天界面使用原生 Vue 组件。
使用浏览器 fetch 和 URLSearchParams，替代 axios、qs 及全局 Vue 原型注入。
原生滚动区域和“加载更多”按钮替代 vue-scroller / iScroll。
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

访问 http://localhost:18890。
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
登录存储键沿用 `admin_im_token` 和 `admin_im_sid`。

`src/socket.ts` 在应用根组件维护唯一连接。
切换页面不会重建连接。
每 15 秒发送 ping，断线时尝试重连。
退出登录时关闭连接。
连接恢复后补查当前会话的最新消息。
聊天面板按消息 ID 去重。
发送成功后通过历史查询回补消息。
新用户资料未加载时显示文字占位。

## 五、目录与路由

| 文件                         | 职责                         |
| ---------------------------- | ---------------------------- |
| src/main.ts                  | Vue、Pinia、Router 初始化    |
| src/router.ts                | 路由定义与登录保护           |
| src/store.ts                 | 登录态、资料缓存、消息通知   |
| src/types.ts                 | HTTP 与 WebSocket 类型       |
| src/views/LoginView.vue      | 登录及验证码倒计时           |
| src/views/ChatView.vue       | 会话列表及分页               |
| src/components/chat/index.ts | 两个独立组件及类型的统一导出 |
| src/views/ProfileView.vue    | 个人资料、签名、换马甲、退出 |
| src/views/PeopleView.vue     | 随机用户、私聊和群聊         |
| tests/chat.spec.ts           | 浏览器回归测试               |

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
浮窗测试覆盖独立拖动、置顶、关闭重开、位置及草稿保留、会话切换隔离和纯 Vue 宿主接入。
管理端也使用窄屏视口检查窗口边界和输入焦点。
实际移动设备软键盘仍需设备联调。

参考：[Vite 文档](https://vite.dev/guide/)、[Tailwind Vite 集成](https://tailwindcss.com/docs/installation/using-vite)。

## 七、独立接入组件

统一入口为 `src/components/chat/index.ts`。
复制整个 `src/components/chat` 文件夹即可在另一个 Vue 3 项目导入。
宿主需要 Vue 3.5+ 和 VueUse 14。
组件自带局部 CSS，不依赖本项目全局样式、Element Plus、Tailwind、Router、Pinia、登录态或 `/im` 接口。
浮窗通过 Teleport 渲染到 body。
宿主不需要提供固定双栏容器。

```vue
<script setup lang="ts">
import { ref } from "vue";
import { ChatDialogue, ChatWindow } from "./components/chat";
import type {
  ChatDialogueItem,
  ChatMessage,
  ChatSend,
} from "./components/chat";

const showList = ref(true);
const showWindow = ref(false);
const selected = ref<ChatDialogueItem>();
const dialogues = ref<ChatDialogueItem[]>([]);
const messages = ref<ChatMessage[]>([]);
const draft = ref("");
const sending = ref(false);

function select(item: ChatDialogueItem) {
  selected.value = item;
  showWindow.value = true;
  // 宿主在这里加载该会话的历史消息和用户资料。
}
function send(content: ChatSend) {
  // 宿主在这里调用自己的发送接口。
  // 成功后才清空 draft；失败时保留草稿。
  console.log(selected.value?.channelId, content);
}
</script>

<template>
  <button @click="showList = true">打开会话列表</button>
  <button v-if="selected" @click="showWindow = true">打开聊天窗口</button>
  <ChatDialogue
    v-model:visible="showList"
    :dialogue-list="dialogues"
    :selected-channel-id="selected?.channelId"
    @select="select"
  />
  <ChatWindow
    v-if="selected"
    v-model:visible="showWindow"
    v-model="draft"
    :title="selected.name"
    :channel-id="selected.channelId"
    :messages="messages"
    current-user-id="当前登录用户ID"
    :sending="sending"
    history-ended
    @send="send"
  />
</template>
```

上面是 UI 接线示例，接口调用由宿主实现。
本项目完整业务接线见 `ChatView.vue`。
`tests/fixtures/Embed.vue` 是可运行的纯 Vue 宿主，发送事件只更新内存消息。
浏览器测试验证该宿主不安装 Router/Pinia、不加载全局 CSS、不请求 `/im` 也能选择、拖动和发送。

| 组件         | 输入                                                                                                        | 事件 / 双向绑定                                                                 |
| ------------ | ----------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| ChatDialogue | dialogueList、selectedChannelId、loading、hasMore、initialPosition                                          | v-model:visible、select(item)、refresh、loadMore                                |
| ChatWindow   | title、channelId、messages、currentUserId、profiles、loading、sending、historyEnded、emoji、initialPosition | v-model:visible、v-model（草稿）、send(content)、loadHistory、read(messageId) |

`initialPosition` 使用 `{ x, y }` 像素坐标。
`profiles` 的键为用户 ID，值为 `{ nickName, avatarUrl }`。
`emoji` 的键为消息中的表情代码，值为图片 URL。
不传 `emoji` 时不展示表情按钮。
组件不假定宿主存在 `/emoji` 目录。
两个组件都提供 `tools` 插槽。

宿主负责消息去重、历史分页、发送状态、失败处理和推送订阅。
关闭窗口只更新 `visible`，保留位置和草稿。
如果宿主用 `v-if` 卸载组件，组件内部位置会重新初始化。
所有 ID 使用字符串。


## 八、已读确认

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

`ChatWindow` 通过 `read(messageId)` 事件通知宿主，组件本身不请求已读接口。
本项目页面通过 `useReadReceipt` 接线，其他宿主应在收到该事件后调用自己的已读接口。
