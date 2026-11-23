import { useEffect, useRef, useState } from "react";

import { factory, submitIntent } from "../api.ts";
import { Icon } from "./Icon.tsx";
import { useWorkspace } from "../context.ts";
import { useResource } from "../hooks.ts";
import { useT } from "../i18n.ts";
import { type Intent, type IntentRef, isSafeLink, pickIntent } from "../model.ts";

type IntentKind = "prompt" | "reference" | "spec";

function IntentDrawer({ intent, onClose }: { intent: Intent | null; onClose: () => void }) {
  const t = useT();
  const { workspace, grant } = useWorkspace();
  const [kind, setKind] = useState<IntentKind>("prompt");
  const [title, setTitle] = useState("");
  const [text, setText] = useState("");
  const [refSystem, setRefSystem] = useState("github");
  const [refId, setRefId] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const focusRef = useRef<HTMLButtonElement | null>(null);

  useEffect(() => {
    focusRef.current?.focus();
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSending(true);
    try {
      const body = kind === "prompt"
        ? { text: text || title, title: title || undefined }
        : kind === "reference"
          ? { ref: `${refSystem}:${refId}`, title: title || undefined }
          : { spec: text, title: title || undefined };
      await submitIntent(workspace.id, body);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submit failed");
    } finally {
      setSending(false);
    }
  };

  const writable = grant.write_enabled;

  return (
    <div
      className="card"
      data-testid="intent-drawer"
      role="dialog"
      aria-modal="true"
      aria-label={t("intent.drawer")}
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 100,
        background: "var(--bg-overlay)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "var(--space-4)",
      }}
      onClick={onClose}
    >
      <div
        className="card"
        style={{ maxWidth: 560, width: "100%", maxHeight: "80vh", overflow: "auto" }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="card-head">
          <h2 className="card-title">{t("intent.drawer")}</h2>
          <button ref={focusRef} type="button" className="btn btn-icon" onClick={onClose} aria-label={t("catalogs.cancel")}>
            <Icon name="close" />
          </button>
        </div>

        {intent && (
          <div className="stack-tight">
            <div className="detail-head">
              <span className="detail-title">{intent.title}</span>
              <span className={`tag scope-${intent.kind === "spec" ? "owner" : intent.kind === "reference" ? "write" : "read"}`}>
                {intent.kind}
              </span>
              <span className="status-pill done">{intent.status}</span>
            </div>
            {intent.text && <p className="small muted">{intent.text}</p>}
            {intent.acceptance.length > 0 && (
              <div>
                <div className="section-label">{t("intent.acceptance")}</div>
                <ul className="entries" data-testid="acceptance-criteria">
                  {intent.acceptance.map((a, i) => (
                    <li key={i} className="small">{a}</li>
                  ))}
                </ul>
              </div>
            )}
            {intent.references.length > 0 && (
              <div>
                <div className="section-label">{t("intent.references")}</div>
                <div data-testid="intent-references" className="stack-tight">
                  {intent.references.map((r: IntentRef, i) => (
                    <span key={i} className="small">
                      {isSafeLink(r.url) ? (
                        <a href={r.url} target="_blank" rel="noopener noreferrer">{r.label}</a>
                      ) : (
                        <span>{r.label}</span>
                      )}
                      <span className="faint"> · {r.url}</span>
                    </span>
                  ))}
                </div>
              </div>
            )}
            <div className="small muted">
              {intent.source.system && intent.source.ref && (
                <span>{t("intent.source", { system: intent.source.system, ref: intent.source.ref })}</span>
              )}
              {intent.run_ids?.length && (
                <span> · {t("intent.runs", { n: intent.run_ids.length })}</span>
              )}
            </div>
          </div>
        )}

        <form className="form" onSubmit={onSubmit}>
          <fieldset>
            <legend className="section-label">{t("intent.new")}</legend>
            <div className="chips" role="radiogroup" aria-label={t("intent.kind")}>
              {(["prompt", "reference", "spec"] as IntentKind[]).map((k) => (
                <button key={k} className="chip" role="radio" aria-checked={kind === k ? "true" : "false"}
                  onClick={() => setKind(k)} disabled={!writable}>
                  {t(`intent.kind.${k}`)}
                </button>
              ))}
            </div>
            {!writable && (
              <p className="small muted">{t("intent.readonly")}</p>
            )}
            <div className="field">
              <label htmlFor="intent-title">{t("intent.title")}</label>
              <input id="intent-title" value={title} onChange={(e) => setTitle(e.target.value)}
                disabled={!writable || sending} />
            </div>
            {kind === "prompt" && (
              <div className="field">
                <label htmlFor="intent-text">{t("intent.text")}</label>
                <textarea id="intent-text" rows={4} style={{ height: "auto", padding: "var(--space-2) var(--space-3)" }}
                  value={text} onChange={(e) => setText(e.target.value)} disabled={!writable || sending} />
              </div>
            )}
            {kind === "reference" && (
              <>
                <div className="field">
                  <label htmlFor="intent-system">{t("intent.system")}</label>
                  <select id="intent-system" value={refSystem} onChange={(e) => setRefSystem(e.target.value)}
                    disabled={!writable || sending}>
                    <option value="github">github</option>
                    <option value="jira">jira</option>
                    <option value="url">url</option>
                    <option value="file">file</option>
                  </select>
                </div>
                <div className="field">
                  <label htmlFor="intent-ref">{t("intent.ref")}</label>
                  <input id="intent-ref" value={refId} onChange={(e) => setRefId(e.target.value)}
                    disabled={!writable || sending} />
                </div>
              </>
            )}
            {kind === "spec" && (
              <div className="field">
                <label htmlFor="intent-spec">{t("intent.spec")}</label>
                <textarea id="intent-spec" rows={8} style={{ height: "auto", padding: "var(--space-2) var(--space-3)" }}
                  value={text} onChange={(e) => setText(e.target.value)} disabled={!writable || sending} />
              </div>
            )}
            {error && <p className="note-error small" role="alert">{error}</p>}
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={!writable || sending}>
                {sending ? t("intent.sending") : t("intent.submit")}
              </button>
            </div>
          </fieldset>
        </form>
      </div>
    </div>
  );
}

export function IntentBar() {
  const t = useT();
  const { workspace } = useWorkspace();
  const [open, setOpen] = useState(false);

  const statusRes = useResource<{ intent: string | null }>(factory(workspace.id, "/status"));
  const intentsRes = useResource<{ entries: Intent[] }>(factory(workspace.id, "/intents"));

  const currentIntent = intentsRes.data ? pickIntent(statusRes.data?.intent, intentsRes.data.entries) : null;

  return (
    <>
      <div
        className="top-bar"
        data-testid="intent-bar"
        style={{ cursor: "pointer" }}
        onClick={() => setOpen(true)}
        role="button"
        tabIndex={0}
        aria-label={t("intent.open")}
        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") setOpen(true); }}
      >
        <span className="source-badge">
          <Icon name="intent" size={14} />
          {currentIntent ? currentIntent.title : t("intent.none")}
        </span>
        {currentIntent && (
          <span className={`tag scope-${currentIntent.kind === "spec" ? "owner" : currentIntent.kind === "reference" ? "write" : "read"}`}>
            {currentIntent.kind}
          </span>
        )}
        <span className="top-bar-gap" />
        <span className="small muted">{t("intent.open")}</span>
      </div>
      {open && <IntentDrawer intent={currentIntent} onClose={() => setOpen(false)} />}
    </>
  );
}
