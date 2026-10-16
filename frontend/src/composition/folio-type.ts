/**
 * Composition root for the folio-type catalog (#1785 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { folioTypeHttpGateway } from "@/adapters/http/folio-type/folio-type-http-gateway";
import { createFolioTypeUseCases } from "@/core/folio-type/application/use-cases";

export const folioTypeUseCases = createFolioTypeUseCases(folioTypeHttpGateway);
