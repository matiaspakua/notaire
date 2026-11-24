#!/usr/bin/env node
// Generates every copy of the architecture views from the LikeC4 model (#2483, ADR-033):
//   generated/mermaid/<view>.mmd                 one Mermaid file per view
//   github-page/lib/architecture-model.json      views and findings for GitHub Pages
//   <!-- likec4-view: <id> --> blocks            in the Markdown docs under docs/
//
// Usage (from this folder):  npm run generate   |   npm run check  (exit 1 when a copy drifted)
import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

import { LikeC4 } from "likec4";

import { toMermaid } from "./mermaid.mjs";

const HERE = path.dirname(fileURLToPath(import.meta.url));
const MODEL = path.resolve(HERE, "..");
const ROOT = path.resolve(MODEL, "..", "..", "..");
const MERMAID_DIR = path.join(MODEL, "generated", "mermaid");
const PAGES_DATA = path.join(ROOT, "github-page", "lib", "architecture-model.json");
const DOCS = path.join(ROOT, "docs");
const SKIP_DIRS = new Set(["000-archive", "openspec", "node_modules", "210-architecture-model"]);
const BLOCK = /<!-- likec4-view: (\S+) -->\n```mermaid\n[\s\S]*?```\n<!-- \/likec4-view -->/g;

const check = process.argv.includes("--check");
const drifted = [];

function emit(file, content) {
  const current = existsSync(file) ? readFileSync(file, "utf8") : null;
  if (current === content) return;
  if (check) {
    drifted.push(path.relative(ROOT, file));
    return;
  }
  mkdirSync(path.dirname(file), { recursive: true });
  writeFileSync(file, content);
  console.log(`wrote ${path.relative(ROOT, file)}`);
}

function text(value) {
  if (!value) return "";
  return typeof value === "string" ? value : (value.txt ?? value.md ?? "");
}

function depth(fqn) {
  return fqn.split(".").length;
}

async function loadModel() {
  const likec4 = await LikeC4.fromWorkspace(MODEL, { logger: false });
  if (likec4.hasErrors()) {
    likec4.printErrors();
    process.exit(1);
  }
  return likec4.computedModel();
}

function relationResolver(model) {
  const find = (id) => {
    try {
      return model.relationship(id);
    } catch {
      return null;
    }
  };
  return {
    title: (id) => find(id)?.title ?? null,
    // The relation drawn between the shallowest elements names a merged edge best.
    order: (ids) =>
      [...ids].sort((a, b) => {
        const ra = find(a);
        const rb = find(b);
        if (!ra || !rb) return 0;
        return depth(ra.source.id) + depth(ra.target.id) - (depth(rb.source.id) + depth(rb.target.id));
      }),
  };
}

function viewsOf(model) {
  const resolver = relationResolver(model);
  return [...model.views()].map((v) => {
    const view = v.$view;
    const edges = view.edges.map((e) => ({ ...e, relations: resolver.order(e.relations ?? []) }));
    return {
      id: view.id,
      title: view.title ?? view.id,
      description: text(view.description),
      type: view._type,
      mermaid: toMermaid({ ...view, edges }, { relationTitle: resolver.title }),
    };
  });
}

function finding(item, kind, title) {
  return {
    kind,
    id: item.id,
    title,
    description: text(item.$element?.description ?? item.$relationship?.description ?? item.$node?.description),
    issue: item.links?.[0]?.url ?? "",
  };
}

function findingsOf(model) {
  const elements = [...model.elements()]
    .filter((e) => e.tags.includes("finding"))
    .map((e) => finding(e, "element", e.title));
  const relations = [...model.relationships()]
    .filter((r) => r.tags.includes("finding"))
    // LikeC4 relationship ids hash the source location, so they change with the checkout path; build a stable one.
    .map((r) => ({
      ...finding(r, "relationship", `${r.source.title} → ${r.target.title}: ${r.title}`),
      id: `${r.source.id} -> ${r.target.id}: ${r.title}`,
    }));
  const nodes = [...model.deployment.elements()]
    .filter((n) => n.tags.includes("finding"))
    .map((n) => ({ ...finding(n, "deployment", n.title), description: text(n.$node?.description ?? n.description) }));
  return [...elements, ...relations, ...nodes];
}

function markdownFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) return SKIP_DIRS.has(entry.name) ? [] : markdownFiles(full);
    return entry.name.endsWith(".md") ? [full] : [];
  });
}

function writeDocs(views) {
  const byId = new Map(views.map((v) => [v.id, v]));
  for (const file of markdownFiles(DOCS)) {
    const source = readFileSync(file, "utf8");
    if (!source.includes("<!-- likec4-view:")) continue;
    const updated = source.replace(BLOCK, (block, id) => {
      const view = byId.get(id);
      if (!view) throw new Error(`${path.relative(ROOT, file)} embeds unknown view '${id}'`);
      return `<!-- likec4-view: ${id} -->\n\`\`\`mermaid\n${view.mermaid}\`\`\`\n<!-- /likec4-view -->`;
    });
    emit(file, updated);
  }
}

function writeMermaid(views) {
  const expected = new Set(views.map((v) => `${v.id}.mmd`));
  if (existsSync(MERMAID_DIR)) {
    for (const name of readdirSync(MERMAID_DIR)) {
      if (expected.has(name)) continue;
      if (check) drifted.push(`stale ${name}`);
      else rmSync(path.join(MERMAID_DIR, name));
    }
  }
  for (const view of views) emit(path.join(MERMAID_DIR, `${view.id}.mmd`), view.mermaid);
}

const model = await loadModel();
const views = viewsOf(model);
const version = JSON.parse(readFileSync(path.join(MODEL, "package.json"), "utf8")).devDependencies.likec4;

writeMermaid(views);
emit(PAGES_DATA, JSON.stringify({ likec4: version, views, findings: findingsOf(model) }, null, 2) + "\n");
writeDocs(views);

if (drifted.length) {
  console.error(`Generated architecture views are out of date (run npm run generate):\n  ${drifted.join("\n  ")}`);
  process.exit(1);
}
if (check) console.log(`${views.length} views in sync`);
