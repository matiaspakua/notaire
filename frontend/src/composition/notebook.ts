/**
 * Composition root for notebooks (#1833 / ADR-030).
 */
import { notebookHttpGateway } from "@/adapters/http/notebook/notebook-http-gateway";
import { createNotebookUseCases } from "@/core/notebook/application/use-cases";

export const notebookUseCases = createNotebookUseCases(notebookHttpGateway);
