"use client";
import { useEffect, useRef } from "react";

const events = [
  {
    year: "2014",
    month: "March",
    title: "The Origin",
    subtitle: "University Project",
    desc: "A Java Swing monolith born as a university notarial management system. Direct DB access, JDBC queries embedded in UI event handlers, tightly coupled business logic.",
    color: "var(--java-orange)",
    icon: "☕",
    tech: ["Java 1.6", "Java Swing", "MySQL", "JDBC"],
    commits: 7,
    side: "left",
  },
  {
    year: "2015–2018",
    month: "Dormancy",
    title: "Years of Silence",
    subtitle: "Project Paused",
    desc: "The project entered a long dormancy. Real-world complexity exceeded the scope of an academic exercise. The code slept, but the vision remained.",
    color: "#64748b",
    icon: "💤",
    tech: ["Frozen in time"],
    commits: 5,
    side: "right",
  },
  {
    year: "2025",
    month: "December",
    title: "The Renaissance",
    subtitle: "Modern Reboot",
    desc: "A decade later, the project was reborn. Java 21, Spring Boot 3.2.9, Docker, PostgreSQL. The monolith began its transformation into a proper microservices architecture.",
    color: "var(--spring-green)",
    icon: "🌱",
    tech: ["Java 21", "Spring Boot 3.2.9", "PostgreSQL 16", "Docker"],
    commits: 14,
    side: "left",
  },
  {
    year: "2026",
    month: "February",
    title: "API & Frontend",
    subtitle: "Next.js Migration",
    desc: "REST API layer solidified. The Swing GUI was retired, replaced by a modern Next.js 15 + React 19 + TypeScript frontend with a proper design system.",
    color: "var(--ai-cyan)",
    icon: "⚡",
    tech: ["Next.js 15", "React 19", "TypeScript", "Tailwind CSS"],
    commits: 39,
    side: "right",
  },
  {
    year: "2026",
    month: "March–April",
    title: "AI Acceleration",
    subtitle: "Claude Code Integration",
    desc: "AI-assisted development became the primary workflow. 226 commits in two months. Tests, E2E coverage, security audits, observability—all accelerated by Claude AI agents.",
    color: "var(--ai-purple)",
    icon: "🤖",
    tech: ["Claude AI", "Playwright", "JaCoCo", "Prometheus"],
    commits: 226,
    side: "left",
  },
  {
    year: "2026",
    month: "May–June",
    title: "Production Ready",
    subtitle: "Observability & Security",
    desc: "Grafana dashboards, Prometheus alerts, Loki log aggregation, SonarQube quality gates. A 2014 university project became a production-grade platform.",
    color: "#f59e0b",
    icon: "🚀",
    tech: ["Grafana", "Loki", "SonarQube", "Spring Boot 4.0"],
    commits: 236,
    side: "right",
  },
];

function TimelineEvent({ event, index }: { event: (typeof events)[0]; index: number }) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.style.opacity = "0";
    el.style.transform = event.side === "left" ? "translateX(-40px)" : "translateX(40px)";

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          el.style.transition = "opacity 0.7s, transform 0.7s cubic-bezier(0.16,1,0.3,1)";
          el.style.opacity = "1";
          el.style.transform = "translateX(0)";
        }, index * 80);
        observer.disconnect();
      }
    }, { threshold: 0.2 });
    observer.observe(el);
    return () => observer.disconnect();
  }, [event.side, index]);

  const isLeft = event.side === "left";

  return (
    <div ref={ref} className={`flex items-center gap-8 mb-16 ${isLeft ? "flex-row" : "flex-row-reverse"}`}>
      {/* Content card */}
      <div className="flex-1 max-w-md">
        <div className="glass rounded-2xl p-6 border" style={{ borderColor: `${event.color}30` }}>
          <div className="flex items-start gap-4 mb-3">
            <span className="text-3xl">{event.icon}</span>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-mono font-bold tracking-widest" style={{ color: event.color }}>{event.month} {event.year}</span>
                <span className="text-xs px-2 py-0.5 rounded-full text-slate-400 bg-slate-800">{event.commits} commits</span>
              </div>
              <h3 className="text-xl font-bold text-white">{event.title}</h3>
              <p className="text-sm text-slate-400">{event.subtitle}</p>
            </div>
          </div>
          <p className="text-slate-400 text-sm leading-relaxed mb-4">{event.desc}</p>
          <div className="flex flex-wrap gap-2">
            {event.tech.map(t => (
              <span key={t} className="tech-badge" style={{ background: `${event.color}15`, color: event.color, border: `1px solid ${event.color}30` }}>
                {t}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Center dot */}
      <div className="relative flex-none flex flex-col items-center" style={{ width: 48 }}>
        <div className="relative w-10 h-10 rounded-full flex items-center justify-center pulse-ring"
          style={{ background: `${event.color}20`, border: `2px solid ${event.color}`, color: event.color }}>
          <span className="text-xs font-bold font-mono">{event.year.slice(-2)}</span>
        </div>
      </div>

      {/* Empty side */}
      <div className="flex-1 max-w-md" />
    </div>
  );
}

export function Timeline() {
  return (
    <section id="origin" className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-20">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Project History</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            A Decade of <span className="grad-fire">Evolution</span>
          </h2>
          <p className="text-slate-400 max-w-xl mx-auto">From dormant Swing monolith to AI-accelerated microservices — every commit tells a story.</p>
        </div>

        {/* Timeline track */}
        <div className="relative timeline-track">
          {events.map((event, i) => (
            <TimelineEvent key={i} event={event} index={i} />
          ))}
        </div>
      </div>
    </section>
  );
}
