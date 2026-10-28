import { src, workflow } from "@/lib/ai-sdlc-content";

/**
 * Who does what (#1970): one change across four lanes, Owner, Foreman agent, Worker agent
 * and CI, following the Constitution roles (§10) and the PR sequence (PR_SEQUENCE).
 * Time flows downward. Inline SVG with <title>/<desc>; an ordered list on narrow screens;
 * the flowing connectors stop when the reader prefers reduced motion.
 */
const LANES = [
  { key: "owner", label: "Owner", sub: "human", fill: "#fff1f2", stroke: "#fb7185", text: "#881337" },
  { key: "foreman", label: "Foreman", sub: "supervising agent", fill: "#f5f3ff", stroke: "#a78bfa", text: "#4c1d95" },
  { key: "worker", label: "Worker", sub: "implementing agent", fill: "#eef6ff", stroke: "#7fbcff", text: "#063a70" },
  { key: "ci", label: "CI", sub: "GitHub Actions", fill: "#fffbeb", stroke: "#f59e0b", text: "#78350f" },
] as const;

type Lane = (typeof LANES)[number]["key"];
type Step = { lane: Lane; title: string; detail: string; kind?: "loop" | "alt" };

const STEPS: Step[] = [
  { lane: "owner", title: "Issues & priorities", detail: "decides what matters" },
  { lane: "foreman", title: "Pick & brief", detail: "issue · use case · AC" },
  { lane: "worker", title: "Plan, then red tests", detail: "OpenSpec change, tests first" },
  { lane: "worker", title: "Code & open PR", detail: "[#N] type(scope) · Closes #N" },
  { lane: "ci", title: "Light + heavy CI", detail: "every required check" },
  { lane: "worker", title: "Fix from red logs", detail: "merge main in, never rebase", kind: "loop" },
  { lane: "ci", title: "All checks green", detail: "exit code, not opinion" },
  { lane: "foreman", title: "Gate 4 review", detail: "heavy CI · archive · board" },
  { lane: "foreman", title: "Merge Worker PR", detail: "Foreman merge rule" },
  { lane: "owner", title: "Merge protected PRs", detail: "Constitution · exception · harness", kind: "alt" },
  { lane: "ci", title: "CD · Pages · Release", detail: "on main" },
  { lane: "foreman", title: "Smoke & close", detail: "evidence, Issue closed" },
];

const LANE_W = 210;
const PAD = 20;
const HEAD = 64;
const ROW = 66;
const NODE_W = 178;
const NODE_H = 48;
const W = PAD * 2 + LANE_W * LANES.length;
const H = HEAD + ROW * STEPS.length + PAD;
const laneIndex = (l: Lane) => LANES.findIndex((x) => x.key === l);
const cx = (l: Lane) => PAD + laneIndex(l) * LANE_W + LANE_W / 2;
const cy = (i: number) => HEAD + i * ROW + ROW / 2;

function connector(a: number, b: number): string {
  if (STEPS[b].kind === "alt") {
    // Side route: leave the Foreman node sideways so the line never crosses the next Foreman step.
    const toLeft = cx(STEPS[b].lane) < cx(STEPS[a].lane);
    const xa = cx(STEPS[a].lane) + (toLeft ? -NODE_W / 2 : NODE_W / 2);
    const xb = cx(STEPS[b].lane);
    return `M${xa} ${cy(a)} L${xb + (toLeft ? 6 : -6)} ${cy(a)} Q${xb} ${cy(a)} ${xb} ${cy(a) + 6} L${xb} ${cy(b) - NODE_H / 2}`;
  }
  const x1 = cx(STEPS[a].lane);
  const x2 = cx(STEPS[b].lane);
  const y1 = cy(a) + NODE_H / 2;
  const y2 = cy(b) - NODE_H / 2;
  if (x1 === x2) return `M${x1} ${y1} L${x2} ${y2}`;
  const my = (y1 + y2) / 2;
  return `M${x1} ${y1} L${x1} ${my - 6} Q${x1} ${my} ${x1 + Math.sign(x2 - x1) * 6} ${my} L${x2 - Math.sign(x2 - x1) * 6} ${my} Q${x2} ${my} ${x2} ${my + 6} L${x2} ${y2}`;
}

export function Swimlane() {
  // The Owner's merge is the alternative to the Foreman's, so both flow on to CD.
  const edges: [number, number][] = STEPS.slice(1).map((_, i) => [i, i + 1]);
  const altFrom = STEPS.findIndex((s) => s.kind === "alt");
  edges.splice(altFrom - 1, 2, [altFrom - 2, altFrom], [altFrom - 1, altFrom + 1], [altFrom, altFrom + 1]);
  const loopAt = STEPS.findIndex((s) => s.kind === "loop");

  return (
    <figure className="my-10">
      <div className="hidden rounded-2xl border border-slate-200 bg-white p-4 shadow-[var(--shadow-card)] md:block md:p-6">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-labelledby="sl-title sl-desc" className="mx-auto h-auto w-full max-w-3xl">
          <title id="sl-title">One change across the Owner, the Foreman agent, a Worker agent and CI</title>
          <desc id="sl-desc">
            {STEPS.map((s, i) => `${i + 1}. ${LANES[laneIndex(s.lane)].label}: ${s.title} (${s.detail}).`).join(" ")}
          </desc>
          <defs>
            <marker id="sl-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M0 0L10 5L0 10z" fill="#64748b" />
            </marker>
          </defs>
          <style>{`
            .sl-flow{stroke-dasharray:5 7;animation:sl-dash 1.6s linear infinite}
            @keyframes sl-dash{to{stroke-dashoffset:-24}}
            @media (prefers-reduced-motion: reduce){.sl-flow{animation:none}}
          `}</style>
          {LANES.map((l, i) => (
            <g key={l.key}>
              <rect x={PAD + i * LANE_W + 4} y={PAD - 8} width={LANE_W - 8} height={H - PAD} rx={18} fill={l.fill} opacity={0.6} />
              <text x={PAD + i * LANE_W + LANE_W / 2} y={PAD + 16} textAnchor="middle" fontSize={16} fontWeight={700} fill={l.text}>
                {l.label}
              </text>
              <text x={PAD + i * LANE_W + LANE_W / 2} y={PAD + 34} textAnchor="middle" fontSize={11} fill={l.text}>
                {l.sub}
              </text>
            </g>
          ))}
          {edges.map(([a, b]) => (
            <path
              key={`${a}-${b}`}
              className={STEPS[b].kind === "alt" || STEPS[a].kind === "alt" ? undefined : "sl-flow"}
              d={connector(a, b)}
              fill="none"
              stroke="#64748b"
              strokeWidth={1.6}
              strokeDasharray={STEPS[b].kind === "alt" || STEPS[a].kind === "alt" ? "3 4" : undefined}
              markerEnd="url(#sl-arrow)"
            />
          ))}
          {loopAt > 0 && (
            <g>
              <path
                d={`M${cx("ci") + NODE_W / 2} ${cy(loopAt + 1)} C ${cx("ci") + NODE_W / 2 + 22} ${cy(loopAt + 1)}, ${cx("ci") + NODE_W / 2 + 22} ${cy(loopAt - 1)}, ${cx("ci") + NODE_W / 2} ${cy(loopAt - 1)}`}
                fill="none"
                stroke="#b45309"
                strokeWidth={1.4}
                strokeDasharray="4 4"
                markerEnd="url(#sl-arrow)"
              />
              <text x={cx("ci") + NODE_W / 2 + 4} y={cy(loopAt) + 4} fontSize={10} fill="#92400e" textAnchor="start">
                until
              </text>
              <text x={cx("ci") + NODE_W / 2 + 4} y={cy(loopAt) + 16} fontSize={10} fill="#92400e" textAnchor="start">
                green
              </text>
            </g>
          )}
          {STEPS.map((s, i) => {
            const lane = LANES[laneIndex(s.lane)];
            const x = cx(s.lane) - NODE_W / 2;
            const y = cy(i) - NODE_H / 2;
            return (
              <g key={s.title}>
                <rect x={x} y={y} width={NODE_W} height={NODE_H} rx={12} fill="#ffffff" stroke={lane.stroke} strokeWidth={1.6} strokeDasharray={s.kind === "alt" ? "4 3" : undefined} />
                <circle cx={x + 2} cy={y + 2} r={10} fill={lane.text} />
                <text x={x + 2} y={y + 6} textAnchor="middle" fontSize={10} fontWeight={700} fill="#ffffff">
                  {i + 1}
                </text>
                <text x={x + NODE_W / 2} y={y + 20} textAnchor="middle" fontSize={13} fontWeight={600} fill="#0f172a">
                  {s.title}
                </text>
                <text x={x + NODE_W / 2} y={y + 36} textAnchor="middle" fontSize={10.5} fill="#475569">
                  {s.detail}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
      <ol className="space-y-2 md:hidden" aria-label="One change, step by step">
        {STEPS.map((s, i) => {
          const lane = LANES[laneIndex(s.lane)];
          return (
            <li key={s.title} className="flex items-start gap-3 rounded-xl border bg-white px-3 py-2" style={{ borderColor: lane.stroke }}>
              <span className="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[11px] font-bold text-white" style={{ background: lane.text }}>
                {i + 1}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block text-sm font-semibold text-slate-900">{s.title}</span>
                <span className="block text-xs text-slate-600">{s.detail}</span>
              </span>
              <span className="shrink-0 rounded-full px-2 py-0.5 text-[11px] font-semibold" style={{ background: lane.fill, color: lane.text }}>
                {lane.label}
              </span>
            </li>
          );
        })}
      </ol>
      <figcaption className="mt-3 text-center text-sm text-slate-600">
        The Worker never merges; the Foreman merges only on green heavy CI; the Owner alone merges Constitution,
        exception and harness changes. Sources:{" "}
        <a className="underline" href={src("CONSTITUTION.md")}>
          CONSTITUTION.md §10 Roles
        </a>
        ,{" "}
        <a className="underline" href={workflow("ci.yml")}>
          ci.yml
        </a>
        .
      </figcaption>
    </figure>
  );
}
