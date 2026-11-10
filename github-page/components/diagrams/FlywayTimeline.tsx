"use client";

import { motion } from "framer-motion";
import { useState } from "react";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "@/components/site/Preferences";
import { StepControls } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type TimelineMigration = { version: number; name: string };
export type TimelinePhase = { from: number; to: number; title: string; body: string };

const W = 920;
const H = 250;
const X0 = 40;
const X1 = W - 40;
const LINE = 140;
const PHASE_FILL = ["#e0f2fe", "#ede9fe", "#fef3c7", "#dcfce7", "#fee2e2"];
const PHASE_INK = ["#075985", "#5b21b6", "#92400e", "#166534", "#991b1b"];

/**
 * Every versioned migration on one line, grouped into the phases the schema went through (#1977).
 * The migrations are listed from db/migration when the site is built; nothing here is typed by hand.
 */
export function FlywayTimeline({ migrations, phases, sourceHref }: { migrations: TimelineMigration[]; phases: TimelinePhase[]; sourceHref: string }) {
  const { locale } = usePreferences();
  const text = (value: string) => tr(locale, value);
  const used = phases
    .map((p) => ({ ...p, items: migrations.filter((m) => m.version >= p.from && m.version <= p.to) }))
    .filter((p) => p.items.length > 0);
  const steps = used.map((p) => ({
    title: `${text(p.title)} · V${p.items[0].version}–V${p.items.at(-1)?.version}`,
    body: (
      <>
        {p.body}{" "}
        <span className="font-mono text-xs text-slate-600">
          {p.items.length} migration{p.items.length === 1 ? "" : "s"}, latest: V{p.items.at(-1)?.version} {p.items.at(-1)?.name}.
        </span>
      </>
    ),
    source: { label: "db/migration", href: sourceHref },
  }));
  const s = useStepper(steps.length);
  const t = transition(s.reduced, 0.25);
  const [hover, setHover] = useState<TimelineMigration | null>(null);
  const n = migrations.length;
  const x = (i: number) => (n <= 1 ? X0 : X0 + ((X1 - X0) * i) / (n - 1));
  const idx = new Map(migrations.map((m, i) => [m.version, i]));
  const phase = used[s.index];
  const focus = hover ?? phase?.items.at(-1) ?? null;

  if (n === 0) return null;
  return (
    <figure className="not-prose my-8 rounded-3xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6" data-diagram="flyway-timeline">
      <div className="hidden md:block">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-labelledby="fw-t fw-d" className="h-auto w-full" onMouseLeave={() => setHover(null)}>
          <title id="fw-t">{text("Flyway migration timeline")}</title>
          <desc id="fw-d">
            {n} versioned migrations from V{migrations[0].version} to V{migrations.at(-1)?.version}, grouped into {used.length} phases.
          </desc>
          {used.map((p, pi) => {
            const a = x(idx.get(p.items[0].version) ?? 0) - 7;
            const b = x(idx.get(p.items.at(-1)?.version ?? 0) ?? 0) + 7;
            const on = pi === s.index;
            return (
              <g key={p.title} onClick={() => s.jump(pi)} style={{ cursor: "pointer" }}>
                <motion.rect
                  x={a}
                  y={52}
                  width={b - a}
                  height={110}
                  rx={12}
                  fill={PHASE_FILL[pi % PHASE_FILL.length]}
                  initial={false}
                  animate={{ opacity: on ? 1 : 0.35 }}
                  transition={t}
                />
                <text x={(a + b) / 2} y={76} textAnchor="middle" fontSize={12.5} fontWeight={700} fill={PHASE_INK[pi % PHASE_INK.length]}>
                  {b - a > 90 ? text(p.title) : text(p.title).split(" ")[0]}
                </text>
                <text x={(a + b) / 2} y={92} textAnchor="middle" fontSize={10.5} fill={PHASE_INK[pi % PHASE_INK.length]} fontFamily="var(--font-mono)">
                  V{p.items[0].version}–V{p.items.at(-1)?.version}
                </text>
              </g>
            );
          })}
          <line x1={X0 - 10} y1={LINE} x2={X1 + 10} y2={LINE} stroke="#94a3b8" strokeWidth={2} />
          {migrations.map((m, i) => {
            const inPhase = phase ? m.version >= phase.from && m.version <= phase.to : false;
            const isFocus = focus?.version === m.version;
            return (
              <motion.circle
                key={m.version}
                cx={x(i)}
                cy={LINE}
                r={isFocus ? 7 : 4.5}
                fill={inPhase ? "var(--accent-700)" : "#ffffff"}
                stroke={inPhase ? "var(--accent-700)" : "#64748b"}
                strokeWidth={1.5}
                initial={false}
                animate={{ scale: isFocus && !s.reduced ? 1.15 : 1 }}
                transition={t}
                style={{ cursor: "pointer", transformBox: "fill-box", originX: 0.5, originY: 0.5 }}
                onMouseEnter={() => setHover(m)}
                onClick={() => {
                  const pi = used.findIndex((p) => m.version >= p.from && m.version <= p.to);
                  if (pi >= 0) s.jump(pi);
                  setHover(m);
                }}
              />
            );
          })}
          {[0, n - 1].map((i) => (
            <text key={i} x={x(i)} y={LINE + 24} textAnchor="middle" fontSize={11} fill="#475569" fontFamily="var(--font-mono)">
              V{migrations[i].version}
            </text>
          ))}
          {focus && (
            <g>
              <line x1={x(idx.get(focus.version) ?? 0)} y1={LINE + 8} x2={x(idx.get(focus.version) ?? 0)} y2={LINE + 40} stroke="var(--accent-700)" strokeDasharray="3 3" />
              <text
                x={Math.min(Math.max(x(idx.get(focus.version) ?? 0), 160), W - 160)}
                y={LINE + 60}
                textAnchor="middle"
                fontSize={13}
                fill="#0f172a"
              >
                <tspan fontFamily="var(--font-mono)" fontWeight={700}>V{focus.version}</tspan> {focus.name}
              </text>
            </g>
          )}
          <text x={W / 2} y={30} textAnchor="middle" fontSize={11.5} fill="#475569">
            {text("Hover a dot to read a migration; click a phase to step to it.")}
          </text>
        </svg>
      </div>
      <div className="md:mt-5">
        <StepControls id="flyway-timeline" steps={steps} s={s} label="Flyway migration timeline" />
      </div>
      <figcaption className="mt-3 text-xs text-slate-600">{text("One dot per V<n>__*.sql file, read from the migration folder at build time.")}</figcaption>
    </figure>
  );
}
