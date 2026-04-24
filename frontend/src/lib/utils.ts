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
