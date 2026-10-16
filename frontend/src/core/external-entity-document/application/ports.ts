import type {
  ExternalEntityDocumentMovementInput,
  ManagementExternalEntityDocuments,
} from "../domain/external-entity-document";

export interface ExternalEntityDocumentGateway {
  getByManagement(managementId: number): Promise<ManagementExternalEntityDocuments>;
  registerMovement(
    managementId: number,
    idSubmittedDocument: number,
    movement: ExternalEntityDocumentMovementInput,
  ): Promise<unknown>;
}
