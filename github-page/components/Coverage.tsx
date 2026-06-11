"use client";
import { useEffect, useRef, useState } from "react";

interface CoverageMetric {
  name: string;
  type: string;
  percentage: number;
  color: string;
  trend: string;
  lastUpdate: string;
}

const coverageData: CoverageMetric[] = [
  {
    name: "Backend (JaCoCo)",
    type: "Instruction Coverage",
    percentage: 31,
    color: "var(--ai-cyan)",
    trend: "↑ +5% this month",
    lastUpdate: "2026-06-11",
  },
  {
    name: "Backend (JaCoCo)",
    type: "Branch Coverage",
    percentage: 18,
    color: "#f59e0b",
    trend: "↑ +3% this month",
    lastUpdate: "2026-06-11",
  },
  {
    name: "Frontend (Jest)",
    type: "Component Coverage",
    percentage: 45,
    color: "var(--spring-green)",
    trend: "→ Stable",
    lastUpdate: "2026-06-11",
  },
  {
    name: "E2E (Playwright)",
    type: "Test Coverage",
    percentage: 85,
    color: "var(--ai-purple)",
    trend: "↑ +10% this month",
    lastUpdate: "2026-06-11",
  },
  {
    name: "API (Bruno)",
    type: "Endpoint Coverage",
    percentage: 78,
    color: "#ec4899",
    trend: "↑ +8% this month",
    lastUpdate: "2026-06-11",
  },
];

function CoverageCard({ metric }: { metric: CoverageMetric }) {
  const ref = useRef<HTMLDivElement>(null);
  const [animatedPercentage, setAnimatedPercentage] = useState(0);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.style.opacity = "0";
    el.style.transform = "translateY(20px)";

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          el.style.transition = "opacity 0.5s, transform 0.5s cubic-bezier(0.16,1,0.3,1)";
          el.style.opacity = "1";
          el.style.transform = "translateY(0)";
        }, 100);

        let current = 0;
        const interval = setInterval(() => {
          current += Math.random() * 15;
          if (current >= metric.percentage) {
            setAnimatedPercentage(metric.percentage);
            clearInterval(interval);
          } else {
            setAnimatedPercentage(Math.floor(current));
          }
        }, 30);

        observer.disconnect();
      }
    }, { threshold: 0.2 });

    observer.observe(el);
    return () => observer.disconnect();
  }, [metric.percentage]);

  return (
    <div ref={ref} className="glass rounded-xl p-5 border" style={{ borderColor: `${metric.color}25` }}>
      <div className="flex items-start justify-between mb-3">
        <div>
          <div className="text-sm font-bold" style={{ color: metric.color }}>
            {metric.name}
          </div>
          <div className="text-xs text-slate-500 mt-0.5">{metric.type}</div>
        </div>
        <div className="text-xs px-2 py-1 rounded" style={{ background: `${metric.color}15`, color: metric.color }}>
          {metric.trend}
        </div>
      </div>

      <div className="flex items-end gap-4">
        <div className="flex-1">
          <div className="h-2 bg-slate-800 rounded-full overflow-hidden mb-1">
            <div
              className="h-full rounded-full transition-all duration-300"
              style={{ background: metric.color, width: `${animatedPercentage}%` }}
            />
          </div>
          <div className="flex justify-between items-center">
            <span className="text-xs text-slate-400">Coverage</span>
            <span className="text-sm font-bold" style={{ color: metric.color }}>
              {animatedPercentage}%
            </span>
          </div>
        </div>
      </div>

      <div className="text-xs text-slate-500 mt-2">Updated: {metric.lastUpdate}</div>
    </div>
  );
}

export function Coverage() {
  return (
    <section className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Quality Metrics</p>
          <h2
            className="text-4xl sm:text-5xl font-bold text-white mb-4"
            style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}
          >
            Test Coverage <span className="grad-cyan">Dashboard</span>
          </h2>
          <p className="text-slate-400 max-w-2xl mx-auto">
            Real-time coverage metrics across backend, frontend, E2E, and API layers. Updated every commit and nightly.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-12">
          {coverageData.map((metric, i) => (
            <CoverageCard key={i} metric={metric} />
          ))}
        </div>

        <div className="glass rounded-2xl p-8 border border-cyan-500/20">
          <h3 className="text-white font-bold mb-4 flex items-center gap-2">
            <span>📊</span> Coverage by Test Type
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <h4 className="text-cyan-400 text-sm font-semibold mb-4">Backend Testing</h4>
              <ul className="space-y-3 text-sm">
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Unit Tests (JUnit 5)</span>
                  <span className="text-cyan-400 font-mono">67 tests</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Integration Tests (H2 DB)</span>
                  <span className="text-cyan-400 font-mono">30+ tests</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">JaCoCo Enforcement</span>
                  <span className="text-cyan-400 font-mono">28% floor</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Target Coverage</span>
                  <span className="text-green-400 font-mono">80%</span>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="text-green-400 text-sm font-semibold mb-4">Frontend Testing</h4>
              <ul className="space-y-3 text-sm">
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Component Tests (React)</span>
                  <span className="text-green-400 font-mono">3+ suites</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">E2E Tests (Playwright)</span>
                  <span className="text-green-400 font-mono">500+ tests</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">API Tests (Bruno)</span>
                  <span className="text-green-400 font-mono">50+ endpoints</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Critical Flows</span>
                  <span className="text-green-400 font-mono">100%</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="mt-8 text-center">
          <a
            href="https://matiaspakua.github.io/notaire/coverage/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition-all"
            style={{
              background: "linear-gradient(135deg, #06b6d4, #7c3aed)",
              color: "white",
            }}
          >
            <span>📈</span> View Detailed Coverage Reports
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </a>
        </div>
      </div>
    </section>
  );
}
