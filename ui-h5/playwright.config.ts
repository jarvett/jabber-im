import { defineConfig } from "@playwright/test";
import { existsSync } from "node:fs";
const edge = "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe";
export default defineConfig({
  testDir: "./tests",
  fullyParallel: false,
  use: {
    baseURL: "http://127.0.0.1:18889",
    viewport: { width: 390, height: 844 },
    launchOptions: { executablePath: existsSync(edge) ? edge : undefined },
  },
  webServer: {
    command: "pnpm dev",
    url: "http://127.0.0.1:18889",
    reuseExistingServer: !process.env.CI,
  },
});
