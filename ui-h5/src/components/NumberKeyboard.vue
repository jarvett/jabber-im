<script setup lang="ts">
const value = defineModel<string>({ required: true });
const props = defineProps<{
  title: string;
  maxLength: number;
  disabled?: boolean;
}>();
const emit = defineEmits<{ confirm: []; back: [] }>();

// 手机号和验证码共用数字键盘，不弹出手机原生输入法。
function press(key: string) {
  if (props.disabled) return;
  if (/^\d$/.test(key)) {
    if (value.value.length < props.maxLength) value.value += key;
  } else if (key === "delete") {
    value.value = value.value.slice(0, -1);
  } else if (key === "clear") {
    value.value = "";
  } else if (key === "back") {
    emit("back");
  } else if (key === "confirm" && value.value) {
    emit("confirm");
  }
}
function onKeydown(event: KeyboardEvent) {
  if (event.ctrlKey || event.metaKey || event.altKey) return;
  const keys: Record<string, string> = {
    Backspace: "delete",
    Delete: "clear",
    Enter: "confirm",
    Escape: "back",
  };
  const key = keys[event.key] || (/^\d$/.test(event.key) ? event.key : "");
  if (key) {
    event.preventDefault();
    press(key);
  }
}
</script>
<template>
  <section class="key-container" :aria-busy="disabled">
    <div class="key-display">
      <h1>{{ title }}</h1>
      <div
        class="input-box"
        role="textbox"
        :aria-label="title"
        aria-readonly="true"
        tabindex="0"
        @keydown="onKeydown"
      >
        {{ value }}
      </div>
    </div>
    <div class="keyboard" role="group" aria-label="数字键盘">
      <template
        v-for="row in [
          [7, 8, 9],
          [4, 5, 6],
          [1, 2, 3],
        ]"
        :key="row[0]"
      >
        <button
          v-for="number in row"
          :key="number"
          type="button"
          :disabled="disabled"
          @click="press(String(number))"
        >
          {{ number }}
        </button>
        <button
          v-if="row[0] === 7"
          type="button"
          aria-label="删除一位"
          :disabled="disabled"
          @click="press('delete')"
        >
          C
        </button>
        <button
          v-else-if="row[0] === 4"
          type="button"
          :disabled="disabled"
          @click="press('clear')"
        >
          清空
        </button>
      </template>
      <button
        class="back"
        type="button"
        :disabled="disabled"
        @click="press('back')"
      >
        返回
      </button>
      <button type="button" :disabled="disabled" @click="press('0')">0</button>
      <button type="button" disabled aria-label="小数点不可用">.</button>
      <button
        class="confirm"
        type="button"
        :disabled="disabled || !value"
        @click="press('confirm')"
      >
        {{ disabled ? "请稍候" : "确认" }}
      </button>
    </div>
  </section>
</template>
<style scoped>
.key-container {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.key-display {
  padding: clamp(24px, 10vh, 80px) 5% 24px;
  text-align: center;
}
h1 {
  font-size: 18px;
  font-weight: normal;
}
.input-box {
  height: 64px;
  margin-top: 30px;
  padding: 10px 0;
  border-bottom: 1px solid #aaa;
  font-size: 30px;
  font-weight: bold;
  letter-spacing: 2px;
}
.input-box:focus-visible {
  outline: 2px solid #2aaf55;
  outline-offset: 4px;
}
.keyboard {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-template-rows: repeat(4, 60px);
  margin-top: auto;
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
}
.keyboard button {
  border-radius: 0;
  border: 0;
  border-top: 1px solid #d5d5d6;
  border-right: 1px solid #d5d5d6;
  font-size: 18px;
  font-weight: bold;
  padding: 0;
}
.keyboard button:active:not(:disabled) {
  background: #d5d5d6;
}
.keyboard button:disabled {
  background: #eee;
}
.back {
  grid-row: 4;
  grid-column: 1;
}
.confirm {
  grid-row: 3 / 5;
  grid-column: 4;
}
</style>
