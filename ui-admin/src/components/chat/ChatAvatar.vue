<script setup lang="ts">
import { ref, watch } from "vue";
const props = defineProps<{ src?: string; name?: string }>();
const failed = ref(false);
watch(
  () => props.src,
  () => {
    failed.value = false;
  },
);
</script>
<template>
  <span class="chat-avatar"
    ><img v-if="src && !failed" :src="src" alt="" @error="failed = true" /><span
      v-else
      >{{ (name || "聊").slice(0, 1) }}</span
    ></span
  >
</template>
<style scoped>
.chat-avatar {
  display: inline-flex;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: #d9e9df;
  color: #367451;
  border-radius: 4px;
  overflow: hidden;
}
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
