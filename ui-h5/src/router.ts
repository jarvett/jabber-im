import { createRouter, createWebHistory } from "vue-router";
import { useSession } from "./store";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      component: () => import("./views/LoginView.vue"),
      meta: { title: "登录" },
    },
    {
      path: "/chat",
      component: () => import("./views/ChatView.vue"),
      meta: { title: "聊天" },
    },
    {
      path: "/chatbox",
      component: () => import("./views/ChatView.vue"),
      meta: { title: "聊天" },
    },
    {
      path: "/profile",
      component: () => import("./views/ProfileView.vue"),
      meta: { title: "我的" },
    },
    {
      path: "/stranger",
      component: () => import("./views/PeopleView.vue"),
      meta: { title: "随便聊聊" },
    },
    {
      path: "/group",
      component: () => import("./views/PeopleView.vue"),
      meta: { title: "发起群聊" },
    },
    { path: "/index", redirect: "/chat" },
    { path: "/:pathMatch(.*)*", redirect: "/chat" },
  ],
});
router.beforeEach((to) => {
  document.title = String(to.meta.title || "Jabber 聊天");
  if (to.path !== "/login" && !useSession().token) return { path: "/login" };
});
