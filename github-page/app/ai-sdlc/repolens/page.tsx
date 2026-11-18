import Link from "next/link";
import { Card, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { FlowDiagram } from "@/components/diagrams/FlowDiagram";
import { RepoLensCharts } from "@/components/factory/RepoLensCharts";
import { RevealSection } from "@/components/factory/RevealSection";
import { T } from "@/components/site/T";
import { src } from "@/lib/ai-sdlc-content";
import { repolensPipelineFlow } from "@/lib/diagram-flows";
import {
  REPOLENS_BASICS,
  REPOLENS_FACTORY,
  REPOLENS_LOOP,
  REPOLENS_SECTIONS,
} from "@/lib/repolens-content";
import { loadRepoLensBench } from "@/lib/repolens-bench";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/repolens/");

/**
 * RepoLens showcase (#2315) + UI polish (#2318): pipeline diagram, chart motion/tooltips,
 * scroll reveals. Numbers come from lib/repolens-bench-snapshot.json.
 */
export default function RepoLensPage() {
  const bench = loadRepoLensBench();
  const sources = [
    ...bench.sources.map((s) => ({ href: src(s.path), label: s.path })),
    { href: src("framework/ai-sdlc/repolens/README.md"), label: "framework/ai-sdlc/repolens/README.md" },
    { href: src("framework/ai-sdlc/repolens/MODULE.md"), label: "framework/ai-sdlc/repolens/MODULE.md" },
  ];
  const pipeline = repolensPipelineFlow();

  return (
    <div>
      <PageHeader eyebrow={REPOLENS_BASICS.eyebrow} title={REPOLENS_BASICS.title} lead={REPOLENS_BASICS.lead} />

      <RevealSection>
        <SectionTitle id="basics">{REPOLENS_SECTIONS.basicsTitle}</SectionTitle>
        <p className="leading-relaxed text-slate-700 dark:text-slate-300">
          <T text={REPOLENS_SECTIONS.basicsBody} />
        </p>
        <p className="mt-3 text-sm text-slate-600 dark:text-slate-400">
          <T text="Also see" />{" "}
          <Link href="/ai-sdlc/framework/" className="font-semibold text-brand-700 hover:underline dark:text-brand-400">
            <T text="Framework" />
          </Link>{" "}
          <T text="and" />{" "}
          <Link href="/ai-sdlc/results/" className="font-semibold text-brand-700 hover:underline dark:text-brand-400">
            <T text="Results" />
          </Link>
          .
        </p>
      </RevealSection>

      <RevealSection>
        <SectionTitle id="how">{REPOLENS_SECTIONS.howTitle}</SectionTitle>
        <p className="leading-relaxed text-slate-700 dark:text-slate-300">
          <T text={REPOLENS_SECTIONS.howLead} />
        </p>
        <FlowDiagram
          id="repolens-pipeline"
          title={REPOLENS_SECTIONS.pipelineTitle}
          desc={REPOLENS_SECTIONS.pipelineDesc}
          caption={REPOLENS_SECTIONS.pipelineCaption}
          {...pipeline}
        />
      </RevealSection>

      <RevealSection>
        <SectionTitle id="factory">{REPOLENS_SECTIONS.factoryTitle}</SectionTitle>
        <p className="leading-relaxed text-slate-700 dark:text-slate-300">
          <T text={REPOLENS_SECTIONS.factoryLead} />
        </p>
        <div className="mt-4 grid gap-3 md:grid-cols-2">
          {REPOLENS_FACTORY.map((item) => (
            <Card key={item.title} className="dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-900 dark:text-slate-100">
                <T text={item.title} />
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-700 dark:text-slate-300">
                <T text={item.body} />
              </p>
            </Card>
          ))}
        </div>
      </RevealSection>

      <RevealSection>
        <SectionTitle id="loop">{REPOLENS_SECTIONS.loopTitle}</SectionTitle>
        <p className="leading-relaxed text-slate-700 dark:text-slate-300">
          <T text={REPOLENS_SECTIONS.loopLead} />
        </p>
        <div className="mt-4 grid gap-3 md:grid-cols-3">
          {REPOLENS_LOOP.map((item) => (
            <Card key={item.title} className="dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-900 dark:text-slate-100">
                <T text={item.title} />
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-700 dark:text-slate-300">
                <T text={item.body} />
              </p>
            </Card>
          ))}
        </div>
      </RevealSection>

      <RevealSection>
        <SectionTitle id="benchmarks">{REPOLENS_SECTIONS.benchesTitle}</SectionTitle>
        <p className="leading-relaxed text-slate-700 dark:text-slate-300">
          <T text={REPOLENS_SECTIONS.benchesLead} />
        </p>
        <div className="mt-6">
          <RepoLensCharts />
        </div>
      </RevealSection>

      <Sources items={sources} />
    </div>
  );
}
