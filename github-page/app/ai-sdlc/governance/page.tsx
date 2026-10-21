import { CONVENTIONS, ISSUE_STATE_MACHINE, LABEL_FAMILIES, MILESTONE_KINDS, PR_SEQUENCE, src } from "@/lib/ai-sdlc-content";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";

export default function GovernancePage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Governance"
        title="One board, one state, many agents"
        lead={
          <>
            Humans and every agent runtime share one coordination protocol on GitHub: Issues, a single Project board, a
            label taxonomy, milestone trains and pull requests. No runtime keeps a private backlog, and scripts check that
            Issue state, board Status and labels agree.
          </>
        }
      />

      <SectionTitle>Issue lifecycle</SectionTitle>
      <Diagram chart={ISSUE_STATE_MACHINE} caption="docs/300-development/305-project-management/LABELS.md — status lifecycle for the agent fleet." />
      <div className="grid md:grid-cols-3 gap-4">
        <Card><h3 className="font-semibold">CLOSED ⇒ Done</h3><p className="mt-2 text-sm text-slate-600">Board Status Done, and no in-progress or blocked label.</p></Card>
        <Card><h3 className="font-semibold">OPEN + WIP ⇒ In Progress</h3><p className="mt-2 text-sm text-slate-600">Status In Progress if and only if <Code>status:in-progress</Code> is set.</p></Card>
        <Card><h3 className="font-semibold">Blocked ⇒ label</h3><p className="mt-2 text-sm text-slate-600">There is no Blocked column; use <Code>status:blocked</Code>, and never Done while open.</p></Card>
      </div>
      <Sources items={[{ label: "check-board-status-label-coherence.py", href: src("workspace/sdlc/check-board-status-label-coherence.py") }, { label: "claim-issue.py", href: src("workspace/sdlc/claim-issue.py") }, { label: "docs/github/README.md", href: src("docs/github/README.md") }]} />

      <SectionTitle>Label families</SectionTitle>
      <Table head={["Family", "Rule", "Labels"]} rows={LABEL_FAMILIES.map((l) => [<Code key={l.family}>{l.family}</Code>, l.rule, l.examples])} />
      <p className="mt-3 text-sm text-slate-600 max-w-3xl">
        There are no lane labels. A lane is derived from the labels (security, cross, backend, frontend, platform, quality),
        and two Issues in a conflicting lane are not claimed at once. The type label also drives the semver bump and the
        CHANGELOG section.
      </p>
      <Sources items={[{ label: "LABELS.md", href: src("docs/300-development/305-project-management/LABELS.md") }, { label: "label_taxonomy.py", href: src("workspace/sdlc/label_taxonomy.py") }, { label: "milestone_policy.py", href: src("workspace/sdlc/milestone_policy.py") }]} />

      <SectionTitle>Milestones are release trains</SectionTitle>
      <Table head={["Kind", "Pattern", "Example"]} rows={MILESTONE_KINDS.map((m) => [m.kind, <Code key={m.kind}>{m.pattern}</Code>, m.example])} />
      <p className="mt-3 text-sm text-slate-600 max-w-3xl">
        Each minor version has one train, and every patch tag belongs to it. Tags do not close milestones; the Owner closes
        a train when its exit criterion holds, and open work moves to the next train with a comment.
      </p>
      <Sources items={[{ label: "MILESTONE-NOMENCLATURE.md", href: src("docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md") }, { label: "ROADMAP.md", href: src("docs/300-development/305-project-management/ROADMAP.md") }]} />

      <SectionTitle>Pull request flow</SectionTitle>
      <Diagram chart={PR_SEQUENCE} caption="From first push to closed Issue. Every stage is visible on GitHub, so another agent can resume." />

      <SectionTitle>Conventions</SectionTitle>
      <Table head={["What", "Rule"]} rows={CONVENTIONS.map((c) => [c.what, c.rule])} />
      <Sources items={[{ label: "CONSTITUTION.md §4, §9", href: src("CONSTITUTION.md") }, { label: "PULL_REQUEST_TEMPLATE.md", href: src(".github/PULL_REQUEST_TEMPLATE.md") }, { label: "publish-progress.sh", href: src("workspace/sdlc/publish-progress.sh") }, { label: "check-closes-refs-guard.py", href: src("workspace/sdlc/check-closes-refs-guard.py") }]} />
    </div>
  );
}
