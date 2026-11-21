/**
 * Foreman Monitor — Sequential stage execution tracker.
 *
 * Shows the Foreman agent monitoring the production line, one stage at a time,
 * with per-agent token consumption and time spent.
 */
import { useState, useMemo, useCallback } from "react";
import { Agent, ForemanState, AgentStageRecord, StationPhase } from "@/types";

// ───────────────────── Demo data ─────────────────────

const DEMO_FOREMAN: ForemanState = {
  status: "monitoring",
  current_stage: "implement",
  current_agent: "backend-implementer",
  stage_index: 10,
  total_stages: 16,
  started_at: new Date(Date.now() - 60000).toISOString(),
  last_event: "Agent backend-implementer started working on implement station",
};

const DEMO_AGENT_RECORDS: AgentStageRecord[] = [
  { agent_id: "sync_issues_and_code", station_id: "intake", stage: "Intake", run_id: "run-demo-001", started_at: new Date(Date.now() - 60000).toISOString(), finished_at: new Date(Date.now() - 55000).toISOString(), duration_seconds: 5, tokens_input: 1200, tokens_output: 340, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
  { agent_id: "cloud-foreman", station_id: "foreman-claim", stage: "Intake", run_id: "run-demo-001", started_at: new Date(Date.now() - 55000).toISOString(), finished_at: new Date(Date.now() - 50000).toISOString(), duration_seconds: 5, tokens_input: 800, tokens_output: 210, model: "claude-haiku-20250414", provider: "claude", status: "completed" },
  { agent_id: "openspec-planner", station_id: "spec", stage: "Plan", run_id: "run-demo-001", started_at: new Date(Date.now() - 50000).toISOString(), finished_at: new Date(Date.now() - 40000).toISOString(), duration_seconds: 10, tokens_input: 3400, tokens_output: 1200, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
  { agent_id: "java-architect", station_id: "architecture", stage: "Plan", run_id: "run-demo-001", started_at: new Date(Date.now() - 40000).toISOString(), finished_at: new Date(Date.now() - 30000).toISOString(), duration_seconds: 10, tokens_input: 2800, tokens_output: 950, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
  { agent_id: "cloud-foreman", station_id: "branch", stage: "Plan", run_id: "run-demo-001", started_at: new Date(Date.now() - 30000).toISOString(), finished_at: new Date(Date.now() - 25000).toISOString(), duration_seconds: 5, tokens_input: 600, tokens_output: 180, model: "claude-haiku-20250414", provider: "claude", status: "completed" },
  { agent_id: "testing-qa", station_id: "test-design", stage: "Prove", run_id: "run-demo-001", started_at: new Date(Date.now() - 25000).toISOString(), finished_at: new Date(Date.now() - 15000).toISOString(), duration_seconds: 10, tokens_input: 2100, tokens_output: 780, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
  { agent_id: "backend-implementer", station_id: "failing-tests", stage: "Prove", run_id: "run-demo-001", started_at: new Date(Date.now() - 15000).toISOString(), finished_at: new Date(Date.now() - 8000).toISOString(), duration_seconds: 7, tokens_input: 1800, tokens_output: 520, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
  { agent_id: "backend-implementer", station_id: "implement", stage: "Prove", run_id: "run-demo-001", started_at: new Date(Date.now() - 8000).toISOString(), tokens_input: 4200, tokens_output: 1800, model: "claude-sonnet-4-20250514", provider: "claude", status: "running" },
];

const PHASE_ORDER: StationPhase[] = ["Intake", "Plan", "Prove", "Publish", "Ship"];

// ───────────────────── Format helpers ─────────────────────

function formatDuration(seconds: number | undefined): string {
  if (seconds === undefined) return "—";
  if (seconds < 60) return `${seconds}s`;
  return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
}

function formatTokens(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1)}k`;
  return String(n);
}

function formatCost(usd: number | undefined): string {
  if (usd === undefined) return "—";
  return `$${usd.toFixed(4)}`;
}

// ───────────────────── Foreman status indicator ─────────────────────

function ForemanStatus({ foreman }: { foreman: ForemanState }) {
  const statusColor = foreman.status === "monitoring" ? "var(--color-success)" :
    foreman.status === "alerting" ? "var(--color-warning)" : "var(--color-text-muted)";

  return (
    <div className="foreman-status-card">
      <div className="foreman-avatar">
        <svg width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
          <circle cx="8" cy="5" r="3" />
          <path d="M3 14c0-2.8 2.2-5 5-5s5 2.2 5 5" strokeLinecap="round" />
          <circle cx="13" cy="3" r="2" />
        </svg>
      </div>
      <div className="foreman-info">
        <div className="foreman-label">Foreman Agent</div>
        <div className="foreman-state" style={{ color: statusColor }}>
          <span className={`status-dot ${foreman.status === "monitoring" ? "connected" : ""}`} />
          {foreman.status === "monitoring" ? "Monitoring" : foreman.status === "alerting" ? "Alerting" : "Idle"}
        </div>
      </div>
      <div className="foreman-progress">
        <div className="foreman-progress-bar">
          <div
            className="foreman-progress-fill"
            style={{ width: `${(foreman.stage_index / foreman.total_stages) * 100}%` }}
          />
        </div>
        <span className="foreman-progress-text">
          {foreman.stage_index} / {foreman.total_stages} stages
        </span>
      </div>
      <div className="foreman-current">
        <span className="foreman-current-label">Current:</span>
        <span className="foreman-current-stage">{foreman.current_stage}</span>
        <span className="foreman-current-agent">via {foreman.current_agent}</span>
      </div>
      {foreman.last_event && (
        <div className="foreman-last-event">{foreman.last_event}</div>
      )}
    </div>
  );
}

// ───────────────────── Sequential stage timeline ─────────────────────

function StageTimeline({ records }: { records: AgentStageRecord[] }) {
  const groupedByPhase = useMemo(() => {
    const map = new Map<StationPhase, AgentStageRecord[]>();
    for (const phase of PHASE_ORDER) {
      map.set(phase, []);
    }
    for (const r of records) {
      const phase = r.stage as StationPhase;
      if (map.has(phase)) {
        map.get(phase)!.push(r);
      }
    }
    return map;
  }, [records]);

  return (
    <div className="stage-timeline">
      {PHASE_ORDER.map((phase) => {
        const phaseRecords = groupedByPhase.get(phase) || [];
        if (phaseRecords.length === 0) return null;
        return (
          <div key={phase} className="timeline-phase">
            <div className="timeline-phase-header">
              <span className={`phase-indicator ${phase.toLowerCase()}`} />
              <span className="phase-name">{phase}</span>
            </div>
            <div className="timeline-stages">
              {phaseRecords.map((record, idx) => (
                <div
                  key={record.station_id}
                  className={`timeline-stage ${record.status === "running" ? "running" : ""}`}
                >
                  <div className="timeline-stage-dot">
                    {record.status === "completed" ? "✓" : record.status === "running" ? "◉" : "○"}
                  </div>
                  <div className="timeline-stage-content">
                    <div className="timeline-stage-title">
                      <span className="timeline-stage-name">{record.station_id}</span>
                      <span className="timeline-stage-agent">{record.agent_id}</span>
                    </div>
                    <div className="timeline-stage-meta">
                      <span className="timeline-stage-model">{record.model}</span>
                      <span className="timeline-stage-duration">
                        {formatDuration(record.duration_seconds)}
                      </span>
                      <span className="timeline-stage-tokens">
                        ↑{formatTokens(record.tokens_input)} ↓{formatTokens(record.tokens_output)}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}

// ───────────────────── Agent token summary table ─────────────────────

function AgentTokenSummary({ records }: { records: AgentStageRecord[] }) {
  const summary = useMemo(() => {
    const map = new Map<string, {
      agent: string;
      stages: number;
      tokens_input: number;
      tokens_output: number;
      duration: number;
      model: string;
      provider: string;
    }>();

    for (const r of records) {
      const existing = map.get(r.agent_id) || {
        agent: r.agent_id,
        stages: 0,
        tokens_input: 0,
        tokens_output: 0,
        duration: 0,
        model: r.model,
        provider: r.provider,
      };
      existing.stages += 1;
      existing.tokens_input += r.tokens_input;
      existing.tokens_output += r.tokens_output;
      existing.duration += r.duration_seconds || 0;
      map.set(r.agent_id, existing);
    }
    return Array.from(map.values()).sort((a, b) => b.tokens_input + b.tokens_output - a.tokens_input - a.tokens_output);
  }, [records]);

  const totalInput = summary.reduce((sum, s) => sum + s.tokens_input, 0);
  const totalOutput = summary.reduce((sum, s) => sum + s.tokens_output, 0);
  const totalDuration = summary.reduce((sum, s) => sum + s.duration, 0);

  return (
    <div className="agent-token-summary">
      <div className="summary-header">
        <h3>Agent Token & Time Summary</h3>
        <div className="summary-totals">
          <span>Total: ↑{formatTokens(totalInput)} ↓{formatTokens(totalOutput)} · {formatDuration(totalDuration)}</span>
        </div>
      </div>
      <table className="token-table">
        <thead>
          <tr>
            <th>Agent</th>
            <th>Stages</th>
            <th>Model</th>
            <th>Input Tokens</th>
            <th>Output Tokens</th>
            <th>Time</th>
          </tr>
        </thead>
        <tbody>
          {summary.map((s) => (
            <tr key={s.agent}>
              <td className="agent-cell">{s.agent}</td>
              <td>{s.stages}</td>
              <td className="model-cell">{s.model}</td>
              <td className="number-cell">{s.tokens_input.toLocaleString()}</td>
              <td className="number-cell">{s.tokens_output.toLocaleString()}</td>
              <td>{formatDuration(s.duration)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ───────────────────── Main view ─────────────────────

export function ForemanMonitorView() {
  const [foreman] = useState<ForemanState>(DEMO_FOREMAN);
  const [records] = useState<AgentStageRecord[]>(DEMO_AGENT_RECORDS);

  return (
    <div className="foreman-monitor-container">
      <div className="foreman-top-section">
        <ForemanStatus foreman={foreman} />
      </div>
      <div className="foreman-middle-section">
        <StageTimeline records={records} />
      </div>
      <div className="foreman-bottom-section">
        <AgentTokenSummary records={records} />
      </div>
    </div>
  );
}
