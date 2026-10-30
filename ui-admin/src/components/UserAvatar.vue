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
  <span class="avatar avatar-fallback" :aria-label="name || '头像'">
    <img v-if="src && !failed" :src="src" alt="" @error="failed = true" />
    <span v-else>{{ (name || "聊").slice(0, 1) }}</span>
  </span>
</template>
<style scoped>
.avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #d7e9df;
  color: #377652;
  overflow: hidden;
}
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
