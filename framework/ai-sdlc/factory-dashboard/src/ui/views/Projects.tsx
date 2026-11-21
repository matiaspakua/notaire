import { useState } from "react";

import { api, factory } from "../api.ts";
import { ActionForm, Field } from "../components/Form.tsx";
import { Empty, Loaded, Section } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useT } from "../i18n.ts";
import type { Project } from "../model.ts";

function AddRepo({ project, onDone }: { project: string; onDone: () => void }) {
  const t = useT();
  const { workspace, writable } = useWorkspace();
  const [repo, setRepo] = useState({ id: "", url: "", default_branch: "main", mode: "dry_run" });
  const set = (key: keyof typeof repo) => (event: { target: { value: string } }) =>
    setRepo((prev) => ({ ...prev, [key]: event.target.value }));
  return (
    <details className="disclosure">
      <summary>{t("projects.addRepo")}</summary>
      <ActionForm
        submitLabel={t("projects.repoSubmit")}
        disabled={!writable}
        onSubmit={async () => {
          await api(factory(workspace.id, `/projects/${encodeURIComponent(project)}/repos`), { method: "POST", body: repo });
          setRepo({ id: "", url: "", default_branch: "main", mode: "dry_run" });
          onDone();
        }}
      >
        <div className="row">
          <Field label={t("projects.repoId")}>
            {(id) => <input id={id} required value={repo.id} onChange={set("id")} autoComplete="off" />}
          </Field>
          <Field label={t("projects.url")}>
            {(id) => <input id={id} type="url" value={repo.url} onChange={set("url")} placeholder="https://github.com/org/repo" />}
          </Field>
        </div>
        <div className="row">
          <Field label={t("projects.branch")}>
            {(id) => <input id={id} value={repo.default_branch} onChange={set("default_branch")} />}
          </Field>
          <Field label={t("projects.mode")}>
            {(id) => (
              <select id={id} value={repo.mode} onChange={set("mode")}>
                <option value="dry_run">{t("projects.mode.dry_run")}</option>
                <option value="live">{t("projects.mode.live")}</option>
              </select>
            )}
          </Field>
        </div>
      </ActionForm>
    </details>
  );
}

function NewProject({ onDone }: { onDone: () => void }) {
  const t = useT();
  const { workspace, writable } = useWorkspace();
  const [id, setId] = useState("");
  const [profile, setProfile] = useState("aisdlc.toml");
  return (
    <Section title={t("projects.create")}>
      <ActionForm
        submitLabel={t("projects.submit")}
        disabled={!writable}
        onSubmit={async () => {
          await api(factory(workspace.id, "/projects"), { method: "POST", body: { id, profile_ref: profile } });
          setId("");
          onDone();
        }}
      >
        <div className="row">
          <Field label={t("projects.id")}>
            {(fid) => <input id={fid} required value={id} onChange={(e) => setId(e.target.value)} autoComplete="off" />}
          </Field>
          <Field label={t("projects.profile")}>
            {(fid) => <input id={fid} required value={profile} onChange={(e) => setProfile(e.target.value)} className="mono" />}
          </Field>
        </div>
      </ActionForm>
    </Section>
  );
}

export function Projects() {
  const t = useT();
  const { workspace } = useWorkspace();
  const projects = useResource<{ entries: Project[] }>(factory(workspace.id, "/projects"));
  return (
    <div className="stack">
      <Section title={t("projects.all")}>
        <Loaded resource={projects}>
          {(data) =>
            data.entries.length === 0 ? (
              <Empty>{t("projects.empty")}</Empty>
            ) : (
              <ul className="projects">
                {data.entries.map((project) => (
                  <li key={project.id} className="project">
                    <header className="detail-head">
                      <h3 className="detail-title">{project.id}</h3>
                      <span className="tag">{project.profile_ref}</span>
                    </header>
                    {project.repos.length === 0 ? (
                      <Empty>{t("projects.noRepos")}</Empty>
                    ) : (
                      <div className="table-wrap">
                        <table>
                          <caption className="sr-only">{t("projects.repos")}</caption>
                          <thead>
                            <tr>
                              <th scope="col">{t("projects.repoId")}</th>
                              <th scope="col">{t("projects.url")}</th>
                              <th scope="col">{t("projects.branch")}</th>
                              <th scope="col">{t("projects.mode")}</th>
                            </tr>
                          </thead>
                          <tbody>
                            {project.repos.map((repo) => (
                              <tr key={repo.id}>
                                <th scope="row">{repo.id}</th>
                                <td className="mono small">{repo.url || "—"}</td>
                                <td className="mono">{repo.default_branch}</td>
                                <td>
                                  <span className={`tag ${repo.mode === "live" ? "live" : ""}`}>{t(`projects.mode.${repo.mode}`)}</span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                    <AddRepo project={project.id} onDone={projects.reload} />
                  </li>
                ))}
              </ul>
            )
          }
        </Loaded>
      </Section>
      <NewProject onDone={projects.reload} />
    </div>
  );
}
