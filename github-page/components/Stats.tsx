"use client";
import { useEffect, useRef, useState } from "react";
import gsap from "gsap";
import { ScrollTrigger } from "gsap/ScrollTrigger";
import type { SiteMetrics } from "@/lib/metrics";

gsap.registerPlugin(ScrollTrigger);

interface StatItem {
  value: number;
  label: string;
  suffix: string;
  color: string;
  icon: string;
  sub: string;
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString("en-US", { month: "short", year: "numeric" });
}

function buildStats(metrics: SiteMetrics): StatItem[] {
  return [
    { value: metrics.commits, label: "Total Commits", suffix: "+", color: "var(--ai-cyan)", icon: "⚡", sub: `Since ${formatDate(metrics.firstCommitDate)}` },
    { value: metrics.pullRequests, label: "Pull Requests", suffix: "", color: "var(--spring-green)", icon: "🔀", sub: "100% merge rate" },
    { value: metrics.useCases, label: "Use Cases", suffix: "", color: "var(--ai-purple)", icon: "📋", sub: `CU-01 → CU-${metrics.useCases}` },
    { value: metrics.functionalRequirements, label: "Functional Requirements", suffix: "", color: "#f59e0b", icon: "📌", sub: `RF-01 → RF-${metrics.functionalRequirements}` },
    { value: metrics.coverage.backendInstruction, label: "Test Coverage", suffix: "%", color: "var(--java-orange)", icon: "🎯", sub: "JaCoCo enforced" },
    { value: metrics.markdownFiles, label: "Docs (Markdown files)", suffix: "", color: "#ec4899", icon: "📚", sub: "Living documentation" },
    { value: metrics.testClasses, label: "Test Classes", suffix: "", color: "#22d3ee", icon: "🧪", sub: "Unit + Integration + E2E" },
    { value: metrics.dockerServices, label: "Docker Services", suffix: "", color: "#a78bfa", icon: "🐳", sub: "App + Infra stacks" },
    { value: metrics.grafanaDashboards, label: "Grafana Dashboards", suffix: "", color: "#f97316", icon: "📊", sub: "Custom provisioned" },
    { value: metrics.aiTools, label: "AI Tools", suffix: "", color: "#10b981", icon: "🤖", sub: "Claude, Copilot, Gemini, OpenCode" },
    { value: metrics.adrCount, label: "Architecture Decisions", suffix: "", color: "#6366f1", icon: "🏛️", sub: `ADR-001 → ADR-0${metrics.adrCount}` },
    { value: metrics.contributors, label: "Contributors", suffix: "", color: "#f43f5e", icon: "👥", sub: "Active development" },
  ];
}

function AnimCounter({ target, suffix }: { target: number; suffix: string }) {
  const [count, setCount] = useState(target);
  const ref = useRef<HTMLSpanElement>(null);
  const started = useRef(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    // Show real value immediately — no reliance on JS animation
    setCount(target);

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && !started.current) {
        started.current = true;
        // Animate from 0 → target as a visual enhancement
        const proxy = { value: 0 };
        gsap.to(proxy, {
          value: target,
          duration: 2.5,
          ease: "power2.out",
          onUpdate() {
            setCount(Math.floor(proxy.value));
          },
          onComplete() {
            setCount(target);
          },
        });
        observer.disconnect();
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [target]);

  return <span ref={ref}>{count}{suffix}</span>;
}

function StatCard({ stat, index }: { stat: StatItem; index: number }) {
  const cardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const card = cardRef.current;
    if (!card) return;

    gsap.set(card, { opacity: 0, y: 30 });

    const trigger = ScrollTrigger.create({
      trigger: card,
      start: "top 90%",
      onEnter: () => {
        gsap.to(card, {
          opacity: 1,
          y: 0,
          duration: 0.6,
          ease: "cubic-bezier(0.34, 1.56, 0.64, 1)",
          delay: index * 0.05,
        });
      },
    });

    return () => {
      trigger.kill();
    };
  }, [index]);

  return (
    <div
      ref={cardRef}
      className="glass rounded-2xl p-6 text-center group hover:scale-105 hover:shadow-lg transition-transform duration-300 border"
      style={{ borderColor: `${stat.color}20` }}
    >
      <div className="text-3xl mb-2 group-hover:scale-110 transition-transform duration-300">
        {stat.icon}
      </div>
      <div className="text-3xl sm:text-4xl font-bold mb-1 font-mono" style={{ color: stat.color }}>
        <AnimCounter target={stat.value} suffix={stat.suffix} />
      </div>
      <div className="text-white text-xs font-semibold mb-1">{stat.label}</div>
      <div className="text-slate-500 text-xs">{stat.sub}</div>
    </div>
  );
}

export function Stats({ metrics }: { metrics: SiteMetrics }) {
  const stats = buildStats(metrics);
  const headingRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (headingRef.current) {
      gsap.from(headingRef.current.querySelector(".grad-cyan"), {
        duration: 1.2,
        backgroundPosition: "200% center",
        ease: "power2.inOut",
      });
    }
  }, []);

  return (
    <section id="today" className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">By The Numbers</p>
          <h2
            ref={headingRef}
            className="text-4xl sm:text-5xl font-bold text-white mb-4"
            style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}
          >
            A Decade of <span className="grad-cyan">Progress</span>
          </h2>
          <p className="text-slate-400 max-w-xl mx-auto">Every metric tells a story. From zero to production-grade in the AI era.</p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {stats.map((stat, i) => (
            <StatCard key={i} stat={stat} index={i} />
          ))}
        </div>
      </div>
    </section>
  );
}
