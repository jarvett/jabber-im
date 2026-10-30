<script setup lang="ts">
import UserAvatar from "../components/UserAvatar.vue";
import { ref, onMounted } from "vue";
import { api, perform } from "../api";
import { useSession } from "../store";
import type { Profile } from "../types";
const session = useSession(),
  profile = ref<Profile>(),
  intro = ref(""),
  busy = ref(false);
async function load() {
  const data = await api<{ profile: Profile }>("getMyProfile");
  profile.value = data.profile;
  intro.value = data.profile.intro;
  session.remember([data.profile]);
}
async function update(path: string) {
  busy.value = true;
  try {
    await perform(async () => {
      await api(
        path,
        path === "changeIntro" ? { intro: intro.value } : {},
        "POST",
      );
      await load();
    });
  } finally {
    busy.value = false;
  }
}
onMounted(() => perform(load));
</script>
<template>
  <section class="panel profile">
    <h1>我的资料</h1>
    <template v-if="profile"
      ><UserAvatar
        class="avatar large"
        :src="profile.avatarUrl"
        :name="profile.nickName"
      />
      <h2>{{ profile.nickName }}</h2>
      <p>{{ profile.uniqueId }}</p>
      <button :disabled="busy" @click="update('changeAvatarAndNickName')">
        换个马甲
      </button>
      <form @submit.prevent="update('changeIntro')">
        <label>个性签名<input v-model="intro" required /></label
        ><button class="primary" :disabled="busy">保存签名</button>
      </form></template
    >
    <button @click="session.logout">退出登录</button>
  </section>
</template>
