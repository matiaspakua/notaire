import { defineConfig, globalIgnores } from "eslint/config";
import nextConfig from "eslint-config-next/core-web-vitals";
import nextTypescript from "eslint-config-next/typescript";

export default defineConfig([
  ...nextConfig,
  ...nextTypescript,
  {
    // Session expiry runs outside React (API interceptors) and intentionally
    // does a full reload to drop in-memory client state; no router there.
    files: ["src/lib/session-expiry.ts"],
    rules: { "@next/next/no-location-assign-relative-destination": "off" },
  },
  {
    // Hexagonal boundaries (ADR-030, #1696): the UI never talks to the API directly.
    files: ["src/app/**", "src/components/**", "src/hooks/**"],
    rules: {
      "no-restricted-globals": ["error", { name: "fetch", message: "UI code calls use cases, not fetch (ADR-030)." }],
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/lib/api-client", "**/lib/api-client"],
              message: "UI code calls use cases, not the API client (ADR-030). Only adapters/http may import it.",
            },
          ],
        },
      ],
    },
  },
  {
    // The core is pure TypeScript: no framework, no adapters, no transport.
    files: ["src/core/**"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["react", "react-dom", "react/*", "next", "next/*", "@tanstack/*", "@/adapters/*", "@/lib/*", "@/app/*", "@/components/*", "@/hooks/*"],
              message: "core/** depends on nothing but itself (ADR-030).",
            },
          ],
        },
      ],
      "no-restricted-globals": ["error", "fetch", "window", "document", "localStorage"],
    },
  },
  globalIgnores([
    ".next/**",
    "out/**",
    "build/**",
    "coverage/**",
    "next-env.d.ts",
  ]),
]);
