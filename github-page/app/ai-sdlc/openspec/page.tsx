import { OPENSPEC_FLOW, skill, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";

export default function OpenSpecPage() {
  const f = loadFactoryFacts();
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · OpenSpec"
        title="Spec-driven development, adapted"
        lead={
          <>
            OpenSpec is the change record. Notaire adapts it instead of replacing it: a forked schema carries the
            Constitution&apos;s mandatory sections, the CLI injects the rules into every agent, and project scripts turn advisory
            rules into failing exit codes. So far {fmt(f.archivedChanges)} changes have completed the flow, and{" "}
            {fmt(f.livingSpecs)} capability specs describe current behaviour.
          </>
        }
      />
      <Diagram chart={OPENSPEC_FLOW} caption="Explore → triage into real Issues → propose → apply with TDD → archive in the same PR → living specs." />

      <SectionTitle>Six adaptations</SectionTitle>
      <div className="grid md:grid-cols-2 gap-4">
        <Card>
          <h3 className="font-semibold">1 · Schema fork <Code>notaire-sdlc</Code></h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            A fork of the packaged <em>spec-driven</em> schema. Everything OpenSpec parses is unchanged; it adds the
            Constitution as mandatory context, the required sections per artifact, a <Code>traceability</Code> artifact
            (P4), and makes <Code>design</Code> mandatory (testing, regression, Playwright, deploy and rollback strategy).
          </p>
          <Sources items={[{ label: "schema.yaml", href: src("docs/openspec/schemas/notaire-sdlc/schema.yaml") }, { label: "templates/", href: src("docs/openspec/schemas/notaire-sdlc/templates/") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">2 · Constitution as CLI context</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            <Code>config.yaml</Code> holds a pointer and the non-negotiables, not a copy. The CLI injects it into
            <Code>openspec instructions</Code> for every artifact, so Claude Code, OpenCode, Copilot, Codex and Cursor all
            receive the same rules through the same channel.
          </p>
          <Sources items={[{ label: "docs/openspec/config.yaml", href: src("docs/openspec/config.yaml") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">3 · Closing the Explore → Propose gap</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            Exploration produces findings but no change. A project-owned triage skill turns each finding into a real,
            estimated, Use-Case-linked Issue, and creates Issues only after confirmation. Only that number may reach
            <Code>/opsx:propose</Code>.
          </p>
          <Sources items={[{ label: "openspec-triage", href: skill("openspec-triage") }, { label: "explore.md", href: src("docs/openspec/explore.md") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">4 · Mechanical Gate 1</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            <Code>validate-sdlc-plan.sh</Code> checks the mandatory sections, all 12 task groups, the Use Case reference, and
            that the Issue exists and is open on GitHub. It rejects a section left as a template comment. It runs in the
            pre-push hook and in PR Validation.
          </p>
          <Sources items={[{ label: "validate-sdlc-plan.sh", href: src("workspace/sdlc/validate-sdlc-plan.sh") }, { label: "seed-openspec-change.sh", href: src("workspace/sdlc/seed-openspec-change.sh") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">5 · Archive in the PR</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            The PR that implements a change also archives it, so no active change outlives its merge. Changes continued by
            later PRs declare <Code>multi_pr: true</Code> and are archived by the last one. Main Health fails on any leftover.
          </p>
          <Sources items={[{ label: "check-openspec-archive-hygiene.py", href: src("workspace/sdlc/check-openspec-archive-hygiene.py") }, { label: "openspec-archive-change", href: skill("openspec-archive-change") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">6 · Traceability that cannot lie</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            A row in <Code>traceability.md</Code> that says a permanent document was updated is a claim that the file is in
            the same diff. <Code>check-doc-traceability.py</Code> rejects the claim when it is not.
          </p>
          <Sources items={[{ label: "check-doc-traceability.py", href: src("workspace/sdlc/check-doc-traceability.py") }]} />
        </Card>
      </div>

      <SectionTitle>The five artifacts of a change</SectionTitle>
      <Table
        head={["Artifact", "Carries", "Checked by"]}
        rows={[
          ["proposal.md", "Issue, Use Case, branch, objective, what changes, business rules, capabilities, impact analysis, documentation impact, out of scope", "validate-sdlc-plan.sh (live Issue)"],
          ["traceability.md", "Issue → spec → tasks → commits → PR → merge → release; requirement coverage; docs ledger; gate log", "check-doc-traceability.py"],
          ["specs/<capability>/spec.md", "ADDED / MODIFIED / REMOVED requirements with #### Scenario: acceptance criteria", "openspec validate --strict"],
          ["design.md", "Decisions, risks, testing, regression, Playwright, deployment and rollback strategy", "schema (mandatory)"],
          ["tasks.md", "12 mandatory groups from Gate 1 to Gate 5 and the Definition of Done", "validate-sdlc-plan.sh, archive hygiene"],
        ]}
      />

      <SectionTitle>Commands</SectionTitle>
      <Card>
        <pre className="overflow-x-auto text-xs leading-relaxed text-slate-800">{`cd docs
openspec new change "<kebab-name>"                       # or: bash workspace/sdlc/seed-openspec-change.sh <name> --issue N --create
openspec validate <change> --strict                     # structure
bash ../workspace/sdlc/validate-sdlc-plan.sh <change>   # Constitution completeness + live Issue
openspec instructions <artifact> --change <change>      # what any agent receives
openspec archive <change> --yes                          # in the implementing PR`}</pre>
        <Sources items={[{ label: "NOTAIRE-ADAPTATIONS.md", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") }, { label: "/opsx slash commands", href: src(".claude/commands/opsx/") }]} />
      </Card>
    </div>
  );
}
