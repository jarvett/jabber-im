<script setup lang="ts">
import UserAvatar from "../components/UserAvatar.vue";
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api, perform } from "../api";
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
</script>
<template>
  <section class="panel">
    <div class="flex justify-between gap-3">
      <h1>{{ group ? "发起群聊" : "随便聊聊" }}</h1>
      <button :disabled="busy" @click="refresh">换一批</button>
    </div>
    <p v-if="!people.length" class="empty">暂无其他用户</p>
    <div v-for="person in people" :key="person.sid" class="person">
      <UserAvatar
        class="avatar"
        :src="person.avatarUrl"
        :name="person.nickName"
      />
      <div class="flex-1 min-w-0">
        <strong>{{ person.nickName }}</strong>
        <p class="truncate text-slate-500">
          {{ person.intro || "这个家伙很懒，什么都没说~" }}
        </p>
      </div>
      <input
        v-if="group"
        v-model="selected"
        type="checkbox"
        :value="person.sid"
        :aria-label="`选择${person.nickName}`"
      /><button v-else :disabled="busy" @click="create(person.sid)">
        撩一下
      </button>
    </div>
    <form v-if="group" @submit.prevent="create()">
      <label
        >群名称<input
          v-model="groupName"
          placeholder="群聊名称（可选）" /></label
      ><button class="primary" :disabled="busy || selected.length < 2">
        创建群聊（{{ selected.length }}）
      </button>
    </form>
    <RouterLink to="/chat">返回聊天</RouterLink>
  </section>
</template>
