/**
 * React Query hooks for CU43 — document reentry.
 * Hexagon wiring: #1841 / ADR-030.
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { documentReentryUseCases } from "@/composition/document-reentry";
import type {
  ManagementDocumentReentry as DomainReentry,
  ReenteredDocument as DomainReentered,
  DocumentReentryInput as DomainInput,
} from "@/core/document-reentry/domain/document-reentry";
import type {
  ReenteredDocument,
  ManagementDocumentReentry,
  DocumentReentryInput,
} from "@/types";

export const documentReentryKeys = {
  byManagement: (managementId: number) => ["documentReentry", "management", managementId] as const,
};

function toReentry(d: DomainReentry): ManagementDocumentReentry {
  return d as ManagementDocumentReentry;
}

function toReentered(d: DomainReentered): ReenteredDocument {
  return d;
}

interface ReenterDocumentationInput {
  managementId: number;
  reentry: DocumentReentryInput;
}

export function useDocumentReentry(managementId: number | undefined) {
  return useQuery({
    queryKey: documentReentryKeys.byManagement(managementId ?? 0),
    queryFn: async () => toReentry(await documentReentryUseCases.getByManagement(managementId!)),
    enabled: !!managementId,
  });
}

export function useReenterDocumentation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ managementId, reentry }: ReenterDocumentationInput) =>
      toReentered(await documentReentryUseCases.reenter(managementId, reentry as DomainInput)),
    onSuccess: (_data, { managementId }) =>
      qc.invalidateQueries({ queryKey: documentReentryKeys.byManagement(managementId) }),
  });
}
