/**
 * Auxiliary protocol domain (#1833 / ADR-030).
 * Folio rows reuse the folio domain shape; start-deed is a thin request.
 */
import type { Folio } from "@/core/folio/domain/folio";

export type AuxiliaryFolio = Folio;

export interface StartAuxiliaryDeedInput {
  idFolio: number;
  body: string;
}
