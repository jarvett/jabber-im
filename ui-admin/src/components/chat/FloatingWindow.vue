<script lang="ts">
// 同一页面多个窗口共用层级，交互窗口自动置顶。
let topLayer = 1000;
</script>
<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useWindowSize } from "@vueuse/core";
import type { WindowPosition } from "./types";
const props = withDefaults(
  defineProps<{
    title: string;
    width: number;
    height?: number;
    initialPosition?: WindowPosition;
    offset?: number;
    radius?: number;
  }>(),
  { height: 600, offset: 0, radius: 12 },
);
const visible = defineModel<boolean>("visible", { default: true });
const { width: viewportWidth, height: viewportHeight } = useWindowSize();
const x = ref(
  props.initialPosition?.x ?? viewportWidth.value * 0.1 + props.offset,
);
const y = ref(props.initialPosition?.y ?? viewportHeight.value * 0.1);
const z = ref(++topLayer);
const width = computed(() =>
  Math.min(props.width, Math.max(0, viewportWidth.value - 16)),
);
const height = computed(() =>
  Math.min(props.height, Math.max(0, viewportHeight.value - 16)),
);
let origin:
  | { pointer: number; x: number; y: number; left: number; top: number }
  | undefined;
function constrain() {
  x.value = Math.max(
    8,
    Math.min(x.value, viewportWidth.value - width.value - 8),
  );
  y.value = Math.max(
    8,
    Math.min(y.value, viewportHeight.value - height.value - 8),
  );
}
watch(
  [viewportWidth, viewportHeight, visible],
  () => {
    constrain();
    if (visible.value) z.value = ++topLayer;
  },
  { immediate: true },
);
function begin(event: PointerEvent) {
  // 只拖标题栏，不抢占输入、文本选择、滚动和关闭按钮。
  if (event.button !== 0 || (event.target as HTMLElement).closest("button"))
    return;
  origin = {
    pointer: event.pointerId,
    x: event.clientX,
    y: event.clientY,
    left: x.value,
    top: y.value,
  };
  (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
  event.preventDefault();
}
function move(event: PointerEvent) {
  if (!origin || event.pointerId !== origin.pointer) return;
  x.value = origin.left + event.clientX - origin.x;
  y.value = origin.top + event.clientY - origin.y;
  constrain();
}
function end() {
  origin = undefined;
}
</script>
<template>
  <Teleport to="body"
    ><section
      v-show="visible"
      class="floating-window"
      role="region"
      :aria-label="title"
      :style="{
        left: x + 'px',
        top: y + 'px',
        width: width + 'px',
        height: height + 'px',
        zIndex: z,
        borderRadius: radius + 'px',
      }"
      @pointerdown="z = ++topLayer"
    >
      <header
        class="window-handle"
        @pointerdown="begin"
        @pointermove="move"
        @pointerup="end"
        @pointercancel="end"
        @lostpointercapture="end"
      >
        <span>{{ title }}</span
        ><button
          class="window-close"
          type="button"
          :aria-label="'关闭' + title"
          @click="visible = false"
        >
          ×
        </button>
      </header>
      <div class="window-content"><slot /></div></section
  ></Teleport>
</template>
<style scoped>
.floating-window {
  position: fixed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #e3e3e3;
  border: 1px solid #e3e3e3;
  border-radius: 12px;
  box-shadow: 0 8px 30px #0002;
  font:
    14px Arial,
    "Microsoft YaHei",
    sans-serif;
  color: #333;
  box-sizing: border-box;
}
.window-handle {
  position: relative;
  flex-shrink: 0;
  padding: 10px 40px;
  text-align: center;
  background: #d9d9d9;
  cursor: move;
  user-select: none;
  touch-action: none;
  line-height: 20px;
}
.window-close {
  position: absolute;
  right: 8px;
  top: 4px;
  border: 0;
  background: transparent;
  font: 24px/28px Arial;
  cursor: pointer;
  color: #555;
  padding: 0 4px;
}
.window-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
