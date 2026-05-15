<template>
  <div v-if="isShowToast" class="toast" @touchmove.prevent>
    {{content}}
  </div>
</template>

<script>
export default {
  data() {
    return {
      isShowToast: true,
      content: "",
    };
  },
};
</script>

<style scoped>
.toast {
  position: fixed;
  z-index: 2000;
  left: 50%;
  top: 45%;
  transition: all 0.5s;
  -webkit-transform: translateX(-50%) translateY(-50%);
  -moz-transform: translateX(-50%) translateY(-50%);
  -ms-transform: translateX(-50%) translateY(-50%);
  -o-transform: translateX(-50%) translateY(-50%);
  transform: translateX(-50%) translateY(-50%);
  text-align: center;
  border-radius: 5px;
  color: #fff;
  background: rgba(17, 17, 17, 0.7);
  height: 45px;
  line-height: 45px;
  padding: 0 15px;
  max-width: 300px;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
