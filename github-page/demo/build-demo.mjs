#!/usr/bin/env node
// Builds the read-only demo (#1985): the real Notaire frontend, exported statically under
// /notaire/demo/ and answered by recorded fixtures instead of the backend. See docs/demo.md.
//
// Usage (from github-page/, after `npm run build` and `npm ci` in ../frontend):
//   npm run build:demo
//
// It copies ../frontend into .demo-build/ (frontend/ itself is never modified), applies a few
// patches that a static export needs, builds, and copies the export to out/demo/. Every patch
// checks the text it replaces and fails the build if the frontend changed underneath it, so the
// demo breaks loudly rather than shipping half-working.
import { cpSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { execFileSync } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const PAGE = path.resolve(HERE, "..");
const FRONTEND = path.resolve(PAGE, "..", "frontend");
const WORK = path.join(PAGE, ".demo-build");
const OUT = path.join(PAGE, "out", "demo");
const BASE = "/notaire/demo";
const BANNER = "Demo – sample data, read-only";
const BANNER_ES = "Demo: datos de ejemplo, solo lectura";
const DEMO_LANG = `(function(){try{var raw=localStorage.getItem("notaire.lang");var es=raw==="es"||(raw!=="en"&&String(navigator.language||"").toLowerCase().indexOf("es")===0);document.querySelectorAll("[data-demo-en]").forEach(function(el){el.hidden=es});document.querySelectorAll("[data-demo-es]").forEach(function(el){el.hidden=!es});var box=document.querySelector("[data-demo-banner]");if(box)box.setAttribute("aria-label",es?"Aviso de la demo":"Demo notice");}catch(e){}})();`;

// Inlined, so it runs while the HTML is parsed, before any async app chunk can call fetch.
const SHIM = `window.__NOTAIRE_DEMO_BASE__=${JSON.stringify(BASE + "/")};\n` + readFileSync(path.join(HERE, "demo-shim.js"), "utf8");
const fixtures = JSON.parse(readFileSync(path.join(HERE, "fixtures.json"), "utf8"));
if (!existsSync(path.join(FRONTEND, "node_modules", "next"))) {
  throw new Error("build-demo: ../frontend/node_modules is missing; run `npm ci` in frontend/ first");
}
if (!existsSync(path.join(PAGE, "out", "index.html"))) {
  throw new Error("build-demo: out/ is missing; run `npm run build` first (the demo is added into its export)");
}

// 1. Copy the frontend without build output, tests, the API proxy route or the edge proxy.
const SKIP = new Set(["node_modules", ".next", "out", "coverage", "test-results", "playwright-report"]);
const DROP = ["src/app/api", "src/proxy.ts", "src/tests"];
rmSync(WORK, { recursive: true, force: true });
cpSync(FRONTEND, WORK, {
  recursive: true,
  filter: (src) => {
    const rel = path.relative(FRONTEND, src);
    const first = rel.split(path.sep)[0];
    if (SKIP.has(first)) return false;
    if (DROP.some((d) => rel === d || rel.startsWith(d + path.sep))) return false;
    return !/\.(test|spec)\.[cm]?[jt]sx?$/.test(rel);
  },
});
// Turbopack does not resolve packages through a symlinked node_modules; hard links are instant.
try {
  execFileSync("cp", ["-al", path.join(FRONTEND, "node_modules"), path.join(WORK, "node_modules")]);
} catch {
  cpSync(path.join(FRONTEND, "node_modules"), path.join(WORK, "node_modules"), { recursive: true });
}

// 2. Patches, each asserted.
function patch(rel, edits) {
  const file = path.join(WORK, rel);
  let text = readFileSync(file, "utf8");
  for (const [from, to] of edits) {
    if (!text.includes(from)) throw new Error(`build-demo: patch for ${rel} no longer applies (missing: ${JSON.stringify(from)})`);
    text = text.replace(from, to);
  }
  writeFileSync(file, text);
}
function replaceFile(rel, mustContain, content) {
  const file = path.join(WORK, rel);
  const text = readFileSync(file, "utf8");
  for (const s of mustContain) {
    if (!text.includes(s)) throw new Error(`build-demo: patch for ${rel} no longer applies (missing: ${JSON.stringify(s)})`);
  }
  writeFileSync(file, content);
}

// A static export has no request: fixed English locale instead of the NEXT_LOCALE cookie.
replaceFile("src/i18n/request.ts", ["getRequestConfig", "cookies()", "export function isSupportedLocale"], `import { getRequestConfig } from "next-intl/server";
// Pages demo (#1985): no request at build time, so the locale is fixed.
const SUPPORTED_LOCALES = ["es", "en"] as const;
export type Locale = (typeof SUPPORTED_LOCALES)[number];
export const DEFAULT_LOCALE: Locale = "en";
export function isSupportedLocale(locale: string): locale is Locale {
  return SUPPORTED_LOCALES.includes(locale as Locale);
}
export default getRequestConfig(async () => ({ locale: DEFAULT_LOCALE, messages: (await import("../../messages/en.json")).default }));
`);
// Server actions cannot run in a static export.
replaceFile("src/i18n/actions.ts", ["export async function setLocale"], `// Pages demo (#1985): no server, so the language switch is a no-op.
export async function setLocale(_locale: string): Promise<void> {}
`);
const recorded = String(fixtures.recordedAt).slice(0, 10);
patch("src/app/layout.tsx", [
  ['import { headers } from "next/headers";\n', ""],
  ['(await headers()).get("x-nonce") ?? undefined', "undefined"],
  ['title: "Notaire — Notary Management System"', 'title: "Notaire demo — sample data, read-only"'],
  ["<html lang={locale}>", `<html lang={locale}>\n      <head>\n        <script dangerouslySetInnerHTML={{ __html: ${JSON.stringify(SHIM)} }} />\n      </head>`],
  ["{children}", `{children}
            <div aria-hidden="true" style={{ height: "4.5rem" }} />
            <div
              role="region"
              aria-label="Demo notice"
              data-demo-banner=""
              style={{ position: "fixed", insetInline: 0, bottom: 0, zIndex: 60, background: "#0f172a", color: "#f8fafc", padding: "0.6rem 1rem", fontSize: "0.875rem", lineHeight: 1.4, textAlign: "center", boxShadow: "0 -2px 8px rgb(0 0 0 / 0.2)" }}
            >
              <script dangerouslySetInnerHTML={{ __html: ${JSON.stringify(DEMO_LANG)} }} />
              <strong data-demo-en="">${BANNER}.</strong>
              <strong data-demo-es="" hidden>${BANNER_ES}.</strong>{" "}
              <span data-demo-en="">Sign in with any name and password; nothing you change is saved. Data recorded ${recorded}.</span>
              <span data-demo-es="" hidden>Entra con cualquier nombre y contraseña. Nada de lo que cambies se guarda. Datos grabados ${recorded}.</span>{" "}
              <a data-demo-en="" href="/notaire/" style={{ color: "#fcd34d", textDecoration: "underline" }}>Back to the project site</a>
              <a data-demo-es="" href="/notaire/" hidden style={{ color: "#fcd34d", textDecoration: "underline" }}>Volver al sitio del proyecto</a>
            </div>`],
]);
replaceFile("next.config.ts", ['output: "standalone"'], `import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";
// Pages demo (#1985): static export under the Pages base path; no redirects or headers (no server).
const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");
const nextConfig: NextConfig = { output: "export", basePath: "${BASE}", trailingSlash: true, images: { unoptimized: true } };
export default withNextIntl(nextConfig);
`);
// The root page is a server redirect(); exported, it renders no <head>, so the inlined shim never runs
// and the client-side hop to /login keeps an unshimmed window. Use a full-page redirect instead.
replaceFile("src/app/page.tsx", ['redirect("/login")'], `"use client";
// Pages demo (#1985): full-page redirect so the login page loads with the demo shim.
import { useEffect } from "react";
export default function RootPage() {
  useEffect(() => { window.location.replace("${BASE}/login/"); }, []);
  return <p style={{ padding: 24 }}><a href="${BASE}/login/">Open the demo</a></p>;
}
`);
// The one dynamic route: export a page per recorded workflow.
const dyn = path.join(WORK, "src/app/dashboard/administration/workflows/[id]");
if (!existsSync(path.join(dyn, "page.tsx")) || existsSync(path.join(dyn, "layout.tsx"))) {
  throw new Error("build-demo: patch for workflows/[id] no longer applies (expected page.tsx and no layout.tsx)");
}
const ids = (fixtures.workflowIds || []).map((id) => ({ id: String(id) }));
writeFileSync(path.join(dyn, "layout.tsx"), `// Pages demo (#1985): one static page per recorded workflow.
export function generateStaticParams() { return ${JSON.stringify(ids.length ? ids : [{ id: "1" }])}; }
export const dynamicParams = false;
export default function Layout({ children }: { children: React.ReactNode }) { return children; }
`);

// 3. The shim and its data ship as static files.
mkdirSync(path.join(WORK, "public"), { recursive: true });
cpSync(path.join(HERE, "demo-shim.js"), path.join(WORK, "public", "demo-shim.js"));
writeFileSync(path.join(WORK, "public", "demo-fixtures.json"), JSON.stringify({ recordedAt: fixtures.recordedAt, responses: fixtures.responses }));

// 4. Build and add to the Pages export.
execFileSync("npx", ["next", "build"], {
  cwd: WORK,
  stdio: "inherit",
  env: { ...process.env, NODE_ENV: "production", NEXT_TELEMETRY_DISABLED: "1" },
});
rmSync(OUT, { recursive: true, force: true });
cpSync(path.join(WORK, "out"), OUT, { recursive: true });
console.log(`build-demo: exported the frontend to ${path.relative(PAGE, OUT)}/ (${BANNER})`);
