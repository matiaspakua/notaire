import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: "html",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
    // Use system Chrome if CHROME_BIN is set
    ...(process.env.CHROME_BIN ? { channel: "chrome" } : {}),
  },
  // Skip webServer since we're using Docker
  // Tests expect backend on :8080 and frontend on :3000
});
