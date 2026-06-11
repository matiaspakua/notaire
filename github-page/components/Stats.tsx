"use client";
import { useEffect, useRef, useState } from "react";

const stats = [
  { value: 527, label: "Total Commits", suffix: "+", color: "var(--ai-cyan)", icon: "⚡" },
  { value: 78, label: "Use Cases Documented", suffix: "", color: "var(--spring-green)", icon: "📋" },
  { value: 95, label: "Functional Requirements", suffix: "", color: "var(--ai-purple)", icon: "📌" },
  { value: 80, label: "Test Coverage Target", suffix: "%", color: "var(--java-orange)", icon: "🎯" },
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
    <section id="today" className="py-24 px-6" style={{ background: "var(--bg-dark)" }}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-16">
          <p className="text-cyan-400 text-sm font-mono font-bold tracking-widest mb-4 uppercase">By The Numbers</p>
          <h2 className="text-4xl sm:text-5xl font-bold text-white mb-4" style={{ fontFamily: "var(--font-display)", letterSpacing: "-0.02em" }}>
            A Decade of <span className="grad-cyan">Progress</span>
          </h2>
        </div>

        <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((s, i) => (
            <div key={i} className="glass rounded-2xl p-8 text-center group hover:scale-105 transition-transform duration-300 border" style={{ borderColor: `${s.color}25` }}>
              <div className="text-4xl mb-3">{s.icon}</div>
              <div className="text-4xl sm:text-5xl font-bold mb-2 font-mono" style={{ color: s.color }}>
                <AnimCounter target={s.value} suffix={s.suffix} />
              </div>
              <div className="text-slate-400 text-sm font-medium">{s.label}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
