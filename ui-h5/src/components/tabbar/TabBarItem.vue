<template>
  <div class="tab-bar-item" @click="clickItem">
    <div  v-if = '!isActive'><slot  name = "item-icon"></slot></div>
    <div :style = 'activeStyle' v-else ><slot name="item-icon-active"></slot></div>
    <div :style = 'activeStyle'><slot name = "item-content"></slot></div>
  </div>
</template>

<script>
  export default {
    name : 'TabBarItem',
    props : {
      path : String,
      //颜色可以自定义,传的值必须是一个字符串
      activeColor: {
        type: String,
        default: 'red'
      }
    },
    data() {
      return {

      }
    },
    methods : {
      clickItem() {
        this.$router.push(this.path)
      }
    },
    computed : {
      //当isACtive为ture的时候,字体颜色发生改变
      isActive() {
        return this.$route.path.indexOf(this.path) !== -1
      },
      activeStyle() {
        return this.isActive ? {color: this.activeColor} : {}
      }
    }
  };
</script>

<style>
  .tab-bar > .tab-bar-item {
    flex: 1;
    text-align: center;
  }
  .tab-bar  .tab-bar-item svg {
    width: 24px;
    height: 24px;
    margin-top: 3px;
    vertical-align: middle;
    margin-bottom: 2px;
  }
</style>
