<template>
  <div v-if="isShowLoading" class="loading-container">
    <div class="loading-box">
      <svg class="icon loading-img" aria-hidden="true"><use xlink:href="#icon-refresh"></use></svg>
      <span class="loading-text">{{content}}</span>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      isShowLoading: false,
      content: ''
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

.loading-box{
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
  max-width: 150px;
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

.loading-img {
  width: 45px;
  height: 45px;
  animation: rotating 2s linear infinite;
}
@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(1turn);
  }
}
.loading-text {
  max-width: 120px;
  word-break: keep-all;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
