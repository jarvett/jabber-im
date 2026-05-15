import Vue from 'vue'

import VueRouter from 'vue-router'

import adminList from './router.admin'

Vue.use(VueRouter)

const router = new VueRouter({
  mode: 'history',
  routes: [
    {
      path: '/index',
      name: 'index',
      meta: {title: '首页'},
      component: () => import('../views/index.vue')
    },
    ...adminList,
    {
      path: '*',
      redirect: {name: 'index'},
    },
  ]
});

router.beforeEach((to, from, next) => {
  /* 路由发生变化修改页面title */
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router
