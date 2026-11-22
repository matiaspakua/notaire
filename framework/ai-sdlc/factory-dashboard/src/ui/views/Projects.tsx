import { useEffect, useRef, useState } from "react";

import { api, factory } from "../api.ts";
import { ActionForm, Field } from "../components/Form.tsx";
import { Icon } from "../components/Icon.tsx";
import { Empty, Loaded, Section } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useT } from "../i18n.ts";
import { buildConfigTree, encodeConfigPath, type ConfigFileEntry, type ConfigFileContent, type ConfigTreeNode, type Project } from "../model.ts";

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

function FileTreeItem({ node, selected, onSelect }: { node: ConfigTreeNode; selected: string | null; onSelect: (path: string) => void }) {
  const isFile = Boolean(node.path);
  const isSelected = node.path === selected;
  if (isFile) {
    return (
      <li role="treeitem">
        <button
          type="button"
          className="file-entry-btn"
          data-testid="file-entry"
          aria-current={isSelected ? "true" : undefined}
          onClick={() => onSelect(node.path!)}
        >
          <Icon name="projects" size={14} />
          <span className="file-entry-name">{node.name}</span>
        </button>
      </li>
    );
  }
  return (
    <li role="treeitem">
      <details className="file-tree-details">
        <summary>
          <Icon name="config" size={14} />
          <span className="file-entry-name">{node.name}</span>
        </summary>
        <ul className="file-tree-list" role="group">
          {node.children.map((child) => (
            <FileTreeItem key={child.name} node={child} selected={selected} onSelect={onSelect} />
          ))}
        </ul>
      </details>
    </li>
  );
}

function FileViewer({ path, workspaceId }: { path: string; workspaceId: string }) {
  const t = useT();
  const scrollRef = useRef<HTMLDivElement>(null);
  const content = useResource<ConfigFileContent>(
    factory(workspaceId, `/config/files/${encodeConfigPath(path)}`),
  );
  useEffect(() => {
    scrollRef.current?.scrollTo(0, 0);
  }, [path]);
  if (content.data === undefined) {
    return (
      <div className="skeleton" role="status" aria-busy="true" aria-label={t("projects.configLoading")}>
        {Array.from({ length: 6 }, (_, i) => (
          <span key={i} className="skeleton-row" />
        ))}
      </div>
    );
  }
  if (content.error) {
    return <div className="note note-error" role="alert"><Icon name="alert" /><span>{content.error.detail}</span></div>;
  }
  const lines = content.data.content.split("\n");
  return (
    <div
      ref={scrollRef}
      className="file-viewer-scroll"
      data-testid="file-viewer"
      tabIndex={0}
      aria-label={path}
    >
      {content.data.redacted && (
        <div className="file-viewer-note" role="status">
          <Icon name="key" size={14} />
          <span>{t("projects.configRedacted")}</span>
        </div>
      )}
      <pre className="file-viewer-pre"><code>{lines.map((line, i) => (
        <span key={i} className="file-viewer-line">
          <span className="file-viewer-ln" data-testid={i === 0 ? "line-number" : undefined} aria-hidden="true">{i + 1}</span>
          {line || " "}
        </span>
      ))}</code></pre>
    </div>
  );
}

function ConfigAsCode({ workspaceId }: { workspaceId: string }) {
  const t = useT();
  const files = useResource<ConfigFileEntry[]>(factory(workspaceId, "/config/files"));
  const [selected, setSelected] = useState<string | null>(null);
  const tree = files.data ? buildConfigTree(files.data) : [];
  return (
    <div className="config-as-code">
      <div className="config-tree-panel">
        <h3 className="section-label">{t("projects.configAsCode")}</h3>
        <div data-testid="file-tree" role="tree" aria-label={t("projects.configAsCode")}>
          <Loaded resource={files}>
            {(data) =>
              data.length === 0 ? (
                <Empty>{t("projects.configEmpty")}</Empty>
              ) : (
                <ul className="file-tree-list" role="group">
                  {tree.map((node) => (
                    <FileTreeItem key={node.name} node={node} selected={selected} onSelect={setSelected} />
                  ))}
                </ul>
              )
            }
          </Loaded>
        </div>
      </div>
      <div className="config-viewer-panel">
        {selected ? (
          <FileViewer path={selected} workspaceId={workspaceId} />
        ) : (
          <Empty>{t("projects.configSelect")}</Empty>
        )}
      </div>
    </div>
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
      <ConfigAsCode workspaceId={workspace.id} />
      <NewProject onDone={projects.reload} />
    </div>
  );
}
