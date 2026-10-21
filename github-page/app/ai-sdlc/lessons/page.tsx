import { LESSONS, issue } from "@/lib/ai-sdlc-content";
import { Card, PageHeader, Sources } from "@/components/FactoryUI";

export default function LessonsPage() {
  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Lessons learned"
        title="Every incident became a rule"
        lead={
          <>
            The factory was not designed in one go. Each rule below exists because something went wrong while agents were
            building Notaire. The fix was written into a permanent document and, where possible, into a test or a script,
            so the next agent cannot repeat it.
          </>
        }
      />
      <div className="grid md:grid-cols-2 gap-4">
        {LESSONS.map((l) => (
          <Card key={l.title}>
            <h3 className="font-semibold text-slate-900">{l.title}</h3>
            <p className="mt-3 text-xs font-semibold uppercase tracking-wide text-red-600">What happened</p>
            <p className="mt-1 text-sm text-slate-600 leading-relaxed">{l.happened}</p>
            <p className="mt-3 text-xs font-semibold uppercase tracking-wide text-emerald-700">Rule now</p>
            <p className="mt-1 text-sm text-slate-700 leading-relaxed">{l.rule}</p>
            <div className="mt-3 flex flex-wrap items-center gap-2">
              {l.refs.map((n) => (
                <a key={n} href={issue(n)} className="rounded-full bg-slate-100 px-2 py-0.5 font-mono text-[11px] text-slate-700 hover:bg-violet-100">
                  #{n}
                </a>
              ))}
            </div>
            <Sources items={[l.source]} />
          </Card>
        ))}
      </div>
    </div>
  );
}
