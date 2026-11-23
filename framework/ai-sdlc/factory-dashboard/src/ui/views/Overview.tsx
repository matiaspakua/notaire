import { factory } from "../api.ts";
import { ProductionLine } from "../components/ProductionLine.tsx";
import { Kpi, Loaded, StatusBadge } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import type { Capabilities, FactoryStatus, Run } from "../model.ts";
import { formatDuration } from "../model.ts";

function StepLog({ status }: { status: FactoryStatus }) {
  const t = useT();
  const lang = useLang();
  if (!status.previous.length && !status.next.length) return null;
  return (
    <div className="step-log">
      {status.previous.length > 0 && (
        <div className="stack-tight">
          <h3 className="section-label">{t("line.previous")}</h3>
          <ul className="timeline">
            {status.previous.map((p) => (
              <li key={`${p.step}-${p.started_at ?? ""}`}>
                <StatusBadge status={p.status} />
                <span className="mono">{p.step}</span>
                {p.stage && <span className="tag">{p.stage}</span>}
                {p.duration_s != null && <span className="small muted">{formatDuration(p.duration_s, lang)}</span>}
                {p.usd != null && <span className="small muted">${p.usd.toFixed(4)}</span>}
              </li>
            ))}
          </ul>
        </div>
      )}
      {status.next.length > 0 && (
        <div className="stack-tight">
          <h3 className="section-label">{t("line.next")}</h3>
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
  const status = useResource<FactoryStatus>(factory(workspace.id, "/status"), 5_000);
  return (
    <div className="stack">
      <section className="phase-group" aria-label={t("pl.region")}>
        <ProductionLine />
        {status.data && <StepLog status={status.data} />}
      </section>
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
