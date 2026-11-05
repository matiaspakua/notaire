"use client";

import { motion } from "framer-motion";
import { StepControls, type Step } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type TestFacts = { backend: number; frontend: number; e2e: number; bruno: number };

const W = 760;
const H = 430;
const TOP = 30;
const BASE = 400;
const HALF_BASE = 340;

/**
 * Notaire's test layers, cheapest at the bottom (#1977). Widths show how wide and fast each layer
 * is, not how many tests it has; the counts sit on each band and come from the repo at build time.
 */
export function TestPyramid({ facts, sources }: { facts: TestFacts; sources: Record<"backend" | "frontend" | "bruno" | "e2e" | "arch", string> }) {
  const layers: (Step & { label: string; count: string; fill: string; ink: string })[] = [
    {
      label: "Static checks",
      count: "every push",
      fill: "#e2e8f0",
      ink: "#0f172a",
      title: "Static checks catch the shape",
      body: "TypeScript, ESLint, the ArchUnit hexagon rules and CodeQL run before anything starts a server. They are the cheapest feedback and fail fastest.",
      source: { label: "HexagonArchitectureTest.java", href: sources.arch },
    },
    {
      label: "Backend JUnit",
      count: `${facts.backend} test classes`,
      fill: "#d1fae5",
      ink: "#064e3b",
      title: "Backend unit and slice tests",
      body: `${facts.backend} JUnit test classes cover domain rules, use cases with mocked ports, and adapters on H2 or, for PostgreSQL-specific behaviour, Testcontainers. preflight.sh runs them before a pull request. ci.yml runs them again on a version tag.`,
      source: { label: "backend-api/src/test", href: sources.backend },
    },
    {
      label: "Frontend Vitest",
      count: `${facts.frontend} test files`,
      fill: "#dbeafe",
      ink: "#1e3a8a",
      title: "Frontend component and core tests",
      body: `${facts.frontend} Vitest files test the core/ domain logic, the HTTP adapters and React components with Testing Library. frontend-ci.yml runs them.`,
      source: { label: "frontend/src", href: sources.frontend },
    },
    {
      label: "Bruno API",
      count: `${facts.bruno} requests`,
      fill: "#ede9fe",
      ink: "#4c1d95",
      title: "API tests against a real backend",
      body: `${facts.bruno} Bruno requests call the running API with real JWTs and assert status, body and side effects. playwright-e2e.yml runs them with PostgreSQL and the backend up.`,
      source: { label: "backend-api/api-test", href: sources.bruno },
    },
    {
      label: "Playwright E2E",
      count: `${facts.e2e} specs`,
      fill: "#fee2e2",
      ink: "#7f1d1d",
      title: "End-to-end journeys in a browser",
      body: `${facts.e2e} Playwright specs drive the real UI over the full stack: login, the notarial workflows and accessibility checks. They are the slowest layer, so they cover journeys, not every rule.`,
      source: { label: "testing/e2e", href: sources.e2e },
    },
  ];
  const s = useStepper(layers.length);
  const t = transition(s.reduced);
  const band = (BASE - TOP) / layers.length;
  const halfAt = (y: number) => (HALF_BASE * (y - TOP + 30)) / (BASE - TOP + 30);

  return (
    <figure className="not-prose my-8 rounded-3xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6" data-diagram="test-pyramid">
      <div className="hidden md:block">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-labelledby="tp-t tp-d" className="mx-auto h-auto w-full max-w-3xl">
          <title id="tp-t">Notaire test pyramid</title>
          <desc id="tp-d">Five layers from static checks at the base to Playwright end-to-end tests at the top, with the count of each.</desc>
          {layers.map((l, i) => {
            const yb = BASE - i * band;
            const yt = yb - band + 4;
            const hb = halfAt(yb);
            const ht = halfAt(yt);
            const cx = W / 2;
            const on = i === s.index;
            return (
              <motion.g
                key={l.label}
                onClick={() => s.jump(i)}
                style={{ cursor: "pointer", transformBox: "fill-box", originX: 0.5, originY: 0.5 }}
                initial={false}
                animate={{ opacity: on || i < s.index ? 1 : 0.5, scale: on && !s.reduced ? 1.03 : 1 }}
                transition={t}
              >
                <polygon
                  points={`${cx - hb},${yb} ${cx + hb},${yb} ${cx + ht},${yt} ${cx - ht},${yt}`}
                  fill={l.fill}
                  stroke={on ? "var(--accent-700)" : "#ffffff"}
                  strokeWidth={on ? 3 : 2}
                />
                <text x={cx} y={(yb + yt) / 2 - 2} textAnchor="middle" fontSize={15} fontWeight={700} fill={l.ink}>
                  {l.label}
                </text>
                <text x={cx} y={(yb + yt) / 2 + 16} textAnchor="middle" fontSize={12} fill={l.ink} fontFamily="var(--font-mono)">
                  {l.count}
                </text>
              </motion.g>
            );
          })}
          <text x={18} y={BASE} fontSize={11.5} fill="#475569">fast, cheap</text>
          <text x={18} y={TOP + 20} fontSize={11.5} fill="#475569">slow, realistic</text>
          <line x1={40} y1={BASE - 16} x2={40} y2={TOP + 34} stroke="#94a3b8" strokeWidth={1.5} markerEnd="url(#tp-arrow)" />
          <defs>
            <marker id="tp-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M0 0 L10 5 L0 10 z" fill="#94a3b8" />
            </marker>
          </defs>
        </svg>
      </div>
      <div className="md:mt-5">
        <StepControls id="test-pyramid" steps={layers} s={s} label="Test pyramid" />
      </div>
      <figcaption className="mt-3 text-xs text-slate-600">
        Band width shows speed and scope, not the number of tests. Counts are read from the repo when the site is built.
      </figcaption>
    </figure>
  );
}
