import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import { ApiError } from "@/lib/api-client";

/** Merge Tailwind classes safely */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Format a date string to DD/MM/YYYY */
export function formatDate(dateStr?: string | null): string {
  if (!dateStr) return "—";
  try {
    return new Date(dateStr).toLocaleDateString("es-AR");
  } catch {
    return dateStr;
  }
}

/** Format a number as currency (ARS) */
export function formatCurrency(amount?: number | null): string {
  if (amount == null) return "—";
  return new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
  }).format(amount);
}

/** Get full name from Persona (firstName/lastName) or DtoPerson (name/lastName) */
export function fullName(p?: {
  firstName?: string;
  name?: string;
  lastName?: string;
} | null): string {
  if (!p) return "—";
  return [p.firstName ?? p.name, p.lastName].filter(Boolean).join(" ") || "—";
}

/** Statuses whose JSON bodies commonly carry a user-visible business message. */
const EXTRACTABLE_STATUSES = new Set([400, 404, 409, 422, 500]);

function parseErrorBody(body: string): string | null {
  try {
    const parsed = JSON.parse(body) as { message?: string; error?: string };
    return parsed.message ?? parsed.error ?? null;
  } catch {
    return null;
  }
}

/**
 * Extract the server-side error message from an API failure.
 * Prefers `ApiError.status` + `ApiError.body` (issue #1054). Falls back to
 * parsing the Error message string for legacy callers. Authenticated 401 is
 * excluded — session expiry (#1053) owns that UX.
 */
export function extractApiError(err: unknown): string | null {
  if (err instanceof ApiError) {
    if (err.status === 401) {
      return null;
    }
    // Prefer listed statuses; still surface any parseable body so we never swallow
    // server text when present (issue #1054).
    if (EXTRACTABLE_STATUSES.has(err.status) || err.body.trim().startsWith("{")) {
      return parseErrorBody(err.body);
    }
    return null;
  }
  if (!(err instanceof Error)) {
    return null;
  }
  const statusMatch = err.message.match(/^\[(\d{3})]/);
  if (statusMatch) {
    const status = Number(statusMatch[1]);
    if (status === 401 || !EXTRACTABLE_STATUSES.has(status)) {
      return null;
    }
  } else if (!err.message.includes("[400]") && !err.message.includes("[409]")) {
    return null;
  }
  const jsonStart = err.message.indexOf("{");
  if (jsonStart !== -1) {
    return parseErrorBody(err.message.slice(jsonStart));
  }
  return null;
}
