"use client";
import { Es } from "@/components/site/Es";
import { T } from "@/components/site/T";
import { useEffect, useRef } from "react";
import type { Versions } from "@/lib/tech-facts";

/** Versions come from the repository at build time (#1976); "—" means the file could not be read. */
const layersFor = (v: Versions) => [
  {
    label: "Frontend",
    color: "var(--ai-cyan)",
    ink: "#0e7490",
    items: [`Next.js ${v.next ?? "—"}`, `React ${v.react ?? "—"}`, `TypeScript ${v.typescript ?? "—"}`, "TanStack Query", "Zustand"],
    icon: "🌐",
  },
  {
    label: "REST API",
    color: "#ff9500",
    ink: "#b45309",
    items: [`Spring Boot ${v.springBoot ?? "—"}`, `Java ${v.java ?? "—"}`, "JWT (HS256)", "OpenAPI (springdoc)"],
    icon: "⚙️",
  },
  {
    label: "Application core",
    color: "var(--spring-green)",
    ink: "#15803d",
    items: ["Use cases + ports", "Domain model", "Workflow engine", "PDF reports (PDFBox)"],
    icon: "🧩",
  },
  {
    label: "Persistence",
    color: "var(--ai-purple)",
    ink: "#7e22ce",
    items: [`PostgreSQL ${v.postgres ?? "—"}`, "Spring Data JPA", "Flyway migrations", "Append-only audit"],
    icon: "🗄️",
  },
  {
    label: "Observability",
    color: "#ff375f",
    // Darker than rose-700 so axe WCAG AA passes on glass + pink badge tint (#2014).
    ink: "#9f1239",
    items: ["OpenTelemetry", "Prometheus", "Grafana · Loki · Tempo", "SonarQube"],
    icon: "📡",
  },
];

export function Architecture({ versions }: { versions: Versions }) {
  const layers = layersFor(versions);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const cards = ref.current?.querySelectorAll(".arch-card");
    if (!cards) return;

    // Transform-only entrance: never opacity:0 — axe samples the last staggered
    // card while it is still transparent and fails color-contrast (#2014).
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      return;
    }

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        cards.forEach((card, i) => {
          setTimeout(() => {
            (card as HTMLElement).style.transition = "transform 0.5s cubic-bezier(0.16,1,0.3,1)";
            (card as HTMLElement).style.transform = "translateY(0) scale(1)";
          }, i * 100);
        });
        observer.disconnect();
      }
    }, { threshold: 0.1 });

    cards.forEach(card => {
      (card as HTMLElement).style.transform = "translateY(30px) scale(0.95)";
    });

    observer.observe(ref.current!);
    return () => observer.disconnect();
  }, []);

  return (
    <Es>
    <section id="architecture" className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-emerald-700 text-sm font-mono font-bold tracking-widest mb-4 uppercase">System Architecture</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            <T text="Modern Layered Platform" />
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto"><T text="From a single Java Swing file to a multi-layer containerized platform." /></p>
        </div>

        <div ref={ref} className="flex flex-col gap-4">
          {layers.map((layer, i) => (
            <div key={i} className="arch-card glass rounded-2xl p-5 border flex items-center gap-6" style={{ borderColor: `color-mix(in srgb, ${layer.color} 15%, transparent)` }}>
              <div className="flex-none w-24 text-center">
                <div className="text-2xl mb-1">{layer.icon}</div>
                <div className="text-xs font-bold font-mono tracking-wider" style={{ color: layer.ink }}>{layer.label}</div>
              </div>
              <div className="h-8 w-px" style={{ background: `color-mix(in srgb, ${layer.color} 19%, transparent)` }} />
              <div className="flex flex-wrap gap-2">
                {layer.items.map(item => (
                  <span key={item} className="tech-badge" style={{ background: `color-mix(in srgb, ${layer.color} 6%, transparent)`, color: layer.ink, border: `1px solid color-mix(in srgb, ${layer.color} 13%, transparent)` }}>
                    {item}
                  </span>
                ))}
              </div>
              {/* Layer connector */}
              {i < layers.length - 1 && (
                <div className="absolute left-1/2 flex items-center justify-center" style={{ transform: "translateX(-50%)", marginTop: "4rem" }}>
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Bottom CTA */}
        <div className="mt-16 text-center">
          <p className="text-neutral-500 text-sm mb-6">All layers run in Docker containers. One command to start everything.</p>
          <div className="inline-block glass rounded-xl px-6 py-3 font-mono text-sm border border-cyan-500/20">
            <span className="text-neutral-500">$ </span>
            <span className="grad-cyan">bash workspace/stack/start-all.sh</span>
          </div>
        </div>
      </div>
    </section>
    </Es>
  );
}
