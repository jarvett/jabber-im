export default [
  {
    path: '/login',
    name: 'login',
    meta: {title: '登录'},
    component: () => import('../views/admin/login.vue')
  },
  {
    path: '/chat',
    name: 'chat',
    meta: {title: '聊天'},
    component: () => import('../views/admin/chat.vue')
  },
]
