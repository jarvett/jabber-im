<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import NumberKeyboard from "../components/NumberKeyboard.vue";
import { api, perform } from "../api";
import { useSession } from "../store";

const phone = ref(""),
  code = ref(""),
  step = ref<"phone" | "code">("phone"),
  busy = ref(false);
const session = useSession(),
  router = useRouter();
// 验证码发送成功才进入第二步；失败时保留输入。
async function getCode() {
  if (busy.value) return;
  if (!/^1[3-9][0-9]{9}$/.test(phone.value)) {
    session.notice = "请输入正确的手机号";
    return;
  }
  busy.value = true;
  try {
    await perform(async () => {
      await api("getPhoneCode", { phone: phone.value });
      code.value = "";
      step.value = "code";
      session.notice = "";
    });
  } finally {
    busy.value = false;
  }
}
async function login() {
  if (busy.value) return;
  if (!/^\d{6}$/.test(code.value)) {
    session.notice = "请输入6位数字验证码";
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
function back() {
  session.notice = "";
  if (step.value === "code") {
    step.value = "phone";
    code.value = "";
  } else {
    void router.push("/chat");
  }
}
</script>
<template>
  <NumberKeyboard
    v-if="step === 'phone'"
    v-model="phone"
    title="请输入手机号"
    :max-length="11"
    :disabled="busy"
    @confirm="getCode"
    @back="back"
  />
  <NumberKeyboard
    v-else
    v-model="code"
    title="请输入验证码"
    :max-length="6"
    :disabled="busy"
    @confirm="login"
    @back="back"
  />
</template>
