<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
const props = defineProps<{
  title: string;
  content: string;
  input?: boolean;
  busy?: boolean;
}>();
const emit = defineEmits<{ confirm: [value: string]; cancel: [] }>();
const value = ref(""),
  field = ref<HTMLInputElement>(),
  box = ref<HTMLElement>();
const previous = document.activeElement as HTMLElement | null;
onMounted(() => {
  (field.value || box.value)?.focus();
});
onBeforeUnmount(() => {
  previous?.focus();
});
function cancel() {
  if (!props.busy) emit("cancel");
}
function keydown(event: KeyboardEvent) {
  if (event.key === "Escape") cancel();
  if (event.key !== "Tab") return;
  const fields = [
    ...(box.value?.querySelectorAll<HTMLElement>(
      "input,button:not(:disabled)",
    ) || []),
  ];
  const index = fields.indexOf(document.activeElement as HTMLElement);
  event.preventDefault();
  fields[
    (index + (event.shiftKey ? -1 : 1) + fields.length) % fields.length
  ]?.focus();
}
</script>
<template>
  <Teleport to="body"
    ><div class="message-box">
      <div class="mask" @click="cancel"></div>
      <div
        ref="box"
        class="message-content"
        role="dialog"
        aria-modal="true"
        :aria-label="title"
        tabindex="-1"
        @keydown="keydown"
      >
        <div class="title">{{ title }}</div>
        <div class="content">{{ content }}</div>
        <form @submit.prevent="emit('confirm', value)">
          <input
            v-if="input"
            ref="field"
            v-model="value"
            :aria-label="title"
            :disabled="busy"
          />
          <div class="btn-group">
            <button
              type="button"
              class="btn-default"
              :disabled="busy"
              @click="cancel"
            >
              取消</button
            ><button
              class="btn-primary btn-confirm"
              :disabled="busy || (input && !value)"
            >
              确定
            </button>
          </div>
        </form>
      </div>
    </div></Teleport
  >
</template>
<style scoped>
.message-box {
  position: relative;
}

.mask {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  z-index: 50000;
  background: rgba(0, 0, 0, 0.5);
}
.message-content {
  position: fixed;
  box-sizing: border-box;
  padding: 1em;
  min-width: 90%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 0.4em;
  background: #fff;
  z-index: 50001;
}

.title {
  font-size: 1.2em;
  font-weight: 600;
  margin-bottom: 10px;
}
.content {
  font-size: 1em;
  line-height: 2em;
  color: #555;
}

input {
  width: 100%;
  margin: 10px 0;
  background-color: #fff;
  border-radius: 0.4em;
  border: 1px solid #d8dce5;
  box-sizing: border-box;
  color: #5a5e66;
  display: inline-block;
  font-size: 14px;
  height: 3em;
  line-height: 1;
  outline: none;
  padding: 0 1em;
}
input:focus {
  border-color: #2d8cf0;
}

.btn-group {
  margin-top: 1em;
  float: right;
  overflow: hidden;
}
.btn-default {
  padding: 0.5em 1em;
  font-size: 1em;
  color: #555;
  border: 1px solid #d8dce5;
  border-radius: 0.2em;
  cursor: pointer;
  background-color: #fff;
  outline: none;
}

.btn-default:hover {
  color: #2d8cf0;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-primary {
  padding: 0.5em 1em;
  font-size: 1em;
  color: #fff;
  border-radius: 0.2em;
  cursor: pointer;
  border: 1px solid #45ad25;
  background-color: #45ad25;
  outline: none;
}
.btn-primary:hover {
  opacity: 0.8;
}
.btn-confirm {
  margin-left: 1em;
}
</style>
