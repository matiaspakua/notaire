/**
 * Outbound port for notebooks (#1833 / ADR-030).
 */
import type { Notebook, NotebookCreateInput } from "../domain/notebook";

export interface NotebookGateway {
  list(): Promise<Notebook[]>;
  create(input: NotebookCreateInput): Promise<void>;
  downloadCover(idNotebook: number): Promise<Blob>;
}
