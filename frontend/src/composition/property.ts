/**
 * Composition root for propertys (#1845 / ADR-030).
 */
import { propertyHttpGateway } from "@/adapters/http/property/property-http-gateway";
import { createPropertyUseCases } from "@/core/property/application/use-cases";

export const propertyUseCases = createPropertyUseCases(propertyHttpGateway);
