<script setup lang="ts">
import UserAvatar from "../components/UserAvatar.vue";
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, perform } from "../api";
import { useSession } from "../store";
import { chatTime } from "../time";
import { usePullRefresh } from "../pull";
import type { Dialogue } from "../types";
import ChatPane from "../components/ChatPane.vue";
const route = useRoute(),
  router = useRouter(),
  session = useSession();
const channel = computed(() =>
  typeof route.query.channelId === "string" ? route.query.channelId : "",
);
let readRevision = 0;
const dialogues = ref<Dialogue[]>([]),
  page = ref(1),
  ended = ref(false),
  busy = ref(false);
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
      const items = reset ? [] : dialogues.value;
      dialogues.value = [
        ...new Map(
          [...items, ...dialogueList].map((item) => [item.id, item]),
        ).values(),
      ];
      page.value = next + 1;
      ended.value = dialogueList.length < 10;
    });
  } finally {
    busy.value = false;
  }
}
watch(
  () => session.latest,
  (message) => {
    if (message)
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
void load(true);

const scroll = ref<HTMLElement>();
const { distance, touchStart, touchMove, touchEnd } = usePullRefresh(
  scroll,
  () => {
    void load(true);
  },
);
function more() {
  const el = scroll.value;
  if (
    el &&
    el.scrollHeight - el.scrollTop - el.clientHeight < 40 &&
    !ended.value
  )
    void load();
}
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
  <ChatPane v-if="channel" :key="channel" :channel="channel" />
  <div
    v-else
    class="chat"
    ref="scroll"
    @scroll="more"
    @touchstart.passive="touchStart"
    @touchmove.passive="touchMove"
    @touchend="touchEnd"
  >
    <div v-if="distance > 20" class="scroll-status">
      {{ distance >= 60 ? "松开刷新" : "下拉刷新" }}
    </div>
    <div class="chat-wrap">
      <div
        v-for="item in dialogues"
        :key="item.id"
        class="chat-item"
        role="button"
        tabindex="0"
        @click="
          router.push({
            path: '/chatbox',
            query: { channelId: item.channelId },
          })
        "
        @keydown.enter="
          router.push({
            path: '/chatbox',
            query: { channelId: item.channelId },
          })
        "
      >
        <div class="chat-item-left">
          <UserAvatar class="chat-item-avatar" :src="item.avatar" />
          <div class="chat-item-avatar-right">
            <div class="chat-item-name">{{ item.name }}</div>
            <div class="chat-item-brief">{{ item.brief }}</div>
          </div>
        </div>
        <div class="chat-item-right">
          <div class="chat-item-time">{{ chatTime(item.lastTime) }}</div>
          <div class="chat-item-icon">
            <div v-if="item.unreadCount" class="chat-item-icon-num">
              {{ item.unreadCount }}
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="busy" class="scroll-status">加载中…</div>
    <button
      v-else-if="!ended"
      class="icon-button scroll-status"
      @click="load()"
    >
      加载更多
    </button>
  </div>
</template>
<style>
.chat {
  width: 100%;
  height: 100%;
}
.chat ._v-container {
  background-color: #e7e7e7;
}
</style>
<style scoped>
.chat-item {
  display: flex;
  padding: 10px;
  background-color: #ffffff;
  margin-bottom: 3px;
  max-height: 60px;
  overflow: hidden;
  align-items: center;
}

.chat-item-left {
  width: 0;
  flex: 1;
  display: flex;
  height: 50px;
}

.chat-item-avatar {
  margin-right: 10px;
}

.chat-item-avatar-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  max-height: 60px;
  overflow: hidden;
}

.chat-item-name {
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-brief {
  font-size: 70%;
  color: #999999;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-right {
  width: 60px;
  height: 50px;
  min-width: 60px;
  max-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

.chat-item-time {
  font-size: 80%;
}

.chat-item-icon {
  font-size: 50%;
}

.chat-item-icon-num {
  color: #ffffff;
  background-color: red;
  padding: 2px 4px;
  border-radius: 50%;
}
</style>
<style scoped>
.chat {
  background: #e7e7e7;
  overflow-y: auto;
  overscroll-behavior: contain;
}
</style>
