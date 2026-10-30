<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useIntervalFn } from "@vueuse/core";
import { api, perform } from "../api";
import { useSession } from "../store";
import { ElInput, ElButton } from "element-plus";
const phone = ref(""),
  code = ref(""),
  remaining = ref(0),
  busy = ref(false);
const session = useSession(),
  router = useRouter();
useIntervalFn(() => {
  if (remaining.value > 0) remaining.value--;
}, 1000);
async function getCode() {
  if (!/^1[3-9][0-9]{9}$/.test(phone.value)) {
    session.notice = "请输入正确的手机号";
    return;
  }
  busy.value = true;
  try {
    await perform(async () => {
      await api("getPhoneCode", { phone: phone.value });
      remaining.value = 60;
      session.notice = "验证码已发送";
    });
  } finally {
    busy.value = false;
  }
}
async function login() {
  if (!/^1[3-9][0-9]{9}$/.test(phone.value) || !/^\d{6}$/.test(code.value)) {
    session.notice = "请输入手机号和6位验证码";
    return;
  }
  busy.value = true;
  try {
    await perform(async () => {
      const data = await api<{ sid: string; token: string }>("getToken", {
        phone: phone.value,
        code: code.value,
      });
      session.login(data);
      session.notice = "";
      await router.replace("/chat");
    });
  } finally {
    busy.value = false;
  }
}
</script>
<template>
  <section class="login-card">
    <h1>登录系统</h1>
    <p class="text-slate-500 mb-6">登录后开始聊天</p>
    <form @submit.prevent="login">
      <ElInput
        v-model="phone"
        aria-label="手机号"
        placeholder="请输入手机号"
        maxlength="11"
        inputmode="tel"
      />
      <div class="flex gap-2 mt-4">
        <ElInput
          v-model="code"
          aria-label="验证码"
          placeholder="请输入6位验证码"
          maxlength="6"
          inputmode="numeric"
        /><button
          type="button"
          :disabled="busy || remaining > 0"
          @click="getCode"
        >
          {{ remaining ? `${remaining}秒后重发` : "获取验证码" }}
        </button>
      </div>
      <ElButton
        class="w-full mt-6"
        type="success"
        native-type="submit"
        :loading="busy"
        >登录</ElButton
      >
    </form>
  </section>
</template>
