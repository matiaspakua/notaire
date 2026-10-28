/**
 * Animated gate flow (#1958): a change token travels through the seven stages; each gate turns
 * green only when it is reached. Pure SVG + CSS, rendered at build time. With prefers-reduced-motion
 * the animation is off and every gate is shown passed; on narrow screens an ordered list replaces it.
 */
const STAGES = [
  { gate: "G0", name: "Intake", proof: "issue + requirement" },
  { gate: "G1", name: "Plan", proof: "change record, real issue" },
  { gate: "G2", name: "Red", proof: "new tests fail" },
  { gate: "G3", name: "Verify", proof: "suite · lint · ratchets" },
  { gate: "G3", name: "Pre-PR", proof: "pipeline · docs" },
  { gate: "G4", name: "Merge", proof: "heavy CI · review" },
  { gate: "G5", name: "Release", proof: "deploy · smoke" },
];

const W = 980;
const X0 = 86;
const STEP = (W - 2 * X0) / (STAGES.length - 1);
const Y = 70;
const CYCLE = 16;
const TRAVEL = 1.6;
const xs = STAGES.map((_, i) => X0 + i * STEP);
const arrive = STAGES.map((_, i) => 0.4 + i * TRAVEL);
const pct = (t: number) => `${((t / CYCLE) * 100).toFixed(2)}%`;

function keyframes(): string {
  const travel = xs
    .map((x, i) => `${pct(arrive[i])}{transform:translateX(${(x - X0).toFixed(1)}px)}${pct(arrive[i] + 0.5)}{transform:translateX(${(x - X0).toFixed(1)}px)}`)
    .join("");
  const gates = STAGES.map(
    (_, i) =>
      `.gf-g${i}{animation:gf-lit${i} ${CYCLE}s step-end infinite}@keyframes gf-lit${i}{0%{fill:#e2e8f0}${pct(arrive[i] + 0.25)}{fill:#16a34a}97%{fill:#e2e8f0}}` +
      `.gf-c${i}{opacity:0;animation:gf-chk${i} ${CYCLE}s step-end infinite}@keyframes gf-chk${i}{0%{opacity:0}${pct(arrive[i] + 0.25)}{opacity:1}97%{opacity:0}}` +
      `.gf-t${i}{animation:gf-txt${i} ${CYCLE}s step-end infinite}@keyframes gf-txt${i}{0%{fill:#0f172a}${pct(arrive[i] + 0.25)}{fill:#ffffff}97%{fill:#0f172a}}`,
  ).join("");
  const reduced = STAGES.map((_, i) => `.gf-g${i}{animation:none;fill:#16a34a}.gf-c${i}{animation:none;opacity:1}.gf-t${i}{animation:none;fill:#ffffff}`).join("");
  return (
    `.gf-tok{animation:gf-travel ${CYCLE}s linear infinite}@keyframes gf-travel{0%{transform:translateX(0)}${travel}100%{transform:translateX(${(xs[xs.length - 1] - X0).toFixed(1)}px)}}` +
    gates +
    `@media (prefers-reduced-motion: reduce){.gf-tok{animation:none;transform:translateX(${(xs[xs.length - 1] - X0).toFixed(1)}px)}${reduced}}`
  );
}

export function GateFlow() {
  return (
    <figure className="my-8">
      <div className="hidden sm:block rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
        <svg viewBox={`0 0 ${W} 236`} role="img" aria-labelledby="gf-title gf-desc" className="w-full h-auto">
          <title id="gf-title">A change moving through the seven stages and five hard gates</title>
          <desc id="gf-desc">
            A change token travels from Intake (G0) through Plan (G1), Red (G2), Verify and Pre-PR (G3), Merge (G4) to
            Release (G5). Each gate turns green only when its command exits as expected. Release feeds incidents back to
            Intake.
          </desc>
          <style>{keyframes()}</style>
          <line x1={xs[0]} y1={Y} x2={xs[xs.length - 1]} y2={Y} stroke="#cbd5e1" strokeWidth={6} strokeLinecap="round" />
          {STAGES.map((s, i) => (
            <g key={s.name}>
              <circle className={`gf-g${i}`} cx={xs[i]} cy={Y} r={28} fill="#e2e8f0" stroke="#94a3b8" strokeWidth={2} />
              <text className={`gf-t${i}`} x={xs[i]} y={Y + 5} textAnchor="middle" fontSize={13} fontWeight={700} fill="#0f172a">
                {s.gate}
              </text>
              <path className={`gf-c${i}`} d={`M${xs[i] + 15} ${Y - 31} l5 5 l9 -11`} fill="none" stroke="#16a34a" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" />
              <text x={xs[i]} y={Y + 56} textAnchor="middle" fontSize={15} fontWeight={600} fill="#0f172a">
                {s.name}
              </text>
              <text x={xs[i]} y={Y + 76} textAnchor="middle" fontSize={12} fill="#475569">
                {s.proof}
              </text>
            </g>
          ))}
          <circle className="gf-tok" cx={X0} cy={Y} r={10} fill="#f59e0b" stroke="#78350f" strokeWidth={2} />
          <path
            d={`M${xs[xs.length - 1]} ${Y + 92} C ${xs[xs.length - 1]} 200, ${xs[0]} 200, ${xs[0]} ${Y + 92}`}
            fill="none"
            stroke="#b45309"
            strokeWidth={2}
            strokeDasharray="6 6"
          />
          <text x={W / 2} y={226} textAnchor="middle" fontSize={12} fill="#92400e">
            incidents, review notes and metrics become new issues — then new gates
          </text>
        </svg>
      </div>
      <ol className="sm:hidden space-y-2" aria-label="Stages and gates">
        {STAGES.map((s) => (
          <li key={s.name} className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white px-3 py-2">
            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-emerald-600 text-xs font-bold text-white">{s.gate}</span>
            <span>
              <span className="block text-sm font-semibold text-slate-900">{s.name}</span>
              <span className="block text-xs text-slate-600">{s.proof}</span>
            </span>
          </li>
        ))}
      </ol>
      <figcaption className="mt-2 text-xs text-slate-500 text-center">
        Machines check machines: a gate opens only on the expected exit code. Motion is off when your system asks for
        reduced motion.
      </figcaption>
    </figure>
  );
}
