import Link from "next/link";
import { CASE_CHAPTERS, CASE_LESSONS, CASE_TERMINALS, issue, src } from "@/lib/ai-sdlc-content";
import { ChapterBlock, HISTORY, HistoryChart, Terminal } from "@/components/CaseStudy";
import { Card, SectionTitle, Sources } from "@/components/FactoryUI";

export const metadata = {
  title: "Case study — building Notaire with an AI SDLC",
  description: "Step by step: how Notaire's AI SDLC evolved from prompts to a Constitution, gates, fleets and ratchets, with the data.",
};

const fmt = (n: number) => n.toLocaleString("en-US");

export default function CaseStudyPage() {
  const t = HISTORY.totals;
  const oct = HISTORY.months.find((m) => m.month === "2026-10");
  const jul = HISTORY.months.find((m) => m.month === "2026-07");
  const headline = [
    { value: fmt(t.commits), label: "commits", note: `${HISTORY.legacy_commits} of them from the 2014 desktop app` },
    { value: fmt(t.merged_prs), label: "merged pull requests", note: `${fmt(t.merged_prs_without_bots)} without Dependabot` },
    { value: "512 → 0", label: "architecture violations", note: "shrink-only baseline, four days" },
    {
      value: jul && oct ? `${(jul.bug_issues / jul.merged_prs).toFixed(2)} → ${(oct.bug_issues / oct.merged_prs).toFixed(2)}` : "—",
      label: "bug issues per merged PR",
      note: "July → October 2026",
    },
  ];

  return (
    <div>
      <section className="relative overflow-hidden rounded-3xl bg-factory-950 px-6 py-12 text-white shadow-xl sm:px-10 sm:py-16">
        <div className="absolute inset-0 opacity-[0.07]" aria-hidden style={{ backgroundImage: "repeating-linear-gradient(0deg,#fff 0 1px,transparent 1px 28px),repeating-linear-gradient(90deg,#fff 0 1px,transparent 1px 28px)" }} />
        <div className="absolute -right-20 -top-24 h-72 w-72 rounded-full bg-factory-400/30 blur-3xl" aria-hidden />
        <div className="relative max-w-3xl space-y-5">
          <p className="font-mono text-xs uppercase tracking-[0.25em] text-factory-200">Case study · Notaire · 2014 → 2026</p>
          <h1 className="text-4xl font-semibold tracking-tight sm:text-6xl" style={{ fontFamily: "var(--font-display)" }}>
            Fast agents, slow trust.
            <span className="block text-factory-200">How Notaire learned to ship AI-written code.</span>
          </h1>
          <p className="text-lg leading-relaxed text-slate-300">
            A legacy Java desktop app was rebuilt as Spring Boot, Next.js and PostgreSQL, mostly by AI agents and one human
            Owner. This is the step-by-step story of the process that made that safe: what broke, what each failure taught,
            and how the lessons became a framework any repository can adopt.
          </p>
          <div className="flex flex-wrap gap-3 pt-1">
            <a href="#legacy" className="rounded-full bg-white px-5 py-2 text-sm font-semibold text-slate-900 hover:bg-factory-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-factory-200">
              Start at chapter 1 ↓
            </a>
            <Link href="/ai-sdlc/framework/" className="rounded-full border border-white/30 px-5 py-2 text-sm font-semibold text-white hover:bg-white/10 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-factory-200">
              Skip to the framework
            </Link>
          </div>
        </div>
      </section>

      <dl className="mt-8 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {headline.map((h) => (
          <Card key={h.label}>
            <dt className="text-xs font-medium uppercase tracking-wider text-slate-500">{h.label}</dt>
            <dd className="mt-1 text-3xl font-semibold tabular-nums text-slate-900">{h.value}</dd>
            <dd className="mt-1 text-xs text-slate-500">{h.note}</dd>
          </Card>
        ))}
      </dl>
      <Sources items={[{ label: "ai-sdlc-history.json (snapshot)", href: src("github-page/lib/ai-sdlc-history.json") }, { label: "ai_sdlc_framework.md §5", href: src("docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md") }]} />

      <SectionTitle id="data">The arc in one chart</SectionTitle>
      <p className="max-w-3xl text-slate-600 leading-relaxed">
        Throughput grew while defects per change fell, but the process itself took up to a third of all merged work while
        it was being built. Both are part of the story.
      </p>
      <HistoryChart />

      <nav aria-label="Chapters" className="mt-12">
        <h2 className="font-mono text-xs uppercase tracking-[0.2em] text-slate-500">Eight chapters</h2>
        <ol className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
          {CASE_CHAPTERS.map((c, i) => (
            <li key={c.id}>
              <a href={`#${c.id}`} className="group flex h-full gap-3 rounded-xl border border-slate-200 bg-white p-3 hover:border-factory-400 focus-visible:outline focus-visible:outline-2 focus-visible:outline-violet-600">
                <span className="font-mono text-sm font-semibold text-factory-700">{String(i + 1).padStart(2, "0")}</span>
                <span className="text-sm leading-snug text-slate-700 group-hover:text-slate-900">{c.title}</span>
              </a>
            </li>
          ))}
        </ol>
      </nav>

      <div className="mt-6">
        {CASE_CHAPTERS.map((c, i) => (
          <ChapterBlock key={c.id} chapter={c} index={i} />
        ))}
      </div>

      <SectionTitle id="run">See it run</SectionTitle>
      <p className="max-w-3xl text-slate-600 leading-relaxed">
        Two real runs of the extracted kit: one on Notaire, one on a brand-new repository with nothing but git and Python.
      </p>
      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        {CASE_TERMINALS.map((term) => (
          <Terminal key={term.title} {...term} />
        ))}
      </div>

      <SectionTitle id="honest">What went wrong while writing this</SectionTitle>
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <h3 className="font-semibold text-slate-900">The tests broke the repository</h3>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            Pushing from a linked worktree, git exported <code className="font-mono text-[12px]">GIT_DIR</code> to the
            pre-push hook. Self-tests that build throwaway repositories ran <code className="font-mono text-[12px]">git init</code>{" "}
            against it and set <code className="font-mono text-[12px]">core.bare = true</code> in the shared config. The fix
            is a gate, not a note: preflight and the kit now drop hook-exported git variables, and tests prove it.
          </p>
          <Sources items={[{ label: "test_preflight_hook_env.py", href: src("workspace/tests/test_preflight_hook_env.py") }]} />
        </Card>
        <Card>
          <h3 className="font-semibold text-slate-900">Two agents took the same issue</h3>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">
            One session opened the issue and pushed its branch; another claimed it minutes later through the claim protocol.
            A pushed branch is not a claim. The lesson went into the framework: claim before branching, in every runtime.
          </p>
          <div className="mt-3 flex gap-2">
            {[1646, 1958].map((n) => (
              <a key={n} href={issue(n)} className="rounded-full bg-slate-100 px-2.5 py-0.5 font-mono text-[11px] text-slate-700 hover:bg-violet-100">#{n}</a>
            ))}
          </div>
        </Card>
      </div>

      <SectionTitle id="lessons">Lessons and trade-offs</SectionTitle>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {CASE_LESSONS.map((l) => (
          <Card key={l.title}>
            <h3 className="font-semibold text-slate-900">{l.title}</h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{l.body}</p>
            <p className="mt-3 border-t border-slate-100 pt-3 text-xs leading-relaxed text-slate-500">
              <span className="font-semibold uppercase tracking-wider text-factory-700">Trade-off · </span>
              {l.tradeoff}
            </p>
          </Card>
        ))}
      </div>

      <section className="mt-14 rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
        <h2 className="text-2xl font-semibold text-slate-900" style={{ fontFamily: "var(--font-display)" }}>
          Take the process, not the stack
        </h2>
        <p className="mt-2 max-w-2xl text-slate-600">
          The framework keeps everything that worked here and names no language, build tool or agent product. One policy,
          one profile, one zero-dependency kit.
        </p>
        <div className="mt-5 flex flex-wrap gap-3">
          <Link href="/ai-sdlc/framework/" className="rounded-full bg-slate-900 px-5 py-2 text-sm font-semibold text-white hover:bg-violet-800 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-violet-600">
            Explore the framework →
          </Link>
          <a href={src("docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md")} className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-800 hover:border-violet-400">
            Read the full report
          </a>
          <Link href="/ai-sdlc/lessons/" className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-800 hover:border-violet-400">
            All lessons
          </Link>
        </div>
      </section>
    </div>
  );
}
