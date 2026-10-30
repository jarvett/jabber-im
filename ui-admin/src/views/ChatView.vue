<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from "vue";
import { useRoute } from "vue-router";
import { useReadReceipt, compareMessages } from "../readReceipt";
import { api, perform } from "../api";
import { useSession } from "../store";
import { emojiMap } from "../emoji";
import type { Dialogue, Message, Profile } from "../types";
import {
  ChatDialogue,
  ChatWindow,
  type ChatDialogueItem,
  type ChatSend,
} from "../components/chat";
const route = useRoute(),
  session = useSession();
const channel = ref(
  typeof route.query.channelId === "string" ? route.query.channelId : "",
);
const showDialogue = ref(true),
  showWindow = ref(Boolean(channel.value));
let readRevision = 0;
const dialogues = ref<Dialogue[]>([]),
  page = ref(1),
  ended = ref(false),
  busy = ref(false);
const title = ref("聊天"),
  messages = ref<Message[]>([]),
  historyPage = ref(1),
  historyEnded = ref(false),
  loading = ref(false),
  sending = ref(false),
  draft = ref("");
const emoji = Object.fromEntries(
  Object.entries(emojiMap).map(([name, file]) => [name, "/emoji/" + file]),
);
// 页面承接应用依赖；两个浮动组件只收数据和发事件。
async function load(reset = false) {
  if (busy.value) return;
  busy.value = true;
  try {
    await perform(async () => {
      const revision = readRevision;
      const next = reset ? 1 : page.value;
      const { dialogueList } = await api<{ dialogueList: Dialogue[] }>(
        "getMyDialogueList",
        { pageNow: next },
      );
      if (revision !== readRevision && session.readDialogue) {
        const index = dialogueList.findIndex(
          (item) => item.id === session.readDialogue?.id,
        );
        if (index >= 0) dialogueList[index] = session.readDialogue;
      }
      dialogues.value = [
        ...new Map(
          [...(reset ? [] : dialogues.value), ...dialogueList].map((item) => [
            item.id,
            item,
          ]),
        ).values(),
      ];
      page.value = next + 1;
      ended.value = dialogueList.length < 10;
    });
  } finally {
    busy.value = false;
  }
}
let generation = 0;
onBeforeUnmount(() => {
  generation++;
});
function merge(items: Message[]) {
  messages.value = [
    ...new Map(
      [...messages.value, ...items].map((item) => [item.id, item]),
    ).values(),
  ].sort(compareMessages);
}
async function history(reset = false) {
  if (!channel.value || loading.value) return;
  const current = generation,
    id = channel.value;
  loading.value = true;
  try {
    await perform(async () => {
      const next = reset ? 1 : historyPage.value;
      const { channelMsgList } = await api<{ channelMsgList: Message[] }>(
        "getMsgListByChannelId",
        { channelId: id, pageNow: next },
      );
      if (current !== generation) return;
      merge(channelMsgList);
      if (!reset || historyPage.value === 1) {
        historyPage.value = next + 1;
        historyEnded.value = channelMsgList.length < 10;
      }
    });
  } finally {
    if (current === generation) loading.value = false;
  }
}
watch(
  channel,
  async (id) => {
    const current = ++generation;
    messages.value = [];
    draft.value = "";
    title.value = "聊天";
    historyPage.value = 1;
    historyEnded.value = false;
    loading.value = false;
    sending.value = false;
    if (!id) return;
    await perform(async () => {
      const [detail, people] = await Promise.all([
        api<{ model: Dialogue }>("getDialogueByChannelId", { channelId: id }),
        api<{ subscriberList: Profile[] }>(
          "getSubscriberModelListByChannelId",
          { channelId: id },
        ),
      ]);
      if (current !== generation) return;
      title.value = detail.model.name;
      session.remember(people.subscriberList);
      await history(true);
    });
  },
  { immediate: true },
);
function select(item: ChatDialogueItem) {
  channel.value = item.channelId;
  showWindow.value = true;
}
watch(
  () => route.query.channelId,
  (id) => {
    if (typeof id === "string") {
      channel.value = id;
      showWindow.value = true;
    }
  },
);
watch(
  () => session.connected,
  (connected) => {
    if (connected) void history(true);
  },
);
watch(
  () => session.latest,
  (message) => {
    if (!message) return;
    if (message.channelId === channel.value) merge([message]);
    void perform(async () => {
      const revision = readRevision;
      const { model } = await api<{ model: Dialogue }>(
        "getDialogueByChannelId",
        { channelId: message.channelId },
      );
      if (
        revision !== readRevision &&
        session.readDialogue?.channelId === message.channelId
      )
        return;
      dialogues.value = [
        model,
        ...dialogues.value.filter((item) => item.id !== model.id),
      ].sort((a, b) => Number(b.lastTime) - Number(a.lastTime));
    });
  },
);
async function send(content: ChatSend) {
  if (sending.value || !channel.value) return;
  const current = generation,
    id = channel.value;
  sending.value = true;
  try {
    await perform(async () => {
      await api("sendMsgToChannel", { channelId: id, ...content }, "POST");
      // 切换会话后，旧请求不得清空新窗口的草稿或覆盖新会话。
      if (current !== generation) return;
      draft.value = "";
      await history(true);
    });
  } finally {
    if (current === generation) sending.value = false;
  }
}
const confirmRead = useReadReceipt(
  () => channel.value,
  () => showWindow.value && document.visibilityState === "visible",
  (model) => {
    session.readDialogue = model;
  },
  () => messages.value[messages.value.length - 1]?.id || "",
);
void load(true);
watch(
  () => session.readDialogue,
  (model) => {
    if (!model) return;
    readRevision++;
    dialogues.value = dialogues.value.map((item) =>
      item.id === model.id ? model : item,
    );
  },
);
</script>
<template>
  <section class="chat-desktop">
    <!-- 窗口操作独立于页面导航，关闭窗口后仍可从底部重新打开。 -->
    <div class="window-launchers" role="group" aria-label="聊天窗口操作">
      <button :class="{ active: showDialogue }" @click="showDialogue = true">会话列表</button>
      <button :class="{ active: showWindow }" :disabled="!channel" @click="showWindow = true">打开聊天窗口</button>
    </div>
    <ChatDialogue
      v-model:visible="showDialogue"
      :dialogue-list="dialogues"
      :selected-channel-id="channel"
      :loading="busy"
      :has-more="!ended"
      @select="select"
      @refresh="load(true)"
      @load-more="load()"
    />
    <ChatWindow
      v-if="channel"
      v-model:visible="showWindow"
      v-model="draft"
      :title="title"
      :channel-id="channel"
      :messages="messages"
      :profiles="session.profiles"
      :current-user-id="session.sid"
      :loading="loading"
      :sending="sending"
      :history-ended="historyEnded"
      :emoji="emoji"
      @send="send"
      @read="confirmRead"
      @load-history="history()"
    />
  </section>
</template>
<style scoped>
.chat-desktop {
  min-height: 100%;
  background: #fff;
}
.window-launchers {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  display: flex;
  gap: 6px;
  padding: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 4px 18px #27344914;
  font-size: 13px;
}
.window-launchers button {
  border-color: transparent;
  color: #64748b;
}
.window-launchers button.active {
  color: #278342;
  background: #edf7ef;
}
.window-launchers button:not(:disabled):hover {
  background: #f1f5f9;
}
</style>
