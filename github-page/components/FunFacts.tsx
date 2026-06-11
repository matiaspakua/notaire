"use client";
import { useEffect, useRef } from "react";

const facts = [
  {
    emoji: "😴",
    title: "3 Years of Sleep",
    desc: "The project was dormant from mid-2015 to late 2018. Not a single commit. Then life happened, then AI happened.",
    color: "#64748b",
  },
  {
    emoji: "🤯",
    title: "226 commits in 60 days",
    desc: "March–April 2026: the most intense development sprint. AI agents wrote tests, reviewed code, and updated docs continuously.",
    color: "var(--ai-purple)",
  },
  {
    emoji: "🏆",
    title: "159 / 159 PRs Merged",
    desc: "100% PR merge rate. Every single pull request made it in — thanks to mandatory TDD, E2E coverage, and automated quality gates.",
    color: "var(--spring-green)",
  },
  {
    emoji: "📚",
    title: "545 Markdown Files",
    desc: "Documentation grew from zero to 545 .md files. Use cases, ADRs, guides, runbooks, API references — all kept in sync by AI.",
    color: "#f59e0b",
  },
  {
    emoji: "☕",
    title: "Java 1.6 → Java 21",
    desc: "15 major Java versions. From brittle Swing event handlers to records, sealed classes, and virtual threads.",
    color: "var(--java-orange)",
  },
  {
    emoji: "🧪",
    title: "Zero Tests → 80% Coverage",
    desc: "The original project had no tests at all. Today, JaCoCo enforces an 80% coverage ratchet and CI fails if it drops.",
    color: "var(--ai-cyan)",
  },
];

const challenges = [
  {
    title: "Schema Dual-Source Drift",
    severity: "HIGH",
    desc: "Docker builds from init-db/*.sql but Flyway runs db/migration/V*.sql. Hibernate entities can drift from both. Caused recurring 500 errors until an integration test guard was added.",
    color: "#ef4444",
  },
  {
    title: "Legacy JPA Controllers",
    severity: "MED",
    desc: "The original monolith had massive JpaController classes (not REST controllers!) with Hibernate sessions and EntityManager leaks directly from the Swing era. Still being migrated to Spring Data repos.",
    color: "#f59e0b",
  },
  {
    title: "Hibernate Proxy & Jackson",
    severity: "MED",
    desc: "@JsonIgnore silently fails on Hibernate proxy subclasses, causing infinite recursion in JSON serialization. Fix: use Maps/DTOs, never serialize JPA entities directly.",
    color: "#f59e0b",
  },
  {
    title: "131 Issues Missing Use Cases",
    severity: "LOW",
    desc: "Workflow rule: every GitHub issue must reference a Use Case (CU-XX). After auditing all 233 open issues, 131 were missing this link. Batch-fixed via gh CLI scripting.",
    color: "#06b6d4",
  },
];

export function FunFacts() {
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
          <p className="text-yellow-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Trivia</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Fun <span className="grad-gold">Facts</span>
          </h2>
          <p className="text-slate-400 max-w-xl mx-auto">Numbers and moments that make this project unique.</p>
        </div>

        <div ref={factsRef} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 mb-24">
          {facts.map((f, i) => (
            <div key={i} className="anim-card glass rounded-2xl p-6 border group hover:scale-[1.02] transition-transform duration-300 cursor-default" style={{ borderColor: `${f.color}25` }}>
              <div className="text-4xl mb-3">{f.emoji}</div>
              <h3 className="text-white font-bold mb-2">{f.title}</h3>
              <p className="text-slate-400 text-sm leading-relaxed">{f.desc}</p>
              <div className="mt-4 w-8 h-0.5 rounded-full group-hover:w-full transition-all duration-500" style={{ background: f.color }} />
            </div>
          ))}
        </div>

        {/* Major challenges */}
        <div className="text-center mb-14">
          <p className="text-red-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Battle Scars</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Major <span className="grad-fire">Challenges</span>
          </h2>
          <p className="text-slate-400 max-w-xl mx-auto">Real production incidents and architectural hurdles that shaped how the project works today.</p>
        </div>

        <div ref={challengesRef} className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {challenges.map((c, i) => (
            <div key={i} className="anim-card glass rounded-2xl p-6 border" style={{ borderColor: `${c.color}25` }}>
              <div className="flex items-start gap-4">
                <span className={`flex-none mt-0.5 px-2 py-1 rounded text-xs font-mono font-bold ${
                  c.severity === "HIGH" ? "bg-red-500/15 text-red-400" :
                  c.severity === "MED" ? "bg-yellow-500/15 text-yellow-400" :
                  "bg-cyan-500/15 text-cyan-400"
                }`}>{c.severity}</span>
                <div>
                  <h3 className="text-white font-bold mb-2">{c.title}</h3>
                  <p className="text-slate-400 text-sm leading-relaxed">{c.desc}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
