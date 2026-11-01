import { MermaidDiagram } from "@/components/MermaidDiagram";
import { DOCS, SYSTEM_CONTEXT_CHART } from "@/lib/docs-content";
import { pageMetadata } from "@/lib/site";
import { adrs, sadVersion } from "@/lib/tech-facts";

export const metadata = pageMetadata("/docs/architecture/");

const STATUS_STYLE: Record<string, string> = {
  Accepted: "bg-emerald-50 text-emerald-800 border-emerald-200",
  Proposed: "bg-amber-50 text-amber-800 border-amber-200",
};

export default function DocsArchitecturePage() {
  const list = adrs();
  const sad = sadVersion();
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Architecture
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Software Architecture Document (arc42), Architecture Decision Records, and the active
          diagram standard. Deep-link into the repository for the full SAD body.
        </p>
      </header>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">System context</h2>
        <MermaidDiagram chart={SYSTEM_CONTEXT_CHART} />
      </section>

      <section className="grid sm:grid-cols-2 gap-4">
        <a
          href={`${DOCS}/200-architecture/201-SAD/sad.md`}
          className="rounded-2xl border border-neutral-200 bg-white p-5 hover:border-cyan-400/50 transition-colors"
        >
          <h3 className="font-semibold text-neutral-900">SAD</h3>
          <p className="mt-2 text-sm text-neutral-600">
            Goals, building blocks, deployment, quality requirements, risks and roadmap{sad ? ` (v${sad})` : ""}.
          </p>
        </a>
        <a
          href={`${DOCS}/200-architecture/202-ADR/README.md`}
          className="rounded-2xl border border-neutral-200 bg-white p-5 hover:border-cyan-400/50 transition-colors"
        >
          <h3 className="font-semibold text-neutral-900">ADR index</h3>
          <p className="mt-2 text-sm text-neutral-600">
            {list.length} decision records, ADR-{list[0]?.id} to ADR-{list.at(-1)?.id}. New active diagrams must be Mermaid (ADR-027).
          </p>
        </a>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Owner decisions pending</h2>
        <p className="text-sm text-neutral-600 max-w-3xl leading-relaxed">
          Topology prep Phase 0 is on main (<strong>P0.1–P0.5 Done</strong>); these Owner choices
          still block marking the work done. Agents must not invent answers. Evidence:{" "}
          <a
            className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
            href={`${DOCS}/300-development/REPO-METRICS-BASELINE.md`}
          >
            REPO-METRICS-BASELINE.md
          </a>{" "}
          and{" "}
          <a
            className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
            href={`${DOCS}/300-development/REPO-SPLIT-PLAN.md`}
          >
            REPO-SPLIT-PLAN.md
          </a>
          .
        </p>
        <ul className="space-y-2 text-sm">
          <li>
            <a
              className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
              href="https://github.com/matiaspakua/notaire/issues/1445"
            >
              #1445
            </a>
            {" — "}
            Live umbrella: accept ADR-024 staged satellites (A+B) vs eight-repo C
          </li>
          <li>
            <a
              className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
              href="https://github.com/matiaspakua/notaire/issues/1438"
            >
              #1438
            </a>
            {" — "}
            P0.6 <code className="text-xs bg-neutral-100 px-1 rounded">deprecated/</code> Option A /
            B / C (history purge deferred by ADR-022)
          </li>
          <li>
            <a
              className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
              href="https://github.com/matiaspakua/notaire/issues/1226"
            >
              #1226
            </a>
            {" — "}
            LICENSE file (README already links one that does not exist)
          </li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Enterprise readiness reports</h2>
        <p className="text-sm text-neutral-600 max-w-3xl leading-relaxed">
          Measured assessment and documentation audit for CU76 — evidence for topology and quality
          decisions without inventing Owner answers.
        </p>
        <ul className="space-y-2 text-sm">
          <li>
            <a
              className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
              href={`${DOCS}/300-development/ASSESSMENT-2026-10.md`}
            >
              ASSESSMENT-2026-10.md
            </a>
            {" — "}
            Full-system assessment (#1242)
          </li>
          <li>
            <a
              className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
              href={`${DOCS}/300-development/DOCUMENTATION-AUDIT-2026-10.md`}
            >
              DOCUMENTATION-AUDIT-2026-10.md
            </a>
            {" — "}
            End-to-end documentation audit (#921)
          </li>
        </ul>
      </section>

      <section className="space-y-3" aria-labelledby="adr-index">
        <h2 id="adr-index" className="text-lg font-semibold text-neutral-900">
          All decisions
        </h2>
        <p className="text-sm text-neutral-600 max-w-3xl leading-relaxed">
          Read from the ADR folder when the site is built, with each record&apos;s own status.
        </p>
        <ol className="divide-y divide-neutral-200 rounded-2xl border border-neutral-200 bg-white">
          {list.map((a) => (
            <li key={a.id} className="flex flex-wrap items-baseline gap-x-3 gap-y-1 px-4 py-2.5">
              <span className="w-16 shrink-0 font-mono text-xs text-neutral-500">ADR-{a.id}</span>
              <a
                className="min-w-0 flex-1 text-sm text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
                href={`${DOCS}/200-architecture/202-ADR/${a.file}`}
              >
                {a.title}
              </a>
              <span
                className={`rounded-full border px-2 py-0.5 text-[11px] font-medium ${STATUS_STYLE[a.status] ?? "bg-neutral-50 text-neutral-700 border-neutral-200"}`}
              >
                {a.status}
              </span>
            </li>
          ))}
        </ol>
      </section>
    </div>
  );
}
