import { createApp } from "vue";
import Embed from "./Embed.vue";
// 不安装 Pinia/Router，不导入项目全局 CSS，验证组件的独立接入。
createApp(Embed).mount("#embed");
