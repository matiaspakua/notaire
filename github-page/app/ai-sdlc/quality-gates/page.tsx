import { QUALITY_GATE_PIPELINE, WORKFLOWS, src, workflow } from "@/lib/ai-sdlc-content";
import { loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/quality-gates/");

const tierLabel: Record<string, string> = {
  release: "bg-violet-100 text-violet-800",
  main: "bg-emerald-100 text-emerald-800",
  schedule: "bg-slate-200 text-slate-700",
  event: "bg-sky-100 text-sky-800",
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
            Each gate is a script that exits non-zero. The pull-request proof is <Code>preflight.sh --full</Code> on the
            agent machine. GitHub runs the product workflows on a version tag or when someone dispatches them. When a gate
            is added or changed, <Code>preflight.sh</Code> changes in the same pull request.
          </>
        }
      />
      <Diagram chart={QUALITY_GATE_PIPELINE} caption="Local preflight is the pull-request proof. Product CI and image publish run on a version tag. Pages and Release Please run on main. ZAP and CodeQL also have a Monday schedule." />

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

      <SectionTitle>A quiet pull request is expected</SectionTitle>
      <Card className="border-violet-200">
        <p className="text-sm text-slate-700 leading-relaxed">
          Product workflows do not start on <Code>pull_request</Code>. <Code>check-heavy-ci.sh</Code> exits 0 when those
          runs are simply absent, and it fails if a run on that commit is pending or red. The desired <Code>protect-main</Code>{" "}
          ruleset in git no longer lists the old required checks. The live ruleset is an Owner step (#2001): if GitHub still
          demands checks that never start, the pull request cannot merge.
        </p>
        <Sources items={[{ label: "CI-MERGE-GATE.md", href: src("docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md") }, { label: "check-heavy-ci.sh", href: src("workspace/sdlc/check-heavy-ci.sh") }, { label: "SECURITY-GATES.md", href: src("docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md") }, { label: "protect-main.desired.json", href: src("security/rulesets/protect-main.desired.json") }]} />
      </Card>

      <SectionTitle>Path filters on a release run</SectionTitle>
      <p className="text-sm text-slate-600 max-w-3xl leading-relaxed">
        Inside the version-tag workflows, a path filter can skip a leaf job. It cannot skip the tag itself. Local{" "}
        <Code>preflight.sh --full</Code> does not use that filter: the agent runs the suite the pull request will not
        re-run.
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
        One security case catalog runs in preflight. Its families are{" "}
        {facts.securityFamilies.length > 0 ? facts.securityFamilies.join(", ") : "listed in the catalog"}. The release job
        runs the same catalog. CodeQL runs on the tag and on Monday and uploads SARIF. ZAP is the Monday baseline and does
        not run on the tag. A diff that touches authentication, authorization, personal data, secrets, dependencies or a
        public endpoint updates the threat model and the YAML register in the same change. SonarQube runs in the local
        quality stack under <Code>infra/</Code>.
      </p>
      <Sources items={[{ label: "security/cases/catalog.yml", href: src("security/cases/catalog.yml") }, { label: "codeql.yml", href: workflow("codeql.yml") }, { label: ".zap/rules.tsv", href: src(".zap/rules.tsv") }, { label: "run-sonar.sh", href: src("infra/scripts/run-sonar.sh") }, { label: "DevSecOps", href: src("docs/200-architecture/208-devsecops/README.md") }]} />

      <SectionTitle>Every workflow and its job</SectionTitle>
      <Table
        head={["Workflow", "When", "What it proves", "Tier"]}
        rows={WORKFLOWS.map((w) => [
          <a key={w.file} className="hover:text-violet-700" href={workflow(w.file)}>
            {w.name}
            <div className="font-mono text-[11px] text-slate-500">{w.file}</div>
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
          <p className="mt-2 text-sm text-slate-600">CD runs on a version tag or manual dispatch. It builds, scans, signs with cosign and publishes the images to GHCR, and attaches SBOMs to the release. A merge to main does not publish.</p>
          <Sources items={[{ label: "cd.yml", href: workflow("cd.yml") }, { label: "SBOM.md", href: src("docs/300-development/SBOM.md") }]} />
        </Card>
      </div>
    </div>
  );
}
