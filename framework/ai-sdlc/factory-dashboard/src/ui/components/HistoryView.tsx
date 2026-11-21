/**
 * HistoryView — Browsable factory history from persistent storage.
 *
 * Shows past runs, evidence records, costs, and projects so the dashboard
 * remains useful even without an active intent. Users can filter by stage,
 * search by issue, and inspect individual evidence records.
 */
import { useMemo, useState, useEffect } from "react";

// ───────────────────── Types ─────────────────────

interface StorageSummary {
  storage_available: boolean;
  evidence_count: number;
  cost_count: number;
  run_count: number;
  project_count: number;
  agent_count: number;
  skill_count: number;
  mcp_count: number;
  workflow_count: number;
  stages: Record<string, number>;
  costs_by_model: Record<string, { entries: number; usd: number }>;
  costs_by_agent: Record<string, number>;
  total_usd: number;
}

interface EvidenceRecord {
  ts: string;
  stage: string;
  ok: boolean;
  commit?: string;
  base?: string;
  gates?: Array<{
    name: string;
    status: string;
    seconds: number;
  }>;
  [key: string]: unknown;
}

interface CostEntry {
  ts: string;
  agent: string;
  model: string;
  input: number;
  output: number;
  usd: number | null;
  issue?: string;
}

// ───────────────────── Data fetching ─────────────────────

function useStorage() {
  const [summary, setSummary] = useState<StorageSummary | null>(null);
  const [evidence, setEvidence] = useState<EvidenceRecord[]>([]);
  const [costs, setCosts] = useState<CostEntry[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      fetch("/api/storage/summary").then((r) => r.json()).catch(() => null),
      fetch("/api/storage/evidence?limit=100").then((r) => r.json()).catch(() => []),
      fetch("/api/storage/costs").then((r) => r.json()).catch(() => []),
    ]).then(([sum, ev, cs]) => {
      if (!cancelled) {
        setSummary(sum);
        setEvidence(Array.isArray(ev) ? ev : []);
        setCosts(Array.isArray(cs) ? cs : []);
        setLoading(false);
      }
    });
    return () => { cancelled = true; };
  }, []);

  return { summary, evidence, costs, loading };
}

// ───────────────────── Sub-components ─────────────────────

function SummaryCards({ summary }: { summary: StorageSummary }) {
  const cards = [
    { label: "Evidence Runs", value: summary.evidence_count, color: "var(--phase-intake)" },
    { label: "Cost Entries", value: summary.cost_count, color: "var(--phase-plan)" },
    { label: "Runs", value: summary.run_count, color: "var(--phase-prove)" },
    { label: "Projects", value: summary.project_count, color: "var(--phase-publish)" },
    { label: "Agents", value: summary.agent_count, color: "var(--phase-ship)" },
    { label: "Skills", value: summary.skill_count, color: "var(--accent)" },
    { label: "MCP Servers", value: summary.mcp_count, color: "var(--text-muted)" },
    { label: "Workflows", value: summary.workflow_count, color: "var(--text)" },
  ];

  return (
    <div className="history-summary-grid">
      {cards.map((card) => (
        <div key={card.label} className="history-summary-card">
          <div className="history-summary-value" style={{ color: card.color }}>
            {card.value}
          </div>
          <div className="history-summary-label">{card.label}</div>
        </div>
      ))}
      {summary.total_usd > 0 && (
        <div className="history-summary-card history-total-cost">
          <div className="history-summary-value">${summary.total_usd.toFixed(4)}</div>
          <div className="history-summary-label">Total Cost</div>
        </div>
      )}
    </div>
  );
}

function StageBreakdown({ stages }: { stages: Record<string, number> }) {
  const entries = Object.entries(stages).sort((a, b) => b[1] - a[1]);

  if (entries.length === 0) return null;

  const max = entries[0][1];

  return (
    <div className="history-section">
      <h3 className="history-section-title">Stage Breakdown</h3>
      <div className="history-stage-bars">
        {entries.map(([stage, count]) => (
          <div key={stage} className="history-stage-bar-row">
            <span className="history-stage-bar-label">{stage}</span>
            <div className="history-stage-bar-track">
              <div
                className="history-stage-bar-fill"
                style={{ width: `${(count / max) * 100}%` }}
              />
            </div>
            <span className="history-stage-bar-value">{count}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function EvidenceTable({
  records,
  searchQuery,
  stageFilter,
}: {
  records: EvidenceRecord[];
  searchQuery: string;
  stageFilter: string;
}) {
  const filtered = useMemo(() => {
    let filtered = records;
    if (stageFilter) {
      filtered = filtered.filter((r) => r.stage === stageFilter);
    }
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (r) =>
          (r.commit || "").toLowerCase().includes(q) ||
          (r.stage || "").toLowerCase().includes(q) ||
          (r.base || "").toLowerCase().includes(q)
      );
    }
    return filtered;
  }, [records, searchQuery, stageFilter]);

  const stages = useMemo(() => {
    const set = new Set(records.map((r) => r.stage).filter(Boolean));
    return Array.from(set).sort();
  }, [records]);

  if (filtered.length === 0) {
    return (
      <div className="history-empty-state">
        <p>No evidence records found</p>
      </div>
    );
  }

  return (
    <div className="history-section">
      <div className="history-section-header">
        <h3 className="history-section-title">Evidence Records</h3>
        <div className="history-filters">
          {stages.length > 1 && (
            <select
              className="history-filter-select"
              value={stageFilter}
              onChange={(e) => {
                const target = e.target as HTMLSelectElement;
                // @ts-expect-error inline handler
                target.dispatchEvent(new Event("change", { bubbles: true }));
              }}
              aria-label="Filter by stage"
            >
              <option value="">All stages</option>
              {stages.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          )}
          <span className="history-filter-count">{filtered.length} records</span>
        </div>
      </div>
      <div className="history-evidence-table-wrapper">
        <table className="history-evidence-table">
          <thead>
            <tr>
              <th>Time</th>
              <th>Stage</th>
              <th>Status</th>
              <th>Commit</th>
              <th>Gates</th>
            </tr>
          </thead>
          <tbody>
            {filtered.slice().reverse().map((record, i) => {
              const gates = record.gates || [];
              const passCount = gates.filter((g) => g.status === "pass").length;
              const failCount = gates.filter((g) => g.status === "fail").length;

              return (
                <tr key={i} className={record.ok ? "" : "history-row-failed"}>
                  <td className="history-time">{new Date(record.ts).toLocaleString()}</td>
                  <td>
                    <span className="history-stage-badge">{record.stage}</span>
                  </td>
                  <td>
                    <span className={`history-status-pill ${record.ok ? "history-status-pass" : "history-status-fail"}`}>
                      {record.ok ? "Pass" : "Fail"}
                    </span>
                  </td>
                  <td className="history-commit">{record.commit?.slice(0, 12) || "—"}</td>
                  <td>
                    <span className="history-gates">
                      {passCount > 0 && <span className="history-gate-pass">{passCount}✓</span>}
                      {failCount > 0 && <span className="history-gate-fail">{failCount}✗</span>}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function CostTable({ entries }: { entries: CostEntry[] }) {
  if (entries.length === 0) return null;

  const byModel: Record<string, { entries: number; input: number; output: number; usd: number }> = {};
  for (const entry of entries) {
    const model = entry.model || "unknown";
    if (!byModel[model]) {
      byModel[model] = { entries: 0, input: 0, output: 0, usd: 0 };
    }
    byModel[model].entries++;
    byModel[model].input += entry.input;
    byModel[model].output += entry.output;
    byModel[model].usd += entry.usd || 0;
  }

  const modelRows = Object.entries(byModel).sort((a, b) => b[1].usd - a[1].usd);

  return (
    <div className="history-section">
      <h3 className="history-section-title">Cost by Model</h3>
      <div className="history-cost-table-wrapper">
        <table className="history-cost-table">
          <thead>
            <tr>
              <th>Model</th>
              <th>Entries</th>
              <th>Input Tokens</th>
              <th>Output Tokens</th>
              <th>USD</th>
            </tr>
          </thead>
          <tbody>
            {modelRows.map(([model, data]) => (
              <tr key={model}>
                <td className="history-model">{model}</td>
                <td>{data.entries}</td>
                <td>{data.input.toLocaleString()}</td>
                <td>{data.output.toLocaleString()}</td>
                <td className="history-cost">${data.usd.toFixed(4)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ───────────────────── Main view ─────────────────────

export function HistoryView() {
  const { summary, evidence, costs, loading } = useStorage();
  const [searchQuery, setSearchQuery] = useState("");
  const [stageFilter, setStageFilter] = useState("");

  if (loading) {
    return (
      <div className="history-loading">
        <div className="history-loading-spinner" />
        <span>Loading factory history…</span>
      </div>
    );
  }

  if (!summary?.storage_available) {
    return (
      <div className="history-empty-state">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
          <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z" />
          <path d="M17 21v-8H7v8M7 3v5h8" />
        </svg>
        <p>No persistent storage found</p>
        <p className="history-empty-hint">
          The .aisdlc directory is not present. Run <code>aisdlc serve</code> or start a factory run to create it.
        </p>
      </div>
    );
  }

  return (
    <div className="history-view">
      {/* ── Header ── */}
      <div className="history-header">
        <h2 className="history-title">Factory History</h2>
        <p className="history-subtitle">
          Persistent storage — browse past runs, evidence, and costs
        </p>
      </div>

      {/* ── Summary cards ── */}
      <SummaryCards summary={summary} />

      {/* ── Stage breakdown ── */}
      {summary.stages && Object.keys(summary.stages).length > 0 && (
        <StageBreakdown stages={summary.stages} />
      )}

      {/* ── Search bar ── */}
      <div className="history-search-bar">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          type="text"
          className="history-search-input"
          placeholder="Search by commit, stage, or branch…"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          aria-label="Search evidence records"
        />
        {searchQuery && (
          <button
            className="history-search-clear"
            onClick={() => setSearchQuery("")}
            aria-label="Clear search"
          >
            ×
          </button>
        )}
      </div>

      {/* ── Evidence table ── */}
      <EvidenceTable
        records={evidence}
        searchQuery={searchQuery}
        stageFilter={stageFilter}
      />

      {/* ── Cost table ── */}
      {costs.length > 0 && <CostTable entries={costs} />}
    </div>
  );
}
