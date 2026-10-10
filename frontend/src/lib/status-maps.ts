/**
 * Status vocabularies per domain (#1366, RF-23, RNF-09).
 *
 * One place maps a stored status to a tone (neutral | info | success | warning
 * | danger) and, where the vocabulary is fixed, an i18n key (relative to the
 * root of the messages). Management statuses are an admin catalog
 * (estados-gestion), so their label stays the stored name; only the seeded
 * names get a tone. Anything outside a vocabulary is neutral with its raw text.
 */
import { normalizeBudgetStatus, type BudgetStatusCode } from "@/lib/budget-status";

export type StatusTone = "neutral" | "info" | "success" | "warning" | "danger";

export type StatusDomain = "budget" | "management" | "testimony" | "folio" | "availability" | "user";

export interface ResolvedStatus {
  tone: StatusTone;
  /** Key from the messages root, or null to show `raw` as is. */
  labelKey: string | null;
  raw: string;
}

const BUDGET_TONES: Record<BudgetStatusCode, StatusTone> = {
  BORRADOR: "neutral",
  PENDIENTE: "warning",
  APROBADO: "success",
  RECHAZADO: "danger",
  FACTURADO: "info",
};

/** Seeded management statuses (V2, V41), compared without case or accents. */
const MANAGEMENT_TONES: Record<string, StatusTone> = {
  "iniciada": "info",
  "en tramite": "info",
  "documentacion completa": "success",
  "archivada": "neutral",
  "gestion modificada": "info",
  "gestion con escritura firmada": "success",
  "gestion con escritura sin firmar": "warning",
  "gestion con escritura anulada": "danger",
  "gestion con escritura no paso": "danger",
  "gestion con escritura inscripta": "success",
  "testimonio generado": "info",
  "testimonio ingresado a inscripcion": "info",
  "testimonio retirado": "success",
};

type FolioCode = "nuevo" | "utilizado" | "asignado" | "errose";

const FOLIO_CODES: Record<string, FolioCode> = {
  "nuevo": "nuevo",
  "utilizado": "utilizado",
  "asignado a cuaderno": "asignado",
  "errose": "errose",
};

const FOLIO_TONES: Record<FolioCode, StatusTone> = {
  nuevo: "info",
  utilizado: "neutral",
  asignado: "success",
  errose: "danger",
};

export type TestimonyStatus = "generated" | "verified" | "flagged";

const TESTIMONY: Record<TestimonyStatus, { tone: StatusTone; labelKey: string }> = {
  generated: { tone: "info", labelKey: "testimonios.estadoGenerado" },
  verified: { tone: "success", labelKey: "testimonios.estadoVerificado" },
  flagged: { tone: "warning", labelKey: "testimonios.estadoObservado" },
};

/** The derived state of a testimony: generated until verified, then verified or flagged. */
export function testimonyStatus(t: { verified?: boolean | null; flagged?: boolean | null }): TestimonyStatus {
  if (!t.verified) return "generated";
  return t.flagged ? "flagged" : "verified";
}

function fold(value: string): string {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim().replace(/\s+/g, " ").toLowerCase();
}

function folioCode(value: string): FolioCode | null {
  return FOLIO_CODES[fold(value).replace(/^folio /, "")] ?? null;
}

export function resolveStatus(domain: StatusDomain, value: string | boolean | null | undefined): ResolvedStatus {
  const raw = value == null || typeof value === "boolean" ? "" : String(value);
  const unknown: ResolvedStatus = { tone: "neutral", labelKey: null, raw };
  switch (domain) {
    case "budget": {
      const code = normalizeBudgetStatus(raw);
      return code ? { tone: BUDGET_TONES[code], labelKey: `presupuestos.status.${code}`, raw } : unknown;
    }
    case "management": {
      const tone = raw ? MANAGEMENT_TONES[fold(raw)] : undefined;
      return tone ? { tone, labelKey: null, raw } : unknown;
    }
    case "testimony": {
      const entry = TESTIMONY[raw as TestimonyStatus];
      return entry ? { ...entry, raw } : unknown;
    }
    case "folio": {
      const code = raw ? folioCode(raw) : null;
      return code ? { tone: FOLIO_TONES[code], labelKey: `status.folio.${code}`, raw } : unknown;
    }
    case "availability": {
      const inUse = raw ? folioCode(raw) === "utilizado" : false;
      return inUse
        ? { tone: "neutral", labelKey: "protocolo.inUse", raw }
        : { tone: "success", labelKey: "protocolo.available", raw };
    }
    case "user":
      return value === true
        ? { tone: "success", labelKey: "common.active", raw }
        : value === false
          ? { tone: "neutral", labelKey: "common.inactive", raw }
          : unknown;
  }
}
