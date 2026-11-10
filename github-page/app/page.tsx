import { HomeView } from "@/components/home/HomeView";
import { loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { factoryFlow } from "@/lib/diagram-flows";
import { fact, loadAbout } from "@/lib/about";
import { hoursText, loadOutcomes, pctText } from "@/lib/outcomes";
import { migrations, openapi, services, testCounts, versions } from "@/lib/tech-facts";

const n = (x: number | null | undefined) => (x === null || x === undefined ? "—" : x.toLocaleString("en-US"));

/**
 * The landing page. Numbers are read when the site is built. Copy is chosen in the browser.
 */
export default function Home() {
  const v = versions();
  const api = openapi();
  const tests = testCounts();
  const factory = loadFactoryFacts();
  const svc = services();
  const list = migrations();
  const out = loadOutcomes();
  const about = loadAbout();
  const role = fact("On this project");
  const f = factoryFlow();

  const beforeNow: [string, string][] = [
    ["Java 1.6 Swing desktop monolith: UI, rules and SQL in one class", `Spring Boot ${v.springBoot ?? "—"} on Java ${v.java ?? "—"}, ports and adapters`],
    ["Swing forms", `Next.js ${v.next ?? "—"}, React ${v.react ?? "—"}, TypeScript`],
    ["MySQL 5, schema not versioned", `PostgreSQL ${v.postgres ?? "—"}, ${list.length} Flyway migrations`],
    ["none", `${n(api?.paths)} paths in a committed, CI-checked OpenAPI contract`],
    ["MD5", "BCrypt (V52)"],
    ["none", `${n(tests.backend)} JUnit classes, ${n(tests.frontend)} Vitest files, ${n(tests.bruno)} Bruno requests, ${n(tests.e2e)} E2E specs`],
    ["no containers, manual builds", `Docker Compose, ${n(factory.workflows)} GitHub Actions workflows, signed images`],
  ];

  return (
    <HomeView
      model={{
        proof: [
          { key: "openspec", value: n(factory.archivedChanges), href: "/ai-sdlc/openspec/" },
          { key: "tests", value: n(tests.backend + tests.frontend), href: "/docs/testing/" },
          { key: "bruno", value: n(tests.bruno), href: "/docs/api/" },
          { key: "e2e", value: n(tests.e2e), href: "/docs/testing/" },
          { key: "adrs", value: n(factory.adrs), href: "/docs/architecture/" },
          { key: "skills", value: n(factory.skills), href: "/ai-sdlc/agents/" },
        ],
        workflows: factory.workflows,
        observability: svc.observability.length,
        before: beforeNow.map(([then, now]) => ({ then, now })),
        flow: f,
        shots: [{ file: "dashboard" }, { file: "cases" }, { file: "administration" }, { file: "audit" }],
        outcomes: [
          { key: "median", value: hoursText(out.lead_time_hours.median) },
          { key: "p90", value: hoursText(out.lead_time_hours.p90) },
          { key: "week", value: String(out.throughput.median_per_week ?? "n/a") },
          { key: "failure", value: pctText(out.change_failure.rate_pct) },
          { key: "issues", value: n(out.issues_closed) },
          { key: "releases", value: n(out.releases.count) },
        ],
        outcomeWindow: out.window,
        doors: [
          { key: "product", href: "/product/", facts: [`v${v.product ?? "—"}`, `${n(api?.paths)} API paths`, `${list.length} migrations`] },
          { key: "docs", href: "/docs/", facts: [`Java ${v.java ?? "—"}`, `Spring Boot ${v.springBoot ?? "—"}`, `Next.js ${v.next ?? "—"}`] },
          { key: "factory", href: "/ai-sdlc/", facts: [`${n(factory.skills)} skills`, `${n(factory.agents)} agents`, `${n(factory.livingSpecs)} living specs`] },
          { key: "security", href: "/security/", facts: ["STRIDE", "local gates", "release scans"] },
        ],
        name: about.name,
        role: role?.value ?? "",
        contacts: about.facts.filter((x) => x.href).map((x) => ({ label: x.label, href: x.href as string })),
      }}
    />
  );
}
