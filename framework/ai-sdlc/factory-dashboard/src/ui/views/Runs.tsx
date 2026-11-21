import { useState } from "react";

import { api, factory } from "../api.ts";
import { ActionForm, Field } from "../components/Form.tsx";
import { Empty, Loaded, Section, StatusBadge, time } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { hashQuery, useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import type { Project, Run, RunEvent, Workflow } from "../model.ts";

const STATUSES = ["requested", "queued", "running", "succeeded", "failed", "blocked", "cancelled"];

const workflowName = (run: Run) =>
  typeof run.workflow === "string" ? run.workflow : `${run.workflow.id}${run.workflow.version ? ` v${run.workflow.version}` : ""}`;

function RunDetail({ run }: { run: Run }) {
  const t = useT();
  const lang = useLang();
  const { workspace } = useWorkspace();
  const events = useResource<{ events: RunEvent[] }>(factory(workspace.id, `/runs/${encodeURIComponent(run.id)}/events`), 5_000);
  return (
    <div className="detail">
      <header className="detail-head">
        <h3 className="mono">{run.id}</h3>
        <StatusBadge status={run.status} />
      </header>
      <dl className="facts">
        <dt>{t("runs.project")}</dt>
        <dd>{run.project}</dd>
        <dt>{t("runs.workflow")}</dt>
        <dd className="mono">{workflowName(run)}</dd>
        <dt>{t("runs.by")}</dt>
        <dd>{run.requested_by ?? "—"}</dd>
        <dt>{t("runs.at")}</dt>
        <dd>{time(run.requested_at, lang)}</dd>
      </dl>
      <h4>{t("runs.steps")}</h4>
      {run.steps && run.steps.length > 0 ? (
        <ol className="steps">
          {run.steps.map((step) => (
            <li key={step.id}>
              <span className="mono">{step.id}</span>
              <StatusBadge status={step.status} />
            </li>
          ))}
        </ol>
      ) : (
        <Empty>—</Empty>
      )}
      <h4>{t("runs.events")}</h4>
      <Loaded resource={events} rows={2}>
        {(data) =>
          data.events.length === 0 ? (
            <Empty>{t("runs.noEvents")}</Empty>
          ) : (
            <ol className="timeline">
              {data.events.map((event, index) => (
                <li key={`${event.kind}-${index}`}>
                  <time className="muted small">{time(event.ts, lang)}</time>
                  <span className="mono">{event.kind}</span>
                  {event.step && <span className="mono muted">{event.step}</span>}
                  {event.step_status && <StatusBadge status={event.step_status} />}
                  {event.actor && <span className="muted small">{event.actor}</span>}
                </li>
              ))}
            </ol>
          )
        }
      </Loaded>
    </div>
  );
}

function RequestRun({ onDone }: { onDone: () => void }) {
  const t = useT();
  const { workspace, writable } = useWorkspace();
  const projects = useResource<{ entries: Project[] }>(factory(workspace.id, "/projects"));
  const workflows = useResource<{ entries: Workflow[] }>(factory(workspace.id, "/workflows?approved=true"));
  const [project, setProject] = useState("");
  const [workflow, setWorkflow] = useState("");
  const projectList = projects.data?.entries ?? [];
  const workflowList = workflows.data?.entries ?? [];
  const ready = projectList.length > 0 && workflowList.length > 0;
  return (
    <Section title={t("runs.request")}>
      <p className="lede">{t("runs.requestHint")}</p>
      {projects.data && workflows.data && !ready ? (
        <Empty>{t("runs.none")}</Empty>
      ) : (
        <ActionForm
          submitLabel={t("runs.submit")}
          disabled={!writable || !ready}
          onSubmit={async () => {
            await api(factory(workspace.id, "/runs"), {
              method: "POST",
              body: { project: project || projectList[0]?.id, workflow: workflow || workflowList[0]?.id },
            });
            onDone();
          }}
        >
          <div className="row">
            <Field label={t("runs.projectPick")}>
              {(id) => (
                <select id={id} value={project} onChange={(event) => setProject(event.target.value)}>
                  {projectList.map((p) => <option key={p.id} value={p.id}>{p.id}</option>)}
                </select>
              )}
            </Field>
            <Field label={t("runs.workflowPick")}>
              {(id) => (
                <select id={id} value={workflow} onChange={(event) => setWorkflow(event.target.value)}>
                  {workflowList.map((w) => (
                    <option key={w.id} value={w.id}>{w.id}{w.version ? ` v${w.version}` : ""}</option>
                  ))}
                </select>
              )}
            </Field>
          </div>
        </ActionForm>
      )}
    </Section>
  );
}

export function Runs() {
  const t = useT();
  const lang = useLang();
  const { workspace } = useWorkspace();
  const runs = useResource<{ entries: Run[] }>(factory(workspace.id, "/runs"), 5_000);
  const [filter, setFilter] = useState<string>(() => hashQuery().get("status") ?? "");
  const [selected, setSelected] = useState<string>();
  return (
    <div className="stack">
      <Section title={t("runs.list")}>
        <div className="chips" role="group" aria-label={t("runs.status")}>
          {["", ...STATUSES].map((status) => (
            <button
              key={status || "all"}
              type="button"
              className="chip"
              aria-pressed={filter === status}
              onClick={() => setFilter(status)}
            >
              {status ? <StatusBadge status={status} /> : t("runs.all")}
            </button>
          ))}
        </div>
        <Loaded resource={runs}>
          {(data) => {
            const rows = data.entries.filter((run) => !filter || run.status === filter);
            const chosen = data.entries.find((run) => run.id === selected);
            if (data.entries.length === 0) {
              return <Empty>{t("runs.empty")}</Empty>;
            }
            return (
              <div className="master">
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th scope="col">{t("runs.id")}</th>
                        <th scope="col">{t("runs.project")}</th>
                        <th scope="col">{t("runs.workflow")}</th>
                        <th scope="col">{t("runs.status")}</th>
                        <th scope="col">{t("runs.at")}</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((run) => (
                        <tr key={run.id} aria-selected={run.id === selected}>
                          <th scope="row">
                            <button type="button" className="link mono" onClick={() => setSelected(run.id)}>
                              {run.id}
                            </button>
                          </th>
                          <td>{run.project}</td>
                          <td className="mono">{workflowName(run)}</td>
                          <td><StatusBadge status={run.status} /></td>
                          <td className="muted">{time(run.requested_at, lang)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {chosen ? <RunDetail run={chosen} /> : <p className="detail empty">{t("runs.select")}</p>}
              </div>
            );
          }}
        </Loaded>
      </Section>
      <RequestRun onDone={runs.reload} />
    </div>
  );
}
