import Vue from 'vue'
import App from './App.vue'
import store from './configs/store'
import router from './configs/router'

Vue.config.productionTip = false

import axios from './configs/axios'
Vue.prototype.axios = axios

import qs from 'qs'
Vue.prototype.qs = qs

import Toast from './components/global/toast'
Vue.use(Toast)

import MsgBox from './components/global/msgbox'
Vue.use(MsgBox)

import VueScroller from 'vue-scroller'
Vue.use(VueScroller)

// import Vconsole from 'vconsole';
// new Vconsole();

// import CircleMenu from 'vue-circle-menu'
// Vue.component('CircleMenu', CircleMenu)

window.vm = new Vue({
  store,
  router,
  render: h => h(App),
}).$mount('#app')
