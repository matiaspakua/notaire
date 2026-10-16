/**
 * HTTP adapter for substitutions (#1845 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Substitution, SubstitutionInput } from "@/core/substitution/domain/substitution";
import type { SubstitutionGateway } from "@/core/substitution/application/ports";

export const substitutionHttpGateway: SubstitutionGateway = {
  list: () => apiGet<Substitution[]>("/substitutions"),
  create: async (input: SubstitutionInput) => { await apiPost<void>("/substitutions", input); },
  update: async (id, input) => { await apiPut<void>(`/substitutions/${id}`, input); },
  delete: async (id) => { await apiDelete(`/substitutions/${id}`); },
};
