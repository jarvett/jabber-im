<script setup lang="ts">
import { ref } from "vue";
import {
  ChatDialogue,
  ChatWindow,
  type ChatMessage,
  type ChatSend,
} from "../../src/components/chat";
const showList = ref(true),
  showChat = ref(true),
  draft = ref(""),
  selected = ref(""),
  sent = ref("");
const messages = ref<ChatMessage[]>([]);
const dialogues = [
  {
    id: "1",
    channelId: "10",
    name: "嵌入好友",
    avatar: "",
    brief: "独立接入",
    lastTime: "1700000000000",
    unreadCount: 0,
  },
];
function send(content: ChatSend) {
  sent.value = JSON.stringify(content);
  messages.value = [
    ...messages.value,
    {
      id: String(messages.value.length + 1),
      channelId: "10",
      senderId: "1",
      time: String(Date.now()),
      content,
    },
  ];
  draft.value = "";
}
</script>
<template>
  <button @click="showList = true">恢复列表</button
  ><button @click="showChat = true">恢复窗口</button
  ><output data-testid="selection">{{ selected }}</output
  ><output data-testid="sent">{{ sent }}</output>
  <ChatDialogue
    v-model:visible="showList"
    :dialogue-list="dialogues"
    @select="selected = $event.channelId"
  />
  <ChatWindow
    v-model:visible="showChat"
    v-model="draft"
    title="嵌入聊天"
    channel-id="10"
    current-user-id="1"
    :messages="messages"
    history-ended
    @send="send"
  />
</template>
