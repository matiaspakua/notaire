"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { PageHeader, SectionTitle, Card, Code, Diagram, Sources } from "@/components/FactoryUI";
import { src } from "@/lib/ai-sdlc-content";
import {
  STATIONS,
  AGENTS_ON_LINE,
  INTEGRATION_POINTS,
  EXAMPLE_EVENTS,
  FACTORY_LINE_DIAGRAM,
  AGENT_FLOW_DIAGRAM,
  PHASE_COLORS,
  STATION_ICONS,
} from "@/lib/factory-line-content";
import type { Station } from "@/lib/factory-line-content";

/* ───────────────────── Intent Panel (always visible) ───────────────────── */

function IntentPanel({ issueId, onClose }: { issueId: string; onClose: () => void }) {
  const [intent, setIntent] = useState(issueId);

  return (
    <div className="sticky top-0 z-10 border-b border-slate-200 bg-white/95 backdrop-blur-sm">
      <div className="flex items-center gap-3 px-4 py-3">
        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-slate-900 text-sm font-bold text-white">
          #
        </span>
        <div className="flex-1">
          <label className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">
            Intent — the goal this factory produces
          </label>
          <div className="flex items-center gap-2">
            <input
              type="text"
              value={intent}
              onChange={(e) => setIntent(e.target.value)}
              className="w-full border-0 border-b border-slate-200 bg-transparent py-1 text-sm font-medium text-slate-900 placeholder-slate-400 focus:border-[var(--accent-500)] focus:outline-none"
              placeholder="e.g. #2380 — CLI comparison experiment"
            />
            <a
              href={`https://github.com/matiaspakua/notaire/issues/${intent.replace(/^#/, "")}`}
              target="_blank"
              rel="noopener noreferrer"
              className="shrink-0 rounded-md border border-slate-200 px-2 py-1 text-[10px] font-medium text-slate-600 hover:bg-slate-50"
              title="Open issue on GitHub"
            >
              ↗ GitHub
            </a>
          </div>
        </div>
        <button
          onClick={onClose}
          className="shrink-0 rounded-md px-2 py-1 text-xs text-slate-500 hover:bg-slate-100"
          aria-label="Hide intent panel"
        >
          ✕
        </button>
      </div>
    </div>
  );
}

/* ───────────────────── Production Line (horizontal conveyor) ───────────────────── */

function ProductionLine({
  selected,
  onSelect,
}: {
  selected: string | null;
  onSelect: (id: string) => void;
}) {
  const phases = ["Intake", "Plan", "Prove", "Publish", "Ship"];
  const stationsByPhase = phases.map((p) => ({
    phase: p,
    stations: STATIONS.filter((s) => s.phase === p),
  }));

  return (
    <div className="conveyor-flow overflow-x-auto pb-4" tabIndex={0} role="region" aria-label="Production line">
      <div className="flex gap-6 min-w-[900px] px-2">
        {stationsByPhase.map(({ phase, stations }) => {
          const colors = PHASE_COLORS[phase];
          return (
            <div key={phase} className="flex-1 min-w-[160px]">
              <div
                className={`mb-3 rounded-lg border px-3 py-1.5 text-center text-xs font-semibold uppercase tracking-wider ${colors.bg} ${colors.border} ${colors.text}`}
              >
                {phase}
              </div>
              <div className="flex flex-col gap-3">
                {stations.map((station, i) => {
                  const isSelected = selected === station.id;
                  const hasGate = !!station.gate;
                  const hasHuman = !!station.human;
                  return (
                    <button
                      key={station.id}
                      onClick={() => onSelect(station.id)}
                      className={`group relative text-left rounded-xl border-2 p-3 transition-all duration-300 ease-out hover:shadow-md focus:outline-none focus:ring-2 focus:ring-[var(--accent-500)] focus:ring-offset-2 ${
                        isSelected
                          ? `${colors.border} ${colors.bg} shadow-md scale-[1.02]`
                          : "border-slate-200 bg-white hover:border-slate-300 hover:scale-[1.01]"
                      }`}
                      aria-pressed={isSelected}
                      aria-label={`${station.title}${hasGate ? `, Gate ${station.gate!.number}` : ""}${hasHuman ? ", human gate" : ""}`}
                    >
                      {/* Gate indicator */}
                      {hasGate && station.gate && (
                        <span className="gate-pulse absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-amber-500 text-[10px] font-bold text-white shadow-sm">
                          G{station.gate.number}
                        </span>
                      )}
                      {/* Human indicator */}
                      {hasHuman && (
                        <span className="absolute -left-1 top-1/2 -translate-y-1/2 text-lg" title="Human gate">
                          👤
                        </span>
                      )}
                      {/* Station icon */}
                      <span className="text-lg">{STATION_ICONS[station.kind]}</span>
                      {/* Title */}
                      <h3 className={`mt-1 text-sm font-semibold leading-tight ${isSelected ? colors.text : "text-slate-900"}`}>
                        {station.title}
                      </h3>
                      {/* Agent */}
                      {station.agent && (
                        <p className="mt-0.5 text-[11px] text-slate-500">
                          {AGENTS_ON_LINE.find((a) => a.name === station.agent)?.emoji} {station.agent}
                        </p>
                      )}
                      {/* Integration badges */}
                      {station.integration && station.integration.length > 0 && (
                        <div className="mt-1.5 flex flex-wrap gap-1">
                          {station.integration.map((int) => {
                            const ip = INTEGRATION_POINTS.find((p) => p.id === int);
                            return ip ? (
                              <span
                                key={int}
                                className="inline-flex items-center gap-0.5 rounded bg-slate-100 px-1.5 py-0.5 text-[9px] font-medium text-slate-600"
                                title={ip.protocol}
                              >
                                {ip.icon}
                              </span>
                            ) : null;
                          })}
                        </div>
                      )}
                      {/* Connector arrow (not last in phase) */}
                      {i < stations.length - 1 && (
                        <div className="absolute -bottom-3 left-1/2 -translate-x-1/2 text-slate-300 text-xs">↓</div>
                      )}
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
      {/* Phase connectors */}
      <div className="mt-4 flex justify-center text-slate-400 text-xs">
        <span>Issue → </span>
        {phases.map((p, i) => (
          <span key={p} className="flex items-center gap-1">
            <span className={`inline-block h-2 w-2 rounded-full ${PHASE_COLORS[p].accent}`} />
            {p}
            {i < phases.length - 1 && <span className="mx-1">→</span>}
          </span>
        ))}
        <span className="ml-1"> → PR Merged</span>
      </div>
    </div>
  );
}

/* ───────────────────── Station Detail Panel ───────────────────── */

function StationDetail({ station }: { station: Station }) {
  const colors = PHASE_COLORS[station.phase];
  const agentInfo = AGENTS_ON_LINE.find((a) => a.name === station.agent);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className={`rounded-2xl border-2 p-6 ${colors.border} ${colors.bg}`}>
        <div className="flex items-start gap-4">
          <span className="text-3xl">{STATION_ICONS[station.kind]}</span>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <h3 className={`text-xl font-semibold ${colors.text}`}>{station.title}</h3>
              {station.gate && (
                <span className="rounded-full bg-amber-500 px-2.5 py-0.5 text-xs font-bold text-white">
                  Gate {station.gate.number}
                </span>
              )}
              {station.human && (
                <span className="rounded-full bg-rose-500 px-2.5 py-0.5 text-xs font-bold text-white">
                  👤 Human Gate
                </span>
              )}
            </div>
            <p className="mt-1 text-sm text-slate-600">{station.subtitle}</p>
          </div>
        </div>
      </div>

      {/* Agent */}
      {agentInfo && (
        <Card>
          <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Agent</h4>
          <div className="mt-2 flex items-center gap-3">
            <span className="text-2xl">{agentInfo.emoji}</span>
            <div>
              <p className="font-medium text-slate-900">{agentInfo.name}</p>
              <p className="text-sm text-slate-600">{agentInfo.role}</p>
            </div>
          </div>
        </Card>
      )}

      {/* Skills */}
      <Card>
        <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Skills</h4>
        <div className="mt-2 flex flex-wrap gap-1.5">
          {station.skills.map((s) => (
            <span
              key={s}
              className="rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-medium text-violet-700"
            >
              {s}
            </span>
          ))}
        </div>
      </Card>

      {/* Tools */}
      <Card>
        <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Tools</h4>
        <div className="mt-2 space-y-1">
          {station.tools.map((t) => (
            <Code key={t}>{t}</Code>
          ))}
        </div>
      </Card>

      {/* Inputs & Outputs */}
      <div className="grid grid-cols-2 gap-4">
        <Card>
          <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Inputs</h4>
          <ul className="mt-2 space-y-1">
            {station.inputs.map((inp) => (
              <li key={inp} className="flex items-start gap-2 text-sm text-slate-700">
                <span className="mt-1 text-xs text-slate-400">→</span>
                {inp}
              </li>
            ))}
          </ul>
        </Card>
        <Card>
          <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Outputs</h4>
          <ul className="mt-2 space-y-1">
            {station.outputs.map((out) => (
              <li key={out} className="flex items-start gap-2 text-sm text-slate-700">
                <span className="mt-1 text-xs text-emerald-500">→</span>
                {out}
              </li>
            ))}
          </ul>
        </Card>
      </div>

      {/* Gate details */}
      {station.gate && (
        <Card className="border-amber-200 bg-amber-50/50">
          <h4 className="text-sm font-semibold text-amber-700 uppercase tracking-wide">
            Gate {station.gate.number} — {station.gate.name}
          </h4>
          <ul className="mt-2 space-y-1">
            {station.gate.items.map((item) => (
              <li key={item} className="flex items-start gap-2 text-sm text-slate-700">
                <span className="mt-1 text-amber-500">●</span>
                {item}
              </li>
            ))}
          </ul>
          <p className="mt-3 text-xs text-slate-500">
            Checked by <Code>{station.gate.check}</Code>
          </p>
        </Card>
      )}

      {/* Human gate */}
      {station.human && (
        <Card className="border-rose-200 bg-rose-50/50">
          <h4 className="text-sm font-semibold text-rose-700 uppercase tracking-wide">👤 Human Gate</h4>
          <p className="mt-1 text-sm text-slate-700"><strong>{station.human.role}:</strong> {station.human.action}</p>
          <p className="mt-1 text-xs text-slate-500 italic">When: {station.human.when}</p>
        </Card>
      )}

      {/* RepoLens query */}
      {station.repolens && (
        <Card className="border-indigo-200 bg-indigo-50/50">
          <h4 className="text-sm font-semibold text-indigo-700 uppercase tracking-wide">🔍 RepoLens Query</h4>
          <p className="mt-1 text-sm text-slate-700">{station.repolens.description}</p>
          <div className="mt-1"><Code>{station.repolens.query}</Code></div>
          <a
            href="/ai-sdlc/repolens/"
            target="__blank"
            className="mt-2 inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-800"
          >
            Open RepoLens dashboard ↗
          </a>
        </Card>
      )}

      {/* Integration points */}
      {station.integration && station.integration.length > 0 && (
        <Card>
          <h4 className="text-sm font-semibold text-slate-500 uppercase tracking-wide">Integration Points</h4>
          <div className="mt-2 space-y-2">
            {station.integration.map((intId) => {
              const ip = INTEGRATION_POINTS.find((p) => p.id === intId);
              if (!ip) return null;
              return (
                <div key={intId} className="flex items-start gap-2 text-sm">
                  <span className="text-lg">{ip.icon}</span>
                  <div>
                    <p className="font-medium text-slate-900">{ip.name}</p>
                    <p className="text-xs text-slate-500">{ip.protocol}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </Card>
      )}
    </div>
  );
}

/* ───────────────────── Event Stream ───────────────────── */

function EventStream() {
  const [playing, setPlaying] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(-1);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (playing) {
      timerRef.current = setInterval(() => {
        setCurrentIndex((prev) => {
          if (prev >= EXAMPLE_EVENTS.length - 1) {
            setPlaying(false);
            return prev;
          }
          return prev + 1;
        });
      }, 800);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [playing]);

  const resultColors: Record<string, string> = {
    pass: "bg-emerald-100 text-emerald-700",
    fail: "bg-rose-100 text-rose-700",
    pending: "bg-amber-100 text-amber-700",
    human: "bg-violet-100 text-violet-700",
  };

  return (
    <div>
      <div className="mb-4 flex items-center gap-3">
        <button
          onClick={() => {
            if (currentIndex >= EXAMPLE_EVENTS.length - 1) setCurrentIndex(-1);
            setPlaying(!playing);
          }}
          className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
        >
          {playing ? "⏸ Pause" : currentIndex >= EXAMPLE_EVENTS.length - 1 ? "↻ Replay" : "▶ Play"}
        </button>
        <button
          onClick={() => {
            setPlaying(false);
            setCurrentIndex(-1);
          }}
          className="rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-600 hover:bg-slate-50"
        >
          Reset
        </button>
        <span className="text-xs text-slate-500">
          {currentIndex + 1} / {EXAMPLE_EVENTS.length} events
        </span>
      </div>

      <div className="max-h-[500px] overflow-y-auto rounded-xl border border-slate-200 bg-slate-50 p-4" role="log" aria-live="polite">
        {EXAMPLE_EVENTS.map((event, i) => {
          const isPast = i <= currentIndex;
          const isCurrent = i === currentIndex;
          const agentInfo = AGENTS_ON_LINE.find((a) => a.name === event.agent);
          return (
            <div
              key={i}
              className={`mb-2 flex items-start gap-3 rounded-lg p-2 text-sm transition-all duration-300 ${
                isCurrent ? "bg-white shadow-sm ring-1 ring-slate-200" : isPast ? "opacity-70" : "opacity-40"
              }`}
            >
              <span className="font-mono text-xs text-slate-400 min-w-[40px]">{event.time}</span>
              <span className="text-base">{agentInfo?.emoji}</span>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-medium text-slate-900 truncate">{event.action}</span>
                  <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${resultColors[event.result]}`}>
                    {event.result.toUpperCase()}
                  </span>
                </div>
                {event.detail && (
                  <p className="text-xs text-slate-500 truncate">{event.detail}</p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* ───────────────────── Agents Roster ───────────────────── */

function AgentsRoster() {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
      {AGENTS_ON_LINE.map((agent) => (
        <Card key={agent.name} className="text-center">
          <span className="text-3xl">{agent.emoji}</span>
          <p className="mt-1 text-sm font-semibold text-slate-900">{agent.name}</p>
          <p className="text-xs text-slate-500">{agent.role}</p>
          <div className="mt-2 flex flex-wrap justify-center gap-1">
            {agent.stations.slice(0, 3).map((s) => {
              const station = STATIONS.find((st) => st.id === s);
              return station ? (
                <span
                  key={s}
                  className={`rounded px-1.5 py-0.5 text-[9px] font-medium ${PHASE_COLORS[station.phase].bg} ${PHASE_COLORS[station.phase].text}`}
                >
                  {STATION_ICONS[station.kind]}
                </span>
              ) : null;
            })}
            {agent.stations.length > 3 && (
              <span className="text-[9px] text-slate-400">+{agent.stations.length - 3}</span>
            )}
          </div>
        </Card>
      ))}
    </div>
  );
}

/* ───────────────────── Integration Points ───────────────────── */

function IntegrationPoints() {
  return (
    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
      {INTEGRATION_POINTS.map((ip) => (
        <Card key={ip.id}>
          <div className="flex items-start gap-3">
            <span className="text-2xl">{ip.icon}</span>
            <div className="flex-1">
              <h4 className="font-semibold text-slate-900">{ip.name}</h4>
              <p className="mt-0.5 text-xs text-slate-500 font-mono">{ip.protocol}</p>
              <p className="mt-1 text-sm text-slate-600">{ip.description}</p>
              <div className="mt-2 flex flex-wrap gap-1">
                {ip.stations.slice(0, 5).map((s) => {
                  const station = STATIONS.find((st) => st.id === s);
                  return station ? (
                    <span
                      key={s}
                      className={`rounded px-1.5 py-0.5 text-[10px] font-medium ${PHASE_COLORS[station.phase].bg} ${PHASE_COLORS[station.phase].text}`}
                    >
                      {station.title}
                    </span>
                  ) : null;
                })}
              </div>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}

/* ───────────────────── Main Page ───────────────────── */

export default function FactoryLinePage() {
  const [selectedStation, setSelectedStation] = useState<string | null>(null);
  const [view, setView] = useState<"line" | "events" | "agents" | "integrations" | "diagrams">("line");
  const [showIntent, setShowIntent] = useState(true);
  const [intentIssue, setIntentIssue] = useState("#2380");

  const selected = STATIONS.find((s) => s.id === selectedStation) ?? null;

  return (
    <>
      <style>{`
        @keyframes gate-pulse {
          0%, 100% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.8; transform: scale(1.1); }
        }
        @keyframes conveyor-flow {
          0% { background-position: 0 0; }
          100% { background-position: 20px 0; }
        }
        @keyframes station-enter {
          from { opacity: 0; transform: translateY(8px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .gate-pulse { animation: gate-pulse 2s ease-in-out infinite; }
        .conveyor-flow {
          background-image: repeating-linear-gradient(90deg, transparent, transparent 18px, rgba(148,163,184,0.15) 18px, rgba(148,163,184,0.15) 20px);
          animation: conveyor-flow 1s linear infinite;
        }
        .station-enter { animation: station-enter 0.3s ease-out both; }
      `}</style>
      <div>
      {/* Intent Panel — always visible at top */}
      {showIntent && (
        <IntentPanel issueId={intentIssue} onClose={() => setShowIntent(false)} />
      )}
      {!showIntent && (
        <button
          onClick={() => setShowIntent(true)}
          className="sticky top-0 z-10 mb-4 inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white/95 px-3 py-1.5 text-xs font-medium text-slate-600 shadow-sm backdrop-blur-sm hover:bg-slate-50"
        >
          # Show intent
        </button>
      )}

      <PageHeader
        eyebrow="AI Factory · Production Line"
        title="From Issue to PR — the full factory floor"
        lead="Every change rides a conveyor through 16 stations across 5 phases. The intent (GitHub issue) is the input; a merged PR is the output. Agents work at each station, gates stop the line until checks pass, and the human Owner decides at critical points."
      />

      {/* Overview diagram */}
      <Diagram
        chart={FACTORY_LINE_DIAGRAM}
        caption="The AI SDLC production line: 5 phases, 16 stations, 5 hard gates. Each station has an agent, skills, tools, and integration points."
      />

      {/* Tab navigation */}
      <div className="mb-6 flex gap-1 rounded-xl bg-slate-100 p-1" role="tablist">
        {[
          { id: "line" as const, label: "🏭 Production Line" },
          { id: "events" as const, label: "📡 Event Stream" },
          { id: "agents" as const, label: "👥 Agents" },
          { id: "integrations" as const, label: "🔌 Integrations" },
          { id: "diagrams" as const, label: "📊 Diagrams" },
        ].map((tab) => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={view === tab.id}
            onClick={() => setView(tab.id)}
            className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
              view === tab.id
                ? "bg-white text-slate-900 shadow-sm"
                : "text-slate-600 hover:text-slate-900"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Production Line view */}
      {view === "line" && (
        <div className="grid lg:grid-cols-5 gap-6">
          <div className="lg:col-span-3">
            <SectionTitle>Production Line</SectionTitle>
            <p className="mb-4 text-sm text-slate-600">
              Click a station to see its agent, skills, tools, inputs, outputs, and gate details.
            </p>
            <ProductionLine selected={selectedStation} onSelect={setSelectedStation} />
          </div>
          <div className="lg:col-span-2">
            <SectionTitle>Station Detail</SectionTitle>
            {selected ? (
              <StationDetail station={selected} />
            ) : (
              <Card className="text-center text-slate-500 py-12">
                <span className="text-4xl">👆</span>
                <p className="mt-3 text-sm">Select a station on the production line to see its details</p>
              </Card>
            )}
          </div>
        </div>
      )}

      {/* Event Stream view */}
      {view === "events" && (
        <div>
          <SectionTitle>Event Stream</SectionTitle>
          <p className="mb-4 text-sm text-slate-600">
            Watch a sample run flow through all 16 stations. Each event shows the agent, action, and result.
            This is how the factory processes a change from issue intake to deployment.
          </p>
          <EventStream />
        </div>
      )}

      {/* Agents view */}
      {view === "agents" && (
        <div>
          <SectionTitle>Agent Crew</SectionTitle>
          <p className="mb-4 text-sm text-slate-600">
            10 specialized agents work the production line. Each agent has a role, a set of skills,
            and works at specific stations. The Foreman orchestrates; Workers execute.
          </p>
          <AgentsRoster />
        </div>
      )}

      {/* Integrations view */}
      {view === "integrations" && (
        <div>
          <SectionTitle>Integration Points</SectionTitle>
          <p className="mb-4 text-sm text-slate-600">
            The factory talks to 5 external systems via API, CLI, and MCP protocols.
            Each integration point connects to specific stations on the line.
          </p>
          <IntegrationPoints />
        </div>
      )}

      {/* Diagrams view */}
      {view === "diagrams" && (
        <div>
          <SectionTitle>Sequence Diagram</SectionTitle>
          <p className="mb-4 text-sm text-slate-600">
            The full interaction sequence from Owner creating an issue to the Foreman closing it after deployment.
          </p>
          <Diagram
            chart={AGENT_FLOW_DIAGRAM}
            caption="Sequence diagram: Owner → Foreman → Worker → Gates → CI → GitHub. Numbered steps show the exact interaction order."
          />
        </div>
      )}

      {/* Sources */}
      <SectionTitle>How it works</SectionTitle>
      <Card>
        <p className="text-sm text-slate-700 leading-relaxed">
          The production line is driven by the <Code>CONSTITUTION.md</Code> workflow: 24 sequential steps enforced by
          5 hard gates. Each gate is a script with an exit code — prose rules are wishes, exit codes are law.
          The <Code>aisdlc</Code> kit orchestrates the stages, the Foreman agent dispatches Workers,
          and machines (not agents) decide whether the work is done.
        </p>
        <Sources
          items={[
            { label: "CONSTITUTION.md §5 Workflow", href: src("CONSTITUTION.md") },
            { label: "FACTORY-UI-SPEC.md", href: src("docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md") },
            { label: "framework/ai-sdlc/", href: src("framework/ai-sdlc/") },
            { label: ".aisdlc/aisdlc.toml", href: src(".aisdlc/aisdlc.toml") },
          ]}
        />
      </Card>
    </div>
    </>
  );
}
