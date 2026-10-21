import { QUALITY_GATE_PIPELINE, WORKFLOWS, src, workflow } from "@/lib/ai-sdlc-content";
import { loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";

const tierLabel: Record<string, string> = {
  light: "bg-sky-100 text-sky-800",
  heavy: "bg-violet-100 text-violet-800",
  after: "bg-emerald-100 text-emerald-800",
  scheduled: "bg-slate-200 text-slate-700",
};

export default function QualityGatesPage() {
  const facts = loadFactoryFacts();
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Quality gates"
        title="Gates are commands, not opinions"
        lead={
          <>
            Each gate is a script or a workflow that exits non-zero. The same checks run on the agent&apos;s machine before a
            push and again in GitHub Actions, and the repository refuses a merge that skipped them. When a CI gate is added
            or changed, <Code>preflight.sh</Code> changes in the same PR, so local and CI never drift.
          </>
        }
      />
      <Diagram chart={QUALITY_GATE_PIPELINE} caption="Local gates → light CI → heavy CI → merge gate → post-merge release, deploy and weekly checks." />

      <SectionTitle>Before the push</SectionTitle>
      <div className="grid md:grid-cols-3 gap-4">
        <Card>
          <h3 className="font-semibold">Plan check</h3>
          <p className="mt-2 text-sm text-slate-600">Every mandatory section and task group is present, and the Issue exists and is open on GitHub.</p>
          <Sources items={[{ label: "validate-sdlc-plan.sh", href: src("workspace/sdlc/validate-sdlc-plan.sh") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">Preflight (pre-push hook)</h3>
          <p className="mt-2 text-sm text-slate-600">A local mirror of the CI gates. <Code>mvn verify</Code> alone does not predict CI: Spotless is deliberately unbound from the Maven lifecycle.</p>
          <Sources items={[{ label: "preflight.sh", href: src("workspace/sdlc/preflight.sh") }, { label: "CI-PREFLIGHT.md", href: src("docs/300-development/CI-PREFLIGHT.md") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">Full pipeline (pre-PR)</h3>
          <p className="mt-2 text-sm text-slate-600">Brings the Docker stack up, runs the security case catalog and preflight <Code>--full</Code>, lints markdown, and writes an HTML dashboard.</p>
          <Sources items={[{ label: "run_pipeline.sh", href: src("workspace/sdlc/run_pipeline.sh") }, { label: "security/run_gates.py", href: src("security/run_gates.py") }]} />
        </Card>
      </div>

      <SectionTitle>Light CI is not enough</SectionTitle>
      <Card className="border-violet-200">
        <p className="text-sm text-slate-700 leading-relaxed">
          PR Validation, Frontend CI and Process Checks finish first and can report &quot;all N checks success&quot; while the
          backend Integration suite and Playwright are still queued. A merge needs the heavy set to be terminal success:
          Unit, Integration, Coverage Gate, Bruno API and Playwright UI. <Code>check-heavy-ci.sh &lt;pr&gt;</Code> must exit 0.
          The <Code>protect-main</Code> ruleset also requires five named checks: CI, Frontend CI, Playwright E2E, Code Lint and PR Validation.
        </p>
        <Sources items={[{ label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") }, { label: "check-heavy-ci.sh", href: src("workspace/sdlc/check-heavy-ci.sh") }, { label: "protect-main.desired.json", href: src("security/rulesets/protect-main.desired.json") }]} />
      </Card>

      <SectionTitle>Path-scoped CI</SectionTitle>
      <p className="text-sm text-slate-600 max-w-3xl leading-relaxed">
        On pull requests, docs, OpenSpec and agent-only paths skip the Java and Playwright leaf jobs. Suite aggregators keep
        the required check names green by treating an intentional <em>skipped</em> as success. Pushes to main, schedules and
        manual dispatch always run the full suite, and a test guards those invariants.
      </p>
      <Sources items={[{ label: "test_ci_workflow_invariants.py", href: src("workspace/tests/test_ci_workflow_invariants.py") }]} />

      <SectionTitle>Coverage and tests</SectionTitle>
      <Table
        head={["Level", "Where", "Gate"]}
        rows={[
          ["Unit + integration (backend)", "backend-api/src/test/java/…/unit, …/integration", "JaCoCo ratchet floor in mvn verify (80% line / 65% branch)"],
          ["Component (frontend)", "frontend/src/**/*.test.tsx (Vitest)", "raise-only floors in vitest.config.ts"],
          ["API contract", "Bruno suite + OpenAPI", "CU-API matrix + oasdiff breaking diff"],
          ["E2E", "testing/e2e/tests/TS-nnnn-*.spec.ts", "≥1 suite per critical Use Case; inventory drift guard"],
          ["Database", "Flyway migrations on PostgreSQL", "Database V&V workflow"],
        ]}
      />
      <Sources items={[{ label: "TEST-PLAN.md", href: src("docs/300-development/303-testing/TEST-PLAN.md") }, { label: "INVENTORY.md (generated)", href: src("docs/300-development/303-testing/INVENTORY.md") }, { label: "validate-testing-inventory.py", href: src("workspace/sdlc/validate-testing-inventory.py") }]} />

      <SectionTitle>Security by design</SectionTitle>
      <p className="text-sm text-slate-600 max-w-3xl leading-relaxed">
        One security case catalog runs locally and in CI. Its families are{" "}
        {facts.securityFamilies.length > 0 ? facts.securityFamilies.join(", ") : "listed in the catalog"}. CI adds CodeQL
        (advanced setup only) and a blocking weekly OWASP ZAP baseline with a reviewed allowlist. A diff that touches
        authentication, authorization, personal data, secrets, dependencies or a public endpoint updates the threat model
        and the catalog in the same change. SonarQube runs in the local quality stack under <Code>infra/</Code>.
      </p>
      <Sources items={[{ label: "security/cases/catalog.yml", href: src("security/cases/catalog.yml") }, { label: "codeql.yml", href: workflow("codeql.yml") }, { label: ".zap/rules.tsv", href: src(".zap/rules.tsv") }, { label: "run-sonar.sh", href: src("infra/scripts/run-sonar.sh") }, { label: "DevSecOps", href: src("docs/200-architecture/208-devsecops/README.md") }]} />

      <SectionTitle>Every workflow and its job</SectionTitle>
      <Table
        head={["Workflow", "When", "What it proves", "Tier"]}
        rows={WORKFLOWS.map((w) => [
          <a key={w.file} className="hover:text-violet-700" href={workflow(w.file)}>
            {w.name}
            <div className="font-mono text-[11px] text-slate-400">{w.file}</div>
          </a>,
          w.when,
          w.role,
          <span key={w.file + "t"} className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${tierLabel[w.tier]}`}>
            {w.tier}
          </span>,
        ])}
      />

      <SectionTitle>Release and deploy</SectionTitle>
      <div className="grid md:grid-cols-3 gap-4">
        <Card>
          <h3 className="font-semibold">One product version</h3>
          <p className="mt-2 text-sm text-slate-600">Backend, frontend and the Flyway schema ship under one SemVer tag. The version shows on <Code>/actuator/info</Code>, in OpenAPI and in the UI shell, and a coherence check runs in preflight, CI and CD.</p>
          <Sources items={[{ label: "ADR-028", href: src("docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md") }, { label: "check-product-version-coherence.py", href: src("workspace/sdlc/check-product-version-coherence.py") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">Release Please</h3>
          <p className="mt-2 text-sm text-slate-600">Merged Conventional Commits drive the release PR and the CHANGELOG. The Unreleased section is curated per release.</p>
          <Sources items={[{ label: "release-please-config.json", href: src("release-please-config.json") }, { label: "RELEASES.md", href: src("docs/300-development/305-project-management/RELEASES.md") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">Signed images</h3>
          <p className="mt-2 text-sm text-slate-600">CD runs only after CI succeeds on main. It builds, scans, signs with cosign and publishes the images to GHCR, and attaches product, database and image SBOMs to releases.</p>
          <Sources items={[{ label: "cd.yml", href: workflow("cd.yml") }, { label: "SBOM.md", href: src("docs/300-development/SBOM.md") }]} />
        </Card>
      </div>
    </div>
  );
}
