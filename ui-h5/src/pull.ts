import { ref, type Ref } from "vue";
/** 原生滚动承接旧 scroller 的顶部下拉，不拦截正常滑动。 */
export function usePullRefresh(
  element: Ref<HTMLElement | undefined>,
  refresh: () => void,
) {
  const distance = ref(0);
  let start: number | undefined;
  function touchStart(event: TouchEvent) {
    start =
      element.value?.scrollTop === 0 ? event.touches[0]?.clientY : undefined;
  }
  function touchMove(event: TouchEvent) {
    if (start !== undefined)
      distance.value = Math.max(0, (event.touches[0]?.clientY || 0) - start);
  }
  function touchEnd() {
    if (distance.value >= 60) refresh();
    distance.value = 0;
    start = undefined;
  }
  return { distance, touchStart, touchMove, touchEnd };
}
