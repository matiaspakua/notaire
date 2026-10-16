/**
 * Composition root for the document-type catalog (#1777 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { documentTypeHttpGateway } from "@/adapters/http/document-type/document-type-http-gateway";
import { createDocumentTypeUseCases } from "@/core/document-type/application/use-cases";

export const documentTypeUseCases = createDocumentTypeUseCases(documentTypeHttpGateway);
