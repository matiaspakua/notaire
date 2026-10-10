import { DOCS, REPO } from "@/lib/docs-content";

const sections = [
  {
    title: "Requirements",
    path: "100-business/101-requirements",
    body: "System survey (RS), SRS and the requirements CSV used for traceability.",
  },
  {
    title: "Use cases",
    path: "100-business/102-use-cases",
    body: "Business use-case catalog (CUxx). Every change must cite one of these.",
  },
  {
    title: "Actors",
    path: "100-business/103-actors",
    body: "Actors and hierarchy for the notarial domain.",
  },
  {
    title: "Traceability",
    path: "100-business/104-traceability",
    body: "Requirements ↔ use-case matrix — the bridge to GitHub issues and code.",
  },
  {
    title: "Manuals",
    path: "100-business/105-manuals",
    body: "Installation, system and user manuals (user PDF via Release — ADR-022).",
  },
];

export default function DocsBusinessPage() {
  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-3xl font-semibold text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Business documentation
        </h1>
        <p className="text-neutral-600 max-w-3xl leading-relaxed">
          Domain requirements, use cases and manuals. Markdown under{" "}
          <code className="text-xs bg-neutral-100 px-1 rounded">docs/100-business/</code> stays the
          source of truth; this page deep-links into GitHub for the full trees.
        </p>
      </header>

      <section className="grid sm:grid-cols-2 gap-4">
        {sections.map((s) => (
          <a
            key={s.path}
            href={`${DOCS}/${s.path}`}
            className="rounded-2xl border border-neutral-200 bg-white p-5 hover:border-cyan-400/50 transition-colors"
          >
            <h2 className="font-semibold text-neutral-900">{s.title}</h2>
            <p className="mt-2 text-sm text-neutral-600 leading-relaxed">{s.body}</p>
            <p className="mt-3 font-mono text-xs text-neutral-400">{s.path}/</p>
          </a>
        ))}
      </section>

      <a className="text-sm text-[#0A84FF] hover:underline" href={`${REPO}/tree/main/docs/100-business`}>
        docs/100-business/ on GitHub →
      </a>
    </div>
  );
}
