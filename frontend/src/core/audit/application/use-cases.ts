import type { AuditGateway } from "./ports";
import type { AuditPageRequest } from "../domain/audit";

export function createAuditUseCases(gateway: AuditGateway) {
  return {
    listPage: (request: AuditPageRequest) => gateway.listPage(request),
  };
}

export type AuditUseCases = ReturnType<typeof createAuditUseCases>;
