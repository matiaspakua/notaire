import type { DocumentReentryGateway } from "./ports";
import type { DocumentReentryInput } from "../domain/document-reentry";

export function createDocumentReentryUseCases(gateway: DocumentReentryGateway) {
  return {
    getByManagement: (managementId: number) => gateway.getByManagement(managementId),
    reenter: (managementId: number, input: DocumentReentryInput) =>
      gateway.reenter(managementId, input),
  };
}

export type DocumentReentryUseCases = ReturnType<typeof createDocumentReentryUseCases>;
