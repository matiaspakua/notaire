import { PLAYBOOK, PRODUCTION_READY, src } from "@/lib/ai-sdlc-content";
import { Card, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";

export default function PlaybookPage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Playbook"
        title="Apply an AI SDLC to your own project"
        lead={
          <>
            What carries over from Notaire to any codebase that wants AI agents to ship production-ready software. Each step
            links the Notaire file that implements it, so you can copy the shape, not just the idea.
          </>
        }
      />
      <ol className="space-y-4">
        {PLAYBOOK.map((p, i) => (
          <li key={p.title}>
            <Card className="flex gap-5">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-amber-500 text-sm font-bold text-white">
                {i + 1}
              </span>
              <div>
                <h3 className="font-semibold text-slate-900">{p.title}</h3>
                <p className="mt-2 text-sm text-slate-600 leading-relaxed">{p.body}</p>
                <Sources items={p.notaire} />
              </div>
            </Card>
          </li>
        ))}
      </ol>

      <SectionTitle>What "production-ready" means here</SectionTitle>
      <p className="text-sm text-slate-600 max-w-3xl">
        The factory is only as good as the evidence it leaves. These are the artifacts Notaire&apos;s gates produce or check
        before a change counts as shipped.
      </p>
      <div className="mt-4 grid md:grid-cols-2 gap-3">
        {PRODUCTION_READY.map((p) => (
          <Card key={p.need} className="flex items-center justify-between gap-4">
            <span className="text-sm text-slate-800">{p.need}</span>
            <Sources items={[p.evidence]} />
          </Card>
        ))}
      </div>

      <SectionTitle>Guiding principles for adapting</SectionTitle>
      <ul className="list-disc pl-5 space-y-2 text-sm text-slate-700 max-w-3xl">
        <li><strong>Adapt, do not replace.</strong> Keep vendor engines and skills; extend them through their own extension points.</li>
        <li><strong>One source of truth.</strong> The governance process lives in one document; tools point at it and never copy it.</li>
        <li><strong>Agent-agnostic.</strong> Deliver the rules through a channel every tool reads (the spec CLI), not a feature of one assistant.</li>
        <li><strong>Mechanical over aspirational.</strong> A rule without an exit code is a wish.</li>
      </ul>
      <Sources items={[{ label: "NOTAIRE-ADAPTATIONS §11 — adapting OpenSpec to a new project", href: src("docs/openspec/NOTAIRE-ADAPTATIONS.md") }]} />
    </div>
  );
}
