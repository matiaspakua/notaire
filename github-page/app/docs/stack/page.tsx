import Link from "next/link";
import { Code, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { Callout, StatGrid } from "@/components/DocsUI";
import { Fill } from "@/components/site/T";
import { REPO, src } from "@/lib/ai-sdlc-content";
import { pageMetadata } from "@/lib/site";
import { adrs, services, testCounts, versions, workflowTriggers, workflows } from "@/lib/tech-facts";
import { PipelineMap } from "@/components/diagrams/PipelineMap";

export const metadata = pageMetadata("/docs/stack/");

type Row = { name: string; version?: string | null; role: string; n?: number; app?: number; obs?: number };

export default function StackPage() {
  const v = versions();
  const wf = workflows();
  const svc = services();
  const tests = testCounts();

  const groups: { id: string; title: string; rows: Row[]; source: { label: string; href: string } }[] = [
    {
      id: "backend",
      title: "Backend",
      source: { label: "backend-api/pom.xml", href: src("backend-api/pom.xml") },
      rows: [
        { name: "Java", version: v.java, role: "Language of the backend; the build pins the release." },
        { name: "Spring Boot", version: v.springBoot, role: "Web, validation, JPA, security, actuator and Flyway starters." },
        { name: "Spring Security + jjwt", version: v.jjwt, role: "Stateless JWT sessions, bcrypt passwords, revoked-token and login-attempt tables." },
        { name: "Spring Data JPA / Hibernate", role: "Persistence behind the out-ports; it never changes the schema in production." },
        { name: "springdoc-openapi", version: v.springdoc, role: "Generates the OpenAPI document from the controllers." },
        { name: "Apache PDFBox", role: "Renders PDF reports behind an out-port adapter." },
        { name: "Lombok", role: "Removes boilerplate in entities and DTOs." },
      ],
    },
    {
      id: "frontend",
      title: "Frontend",
      source: { label: "frontend/package.json", href: src("frontend/package.json") },
      rows: [
        { name: "Next.js", version: v.next, role: "App Router front end for the office staff." },
        { name: "React", version: v.react, role: "UI library." },
        { name: "TypeScript", version: v.typescript, role: "Strict types, including types generated from the API contract." },
        { name: "Tailwind CSS + Radix UI", version: v.tailwind, role: "Styling and accessible primitives (dialogs, menus, tabs, selects)." },
        { name: "TanStack Query", version: v.reactQuery, role: "Server state: fetching, caching, invalidation." },
        { name: "Zustand", version: v.zustand, role: "Client-side auth session store." },
        { name: "next-intl", version: v.nextIntl, role: "Spanish and English messages." },
        { name: "React Flow (xyflow) + dagre", version: v.xyflow, role: "The workflow editor and progress graphs." },
        { name: "openapi-typescript", version: v.openapiTypescript, role: "Turns openapi.yaml into api.generated.ts." },
      ],
    },
    {
      id: "data",
      title: "Data",
      source: { label: "db/migration/", href: src("backend-api/src/main/resources/db/migration/") },
      rows: [
        { name: "PostgreSQL", version: v.postgres, role: "The only production database." },
        { name: "Flyway", role: "Versioned SQL migrations; the single owner of the schema." },
        { name: "H2", role: "In-memory database used only to export the OpenAPI document in CI." },
      ],
    },
    {
      id: "testing",
      title: "Testing",
      source: { label: "testing/", href: src("testing/") },
      rows: [
        { name: "JUnit 5 + Spring Boot Test", role: "{n} backend test classes.", n: tests.backend },
        { name: "Testcontainers", role: "Integration tests against a real PostgreSQL." },
        { name: "ArchUnit", version: v.archunit, role: "Enforces the hexagonal rules R1–R7 in the build." },
        { name: "Vitest + Testing Library", version: v.vitest, role: "{n} frontend test files.", n: tests.frontend },
        { name: "Playwright", version: v.playwright, role: "{n} end-to-end specs against the full stack.", n: tests.e2e },
        { name: "Bruno", role: "API requests with assertions, run by CI against the live backend." },
        { name: "k6", role: "Performance tests in their own workflow." },
        { name: "JaCoCo", role: "Backend coverage reports." },
      ],
    },
    {
      id: "quality",
      title: "Quality and security",
      source: { label: ".github/", href: src(".github/") },
      rows: [
        { name: "Spotless, Checkstyle, SpotBugs", role: "Formatting and static analysis in the Maven build." },
        { name: "ESLint", role: "Front-end lint, including the import rules that keep the hexagon closed." },
        { name: "CodeQL", role: "Code scanning for Java, TypeScript and the workflows themselves." },
        { name: "OWASP ZAP", role: "Baseline DAST scan against the running app." },
        { name: "SonarQube", version: v.sonarqube, role: "Local quality dashboard in the observability stack." },
        { name: "Dependabot", role: "Dependency update pull requests." },
      ],
    },
    {
      id: "observability",
      title: "Observability",
      source: { label: "infra/observability/", href: src("infra/observability/") },
      rows: [
        { name: "OpenTelemetry", version: v.otelCollector, role: "Traces from backend and browser through the collector." },
        { name: "Micrometer + Prometheus", version: v.prometheus, role: "Metrics scraped from the actuator." },
        { name: "Grafana", version: v.grafana, role: "Dashboards over metrics, traces and logs." },
        { name: "Tempo", version: v.tempo, role: "Trace storage." },
        { name: "Loki + logstash encoder", version: v.loki, role: "Log storage; the backend writes structured JSON logs." },
      ],
    },
    {
      id: "delivery",
      title: "Delivery and AI tooling",
      source: { label: "CONSTITUTION.md", href: src("CONSTITUTION.md") },
      rows: [
        { name: "GitHub Actions", role: "{n} workflows (listed below).", n: wf.length },
        { name: "Docker Compose", role: "{app} application services and {obs} observability services.", app: svc.app.length, obs: svc.observability.length },
        { name: "release-please", role: "Versions and changelog for product releases." },
        { name: "OpenSpec", role: "Every change starts as a proposal, design, spec and tasks." },
        { name: "Claude Code, Cursor, Copilot, OpenCode", role: "AI agents that work through the same SDLC gates as people." },
      ],
    },
  ];

  return (
    <>
      <PageHeader
        eyebrow="Start here"
        title="Technology stack"
        lead="Every technology Notaire uses and the job it does. Versions are read from pom.xml, package.json and the Compose files when this site is built, so they cannot drift from the code."
      />
      <StatGrid
        items={[
          { value: v.java ?? "—", label: "Java" },
          { value: v.springBoot ?? "—", label: "Spring Boot" },
          { value: v.next ?? "—", label: "Next.js" },
          { value: adrs().length, label: "ADRs", hint: "the decisions behind this list" },
        ]}
      />

      <SectionTitle id="patterns">Patterns</SectionTitle>
      <Table
        head={["Pattern", "Where it shows"]}
        rows={[
          ["Ports and adapters (hexagonal)", <Link key="h" className="underline" href="/docs/hexagonal/">Hexagonal architecture</Link>],
          ["Database as code", <Link key="d" className="underline" href="/docs/database/">Database &amp; Flyway</Link>],
          ["Contract-first checks on a code-first API", <Link key="a" className="underline" href="/docs/api/">API contract &amp; Bruno</Link>],
          ["Architecture decision records", <Link key="r" className="underline" href="/docs/architecture/">Architecture</Link>],
          ["Spec-driven, gated SDLC for people and AI agents", <Link key="s" className="underline" href="/ai-sdlc/">AI Factory</Link>],
        ]}
      />

      {groups.map((g) => (
        <section key={g.id} aria-labelledby={`stack-${g.id}`}>
          <SectionTitle id={`stack-${g.id}`}>{g.title}</SectionTitle>
          <Table
            head={["Technology", "Version", "What it does here"]}
            rows={g.rows.map((r) => [
              r.name,
              r.version === undefined ? "" : <Code key={r.name}>{r.version ?? "—"}</Code>,
              r.role.includes("{") ? <Fill key={r.name} text={r.role} values={{ n: r.n ?? "", app: r.app ?? "", obs: r.obs ?? "" }} /> : r.role,
            ])}
          />
          <Sources items={[g.source]} />
        </section>
      ))}

      <SectionTitle id="workflows">CI/CD workflows</SectionTitle>
      <p>What starts each workflow, read from its on: block. Product workflows sit on the tag and on manual dispatch, not on the pull request.</p>
      <PipelineMap workflows={workflowTriggers()} repoHref={REPO} />
      <ul className="my-4 grid gap-2 sm:grid-cols-2">
        {wf.map((w) => (
          <li key={w.file} className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm">
            <a className="font-medium text-slate-900 hover:text-[var(--accent-700)]" href={src(`.github/workflows/${encodeURIComponent(w.file)}`)}>{w.name}</a>
            <span className="block font-mono text-xs text-slate-500">{w.file}</span>
          </li>
        ))}
      </ul>
      <Callout title="Full write-up">
        The same assessment, with the reasons behind each choice, lives in the repository at{" "}
        <a className="underline" href={src("docs/200-architecture/TECHNOLOGY-STACK.md")}>docs/200-architecture/TECHNOLOGY-STACK.md</a>.
      </Callout>
    </>
  );
}
