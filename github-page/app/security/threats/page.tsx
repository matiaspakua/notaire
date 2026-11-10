import { StrideExplorer } from "@/components/security/StrideExplorer";
import { T } from "@/components/site/T";
import { LINDDUN, STATUS_STYLE } from "@/lib/security-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/security/threats/");

export default function SecurityThreatsPage() {
  return (
    <div>
      <header className="mb-8 space-y-3">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700"><T text="Security · threats" /></p>
        <h1 className="font-display text-4xl font-semibold tracking-tight text-slate-900"><T text="STRIDE, then privacy" /></h1>
        <p className="max-w-3xl text-lg leading-relaxed text-slate-600">
          <T text="Pick a property. Each card is a requirement id from the system model, with the status recorded on 5 November 2026. Status is a design label, not the result of a penetration test." />
        </p>
      </header>
      <StrideExplorer />
      <h2 className="mb-4 mt-14 font-display text-2xl font-semibold text-slate-900"><T text="Privacy notes (LINDDUN)" /></h2>
      <p className="mb-4 max-w-3xl text-sm leading-relaxed text-slate-600">
        <T text="Personal data in a notarial file is identifiable on purpose. These rows say what the product does about that. They are not a legal opinion." />
      </p>
      <ul className="space-y-3">
        {LINDDUN.map((row) => (
          <li key={row.id} className="rounded-2xl border border-slate-200 bg-white p-4">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-mono text-sm font-semibold">{row.id}</span>
              <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${STATUS_STYLE[row.status]}`}>{row.status}</span>
              <span className="font-medium text-slate-900"><T text={row.name} /></span>
            </div>
            <p className="mt-2 text-sm text-slate-700"><T text={row.summary} /></p>
          </li>
        ))}
      </ul>
    </div>
  );
}
