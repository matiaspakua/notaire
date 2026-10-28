import Link from "next/link";
import { issue, src } from "@/lib/ai-sdlc-content";
import { fmt, loadFactoryFacts } from "@/lib/ai-sdlc-facts";
import { loadMetrics } from "@/lib/metrics";
import { HISTORY, HistoryChart } from "@/components/CaseStudy";
import { Card, PageHeader, SectionTitle, Sources } from "@/components/FactoryUI";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/ai-sdlc/results/");

/**
 * Results (#1970): what the factory produced. Every number on this page is read at build time,
 * from the repository (lib/ai-sdlc-facts.ts), the committed history snapshot with its provenance
 * (lib/ai-sdlc-history.json) or the deploy-time metrics file (public/metrics.json). None is typed here.
 */
export default function ResultsPage() {
  const f = loadFactoryFacts();
  const m = loadMetrics();
  const t = HISTORY.totals;
  const first = HISTORY.months.find((x) => x.merged_prs > 0);
  const last = HISTORY.months[HISTORY.months.length - 1];
  const busiest = HISTORY.months.reduce((a, b) => (b.merged_prs > a.merged_prs ? b : a));
  const processPrs = HISTORY.months.reduce((s, x) => s + x.process_prs, 0);

  const delivery = [
    { value: fmt(t.commits), label: "commits", note: `${fmt(HISTORY.legacy_commits)} from the 2014 desktop app` },
    { value: fmt(t.merged_prs_without_bots), label: "merged pull requests", note: `${fmt(t.merged_prs)} including Dependabot` },
    { value: fmt(processPrs), label: "PRs about the process itself", note: "the factory improving the factory" },
    { value: fmt(t.issues), label: "issues", note: `${fmt(t.bug_issues)} of them labelled type:bug` },
  ];

  const factory = [
    { value: fmt(f.archivedChanges), label: "OpenSpec changes archived", note: "one per shipped change" },
    { value: fmt(f.livingSpecs), label: "living capability specs", note: "docs/openspec/specs" },
    { value: fmt(f.skills), label: "skills", note: ".claude/skills" },
    { value: fmt(f.agents), label: "agent definitions", note: ".claude/agents" },
    { value: fmt(f.workflows), label: "CI/CD workflows", note: ".github/workflows" },
    { value: fmt(f.sdlcScripts), label: "process scripts", note: "workspace/sdlc" },
    { value: fmt(f.e2eSuites), label: "Playwright E2E suites", note: "testing/e2e" },
    { value: fmt(f.adrs), label: "architecture decisions", note: "ADRs" },
    { value: fmt(f.useCases), label: "use cases", note: "docs/100-business" },
    { value: fmt(f.securityFamilies.length), label: "security case families", note: "security/cases" },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="AI Factory · Results"
        title="What the factory produced"
        lead={
          <>
            Claims are cheap; counts are not. Every number on this page is read when the site is built, from the
            repository itself or from a committed snapshot that records the command that made it.
          </>
        }
      />

      <SectionTitle id="delivery">Delivery</SectionTitle>
      <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {delivery.map((d) => (
          <Card key={d.label}>
            <dt className="text-xs font-medium uppercase tracking-wider text-slate-600">{d.label}</dt>
            <dd className="mt-1 font-display text-4xl font-semibold tabular-nums text-slate-900">{d.value}</dd>
            <dd className="mt-1 text-xs text-slate-600">{d.note}</dd>
          </Card>
        ))}
      </dl>
      <p className="mt-4 leading-relaxed text-slate-700">
        The first merged pull request in the snapshot is from {first?.month ?? "—"}.{" "}
        {busiest.month === last.month
          ? `The latest month, ${last.month}, is also the busiest: ${fmt(last.merged_prs)} merged PRs by ${HISTORY.generated_at.slice(0, 10)}.`
          : `The busiest month is ${busiest.month}, with ${fmt(busiest.merged_prs)} merged PRs; the latest, ${last.month}, has ${fmt(last.merged_prs)}.`}
      </p>
      <HistoryChart />

      <SectionTitle id="factory">The factory itself</SectionTitle>
      <p className="leading-relaxed text-slate-700">
        The process is code too. These are counted from the folders that hold it.
      </p>
      <div className="mt-4 grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-5">
        {factory.map((d) => (
          <Card key={d.label} className="text-center">
            <div className="font-display text-3xl font-semibold tabular-nums text-slate-900">{d.value}</div>
            <div className="mt-1 text-sm font-medium text-slate-800">{d.label}</div>
            <div className="mt-0.5 font-mono text-[11px] text-slate-600">{d.note}</div>
          </Card>
        ))}
      </div>

      <SectionTitle id="quality">Quality held while speed grew</SectionTitle>
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <p className="text-xs font-medium uppercase tracking-wider text-slate-600">Backend coverage (JaCoCo)</p>
          <p className="mt-1 font-display text-4xl font-semibold tabular-nums text-slate-900">
            {m.coverage.backendInstruction}%<span className="ml-2 text-base font-normal text-slate-600">instructions</span>
          </p>
          <p className="mt-1 text-sm text-slate-700">{m.coverage.backendBranch}% of branches</p>
          <p className="mt-3 text-xs text-slate-600">
            From <code className="font-mono">public/metrics.json</code>, regenerated on every deploy (as of {m.coverage.lastUpdated}).{" "}
            <Link href="/coverage/" prefetch={false}>Full report</Link>.
          </p>
        </Card>
        <Card>
          <p className="text-xs font-medium uppercase tracking-wider text-slate-600">Ratchets: quality only moves one way</p>
          <p className="mt-2 text-sm leading-relaxed text-slate-700">
            <code className="font-mono">mvn verify</code> enforces the JaCoCo ratchet floor; Checkstyle and SpotBugs counts
            may only shrink; architecture baselines may only shrink. The case study tells how the hexagonal baseline went
            to zero.
          </p>
          <Sources
            items={[
              { label: "enforce-quality-ratchet.py", href: src("workspace/ci/enforce-quality-ratchet.py") },
              { label: "quality-ratchet-baseline.json", href: src("workspace/ci/quality-ratchet-baseline.json") },
              { label: "hexagon-baseline.txt", href: src("backend-api/src/test/resources/architecture/hexagon-baseline.txt") },
            ]}
          />
        </Card>
      </div>

      <SectionTitle id="mechanisms">Each mechanism, when it arrived</SectionTitle>
      <ol className="relative mt-4 space-y-3 border-l-2 border-factory-200 pl-6">
        {HISTORY.mechanisms.map((x) => (
          <li key={x.date + x.label} className="relative">
            <span aria-hidden="true" className="absolute -left-[1.92rem] top-1.5 h-3 w-3 rounded-full border-2 border-white bg-factory-600" />
            <p className="font-mono text-xs text-slate-600">{x.date}</p>
            <p className="font-semibold text-slate-900">{x.label}</p>
            <p className="text-sm text-slate-600">
              <a href={src(x.path)} className="font-mono text-[12px]">
                {x.path}
              </a>
              {x.issue && (
                <>
                  {" · "}
                  <a href={issue(x.issue)}>#{x.issue}</a>
                </>
              )}
            </p>
          </li>
        ))}
      </ol>

      <SectionTitle id="caveats">Reading these numbers honestly</SectionTitle>
      <ul className="list-disc space-y-2 pl-5 text-slate-700">
        <li>Throughput is not value. Many October PRs are small, and a share of them are about the process itself.</li>
        <li>Bug issues are counted by the month they were opened. A falling ratio is a correlation, not proof that the gates caused it.</li>
        <li>
          The history snapshot was generated {HISTORY.generated_at.slice(0, 10)} by <code className="font-mono">{HISTORY.generated_by}</code>; re-run it to refresh.
        </li>
      </ul>
      <p className="mt-8">
        <Link href="/ai-sdlc/framework/" className="inline-flex items-center gap-2 rounded-full bg-factory-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-factory-800">
          Next: apply it to your project →
        </Link>
      </p>
    </div>
  );
}
