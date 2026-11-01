#!/usr/bin/env node
/**
 * Records the API answers the demo replays (#1985).
 *
 * Run it against the real stack started from this repository: Flyway seeds the demo data, so
 * nothing here is typed by hand. It signs in, opens every page under frontend/src/app, and saves
 * each GET /api/v1/** response the browser received. Fields whose names look like credentials are
 * replaced before anything is written.
 *
 *   APP_URL=http://localhost:3000 DEMO_USER=admin DEMO_PASSWORD=admin node demo/record-fixtures.mjs
 */
import { readdirSync, statSync, writeFileSync } from "node:fs";
import { join, relative } from "node:path";
import { fileURLToPath } from "node:url";
import { chromium } from "@playwright/test";

const here = fileURLToPath(new URL(".", import.meta.url));
const APP_DIR = join(here, "..", "..", "frontend", "src", "app");
const APP = process.env.APP_URL ?? "http://localhost:3000";
const OUT = join(here, "fixtures.json");

/** Static pages of the product app, read from the App Router tree. */
export function appRoutes(dir = APP_DIR) {
  const routes = [];
  const walk = (d) => {
    for (const name of readdirSync(d)) {
      const p = join(d, name);
      if (!statSync(p).isDirectory()) continue;
      if (name === "api" || name.startsWith("[") || name.startsWith("(") || name.startsWith("_")) continue;
      walk(p);
    }
    if (readdirSync(d).includes("page.tsx")) routes.push("/" + relative(dir, d).split("\\").join("/"));
  };
  walk(dir);
  return routes.map((r) => (r === "/" ? r : r.replace(/\/$/, ""))).filter((r) => r !== "/" && r !== "/login").sort();
}

const SECRET_KEY = /pass(word)?|token|secret|hash|jwt|credential/i;
export function scrub(value) {
  if (Array.isArray(value)) return value.map(scrub);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.entries(value).map(([k, v]) => [k, SECRET_KEY.test(k) && typeof v === "string" ? "demo" : scrub(v)]));
  }
  return value;
}

async function main() {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: "en-US" });
  await ctx.addCookies([{ name: "NEXT_LOCALE", value: "en", url: APP }]);
  const page = await ctx.newPage();
  const responses = {};
  page.on("response", async (res) => {
    const url = new URL(res.url());
    if (!url.pathname.startsWith("/api/v1/")) return;
    const method = res.request().method();
    const key = `${method} ${url.pathname.slice("/api/v1".length)}${url.search}`;
    if (method !== "GET" && key !== "POST /users/login") return;
    const type = res.headers()["content-type"] ?? "";
    if (!type.includes("json")) return;
    try {
      responses[key] = { status: res.status(), body: scrub(await res.json()) };
    } catch {
      /* body already gone (navigation) */
    }
  });

  await page.goto(`${APP}/login`, { waitUntil: "networkidle" });
  const inputs = page.locator("input");
  await inputs.nth(0).fill(process.env.DEMO_USER ?? "admin");
  await inputs.nth(1).fill(process.env.DEMO_PASSWORD ?? "admin");
  await page.keyboard.press("Enter");
  await page.waitForURL(/dashboard/, { timeout: 20000 });

  const routes = appRoutes();
  for (const r of routes) {
    await page.goto(`${APP}${r}`, { waitUntil: "networkidle" }).catch(() => {});
    await page.waitForTimeout(1500);
  }
  const workflows = responses["GET /workflow-definition"]?.body;
  const workflowIds = Array.isArray(workflows) ? workflows.map((w) => String(w.id)).filter(Boolean) : [];
  for (const id of workflowIds) {
    await page.goto(`${APP}/dashboard/administration/workflows/${id}`, { waitUntil: "networkidle" }).catch(() => {});
    await page.waitForTimeout(1500);
  }
  await browser.close();

  const sorted = Object.fromEntries(Object.entries(responses).sort(([a], [b]) => a.localeCompare(b)));
  const doc = {
    about: "API answers replayed by the Pages demo (#1985). Recorded by github-page/demo/record-fixtures.mjs from the local stack seeded by Flyway.",
    recordedAt: new Date().toISOString().slice(0, 10),
    routes,
    workflowIds,
    responses: sorted,
  };
  writeFileSync(OUT, JSON.stringify(doc, null, 1) + "\n");
  console.log(`record-fixtures: ${Object.keys(sorted).length} responses from ${routes.length} pages -> ${relative(process.cwd(), OUT)}`);
}

if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) await main();
