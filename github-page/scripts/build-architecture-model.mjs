#!/usr/bin/env node
// Builds the interactive LikeC4 architecture site into out/architecture-model (#2483, ADR-033).
// Usage (from github-page/, after `npm run build` and `npm ci` in the model folder):  npm run build:model
// Hash routing: GitHub Pages serves only the root 404.html (the Next.js one), so a deep link such as
// /notaire/architecture-model/view/prod/ would 404; #/view/prod/ always loads index.html.
import { execFileSync } from "node:child_process";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const PAGE = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const MODEL = path.resolve(PAGE, "..", "docs", "200-architecture", "210-architecture-model");
const CLI = path.join(MODEL, "node_modules", ".bin", "likec4");
const OUT = path.join(PAGE, "out", "architecture-model");
const BASE = "/notaire/architecture-model/"; // the export is always served under the Pages basePath

if (!existsSync(CLI)) {
  console.error(`likec4 is not installed: run \`npm ci\` in ${path.relative(PAGE, MODEL)}`);
  process.exit(1);
}
execFileSync(
  CLI,
  ["build", "--base", BASE, "--use-hash-history", "--title", "Notaire architecture", "-o", OUT, "."],
  { cwd: MODEL, stdio: "inherit" },
);
if (!existsSync(path.join(OUT, "index.html"))) {
  console.error("LikeC4 build produced no index.html");
  process.exit(1);
}
console.log(`architecture model site: ${path.relative(PAGE, OUT)} (base ${BASE})`);
