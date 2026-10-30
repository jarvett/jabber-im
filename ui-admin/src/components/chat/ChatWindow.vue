<script setup lang="ts">
import { ref, watch, nextTick } from "vue";
import { useDocumentVisibility } from "@vueuse/core";
import FloatingWindow from "./FloatingWindow.vue";
import ChatAvatar from "./ChatAvatar.vue";
import type {
  ChatMessage,
  ChatProfile,
  ChatSend,
  WindowPosition,
} from "./types";
const props = withDefaults(
  defineProps<{
    title: string;
    channelId: string;
    messages: ChatMessage[];
    profiles?: Record<string, ChatProfile>;
    currentUserId: string;
    loading?: boolean;
    sending?: boolean;
    historyEnded?: boolean;
    emoji?: Record<string, string>;
    initialPosition?: WindowPosition;
  }>(),
  { profiles: () => ({}), emoji: () => ({}) },
);
const visible = defineModel<boolean>("visible", { default: true });
const draft = defineModel<string>({ default: "" });
const emit = defineEmits<{
  send: [content: ChatSend];
  loadHistory: [];
  read: [messageId: string];
}>();
const list = ref<HTMLElement>(),
  input = ref<HTMLTextAreaElement>(),
  showEmoji = ref(false);
// 历史向前补页时维持阅读位置；新消息到达时仅在原来位于底部的情况下跟随。
watch(
  () => props.messages.map((message) => message.id),
  async (messages, previous) => {
    const el = list.value,
      oldHeight = el?.scrollHeight || 0,
      oldTop = el?.scrollTop || 0;
    const atBottom = !el || oldHeight - oldTop - el.clientHeight < 80;
    const prepended =
      previous?.length &&
      messages[0] !== previous[0] &&
      messages.includes(previous[0]);
    await nextTick();
    if (!list.value) return;
    if (prepended)
      list.value.scrollTop = oldTop + list.value.scrollHeight - oldHeight;
    else if (atBottom) list.value.scrollTop = list.value.scrollHeight;
  },
);
watch(
  () => props.channelId,
  () => {
    showEmoji.value = false;
  },
);
watch(draft, (value) => {
  if (!value) {
    showEmoji.value = false;
  }
});
function send() {
  if (props.sending || !draft.value.trim()) return;
  // 点击发送后回到输入框；发送期间只读以保留焦点，完成时不抢走用户的新焦点。
  input.value?.focus({ preventScroll: true });
  emit("send", { type: "text", text: draft.value });
}
function enter(event: KeyboardEvent) {
  if (!event.isComposing && !event.shiftKey) {
    event.preventDefault();
    send();
  }
}
function parts(text = "") {
  return text.split(/(\[[^\[\]]+\])/g).filter(Boolean);
}
const documentVisibility = useDocumentVisibility();
watch(visible, async (value) => {
  if (!value) return;
  await nextTick();
  if (list.value) list.value.scrollTop = list.value.scrollHeight;
  void reportRead();
});
async function reportRead() {
  await nextTick();
  if (
    !visible.value ||
    documentVisibility.value !== "visible" ||
    props.loading ||
    !list.value
  )
    return;
  if (
    list.value.scrollHeight - list.value.scrollTop - list.value.clientHeight >
    20
  )
    return;
  const latest = props.messages[props.messages.length - 1];
  if (latest) emit("read", latest.id);
}
watch(
  [() => props.messages, () => props.loading, visible, documentVisibility],
  reportRead,
  { flush: "post" },
);
</script>
<template>
  <FloatingWindow
    v-model:visible="visible"
    :title="title"
    :width="800"
    :offset="300"
    :radius="32"
    :initial-position="initialPosition"
    ><div ref="list" class="chat-messages" @scroll="reportRead">
      <button
        v-if="!historyEnded"
        class="history"
        :disabled="loading"
        @click="emit('loadHistory')"
      >
        加载历史消息
      </button>
      <p v-if="!loading && !messages.length" class="empty">暂无消息</p>
      <article
        v-for="message in messages"
        :key="message.id"
        class="message"
        :class="{ mine: message.senderId === currentUserId }"
      >
        <time>{{ new Date(Number(message.time)).toLocaleString() }}</time>
        <div class="message-row">
          <ChatAvatar
            :src="profiles[message.senderId]?.avatarUrl"
            :name="profiles[message.senderId]?.nickName"
          />
          <div class="bubble">
            <p class="sender">
              {{ profiles[message.senderId]?.nickName || "聊天用户" }}
            </p>
            <template v-if="message.content.type === 'text'"
              ><template
                v-for="(part, index) in parts(message.content.text)"
                :key="index"
                ><img
                  v-if="emoji[part]"
                  class="inline-emoji"
                  :src="emoji[part]"
                  :alt="part"
                /><span v-else>{{ part }}</span></template
              ></template
            ><img
              v-else-if="message.content.type === 'image'"
              class="message-image"
              :src="message.content.imageUrl"
              alt="图片消息"
            /><span v-else>系统消息</span>
          </div>
        </div>
      </article>
    </div>
    <form class="chat-composer" @submit.prevent="send()">
      <div class="tools">
        <button
          v-if="Object.keys(emoji).length"
          type="button"
          @click="showEmoji = !showEmoji"
        >
          表情</button
        ><slot name="tools" />
      </div>
      <div v-if="showEmoji" class="emoji-picker">
        <button
          v-for="(url, name) in emoji"
          :key="name"
          type="button"
          :aria-label="name"
          @click="draft += name"
        >
          <img :src="url" :alt="name" />
        </button>
      </div>
      <div class="compose-row">
        <textarea
          ref="input"
          v-model="draft"
          aria-label="消息内容"
          rows="3"
          placeholder="输入消息，Enter 发送"
          :readonly="sending"
          @keydown.enter="enter"
        ></textarea
        ><button class="send" :disabled="sending || !draft.trim()">发送</button>
      </div>
    </form></FloatingWindow
  >
</template>
<style scoped>
.chat-messages {
  flex: 1;
  min-height: 0;
  overflow: auto;
  overscroll-behavior: contain;
  padding: 16px;
  background: #f5f5f5;
  overflow-anchor: none;
}
.history {
  display: block;
  margin: 0 auto 16px;
  border: 0;
  background: transparent;
  color: #999;
  cursor: pointer;
}
.empty {
  text-align: center;
  color: #999;
}
.message {
  margin-bottom: 18px;
}
.message time {
  display: block;
  text-align: center;
  color: #999;
  font-size: 11px;
  margin: 8px;
}
.message-row {
  display: flex;
  gap: 10px;
  align-items: start;
}
.mine .message-row {
  flex-direction: row-reverse;
}
.bubble {
  max-width: 80%;
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
.mine .bubble {
  background: #9eea6a;
}
.sender {
  font-size: 11px;
  color: #777;
  margin: 0 0 5px;
}
.inline-emoji {
  display: inline-block;
  width: 22px;
  height: 22px;
  vertical-align: middle;
}
.message-image {
  max-width: 240px;
  max-height: 260px;
  object-fit: contain;
  width: 100%;
}
.chat-composer {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fff;
  flex-shrink: 0;
}
.tools {
  display: flex;
  gap: 8px;
}
.chat-composer button {
  font: inherit;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  color: #777;
  padding: 6px 10px;
  cursor: pointer;
}
.chat-composer button:disabled {
  opacity: 0.5;
  cursor: default;
}
.compose-row,
.image-input {
  display: flex;
  gap: 8px;
  align-items: end;
}
.chat-composer textarea,
.chat-composer input {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  resize: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px;
  font: inherit;
  background: #fff;
  color: #333;
}
.chat-composer .send {
  background: #45ad25;
  color: #fff;
  border-color: #45ad25;
}
.emoji-picker {
  display: flex;
  flex-wrap: wrap;
  max-height: 120px;
  overflow: auto;
}
.emoji-picker button {
  border: 0;
  padding: 4px;
}
.emoji-picker img {
  width: 28px;
  height: 28px;
}
</style>
