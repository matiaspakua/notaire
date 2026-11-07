import Link from "next/link";
import { QuestionPicker } from "@/components/docs/QuestionPicker";
import { READING_PATH } from "@/lib/artifacts-content";
import { DOCS, REPO } from "@/lib/docs-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/docs/");

const cards = [
  {
    href: "/docs/artifacts/",
    title: "Engineering artifacts",
    body: "The chain from requirement to runbook: requirements, use cases, design models, SAD and ADRs, OpenSpec, tests, threat models, pipelines and manuals, with counts read from the repository.",
  },
  {
    href: "/docs/stack/",
    title: "Technology stack",
    body: "Every technology and the job it does, with versions read from pom.xml, package.json and Compose at build time.",
  },
  {
    href: "/docs/business/",
    title: "Business",
    body: "Requirements, use cases (CUxx), actors, traceability matrix and manuals under docs/100-business/.",
  },
  {
    href: "/docs/modules/",
    title: "Module ownership",
    body: "Nine in-repo modules (ADR-026). One folder, one fleet, one verify — Owner tracker #1445; no repo split yet.",
  },
  {
    href: "/docs/architecture/",
    title: "Architecture",
    body: "SAD overview, every ADR with its status, Mermaid as the active diagram language (ADR-027).",
  },
  {
    href: "/docs/hexagonal/",
    title: "Hexagonal architecture",
    body: "Ports and adapters in the backend and front end, one request traced through a slice, and the ArchUnit rules that hold it.",
  },
  {
    href: "/docs/database/",
    title: "Database & Flyway",
    body: "How a schema change ships, the Flyway settings and why, and the full migration history.",
  },
  {
    href: "/docs/api/",
    title: "API contract & Bruno",
    body: "Code-first OpenAPI with breaking-change, type and reachability checks, and the Bruno suite that calls the live API.",
  },
  {
    href: "/docs/testing/",
    title: "Testing",
    body: "Pyramid, local preflight, and the release-tag suites. Bruno and Playwright stay with the testing module.",
  },
  {
    href: "/docs/security/",
    title: "Security docs",
    body: "Bridge from this tab to the interactive Security section and the architecture notes.",
  },
  {
    href: "/docs/roadmap/",
    title: "Roadmap",
    body: "Milestone history, delivery trains, requirements catalog, and parallel-agent lanes.",
  },
  {
    href: "/docs/releases/",
    title: "Releases",
    body: "Product semver cuts: backend, frontend, and the Flyway schema recorded with each tag.",
  },
];

export default function DocsHomePage() {
  return (
    <div className="space-y-10">
      <header className="space-y-4">
        <p className="text-xs uppercase tracking-[0.2em] text-brand-700 font-semibold">Technical documentation</p>
        <h1 className="text-4xl sm:text-5xl font-semibold tracking-tight text-neutral-900" style={{ fontFamily: "var(--font-display)" }}>
          Notaire knowledge base
        </h1>
        <p className="text-lg text-neutral-600 max-w-2xl leading-relaxed">
          Business and engineering docs rendered for the public site. Full Markdown trees stay in{" "}
          <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={`${DOCS}`}>
            docs/
          </a>{" "}
          on GitHub; this tab is the curated entry for business, modules, architecture, testing and
          DevSecOps.
        </p>
      </header>

      <section aria-labelledby="path-title" className="rounded-2xl border border-slate-200 bg-white p-6">
        <h2 id="path-title" className="text-xl font-semibold text-neutral-900">
          Read it as a story
        </h2>
        <p className="mt-1 text-sm text-neutral-600">
          New to Notaire? Follow these eight pages in order, from what the product does down to how each change is made,
          proved, protected and shipped.
        </p>
        <ol className="mt-4 grid gap-2 sm:grid-cols-2">
          {READING_PATH.map((p, i) => (
            <li key={p.href}>
              <Link href={p.href} className="group flex gap-3 rounded-xl border border-slate-200 px-3 py-2 transition-colors hover:border-brand-500">
                <span className="font-mono text-xs text-slate-500 pt-0.5">{i + 1}</span>
                <span>
                  <span className="block font-semibold text-neutral-900 group-hover:text-brand-700">{p.title}</span>
                  <span className="block text-sm text-neutral-600">{p.question}</span>
                </span>
              </Link>
            </li>
          ))}
        </ol>
      </section>

      <QuestionPicker />

      <div className="grid sm:grid-cols-2 gap-4">
        {cards.map((c) => (
          <Link
            key={c.href}
            href={c.href}
            className="group rounded-2xl border border-neutral-200 bg-white/80 p-6 hover:border-cyan-400/50 hover:shadow-lg transition-all"
          >
            <h2 className="text-xl font-semibold text-neutral-900 group-hover:text-brand-700 transition-colors">
              {c.title}
            </h2>
            <p className="mt-2 text-sm text-neutral-600 leading-relaxed">{c.body}</p>
          </Link>
        ))}
      </div>

      <aside className="rounded-2xl border border-dashed border-neutral-300 p-5 text-sm text-neutral-600">
        <strong className="text-neutral-900">Source of truth:</strong>{" "}
        <a className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current" href={REPO}>
          matiaspakua/notaire
        </a>
        . OpenSpec changes live under <code className="text-xs bg-neutral-100 px-1 rounded">docs/openspec/</code>.
        Delivery board:{" "}
        <a
          className="text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current"
          href="https://github.com/users/matiaspakua/projects/1"
        >
          Project #1
        </a>
        .
      </aside>
    </div>
  );
}
