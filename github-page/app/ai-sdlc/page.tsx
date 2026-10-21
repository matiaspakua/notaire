import Link from "next/link";
import { FACTORY_VS_PRODUCT, SOURCES_OF_TRUTH, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Diagram, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";

const pages = [
  { href: "/ai-sdlc/lifecycle/", title: "Lifecycle", body: "The 24 mandatory steps from Issue to closed Issue, grouped into Plan, Prove, Publish and Ship, with the five hard gates." },
  { href: "/ai-sdlc/guardrails/", title: "Guardrails", body: "Nine layers, from the Constitution to the Owner, that keep agents inside the process. Each layer is enforced by a file you can read." },
  { href: "/ai-sdlc/quality-gates/", title: "Quality gates & CI", body: "Local gates, light and heavy CI, the merge gate, path-scoped CI, security cases, release and deploy." },
  { href: "/ai-sdlc/openspec/", title: "OpenSpec adaptations", body: "A forked spec-driven schema, Constitution context injection, the Explore → Issue → Propose gap, and archive-in-the-PR." },
  { href: "/ai-sdlc/agents/", title: "Agents & skills", body: "Owner, Foreman and Workers; the specialist crew; skills by SDLC stage; the Cloud fleet and the local-model harness." },
  { href: "/ai-sdlc/governance/", title: "Governance", body: "Issue lifecycle, label families, milestone trains, branch/commit/PR conventions, and the PR flow end to end." },
  { href: "/ai-sdlc/lessons/", title: "Lessons learned", body: "Incidents that became rules: light-green merges, invented issue numbers, models claiming unearned success, and more." },
  { href: "/ai-sdlc/playbook/", title: "Playbook", body: "How to apply an AI SDLC to your own project and reach production-ready software." },
];

const principles = [
  { k: "Policy first", v: "One Constitution, agent-agnostic, above every other document. Tools implement it; they never redefine it." },
  { k: "Spec before code", v: "No implementation without an Issue, a Use Case and an OpenSpec specification (Gate 1)." },
  { k: "Red before green", v: "Tests are written and observed failing before implementation (Gate 2). A test you never saw fail proves nothing." },
  { k: "Machines check machines", v: "Gates are commands with exit codes. A model's own claim of \"all green\" is never trusted." },
  { k: "Traceable end to end", v: "Issue → Use Case → spec → tests → code → docs → PR → deploy, recorded in each change's traceability ledger." },
  { k: "Humans own decisions", v: "The Owner approves specifications, grants exceptions and merges policy. Agents write down Owner-only steps." },
];

export default function FactoryHome() {
  const f = loadFactoryFacts();
  const stats = [
    { label: "skills in the catalog", value: fmt(f.skills) },
    { label: "agent definitions", value: fmt(f.agents) },
    { label: "CI/CD workflows", value: fmt(f.workflows) },
    { label: "OpenSpec changes archived", value: fmt(f.archivedChanges) },
    { label: "living capability specs", value: fmt(f.livingSpecs) },
    { label: "process scripts (workspace/sdlc)", value: fmt(f.sdlcScripts) },
    { label: "Playwright E2E suites", value: fmt(f.e2eSuites) },
    { label: "ADRs", value: fmt(f.adrs) },
  ];

  return (
    <div>
      <section className="relative overflow-hidden rounded-3xl bg-slate-950 px-8 py-14 text-white shadow-xl">
        <div className="absolute -top-24 -right-24 h-72 w-72 rounded-full bg-violet-600/40 blur-3xl" />
        <div className="absolute -bottom-24 -left-16 h-72 w-72 rounded-full bg-amber-500/30 blur-3xl" />
        <div className="relative space-y-5 max-w-3xl">
          <p className="text-xs uppercase tracking-[0.25em] text-violet-300 font-semibold">AI SDLC · the factory, not the product</p>
          <h1 className="text-4xl sm:text-6xl font-semibold tracking-tight" style={{ fontFamily: "var(--font-display)" }}>
            The AI Factory that builds Notaire
          </h1>
          <p className="text-lg text-slate-300 leading-relaxed">
            Notaire is a notarial management system: Next.js, Spring Boot and PostgreSQL. This section is about something
            else: the <strong className="text-white">software factory</strong> that builds it. A written Constitution, a
            spec-driven workflow, a crew of specialized AI agents, and layered, mechanical quality gates take every change
            from a GitHub Issue to a deployed, smoke-tested release, with a human Owner on every decision that matters.
          </p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Link href="/ai-sdlc/lifecycle/" className="rounded-full bg-white px-5 py-2 text-sm font-semibold text-slate-900 hover:bg-violet-100">
              Walk the lifecycle →
            </Link>
            <Link href="/ai-sdlc/playbook/" className="rounded-full border border-white/30 px-5 py-2 text-sm font-semibold text-white hover:bg-white/10">
              Apply it to your project
            </Link>
          </div>
        </div>
      </section>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mt-8">
        {stats.map((s) => (
          <Card key={s.label} className="text-center">
            <div className="text-3xl font-semibold text-slate-900 tabular-nums">{s.value}</div>
            <div className="mt-1 text-xs text-slate-500">{s.label}</div>
          </Card>
        ))}
      </div>
      <p className="mt-2 text-xs text-slate-400">Counted from the repository when this site was built.</p>

      <SectionTitle>Factory vs. product</SectionTitle>
      <p className="text-slate-600 max-w-3xl leading-relaxed">
        The factory and the product live in one monorepo but answer different questions. The product docs (
        <Link className="text-violet-700 hover:underline" href="/docs/">Docs tab</Link>) describe <em>what Notaire does</em>:
        use cases, architecture, modules. The factory describes <em>how any change to Notaire is made</em>, by a human or
        by an agent, and it is the same for every module and every tool.
      </p>
      <Diagram chart={FACTORY_VS_PRODUCT} caption="An Issue enters the factory; only a reviewed, green pull request reaches the product. Production feedback becomes new Issues." />

      <SectionTitle>Six principles</SectionTitle>
      <div className="grid md:grid-cols-3 gap-4">
        {principles.map((p) => (
          <Card key={p.k}>
            <h3 className="font-semibold text-slate-900">{p.k}</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">{p.v}</p>
          </Card>
        ))}
      </div>
      <Sources items={[{ label: "CONSTITUTION.md §2 Principles", href: src("CONSTITUTION.md") }, { label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") }]} />

      <SectionTitle>Explore the factory</SectionTitle>
      <div className="grid sm:grid-cols-2 gap-4">
        {pages.map((p) => (
          <Link key={p.href} href={p.href} className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-sm hover:border-violet-400 hover:shadow-lg transition-all">
            <h3 className="text-lg font-semibold text-slate-900 group-hover:text-violet-700">{p.title}</h3>
            <p className="mt-2 text-sm text-slate-600 leading-relaxed">{p.body}</p>
          </Link>
        ))}
      </div>

      <SectionTitle>Sources of truth</SectionTitle>
      <p className="text-sm text-slate-600">These pages summarize; the repository decides. When they differ, the files below win.</p>
      <Sources items={SOURCES_OF_TRUTH} />
    </div>
  );
}
