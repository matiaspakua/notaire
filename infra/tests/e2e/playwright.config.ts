/**
 * Playwright E2E test configuration for Notaire full-stack tests
 *
 * These tests run against the running Docker stack:
 *   Frontend: http://localhost:3000
 *   Backend:  http://localhost:8080/api/v1
 *   Database: PostgreSQL on localhost:5432
 *
 * Run: npx playwright test --config=infra/tests/e2e/playwright.config.ts
 */
import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: ".",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1,
  workers: 1,
  reporter: [
    ["html", { outputFolder: "../../reports/playwright" }],
    ["list"],
  ],
  timeout: 60000,
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    actionTimeout: 15000,
    navigationTimeout: 30000,
    ...(process.env.CHROME_BIN ? { channel: "chrome" } : {}),
  },
  // Tests expect Docker stack running (postgres + backend + frontend)
});
