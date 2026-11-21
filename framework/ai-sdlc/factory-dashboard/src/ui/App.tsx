/**
 * Factory Dashboard — Main app component.
 *
 * Manufacturing control room: sidebar navigation, always-visible intent bar,
 * production line with clickable stations opening an inspection panel.
 */
import { useState, useMemo, useCallback, useEffect } from "react";
import { Station, Agent, EvidenceEvent, StationPhase, ControlState, Intent } from "@/types";
import { EventStream } from "@/components/EventStream";
import { StationDetail } from "@/components/StationDetail";
import { MetricsView } from "@/components/MetricsView";
import { ControlBar } from "@/components/ControlBar";
import { ConfigView } from "@/components/ConfigView";
import { FileViewerView } from "@/components/FileViewerView";
import { RepoLensMetricsView } from "@/components/RepoLensMetricsView";
import { HistoryView } from "@/components/HistoryView";
import { ActiveStationView } from "@/components/ActiveStationView";
import { CompactTimeline } from "@/components/CompactTimeline";
import { IntentView } from "@/components/IntentView";
import { useWebSocket } from "@/hooks/useWebSocket";
import "@/styles/dashboard.css";

// ───────────────────── Default stations ─────────────────────

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

const PHASE_INDICATOR_CLASS: Record<StationPhase, string> = {
  Intake: "intake",
  Plan: "plan",
  Prove: "prove",
  Publish: "publish",
  Ship: "ship",
};

// Map StationPhase to data-phase attribute (lowercase)
const PHASE_DATA_ATTR: Record<StationPhase, string> = {
  Intake: "intake",
  Plan: "plan",
  Prove: "prove",
  Publish: "publish",
  Ship: "ship",
};

// Map status to data-status attribute
const STATUS_MAP: Record<string, string> = {
  idle: "idle",
  active: "running",
  completed: "done",
  blocked: "error",
  waiting: "waiting",
  skipped: "skipped",
  paused: "paused",
};

type ViewId = "line" | "metrics" | "events" | "config" | "files" | "repolens-metrics" | "history";

type Theme = "light" | "dark";

// ───────────────────── App ─────────────────────

export default function App() {
  const [activeView, setActiveView] = useState<ViewId>("line");
  const [theme, setTheme] = useState<Theme>("light");
  const [controlState, setControlState] = useState<ControlState>("idle");
  const [selectedIntentId, setSelectedIntentId] = useState<string | undefined>(undefined);
  const [stations, setStations] = useState<Station[]>(DEFAULT_STATIONS);
  const [agents] = useState<Agent[]>(DEFAULT_AGENTS);
  const [events, setEvents] = useState<EvidenceEvent[]>([]);
  const [selectedStation, setSelectedStation] = useState<Station | null>(null);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setTheme((t) => (t === "light" ? "dark" : "light"));
  }, []);

  const { connected, send } = useWebSocket(
    typeof window !== "undefined"
      ? `ws://${window.location.host}/ws`
      : "ws://127.0.0.1:8766/ws"
  );

  const processMessage = useCallback((data: any) => {
    if (data.kind) {
      setEvents((prev) => [...prev.slice(-100), data as EvidenceEvent]);
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
      if (data.kind === "step.skipped" && data.step) {
        setStations((prev) =>
          prev.map((s) => (s.id === data.step ? { ...s, status: "skipped" as const } : s))
        );
      }
      if (data.kind === "run.paused") {
        setControlState("paused");
      }
      if (data.kind === "run.resumed" || data.kind === "run.play") {
        setControlState("playing");
      }
      if (data.kind === "run.stopped") {
        setControlState("stopped");
      }
    }
    // Handle control acknowledgments
    if (data.command && data.accepted !== undefined) {
      setEvents((prev) => [...prev.slice(-100), { kind: "run.finished", timestamp: new Date().toISOString(), ...data } as EvidenceEvent]);
    }
  }, []);

  // Simulate demo events for showcase
  const demoEvents: EvidenceEvent[] = useMemo(() => {
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
  }, []);

  // Apply demo station statuses
  const demoStations = useMemo(() => {
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
  }, [stations, demoEvents]);

  const intent: Intent = useMemo(
    () => ({
      id: "#2380",
      source: "github" as const,
      title: "CLI comparison experiments & factory dashboard",
      description: "Run controlled experiments comparing AI SDLC + RepoLens across 4 CLI tools (Codex, AGY, kiro-cli, opencode). Extend providers.py, run 4 tasks per CLI with baseline and graph arms, capture metrics, publish to GitHub Pages, and create a GitHub issue.",
      created_at: new Date().toISOString(),
      run_id: "run-demo-001",
      flow: "1. Extend providers.py with 4 new CLI specs → 2. Run lightweight experiments (4 tasks × 2 arms) → 3. Capture recall, latency, tool calls → 4. Publish results to GitHub Pages → 5. Create PR with findings",
      acceptance_criteria: [
        { text: "providers.py supports codex, agy, kiro-cli, and opencode", done: true },
        { text: "Each CLI runs 4 tasks with and without RepoLens MCP", done: true },
        { text: "Results saved as agent-<cli-name>.json in bench/results/", done: true },
        { text: "repolens-bench-snapshot.json updated with new CLI data", done: true },
        { text: "GitHub issue created with findings summary", done: true },
        { text: "PR created with all changes", done: false },
      ],
      restrictions: [
        { text: "Free-tier rate limits — sequential runs with delays", severity: "warning" },
        { text: "MCP support varies per CLI — test before graph arm", severity: "required" },
      ],
      links: [
        { label: "AI SDLC Framework README", url: "https://github.com/matiaspakua/notaire/tree/main/framework/ai-sdlc", type: "documentation" },
        { label: "RepoLens Bench Tasks", url: "https://github.com/matiaspakua/notaire/blob/main/framework/ai-sdlc/repolens/bench/tasks/notaire.yaml", type: "reference" },
        { label: "Factory UI Spec", url: "https://github.com/matiaspakua/notaire/blob/main/docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md", type: "design" },
        { label: "PR #2381 — CLI Comparison", url: "https://github.com/matiaspakua/notaire/pull/2381", type: "related" },
      ],
      references: [
        "ADR-030: Hexagonal Architecture Full Adoption",
        "CONSTITUTION.md §12: Self-improve while delivering",
        "framework/ai-sdlc/repolens/bench/providers.py",
      ],
    }),
    []
  );

  const handleStationClick = useCallback((station: Station) => {
    setSelectedStation(station);
  }, []);

  const handleControl = useCallback(
    (command: "play" | "pause" | "resume" | "stop" | "skip", reason?: string) => {
      const runId = intent?.run_id || "run-demo-001";
      send({ command, run_id: runId, reason });

      // Update local state immediately for responsive UI
      if (command === "play") setControlState("playing");
      if (command === "pause") setControlState("paused");
      if (command === "resume") setControlState("playing");
      if (command === "stop") setControlState("stopped");

      // Add control event to the stream
      setEvents((prev) => [
        ...prev.slice(-100),
        {
          kind: `run.${command}` as any,
          run_id: runId,
          timestamp: new Date().toISOString(),
          reason,
        } as EvidenceEvent,
      ]);
    },
    [send, intent]
  );

  const handleSkipStation = useCallback(
    (stationId: string) => {
      handleControl("skip", `Skip station: ${stationId}`);
      setStations((prev) =>
        prev.map((s) => (s.id === stationId ? { ...s, status: "skipped" as const } : s))
      );
    },
    [handleControl]
  );

  const handleCloseDetail = useCallback(() => {
    setSelectedStation(null);
  }, []);

  const phasesByGroup = (phase: StationPhase) =>
    demoStations.filter((s) => s.phase === phase);

  const globalStationIndex = useMemo(() => {
    const map: Record<string, number> = {};
    let idx = 1;
    for (const s of demoStations) {
      map[s.id] = idx++;
    }
    return map;
  }, [demoStations]);

  const completedCount = demoStations.filter((s) => s.status === "completed").length;
  const runningCount = demoStations.filter((s) => s.status === "active").length;

  return (
    <div className="app-shell">
      {/* ── Sidebar ── */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="sidebar-brand-icon">F</span>
          AI SDLC Factory
        </div>

        <nav className="sidebar-nav">
          <div className="sidebar-section-label">Views</div>
          <button
            className={`sidebar-nav-item ${activeView === "line" ? "active" : ""}`}
            onClick={() => setActiveView("line")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <rect x="1" y="1" width="4" height="4" rx="1" />
                <rect x="6" y="1" width="4" height="4" rx="1" />
                <rect x="11" y="1" width="4" height="4" rx="1" />
                <rect x="1" y="6" width="4" height="4" rx="1" />
                <rect x="6" y="6" width="4" height="4" rx="1" />
                <rect x="11" y="6" width="4" height="4" rx="1" />
                <rect x="1" y="11" width="4" height="4" rx="1" />
                <rect x="6" y="11" width="4" height="4" rx="1" />
                <rect x="11" y="11" width="4" height="4" rx="1" />
              </svg>
            </span>
            Production Line
          </button>
          <button
            className={`sidebar-nav-item ${activeView === "metrics" ? "active" : ""}`}
            onClick={() => setActiveView("metrics")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M2 12l4-4 3 2 5-6" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </span>
            Metrics
          </button>
          <button
            className={`sidebar-nav-item ${activeView === "events" ? "active" : ""}`}
            onClick={() => setActiveView("events")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M2 4h12M2 8h12M2 12h8" strokeLinecap="round" />
              </svg>
            </span>
            Event Stream
          </button>
          <button
            className={`sidebar-nav-item ${activeView === "config" ? "active" : ""}`}
            onClick={() => setActiveView("config")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M6 2H3a1 1 0 00-1 1v3a4 4 0 004 4h1a4 4 0 014 4v3a1 1 0 01-1 1H7" strokeLinecap="round" />
                <path d="M14 2a1 1 0 00-1-1H9a1 1 0 00-1 1v3a1 1 0 001 1h4a1 1 0 001-1V2z" strokeLinecap="round" />
              </svg>
            </span>
            Configuration
          </button>
          <button
            className={`sidebar-nav-item ${activeView === "files" ? "active" : ""}`}
            onClick={() => setActiveView("files")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M6 2H3a1 1 0 00-1 1v10a1 1 0 001 1h10a1 1 0 001-1V7l-5-5z" />
                <path d="M11 2v5h4" />
              </svg>
            </span>
            Files
          </button>

          <div className="sidebar-section-label">Data</div>
          <button
            className={`sidebar-nav-item ${activeView === "history" ? "active" : ""}`}
            onClick={() => setActiveView("history")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="8" cy="8" r="6" />
                <path d="M8 4v4l3 2" strokeLinecap="round" />
              </svg>
            </span>
            History
          </button>

          <div className="sidebar-section-label">Integrations</div>
          <button
            className={`sidebar-nav-item ${activeView === "repolens-metrics" ? "active" : ""}`}
            onClick={() => setActiveView("repolens-metrics")}
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="6" cy="6" r="4" />
                <path d="M13 13l-3-3" strokeLinecap="round" />
              </svg>
            </span>
            RepoLens Impact
          </button>
          <button
            className="sidebar-nav-item"
            onClick={() => window.open("http://127.0.0.1:3456", "_blank")}
            title="Open RepoLens Explorer in new tab"
          >
            <span className="sidebar-nav-icon">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M7 3H3a1 1 0 00-1 1v10a1 1 0 001 1h10a1 1 0 001-1v-4" />
                <path d="M13 3l-7 7h7L13 3z" />
              </svg>
            </span>
            RepoLens Explorer
          </button>
        </nav>

        <div className="sidebar-footer">
          <button
            className="sidebar-nav-item"
            onClick={toggleTheme}
            title={`Switch to ${theme === "light" ? "dark" : "light"} theme`}
          >
            <span className="sidebar-nav-icon">
              {theme === "light" ? (
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M14 9A6 6 0 016 2a6.5 6.5 0 108 7z" />
                </svg>
              ) : (
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <circle cx="8" cy="8" r="3" />
                  <path d="M8 1v2M8 13v2M1 8h2M13 8h2M3.5 3.5l1.4 1.4M11.1 11.1l1.4 1.4M3.5 12.5l1.4-1.4M11.1 4.9l1.4-1.4" strokeLinecap="round" />
                </svg>
              )}
            </span>
            {theme === "light" ? "Dark Mode" : "Light Mode"}
          </button>
          <div className="sidebar-status">
            <span className={`status-dot ${connected ? "connected" : ""}`} />
            {connected ? "Live" : "Connecting…"}
          </div>
        </div>
      </aside>

      {/* ── Main ── */}
      <div className="main-content">
        {/* Intent — the core input of the factory */}
        <IntentView intent={intent} />

        {/* View content */}
        <div className="view-container">
          {/* ── Production Line ── */}
          {activeView === "line" && (
            <>
              <div className="view-header">
                <h1 className="view-title">Production Line</h1>
                <p className="view-subtitle">
                  {completedCount} of {demoStations.length} stations complete
                  {runningCount > 0 && ` · ${runningCount} running`}
                  {intent && ` · ${intent.run_id}`}
                </p>
              </div>
              <ControlBar
                controlState={controlState}
                runId={intent?.run_id || null}
                onControl={handleControl}
              />
              <ActiveStationView
                stations={demoStations}
                agents={agents}
                phases={PHASES}
                phaseDataAttr={PHASE_DATA_ATTR}
                onStationClick={handleStationClick}
              />
              <CompactTimeline
                stations={demoStations}
                phases={PHASES}
                phaseDataAttr={PHASE_DATA_ATTR}
                onStationClick={handleStationClick}
              />
            </>
          )}

          {/* ── Metrics ── */}
          {activeView === "metrics" && (
            <>
              <div className="view-header">
                <h1 className="view-title">Metrics</h1>
                <p className="view-subtitle">Production line KPIs and trends</p>
              </div>
              <MetricsView onSelectIntentId={setSelectedIntentId} />
            </>
          )}

          {/* ── Event Stream ── */}
          {activeView === "events" && (
            <>
              <div className="view-header">
                <h1 className="view-title">Event Stream</h1>
                <p className="view-subtitle">
                  {demoEvents.length + events.length} events captured
                </p>
              </div>
              <EventStream events={demoEvents} filterIntentId={selectedIntentId} />
            </>
          )}

          {/* ── Configuration ── */}
          {activeView === "config" && (
            <>
              <div className="view-header">
                <h1 className="view-title">Configuration</h1>
                <p className="view-subtitle">Agents, rules, MCP servers, and skills catalog</p>
              </div>
              <ConfigView />
            </>
          )}

          {/* ── File Viewer ── */}
          {activeView === "files" && (
            <>
              <div className="view-header">
                <h1 className="view-title">Files</h1>
                <p className="view-subtitle">Browse factory configuration files</p>
              </div>
              <FileViewerView />
            </>
          )}

          {/* ── History ── */}
          {activeView === "history" && (
            <HistoryView />
          )}

          {/* ── RepoLens Impact ── */}
          {activeView === "repolens-metrics" && (
            <RepoLensMetricsView />
          )}
        </div>
      </div>

      {/* ── Station Detail Panel ── */}
      {selectedStation && (
        <StationDetail
          station={selectedStation}
          agents={agents}
          phaseDataAttr={PHASE_DATA_ATTR[selectedStation.phase]}
          onClose={handleCloseDetail}
          onSkip={handleSkipStation}
        />
      )}
    </div>
  );
}
