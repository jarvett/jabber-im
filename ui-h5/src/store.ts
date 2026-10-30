import { defineStore } from "pinia";
import { ref } from "vue";
import { useStorage } from "@vueuse/core";
import { storagePrefix } from "./platform";
import type { Profile, Message, Dialogue } from "./types";

export const useSession = defineStore("session", () => {
  // 沿用旧存储键，升级后保留登录态；管理端和 H5 互不覆盖。
  const token = useStorage(storagePrefix + "token", "");
  const sid = useStorage(storagePrefix + "sid", "");
  const profiles = ref<Record<string, Profile>>({});
  const latest = ref<Message | null>(null);
  const readDialogue = ref<Dialogue | null>(null);
  const notice = ref("");
  const connected = ref(false);
  function login(value: { token: string; sid: string }) {
    sid.value = String(value.sid);
    token.value = value.token;
  }
  function logout() {
    token.value = "";
    sid.value = "";
    profiles.value = {};
    latest.value = null;
    readDialogue.value = null;
  }
  function remember(items: Profile[]) {
    for (const item of items) profiles.value[item.sid] = item;
  }
  return {
    token,
    sid,
    profiles,
    latest,
    readDialogue,
    notice,
    connected,
    login,
    logout,
    remember,
  };
});
