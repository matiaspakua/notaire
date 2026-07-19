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
 * Extract the server-side error message from a 409 Conflict Error.
 * Returns the `error` field from the JSON body embedded in the message,
 * or null when the message isn't a 409 or carries no parseable JSON.
 */
export function extractApiError(err: unknown): string | null {
  if (!(err instanceof Error)) return null;
  if (!err.message.includes("[409]")) return null;
  try {
    const jsonStart = err.message.indexOf("{");
    if (jsonStart !== -1) {
      const body = JSON.parse(err.message.slice(jsonStart)) as { error?: string };
      return body.error ?? null;
    }
  } catch {
    // message is not JSON — fall through
  }
  return null;
}
