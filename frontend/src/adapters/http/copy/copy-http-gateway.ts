/**
 * HTTP adapter for copys (#1845 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Copy, CopyInput } from "@/core/copy/domain/copy";
import type { CopyGateway } from "@/core/copy/application/ports";

export const copyHttpGateway: CopyGateway = {
  list: () => apiGet<Copy[]>("/copies"),
  create: async (input: CopyInput) => { await apiPost<void>("/copies", input); },
  update: async (id, input) => { await apiPut<void>(`/copies/${id}`, input); },
  delete: async (id) => { await apiDelete(`/copies/${id}`); },
};
