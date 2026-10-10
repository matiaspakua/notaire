import Link from "next/link";
import { DOCS, REPO } from "@/lib/docs-content";

const cards = [
  {
    href: "/docs/modules/",
    title: "Module ownership",
    body: "Nine in-repo modules (ADR-026). One folder, one fleet, one verify — prepare #1197 without splitting repos.",
  },
  {
    href: "/docs/architecture/",
    title: "Architecture",
    body: "SAD overview, ADR index, Mermaid as the active diagram language (ADR-027).",
  },
  {
    href: "/docs/testing/",
    title: "Testing",
    body: "Pyramid, preflight, heavy-CI merge gate, Bruno and Playwright ownership.",
  },
  {
    href: "/docs/security/",
    title: "Security & DevSecOps",
    body: "Auth, audit, CodeQL/Trivy/DAST, rulesets, and the security module map.",
  },
];

export default function DocsHomePage() {
  return (
    <div className="space-y-10">
      <header className="space-y-4">
        <p className="text-xs uppercase tracking-[0.2em] text-[#0A84FF] font-semibold">Technical documentation</p>
        <h1 className="text-4xl sm:text-5xl font-semibold tracking-tight text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Notaire knowledge base
        </h1>
        <p className="text-lg text-neutral-600 max-w-2xl leading-relaxed">
          Business and engineering docs rendered for the public site. Full Markdown trees stay in{" "}
          <a className="text-[#0A84FF] underline-offset-2 hover:underline" href={`${DOCS}`}>
            docs/
          </a>{" "}
          on GitHub; this tab is the curated entry for modules, architecture, testing and DevSecOps.
        </p>
      </header>

      <div className="grid sm:grid-cols-2 gap-4">
        {cards.map((c) => (
          <Link
            key={c.href}
            href={c.href}
            className="group rounded-2xl border border-neutral-200 bg-white/80 p-6 hover:border-cyan-400/50 hover:shadow-lg transition-all"
          >
            <h2 className="text-xl font-semibold text-neutral-900 group-hover:text-[#0A84FF] transition-colors">
              {c.title}
            </h2>
            <p className="mt-2 text-sm text-neutral-600 leading-relaxed">{c.body}</p>
          </Link>
        ))}
      </div>

      <aside className="rounded-2xl border border-dashed border-neutral-300 p-5 text-sm text-neutral-600">
        <strong className="text-neutral-900">Source of truth:</strong>{" "}
        <a className="text-[#0A84FF] hover:underline" href={REPO}>
          matiaspakua/notaire
        </a>
        . OpenSpec changes live under <code className="text-xs bg-neutral-100 px-1 rounded">docs/openspec/</code>.
        Delivery board:{" "}
        <a
          className="text-[#0A84FF] hover:underline"
          href="https://github.com/users/matiaspakua/projects/1"
        >
          Project #1
        </a>
        .
      </aside>
    </div>
  );
}
