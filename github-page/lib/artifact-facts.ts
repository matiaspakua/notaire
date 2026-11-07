/**
 * Build-time counts for the Engineering artifacts page (#2067).
 * Every number the page and the artifact cabinet figure print is read here, from the repository,
 * reusing the Technical docs readers (lib/tech-facts.ts) where one already exists,
 * when `next build` runs (CONSTITUTION §7). A folder or file that is missing yields null, and the
 * page prints "—" instead of a guess.
 */
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import path from "node:path";
import { bruno, migrations } from "./tech-facts";

const ROOT = path.resolve(process.cwd(), "..");

function at(rel: string): string {
  return path.join(ROOT, rel);
}

function text(rel: string): string | null {
  const file = at(rel);
  return existsSync(file) ? readFileSync(file, "utf-8") : null;
}

/** Files under `rel` (recursive, skipping node_modules and dot folders) whose name passes `test`. */
function files(rel: string, test: (name: string) => boolean): number | null {
  const dir = at(rel);
  if (!existsSync(dir)) return null;
  let total = 0;
  for (const name of readdirSync(dir)) {
    if (name === "node_modules" || name.startsWith(".")) continue;
    const full = path.join(dir, name);
    if (statSync(full).isDirectory()) total += files(path.join(rel, name), test) ?? 0;
    else if (test(name)) total += 1;
  }
  return total;
}

function dirsWith(rel: string, marker: string): number | null {
  const dir = at(rel);
  if (!existsSync(dir)) return null;
  return readdirSync(dir).filter((name) => existsSync(path.join(dir, name, marker))).length;
}

/** Occurrences of `pattern` in every Markdown file under `rel`. */
function occurrences(rel: string, pattern: RegExp): number | null {
  const dir = at(rel);
  if (!existsSync(dir)) return null;
  let total = 0;
  const visit = (sub: string) => {
    for (const name of readdirSync(at(sub))) {
      if (name === "node_modules" || name.startsWith(".")) continue;
      const child = path.join(sub, name);
      if (statSync(at(child)).isDirectory()) visit(child);
      else if (name.endsWith(".md")) total += (readFileSync(at(child), "utf-8").match(pattern) ?? []).length;
    }
  };
  visit(rel);
  return total;
}

/** Data rows of a CSV whose first column is an issue id such as `#12`. */
function requirementRows(rel: string): { total: number; functional: number } | null {
  const body = text(rel);
  if (body === null) return null;
  const rows = body.split(/\r?\n/).slice(1).filter((line) => /^#\d+,/.test(line));
  return { total: rows.length, functional: rows.filter((line) => line.includes(",Requerimientos Funcionales,")).length };
}

function matches(rel: string, pattern: RegExp): number | null {
  const body = text(rel);
  return body === null ? null : (body.match(pattern) ?? []).length;
}

function distinct(rel: string, pattern: RegExp): number | null {
  const body = text(rel);
  return body === null ? null : new Set(body.match(pattern) ?? []).size;
}

export const PATHS = {
  requirementsCsv: "docs/100-business/101-requirements/requerimientos.csv",
  useCases: "docs/100-business/102-use-cases",
  manuals: "docs/100-business/105-manuals",
  design: "docs/200-architecture/203-design",
  diagrams: "docs/200-architecture/204-diagrams",
  architectureDocs: "docs/200-architecture",
  sad: "docs/200-architecture/201-SAD/sad.md",
  adrs: "docs/200-architecture/202-ADR",
  specs: "docs/openspec/specs",
  archive: "docs/openspec/changes/archive",
  migrations: "backend-api/src/main/resources/db/migration",
  backendTests: "backend-api/src/test",
  testCases: "docs/300-development/303-testing/test-cases",
  e2e: "testing/e2e",
  bruno: "backend-api/api-test",
  strideRegister: "docs/200-architecture/206-security/threat-model/system.stride.yml",
  authThreatModel: "docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md",
  securityCatalog: "security/cases/catalog.yml",
  workflows: ".github/workflows",
} as const;

export type ArtifactFacts = {
  requirements: number | null;
  functionalRequirements: number | null;
  useCases: number | null;
  manuals: number | null;
  designDocs: number | null;
  plantUml: number | null;
  mermaid: number | null;
  sadSections: number | null;
  adrs: number | null;
  specs: number | null;
  scenarios: number | null;
  archivedChanges: number | null;
  migrations: number | null;
  backendTests: number | null;
  testCases: number | null;
  e2eSpecs: number | null;
  brunoRequests: number | null;
  strideEntries: number | null;
  securityRequirements: number | null;
  securityCases: number | null;
  workflows: number | null;
};

export function artifactFacts(): ArtifactFacts {
  const req = requirementRows(PATHS.requirementsCsv);
  return {
    requirements: req?.total ?? null,
    functionalRequirements: req?.functional ?? null,
    useCases: files(PATHS.useCases, (n) => /^CU\d+/.test(n) && n.endsWith(".md")),
    manuals: files(PATHS.manuals, (n) => n.endsWith(".md")),
    designDocs: files(PATHS.design, (n) => n.endsWith(".md") && n !== "README.md"),
    plantUml: files(PATHS.diagrams, (n) => n.endsWith(".puml")),
    mermaid: occurrences(PATHS.architectureDocs, /```mermaid/g),
    sadSections: matches(PATHS.sad, /^## \d+\. /gm),
    adrs: files(PATHS.adrs, (n) => /^ADR-\d+.*\.md$/.test(n)),
    specs: dirsWith(PATHS.specs, "spec.md"),
    scenarios: occurrences(PATHS.specs, /^#### Scenario/gm),
    archivedChanges: dirsWith(PATHS.archive, "proposal.md"),
    migrations: existsSync(at(PATHS.migrations)) ? migrations().length : null,
    backendTests: files(PATHS.backendTests, (n) => /Test\.java$/.test(n)),
    testCases: files(PATHS.testCases, (n) => /^TC-\d+/.test(n)),
    e2eSpecs: files(PATHS.e2e, (n) => n.endsWith(".spec.ts")),
    brunoRequests: existsSync(at(PATHS.bruno)) ? bruno().requests : null,
    strideEntries: matches(PATHS.strideRegister, /^\s*- id: SR-/gm),
    securityRequirements: distinct(PATHS.authThreatModel, /SR-\d+/g),
    securityCases: matches(PATHS.securityCatalog, /^\s*- id:/gm),
    workflows: files(PATHS.workflows, (n) => /\.ya?ml$/.test(n)),
  };
}
