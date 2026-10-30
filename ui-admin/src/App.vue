<script setup lang="ts">
import { watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useSession } from "./store";
import { mobile } from "./platform";
import { useChatSocket } from "./socket";
const session = useSession();
const router = useRouter();
const route = useRoute();
useChatSocket();
watch(
  () => session.token,
  (token) => {
    if (!token) void router.replace("/login");
  },
);
</script>
<template>
  <div class="app-shell" :class="{ mobile }">
    <header v-if="session.token" class="app-header">
      <strong>Jabber</strong
      ><span class="text-xs text-slate-500">{{
        session.connected ? "已连接" : "连接中…"
      }}</span>
      <nav aria-label="页面导航">
        <RouterLink to="/chat" :class="{ 'router-link-active': route.path === '/chatbox' }">聊天</RouterLink>
        <RouterLink to="/stranger">随便聊聊</RouterLink>
        <RouterLink to="/group">发起群聊</RouterLink>
        <RouterLink to="/profile">我的</RouterLink>
      </nav>
    </header>
    <div v-if="session.notice" class="notice" role="alert">
      <span>{{ session.notice }}</span
      ><button aria-label="关闭提示" @click="session.notice = ''">×</button>
    </div>
    <main><RouterView /></main>
  </div>
</template>
