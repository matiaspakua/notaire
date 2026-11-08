/**
 * Composition root for the management-status catalog (#1769 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { managementStatusHttpGateway } from "@/adapters/http/management-status/management-status-http-gateway";
import { createManagementStatusUseCases } from "@/core/management-status/application/use-cases";

export const managementStatusUseCases = createManagementStatusUseCases(
  managementStatusHttpGateway,
);
