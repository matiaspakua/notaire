/**
 * HTTP adapter for protocol PDF reports (#1833 / ADR-030).
 */
import { apiGetBytes } from "@/lib/api-client";
import type { ProtocolReportGateway } from "@/core/protocol-report/application/ports";

export const protocolReportHttpGateway: ProtocolReportGateway = {
  download: async (path: string) => apiGetBytes(path),
};
