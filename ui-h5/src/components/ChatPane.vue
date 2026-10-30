<script setup lang="ts">
import UserAvatar from "./UserAvatar.vue";
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from "vue";
import { useSession } from "../store";
import { useDocumentVisibility } from "@vueuse/core";
import { useReadReceipt, compareMessages } from "../readReceipt";
import { api, perform } from "../api";
import UiIcon from "./UiIcon.vue";
import { chatTime } from "../time";
import { usePullRefresh } from "../pull";
import { useRouter } from "vue-router";
import { emojiMap } from "../emoji";
import type { Dialogue, Message, Profile } from "../types";
const props = defineProps<{ channel: string }>();
const session = useSession(),
  title = ref("聊天"),
  messages = ref<Message[]>([]),
  input = ref("");
const page = ref(1),
  ended = ref(false),
  loading = ref(false),
  sending = ref(false),
  showEmoji = ref(false),
  showImage = ref(false),
  list = ref<HTMLElement>();
let disposed = false;
onBeforeUnmount(() => {
  disposed = true;
});
function merge(items: Message[]) {
  messages.value = [
    ...new Map(
      [...messages.value, ...items].map((item) => [item.id, item]),
    ).values(),
  ].sort(compareMessages);
}
async function bottom() {
  await nextTick();
  list.value?.scrollTo({ top: list.value.scrollHeight });
}
async function history(reset = false) {
  if (loading.value) return;
  loading.value = true;
  const oldHeight = list.value?.scrollHeight || 0;
  try {
    await perform(async () => {
      const next = reset ? 1 : page.value;
      const data = await api<{ channelMsgList: Message[] }>(
        "getMsgListByChannelId",
        { channelId: props.channel, pageNow: next },
      );
      if (disposed) return;
      merge(data.channelMsgList);
      if (!reset || page.value === 1) {
        page.value = next + 1;
        ended.value = data.channelMsgList.length < 10;
      }
      if (reset) await bottom();
      else {
        await nextTick();
        if (list.value)
          list.value.scrollTop += list.value.scrollHeight - oldHeight;
      }
    });
  } finally {
    loading.value = false;
  }
}
watch(
  () => session.connected,
  (connected) => {
    if (connected) void history(true);
  },
);
onMounted(() =>
  perform(async () => {
    const [detail, people] = await Promise.all([
      api<{ model: Dialogue }>("getDialogueByChannelId", {
        channelId: props.channel,
      }),
      api<{ subscriberList: Profile[] }>("getSubscriberModelListByChannelId", {
        channelId: props.channel,
      }),
    ]);
    if (disposed) return;
    title.value = detail.model.name;
    session.remember(people.subscriberList);
    await history(true);
  }),
);
watch(
  () => session.latest,
  (message) => {
    if (message?.channelId === props.channel) {
      merge([message]);
      void bottom();
    }
  },
);
async function send() {
  if (sending.value || !input.value.trim()) return;
  sending.value = true;
  try {
    await perform(async () => {
      await api(
        "sendMsgToChannel",
        { channelId: props.channel, type: "text", text: input.value },
        "POST",
      );
      input.value = "";
      showImage.value = false;
      showEmoji.value = false;
      await history(true);
    });
  } finally {
    sending.value = false;
  }
}
function enter(event: KeyboardEvent) {
  if (!event.isComposing && !event.shiftKey) {
    event.preventDefault();
    void send();
  }
}
function parts(text = "") {
  return text.split(/(\[[^\[\]]+\])/g).filter(Boolean);
}

const router = useRouter();
function back() {
  if (router.options.history.state.back) router.back();
  else void router.replace("/chat");
}
const { distance, touchStart, touchMove, touchEnd } = usePullRefresh(
  list,
  () => {
    if (!ended.value) void history();
  },
);
watch(title, (value) => {
  document.title = value;
});
const documentVisibility = useDocumentVisibility();
const confirmRead = useReadReceipt(
  () => props.channel,
  () => !disposed && documentVisibility.value === "visible",
  (model) => {
    session.readDialogue = model;
  },
  () => messages.value[messages.value.length - 1]?.id || "",
);
async function reportRead() {
  await nextTick();
  if (
    disposed ||
    loading.value ||
    !list.value ||
    documentVisibility.value !== "visible"
  )
    return;
  if (
    list.value.scrollHeight - list.value.scrollTop - list.value.clientHeight >
    20
  )
    return;
  const latest = messages.value[messages.value.length - 1];
  if (latest) await confirmRead(latest.id);
}
watch([messages, loading, documentVisibility], reportRead, { flush: "post" });
</script>
<template>
  <div class="chatbox">
    <div
      ref="list"
      class="msgbox"
      @scroll="reportRead"
      :class="{ expanded: showEmoji || showImage }"
      @touchstart.passive="touchStart"
      @touchmove.passive="touchMove"
      @touchend="touchEnd"
    >
      <div v-if="distance > 20" class="scroll-status">查看更多</div>
      <button
        v-if="!ended"
        class="icon-button scroll-status history"
        :disabled="loading"
        @click="history()"
      >
        加载历史消息
      </button>
      <div
        v-for="item in messages"
        :key="item.id"
        class="msgbox-item"
        :class="
          session.sid === item.senderId ? 'move-from-right' : 'move-from-left'
        "
      >
        <div class="time">
          <span>{{ chatTime(item.time, false) }}</span>
        </div>
        <div :class="{ mine: session.sid === item.senderId }">
          <UserAvatar
            class="content-avatar"
            :src="session.profiles[item.senderId]?.avatarUrl"
            :size="40"
          />
          <div class="content">
            <template v-if="item.content.type === 'text'"
              ><template
                v-for="(part, index) in parts(item.content.text)"
                :key="index"
                ><img
                  v-if="emojiMap[part]"
                  :src="'/emoji/' + emojiMap[part]"
                  :alt="part"
                  width="20"
                  height="20"
                /><span v-else class="text-box">{{ part }}</span></template
              ></template
            ><img
              v-else-if="item.content.type === 'image'"
              class="message-image"
              :src="item.content.imageUrl"
              alt="图片消息"
            /><span v-else>系统消息</span>
          </div>
        </div>
      </div>
    </div>
    <form
      class="toolbox"
      :class="showEmoji || showImage ? 'bottom150' : 'bottom0'"
      @submit.prevent="send()"
    >
      <button
        class="icon-button back"
        type="button"
        aria-label="返回"
        @click="back"
      >
        <UiIcon name="undo" />
      </button>
      <div class="input">
        <input
          v-model="input"
          type="text"
          aria-label="消息内容"
          enterkeyhint="send"
          @keydown.enter="enter"
        />
      </div>
      <button
        class="icon-button emoji"
        type="button"
        aria-label="表情"
        @click="
          showEmoji = !showEmoji;
          showImage = false;
        "
      >
        <UiIcon name="emoji" /></button
      ><button
        class="icon-button other"
        type="button"
        aria-label="更多"
        @click="
          showImage = !showImage;
          showEmoji = false;
        "
      >
        <UiIcon name="add" /></button
      ><button
        class="sr-only"
        type="submit"
        :disabled="sending || !input.trim()"
      >
        发送
      </button>
    </form>
    <div class="function" v-show="showEmoji || showImage">
      <div class="function-emoji" v-show="showEmoji">
        <button
          v-for="(file, name) in emojiMap"
          :key="name"
          class="icon-button emoji"
          :aria-label="name"
          @click="input += name"
        >
          <img :src="'/emoji/' + file" width="30" height="30" :alt="name" />
        </button>
      </div>
      <div v-show="showImage" class="function-other"></div>
    </div>
  </div>
</template>
<style scoped>
.chatbox {
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  position: absolute;
  background-color: #e7e7e7;
  overflow: hidden;
}

.msgbox .msgbox-item {
  margin-bottom: 15px;
  position: relative;
  display: block;
  margin-bottom: 10px;
}
.msgbox .msgbox-item .time {
  margin-bottom: 10px;
  text-align: center;
}

.msgbox .msgbox-item .time span {
  display: inline-block;
  padding: 0 5px;
  font-size: 12px;
  color: #fff;
  border-radius: 2px;
  background-color: #c7c7c7;
}

.content {
  display: inline-block;
  position: relative;
  padding: 0 10px;
  max-width: calc(100% - 75px);
  min-height: 35px;
  line-height: 2rem;
  font-size: 15px;
  padding: 6px 10px;
  text-align: left;
  word-break: break-all;
  background-color: #fff;
  color: #000;
  border-radius: 4px;
  box-shadow: 0 1px 7px -5px #000;
}

.content:before {
  content: " ";
  position: absolute;
  top: 9px;
  right: 100%;
  border: 6px solid transparent;
  border-right-color: #fff;
}

.content-avatar {
  margin: 0 10px;
  float: left;
}

.content span {
  display: inline;
  vertical-align: middle;
}
.content img {
  display: inline;
  vertical-align: middle;
}

.mine {
  text-align: right;
}

.mine .content-avatar {
  float: right;
}

.mine .content {
  background-color: #9eea6a;
}
.mine .content:before {
  right: inherit;
  left: 100%;
  border-right-color: transparent;
  border-left-color: #9eea6a;
}

.msgbox .move-from-left {
  left: 0;
  animation: moveLeft 0.7s ease;
  -webkit-animation: moveLeft 0.7s ease;
}
.msgbox .move-from-right {
  left: 0;
  animation: moveRight 0.7s ease;
  -webkit-animation: moveRight 0.7s ease;
}

@keyframes moveRight {
  0% {
    left: -20px;
    opacity: 0;
  }
  100% {
    left: 0;
    opacity: 1;
  }
}
@-webkit-keyframes moveRight {
  0% {
    left: -20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}
@keyframes moveLeft {
  0% {
    left: 20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}
@-webkit-keyframes moveLeft {
  0% {
    left: 20px;
    opacity: 0;
  }
  100% {
    left: 0px;
    opacity: 1;
  }
}

.chatbox .toolbox {
  display: flex;
  position: absolute;
  right: 0;
  left: 0;
  height: 50px;
  line-height: 50px;
  box-shadow: 0px -2px 1px rgba(204, 204, 204, 0.5);
  background-color: #f1f1f1;
  align-items: center;
}

.bottom0 {
  bottom: 0;
}

.bottom150 {
  bottom: 150px;
}

.toolbox .back {
  align-items: center;
  display: flex;
  font-size: 2rem;
}

.toolbox .input {
  flex: 1;
  width: 0;
}

.toolbox .input input {
  width: 100%;
  height: 40px;
}

.toolbox .emoji {
  align-items: center;
  display: flex;
  font-size: 2rem;
}

.toolbox .other {
  align-items: center;
  display: flex;
  font-size: 2.5rem;
}

.chatbox .function {
  display: flex;
  position: absolute;
  right: 0;
  left: 0;
  bottom: 0;
  height: 150px;
  box-shadow: 0px -2px 1px rgba(204, 204, 204, 0.5);
  background-color: #f1f1f1;
  align-items: center;
}

.function-emoji {
  height: 150px;
  padding: 10px;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  overflow-y: scroll;
}
</style>
<style scoped>
.msgbox {
  position: absolute;
  inset: 0 0 calc(50px + env(safe-area-inset-bottom));
  overflow-y: auto;
  overscroll-behavior: contain;
  overflow-anchor: none;
}
.msgbox.expanded {
  bottom: calc(200px + env(safe-area-inset-bottom));
}
.msgbox-item {
  display: flow-root !important;
}
.mine {
  display: flow-root;
}
.text-box {
  white-space: pre-wrap;
}
.content .message-image {
  max-width: 220px;
  max-height: 260px;
  object-fit: contain;
}
.chatbox .toolbox {
  margin: 0;
  height: calc(50px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
}
.chatbox .bottom150 {
  bottom: 150px;
}
.chatbox .function {
  height: 150px;
}
.function-other {
  padding: 15px;
  width: 100%;
  display: flex;
  gap: 8px;
  align-items: end;
}
.function-other input {
  width: 100%;
  min-width: 0;
}
.function-other label {
  min-width: 0;
}
.history {
  display: block;
  margin: auto;
}
</style>
