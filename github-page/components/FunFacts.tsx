"use client";
import { Es } from "@/components/site/Es";
import type { SiteMetrics } from "@/lib/metrics";

function buildFacts(metrics: SiteMetrics, java: string | null) {
  return [
    {
      title: "3 Years of Sleep",
      desc: "The project was dormant from mid-2015 to late 2018. Not a single commit. Then life happened, then AI happened.",
      color: "#64748b",
    },
    {
      title: `${metrics.commits} commits`,
      desc: "The 2014 to 2018 years left fewer than thirty commits; the AI-paired period wrote the rest. Agents wrote tests, reviewed code and updated docs continuously.",
      color: "var(--ai-purple)",
    },
    {
      title: `${metrics.pullRequests}+ Pull Requests Merged`,
      desc: "A stream of merged pull requests. The pull-request proof is preflight.sh --full on the agent machine. Product CI runs again on a version tag.",
      color: "var(--spring-green)",
    },
    {
      title: `${metrics.markdownFiles} Markdown Files`,
      desc: "Documentation grew from zero to hundreds of .md files. Use cases, ADRs, guides, runbooks, API references — all kept in sync by AI.",
      color: "#ff9500",
    },
    {
      title: `Java 1.6 → Java ${java ?? "—"}`,
      desc: `${java ? `${Number(java) - 6} major Java versions` : "Every major Java version"}. From brittle Swing event handlers to records, sealed classes, and virtual threads.`,
      color: "var(--java-orange)",
    },
    {
      title: "Zero Tests → JaCoCo-Enforced",
      desc: "The original project had no tests at all. Today, JaCoCo enforces a rising coverage ratchet and CI fails if it drops.",
      color: "var(--ai-cyan)",
    },
  ];
}

const challenges = [
  {
    title: "Schema Dual-Source Drift",
    severity: "HIGH",
    desc: "Docker once built from init-db/*.sql while Flyway ran db/migration/V*.sql, so Hibernate entities could drift from both and produce recurring 500s until an integration test guard landed. Resolved: Flyway is now the sole schema source; the old init-db scripts are archived.",
    color: "#ff453a",
  },
  {
    title: "Legacy JPA Controllers",
    severity: "MED",
    desc: "The Swing-era JpaAdministrator and the com.licensis.notaire.jpa package were retired (#576). Flyway is the only schema source.",
    color: "#ff9500",
  },
  {
    title: "Hibernate Proxy & Jackson",
    severity: "MED",
    desc: "@JsonIgnore silently fails on Hibernate proxy subclasses, causing infinite recursion in JSON serialization. Fix: use Maps/DTOs, never serialize JPA entities directly.",
    color: "#ff9500",
  },
  {
    title: "131 issues had no use case",
    severity: "LOW",
    desc: "Workflow rule: every GitHub issue must reference a Use Case (CU-XX). After auditing all 233 open issues, 131 were missing this link. Batch-fixed via gh CLI scripting.",
    color: "#0a84ff",
  },
  {
    title: "The Autonomous Loop Corrupted Its Own Memory",
    severity: "MED",
    desc: "Two scheduled AI agent Routines ran against the repo in parallel, both writing to the same loop-state.md with no locking — it got corrupted, and a stale pointer claimed an already-merged PR was still open two days later. Fixed by making the loop verify its own memory against live GitHub state before trusting it, instead of assuming the file is always right.",
    color: "#AF52DE",
  },
];

export function FunFacts({ metrics, java }: { metrics: SiteMetrics; java: string | null }) {
  const facts = buildFacts(metrics, java);

  return (
    <Es>
      <section className="bg-slate-50 px-6 py-24">
        <div className="mx-auto max-w-6xl">
          <div className="mb-10 max-w-2xl">
            <h2 className="font-display text-4xl font-semibold tracking-tight text-slate-900">Fun Facts</h2>
            <p className="mt-3 text-slate-600">Numbers and moments that make this project unique.</p>
          </div>
          <ul className="mb-20 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {facts.map((fact) => (
              <li key={fact.title} className="rounded-2xl border border-slate-200 border-t-4 bg-white p-6" style={{ borderTopColor: fact.color }}>
                <h3 className="font-semibold text-slate-900">{fact.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-700">{fact.desc}</p>
              </li>
            ))}
          </ul>
          <div className="mb-10 max-w-2xl">
            <h2 className="font-display text-4xl font-semibold tracking-tight text-slate-900">Major Challenges</h2>
            <p className="mt-3 text-slate-600">Real production incidents and architectural hurdles that shaped how the project works today.</p>
          </div>
          <ul className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            {challenges.map((challenge) => (
              <li key={challenge.title} className="rounded-2xl border border-slate-200 bg-white p-6">
                <div className="flex items-start gap-4">
                  <span
                    className={`mt-0.5 flex-none rounded px-2 py-1 font-mono text-xs font-bold ${
                      challenge.severity === "HIGH"
                        ? "bg-red-500/10 text-red-700"
                        : challenge.severity === "MED"
                          ? "bg-amber-500/10 text-amber-800"
                          : "bg-brand-600/10 text-brand-700"
                    }`}
                  >
                    {challenge.severity}
                  </span>
                  <div>
                    <h3 className="font-semibold text-slate-900">{challenge.title}</h3>
                    <p className="mt-2 text-sm leading-relaxed text-slate-700">{challenge.desc}</p>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </Es>
  );
}
