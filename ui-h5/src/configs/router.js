import Vue from 'vue'
import VueRouter from 'vue-router'
Vue.use(VueRouter)

const routes = [
    {
        path: '/index',
        name: 'index',
        meta: {
            title: '首页'
        },
        redirect: {
            name: 'chat'
        },
        component: () => import('../views/index.vue'),
        children:[
            {
                path: '/chat',
                name: 'chat',
                meta: {
                    title: '聊天'
                },
                component: () => import('../views/chat.vue')
            },
            {
                path: '/profile',
                name: 'profile',
                meta: {
                    title: '我的'
                },
                component: () => import('../views/profile.vue')
            },
        ]
    },
    {
        path: '/stranger',
        name: 'stranger',
        meta: {
            title: '随便聊聊'
        },
        component: () => import('../views/stranger.vue')
    },
    {
      path: '/group',
      name: 'group',
      meta: {
          title: '发起群聊'
      },
      component: () => import('../views/group.vue')
  },
    {
        path: '/login',
        name: 'login',
        meta: {
            title: '登录'
        },
        component: () => import('../views/login.vue')
    },
    {
        path: '/chatbox',
        name: 'chatbox',
        meta: {
            title: '聊天'
        },
        component: () => import('../views/chatbox.vue')
    },
    {
        path: '*',
        redirect: {
            name: 'chat'
        },
    },
]

const router = new VueRouter({
    routes,
    mode: 'history'
})

//防止重复点击导航栏报错
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err);
};

router.beforeEach((to, from, next) => {
    /* 路由发生变化修改页面title */
    if (to.meta.title) {
        document.title = to.meta.title
    }
    next()
})

export default router
