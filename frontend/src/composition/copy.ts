/**
 * Composition root for copys (#1845 / ADR-030).
 */
import { copyHttpGateway } from "@/adapters/http/copy/copy-http-gateway";
import { createCopyUseCases } from "@/core/copy/application/use-cases";

export const copyUseCases = createCopyUseCases(copyHttpGateway);
