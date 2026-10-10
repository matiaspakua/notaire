import { MermaidDiagram } from "@/components/MermaidDiagram";
import { DOCS, SYSTEM_CONTEXT_CHART } from "@/lib/docs-content";

const adrs = [
  {
    id: "022",
    title: "Git history / large binaries — pending Owner decision (#1435)",
    file: "ADR-022-git-history-rewrite-and-large-binaries.md",
  },
  { id: "024", title: "Repository topology (Proposed)", file: "ADR-024-repository-topology.md" },
  { id: "025", title: "Retire notaire-shared", file: "ADR-025-retire-notaire-shared.md" },
  { id: "026", title: "Module separation inside the repo", file: "ADR-026-module-separation.md" },
  { id: "027", title: "Mermaid diagram language", file: "ADR-027-mermaid-diagrams.md" },
];

export default function DocsArchitecturePage() {
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
            Goals, building blocks, deployment, quality requirements, risks and roadmap (v3.2).
          </p>
        </a>
        <a
          href={`${DOCS}/200-architecture/202-ADR/README.md`}
          className="rounded-2xl border border-neutral-200 bg-white p-5 hover:border-cyan-400/50 transition-colors"
        >
          <h3 className="font-semibold text-neutral-900">ADR index</h3>
          <p className="mt-2 text-sm text-neutral-600">
            ADR-001 … ADR-027. New active diagrams must be Mermaid (ADR-027).
          </p>
        </a>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Recent decisions</h2>
        <ul className="space-y-2">
          {adrs.map((a) => (
            <li key={a.id}>
              <a
                className="text-sm text-[#0A84FF] hover:underline"
                href={`${DOCS}/200-architecture/202-ADR/${a.file}`}
              >
                ADR-{a.id} — {a.title}
              </a>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
