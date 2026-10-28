import Link from "next/link";
import { FACTORY_VS_PRODUCT, LESSONS, SOURCES_OF_TRUTH, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { Card, Diagram, SectionTitle, Sources } from "@/components/FactoryUI";
import { FACTORY_ACTS, pageMetadata } from "@/lib/site";
import { GateFlow } from "@/components/GateFlow";

export const metadata = pageMetadata("/ai-sdlc/");

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

  // The problem, told by the factory's own incidents (LESSONS), not by adjectives.
  const problems = ["Light green is not mergeable", "An invented Issue number passed Gate 1", "Models report success they have not earned", "Too much always-loaded context"]
    .map((t) => LESSONS.find((l) => l.title === t))
    .filter((l): l is (typeof LESSONS)[number] => Boolean(l));

  return (
    <div>
      <section className="relative isolate overflow-hidden rounded-3xl border border-factory-100 bg-gradient-to-br from-factory-50 via-white to-brand-50 px-6 py-12 sm:px-10 sm:py-16">
        <div aria-hidden="true" className="absolute -right-20 -top-28 -z-10 h-72 w-72 rounded-full bg-factory-200/60 blur-3xl" />
        <div className="max-w-3xl space-y-5">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-factory-700">AI SDLC · the factory, not the product</p>
          <h1 className="font-display text-4xl font-semibold leading-[1.05] tracking-tight text-slate-900 text-balance sm:text-6xl">
            The AI Factory that builds Notaire
          </h1>
          <p className="text-lg leading-relaxed text-slate-700 sm:text-xl">
            Notaire is a notarial management system: Next.js, Spring Boot and PostgreSQL. This section is about the{" "}
            <strong className="text-slate-900">software factory</strong> that builds it: a written Constitution, a
            spec-driven workflow, a crew of specialised AI agents and mechanical quality gates take every change from a
            GitHub Issue to a deployed, smoke-tested release, with a human Owner on every decision that matters.
          </p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Link href="/ai-sdlc/case-study/" className="rounded-full bg-factory-700 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-factory-800">
              Start the story →
            </Link>
            <Link href="/ai-sdlc/lifecycle/" className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-900 hover:border-slate-400">
              How a change flows
            </Link>
            <Link href="/ai-sdlc/playbook/" className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-900 hover:border-slate-400">
              Apply it to your project
            </Link>
          </div>
        </div>
      </section>

      <SectionTitle id="problem">The problem: speed without proof</SectionTitle>
      <p className="text-lg leading-relaxed text-slate-700">
        AI agents write code fast. Left to themselves they also skip steps, cite issues that do not exist and report
        green runs that never happened. Notaire hit every one of these. Each incident below became a rule, and then a
        command with an exit code.
      </p>
      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {problems.map((l) => (
          <Card key={l.title}>
            <h3 className="font-semibold text-slate-900">{l.title}</h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{l.happened}</p>
            <p className="mt-3 border-t border-slate-100 pt-3 text-sm leading-relaxed text-slate-800">
              <span className="font-semibold text-factory-700">Now: </span>
              {l.rule}
            </p>
          </Card>
        ))}
      </div>
      <p className="mt-3 text-sm text-slate-600">
        All of them, with the issues that fixed them, are in <Link href="/ai-sdlc/lessons/">Lessons</Link>.
      </p>

      <SectionTitle id="answer">The answer: a factory with gates</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        Every change, by a human or an agent, takes the same path. A gate opens only when its command exits as expected.
      </p>
      <GateFlow />

      <SectionTitle id="story">The story in five acts</SectionTitle>
      <ol className="relative mt-6 space-y-4 border-l-2 border-factory-200 pl-6 sm:pl-8">
        {FACTORY_ACTS.map((act, i) => (
          <li key={act.label} className="relative">
            <span aria-hidden="true" className="absolute -left-[2.15rem] top-4 flex h-7 w-7 items-center justify-center rounded-full bg-factory-700 font-mono text-xs font-bold text-white sm:-left-[2.65rem]">
              {i + 1}
            </span>
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)]">
              <h3 className="font-display text-xl font-semibold text-slate-900">{act.label.replace(/^[IVX]+ · /, "")}</h3>
              <ul className="mt-3 grid gap-2 sm:grid-cols-2">
                {act.pages.map((p) => (
                  <li key={p.href}>
                    <Link href={p.href} className="group block rounded-xl px-3 py-2 hover:bg-factory-50">
                      <span className="font-semibold text-factory-700 group-hover:underline">{p.title}</span>
                      <span className="block text-sm text-slate-600">{p.summary}</span>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          </li>
        ))}
      </ol>

      <SectionTitle id="numbers">The factory, counted</SectionTitle>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.label} className="text-center">
            <div className="font-display text-3xl font-semibold tabular-nums text-slate-900">{s.value}</div>
            <div className="mt-1 text-xs text-slate-600">{s.label}</div>
          </Card>
        ))}
      </div>
      <p className="mt-2 text-xs text-slate-600">
        Counted from the repository when this site was built. More in <Link href="/ai-sdlc/results/">Results</Link>.
      </p>

      <SectionTitle>Factory vs. product</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        The factory and the product live in one monorepo but answer different questions. The{" "}
        <Link href="/docs/">Technical docs</Link> describe <em>what Notaire does</em>: use cases, architecture, modules.
        The factory describes <em>how any change to Notaire is made</em>, by a human or by an agent, and it is the same
        for every module and every tool.
      </p>
      <Diagram chart={FACTORY_VS_PRODUCT} caption="An Issue enters the factory; only a reviewed, green pull request reaches the product. Production feedback becomes new Issues." />

      <SectionTitle>Six principles</SectionTitle>
      <div className="grid gap-4 md:grid-cols-3">
        {principles.map((p) => (
          <Card key={p.k}>
            <h3 className="font-semibold text-slate-900">{p.k}</h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{p.v}</p>
          </Card>
        ))}
      </div>
      <Sources items={[{ label: "CONSTITUTION.md §2 Principles", href: src("CONSTITUTION.md") }, { label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") }]} />

      <SectionTitle>Sources of truth</SectionTitle>
      <p className="text-sm text-slate-600">These pages summarise; the repository decides. When they differ, the files below win.</p>
      <Sources items={SOURCES_OF_TRUTH} />
    </div>
  );
}
