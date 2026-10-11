/**
 * Generated OpenAPI schema aliases (#1260 / #1197 P0.5).
 * Source of truth: `npm run openapi:types` → `api.generated.ts`.
 */
import type { components } from "@/types/api.generated";

export type ApiSchemas = components["schemas"];

/** GET /managements page content — DtoManagementSummary. */
export type DeedManagement = ApiSchemas["DtoManagementSummary"];

/** GET /budgets page/detail — BudgetResponse. */
export type Budget = ApiSchemas["BudgetResponse"];

/** GET /budgets/{id}/summary — DtoBudgetResumen. */
export type BudgetSummary = ApiSchemas["DtoBudgetResumen"];

/** GET /submitted-documents item — SubmittedDocumentResponse. */
export type SubmittedDocument = ApiSchemas["SubmittedDocumentResponse"];

/** POST /submitted-documents body. */
export type SubmittedDocumentRequest = ApiSchemas["SubmittedDocumentCreateRequest"];

/** PUT /submitted-documents/{id} body. */
export type SubmittedDocumentUpdateRequest = ApiSchemas["SubmittedDocumentRequest"];

/** GET /managements/{id}/outstanding-balance. */
export type DtoOutstandingBalance = ApiSchemas["DtoSaldoPending"];
