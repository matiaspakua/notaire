/**
 * MetricsView — Production line KPIs and charts.
 *
 * Shows factory health: lead time, throughput, token cost, stage durations,
 * error distribution, and agent utilization. Pure SVG charts — no external
 * charting library.
 */
import { useMemo } from "react";
import { MetricsSnapshot } from "@/types";

// ───────────────────── Demo metrics data ─────────────────────

function generateDemoMetrics(): MetricsSnapshot {
  const now = Date.now();
  const day = 86400000;

  // Token usage over the last 14 days
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

  // Stage durations (median seconds per stage)
  const stageDurations = [
    { stage: "Intake", runs: 12, okRuns: 12, medianSeconds: 45, totalSeconds: 540 },
    { stage: "Plan", runs: 10, okRuns: 9, medianSeconds: 180, totalSeconds: 1800 },
    { stage: "Prove", runs: 8, okRuns: 7, medianSeconds: 420, totalSeconds: 3360 },
    { stage: "Publish", runs: 7, okRuns: 6, medianSeconds: 120, totalSeconds: 840 },
    { stage: "Ship", runs: 6, okRuns: 5, medianSeconds: 90, totalSeconds: 540 },
  ];

  // Error distribution
  const errorBuckets = [
    { category: "Rate limit", count: 5 },
    { category: "Auth error", count: 3 },
    { category: "Timeout", count: 2 },
    { category: "Gate failure", count: 4 },
    { category: "Test failure", count: 6 },
    { category: "Merge conflict", count: 1 },
  ];

  // Lead times (seconds from issue to PR green)
  const leadTimes = [
    { issue: 2348, pullRequest: 2377, seconds: 7200, startAt: new Date(now - 3 * day).toISOString(), endAt: new Date(now - 3 * day + 7200000).toISOString() },
    { issue: 2350, pullRequest: 2380, seconds: 14400, startAt: new Date(now - 2 * day).toISOString(), endAt: new Date(now - 2 * day + 14400000).toISOString() },
    { issue: 2352, pullRequest: 2381, seconds: 3600, startAt: new Date(now - day).toISOString(), endAt: new Date(now - day + 3600000).toISOString() },
    { issue: 2380, pullRequest: 2383, seconds: 10800, startAt: new Date(now - 0.5 * day).toISOString(), endAt: new Date(now - 0.5 * day + 10800000).toISOString() },
  ];

  // Runs by day (throughput)
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

// ───────────────────── Formatting helpers ─────────────────────

function formatDuration(seconds: number): string {
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

interface BarChartProps {
  data: { label: string; value: number; color: string }[];
  title: string;
  unit: string;
}

function BarChart({ data, title, unit }: BarChartProps) {
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
              <rect
                x={labelWidth}
                y={y}
                width={barArea}
                height={barHeight}
                rx={4}
                className="chart-bar-bg"
              />
              <rect
                x={labelWidth}
                y={y}
                width={barW}
                height={barHeight}
                rx={4}
                fill={d.color}
                className="chart-bar"
              />
              <text
                x={labelWidth + barW + 6}
                y={y + barHeight / 2 + 4}
                className="chart-value"
                textAnchor="start"
              >
                {d.value}{unit}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

// ───────────────────── SVG Line Chart (Token Cost Trend) ─────────────────────

interface LineChartProps {
  data: { label: string; value: number }[];
  title: string;
  unit: string;
  color: string;
}

function LineChart({ data, title, unit, color }: LineChartProps) {
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

  // Grid lines
  const gridLines = 4;
  const gridSteps = Array.from({ length: gridLines }, (_, i) => {
    const val = minVal + (range * i) / (gridLines - 1);
    const y = padding.top + chartH - (i / (gridLines - 1)) * chartH;
    return { val, y };
  });

  return (
    <div className="chart-card">
      <h3 className="chart-title">{title}</h3>
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="chart-svg"
        role="img"
        aria-label={title}
      >
        {/* Grid */}
        {gridSteps.map((g, i) => (
          <g key={i}>
            <line
              x1={padding.left}
              y1={g.y}
              x2={width - padding.right}
              y2={g.y}
              className="chart-grid-line"
            />
            <text x={padding.left - 6} y={g.y + 4} className="chart-axis-label" textAnchor="end">
              {unit === "$" ? formatCost(g.val) : formatTokens(Math.round(g.val))}
            </text>
          </g>
        ))}
        {/* Area fill */}
        <path d={areaD} fill={color} opacity={0.08} />
        {/* Line */}
        <path d={pathD} fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
        {/* Dots */}
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

// ───────────────────── Donut Chart (Error Distribution) ─────────────────────

interface DonutChartProps {
  data: { label: string; value: number; color: string }[];
  title: string;
}

function DonutChart({ data, title }: DonutChartProps) {
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
          <text x={cx} y={cy - 6} className="donut-center-value" textAnchor="middle">
            {total}
          </text>
          <text x={cx} y={cy + 10} className="donut-center-label" textAnchor="middle">
            errors
          </text>
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
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="chart-svg"
        role="img"
        aria-label={title}
      >
        {/* Grid */}
        {[0, 0.25, 0.5, 0.75, 1].map((frac, i) => {
          const y = padding.top + chartH * (1 - frac);
          const val = Math.round(maxVal * frac);
          return (
            <g key={i}>
              <line x1={padding.left} y1={y} x2={width - padding.right} y2={y} className="chart-grid-line" />
              <text x={padding.left - 6} y={y + 4} className="chart-axis-label" textAnchor="end">
                {val}
              </text>
            </g>
          );
        })}
        {/* Bars */}
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
          <tr>
            <th>Issue</th>
            <th>PR</th>
            <th>Lead Time</th>
          </tr>
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

  const stageBarData = metrics.stageDurations.map((s) => ({
    label: s.stage,
    value: s.medianSeconds,
    color: PHASE_COLORS[s.stage] || "var(--text-faint)",
  }));

  const tokenCostData = metrics.tokenUsage.map((t) => ({
    label: t.timestamp,
    value: t.costUsd,
  }));

  const tokenVolumeData = metrics.tokenUsage.map((t) => ({
    label: t.timestamp,
    value: t.totalTokens,
  }));

  const errorDonutData = metrics.errorBuckets.map((e, i) => ({
    label: e.category,
    value: e.count,
    color: ERROR_COLORS[i % ERROR_COLORS.length],
  }));

  const throughputData = metrics.runsByDay.map((r) => ({
    label: r.date,
    value: r.count,
  }));

  const leadTimeRows = metrics.leadTimes.map((l) => ({
    issue: l.issue,
    pr: l.pullRequest,
    seconds: l.seconds,
  }));

  return (
    <div className="metrics-view">
      {/* KPI Row */}
      <div className="kpi-row">
        <KPICard
          label="Lead Time (median)"
          value={formatDuration(metrics.medianLeadTimeSeconds)}
          subtitle="Issue → PR green"
          trend="down"
          color="var(--phase-intake)"
        />
        <KPICard
          label="Throughput"
          value={`${metrics.totalRuns} runs`}
          subtitle={`${metrics.successfulRuns} succeeded, ${metrics.failedRuns} failed`}
          color="var(--phase-prove)"
        />
        <KPICard
          label="Token Cost"
          value={formatCost(metrics.totalTokenCost)}
          subtitle={`${formatTokens(metrics.totalTokens)} tokens total`}
          trend="up"
          color="var(--phase-plan)"
        />
        <KPICard
          label="Error Rate"
          value={`${metrics.errorRate}%`}
          subtitle={`${formatTokens(metrics.avgTokensPerRun)} tokens/run avg`}
          trend={metrics.errorRate < 20 ? "down" : "up"}
          color="var(--status-error)"
        />
        <KPICard
          label="Success Rate"
          value={`${((metrics.successfulRuns / metrics.totalRuns) * 100).toFixed(0)}%`}
          subtitle="Runs completed without error"
          trend="down"
          color="var(--status-done)"
        />
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
    </div>
  );
}
