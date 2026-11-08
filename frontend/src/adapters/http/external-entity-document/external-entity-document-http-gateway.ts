/**
 * HTTP adapter for external-entity documents (#1841 / ADR-030).
 */
import { apiGet, apiPut } from "@/lib/api-client";
import type {
  ExternalEntityDocumentMovementInput,
  ManagementExternalEntityDocuments,
} from "@/core/external-entity-document/domain/external-entity-document";
import type { ExternalEntityDocumentGateway } from "@/core/external-entity-document/application/ports";

export const externalEntityDocumentHttpGateway: ExternalEntityDocumentGateway = {
  getByManagement: (managementId: number) =>
    apiGet<ManagementExternalEntityDocuments>(
      `/managements/${managementId}/external-entity-documents`,
    ),
  registerMovement: (managementId, idSubmittedDocument, movement: ExternalEntityDocumentMovementInput) =>
    apiPut(
      `/managements/${managementId}/external-entity-documents/${idSubmittedDocument}`,
      movement,
    ),
};
