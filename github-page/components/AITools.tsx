"use client";
import { useEffect, useRef } from "react";

const tools = [
  {
    name: "Claude Code",
    maker: "Anthropic",
    role: "Primary AI agent — TDD, code review, E2E tests, docs, architecture decisions",
    color: "#f97316",
    icon: "🧠",
    badge: "Core",
    contrib: "~80% of AI work",
  },
  {
    name: "OpenCode",
    maker: "OpenCode",
    role: "Terminal-native AI coding agent, integrated directly into the shell workflow",
    color: "var(--ai-cyan)",
    icon: "⌨️",
    badge: "Shell",
    contrib: "RTK token proxy",
  },
  {
    name: "GitHub Copilot",
    maker: "Microsoft / GitHub",
    role: "VS Code inline completions, chat, and PR summaries for rapid in-editor development",
    color: "#6366f1",
    icon: "✈️",
    badge: "IDE",
    contrib: "Inline completions",
  },
  {
    name: "Gemini CLI",
    maker: "Google",
    role: "Command-line Gemini access for research, alternative perspectives, and cross-validation",
    color: "#22c55e",
    icon: "💎",
    badge: "Research",
    contrib: "Cross-validation",
  },
];

export function AITools() {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const cards = ref.current?.querySelectorAll(".tool-card");
    if (!cards) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        cards.forEach((card, i) => {
          setTimeout(() => {
            (card as HTMLElement).style.transition = "opacity 0.6s, transform 0.6s cubic-bezier(0.16,1,0.3,1)";
            (card as HTMLElement).style.opacity = "1";
            (card as HTMLElement).style.transform = "translateY(0)";
          }, i * 100);
        });
        observer.disconnect();
      }
    }, { threshold: 0.15 });

    cards.forEach(card => {
      (card as HTMLElement).style.opacity = "0";
      (card as HTMLElement).style.transform = "translateY(40px)";
    });
    if (ref.current) observer.observe(ref.current);
    return () => observer.disconnect();
  }, []);

  return (
    <section className="py-24 px-6" style={{ background: "var(--bg-mid)" }}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-14">
          <p className="text-orange-600 text-sm font-mono font-bold tracking-widest mb-4 uppercase">AI Arsenal</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-neutral-900 mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            The <span className="grad-fire">AI Tools</span> That Built It
          </h2>
          <p className="text-neutral-600 max-w-xl mx-auto">
            Four AI tools, one codebase. Each played a different role in accelerating from idea to production.
          </p>
        </div>

        <div ref={ref} className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          {tools.map((t, i) => (
            <div key={i} className="tool-card glass rounded-2xl p-6 border group hover:scale-[1.02] transition-transform duration-300" style={{ borderColor: `${t.color}25` }}>
              <div className="flex items-start gap-4">
                <div className="text-4xl flex-none">{t.icon}</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1 flex-wrap">
                    <span className="text-neutral-900 font-bold">{t.name}</span>
                    <span className="tech-badge text-xs" style={{ background: `${t.color}15`, color: t.color, border: `1px solid ${t.color}30` }}>{t.badge}</span>
                  </div>
                  <div className="text-neutral-500 text-xs mb-2">{t.maker}</div>
                  <p className="text-neutral-600 text-sm leading-relaxed">{t.role}</p>
                  <div className="mt-3 flex items-center gap-2">
                    <div className="h-1 rounded-full flex-1 bg-neutral-100">
                      <div className="h-1 rounded-full transition-all duration-1000" style={{ background: t.color, width: t.contrib.startsWith("~80") ? "80%" : t.contrib.startsWith("RTK") ? "55%" : "40%" }} />
                    </div>
                    <span className="text-xs font-mono" style={{ color: t.color }}>{t.contrib}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
