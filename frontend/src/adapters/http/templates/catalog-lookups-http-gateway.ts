/**
 * Thin catalog lookups for the templates admin page (#1788).
 * Procedure-type and concept full hexagons land in separate children; this adapter only lists.
 */
import { apiGet } from "@/lib/api-client";
import type { Concept, ProcedureType } from "@/types";

export const templatesCatalogLookups = {
  listProcedureTypes: () => apiGet<ProcedureType[]>("/procedure-types"),
  listConcepts: () => apiGet<Concept[]>("/concepts"),
};
