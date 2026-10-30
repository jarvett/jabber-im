<script setup lang="ts">
import { computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useTimeoutFn } from "@vueuse/core";
import { useSession } from "./store";
import { useChatSocket } from "./socket";
import UiIcon from "./components/UiIcon.vue";
const session = useSession(),
  route = useRoute(),
  router = useRouter();
const tabs = computed(() => ["/chat", "/profile"].includes(route.path));
useChatSocket();
watch(
  () => session.token,
  (token) => {
    if (!token) void router.replace("/login");
  },
);
const { start } = useTimeoutFn(
  () => {
    session.notice = "";
  },
  3000,
  { immediate: false },
);
watch(
  () => session.notice,
  (value) => {
    if (value) start();
  },
);
</script>
<template>
  <div class="h5-app" :class="{ 'with-tabs': tabs }">
    <RouterView />
    <nav v-if="tabs" class="tab-bar" aria-label="主导航">
      <RouterLink
        to="/chat"
        class="tab-bar-item"
        :class="{ active: route.path === '/chat' }"
        ><div>
          <UiIcon
            :name="route.path === '/chat' ? 'interactive_fill' : 'interactive'"
          />
        </div>
        <div>聊天</div></RouterLink
      >
      <RouterLink
        to="/profile"
        class="tab-bar-item"
        :class="{ active: route.path === '/profile' }"
        ><div>
          <UiIcon
            :name="route.path === '/profile' ? 'people_fill' : 'people'"
          />
        </div>
        <div>我的</div></RouterLink
      >
    </nav>
    <div v-if="session.notice" class="toast" role="alert">
      {{ session.notice }}
    </div>
  </div>
</template>
<style scoped>
.toast {
  position: fixed;
  z-index: 2000;
  left: 50%;
  top: 45%;
  transition: all 0.5s;
  -webkit-transform: translateX(-50%) translateY(-50%);
  -moz-transform: translateX(-50%) translateY(-50%);
  -ms-transform: translateX(-50%) translateY(-50%);
  -o-transform: translateX(-50%) translateY(-50%);
  transform: translateX(-50%) translateY(-50%);
  text-align: center;
  border-radius: 5px;
  color: #fff;
  background: rgba(17, 17, 17, 0.7);
  height: 45px;
  line-height: 45px;
  padding: 0 15px;
  max-width: 300px;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
