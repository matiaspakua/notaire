/**
 * Composition root for roles (#1843 / ADR-030).
 */
import { roleHttpGateway } from "@/adapters/http/role/role-http-gateway";
import { createRoleUseCases } from "@/core/role/application/use-cases";

export const roleUseCases = createRoleUseCases(roleHttpGateway);
