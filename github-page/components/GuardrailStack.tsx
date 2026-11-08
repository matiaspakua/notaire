import { GUARDRAILS, src } from "@/lib/ai-sdlc-content";

/**
 * Defence in depth (#1970): the nine guardrail layers from GUARDRAILS drawn as nested rings
 * around `main`. An agent's change starts outside and must cross every ring to land.
 * Inline SVG, named by <title>/<desc>; on narrow screens an ordered list replaces it;
 * the rings light up in turn, outermost first (a change crossing each layer); the animation stops when
 * the reader prefers reduced motion.
 */
const WHERE: Record<string, { tone: string; fill: string; stroke: string; text: string }> = {
  agent: { tone: "Agent side", fill: "#f5f3ff", stroke: "#a78bfa", text: "#4c1d95" },
  local: { tone: "Local machine", fill: "#eef6ff", stroke: "#7fbcff", text: "#063a70" },
  ci: { tone: "CI on every PR", fill: "#fffbeb", stroke: "#f59e0b", text: "#78350f" },
  github: { tone: "GitHub", fill: "#ecfdf5", stroke: "#34d399", text: "#064e3b" },
  human: { tone: "Human", fill: "#fff1f2", stroke: "#fb7185", text: "#881337" },
};

function where(i: number): keyof typeof WHERE {
  if (i <= 3) return "agent";
  if (i === 4) return "local";
  if (i <= 6) return "ci";
  if (i === 7) return "github";
  return "human";
}

const W = 960;
const H = 600;
const BAND = 30;

export function GuardrailStack() {
  const layers = GUARDRAILS.map((g, i) => ({ ...g, name: g.layer.replace(/^\d+\s*·\s*/, ""), n: i + 1, where: where(i) }));
  return (
    <figure className="my-10">
      <div className="hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:block sm:p-6">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-labelledby="gs-title gs-desc" className="h-auto w-full">
          <title id="gs-title">Nine guardrail layers between an AI agent and the main branch</title>
          <desc id="gs-desc">
            {`Nested rings, outermost first: ${layers.map((l) => `${l.n} ${l.name}`).join(", ")}. A change reaches main only after crossing every ring.`}
          </desc>
          <style>{`
            .gs-ring{animation:gs-pass 5.4s ease-in-out infinite;stroke-width:1.5}
            @keyframes gs-pass{0%,14%,100%{stroke-width:1.5}5%{stroke-width:4}}
            @media (prefers-reduced-motion: reduce){.gs-ring{animation:none}}
          `}</style>
          {layers.map((l, i) => {
            const c = WHERE[l.where];
            const x = 8 + i * BAND;
            const y = 8 + i * BAND;
            return (
              <g key={l.layer}>
                <rect x={x} y={y} width={W - 2 * x} height={H - 2 * y} rx={34 - i * 2} fill={c.fill} stroke={c.stroke} className="gs-ring" style={{ animationDelay: `${i * 0.5}s` }} />
                <text x={x + 18} y={y + 20} fontSize={13} fontWeight={600} fill={c.text}>
                  <tspan fontFamily="var(--font-mono)" fontWeight={700}>{String(l.n).padStart(2, "0")}</tspan>
                  <tspan dx={8}>{l.name}</tspan>
                </text>
                <text x={W - x - 18} y={y + 20} fontSize={11} textAnchor="end" fill={c.text} opacity={0.85}>
                  {c.tone}
                </text>
              </g>
            );
          })}
          <g>
            <rect x={W / 2 - 92} y={H / 2 - 30} width={184} height={60} rx={14} fill="#0f172a" />
            <text x={W / 2} y={H / 2 - 4} textAnchor="middle" fontSize={18} fontWeight={700} fill="#ffffff" fontFamily="var(--font-mono)">
              main
            </text>
            <text x={W / 2} y={H / 2 + 16} textAnchor="middle" fontSize={11} fill="#cbd5e1">
              protected branch
            </text>
          </g>
        </svg>
      </div>
      <ol className="space-y-2 sm:hidden" aria-label="Guardrail layers, outermost first">
        {layers.map((l) => {
          const c = WHERE[l.where];
          return (
            <li key={l.layer} className="flex items-center gap-3 rounded-xl border px-3 py-2" style={{ background: c.fill, borderColor: c.stroke }}>
              <span className="w-6 shrink-0 font-mono text-sm font-bold" style={{ color: c.text }}>
                {String(l.n).padStart(2, "0")}
              </span>
              <span className="flex-1 text-sm font-semibold text-slate-900">{l.name}</span>
              <span className="text-[11px]" style={{ color: c.text }}>
                {c.tone}
              </span>
            </li>
          );
        })}
        <li className="rounded-xl bg-slate-900 px-3 py-2 text-center font-mono text-sm font-bold text-white">main</li>
      </ol>
      <figcaption className="mt-3 text-center text-sm text-slate-600">
        Defence in depth: each ring is a different mechanism, so one failure is not a breach. Layers and sources:{" "}
        <a className="underline" href={src("CONSTITUTION.md")}>
          CONSTITUTION.md
        </a>{" "}
        and the cards below.
      </figcaption>
    </figure>
  );
}
