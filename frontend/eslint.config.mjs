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
  globalIgnores([
    ".next/**",
    "out/**",
    "build/**",
    "coverage/**",
    "next-env.d.ts",
  ]),
]);
