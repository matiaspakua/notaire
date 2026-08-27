import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

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

/** Get full name from Persona */
export function fullName(p?: {
  nombre?: string;
  apellido?: string;
} | null): string {
  if (!p) return "—";
  return [p.nombre, p.apellido].filter(Boolean).join(" ") || "—";
}

/**
 * Extract the server-side error message from a 400/409 API Error.
 * Returns the `message` field (populated by `GlobalExceptionHandler` for
 * `BusinessValidationException`/`ResourceNotFoundException`), falling back to
 * the legacy `error` field, or null when the status isn't 400/409 or the body
 * carries no parseable JSON.
 */
export function extractApiError(err: unknown): string | null {
  if (!(err instanceof Error)) return null;
  if (!err.message.includes("[400]") && !err.message.includes("[409]")) return null;
  try {
    const jsonStart = err.message.indexOf("{");
    if (jsonStart !== -1) {
      const body = JSON.parse(err.message.slice(jsonStart)) as { message?: string; error?: string };
      return body.message ?? body.error ?? null;
    }
  } catch {
    // message is not JSON — fall through
  }
  return null;
}
