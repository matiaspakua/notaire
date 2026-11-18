import Link from "next/link";
import { Card, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { RepoLensCharts } from "@/components/factory/RepoLensCharts";
import { T } from "@/components/site/T";
import { src } from "@/lib/ai-sdlc-content";
import {
  REPOLENS_BASICS,
  REPOLENS_FACTORY,
  REPOLENS_LOOP,
  REPOLENS_SECTIONS,
  REPOLENS_STEPS,
} from "@/lib/repolens-content";
import { loadRepoLensBench } from "@/lib/repolens-bench";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/repolens/");

/**
 * RepoLens showcase (#2315): factory knowledge tool + charts from committed benches.
 * Separate from product docs. Numbers come from lib/repolens-bench.json.
 */
export default function RepoLensPage() {
  const bench = loadRepoLensBench();
  const sources = [
    ...bench.sources.map((s) => ({ href: src(s.path), label: s.path })),
    { href: src("framework/ai-sdlc/repolens/README.md"), label: "framework/ai-sdlc/repolens/README.md" },
    { href: src("framework/ai-sdlc/repolens/MODULE.md"), label: "framework/ai-sdlc/repolens/MODULE.md" },
  ];

  return (
    <div>
      <PageHeader eyebrow={REPOLENS_BASICS.eyebrow} title={REPOLENS_BASICS.title} lead={REPOLENS_BASICS.lead} />

      <SectionTitle id="basics">{REPOLENS_SECTIONS.basicsTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text={REPOLENS_SECTIONS.basicsBody} />
      </p>
      <p className="mt-3 text-sm text-slate-600">
        <T text="Also see" />{" "}
        <Link href="/ai-sdlc/framework/" className="font-semibold text-brand-700 hover:underline">
          <T text="Framework" />
        </Link>{" "}
        <T text="and" />{" "}
        <Link href="/ai-sdlc/results/" className="font-semibold text-brand-700 hover:underline">
          <T text="Results" />
        </Link>
        .
      </p>

      <SectionTitle id="how">{REPOLENS_SECTIONS.howTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text={REPOLENS_SECTIONS.howLead} />
      </p>
      <ol className="mt-4 grid gap-3 md:grid-cols-2">
        {REPOLENS_STEPS.map((step, i) => (
          <li key={step.title}>
            <Card>
              <div className="text-xs font-semibold uppercase tracking-wider text-slate-500">{i + 1}</div>
              <h3 className="mt-1 font-semibold text-slate-900">
                <T text={step.title} />
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-700">
                <T text={step.body} />
              </p>
            </Card>
          </li>
        ))}
      </ol>

      <SectionTitle id="factory">{REPOLENS_SECTIONS.factoryTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text={REPOLENS_SECTIONS.factoryLead} />
      </p>
      <div className="mt-4 grid gap-3 md:grid-cols-2">
        {REPOLENS_FACTORY.map((item) => (
          <Card key={item.title}>
            <h3 className="font-semibold text-slate-900">
              <T text={item.title} />
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-700">
              <T text={item.body} />
            </p>
          </Card>
        ))}
      </div>

      <SectionTitle id="loop">{REPOLENS_SECTIONS.loopTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text={REPOLENS_SECTIONS.loopLead} />
      </p>
      <div className="mt-4 grid gap-3 md:grid-cols-3">
        {REPOLENS_LOOP.map((item) => (
          <Card key={item.title}>
            <h3 className="font-semibold text-slate-900">
              <T text={item.title} />
            </h3>
            <p className="mt-2 text-sm leading-relaxed text-slate-700">
              <T text={item.body} />
            </p>
          </Card>
        ))}
      </div>

      <SectionTitle id="benchmarks">{REPOLENS_SECTIONS.benchesTitle}</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        <T text={REPOLENS_SECTIONS.benchesLead} />
      </p>
      <div className="mt-6">
        <RepoLensCharts />
      </div>

      <Sources items={sources} />
    </div>
  );
}
