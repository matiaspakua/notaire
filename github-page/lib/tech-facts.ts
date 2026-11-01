/**
 * Build-time facts for the Technical docs (#1976).
 * Everything the stack, hexagon, database and API pages print is read from the repository
 * when `next build` runs (CONSTITUTION §7: never hard-code versions or counts). A missing
 * file yields null or an empty list, and the page says "not found" instead of inventing.
 */
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import path from "node:path";

const ROOT = path.resolve(process.cwd(), "..");
const JAVA = "backend-api/src/main/java/com/licensis/notaire";

function at(rel: string): string {
  return path.join(ROOT, rel);
}

function read(rel: string): string {
  const file = at(rel);
  return existsSync(file) ? readFileSync(file, "utf-8") : "";
}

function walk(rel: string, test: (name: string, rel: string) => boolean): string[] {
  const dir = at(rel);
  if (!existsSync(dir)) return [];
  const out: string[] = [];
  for (const name of readdirSync(dir).sort()) {
    if (name === "node_modules" || name.startsWith(".")) continue;
    const child = path.join(rel, name);
    if (statSync(at(child)).isDirectory()) out.push(...walk(child, test));
    else if (test(name, child)) out.push(child);
  }
  return out;
}

function dirs(rel: string): string[] {
  const dir = at(rel);
  if (!existsSync(dir)) return [];
  return readdirSync(dir)
    .filter((n) => statSync(path.join(dir, n)).isDirectory())
    .sort();
}

/** `^1.2.3` → `1.2.3`; null when the dependency is not declared. */
function npmVersion(pkg: string, name: string): string | null {
  const text = read(pkg);
  if (!text) return null;
  const json = JSON.parse(text) as { dependencies?: Record<string, string>; devDependencies?: Record<string, string> };
  const v = json.dependencies?.[name] ?? json.devDependencies?.[name];
  return v ? v.replace(/^[\^~]/, "") : null;
}

/** The `<version>` right after an `<artifactId>` in a pom, resolving `${property}` once. */
function pomVersion(pom: string, artifactId: string): string | null {
  const text = read(pom);
  const m = text.match(new RegExp(`<artifactId>${artifactId}</artifactId>\\s*<version>([^<]+)</version>`));
  if (!m) return null;
  const prop = m[1].match(/^\$\{(.+)\}$/);
  if (!prop) return m[1];
  return text.match(new RegExp(`<${prop[1]}>([^<]+)</${prop[1]}>`))?.[1] ?? null;
}

function pomProperty(pom: string, name: string): string | null {
  return read(pom).match(new RegExp(`<${name}>([^<]+)</${name}>`))?.[1] ?? null;
}

function image(compose: string, repo: string): string | null {
  const tag = read(compose).match(new RegExp(`image:\\s*${repo.replace(/[/.]/g, "\\$&")}:([\\w.\\-]+)`))?.[1];
  return tag ? tag.replace(/^v(?=\d)/, "") : null;
}

function composeServices(compose: string): string[] {
  const text = read(compose);
  const block = text.split(/^services:\s*$/m)[1] ?? "";
  const end = block.search(/^\S/m);
  return Array.from((end >= 0 ? block.slice(0, end) : block).matchAll(/^ {2}([\w-]+):\s*$/gm)).map((m) => m[1]);
}

// ── Versions ──────────────────────────────────────────────────────────────────

export type Versions = Record<
  | "product" | "java" | "springBoot" | "springdoc" | "jjwt" | "archunit" | "postgres"
  | "next" | "react" | "typescript" | "tailwind" | "reactQuery" | "zustand" | "nextIntl" | "xyflow" | "vitest"
  | "openapiTypescript" | "playwright" | "grafana" | "prometheus" | "otelCollector" | "tempo" | "loki" | "sonarqube",
  string | null
>;

export function versions(): Versions {
  const fe = "frontend/package.json";
  const obs = "infra/observability/docker-compose.yml";
  return {
    product: read("pom.xml").match(/<artifactId>notaire[^<]*<\/artifactId>\s*<version>([^<]+)</)?.[1] ?? pomVersion("backend-api/pom.xml", "notaire") ?? read("backend-api/pom.xml").match(/<parent>[\s\S]*?<version>([^<]+)<\/version>/)?.[1] ?? null,
    java: pomProperty("pom.xml", "java.version"),
    springBoot: pomVersion("pom.xml", "spring-boot-starter-parent"),
    springdoc: pomVersion("backend-api/pom.xml", "springdoc-openapi-starter-webmvc-ui"),
    jjwt: pomVersion("backend-api/pom.xml", "jjwt-api"),
    archunit: pomProperty("backend-api/pom.xml", "archunit.version"),
    postgres: image("docker-compose.yml", "postgres"),
    next: npmVersion(fe, "next"),
    react: npmVersion(fe, "react"),
    typescript: npmVersion(fe, "typescript"),
    tailwind: npmVersion(fe, "tailwindcss"),
    reactQuery: npmVersion(fe, "@tanstack/react-query"),
    zustand: npmVersion(fe, "zustand"),
    nextIntl: npmVersion(fe, "next-intl"),
    xyflow: npmVersion(fe, "@xyflow/react"),
    vitest: npmVersion(fe, "vitest"),
    openapiTypescript: npmVersion(fe, "openapi-typescript"),
    playwright: npmVersion("testing/e2e/package.json", "@playwright/test"),
    grafana: image(obs, "grafana/grafana"),
    prometheus: image(obs, "prom/prometheus"),
    otelCollector: image(obs, "otel/opentelemetry-collector-contrib"),
    tempo: image(obs, "grafana/tempo"),
    loki: image(obs, "grafana/loki"),
    sonarqube: image(obs, "sonarqube"),
  };
}

// ── Database (Flyway) ─────────────────────────────────────────────────────────

export const MIGRATIONS_DIR = "backend-api/src/main/resources/db/migration";

export type Migration = { version: number; name: string; file: string };

export function migrations(): Migration[] {
  const dir = at(MIGRATIONS_DIR);
  if (!existsSync(dir)) return [];
  return readdirSync(dir)
    .map((file) => {
      const m = file.match(/^V(\d+)__(.+)\.sql$/);
      return m ? { version: Number(m[1]), name: m[2].replace(/_/g, " "), file } : null;
    })
    .filter((m): m is Migration => m !== null)
    .sort((a, b) => a.version - b.version);
}

/** `R<n>__*.sql`: hand-applied rollback scripts that Flyway never runs (see the migration README). */
export function rollbackScripts(): string[] {
  const dir = at(MIGRATIONS_DIR);
  if (!existsSync(dir)) return [];
  return readdirSync(dir).filter((f) => /^R\d+__.+\.sql$/.test(f)).sort();
}

export function flywaySettings(): { key: string; value: string }[] {
  return read("backend-api/src/main/resources/application.properties")
    .split("\n")
    .map((l) => l.match(/^spring\.flyway\.([\w.-]+)=(.*)$/))
    .filter((m): m is RegExpMatchArray => m !== null && !m[1].startsWith("placeholders."))
    .map((m) => ({ key: m[1], value: m[2].trim() }));
}

// ── API contract (OpenAPI) and Bruno ──────────────────────────────────────────

export const OPENAPI_FILE = "backend-api/openapi/openapi.yaml";
export const BRUNO_DIR = "backend-api/api-test";

export function openapi(): { paths: number; operations: number; tags: number } | null {
  const text = read(OPENAPI_FILE);
  if (!text) return null;
  const paths = text.split(/^paths:\s*$/m)[1] ?? "";
  const body = paths.slice(0, Math.max(0, paths.search(/^\S/m)) || paths.length);
  return {
    paths: (body.match(/^ {2}\/\S*:\s*$/gm) ?? []).length,
    operations: (text.match(/^\s+operationId:/gm) ?? []).length,
    tags: new Set(Array.from(body.matchAll(/^ {6}- ([\w -]+)$/gm)).map((m) => m[1])).size,
  };
}

/** The Bruno login request, verbatim: the first request of the suite. */
export const BRUNO_SAMPLE_FILE = `${BRUNO_DIR}/00-auth/01-login.yml`;
export function brunoSample(): string {
  return read(BRUNO_SAMPLE_FILE).trim();
}

/** Rows of the "Backend defects found and fixed via this suite" table in api-test/COVERAGE.md. */
export function brunoDefectsFixed(): number {
  const section = read(`${BRUNO_DIR}/COVERAGE.md`).split(/^## Backend defects found/m)[1]?.split(/^## /m)[0] ?? "";
  return (section.match(/^\| \d+ \|/gm) ?? []).length;
}

export function bruno(): { requests: number; folders: string[] } {
  const isRequest = (name: string, rel: string) =>
    name.endsWith(".yml") && name !== "folder.yml" && name !== "opencollection.yml" && !rel.includes(`${path.sep}environments${path.sep}`);
  const folders = dirs(BRUNO_DIR).filter((d) => d !== "environments" && d !== "node_modules");
  return { requests: walk(BRUNO_DIR, isRequest).length, folders };
}

// ── Hexagon (ADR-030) ─────────────────────────────────────────────────────────

const javaFiles = (rel: string) => walk(rel, (n) => n.endsWith(".java")).length;

export function hexagon() {
  return {
    domainContexts: dirs(`${JAVA}/domain`).length,
    useCaseContexts: dirs(`${JAVA}/application/usecase`).length,
    inPorts: javaFiles(`${JAVA}/application/port/in`),
    outPorts: javaFiles(`${JAVA}/application/port/out`),
    useCases: javaFiles(`${JAVA}/application/usecase`),
    webAdapters: javaFiles(`${JAVA}/adapter/in`),
    outAdapters: javaFiles(`${JAVA}/adapter/out`),
    /** Closed legacy packages (ADR-030 rule 8): no new dependents, deleted when empty. */
    legacy: (["jpa", "service", "repository", "business", "dto"] as const).map((pkg) => ({ pkg, files: javaFiles(`${JAVA}/${pkg}`) })),
    baselineViolations: read("backend-api/src/test/resources/architecture/hexagon-baseline.txt")
      .split("\n")
      .filter((l) => l.trim() && !l.startsWith("#")).length,
    frontendContexts: dirs("frontend/src/core").length,
    frontendHttpAdapters: dirs("frontend/src/adapters/http").length,
  };
}

// ── Tests, workflows, services, ADRs ──────────────────────────────────────────

export function testCounts() {
  return {
    backend: walk("backend-api/src/test", (n) => /Test\.java$/.test(n)).length,
    frontend: walk("frontend/src", (n) => /\.test\.tsx?$/.test(n)).length,
    e2e: walk("testing/e2e", (n) => /\.spec\.ts$/.test(n)).length,
    bruno: bruno().requests,
  };
}

export function workflows(): { file: string; name: string }[] {
  const dir = ".github/workflows";
  if (!existsSync(at(dir))) return [];
  return readdirSync(at(dir))
    .filter((f) => /\.ya?ml$/.test(f))
    .sort()
    .map((file) => ({ file, name: read(`${dir}/${file}`).match(/^name:\s*["']?(.+?)["']?\s*$/m)?.[1] ?? file }));
}

export function services(): { app: string[]; observability: string[] } {
  return { app: composeServices("docker-compose.yml"), observability: composeServices("infra/observability/docker-compose.yml") };
}

export const ADR_DIR = "docs/200-architecture/202-ADR";

export type Adr = { id: string; title: string; status: string; file: string };

export function adrs(): Adr[] {
  if (!existsSync(at(ADR_DIR))) return [];
  return readdirSync(at(ADR_DIR))
    .filter((f) => /^ADR-\d{3}-.+\.md$/.test(f))
    .sort()
    .map((file) => {
      const text = read(`${ADR_DIR}/${file}`);
      const heading = text.match(/^#\s+ADR-\d+:?\s*(.+)$/m)?.[1]?.trim() ?? file;
      const inline = text.match(/^\*\*Status:?\*\*:?\s*(\w+)/m)?.[1];
      const block = text.match(/^##\s+Status\s*\n+\s*(?:\*\*)?(\w+)/m)?.[1];
      return { id: file.slice(4, 7), title: heading, status: inline ?? block ?? "Unknown", file };
    });
}

/** "4.1.2" or "—" when a fact could not be read; pages print this, never a guess. */
/** "3.4" from the SAD's "**Version:** 3.4" line. */
export function sadVersion(): string | null {
  return read("docs/200-architecture/201-SAD/sad.md").match(/\*\*Version:\*\*\s*([\d.]+)/)?.[1] ?? null;
}

export function v(value: string | null | undefined): string {
  return value ?? "—";
}

// ── Product (what the app offers) ─────────────────────────────────────────────

export const NAV_FILE = "frontend/src/lib/dashboard-nav.ts";

/** The app's sidebar sections and screens, read from the frontend's nav and its English messages. */
export function productModules(): { group: string; items: { label: string; href: string; adminOnly: boolean }[] }[] {
  const nav = read(NAV_FILE);
  const messages = read("frontend/messages/en.json");
  if (!nav || !messages) return [];
  const en = JSON.parse(messages) as { navigation?: Record<string, string>; navGroups?: Record<string, string> };
  const body = nav.split("export const DASHBOARD_NAV_GROUPS")[1]?.split("];")[0] ?? "";
  return body
    .split(/\n\s{2}\{\n\s{4}labelKey:/)
    .slice(1)
    .map((chunk) => {
      const key = chunk.match(/^\s*"(\w+)"/)?.[1] ?? "";
      const items = Array.from(chunk.matchAll(/\{ labelKey: "(\w+)", href: "([^"]+)"([^}]*)\}/g)).map((m) => ({
        label: en.navigation?.[m[1]] ?? m[1],
        href: m[2],
        adminOnly: m[3].includes("adminOnly: true"),
      }));
      return { group: en.navGroups?.[key] ?? key, items };
    })
    .filter((g) => g.items.length > 0);
}

export function useCaseCount(): number {
  const dir = at("docs/100-business/102-use-cases");
  return existsSync(dir) ? readdirSync(dir).filter((f) => /^CU\d+/.test(f)).length : 0;
}
