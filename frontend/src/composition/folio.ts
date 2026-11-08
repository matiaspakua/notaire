/**
 * Composition root for folio instance admin (#1785 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { folioHttpGateway } from "@/adapters/http/folio/folio-http-gateway";
import { createFolioUseCases } from "@/core/folio/application/use-cases";

export const folioUseCases = createFolioUseCases(folioHttpGateway);
