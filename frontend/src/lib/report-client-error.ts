/**
 * Best-effort browser helper that POSTs a validated client error to the beacon.
 * Never throws; failures are swallowed so callers stay non-blocking (#1483).
 */

export type ReportableClientError = {
  level?: "ERROR" | "WARN";
  message: string;
  name?: string;
  path?: string;
  stack?: string;
};

export function reportClientError(error: ReportableClientError): void {
  if (typeof window === "undefined" || typeof fetch !== "function") {
    return;
  }

  const body = {
    level: error.level ?? "ERROR",
    message: String(error.message ?? "").slice(0, 1024),
    ...(error.name ? { name: String(error.name).slice(0, 128) } : {}),
    ...(error.path
      ? { path: String(error.path).split("?", 1)[0].slice(0, 256) }
      : { path: window.location?.pathname?.slice(0, 256) }),
    ...(error.stack ? { stack: String(error.stack).slice(0, 4096) } : {}),
  };

  try {
    void fetch("/api/client-errors", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(body),
      keepalive: true,
      credentials: "same-origin",
    }).catch(() => undefined);
  } catch {
    // swallow
  }
}
