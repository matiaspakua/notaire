import Link from "next/link";
import { Card, Code, PageHeader, SectionTitle } from "@/components/FactoryUI";
import { Callout, StatGrid } from "@/components/DocsUI";
import { ArtifactCabinet } from "@/components/figures/cabinet/figure";
import { src } from "@/lib/ai-sdlc-content";
import { artifactFacts } from "@/lib/artifact-facts";
import { STAGES, type Artifact } from "@/lib/artifacts-content";
import { pageMetadata } from "@/lib/site";

export const metadata = pageMetadata("/docs/artifacts/");

const fmt = (n: number | null) => (n === null ? "—" : n.toLocaleString("en-US"));

const linkClass = "text-brand-700 underline decoration-brand-700/30 underline-offset-2 hover:decoration-current";

function ArtifactCard({ artifact, value }: { artifact: Artifact; value: number | null | undefined }) {
  return (
    <Card className="h-full">
      <div className="flex items-baseline justify-between gap-3">
        <h3 className="font-semibold text-slate-900">{artifact.name}</h3>
        {artifact.id && <span className="shrink-0 font-mono text-xs text-slate-500">{artifact.id}</span>}
      </div>
      {artifact.count && (
        <p className="mt-1 text-sm text-slate-700">
          <span className="font-display text-2xl font-semibold text-slate-900">{fmt(value ?? null)}</span> {artifact.count.unit}
        </p>
      )}
      <dl className="mt-3 space-y-2 text-sm leading-relaxed">
        <div>
          <dt className="inline font-medium text-slate-900">What. </dt>
          <dd className="inline text-slate-600">{artifact.what}</dd>
        </div>
        <div>
          <dt className="inline font-medium text-slate-900">Why. </dt>
          <dd className="inline text-slate-600">{artifact.why}</dd>
        </div>
        <div>
          <dt className="inline font-medium text-slate-900">How. </dt>
          <dd className="inline text-slate-600">{artifact.how}</dd>
        </div>
        {artifact.gap && (
          <div>
            <dt className="inline font-medium text-amber-800">Gap. </dt>
            <dd className="inline text-slate-600">{artifact.gap}</dd>
          </div>
        )}
      </dl>
      <p className="mt-3 flex flex-wrap gap-x-3 gap-y-1 text-xs">
        {artifact.links.map((l) => (
          <a key={l.href} className={`font-mono ${linkClass}`} href={l.href}>
            {l.label}
          </a>
        ))}
      </p>
    </Card>
  );
}

export default function ArtifactsPage() {
  const f = artifactFacts();
  return (
    <div>
      <PageHeader
        eyebrow="Technical docs · Engineering artifacts"
        title="Every artifact that makes Notaire, and how they connect"
        lead={
          <>
            Notaire is not only code. Before a line of Java was written, someone surveyed a notary office and wrote down
            what it needed. That requirement became a use case, the use case a state model and an architecture decision,
            and today every change becomes a specification, tests, a threat review and a release. This page follows that
            chain, one stage at a time: what each artifact is, why it exists, how it was made and where it lives. Every
            number on it is counted from the repository when the site is built.
          </>
        }
      />

      <StatGrid
        items={[
          { value: fmt(f.requirements), label: "Requirements", hint: `${fmt(f.functionalRequirements)} functional` },
          { value: fmt(f.useCases), label: "Use cases", hint: "CU files" },
          { value: fmt(f.adrs), label: "Decisions", hint: "ADRs" },
          { value: fmt(f.scenarios), label: "Scenarios", hint: `in ${fmt(f.specs)} living specs` },
        ]}
      />

      <ArtifactCabinet facts={f} />

      <SectionTitle id="chain">One chain, read in both directions</SectionTitle>
      <p className="text-slate-700 leading-relaxed">
        Each artifact cites the one before it and is cited by the one after it. A use case names its requirement, an Issue
        names its use case, a commit and a pull request name the Issue, and a test names the use case it proves. Read
        forward, a requirement leads to the screen and the log line that implement it. Read backward, a bug in production
        leads to the use case and the requirement it broke. The method is written down in{" "}
        <a className={linkClass} href={src("docs/300-development/INGENIERIA.md")}>
          INGENIERIA.md
        </a>{" "}
        and enforced by checks such as <Code>check-doc-traceability.py</Code>, which fails a pull request that changes
        behaviour without touching the permanent document that describes it.
      </p>
      <ol className="mt-6 grid gap-2 sm:grid-cols-3 lg:grid-cols-9">
        {STAGES.map((s) => (
          <li key={s.key}>
            <a
              href={`#${s.key}`}
              className="flex h-full flex-col rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm transition-colors hover:border-brand-500"
            >
              <span className="font-mono text-xs text-slate-500">{s.n}</span>
              <span className="font-semibold text-slate-900">{s.verb}</span>
            </a>
          </li>
        ))}
      </ol>

      {STAGES.map((s) => (
        <section key={s.key} aria-labelledby={s.key}>
          <SectionTitle id={s.key}>
            <span className="mr-3 font-mono text-base text-slate-500">{s.n}</span>
            {s.verb}: {s.title.charAt(0).toLowerCase() + s.title.slice(1)}
          </SectionTitle>
          <p className="max-w-3xl text-slate-700 leading-relaxed">{s.story}</p>
          <p className="mt-3 max-w-3xl text-sm text-slate-600">
            <span className="font-semibold text-slate-900">In the AI SDLC. </span>
            {s.sdlc}
          </p>
          <div className="mt-5 grid gap-4 md:grid-cols-2">
            {s.artifacts.map((a) => (
              <ArtifactCard key={a.name} artifact={a} value={a.count ? f[a.count.key] : undefined} />
            ))}
          </div>
        </section>
      ))}

      <SectionTitle id="honest">What is still missing</SectionTitle>
      <p className="text-slate-700 leading-relaxed">
        The gaps above are the ones known today. The full list, with the command that found each one, is the documentation
        audit in{" "}
        <a className={linkClass} href={src("docs/300-development/DOCUMENTATION-AUDIT-2026-11.md")}>
          DOCUMENTATION-AUDIT-2026-11.md
        </a>
        . The repository still has no license file: that is the Owner&apos;s decision, tracked as{" "}
        <a className={linkClass} href="https://github.com/matiaspakua/notaire/issues/1226">
          #1226
        </a>
        .
      </p>

      <Callout title="Where to go next">
        <p>
          The <Link className={linkClass} href="/ai-sdlc/lifecycle/">AI SDLC lifecycle</Link> shows the 24 steps a single
          change takes through these artifacts. The <Link className={linkClass} href="/docs/architecture/">Architecture</Link>{" "}
          page lists every ADR, and the <Link className={linkClass} href="/security/threats/">Threats</Link> page lets you
          explore the STRIDE register.
        </p>
      </Callout>
    </div>
  );
}
