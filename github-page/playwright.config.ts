import { defineConfig, devices } from "@playwright/test";

/** Smoke suite for the static export (#1970): build first (`npm run build`), then `npm run test:smoke`. */
export default defineConfig({
  testDir: "e2e",
  timeout: 60_000,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [["list"], ["html", { open: "never", outputFolder: "playwright-report" }]] : "list",
  use: { baseURL: "http://localhost:4173/notaire/", trace: "retain-on-failure" },
  webServer: { command: "node scripts/serve-out.mjs out", url: "http://localhost:4173/notaire/", reuseExistingServer: !process.env.CI },
  projects: [
    { name: "mobile", use: { ...devices["Pixel 7"], viewport: { width: 375, height: 800 } } },
    { name: "desktop", use: { ...devices["Desktop Chrome"], viewport: { width: 1280, height: 900 } } },
  ],
});
