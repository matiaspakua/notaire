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
    exclude: ["node_modules/**"],
    coverage: {
      provider: "v8",
      // json-summary produces coverage/coverage-summary.json, which the
      // GitHub Pages metrics pipeline reads for a real frontend coverage
      // number instead of a hardcoded constant (issue #758).
      reporter: ["text", "html", "lcov", "json-summary"],
      reportsDirectory: "./coverage",
      include: ["src/**/*.{ts,tsx}"],
      exclude: [
        "src/tests/**",
        "src/**/*.d.ts",
        "src/app/globals.css",
        "src/app/layout.tsx",
        "src/app/providers.tsx",
      ],
      // Enforced raise-only ratchet floor (mirrors backend JaCoCo; issue #976):
      // raise these as coverage improves, never lower them without ADR/exception.
      // Re-measured 2026-10-13 on origin/main @ 38d5d93 (issue #1663):
      // Statements 52.07% / Branches 48.85% / Functions 46.22% / Lines 52.39%.
      // Floors sit ~2pp below measured coverage for headroom.
      thresholds: {
        statements: 50,
        branches: 46,
        functions: 44,
        lines: 50,
      },
    },
  },
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
  },
});
