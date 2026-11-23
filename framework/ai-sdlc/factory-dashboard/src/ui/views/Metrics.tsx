import { useState } from "react";

import { factory } from "../api.ts";
import { Empty, Kpi, Loaded, Section, StatusBadge, time } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import { formatDuration, type Report, type TelemetryResponse } from "../model.ts";
import { IntentMetrics } from "./IntentMetrics.tsx";

function seconds(value: number, lang: string): string {
  if (value >= 90) {
    return `${(value / 60).toLocaleString(lang, { maximumFractionDigits: 1 })} min`;
  }
  return `${value.toLocaleString(lang, { maximumFractionDigits: 2 })} s`;
}

function Kpis({ report }: { report: Report }) {
  const t = useT();
  const lang = useLang();
  const ok = report.runs - report.failed_runs;
  const rate = report.runs ? Math.round((ok / report.runs) * 100) : null;
  const passing = report.gates.filter((gate) => gate.last_status === "pass").length;
  const usd = (report.cost.total_usd ?? 0).toLocaleString(lang, { style: "currency", currency: "USD" });
  return (
    <div className="kpi-row">
      <Kpi label={t("metrics.stageRuns")} value={report.runs.toLocaleString(lang)} sub={t("metrics.failed", { n: report.failed_runs })} />
      <Kpi label={t("metrics.success")} tone="prove" value={rate === null ? "—" : `${rate}%`} sub={t("metrics.successSub")} />
      <Kpi label={t("metrics.gates")} tone="publish" value={`${passing}/${report.gates.length}`} sub={t("metrics.gatesSub")} />
      <Kpi label={t("autonomy.title")} tone="plan"
        value={report.autonomy ? t("autonomy.level", { level: report.autonomy.level }) : "—"}
        sub={report.autonomy ? t("autonomy.trials", { ok: report.autonomy.successes, n: report.autonomy.trials }) : undefined} />
      <Kpi label={t("cost.title")} tone="ship" value={usd} sub={t("metrics.costSub")} />
    </div>
  );
}

function StageBars({ report }: { report: Report }) {
  const t = useT();
  const lang = useLang();
  if (report.stages.length === 0) {
    return <Empty>{t("metrics.noStages")}</Empty>;
  }
  const max = Math.max(...report.stages.map((stage) => stage.median_seconds), 0.001);
  return (
    <ul className="bars">
      {report.stages.map((stage) => (
        <li key={stage.stage} className="bar-row">
          <span className="bar-label">{stage.stage}</span>
          <span className="bar-track" role="img" aria-label={`${stage.stage}: ${seconds(stage.median_seconds, lang)}`}>
            <span className="bar-fill" style={{ width: `${Math.max(1, (stage.median_seconds / max) * 85)}%` }} />
            <span className="bar-value" style={{ left: `${Math.max(1, (stage.median_seconds / max) * 85)}%` }}>
              {seconds(stage.median_seconds, lang)}
            </span>
          </span>
        </li>
      ))}
    </ul>
  );
}

function Gates({ report }: { report: Report }) {
  const t = useT();
  const lang = useLang();
  if (report.gates.length === 0) {
    return <Empty>{t("gates.empty")}</Empty>;
  }
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th scope="col">{t("gates.gate")}</th>
            <th scope="col">{t("gates.stage")}</th>
            <th scope="col" className="num">{t("gates.runs")}</th>
            <th scope="col" className="num">{t("gates.passRate")}</th>
            <th scope="col" className="num">{t("gates.median")}</th>
            <th scope="col">{t("gates.last")}</th>
          </tr>
        </thead>
        <tbody>
          {report.gates.map((gate) => (
            <tr key={`${gate.stage}/${gate.gate}`}>
              <th scope="row">{gate.gate}</th>
              <td className="mono small">{gate.stage}</td>
              <td className="num">{gate.runs}</td>
              <td className="num">{gate.pass_rate === null ? "—" : `${gate.pass_rate.toLocaleString(lang)}%`}</td>
              <td className="num">{gate.median_seconds === null ? "—" : seconds(gate.median_seconds, lang)}</td>
              <td>
                <StatusBadge status={gate.last_status} /> <span className="faint small">{time(gate.last_ts, lang)}</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function CostByModel({ report }: { report: Report }) {
  const t = useT();
  const lang = useLang();
  const models = Object.entries(report.cost.by_model ?? {});
  if (models.length === 0) {
    return <Empty>{t("cost.empty")}</Empty>;
  }
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th scope="col">{t("cost.model")}</th>
            <th scope="col" className="num">{t("cost.entries")}</th>
            <th scope="col" className="num">{t("cost.tokens")}</th>
            <th scope="col" className="num">USD</th>
          </tr>
        </thead>
        <tbody>
          {models.map(([model, row]) => (
            <tr key={model}>
              <th scope="row" className="mono small">{model}</th>
              <td className="num">{row.entries}</td>
              <td className="num">{row.input.toLocaleString(lang)} / {row.output.toLocaleString(lang)}</td>
              <td className="num">{row.usd.toLocaleString(lang, { style: "currency", currency: "USD" })}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

const TELEMETRY_GROUPS = [
  { key: "stage", labelKey: "telemetry.tab.stage" },
  { key: "intent", labelKey: "telemetry.tab.intent" },
  { key: "agent", labelKey: "telemetry.tab.agent" },
  { key: "skill", labelKey: "telemetry.tab.skill" },
  { key: "tool", labelKey: "telemetry.tab.tool" },
] as const;

function TelemetryPanel({ workspaceId }: { workspaceId: string }) {
  const t = useT();
  const lang = useLang();
  const [activeTab, setActiveTab] = useState("stage");
  const data = useResource<TelemetryResponse>(factory(workspaceId, `/telemetry?group=${activeTab}`));
  const tabId = (g: string) => `telemetry-tab-${g}`;
  const handleTabKeyDown = (e: React.KeyboardEvent, index: number) => {
    if (e.key === "ArrowRight" || e.key === "ArrowDown") {
      e.preventDefault();
      setActiveTab(TELEMETRY_GROUPS[(index + 1) % TELEMETRY_GROUPS.length].key);
    } else if (e.key === "ArrowLeft" || e.key === "ArrowUp") {
      e.preventDefault();
      setActiveTab(TELEMETRY_GROUPS[(index - 1 + TELEMETRY_GROUPS.length) % TELEMETRY_GROUPS.length].key);
    }
  };
  return (
    <div data-testid="telemetry-panel" className="telemetry-panel">
      <h3 className="section-label">{t("telemetry.panel")}</h3>
      <Loaded resource={data}>
        {(resp) => {
          const rows = resp.rows;
          const totalDur = resp.total_duration_s;
          const totalCost = resp.total_usd;
          const totalFail = resp.total_failures;
          const totalRuns = resp.total_runs;
          const failuresByIntegration = resp.failures_by_integration;
          const repolens = resp.repolens;
          return (
            <div className="stack">
              <div className="kpi-row">
                <Kpi label={t("telemetry.totalDuration")} value={formatDuration(totalDur, lang)} tone="intake" />
                <Kpi label={t("telemetry.totalCost")} value={totalCost.toLocaleString(lang, { style: "currency", currency: "USD" })} tone="ship" />
                <Kpi label={t("telemetry.totalFailures")} value={totalFail.toLocaleString(lang)} tone={totalFail > 0 ? "error" : "prove"} />
                <Kpi label={t("telemetry.totalRuns")} value={totalRuns.toLocaleString(lang)} tone="plan" />
              </div>
              <div className="tabs" role="tablist" aria-label={t("telemetry.panel")}>
                {TELEMETRY_GROUPS.map((g, i) => (
                  <button
                    key={g.key}
                    type="button"
                    id={tabId(g.key)}
                    data-testid={`telemetry-tab-${g.key}`}
                    role="tab"
                    aria-selected={activeTab === g.key}
                    tabIndex={activeTab === g.key ? 0 : -1}
                    className={activeTab === g.key ? "active" : ""}
                    onClick={() => setActiveTab(g.key)}
                    onKeyDown={(e) => handleTabKeyDown(e, i)}
                  >
                    {t(g.labelKey as any)}
                  </button>
                ))}
              </div>
              <div className="table-wrap">
                <table>
                  <caption className="sr-only">{t("telemetry.panel")}: {t(`telemetry.tab.${activeTab}` as any)}</caption>
                  <thead>
                    <tr>
                      <th scope="col">{t("telemetry.key")}</th>
                      <th scope="col" className="num">{t("telemetry.runs")}</th>
                      <th scope="col" className="num">{t("telemetry.duration")}</th>
                      <th scope="col" className="num">{t("telemetry.cost")}</th>
                      <th scope="col" className="num">{t("telemetry.failures")}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.length === 0 ? (
                      <tr data-testid="telemetry-row"><td colSpan={5}><Empty>{t("telemetry.noData")}</Empty></td></tr>
                    ) : (
                      rows.map((row) => (
                        <tr key={row.key} data-testid="telemetry-row">
                          <th scope="row">{row.key}</th>
                          <td className="num">{row.runs}</td>
                          <td className="num">{formatDuration(row.duration_s, lang)}</td>
                          <td className="num">{row.usd.toLocaleString(lang, { style: "currency", currency: "USD" })}</td>
                          <td className="num">
                            {row.failures}
                            {row.failures > 0 && (
                              <span className="failure-marker" role="img" aria-label={t("telemetry.failureMarker")}> ⚠</span>
                            )}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
              {(Object.keys(failuresByIntegration).length > 0 || (repolens.calls ?? 0) > 0) && (
                <div className="charts-grid">
                  {Object.keys(failuresByIntegration).length > 0 && (
                    <Section title={t("telemetry.failuresByIntegration")}>
                      <ul className="facts-list">
                        {Object.entries(failuresByIntegration).map(([name, count]) => (
                          <li key={name} className="fact-row">
                            <span className="mono">{name}</span>
                            <span className="num">{count}</span>
                          </li>
                        ))}
                      </ul>
                    </Section>
                  )}
                  {(repolens.calls ?? 0) > 0 && (
                    <Section title={t("telemetry.repolens")}>
                      <dl className="facts">
                        <dt>{t("telemetry.repolensCalls")}</dt>
                        <dd className="num">{repolens.calls}</dd>
                        <dt>{t("telemetry.repolensAvg")}</dt>
                        <dd className="num">{repolens.avg_ms?.toFixed(1) ?? "—"}</dd>
                        <dt>{t("telemetry.repolensTotal")}</dt>
                        <dd className="num">{repolens.total_ms ?? "—"}</dd>
                        {repolens.by_tool && Object.entries(repolens.by_tool).map(([tool, info]) => (
                          <div key={tool} className="fact">
                            <dt className="mono small">{tool}</dt>
                            <dd>{info.calls} calls · {info.avg_ms.toFixed(1)}ms avg</dd>
                          </div>
                        ))}
                      </dl>
                    </Section>
                  )}
                </div>
              )}
            </div>
          );
        }}
      </Loaded>
    </div>
  );
}

export function Metrics() {
  const t = useT();
  const { workspace } = useWorkspace();
  const report = useResource<Report>(factory(workspace.id, "/report"), 30_000);
  return (
    <Loaded resource={report} rows={5}>
      {(data) => (
        <div className="stack">
          <IntentMetrics />
          <Kpis report={data} />
          <div className="charts-grid">
            <Section title={t("metrics.stages")}><StageBars report={data} /></Section>
            <Section title={t("cost.byModel")}><CostByModel report={data} /></Section>
          </div>
          <Section title={t("gates.title")}><Gates report={data} /></Section>
          <TelemetryPanel workspaceId={workspace.id} />
        </div>
      )}
    </Loaded>
  );
}
