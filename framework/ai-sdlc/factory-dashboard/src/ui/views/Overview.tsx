import { factory } from "../api.ts";
import { Icon } from "../components/Icon.tsx";
import { Empty, Loaded, Section, StatusBadge, time } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import type { Capabilities, Report, Run } from "../model.ts";

const FLOW = ["requested", "queued", "running"] as const;
const OUTCOMES = ["succeeded", "failed", "blocked", "cancelled"] as const;

function Capability({ label, on, detail }: { label: string; on: boolean; detail?: string }) {
  const t = useT();
  return (
    <li className={`cap ${on ? "cap-on" : "cap-off"}`}>
      <Icon name={on ? "check" : "minus"} size={16} />
      <span className="cap-label">{label}</span>
      <span className="cap-state">{on ? t("cap.on") : t("cap.off")}</span>
      {detail && <span className="cap-detail">{detail}</span>}
    </li>
  );
}

function ProductionLine({ runs }: { runs: Run[] }) {
  const t = useT();
  const { workspace, go } = useWorkspace();
  const count = (status: string) => runs.filter((run) => run.status === status).length;
  const station = (status: string) => (
    <li key={status}>
      <button
        type="button"
        className={`station station-${status}${count(status) ? " station-busy" : ""}`}
        onClick={() => go(`${workspace.id}/runs?status=${status}`)}
      >
        <span className="station-count">{count(status)}</span>
        <StatusBadge status={status} />
      </button>
    </li>
  );
  return (
    <div className="line">
      <ol className="line-flow" aria-label={t("line.title")}>
        {FLOW.map(station)}
      </ol>
      <ol className="line-outcomes">{OUTCOMES.map(station)}</ol>
    </div>
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
            <th scope="col">{t("gates.passRate")}</th>
            <th scope="col" className="num">{t("gates.median")}</th>
            <th scope="col">{t("gates.last")}</th>
          </tr>
        </thead>
        <tbody>
          {report.gates.map((gate) => (
            <tr key={`${gate.stage}/${gate.gate}`}>
              <th scope="row">{gate.gate}</th>
              <td>{gate.stage}</td>
              <td className="num">{gate.runs}</td>
              <td>
                {gate.pass_rate === null ? "—" : (
                  <span className="meter">
                    <meter min={0} max={100} value={gate.pass_rate} aria-hidden="true" />
                    <span className="num">{gate.pass_rate.toLocaleString(lang)}%</span>
                  </span>
                )}
              </td>
              <td className="num">{gate.median_seconds === null ? "—" : `${gate.median_seconds.toLocaleString(lang)} s`}</td>
              <td>
                <StatusBadge status={gate.last_status} />
                <span className="muted small"> {time(gate.last_ts, lang)}</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Cost({ report }: { report: Report }) {
  const t = useT();
  const lang = useLang();
  const models = Object.entries(report.cost.by_model ?? {});
  const usd = (value: number) => value.toLocaleString(lang, { style: "currency", currency: "USD" });
  if (models.length === 0) {
    return <Empty>{t("cost.empty")}</Empty>;
  }
  return (
    <>
      <p className="figure">
        <span className="figure-value num">{usd(report.cost.total_usd ?? 0)}</span>
        <span className="figure-label">{t("cost.total")}</span>
      </p>
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
                <th scope="row" className="mono">{model}</th>
                <td className="num">{row.entries}</td>
                <td className="num">{row.input.toLocaleString(lang)} / {row.output.toLocaleString(lang)}</td>
                <td className="num">{usd(row.usd)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

export function Overview() {
  const t = useT();
  const { workspace } = useWorkspace();
  const caps = useResource<Capabilities>(factory(workspace.id, "/capabilities"), 30_000);
  const runs = useResource<{ entries: Run[] }>(factory(workspace.id, "/runs"), 5_000);
  const report = useResource<Report>(factory(workspace.id, "/report"), 30_000);
  return (
    <div className="stack">
      <Section title={t("line.title")}>
        <p className="lede">{t("line.hint")}</p>
        <Loaded resource={runs} rows={2}>{(data) => <ProductionLine runs={data.entries} />}</Loaded>
      </Section>
      <Section title={t("cap.title")}>
        <Loaded resource={caps} rows={2}>
          {(data) => (
            <ul className="caps">
              <Capability label={t("cap.runner")} on={data.runner} detail={data.runner ? t("cap.runner.on") : t("cap.runner.off")} />
              <Capability label={t("cap.approvals")} on={data.approvals_live} />
              <Capability label={t("cap.forge")} on={data.forge_write} />
              <Capability label={t("cap.sandbox")} on={data.isolator.sandbox} detail={data.isolator.backend ?? undefined} />
            </ul>
          )}
        </Loaded>
      </Section>
      <div className="split">
        <Section title={t("gates.title")}>
          <Loaded resource={report}>{(data) => <Gates report={data} />}</Loaded>
        </Section>
        <div className="stack">
          <Section title={t("autonomy.title")}>
            <Loaded resource={report} rows={2}>
              {(data) =>
                data.autonomy ? (
                  <>
                    <p className="figure">
                      <span className="figure-value">{t("autonomy.level", { level: data.autonomy.level })}</span>
                      <span className="tag">{data.autonomy.status}</span>
                    </p>
                    <p className="muted">{t("autonomy.trials", { ok: data.autonomy.successes, n: data.autonomy.trials })}</p>
                    {data.autonomy.detail && <p className="muted small">{data.autonomy.detail}</p>}
                  </>
                ) : (
                  <Empty>—</Empty>
                )
              }
            </Loaded>
          </Section>
          <Section title={t("cost.title")}>
            <Loaded resource={report}>{(data) => <Cost report={data} />}</Loaded>
          </Section>
        </div>
      </div>
    </div>
  );
}
