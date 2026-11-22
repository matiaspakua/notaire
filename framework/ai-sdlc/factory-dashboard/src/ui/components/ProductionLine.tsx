import { type CSSProperties, type KeyboardEvent, useRef, useState } from "react";

import { factory } from "../api.ts";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { type Key, useLang, useT } from "../i18n.ts";
import {
  gateState,
  intentProgress,
  normalizeStage,
  stationKind,
  stationStates,
  stationThroughput,
  type GateState,
  type StationKind,
  type StationState,
  type Throughput,
  type WorkflowStep,
} from "../line-model.ts";
import type { FactoryStatus, Gate, Intent, LinePayload, LineStage, Report, TelemetryResponse } from "../model.ts";
import { formatDuration } from "../model.ts";
import { EmptyState, Loaded } from "./State.tsx";
import "../production-line.css";

interface WorkflowPayload {
  body?: { steps?: WorkflowStep[] };
}

const PILL: Record<StationState, string> = {
  done: "done",
  running: "running",
  failed: "error",
  next: "idle",
  idle: "idle",
};

function money(usd: number): string {
  return usd > 0 && usd < 0.01 ? "<$0.01" : `$${usd.toFixed(2)}`;
}

function KindMark({ kind }: { kind: StationKind }) {
  return <i className={`pl-kind-mark ${kind}`} aria-hidden="true" />;
}

function ThroughputLine({ throughput }: { throughput: Throughput | null }) {
  const t = useT();
  const lang = useLang();
  if (!throughput) return <span className="pl-throughput faint">{t("pl.noRuns")}</span>;
  const parts = [
    t("pl.runs", { n: throughput.runs }),
    t("pl.avg", { d: formatDuration(throughput.avgSeconds, lang) }),
    throughput.usd > 0 ? money(throughput.usd) : t("pl.costUnknown"),
  ];
  if (throughput.failures > 0) parts.push(t("pl.failures", { n: throughput.failures }));
  return <span className="pl-throughput">{parts.join(" · ")}</span>;
}

function GateChip({ name, mode, state }: { name: string; mode: string; state: GateState }) {
  const t = useT();
  return (
    <li
      className={`pl-gate ${state} ${mode === "required" ? "required" : "advisory"}`}
      data-testid="gate"
      data-state={state}
      title={`${name} · ${mode === "required" ? t("gate.required") : t("gate.advisory")}`}
    >
      <span className="pl-gate-state">{t(`pl.gate.${state}` as Key)}</span>
      <span className="pl-gate-name">{name}</span>
    </li>
  );
}

function evidenceFor(report: Report | undefined, stageId: string, gateName: string): Gate | undefined {
  return report?.gates.find((g) => g.stage === stageId && g.gate === gateName);
}

function IntentStrip({
  stages,
  states,
  status,
  intent,
}: {
  stages: LineStage[];
  states: Record<string, StationState>;
  status: FactoryStatus | undefined;
  intent: Intent | undefined;
}) {
  const t = useT();
  const lang = useLang();
  const progress = intentProgress(stages, states, status, new Date());
  const openIntentBar = () => document.querySelector<HTMLElement>('[data-testid="intent-bar"]')?.click();
  return (
    <div className="pl-strip" data-testid="intent-strip">
      <div className="pl-strip-intent">
        {intent ? (
          <span className="pl-strip-title">{intent.title}</span>
        ) : (
          <>
            <span className="pl-strip-title muted">{status?.run ? t("pl.noIntent") : t("pl.noRun")}</span>
            <button type="button" className="btn btn-quiet" onClick={openIntentBar}>
              {t("pl.submitIntent")}
            </button>
          </>
        )}
        {status?.run && <span className="mono small">{status.run}</span>}
        {status?.run_status && <span className={`status-pill ${status.run_status === "failed" ? "error" : status.run_status === "succeeded" ? "done" : "running"}`}>{status.run_status}</span>}
      </div>
      <div
        className="pl-progress"
        role="img"
        aria-label={t("pl.progress", { done: progress.done, total: progress.total })}
      >
        {stages.map((s) => (
          <span key={s.id} className={`pl-seg ${states[s.id]}`} />
        ))}
      </div>
      <div className="pl-strip-facts mono small">
        <span>{t("pl.progress", { done: progress.done, total: progress.total })}</span>
        <span>{progress.elapsedSeconds != null ? formatDuration(progress.elapsedSeconds, lang) : t("pl.elapsedUnknown")}</span>
        <span>{progress.usd != null ? money(progress.usd) : t("pl.costUnknown")}</span>
        {status?.step && <span>{t("pl.now", { step: status.step })}</span>}
      </div>
    </div>
  );
}

function Legend() {
  const t = useT();
  const kinds: StationKind[] = ["agent", "deterministic", "human"];
  const gates: GateState[] = ["passed", "failed", "not-evaluable", "not-run"];
  return (
    <div className="pl-legend small">
      {kinds.map((k) => (
        <span key={k} className="pl-legend-item">
          <KindMark kind={k} />
          {t(`pl.kind.${k}` as Key)}
        </span>
      ))}
      <span className="pl-legend-sep" aria-hidden="true" />
      {gates.map((g) => (
        <span key={g} className={`pl-legend-gate ${g}`}>{t(`pl.gate.${g}` as Key)}</span>
      ))}
      <span className="faint">{t("pl.skipNote")}</span>
    </div>
  );
}

function Detail({ stage, kind, state, report }: { stage: LineStage; kind: StationKind; state: StationState; report: Report | undefined }) {
  const t = useT();
  const lang = useLang();
  const list = (items: string[]) => (items.length ? items.join(", ") : t("line.none"));
  return (
    <section className="pl-detail" data-testid="station-details" aria-live="polite" aria-labelledby="pl-detail-title">
      <div className="pl-detail-head">
        <h3 id="pl-detail-title" className="pl-detail-title">{stage.id}</h3>
        <span className="pl-legend-item small"><KindMark kind={kind} />{t(`pl.kind.${kind}` as Key)}</span>
        <span className={`status-pill ${PILL[state]}`}>{t(`pl.state.${state}` as Key)}</span>
      </div>
      <div className="pl-detail-body">
        <dl className="facts">
          <dt className="section-label">{t("line.agents")}</dt><dd>{list(stage.agents)}</dd>
          <dt className="section-label">{t("line.skills")}</dt><dd>{list(stage.skills)}</dd>
          <dt className="section-label">{t("line.mcp")}</dt><dd>{list([...new Set([...stage.mcp, ...stage.tools])])}</dd>
          <dt className="section-label">{t("line.model")}</dt><dd className="mono">{stage.model ?? t("line.none")}</dd>
          <dt className="section-label">{t("line.effort")}</dt>
          <dd className="mono">{Object.keys(stage.effort).length ? Object.entries(stage.effort).map(([k, v]) => `${k} ${v.toLocaleString(lang)}`).join(" · ") : t("line.none")}</dd>
          <dt className="section-label">{t("line.inputs")}</dt><dd data-testid="stage-inputs">{list(stage.inputs)}</dd>
          <dt className="section-label">{t("line.outputs")}</dt><dd data-testid="stage-outputs">{list(stage.outputs)}</dd>
          <dt className="section-label">{t("pl.grants")}</dt><dd className="mono">{list(stage.guardrails.map((g) => g.id))}</dd>
        </dl>
        <div className="table-wrap">
          <table className="table pl-gates-table">
            <caption className="section-label">{t("pl.gatesOf", { stage: stage.id })}</caption>
            <thead>
              <tr>
                <th scope="col">{t("pl.col.gate")}</th>
                <th scope="col">{t("pl.col.mode")}</th>
                <th scope="col">{t("pl.col.last")}</th>
                <th scope="col" className="num">{t("pl.col.passRate")}</th>
                <th scope="col" className="num">{t("pl.col.median")}</th>
                <th scope="col" className="num">{t("pl.col.runs")}</th>
              </tr>
            </thead>
            <tbody>
              {stage.gates.length === 0 && (
                <tr><td colSpan={6} className="muted">{t("pl.noGates")}</td></tr>
              )}
              {stage.gates.map((g) => {
                const ev = evidenceFor(report, stage.id, g.name);
                const state = gateState(ev?.last_status);
                return (
                  <tr key={g.id}>
                    <td>{g.name}</td>
                    <td>{g.mode === "required" ? t("gate.required") : t("gate.advisory")}</td>
                    <td><span className={`pl-gate-word ${state}`}>{t(`pl.gate.${state}` as Key)}</span></td>
                    <td className="num mono">{ev?.pass_rate != null ? `${ev.pass_rate.toLocaleString(lang)}%` : t("line.none")}</td>
                    <td className="num mono">{ev?.median_seconds != null ? formatDuration(ev.median_seconds, lang) : t("line.none")}</td>
                    <td className="num mono">{ev ? ev.runs : t("line.none")}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}

export function ProductionLine() {
  const t = useT();
  const { workspace } = useWorkspace();
  const lineRes = useResource<LinePayload>(factory(workspace.id, "/line"));
  const statusRes = useResource<FactoryStatus>(factory(workspace.id, "/status"), 5_000);
  const reportRes = useResource<Report>(factory(workspace.id, "/report"), 30_000);
  const telemetryRes = useResource<TelemetryResponse>(factory(workspace.id, "/telemetry?group=stage"), 30_000);
  const workflowRes = useResource<WorkflowPayload>(factory(workspace.id, "/workflows/factory-feature"));
  const intentsRes = useResource<{ entries: Intent[] }>(factory(workspace.id, "/intents"), 30_000);
  const [selected, setSelected] = useState<string | null>(null);
  const buttons = useRef<(HTMLButtonElement | null)[]>([]);

  if (!lineRes.data) {
    if (lineRes.error) return <EmptyState icon="alert" title={t("state.empty")} />;
    return <Loaded resource={lineRes} rows={4}>{() => <></>}</Loaded>;
  }

  const stages = lineRes.data.stages.map(normalizeStage);
  if (stages.length === 0) return <EmptyState icon="alert" title={t("pl.empty")} />;
  const status = statusRes.data;
  const steps = workflowRes.data?.body?.steps ?? [];
  const states = stationStates(stages, status, steps);
  const runningIndex = stages.findIndex((s) => states[s.id] === "running" || states[s.id] === "failed");
  const active = selected && stages.some((s) => s.id === selected) ? selected : stages[Math.max(runningIndex, 0)]!.id;
  const activeStage = stages.find((s) => s.id === active)!;
  const intent = status?.intent ? intentsRes.data?.entries.find((i) => i.id === status.intent) : undefined;
  const redAt = stages.findIndex((s) => s.id === "red");
  const prePushAt = stages.findIndex((s) => s.id === "pre-push");
  const hasLoop = redAt >= 0 && prePushAt > redAt;
  const n = stages.length;

  const onKey = (event: KeyboardEvent<HTMLButtonElement>, index: number) => {
    const delta = event.key === "ArrowRight" || event.key === "ArrowDown" ? 1 : event.key === "ArrowLeft" || event.key === "ArrowUp" ? -1 : 0;
    if (delta === 0) return;
    event.preventDefault();
    buttons.current[(index + delta + n) % n]?.focus();
  };

  const center = (i: number) => `${((i + 0.5) / n) * 100}%`;

  return (
    <div className="pl">
      <IntentStrip stages={stages} states={states} status={status} intent={intent} />
      <Legend />
      <div className="pl-scroll" role="region" aria-label={t("pl.region")} tabIndex={0}>
        <div className="pl-track" style={{ "--n": n } as CSSProperties}>
          {hasLoop && (
            <div
              className="pl-loop"
              data-testid="correction-loop"
              style={{ left: center(redAt), width: `${((prePushAt - redAt) / n) * 100}%` }}
            >
              <svg className="pl-loop-arc" viewBox="0 0 100 40" preserveAspectRatio="none" aria-hidden="true">
                <path d="M100 40 C100 4 0 4 0 40" vectorEffect="non-scaling-stroke" />
              </svg>
              <svg className="pl-loop-head" viewBox="0 0 12 12" aria-hidden="true">
                <path d="M1 3 L6 11 L11 3" />
              </svg>
              <span className="pl-loop-label">{t("pl.loop")}</span>
            </div>
          )}
          {runningIndex >= 0 && (
            <span
              className={`pl-token ${states[stages[runningIndex]!.id]}`}
              data-testid="line-token"
              data-stage={stages[runningIndex]!.id}
              style={{ left: center(runningIndex) }}
              aria-hidden="true"
            />
          )}
          {stages.map((stage, i) => {
            const state = states[stage.id]!;
            const kind = stationKind(stage);
            const throughput = stationThroughput(stage.id, telemetryRes.data?.rows ?? [], steps, stages);
            return (
              <div key={stage.id} className={`pl-col ${state}`}>
                {hasLoop && i === prePushAt && <p className="pl-loop-note">{t("pl.loop")}</p>}
                <span className={`pl-node ${state}`} aria-hidden="true" />
                <button
                  ref={(el) => { buttons.current[i] = el; }}
                  type="button"
                  className={`pl-station ${state}`}
                  data-testid="station"
                  data-stage={stage.id}
                  data-state={state}
                  aria-pressed={stage.id === active}
                  aria-current={state === "running" ? "step" : undefined}
                  onClick={() => setSelected(stage.id)}
                  onKeyDown={(e) => onKey(e, i)}
                >
                  <span className={`pl-lamp ${state}`} aria-hidden="true" />
                  <span className="pl-station-name">{stage.id}</span>
                  <span className={`status-pill ${PILL[state]}`}>{t(`pl.state.${state}` as Key)}</span>
                  <span className="pl-station-foot">
                    <span className="pl-station-kind"><KindMark kind={kind} />{t(`pl.kind.${kind}` as Key)}</span>
                    <ThroughputLine throughput={throughput} />
                  </span>
                </button>
                {stage.gates.length > 0 && (
                  <ul className="pl-gates" aria-label={t("pl.gatesOf", { stage: stage.id })}>
                    {stage.gates.map((g) => (
                      <GateChip key={g.id} name={g.name} mode={g.mode} state={gateState(evidenceFor(reportRes.data, stage.id, g.name)?.last_status)} />
                    ))}
                  </ul>
                )}
              </div>
            );
          })}
        </div>
      </div>
      <Detail stage={activeStage} kind={stationKind(activeStage)} state={states[active]!} report={reportRes.data} />
    </div>
  );
}
