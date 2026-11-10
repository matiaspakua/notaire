"use client";

import Link from "next/link";
import { Card, Diagram, SectionTitle, Sources } from "@/components/FactoryUI";
import { FlowDiagram, type FlowEdge, type FlowNode, type FlowStep } from "@/components/diagrams/FlowDiagram";
import { GateLine } from "@/components/figures/gateline/figure";
import { ReadingPath } from "@/components/site/ReadingPath";
import { usePreferences } from "@/components/site/Preferences";
import { FACTORY_VS_PRODUCT, SOURCES_OF_TRUTH, src } from "@/lib/ai-sdlc-content";
import { actLabel, factoryCopy, factoryPageSummary, factoryPageTitle, lessonCopy } from "@/lib/i18n";
import { FACTORY_ACTS } from "@/lib/site";

type Problem = { title: string; happened: string; rule: string };
type Stat = { key: keyof ReturnType<typeof factoryCopy>["stat"]; value: string };

export function FactoryHomeView({
  stats,
  problems,
  flow,
}: {
  stats: Stat[];
  problems: Problem[];
  flow: { nodes: FlowNode[]; edges: FlowEdge[]; steps: FlowStep[]; width: number; height: number };
}) {
  const { locale } = usePreferences();
  const c = factoryCopy(locale);

  return (
    <div>
      <section className="hero-wash relative isolate overflow-hidden rounded-3xl border border-factory-100 px-6 py-12 sm:px-10 sm:py-16">
        <div className="max-w-3xl space-y-5">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-factory-700">{c.kicker}</p>
          <h1 className="font-display text-4xl font-semibold leading-[1.05] tracking-tight text-slate-900 text-balance sm:text-6xl">{c.title}</h1>
          <p className="text-lg leading-relaxed text-slate-700 sm:text-xl">{c.lead}</p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Link href="/ai-sdlc/case-study/" className="rounded-full bg-factory-700 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-factory-800">
              {c.start} →
            </Link>
            <Link href="/ai-sdlc/lifecycle/" className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-900 hover:border-slate-400">
              {c.flow}
            </Link>
            <Link href="/ai-sdlc/playbook/" className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-semibold text-slate-900 hover:border-slate-400">
              {c.apply}
            </Link>
          </div>
        </div>
      </section>

      <ReadingPath />

      <SectionTitle id="problem">{c.problemTitle}</SectionTitle>
      <p className="text-lg leading-relaxed text-slate-700">{c.problemLead}</p>
      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {problems.map((problem) => {
          const text = lessonCopy(locale, problem.title, problem.happened, problem.rule);
          return (
            <Card key={problem.title}>
              <h3 className="font-semibold text-slate-900">{problem.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-600">{text.happened}</p>
              <p className="mt-3 border-t border-slate-100 pt-3 text-sm leading-relaxed text-slate-800">
                <span className="font-semibold text-factory-700">{c.now}</span>
                {text.rule}
              </p>
            </Card>
          );
        })}
      </div>
      <p className="mt-3 text-sm text-slate-600">
        <Link href="/ai-sdlc/lessons/">{c.lessonsLink}</Link>. {c.allLessons}
      </p>

      <SectionTitle id="answer">{c.answerTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        {c.answerLead}{" "}
        <Link href="/ai-sdlc/lifecycle/" className="font-semibold text-factory-700 hover:underline">{c.lifecycle}</Link>
        {" · "}
        <Link href="/ai-sdlc/anatomy/" className="font-semibold text-factory-700 hover:underline">{c.anatomy}</Link>
      </p>
      <GateLine />
      <FlowDiagram id="factory" title={c.answerTitle} desc={c.answerLead} caption={c.numbersNote} {...flow} />

      <SectionTitle id="story">{c.storyTitle}</SectionTitle>
      <ol className="relative mt-6 space-y-4 border-l-2 border-factory-200 pl-6 sm:pl-8">
        {FACTORY_ACTS.map((act, i) => (
          <li key={act.label} className="relative">
            <span aria-hidden="true" className="absolute -left-[2.15rem] top-4 flex h-7 w-7 items-center justify-center rounded-full bg-factory-700 font-mono text-xs font-bold text-white sm:-left-[2.65rem]">
              {i + 1}
            </span>
            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-[var(--shadow-card)]">
              <h3 className="font-display text-xl font-semibold text-slate-900">{actLabel(act.label, locale).replace(/^[IVX]+ · /, "")}</h3>
              <ul className="mt-3 grid gap-2 sm:grid-cols-2">
                {act.pages.map((p) => (
                  <li key={p.href}>
                    <Link href={p.href} className="group block rounded-xl px-3 py-2 hover:bg-factory-50">
                      <span className="font-semibold text-factory-700 group-hover:underline">{factoryPageTitle(p.href, locale, p.title)}</span>
                      <span className="block text-sm text-slate-600">{factoryPageSummary(p.href, locale, p.summary)}</span>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          </li>
        ))}
      </ol>

      <SectionTitle id="numbers">{c.numbersTitle}</SectionTitle>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.key} className="text-center">
            <div className="font-display text-3xl font-semibold tabular-nums text-slate-900">{s.value}</div>
            <div className="mt-1 text-xs text-slate-600">{c.stat[s.key]}</div>
          </Card>
        ))}
      </div>
      <p className="mt-2 text-xs text-slate-600">
        {c.numbersNote.split(locale === "es" ? "Resultados" : "Results")[0]}
        <Link href="/ai-sdlc/results/">{c.resultsLink}</Link>.
      </p>

      <SectionTitle>{c.versusTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        {c.versus} <Link href="/docs/">{c.docsLink}</Link>.
      </p>
      <Diagram chart={FACTORY_VS_PRODUCT} caption={c.versus} />

      <SectionTitle>{c.principlesTitle}</SectionTitle>
      <div className="grid gap-4 md:grid-cols-3">
        {c.principles.map((p) => (
          <Card key={p.k}>
            <h3 className="font-semibold text-slate-900">{p.k}</h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-600">{p.v}</p>
          </Card>
        ))}
      </div>
      <Sources items={[{ label: "CONSTITUTION.md §2 Principles", href: src("CONSTITUTION.md") }, { label: "local-ai/sdlc/AI-SDLC.md", href: src("local-ai/sdlc/AI-SDLC.md") }]} />

      <SectionTitle>{c.sourcesTitle}</SectionTitle>
      <p className="text-sm text-slate-600">{c.sourcesLead}</p>
      <Sources items={SOURCES_OF_TRUTH} />
    </div>
  );
}
