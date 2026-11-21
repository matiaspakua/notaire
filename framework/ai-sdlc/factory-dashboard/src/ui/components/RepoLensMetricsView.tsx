/**
 * RepoLensMetricsView — Shows how RepoLens improves factory performance.
 *
 * Displays benchmark comparisons: retrieval recall, latency, output size,
 * agent tool calls, token usage, and cost savings — all from the RepoLens
 * API (/api/metrics). Pure SVG charts, no external library.
 */
import { useMemo, useState, useEffect } from "react";
import { RepoLensMetrics, RetrievalMetrics, AgentComparison } from "@/types";

// ───────────────────── Data fetching ─────────────────────

function useRepoLensMetrics() {
  const [metrics, setMetrics] = useState<RepoLensMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetch("/api/repolens/metrics")
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (!cancelled) {
          setMetrics(data);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message);
          setLoading(false);
        }
      });
    return () => { cancelled = true; };
  }, []);

  return { metrics, loading, error };
}

// ───────────────────── Sub-components ─────────────────────

function MetricCard({ label, value, sub, color }: {
  label: string;
  value: string;
  sub?: string;
  color?: string;
}) {
  return (
    <div className="repolens-metric-card">
      <div className="repolens-metric-label">{label}</div>
      <div className="repolens-metric-value" style={{ color: color || "var(--text)" }}>{value}</div>
      {sub && <div className="repolens-metric-sub">{sub}</div>}
    </div>
  );
}

function RecallBarChart({ data }: { data: RetrievalMetrics }) {
  const max = 1;
  const bars = [
    { label: "grep", value: data.grep_recall_at_10, color: "var(--text-muted)" },
    { label: "hybrid", value: data.hybrid_recall_at_10, color: "var(--accent)" },
    { label: "graph", value: data.graph_recall_at_10, color: "var(--phase-ship)" },
  ];

  return (
    <div className="repolens-chart-card">
      <h3 className="repolens-chart-title">Recall@10 — {data.tasks} tasks</h3>
      <div className="repolens-bar-chart" role="img" aria-label="Recall comparison bar chart">
        {bars.map((bar) => (
          <div key={bar.label} className="repolens-bar-row">
            <span className="repolens-bar-label">{bar.label}</span>
            <div className="repolens-bar-track">
              <div
                className="repolens-bar-fill"
                style={{
                  width: `${(bar.value / max) * 100}%`,
                  backgroundColor: bar.color,
                }}
              />
            </div>
            <span className="repolens-bar-value">{(bar.value * 100).toFixed(1)}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function LatencyBarChart({ data }: { data: RetrievalMetrics }) {
  const max = Math.max(data.grep_latency_ms, data.hybrid_latency_ms, data.graph_latency_ms);
  const bars = [
    { label: "grep", value: data.grep_latency_ms, color: "var(--text-muted)" },
    { label: "hybrid", value: data.hybrid_latency_ms, color: "var(--accent)" },
    { label: "graph", value: data.graph_latency_ms, color: "var(--phase-ship)" },
  ];

  return (
    <div className="repolens-chart-card">
      <h3 className="repolens-chart-title">Latency (ms)</h3>
      <div className="repolens-bar-chart" role="img" aria-label="Latency comparison bar chart">
        {bars.map((bar) => (
          <div key={bar.label} className="repolens-bar-row">
            <span className="repolens-bar-label">{bar.label}</span>
            <div className="repolens-bar-track">
              <div
                className="repolens-bar-fill"
                style={{
                  width: `${(bar.value / max) * 100}%`,
                  backgroundColor: bar.color,
                }}
              />
            </div>
            <span className="repolens-bar-value">{bar.value.toFixed(1)}ms</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function OutputBytesChart({ data }: { data: RetrievalMetrics }) {
  const max = Math.max(data.grep_bytes, data.hybrid_bytes, data.graph_bytes);
  const bars = [
    { label: "grep", value: data.grep_bytes, color: "var(--text-muted)" },
    { label: "hybrid", value: data.hybrid_bytes, color: "var(--accent)" },
    { label: "graph", value: data.graph_bytes, color: "var(--phase-ship)" },
  ];

  const fmt = (bytes: number) => {
    if (bytes > 1000) return `${(bytes / 1000).toFixed(1)}KB`;
    return `${bytes.toFixed(0)}B`;
  };

  return (
    <div className="repolens-chart-card">
      <h3 className="repolens-chart-title">Output Size (bytes/task)</h3>
      <div className="repolens-bar-chart" role="img" aria-label="Output size comparison">
        {bars.map((bar) => (
          <div key={bar.label} className="repolens-bar-row">
            <span className="repolens-bar-label">{bar.label}</span>
            <div className="repolens-bar-track">
              <div
                className="repolens-bar-fill"
                style={{
                  width: `${(bar.value / max) * 100}%`,
                  backgroundColor: bar.color,
                }}
              />
            </div>
            <span className="repolens-bar-value">{fmt(bar.value)}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function AgentComparisonView({ label, data }: { label: string; data: AgentComparison }) {
  const { improvement } = data;

  const ratioCards = [
    { label: "Tool Calls", ratio: improvement.tool_calls_ratio, desc: "fewer with RepoLens" },
    { label: "Input Tokens", ratio: improvement.input_tokens_ratio, desc: "less with RepoLens" },
    { label: "Cost", ratio: improvement.cost_ratio, desc: "less with RepoLens" },
    { label: "Duration", ratio: improvement.duration_ratio, desc: "faster with RepoLens" },
  ];

  return (
    <div className="repolens-agent-section">
      <h3 className="repolens-agent-title">{label} ({data.baseline.runs} runs)</h3>
      <div className="repolens-ratio-grid">
        {ratioCards.map((rc) => (
          <div key={rc.label} className="repolens-ratio-card">
            <div className="repolens-ratio-label">{rc.label}</div>
            <div className="repolens-ratio-value">{rc.ratio.toFixed(1)}x</div>
            <div className="repolens-ratio-sub">{rc.desc}</div>
            <div className="repolens-ratio-detail">
              <span className="repolens-baseline-val">{data.baseline[rc.label.toLowerCase().replace(" ", "_") as keyof typeof data.baseline] as number}</span>
              <span className="repolens-arrow">→</span>
              <span className="repolens-graph-val">{data.graph[rc.label.toLowerCase().replace(" ", "_") as keyof typeof data.graph] as number}</span>
            </div>
          </div>
        ))}
      </div>
      <div className="repolens-agent-detail">
        <div className="repolens-detail-row">
          <span className="repolens-detail-label">Recall</span>
          <span className="repolens-detail-value">{(data.baseline.recall * 100).toFixed(1)}% → {(data.graph.recall * 100).toFixed(1)}%</span>
        </div>
        <div className="repolens-detail-row">
          <span className="repolens-detail-label">Turns</span>
          <span className="repolens-detail-value">{data.baseline.turns.toFixed(1)} → {data.graph.turns.toFixed(1)}</span>
        </div>
        <div className="repolens-detail-row">
          <span className="repolens-detail-label">Output Tokens</span>
          <span className="repolens-detail-value">{data.baseline.output_tokens.toFixed(0)} → {data.graph.output_tokens.toFixed(0)}</span>
        </div>
      </div>
    </div>
  );
}

function IndexStats({ stats }: { stats: RepoLensMetrics["index_stats"] }) {
  if (!stats) return null;
  const items = [
    { label: "Nodes", value: stats.nodes },
    { label: "Edges", value: stats.edges },
    { label: "Files", value: stats.files },
    { label: "Projects", value: stats.projects },
  ];
  return (
    <div className="repolens-index-stats">
      <h3 className="repolens-chart-title">Knowledge Graph</h3>
      <div className="repolens-stats-grid">
        {items.map((item) => (
          <div key={item.label} className="repolens-stat-item">
            <div className="repolens-stat-value">{item.value?.toLocaleString() || "—"}</div>
            <div className="repolens-stat-label">{item.label}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ───────────────────── Main view ─────────────────────

export function RepoLensMetricsView() {
  const { metrics, loading, error } = useRepoLensMetrics();
  const [activeTab, setActiveTab] = useState<"retrieval" | "agents" | "index">("retrieval");

  const agentEntries = useMemo(() => {
    if (!metrics) return [];
    return Object.entries(metrics.agents).map(([key, val]) => ({
      label: key.replace("-", " ").replace(/\b\w/g, (c) => c.toUpperCase()),
      data: val,
    }));
  }, [metrics]);

  if (loading) {
    return (
      <div className="repolens-metrics-loading">
        <div className="repolens-loading-spinner" />
        <span>Loading RepoLens metrics…</span>
      </div>
    );
  }

  if (error || !metrics) {
    return (
      <div className="repolens-metrics-error">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 8v4M12 16h.01" strokeLinecap="round" />
        </svg>
        <p>Could not load RepoLens metrics</p>
        <p className="repolens-error-detail">{error || "No data"}</p>
        <p className="repolens-error-hint">Make sure RepoLens is running on port 3456</p>
      </div>
    );
  }

  const tuning = metrics.retrieval.tuning;
  const holdout = metrics.retrieval.holdout;

  return (
    <div className="repolens-metrics-view">
      {/* ── Header ── */}
      <div className="repolens-header">
        <h2 className="repolens-title">RepoLens Impact</h2>
        <p className="repolens-subtitle">
          How the knowledge graph improves factory performance — baseline vs. with RepoLens
        </p>
      </div>

      {/* ── Headline KPIs ── */}
      <div className="repolens-kpi-row">
        {tuning && (
          <>
            <MetricCard
              label="Recall@10"
              value={`${(tuning.graph_recall_at_10 * 100).toFixed(0)}%`}
              sub={`grep: ${(tuning.grep_recall_at_10 * 100).toFixed(0)}% → graph: ${(tuning.graph_recall_at_10 * 100).toFixed(0)}%`}
              color="var(--phase-ship)"
            />
            <MetricCard
              label="Latency"
              value={`${(tuning.grep_latency_ms / tuning.graph_latency_ms).toFixed(0)}x faster`}
              sub={`grep: ${tuning.grep_latency_ms.toFixed(0)}ms → graph: ${tuning.graph_latency_ms.toFixed(1)}ms`}
              color="var(--accent)"
            />
            <MetricCard
              label="Output Size"
              value={`${(tuning.grep_bytes / tuning.graph_bytes).toFixed(0)}x less`}
              sub={`grep: ${(tuning.grep_bytes / 1000).toFixed(1)}KB → graph: ${(tuning.graph_bytes / 1000).toFixed(1)}KB`}
              color="var(--phase-prove)"
            />
          </>
        )}
        {agentEntries.length > 0 && (
          <MetricCard
            label="Tool Calls"
            value={`${agentEntries[0].data.improvement.tool_calls_ratio.toFixed(1)}x fewer`}
            sub="with RepoLens MCP"
            color="var(--phase-plan)"
          />
        )}
      </div>

      {/* ── Tabs ── */}
      <div className="repolens-tabs">
        <button
          className={`repolens-tab ${activeTab === "retrieval" ? "active" : ""}`}
          onClick={() => setActiveTab("retrieval")}
        >
          Retrieval
        </button>
        <button
          className={`repolens-tab ${activeTab === "agents" ? "active" : ""}`}
          onClick={() => setActiveTab("agents")}
        >
          Agent Sessions
        </button>
        <button
          className={`repolens-tab ${activeTab === "index" ? "active" : ""}`}
          onClick={() => setActiveTab("index")}
        >
          Index
        </button>
      </div>

      {/* ── Retrieval tab ── */}
      {activeTab === "retrieval" && (
        <div className="repolens-tab-content">
          {tuning && (
            <>
              <RecallBarChart data={tuning} />
              <LatencyBarChart data={tuning} />
              <OutputBytesChart data={tuning} />
            </>
          )}
          {holdout && (
            <div className="repolens-holdout-section">
              <h3 className="repolens-chart-title">Holdout Set — {holdout.tasks} unseen tasks</h3>
              <RecallBarChart data={holdout} />
              <LatencyBarChart data={holdout} />
              <OutputBytesChart data={holdout} />
            </div>
          )}
        </div>
      )}

      {/* ── Agents tab ── */}
      {activeTab === "agents" && (
        <div className="repolens-tab-content">
          {agentEntries.length === 0 ? (
            <div className="repolens-empty-state">
              <p>No agent benchmark data available</p>
            </div>
          ) : (
            agentEntries.map((entry) => (
              <AgentComparisonView key={entry.label} label={entry.label} data={entry.data} />
            ))
          )}
        </div>
      )}

      {/* ── Index tab ── */}
      {activeTab === "index" && (
        <div className="repolens-tab-content">
          <IndexStats stats={metrics.index_stats} />
          {metrics.headlines && metrics.headlines.length > 0 && (
            <div className="repolens-headlines">
              <h3 className="repolens-chart-title">Captions</h3>
              {metrics.headlines.map((h, i) => (
                <p key={i} className="repolens-headline">{h}</p>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
