import { defineConfig, globalIgnores } from "eslint/config";
import nextConfig from "eslint-config-next/core-web-vitals";
import nextTypescript from "eslint-config-next/typescript";

export default defineConfig([
  ...nextConfig,
  ...nextTypescript,
  globalIgnores([".next/**", "out/**", "node_modules/**", "playwright-report/**", "test-results/**", "next-env.d.ts"]),
]);
