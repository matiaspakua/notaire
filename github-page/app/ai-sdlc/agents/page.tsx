import { CREW, ENTRY_POINTS, HARNESS_PHASES, ROLES_CHART, SKILL_STAGES, agent, skill, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources, Table } from "@/components/FactoryUI";
import { pageMetadata } from "@/lib/site";
import { Swimlane } from "@/components/Swimlane";

export const metadata = pageMetadata("/ai-sdlc/agents/");

export default function AgentsPage() {
  const f = loadFactoryFacts();
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Agents & skills"
        title="A crew, not a chatbot"
        lead={
          <>
            Three roles carry authority: Owner, Foreman and Worker. Specialist agents ({fmt(f.agents)} definitions) work
            from one catalog of {fmt(f.skills)} skills. Multi-part work is delegated to specialized sub-agents in parallel by
            default, each with an explicit scope and a model tier matched to the task.
          </>
        }
      />
      <Diagram chart={ROLES_CHART} caption="The Foreman briefs specialists and reviews their evidence. The Owner holds the decisions." />

      <SectionTitle id="swimlane">One change, four lanes</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        Follow a single Issue from the Owner&apos;s backlog to a closed Issue. Each lane can only do what its role allows.
      </p>
      <Swimlane />

      <SectionTitle>Roles and authority</SectionTitle>
      <Table
        head={["Role", "Who", "May", "May never"]}
        rows={[
          ["Owner", "The human code owner (CODEOWNERS)", "Approve specs; grant exceptions; merge Constitution, sdlc-exception and harness PRs; merge any PR", "—"],
          ["Foreman", "Supervising AI agent", "Run and validate gates, write review notes, merge Worker PRs when all required checks are green and check-heavy-ci.sh exits 0 (#1624)", "Implement the change it reviews; merge policy or its own harness"],
          ["Worker", "Implementing AI agent", "Implement phase by phase; answer review notes", "Merge, push to main, grant itself an exception"],
        ]}
      />
      <Sources items={[{ label: "CONSTITUTION.md §10 Roles", href: src("CONSTITUTION.md") }, { label: "cloud-foreman", href: agent("cloud-foreman") }]} />

      <SectionTitle>The specialist crew</SectionTitle>
      <div className="grid md:grid-cols-2 gap-4">
        {CREW.map((c) => (
          <Card key={c.name}>
            <a href={agent(c.name)} className="font-mono text-sm font-semibold text-violet-700 hover:underline">
              {c.name}
            </a>
            <p className="mt-1 text-sm text-slate-600">{c.role}</p>
            <div className="mt-3 flex flex-wrap gap-1">
              {c.skills.map((s) => (
                <a key={s} href={skill(s)} className="rounded-full bg-slate-100 px-2 py-0.5 font-mono text-[11px] text-slate-700 hover:bg-violet-100">
                  {s}
                </a>
              ))}
            </div>
          </Card>
        ))}
      </div>

      <SectionTitle>Cost-matched delegation</SectionTitle>
      <div className="grid md:grid-cols-3 gap-4">
        <Card><h3 className="font-semibold">Mechanical</h3><p className="mt-2 text-sm text-slate-600">Renames, applying a known pattern, running a fixed script → the cheapest capable model.</p></Card>
        <Card><h3 className="font-semibold">Standard</h3><p className="mt-2 text-sm text-slate-600">Writing and fixing tests, routine bug fixes, consistency checks → the mid tier.</p></Card>
        <Card><h3 className="font-semibold">Critical</h3><p className="mt-2 text-sm text-slate-600">Architecture, ambiguous judgment, security consequences → the top tier.</p></Card>
      </div>
      <Sources items={[{ label: "CONSTITUTION.md §5 Sub-agent delegation", href: src("CONSTITUTION.md") }, { label: "fleet-manifest.yaml", href: src("docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml") }, { label: "FLEET-ARCHITECTURE §4–5 (handoff contracts)", href: src("docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md") }]} />

      <SectionTitle>Skills by SDLC stage</SectionTitle>
      <Table
        head={["Stage", "Skills", "Primary output"]}
        rows={SKILL_STAGES.map((s) => [
          s.stage,
          <span key={s.stage} className="flex flex-wrap gap-1">
            {s.skills.map((k) => (
              <a key={k} href={skill(k)} className="font-mono text-[11px] text-violet-700 hover:underline">
                {k}
              </a>
            ))}
          </span>,
          s.output,
        ])}
      />
      <p className="mt-3 text-sm text-slate-600 max-w-3xl">
        Selection rule: use the narrowest project skill for implementation details and a generic lifecycle skill for
        cross-cutting decisions. Skills are guidance, not a second policy layer, and cannot override the Constitution, an
        approved spec or a human. Each generic skill ships an evaluation manifest (<Code>evals/evals.json</Code>).
      </p>
      <Sources items={[{ label: ".claude/skills/README.md", href: src(".claude/skills/README.md") }]} />

      <SectionTitle>Two runtimes, one process</SectionTitle>
      <div className="grid md:grid-cols-2 gap-4">
        <Card>
          <h3 className="font-semibold">Cursor Cloud fleet</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            A Foreman with specialist sub-agents on cloud VMs drives pick → Gate 1 → TDD → preflight → PR → CI → review →
            merge. Coordination goes through GitHub (Issue state, board Status, labels, PR Closes/Refs), never a private queue.
          </p>
          <Sources items={[{ label: "304-ai-sdlc-cloud/README.md", href: src("docs/300-development/304-ai-sdlc-cloud/README.md") }, { label: "PARALLEL-AGENTS.md", href: src("docs/300-development/305-project-management/PARALLEL-AGENTS.md") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold">Local-AI harness</h3>
          <p className="mt-2 text-sm text-slate-600 leading-relaxed">
            A small local model with a 32K window cannot hold the whole process, and it tends to report success it has not
            earned. <Code>foreman.sh</Code> splits the Constitution into phases, gives each a fresh context and a short brief,
            checks every result with real commands, and retries with the gate output attached.
          </p>
          <Sources items={[{ label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") }, { label: "prompts/", href: src("local-ai/sdlc/prompts/") }]} />
        </Card>
      </div>
      <div className="mt-4">
        <Table head={["Harness phase", "Who", "Deterministic gate"]} rows={HARNESS_PHASES.map((p) => [p.phase, p.who, p.gate])} />
      </div>

      <SectionTitle>Entry points by tool</SectionTitle>
      <Table head={["Tool", "Reads"]} rows={ENTRY_POINTS.map((e) => [e.tool, <Sources key={e.tool} items={e.entry} />])} />
      <p className="mt-3 text-sm text-slate-600">
        <Code>.claude/skills/</Code> and <Code>.claude/agents/</Code> are the only catalogs; no per-tool copy may exist. The
        always-loaded context (CLAUDE.md, slim AGENTS.md, the Constitution card, the workflow rule) stays under 8,000 tokens.
      </p>
      <Sources items={[{ label: "AGENTS.md", href: src("AGENTS.md") }, { label: "agent-context-budget.py", href: src("workspace/ci/agent-context-budget.py") }]} />
    </div>
  );
}
