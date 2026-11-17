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
      // Re-measured 2026-11-17 (issue #2047):
      // Statements 89.15% / Branches 81.43% / Functions 87.99% / Lines 90.46%.
      // Floors sit ~2pp below measured coverage for headroom.
      // Only statements rise (86 → 87). A higher integer on the other three
      // would leave less than two points.
      thresholds: {
        statements: 87,
        branches: 79,
        functions: 85,
        lines: 88,
      },
    },
  },
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
  },
});
