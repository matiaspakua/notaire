"use client";
import { useEffect, useRef } from "react";

const eras = [
  {
    label: "2014",
    title: "The Monolith",
    color: "var(--java-orange)",
    code: `// 2014 — Everything in one place
class NotaireForm extends JFrame {
  private Connection conn;

  void saveDocument() {
    // Direct SQL from event handler
    PreparedStatement ps = conn.prepareStatement(
      "INSERT INTO escrituras VALUES(?,?,?)"
    );
    ps.setString(1, txtNombre.getText());
    // business logic tangled in UI
  }
}`,
    stack: [
      { name: "Java 1.6", icon: "☕" },
      { name: "Swing GUI", icon: "🖥️" },
      { name: "MySQL", icon: "🗄️" },
      { name: "JDBC", icon: "🔌" },
    ],
  },
  {
    label: "2025",
    title: "Spring Boot",
    color: "var(--spring-green)",
    code: `// 2025 — Clean separation
@RestController
@RequestMapping("/api/v1/escrituras")
public class EscrituraController {

  @Autowired
  EscrituraService service;

  @PostMapping
  public ResponseEntity<DtoEscritura> create(
      @RequestBody @Valid DtoEscritura dto) {
    return ResponseEntity.ok(service.save(dto));
  }
}`,
    stack: [
      { name: "Java 21", icon: "☕" },
      { name: "Spring Boot 4", icon: "🌱" },
      { name: "PostgreSQL 16", icon: "🐘" },
      { name: "Docker", icon: "🐳" },
    ],
  },
  {
    label: "2026",
    title: "AI-Powered",
    color: "var(--ai-purple)",
    code: `// 2026 — AI-accelerated development
// Claude AI generates tests, reviews code,
// writes E2E tests, updates documentation

const tramite = await page.getByRole(
  'link', { name: 'Nuevo Trámite' }
);
await expect(tramite).toBeVisible();
await tramite.click();

// 200+ Playwright E2E tests
// 84% JaCoCo line coverage
// Grafana + Loki observability`,
    stack: [
      { name: "Next.js 15", icon: "▲" },
      { name: "Claude AI", icon: "🤖" },
      { name: "Playwright", icon: "🎭" },
      { name: "Grafana", icon: "📊" },
    ],
  },
];

function EraCard({ era, index }: { era: (typeof eras)[0]; index: number }) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    el.style.opacity = "0";
    el.style.transform = "translateY(50px)";

    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          el.style.transition = "opacity 0.7s, transform 0.7s cubic-bezier(0.16,1,0.3,1)";
          el.style.opacity = "1";
          el.style.transform = "translateY(0)";
        }, index * 150);
        observer.disconnect();
      }
    }, { threshold: 0.15 });
    observer.observe(el);
    return () => observer.disconnect();
  }, [index]);

  return (
    <div ref={ref} className="flex-1 min-w-[280px] max-w-sm">
      <div className="glass rounded-2xl overflow-hidden border" style={{ borderColor: `${era.color}30` }}>
        {/* Header */}
        <div className="px-6 py-4 flex items-center justify-between" style={{ borderBottom: `1px solid ${era.color}20`, background: `${era.color}08` }}>
          <div>
            <div className="text-xs font-mono font-bold tracking-widest mb-1" style={{ color: era.color }}>{era.label}</div>
            <div className="text-neutral-900 font-bold">{era.title}</div>
          </div>
          <div className="flex gap-1">
            <div className="w-2.5 h-2.5 rounded-full bg-red-500/60" />
            <div className="w-2.5 h-2.5 rounded-full bg-yellow-500/60" />
            <div className="w-2.5 h-2.5 rounded-full bg-green-500/60" />
          </div>
        </div>

        {/* Code block */}
        <div className="px-4 py-4 overflow-auto" style={{ background: "#0a0a12" }}>
          <pre className="text-xs leading-relaxed" style={{ fontFamily: "var(--font-mono)", color: "#94a3b8" }}>
            <code>{era.code}</code>
          </pre>
        </div>

        {/* Tech stack */}
        <div className="px-6 py-4 flex flex-wrap gap-2" style={{ borderTop: `1px solid ${era.color}20` }}>
          {era.stack.map(s => (
            <span key={s.name} className="flex items-center gap-1.5 text-xs font-medium px-3 py-1.5 rounded-full"
              style={{ background: `${era.color}12`, color: era.color, border: `1px solid ${era.color}25` }}>
              <span>{s.icon}</span> {s.name}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}

export function TechEvolution() {
  return (
    <section id="evolution" className="py-32 px-6" style={{ background: "var(--bg-mid)" }}>
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-[#AF52DE] text-sm font-mono font-bold tracking-widest mb-4 uppercase">Tech Evolution</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            Three <span className="grad-green">Generations</span>
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">The same business domain, reimagined through each era of software engineering.</p>
        </div>

        {/* Arrow connectors + cards */}
        <div className="flex flex-col lg:flex-row items-start gap-6 lg:gap-4">
          {eras.map((era, i) => (
            <div key={i} className="flex flex-col lg:flex-row items-center gap-4 flex-1">
              <EraCard era={era} index={i} />
              {i < eras.length - 1 && (
                <div className="hidden lg:flex items-center justify-center text-neutral-500 text-2xl flex-none">→</div>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
