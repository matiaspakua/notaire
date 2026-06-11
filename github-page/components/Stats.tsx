"use client";
import { useEffect, useRef, useState } from "react";

const stats = [
  { value: 527, label: "Total Commits", suffix: "+", color: "var(--ai-cyan)", icon: "⚡", sub: "Since March 2014" },
  { value: 159, label: "Pull Requests", suffix: "", color: "var(--spring-green)", icon: "🔀", sub: "100% merge rate" },
  { value: 78, label: "Use Cases", suffix: "", color: "var(--ai-purple)", icon: "📋", sub: "CU-01 → CU-78" },
  { value: 95, label: "Functional Requirements", suffix: "", color: "#f59e0b", icon: "📌", sub: "RF-01 → RF-95" },
  { value: 80, label: "Test Coverage", suffix: "%", color: "var(--java-orange)", icon: "🎯", sub: "JaCoCo enforced" },
  { value: 545, label: "Docs (Markdown files)", suffix: "", color: "#ec4899", icon: "📚", sub: "Living documentation" },
  { value: 69, label: "Test Classes", suffix: "", color: "#22d3ee", icon: "🧪", sub: "Unit + Integration" },
  { value: 10, label: "Docker Services", suffix: "", color: "#a78bfa", icon: "🐳", sub: "App + Infra stacks" },
  { value: 4, label: "Grafana Dashboards", suffix: "", color: "#f97316", icon: "📊", sub: "Custom provisioned" },
  { value: 4, label: "AI Tools", suffix: "", color: "#10b981", icon: "🤖", sub: "Claude, Copilot, Gemini, OpenCode" },
  { value: 11, label: "Architecture Decisions", suffix: "", color: "#6366f1", icon: "🏛️", sub: "ADR-001 → ADR-011" },
  { value: 97, label: "Branches Created", suffix: "", color: "#f43f5e", icon: "🌿", sub: "feature, fix, refactor, docs…" },
];

function AnimCounter({ target, suffix }: { target: number; suffix: string }) {
  const [count, setCount] = useState(0);
  const ref = useRef<HTMLSpanElement>(null);
  const started = useRef(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && !started.current) {
        started.current = true;
        const duration = 1800;
        const startTime = performance.now();
        const step = (now: number) => {
          const pct = Math.min((now - startTime) / duration, 1);
          const ease = 1 - Math.pow(1 - pct, 3);
          setCount(Math.floor(ease * target));
          if (pct < 1) requestAnimationFrame(step);
          else setCount(target);
        };
        requestAnimationFrame(step);
        observer.disconnect();
      }
    }, { threshold: 0.3 });
    observer.observe(el);
    return () => observer.disconnect();
  }, [target]);

  return <span ref={ref}>{count}{suffix}</span>;
}

export function Stats() {
  return (
    <section id="today" className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">By The Numbers</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            A Decade of <span className="grad-cyan">Progress</span>
          </h2>
          <p className="text-slate-400 max-w-xl mx-auto">Every metric tells a story. From zero to production-grade in the AI era.</p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {stats.map((s, i) => (
            <div key={i} className="glass rounded-2xl p-6 text-center group hover:scale-105 transition-transform duration-300 border" style={{ borderColor: `${s.color}20` }}>
              <div className="text-3xl mb-2">{s.icon}</div>
              <div className="text-3xl sm:text-4xl font-bold mb-1 font-mono" style={{ color: s.color }}>
                <AnimCounter target={s.value} suffix={s.suffix} />
              </div>
              <div className="text-white text-xs font-semibold mb-1">{s.label}</div>
              <div className="text-slate-500 text-xs">{s.sub}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
