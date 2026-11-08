/**
 * Protocol report download use cases (#1833 / ADR-030).
 */
import type { ProtocolReportGateway } from "./ports";

export function createProtocolReportUseCases(gateway: ProtocolReportGateway) {
  return {
    download: (path: string) => gateway.download(path),
  };
}

export type ProtocolReportUseCases = ReturnType<typeof createProtocolReportUseCases>;
