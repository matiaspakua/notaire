/**
 * Composition root for managements (#1839 / ADR-030).
 */
import { managementHttpGateway } from "@/adapters/http/management/management-http-gateway";
import { createManagementUseCases } from "@/core/management/application/use-cases";

export const managementUseCases = createManagementUseCases(managementHttpGateway);
