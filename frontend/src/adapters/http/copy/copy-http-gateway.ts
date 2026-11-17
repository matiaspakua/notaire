/**
 * HTTP adapter for copys (#1845 / ADR-030).
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import type { Copy, CopyInput } from "@/core/copy/domain/copy";
import type { CopyGateway, CopyPage, CopyPageRequest } from "@/core/copy/application/ports";

export const copyHttpGateway: CopyGateway = {
  list: () => apiGet<Copy[]>("/copies"),
  listPage: async (request: CopyPageRequest): Promise<CopyPage> => {
    const page = await apiGetPage<Copy>("/copies", {
      page: request.page,
      size: request.size,
      sort: request.sort,
    });
    return {
      content: page.content,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
  create: async (input: CopyInput) => { await apiPost<void>("/copies", input); },
  update: async (id, input) => { await apiPut<void>(`/copies/${id}`, input); },
  delete: async (id) => { await apiDelete(`/copies/${id}`); },
};
