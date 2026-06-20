"use client";
import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import type { SiteMetrics } from "@/lib/metrics";

gsap.registerPlugin(ScrollTrigger);

interface CoverageMetric {
  name: string;
  type: string;
  percentage: number;
  color: string;
  trend: string;
  lastUpdate: string;
}

function buildCoverageData(metrics: SiteMetrics): CoverageMetric[] {
  const { coverage } = metrics;
  return [
    {
      name: "Backend (JaCoCo)",
      type: "Instruction Coverage",
      percentage: coverage.backendInstruction,
      color: "var(--ai-cyan)",
      trend: "↑ Increasing",
      lastUpdate: coverage.lastUpdated,
    },
    {
      name: "Backend (JaCoCo)",
      type: "Branch Coverage",
      percentage: coverage.backendBranch,
      color: "#f59e0b",
      trend: "↑ Increasing",
      lastUpdate: coverage.lastUpdated,
    },
    {
      name: "Frontend (Jest)",
      type: "Component Coverage",
      percentage: coverage.frontendComponent,
      color: "var(--spring-green)",
      trend: "→ Stable",
      lastUpdate: coverage.lastUpdated,
    },
    {
      name: "E2E (Playwright)",
      type: "Test Coverage",
      percentage: coverage.e2eTests,
      color: "var(--ai-purple)",
      trend: "↑ Increasing",
      lastUpdate: coverage.lastUpdated,
    },
    {
      name: "API (Bruno)",
      type: "Endpoint Coverage",
      percentage: coverage.apiEndpoints,
      color: "#ec4899",
      trend: "↑ Increasing",
      lastUpdate: coverage.lastUpdated,
    },
  ];
}

function CoverageCard({ metric, index }: { metric: CoverageMetric; index: number }) {
  const ref = useRef<HTMLDivElement>(null);
  const progressBarRef = useRef<HTMLDivElement>(null);
  const [animatedPercentage, setAnimatedPercentage] = useState(0);

  useEffect(() => {
    const el = ref.current;
    const progressBar = progressBarRef.current;
    if (!el || !progressBar) return;

    gsap.set(el, { opacity: 0, y: 30 });
    gsap.set(progressBar, { scaleX: 0, transformOrigin: "left" });

    const trigger = ScrollTrigger.create({
      trigger: el,
      start: "top 85%",
      onEnter: () => {
        gsap.to(el, {
          opacity: 1,
          y: 0,
          duration: 0.6,
          ease: "cubic-bezier(0.34, 1.56, 0.64, 1)",
          delay: index * 0.08,
        });

        gsap.to({ value: 0 }, {
          value: metric.percentage,
          duration: 1.4,
          ease: "power2.out",
          delay: index * 0.08 + 0.2,
          onUpdate(tween) {
            setAnimatedPercentage(Math.floor(tween.progress() * metric.percentage));
          },
        });

        gsap.to(progressBar, {
          scaleX: 1,
          duration: 1.4,
          ease: "power2.out",
          delay: index * 0.08 + 0.2,
        });
      },
    });

    return () => {
      trigger.kill();
    };
  }, [metric.percentage, index]);

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
              ref={progressBarRef}
              className="h-full rounded-full"
              style={{ background: metric.color }}
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

export function Coverage({ metrics }: { metrics: SiteMetrics }) {
  const coverageData = buildCoverageData(metrics);
  const headingRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (headingRef.current) {
      const section = headingRef.current.closest("section");
      if (section) {
        gsap.from(section, {
          opacity: 0,
          duration: 0.6,
          scrollTrigger: {
            trigger: section,
            start: "top 80%",
            once: true,
          },
        });
      }
    }
  }, []);

  return (
    <section className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">Quality Metrics</p>
          <h2
            ref={headingRef}
            className="text-4xl sm:text-5xl font-bold text-white mb-4"
            style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}
          >
            Test Coverage <span className="grad-cyan">Dashboard</span>
          </h2>
          <p className="text-slate-400 max-w-2xl mx-auto">
            Real-time coverage metrics across backend, frontend, E2E, and API layers. Updated on every merge to main.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-12">
          {coverageData.map((metric, i) => (
            <CoverageCard key={i} metric={metric} index={i} />
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
                  <span className="text-cyan-400 font-mono">{metrics.testClasses} classes</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">Integration Tests (H2 DB)</span>
                  <span className="text-cyan-400 font-mono">H2 + Pg</span>
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
                  <span className="text-green-400 font-mono">28 suites</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">E2E Test Cases</span>
                  <span className="text-green-400 font-mono">163 tests</span>
                </li>
                <li className="flex justify-between items-center">
                  <span className="text-slate-400">API Tests (Bruno)</span>
                  <span className="text-green-400 font-mono">86 tests</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="mt-8 text-center">
          <Link
            href="/coverage/"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full text-sm font-medium transition-all hover:scale-105 duration-300"
            style={{
              background: "linear-gradient(135deg, #06b6d4, #7c3aed)",
              color: "white",
            }}
          >
            <span>📈</span> View Detailed Coverage Reports
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        </div>
      </div>
    </section>
  );
}
