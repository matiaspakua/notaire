/**
 * Notebook use cases (#1833 / ADR-030).
 */
import type { NotebookGateway } from "./ports";
import type { NotebookCreateInput } from "../domain/notebook";

export function createNotebookUseCases(gateway: NotebookGateway) {
  return {
    list: () => gateway.list(),
    create: (input: NotebookCreateInput) => gateway.create(input),
    downloadCover: (idNotebook: number) => gateway.downloadCover(idNotebook),
  };
}

export type NotebookUseCases = ReturnType<typeof createNotebookUseCases>;
