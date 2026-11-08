/**
 * Composition root for auxiliary protocol (#1833 / ADR-030).
 */
import { auxiliaryProtocolHttpGateway } from "@/adapters/http/auxiliary-protocol/auxiliary-protocol-http-gateway";
import { createAuxiliaryProtocolUseCases } from "@/core/auxiliary-protocol/application/use-cases";

export const auxiliaryProtocolUseCases = createAuxiliaryProtocolUseCases(auxiliaryProtocolHttpGateway);
