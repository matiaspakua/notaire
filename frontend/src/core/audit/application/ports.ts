import type { AuditPage, AuditPageRequest } from "../domain/audit";

export interface AuditGateway {
  listPage(request: AuditPageRequest): Promise<AuditPage>;
}
