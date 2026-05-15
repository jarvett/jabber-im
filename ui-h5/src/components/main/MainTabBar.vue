<template>
  <tab-bar>
    <tab-bar-item path="/chat" activeColor="#FF5977">
    <svg slot="item-icon" class="icon" aria-hidden="true"><use xlink:href="#icon-interactive"></use></svg>
    <svg slot="item-icon-active" class="icon" aria-hidden="true"><use xlink:href="#icon-interactive_fill"></use></svg>
      <div slot="item-content">聊天</div>
    </tab-bar-item>
    <tab-bar-item path="/profile" activeColor="#FF5977">
       <svg slot="item-icon" class="icon" aria-hidden="true"><use xlink:href="#icon-people"></use></svg>
    <svg slot="item-icon-active" class="icon" aria-hidden="true"><use xlink:href="#icon-people_fill"></use></svg>
      <div slot="item-content">我的</div>
    </tab-bar-item>
  </tab-bar>
</template>

<script>
import TabBar from "../tabbar/TabBar.vue";
import TabBarItem from "../tabbar/TabBarItem.vue";

export default {
  name: "App",
  components: {
    TabBar,
    TabBarItem,
  },
};
</script>

<style>
</style>
