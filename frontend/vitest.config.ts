import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import { resolve } from "path";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: ["./src/tests/setup.ts"],
    globals: true,
    include: ["src/**/*.{test,spec}.{ts,tsx}"],
    exclude: ["tests/e2e/**", "node_modules/**"],
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "lcov"],
      reportsDirectory: "./coverage",
      include: ["src/**/*.{ts,tsx}"],
      exclude: [
        "src/tests/**",
        "src/**/*.d.ts",
        "src/app/globals.css",
        "src/app/layout.tsx",
        "src/app/providers.tsx",
      ],
      // Enforced ratchet floor (see backend-api's JaCoCo gate for the same pattern):
      // raise these as coverage improves, never lower them. Set below the actual
      // measured coverage at introduction time (2026-07-29) to leave headroom.
      thresholds: {
        statements: 10,
        branches: 6,
        functions: 9,
        lines: 10,
      },
    },
  },
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
  },
});
