import type { ExternalEntityDocumentGateway } from "./ports";
import type { ExternalEntityDocumentMovementInput } from "../domain/external-entity-document";

export function createExternalEntityDocumentUseCases(gateway: ExternalEntityDocumentGateway) {
  return {
    getByManagement: (managementId: number) => gateway.getByManagement(managementId),
    registerMovement: (
      managementId: number,
      idSubmittedDocument: number,
      movement: ExternalEntityDocumentMovementInput,
    ) => gateway.registerMovement(managementId, idSubmittedDocument, movement),
  };
}

export type ExternalEntityDocumentUseCases = ReturnType<typeof createExternalEntityDocumentUseCases>;
