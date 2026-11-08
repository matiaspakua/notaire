/**
 * HTTP adapter for document reentry (#1841 / ADR-030).
 */
import { apiGet, apiPost } from "@/lib/api-client";
import type {
  DocumentReentryInput,
  ManagementDocumentReentry,
  ReenteredDocument,
} from "@/core/document-reentry/domain/document-reentry";
import type { DocumentReentryGateway } from "@/core/document-reentry/application/ports";

export const documentReentryHttpGateway: DocumentReentryGateway = {
  getByManagement: (managementId: number) =>
    apiGet<ManagementDocumentReentry>(`/managements/${managementId}/document-reentry`),
  reenter: (managementId: number, input: DocumentReentryInput) =>
    apiPost<ReenteredDocument>(`/managements/${managementId}/document-reentry`, input),
};
