import { defineConfig, devices } from "@playwright/test";

const isHeaded = !process.env.CI && process.env.HEADED === "1";
const slowMo = isHeaded ? Number(process.env.SLOW_MO ?? 600) : 0;

export default defineConfig({
  testDir: "./tests",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  // #1066 / CU76: at most one CI retry so flakes stay visible; triage via on-first-retry traces.
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 2 : 2,
  // Default hang budget; demo/tutorial specs override with test.describe.configure({ timeout }).
  timeout: 90000,
  reporter: [
    ["html", { outputFolder: "playwright-report", open: "never" }],
    ["json", { outputFile: "test-results/results.json" }],
    ["junit", { outputFile: "test-results/results.xml" }],
    ["./tests/reporters/coverage-report.ts", { outputFile: "test-results/coverage-report.html" }],
  ],
  globalSetup: "./tests/setup/global-setup",
  globalTeardown: "./tests/setup/global-teardown",
  use: {
    baseURL: process.env.BASE_URL || "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
    actionTimeout: 15000,
    navigationTimeout: 30000,
    launchOptions: { slowMo },
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
    {
      name: "mobile",
      testMatch: "**/mobile-viewport.spec.ts",
      use: { ...devices["iPhone SE"] },
    },
  ],
  // Skip webServer since we're using Docker
  // Tests expect backend on :8080 and frontend on :3000
});
