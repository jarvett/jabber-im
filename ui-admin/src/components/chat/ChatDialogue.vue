<script setup lang="ts">
import FloatingWindow from "./FloatingWindow.vue";
import ChatAvatar from "./ChatAvatar.vue";
import type { ChatDialogueItem, WindowPosition } from "./types";
withDefaults(
  defineProps<{
    dialogueList: ChatDialogueItem[];
    selectedChannelId?: string;
    loading?: boolean;
    hasMore?: boolean;
    initialPosition?: WindowPosition;
  }>(),
  { selectedChannelId: "" },
);
const visible = defineModel<boolean>("visible", { default: true });
const emit = defineEmits<{
  select: [item: ChatDialogueItem];
  refresh: [];
  loadMore: [];
}>();
</script>
<template>
  <FloatingWindow
    v-model:visible="visible"
    title="会话列表"
    :width="250"
    :initial-position="initialPosition"
    ><div class="dialogue-tools">
      <button :disabled="loading" @click="emit('refresh')">刷新</button
      ><slot name="tools" />
    </div>
    <div class="dialogue-items">
      <button
        v-for="item in dialogueList"
        :key="item.id"
        class="dialogue-item"
        :class="{ active: item.channelId === selectedChannelId }"
        @click="emit('select', item)"
      >
        <ChatAvatar :src="item.avatar" :name="item.name" /><span class="summary"
          ><strong>{{ item.name }}</strong
          ><span>{{ item.brief }}</span></span
        ><span class="meta"
          ><time>{{
            new Date(Number(item.lastTime)).toLocaleDateString()
          }}</time
          ><span v-if="item.unreadCount" class="unread">{{
            item.unreadCount
          }}</span></span
        >
      </button>
      <p v-if="!loading && !dialogueList.length" class="empty">暂无会话</p>
      <button
        v-if="hasMore"
        class="load-more"
        :disabled="loading"
        @click="emit('loadMore')"
      >
        加载更多会话
      </button>
    </div></FloatingWindow
  >
</template>
<style scoped>
.dialogue-tools {
  padding: 6px;
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 12px;
}
.dialogue-tools button,
.load-more {
  border: 0;
  background: transparent;
  color: #777;
  padding: 4px;
  cursor: pointer;
  font: inherit;
}
.dialogue-items {
  overflow: auto;
  flex: 1;
  min-height: 0;
}
.dialogue-item {
  box-sizing: border-box;
  display: flex;
  width: 100%;
  gap: 8px;
  border: 0;
  border-radius: 0;
  background: #f5f5f5;
  padding: 5px;
  cursor: pointer;
  text-align: left;
  color: #333;
  font: inherit;
}
.dialogue-item.active {
  background: #c6c6c6;
}
.summary {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.summary strong {
  font-size: 13px;
  font-weight: normal;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.summary > span {
  font-size: 11px;
  color: #999;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.meta {
  display: flex;
  flex-direction: column;
  align-items: end;
  gap: 6px;
  font-size: 10px;
  color: #aaa;
}
.unread {
  background: #ef5757;
  color: #fff;
  border-radius: 10px;
  padding: 1px 5px;
}
.empty {
  text-align: center;
  color: #999;
  padding: 24px;
}
</style>
