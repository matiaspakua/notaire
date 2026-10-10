/**
 * Generated OpenAPI schema aliases (#1260 / #1197 P0.5).
 * Source of truth: `npm run openapi:types` → `api.generated.ts`.
 */
import type { components } from "@/types/api.generated";

export type ApiSchemas = components["schemas"];

/** GET /gestiones page content — DtoManagementSummary. */
export type GestionDeEscritura = ApiSchemas["DtoManagementSummary"];

/** GET /presupuestos page/detail — BudgetResponse. */
export type Presupuesto = ApiSchemas["BudgetResponse"];

/** GET /presupuestos/{id}/resumen — DtoBudgetResumen. */
export type PresupuestoResumen = ApiSchemas["DtoBudgetResumen"];

/** GET /documento-presentado item — SubmittedDocumentResponse. */
export type DocumentoPresentado = ApiSchemas["SubmittedDocumentResponse"];

/** POST /documento-presentado body. */
export type DocumentoPresentadoRequest = ApiSchemas["SubmittedDocumentCreateRequest"];

/** PUT /documento-presentado/{id} body. */
export type DocumentoPresentadoUpdateRequest = ApiSchemas["SubmittedDocumentRequest"];

/** GET /gestiones/{id}/saldo-pendiente. */
export type DtoSaldoPendiente = ApiSchemas["DtoSaldoPending"];
