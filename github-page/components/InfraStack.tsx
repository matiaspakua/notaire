"use client";
import { useEffect, useRef, useState } from "react";

const services = [
  {
    name: "Homer",
    port: "8888",
    role: "DevSecOps Hub — single-page dashboard linking every service",
    icon: "🏠",
    color: "#ff9500",
    category: "Dashboard",
  },
  {
    name: "Next.js Frontend",
    port: "3000",
    role: "React 19 + TypeScript UI. Notarial forms, dashboards, audit trail",
    icon: "▲",
    color: "var(--ai-cyan)",
    category: "App",
  },
  {
    name: "Spring Boot API",
    port: "8080",
    role: "REST API + Swagger UI. JWT auth, JasperReports, audit aspect",
    icon: "🌱",
    color: "var(--spring-green)",
    category: "App",
  },
  {
    name: "PostgreSQL 16",
    port: "5432",
    role: "Primary database. Managed by Flyway migrations + init-db scripts",
    icon: "🐘",
    color: "#336791",
    category: "Data",
  },
  {
    name: "pgAdmin",
    port: "5050",
    role: "Database management UI for schema inspection and query execution",
    icon: "🗄️",
    color: "#336791",
    category: "Data",
  },
  {
    name: "Prometheus",
    port: "9090",
    role: "Scrapes Spring Boot Actuator + PostgreSQL exporter every 10–15 s",
    icon: "🔥",
    color: "#e6522c",
    category: "Observability",
  },
  {
    name: "Grafana",
    port: "3001",
    role: "4 custom dashboards: backend, postgres, logs, auth. Real-time alerts",
    icon: "📊",
    color: "#f46800",
    category: "Observability",
  },
  {
    name: "Loki + Promtail",
    port: "3100",
    role: "Structured JSON log aggregation via Logback LogstashEncoder",
    icon: "📋",
    color: "#f97316",
    category: "Observability",
  },
  {
    name: "SonarQube",
    port: "9000",
    role: "SAST, code smells, coverage gating. Community Edition, auto-provisioned",
    icon: "🛡️",
    color: "#4e9bcd",
    category: "Quality",
  },
  {
    name: "PostgreSQL Exporter",
    port: "9187",
    role: "Translates pg_stat_* to Prometheus metrics for Grafana's DB dashboard",
    icon: "📡",
    color: "#336791",
    category: "Observability",
  },
];

const alerts = [
  { name: "HighLoginFailureRate", severity: "warning", desc: ">50% login failures for 2 min" },
  { name: "SuspiciousLoginActivity", severity: "critical", desc: "Brute-force: >30 bad creds/min" },
  { name: "BackendDown", severity: "critical", desc: "API unreachable for >1 min" },
  { name: "HighJvmHeapUsage", severity: "warning", desc: "JVM heap >85% for 5 min" },
];

const categories = ["All", "App", "Data", "Observability", "Quality", "Dashboard"] as const;

export function InfraStack() {
  const [active, setActive] = useState<string>("All");
  const ref = useRef<HTMLDivElement>(null);

  const filtered = active === "All" ? services : services.filter(s => s.category === active);

  useEffect(() => {
    const cards = ref.current?.querySelectorAll(".svc-card");
    if (!cards) return;
    cards.forEach((card, i) => {
      const el = card as HTMLElement;
      el.style.opacity = "0";
      el.style.transform = "scale(0.95)";
      setTimeout(() => {
        el.style.transition = "opacity 0.4s, transform 0.4s";
        el.style.opacity = "1";
        el.style.transform = "scale(1)";
      }, i * 50);
    });
  }, [active]);

  return (
    <section className="py-32 px-6 relative overflow-hidden" style={{ background: "var(--bg-dark)" }}>
      <div className="absolute inset-0 pointer-events-none" style={{ background: "radial-gradient(ellipse at bottom, rgba(10,132,255,0.06) 0%, transparent 60%)" }} />
      <div className="max-w-6xl mx-auto relative z-10">
        {/* Header */}
        <div className="text-center mb-16">
          <p className="text-[#0A84FF] text-sm font-mono font-bold tracking-widest mb-4 uppercase">Infrastructure</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            DevSecOps <span className="grad-cyan">Platform</span>
          </h2>
          <p className="text-neutral-600 max-w-2xl mx-auto">
            10 containerized services. Two Docker Compose stacks. One command to run everything.
            From a university project with <em>no infrastructure</em> to a full observability platform.
          </p>
        </div>

        {/* Category filter */}
        <div className="flex gap-2 flex-wrap justify-center mb-10">
          {categories.map(cat => (
            <button key={cat} onClick={() => setActive(cat)}
              className="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
              style={active === cat
                ? { background: "rgba(10,132,255,0.12)", color: "var(--ai-cyan)", border: "1px solid rgba(10,132,255,0.35)" }
                : { background: "transparent", color: "#86868b", border: "1px solid #d2d2d7" }}>
              {cat}
            </button>
          ))}
        </div>

        {/* Services grid */}
        <div ref={ref} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-16">
          {filtered.map((s, i) => (
            <div key={i} className="svc-card glass rounded-xl p-5 border group hover:scale-[1.02] transition-transform" style={{ borderColor: `${s.color}25` }}>
              <div className="flex items-start gap-3">
                <span className="text-2xl flex-none">{s.icon}</span>
                <div className="min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <span className="text-neutral-900 font-semibold text-sm">{s.name}</span>
                    <code className="text-xs px-2 py-0.5 rounded" style={{ background: `${s.color}15`, color: s.color }}>:{s.port}</code>
                  </div>
                  <p className="text-neutral-600 text-xs leading-relaxed">{s.role}</p>
                </div>
              </div>
              <div className="mt-3 w-6 h-0.5 rounded-full group-hover:w-12 transition-all duration-300" style={{ background: s.color }} />
            </div>
          ))}
        </div>

        {/* Two-column: start cmd + alert rules */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Start commands */}
          <div className="glass rounded-2xl p-6 border border-[#0A84FF]/20">
            <h3 className="text-neutral-900 font-bold mb-4 flex items-center gap-2">
              <span>🚀</span> Start Everything
            </h3>
            <div className="space-y-3">
              {[
                { cmd: "bash scripts/start.sh", desc: "App stack (Backend + Postgres + pgAdmin)" },
                { cmd: "bash scripts/start-infra.sh", desc: "Infra stack (Prometheus + Grafana + Loki + SonarQube)" },
                { cmd: "bash scripts/start-all.sh", desc: "Everything at once" },
              ].map((c, i) => (
                <div key={i} className="rounded-xl p-3" style={{ background: "rgba(0,0,0,0.03)" }}>
                  <code className="text-[#0A84FF] text-xs font-mono">$ {c.cmd}</code>
                  <p className="text-neutral-500 text-xs mt-1">{c.desc}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Alert rules */}
          <div className="glass rounded-2xl p-6 border border-[#AF52DE]/20">
            <h3 className="text-neutral-900 font-bold mb-4 flex items-center gap-2">
              <span>🔔</span> Active Alert Rules
            </h3>
            <div className="space-y-3">
              {alerts.map((a, i) => (
                <div key={i} className="flex items-start gap-3 rounded-xl p-3" style={{ background: "rgba(0,0,0,0.03)" }}>
                  <span className={`mt-0.5 w-2 h-2 rounded-full flex-none ${a.severity === "critical" ? "bg-red-500" : "bg-yellow-500"}`} />
                  <div>
                    <div className="text-neutral-900 text-xs font-mono font-medium">{a.name}</div>
                    <div className="text-neutral-500 text-xs">{a.desc}</div>
                  </div>
                  <span className={`ml-auto text-xs px-2 py-0.5 rounded-full flex-none ${a.severity === "critical" ? "bg-red-500/15 text-red-600" : "bg-yellow-500/15 text-yellow-600"}`}>{a.severity}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
