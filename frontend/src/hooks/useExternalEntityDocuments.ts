/**
 * React Query hooks for CU10 — movements of external-entity documents.
 * Hexagon wiring: #1841 / ADR-030.
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { externalEntityDocumentUseCases } from "@/composition/external-entity-document";
import type {
  ManagementExternalEntityDocuments as DomainDocs,
  ExternalEntityDocumentMovementInput as DomainMovement,
} from "@/core/external-entity-document/domain/external-entity-document";
import type { ManagementExternalEntityDocuments, ExternalEntityDocumentMovementInput } from "@/types";

export const externalEntityDocumentKeys = {
  byManagement: (managementId: number) => ["externalEntityDocuments", "management", managementId] as const,
};

function toWire(d: DomainDocs): ManagementExternalEntityDocuments {
  return d as ManagementExternalEntityDocuments;
}

interface RegisterMovementInput {
  managementId: number;
  idSubmittedDocument: number;
  movement: ExternalEntityDocumentMovementInput;
}

export function useExternalEntityDocuments(managementId: number | undefined) {
  return useQuery({
    queryKey: externalEntityDocumentKeys.byManagement(managementId ?? 0),
    queryFn: async () =>
      toWire(await externalEntityDocumentUseCases.getByManagement(managementId!)),
    enabled: !!managementId,
  });
}

export function useRegisterExternalEntityDocumentMovement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ managementId, idSubmittedDocument, movement }: RegisterMovementInput) =>
      externalEntityDocumentUseCases.registerMovement(
        managementId,
        idSubmittedDocument,
        movement as DomainMovement,
      ),
    onSuccess: (_data, { managementId }) =>
      qc.invalidateQueries({ queryKey: externalEntityDocumentKeys.byManagement(managementId) }),
  });
}
