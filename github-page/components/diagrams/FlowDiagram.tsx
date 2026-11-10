"use client";

import { motion } from "framer-motion";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";
import { StepControls, type Step } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type FlowNode = {
  id: string;
  label: string;
  sub?: string;
  x: number;
  y: number;
  w?: number;
  kind?: "actor" | "adapter" | "port" | "core" | "store" | "check" | "fail" | "ok";
};
export type FlowEdge = { from: string; to: string; label?: string; dashed?: boolean };
export type FlowStep = Step & { nodes: string[]; edges?: number[] };

const H = 54;
const KIND: Record<NonNullable<FlowNode["kind"]>, { fill: string; stroke: string; ink: string }> = {
  actor: { fill: "#f8fafc", stroke: "#94a3b8", ink: "#0f172a" },
  adapter: { fill: "#eff6ff", stroke: "#93c5fd", ink: "#1e3a8a" },
  port: { fill: "#f5f3ff", stroke: "#c4b5fd", ink: "#4c1d95" },
  core: { fill: "#ecfdf5", stroke: "#6ee7b7", ink: "#064e3b" },
  store: { fill: "#fffbeb", stroke: "#fcd34d", ink: "#78350f" },
  check: { fill: "#f8fafc", stroke: "#cbd5e1", ink: "#0f172a" },
  fail: { fill: "#fff1f2", stroke: "#fda4af", ink: "#881337" },
  ok: { fill: "#f0fdf4", stroke: "#86efac", ink: "#14532d" },
};

function edgePath(a: FlowNode, b: FlowNode): string {
  const aw = (a.w ?? 150) / 2;
  const bw = (b.w ?? 150) / 2;
  const dx = b.x - a.x;
  const dy = b.y - a.y;
  if (Math.abs(dx) >= Math.abs(dy)) {
    const sx = a.x + Math.sign(dx) * aw;
    const ex = b.x - Math.sign(dx) * bw;
    const mx = (sx + ex) / 2;
    return `M${sx} ${a.y} C ${mx} ${a.y}, ${mx} ${b.y}, ${ex} ${b.y}`;
  }
  const sy = a.y + Math.sign(dy) * (H / 2);
  const ey = b.y - Math.sign(dy) * (H / 2);
  const my = (sy + ey) / 2;
  return `M${a.x} ${sy} C ${a.x} ${my}, ${b.x} ${my}, ${b.x} ${ey}`;
}

/**
 * A node-and-edge diagram that a reader steps through (#1977). Each step lights its nodes and
 * draws its edges; the SVG is decorative for assistive tech, which reads the step list instead.
 */
export function FlowDiagram({
  id,
  title,
  desc,
  width,
  height,
  nodes,
  edges,
  steps,
  caption,
}: {
  id: string;
  title: string;
  desc: string;
  width: number;
  height: number;
  nodes: FlowNode[];
  edges: FlowEdge[];
  steps: FlowStep[];
  caption: string;
}) {
  const { locale } = usePreferences();
  const titleText = tr(locale, title);
  const descText = tr(locale, desc);
  const captionText = tr(locale, caption);
  const s = useStepper(steps.length);
  const step = steps[s.index];
  const byId = new Map(nodes.map((n) => [n.id, n]));
  const litNodes = new Set(step.nodes);
  const litEdges = new Set(step.edges ?? []);
  const seen = new Set(steps.slice(0, s.index).flatMap((st) => st.nodes));
  const t = transition(s.reduced);

  return (
    <figure className="not-prose my-8 rounded-3xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6" data-diagram={id}>
      <div className="hidden overflow-hidden rounded-2xl bg-[radial-gradient(circle_at_1px_1px,#e2e8f0_1px,transparent_0)] [background-size:22px_22px] md:block">
        <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-labelledby={`${id}-t ${id}-d`} className="h-auto w-full">
          <title id={`${id}-t`}>{titleText}</title>
          <desc id={`${id}-d`}>{descText}</desc>
          <defs>
            <marker id={`${id}-arrow`} viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M0 0 L10 5 L0 10 z" fill="#64748b" />
            </marker>
            <marker id={`${id}-arrow-on`} viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M0 0 L10 5 L0 10 z" fill="var(--accent-700)" />
            </marker>
          </defs>
          {edges.map((e, i) => {
            const a = byId.get(e.from);
            const b = byId.get(e.to);
            if (!a || !b) return null;
            const d = edgePath(a, b);
            const on = litEdges.has(i);
            return (
              <g key={`${e.from}-${e.to}-${i}`}>
                <path d={d} fill="none" stroke="#cbd5e1" strokeWidth={2} strokeDasharray={e.dashed ? "6 6" : undefined} markerEnd={`url(#${id}-arrow)`} />
                {on && (
                  <motion.path
                    key={`on-${s.index}`}
                    d={d}
                    fill="none"
                    stroke="var(--accent-700)"
                    strokeWidth={3}
                    markerEnd={`url(#${id}-arrow-on)`}
                    initial={{ pathLength: s.reduced ? 1 : 0 }}
                    animate={{ pathLength: 1 }}
                    transition={transition(s.reduced, 0.5)}
                  />
                )}
                {e.label && (
                  <text x={(a.x + b.x) / 2} y={(a.y + b.y) / 2 - 8} textAnchor="middle" fontSize={11} fill={on ? "#0f172a" : "#475569"} fontFamily="var(--font-mono)">
                    {e.label}
                  </text>
                )}
              </g>
            );
          })}
          {nodes.map((n) => {
            const k = KIND[n.kind ?? "check"];
            const w = n.w ?? 150;
            const on = litNodes.has(n.id);
            const done = seen.has(n.id);
            return (
              <motion.g
                key={n.id}
                onClick={() => {
                  const i = steps.findIndex((st) => st.nodes.includes(n.id));
                  if (i >= 0) s.jump(i);
                }}
                style={{ cursor: "pointer", transformBox: "fill-box", originX: 0.5, originY: 0.5 }}
                animate={{ scale: on && !s.reduced ? 1.06 : 1, opacity: on || done || s.index === 0 ? 1 : 0.55 }}
                transition={t}
              >
                <rect
                  x={n.x - w / 2}
                  y={n.y - H / 2}
                  width={w}
                  height={H}
                  rx={14}
                  fill={k.fill}
                  stroke={on ? "var(--accent-700)" : k.stroke}
                  strokeWidth={on ? 3 : 1.5}
                />
                <text x={n.x} y={n.sub ? n.y - 3 : n.y + 5} textAnchor="middle" fontSize={13} fontWeight={600} fill={k.ink}>
                  {n.label}
                </text>
                {n.sub && (
                  <text x={n.x} y={n.y + 14} textAnchor="middle" fontSize={10.5} fill="#475569" fontFamily="var(--font-mono)">
                    {n.sub}
                  </text>
                )}
              </motion.g>
            );
          })}
        </svg>
      </div>
      <div className="md:mt-5">
        <StepControls id={id} steps={steps} s={s} label={titleText} />
      </div>
      <figcaption className="mt-3 text-xs text-slate-600">{captionText}</figcaption>
    </figure>
  );
}
