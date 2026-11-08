/**
 * Build-time facts for the AI Factory section (#1934).
 * Read from the repository when `next build` runs, so nothing is hard-coded
 * (CONSTITUTION §7: never hard-code suite counts). A missing folder yields null.
 */
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import path from "node:path";

const ROOT = path.resolve(process.cwd(), "..");

function at(rel: string): string {
  return path.join(ROOT, rel);
}

function countDirs(rel: string, marker?: string): number | null {
  const dir = at(rel);
  if (!existsSync(dir)) return null;
  return readdirSync(dir).filter((name) => {
    const full = path.join(dir, name);
    if (!statSync(full).isDirectory()) return false;
    return marker ? existsSync(path.join(full, marker)) : true;
  }).length;
}

function countFiles(rel: string, test: (name: string) => boolean, recursive = false): number | null {
  const dir = at(rel);
  if (!existsSync(dir)) return null;
  let total = 0;
  for (const name of readdirSync(dir)) {
    const full = path.join(dir, name);
    if (statSync(full).isDirectory()) {
      if (recursive && name !== "node_modules") total += countFiles(path.join(rel, name), test, true) ?? 0;
    } else if (test(name)) {
      total += 1;
    }
  }
  return total;
}

function securityFamilies(): string[] {
  const file = at("security/cases/catalog.yml");
  if (!existsSync(file)) return [];
  const families = readFileSync(file, "utf-8")
    .split("\n")
    .map((line) => line.match(/^\s+family:\s*([\w-]+)/)?.[1])
    .filter((f): f is string => Boolean(f));
  return Array.from(new Set(families));
}

export type FactoryFacts = {
  skills: number | null;
  agents: number | null;
  workflows: number | null;
  adrs: number | null;
  archivedChanges: number | null;
  livingSpecs: number | null;
  e2eSuites: number | null;
  useCases: number | null;
  sdlcScripts: number | null;
  securityFamilies: string[];
};

export function loadFactoryFacts(): FactoryFacts {
  return {
    skills: countDirs(".claude/skills", "SKILL.md"),
    agents: countFiles(".claude/agents", (n) => n.endsWith(".md")),
    workflows: countFiles(".github/workflows", (n) => n.endsWith(".yml") || n.endsWith(".yaml")),
    adrs: countFiles("docs/200-architecture/202-ADR", (n) => /^ADR-\d+.*\.md$/.test(n)),
    archivedChanges: countDirs("docs/openspec/changes/archive"),
    livingSpecs: countDirs("docs/openspec/specs", "spec.md"),
    e2eSuites: countFiles("testing/e2e/tests", (n) => n.endsWith(".spec.ts"), true),
    useCases: countFiles("docs/100-business/102-use-cases", (n) => /^CU\d+/.test(n) && n.endsWith(".md")),
    sdlcScripts: countFiles("workspace/sdlc", (n) => n.endsWith(".py") || n.endsWith(".sh")),
    securityFamilies: securityFamilies(),
  };
}

export function fmt(n: number | null): string {
  return n === null ? "—" : n.toLocaleString("en-US");
}
