import type { ReactNode } from "react";

import type { ApiError } from "../api.ts";
import type { Resource } from "../hooks.ts";
import { hasKey, useLang, useT } from "../i18n.ts";
import { Icon, type IconName } from "./Icon.tsx";

/** Run, step and gate states map onto the five station tones of the production line. */
const TONE: Record<string, "idle" | "running" | "done" | "error" | "waiting"> = {
  requested: "idle",
  queued: "idle",
  pending: "idle",
  running: "running",
  succeeded: "done",
  pass: "done",
  failed: "error",
  fail: "error",
  blocked: "waiting",
  cancelled: "idle",
  skip: "idle",
};

export function toneOf(status: string) {
  return TONE[status] ?? "idle";
}

/** The word always names the state; the tint only reinforces it. */
export function StatusBadge({ status }: { status: string }) {
  const t = useT();
  const key = `status.${status}`;
  return <span className={`status-pill ${toneOf(status)}`}>{hasKey(key) ? t(key) : status}</span>;
}

export function time(value: string | Date | undefined, lang: string): string {
  if (!value) {
    return "—";
  }
  const date = typeof value === "string" ? new Date(value) : value;
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return date.toLocaleString(lang, { dateStyle: "medium", timeStyle: "short" });
}

export function ErrorNote({ error, onRetry }: { error: ApiError; onRetry?: () => void }) {
  const t = useT();
  const key = `error.${error.code}`;
  const forbidden = error.status === 403;
  return (
    <div className={`note ${forbidden ? "note-lock" : "note-error"}`} role="alert">
      <Icon name={forbidden ? "lock" : "alert"} />
      <div>
        <p className="note-title">{hasKey(key) ? rich(t(key)) : t("error.generic")}</p>
        {!hasKey(key) && <p className="note-detail">{error.detail}</p>}
        {onRetry && error.status !== 403 && (
          <button type="button" className="btn btn-quiet" onClick={onRetry}>
            <Icon name="refresh" size={16} />
            {t("state.retry")}
          </button>
        )}
      </div>
    </div>
  );
}

export function Skeleton({ rows = 3 }: { rows?: number }) {
  const t = useT();
  return (
    <div className="skeleton" role="status" aria-busy="true" aria-label={t("state.loading")}>
      {Array.from({ length: rows }, (_, i) => (
        <span key={i} className="skeleton-row" />
      ))}
    </div>
  );
}

/** Copy names commands in backticks; render those spans as code. */
export function rich(text: string): ReactNode {
  return text.split("`").map((part, index) => (index % 2 === 1 ? <code key={index}>{part}</code> : part));
}

export function Empty({ children }: { children: ReactNode }) {
  return <p className="empty">{typeof children === "string" ? rich(children) : children}</p>;
}

/** Page-level empty state: an icon, what is missing, and what to do. */
export function EmptyState({ icon, title, children }: { icon: IconName; title: string; children?: ReactNode }) {
  return (
    <div className="empty-state">
      <Icon name={icon} size={32} />
      <p className="empty-state-title">{title}</p>
      {children && <p>{typeof children === "string" ? rich(children) : children}</p>}
    </div>
  );
}

/** Loading, error, stale and data states for one resource, the same way on every screen. */
export function Loaded<T>({
  resource,
  rows,
  children,
}: {
  resource: Resource<T>;
  rows?: number;
  children: (data: T) => ReactNode;
}) {
  const t = useT();
  const lang = useLang();
  if (resource.data === undefined) {
    if (resource.error) {
      return <ErrorNote error={resource.error} onRetry={resource.reload} />;
    }
    return <Skeleton rows={rows} />;
  }
  return (
    <>
      {resource.stale && resource.updatedAt && (
        <p className="stale" role="status">
          <Icon name="clock" size={16} />
          {t("state.stale", { time: time(resource.updatedAt, lang) })}
        </p>
      )}
      {children(resource.data)}
    </>
  );
}

export function Section({
  title,
  actions,
  hint,
  children,
  id,
}: {
  title: string;
  actions?: ReactNode;
  hint?: string;
  children: ReactNode;
  id?: string;
}) {
  const headingId = id ?? `h-${title.replace(/\W+/g, "-").toLowerCase()}`;
  return (
    <section className="card" aria-labelledby={headingId}>
      <header className="card-head">
        <h2 className="card-title" id={headingId}>{title}</h2>
        {actions}
      </header>
      {hint && <p className="card-hint">{rich(hint)}</p>}
      {children}
    </section>
  );
}

export function Kpi({ label, value, sub, tone }: { label: string; value: ReactNode; sub?: ReactNode; tone?: string }) {
  return (
    <div className={`kpi-card${tone ? ` ${tone}` : ""}`}>
      <span className="kpi-label">{label}</span>
      <span className="kpi-value">{value}</span>
      {sub !== undefined && <span className="kpi-subtitle">{sub}</span>}
    </div>
  );
}

/** A labelled band on the canvas, like a phase of the production line; KPI rows sit in it without a card. */
export function Group({ title, tone = "", meta, actions, children }: {
  title: string;
  tone?: string;
  meta?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
}) {
  const headingId = `g-${title.replace(/\W+/g, "-").toLowerCase()}`;
  return (
    <section className="phase-group" aria-labelledby={headingId}>
      <header className="phase-header">
        <span className={`phase-indicator ${tone}`} aria-hidden="true" />
        <h2 className="phase-name" id={headingId}>{title}</h2>
        {meta !== undefined && <span className="phase-meta">{meta}</span>}
        {actions}
      </header>
      {children}
    </section>
  );
}
