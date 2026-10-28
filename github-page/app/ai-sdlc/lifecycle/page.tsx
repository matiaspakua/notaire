import { GATES, LIFECYCLE_FLOW, LIFECYCLE_PHASES, src } from "@/lib/ai-sdlc-content";
import { Card, Code, Diagram, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/lifecycle/");

const tone: Record<string, string> = {
  violet: "border-violet-300 bg-violet-50 text-violet-800",
  cyan: "border-cyan-300 bg-cyan-50 text-cyan-800",
  emerald: "border-emerald-300 bg-emerald-50 text-emerald-800",
  amber: "border-amber-300 bg-amber-50 text-amber-800",
};

export default function LifecyclePage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Lifecycle"
        title="From Issue to closed Issue, step by step"
        lead={
          <>
            The Constitution defines one mandatory, sequential workflow of 24 steps for every change: feature, bug fix,
            refactor, docs, CI or schema. No step may be skipped. Five hard gates stop the work until their conditions hold,
            whether a human or an agent is doing it.
          </>
        }
      />
      <Diagram chart={LIFECYCLE_FLOW} caption="CONSTITUTION.md §5 Official SDLC Workflow. The yellow hexagons are the hard gates of §6." />

      {LIFECYCLE_PHASES.map((ph) => (
        <section key={ph.phase}>
          <SectionTitle id={ph.phase.toLowerCase()}>
            <span className={`mr-3 inline-block rounded-full border px-3 py-0.5 text-sm ${tone[ph.tone]}`}>{ph.phase}</span>
            {ph.phase === "Plan" && "Understand and specify"}
            {ph.phase === "Prove" && "Tests first, then code"}
            {ph.phase === "Publish" && "Document, commit, open the PR"}
            {ph.phase === "Ship" && "Review, merge, deploy, verify"}
          </SectionTitle>
          <ol className="grid md:grid-cols-2 gap-4">
            {ph.steps.map((s) => (
              <li key={s.n}>
                <Card className="h-full">
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-xs text-slate-500">step {s.n}</span>
                    {s.gate && (
                      <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[11px] font-semibold text-amber-800">
                        Gate {s.gate}
                      </span>
                    )}
                  </div>
                  <h3 className="mt-1 font-semibold text-slate-900">{s.title}</h3>
                  <p className="mt-2 text-sm text-slate-600 leading-relaxed">{s.what}</p>
                  {s.check && (
                    <p className="mt-3 text-xs text-slate-500">
                      Checked by <Code>{s.check}</Code>
                    </p>
                  )}
                </Card>
              </li>
            ))}
          </ol>
        </section>
      ))}

      <SectionTitle id="gates">The five hard gates</SectionTitle>
      <div className="grid md:grid-cols-5 gap-3">
        {GATES.map((g) => (
          <Card key={g.n} className="border-amber-200">
            <div className="text-2xl font-bold text-amber-600">G{g.n}</div>
            <div className="mt-1 text-xs font-semibold uppercase tracking-wide text-slate-500">{g.name}</div>
            <ul className="mt-3 space-y-1 text-sm text-slate-700 list-disc pl-4">
              {g.items.map((i) => (
                <li key={i}>{i}</li>
              ))}
            </ul>
            <p className="mt-3 text-xs text-slate-500 leading-relaxed">{g.machine}</p>
          </Card>
        ))}
      </div>

      <SectionTitle>Definition of Done</SectionTitle>
      <Card>
        <p className="text-sm text-slate-700 leading-relaxed">
          A change is Done only when the Issue is linked to a Use Case with acceptance criteria, the specification passed
          Gate 1, tests were written first and seen failing, the full suite and coverage ratchet are green, Playwright is
          green for UI changes, permanent docs are updated without duplication, commits are atomic and conventional, the
          PR is green and reviewed, and the change is merged, deployed, smoke-tested and its Issue closed.
        </p>
        <Sources items={[{ label: "CONSTITUTION.md §3 Definition of Done", href: src("CONSTITUTION.md") }, { label: "tasks.md template (12 groups)", href: src("docs/openspec/schemas/notaire-sdlc/templates/tasks.md") }]} />
      </Card>
    </div>
  );
}
