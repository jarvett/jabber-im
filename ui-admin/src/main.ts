import { createApp } from "vue";
import { createPinia } from "pinia";
import { router } from "./router";
import App from "./App.vue";
import "./style.css";
import "element-plus/es/components/input/style/css";
import "element-plus/es/components/button/style/css";

createApp(App).use(createPinia()).use(router).mount("#app");
