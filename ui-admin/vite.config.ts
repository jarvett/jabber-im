import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  // 显式预构建依赖，避免路由懒加载触发并发缓存重建。
  optimizeDeps: {
    include: ["vue", "vue-router", "pinia", "@vueuse/core", "element-plus"],
    noDiscovery: true,
  },
  plugins: [vue(), tailwindcss()],
  server: {
    host: "0.0.0.0",
    port: 18890,
    strictPort: true, // 端口冲突时退出，避免静默切换访问地址。
    proxy: {
      "/im": { target: "http://127.0.0.1:18888", ws: true, changeOrigin: true },
    },
  },
});
