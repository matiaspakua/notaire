import { factory } from "../api.ts";
import { Empty, Kpi, Loaded, Section, StatusBadge, time } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import type { Report } from "../model.ts";

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

export function Metrics() {
  const t = useT();
  const { workspace } = useWorkspace();
  const report = useResource<Report>(factory(workspace.id, "/report"), 30_000);
  return (
    <Loaded resource={report} rows={5}>
      {(data) => (
        <div className="stack">
          <Kpis report={data} />
          <div className="charts-grid">
            <Section title={t("metrics.stages")}><StageBars report={data} /></Section>
            <Section title={t("cost.byModel")}><CostByModel report={data} /></Section>
          </div>
          <Section title={t("gates.title")}><Gates report={data} /></Section>
        </div>
      )}
    </Loaded>
  );
}
