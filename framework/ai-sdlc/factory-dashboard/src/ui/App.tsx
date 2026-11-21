/**
 * Factory Dashboard — Main app component.
 *
 * Production line control room: intent panel, station grid, event stream,
 * agent roster, and tabs for navigation.
 */
import { useState, useMemo, useCallback, useEffect } from "react";
import { Station, Agent, EvidenceEvent, StationPhase, ControlState, EvidenceKind } from "@/types";
import { StationCard } from "@/components/StationCard";
import { EventStream } from "@/components/EventStream";
import { StationDetail } from "@/components/StationDetail";
import { MetricsView } from "@/components/MetricsView";
import { ControlBar } from "@/components/ControlBar";
import { ConfigView } from "@/components/ConfigView";
import { useWebSocket } from "@/hooks/useWebSocket";
import { IntentPanel } from "@/components/IntentPanel";
import "@/styles/dashboard.css";

// ───────────────────── Default stations (production line definition) ─────────────────────

const DEFAULT_STATIONS: Station[] = [
  // Intake
  { id: "intake", kind: "intake", title: "Issue Intake", phase: "Intake", agent: "sync_issues_and_code", status: "idle", skills: ["devsecops-traceability"], tools: ["gh issue"] },
  { id: "foreman-claim", kind: "human", title: "Foreman Claim", phase: "Intake", agent: "cloud-foreman", status: "idle", skills: ["ai-agent-workflow"], tools: ["aisdlc foreman"], human: { role: "Owner", action: "Approves spec" } },

  // Plan
  { id: "spec", kind: "plan", title: "Specification", phase: "Plan", agent: "openspec-planner", status: "idle", skills: ["openspec-propose"], tools: ["openspec propose"], gate: { number: 1, name: "Issue + AC", check: "validate-sdlc-plan.sh" } },
  { id: "architecture", kind: "plan", title: "Architecture", phase: "Plan", agent: "java-architect", status: "idle", skills: ["hexagonal-arch"], tools: ["repolens"] },
  { id: "branch", kind: "plan", title: "Branch & Setup", phase: "Plan", agent: "cloud-foreman", status: "idle", skills: ["ai-agent-workflow"], tools: ["git"] },

  // Prove
  { id: "test-design", kind: "prove", title: "Test Design", phase: "Prove", agent: "testing-qa", status: "idle", skills: ["qa-automation"], tools: ["Playwright"], gate: { number: 2, name: "TDD evidence", check: "check-tdd-evidence.sh" } },
  { id: "failing-tests", kind: "prove", title: "Failing Tests", phase: "Prove", agent: "backend-implementer", status: "idle", skills: ["backend", "java"], tools: ["mvn test"] },
  { id: "implement", kind: "prove", title: "Implementation", phase: "Prove", agent: "backend-implementer", status: "idle", skills: ["programming"], tools: ["repolens", "RepoLens MCP"] },
  { id: "full-suite", kind: "prove", title: "Full Suite & E2E", phase: "Prove", agent: "testing-qa", status: "idle", skills: ["testing"], tools: ["mvn verify", "Playwright"] },

  // Publish
  { id: "docs", kind: "publish", title: "Documentation", phase: "Publish", agent: "backend-implementer", status: "idle", skills: ["openspec-apply"], tools: ["check-doc-traceability.py"], gate: { number: 3, name: "Pre-PR gate", check: "preflight.sh --full" } },
  { id: "commits", kind: "publish", title: "Atomic Commits", phase: "Publish", agent: "backend-implementer", status: "idle", skills: ["openspec-apply"], tools: ["git commit"] },
  { id: "pipeline", kind: "publish", title: "Local Pipeline", phase: "Publish", agent: "devops-engineer", status: "idle", skills: ["ci-cd"], tools: ["run_pipeline.sh"] },
  { id: "pr", kind: "publish", title: "Pull Request", phase: "Publish", agent: "backend-implementer", status: "idle", skills: ["openspec-apply"], tools: ["gh pr create"] },

  // Ship
  { id: "review", kind: "ship", title: "Code Review", phase: "Ship", agent: "code-reviewer", status: "idle", skills: ["programming"], tools: ["gh pr review"], gate: { number: 4, name: "No open findings", check: "check-heavy-ci.sh" }, human: { role: "Owner", action: "Merges owner-only paths" } },
  { id: "merge", kind: "ship", title: "Merge & Archive", phase: "Ship", agent: "cloud-foreman", status: "idle", skills: ["ai-agent-workflow"], tools: ["gh pr merge"] },
  { id: "deploy", kind: "ship", title: "Deploy & Sign", phase: "Ship", agent: "devops-engineer", status: "idle", skills: ["ci-cd"], tools: ["cosign", "GHCR"] },
  { id: "smoke", kind: "ship", title: "Smoke & Close", phase: "Ship", agent: "cloud-foreman", status: "idle", skills: ["devsecops-traceability"], tools: ["gh issue close"], gate: { number: 5, name: "Deploy verified", check: "health endpoint UP" } },
];

const DEFAULT_AGENTS: Agent[] = [
  { name: "cloud-foreman", emoji: "🧭", role: "Orchestrator", status: "idle" },
  { name: "openspec-planner", emoji: "📋", role: "Spec writer", status: "idle" },
  { name: "java-architect", emoji: "🏗️", role: "Architecture", status: "idle" },
  { name: "testing-qa", emoji: "🧪", role: "Test design", status: "idle" },
  { name: "backend-implementer", emoji: "⚙️", role: "Implementation", status: "idle" },
  { name: "devops-engineer", emoji: "🔧", role: "CI/CD", status: "idle" },
  { name: "code-reviewer", emoji: "🔍", role: "Gate 4 review", status: "idle" },
  { name: "security-auditor", emoji: "🔒", role: "Security", status: "idle" },
  { name: "sync_issues_and_code", emoji: "🔄", role: "Issue hygiene", status: "idle" },
];

const PHASES: StationPhase[] = ["Intake", "Plan", "Prove", "Publish", "Ship"];

type TabId = "line" | "events" | "agents";

// ───────────────────── App ─────────────────────

export default function App() {
  const [activeTab, setActiveTab] = useState<TabId>("line");
  const [stations, setStations] = useState<Station[]>(DEFAULT_STATIONS);
  const [agents] = useState<Agent[]>(DEFAULT_AGENTS);
  const [events, setEvents] = useState<EvidenceEvent[]>([]);

  const { connected } = useWebSocket(
    typeof window !== "undefined"
      ? `ws://${window.location.host}/ws`
      : "ws://127.0.0.1:8766/ws"
  );

  // Process WebSocket messages to update station status
  const processMessage = (data: any) => {
    if (data.kind) {
      setEvents((prev) => [...prev.slice(-100), data as EvidenceEvent]);

      // Map evidence events to station status
      if (data.kind === "run.started") {
        setStations((prev) =>
          prev.map((s) => (s.id === "intake" ? { ...s, status: "completed" as const } : s))
        );
      }
      if (data.kind === "step.started" && data.step) {
        setStations((prev) =>
          prev.map((s) => (s.id === data.step ? { ...s, status: "active" as const } : s))
        );
      }
      if (data.kind === "step.finished" && data.step) {
        setStations((prev) =>
          prev.map((s) => (s.id === data.step ? { ...s, status: "completed" as const } : s))
        );
      }
      if (data.kind === "run.finished") {
        setStations((prev) =>
          prev.map((s) => (s.status === "active" ? { ...s, status: "completed" as const } : s))
        );
      }
    }
  };

  // Simulate demo events for showcase
  const [demoMode] = useState(true);
  const demoEvents: EvidenceEvent[] = useMemo(() => {
    if (!demoMode) return [];
    const now = Date.now();
    return [
      { kind: "run.request", run_id: "run-demo-001", timestamp: new Date(now - 60000).toISOString() },
      { kind: "run.started", run_id: "run-demo-001", timestamp: new Date(now - 55000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "intake", timestamp: new Date(now - 50000).toISOString() },
      { kind: "step.finished", run_id: "run-demo-001", step: "intake", status: "ok", timestamp: new Date(now - 45000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "foreman-claim", timestamp: new Date(now - 40000).toISOString() },
      { kind: "step.finished", run_id: "run-demo-001", step: "foreman-claim", status: "ok", timestamp: new Date(now - 35000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "spec", timestamp: new Date(now - 30000).toISOString() },
      { kind: "step.finished", run_id: "run-demo-001", step: "spec", status: "ok", timestamp: new Date(now - 25000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "architecture", timestamp: new Date(now - 20000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "branch", timestamp: new Date(now - 15000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "test-design", timestamp: new Date(now - 10000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "failing-tests", timestamp: new Date(now - 5000).toISOString() },
      { kind: "step.started", run_id: "run-demo-001", step: "implement", timestamp: new Date(now).toISOString() },
    ];
  }, [demoMode]);

  // Apply demo station statuses
  const demoStations = useMemo(() => {
    if (!demoMode) return stations;
    const completedSteps = new Set<string>();
    const activeSteps = new Set<string>();
    for (const evt of demoEvents) {
      if (evt.kind === "step.finished" && evt.step) completedSteps.add(evt.step);
      if (evt.kind === "step.started" && evt.step) activeSteps.add(evt.step);
    }
    return stations.map((s) => {
      if (completedSteps.has(s.id) && !activeSteps.has(s.id)) return { ...s, status: "completed" as const };
      if (activeSteps.has(s.id)) return { ...s, status: "active" as const };
      return s;
    });
  }, [stations, demoEvents, demoMode]);

  const intent = useMemo(
    () => ({
      id: "#2380",
      source: "github" as const,
      title: "CLI comparison experiments & factory dashboard",
      description: "Run controlled experiments comparing AI SDLC + RepoLens across 4 CLI tools and build a production line dashboard.",
      created_at: new Date().toISOString(),
      run_id: "run-demo-001",
    }),
    []
  );

  const phasesByGroup = (phase: StationPhase) =>
    demoStations.filter((s) => s.phase === phase);

  return (
    <div className="app-shell">
      {/* Header */}
      <header className="header">
        <div className="header-title">
          <span>⚙️</span>
          AI SDLC Factory Dashboard
        </div>
        <div className="header-status">
          <span className={`status-dot ${connected ? "connected" : ""}`} />
          {connected ? "Live" : "Connecting…"}
        </div>
      </header>

      {/* Intent panel — always visible */}
      <IntentPanel intent={intent} />

      {/* Main content */}
      <main className="main-content">
        {/* Tabs */}
        <div className="tabs" role="tablist">
          <button
            className={`tab ${activeTab === "line" ? "active" : ""}`}
            onClick={() => setActiveTab("line")}
            role="tab"
            aria-selected={activeTab === "line"}
          >
            Production Line
          </button>
          <button
            className={`tab ${activeTab === "events" ? "active" : ""}`}
            onClick={() => setActiveTab("events")}
            role="tab"
            aria-selected={activeTab === "events"}
          >
            Event Stream
          </button>
          <button
            className={`tab ${activeTab === "agents" ? "active" : ""}`}
            onClick={() => setActiveTab("agents")}
            role="tab"
            aria-selected={activeTab === "agents"}
          >
            Agents
          </button>
          <button
            className={`tab ${activeTab === "metrics" ? "active" : ""}`}
            onClick={() => setActiveTab("metrics")}
            role="tab"
            aria-selected={activeTab === "metrics"}
          >
            Metrics
          </button>
          <button
            className={`tab ${activeTab === "config" ? "active" : ""}`}
            onClick={() => setActiveTab("config")}
            role="tab"
            aria-selected={activeTab === "config"}
          >
            Configuration
          </button>
        </div>

        {/* Production Line tab */}
        {activeTab === "line" && (
          <div className="production-line">
            {PHASES.map((phase) => {
              const phaseStations = phasesByGroup(phase);
              if (phaseStations.length === 0) return null;
              return (
                <div key={phase} className="phase-group">
                  <div className="phase-label">{phase}</div>
                  <div className="conveyor-line" />
                  <div className="stations-row">
                    {phaseStations.map((station) => (
                      <StationCard key={station.id} station={station} />
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Event Stream tab */}
        {activeTab === "events" && (
          <EventStream events={demoEvents} />
        )}

        {/* Agents tab */}
        {activeTab === "agents" && (
          <div className="agents-grid">
            {agents.map((agent) => (
              <div key={agent.name} className="agent-card">
                <div className="agent-emoji">{agent.emoji}</div>
                <div className="agent-name">{agent.name}</div>
                <div className="agent-role">{agent.role}</div>
                <div className="agent-status">{agent.status}</div>
              </div>
            ))}
          </div>
        )}

        {/* Metrics tab */}
        {activeTab === "metrics" && <MetricsView />}

        {/* Configuration tab */}
        {activeTab === "config" && <ConfigView />}
      </main>
    </div>
  );
}
