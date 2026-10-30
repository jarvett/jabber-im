<script setup lang="ts">
import UserAvatar from "../components/UserAvatar.vue";
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, perform } from "../api";
import UiIcon from "../components/UiIcon.vue";
import MessageBox from "../components/MessageBox.vue";
import { useSession } from "../store";
import type { Profile, Dialogue } from "../types";
const route = useRoute(),
  router = useRouter(),
  group = computed(() => route.path === "/group");
const people = ref<Profile[]>([]),
  selected = ref<string[]>([]),
  groupName = ref(""),
  busy = ref(false);
async function refresh() {
  busy.value = true;
  try {
    await perform(async () => {
      people.value = (
        await api<{ subscriberList: Profile[] }>("getRandomStranger")
      ).subscriberList;
      selected.value = [];
    });
  } finally {
    busy.value = false;
  }
}
async function create(sid?: string) {
  busy.value = true;
  try {
    await perform(async () => {
      const result = await api<{ model: Dialogue }>(
        sid ? "createPrivateDialogue" : "createGroupDialogue",
        sid ? { sid } : { sidList: selected.value, groupName: groupName.value },
        "POST",
      );
      await router.push({
        path: "/chatbox",
        query: { channelId: result.model.channelId },
      });
    });
  } finally {
    busy.value = false;
  }
}
watch(() => route.path, refresh, { immediate: true });

const dialog = ref(false),
  session = useSession();
function toggle(sid: string) {
  if (!group.value || busy.value) return;
  selected.value = selected.value.includes(sid)
    ? selected.value.filter((id) => id !== sid)
    : [...selected.value, sid];
}
function check() {
  if (selected.value.length < 2) session.notice = "请至少选择2个用户发起群聊";
  else dialog.value = true;
}
function confirmGroup(value: string) {
  groupName.value = value;
  void create();
}
</script>
<template>
  <div class="stranger">
    <div class="stranger-wrap">
      <div
        v-for="person in people"
        :key="person.sid"
        class="stranger-item"
        :role="group ? 'checkbox' : undefined"
        :aria-checked="group ? selected.includes(person.sid) : undefined"
        :aria-label="group ? '选择' + person.nickName : undefined"
        :tabindex="group ? 0 : undefined"
        @click="toggle(person.sid)"
        @keydown.space.prevent="toggle(person.sid)"
      >
        <div class="stranger-item-left">
          <UserAvatar class="stranger-item-avatar" :src="person.avatarUrl" />
          <div class="stranger-item-avatar-right">
            <div class="stranger-item-name">{{ person.nickName }}</div>
            <div class="stranger-item-brief">
              {{ person.intro || "这个家伙很懒,什么都没说~" }}
            </div>
          </div>
        </div>
        <div class="stranger-item-right">
          <div v-if="group" class="right">
            <UiIcon v-show="selected.includes(person.sid)" name="right" />
          </div>
          <button
            v-else
            class="icon-button stranger-action-text"
            :disabled="busy"
            @click.stop="create(person.sid)"
          >
            撩一下
          </button>
        </div>
      </div>
    </div>
    <div class="stranger-opt">
      <button class="icon-button back" aria-label="返回" @click="router.back()">
        <UiIcon name="undo" /></button
      ><button
        class="icon-button refresh"
        aria-label="刷新"
        :disabled="busy"
        @click="refresh"
      >
        <UiIcon name="refresh" /></button
      ><button
        v-if="group"
        class="icon-button right"
        aria-label="创建群聊"
        :disabled="busy"
        @click="check"
      >
        <UiIcon name="right" />
      </button>
    </div>
    <MessageBox
      v-if="dialog"
      title="设置群名称"
      content="请为您的群起个好听的名字吧"
      input
      :busy="busy"
      @confirm="confirmGroup"
      @cancel="dialog = false"
    />
  </div>
</template>
<style>
.stranger {
  width: 100%;
  height: 100%;
  background-color: #e7e7e7;
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
}
</style>
<style scoped>
.stranger-item {
  display: flex;
  padding: 10px;
  background-color: #ffffff;
  margin-bottom: 3px;
  max-height: 60px;
  overflow: hidden;
  align-items: center;
}

.stranger-item-left {
  width: 0;
  flex: 1;
  display: flex;
  height: 50px;
}

.stranger-item-avatar {
  min-width: 60px;
}

.stranger-item-avatar-right {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  max-height: 60px;
  overflow: hidden;
}

.stranger-item-name {
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stranger-item-brief {
  font-size: 70%;
  color: #999999;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stranger-item-right {
  line-height: 50px;
  font-size: small;
  color: #666666;
}

.stranger-item-right .right {
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
  color: #44d817;
}

.stranger-opt {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #ffffff;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.stranger-opt .back {
  flex: 1;
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
}

.stranger-opt .refresh {
  flex: 1;
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
}

.stranger-opt .right {
  flex: 1;
  align-items: center;
  display: flex;
  flex-direction: column;
  font-size: 1.5rem;
}
</style>
<style scoped>
.stranger-wrap {
  height: calc(100% - 60px - env(safe-area-inset-bottom));
  overflow-y: auto;
}
.stranger-opt {
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
}
.stranger-action-text {
  color: #44d817;
}
</style>
