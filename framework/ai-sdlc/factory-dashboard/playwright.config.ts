import { defineConfig, devices } from "@playwright/test";

const factory = `${import.meta.dirname}/.e2e-factory`;

export default defineConfig({
  testDir: "tests/e2e",
  timeout: 30_000,
  fullyParallel: false,
  workers: 1,
  reporter: [["list"]],
  use: { baseURL: "http://127.0.0.1:18866", trace: "retain-on-failure" },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: [
    {
      command: "bash tests/e2e/start-kit.sh",
      // Playwright counts the 401 of an unauthenticated request as up.
      url: "http://127.0.0.1:18865/factory/capabilities",
      env: { E2E_FACTORY_DIR: factory, E2E_KIT_PORT: "18865" },
      reuseExistingServer: false,
      timeout: 60_000,
    },
    {
      command: "pnpm build && tsx src/server/main.ts",
      url: "http://127.0.0.1:18866/api/workspaces",
      env: { FACTORY_DASHBOARD_CONFIG: "tests/e2e/dashboard.e2e.json" },
      reuseExistingServer: false,
      timeout: 120_000,
    },
  ],
});
