import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 1,
  workers: process.env.CI ? 2 : 1,
  reporter: [
    ["html", { outputFolder: "playwright-report", open: "never" }],
    ["json", { outputFile: "test-results/results.json" }],
    ["junit", { outputFile: "test-results/results.xml" }],
    ["./tests/e2e/reporters/coverage-report.ts", { outputFile: "test-results/coverage-report.html" }],
  ],
  globalSetup: "./tests/e2e/setup/global-setup",
  globalTeardown: "./tests/e2e/setup/global-teardown",
  use: {
    baseURL: "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    actionTimeout: 15000,
    navigationTimeout: 30000,
  },
  projects: [
    {
      name: "smoke",
      testMatch: "**/*.spec.ts",
      grep: /@smoke/,
      retries: 1,
    },
    {
      name: "health",
      testMatch: "**/*.spec.ts",
      grep: /@health/,
      retries: 0,
    },
    {
      name: "chromium",
      testMatch: "**/*.spec.ts",
      grepInvert: /@smoke|@health/,
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  // Skip webServer since we're using Docker
  // Tests expect backend on :8080 and frontend on :3000
});
