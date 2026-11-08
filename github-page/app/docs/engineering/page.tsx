import Link from "next/link";
import { AnatomyFigure } from "@/components/anatomy/AnatomyFigure";
import { engineeringFigure } from "@/components/anatomy/engineering-figure";
import { Callout } from "@/components/DocsUI";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { WORKFLOWS, src, workflow } from "@/lib/ai-sdlc-content";
import { DELIVERY_LOOP_CHART } from "@/lib/anatomy-content";
import { pageMetadata } from "@/lib/site";
import { services, testCounts, v, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/engineering/");

const LOOPS = [
  { title: "Development", body: "An issue tied to a use case, an OpenSpec change, a branch named type/issue_description, tests first, atomic Conventional Commits, and docs updated in the same pull request." },
  { title: "Quality (QA)", body: "A test pyramid from unit to end-to-end, database checks, an OpenAPI contract check, and coverage floors that can only go up." },
  { title: "Security", body: "A gate, not a phase: secrets, static analysis, dependency and image scanning, a DAST baseline, and rules for who may merge what." },
  { title: "Operations", body: "Metrics, logs and traces from the running system, alerts, dashboards, a smoke test after a deploy, and findings that return to development as issues." },
];

function Cmd({ label, children }: { label: string; children: string }) {
  return (
    <pre tabIndex={0} role="region" aria-label={label} className="my-4 overflow-x-auto rounded-xl bg-slate-900 p-4 text-xs text-slate-100">
      {children}
    </pre>
  );
}

export default function EngineeringAnatomyPage() {
  const ver = versions();
  const svc = services();
  const tests = testCounts();
  const count = (t: (typeof WORKFLOWS)[number]["tier"]) => WORKFLOWS.filter((w) => w.tier === t).length;
  return (
    <>
      <PageHeader
        eyebrow="Technical docs · Engineering anatomy"
        title="Development, QA, security and operations as one loop"
        lead="Notaire is built and run as one loop, not four teams handing work over a wall. This page shows the loop as a machine, then the environments it runs on, then the commands that run it."
      />

      <SectionTitle id="loop">One loop, four concerns</SectionTitle>
      <div className="grid gap-3 sm:grid-cols-2">
        {LOOPS.map((l) => (
          <Card key={l.title}>
            <h3 className="font-semibold text-slate-900">{l.title}</h3>
            <p className="mt-2 text-sm text-slate-600">{l.body}</p>
          </Card>
        ))}
      </div>
      <Diagram chart={DELIVERY_LOOP_CHART} caption="A change goes from issue to a version tag, and what operations learn comes back as a new issue." />

      <SectionTitle id="machine">The machine: a release line</SectionTitle>
      <p>
        The package on the belt is one change. It is built at the workbench, proved on the test rig, scanned under the security
        arch, held by the gate board until every lamp is green, promoted through three environment crates, and watched from the
        observability cabinet. An alarm lamp on the workbench closes the loop.
      </p>
      <AnatomyFigure data={engineeringFigure} />

      <SectionTitle id="qa">QA: what is tested, and what must not drop</SectionTitle>
      <p>
        {tests.backend} backend test classes, {tests.frontend} front-end test files, {tests.e2e} Playwright specs and {tests.bruno} Bruno
        requests make up the pyramid below. Coverage is a <strong>raise-only ratchet</strong>: each floor sits a little under the measured
        value, is enforced in the build, and may be raised but never lowered.
      </p>
      <Table
        head={["Layer", "Tools", "Lives in"]}
        rows={[
          ["Unit", "JUnit 5 and Mockito (backend); Vitest (front end)", "backend-api, frontend"],
          ["Integration", "Spring Boot tests; Testcontainers for PostgreSQL", "backend-api"],
          ["API", "A Bruno collection run against a live backend", "backend-api/api-test"],
          ["End to end", "Playwright through the real UI", "testing/e2e"],
          ["Database", "Flyway migrations verified on PostgreSQL", "workflow database-vv"],
          ["Contract", "OpenAPI diff for breaking changes", "workflow openapi-contract"],
          ["Load", "k6 against a live API", "infra/performance"],
        ].map((r) => r.map((c, i) => (i === 2 ? <Code key={c}>{c}</Code> : c)))}
      />
      <p>
        More: <Link href="/docs/testing/">Testing</Link>, <Link href="/docs/api/">API contract and Bruno</Link>.
      </p>

      <SectionTitle id="security">Security: gates, not a phase</SectionTitle>
      <p>
        The same scanners run locally and on release, so a result does not depend on where it ran. One command runs the local catalog, and
        a catalog of named security cases defines what must hold.
      </p>
      <Cmd label="Run the local security gates">{`python3 security/run_gates.py --profile local`}</Cmd>
      <Table
        head={["Control", "What it checks", "When it runs"]}
        rows={[
          ["Secrets", "High-confidence secrets fail the gate", "Local catalog and version tag"],
          ["SAST", "Semgrep rules locally; CodeQL as advanced setup", "Local; CodeQL on a version tag and every Monday"],
          ["SCA", "Trivy on files and images", "Local and version tag"],
          ["DAST", "OWASP ZAP baseline against a running stack", "Every Monday"],
          ["Threat model", "STRIDE and LINDDUN register kept current", "Updated with any change that touches auth, data or secrets"],
          ["Repository protection", "A ruleset on main: pull requests only, no force-push", "Always"],
        ]}
      />
      <p>
        More: <Link href="/security/toolchain/">the security toolchain</Link> and <Link href="/security/threats/">the threat model</Link>.
      </p>

      <SectionTitle id="gates">The gates: preflight before the pull request, CI on a version tag</SectionTitle>
      <p>
        A pull request is proved on the agent machine by <Code>preflight.sh --full</Code> and the pre-PR pipeline. GitHub does not re-run the
        product suite on a pull request (#2001): {count("release")} workflows run on a version tag or on demand, {count("main")} on a push to{" "}
        <Code>main</Code>, {count("schedule")} on a weekly or daily schedule and {count("event")} on an event. Each is listed, with its job, on the{" "}
        <Link href="/ai-sdlc/quality-gates/">quality gates page</Link>.
      </p>
      <Cmd label="Run the gates locally">{`bash workspace/sdlc/install-git-hooks.sh     # once per clone: a push runs the gates
bash workspace/sdlc/preflight.sh              # every blocking gate that needs no server
bash workspace/sdlc/preflight.sh --full       # adds E2E, Bruno, Docker build and database V&V
bash workspace/sdlc/run_pipeline.sh           # the pre-PR pipeline, with the local security gate
python3 workspace/modules.py verify --all     # every module's own check`}</Cmd>
      <Sources
        items={[
          { label: "CI-PREFLIGHT.md", href: src("docs/300-development/CI-PREFLIGHT.md") },
          { label: "preflight.sh", href: src("workspace/sdlc/preflight.sh") },
          { label: "ci.yml", href: workflow("ci.yml") },
        ]}
      />

      <SectionTitle id="env">The infrastructure environments</SectionTitle>
      <p>The same application moves through every environment; what changes is the stack around it.</p>
      <Table
        head={["Environment", "Defined in", "What runs"]}
        rows={[
          ["Development stack", <Code key="a">docker-compose.yml</Code>, `${svc.app.join(", ")}. Started with workspace/stack/start.sh; Flyway builds the schema on first start.`],
          ["Observability and quality stack", <Code key="b">infra/observability/docker-compose.yml</Code>, `${svc.observability.join(", ")}. Started with infra/scripts/start-infra.sh or workspace/stack/start-all.sh; the Homer hub opens it all.`],
          ["Staging", <Code key="c">infra/deploy/kustomize</Code>, "Kubernetes manifests: a Kustomize base and a staging overlay, with one shared nginx.conf."],
          ["Production", <Code key="d">docker-compose.prod.yml</Code>, "A separate Compose file where only the reverse proxy is published on the host."],
          ["Version-tag runners", <Code key="e">.github/workflows/</Code>, "Disposable runners that build, test, scan, sign and publish the images to GHCR."],
        ]}
      />
      <Callout title="What the observability stack answers">
        <ul className="list-disc space-y-1 pl-5 text-sm">
          <li>Is it up, and how fast? Micrometer metrics scraped by Prometheus {v(ver.prometheus)}.</li>
          <li>What happened? Structured JSON logs collected into Loki {v(ver.loki)}.</li>
          <li>Where did the time go? Traces through the OpenTelemetry collector {v(ver.otelCollector)} into Tempo {v(ver.tempo)}.</li>
          <li>Who should know? Alertmanager rules such as backend down, login failure spikes and JVM heap pressure, drawn in Grafana {v(ver.grafana)}.</li>
          <li>Is the code healthy? SonarQube {v(ver.sonarqube)} quality gates on new code.</li>
        </ul>
      </Callout>
      <Sources
        items={[
          { label: "infra/README.md", href: src("infra/README.md") },
          { label: "infra/docs/OPERATION.md", href: src("infra/docs/OPERATION.md") },
          { label: "docs/200-architecture/207-monitoring/", href: src("docs/200-architecture/207-monitoring/") },
        ]}
      />
    </>
  );
}
