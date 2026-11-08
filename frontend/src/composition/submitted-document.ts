/**
 * Composition root for submitted documents (#1841 / ADR-030).
 */
import { submittedDocumentHttpGateway } from "@/adapters/http/submitted-document/submitted-document-http-gateway";
import { createSubmittedDocumentUseCases } from "@/core/submitted-document/application/use-cases";

export const submittedDocumentUseCases = createSubmittedDocumentUseCases(submittedDocumentHttpGateway);
