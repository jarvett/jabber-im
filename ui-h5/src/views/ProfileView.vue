<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { api, perform } from "../api";
import { useSession } from "../store";
import { usePullRefresh } from "../pull";
import type { Profile } from "../types";
import UserAvatar from "../components/UserAvatar.vue";
import UiIcon from "../components/UiIcon.vue";
import MessageBox from "../components/MessageBox.vue";
const session = useSession(),
  router = useRouter(),
  profile = ref<Profile>(),
  busy = ref(false),
  dialog = ref<"intro" | "avatar">();
const scroll = ref<HTMLElement>();
async function load() {
  const data = await api<{ profile: Profile }>("getMyProfile");
  profile.value = data.profile;
  session.remember([data.profile]);
}
const { distance, touchStart, touchMove, touchEnd } = usePullRefresh(
  scroll,
  () => {
    void perform(load);
  },
);
async function update(value: string) {
  busy.value = true;
  try {
    await perform(async () => {
      await api(
        dialog.value === "intro" ? "changeIntro" : "changeAvatarAndNickName",
        dialog.value === "intro" ? { intro: value } : {},
        "POST",
      );
      dialog.value = undefined;
      await load();
    });
  } finally {
    busy.value = false;
  }
}
onMounted(() => perform(load));
</script>
<template>
  <div
    ref="scroll"
    class="profile"
    @touchstart.passive="touchStart"
    @touchmove.passive="touchMove"
    @touchend="touchEnd"
  >
    <div v-if="distance > 20" class="scroll-status">下拉刷新</div>
    <div class="profile-wrap">
      <div class="profile-head">
        <UserAvatar class="profile-head-left" :src="profile?.avatarUrl" />
        <div class="profile-head-right">
          <div class="profile-head-nickname">
            {{ profile?.nickName || "匿名" }}
          </div>
          <div class="profile-head-unique-id">
            聊天号:{{ profile?.uniqueId || "" }}
          </div>
        </div>
        <button class="icon-button profile-head-opt" @click="dialog = 'avatar'">
          <span>换个马甲</span><UiIcon name="refresh" />
        </button>
      </div>
      <div
        class="profile-intro"
        role="button"
        tabindex="0"
        aria-label="修改个性签名"
        @click="dialog = 'intro'"
        @keydown.enter="dialog = 'intro'"
      >
        <span>{{ profile?.intro || "这个家伙很懒,什么都没说~" }}</span
        ><UiIcon name="brush" />
      </div>
      <div class="profile-item-divider"></div>
      <button
        class="icon-button profile-item"
        @click="router.push('/stranger')"
      >
        随便聊聊
      </button>
      <div class="profile-item-divider"></div>
      <button class="icon-button profile-item" @click="router.push('/group')">
        发起群聊
      </button>
      <div class="profile-item-divider"></div>
      <button
        class="icon-button profile-item"
        @click="session.notice = '模块开发中'"
      >
        查找好友
      </button>
      <div class="profile-item-divider"></div>
      <button class="icon-button profile-item" @click="session.logout">
        退出
      </button>
      <div class="profile-item-bg"></div>
    </div>
    <MessageBox
      v-if="dialog"
      :title="dialog === 'intro' ? '个性签名' : '换个马甲'"
      :content="
        dialog === 'intro' ? '请填写个性签名' : '是否需要换了头像和昵称'
      "
      :input="dialog === 'intro'"
      :busy="busy"
      @confirm="update"
      @cancel="dialog = undefined"
    />
  </div>
</template>
<style>
.profile {
  width: 100%;
  height: 100%;
}

.profile ._v-container {
  background-color: #e7e7e7;
}
</style>
<style scoped>
.profile-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.profile-head {
  padding: 30px 15px 15px 15px;
  display: flex;
  background-color: #ffffff;
}

.profile-head-right {
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.profile-head-nickname {
  font-weight: bolder;
}

.profile-head-unique-id {
  font-size: 80%;
  color: #999999;
}

.profile-head-opt {
  align-items: center;
  display: flex;
  flex: 1;
  justify-content: center;
  color: #999999;
}

.profile-head-opt span {
  font-size: 80%;
}

.profile-intro {
  padding: 0 15px 15px 15px;
  background: #ffffff;
  font-size: 80%;
  color: #999999;
}

.profile-item-divider {
  height: 3px;
  width: 100%;
}

.profile-item {
  width: 100%;
  text-align: center;
  padding-top: 10px;
  padding-bottom: 10px;
  background-color: #ffffff;
}

.profile-item-bg {
  flex: 1;
  background-color: #e7e7e7;
}
</style>
<style scoped>
.profile {
  overflow-y: auto;
  background: #e7e7e7;
}
.profile-wrap {
  min-height: 100%;
  height: auto;
}
</style>
