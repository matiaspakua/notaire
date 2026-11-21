import { factory } from "../api.ts";
import { Icon } from "../components/Icon.tsx";
import { Kpi, Loaded } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { type Key, useT } from "../i18n.ts";
import type { Capabilities, Run } from "../model.ts";

const GROUPS: { id: string; label: Key; phase: string; statuses: string[] }[] = [
  { id: "flow", label: "line.flow", phase: "", statuses: ["requested", "queued", "running"] },
  { id: "outcome", label: "line.outcome", phase: "prove", statuses: ["succeeded", "failed", "blocked", "cancelled"] },
];
/** A station with runs takes the tone of what those runs need: queued ones wait for a runner adapter. */
const BUSY: Record<string, string> = {
  requested: "waiting", queued: "waiting", running: "running", succeeded: "done", failed: "error", blocked: "waiting",
  cancelled: "idle",
};
const ORDER = GROUPS.flatMap((group) => group.statuses);

function Station({ status, runs }: { status: string; runs: Run[] }) {
  const t = useT();
  const { workspace, go } = useWorkspace();
  const count = runs.length;
  const label = t(`status.${status}` as Key);
  const tone = count ? BUSY[status] ?? "idle" : "idle";
  return (
    <li>
      <button
        type="button"
        className="station-card"
        data-status={tone}
        aria-label={t("line.station", { status: label, n: count })}
        onClick={() => go(`${workspace.id}/runs?status=${status}`)}
      >
        <span className="station-header">
          <span className="station-number">{ORDER.indexOf(status) + 1}</span>
          <span className={`status-pill ${tone}`}>{t(`station.${tone}` as Key)}</span>
        </span>
        <span className="station-title">{label}</span>
        <span className="station-count">{count}</span>
        <span className="station-agent">
          <Icon name="runs" size={14} />
          {t(`station.about.${status}` as Key)}
        </span>
        {count > 0 && (
          <span className="station-tags">
            {runs.slice(0, 3).map((run) => <span key={run.id} className="tag">{run.id}</span>)}
            {count > 3 && <span className="tag">+{count - 3}</span>}
          </span>
        )}
      </button>
    </li>
  );
}

function ProductionLine({ runs }: { runs: Run[] }) {
  const t = useT();
  return (
    <div className="production-line">
      {GROUPS.map((group) => {
        const total = runs.filter((run) => group.statuses.includes(run.status)).length;
        return (
          <section key={group.id} className="phase-group" aria-labelledby={`phase-${group.id}`}>
            <header className="phase-header">
              <span className={`phase-indicator ${group.phase}`} aria-hidden="true" />
              <h2 className="phase-name" id={`phase-${group.id}`}>{t(group.label)}</h2>
              <span className="phase-meta">{total}/{runs.length}</span>
            </header>
            <ol className="stations">
              {group.statuses.map((status) => (
                <Station key={status} status={status} runs={runs.filter((run) => run.status === status)} />
              ))}
            </ol>
          </section>
        );
      })}
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
  const runs = useRuns();
  return (
    <div className="stack">
      <Loaded resource={runs} rows={4}>{(data) => <ProductionLine runs={data.entries} />}</Loaded>
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
