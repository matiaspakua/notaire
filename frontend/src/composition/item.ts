/**
 * Composition root for items (#1845 / ADR-030).
 */
import { itemHttpGateway } from "@/adapters/http/item/item-http-gateway";
import { createItemUseCases } from "@/core/item/application/use-cases";

export const itemUseCases = createItemUseCases(itemHttpGateway);
