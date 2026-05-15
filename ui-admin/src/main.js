import Vue from 'vue'
import App from './App.vue'
import store from './config/store'
import router from './config/router'

import VueCropper from 'vue-cropper'
Vue.use(VueCropper)

import Toast from './components/global/toast'
Vue.use(Toast)

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(ElementUI)

import JwChat from 'jwchat';
Vue.use(JwChat)

import qs from 'qs'
Vue.prototype.qs = qs

import axios from './config/axios'
Vue.prototype.axios = axios

Vue.config.productionTip = true

import {drag} from './config/directives'
Vue.directive('drag', drag);

// import weui from 'weui.js'
// import 'weui'
// Vue.prototype.weui = weui


new Vue({
  router,
  store,
  render: h => h(App),
}).$mount('#app')
