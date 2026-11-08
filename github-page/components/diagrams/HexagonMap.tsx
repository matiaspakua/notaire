"use client";

import { motion } from "framer-motion";
import { StepControls, type Step } from "./StepControls";
import { transition, useStepper } from "./useStepper";

export type HexFacts = {
  domainContexts: number;
  useCaseContexts: number;
  inPorts: number;
  outPorts: number;
  useCases: number;
  webAdapters: number;
  outAdapters: number;
  baselineViolations: number;
};

type Region = "domain" | "usecases" | "inports" | "outports" | "inadapters" | "outadapters" | "outside" | "rules";

const W = 920;
const HH = 520;
const CX = W / 2;
const CY = HH / 2;

function hex(r: number, sx = 1.25): string {
  return Array.from({ length: 6 }, (_, k) => {
    const a = (Math.PI / 180) * (60 * k);
    return `${(CX + r * sx * Math.cos(a)).toFixed(1)},${(CY + r * Math.sin(a)).toFixed(1)}`;
  }).join(" ");
}

/**
 * The backend hexagon (#1977): domain in the middle, use cases and ports around it, adapters on the
 * outside, and the world beyond. Counts are passed in from lib/tech-facts.ts at build time.
 */
export function HexagonMap({ facts, sources }: { facts: HexFacts; sources: Record<"domain" | "port" | "usecase" | "adapter" | "rules", string> }) {
  const steps: (Step & { regions: Region[] })[] = [
    {
      title: "The domain is the centre",
      body: `${facts.domainContexts} bounded contexts keep their entities and value objects in domain/ — plain Java with no Spring, JPA or HTTP. Money, BudgetCharges and PaymentStatus in domain/payment are examples.`,
      regions: ["domain"],
      source: { label: "domain/", href: sources.domain },
    },
    {
      title: "Use cases orchestrate",
      body: `${facts.useCases} files in application/usecase, across ${facts.useCaseContexts} contexts. Each service implements an in-port and reaches the outside only through out-ports.`,
      regions: ["usecases"],
      source: { label: "application/usecase/", href: sources.usecase },
    },
    {
      title: "In-ports: what the app can do",
      body: `${facts.inPorts} files in application/port/in: use-case interfaces and their commands (for example ProcessPaymentUseCase). Controllers depend on these, never on a service class.`,
      regions: ["inports"],
      source: { label: "application/port/in/", href: sources.port },
    },
    {
      title: "Out-ports: what the app needs",
      body: `${facts.outPorts} files in application/port/out: interfaces and the records they carry (for example PaymentRepositoryPort, BudgetLookupPort). The use case states the need; an adapter decides how.`,
      regions: ["outports"],
      source: { label: "application/port/out/", href: sources.port },
    },
    {
      title: "Driving adapters call in",
      body: `${facts.webAdapters} web adapter files: REST controllers and their mappers translate HTTP into commands and back.`,
      regions: ["inadapters", "outside"],
      source: { label: "adapter/in/", href: sources.adapter },
    },
    {
      title: "Driven adapters reach out",
      body: `${facts.outAdapters} outbound adapter files implement the out-ports: JPA persistence per context, plus PDFBox report rendering.`,
      regions: ["outadapters", "outside"],
      source: { label: "adapter/out/", href: sources.adapter },
    },
    {
      title: "Rules hold the shape",
      body: `Two ArchUnit tests check seven rules in every backend build: R1–R6 on dependencies, R7 on use-case cycles. The dependency baseline has ${facts.baselineViolations} entries, so any new arrow pointing the wrong way fails the build.`,
      regions: ["rules", "domain", "usecases", "inports", "outports", "inadapters", "outadapters"],
      source: { label: "HexagonArchitectureTest.java", href: sources.rules },
    },
  ];
  const s = useStepper(steps.length);
  const lit = new Set(steps[s.index].regions);
  const t = transition(s.reduced);
  const on = (r: Region) => lit.has(r);
  const pick = (r: Region) => {
    const i = steps.findIndex((st) => st.regions[0] === r);
    if (i >= 0) s.jump(i);
  };
  const glow = (r: Region) => ({ opacity: on(r) ? 1 : 0 });

  return (
    <figure className="not-prose my-8 rounded-3xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] sm:p-6" data-diagram="hexagon">
      <div className="hidden md:block">
        <svg viewBox={`0 0 ${W} ${HH}`} role="img" aria-labelledby="hex-t hex-d" className="h-auto w-full">
          <title id="hex-t">The Notaire backend as a hexagon</title>
          <desc id="hex-d">
            Domain in the centre, use cases and ports around it, driving adapters on the left and driven adapters on the
            right, with the browser and the database outside.
          </desc>
          <defs>
            <clipPath id="hex-left">
              <rect x={0} y={0} width={CX} height={HH} />
            </clipPath>
            <clipPath id="hex-right">
              <rect x={CX} y={0} width={CX} height={HH} />
            </clipPath>
          </defs>

          {/* outer ring: adapters */}
          <polygon points={hex(205)} fill="#eff6ff" stroke="#93c5fd" strokeWidth={1.5} />
          <motion.polygon points={hex(205)} clipPath="url(#hex-left)" fill="#bfdbfe" initial={false} animate={glow("inadapters")} transition={t} />
          <motion.polygon points={hex(205)} clipPath="url(#hex-right)" fill="#bfdbfe" initial={false} animate={glow("outadapters")} transition={t} />
          <line x1={CX} y1={CY - 205} x2={CX} y2={CY - 140} stroke="#93c5fd" strokeDasharray="4 4" />
          <line x1={CX} y1={CY + 140} x2={CX} y2={CY + 205} stroke="#93c5fd" strokeDasharray="4 4" />

          {/* middle ring: application */}
          <polygon points={hex(140)} fill="#f5f3ff" stroke="#c4b5fd" strokeWidth={1.5} />
          <motion.polygon points={hex(140)} fill="#ddd6fe" initial={false} animate={glow("usecases")} transition={t} />

          {/* core: domain */}
          <polygon points={hex(70)} fill="#ecfdf5" stroke="#6ee7b7" strokeWidth={1.5} />
          <motion.polygon points={hex(70)} fill="#a7f3d0" initial={false} animate={glow("domain")} transition={t} />

          <motion.polygon
            points={hex(212)}
            fill="none"
            stroke="var(--accent-700)"
            strokeWidth={3}
            strokeDasharray="10 8"
            initial={false}
            animate={{ opacity: on("rules") ? 1 : 0 }}
            transition={t}
          />

          <g onClick={() => pick("domain")} style={{ cursor: "pointer" }}>
            <text x={CX} y={CY - 6} textAnchor="middle" fontSize={16} fontWeight={700} fill="#064e3b">Domain</text>
            <text x={CX} y={CY + 14} textAnchor="middle" fontSize={12} fill="#065f46" fontFamily="var(--font-mono)">{facts.domainContexts} contexts</text>
          </g>
          <g onClick={() => pick("usecases")} style={{ cursor: "pointer" }}>
            <text x={CX} y={CY - 98} textAnchor="middle" fontSize={14} fontWeight={700} fill="#4c1d95">Use cases</text>
            <text x={CX} y={CY - 80} textAnchor="middle" fontSize={12} fill="#5b21b6" fontFamily="var(--font-mono)">{facts.useCases} files</text>
          </g>

          {/* ports as chips on the application ring */}
          {(["inports", "outports"] as const).map((r) => {
            const left = r === "inports";
            const x = left ? CX - 150 : CX + 150;
            return (
              <motion.g key={r} onClick={() => pick(r)} style={{ cursor: "pointer" }} initial={false} animate={{ scale: on(r) && !s.reduced ? 1.08 : 1 }} transition={t}>
                <rect x={x - 58} y={CY - 22} width={116} height={44} rx={22} fill={on(r) ? "var(--accent-700)" : "#ffffff"} stroke="#7c3aed" strokeWidth={1.5} />
                <text x={x} y={CY - 3} textAnchor="middle" fontSize={12.5} fontWeight={700} fill={on(r) ? "#ffffff" : "#4c1d95"}>
                  {left ? "In-ports" : "Out-ports"}
                </text>
                <text x={x} y={CY + 13} textAnchor="middle" fontSize={11} fill={on(r) ? "#ffffff" : "#5b21b6"} fontFamily="var(--font-mono)">
                  {left ? facts.inPorts : facts.outPorts}
                </text>
              </motion.g>
            );
          })}

          <g onClick={() => pick("inadapters")} style={{ cursor: "pointer" }}>
            <text x={CX - 232} y={CY - 112} textAnchor="middle" fontSize={14} fontWeight={700} fill="#1e3a8a">Driving adapters</text>
            <text x={CX - 232} y={CY - 94} textAnchor="middle" fontSize={11.5} fill="#1e40af" fontFamily="var(--font-mono)">adapter/in · {facts.webAdapters}</text>
            <text x={CX - 232} y={CY + 104} textAnchor="middle" fontSize={11.5} fill="#1e40af">REST controllers, mappers</text>
          </g>
          <g onClick={() => pick("outadapters")} style={{ cursor: "pointer" }}>
            <text x={CX + 232} y={CY - 112} textAnchor="middle" fontSize={14} fontWeight={700} fill="#1e3a8a">Driven adapters</text>
            <text x={CX + 232} y={CY - 94} textAnchor="middle" fontSize={11.5} fill="#1e40af" fontFamily="var(--font-mono)">adapter/out · {facts.outAdapters}</text>
            <text x={CX + 232} y={CY + 104} textAnchor="middle" fontSize={11.5} fill="#1e40af">JPA persistence, PDFBox reports</text>
          </g>

          {/* outside world */}
          <motion.g initial={false} animate={{ opacity: on("outside") || s.index === 0 ? 1 : 0.6 }} transition={t}>
            <rect x={14} y={CY - 26} width={104} height={52} rx={12} fill="#f8fafc" stroke="#94a3b8" />
            <text x={66} y={CY - 3} textAnchor="middle" fontSize={12.5} fontWeight={600} fill="#0f172a">Next.js app</text>
            <text x={66} y={CY + 14} textAnchor="middle" fontSize={10.5} fill="#475569" fontFamily="var(--font-mono)">HTTP + JWT</text>
            <path d={`M118 ${CY} L ${CX - 262} ${CY}`} stroke="#64748b" strokeWidth={2} markerEnd="url(#hex-arrow)" />
            <rect x={W - 118} y={CY - 26} width={104} height={52} rx={12} fill="#fffbeb" stroke="#f59e0b" />
            <text x={W - 66} y={CY - 3} textAnchor="middle" fontSize={12.5} fontWeight={600} fill="#78350f">PostgreSQL</text>
            <text x={W - 66} y={CY + 14} textAnchor="middle" fontSize={10.5} fill="#92400e" fontFamily="var(--font-mono)">Flyway schema</text>
            <path d={`M${CX + 262} ${CY} L ${W - 122} ${CY}`} stroke="#64748b" strokeWidth={2} markerEnd="url(#hex-arrow)" />
          </motion.g>
          <defs>
            <marker id="hex-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M0 0 L10 5 L0 10 z" fill="#64748b" />
            </marker>
          </defs>
          <text x={CX} y={HH - 14} textAnchor="middle" fontSize={11.5} fill="#475569">
            Every dependency points inwards. Click a ring or use the steps below.
          </text>
        </svg>
      </div>
      <div className="md:mt-5">
        <StepControls id="hexagon" steps={steps} s={s} label="The backend hexagon" />
      </div>
      <figcaption className="mt-3 text-xs text-slate-600">Counts are read from the source tree when the site is built.</figcaption>
    </figure>
  );
}
