/**
 * MetricsView — Production line KPIs, Foreman monitor, and intent-based filtering.
 *
 * Shows factory health: lead time, throughput, token cost, stage durations,
 * error distribution, foreman sequential execution, and per-intent metrics.
 * Pure SVG charts — no external charting library.
 */
import { useMemo, useState, useCallback } from "react";
import {
  MetricsSnapshot,
  ForemanState,
  AgentStageRecord,
  IntentRun,
  StationPhase,
} from "@/types";

// ───────────────────── Demo data generators ─────────────────────

function generateDemoMetrics(): MetricsSnapshot {
  const now = Date.now();
  const day = 86400000;

  const tokenUsage = Array.from({ length: 14 }, (_, i) => {
    const dayAgo = new Date(now - (13 - i) * day).toISOString().slice(0, 10);
    const input = 12000 + Math.floor(Math.random() * 18000);
    const output = 4000 + Math.floor(Math.random() * 8000);
    const total = input + output;
    return {
      timestamp: dayAgo,
      inputTokens: input,
      outputTokens: output,
      totalTokens: total,
      costUsd: +(total * 0.000015).toFixed(4),
      stage: "all",
    };
  });

  const stageDurations = [
    { stage: "Intake", runs: 12, okRuns: 12, medianSeconds: 45, totalSeconds: 540 },
    { stage: "Plan", runs: 10, okRuns: 9, medianSeconds: 180, totalSeconds: 1800 },
    { stage: "Prove", runs: 8, okRuns: 7, medianSeconds: 420, totalSeconds: 3360 },
    { stage: "Publish", runs: 7, okRuns: 6, medianSeconds: 120, totalSeconds: 840 },
    { stage: "Ship", runs: 6, okRuns: 5, medianSeconds: 90, totalSeconds: 540 },
  ];

  const errorBuckets = [
    { category: "Rate limit", count: 5 },
    { category: "Auth error", count: 3 },
    { category: "Timeout", count: 2 },
    { category: "Gate failure", count: 4 },
    { category: "Test failure", count: 6 },
    { category: "Merge conflict", count: 1 },
  ];

  const leadTimes = [
    { issue: 2348, pullRequest: 2377, seconds: 7200, startAt: new Date(now - 3 * day).toISOString(), endAt: new Date(now - 3 * day + 7200000).toISOString() },
    { issue: 2350, pullRequest: 2380, seconds: 14400, startAt: new Date(now - 2 * day).toISOString(), endAt: new Date(now - 2 * day + 14400000).toISOString() },
    { issue: 2352, pullRequest: 2381, seconds: 3600, startAt: new Date(now - day).toISOString(), endAt: new Date(now - day + 3600000).toISOString() },
    { issue: 2380, pullRequest: 2383, seconds: 10800, startAt: new Date(now - 0.5 * day).toISOString(), endAt: new Date(now - 0.5 * day + 10800000).toISOString() },
  ];

  const runsByDay = Array.from({ length: 14 }, (_, i) => ({
    date: new Date(now - (13 - i) * day).toISOString().slice(0, 10),
    count: Math.floor(Math.random() * 5) + 1,
  }));

  return {
    totalRuns: 42,
    successfulRuns: 35,
    failedRuns: 7,
    avgLeadTimeSeconds: 9000,
    medianLeadTimeSeconds: 9000,
    totalTokenCost: 12.45,
    totalTokens: 830000,
    totalInputTokens: 580000,
    totalOutputTokens: 250000,
    errorRate: 16.7,
    avgTokensPerRun: 19762,
    stageDurations,
    tokenUsage,
    errorBuckets,
    leadTimes,
    runsByDay,
  };
}

function generateDemoForeman(): ForemanState {
  return {
    status: "monitoring",
    current_stage: "implement",
    current_agent: "backend-implementer",
    stage_index: 10,
    total_stages: 16,
    started_at: new Date(Date.now() - 60000).toISOString(),
    last_event: "Agent backend-implementer started working on implement station",
  };
}

function generateDemoAgentRecords(): AgentStageRecord[] {
  const now = Date.now();
  return [
    { agent_id: "sync_issues_and_code", station_id: "intake", stage: "Intake", run_id: "run-demo-001", started_at: new Date(now - 60000).toISOString(), finished_at: new Date(now - 55000).toISOString(), duration_seconds: 5, tokens_input: 1200, tokens_output: 340, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
    { agent_id: "cloud-foreman", station_id: "foreman-claim", stage: "Intake", run_id: "run-demo-001", started_at: new Date(now - 55000).toISOString(), finished_at: new Date(now - 50000).toISOString(), duration_seconds: 5, tokens_input: 800, tokens_output: 210, model: "claude-haiku-20250414", provider: "claude", status: "completed" },
    { agent_id: "openspec-planner", station_id: "spec", stage: "Plan", run_id: "run-demo-001", started_at: new Date(now - 50000).toISOString(), finished_at: new Date(now - 40000).toISOString(), duration_seconds: 10, tokens_input: 3400, tokens_output: 1200, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
    { agent_id: "java-architect", station_id: "architecture", stage: "Plan", run_id: "run-demo-001", started_at: new Date(now - 40000).toISOString(), finished_at: new Date(now - 30000).toISOString(), duration_seconds: 10, tokens_input: 2800, tokens_output: 950, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
    { agent_id: "cloud-foreman", station_id: "branch", stage: "Plan", run_id: "run-demo-001", started_at: new Date(now - 30000).toISOString(), finished_at: new Date(now - 25000).toISOString(), duration_seconds: 5, tokens_input: 600, tokens_output: 180, model: "claude-haiku-20250414", provider: "claude", status: "completed" },
    { agent_id: "testing-qa", station_id: "test-design", stage: "Prove", run_id: "run-demo-001", started_at: new Date(now - 25000).toISOString(), finished_at: new Date(now - 15000).toISOString(), duration_seconds: 10, tokens_input: 2100, tokens_output: 780, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
    { agent_id: "backend-implementer", station_id: "failing-tests", stage: "Prove", run_id: "run-demo-001", started_at: new Date(now - 15000).toISOString(), finished_at: new Date(now - 8000).toISOString(), duration_seconds: 7, tokens_input: 1800, tokens_output: 520, model: "claude-sonnet-4-20250514", provider: "claude", status: "completed" },
    { agent_id: "backend-implementer", station_id: "implement", stage: "Prove", run_id: "run-demo-001", started_at: new Date(now - 8000).toISOString(), tokens_input: 4200, tokens_output: 1800, model: "claude-sonnet-4-20250514", provider: "claude", status: "running" },
  ];
}

function generateDemoIntentRuns(): IntentRun[] {
  const now = Date.now();
  const day = 86400000;

  const baseRecords = generateDemoAgentRecords();

  return [
    {
      run_id: "run-demo-001",
      intent_id: "#2380",
      intent_title: "CLI comparison experiments & factory dashboard",
      intent_source: "github",
      status: "running",
      started_at: new Date(now - 60000).toISOString(),
      total_tokens: 21930,
      input_tokens: 16900,
      output_tokens: 5030,
      total_cost_usd: 0.329,
      stages_completed: 7,
      stages_total: 16,
      stages_failed: 0,
      agent_records: baseRecords,
      configuration_used: {
        agents: ["cloud-foreman", "backend-implementer", "openspec-planner", "java-architect", "testing-qa"],
        rules: ["hexagonal-architecture", "programming", "code-quality"],
        skills: ["hexagonal-arch", "backend", "java", "openspec-propose"],
        mcp_servers: ["repolens", "factory"],
        models: ["claude-sonnet-4-20250514", "claude-haiku-20250414"],
      },
      outcome: { branch: "feat/2381_factory_dashboard", commits: 3, files_changed: 26 },
      errors: [{ category: "Rate limit", count: 1 }],
    },
    {
      run_id: "run-demo-002",
      intent_id: "#2348",
      intent_title: "Extract business.* JPA entities into per-slice adapters",
      intent_source: "github",
      status: "succeeded",
      started_at: new Date(now - 3 * day).toISOString(),
      finished_at: new Date(now - 3 * day + 7200000).toISOString(),
      lead_time_seconds: 7200,
      total_tokens: 45200,
      input_tokens: 32000,
      output_tokens: 13200,
      total_cost_usd: 0.678,
      stages_completed: 16,
      stages_total: 16,
      stages_failed: 0,
      agent_records: baseRecords.map((r, i) => ({ ...r, run_id: "run-demo-002", tokens_input: r.tokens_input * 2 })),
      configuration_used: {
        agents: ["cloud-foreman", "backend-implementer", "java-architect"],
        rules: ["hexagonal-architecture", "programming"],
        skills: ["hexagonal-arch", "java", "backend"],
        mcp_servers: ["repolens"],
        models: ["claude-sonnet-4-20250514"],
      },
      outcome: { pull_request: 2377, branch: "fix/2348_extract_entities", commits: 5, files_changed: 42 },
    },
    {
      run_id: "run-demo-003",
      intent_id: "#2350",
      intent_title: "Retire top-level dto/ package",
      intent_source: "github",
      status: "succeeded",
      started_at: new Date(now - 2 * day).toISOString(),
      finished_at: new Date(now - 2 * day + 14400000).toISOString(),
      lead_time_seconds: 14400,
      total_tokens: 38100,
      input_tokens: 27000,
      output_tokens: 11100,
      total_cost_usd: 0.572,
      stages_completed: 16,
      stages_total: 16,
      stages_failed: 1,
      agent_records: baseRecords.map((r) => ({ ...r, run_id: "run-demo-003" })),
      configuration_used: {
        agents: ["cloud-foreman", "backend-implementer"],
        rules: ["programming", "code-quality"],
        skills: ["backend", "java"],
        mcp_servers: ["repolens"],
        models: ["claude-sonnet-4-20250514"],
      },
      outcome: { pull_request: 2380, branch: "refactor/2350_retire_dto", commits: 4, files_changed: 31 },
      errors: [{ category: "Test failure", count: 2 }, { category: "Gate failure", count: 1 }],
    },
    {
      run_id: "run-demo-004",
      intent_id: "#2352",
      intent_title: "Complete pure domain models for remaining aggregates",
      intent_source: "github",
      status: "failed",
      started_at: new Date(now - day).toISOString(),
      finished_at: new Date(now - day + 3600000).toISOString(),
      lead_time_seconds: 3600,
      total_tokens: 12400,
      input_tokens: 9000,
      output_tokens: 3400,
      total_cost_usd: 0.186,
      stages_completed: 5,
      stages_total: 16,
      stages_failed: 2,
      agent_records: baseRecords.slice(0, 3).map((r) => ({ ...r, run_id: "run-demo-004" })),
      configuration_used: {
        agents: ["cloud-foreman", "backend-implementer"],
        rules: ["hexagonal-architecture"],
        skills: ["hexagonal-arch"],
        mcp_servers: ["repolens"],
        models: ["claude-sonnet-4-20250514"],
      },
      errors: [{ category: "Rate limit", count: 3 }, { category: "Test failure", count: 4 }],
    },
  ];
}

// ───────────────────── Formatting helpers ─────────────────────

function formatDuration(seconds: number | undefined): string {
  if (seconds === undefined) return "—";
  if (seconds < 60) return `${Math.round(seconds)}s`;
  if (seconds < 3600) return `${Math.round(seconds / 60)}m`;
  const h = Math.floor(seconds / 3600);
  const m = Math.round((seconds % 3600) / 60);
  return m > 0 ? `${h}h ${m}m` : `${h}h`;
}

function formatTokens(count: number): string {
  if (count >= 1_000_000) return `${(count / 1_000_000).toFixed(1)}M`;
  if (count >= 1_000) return `${(count / 1_000).toFixed(0)}K`;
  return String(count);
}

function formatCost(usd: number): string {
  return `$${usd.toFixed(2)}`;
}

// ───────────────────── KPI Card ─────────────────────

interface KPICardProps {
  label: string;
  value: string;
  subtitle?: string;
  trend?: "up" | "down" | "neutral";
  color: string;
}

function KPICard({ label, value, subtitle, trend, color }: KPICardProps) {
  return (
    <div className="kpi-card" style={{ borderTop: `3px solid ${color}` }}>
      <div className="kpi-label">{label}</div>
      <div className="kpi-value">{value}</div>
      {subtitle && (
        <div className="kpi-subtitle">
          {trend === "up" && "↑ "}
          {trend === "down" && "↓ "}
          {subtitle}
        </div>
      )}
    </div>
  );
}

// ───────────────────── SVG Bar Chart (Stage Durations) ─────────────────────

function BarChart({ data, title, unit }: { data: { label: string; value: number; color: string }[]; title: string; unit: string }) {
  const maxVal = Math.max(...data.map((d) => d.value), 1);
  const barHeight = 28;
  const gap = 12;
  const labelWidth = 70;
  const valueWidth = 50;
  const chartHeight = data.length * (barHeight + gap);
  const chartWidth = 500;
  const barArea = chartWidth - labelWidth - valueWidth - 20;

  return (
    <div className="chart-card">
      <h3 className="chart-title">{title}</h3>
      <svg
        viewBox={`0 0 ${chartWidth} ${chartHeight}`}
        className="chart-svg"
        role="img"
        aria-label={title}
      >
        {data.map((d, i) => {
          const y = i * (barHeight + gap) + 4;
          const barW = (d.value / maxVal) * barArea;
          return (
            <g key={d.label}>
              <text x="0" y={y + barHeight / 2 + 4} className="chart-label" textAnchor="start">
                {d.label}
              </text>
              <rect x={labelWidth} y={y} width={barArea} height={barHeight} rx={4} className="chart-bar-bg" />
              <rect x={labelWidth} y={y} width={barW} height={barHeight} rx={4} fill={d.color} className="chart-bar" />
              <text x={labelWidth + barW + 6} y={y + barHeight / 2 + 4} className="chart-value" textAnchor="start">
                {d.value}{unit}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

// ───────────────────── SVG Line Chart ─────────────────────

function LineChart({ data, title, unit, color }: { data: { label: string; value: number }[]; title: string; unit: string; color: string }) {
  if (data.length === 0) return null;

  const width = 500;
  const height = 180;
  const padding = { top: 20, right: 20, bottom: 30, left: 50 };
  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;

  const maxVal = Math.max(...data.map((d) => d.value), 1);
  const minVal = Math.min(...data.map((d) => d.value), 0);
  const range = maxVal - minVal || 1;

  const points = data.map((d, i) => ({
    x: padding.left + (i / Math.max(data.length - 1, 1)) * chartW,
    y: padding.top + chartH - ((d.value - minVal) / range) * chartH,
    label: d.label,
    value: d.value,
  }));

  const pathD = points.map((p, i) => `${i === 0 ? "M" : "L"}${p.x},${p.y}`).join(" ");
  const areaD = `${pathD} L${points[points.length - 1].x},${padding.top + chartH} L${points[0].x},${padding.top + chartH} Z`;

  const gridLines = 4;
  const gridSteps = Array.from({ length: gridLines }, (_, i) => {
    const val = minVal + (range * i) / (gridLines - 1);
    const y = padding.top + chartH - (i / (gridLines - 1)) * chartH;
    return { val, y };
  });

  return (
    <div className="chart-card">
      <h3 className="chart-title">{title}</h3>
      <svg viewBox={`0 0 ${width} ${height}`} className="chart-svg" role="img" aria-label={title}>
        {gridSteps.map((g, i) => (
          <g key={i}>
            <line x1={padding.left} y1={g.y} x2={width - padding.right} y2={g.y} className="chart-grid-line" />
            <text x={padding.left - 6} y={g.y + 4} className="chart-axis-label" textAnchor="end">
              {unit === "$" ? formatCost(g.val) : formatTokens(Math.round(g.val))}
            </text>
          </g>
        ))}
        <path d={areaD} fill={color} opacity={0.08} />
        <path d={pathD} fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
        {points.map((p, i) => (
          <g key={i}>
            <circle cx={p.x} cy={p.y} r={3} fill={color} />
            <text x={p.x} y={height - 8} className="chart-x-label" textAnchor="middle">
              {p.label.slice(5)}
            </text>
          </g>
        ))}
      </svg>
    </div>
  );
}

// ───────────────────── Donut Chart ─────────────────────

function DonutChart({ data, title }: { data: { label: string; value: number; color: string }[]; title: string }) {
  const total = data.reduce((s, d) => s + d.value, 0);
  if (total === 0) return null;

  const size = 160;
  const cx = size / 2;
  const cy = size / 2;
  const radius = 60;
  const innerRadius = 40;

  let cumulative = 0;
  const slices = data.map((d) => {
    const startAngle = (cumulative / total) * 2 * Math.PI - Math.PI / 2;
    cumulative += d.value;
    const endAngle = (cumulative / total) * 2 * Math.PI - Math.PI / 2;
    const largeArc = d.value / total > 0.5 ? 1 : 0;
    const x1 = cx + radius * Math.cos(startAngle);
    const y1 = cy + radius * Math.sin(startAngle);
    const x2 = cx + radius * Math.cos(endAngle);
    const y2 = cy + radius * Math.sin(endAngle);
    const ix1 = cx + innerRadius * Math.cos(startAngle);
    const iy1 = cy + innerRadius * Math.sin(startAngle);
    const ix2 = cx + innerRadius * Math.cos(endAngle);
    const iy2 = cy + innerRadius * Math.sin(endAngle);
    const path = `M${x1},${y1} A${radius},${radius} 0 ${largeArc},1 ${x2},${y2} L${ix2},${iy2} A${innerRadius},${innerRadius} 0 ${largeArc},0 ${ix1},${iy1} Z`;
    return { ...d, path };
  });

  return (
    <div className="chart-card">
      <h3 className="chart-title">{title}</h3>
      <div className="donut-container">
        <svg viewBox={`0 0 ${size} ${size}`} className="donut-svg" role="img" aria-label={title}>
          {slices.map((s, i) => (
            <path key={i} d={s.path} fill={s.color} className="donut-slice" />
          ))}
          <text x={cx} y={cy - 6} className="donut-center-value" textAnchor="middle">{total}</text>
          <text x={cx} y={cy + 10} className="donut-center-label" textAnchor="middle">errors</text>
        </svg>
        <div className="donut-legend">
          {slices.map((s, i) => (
            <div key={i} className="donut-legend-item">
              <span className="donut-legend-dot" style={{ background: s.color }} />
              <span className="donut-legend-label">{s.label}</span>
              <span className="donut-legend-value">{s.value}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ───────────────────── Throughput Bar Chart ─────────────────────

function ThroughputChart({ data, title }: { data: { label: string; value: number }[]; title: string }) {
  const maxVal = Math.max(...data.map((d) => d.value), 1);
  const width = 500;
  const height = 160;
  const padding = { top: 10, right: 10, bottom: 30, left: 30 };
  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;
  const barW = Math.min(24, (chartW / data.length) * 0.7);
  const gap = chartW / data.length;

  return (
    <div className="chart-card">
      <h3 className="chart-title">{title}</h3>
      <svg viewBox={`0 0 ${width} ${height}`} className="chart-svg" role="img" aria-label={title}>
        {[0, 0.25, 0.5, 0.75, 1].map((frac, i) => {
          const y = padding.top + chartH * (1 - frac);
          const val = Math.round(maxVal * frac);
          return (
            <g key={i}>
              <line x1={padding.left} y1={y} x2={width - padding.right} y2={y} className="chart-grid-line" />
              <text x={padding.left - 6} y={y + 4} className="chart-axis-label" textAnchor="end">{val}</text>
            </g>
          );
        })}
        {data.map((d, i) => {
          const x = padding.left + i * gap + (gap - barW) / 2;
          const barH = (d.value / maxVal) * chartH;
          const y = padding.top + chartH - barH;
          return (
            <g key={d.label}>
              <rect x={x} y={y} width={barW} height={barH} rx={3} fill="var(--phase-intake)" opacity={0.7} className="throughput-bar" />
              <text x={x + barW / 2} y={height - 8} className="chart-x-label" textAnchor="middle">
                {d.label.slice(5)}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

// ───────────────────── Lead Time Table ─────────────────────

function LeadTimeTable({ data }: { data: { issue: number; pr: number; seconds: number }[] }) {
  return (
    <div className="chart-card">
      <h3 className="chart-title">Lead Time — Issue to PR Green</h3>
      <table className="metrics-table">
        <thead>
          <tr><th>Issue</th><th>PR</th><th>Lead Time</th></tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr key={row.issue}>
              <td className="mono">#{row.issue}</td>
              <td className="mono">#{row.pr}</td>
              <td className="mono">{formatDuration(row.seconds)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ───────────────────── Foreman Status Indicator ─────────────────────

function ForemanStatus({ foreman }: { foreman: ForemanState }) {
  const statusColor = foreman.status === "monitoring"
    ? "var(--color-success)"
    : foreman.status === "alerting"
      ? "var(--color-warning)"
      : "var(--color-text-muted)";

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
          <div className="foreman-progress-fill" style={{ width: `${(foreman.stage_index / foreman.total_stages) * 100}%` }} />
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

// ───────────────────── Sequential Stage Timeline ─────────────────────

const PHASE_ORDER: StationPhase[] = ["Intake", "Plan", "Prove", "Publish", "Ship"];

function StageTimeline({ records }: { records: AgentStageRecord[] }) {
  const groupedByPhase = useMemo(() => {
    const map = new Map<StationPhase, AgentStageRecord[]>();
    for (const phase of PHASE_ORDER) map.set(phase, []);
    for (const r of records) {
      const phase = r.stage as StationPhase;
      if (map.has(phase)) map.get(phase)!.push(r);
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
              {phaseRecords.map((record) => (
                <div key={record.station_id} className={`timeline-stage ${record.status === "running" ? "running" : ""}`}>
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
                      <span className="timeline-stage-duration">{formatDuration(record.duration_seconds)}</span>
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

// ───────────────────── Agent Token Summary Table ─────────────────────

function AgentTokenSummary({ records }: { records: AgentStageRecord[] }) {
  const summary = useMemo(() => {
    const map = new Map<string, {
      agent: string; stages: number; tokens_input: number; tokens_output: number; duration: number; model: string;
    }>();
    for (const r of records) {
      const existing = map.get(r.agent_id) || { agent: r.agent_id, stages: 0, tokens_input: 0, tokens_output: 0, duration: 0, model: r.model };
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
          <tr><th>Agent</th><th>Stages</th><th>Model</th><th>Input Tokens</th><th>Output Tokens</th><th>Time</th></tr>
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

// ───────────────────── Intent Filter Bar ─────────────────────

function IntentFilter({
  runs,
  selectedRunId,
  onSelect,
}: {
  runs: IntentRun[];
  selectedRunId: string | null;
  onSelect: (runId: string | null) => void;
}) {
  const [searchQuery, setSearchQuery] = useState("");

  const filteredRuns = useMemo(() => {
    if (!searchQuery.trim()) return runs;
    const q = searchQuery.toLowerCase().trim();
    return runs.filter((run) => {
      const idMatch = run.intent_id.toLowerCase().includes(q);
      const titleMatch = run.intent_title.toLowerCase().includes(q);
      const sourceMatch = run.intent_source.toLowerCase().includes(q);
      const runIdMatch = run.run_id.toLowerCase().includes(q);
      return idMatch || titleMatch || sourceMatch || runIdMatch;
    });
  }, [runs, searchQuery]);

  const handleSelect = useCallback((runId: string | null) => {
    onSelect(runId);
    if (runId === null) {
      setSearchQuery("");
    }
  }, [onSelect]);

  const handleClear = useCallback(() => {
    setSearchQuery("");
    onSelect(null);
  }, [onSelect]);

  return (
    <div className="intent-filter-bar">
      <span className="intent-filter-label">Filter by Intent</span>
      <div className="intent-filter-controls">
        <div className="intent-search-wrapper">
          <svg className="intent-search-icon" width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
            <circle cx="7" cy="7" r="4" />
            <path d="M10 10l3 3" strokeLinecap="round" />
          </svg>
          <input
            type="text"
            className="intent-search-input"
            placeholder="Search by issue #, title, or source…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            aria-label="Search intents"
          />
          {searchQuery && (
            <button
              className="intent-search-clear"
              onClick={handleClear}
              aria-label="Clear search"
              title="Clear search"
            >
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 4l8 8M12 4l-8 8" strokeLinecap="round" />
              </svg>
            </button>
          )}
        </div>
        {searchQuery && (
          <span className="intent-search-count">
            {filteredRuns.length} of {runs.length} intents
          </span>
        )}
      </div>
      <div className="intent-filter-buttons">
        <button
          className={`intent-filter-btn ${selectedRunId === null ? "active" : ""}`}
          onClick={() => handleSelect(null)}
        >
          All Intents
        </button>
        {filteredRuns.map((run) => (
          <button
            key={run.run_id}
            className={`intent-filter-btn ${selectedRunId === run.run_id ? "active" : ""}`}
            onClick={() => handleSelect(run.run_id)}
          >
            <span className={`intent-source-badge ${run.intent_source}`}>{run.intent_source}</span>
            <span className="intent-filter-title">{run.intent_id}</span>
            <span className="intent-filter-status">
              {run.intent_title}
            </span>
            <span className={`intent-filter-status ${run.status}`}>
              {run.status === "running" ? "◉" : run.status === "succeeded" ? "✓" : run.status === "failed" ? "✗" : "○"}
            </span>
          </button>
        ))}
        {searchQuery && filteredRuns.length === 0 && (
          <div className="intent-search-empty">
            No intents matching "{searchQuery}"
          </div>
        )}
      </div>
    </div>
  );
}

// ───────────────────── Intent Detail View ─────────────────────

function IntentDetailView({ run }: { run: IntentRun }) {
  const [tab, setTab] = useState<"overview" | "stages" | "agents" | "config">("overview");

  const statusColor = run.status === "succeeded"
    ? "var(--status-done)"
    : run.status === "running"
      ? "var(--phase-prove)"
      : run.status === "failed"
        ? "var(--status-error)"
        : "var(--color-text-muted)";

  return (
    <div className="intent-detail-view">
      {/* Intent header */}
      <div className="intent-detail-header">
        <div className="intent-detail-title">
          <span className={`intent-source-badge ${run.intent_source}`}>{run.intent_source}</span>
          <span className="intent-detail-id">{run.intent_id}</span>
          <span className="intent-detail-status" style={{ color: statusColor }}>
            <span className={`status-dot ${run.status === "running" ? "connected" : ""}`} />
            {run.status}
          </span>
        </div>
        <div className="intent-detail-subtitle">{run.intent_title}</div>
        <div className="intent-detail-run-id">{run.run_id}</div>
      </div>

      {/* Intent KPIs */}
      <div className="intent-kpi-row">
        <div className="intent-kpi-card">
          <div className="intent-kpi-label">Lead Time</div>
          <div className="intent-kpi-value">{formatDuration(run.lead_time_seconds)}</div>
        </div>
        <div className="intent-kpi-card">
          <div className="intent-kpi-label">Token Cost</div>
          <div className="intent-kpi-value">{formatCost(run.total_cost_usd)}</div>
          <div className="intent-kpi-sub">{formatTokens(run.total_tokens)} tokens</div>
        </div>
        <div className="intent-kpi-card">
          <div className="intent-kpi-label">Stages</div>
          <div className="intent-kpi-value">{run.stages_completed}/{run.stages_total}</div>
          {run.stages_failed > 0 && (
            <div className="intent-kpi-sub" style={{ color: "var(--status-error)" }}>{run.stages_failed} failed</div>
          )}
        </div>
        <div className="intent-kpi-card">
          <div className="intent-kpi-label">Tokens</div>
          <div className="intent-kpi-value">↑{formatTokens(run.input_tokens)}</div>
          <div className="intent-kpi-sub">↓{formatTokens(run.output_tokens)}</div>
        </div>
        {run.outcome?.pull_request && (
          <div className="intent-kpi-card">
            <div className="intent-kpi-label">Outcome</div>
            <div className="intent-kpi-value">PR #{run.outcome.pull_request}</div>
            <div className="intent-kpi-sub">{run.outcome.commits} commits, {run.outcome.files_changed} files</div>
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="intent-detail-tabs">
        {(["overview", "stages", "agents", "config"] as const).map((t) => (
          <button
            key={t}
            className={`intent-detail-tab ${tab === t ? "active" : ""}`}
            onClick={() => setTab(t)}
          >
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {/* Tab content */}
      <div className="intent-detail-content">
        {tab === "overview" && (
          <div className="intent-overview">
            {run.outcome && (
              <div className="intent-outcome-section">
                <h4>Outcome</h4>
                <div className="intent-outcome-grid">
                  {run.outcome.pull_request && (
                    <div className="intent-outcome-item">
                      <span className="intent-outcome-label">Pull Request</span>
                      <span className="intent-outcome-value">#{run.outcome.pull_request}</span>
                    </div>
                  )}
                  {run.outcome.branch && (
                    <div className="intent-outcome-item">
                      <span className="intent-outcome-label">Branch</span>
                      <span className="intent-outcome-value mono">{run.outcome.branch}</span>
                    </div>
                  )}
                  {run.outcome.commits !== undefined && (
                    <div className="intent-outcome-item">
                      <span className="intent-outcome-label">Commits</span>
                      <span className="intent-outcome-value">{run.outcome.commits}</span>
                    </div>
                  )}
                  {run.outcome.files_changed !== undefined && (
                    <div className="intent-outcome-item">
                      <span className="intent-outcome-label">Files Changed</span>
                      <span className="intent-outcome-value">{run.outcome.files_changed}</span>
                    </div>
                  )}
                </div>
              </div>
            )}
            {run.errors && run.errors.length > 0 && (
              <div className="intent-errors-section">
                <h4>Errors</h4>
                <div className="intent-errors-list">
                  {run.errors.map((e) => (
                    <div key={e.category} className="intent-error-item">
                      <span className="intent-error-category">{e.category}</span>
                      <span className="intent-error-count">{e.count}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="intent-timeline-section">
              <h4>Timeline</h4>
              <div className="intent-timeline">
                <div className="intent-timeline-item">
                  <span className="intent-timeline-label">Started</span>
                  <span className="intent-timeline-value">{new Date(run.started_at).toLocaleString()}</span>
                </div>
                {run.finished_at && (
                  <div className="intent-timeline-item">
                    <span className="intent-timeline-label">Finished</span>
                    <span className="intent-timeline-value">{new Date(run.finished_at).toLocaleString()}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {tab === "stages" && (
          <StageTimeline records={run.agent_records} />
        )}

        {tab === "agents" && (
          <AgentTokenSummary records={run.agent_records} />
        )}

        {tab === "config" && (
          <div className="intent-config-section">
            <div className="intent-config-grid">
              <div className="intent-config-card">
                <h4>Agents</h4>
                <div className="intent-config-list">
                  {run.configuration_used.agents.map((a) => (
                    <span key={a} className="intent-config-tag">{a}</span>
                  ))}
                </div>
              </div>
              <div className="intent-config-card">
                <h4>Rules</h4>
                <div className="intent-config-list">
                  {run.configuration_used.rules.map((r) => (
                    <span key={r} className="intent-config-tag">{r}</span>
                  ))}
                </div>
              </div>
              <div className="intent-config-card">
                <h4>Skills</h4>
                <div className="intent-config-list">
                  {run.configuration_used.skills.map((s) => (
                    <span key={s} className="intent-config-tag">{s}</span>
                  ))}
                </div>
              </div>
              <div className="intent-config-card">
                <h4>MCP Servers</h4>
                <div className="intent-config-list">
                  {run.configuration_used.mcp_servers.map((m) => (
                    <span key={m} className="intent-config-tag">{m}</span>
                  ))}
                </div>
              </div>
              <div className="intent-config-card">
                <h4>Models</h4>
                <div className="intent-config-list">
                  {run.configuration_used.models.map((m) => (
                    <span key={m} className="intent-config-tag mono">{m}</span>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

// ───────────────────── Main Metrics View ─────────────────────

const PHASE_COLORS: Record<string, string> = {
  Intake: "var(--phase-intake)",
  Plan: "var(--phase-plan)",
  Prove: "var(--phase-prove)",
  Publish: "var(--phase-publish)",
  Ship: "var(--phase-ship)",
};

const ERROR_COLORS = [
  "var(--phase-intake)",
  "var(--phase-plan)",
  "var(--phase-prove)",
  "var(--phase-publish)",
  "var(--phase-ship)",
  "var(--status-error)",
];

export function MetricsView() {
  const metrics = useMemo(() => generateDemoMetrics(), []);
  const foreman = useMemo(() => generateDemoForeman(), []);
  const intentRuns = useMemo(() => generateDemoIntentRuns(), []);
  const [selectedRunId, setSelectedRunId] = useState<string | null>(null);

  const selectedRun = useMemo(
    () => intentRuns.find((r) => r.run_id === selectedRunId) || null,
    [intentRuns, selectedRunId]
  );

  const handleSelectRun = useCallback((runId: string | null) => {
    setSelectedRunId(runId);
  }, []);

  // Chart data
  const stageBarData = metrics.stageDurations.map((s) => ({
    label: s.stage,
    value: s.medianSeconds,
    color: PHASE_COLORS[s.stage] || "var(--text-faint)",
  }));

  const tokenCostData = metrics.tokenUsage.map((t) => ({ label: t.timestamp, value: t.costUsd }));
  const tokenVolumeData = metrics.tokenUsage.map((t) => ({ label: t.timestamp, value: t.totalTokens }));

  const errorDonutData = metrics.errorBuckets.map((e, i) => ({
    label: e.category,
    value: e.count,
    color: ERROR_COLORS[i % ERROR_COLORS.length],
  }));

  const throughputData = metrics.runsByDay.map((r) => ({ label: r.date, value: r.count }));
  const leadTimeRows = metrics.leadTimes.map((l) => ({ issue: l.issue, pr: l.pullRequest, seconds: l.seconds }));

  return (
    <div className="metrics-view">
      {/* Intent Filter */}
      <IntentFilter runs={intentRuns} selectedRunId={selectedRunId} onSelect={handleSelectRun} />

      {/* Per-Intent Detail View */}
      {selectedRun && <IntentDetailView run={selectedRun} />}

      {/* Global Metrics (shown when no intent selected) */}
      {!selectedRun && (
        <>
          {/* KPI Row */}
          <div className="kpi-row">
            <KPICard label="Lead Time (median)" value={formatDuration(metrics.medianLeadTimeSeconds)} subtitle="Issue → PR green" trend="down" color="var(--phase-intake)" />
            <KPICard label="Throughput" value={`${metrics.totalRuns} runs`} subtitle={`${metrics.successfulRuns} succeeded, ${metrics.failedRuns} failed`} color="var(--phase-prove)" />
            <KPICard label="Token Cost" value={formatCost(metrics.totalTokenCost)} subtitle={`${formatTokens(metrics.totalTokens)} tokens total`} trend="up" color="var(--phase-plan)" />
            <KPICard label="Error Rate" value={`${metrics.errorRate}%`} subtitle={`${formatTokens(metrics.avgTokensPerRun)} tokens/run avg`} trend={metrics.errorRate < 20 ? "down" : "up"} color="var(--status-error)" />
            <KPICard label="Success Rate" value={`${((metrics.successfulRuns / metrics.totalRuns) * 100).toFixed(0)}%`} subtitle="Runs completed without error" trend="down" color="var(--status-done)" />
          </div>

          {/* Foreman Monitor */}
          <div className="foreman-top-section">
            <ForemanStatus foreman={foreman} />
          </div>

          {/* Charts Grid */}
          <div className="charts-grid">
            <BarChart data={stageBarData} title="Stage Duration (median)" unit="s" />
            <LineChart data={tokenCostData} title="Token Cost Trend" unit="$" color="var(--phase-plan)" />
            <LineChart data={tokenVolumeData} title="Token Volume Trend" unit="" color="var(--phase-intake)" />
            <ThroughputChart data={throughputData} title="Runs per Day" />
            <DonutChart data={errorDonutData} title="Error Distribution" />
            <LeadTimeTable data={leadTimeRows} />
          </div>
        </>
      )}
    </div>
  );
}
