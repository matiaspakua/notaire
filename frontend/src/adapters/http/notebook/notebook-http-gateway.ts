/**
 * HTTP adapter for notebooks (#1833 / ADR-030).
 */
import { apiGet, apiGetBytes, apiPost } from "@/lib/api-client";
import type { Notebook, NotebookCreateInput } from "@/core/notebook/domain/notebook";
import type { NotebookGateway } from "@/core/notebook/application/ports";

export const notebookHttpGateway: NotebookGateway = {
  list: async () => apiGet<Notebook[]>("/notebooks"),
  create: async (input: NotebookCreateInput) => {
    await apiPost("/notebooks", input);
  },
  downloadCover: async (idNotebook: number) => apiGetBytes(`/notebooks/${idNotebook}/cover`),
};
