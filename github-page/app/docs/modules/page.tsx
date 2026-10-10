import { MermaidDiagram } from "@/components/MermaidDiagram";
import { DOCS, MODULE_OWNERSHIP_CHART, MODULES } from "@/lib/docs-content";

export default function DocsModulesPage() {
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Module ownership
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Phase 0 of repository topology (#1197): keep one monorepo, give every folder a clear
          responsibility (ADR-026). Extraction happens later, only behind evidence gates.
        </p>
        <a
          className="inline-flex text-sm text-[#0A84FF] hover:underline"
          href={`${DOCS}/300-development/MODULE-OWNERSHIP.md`}
        >
          Full MODULE-OWNERSHIP.md on GitHub →
        </a>
      </header>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-neutral-900">Dependencies</h2>
        <MermaidDiagram chart={MODULE_OWNERSHIP_CHART} />
      </section>

      <section className="overflow-x-auto rounded-2xl border border-neutral-200 bg-white">
        <table className="min-w-full text-sm">
          <thead className="bg-neutral-50 text-left text-neutral-500">
            <tr>
              <th className="px-4 py-3 font-medium">Module</th>
              <th className="px-4 py-3 font-medium">Fleet</th>
              <th className="px-4 py-3 font-medium">Responsibility</th>
              <th className="px-4 py-3 font-medium">Verify</th>
            </tr>
          </thead>
          <tbody>
            {MODULES.map((m) => (
              <tr key={m.name} className="border-t border-neutral-100">
                <td className="px-4 py-3 font-mono text-xs text-neutral-900">{m.name}</td>
                <td className="px-4 py-3 text-neutral-600">{m.fleet}</td>
                <td className="px-4 py-3 text-neutral-700">{m.responsibility}</td>
                <td className="px-4 py-3 font-mono text-xs text-neutral-500">{m.verify}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <pre className="rounded-xl bg-neutral-900 text-neutral-100 text-xs p-4 overflow-x-auto">{`python3 workspace/modules.py list
python3 workspace/modules.py verify --all`}</pre>
    </div>
  );
}
