"use client";
import { useEffect, useRef } from "react";
import type { SiteMetrics } from "@/lib/metrics";

function buildFacts(metrics: SiteMetrics) {
  return [
    {
      emoji: "😴",
      title: "3 Years of Sleep",
      desc: "The project was dormant from mid-2015 to late 2018. Not a single commit. Then life happened, then AI happened.",
      color: "#64748b",
    },
    {
      emoji: "🤯",
      title: "786 commits in 2026",
      desc: "One calendar year of AI-paired development outpaced the previous decade combined. Agents wrote tests, reviewed code, and updated docs continuously.",
      color: "var(--ai-purple)",
    },
    {
      emoji: "🏆",
      title: `${metrics.pullRequests}+ Pull Requests Merged`,
      desc: "A steady, ongoing stream of merged PRs — thanks to mandatory TDD, E2E coverage, and automated quality gates that never let a red build through.",
      color: "var(--spring-green)",
    },
    {
      emoji: "📚",
      title: `${metrics.markdownFiles} Markdown Files`,
      desc: "Documentation grew from zero to hundreds of .md files. Use cases, ADRs, guides, runbooks, API references — all kept in sync by AI.",
      color: "#ff9500",
    },
    {
      emoji: "☕",
      title: "Java 1.6 → Java 21",
      desc: "15 major Java versions. From brittle Swing event handlers to records, sealed classes, and virtual threads.",
      color: "var(--java-orange)",
    },
    {
      emoji: "🧪",
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
    desc: "Docker builds from init-db/*.sql but Flyway runs db/migration/V*.sql. Hibernate entities can drift from both. Caused recurring 500 errors until an integration test guard was added.",
    color: "#ff453a",
  },
  {
    title: "Legacy JPA Controllers",
    severity: "MED",
    desc: "The original monolith had massive JpaController classes (not REST controllers!) with Hibernate sessions and EntityManager leaks directly from the Swing era. Still being migrated to Spring Data repos.",
    color: "#ff9500",
  },
  {
    title: "Hibernate Proxy & Jackson",
    severity: "MED",
    desc: "@JsonIgnore silently fails on Hibernate proxy subclasses, causing infinite recursion in JSON serialization. Fix: use Maps/DTOs, never serialize JPA entities directly.",
    color: "#ff9500",
  },
  {
    title: "131 Issues Missing Use Cases",
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

export function FunFacts({ metrics }: { metrics: SiteMetrics }) {
  const facts = buildFacts(metrics);
  const factsRef = useRef<HTMLDivElement>(null);
  const challengesRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    [factsRef, challengesRef].forEach(ref => {
      const cards = ref.current?.querySelectorAll(".anim-card");
      if (!cards) return;
      const observer = new IntersectionObserver(([entry]) => {
        if (entry.isIntersecting) {
          cards.forEach((card, i) => {
            setTimeout(() => {
              (card as HTMLElement).style.transition = "opacity 0.5s, transform 0.5s cubic-bezier(0.16,1,0.3,1)";
              (card as HTMLElement).style.opacity = "1";
              (card as HTMLElement).style.transform = "translateY(0)";
            }, i * 80);
          });
          observer.disconnect();
        }
      }, { threshold: 0.1 });

      cards.forEach(card => {
        (card as HTMLElement).style.opacity = "0";
        (card as HTMLElement).style.transform = "translateY(30px)";
      });
      if (ref.current) observer.observe(ref.current);
      return () => observer.disconnect();
    });
  }, []);

  return (
    <section className="py-32 px-6" style={{ background: "var(--bg-mid)" }}>
      <div className="max-w-6xl mx-auto">

        {/* Fun facts */}
        <div className="text-center mb-14">
          <p className="text-yellow-600 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Trivia</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Fun <span className="grad-gold">Facts</span>
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">Numbers and moments that make this project unique.</p>
        </div>

        <div ref={factsRef} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 mb-24">
          {facts.map((f, i) => (
            <div key={i} className="anim-card glass rounded-2xl p-6 border group hover:scale-[1.02] transition-transform duration-300 cursor-default" style={{ borderColor: `${f.color}25` }}>
              <div className="text-4xl mb-3">{f.emoji}</div>
              <h3 className="text-neutral-900 font-bold mb-2">{f.title}</h3>
              <p className="text-neutral-600 text-sm leading-relaxed">{f.desc}</p>
              <div className="mt-4 w-8 h-0.5 rounded-full group-hover:w-full transition-all duration-500" style={{ background: f.color }} />
            </div>
          ))}
        </div>

        {/* Major challenges */}
        <div className="text-center mb-14">
          <p className="text-red-600 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Battle Scars</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Major <span className="grad-fire">Challenges</span>
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">Real production incidents and architectural hurdles that shaped how the project works today.</p>
        </div>

        <div ref={challengesRef} className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {challenges.map((c, i) => (
            <div key={i} className="anim-card glass rounded-2xl p-6 border" style={{ borderColor: `${c.color}25` }}>
              <div className="flex items-start gap-4">
                <span className={`flex-none mt-0.5 px-2 py-1 rounded text-xs font-mono font-bold ${
                  c.severity === "HIGH" ? "bg-red-500/10 text-red-600" :
                  c.severity === "MED" ? "bg-yellow-500/10 text-yellow-600" :
                  "bg-[#0A84FF]/10 text-[#0A84FF]"
                }`}>{c.severity}</span>
                <div>
                  <h3 className="text-neutral-900 font-bold mb-2">{c.title}</h3>
                  <p className="text-neutral-600 text-sm leading-relaxed">{c.desc}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
