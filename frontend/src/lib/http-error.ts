/**
 * Duck-typed reads of an ApiError so app/components need not import api-client (ADR-030).
 */
export function httpStatus(err: unknown): number | undefined {
  if (typeof err === "object" && err !== null && "status" in err) {
    const status = (err as { status: unknown }).status;
    return typeof status === "number" ? status : undefined;
  }
  return undefined;
}

export function errorBody(err: unknown): string {
  if (typeof err === "object" && err !== null && "body" in err) {
    const body = (err as { body: unknown }).body;
    return typeof body === "string" ? body : "";
  }
  return "";
}

/** Reads the backend's `message` field from a lockout response body, if present. */
export function lockoutMessage(body: string, fallback: string): string {
  try {
    const parsed = JSON.parse(body) as { message?: string };
    return parsed.message?.trim() || fallback;
  } catch {
    return fallback;
  }
}
