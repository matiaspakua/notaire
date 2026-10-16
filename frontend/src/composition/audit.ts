/**
 * Composition root for audit (#1843 / ADR-030).
 */
import { auditHttpGateway } from "@/adapters/http/audit/audit-http-gateway";
import { createAuditUseCases } from "@/core/audit/application/use-cases";

export const auditUseCases = createAuditUseCases(auditHttpGateway);
