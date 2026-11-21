import type { ReactNode } from "react";

import type { ApiError } from "../api.ts";
import type { Resource } from "../hooks.ts";
import { hasKey, useLang, useT } from "../i18n.ts";
import { Icon, type IconName } from "./Icon.tsx";

const STATUS_ICON: Record<string, IconName> = {
  requested: "dot",
  queued: "clock",
  running: "runs",
  succeeded: "check",
  pass: "check",
  failed: "cross",
  fail: "cross",
  blocked: "alert",
  cancelled: "minus",
  pending: "pause",
  skip: "minus",
};

/** Status always carries an icon and a word, never color alone. */
export function StatusBadge({ status }: { status: string }) {
  const t = useT();
  const key = `status.${status}`;
  return (
    <span className={`status status-${status}`}>
      <Icon name={STATUS_ICON[status] ?? "dot"} size={14} />
      {hasKey(key) ? t(key) : status}
    </span>
  );
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
  children,
  id,
}: {
  title: string;
  actions?: ReactNode;
  children: ReactNode;
  id?: string;
}) {
  const headingId = id ?? `h-${title.replace(/\W+/g, "-").toLowerCase()}`;
  return (
    <section className="panel" aria-labelledby={headingId}>
      <header className="panel-head">
        <h2 id={headingId}>{title}</h2>
        {actions}
      </header>
      {children}
    </section>
  );
}
