import { useState } from "react";

import { factory } from "../api.ts";
import { Icon } from "../components/Icon.tsx";
import { EmptyState, Kpi, Loaded, StatusBadge } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { type Key, useT } from "../i18n.ts";
import type { Capabilities, FactoryStatus, LinePayload, Run } from "../model.ts";
import { stationState } from "../model.ts";

function GatePill({ gate }: { gate: { name: string; mode: string } }) {
  const t = useT();
  return (
    <span className="tag gate" data-testid="gate" title={`${gate.mode}: ${gate.name}`}>
      <span>{gate.mode === "required" ? t("gate.required") : t("gate.advisory")}</span>
      <span className="faint">·</span>
      <span>{gate.name}</span>
    </span>
  );
}

function StationCard({
  station,
  index,
  guardrails,
  stageGuardrails,
}: {
  station: {
    id: string;
    gates: { id: string; name: string; required: boolean; mode: string }[];
    inputs: string[];
    outputs: string[];
    agents: string[];
    skills: string[];
    mcp: string[];
    model?: string | null;
    effort: Record<string, number>;
    state: "done" | "current" | "upcoming";
  };
  index: number;
  guardrails: {
    protected: { paths: string[] };
    hooks: string[];
    rules: unknown[];
    isolator: { status?: string; sandbox: boolean; enforces_committer: boolean };
  };
  stageGuardrails: Array<{ kind: string; id: string; write_set?: string[] }>;
}) {
  const t = useT();
  const [expanded, setExpanded] = useState(false);
  const tone = station.state === "current" ? "running" : station.state === "done" ? "done" : "idle";
  return (
    <div>
      <button
        type="button"
        className="station-card"
        data-testid="station"
        data-status={tone}
        aria-expanded={expanded}
        aria-current={station.state === "current" ? "step" : undefined}
        onClick={() => setExpanded((v) => !v)}
      >
        <span className="station-header">
          <span className="station-number">{index + 1}</span>
          <span className="station-title">{station.id}</span>
          {station.state === "current" && (
            <span className="status-pill running">{t("station.current")}</span>
          )}
          {station.state !== "current" && (
            <span className={`status-pill ${tone}`}>{t(`station.${tone}` as Key)}</span>
          )}
        </span>
        {station.gates.length > 0 && (
          <span className="station-tags">
            {station.gates.map((g) => (
              <GatePill key={g.id} gate={g} />
            ))}
          </span>
        )}
        {station.agents.length > 0 && (
          <span className="station-agent">
            <Icon name="agent" size={14} />
            {station.agents.join(", ")}
          </span>
        )}
        {station.model && <span className="tag">{station.model}</span>}
      </button>
      {expanded && (
        <div className="card" data-testid="station-details" style={{ marginTop: "var(--space-2)" }}>
          <dl className="facts">
            <dt className="section-label">{t("line.agents")}</dt>
            <dd>{station.agents.length ? station.agents.join(", ") : t("line.none")}</dd>
            <dt className="section-label">{t("line.skills")}</dt>
            <dd>{station.skills.length ? station.skills.join(", ") : t("line.none")}</dd>
            <dt className="section-label">{t("line.mcp")}</dt>
            <dd>{station.mcp.length ? station.mcp.join(", ") : t("line.none")}</dd>
            <dt className="section-label">{t("line.model")}</dt>
            <dd>{station.model ?? t("line.none")}</dd>
            {Object.keys(station.effort).length > 0 && (
              <>
                <dt className="section-label">{t("line.effort")}</dt>
                <dd>
                  {Object.entries(station.effort).map(([k, v]) => (
                    <span key={k} className="tag">{k}: {v}</span>
                  ))}
                </dd>
              </>
            )}
            <dt className="section-label">{t("line.inputs")}</dt>
            <dd data-testid="stage-inputs">
              {station.inputs.length ? station.inputs.map((i) => <span key={i} className="tag">{i}</span>) : t("line.none")}
            </dd>
            <dt className="section-label">{t("line.outputs")}</dt>
            <dd data-testid="stage-outputs">
              {station.outputs.length ? station.outputs.map((o) => <span key={o} className="tag">{o}</span>) : t("line.none")}
            </dd>
          </dl>
          <div className="section-label">{t("line.guardrails")}</div>
          <div className="stack-tight">
            <span className="small muted">
              {t("line.protected", { n: guardrails.protected.paths.length })}
              {guardrails.protected.paths.length > 0 && (
                <span className="faint">: {guardrails.protected.paths.join(", ")}</span>
              )}
            </span>
            <span className="small muted">
              {t("line.hooks", { n: guardrails.hooks.length })}
            </span>
            <span className="small muted">
              {t("line.rules", { n: guardrails.rules.length })}
            </span>
            <span className="small muted">
              {t("line.isolator", {
                status: guardrails.isolator.status ?? t("line.none"),
                sandbox: guardrails.isolator.sandbox ? t("cap.on") : t("cap.off"),
                committer: guardrails.isolator.enforces_committer ? t("cap.on") : t("cap.off"),
              })}
            </span>
            {stageGuardrails.length > 0 && (
              <span className="small muted">
                {t("line.stageGrants", { n: stageGuardrails.length })}
                {stageGuardrails.length > 0 && (
                  <span className="faint">: {stageGuardrails.map((g) => g.id).join(", ")}</span>
                )}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function ProductionLineTimeline() {
  const t = useT();
  const { workspace } = useWorkspace();
  const lineRes = useResource<LinePayload>(factory(workspace.id, "/line"));
  const statusRes = useResource<FactoryStatus>(factory(workspace.id, "/status"), 5_000);

  if (!lineRes.data) {
    if (lineRes.error) return <EmptyState icon="alert" title={t("state.empty")} />;
    return <Loaded resource={lineRes} rows={4}>{() => <></>}</Loaded>;
  }

  const status = statusRes.data;

  return (
    <div className="production-line">
      <div className="stack" style={{ gap: "var(--space-4)" }}>
        {lineRes.data.stages.map((stage, i) => {
          const state = stationState(stage.id, status);
          return (
            <StationCard
              key={stage.id}
              index={i}
              guardrails={lineRes.data!.guardrails}
              stageGuardrails={stage.guardrails}
              station={{
                id: stage.id,
                gates: stage.gates,
                inputs: stage.inputs,
                outputs: stage.outputs,
                agents: stage.agents,
                skills: stage.skills,
                mcp: stage.mcp,
                model: stage.model,
                effort: stage.effort,
                state,
              }}
            />
          );
        })}
      </div>
      {status?.previous.length && (
        <div className="stack-tight">
          <div className="section-label">{t("line.previous")}</div>
          <ul className="timeline">
            {status.previous.map((p) => (
              <li key={p.step}>
                <StatusBadge status={p.status} />
                <span className="mono">{p.step}</span>
                {p.stage && <span className="tag">{p.stage}</span>}
                {p.duration_s != null && <span className="small muted">{p.duration_s.toFixed(1)}s</span>}
                {p.usd != null && <span className="small muted">${p.usd.toFixed(4)}</span>}
              </li>
            ))}
          </ul>
        </div>
      )}
      {status?.next.length && (
        <div className="stack-tight">
          <div className="section-label">{t("line.next")}</div>
          <ul className="timeline">
            {status.next.map((n) => (
              <li key={n.step}>
                <span className="mono">{n.step}</span>
                {n.stage && <span className="tag">{n.stage}</span>}
                {n.source && <span className="small muted">{n.source}</span>}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

function CapabilityCards({ caps }: { caps: Capabilities }) {
  const t = useT();
  const state = (on: boolean) => (on ? t("cap.on") : t("cap.off"));
  return (
    <div className="kpi-row">
      <Kpi label={t("cap.runner")} value={state(caps.runner)} tone={caps.runner ? "prove" : "idle"}
        sub={caps.runner ? t("cap.runner.on") : t("cap.runner.off")} />
      <Kpi label={t("cap.approvals")} value={state(caps.approvals_live)} tone={caps.approvals_live ? "prove" : "idle"} />
      <Kpi label={t("cap.forge")} value={state(caps.forge_write)} tone={caps.forge_write ? "prove" : "idle"} />
      <Kpi label={t("cap.sandbox")} value={state(caps.isolator.sandbox)} tone={caps.isolator.sandbox ? "prove" : "idle"}
        sub={caps.isolator.backend ?? undefined} />
    </div>
  );
}

export function useRuns() {
  const { workspace } = useWorkspace();
  return useResource<{ entries: Run[] }>(factory(workspace.id, "/runs"), 5_000);
}

export function Overview() {
  const t = useT();
  const { workspace } = useWorkspace();
  const caps = useResource<Capabilities>(factory(workspace.id, "/capabilities"), 30_000);
  return (
    <div className="stack">
      <ProductionLineTimeline />
      <section className="phase-group" aria-labelledby="phase-caps">
        <header className="phase-header">
          <span className="phase-indicator plan" aria-hidden="true" />
          <h2 className="phase-name" id="phase-caps">{t("cap.title")}</h2>
        </header>
        <Loaded resource={caps} rows={2}>{(data) => <CapabilityCards caps={data} />}</Loaded>
      </section>
    </div>
  );
}
