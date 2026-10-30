<script setup lang="ts">
import { ref, watch } from "vue";
const props = withDefaults(
  defineProps<{ src?: string; name?: string; size?: number }>(),
  { name: "匿名", size: 50 },
);
const failed = ref(false);
watch(
  () => props.src,
  () => {
    failed.value = false;
  },
);
</script>
<template>
  <span
    class="vue-avatar--wrapper"
    :style="{
      width: size + 'px',
      height: size + 'px',
      fontSize: Math.floor(size / 2.5) + 'px',
    }"
    aria-hidden="true"
    ><img v-if="src && !failed" :src="src" alt="" @error="failed = true" /><span
      v-else
      >匿</span
    ></span
  >
</template>
<style scoped>
.vue-avatar--wrapper {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  overflow: hidden;
  background: #9c27b0;
  color: #ec77ff;
  font-family: Helvetica, Arial, sans-serif;
}
img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
