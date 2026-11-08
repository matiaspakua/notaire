/**
 * HTTP adapter for audit log (#1843 / ADR-030).
 */
import { apiGetPage } from "@/lib/api-client";
import type { AuditPage, AuditPageRequest, AuditRecord } from "@/core/audit/domain/audit";
import type { AuditGateway } from "@/core/audit/application/ports";

export const auditHttpGateway: AuditGateway = {
  listPage: async (request: AuditPageRequest): Promise<AuditPage> => {
    const page = await apiGetPage<AuditRecord>("/audit-log", {
      page: request.page,
      size: request.size,
      sort: "date,desc",
      params: {
        module: request.module,
        idUser: request.idUser,
        from: request.from,
        to: request.to,
      },
    });
    return {
      content: page.content,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
};
