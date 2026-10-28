// Internal link checker for the static export (#1970).
// Every same-site href in out/**/*.html must resolve to a file, and every #fragment
// must match an id on the target page. Report folders copied in by the deploy
// workflow (coverage, playwright, cicd-reports) are allowed to be absent locally.
import { readFileSync, readdirSync, existsSync, statSync } from "node:fs";
import path from "node:path";

const OUT = path.resolve(process.argv[2] || "out");
const BASE = "/notaire";
const DEPLOY_ONLY = ["/coverage/", "/playwright/", "/cicd-reports/"];

function walk(dir) {
  return readdirSync(dir).flatMap((n) => {
    const f = path.join(dir, n);
    return statSync(f).isDirectory() ? walk(f) : f.endsWith(".html") ? [f] : [];
  });
}
const ids = new Map();
function idsOf(file) {
  if (!ids.has(file)) ids.set(file, new Set([...readFileSync(file, "utf8").matchAll(/\sid="([^"]+)"/g)].map((m) => m[1])));
  return ids.get(file);
}
function target(urlPath) {
  let f = path.join(OUT, urlPath);
  if (existsSync(f) && statSync(f).isDirectory()) f = path.join(f, "index.html");
  return existsSync(f) ? f : null;
}

const pages = walk(OUT);
const broken = [];
let checked = 0;
for (const page of pages) {
  const html = readFileSync(page, "utf8");
  const here = "/" + path.relative(OUT, page).replace(/index\.html$/, "");
  for (const m of html.matchAll(/<a\b[^>]*\shref="([^"]+)"/g)) {
    const href = m[1].replace(/&amp;/g, "&");
    if (/^(https?:|mailto:|tel:)/.test(href)) continue;
    let [p, frag] = href.split("#");
    if (p === "") p = BASE + here;
    if (!p.startsWith(BASE + "/") && p !== BASE) {
      broken.push(`${here}: ${href} (not under ${BASE})`);
      continue;
    }
    const rel = p.slice(BASE.length) || "/";
    checked += 1;
    if (DEPLOY_ONLY.some((d) => rel.startsWith(d))) continue;
    const file = target(rel);
    if (!file) broken.push(`${here}: ${href} (no such page)`);
    else if (frag && !idsOf(file).has(frag)) broken.push(`${here}: ${href} (no #${frag} on the page)`);
  }
}
if (broken.length) {
  console.error(`check-links: ${broken.length} broken of ${checked} internal links`);
  for (const b of [...new Set(broken)]) console.error("  " + b);
  process.exit(1);
}
console.log(`check-links: ${checked} internal links across ${pages.length} pages, all resolve`);
