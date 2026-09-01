"use client";
import { useEffect, useRef } from "react";

const features = [
  {
    icon: "🧠",
    title: "AI-Driven TDD",
    desc: "Claude AI writes failing tests first, implements to pass them, then refactors. Enforced by mandatory workflow rules.",
    color: "var(--ai-purple)",
  },
  {
    icon: "🎭",
    title: "Playwright E2E",
    desc: "200+ automated end-to-end tests covering every user flow, form, and edge case. Every PR requires passing E2E coverage.",
    color: "var(--ai-cyan)",
  },
  {
    icon: "📊",
    title: "Full Observability",
    desc: "Prometheus metrics, Grafana dashboards, Loki log aggregation, and SonarQube quality gates. Production-grade from day one.",
    color: "var(--spring-green)",
  },
  {
    icon: "🔐",
    title: "Security-First",
    desc: "OWASP Top 10 guides, JWT authentication, SQL injection prevention, Trivy vulnerability scanning in CI/CD.",
    color: "var(--java-orange)",
  },
  {
    icon: "📖",
    title: "Living Documentation",
    desc: "87 use cases, 121 functional/non-functional requirements, 21 ADRs. Documentation updated with every PR.",
    color: "#ff9500",
  },
  {
    icon: "⚡",
    title: "Accelerated Delivery",
    desc: "786 commits in 2026 alone with AI pairing. What took years of manual work now happens in sprints.",
    color: "#ff375f",
  },
];

export function AIEra() {
  const headerRef = useRef<HTMLDivElement>(null);
  const gridRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const cards = gridRef.current?.querySelectorAll(".feature-card");
    if (!cards) return;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const idx = Array.from(cards).indexOf(entry.target as Element);
          setTimeout(() => {
            (entry.target as HTMLElement).style.transition = "opacity 0.6s, transform 0.6s cubic-bezier(0.16,1,0.3,1)";
            (entry.target as HTMLElement).style.opacity = "1";
            (entry.target as HTMLElement).style.transform = "translateY(0)";
          }, idx * 80);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });

    cards.forEach(card => {
      (card as HTMLElement).style.opacity = "0";
      (card as HTMLElement).style.transform = "translateY(40px)";
      observer.observe(card);
    });

    return () => observer.disconnect();
  }, []);

  return (
    <section id="ai" className="py-32 px-6 relative overflow-hidden" style={{ background: "var(--bg-mid)" }}>
      {/* BG glow */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-64 pointer-events-none" style={{ background: "radial-gradient(ellipse, rgba(175,82,222,0.12) 0%, transparent 70%)" }} />

      <div className="max-w-6xl mx-auto">
        <div ref={headerRef} className="text-center mb-16">
          <p className="text-[#AF52DE] text-sm font-mono font-bold tracking-widest mb-4 uppercase">The AI Era</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Built with <span className="grad-cyan">Artificial Intelligence</span>
          </h2>
          <p className="text-neutral-600 max-w-2xl mx-auto leading-relaxed">
            In 2026, Claude AI became the primary co-developer. Not just autocomplete—
            full workflow automation: TDD, code review, documentation, E2E testing, and security audits.
          </p>
        </div>

        {/* Workflow banner */}
        <div className="mb-16 glass rounded-2xl p-6 border border-[#AF52DE]/20 overflow-hidden relative">
          <div className="absolute inset-0 shimmer" />
          <div className="relative z-10 flex items-center gap-3 flex-wrap justify-center text-sm font-mono text-neutral-600">
            {["GitHub Issue", "→", "Use Case (CU-XX)", "→", "Feature Branch", "→", "TDD", "→", "Implementation", "→", "E2E Tests", "→", "PR Review", "→", "Merged"].map((step, i) => (
              <span key={i} className={step === "→" ? "text-neutral-500" : "px-3 py-1 rounded-full text-xs"} style={step !== "→" ? { background: "rgba(175,82,222,0.15)", color: "#AF52DE", border: "1px solid rgba(175,82,222,0.25)" } : {}}>
                {step}
              </span>
            ))}
          </div>
        </div>

        {/* Feature grid */}
        <div ref={gridRef} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((f, i) => (
            <div key={i} className="feature-card glass rounded-2xl p-6 border group hover:scale-[1.02] transition-transform cursor-default" style={{ borderColor: `${f.color}20` }}>
              <div className="text-3xl mb-4">{f.icon}</div>
              <h3 className="text-neutral-900 font-bold mb-2">{f.title}</h3>
              <p className="text-neutral-600 text-sm leading-relaxed">{f.desc}</p>
              <div className="mt-4 w-8 h-0.5 rounded-full transition-all duration-300 group-hover:w-16" style={{ background: f.color }} />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
