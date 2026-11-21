import { useState } from "react";

import { api, factory } from "../api.ts";
import { ActionForm, Field } from "../components/Form.tsx";
import { Icon } from "../components/Icon.tsx";
import { Empty, Loaded, Section, time } from "../components/State.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useLang, useT } from "../i18n.ts";
import { ENVELOPE, type Entry, SECRET_FIELD } from "../model.ts";

export const KINDS = ["providers", "agents", "skills", "mcp", "workflows", "rules", "prompts", "hooks", "memory",
  "integrations"] as const;

const PRESETS: Record<string, string[]> = {
  providers: ["model", "endpoint", "api_key"],
  agents: ["provider", "model", "role"],
  mcp: ["command", "url"],
  integrations: ["url", "token"],
};

interface Row {
  name: string;
  value: string;
}

function fieldsOf(entry: Entry): Row[] {
  return Object.entries(entry)
    .filter(([key]) => !ENVELOPE.has(key))
    .map(([name, value]) => ({ name, value: typeof value === "string" ? value : JSON.stringify(value) }));
}

function Value({ name, value }: Row) {
  if (value.startsWith("env:")) {
    return (
      <span className="env" title={name}>
        <Icon name="key" size={14} />
        <span className="mono">{value.slice(4)}</span>
      </span>
    );
  }
  return <span className={SECRET_FIELD.test(name) ? "muted" : "mono"}>{value}</span>;
}

function parse(value: string): unknown {
  try {
    const parsed = JSON.parse(value);
    return typeof parsed === "object" ? parsed : value;
  } catch {
    return value;
  }
}

function FieldsEditor({ rows, onChange }: { rows: Row[]; onChange: (rows: Row[]) => void }) {
  const t = useT();
  const update = (index: number, patch: Partial<Row>) =>
    onChange(rows.map((row, i) => (i === index ? { ...row, ...patch } : row)));
  return (
    <fieldset className="fields-editor">
      <legend>{t("catalogs.fields")}</legend>
      <p className="field-hint">{t("catalogs.envHint")}</p>
      {rows.map((row, index) => {
        const secret = SECRET_FIELD.test(row.name);
        return (
          <div className="row row-tight" key={index}>
            <Field label={t("catalogs.fieldName")}>
              {(id) => <input id={id} className="mono" value={row.name} onChange={(e) => update(index, { name: e.target.value })} />}
            </Field>
            <Field label={t("catalogs.fieldValue")}>
              {(id) =>
                secret ? (
                  <span className="prefixed">
                    <span className="prefix mono" aria-hidden="true">env:</span>
                    <input
                      id={id}
                      className="mono"
                      pattern="[A-Z_][A-Z0-9_]*"
                      placeholder="PROVIDER_API_KEY"
                      aria-label={`${row.name} (env:NAME)`}
                      value={row.value.replace(/^env:/, "")}
                      onChange={(e) => update(index, { value: `env:${e.target.value.toUpperCase()}` })}
                    />
                  </span>
                ) : (
                  <input id={id} value={row.value} onChange={(e) => update(index, { value: e.target.value })} />
                )
              }
            </Field>
            <button
              type="button"
              className="btn btn-icon"
              aria-label={t("catalogs.removeField", { name: row.name || "—" })}
              onClick={() => onChange(rows.filter((_, i) => i !== index))}
            >
              <Icon name="minus" />
            </button>
          </div>
        );
      })}
      <button type="button" className="btn btn-quiet" onClick={() => onChange([...rows, { name: "", value: "" }])}>
        <Icon name="plus" size={16} />
        {t("catalogs.addField")}
      </button>
    </fieldset>
  );
}

function EntryForm({ kind, entry, onDone, onCancel }: {
  kind: string;
  entry?: Entry;
  onDone: (saved: Entry) => void;
  onCancel: () => void;
}) {
  const t = useT();
  const { workspace, writable } = useWorkspace();
  const [id, setId] = useState(entry?.id ?? "");
  const [summary, setSummary] = useState(entry?.summary ?? "");
  const [ref, setRef] = useState(entry?.ref ?? "");
  const [rows, setRows] = useState<Row[]>(
    entry ? fieldsOf(entry) : (PRESETS[kind] ?? []).map((name) => ({ name, value: SECRET_FIELD.test(name) ? "env:" : "" })),
  );
  const submit = async () => {
    const fields = Object.fromEntries(rows.filter((row) => row.name.trim() && row.value !== "" && row.value !== "env:").map((row) => [row.name.trim(), parse(row.value)]));
    const body = { id, summary, ref: ref || null, fields };
    const saved = entry
      ? await api<Entry>(factory(workspace.id, `/catalogs/${kind}/${encodeURIComponent(entry.id)}`), {
          method: "PUT", body, ifMatch: entry.version,
        })
      : await api<Entry>(factory(workspace.id, `/catalogs/${kind}`), { method: "POST", body });
    onDone(saved);
  };
  return (
    <ActionForm
      submitLabel={entry ? t("catalogs.save") : t("catalogs.create")}
      disabled={!writable}
      onSubmit={submit}
      footer={<button type="button" className="btn btn-quiet" onClick={onCancel}>{t("catalogs.cancel")}</button>}
    >
      {!entry && (
        <Field label={t("catalogs.id")}>
          {(fid) => <input id={fid} required className="mono" value={id} onChange={(e) => setId(e.target.value)} autoComplete="off" />}
        </Field>
      )}
      <Field label={t("catalogs.summary")}>
        {(fid) => <input id={fid} value={summary} onChange={(e) => setSummary(e.target.value)} />}
      </Field>
      <Field label={t("catalogs.ref")}>
        {(fid) => <input id={fid} className="mono" value={ref} onChange={(e) => setRef(e.target.value)} />}
      </Field>
      <FieldsEditor rows={rows} onChange={setRows} />
    </ActionForm>
  );
}

function EntryDetail({ kind, entry, onSaved }: { kind: string; entry: Entry; onSaved: () => void }) {
  const t = useT();
  const lang = useLang();
  const { workspace, writable } = useWorkspace();
  const [editing, setEditing] = useState(false);
  const versions = useResource<{ versions: { version: number; digest: string; created_by?: string; created_at?: string }[] }>(
    factory(workspace.id, `/catalogs/${kind}/${encodeURIComponent(entry.id)}/versions`),
  );
  const rows = fieldsOf(entry);
  return (
    <div className="detail">
      <header className="detail-head">
        <h3 className="mono">{entry.id}</h3>
        <span className="tag">v{entry.version}</span>
        {writable && !editing && (
          <button type="button" className="btn btn-quiet" onClick={() => setEditing(true)}>{t("catalogs.edit")}</button>
        )}
      </header>
      {editing ? (
        <EntryForm kind={kind} entry={entry} onCancel={() => setEditing(false)} onDone={() => { setEditing(false); onSaved(); versions.reload(); }} />
      ) : (
        <>
          {entry.summary && <p>{entry.summary}</p>}
          <dl className="facts">
            <dt>{t("catalogs.ref")}</dt>
            <dd className="mono">{entry.ref ?? "—"}</dd>
            <dt>{t("catalogs.by")}</dt>
            <dd>{entry.created_by ?? "—"} <span className="muted small">{time(entry.created_at, lang)}</span></dd>
          </dl>
          <h4>{t("catalogs.fields")}</h4>
          {rows.length === 0 ? <Empty>{t("catalogs.noFields")}</Empty> : (
            <dl className="facts">
              {rows.map((row) => (
                <div key={row.name} className="fact">
                  <dt className="mono">{row.name}</dt>
                  <dd><Value {...row} /></dd>
                </div>
              ))}
            </dl>
          )}
        </>
      )}
      <h4>{t("catalogs.versions")}</h4>
      <Loaded resource={versions} rows={2}>
        {(data) => (
          <ol className="timeline">
            {[...data.versions].reverse().map((v) => (
              <li key={v.version}>
                <span className="tag">v{v.version}</span>
                <span className="mono muted small">{v.digest.slice(0, 12)}</span>
                <span className="small">{v.created_by ?? ""}</span>
                <time className="muted small">{time(v.created_at, lang)}</time>
              </li>
            ))}
          </ol>
        )}
      </Loaded>
    </div>
  );
}

export function Catalogs({ kind }: { kind: string }) {
  const t = useT();
  const { workspace, writable, go } = useWorkspace();
  const list = useResource<{ entries: Entry[] }>(factory(workspace.id, `/catalogs/${kind}`));
  const [selected, setSelected] = useState<string>();
  const [creating, setCreating] = useState(false);
  const current = list.data?.entries.find((entry) => entry.id === selected);
  const detail = useResource<Entry>(current ? factory(workspace.id, `/catalogs/${kind}/${encodeURIComponent(current.id)}`) : null);
  return (
    <div className="stack">
      <nav className="tabs" aria-label={t("catalogs.kinds")}>
        {KINDS.map((k) => (
          <a key={k} href={`#/${workspace.id}/catalogs/${k}`} aria-current={k === kind ? "page" : undefined}
            onClick={() => { setSelected(undefined); setCreating(false); }}>
            {k}
          </a>
        ))}
      </nav>
      <Section
        title={kind}
        actions={writable && !creating && (
          <button type="button" className="btn btn-primary" onClick={() => { setCreating(true); setSelected(undefined); }}>
            <Icon name="plus" size={16} />
            {t("catalogs.new")}
          </button>
        )}
      >
        <div className="master">
          <Loaded resource={list}>
            {(data) =>
              data.entries.length === 0 ? <Empty>{t("catalogs.empty", { kind })}</Empty> : (
                <ul className="entries">
                  {data.entries.map((entry) => (
                    <li key={entry.id}>
                      <button type="button" className="entry" aria-current={entry.id === selected ? "true" : undefined}
                        onClick={() => { setSelected(entry.id); setCreating(false); }}>
                        <span className="mono">{entry.id}</span>
                        <span className="muted small">{entry.summary}</span>
                        <span className="tag">v{entry.version}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              )
            }
          </Loaded>
          {creating ? (
            <div className="detail">
              <EntryForm kind={kind} onCancel={() => setCreating(false)}
                onDone={(saved) => { setCreating(false); list.reload(); setSelected(saved.id); go(`${workspace.id}/catalogs/${kind}`); }} />
            </div>
          ) : current ? (
            <Loaded resource={detail}>
              {(entry) => <EntryDetail kind={kind} entry={entry} onSaved={() => { detail.reload(); list.reload(); }} />}
            </Loaded>
          ) : (
            <p className="detail empty">{t("catalogs.select")}</p>
          )}
        </div>
      </Section>
    </div>
  );
}
