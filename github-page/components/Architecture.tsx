"use client";
import { useEffect, useRef } from "react";

const layers = [
  {
    label: "Frontend",
    color: "var(--ai-cyan)",
    items: ["Next.js 15", "React 19", "TypeScript", "Tailwind CSS", "Zustand"],
    icon: "🌐",
  },
  {
    label: "API Gateway",
    color: "#ff9500",
    items: ["Spring Boot 4", "REST Controllers", "JWT Auth", "OpenAPI / Swagger"],
    icon: "⚙️",
  },
  {
    label: "Services",
    color: "var(--spring-green)",
    items: ["Business Logic", "Audit Aspect", "JasperReports", "Email Service"],
    icon: "🧩",
  },
  {
    label: "Persistence",
    color: "var(--ai-purple)",
    items: ["PostgreSQL 16", "Spring Data JPA", "Flyway Migrations", "HikariCP"],
    icon: "🗄️",
  },
  {
    label: "Observability",
    color: "#ff375f",
    items: ["Prometheus", "Grafana", "Loki", "SonarQube"],
    icon: "📡",
  },
];

export function Architecture() {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const cards = ref.current?.querySelectorAll(".arch-card");
    if (!cards) return;

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        cards.forEach((card, i) => {
          setTimeout(() => {
            (card as HTMLElement).style.transition = "opacity 0.5s, transform 0.5s cubic-bezier(0.16,1,0.3,1)";
            (card as HTMLElement).style.opacity = "1";
            (card as HTMLElement).style.transform = "translateY(0) scale(1)";
          }, i * 100);
        });
        observer.disconnect();
      }
    }, { threshold: 0.1 });

    cards.forEach(card => {
      (card as HTMLElement).style.opacity = "0";
      (card as HTMLElement).style.transform = "translateY(30px) scale(0.95)";
    });

    observer.observe(ref.current!);
    return () => observer.disconnect();
  }, []);

  return (
    <section id="architecture" className="py-32 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-[#34C759] text-sm font-mono font-bold tracking-widest mb-4 uppercase">System Architecture</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Modern <span className="grad-green">Microservices</span>
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">From a single Java Swing file to a multi-layer containerized platform.</p>
        </div>

        <div ref={ref} className="flex flex-col gap-4">
          {layers.map((layer, i) => (
            <div key={i} className="arch-card glass rounded-2xl p-5 border flex items-center gap-6" style={{ borderColor: `${layer.color}25` }}>
              <div className="flex-none w-24 text-center">
                <div className="text-2xl mb-1">{layer.icon}</div>
                <div className="text-xs font-bold font-mono tracking-wider" style={{ color: layer.color }}>{layer.label}</div>
              </div>
              <div className="h-8 w-px" style={{ background: `${layer.color}30` }} />
              <div className="flex flex-wrap gap-2">
                {layer.items.map(item => (
                  <span key={item} className="tech-badge" style={{ background: `${layer.color}10`, color: layer.color, border: `1px solid ${layer.color}20` }}>
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
            <span className="grad-cyan">bash scripts/start-all.sh</span>
          </div>
        </div>
      </div>
    </section>
  );
}
