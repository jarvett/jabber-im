<template>
  <div>
    <router-view />
    <main-tab-bar></main-tab-bar>
  </div>
</template>

<script>
import MainTabBar from '../components/main/MainTabBar.vue';
import {jsonParse, WS} from '../configs/utils'
import bus from '../configs/vbus'

export default {
  name:'index',
  components: {
    MainTabBar,
  },
  mounted() {
    let url = location.origin.replace("http",'ws') + '/im/webSocket';
    WS.connectServer(url,this.$store.state.token,this.handleMessage)
  },
  methods:{
    handleMessage(msg){
      bus.$emit('ws_msg',jsonParse(msg));
    }
  },
}
</script>

<style scoped>
</style>
