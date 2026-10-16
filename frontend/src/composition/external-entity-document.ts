/**
 * Composition root for external-entity documents (#1841 / ADR-030).
 */
import { externalEntityDocumentHttpGateway } from "@/adapters/http/external-entity-document/external-entity-document-http-gateway";
import { createExternalEntityDocumentUseCases } from "@/core/external-entity-document/application/use-cases";

export const externalEntityDocumentUseCases = createExternalEntityDocumentUseCases(
  externalEntityDocumentHttpGateway,
);
