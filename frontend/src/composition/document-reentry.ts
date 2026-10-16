/**
 * Composition root for document reentry (#1841 / ADR-030).
 */
import { documentReentryHttpGateway } from "@/adapters/http/document-reentry/document-reentry-http-gateway";
import { createDocumentReentryUseCases } from "@/core/document-reentry/application/use-cases";

export const documentReentryUseCases = createDocumentReentryUseCases(documentReentryHttpGateway);
